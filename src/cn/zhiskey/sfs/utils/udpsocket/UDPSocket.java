package cn.zhiskey.sfs.utils.udpsocket;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.List;

/**
 * UDP Socket 工具类
 *
 * @author Zhiskey
 */
public class UDPSocket {
    /**
     * UDP发送字符串消息
     *
     * @param host 目的主机
     * @param port 目的端口
     * @param str 待发送的消息
     * @author Zhiskey
     */
    public static void send(String host, int port, String str) {
        send(host, port, str.getBytes());
    }

    /**
     * UDP发送字节数组消息
     *
     * @param host 目的主机
     * @param port 目的端口
     * @param bytes 待发送的消息
     * @author Zhiskey
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
     * UDP本地广播字符串消息
     *
     * @param port 目的端口
     * @param str 待发送的消息
     * @author Zhiskey
     */
    public static void broadcast(int port, String str) {
        String broadcastIP = getLocalBroadcastIP();
        if (broadcastIP != null) {
            send("192.168.1.255", port, str);
        }
    }

    /**
     * 获取本地广播IP地址
     *
     * @return java.lang.String 广播IP地址
     * @author Zhiskey
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
}

/*
package com.zltf.fightnowserver.utils;

import java.io.IOException;
import java.net.*;

public class UDPManager {

    private DatagramSocket datagramSocket = null;

    public UDPManager() {
        open();
    }

    public void open() {
        try {
            datagramSocket = new DatagramSocket(InfoManager.SERVER_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if(datagramSocket != null) {
            datagramSocket.close();
            datagramSocket = null;
        }
    }

    public void send(String host, String string) {
        new Thread(() -> {
            byte[] buff = string.getBytes();
            DatagramPacket datagramPacket = null;
            try {
                datagramPacket = new DatagramPacket(buff, buff.length, InetAddress.getByName(host), InfoManager.CLIENT_PORT);
                datagramSocket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public DatagramPacket recv() {
        byte [] buff = new byte [1024];
        DatagramPacket datagramPacket = new DatagramPacket(buff,buff.length);

        try {
            datagramSocket.receive(datagramPacket);
            return datagramPacket;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getDataFromRecv(DatagramPacket datagramPacket) {
        return new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getOffset()+datagramPacket.getLength());
    }
}

 */
