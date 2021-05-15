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
     * 计算两个HashID的前缀长<br>
     * 此处的前缀长指异或后最长二进制公共前缀的长度
     *
     * @param hashID1 指定的HashID1
     * @param hashID2 指定的HashID2
     * @return int 计算出的前缀长
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static int cpl(byte[] hashID1, byte[] hashID2) {
        int res = 0;
        for(int i=0; i<hashID1.length; i++) {
            byte tmp = (byte) (hashID1[i] ^ hashID2[i]);
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
     * 计算自己的HashID和指定的HashID的前缀长<br>
     * 此处的前缀长指异或后最长二进制公共前缀的长度
     *
     * @param hashID 指定的HashID
     * @return int 计算出的前缀长
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public int cpl(byte[] hashID) {
        return cpl(selfHashID, hashID);
    }

    /**
     * 计算两个HashID的前缀长<br>
     * 此处的前缀长指异或后最长二进制公共前缀的长度
     *
     * @param hashID1 指定的HashID1字符串
     * @param hashID2 指定的HashID2字符串
     * @return int 计算出的前缀长
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static int cpl(String hashID1, String hashID2) {
        return cpl(toBytes(hashID1), toBytes(hashID2));
    }

    /**
     * 计算自己的HashID和指定的HashID的前缀长<br>
     * 此处的前缀长指异或后最长二进制公共前缀的长度
     *
     * @param hashID 指定的HashID字符串
     * @return int 计算出的前缀长
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public int cpl(String hashID) {
        return cpl(toBytes(hashID));
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

    /**
     * 将字节数组hashID转换为字符串hashID<br>
     * Base64替换“/”
     *
     * @param hashID 字节数组hashID
     * @return java.lang.String 字符串hashID
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static String toString(byte[] hashID) {
        String res = Base64.getEncoder().encodeToString(hashID);
        return res.replace('/', '-');
    }

    /**
     * 将字符串hashID转换为字节数组hashID<br>
     * Base64替换“/”
     *
     * @param hashID 字符串hashID
     * @return byte[] 字节数组hashID
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static byte[] toBytes(String hashID) {
        hashID = hashID.replace('-', '/');
        return Base64.getDecoder().decode(hashID);
    }
}
