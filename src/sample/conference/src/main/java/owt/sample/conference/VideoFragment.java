/*
 * Copyright (C) 2018 Intel Corporation
 * SPDX-License-Identifier: Apache-2.0
 */
package owt.sample.conference;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.webrtc.EglBase;
import org.webrtc.RTCStats;
import org.webrtc.RTCStatsReport;

import java.math.BigInteger;
import java.util.Map;

import owt.sample.conference.view.LargeVideo;


public class VideoFragment extends Fragment {

    private VideoFragmentListener listener;
    private LargeVideo largeVideo;
    private RecyclerView rvRemote;
    private TextView statsInView, statsOutView;
    private BigInteger lastBytesSent = BigInteger.valueOf(0);
    private BigInteger lastBytesReceived = BigInteger.valueOf(0);
    private Long lastFrameDecoded = Long.valueOf(0);
    private Long lastFrameEncoded = Long.valueOf(0);
    private RendererAdapter adapter;

    public VideoFragment() {
    }

    public void setListener(VideoFragmentListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_video, container, false);

        statsInView = mView.findViewById(R.id.stats_in);
        statsInView.setVisibility(View.GONE);
        statsOutView = mView.findViewById(R.id.stats_out);
        statsOutView.setVisibility(View.GONE);

        largeVideo = mView.findViewById(R.id.largeVideo);
        rvRemote = mView.findViewById(R.id.rvSmallRenderer);
        EglBase.Context eglBaseContext = ((MainActivity) getActivity()).rootEglBase.getEglBaseContext();
        largeVideo.initEgl(eglBaseContext);

        adapter = new RendererAdapter(eglBaseContext, largeVideo.getParticipantView());
        rvRemote.setAdapter(adapter);

        listener.onAdapter(adapter);
        clearStats(true);
        clearStats(false);
        return mView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    void clearStats(boolean outbound) {
        final TextView statsView = outbound ? statsOutView : statsInView;
        if (outbound) {
            lastBytesSent = BigInteger.valueOf(0);
            lastFrameEncoded = Long.valueOf(0);
        } else {
            lastBytesReceived = BigInteger.valueOf(0);
            lastFrameDecoded = Long.valueOf(0);
        }
        final String statsReport = (outbound ? "\n--- OUTBOUND ---" : "\n--- INBOUND ---")
                + "\nCodec: "
                + "\nResolution: "
                + "\nBitrate: "
                + "\nFrameRate: ";
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statsView.setVisibility(View.VISIBLE);
                statsView.setText(statsReport);
            }
        });
    }

    void updateStats(RTCStatsReport report, boolean outbound) {
        final TextView statsView = outbound ? statsOutView : statsInView;
        String codecId = null;
        String codec = "";
        long bytesSR = 0;
        long width = 0, height = 0;
        long frameRate = 0;
        for (RTCStats stats : report.getStatsMap().values()) {
            if (stats.getType().equals(outbound ? "outbound-rtp" : "inbound-rtp")) {
                Map<String, Object> members = stats.getMembers();
                if (members.get("mediaType").equals("video")) {
                    codecId = (String) members.get("codecId");
                    if (outbound) {
                        BigInteger bytes = (BigInteger) members.get("bytesSent");
                        bytesSR = bytes.longValue() - lastBytesSent.longValue();
                        lastBytesSent = bytes;
                    } else {
                        BigInteger bytes = (BigInteger) members.get("bytesReceived");
                        bytesSR = bytes.longValue() - lastBytesReceived.longValue();
                        lastBytesReceived = bytes;
                    }

                    long currentFrame = (long) members.get(outbound ? "framesEncoded" : "framesDecoded");
                    long lastFrame = outbound ? lastFrameEncoded : lastFrameDecoded ;
                    frameRate = (currentFrame - lastFrame) * 1000
                            / MainActivity.STATS_INTERVAL_MS;
                    if (outbound) {
                        lastFrameEncoded = currentFrame;
                    } else {
                        lastFrameDecoded = currentFrame;
                    }
                }
            }
            if (stats.getType().equals("track")) {
                Map<String, Object> members = stats.getMembers();
                if (members.get("kind").equals("video")) {
                    width = members.get("frameWidth") == null ? 0 : (long) members.get(
                            "frameWidth");
                    height = members.get("frameHeight") == null ? 0 : (long) members.get(
                            "frameHeight");
                }
            }
        }
        if (codecId != null) {
            codec = (String) report.getStatsMap().get(codecId).getMembers().get("mimeType");
        }

        final String statsReport = (outbound ? "\n--- OUTBOUND ---" : "\n--- INBOUND ---")
                + "\nCodec: " + codec
                + "\nResolution: " + width + "x" + height
                + "\nBitrate: " + bytesSR * 8 / MainActivity.STATS_INTERVAL_MS + "kbps"
                + "\nFrameRate: " + frameRate;
        getActivity().runOnUiThread(() -> {
            statsView.setVisibility(View.VISIBLE);
            statsView.setText(statsReport);
        });
    }

    public interface VideoFragmentListener {
        void onAdapter(RendererAdapter adapter);
    }
}
