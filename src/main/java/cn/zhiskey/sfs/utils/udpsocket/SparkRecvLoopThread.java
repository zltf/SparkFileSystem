package cn.zhiskey.sfs.utils.udpsocket;

import cn.zhiskey.sfs.network.Route;
import cn.zhiskey.sfs.peer.Peer;
import cn.zhiskey.sfs.utils.BytesUtil;
import cn.zhiskey.sfs.utils.FileUtil;
import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.hash.HashIDUtil;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

/**
 * TODO: description
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class SparkRecvLoopThread extends Thread {
    Peer peer;

    /**
     * UDP Socket对象
     */
    private DatagramSocket datagramSocket = null;

    /**
     * 接收缓冲区大小<br>
     * 单位：字节<br>
     * 值为1024 * 63
     */
    public static final int BUFF_SIZE = 1024 * 63;

    /**
     * 构造方法<br>
     * 初始化UDP Socket对象
     *
     * @param recvPort 接收消息的端口
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

    @Override
    public void run() {
        while (true) {
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

            System.out.println("recv " + HashIDUtil.toString(hashID) + " " + datagramPacket.getAddress().getHostAddress());

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
                default:
                    break;
            }
        }
    }

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
            SparkRecvLoopThread.sendSpark(resList, peer.getHashIDString(), hashIDStr, sparkBakCount, peer.getSparkFileList());
        }
    }

    /**
     * 向路由列表中的各个路由发送spark文件，并且如果本节点不是离spark较近的节点，删除本地的spark文件
     *
     * @param routeList 路由列表
     * @param peerHashID 本节点的hashID
     * @param hashID spark的hashID
     * @param count 文件存在的最大备份份数
     * @param sparkFileList 本节点spark文件表
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void sendSpark(List<Route> routeList, String peerHashID, String hashID, int count, List<String> sparkFileList) {
        File file = FileUtil.getSparkFile(hashID);
        boolean selfFlag = false;
        for (Route route : routeList) {
            try {
                UDPSocket.send(route.getHost(), file, SparkDataType.PUSH_SPARK);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(route.getHashIDString().equals(peerHashID)) {
                // 结果列表中包含节点自己
                selfFlag = true;
            }
        }

        // 本地的文件是多余的，删除
        if(routeList.size() >= count && !selfFlag) {
            boolean deleteRes = file.delete();
            if(!deleteRes) {
                new IOException("Can not delete file " + file.getName()).printStackTrace();
            }
            sparkFileList.remove(hashID);
            System.out.println("delete spark: " + hashID);
        }
    }

    /**
     * 保存接收到的spark文件
     *
     * @param fileLength 文件的长度
     * @param hashIDStr spark的hashID
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
        System.out.println("new spark: " + hashIDStr);
    }

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
        System.out.println("new spark: " + hashIDStr);
    }
}

// D:/apache-maven-3.8.1-bin.zip