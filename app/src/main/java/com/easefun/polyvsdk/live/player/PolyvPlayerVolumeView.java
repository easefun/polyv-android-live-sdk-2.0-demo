package com.easefun.polyvsdk.live.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyvsdk.live.video.PolyvLiveVideoViewListener;
import com.easefun.polyvsdk.live.R;

/**
 * 手势音量指示图标类
 */
public class PolyvPlayerVolumeView extends FrameLayout {
    //volumeView
    private View view;
    private RelativeLayout rl_center_volume;
    private TextView tv_volume;

    public PolyvPlayerVolumeView(Context context) {
        this(context, null);
    }

    public PolyvPlayerVolumeView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvPlayerVolumeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.view = LayoutInflater.from(context).inflate(R.layout.polyv_player_media_center_volume, this);
        findIdAndNew();
    }

    private void findIdAndNew() {
        rl_center_volume = (RelativeLayout) view.findViewById(R.id.rl_center_volume);
        tv_volume = (TextView) view.findViewById(R.id.tv_volume);
    }

    public void hide() {
        if (rl_center_volume != null && rl_center_volume.getVisibility() == View.VISIBLE)
            rl_center_volume.setVisibility(View.GONE);
    }

    /**
     * 设置view显示的音量值
     * @param volume 音量值
     * @param end {@link PolyvLiveVideoViewListener.OnGestureRightDownListener}的回调方法参数
     */
    public void setViewVolumeValue(int volume, boolean end) {
        if (end)
            rl_center_volume.setVisibility(View.GONE);
        else
            rl_center_volume.setVisibility(View.VISIBLE);
        tv_volume.setText(volume + "%");
    }
}
