/*
 * Copyright (C) 2018 Intel Corporation
 * SPDX-License-Identifier: Apache-2.0
 */
package owt.sample.conference.p2p;

import org.webrtc.MediaStream;

public final class P2PRemoteStream extends owt.base.RemoteStream {

    P2PRemoteStream(String origin, MediaStream mediaStream) {
        super(mediaStream.getId(), origin);
        this.mediaStream = mediaStream;
    }

    void setInfo(StreamSourceInfo streamSourceInfo) {
        setStreamSourceInfo(streamSourceInfo);
    }

    void onEnded() {
        triggerEndedEvent();
    }
}
