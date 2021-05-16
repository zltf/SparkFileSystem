package cn.zhiskey.sfs.utils.udpsocket.spark;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * TODO: description
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public enum TempSparkList {
    INSTANCE;

    private Map<String, List<String>> sparkListMap;

    TempSparkList() {
        sparkListMap = new Hashtable<>();
    }

    public static TempSparkList getInstance() {
        return INSTANCE;
    }

    public List<String> get(String seedHashID) {
        return sparkListMap.get(seedHashID);
    }

    public void put(String seedHashID, List<String> list) {
        sparkListMap.put(seedHashID, list);
    }

    public void put(String seedHashID, String sparkHashID) {
        // 如果seedHashID对应的list为空，将new Vector<>()存入
        List<String> list = sparkListMap.computeIfAbsent(seedHashID, k -> new Vector<>());
        list.add(sparkHashID);
    }

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
