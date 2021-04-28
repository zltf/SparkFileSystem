package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.utils.config.PropertiesUtil;

/**
 * 测试模块功能
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Test {
    public static void main(String[] args) {
        PropertiesUtil.getInstance().load("res/configTest.properties");
        System.out.println(PropertiesUtil.getInstance().get("a"));
        System.out.println(PropertiesUtil.getInstance().get("b"));
        PropertiesUtil.getInstance().set("b", "1");
        System.out.println(PropertiesUtil.getInstance().get("b"));
        PropertiesUtil.getInstance().store("Configs");
//        new UDPRecvLoopThread(54321,
//                datagramPacket -> System.out.println(UDPRecvLoopThread.getDataString(datagramPacket))).start();
    }
}
