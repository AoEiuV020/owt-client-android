/*
 * Copyright (C) 2018 Intel Corporation
 * SPDX-License-Identifier: Apache-2.0
 */
package owt.sample.conference.p2p;

import owt.base.ActionCallback;

/**
 * Interface for signaling channel implementation that P2PClient relies on for sending and
 * receiving data. Member methods and SignalingChannelObserver are expected to be implemented and
 * triggered by app level.
 */
public interface P2PSignalingChannelInterface {

    void connect(P2PSocket socketIOClient);

    /**
     * Send a message through signaling channel.
     */
    void sendMessage(String peerId, String message, ActionCallback<Void> callback);

    /**
     * Add a SignalingChannelObserver.
     */
    void addObserver(SignalingChannelObserver observer);

    /**
     * Remove a SignalingChannelObserver.
     */
    void removeObserver(SignalingChannelObserver observer);

    /**
     * Interface for observing signaling channel events.
     */
    interface SignalingChannelObserver {
        /**
         * Called upon receiving a message.
         *
         * @param peerId id of message sender.
         * @param message message received.
         */
        void onMessage(String peerId, String message);

    }
}
