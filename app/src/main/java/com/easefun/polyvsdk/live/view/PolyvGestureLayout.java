package com.easefun.polyvsdk.live.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.easefun.polyvsdk.live.video.PolyvLiveVideoView;

/**
 * ppt直播画板的父控件，用于传递手势事件给ppt播放器
 */
public class PolyvGestureLayout extends RelativeLayout {
    private PolyvLiveVideoView videoView;

    public PolyvGestureLayout(Context context) {
        super(context);
    }

    public PolyvGestureLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvGestureLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPolyvLiveVideoView(PolyvLiveVideoView videoView) {
        this.videoView = videoView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return videoView != null ? videoView.onPPTLiveTranTouchEvent(event, getMeasuredWidth()) : super.onTouchEvent(event);
    }
}
