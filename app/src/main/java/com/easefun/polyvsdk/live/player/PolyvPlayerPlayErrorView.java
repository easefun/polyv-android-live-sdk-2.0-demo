package com.easefun.polyvsdk.live.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.util.PolyvLiveErrorMessageUtils;
import com.easefun.polyvsdk.live.video.PolyvLivePlayErrorReason;

/**
 * 视频播放错误提示界面
 * @author Lionel 2019-3-20
 */
public class PolyvPlayerPlayErrorView extends LinearLayout {
// <editor-fold defaultstate="collapsed" desc="成员变量">
    /**
     * 错误提示内容
     */
    private TextView videoErrorContent;
    /**
     * 点击重试按钮
     */
    private TextView videoErrorRetry;

    private IRetryPlayListener retryPlayListener;
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="构造方法">
    public PolyvPlayerPlayErrorView(Context context) {
        this(context, null);
    }

    public PolyvPlayerPlayErrorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvPlayerPlayErrorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.polyv_player_play_error_view, this);
        findIdAndNew();
        addListener();
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="初始化方法">
    private void findIdAndNew() {
        videoErrorContent = (TextView) findViewById(R.id.video_error_content);
        videoErrorRetry = (TextView) findViewById(R.id.video_error_retry);
    }

    private void addListener() {
        videoErrorRetry.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide();
                if (retryPlayListener != null) {
                    retryPlayListener.onRetry();
                }
            }
        });
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="功能方法">
    /**
     * 显示界面
     * @param errorReason 错误
     */
    public void show(PolyvLivePlayErrorReason errorReason) {
        String message = PolyvLiveErrorMessageUtils.getPlayErrorMessage(errorReason);
        message += "(error code " + errorReason.getType().getCode() + ")";
        videoErrorContent.setText(message);
        setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏界面
     */
    public void hide() {
        setVisibility(View.GONE);
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="设置监听回调">
    public void setRetryPlayListener(IRetryPlayListener retryPlayListener) {
        this.retryPlayListener = retryPlayListener;
    }
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="监听回调接口定义">
    /**
     * 重试播放监听回调
     * @author Lionel 2019-3-20
     */
    public interface IRetryPlayListener {
        void onRetry();
    }
// </editor-fold>
}
