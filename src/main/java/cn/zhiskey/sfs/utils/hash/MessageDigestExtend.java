package cn.zhiskey.sfs.utils.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * TODO: description
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class MessageDigestExtend {

    private static final String BASIC_HASH_TYPE = "md5";

    private String algorithm;
    MessageDigest messageDigest;

    public MessageDigestExtend(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        this.messageDigest = MessageDigest.getInstance(algorithm.equals("md-test-1byte") ? BASIC_HASH_TYPE : algorithm);
    }

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
