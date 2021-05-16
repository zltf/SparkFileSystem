package cn.zhiskey.sfs.network;

import cn.zhiskey.sfs.utils.hash.HashIDUtil;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * 路由桶类
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Bucket {
    /**
     * 该桶中路由信息与节点的距离<br>
     * HashID最长公共前缀越长，距离越近
     */
    private int cpl;

    /**
     * 桶中路由信息的Map，键为节点HashID，值为主机地址
     */
    private final Map<String, Route> routeMap;

    /**
     * 构造方法<br>
     * 传入该桶的cpl值
     *
     * @param cpl 该桶的cpl值
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public Bucket(int cpl) {
        this.cpl = cpl;
        // 使用Hashtable，线程安全
        routeMap = new Hashtable<>();
    }

    /**
     * 向桶中增加一条路由信息
     *
     * @param route 增加的路由信息对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void add(Route route) {
        routeMap.put(route.getHashIDString(), route);
    }

    /**
     * 删除桶中某一路由信息
     *
     * @param hashID 删除节点的HashID
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void remove(String hashID) {
        routeMap.remove(hashID);
    }

    /**
     * 删除桶中某一路由信息
     *
     * @param route 删除节点的路由信息对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void remove(Route route) {
        remove(route.getHashIDString());
    }

    /**
     * 获取桶中路由信息的数量
     *
     * @return int 桶中路由信息的数量
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public int size() {
        return routeMap.size();
    }

    public Bucket splitSelf() {
        Bucket newBucket = new Bucket(cpl+1);
        // 迭代器遍历HashMap中的route，寻找前缀长较大的节点
        Iterator<String> iterator = routeMap.keySet().iterator();
        while (iterator.hasNext()) {
            Route route = routeMap.get(iterator.next());
            int dis = HashIDUtil.getInstance().cpl(route.getHashID());
            if(dis != cpl) {
                // 添加到新桶
                newBucket.add(route);
                // 从旧桶移除
                iterator.remove();
            }
        }
        return newBucket;
    }

    /**
     * 丢弃超过数量限制的节点<br>
     * TODO: 暂缓完成
     *
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void lose() {}

    /**
     * 获取该桶的cpl值
     *
     * @return int 该桶的cpl值
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public int getCpl() {
        return cpl;
    }

    /**
     * 设置该桶的cpl值
     *
     * @param cpl 新的cpl值
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void setCpl(int cpl) {
        this.cpl = cpl;
    }

    /**
     * 获取路由信息Map的对象
     *
     * @return java.util.Map<java.lang.String,cn.zhiskey.sfs.network.Route> 路由信息Map的对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public Map<String, Route> getRouteMap() {
        return routeMap;
    }

    /**
     * 获取桶中某条路由信息
     *
     * @param hashID 路由节点的HashID
     * @return cn.zhiskey.sfs.network.Route 路由信息对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public Route getRoute(String hashID) {
        return routeMap.get(hashID);
    }

    /**
     * 通过节点主机地址获取其路由信息
     *
     * @param host 节点的主机地址
     * @return cn.zhiskey.sfs.network.Route 路由信息对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public Route getRouteByHost(String host) {
        for (String key : routeMap.keySet()) {
            if(routeMap.get(key).getHost().equals(host)) {
                return routeMap.get(key);
            }
        }
        return null;
    }

    /**
     * 显示桶中的所有路由信息
     *
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void show() {
        if(routeMap.size() > 0) {
            System.out.println("cpl = " + cpl);
            for (String key : routeMap.keySet()) {
                routeMap.get(key).show();
            }
            System.out.println();
        }
    }

    @Override
    public String toString() {
        return "Bucket{" +
                "cpl=" + cpl +
                ", routeMap=" + routeMap +
                '}';
    }
}
