package cn.zhiskey.sfs.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * TODO: description
 * 消息类<br>
 * 不安全
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Message {
    JSONObject jsonObject;

    public Message(String type) {
        jsonObject = new JSONObject();
        jsonObject.put("type", type);
    }

    private Message(JSONObject jsonObject) {
        if(jsonObject.containsKey("type")) {
            this.jsonObject = jsonObject;
        } else {
            new Exception("A message instance mast has key \"type\"").printStackTrace();
        }
    }

    public String getType() {
        return jsonObject.getString("type");
    }

    public void put(String key, Object value) {
        jsonObject.put(key, value);
    }

    public Object get(String key) {
        Object obj = jsonObject.get(key);
        if(obj == null) {
            new Exception("Can not find key \"" + key + "\"").printStackTrace();
        }
        return obj;
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public String toJSONString() {
        return jsonObject.toJSONString();
    }

    public static Message parseByJSON(String jsonString) {
        return new Message(JSON.parseObject(jsonString));
    }
}
