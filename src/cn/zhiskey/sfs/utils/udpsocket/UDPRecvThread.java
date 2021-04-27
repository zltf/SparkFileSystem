package cn.zhiskey.sfs.utils.udpsocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * UDP Socket 接收线程
 *
 * @author Zhiskey
 */
public abstract class UDPRecvThread extends Thread {
    /**
     * 线程状态
     */
    private Status status = null;

    /**
     * UDP Socket对象
     */
    DatagramSocket datagramSocket = null;

    /**
     * 构造方法<br>
     * 初始化UDP Socket对象
     *
     * @param recvPort 接收消息的端口
     * @author Zhiskey
     */
    public UDPRecvThread(int recvPort) {
        // 创建Socket对象
        try {
            datagramSocket = new DatagramSocket(recvPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        status = datagramSocket == null ? Status.DISABLE : Status.RUNNABLE;
    }

    /**
     * 停止接收消息
     *
     * @author Zhiskey
     */
    public void stopRecv() {
        status = Status.STOP;
    }

    /**
     * 关闭UDP Socket对象，释放端口
     *
     * @author Zhiskey
     */
    public void close() {
        status = Status.DISABLE;
        datagramSocket.close();
    }

    /**
     * 接收到消息后会调用该方法
     *
     * @param datagramPacket 接收到的消息对象
     * @author Zhiskey
     */
    public abstract void recv(DatagramPacket datagramPacket);

    /**
     * 重写run()方法<br>
     * 循环接收消息，并且将消息传递给recv()方法处理
     *
     * @author Zhiskey
     */
    @Override
    public void run() {
        while (status == Status.RUNNABLE) {
            byte [] buff = new byte [1024];
            DatagramPacket datagramPacket = new DatagramPacket(buff,buff.length);
            try {
                datagramSocket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            recv(datagramPacket);
        }
    }
}

/**
 * UDP Socket 接收线程的状态
 */
enum Status {
    DISABLE,
    RUNNABLE,
    STOP,
}
