package cn.zhiskey.sfs.message;

import cn.zhiskey.sfs.network.Route;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * TODO: description
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public enum TempRouteRes {
    INSTANCE;

    private Map<String, List<Route>> routeMap = new Hashtable<>();

    public static TempRouteRes getInstance() {
        return INSTANCE;
    }

    public void put(String key, List<Route> value) {
        routeMap.put(key, value);
    }

    public List<Route> get(String key) {
        return routeMap.get(key);
    }
}
