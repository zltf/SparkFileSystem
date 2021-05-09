package cn.zhiskey.sfs.network;

import cn.zhiskey.sfs.utils.hash.HashIDUtil;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * TODO: description
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Bucket {
    private int distance;
    private Map<String, Route> routeMap;

    public Bucket(int distance) {
        this.distance = distance;
        // 使用Hashtable，线程安全
        routeMap = new Hashtable<>();
    }

    public void add(Route route) {
        routeMap.put(route.getHashIDString(), route);
    }

    public void remove(String hashID) {
        routeMap.remove(hashID);
    }

    public void remove(Route route) {
        remove(route.getHashIDString());
    }

    public int size() {
        return routeMap.size();
    }

    public Bucket splitSelf() {
        Bucket newBucket = new Bucket(distance+1);
        // 迭代器遍历HashMap中的route，寻找距离过大的节点
        Iterator<String> iterator = routeMap.keySet().iterator();
        while (iterator.hasNext()) {
            Route route = routeMap.get(iterator.next());
            int dis = HashIDUtil.getInstance().distance(route.getHashID());
            if(dis != distance) {
                // 添加到新桶
                newBucket.add(route);
                // 从旧桶移除
                iterator.remove();
            }
        }
        return newBucket;
    }

    public void lose() {
        // TODO 查找存储最多的节点丢弃

    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public Map<String, Route> getRouteMap() {
        return routeMap;
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "distance=" + distance +
                ", routeMap=" + routeMap +
                '}';
    }
}
