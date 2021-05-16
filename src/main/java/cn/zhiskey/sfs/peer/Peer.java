package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.message.Message;
import cn.zhiskey.sfs.message.MessageHandler;
import cn.zhiskey.sfs.network.Route;
import cn.zhiskey.sfs.network.RouteList;
import cn.zhiskey.sfs.utils.FileUtil;
import cn.zhiskey.sfs.utils.MacUtil;
import cn.zhiskey.sfs.utils.hash.HashIDUtil;
import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.XMLUtil;
import cn.zhiskey.sfs.network.udpsocket.spark.SparkRecvLoopThread;
import cn.zhiskey.sfs.network.udpsocket.UDPRecvLoopThread;
import cn.zhiskey.sfs.network.udpsocket.UDPSocket;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * P2P节点类
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Peer {
    /**
     * 节点的HashID
     */
    private byte[] hashID = null;

    /**
     * 节点的路由表
     */
    private final RouteList routeList = new RouteList();

    /**
     * 节点存储的Spark文件列表
     */
    private final List<String> sparkFileList = new ArrayList<>();

    /**
     * 节点状态
     */
    private PeerStatus status = PeerStatus.START;

    /**
     * 生成一个随机的HashID<br>
     * 基于时间戳、MAC地址、随机数生成
     *
     * @return byte[] 生成对HashID
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private static byte[] createHashID() {
        StringBuilder seed = new StringBuilder();

        // 时间戳
        long timeStamp = System.currentTimeMillis();
        seed.append(timeStamp);

        // MAC地址
        seed.append(MacUtil.getMacId());

        // 随机数
        seed.append(new Random().nextInt());

        // 生成哈希ID
        return HashIDUtil.getHashID(seed.toString());
    }

    /**
     * 将节点加入网络，需要提供种子节点<br>
     * 会尝试与种子节点通信，并获取种子节点的hashID
     *
     * @param seedPeerHost 种子节点Host，网络中第一个节点的种子节点是他自己：localhost
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void joinNetWork(String seedPeerHost) {
        initPeer();

        // 启动消息接收循环
        new UDPRecvLoopThread(UDPSocket.getCommonRecvPort(), datagramPacket -> {
            MessageHandler messageHandler = new MessageHandler(this);
            messageHandler.handle(datagramPacket);
        }).start();

        // 启动文件接收循环
        new SparkRecvLoopThread(UDPSocket.getSparkRecvPort(), this).start();

        // 获取种子节点hashID
        Message msg = new Message("GetHashID");
        UDPSocket.send(seedPeerHost, msg);
        status = PeerStatus.WAIT_SEED_HASH_ID;
    }

    /**
     * 将文件制作为Spark种子并在网络上传播
     *
     * @param filePath 文件路径
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void makeSpark(String filePath) {
        List<String> list = new ArrayList<>();
        try {
            list = FileUtil.makeSpark(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int sparkBakCount = Integer.parseInt(ConfigUtil.getInstance().get("sparkBakCount"));
        // 向网络中节点发送spark
        for (String hashID : list) {
            sparkFileList.add(hashID);
            List<Route> resList = routeList.searchFromRouteList(hashID, sparkBakCount);
            // 去掉自己
            resList.removeIf(route -> route.equalsByHashID(getHashID()));
            SparkRecvLoopThread.sendSpark(resList, hashID, sparkBakCount, sparkFileList);
        }
        System.out.println("Make " + list.get(0) + " finished!");
    }

    /**
     * 通过Spark种子下载文件
     *
     * @param seedSparkHashID Spark种子的HashID
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void download(String seedSparkHashID) {
        // 下载seedSpark文件
        if(getSparkFileList().contains(seedSparkHashID)) {
            SparkRecvLoopThread.askForSparkBySeedFile(seedSparkHashID, this);
            return;
        }
        int sparkBakCount = Integer.parseInt(ConfigUtil.getInstance().get("sparkBakCount"));
        List<Route> resList = routeList.searchFromRouteList(seedSparkHashID, sparkBakCount);

        for (Route route : resList) {
            // 节点索要spark文件
            Message msg = new Message("AskForSpark");
            msg.put("sparkHashID", seedSparkHashID);
            msg.put("isSeed", "true");
            UDPSocket.send(route.getHost(), msg);
        }
    }

    /**
     * 初始化节点<br>
     * 从XML文件读出节点数据或是新建一个节点
     *
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void initPeer() {
        String path = FileUtil.getResourcesPath() + ConfigUtil.getInstance().get("peerDataPath");
        File peerXMLFile = new File(path);
        Document document;
        if (peerXMLFile.exists()) {
            document = XMLUtil.parse(peerXMLFile);
            initPeerData(document);
        } else {
            newPeer();
        }
        // 设置自己的hashID，方便后面计算前缀长
        HashIDUtil.getInstance().setSelfHashID(hashID);
    }

    /**
     * 初始化节点的数据<br>
     * 读取之前存储的数据XML文件，并恢复节点sparkFileList
     *
     * @param document XML文件的document对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void initPeerData(Document document) {
        Element elementPeer = document.getDocumentElement();
        hashID = HashIDUtil.toBytes(elementPeer.getAttribute("hashID"));
        // 读取spark列表
        NodeList nodeListSparks = elementPeer.getElementsByTagName("sparks");
        for (int i = 0; i < nodeListSparks.getLength(); i++) {
            NodeList nodeListSpark = ((Element)nodeListSparks.item(i)).getElementsByTagName("spark");
            for (int j = 0; j < nodeListSpark.getLength(); j++) {
                sparkFileList.add(nodeListSpark.item(j).getTextContent());
            }
        }
    }

    /**
     * 创建新节点，生成节点的HashID
     *
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void newPeer() {
        hashID = createHashID();
    }

    /**
     * 关闭节点<br>
     * 如果节点正在正常运行<br>
     * 将节点的数据存储在XML数据文件里
     *
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void close() {
        if (status == PeerStatus.RUNNING) {
            saveData();
        }
    }

    /**
     * 将节点的数据存储在XML数据文件里
     *
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void saveData() {
        String path = FileUtil.getResourcesPath() + ConfigUtil.getInstance().get("peerDataPath");
        File peerXMLFile = new File(path);
        FileUtil.makeParentFolder(peerXMLFile);
        Document document = XMLUtil.create();

        Element elementPeer = document.createElement("peer");

        // 存储hashID
        elementPeer.setAttribute("hashID", HashIDUtil.toString(hashID));
        document.appendChild(elementPeer);

        // 存储spark列表
        Element elementSparks = document.createElement("sparks");
        for (String sparkFileHashID : sparkFileList) {
            Element elementSpark = document.createElement("spark");
            elementSpark.setTextContent(sparkFileHashID);
            elementSparks.appendChild(elementSpark);
        }
        elementPeer.appendChild(elementSparks);

        // 保存
        XMLUtil.save(document, peerXMLFile);
    }

    /**
     * 获取本节点HashID
     *
     * @return byte[] 本节点HashID
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public byte[] getHashID() {
        return hashID;
    }

    /**
     * 获取本节点HashID字符串
     *
     * @return java.lang.String 本节点HashID字符串
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public String getHashIDString() {
        return HashIDUtil.toString(hashID);
    }

    /**
     * 获取节点路由表
     *
     * @return cn.zhiskey.sfs.network.RouteList 节点路由表
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public RouteList getRouteList() {
        return routeList;
    }

    /**
     * 获取本节点Spark文件列表
     *
     * @return java.util.List<java.lang.String> 本节点Spark文件列表
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public List<String> getSparkFileList() {
        return sparkFileList;
    }

    /**
     * 获取节点状态
     *
     * @return cn.zhiskey.sfs.peer.PeerStatus 节点状态
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public PeerStatus getStatus() {
        return status;
    }

    /**
     * 设置节点状态
     *
     * @param status 新的节点状态
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void setStatus(PeerStatus status) {
        this.status = status;
    }

    /**
     * 显示节点路由表
     *
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void showRouteList() {
        routeList.show();
    }

    /**
     * 程序入口主函数
     *
     * @param args 外部输入参数
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void main(String[] args) {
        // 载入配置文件
        try {
            ConfigUtil.getInstance().load(FileUtil.getResourcesPath() + "configs/config.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner((System.in));

        System.out.println("Input seed peer host:");
        Peer peer = new Peer();
        peer.joinNetWork(scanner.next());

        String op = scanner.next();
        while (!op.equals("exit")) {
            switch (op) {
                case "make":
                    peer.makeSpark(scanner.next());
                    break;
                case "down":
                    peer.download(scanner.next());
                    break;
                case "route":
                    peer.showRouteList();
                    break;
                default:
                    System.out.println("Unrecognized!");
                    break;
            }
            op = scanner.next();
        }
        peer.close();
        System.exit(0);
    }
}

// D:/apache-maven-3.8.1-bin.zip
