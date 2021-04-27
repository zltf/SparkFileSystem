package cn.zhiskey.sfs.peer;

import java.io.IOException;
import java.net.*;

/**
 * TODO: description
 *
 * @author Zhiskey
 */
public class TestRecv {
    public static void main(String[] args) {
        new Thread(() -> {
            try {
                System.out.println(InetAddress.getLocalHost());
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            DatagramSocket datagramSocket = null;
            try {
                datagramSocket = new DatagramSocket(54321);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            if (datagramSocket == null) return;
            while (true) {
                byte [] buff = new byte [1024];
                DatagramPacket datagramPacket = new DatagramPacket(buff,buff.length);

                try {
                    datagramSocket.receive(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String data = new String(datagramPacket.getData(), datagramPacket.getOffset(), datagramPacket.getOffset()+datagramPacket.getLength());
                System.out.println(datagramPacket.getAddress()+":"+datagramPacket.getPort()+" "+data);
            }
        }).start();
    }
}
