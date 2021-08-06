package owt.sample.conference.view;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import org.webrtc.EglBase;

import owt.sample.conference.R;

public class Thumbnail extends RelativeLayout {
    private ParticipantView participantView;

    public Thumbnail(Context context) {
        super(context);
        init();
    }

    public Thumbnail(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Thumbnail(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Thumbnail(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public ParticipantView getParticipantView() {
        return participantView;
    }

    private void init() {
        View.inflate(getContext(), R.layout.view_tumbnail, this);
        participantView = findViewById(R.id.participant);
    }

    public void initEgl(EglBase.Context eglBaseContext) {
        participantView.initEgl(eglBaseContext);
        participantView.setZOrderOnTop(true);
    }
}
