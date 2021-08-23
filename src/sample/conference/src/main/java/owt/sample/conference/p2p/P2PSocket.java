package owt.sample.conference.p2p;

import com.alibaba.fastjson.JSON;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;
import owt.base.ActionCallback;
import owt.base.OwtError;
import owt.conference.ConferenceClient;
import owt.conference.Participant;
import owt.conference.RemoteStream;
import owt.sample.conference.Message;

public class P2PSocket implements ConferenceClient.ConferenceClientObserver {
    private ConferenceClient conferenceClient;
    private ConcurrentLinkedQueue<Emitter.Listener> listenerList = new ConcurrentLinkedQueue<>();

    public P2PSocket(ConferenceClient conferenceClient) {
        this.conferenceClient = conferenceClient;
        conferenceClient.addObserver(this);
    }

    public void on(Emitter.Listener fn) {
        listenerList.add(fn);
    }

    public void send(String peerId, String message, ActionCallback<Void> callback) {
        String json = JSON.toJSONString(new Message(Message.TYPE_P2P_SIGNALING, message));
        conferenceClient.send(peerId, json, callback);
    }

    @Override
    public void onStreamAdded(RemoteStream remoteStream) {
        // ignored
    }

    @Override
    public void onParticipantJoined(Participant participant) {
        // ignored
    }

    @Override
    public void onMessageReceived(String json, String from, String to) {
        Message message = Message.fromJson(json);
        if (message.getType() != Message.TYPE_P2P_SIGNALING) {
            return;
        }
        String data = message.getData();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("from", from);
            jsonObject.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (Emitter.Listener fn : listenerList) {
            fn.call(jsonObject);
        }
    }

    @Override
    public void onServerDisconnected() {
        // ignored
    }
}
