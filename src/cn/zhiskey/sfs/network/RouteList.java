package cn.zhiskey.sfs.network;

import cn.zhiskey.sfs.utils.config.ConfigUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class RouteList {
    List<Bucket> list;
    Bucket curBucket;

    public RouteList() {
        // 创建路由桶列表
        list = new ArrayList<Bucket>(Integer.valueOf(ConfigUtil.getInstance().get("bucketCount")));
        // 初始只有一个距离为0的桶
        curBucket = new Bucket(0);
        list.add(curBucket);
    }

    public void add(Route route) {
        curBucket.add(route);
        int bucketSizeLimit = Integer.valueOf(ConfigUtil.getInstance().get("bucketSizeLimit"));
        while(curBucket.size() > bucketSizeLimit) {
            curBucket = curBucket.splitSelf();
            list.add(curBucket);
        }
    }
}
