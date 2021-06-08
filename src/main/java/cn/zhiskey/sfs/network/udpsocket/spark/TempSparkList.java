package cn.zhiskey.sfs.network.udpsocket.spark;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * 记录目前在下载的文件需要的Spark文件，方便在文件接收完成后恢复原文件<br>
 * 单例模式，枚举实现
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public enum TempSparkList {
    /**
     * 实例
     */
    INSTANCE;

    /**
     * 存储信息的Map对象<br>
     * 键为Spark种子文件HashID<br>
     * 值为需要的Spark文件HashID列表
     */
    private final Map<String, List<String>> sparkListMap = new Hashtable<>();

    /**
     * 获取本类实例
     * 兼容传统的单例模式实例获取方式
     *
     * @return cn.zhiskey.sfs.network.udpsocket.spark.TempSparkList 本类实例
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static TempSparkList getInstance() {
        return INSTANCE;
    }

    /**
     * 通过Spark种子的HashID获取其需要的Spark文件HashID列表
     *
     * @param seedHashID Spark种子的HashID
     * @return java.util.List<java.lang.String> 需要的Spark文件HashID列表
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public List<String> get(String seedHashID) {
        return sparkListMap.get(seedHashID);
    }

    /**
     * 设置某一种子文件需要的Spark文件HashID列表
     *
     * @param seedHashID 种子文件HashID
     * @param list 需要的Spark文件HashID列表
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void put(String seedHashID, List<String> list) {
        sparkListMap.put(seedHashID, list);
    }

    /**
     * 增加某一种子文件需要的Spark文件HashID列表项目<br>
     * 如果该列表尚未创建，则先创建该列表
     *
     * @param seedHashID 种子文件HashID
     * @param sparkHashID 需要的Spark文件HashID列表
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void put(String seedHashID, String sparkHashID) {
        // 如果seedHashID对应的list为空，将new Vector<>()存入
        List<String> list = sparkListMap.computeIfAbsent(seedHashID, k -> new Vector<>());
        list.add(sparkHashID);
    }

    /**
     * 检查当前是否有文件的所有Spark文件已经下载完成
     *
     * @param sparkFileList 节点当前保存的Spark文件列表
     * @return java.lang.String 已完成的文件的种子文件HashID
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public String check(List<String> sparkFileList) {
        for (String seedHashID : sparkListMap.keySet()) {
            boolean finFlag = true;
            for (String hashID : sparkListMap.get(seedHashID)) {
                if (!sparkFileList.contains(hashID)) {
                    finFlag = false;
                    break;
                }
            }
            if(finFlag) {
                sparkListMap.remove(seedHashID);
                return seedHashID;
            }
        }
        return null;
    }
}
