package cn.zhiskey.sfs.message;

import cn.zhiskey.sfs.network.Bucket;
import cn.zhiskey.sfs.network.Route;
import cn.zhiskey.sfs.peer.Peer;
import cn.zhiskey.sfs.peer.PeerStatus;
import cn.zhiskey.sfs.utils.BytesUtil;
import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.hash.HashIDUtil;
import cn.zhiskey.sfs.utils.udpsocket.UDPRecvLoopThread;
import cn.zhiskey.sfs.utils.udpsocket.UDPSocket;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.*;

/**
 * TODO: description
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class MessageHandler {
    private Peer peer;

    public MessageHandler(Peer peer) {
        this.peer = peer;
    }

    public void handle(DatagramPacket datagramPacket) {
//        byte[] res = datagramPacket.getData();
//        // 文件hashID的长度
//        int hashIDSize = Integer.parseInt(ConfigUtil.getInstance().get("hashIDSize"));
//        // 文件长度byte[]位数
//        int fileLengthSize = BytesUtil.INT_BYTES_SIZE;
//
//        byte[] hashID = new byte[hashIDSize];
//        System.arraycopy(res, 0, hashID, 0, hashIDSize);
//
//        byte[] fileLengthBytes = new byte[fileLengthSize];
//        System.arraycopy(res, hashIDSize, fileLengthBytes, 0, fileLengthSize);
//
//        int fileLength = BytesUtil.bytes2Int(fileLengthBytes);
//        byte[] fileBytes = new byte[fileLength];
//        System.arraycopy(res, hashIDSize + fileLengthSize, fileBytes, 0, fileLength);
//
//        String hashIDStr = Base64.getEncoder().encodeToString(hashID);
//        String filePath = ConfigUtil.getInstance().get("SparkFolder");
//        filePath += filePath.charAt(filePath.length()-1) == '/' ? hashIDStr : '/' + hashIDStr;
//        filePath += '.' + ConfigUtil.getInstance().get("SparkFileExtension");
//
//        File file = new File(filePath);
//        FileUtil.makeParentFolder(file);
//        try {
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(fileBytes);
//            fos.flush();
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println(Base64.getEncoder().encodeToString(hashID));
//        System.out.println(fileLength);

        Message msg = UDPRecvLoopThread.getMessage(datagramPacket);
        String fromHost = datagramPacket.getAddress().getHostAddress();
        switch (msg.getType()) {
            case "GetHashID":
                getHashID(fromHost);
                break;
            case "ResGetHashID":
                resGetHashID(msg, fromHost);
                break;
            case "SearchNode":
                searchNode(msg, fromHost);
                break;
            case "ResSearchNode":
                resSearchNode(msg);
                break;
            default:
                break;
        }
    }

    private void getHashID(String fromHost) {
        Message res = new Message("ResGetHashID");
        res.put("hashID", peer.getHashID());
        UDPSocket.send(fromHost, res);
    }

    private void resGetHashID(Message msg, String fromHost) {
        if(peer.getStatus() == peer.getStatus()) {
            System.out.println("已和种子节点取得通信");

            // 将种子节点加入路由表
            byte[] seedPeerHashIDBytes = Base64.getDecoder().decode(msg.getString("hashID"));
            peer.getRouteList().add(new Route(seedPeerHashIDBytes, fromHost));

            // 通知种子节点将自己加入网络
            msg = new Message("SearchNode");
            msg.put("hashID", Base64.getEncoder().encodeToString(peer.getHashID()));
            UDPSocket.send(fromHost, msg);

            // 设置状态为运行中
            peer.setStatus(PeerStatus.RUNNING);
        }
    }

    private void searchNode(Message msg, String fromHost) {
        String hashID = msg.getString("hashID");

        // 将节点加入路由表
        byte[] seedPeerHashIDBytes = Base64.getDecoder().decode(hashID);
        peer.getRouteList().add(new Route(seedPeerHashIDBytes, fromHost));

        // 寻找findPeerCount个节点返回
        int findPeerCount = Integer.parseInt(ConfigUtil.getInstance().get("findPeerCount"));
        int bucketCount = Integer.parseInt(ConfigUtil.getInstance().get("bucketCount"));
        int idLeft = HashIDUtil.getInstance().distance(hashID);
        int idRight = idLeft + 1;
        List<Route> resList = new ArrayList<>(findPeerCount);
        while (resList.size() < findPeerCount && (idLeft >= 0 || idRight < bucketCount)) {
            if(idLeft >= 0) {
                iteratorSearchNode(idLeft--, resList);
            }
            if(idRight < bucketCount && resList.size() < findPeerCount) {
                iteratorSearchNode(idRight++, resList);
            }
        }

        // 返回结果
        Message res = new Message("ResSearchNode");
        JSONArray peerArray = new JSONArray();
        for(Route route : resList) {
            JSONObject peer = new JSONObject();
            peer.put("hashID", route.getHashIDString());
            peer.put("host", route.getHost());
            peerArray.add(peer);
        }
        res.put("peers", peerArray);
        UDPSocket.send(fromHost, res);
    }

    private void resSearchNode(Message msg) {
        JSONArray peerArray = (JSONArray) msg.get("peers");
        for (Object obj : peerArray) {
            JSONObject peerObj = (JSONObject) obj;
            String hashIDStr = peerObj.getString("hashID");
            byte[] hashID = Base64.getDecoder().decode(hashIDStr);
            String host = peerObj.getString("host");

            // 如果路由表没有该路由，防止消息风暴
            if(!peer.getRouteList().containsRoute(hashIDStr)) {
                peer.getRouteList().add(new Route(hashID, host));
                // 通知返回的节点将自己加入网络
                msg = new Message("SearchNode");
                msg.put("hashID", Base64.getEncoder().encodeToString(peer.getHashID()));
                UDPSocket.send(host, msg);
            }
        }
    }

    /**
     * 通过迭代器遍历桶中route加入搜索结果路由数组中，并保证结果数组大小不超过findPeerCount<br>
     * 该方法目的是减少重复代码
     *
     * @param distance 要搜索的桶的距离
     * @param resList 搜索结果路由数组
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void iteratorSearchNode(int distance, List<Route> resList) {
        int findPeerCount = Integer.parseInt(ConfigUtil.getInstance().get("findPeerCount"));
        Bucket bucket = peer.getRouteList().getBucket(distance);
        // 迭代器遍历bucket中的route
        Map<String, Route> routeMap = bucket.getRouteMap();
        Iterator<String> iterator = routeMap.keySet().iterator();
        while (iterator.hasNext() && resList.size() < findPeerCount) {
            Route route = routeMap.get(iterator.next());
            resList.add(route);
        }
    }
}
