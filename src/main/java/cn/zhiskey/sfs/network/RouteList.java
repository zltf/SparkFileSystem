package cn.zhiskey.sfs.network;

import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.hash.HashIDUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * TODO
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class RouteList {
    List<Bucket> bucketList;

    public RouteList() {
        int bucketCount = Integer.parseInt(ConfigUtil.getInstance().get("bucketCount"));
        // 创建路由桶列表
        bucketList = new ArrayList<>(bucketCount);
        // 初始化各个路由桶
        for (int i = 0; i < bucketCount; i++) {
            bucketList.add(new Bucket(i));
        }
    }

    public void add(Route route) {
        // 如果已经有该hashID，不允许写入新路由
        if(containsRoute(route.getHashIDString())) {
            return;
        }
        int cpl = HashIDUtil.getInstance().cpl(route.getHashID());
        Bucket bucket = bucketList.get(cpl);
        bucketList.get(cpl).add(route);
        int bucketSizeLimit = Integer.parseInt(ConfigUtil.getInstance().get("bucketSizeLimit"));
        if(bucket.size() > bucketSizeLimit) {
            bucket.lose();
        }
        System.out.println("\tnew route: " + route);
    }

    public void remove(int cpl, String hashID) {
        Bucket bucket =  bucketList.get(cpl);
        bucket.remove(hashID);
    }

    public Bucket getBucket(int cpl) {
        return bucketList.get(cpl);
    }

    public Route getRoute(String hashID) {
        int cpl = HashIDUtil.getInstance().cpl(hashID);
        Bucket bucket = bucketList.get(cpl);
        return bucket.getRoute(hashID);
    }

    public Route getRouteByHost(String host) {
        for (Bucket bucket : bucketList) {
            Route route = bucket.getRouteByHost(host);
            if(route != null) {
                return route;
            }
        }
        return null;
    }

    public boolean containsRoute(String hashID) {
        int cpl = HashIDUtil.getInstance().cpl(hashID);
        Bucket bucket = getBucket(cpl);
        return bucket.getRouteMap().containsKey(hashID);
    }

    /**
     * 从本地路由表寻找离hashID最近的count个节点返回
     *
     * @param hashID 搜索的源节点的hashID
     * @param count 要返回的节点个数
     * @return java.util.List<cn.zhiskey.sfs.network.Route> 搜素到的节点列表
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public List<Route> searchFromRouteList(String hashID, int count) {
        int cpl = HashIDUtil.getInstance().cpl(hashID);
        List<Route> resList = new ArrayList<>(count);

        int bucketCount = Integer.parseInt(ConfigUtil.getInstance().get("bucketCount"));

        // 先找距离大于（cpl相等）、等于（cpl变大）的节点
        int cplTemp = cpl;
        while (resList.size() < count && cplTemp < bucketCount) {
            searchFromBucket(hashID, count, resList, cplTemp++);
        }
        // 最后找距离小于（cpl变小）的节点
        cplTemp = cpl-1;
        while (resList.size() < count && cplTemp >= 0) {
            searchFromBucket(hashID, count, resList, cplTemp--);
        }
        return resList;
    }

    /**
     * 通过遍历桶中route加入搜索结果路由数组中，并保证结果数组大小不超过findPeerCount<br>
     * 该方法目的是减少重复代码
     *
     * @param hashID 要搜索的hashID
     * @param count 返回的路由个数
     * @param resList 写入结果的列表
     * @param cpl 桶的前缀数
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private void searchFromBucket(String hashID, int count, List<Route> resList, int cpl) {
        Bucket bucket = getBucket(cpl);
        // 遍历bucket中的route
        Map<String, Route> routeMap = bucket.getRouteMap();
        for (String key : routeMap.keySet()) {
            Route route = routeMap.get(key);

            if (resList.size() < count) {
                resList.add(route);
            } else {
                // 按cpl从小到大，即公共前缀从短到长
                resList.sort(Comparator.comparingInt(o -> HashIDUtil.cpl(o.getHashIDString(), hashID)));
                // 如果遇到更近的节点，替换最远的节点，公共前缀从短到长排列
                if (HashIDUtil.cpl(route.getHashIDString(), hashID)
                        > HashIDUtil.cpl(resList.get(0).getHashIDString(), hashID)) {
                    resList.set(0, route);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "RouteList{" +
                "list=" + bucketList +
                '}';
    }
}
