package cn.zhiskey.sfs.message.trr;

import cn.zhiskey.sfs.network.Route;

import java.util.List;
import java.util.Vector;

/**
 * TODO: description
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class TempRouteResItem {
    List<Route> resRouteList;
    List<String> visitedList;
    CompleteStrategy searchFinishStrategy;

    public TempRouteResItem(CompleteStrategy searchFinishStrategy) {
        // 线程安全
        this.resRouteList = new Vector<>();
        this.visitedList = new Vector<>();
        this.searchFinishStrategy = searchFinishStrategy;
    }

    public void finish() {
        searchFinishStrategy.finish(resRouteList);
    }

    public void visit(String host) {
        if(!isVisited(host)) {
            visitedList.add(host);
        }
    }

    public boolean isVisited(String host) {
        return visitedList.contains(host);
    }

    public List<Route> getResRouteList() {
        return resRouteList;
    }

    public void setResRouteList(List<Route> resRouteList) {
        this.resRouteList = resRouteList;
    }

    public CompleteStrategy getSearchFinishStrategy() {
        return searchFinishStrategy;
    }

    public void setSearchFinishStrategy(CompleteStrategy searchFinishStrategy) {
        this.searchFinishStrategy = searchFinishStrategy;
    }
}
