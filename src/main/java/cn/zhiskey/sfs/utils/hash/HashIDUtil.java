package cn.zhiskey.sfs.utils.hash;

import cn.zhiskey.sfs.utils.config.ConfigUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * TODO: description
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public enum  HashIDUtil {
    INSTANCE;

    private byte[] selfHashID = null;

    public static HashIDUtil getInstance() {
        return INSTANCE;
    }

    public void setSelfHashID(byte[] selfHashID) {
        this.selfHashID = selfHashID;
    }

    /**
     * 计算自己的HashID和指定的HashID的距离<br>
     * 此处的距离指异或后最长二进制公共前缀的长度
     *
     * @param hashID 指定的HashID
     * @return int 计算出的距离
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public int distance(byte[] hashID) {
        int res = 0;
        for(int i=0; i<hashID.length; i++) {
            byte tmp = (byte) (selfHashID[i] ^ hashID[i]);
            // 已找到最长前缀
            if(tmp != 0) {
                while(tmp == tmp<<1>>>1) {
                    res++;
                    tmp <<= 1;
                }
                break;
            }
            res += 8;
        }
        return res;
    }

    /**
     * 计算自己的HashID和指定的HashID的距离<br>
     * 此处的距离指异或后最长二进制公共前缀的长度
     *
     * @param hashID 指定的HashID字符串
     * @return int 计算出的距离
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public int distance(String hashID) {
        return distance(Base64.getDecoder().decode(hashID));
    }

    /**
     * 计算字节数组的HashID
     *
     * @param bytes 待计算HashID的字节数组
     * @return byte[] 得到的HashID值的字节数组
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static byte[] getHashID(byte[] bytes) {
        String hashType = ConfigUtil.getInstance().get("hashType", "SHA-256");
        return HashUtil.getHash(bytes, hashType);
    }

    /**
     * 计算字符串的HashID
     *
     * @param str 待计算HashID的字符串
     * @return byte[] 得到的HashID值的字节数组
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static byte[] getHashID(String str) {
        return getHashID(str.getBytes());
    }
}
