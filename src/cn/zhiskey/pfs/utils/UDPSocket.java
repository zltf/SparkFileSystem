package cn.zhiskey.pfs.utils;

/**
 * TODO: description
 *
 * @author Zhiskey
 */
public class UDPSocket {
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
