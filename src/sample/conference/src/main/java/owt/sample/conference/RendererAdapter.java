package owt.sample.conference;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
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
import java.util.List;

import owt.base.ActionCallback;
import owt.base.LocalStream;
import owt.base.RemoteStream;
import owt.base.Stream;
import owt.conference.Publication;
import owt.conference.Subscription;

public class RendererAdapter extends RecyclerView.Adapter<RendererAdapter.ViewHolder> {
    private static final String TAG = "RendererAdapter";
    private final List<Item> data = new ArrayList<>();
    private final EglBase rootEglBase;
    @NonNull
    private final SurfaceViewRenderer fullRenderer;
    @Nullable
    private Item selectedItem;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public RendererAdapter(EglBase rootEglBase, @NonNull SurfaceViewRenderer fullRenderer) {
        this.rootEglBase = rootEglBase;
        this.fullRenderer = fullRenderer;
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_remote_video, viewGroup, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Item item = data.get(i);
        Stream stream = item.stream;
        _detachStream(stream, item.renderer);
        item.renderer = viewHolder.renderer;
        _attackStream(stream, item.renderer);
        viewHolder.itemView.setOnClickListener(view -> {
            selectedItem = item;
            updateFullVideo();
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private void _attackStream(Stream stream, SurfaceViewRenderer renderer) {
        if (renderer == null) {
            Log.w(TAG, "_attackStream: renderer not found");
            return;
        }
        Stream oldStream = (Stream) renderer.getTag(R.id.tag_stream);
        if (stream != null) {
            if (oldStream != stream) {
                if (oldStream != null) {
                    safetyDetach(oldStream, renderer);
                }
                safetyAttach(stream, renderer);
                renderer.setTag(R.id.tag_stream, stream);
            }
        } else {
            if (oldStream != null) {
                safetyDetach(oldStream, renderer);
                renderer.setTag(R.id.tag_stream, null);
            }
        }
    }

    private void _detachStream(Stream stream, SurfaceViewRenderer renderer) {
        if (stream == null) {
            Log.w(TAG, "_detachStream: stream not found");
            return;
        }
        if (renderer == null) {
            Log.w(TAG, "_detachStream: renderer not found");
            return;
        }
        Stream oldStream = (Stream) renderer.getTag(R.id.tag_stream);
        if (oldStream == stream) {
            safetyDetach(stream, renderer);
            renderer.setTag(R.id.tag_stream, null);
        }
    }

    private void safetyAttach(Stream stream, @NonNull SurfaceViewRenderer renderer) {
        try {
            stream.attach(renderer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderer.setMirror(stream instanceof LocalStream);
    }

    private void safetyDetach(Stream stream, @NonNull SurfaceViewRenderer renderer) {
        renderer.clearImage();
        try {
            stream.detach(renderer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @WorkerThread
    public void attachLocalStream(String participantId, Publication publication, LocalStream stream) {
        Log.d(TAG, "attachLocalStream() called with: participantId = [" + participantId + "], publication = [" + publication.id() + "], stream = [" + stream.id() + "]");
        Item item = getOrCreateItem(participantId, stream);
        item.stream = stream;
        item.publication = publication;
        _attackStream(item.stream, item.renderer);
        updateFullVideo();
    }

    @WorkerThread
    public void attachRemoteStream(Subscription subscription, @NonNull RemoteStream stream) {
        Log.d(TAG, "attachRemoteStream() called with: subscription = [" + subscription.id + "], stream = [" + stream.id() + "]");
        Item item = getOrCreateItem(stream.origin(), stream);
        if (item.stream instanceof LocalStream) {
            Log.d(TAG, "attachRemoteStream: ignore local user");
            return;
        }
        item.stream = stream;
        item.participantId = stream.origin();
        item.subscription = subscription;
        _attackStream(item.stream, item.renderer);
        updateFullVideo();
    }

    @WorkerThread
    public void detachLocalStream(String participantId, LocalStream stream) {
        Log.d(TAG, "detachLocalStream() called with: participantId = [" + participantId + "], stream = [" + stream.id() + "]");
        Item item = getOrCreateItem(participantId, stream);
        _detachStream(item.stream, item.renderer);
        item.stream = null;
        if (item.publication != null) {
            item.publication.stop();
        }
        item.publication = null;
        updateFullVideo();
    }

    @WorkerThread
    public void detachRemoteStream(RemoteStream stream) {
        Log.d(TAG, "detachRemoteStream() called with: stream = [" + stream.id() + "]");
        Item item = getOrCreateItem(stream.origin(), stream);
        _detachStream(item.stream, item.renderer);
        item.stream = null;
        if (item.subscription != null) {
            item.subscription.stop();
            item.subscription = null;
        }
        updateFullVideo();
    }

    private void notifyItemIfExists(int pos) {
        if (pos < 0 || pos >= data.size()) {
            Log.e(TAG, String.format("notifyItemIfExists: out of range %d not in [0,%d]", pos, data.size() - 1));
            return;
        }
        notifyItemChanged(pos);
    }

    private int getIndexByParticipantId(String participantId) {
        for (int i = 0; i < data.size(); i++) {
            Item item = data.get(i);
            if (TextUtils.equals(item.participantId, participantId)) {
                return i;
            }
        }
        return -1;
    }

    private Item getOrCreateItem(String participantId, Stream stream) {
        Item item = getItemByParticipantId(participantId);
        if (item == null) {
            item = new Item(participantId, stream);
            Item finalItem = item;
            runOnUiThread(() -> {
                data.add(finalItem);
                notifyItemInserted(data.size() - 1);
            });
        }
        return item;
    }

    private boolean notUiThread() {
        return Looper.myLooper() != uiHandler.getLooper();
    }

    public final void runOnUiThread(Runnable action) {
        if (notUiThread()) {
            uiHandler.post(action);
        } else {
            action.run();
        }
    }

    @Nullable
    private Item getItemByParticipantId(String participantId) {
        Item item;
        int index = getIndexByParticipantId(participantId);
        if (index == -1) {
            item = null;
        } else {
            item = data.get(index);
        }
        return item;
    }

    @UiThread
    public void add(String participantId, @Nullable UserInfo userInfo) {
        Log.d(TAG, "add() called with: participantId = [" + participantId + "], userInfo = [" + userInfo + "]");
        int index = getIndexByParticipantId(participantId);
        if (index == -1) {
            Item item = new Item(participantId);
            item.userInfo = userInfo;
            data.add(item);
            notifyItemInserted(data.size() - 1);
            return;
        }
        Item item = data.get(index);
        item.userInfo = userInfo;
        notifyItemIfExists(index);
        updateFullVideo();
    }

    @UiThread
    public void update(@NonNull UserInfo userInfo) {
        Log.d(TAG, "update() called with: userInfo = [" + userInfo + "]");
        int index = getIndexByParticipantId(userInfo.getParticipantId());
        if (index == -1) {
            Log.w(TAG, "update: not found " + userInfo);
            return;
        }
        Item item = data.get(index);
        item.userInfo = userInfo;
        notifyItemIfExists(index);
        updateFullVideo();
    }

    @UiThread
    public void remove(String participantId, @Nullable UserInfo userInfo) {
        Log.d(TAG, "remove() called with: participantId = [" + participantId + "], userInfo = [" + userInfo + "]");
        int index = getIndexByParticipantId(participantId);
        if (index == -1) {
            Log.e(TAG, "remove: not found participantId = " + participantId);
            return;
        }
        Item item = data.remove(index);
        _detachStream(item.stream, item.renderer);
        item.stream = null;
        item.publication = null;
        item.subscription = null;
        item.renderer = null;
        notifyItemRemoved(index);
        updateFullVideo();
    }

    private void updateFullVideo() {
        if (selectedItem != null) {
            Item item = selectedItem;
            int index = data.indexOf(item);
            if (index != -1 && item.stream != null) {
                _attackStream(item.stream, fullRenderer);
                return;
            }
        }
        for (int i = data.size() - 1; i >= 0; i--) {
            Item item = data.get(i);
            if (item.stream != null) {
                _attackStream(item.stream, fullRenderer);
                return;
            }
        }
        Stream oldStream = (Stream) fullRenderer.getTag(R.id.tag_stream);
        _detachStream(oldStream, fullRenderer);
    }

    private void setVisibility(View view, int visibility) {
        if (notUiThread()) {
            uiHandler.post(() -> view.setVisibility(visibility));
        } else {
            view.setVisibility(visibility);
        }
    }

    public void onStop() {
        for (Item item : data) {
            _detachStream(item.stream, item.renderer);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void onStart() {
        notifyDataSetChanged();
    }

    public void getStatus(ActionCallback<RTCStatsReport> rtcStatsReportActionCallback) {
        if (selectedItem != null) {
            Item item = selectedItem;
            if (item.subscription != null) {
                item.subscription.getStats(rtcStatsReportActionCallback);
            }
        }
    }

    public static class Item {
        @NonNull
        private String participantId;
        @Nullable
        @SuppressWarnings("unused")
        private UserInfo userInfo;
        @Nullable
        private Stream stream;
        @Nullable
        private Publication publication;
        @Nullable
        private Subscription subscription;
        @Nullable
        private SurfaceViewRenderer renderer;

        public Item(@NonNull String participantId, @NonNull Stream stream) {
            this.participantId = participantId;
            this.stream = stream;
        }

        public Item(@NonNull String participantId) {
            this.participantId = participantId;
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
            renderer.setZOrderOnTop(true);
        }

    }
}
