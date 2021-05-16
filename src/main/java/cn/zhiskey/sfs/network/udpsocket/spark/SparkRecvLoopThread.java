package cn.zhiskey.sfs.network.udpsocket.spark;

import cn.zhiskey.sfs.message.Message;
import cn.zhiskey.sfs.network.Route;
import cn.zhiskey.sfs.peer.Peer;
import cn.zhiskey.sfs.peer.PeerStatus;
import cn.zhiskey.sfs.utils.BytesUtil;
import cn.zhiskey.sfs.utils.FileUtil;
import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.hash.HashIDUtil;
import cn.zhiskey.sfs.network.udpsocket.UDPSocket;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.List;

/**
 * 接收Spark文件消息的循环线程<br>
 * 不同于UDPRecvLoopThread，没有采用策略模式
 * TODO 重构为策略模式
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class SparkRecvLoopThread extends Thread {
    /**
     * 要接收消息的本地节点对象，用于获取/更新节点状态
     */
    Peer peer;

    /**
     * UDP Socket对象
     */
    private DatagramSocket datagramSocket = null;

    /**
     * 接收缓冲区大小<br>
     * 单位：字节<br>
     * 值为1024 * 63<br>
     * 小于UDP单个数据包最大尺寸
     */
    public static final int BUFF_SIZE = 1024 * 63;

    /**
     * 构造方法<br>
     * 初始化UDP Socket对象
     *
     * @param recvPort 接收消息的端口
     * @param peer 要接收消息的本地节点对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public SparkRecvLoopThread(int recvPort, Peer peer) {
        this.peer = peer;
        // 创建Socket对象
        try {
            datagramSocket = new DatagramSocket(recvPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**
     * 重写线程的run方法，实现循环接收消息的逻辑<br>
     * 通过消息的SparkDataType将消息交由不同的方法处理
     *
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    @Override
    public void run() {
        while (peer.getStatus() != PeerStatus.CLOSED) {
            byte[] preData = new byte[BUFF_SIZE];
            DatagramPacket datagramPacket = new DatagramPacket(preData,preData.length);
            try {
                datagramSocket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 文件hashID的长度
            int hashIDSize = Integer.parseInt(ConfigUtil.getInstance().get("hashIDSize"));

            byte[] data = datagramPacket.getData();
            byte[] dataTypeBytes = new byte[BytesUtil.INT_BYTES_SIZE];
            System.arraycopy(data, 0, dataTypeBytes, 0, BytesUtil.INT_BYTES_SIZE);
            byte[] hashID = new byte[hashIDSize];
            System.arraycopy(data, BytesUtil.INT_BYTES_SIZE, hashID, 0, hashIDSize);
            byte[] fileLengthBytes = new byte[BytesUtil.INT_BYTES_SIZE];
            System.arraycopy(data, BytesUtil.INT_BYTES_SIZE + hashIDSize,
                    fileLengthBytes, 0, BytesUtil.INT_BYTES_SIZE);

            System.out.println("\trecv " + HashIDUtil.toString(hashID) + " " + datagramPacket.getAddress().getHostAddress());

            SparkDataType dataType = SparkDataType.values()[BytesUtil.bytes2Int(dataTypeBytes)];
            int fileLength = BytesUtil.bytes2Int(fileLengthBytes);
            String hashIDStr = HashIDUtil.toString(hashID);

            byte[] fileData = new byte[fileLength];
            System.arraycopy(data, BytesUtil.INT_BYTES_SIZE + hashIDSize + BytesUtil.INT_BYTES_SIZE,
                    fileData, 0, fileLength);

            switch (dataType) {
                case PUSH_SPARK:
                    pushSpark(hashIDStr, fileData, datagramPacket.getAddress().getHostAddress());
                    break;
                case DOWN_SEED_SPARK:
                    downSeedSpark(hashIDStr, fileData);
                    break;
                case DOWN_SPARK:
                    downSpark(hashIDStr, fileData);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 收到Spark推送消息
     *
     * @param hashIDStr Spark的HashID
     * @param fileData Spark文件的数据字节数组
     * @param fromHost 消息来路主机
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void pushSpark(String hashIDStr, byte[] fileData, String fromHost) {
        // 如果本地已有该spark，就不转发，防止消息风暴
        if(!peer.getSparkFileList().contains(hashIDStr)) {
            saveSpark(hashIDStr, fileData);
            peer.getSparkFileList().add(hashIDStr);
            int sparkBakCount = Integer.parseInt(ConfigUtil.getInstance().get("sparkBakCount"));
            // 向网络中节点发送spark
            List<Route> resList = peer.getRouteList().searchFromRouteList(hashIDStr, sparkBakCount);
            // 去掉自己
            resList.removeIf(route -> route.equalsByHashID(peer.getHashID()));
            // 去掉发送的源节点
            Route formRoute = peer.getRouteList().getRouteByHost(fromHost);
            if(formRoute != null) {
                resList.removeIf(route -> route.equalsByHashID(formRoute.getHashID()));
            }
            SparkRecvLoopThread.sendSpark(resList, hashIDStr, sparkBakCount, peer.getSparkFileList());
        }
    }

    /**
     * 收到Spark种子文件下载响应
     *
     * @param seedHashID Spark种子文件的HashID
     * @param fileData Spark文件的数据字节数组
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void downSeedSpark(String seedHashID, byte[] fileData) {
        // 如果本地已有该spark，就不保存
        if(!peer.getSparkFileList().contains(seedHashID)) {
            saveSpark(seedHashID, fileData);
            peer.getSparkFileList().add(seedHashID);
            askForSparkBySeedFile(seedHashID, peer);
        }
    }

    /**
     * 收到Spark文件下载响应
     *
     * @param hashIDStr Spark的HashID
     * @param fileData Spark文件的数据字节数组
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void downSpark(String hashIDStr, byte[] fileData) {
        // 如果本地已有该spark，就不保存
        if(!peer.getSparkFileList().contains(hashIDStr)) {
            saveSpark(hashIDStr, fileData);
            peer.getSparkFileList().add(hashIDStr);

            String seedHashID = TempSparkList.getInstance().check(peer.getSparkFileList());
            if(seedHashID != null) {
                String path = FileUtil.recoverSpark(seedHashID);
                System.out.println("Down " + seedHashID + " " + "finished!");
                System.out.println("Path: " + path);
            }
        }
    }

    /**
     * 向路由列表中的各个路由发送Spark文件，并且如果本节点不是离Spark较近的节点，删除本地的Spark文件
     *
     * @param routeList 路由列表
     * @param hashID Spark的HashID
     * @param count 文件存在的最大备份份数
     * @param sparkFileList 本节点Spark文件表
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void sendSpark(List<Route> routeList, String hashID, int count, List<String> sparkFileList) {
        File file = FileUtil.getSparkFile(hashID);
        for (Route route : routeList) {
            try {
                UDPSocket.send(route.getHost(), file, SparkDataType.PUSH_SPARK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 本地的文件是多余的（此处一定不包含节点自己，前面已去除自己），删除文件
        if(routeList.size() >= count) {
            boolean deleteRes = file.delete();
            if(!deleteRes) {
                new IOException("Can not delete file " + file.getName()).printStackTrace();
            }
            sparkFileList.remove(hashID);
            System.out.println("\tdelete spark: " + hashID);
        }
    }

    /**
     * 保存接收到的Spark文件
     *
     * @param fileLength 文件的长度
     * @param hashIDStr Spark的HashID
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void saveSpark(int fileLength,String hashIDStr) {
        File file = FileUtil.getSparkFile(hashIDStr);
        FileUtil.makeParentFolder(file);

        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            int pos = 0;
            // 读取文件包
            byte[] buf = new byte[BUFF_SIZE];
            DatagramPacket fileData = new DatagramPacket(buf, buf.length);
            // 文件顺序号
            int dataSeq = 0;
            while (pos < fileLength) {
                datagramSocket.receive(fileData);
                pos += fileData.getLength();
                // 处理最后一个数据包
                int length = pos >= fileLength ? fileData.getLength() - pos + fileLength : fileData.getLength();
                bos.write(fileData.getData(), 0, length);
                bos.flush();

//                byte[] ackBytes = BytesUtil.int2Bytes(dataSeq++);
//                DatagramPacket ack = new DatagramPacket(ackBytes, ackBytes.length,
//                        InetAddress.getByName(fileData.getAddress().getHostAddress()), UDPSocket.getSparkRecvPort());
//                System.out.println(dataSeq + "r");
//                datagramSocket.send(ack);
            }
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\tnew spark: " + hashIDStr);
    }

    /**
     * 保存Spark文件
     *
     * @param hashIDStr Spark的HashID
     * @param fileBytes 文件数据字节数组
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void saveSpark(String hashIDStr, byte[] fileBytes) {
        File file = FileUtil.getSparkFile(hashIDStr);
        FileUtil.makeParentFolder(file);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileBytes);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        peer.getSparkFileList().add(hashIDStr);
        System.out.println("\tnew spark: " + hashIDStr);
    }

    /**
     * 通过Spark种子文件的信息向其他节点索要需要的Spark文件
     *
     * @param seedHashID Spark种子文件的HashID
     * @param peer 本地节点对象，用于获取/更新节点状态
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void askForSparkBySeedFile(String  seedHashID, Peer peer) {
        File seedSpark = FileUtil.getSparkFile(seedHashID);
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(seedSpark));
            // 无需文件名和文件长度信息，直接丢弃
            bufferedReader.readLine();
            bufferedReader.readLine();

            String sparkHashID = bufferedReader.readLine();
            while (sparkHashID != null && !sparkHashID.equals("")) {
                TempSparkList.getInstance().put(seedHashID, sparkHashID);
                sparkHashID = bufferedReader.readLine();
            }
            for (String hashID : TempSparkList.getInstance().get(seedHashID)) {
                if(peer.getSparkFileList().contains(hashID)) {
                    continue;
                }
                int sparkBakCount = Integer.parseInt(ConfigUtil.getInstance().get("sparkBakCount"));
                List<Route> resList = peer.getRouteList().searchFromRouteList(hashID, sparkBakCount);

                for (Route route : resList) {
                    // 节点索要spark文件
                    Message msg = new Message("AskForSpark");
                    msg.put("sparkHashID", hashID);
                    msg.put("isSeed", "false");
                    UDPSocket.send(route.getHost(), msg);
                }
            }
            String seedHashIDFin = TempSparkList.getInstance().check(peer.getSparkFileList());
            if(seedHashIDFin != null) {
                String path = FileUtil.recoverSpark(seedHashIDFin);
                System.out.println("Down " + seedHashIDFin + " finished!");
                System.out.println("Path: " + path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
