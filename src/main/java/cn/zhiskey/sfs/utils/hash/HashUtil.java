package cn.zhiskey.sfs.utils.hash;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

/**
 * Hash工具类
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class HashUtil {
    /**
     * 计算字节数组的Hash值
     *
     * @param bytes 待计算Hash的字节数组
     * @param hashType Hash的类型
     * @return byte[] 得到的Hash值的字节数组
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static byte[] getHash(byte[] bytes, String hashType) {
        MessageDigestExtend messageDigestExtend;
        byte[] res = null;
        try {
            messageDigestExtend = new MessageDigestExtend(hashType);
            res = messageDigestExtend.digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 计算字符串的Hash值
     *
     * @param str 待计算Hash的字符串
     * @param hashType Hash的类型
     * @return byte[] 得到的Hash值的字节数组
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static byte[] getHash(String str, String hashType) {
        return getHash(str.getBytes(), hashType);
    }

    /**
     * 将字节数组转换为16进制字符串
     *
     * @param bytes 待转换的字节数组
     * @return java.lang.String 转换后的字符串
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static String bytes2Hex(byte[] bytes){
        StringBuilder stringBuilder = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                stringBuilder.append("0");
            }
            stringBuilder.append(temp);
        }
        return stringBuilder.toString();
    }
}
