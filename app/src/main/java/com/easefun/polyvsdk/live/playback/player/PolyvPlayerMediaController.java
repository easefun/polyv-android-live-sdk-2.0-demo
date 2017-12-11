package com.easefun.polyvsdk.live.playback.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.easefun.polyvsdk.ijk.PolyvPlayerScreenRatio;
import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.fragment.PolyvDanmuFragment;
import com.easefun.polyvsdk.live.playback.util.PolyvShareUtils;
import com.easefun.polyvsdk.live.util.PolyvScreenUtils;
import com.easefun.polyvsdk.live.util.PolyvTimeUtils;
import com.easefun.polyvsdk.video.IPolyvVideoView;
import com.easefun.polyvsdk.video.PolyvBaseMediaController;
import com.easefun.polyvsdk.video.PolyvVideoView;
import com.easefun.polyvsdk.vo.PolyvVideoVO;

import java.util.ArrayList;
import java.util.List;

public class PolyvPlayerMediaController extends PolyvBaseMediaController implements View.OnClickListener {
    private static final String TAG = PolyvPlayerMediaController.class.getSimpleName();
    private Context mContext = null;
    private PolyvVideoView videoView = null;
    private PolyvVideoVO videoVO;
    private PolyvDanmuFragment danmuFragment;
    //播放器所在的activity
    private Activity videoActivity;
    //播放器的ParentView
    private View parentView;
    //显示的状态
    private boolean isShowing;
    //控制栏显示的时间
    private static final int longTime = 5000;
    private static final int HIDE = 12;
    private static final int SHOW_PROGRESS = 13;
    //controllerView
    private View view;
    /**
     * 竖屏的view
     */
    // 竖屏的控制栏
    private RelativeLayout rl_port;
    // 竖屏的切屏按钮，竖屏的播放/暂停按钮，弹幕开关
    private ImageView iv_land, iv_play, iv_dmswitch_port;
    // 竖屏的显示播放进度控件
    private TextView tv_curtime, tv_tottime;
    // 竖屏的进度条
    private SeekBar sb_play;
    /**
     * 横屏的view
     */
    //横屏的控制栏，顶部布局，底部布局
    private RelativeLayout rl_land, rl_top, rl_bot;
    //横屏的切屏按钮，横屏的播放/暂停按钮,横屏的返回按钮，设置按钮，分享按钮，弹幕开关
    private ImageView iv_port, iv_play_land, iv_finish, iv_set, iv_share, iv_dmswitch;
    // 横屏的显示播放进度控件,视频的标题,选择播放速度按钮，选择码率按钮
    private TextView tv_curtime_land, tv_tottime_land, tv_title, tv_speed, tv_bit;
    // 横屏的进度条
    private SeekBar sb_play_land;
    /**
     * 设置布局的view
     */
    //设置布局
    private RelativeLayout rl_center_set;
    private LinearLayout ll_scr;
    //调节亮度控件，调节音量控件
    private SeekBar sb_light, sb_volume;
    // 设置播放器银幕比率控件，设置字幕的控件
    private TextView tv_full, tv_fit, tv_sixteennine, tv_fourthree, tv_srt1, tv_srt2, tv_srt3, tv_srtnone;
    // 关闭布局按钮
    private ImageView iv_close_set;
    /**
     * 分享布局的view
     */
    //分享布局
    private RelativeLayout rl_center_share;
    // 分享控件,关闭布局按钮
    private ImageView iv_shareqq, iv_sharewechat, iv_shareweibo, iv_close_share;
    /**
     * 播放速度布局
     */
    //播放速度布局
    private RelativeLayout rl_center_speed;
    //选择播放速度控件
    private TextView tv_speed05, tv_speed10, tv_speed12, tv_speed15, tv_speed20;
    //关闭布局按钮
    private ImageView iv_close_speed;
    /**
     * 播放码率布局
     */
    //播放码率布局
    private RelativeLayout rl_center_bit;
    //选择播放码率的控件
    private TextView tv_sc, tv_hd, tv_flu, tv_auto;
    //关闭布局按钮
    private ImageView iv_close_bit;
    //-----------------------------------------
    // 进度条是否处于拖动的状态
    private boolean status_dragging;
    // 控制栏是否处于一直显示的状态
    private boolean status_showalways;
    private PopupWindow popupWindow;
    // 是否使用popupWindow弹出控制栏，非ppt直播/回放用false即可。
    // (注：如果是ppt直播/回放时，true：可移动的播放器层级<弹幕层级<控制栏层级，false：控制栏<可移动的播放器<弹幕)
    private boolean usePopupWindow;

