package owt.sample.conference.p2p;

import static owt.base.MediaCodecs.VideoCodec.H264;
import static owt.base.MediaCodecs.VideoCodec.H265;
import static owt.base.MediaCodecs.VideoCodec.VP8;

import android.text.TextUtils;
import android.util.Log;

import java.util.Objects;

import owt.base.ActionCallback;
import owt.base.LocalStream;
import owt.base.OwtError;
import owt.base.VideoEncodingParameters;
import owt.conference.ConferenceInfo;
import owt.conference.Participant;

public class P2PHelper {
    public static final String TAG = "P2PHelper";
    private P2PClient p2PClient;
    private boolean enabled;
    private ConferenceInfo conferenceInfo;
    private LocalStream localStream;

    public void onConnected() {
    }

    public void onServerDisconnected() {
        p2PClient.onServerDisconnected();
    }

    public void initClient(P2PClient.P2PClientObserver observer) {
        VideoEncodingParameters h264 = new VideoEncodingParameters(H264);
        VideoEncodingParameters h265 = new VideoEncodingParameters(H265);
        VideoEncodingParameters vp8 = new VideoEncodingParameters(VP8);
        P2PClientConfiguration configuration = P2PClientConfiguration.builder()
                .addVideoParameters(h264)
                .addVideoParameters(vp8)
                .addVideoParameters(h265)
                .build();
        p2PClient = new P2PClient(configuration, new P2PSocketSignalingChannel());
        p2PClient.addObserver(observer);
    }

    public void onParticipantJoined(Participant participant) {
        updateEnabled();
        p2PClient.addAllowedRemotePeer(participant.id);
//        publish(participant);
    }

    public void onJoinSuccess(ConferenceInfo conferenceInfo, P2PSocket p2PSocket) {
        this.conferenceInfo = conferenceInfo;
        updateEnabled();
        for (Participant participant : conferenceInfo.getParticipants()) {
            p2PClient.addAllowedRemotePeer(participant.id);
        }
        p2PClient.connect(p2PSocket, conferenceInfo.self().id);
    }

    private void updateEnabled() {
        setEnabled(conferenceInfo.getParticipants().size() <= 2);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void publish(LocalStream localStream) {
        this.localStream = localStream;
        if (!enabled) {
            return;
        }
        for (Participant participant : conferenceInfo.getParticipants()) {
            if (!TextUtils.equals(participant.id, conferenceInfo.self().id)) {
                publish(participant);
            }
        }
    }

    private void publish(Participant participant) {
        if (!enabled) {
            return;
        }
        Log.d(TAG, "publish() called with: participant = [" + participant.id + "]");
        p2PClient.addAllowedRemotePeer(participant.id);
        p2PClient.publish(participant.id, localStream, new ActionCallback<P2PPublication>() {
            @Override
            public void onSuccess(P2PPublication result) {
                Log.d(TAG, "onSuccess() called with: result = [" + result + "]");
            }

            @Override
            public void onFailure(OwtError error) {
                Log.d(TAG, "onFailure() called with: error = [" + error.errorMessage + "]");
            }
        });
    }
}
