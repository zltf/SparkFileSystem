package cn.zhiskey.sfs.message;

import cn.zhiskey.sfs.network.Route;
import cn.zhiskey.sfs.peer.Peer;
import cn.zhiskey.sfs.peer.PeerStatus;
import cn.zhiskey.sfs.utils.FileUtil;
import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.hash.HashIDUtil;
import cn.zhiskey.sfs.network.udpsocket.spark.SparkDataType;
import cn.zhiskey.sfs.network.udpsocket.UDPRecvLoopThread;
import cn.zhiskey.sfs.network.udpsocket.UDPSocket;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.util.*;

/**
 * 处理接收到的消息对象<br>
 * 根据消息的不同类型分别进行处理
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class MessageHandler {
    /**
     * 要处理消息的本地节点对象，用于获取/更新节点状态
     */
    private final Peer peer;

    /**
     * 构造方法<br>
     * 传入要处理消息的本地节点对象
     *
     * @param peer 要处理消息的本地节点对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public MessageHandler(Peer peer) {
        this.peer = peer;
    }

    /**
     * 消息处理方法
     * 根据消息的不同类型分别交由不同方法进行处理
     *
     * @param datagramPacket 消息的数据包对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void handle(DatagramPacket datagramPacket) {
        Message msg = UDPRecvLoopThread.getMessage(datagramPacket);
        String fromHost = datagramPacket.getAddress().getHostAddress();
        switch (msg.getType()) {
            /*
             * 获取HashID
             * no params
             */
            case "GetHashID":
                getHashID(fromHost);
                break;
            /*
             * 获取HashID响应
             * param :
             *     hashID
             */
            case "ResGetHashID":
                resGetHashID(msg, fromHost);
                break;
            /*
             * 搜索较近节点
             * param :
             *     hashID
             */
            case "SearchPeer":
                searchPeer(msg, fromHost);
                break;
            /*
             * 搜索较近节点响应
             * params :
             *     [peers]:
             *         hashID
             *         host
             */
            case "ResSearchPeer":
                resSearchPeer(msg);
                break;
            /*
             * 索要Spark
             * params :
             *     sparkHashID
             *     isSeed
             */
            case "AskForSpark":
                askForSpark(msg, fromHost);
                break;
            /*
             * 索要Spark响应
             * 告知可能有Spark的节点列表
             * params :
             *     sparkHashID
             *     isSeed
             *     [peers]:
             *         host
             */
            case "ResAskForSpark":
                resAskForSpark(msg);
                break;
            default:
                break;
        }
    }

    /**
     * 收到获取本节点HashID的消息
     *
     * @param fromHost 消息来路主机
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void getHashID(String fromHost) {
        Message res = new Message("ResGetHashID");
        res.put("hashID", peer.getHashID());
        UDPSocket.send(fromHost, res);
    }

    /**
     * 收到获取本地节点HashID消息的响应
     *
     * @param msg 消息对象
     * @param fromHost 消息来路主机
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void resGetHashID(Message msg, String fromHost) {
        if(peer.getStatus() == PeerStatus.WAIT_SEED_HASH_ID) {
            System.out.println("Seed peer connected!");

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

    /**
     * 搜素较近节点
     *
     * @param msg 消息对象
     * @param fromHost 消息来路主机
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
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

        // 将本地和新节点更近的Spark传给新节点，涉及到列表元素删除迭代器遍历
        Iterator<String> iterator = peer.getSparkFileList().iterator();
        while (iterator.hasNext()) {
            String sparkHashID = iterator.next();
            // 本地据Spark文件的距离
            int cpl1 = HashIDUtil.getInstance().cpl(sparkHashID);
            // 新节点据Spark文件的距离
            int cpl2 = HashIDUtil.cpl(sparkHashID, hashID);

            // 如果新节点更近
            if(cpl1 < cpl2) {
                File file = FileUtil.getSparkFile(sparkHashID);
                // 发送文件
                try {
                    UDPSocket.send(fromHost, file, SparkDataType.PUSH_SPARK);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                // 删除本地的文件
//                FileUtil.deleteSparkFile(file, sparkHashID);
//                iterator.remove();
            }
        }
    }

    /**
     * 搜索较近节点响应
     *
     * @param msg 消息对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
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

    /**
     * 索要Spark
     *
     * @param msg 消息对象
     * @param fromHost 消息来路主机
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void askForSpark(Message msg, String fromHost) {
        String sparkHashID = msg.getString("sparkHashID");
        String isSeed = msg.getString("isSeed");

        // 如果本地有该spark文件
        if(peer.getSparkFileList().contains(sparkHashID)) {
            try {
                UDPSocket.send(fromHost, FileUtil.getSparkFile(sparkHashID),
                        isSeed.equals("true") ? SparkDataType.DOWN_SEED_SPARK : SparkDataType.DOWN_SPARK);
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
        res.put("isSeed", isSeed);
        JSONArray peerArray = new JSONArray();
        for(Route route : resList) {
            JSONObject peer = new JSONObject();
            peer.put("host", route.getHost());
            peerArray.add(peer);
        }
        res.put("peers", peerArray);
        UDPSocket.send(fromHost, res);
    }

    /**
     * 索要Spark响应<br>
     * 只有在索要的主机没有该Spark时才会响应此消息<br>
     * 否则直接发送Spark文件
     *
     * @param msg 消息对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void resAskForSpark(Message msg) {
        String sparkHashID = msg.getString("sparkHashID");
        String isSeed = msg.getString("isSeed");
        JSONArray peerArray = (JSONArray) msg.get("peers");
        for (Object obj : peerArray) {
            JSONObject peerObj = (JSONObject) obj;
            String host = peerObj.getString("host");

            // 向返回的节点索要spark文件
            msg = new Message("AskForSpark");
            msg.put("sparkHashID", sparkHashID);
            msg.put("isSeed", isSeed);
            UDPSocket.send(host, msg);
        }
    }
}
