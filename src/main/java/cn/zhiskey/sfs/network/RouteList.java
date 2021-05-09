package cn.zhiskey.sfs.network;

import cn.zhiskey.sfs.utils.config.ConfigUtil;
import cn.zhiskey.sfs.utils.hash.HashIDUtil;

import java.util.ArrayList;
import java.util.List;

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
        int distance = HashIDUtil.getInstance().distance(route.getHashID());
        Bucket bucket = bucketList.get(distance);
        bucketList.get(distance).add(route);
        int bucketSizeLimit = Integer.parseInt(ConfigUtil.getInstance().get("bucketSizeLimit"));
        if(bucket.size() > bucketSizeLimit) {
            bucket.lose();
        }
        System.out.println(this);
    }

    public void remove(int distance, String hashID) {
        Bucket bucket =  bucketList.get(distance);
        bucket.remove(hashID);
    }

    public Bucket getBucket(int distance) {
        return bucketList.get(distance);
    }

    public boolean containsRoute(String hashID) {
        int distance = HashIDUtil.getInstance().distance(hashID);
        Bucket bucket = getBucket(distance);
        return bucket.getRouteMap().containsKey(hashID);
    }

    @Override
    public String toString() {
        return "RouteList{" +
                "list=" + bucketList +
                '}';
    }
}
