package cn.zhiskey.sfs.utils.config;

/**
 * properties文件未加载异常
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class PropertiesFileNotLoadException extends Exception {
    /**
     * 构造方法<br>
     * 构造properties文件未加载异常
     *
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public PropertiesFileNotLoadException(){
        super("Please call PropertiesUtil.INSTANCE.load(path) to load properties file");
    }
}
