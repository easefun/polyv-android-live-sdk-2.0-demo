package com.easefun.polyvsdk.live.view;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.easefun.polyvsdk.live.R;

public class PolyvPopupWindow extends PopupWindow implements View.OnClickListener {
    public static final String autoText = "根据接口自动判断"/*根据接口自动播放直播/ppt直播/回放/ppt回放*/
            , liveText = "直播助手直播"/*仅播放普通直播*/
            , pptLiveText = "云课堂直播"/*仅播放ppt直播*/
            , playbackListText = "回放列表"/*获取回放列表播放回放视频*/
            , selectFlag = "> ";
    public static final int auto = 1, live = 2, pptLive = 3, playbacklist = 4;
    private int playtype = -1;
    private TextView tv_auto, tv_live, tv_ppt_live, tv_playbacklist;
    private View view;
    private View.OnClickListener onClickListener;

    public PolyvPopupWindow(Context context) {
        super(context);
        init(context);
        setProperties();
        resetSelect(R.id.tv_auto);
    }

    private void init(Context context) {
        view = LayoutInflater.from(context).inflate(R.layout.polyv_popupwindow_playtype, null);
        tv_auto = (TextView) view.findViewById(R.id.tv_auto);
        tv_live = (TextView) view.findViewById(R.id.tv_live);
        tv_ppt_live = (TextView) view.findViewById(R.id.tv_ppt_live);
        tv_playbacklist = (TextView) view.findViewById(R.id.tv_playbacklist);
        tv_auto.setOnClickListener(this);
        tv_live.setOnClickListener(this);
        tv_ppt_live.setOnClickListener(this);
        tv_playbacklist.setOnClickListener(this);
    }

    private void setProperties() {
        setContentView(view);
        setFocusable(true);
        setOutsideTouchable(true);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);// 设置弹出窗口的高
        setBackgroundDrawable(new BitmapDrawable());
        setAnimationStyle(R.style.popupwindown_anim_style);
    }

    public void setOnButtonClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public int getPlaytype() {
        return playtype;
    }

    private void resetSelect(int id) {
        tv_auto.setText(autoText);
        tv_live.setText(liveText);
        tv_ppt_live.setText(pptLiveText);
        tv_playbacklist.setText(playbackListText);
        switch (id) {
            case R.id.tv_auto:
                playtype = auto;
                tv_auto.setText(selectFlag + autoText);
                break;
            case R.id.tv_live:
                playtype = live;
                tv_live.setText(selectFlag + liveText);
                break;
            case R.id.tv_ppt_live:
                playtype = pptLive;
                tv_ppt_live.setText(selectFlag + pptLiveText);
                break;
            case R.id.tv_playbacklist:
                playtype = playbacklist;
                tv_playbacklist.setText(selectFlag + playbackListText);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        resetSelect(v.getId());
        if (onClickListener != null)
            onClickListener.onClick(v);
        dismiss();
    }
}
