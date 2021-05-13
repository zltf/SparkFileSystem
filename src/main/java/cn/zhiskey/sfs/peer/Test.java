package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.message.Message;
import cn.zhiskey.sfs.message.MessageHandler;
import cn.zhiskey.sfs.message.trr.TempRouteRes;
import cn.zhiskey.sfs.message.trr.TempRouteResItem;
import cn.zhiskey.sfs.utils.FileUtil;
import cn.zhiskey.sfs.utils.config.ConfigFileNotLoadException;
import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.udpsocket.UDPRecvLoopThread;
import cn.zhiskey.sfs.utils.udpsocket.UDPSocket;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * 测试模块功能
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Test {
    public static void main(String[] args) {
        // 载入配置文件
        try {
            ConfigUtil.getInstance().load(FileUtil.getResourcesPath() + "configs/config.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scanner scanner = new Scanner((System.in));

        Peer peer = new Peer();
        peer.joinNetWork(scanner.next());

        while (true) {
            String hashID = scanner.next();
            Message msg = new Message("SearchNode");
            msg.put("count", Integer.parseInt(ConfigUtil.getInstance().get("findPeerCount")));
            msg.put("hashID", hashID);
            msg.put("searchType", "nearSpark");
            TempRouteRes.getInstance().put(hashID, new TempRouteResItem(
                    res -> System.out.println(TempRouteRes.getInstance().get(hashID))));
            UDPSocket.send("192.168.1.108", msg);
        }
    }
}
