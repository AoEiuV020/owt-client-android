package owt.sample.conference;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class Message {
    public static final int TYPE_USER_INFO = 100;
    public static final int TYPE_P2P_SIGNALING = 101;
    private int type;
    private String data;

    public Message() {
    }

    public Message(int type, String data) {
        this.type = type;
        this.data = data;
    }

    public static  Message fromJson(String json) {
        return JSON.parseObject(json, Message.class);
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", data=" + data +
                '}';
    }

    public String toJsonString() {
        return JSON.toJSONString(this);
    }

    public String getData() {
        return data;
    }

    public <T> T getDataBean(Class<T> type) {
        return JSON.parseObject(data, type);
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
