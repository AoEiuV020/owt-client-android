package owt.sample.conference;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.WorkerThread;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.webrtc.EglBase;
import org.webrtc.RTCStatsReport;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import owt.base.ActionCallback;
import owt.base.Stream;
import owt.conference.RemoteStream;
import owt.conference.Subscription;

public class RendererAdapter extends RecyclerView.Adapter<RendererAdapter.ViewHolder> {
    private static final String TAG = "RendererAdapter";
    private List<UserInfo> data = new ArrayList<>();
    private Map<String, Stream> streamMap = new HashMap<>();
    private Map<String, Subscription> subscriptionMap = new HashMap<>();
    private Map<String, SurfaceViewRenderer> rendererMap = new HashMap<>();
    private EglBase rootEglBase;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public RendererAdapter(EglBase rootEglBase) {
        this.rootEglBase = rootEglBase;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_remote_video, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        UserInfo userInfo = data.get(i);
        Stream stream = streamMap.get(userInfo.getParticipantId());
        SurfaceViewRenderer renderer = viewHolder.renderer;
        rendererMap.put(userInfo.getParticipantId(), renderer);
        _attackStream(userInfo.getParticipantId(), stream, renderer);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void _attackStream(String participantId, Stream stream, SurfaceViewRenderer renderer) {
        if (renderer == null) {
            Log.w(TAG, "_attackStream: renderer not found participantId = " + participantId);
            return;
        }
        Stream oldStream = (Stream) renderer.getTag(R.id.tag_stream);
        if (stream != null) {
            if (oldStream != stream) {
                if (oldStream != null) {
                    oldStream.detach(renderer);
                }
                stream.attach(renderer);
                renderer.setTag(R.id.tag_stream, stream);
            }
        } else {
            if (oldStream != null) {
                oldStream.detach(renderer);
            }
        }
    }

    private void _detachStream(String participantId, Stream stream, SurfaceViewRenderer renderer) {
        if (stream == null) {
            Log.w(TAG, "_detachStream: stream not found participantId = " + participantId);
            return;
        }
        if (renderer == null) {
            Log.w(TAG, "_detachStream: renderer not found participantId = " + participantId);
            return;
        }
        Stream oldStream = (Stream) renderer.getTag(R.id.tag_stream);
        if (oldStream == stream) {
            stream.detach(renderer);
            renderer.setTag(R.id.tag_stream, null);
        }
    }

    @WorkerThread
    public void attachStream(String participantId, Stream stream) {
        streamMap.put(participantId, stream);
        SurfaceViewRenderer renderer = rendererMap.get(participantId);
        _attackStream(participantId, stream, renderer);
        mainHandler.post(() -> {
            notifyItemIfExists(getIndexById(participantId));
        });
    }

    @WorkerThread
    public void attachRemoteStream(String participantId, Subscription subscription, RemoteStream remoteStream) {
        subscriptionMap.put(participantId, subscription);
        attachStream(participantId, remoteStream);
    }

    @WorkerThread
    public void detachStream(String participantId) {
        Stream stream = streamMap.remove(participantId);
        SurfaceViewRenderer renderer = rendererMap.get(participantId);
        _detachStream(participantId, stream, renderer);
        mainHandler.post(() -> {
            notifyItemIfExists(getIndexById(participantId));
        });
    }

    @WorkerThread
    public void detachRemoteStream(String participantId) {
        detachStream(participantId);
        Subscription subscription = subscriptionMap.remove(participantId);
        if (subscription != null) {
            subscription.stop();
        }
    }

    public void detachAllRemoteStream(String selfParticipantId) {
        for (UserInfo userInfo : data) {
            if (TextUtils.equals(userInfo.getParticipantId(), selfParticipantId)) {
                continue;
            }
            detachRemoteStream(userInfo.getParticipantId());
        }
    }

    private void notifyItemIfExists(int pos) {
        if (pos < 0 || pos >= data.size()) {
            Log.e(TAG, String.format("notifyItemIfExists: out of range %d not in [0,%d]", pos, data.size() - 1));
            return;
        }
        notifyItemChanged(pos);
    }

    private int getIndexById(String participantId) {
        for (int i = 0; i < data.size(); i++) {
            UserInfo userInfo = data.get(i);
            if (TextUtils.equals(userInfo.getParticipantId(), participantId)) {
                return i;
            }
        }
        return -1;
    }

    public void add(UserInfo userInfo) {
        data.add(userInfo);
        notifyItemInserted(data.size() - 1);
    }

    public void update(UserInfo userInfo) {
        int index = getIndexById(userInfo.getParticipantId());
        if (index == -1) {
            Log.e(TAG, "update: not found" + userInfo);
            add(userInfo);
            return;
        }
        data.set(index, userInfo);
        notifyItemIfExists(index);
    }

    public void remove(UserInfo userInfo) {
        int index = getIndexById(userInfo.getParticipantId());
        if (index == -1) {
            Log.e(TAG, "remove: not found" + userInfo);
            return;
        }
        data.remove(index);
        streamMap.remove(userInfo.getParticipantId());
        notifyItemRemoved(index);
    }

    public void onStop() {
        for (UserInfo userInfo : data) {
            _detachStream(userInfo.getParticipantId(),
                    streamMap.get(userInfo.getParticipantId()),
                    rendererMap.get(userInfo.getParticipantId())
            );
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void onStart() {
        notifyDataSetChanged();
    }

    public void getStatus(ActionCallback<RTCStatsReport> rtcStatsReportActionCallback) {
        Collection<Subscription> subscriptions = subscriptionMap.values();
        if (!subscriptions.isEmpty()) {
            for (Subscription subscription : subscriptions) {
                subscription.getStats(rtcStatsReportActionCallback);
                break;
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final SurfaceViewRenderer renderer = itemView.findViewById(R.id.renderer);

        public ViewHolder(View itemView) {
            super(itemView);
            renderer.init(rootEglBase.getEglBaseContext(), null);
            renderer.setMirror(true);
            renderer.setEnableHardwareScaler(true);
            renderer.setZOrderMediaOverlay(true);
        }

    }
}
