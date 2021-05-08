package cn.zhiskey.sfs.network;

import java.util.Base64;

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
        return Base64.getEncoder().encodeToString(hashID);
    }
}
