package cn.zhiskey.sfs.utils.config;

import java.io.FileInputStream;
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
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void load(String path) {
        this.path = path;
        properties = new Properties();
        try {
            FileInputStream inputStream = new FileInputStream(this.path);
            properties.load(inputStream);
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存配置信息至指定配置文件，需要解释
     *
     * @param path 配置文件路径
     * @param comments 配置文件解释，取null值表示无解释
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void store(String path, String comments) {
        if(properties != null) {
            try {
                FileOutputStream outputStream = new FileOutputStream(path);
                properties.store(outputStream, comments);
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            new ConfigFileNotLoadException().printStackTrace();
        }
    }

    /**
     * 保存配置信息至加载路径的配置文件，需要解释
     *
     * @param comments 配置文件解释，取null值表示无解释
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void store(String comments) {
        // path为null，properties一定为null
        if(properties != null) {
            store(path, comments);
        } else {
            new ConfigFileNotLoadException().printStackTrace();
        }
    }

    /**
     * 获取某一属性的值
     *
     * @param key 属性的键
     * @return java.lang.String 属性的值
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public String get(String key) {
        if(properties != null) {
            return properties.getProperty(key);
        } else {
            new ConfigFileNotLoadException().printStackTrace();
            return null;
        }
    }

    /**
     * 获取某一属性的值，带默认值
     *
     * @param key 属性的键
     * @param defaultValue 默认值
     * @return java.lang.String 属性的值
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public String get(String key, String defaultValue) {
        if(properties != null) {
            return properties.getProperty(key, defaultValue);
        } else {
            new ConfigFileNotLoadException().printStackTrace();
            return defaultValue;
        }
    }

    /**
     * 设置某一属性的值
     *
     * @param key 属性的键
     * @param value 属性的值
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void set(String key, String value) {
        if(properties != null) {
            properties.setProperty(key, value);
        } else {
            new ConfigFileNotLoadException().printStackTrace();
        }
    }
}
