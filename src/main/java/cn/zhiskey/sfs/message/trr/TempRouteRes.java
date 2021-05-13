package cn.zhiskey.sfs.message.trr;

import cn.zhiskey.sfs.network.Route;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 用于存储需要多次查询的路由的状态 TODO
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public enum TempRouteRes {
    INSTANCE;

    private Map<String, TempRouteResItem> itemMap = new Hashtable<>();

    public static TempRouteRes getInstance() {
        return INSTANCE;
    }

    public void put(String key, TempRouteResItem item) {
        itemMap.put(key, item);
    }

    public List<Route> get(String key) {
        return itemMap.get(key).getResRouteList();
    }

    public void finish(String key) {
        itemMap.get(key).finish();
    }

    public void setSearchFinishStrategy(String key, CompleteStrategy searchFinishStrategy) {
        itemMap.get(key).setSearchFinishStrategy(searchFinishStrategy);
    }

    public void visit(String key, String host) {
        itemMap.get(key).visit(host);
    }

    public boolean isVisited(String key, String host) {
        return itemMap.get(key).isVisited(host);
    }
}
