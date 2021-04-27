package cn.zhiskey.sfs.utils.config;

/**
 * properties文件未加载异常
 *
 * @author Zhiskey
 */
public class PropertiesFileNotLoadException extends Exception {
    /**
     * 构造方法<br>
     * 构造properties文件未加载异常
     *
     * @author Zhiskey
     */
    public PropertiesFileNotLoadException(){
        super("Please call ConfigUtil.INSTANCE.load(path) to load properties file");
    }
}
