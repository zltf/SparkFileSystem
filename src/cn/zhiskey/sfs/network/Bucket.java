package cn.zhiskey.sfs.network;

import cn.zhiskey.sfs.utils.HashIDUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * TODO: description
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Bucket {
    private int distance;
    Map<String, Route> routeMap;

    public Bucket(int distance) {
        this.distance = distance;
        routeMap = new HashMap<>();
    }

    public void add(Route route) {
        routeMap.put(route.getHashIDString(), route);
    }

    public void remove(Route route) {
        routeMap.remove(route.getHashIDString());
    }

    public int size() {
        return routeMap.size();
    }

    public Bucket splitSelf() {
        Bucket newBucket = new Bucket(distance+1);
        // 遍历HashMap中的route，寻找距离过大的节点
        for(Map.Entry<String, Route> routeEntry : routeMap.entrySet()) {
            Route route = routeEntry.getValue();
            int dis = HashIDUtil.getInstance().distance(route.getHashID());
            if(dis != distance) {
                // 添加到新桶
                newBucket.add(route);
                // 从旧桶移除
                remove(route);
            }
        }
        return newBucket;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

}
