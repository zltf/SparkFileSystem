package cn.zhiskey.sfs.message.trr;

import cn.zhiskey.sfs.network.Route;

import java.util.List;

/**
 * TODO: description
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public interface CompleteStrategy {
    void finish(List<Route> res);
}
