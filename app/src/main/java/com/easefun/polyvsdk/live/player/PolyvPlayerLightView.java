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
 * 手势亮度指示图标类
 */
public class PolyvPlayerLightView extends FrameLayout {
    //lightView
    private View view;
    private RelativeLayout rl_center_light;
    private TextView tv_light;

    public PolyvPlayerLightView(Context context) {
        this(context, null);
    }

    public PolyvPlayerLightView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvPlayerLightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.view = LayoutInflater.from(context).inflate(R.layout.polyv_player_media_center_light, this);
        findIdAndNew();
    }

    private void findIdAndNew() {
        rl_center_light = (RelativeLayout) view.findViewById(R.id.rl_center_light);
        tv_light = (TextView) view.findViewById(R.id.tv_light);
    }

    public void hide() {
        if (rl_center_light != null && rl_center_light.getVisibility() == View.VISIBLE)
            rl_center_light.setVisibility(View.GONE);
    }

    /**
     * 设置view显示的亮度值
     * @param brightness 亮度值
     * @param end {@link PolyvLiveVideoViewListener.OnGestureLeftDownListener}的回调方法参数
     */
    public void setViewLightValue(int brightness, boolean end) {
        if (end)
            rl_center_light.setVisibility(View.GONE);
        else
            rl_center_light.setVisibility(View.VISIBLE);
        tv_light.setText(brightness + "%");
    }
}
