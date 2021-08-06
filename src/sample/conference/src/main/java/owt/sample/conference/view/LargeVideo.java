package owt.sample.conference.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import org.webrtc.EglBase;

import owt.sample.conference.R;

public class LargeVideo extends RelativeLayout {
    private ParticipantView participantView;

    public LargeVideo(Context context) {
        super(context);
        init();
    }

    public LargeVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LargeVideo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LargeVideo(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        View.inflate(getContext(), R.layout.view_large_video, this);
        participantView = findViewById(R.id.participant);
    }

    public ParticipantView getParticipantView() {
        return participantView;
    }

    public void initEgl(EglBase.Context eglBaseContext) {
        participantView.initEgl(eglBaseContext);
        participantView.setOnTop(false);
    }
}
