package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.utils.config.ConfigFileNotLoadException;
import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.udpsocket.UDPRecvLoopThread;

import java.io.File;
import java.io.IOException;

/**
 * 测试模块功能
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Test {
    public static void main(String[] args) {
        File file = new File("D:/QQ==");
        System.out.println(file.exists());
    }
}
