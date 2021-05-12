package cn.zhiskey.sfs.utils.udpsocket;

import cn.zhiskey.sfs.message.Message;
import cn.zhiskey.sfs.utils.BytesUtil;
import cn.zhiskey.sfs.utils.config.ConfigUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    /**
     * UDP发送文件
     * TODO: 分段发送
     *
     * @param host 目的主机
     * @param port 目的端口
     * @param file 待发送的文件
     * @throws IOException 发送的文件不存在时，或IO操作异常时
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static void send(String host, int port, File file) throws IOException {
        if(!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        String fileName = file.getName();
        // 去掉文件后缀名
        fileName = fileName.substring(0, fileName.length() - ConfigUtil.getInstance().get("sparkFileExtension").length() - 1);
        // 文件hashID的长度
        int hashIDSize = Integer.parseInt(ConfigUtil.getInstance().get("hashIDSize"));
        // 文件长度byte[]位数
        int fileLengthSize = BytesUtil.INT_BYTES_SIZE;
        // 文件长度
        int fileLength = (int) file.length();
        // 文件名就是文件的hashID的Base64编码结果
        byte[] hashIDBytes = Base64.getDecoder().decode(fileName);
        // 文件长度字节数组
        byte[] fileLengthBytes = BytesUtil.int2Bytes(fileLength);
        // 要发送的字节数组
        byte[] sendBytes = new byte[hashIDSize + fileLengthSize + fileLength];
        // 复制hashID到发送数组
        System.arraycopy(hashIDBytes, 0, sendBytes, 0, hashIDSize);
        // 文件长度字节数组到发送数组
        System.arraycopy(fileLengthBytes, 0, sendBytes, hashIDSize, fileLengthSize);
        // 写文件的起始位置
        int staPos = hashIDSize + fileLengthSize;
        // 将文件内容写入发送数组
        FileInputStream fis = new FileInputStream(file);
        System.arraycopy(fis.readAllBytes(), 0, sendBytes, staPos, fileLength);
        send(host, port, sendBytes);
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
     * 获取配置文件中的通用接收端口
     *
     * @return int 配置文件中的通用接收端口
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static int getCommonRecvPort() {
        return Integer.parseInt(ConfigUtil.getInstance().get("commonRecvPort"));
    }
}
