package com.easefun.polyvsdk.live.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import com.easefun.polyvsdk.live.video.PolyvLiveVideoView;
import com.easefun.polyvsdk.video.PolyvVideoView;

/**
 * ppt直播画板的父控件，用于传递手势事件给ppt播放器
 */
public class PolyvGestureLayout extends RelativeLayout {
    private Object videoView;

    public PolyvGestureLayout(Context context) {
        super(context);
    }

    public PolyvGestureLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvGestureLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setPolyvLiveVideoView(Object videoView) {
        this.videoView = videoView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if  (videoView instanceof PolyvLiveVideoView){
            return ((PolyvLiveVideoView)videoView).onPPTLiveTranTouchEvent(event, getMeasuredWidth());
        }else if(videoView instanceof PolyvVideoView){
            return ((PolyvVideoView)videoView).onPPTLiveTranTouchEvent(event, getMeasuredWidth());
        }
        return super.onTouchEvent(event);
    }
}
