package cn.zhiskey.sfs.message;

import com.alibaba.fastjson.JSONObject;

/**
 * TODO: description
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class FindNodeMsg extends JSONObject {
    public FindNodeMsg(String hashID, String host) {
        put("type", "FIND_NODE");
        put("hashID", hashID);
        put("host", host);
    }

    public String getType() {
        return getString("type");
    }
}
