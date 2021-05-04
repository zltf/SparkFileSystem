package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.utils.config.ConfigFileNotLoadException;
import cn.zhiskey.sfs.utils.config.ConfigUtil;

import java.io.IOException;

/**
 * 测试模块功能
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Test {
    public static void main(String[] args) {
        try {
            ConfigUtil.getInstance().load("configs/configTest.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(ConfigUtil.getInstance().get("a"));
        System.out.println(ConfigUtil.getInstance().get("b"));
        ConfigUtil.getInstance().set("b", "1");
        System.out.println(ConfigUtil.getInstance().get("b"));
        ConfigUtil.getInstance().store("Configs");
//        new UDPRecvLoopThread(54321,
//                datagramPacket -> System.out.println(UDPRecvLoopThread.getDataString(datagramPacket))).start();
    }
}
