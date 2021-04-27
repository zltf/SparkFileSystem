package cn.zhiskey.pfs.peer;

import cn.zhiskey.pfs.utils.udpsocket.UDPSocket;

/**
 * TODO: description
 *
 * @author Zhiskey
 */
public class Peer {

    private byte[] hashID = null;

    private byte[] createHashID() {
        return null;
    }

    private void joinNetWork() {

    }

    public static void main(String[] args) {
//        Peer peer = new Peer();
//        peer.joinNetWork();
        UDPSocket.broadcast(54321, "test");
    }
}
