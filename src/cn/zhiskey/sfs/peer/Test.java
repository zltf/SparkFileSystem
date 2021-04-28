package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.utils.config.PropertiesUtil;

/**
 * 测试模块功能
 *
 * @author Zhiskey
 */
public class Test {
    public static void main(String[] args) {
        PropertiesUtil.INSTANCE.load("res/configTest.properties");
        System.out.println(PropertiesUtil.INSTANCE.get("a"));
        System.out.println(PropertiesUtil.INSTANCE.get("b"));
        PropertiesUtil.INSTANCE.set("b", "1");
        System.out.println(PropertiesUtil.INSTANCE.get("b"));
        PropertiesUtil.INSTANCE.store("Configs");
//        new UDPRecvLoopThread(54321,
//                datagramPacket -> System.out.println(UDPRecvLoopThread.getDataString(datagramPacket))).start();
    }
}
