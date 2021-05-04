package cn.zhiskey.sfs.utils.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * 配置信息管理工具类<br>
 * 单例模式，枚举实现
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public enum ConfigUtil {
    /**
     * 实例
     */
    INSTANCE;

    /**
     * 属性对象
     */
    private Properties properties = null;

    /**
     * 默认的配置文件路径
     */
    private String path = null;

    /**
     * 返回配置信息管理工具实例<br>
     * 兼容传统的单例模式实力获取方式
     *
     * @return cn.zhiskey.sfs.utils.config.ConfigUtil 配置信息管理工具实例
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static ConfigUtil getInstance() {
        return INSTANCE;
    }

    /**
     * 加载指定路径配置文件
     *
     * @param path 配置文件路径
     * @throws IOException 当文件IO异常时
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void load(String path) throws IOException {
        this.path = path;
        properties = new Properties();
        FileInputStream inputStream = new FileInputStream(this.path);
        properties.load(inputStream);
        inputStream.close();
    }

    /**
     * 保存配置信息至指定配置文件，需要解释
     *
     * @param path 配置文件路径
     * @param comments 配置文件解释，取null值表示无解释
     * @throws ConfigFileNotLoadException 当properties文件未成功加载时
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void store(String path, String comments) throws ConfigFileNotLoadException {
        if(properties != null) {
            try {
                FileOutputStream outputStream = new FileOutputStream(path);
                properties.store(outputStream, comments);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new ConfigFileNotLoadException();
        }
    }

    /**
     * 保存配置信息至加载路径的配置文件，需要解释
     *
     * @param comments 配置文件解释，取null值表示无解释
     * @throws ConfigFileNotLoadException 当properties文件未成功加载时
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void store(String comments) throws ConfigFileNotLoadException {
        // path为null，properties一定为null
        if(properties != null) {
            store(path, comments);
        } else {
            throw new ConfigFileNotLoadException();
        }
    }

    /**
     * 获取某一属性的值
     *
     * @param key 属性的键
     * @return java.lang.String 属性的值
     * @throws ConfigFileNotLoadException 当properties文件未成功加载时
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public String get(String key) throws ConfigFileNotLoadException {
        if(properties != null) {
            return properties.getProperty(key);
        } else {
            throw  new ConfigFileNotLoadException();
        }
    }

    /**
     * 获取某一属性的值，带默认值
     *
     * @param key 属性的键
     * @param defaultValue 默认值
     * @return java.lang.String 属性的值
     * @throws ConfigFileNotLoadException 当properties文件未成功加载时
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public String get(String key, String defaultValue) throws ConfigFileNotLoadException {
        if(properties != null) {
            return properties.getProperty(key, defaultValue);
        } else {
            throw new ConfigFileNotLoadException();
        }
    }

    /**
     * 设置某一属性的值
     *
     * @param key 属性的键
     * @param value 属性的值
     * @throws ConfigFileNotLoadException 当properties文件未成功加载时
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void set(String key, String value) throws ConfigFileNotLoadException {
        if(properties != null) {
            properties.setProperty(key, value);
        } else {
            throw new ConfigFileNotLoadException();
        }
    }
}
