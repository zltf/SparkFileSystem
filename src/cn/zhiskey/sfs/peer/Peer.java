package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.utils.HashUtil;
import cn.zhiskey.sfs.utils.config.ConfigUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

/**
 * P2P节点类
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Peer {

    private byte[] hashID = null;

    private byte[] createHashID() {
        StringBuilder seed = new StringBuilder();

        // 时间戳
        long timeStamp = System.currentTimeMillis();
        seed.append(timeStamp);

        // MAC地址
        try {
            byte[] mac = NetworkInterface.getByInetAddress(InetAddress.getLocalHost()).getHardwareAddress();
            seed.append(HashUtil.bytes2Hex(mac));
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }

        // 随机数
        seed.append(new Random().nextInt());

        // 生成哈希ID
        String hashType = ConfigUtil.getInstance().get("hash");
        return HashUtil.getHash(seed.toString(), hashType);
    }

    private void joinNetWork(String seedPeerHost) {
        initPeer();
    }

    private void initPeer() {

    }

    public static void main(String[] args) {
        // 载入配置文件
        ConfigUtil.getInstance().load("config/config.properties");

        Peer peer = new Peer();
//        peer.joinNetWork();
//        UDPSocket.broadcast(54321, "test");
        System.out.println(HashUtil.bytes2Hex(peer.createHashID()));
    }
}
