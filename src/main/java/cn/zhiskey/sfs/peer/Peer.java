package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.message.Message;
import cn.zhiskey.sfs.network.Route;
import cn.zhiskey.sfs.network.RouteList;
import cn.zhiskey.sfs.utils.FileUtil;
import cn.zhiskey.sfs.utils.hash.HashIDUtil;
import cn.zhiskey.sfs.utils.hash.HashUtil;
import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.XMLUtil;
import cn.zhiskey.sfs.utils.udpsocket.UDPRecvLoopThread;
import cn.zhiskey.sfs.utils.udpsocket.UDPSocket;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.Base64;
import java.util.Random;
import java.util.Scanner;

/**
 * P2P节点类 TODO
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Peer {

    private byte[] hashID = null;

    private RouteList routeList;

    /**
     * 生成一个随机的HashID<br>
     * 基于时间戳、MAC地址、随机数生成
     *
     * @return byte[] 生成对HashID
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private byte[] createHashID() {
        StringBuilder seed = new StringBuilder();

        // 时间戳
        long timeStamp = System.currentTimeMillis();
        seed.append(timeStamp);

        // MAC地址
        try {
            byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
            seed.append(HashUtil.bytes2Hex(mac));
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        // 随机数
        seed.append(new Random().nextInt());

        // 生成哈希ID
        String hashType = ConfigUtil.getInstance().get("hashType", "SHA-256");
        return HashUtil.getHash(seed.toString(), hashType);
    }

    private void joinNetWork(String seedPeerHost, String seedPeerHashID) {
        initPeer();

        // 初始化路由表 TODO
        routeList = new RouteList();

//        Message msg = new Message("FIND_NODE");
//        msg.put("hashID", Base64.getEncoder().encodeToString(hashID));
//        msg.put("host", "localhost");
//
//        String data = msg.toJSONString();
//        System.out.println(data);
//        Message msg1 = Message.parseByJSON(data);
//        System.out.println(msg1.getType());
//        String res = msg1.getString("abc");
//        String res1 = (String) msg1.get("host");
//        System.out.println(res);
//        System.out.println(res1);

        // 启动消息接收循环 TODO
        new UDPRecvLoopThread(UDPSocket.getCommonRecvPort(), datagramPacket -> {
            Message msg = UDPRecvLoopThread.getMessage(datagramPacket);
            System.out.println(msg.getType());
            System.out.println(datagramPacket.getAddress());
        }).start();

        if(!seedPeerHost.equals("null")) {
            // 将种子节点加入路由表
            byte[] seedPeerHashIDBytes = Base64.getDecoder().decode(seedPeerHashID);
            routeList.add(new Route(seedPeerHashIDBytes, seedPeerHost));
            // 通知种子节点自己加入
            Message msg = new Message("FIND_NODE");
            msg.put("hashID", Base64.getEncoder().encodeToString(hashID));
            UDPSocket.send(seedPeerHost, UDPSocket.getCommonRecvPort(), msg);
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
            if (!peerXMLFile.getParentFile().exists()) {
                boolean mkdirsRes = peerXMLFile.getParentFile().mkdirs();
                if (!mkdirsRes) {
                    new IOException("Can not create data folder!").printStackTrace();
                }
            }
            document = XMLUtil.create();
            newPeer(document, peerXMLFile);
        }
        // 设置自己的hashID，方便后面计算距离
        HashIDUtil.getInstance().setSelfHashID(hashID);
    }

    private void initPeerData(Document document) {
        Element elementPeer = document.getDocumentElement();
        hashID = Base64.getDecoder().decode(elementPeer.getAttribute("hashID"));
    }

    private void newPeer(Document document, File peerXMLFile) {
        hashID = createHashID();

        Element elementPeer = document.createElement("peer");
        elementPeer.setAttribute("hashID", Base64.getEncoder().encodeToString(hashID));
        document.appendChild(elementPeer);

        XMLUtil.save(document, peerXMLFile);
    }

    public static void main(String[] args) {
        // 载入配置文件
        try {
            ConfigUtil.getInstance().load(FileUtil.getResourcesPath() + "configs/config.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner((System.in));

        Peer peer = new Peer();
        peer.joinNetWork("localhost", "");
    }
}
