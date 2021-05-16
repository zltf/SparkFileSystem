package cn.zhiskey.sfs.network;

import cn.zhiskey.sfs.utils.hash.HashIDUtil;

import java.util.Arrays;
import java.util.Objects;

/**
 * 路由信息类
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Route {
    /**
     * 节点的HashID
     */
    private final byte[] hashID;

    /**
     * 节点的主机地址
     */
    private final String host;

    /**
     * 构造方法
     *
     * @param hashID 节点的HashID
     * @param host 节点的主机地址
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public Route(byte[] hashID, String host) {
        this.hashID = hashID;
        this.host = host;
    }

    /**
     * 获取节点主机地址
     *
     * @return java.lang.String 节点主机地址
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public String getHost() {
        return host;
    }

    /**
     * 获取节点HashID
     *
     * @return byte[] 节点HashID
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public byte[] getHashID() {
        return hashID;
    }

    /**
     * 获取节点HashID字符串
     *
     * @return java.lang.String 节点HashID字符串
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public String getHashIDString() {
        return HashIDUtil.toString(hashID);
    }

    /**
     * 判断两条路由信息HashID是否相同
     *
     * @param bytes HashID字节数组
     * @return boolean 两条路由信息HashID是否相同
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public boolean equalsByHashID(byte[] bytes) {
        return Arrays.equals(hashID, bytes);
    }

    /**
     * 显示路由信息
     *
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void show() {
        System.out.println("\tHashID: " + HashIDUtil.toString(hashID) + "  Host: " + host);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Arrays.equals(hashID, route.hashID) && Objects.equals(host, route.host);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(host);
        result = 31 * result + Arrays.hashCode(hashID);
        return result;
    }

    @Override
    public String toString() {
        return "Route{" +
                "hashID=" + HashIDUtil.toString(hashID) +
                ", host='" + host + '\'' +
                '}';
    }
}
