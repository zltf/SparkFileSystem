package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.message.Message;
import cn.zhiskey.sfs.utils.FileUtil;
import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.udpsocket.UDPSocket;

import java.io.File;
import java.io.IOException;
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

        FileUtil.recoverSpark("fgpyfOiCzopgkD50xxeVdkyRcDYIt3SaxFJ2Id5mtOs=");
        System.out.println("finish");
    }
}
