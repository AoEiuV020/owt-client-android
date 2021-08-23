/*
 * Copyright (C) 2018 Intel Corporation
 * SPDX-License-Identifier: Apache-2.0
 */
package owt.sample.conference.p2p;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.socket.emitter.Emitter.Listener;
import owt.base.ActionCallback;
import owt.base.Const;

/**
 * Socket.IO implementation of P2P signaling channel.
 */
public class P2PSocketSignalingChannel implements P2PSignalingChannelInterface {
    private static final String TAG = "OWT-SocketClient";
    private final String CLIENT_CHAT_TYPE = "owt-message";
    private final String CLIENT_AUTHENTICATE_TYPE = "authentication";
    private final String FORCE_DISCONNECT = "server-disconnect";
    private final String CLIENT_TYPE = "&clientType=";
    private final String CLIENT_TYPE_VALUE = "Android";
    private final String CLIENT_VERSION = "&clientVersion=";
    private final String CLIENT_VERSION_VALUE = Const.CLIENT_VERSION;

    private final int MAX_RECONNECT_ATTEMPTS = 5;
    private int reconnectAttempts = 0;
    private P2PSocket socketIOClient;
    private List<SignalingChannelObserver> signalingChannelObservers;
    private ActionCallback<String> connectCallback;

    private String userInfo = null;

    private Listener onMessageCallback = args -> {
        JSONObject argumentJsonObject = (JSONObject) args[0];
        for (SignalingChannelObserver observer : signalingChannelObservers) {
            try {
                observer.onMessage(argumentJsonObject.getString("from"),
                        argumentJsonObject.getString("data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    P2PSocketSignalingChannel() {
        this.signalingChannelObservers = new ArrayList<>();
    }

    @Override
    public void addObserver(SignalingChannelObserver observer) {
        this.signalingChannelObservers.add(observer);
    }

    @Override
    public void removeObserver(SignalingChannelObserver observer) {
        this.signalingChannelObservers.remove(observer);
    }

    @Override
    public void connect(P2PSocket socketIOClient) {
        this.socketIOClient = socketIOClient;
        socketIOClient.on(onMessageCallback);
    }

    private boolean isValid(String urlString) {
        try {
            URL url = new URL(urlString);
            return url.getPort() <= 65535;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    @Override
    public void sendMessage(String peerId, String message, final ActionCallback<Void> callback) {
        if (socketIOClient == null) {
            Log.d(TAG, "socketIOClient is not established.");
            return;
        }
        socketIOClient.send(peerId, message, callback);
    }
}
