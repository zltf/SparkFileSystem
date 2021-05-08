package cn.zhiskey.sfs.utils;

import java.io.File;
import java.net.URL;

/**
 * 文件处理工具类
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class FileUtil {
    /**
     * 获取resources目录根路径
     *
     * @return java.lang.String TODO: description
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static String getResourcesPath() {
        URL url = FileUtil.class.getResource("/");
        return url == null ? "" : url.getPath();
    }
}
