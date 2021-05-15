package cn.zhiskey.sfs.utils.udpsocket;

import cn.zhiskey.sfs.message.Message;
import cn.zhiskey.sfs.utils.BytesUtil;
import cn.zhiskey.sfs.utils.config.ConfigUtil;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * UDP Socket工具类
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class UDPSocket {
    /**
     * UDP发送字节数组消息
     *
     * @param host 目的主机
     * @param port 目的端口
     * @param bytes 待发送的消息
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void send(String host, int port, byte[] bytes) {
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.send(new DatagramPacket(bytes, bytes.length, InetAddress.getByName(host), port));
            datagramSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (datagramSocket != null) {
            datagramSocket.close();
        }
    }

    /**
     * UDP发送字符串消息
     *
     * @param host 目的主机
     * @param port 目的端口
     * @param str 待发送的消息
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void send(String host, int port, String str) {
        send(host, port, str.getBytes());
    }

    /**
     * UDP发送消息对象
     *
     * @param host 目的主机
     * @param port 目的端口
     * @param msg 待发送的消息对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void send(String host, int port, Message msg) {
        send(host, port, msg.toJSONString().getBytes());
    }

    /**
     * UDP发送消息对象，目的端口为配置文件中指定的通用接收端口
     *
     * @param host 目的主机
     * @param msg 待发送的消息对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void send(String host, Message msg) {
        send(host, getCommonRecvPort(), msg);
    }

//    /**
//     * UDP发送文件，会发生spark文件标识码
//     * TODO: 分段发送
//     *
//     * @param host 目的主机
//     * @param port 目的端口
//     * @param file 待发送的文件
//     * @param dataType 发送文件的数据类型：PUSH_SPARK、DOWN_SPARK
//     * @throws IOException 发送的文件不存在时，或IO操作异常时
//     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
//     */
//    public static void send(String host, int port, File file, DataType dataType) throws IOException {
//        if(!file.exists()) {
//            throw new FileNotFoundException(file.getAbsolutePath());
//        }
//        String fileName = file.getName();
//        // 去掉文件后缀名
//        fileName = fileName.substring(0, fileName.length() - ConfigUtil.getInstance().get("sparkFileExtension").length() - 1);
//        // 文件hashID的长度
//        int hashIDSize = Integer.parseInt(ConfigUtil.getInstance().get("hashIDSize"));
//        // 文件长度byte[]位数
//        int fileLengthSize = BytesUtil.INT_BYTES_SIZE;
//        // 文件长度
//        int fileLength = (int) file.length();
//        // 文件名就是文件的hashID的Base64编码结果
//        byte[] hashIDBytes = Base64.getDecoder().decode(fileName);
//        // 文件长度字节数组
//        byte[] fileLengthBytes = BytesUtil.int2Bytes(fileLength);
//        // 要发送的字节数组
//        byte[] sendBytes = new byte[hashIDSize + fileLengthSize + fileLength];
//        // 复制hashID到发送数组
//        System.arraycopy(hashIDBytes, 0, sendBytes, 0, hashIDSize);
//        // 文件长度字节数组到发送数组
//        System.arraycopy(fileLengthBytes, 0, sendBytes, hashIDSize, fileLengthSize);
//        // 写文件的起始位置
//        int staPos = hashIDSize + fileLengthSize;
//        // 将文件内容写入发送数组
//        FileInputStream fis = new FileInputStream(file);
//        System.arraycopy(fis.readAllBytes(), 0, sendBytes, staPos, fileLength);
//
//        // 添加spark文件标识码
//        byte[] dataTypeBytes = BytesUtil.int2Bytes(dataType.ordinal());
//        byte[] data = new byte[BytesUtil.INT_BYTES_SIZE + sendBytes.length];
//        System.arraycopy(dataTypeBytes, 0, data, 0, BytesUtil.INT_BYTES_SIZE);
//        System.arraycopy(sendBytes, 0, data, BytesUtil.INT_BYTES_SIZE, sendBytes.length);
//
//        sendSegmented(host, port, data);
//    }

    /**
     * UDP分段发送文件，会发生spark文件标识码
     *
     * @param host 目的主机
     * @param file 待发送的文件
     * @param dataType 发送文件的数据类型：PUSH_SPARK、DOWN_SPARK
     * @throws IOException 发送的文件不存在时，或IO操作异常时
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void sendSpark(String host, File file, SparkDataType dataType) throws IOException {
        if(!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        String fileName = file.getName();
        // 去掉文件后缀名
        fileName = fileName.substring(0, fileName.length() - ConfigUtil.getInstance().get("sparkFileExtension").length() - 1);
        // 文件hashID的长度
        int hashIDSize = Integer.parseInt(ConfigUtil.getInstance().get("hashIDSize"));
        // 文件长度
        int fileLength = (int) file.length();

        // spark文件标识码
        byte[] dataTypeBytes = BytesUtil.int2Bytes(dataType.ordinal());
        // 文件名就是文件的hashID的Base64编码结果
        byte[] hashIDBytes = Base64.getDecoder().decode(fileName);
        // 文件长度字节数组
        byte[] fileLengthBytes = BytesUtil.int2Bytes(fileLength);
        // 要发送的字节数组
        byte[] sendBytes = new byte[BytesUtil.INT_BYTES_SIZE + hashIDSize + BytesUtil.INT_BYTES_SIZE];

        System.arraycopy(dataTypeBytes, 0, sendBytes, 0, BytesUtil.INT_BYTES_SIZE);
        // 复制hashID到发送数组
        System.arraycopy(hashIDBytes, 0, sendBytes, BytesUtil.INT_BYTES_SIZE, hashIDSize);
        // 文件长度字节数组到发送数组
        System.arraycopy(fileLengthBytes, 0, sendBytes, BytesUtil.INT_BYTES_SIZE + hashIDSize, BytesUtil.INT_BYTES_SIZE);

        System.out.println("send " + Base64.getEncoder().encodeToString(hashIDBytes) + " " + host);

        DatagramSocket datagramSocket = new DatagramSocket();
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

        DatagramPacket perData = new DatagramPacket(sendBytes, sendBytes.length, InetAddress.getByName(host), getSparkRecvPort());
        datagramSocket.send(perData);

//        byte[] ackBytes = new byte[BytesUtil.INT_BYTES_SIZE];
//        DatagramPacket ack = new DatagramPacket(ackBytes,ackBytes.length);

//        // 文件包顺序号
//        int dataSeq = 0;
        byte[] buf = new byte[SparkRecvLoopThread.BUFF_SIZE];
        int len;
        while ((len = bis.read(buf)) != -1) {
            DatagramPacket data = new DatagramPacket(buf, len, InetAddress.getByName(host), getSparkRecvPort());
            datagramSocket.send(data);
//            // 设置确认信息接收时间，3秒后未收到对方确认信息，则重新发送一次
//            datagramSocket.setSoTimeout(3000);
//            while (true) {
//                datagramSocket.send(data);
//                datagramSocket.receive(ack);
//                if(dataSeq == BytesUtil.bytes2Int(ack.getData())) {
//                    System.out.println(dataSeq + "s");
//                    dataSeq++;
//                    break;
//                }
//            }
        }

        bis.close();
        datagramSocket.close();
    }

    /**
     * UDP本地广播字符串消息
     *
     * @param port 目的端口
     * @param str 待发送的消息
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void broadcast(int port, String str) {
        String broadcastIP = getLocalBroadcastIP();
        if (broadcastIP != null) {
            send(broadcastIP, port, str);
        }
    }

    /**
     * UDP本地广播消息对象
     *
     * @param port 目的端口
     * @param msg 待发送的消息对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void broadcast(int port, Message msg) {
        broadcast(port, msg.toJSONString());
    }

    /**
     * 获取本地广播IP地址
     *
     * @return java.lang.String 广播IP地址
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static String getLocalBroadcastIP() {
        String broadcastIP = null;
        try {
            Enumeration<?> netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) netInterfaces.nextElement();
                if (!netInterface.isLoopback()&& netInterface.isUp()) {
                    List<InterfaceAddress> interfaceAddresses = netInterface.getInterfaceAddresses();
                    for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                        //只有 IPv4 网络具有广播地址，因此对于 IPv6 网络将返回 null。
                        if(interfaceAddress.getBroadcast()!= null){
                            broadcastIP =interfaceAddress.getBroadcast().getHostAddress();

                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        System.out.println(broadcastIP);
        return broadcastIP;
    }

    /**
     * 获取配置文件中的消息接收端口
     *
     * @return int 配置文件中的消息接收端口
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static int getCommonRecvPort() {
        return Integer.parseInt(ConfigUtil.getInstance().get("messageRecvPort"));
    }

    /**
     * 获取配置文件中的spark接收端口
     *
     * @return int 配置文件中的spark接收端口
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static int getSparkRecvPort() {
        return Integer.parseInt(ConfigUtil.getInstance().get("sparkRecvPort"));
    }
}
