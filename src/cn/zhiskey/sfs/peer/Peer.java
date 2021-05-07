package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.utils.HashUtil;
import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.xml.XMLUtil;
import org.w3c.dom.Document;

import java.io.IOException;
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
        String hashType = ConfigUtil.getInstance().get("hashType", "SHA-256");
        return HashUtil.getHash(seed.toString(), hashType);
    }

    private void joinNetWork(String seedPeerHost) {
        initPeer();
    }

    private void initPeer() {
//        Document document;
//        try {
//            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//            DocumentBuilder builder = factory.newDocumentBuilder();
//
//            String path = "data/" + ConfigUtil.getInstance().get("peerDataPath");
//            File file = new File(path);
//
//            // 判断数据文件是否存在
//            if(file.exists()) {
//                // 解析xml文件
//                document = builder.parse(file);
//            } else {
//                document = builder.newDocument();
//            }
//        } catch (SAXException | ParserConfigurationException | IOException e) {
//            e.printStackTrace();
//        }
        String path = "data/" + ConfigUtil.getInstance().get("peerDataPath");
        try {
            Document document = XMLUtil.parse(path);
            if(document != null) {
                System.out.println(xmlObject.node.getNodeName());
                System.out.println(xmlObject.node.getBaseURI());
                System.out.println(xmlObject.node.getLocalName());
                System.out.println(xmlObject.node.getFirstChild().getNodeType());
                System.out.println(xmlObject.node.getFirstChild().getNodeValue().trim());
                System.out.println(xmlObject.node.getAttributes().getNamedItem("hashID").getNodeValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // 载入配置文件
        try {
            ConfigUtil.getInstance().load("configs/config.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Peer peer = new Peer();
        peer.joinNetWork("");
//        UDPSocket.broadcast(54321, "test");
//        System.out.println(HashUtil.bytes2Hex(peer.createHashID()));
    }
}
