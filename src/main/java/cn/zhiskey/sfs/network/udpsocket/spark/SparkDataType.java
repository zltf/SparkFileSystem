package cn.zhiskey.sfs.network.udpsocket.spark;

/**
 * Spark文件传输的类型
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public enum SparkDataType {
    /**
     * 推送Spark
     */
    PUSH_SPARK,

    /**
     * 下载Spark种子文件
     */
    DOWN_SEED_SPARK,

    /**
     * 下载普通Spark文件
     */
    DOWN_SPARK,
}
