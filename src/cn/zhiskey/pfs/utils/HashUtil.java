package cn.zhiskey.pfs.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 计算Hash值
 *
 * @author Zhiskey
 */
public class HashUtil {
    /**
     * 计算字节数组的Hash值
     *
     * @param bytes 待计算Hash的字节数组
     * @param hashType Hash的类型
     * @return byte[] 得到的Hash值的字节数组
     * @author Zhiskey
     */
    public static byte[] getHash(byte[] bytes, String hashType) {
        MessageDigest messageDigest;
        byte[] res = null;
        try {
            messageDigest = MessageDigest.getInstance(hashType);
            messageDigest.update(bytes);
            res = messageDigest.digest();
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
     * @author Zhiskey
     */
    public static byte[] getHash(String str, String hashType) {
        return getHash(str.getBytes(StandardCharsets.UTF_8), hashType);
    }

    /**
     * 将字节数组转换为16进制字符串
     *
     * @param bytes 待转换的字节数组
     * @return java.lang.String 转换后的字符串
     * @author Zhiskey
     */
    private static String bytes2Hex(byte[] bytes){
        StringBuilder stringBuffer = new StringBuilder();
        String temp;
        for (byte aByte : bytes) {
            temp = Integer.toHexString(aByte & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }
}
