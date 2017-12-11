package com.easefun.polyvsdk.live.playback.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.util.PolyvTimeUtils;


public class PolyvPlayerProgressView extends FrameLayout {
    //progressView
    private View view;
    private RelativeLayout rl_center_progress;
    private TextView tv_curtime, tv_tottime;
    private ImageView iv_left, iv_right;

    public PolyvPlayerProgressView(Context context) {
        this(context, null);
    }

    public PolyvPlayerProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvPlayerProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.view = LayoutInflater.from(context).inflate(R.layout.polyv_player_media_center_progress_playback, this);
        findIdAndNew();
    }

    private void findIdAndNew() {
        rl_center_progress = (RelativeLayout) view.findViewById(R.id.rl_center_progress);
        tv_curtime = (TextView) view.findViewById(R.id.tv_curtime);
        tv_tottime = (TextView) view.findViewById(R.id.tv_tottime);
        iv_left = (ImageView) view.findViewById(R.id.iv_left);
        iv_right = (ImageView) view.findViewById(R.id.iv_right);
    }

    public void hide() {
        if (rl_center_progress != null && rl_center_progress.getVisibility() == View.VISIBLE)
            rl_center_progress.setVisibility(View.GONE);
    }

    public void resetMaxValue() {
        setViewMaxValue(0);
    }

    public void setViewMaxValue(int totaltime) {
        tv_tottime.setText(PolyvTimeUtils.generateTime(totaltime));
    }

    public void setViewProgressValue(int fastForwardPos, int totaltime, boolean end, boolean isRightSwipe) {
        if (end)
            rl_center_progress.setVisibility(View.GONE);
        else
            rl_center_progress.setVisibility(View.VISIBLE);
        if (isRightSwipe) {
            iv_left.setVisibility(View.GONE);
            iv_right.setVisibility(View.VISIBLE);
        } else {
            iv_left.setVisibility(View.VISIBLE);
            iv_right.setVisibility(View.GONE);
        }
        if (fastForwardPos < 0)
            fastForwardPos = 0;
        if (fastForwardPos > totaltime)
            fastForwardPos = totaltime;
        tv_curtime.setText(PolyvTimeUtils.generateTime(fastForwardPos));
    }
}
