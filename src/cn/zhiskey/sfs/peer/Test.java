package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.utils.config.ConfigUtil;

/**
 * 测试模块功能
 *
 * @author Zhiskey
 */
public class Test {
    public static void main(String[] args) {
        ConfigUtil.INSTANCE.load(ConfigUtil.getResPath() + "configTest.properties");
        System.out.println(ConfigUtil.INSTANCE.get("a"));
        System.out.println(ConfigUtil.INSTANCE.get("b"));
        ConfigUtil.INSTANCE.set("b", "1");
        System.out.println(ConfigUtil.INSTANCE.get("b"));
        ConfigUtil.INSTANCE.store("Configs");
    }
}
