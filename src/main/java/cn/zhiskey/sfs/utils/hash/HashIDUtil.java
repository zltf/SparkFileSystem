package cn.zhiskey.sfs.utils.hash;

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
}
