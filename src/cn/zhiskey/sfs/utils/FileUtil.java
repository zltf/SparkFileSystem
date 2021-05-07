package cn.zhiskey.sfs.utils;

import java.io.File;

/**
 * 文件处理工具类
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class FileUtil {
    /**
     * 判断文件是否存在
     *
     * @param path 文件路径
     * @return boolean 文件是否存在
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static boolean exists(String path) {
        File file = new File(path);
        return file.exists();
    }
}
