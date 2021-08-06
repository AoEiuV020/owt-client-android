package owt.sample.conference.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import org.webrtc.EglBase;
import org.webrtc.SurfaceViewRenderer;

import owt.base.LocalStream;
import owt.base.Stream;
import owt.sample.conference.R;

public class ParticipantView extends RelativeLayout {
    private SurfaceViewRenderer renderer;
    private Stream stream;
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
    }

    public void initEgl(EglBase.Context eglBaseContext) {
        renderer.init(eglBaseContext, null);
        renderer.setMirror(true);
        renderer.setEnableHardwareScaler(true);
        renderer.setZOrderMediaOverlay(true);
    }

    public void setZOrderOnTop(boolean b) {
        renderer.setZOrderOnTop(b);
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
    }

    private void _detachStream() {
        renderer.clearImage();
        try {
            stream.detach(renderer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.stream = null;
    }

}
