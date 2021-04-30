package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.utils.config.ConfigUtil;

/**
 * 测试模块功能
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Test {
    public static void main(String[] args) {
        ConfigUtil.getInstance().load("config/configTest.properties");
        System.out.println(ConfigUtil.getInstance().get("a"));
        System.out.println(ConfigUtil.getInstance().get("b"));
        ConfigUtil.getInstance().set("b", "1");
        System.out.println(ConfigUtil.getInstance().get("b"));
        ConfigUtil.getInstance().store("Configs");
//        new UDPRecvLoopThread(54321,
//                datagramPacket -> System.out.println(UDPRecvLoopThread.getDataString(datagramPacket))).start();
    }
}
