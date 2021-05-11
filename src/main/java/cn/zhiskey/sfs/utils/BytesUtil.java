package cn.zhiskey.sfs.utils;

/**
 * byte[]和其他类型之间转换的工具类
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class BytesUtil {
    public static final int INT_BYTES_SIZE = 4;

    /**
     * int转byte[]<br>
     * 低位在前
     *
     * @param n 要转换的int
     * @return byte[] 转换结果
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static byte[] int2Bytes(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    /**
     * byte[]转int<br>
     * 低位在前
     *
     * @param bytes 要转换的byte[]
     * @return int 转换结果
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static int bytes2Int(byte[] bytes){
        int res = 0;
        for(int i=0;i<bytes.length;i++){
            res += (bytes[i] & 0xff) << (i*8);
        }
        return res;
    }
}
