package cn.zhiskey.sfs.network;

import cn.zhiskey.sfs.utils.hash.HashIDUtil;
import cn.zhiskey.sfs.utils.hash.HashUtil;

import java.util.Arrays;
import java.util.Objects;

/**
 * TODO: description
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Route {
    private byte[] hashID;
    private String host;

    public Route(byte[] hashID, String host) {
        this.hashID = hashID;
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    public byte[] getHashID() {
        return hashID;
    }

    public String getHashIDString() {
        return HashIDUtil.toString(hashID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Arrays.equals(hashID, route.hashID) && Objects.equals(host, route.host);
    }

    public boolean equalsByHashID(byte[] bytes) {
        return Arrays.equals(hashID, bytes);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(host);
        result = 31 * result + Arrays.hashCode(hashID);
        return result;
    }

    public void show() {
        System.out.println("\tHashID: " + HashIDUtil.toString(hashID) + "  Host: " + host);
    }

    @Override
    public String toString() {
        return "Route{" +
                "hashID=" + HashIDUtil.toString(hashID) +
                ", host='" + host + '\'' +
                '}';
    }
}
