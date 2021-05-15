package cn.zhiskey.sfs.message;

import cn.zhiskey.sfs.network.Route;
import cn.zhiskey.sfs.peer.Peer;
import cn.zhiskey.sfs.peer.PeerStatus;
import cn.zhiskey.sfs.utils.FileUtil;
import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.hash.HashIDUtil;
import cn.zhiskey.sfs.utils.udpsocket.SparkDataType;
import cn.zhiskey.sfs.utils.udpsocket.UDPRecvLoopThread;
import cn.zhiskey.sfs.utils.udpsocket.UDPSocket;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

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
        Message msg = UDPRecvLoopThread.getMessage(datagramPacket);
        String fromHost = datagramPacket.getAddress().getHostAddress();
        switch (msg.getType()) {
            // 获取hashID
            // 无参数
            case "GetHashID":
                getHashID(fromHost);
                break;
            // 获取hashID响应
            // hashID
            case "ResGetHashID":
                resGetHashID(msg, fromHost);
                break;
            // 搜索最近节点
            // hashID
            case "SearchPeer":
                searchPeer(msg, fromHost);
                break;
            // 搜索最近节点响应
            // peers
            //     hashID
            //     host
            case "ResSearchPeer":
                resSearchPeer(msg);
                break;
            // 索要spark
            // sparkHashID
            case "AskForSpark":
                askForSpark(msg, fromHost);
                break;
            // 告知可能有spark的节点列表
            // sparkHashID
            // peers
            //     host
            case "ResAskForSpark":
                resAskForSpark(msg);
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
        if(peer.getStatus() == PeerStatus.WAIT_SEED_HASH_ID) {
            System.out.println("已和种子节点取得通信");

            // 将种子节点加入路由表
            byte[] seedPeerHashIDBytes = HashIDUtil.toBytes(msg.getString("hashID"));
            peer.getRouteList().add(new Route(seedPeerHashIDBytes, fromHost));

            // 通知种子节点将自己加入网络
            msg = new Message("SearchPeer");
            msg.put("hashID", HashIDUtil.toString(peer.getHashID()));
            UDPSocket.send(fromHost, msg);

            // 设置状态为运行中
            peer.setStatus(PeerStatus.RUNNING);
        }
    }

    private void searchPeer(Message msg, String fromHost) {
        String hashID = msg.getString("hashID");

        // 将节点加入路由表
        byte[] seedPeerHashIDBytes = HashIDUtil.toBytes(hashID);
        peer.getRouteList().add(new Route(seedPeerHashIDBytes, fromHost));

        // 寻找searchPeerCount个节点返回
        int searchPeerCount = Integer.parseInt(ConfigUtil.getInstance().get("searchPeerCount"));
        List<Route> resList = peer.getRouteList().searchFromRouteList(hashID, searchPeerCount);
        // 返回结果
        Message res = new Message("ResSearchPeer");
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

    private void resSearchPeer(Message msg) {
        JSONArray peerArray = (JSONArray) msg.get("peers");
        for (Object obj : peerArray) {
            JSONObject peerObj = (JSONObject) obj;
            String hashIDStr = peerObj.getString("hashID");
            byte[] hashID = HashIDUtil.toBytes(hashIDStr);
            String host = peerObj.getString("host");

            Route route = new Route(hashID, host);

            // 如果路由表没有该路由才处理，防止重复路由，造成循环
            if(!peer.getRouteList().containsRoute(route.getHashIDString())) {
                // 添加到路由表
                peer.getRouteList().add(route);
                // 通知返回的节点将自己加入网络
                msg = new Message("SearchPeer");
                msg.put("hashID", HashIDUtil.toString(peer.getHashID()));
                UDPSocket.send(route.getHost(), msg);
            }
        }
    }

    private void askForSpark(Message msg, String fromHost) {
        String sparkHashID = msg.getString("sparkHashID");

        // 如果本地有该spark文件
        if(peer.getSparkFileList().contains(sparkHashID)) {
            try {
                UDPSocket.send(fromHost, FileUtil.getSparkFile(sparkHashID), SparkDataType.DOWN_SPARK);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        // 如本地没有该spark，寻找sparkBakCount个节点返回
        int sparkBakCount = Integer.parseInt(ConfigUtil.getInstance().get("sparkBakCount"));
        List<Route> resList = peer.getRouteList().searchFromRouteList(sparkHashID, sparkBakCount);

        // 返回结果
        Message res = new Message("ResAskForSpark");
        res.put("sparkHashID", sparkHashID);
        JSONArray peerArray = new JSONArray();
        for(Route route : resList) {
            JSONObject peer = new JSONObject();
            peer.put("host", route.getHost());
            peerArray.add(peer);
        }
        res.put("peers", peerArray);
        UDPSocket.send(fromHost, res);
    }

    private void resAskForSpark(Message msg) {
        String sparkHashID = msg.getString("sparkHashID");
        JSONArray peerArray = (JSONArray) msg.get("peers");
        for (Object obj : peerArray) {
            JSONObject peerObj = (JSONObject) obj;
            String host = peerObj.getString("host");

            // 向返回的节点索要spark文件
            msg = new Message("AskForSpark");
            msg.put("hashID", sparkHashID);
            UDPSocket.send(host, msg);
        }
    }
}
