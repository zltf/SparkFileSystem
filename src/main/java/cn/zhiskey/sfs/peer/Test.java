package cn.zhiskey.sfs.peer;

import cn.zhiskey.sfs.utils.FileUtil;
import cn.zhiskey.sfs.utils.config.ConfigFileNotLoadException;
import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.udpsocket.UDPRecvLoopThread;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 测试模块功能
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Test {
    public static void main(String[] args) {
//        System.out.println(FileUtil.getFileByteSize("3Mb"));
        // 载入配置文件
        try {
            ConfigUtil.getInstance().load(FileUtil.getResourcesPath() + "configs/config.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            List<String> list = FileUtil.makeSpark(new File("D:/apache-maven-3.8.1-bin.zip"));
            for (String s : list) {
                System.out.println(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
