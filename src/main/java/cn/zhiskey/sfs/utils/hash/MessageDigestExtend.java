package cn.zhiskey.sfs.utils.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash类型扩展类<br>
 * 扩展了1字节Hash：md-test-1byte
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class MessageDigestExtend {
    /**
     * 源Hash类型
     */
    private static final String BASIC_HASH_TYPE = "md5";

    /**
     * Hash类型字符串
     */
    private final String algorithm;

    /**
     * MessageDigest对象
     */
    MessageDigest messageDigest;

    /**
     * 构造方法<br>
     * 判断是原生Hash类型还是扩展的类型
     *
     * @param algorithm Hash类型字符串
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public MessageDigestExtend(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        this.messageDigest = MessageDigest.getInstance(algorithm.equals("md-test-1byte") ? BASIC_HASH_TYPE : algorithm);
    }

    /**
     * 根据不同类型生成输入字节数组的Hash值
     *
     * @param input 输入的字节数组
     * @return byte[] Hash的结果
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public byte[] digest(byte[] input) {
        byte[] resTemp = messageDigest.digest(input);
        byte[] res;
        if(algorithm.equals("md-test-1byte")) {
            res = new byte[1];
            res[0] = resTemp[0];
        } else {
            res = resTemp;
        }
        return res;
    }
}
