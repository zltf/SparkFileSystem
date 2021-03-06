package cn.zhiskey.sfs.network.udpsocket;

import cn.zhiskey.sfs.message.Message;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * UDP Socket消息接收循环线程<br>
 * 策略模式，构造时传入消息处理策略
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class UDPRecvLoopThread extends Thread {
    /**
     * 消息接收循环状态
     */
    private Status status;

    /**
     * UDP Socket对象
     */
    private DatagramSocket datagramSocket = null;

    /**
     * 消息接收策略
     */
    private final Recvable recvable;

    /**
     * 接收缓冲区大小<br>
     * 单位：字节<br>
     * 默认取1024
     */
    private int buffSize = 1024;

    /**
     * 构造方法<br>
     * 初始化UDP Socket对象
     *
     * @param recvPort 接收消息的端口
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public UDPRecvLoopThread(int recvPort, Recvable recvable) {
        this.recvable = recvable;

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
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void stopRecv() {
        status = Status.STOP;
    }

    /**
     * 关闭UDP Socket对象，释放端口
     *
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void close() {
        status = Status.DISABLE;
        datagramSocket.close();
    }

    /**
     * 重写run()方法，并将其设置为final，不可重写<br>
     * 循环接收消息，并且将消息传递给recv()方法处理
     *
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    @Override
    public final void run() {
        while (status == Status.RUNNABLE) {
            byte [] buff = new byte [buffSize];
            DatagramPacket datagramPacket = new DatagramPacket(buff,buff.length);
            try {
                datagramSocket.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            recvable.recv(datagramPacket);
        }
    }

    /**
     * 从DatagramPacket对象中获取data字符串
     *
     * @param datagramPacket DatagramPacket对象
     * @return java.lang.String data字符串
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static String getDataString(DatagramPacket datagramPacket) {
        return new String(datagramPacket.getData(), datagramPacket.getOffset(),
                datagramPacket.getOffset() + datagramPacket.getLength());
    }

    /**
     * 从DatagramPacket对象中获取消息对象
     *
     * @param datagramPacket DatagramPacket对象
     * @return cn.zhiskey.sfs.message.Message 消息对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static Message getMessage(DatagramPacket datagramPacket) {
        return Message.parseByJSON(new String(datagramPacket.getData(), datagramPacket.getOffset(),
                datagramPacket.getOffset() + datagramPacket.getLength()));
    }

    /**
     * 获取消息接收缓冲区大小
     *
     * @return int 消息接收缓冲区大小
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public int getBuffSize() {
        return buffSize;
    }

    /**
     * 设置消息接收缓冲区大小
     *
     * @param buffSize 消息接收缓冲区大小
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void setBuffSize(int buffSize) {
        this.buffSize = buffSize;
    }
}

/**
 * 消息接收循环状态
 */
enum Status {
    /**
     * 不可用状态
     */
    DISABLE,

    /**
     * 正在运行状态
     */
    RUNNABLE,

    /**
     * 暂停状态
     */
    STOP,
}
