package owt.sample.conference;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import java.lang.reflect.Type;

@SuppressWarnings("unused")
public class Message<T> {
    public static final int TYPE_USER_INFO = 100;
    private int type;
    private T data;

    public Message() {
    }

    public Message(int type, T data) {
        this.type = type;
        this.data = data;
    }

    public static <T> Message<T> fromJson(String json, Class<T> type) {
        return fromJson(json, (Type) type);
    }

    public static <T> Message<T> fromJson(String json, Type type) {
        return JSON.parseObject(json, new TypeReference<Message<T>>(type) {
        });
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
