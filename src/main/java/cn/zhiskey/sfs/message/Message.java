package cn.zhiskey.sfs.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 消息类<br>
 * 内部封装了JSONObject对象作为消息载体<br>
 * 由于没有限制消息字段数的机制，安全性欠佳<br>
 * 使用阿里巴巴的fastjson项目完成Json的序列化和反序列化
 *
 * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
 */
public class Message {
    /**
     * 内部封装的JSONObject对象，作为消息载体，方便消息转换为Json字符串
     */
    JSONObject jsonObject;

    /**
     * 构造方法<br>
     * 构造每个类型的消息
     *
     * @param type 消息的类型 TODO 重构为枚举类型
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public Message(String type) {
        jsonObject = new JSONObject();
        jsonObject.put("type", type);
    }

    /**
     * 构造方法<br>
     * 将某一JSONObject对象封装为消息对象<br>
     * 注意：该JSONObject对象必须有type字段
     *
     * @param jsonObject 封装的JSONObject对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    private Message(JSONObject jsonObject) {
        if(jsonObject.containsKey("type")) {
            this.jsonObject = jsonObject;
        } else {
            new Exception("A message instance mast has key \"type\"").printStackTrace();
        }
    }

    /**
     * 获取消息的类型
     *
     * @return java.lang.String 消息的类型字符串
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public String getType() {
        return jsonObject.getString("type");
    }

    /**
     * 为消息添加一个字段
     *
     * @param key 字段的键
     * @param value 字段的值
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public void put(String key, Object value) {
        jsonObject.put(key, value);
    }

    /**
     * 获取某个字段的值<br>
     * 返回的是Object对象，需自己根据对象实际类型做强转
     *
     * @param key 字段的键
     * @return java.lang.Object 字段的值
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public Object get(String key) {
        Object obj = jsonObject.get(key);
        if(obj == null) {
            new Exception("Can not find key \"" + key + "\"").printStackTrace();
        }
        return obj;
    }

    /**
     * 获取字符串类型的字段值
     *
     * @param key 字段的键
     * @return java.lang.String 字段的值
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public String getString(String key) {
        return (String) get(key);
    }

    /**
     * 将消息序列化为Json格式的字符串，用于网络传输
     *
     * @return java.lang.String 消息的Json格式字符串
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public String toJSONString() {
        return jsonObject.toJSONString();
    }

    /**
     * 从Json格式字符串中解析出消息对象<br>
     * 由于调用了JSONObject参数的构造方法，Json字符串中必须有type字段
     *
     * @param jsonString Json格式的字符串
     * @return cn.zhiskey.sfs.message.Message 解析的消息对象
     * @author <a href="https://www.zhiskey.cn">Zhiskey</a>
     */
    public static Message parseByJSON(String jsonString) {
        return new Message(JSON.parseObject(jsonString));
    }
}
