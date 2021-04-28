package cn.zhiskey.sfs.utils.udpsocket;

import java.net.DatagramPacket;

/**
 * 可接收消息的<br>
 * 消息接收策略
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public interface Recvable {
    /**
     * 接收消息后的处理策略
     *
     * @param datagramPacket 接收的消息对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    void recv(DatagramPacket datagramPacket);
}
