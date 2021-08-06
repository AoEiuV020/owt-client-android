package owt.sample.conference.view;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;

import owt.base.LocalStream;
import owt.base.Stream;
import owt.sample.conference.R;
import owt.sample.conference.UserInfo;

public class ParticipantView extends RelativeLayout {
    private static final String TAG = "ParticipantView";
    private SurfaceViewRenderer renderer;
    private Stream stream;
    @Nullable
    private UserInfo userInfo;
    private ImageView ivAvatar;
    private boolean onTop;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    public ParticipantView(Context context) {
        super(context);
        init();
    }

    public ParticipantView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ParticipantView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ParticipantView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public SurfaceViewRenderer getRenderer() {
        return renderer;
    }

    private void init() {
        View.inflate(getContext(), R.layout.view_participant, this);
        renderer = findViewById(R.id.renderer);
        ivAvatar = findViewById(R.id.ivAvatar);
    }

    public void initEgl(EglBase.Context eglBaseContext) {
        renderer.init(eglBaseContext, null);
        renderer.setMirror(true);
        renderer.setEnableHardwareScaler(true);
        renderer.setZOrderMediaOverlay(true);
    }

    public void setOnTop(boolean onTop) {
        this.onTop = onTop;
        renderer.setZOrderMediaOverlay(onTop);
    }

    public void attachStream(Stream stream) {
        Stream oldStream = this.stream;
        if (stream != null) {
            if (oldStream != stream) {
                if (oldStream != null) {
                    _detachStream();
                }
                _attachStream(stream);
            }
        } else {
            if (oldStream != null) {
                _detachStream();
            }
        }
    }

    public void detachStream(Stream stream) {
        Stream oldStream = this.stream;
        if (oldStream == stream) {
            _detachStream();
        }
    }

    private void _attachStream(Stream stream) {
        this.stream = stream;
        try {
            stream.attach(renderer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderer.setMirror(stream instanceof LocalStream);
        updateAvatar();
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

    private void updateAvatar() {
        Log.d(TAG, "updateAvatar() called: stream is null " + (stream == null));
        if (notUiThread()) {
            runOnUiThread(this::updateAvatar);
            return;
        }
        if (stream != null) {
            ivAvatar.setVisibility(View.GONE);
            renderer.setZOrderMediaOverlay(onTop);
        } else {
            ivAvatar.setVisibility(View.VISIBLE);
            renderer.setZOrderMediaOverlay(false);
        }
    }

    private void _detachStream() {
        renderer.clearImage();
        try {
            stream.detach(renderer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.stream = null;
        updateAvatar();
    }

    public void setUserInfo(@Nullable UserInfo userInfo) {
        if (this.userInfo == userInfo) {
            return;
        }
        this.userInfo = userInfo;
        runOnUiThread(() -> {
            if (userInfo == null || TextUtils.isEmpty(userInfo.getAvatarUrl())) {
                ivAvatar.setImageResource(R.drawable.default_avatar);
                return;
            }
            Log.d(TAG, "load avatar: url = [" + userInfo.getAvatarUrl() + "]");
            Glide.with(getContext()).load(userInfo.getAvatarUrl()).into(ivAvatar);
        });
    }

    public Stream getStream() {
        return stream;
    }
}