    //用于处理控制栏的显示状态
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE:
                    hide();
                    break;
                case SHOW_PROGRESS:
                    showProgress();
                    break;
            }
        }
    };

    // 更新显示的播放进度，以及暂停/播放按钮
    private void showProgress() {
        if (isShowing && videoView != null) {
            // 单位：毫秒
            int position = videoView.getCurrentPosition();
            int totalTime = videoView.getDuration() / 1000 * 1000;
            if (videoView.isCompletedState() || position > totalTime)
                position = totalTime;
            int bufPercent = videoView.getBufferPercentage();
            //在拖动进度条的时候，这里不更新
            if (!status_dragging) {
                tv_curtime.setText(PolyvTimeUtils.generateTime(position));
                tv_curtime_land.setText(PolyvTimeUtils.generateTime(position));
                if (totalTime > 0) {
                    sb_play.setProgress((int) (1000L * position / totalTime));
                    sb_play_land.setProgress((int) (1000L * position / totalTime));
                } else {
                    sb_play.setProgress(0);
                    sb_play_land.setProgress(0);
                }
            }
            sb_play.setSecondaryProgress(1000 * bufPercent / 100);
            sb_play_land.setSecondaryProgress(1000 * bufPercent / 100);
            if (videoView.isPlaying()) {
                iv_play.setSelected(false);
                iv_play_land.setSelected(false);
            } else {
                iv_play.setSelected(true);
                iv_play_land.setSelected(true);
            }
            handler.sendMessageDelayed(handler.obtainMessage(SHOW_PROGRESS), 1000 - (position % 1000));
        }
    }

    public PolyvPlayerMediaController(Context context) {
        this(context, null);
    }

    public PolyvPlayerMediaController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvPlayerMediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        this.videoActivity = (Activity) mContext;
    }

    /**
     * 初始化控制栏的配置
     *
     * @param parentView     播放器的父控件
     * @param usePopupWindow 是否用popupWindow弹出控制栏
     */
    public void initConfig(final ViewGroup parentView, boolean usePopupWindow) {
        this.parentView = parentView;
        this.usePopupWindow = usePopupWindow;
        this.view = LayoutInflater.from(getContext()).inflate(R.layout.polyv_controller_media_playback, usePopupWindow ? null : this);
        if (usePopupWindow) {
            popupWindow = new PopupWindow(mContext);
            popupWindow.setBackgroundDrawable(null);
            popupWindow.setContentView(view);
            view.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return videoView == null ? false : videoView.onPPTLiveTranTouchEvent(event, parentView.getMeasuredWidth());
                }
            });
        }
        findIdAndNew();
        initView();
    }

    public void setDanmuFragment(PolyvDanmuFragment danmuFragment) {
        this.danmuFragment = danmuFragment;
    }

    private void findIdAndNew() {
        //竖屏的view
        rl_port = (RelativeLayout) view.findViewById(R.id.rl_port);
        iv_land = (ImageView) view.findViewById(R.id.iv_land);
        iv_play = (ImageView) view.findViewById(R.id.iv_play);
        tv_curtime = (TextView) view.findViewById(R.id.tv_curtime);
        tv_tottime = (TextView) view.findViewById(R.id.tv_tottime);
        sb_play = (SeekBar) view.findViewById(R.id.sb_play);
        iv_dmswitch_port = (ImageView) view.findViewById(R.id.iv_dmswitch_port);
        //横屏的view
        rl_land = (RelativeLayout) view.findViewById(R.id.rl_land);
        rl_top = (RelativeLayout) view.findViewById(R.id.rl_top);
        rl_bot = (RelativeLayout) view.findViewById(R.id.rl_bot);
        iv_port = (ImageView) view.findViewById(R.id.iv_port);
        iv_play_land = (ImageView) view.findViewById(R.id.iv_play_land);
        iv_finish = (ImageView) view.findViewById(R.id.iv_finish);
        tv_curtime_land = (TextView) view.findViewById(R.id.tv_curtime_land);
        tv_tottime_land = (TextView) view.findViewById(R.id.tv_tottime_land);
        sb_play_land = (SeekBar) view.findViewById(R.id.sb_play_land);
        tv_title = (TextView) view.findViewById(R.id.tv_title);
        iv_set = (ImageView) view.findViewById(R.id.iv_set);
        iv_share = (ImageView) view.findViewById(R.id.iv_share);
        iv_dmswitch = (ImageView) view.findViewById(R.id.iv_dmswitch);
        tv_speed = (TextView) view.findViewById(R.id.tv_speed);
        tv_bit = (TextView) view.findViewById(R.id.tv_bit);
        //设置布局的view
        rl_center_set = (RelativeLayout) view.findViewById(R.id.rl_center_set);
        ll_scr = (LinearLayout) view.findViewById(R.id.ll_scr);
        sb_light = (SeekBar) view.findViewById(R.id.sb_light);
        sb_volume = (SeekBar) view.findViewById(R.id.sb_volume);
        tv_full = (TextView) view.findViewById(R.id.tv_full);
        tv_fit = (TextView) view.findViewById(R.id.tv_fit);
        tv_sixteennine = (TextView) view.findViewById(R.id.tv_sixteennine);
        tv_fourthree = (TextView) view.findViewById(R.id.tv_fourthree);
        tv_srt1 = (TextView) view.findViewById(R.id.tv_srt1);
        tv_srt2 = (TextView) view.findViewById(R.id.tv_srt2);
        tv_srt3 = (TextView) view.findViewById(R.id.tv_srt3);
        tv_srtnone = (TextView) view.findViewById(R.id.tv_srtnone);
        iv_close_set = (ImageView) view.findViewById(R.id.iv_close_set);
        //分享布局的view
        rl_center_share = (RelativeLayout) view.findViewById(R.id.rl_center_share);
        iv_shareqq = (ImageView) view.findViewById(R.id.iv_shareqq);
        iv_sharewechat = (ImageView) view.findViewById(R.id.iv_sharewechat);
        iv_shareweibo = (ImageView) view.findViewById(R.id.iv_shareweibo);
        iv_close_share = (ImageView) view.findViewById(R.id.iv_close_share);
        //播放速度布局的view
        rl_center_speed = (RelativeLayout) view.findViewById(R.id.rl_center_speed);
        tv_speed05 = (TextView) view.findViewById(R.id.tv_speed05);
        tv_speed10 = (TextView) view.findViewById(R.id.tv_speed10);
        tv_speed12 = (TextView) view.findViewById(R.id.tv_speed12);
        tv_speed15 = (TextView) view.findViewById(R.id.tv_speed15);
        tv_speed20 = (TextView) view.findViewById(R.id.tv_speed20);
        iv_close_speed = (ImageView) view.findViewById(R.id.iv_close_speed);
        //播放码率布局的view
        rl_center_bit = (RelativeLayout) view.findViewById(R.id.rl_center_bit);
        tv_sc = (TextView) view.findViewById(R.id.tv_sc);
        tv_hd = (TextView) view.findViewById(R.id.tv_hd);
        tv_flu = (TextView) view.findViewById(R.id.tv_flu);
        tv_auto = (TextView) view.findViewById(R.id.tv_auto);
        iv_close_bit = (ImageView) view.findViewById(R.id.iv_close_bit);
    }

    private void initView() {
        iv_land.setOnClickListener(this);
        iv_port.setOnClickListener(this);
        iv_play.setOnClickListener(this);
        iv_play_land.setOnClickListener(this);
        iv_finish.setOnClickListener(this);
        iv_set.setOnClickListener(this);
        iv_share.setOnClickListener(this);
        tv_full.setOnClickListener(this);
        tv_fit.setOnClickListener(this);
        tv_sixteennine.setOnClickListener(this);
        tv_fourthree.setOnClickListener(this);
        iv_shareqq.setOnClickListener(this);
        iv_sharewechat.setOnClickListener(this);
        iv_shareweibo.setOnClickListener(this);
        iv_dmswitch.setOnClickListener(this);
        iv_dmswitch_port.setOnClickListener(this);
        tv_srt1.setOnClickListener(this);
        tv_srt2.setOnClickListener(this);
        tv_srt3.setOnClickListener(this);
        tv_srtnone.setOnClickListener(this);
        tv_speed.setOnClickListener(this);
        tv_speed05.setOnClickListener(this);
        tv_speed10.setOnClickListener(this);
        tv_speed12.setOnClickListener(this);
        tv_speed15.setOnClickListener(this);
        tv_speed20.setOnClickListener(this);
        tv_bit.setOnClickListener(this);
        tv_sc.setOnClickListener(this);
        tv_hd.setOnClickListener(this);
        tv_flu.setOnClickListener(this);
        tv_auto.setOnClickListener(this);
        iv_close_bit.setOnClickListener(this);
        iv_close_set.setOnClickListener(this);
        iv_close_share.setOnClickListener(this);
        iv_close_speed.setOnClickListener(this);
        sb_play.setOnSeekBarChangeListener(seekBarChangeListener);
        sb_play_land.setOnSeekBarChangeListener(seekBarChangeListener);
        sb_light.setOnSeekBarChangeListener(seekBarChangeListener);
        sb_volume.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    /**
     * 初始化控制栏的一些控件
     *
     * @param isPlaybackUrl    是否是播放url
     * @param isPPTPlayback    是否是ppt回放
     * @param playBackUrlTitle 播放url的title
     */
    public void preparedView(boolean isPlaybackUrl, boolean isPPTPlayback, String playBackUrlTitle) {
        if (videoView != null) {
            videoVO = videoView.getVideo();
            if (isPlaybackUrl)
                tv_title.setText(playBackUrlTitle);
            else if (videoVO != null)
                tv_title.setText(videoVO.getTitle());
            if (isPlaybackUrl)
                tv_bit.setVisibility(View.GONE);
            else
                tv_bit.setVisibility(View.VISIBLE);
            if (isPPTPlayback)
                ll_scr.setVisibility(View.GONE);
            else
                ll_scr.setVisibility(View.VISIBLE);
            int totalTime = videoView.getDuration();
            tv_tottime.setText(PolyvTimeUtils.generateTime(totalTime));
            tv_tottime_land.setText(PolyvTimeUtils.generateTime(totalTime));
            //初始化播放器的银幕比率的显示控件
            initRatioView(videoView.getCurrentAspectRatio());
            //初始化字幕控件
            initSrtView(videoView.getCurrSRTKey());
            //初始化倍速控件及其可见性
            initSpeedView((int) (videoView.getSpeed() * 10));
            //初始化码率控件及其可见性
            initBitRateView(videoView.getBitRate());
            initBitRateViewVisible(videoView.getBitRate());
        }
    }

    @Override
    public void setMediaPlayer(IPolyvVideoView player) {
        videoView = (PolyvVideoView) player;
    }

    @Override
    public void release() {
        //播放器release时主动调用
    }

    @Override
    public void destroy() {
        //播放器destroy时主动调用
    }

    @Override
    public void hide() {
        if (isShowing) {
            handler.removeMessages(HIDE);
            handler.removeMessages(SHOW_PROGRESS);
            resetSetLayout(View.GONE);
            resetShareLayout(View.GONE);
            resetSpeedLayout(View.GONE);
            resetBitRateLayout(View.GONE);
            isShowing = !isShowing;
            if (usePopupWindow) {
                try {
                    popupWindow.dismiss();
                } catch (Exception e) {
                }
                if (popupWindowDismissListener != null)
                    popupWindowDismissListener.dismiss();
            } else {
                setVisibility(View.GONE);
            }
        }
    }

    @Override
    public boolean isShowing() {
        return isShowing;
    }

    @Override
    public void setAnchorView(View view) {
        //...
    }

    /**
     * 退出播放器的Activity时需调用
     */
    public void disable() {
        hide();
    }

    /**
     * 显示控制栏
     *
     * @param timeout 显示的时间，<0时将一直显示
     */
    @Override
    public void show(int timeout) {
        if (timeout < 0)
            status_showalways = true;
        else
            status_showalways = false;
        if (!isShowing) {
            resetTopBottomLayout(View.VISIBLE);
            //获取焦点
            requestFocus();
            handler.removeMessages(SHOW_PROGRESS);
            handler.sendEmptyMessage(SHOW_PROGRESS);
            isShowing = !isShowing;
            if (usePopupWindow) {
                int[] location = new int[2];
                parentView.getLocationInWindow(location);
                popupWindow.setWidth(parentView.getMeasuredWidth());
                popupWindow.setHeight(parentView.getMeasuredHeight());
                try {
                    popupWindow.showAtLocation(parentView, Gravity.NO_GRAVITY, location[0], location[1]);
                } catch (Exception e) {
                }
            } else {
                setVisibility(View.VISIBLE);
            }
        }
        resetHideTime(timeout);
    }

    @Override
    public void show() {
        show(longTime);
    }

    /**
     * 切换到横屏
     */
    public void changeToLandscape() {
        PolyvScreenUtils.setLandscape(videoActivity);
        //初始为横屏时，状态栏需要隐藏
        PolyvScreenUtils.hideStatusBar(videoActivity);
        //初始为横屏时，控制栏的宽高需要设置
        initLandScapeWH();
    }

    private void initLandScapeWH() {
        ViewGroup.LayoutParams vlp = parentView.getLayoutParams();
        vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        vlp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        rl_land.setVisibility(View.VISIBLE);
        rl_port.setVisibility(View.GONE);
    }

    /**
     * 切换到竖屏
     */
    public void changeToPortrait() {
        PolyvScreenUtils.setPortrait(videoActivity);
        initPortraitWH();
    }

    private void initPortraitWH() {
        ViewGroup.LayoutParams vlp = parentView.getLayoutParams();
        vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        vlp.height = PolyvScreenUtils.getHeight16_9();
        rl_port.setVisibility(View.VISIBLE);
        rl_land.setVisibility(View.GONE);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetControllerLayout();
    }

    //根据屏幕状态改变控制栏布局
    private void resetControllerLayout() {
        hide();
        PolyvScreenUtils.reSetStatusBar(videoActivity);
        if (PolyvScreenUtils.isLandscape(mContext)) {
            initLandScapeWH();
        } else {
            initPortraitWH();
        }
    }

    //根据视频的播放状态去暂停或播放
    private void playOrPause() {
        if (videoView != null) {
            if (videoView.isPlaying()) {
                videoView.pause();
                iv_play.setSelected(true);
                iv_play_land.setSelected(true);
            } else {
                videoView.start();
                iv_play.setSelected(false);
                iv_play_land.setSelected(false);
            }
        }
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
            if (!b)
                return;
            switch (seekBar.getId()) {
                case R.id.sb_play:
                case R.id.sb_play_land:
                    resetHideTime(longTime);
                    status_dragging = true;
                    if (videoView != null) {
                        int newPosition = (int) (videoView.getDuration() * (long) i / 1000);
                        tv_curtime.setText(PolyvTimeUtils.generateTime(newPosition));
                        tv_curtime_land.setText(PolyvTimeUtils.generateTime(newPosition));
                    }
                    break;
                case R.id.sb_light:
                    if (videoView != null)
                        videoView.setBrightness(videoActivity, i);
                    break;
                case R.id.sb_volume:
                    if (videoView != null)
                        videoView.setVolume(i);
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (!seekBar.isSelected())
                seekBar.setSelected(true);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (seekBar.isSelected())
                seekBar.setSelected(false);
            switch (seekBar.getId()) {
                case R.id.sb_play:
                case R.id.sb_play_land:
                    if (videoView != null) {
                        int seekToPosition = (int) (videoView.getDuration() * (long) seekBar.getProgress() / seekBar.getMax());
                        if (!videoView.isCompletedState()) {
                            videoView.seekTo(seekToPosition);
                        } else if (videoView.isCompletedState() && seekToPosition / 1000 * 1000 < videoView.getDuration() / 1000 * 1000) {
                            videoView.seekTo(seekToPosition);
                            videoView.start();
                        }
                    }
                    status_dragging = false;
                    break;
            }
        }
    };

    //重置控制栏的隐藏时间
    private void resetHideTime(int delayedTime) {
        handler.removeMessages(HIDE);
        if (delayedTime >= 0)
            handler.sendMessageDelayed(handler.obtainMessage(HIDE), delayedTime);
    }

    //重置控制栏的顶部和底部布局以及进度条的显示状态
    private void resetTopBottomLayout(int isVisible) {
        resetTopBottomLayout(isVisible, false);
    }

    private void resetTopBottomLayout(int isVisible, boolean onlyHideTop) {
        rl_top.setVisibility(isVisible);
        if (!onlyHideTop) {
            rl_bot.setVisibility(isVisible);
            sb_play_land.setVisibility(isVisible);
        }
    }

    //重置设置布局的显示状态
    private void resetSetLayout(int isVisible) {
        if (isVisible == View.VISIBLE) {
            show(-1);
            resetTopBottomLayout(View.GONE);
            if (videoView != null) {
                sb_light.setProgress(videoView.getBrightness(videoActivity));
                sb_volume.setProgress(videoView.getVolume());
            }
        }
        rl_center_set.setVisibility(isVisible);
    }

    //重置分享布局的显示状态
    private void resetShareLayout(int isVisible) {
        if (isVisible == View.VISIBLE) {
            show(-1);
            resetTopBottomLayout(View.GONE);
        }
        rl_center_share.setVisibility(isVisible);
    }

    //重置选择播放器银幕比率控件的状态
    private void resetRatioView(int screenRatio) {
        initRatioView(screenRatio);
        if (videoView != null)
            videoView.setAspectRatio(screenRatio);
    }

    //初始化选择播放器银幕比率控件
    private void initRatioView(int screenRatio) {
        tv_full.setSelected(false);
        tv_fit.setSelected(false);
        tv_sixteennine.setSelected(false);
        tv_fourthree.setSelected(false);
        switch (screenRatio) {
            case PolyvPlayerScreenRatio.AR_ASPECT_FILL_PARENT:
                tv_full.setSelected(true);
                break;
            case PolyvPlayerScreenRatio.AR_ASPECT_FIT_PARENT:
                tv_fit.setSelected(true);
                break;
            case PolyvPlayerScreenRatio.AR_16_9_FIT_PARENT:
                tv_sixteennine.setSelected(true);
                break;
            case PolyvPlayerScreenRatio.AR_4_3_FIT_PARENT:
                tv_fourthree.setSelected(true);
                break;
        }
    }

    //重置选择字幕的控件
    private void resetSrtView(int srtPosition) {
        if (videoView != null)
            videoView.changeSRT(initSrtView(srtPosition));
    }

    private String initSrtView(int srtPosition) {
        tv_srt1.setSelected(false);
        tv_srt2.setSelected(false);
        tv_srt3.setSelected(false);
        tv_srtnone.setSelected(false);
        List<String> srtKeys = new ArrayList<String>();
        if (videoVO != null)
            srtKeys.addAll(videoVO.getVideoSRT().keySet());
        switch (srtPosition) {
            case 0:
                tv_srt1.setSelected(true);
                break;
            case 1:
                tv_srt2.setSelected(true);
                break;
            case 2:
                tv_srt3.setSelected(true);
                break;
            case 3:
                tv_srtnone.setSelected(true);
                break;
        }
        return srtPosition == 3 ? "不显示" : srtKeys.get(srtPosition);
    }

    //初始化选择字幕的控件
    private void initSrtView(String srtKey) {
        tv_srt1.setSelected(false);
        tv_srt2.setSelected(false);
        tv_srt3.setSelected(false);
        tv_srt1.setVisibility(View.VISIBLE);
        tv_srt2.setVisibility(View.VISIBLE);
        tv_srt3.setVisibility(View.VISIBLE);
        tv_srtnone.setSelected(false);
        List<String> srtKeys = new ArrayList<String>();
        if (videoVO != null)
            srtKeys.addAll(videoVO.getVideoSRT().keySet());
        switch (srtKeys.size()) {
            case 0:
                tv_srt1.setVisibility(View.GONE);
                tv_srt2.setVisibility(View.GONE);
                tv_srt3.setVisibility(View.GONE);
                break;
            case 1:
                tv_srt1.setText(srtKeys.get(0));
                tv_srt2.setVisibility(View.GONE);
                tv_srt3.setVisibility(View.GONE);
                break;
            case 2:
                tv_srt1.setText(srtKeys.get(0));
                tv_srt2.setText(srtKeys.get(1));
                tv_srt3.setVisibility(View.GONE);
                break;
            default:
                tv_srt1.setText(srtKeys.get(0));
                tv_srt2.setText(srtKeys.get(1));
                tv_srt3.setText(srtKeys.get(2));
                break;
        }
        if (TextUtils.isEmpty(srtKey)) {
            tv_srtnone.setSelected(true);
            return;
        }
        switch (srtKeys.indexOf(srtKey)) {
            case 0:
                tv_srt1.setSelected(true);
                break;
            case 1:
                tv_srt2.setSelected(true);
                break;
            case 2:
                tv_srt3.setSelected(true);
                break;
        }
    }

    //重置选择播放速度的布局
    private void resetSpeedLayout(int isVisible) {
        resetSpeedLayout(isVisible, true);
    }

    private void resetSpeedLayout(int isVisible, boolean isShowTopBottomLayout) {
        if (isVisible == View.VISIBLE) {
            show(-1);
            resetTopBottomLayout(View.GONE, true);
            resetBitRateLayout(View.GONE, false);
        } else if (isShowTopBottomLayout) {
            resetTopBottomLayout(View.VISIBLE);
            requestFocus();
            resetHideTime(longTime);
        }
        rl_center_speed.setVisibility(isVisible);
    }

    //初始化选择播放速度的控件
    private void initSpeedView(int speed) {
        tv_speed05.setSelected(false);
        tv_speed10.setSelected(false);
        tv_speed12.setSelected(false);
        tv_speed15.setSelected(false);
        tv_speed20.setSelected(false);
        switch (speed) {
            case 5:
                tv_speed05.setSelected(true);
                tv_speed.setText("0.5x");
                break;
            case 10:
                tv_speed10.setSelected(true);
                tv_speed.setText("1x");
                break;
            case 12:
                tv_speed12.setSelected(true);
                tv_speed.setText("1.2x");
                break;
            case 15:
                tv_speed15.setSelected(true);
                tv_speed.setText("1.5x");
                break;
            case 20:
                tv_speed20.setSelected(true);
                tv_speed.setText("2x");
                break;
        }
    }

    //重置选择播放速度的控件
    private void resetSpeedView(int speed) {
        initSpeedView(speed);
        if (videoView != null) {
            videoView.setSpeed(speed / 10f);
        }
        hide();
    }

    //初始化选择码率的控件
    private void initBitRateView(int bitRate) {
        tv_sc.setSelected(false);
        tv_hd.setSelected(false);
        tv_flu.setSelected(false);
        tv_auto.setSelected(false);
        switch (bitRate) {
            case 0:
                tv_bit.setText("自动");
                tv_auto.setSelected(true);
                break;
            case 1:
                tv_bit.setText("流畅");
                tv_flu.setSelected(true);
                break;
            case 2:
                tv_bit.setText("高清");
                tv_hd.setSelected(true);
                break;
            case 3:
                tv_bit.setText("超清");
                tv_sc.setSelected(true);
                break;
        }
    }

    //初始化选择码率控件的可见性
    private void initBitRateViewVisible(int currentBitRate) {
        tv_sc.setVisibility(View.GONE);
        tv_hd.setVisibility(View.GONE);
        tv_flu.setVisibility(View.GONE);
        tv_auto.setVisibility(View.GONE);
        if (videoVO != null) {
            switch (videoVO.getDfNum()) {
                case 1:
                    tv_flu.setVisibility(View.VISIBLE);
                    tv_auto.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    tv_hd.setVisibility(View.VISIBLE);
                    tv_flu.setVisibility(View.VISIBLE);
                    tv_auto.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    tv_sc.setVisibility(View.VISIBLE);
                    tv_hd.setVisibility(View.VISIBLE);
                    tv_flu.setVisibility(View.VISIBLE);
                    tv_auto.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            switch (currentBitRate) {
                case 0:
                    tv_auto.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    tv_flu.setVisibility(View.VISIBLE);
                    break;
                case 2:
                    tv_hd.setVisibility(View.VISIBLE);
                    break;
                case 3:
                    tv_sc.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    //重置播放码率布局的状态
    private void resetBitRateLayout(int isVisible) {
        resetBitRateLayout(isVisible, true);
    }

    private void resetBitRateLayout(int isVisible, boolean isShowTopBottomLayout) {
        if (isVisible == View.VISIBLE) {
            show(-1);
            resetTopBottomLayout(View.GONE, true);
            resetSpeedLayout(View.GONE, false);
        } else if (isShowTopBottomLayout) {
            resetTopBottomLayout(View.VISIBLE);
            requestFocus();
            resetHideTime(longTime);
        }
        rl_center_bit.setVisibility(isVisible);
    }

    //重置选择码率的控件
    private void resetBitRateView(int bitRate) {
        initBitRateView(bitRate);
        if (videoView != null)
            videoView.changeBitRate(bitRate);
        hide();
    }

    //重置显示/隐藏弹幕的控件
    private void resetDmSwitchView() {
        if (iv_dmswitch.isSelected()) {
            iv_dmswitch.setSelected(false);
            iv_dmswitch_port.setSelected(false);
            danmuFragment.show();
        } else {
            iv_dmswitch.setSelected(true);
            iv_dmswitch_port.setSelected(true);
            danmuFragment.hide();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_land:
                changeToLandscape();
                break;
            case R.id.iv_port:
                changeToPortrait();
                break;
            case R.id.iv_play:
                playOrPause();
                break;
            case R.id.iv_play_land:
                playOrPause();
                break;
            case R.id.iv_finish:
                changeToPortrait();
                break;
            case R.id.iv_set:
                resetSetLayout(View.VISIBLE);
                break;
            case R.id.iv_share:
                resetShareLayout(View.VISIBLE);
                break;
            case R.id.tv_full:
                resetRatioView(PolyvPlayerScreenRatio.AR_ASPECT_FILL_PARENT);
                break;
            case R.id.tv_fit:
                resetRatioView(PolyvPlayerScreenRatio.AR_ASPECT_FIT_PARENT);
                break;
            case R.id.tv_sixteennine:
                resetRatioView(PolyvPlayerScreenRatio.AR_16_9_FIT_PARENT);
                break;
            case R.id.tv_fourthree:
                resetRatioView(PolyvPlayerScreenRatio.AR_4_3_FIT_PARENT);
                break;
            case R.id.iv_shareqq:
                PolyvShareUtils.shareQQFriend(mContext, "", "test", PolyvShareUtils.TEXT, null);
                hide();
                break;
            case R.id.iv_sharewechat:
                PolyvShareUtils.shareWeChatFriend(mContext, "", "test", PolyvShareUtils.TEXT, null);
                hide();
                break;
            case R.id.iv_shareweibo:
                PolyvShareUtils.shareWeiBo(mContext, "", "test", PolyvShareUtils.TEXT, null);
                hide();
                break;
            case R.id.iv_dmswitch:
            case R.id.iv_dmswitch_port:
                resetDmSwitchView();
                break;
            case R.id.tv_srt1:
                resetSrtView(0);
                break;
            case R.id.tv_srt2:
                resetSrtView(1);
                break;
            case R.id.tv_srt3:
                resetSrtView(2);
                break;
            case R.id.tv_srtnone:
                resetSrtView(3);
                break;
            case R.id.tv_speed:
                if (rl_center_speed.getVisibility() == View.GONE)
                    resetSpeedLayout(View.VISIBLE);
                else
                    resetSpeedLayout(View.GONE);
                break;
            case R.id.tv_bit:
                if (rl_center_bit.getVisibility() == View.GONE)
                    resetBitRateLayout(View.VISIBLE);
                else
                    resetBitRateLayout(View.GONE);
                break;
            case R.id.tv_sc:
                resetBitRateView(3);
                break;
            case R.id.tv_hd:
                resetBitRateView(2);
                break;
            case R.id.tv_flu:
                resetBitRateView(1);
                break;
            case R.id.tv_auto:
                resetBitRateView(0);
                break;
            case R.id.tv_speed05:
                resetSpeedView(5);
                break;
            case R.id.tv_speed10:
                resetSpeedView(10);
                break;
            case R.id.tv_speed12:
                resetSpeedView(12);
                break;
            case R.id.tv_speed15:
                resetSpeedView(15);
                break;
            case R.id.tv_speed20:
                resetSpeedView(20);
                break;
            case R.id.iv_close_bit:
                hide();
                break;
            case R.id.iv_close_set:
                hide();
                break;
            case R.id.iv_close_share:
                hide();
                break;
            case R.id.iv_close_speed:
                hide();
                break;
        }
        //如果控制栏不是处于一直显示的状态，那么重置控制栏隐藏的时间
        if (!status_showalways)
            resetHideTime(longTime);
    }

    public interface PopupWindowDismissListener {
        void dismiss();
    }

    private PopupWindowDismissListener popupWindowDismissListener;

    public void setPopupWindowDismissListener(PopupWindowDismissListener listener) {
        this.popupWindowDismissListener = listener;
    }
}
