package com.easefun.polyvsdk.live.activity;


import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.admaster.sdk.api.AdmasterSdk;
import com.easefun.polyvsdk.live.PolyvLivePlayerScreenSize;
import com.easefun.polyvsdk.live.PolyvLiveSDKUtil;
import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.chat.IPolyvLivePPTView;
import com.easefun.polyvsdk.live.chat.PolyvChatManager;
import com.easefun.polyvsdk.live.chat.PolyvLivePPTLoadErrorListener;
import com.easefun.polyvsdk.live.fragment.PolyvChatFragment;
import com.easefun.polyvsdk.live.fragment.PolyvDanmuFragment;
import com.easefun.polyvsdk.live.player.PolyvPlayerAuxiliaryView;
import com.easefun.polyvsdk.live.player.PolyvPlayerLightView;
import com.easefun.polyvsdk.live.player.PolyvPlayerMediaController;
import com.easefun.polyvsdk.live.player.PolyvPlayerVolumeView;
import com.easefun.polyvsdk.live.util.AdmasterSdkUtils;
import com.easefun.polyvsdk.live.util.PolyvScreenUtils;
import com.easefun.polyvsdk.live.video.PolyvLiveMediaInfoType;
import com.easefun.polyvsdk.live.video.PolyvLivePlayErrorReason;
import com.easefun.polyvsdk.live.video.PolyvLiveVideoView;
import com.easefun.polyvsdk.live.video.PolyvLiveVideoViewListener;
import com.easefun.polyvsdk.live.video.auxiliary.PolyvLiveAuxiliaryVideoView;
import com.easefun.polyvsdk.live.view.PolyvGestureLayout;
import com.easefun.polyvsdk.live.vo.PolyvLiveChannelVO;

import java.net.MalformedURLException;
import java.net.URL;

public class PolyvPPTPlayerActivity extends FragmentActivity {
    private static final String TAG = PolyvPPTPlayerActivity.class.getSimpleName();
    private PolyvChatFragment polyvChatFragment;
    private PolyvChatManager chatManager;
    private PolyvDanmuFragment danmuFragment;
    private String uid, cid;
    /**
     * ppt绘制视图
     */
    private IPolyvLivePPTView ppt_view;
    private RelativeLayout rl_container, rl_parent;
    private View.OnLayoutChangeListener rl_parent_onLayoutChangeListener;
    private PolyvGestureLayout viewLayout = null;
    /**
     * 播放器主类
     */
    private PolyvLiveVideoView videoView = null;
    /**
     * 播放器控制栏
     */
    private PolyvPlayerMediaController mediaController = null;
    /**
     * 辅助播放器类，用于播放视频片头广告
     */
    private PolyvLiveAuxiliaryVideoView auxiliaryVideoView = null;
    /**
     * 辅助显示类，用于显示图片广告
     */
    private PolyvPlayerAuxiliaryView auxiliaryView = null;
    /**
     * 手势亮度指示标志
     */
    private PolyvPlayerLightView lightView = null;
    /**
     * 手势音量指示标志
     */
    private PolyvPlayerVolumeView volumeView = null;
    /**
     * 用于显示广告倒计时
     */
    private TextView advertCountDown = null;
    private boolean isPlay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.polyv_activity_player_ppt);

        uid = getIntent().getStringExtra("uid");
        cid = getIntent().getStringExtra("cid");
        // 初始化广告监测器
        AdmasterSdk.init(getApplicationContext(), "");

        addFragment();
        findIdAndNew();
        initView();
    }

    private void addFragment() {
        // ppt直播聊天室必须配置
        polyvChatFragment = new PolyvChatFragment();
        polyvChatFragment.setIsPPTLive(true);
        chatManager = new PolyvChatManager();
        // 该方法并不会立即登录，只是初始化登录的信息，登录操作将交给ppt控件初始化完成后处理
        chatManager.pptLogin(uid, cid, "游客");
        polyvChatFragment.initPPTLiveChatConfig(chatManager, uid, cid, "游客");
        danmuFragment = new PolyvDanmuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_chat, polyvChatFragment, "chatFragment")
                .add(R.id.fl_danmu, danmuFragment, "danmuFragment").commit();
    }

    private void findIdAndNew() {
        viewLayout = (PolyvGestureLayout) findViewById(R.id.view_layout);
        videoView = (PolyvLiveVideoView) findViewById(R.id.polyv_live_video_view);
        mediaController = (PolyvPlayerMediaController) findViewById(R.id.polyv_player_media_controller);
        ProgressBar loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
        ImageView noStream = (ImageView) findViewById(R.id.no_stream);
        auxiliaryVideoView = (PolyvLiveAuxiliaryVideoView) findViewById(R.id.polyv_live_auxiliary_video_view);
        ProgressBar auxiliaryLoadingProgress = (ProgressBar) findViewById(R.id.auxiliary_loading_progress);
        auxiliaryView = (PolyvPlayerAuxiliaryView) findViewById(R.id.polyv_player_auxiliary_view);
        lightView = (PolyvPlayerLightView) findViewById(R.id.polyv_player_light_view);
        volumeView = (PolyvPlayerVolumeView) findViewById(R.id.polyv_player_volume_view);
        advertCountDown = (TextView) findViewById(R.id.count_down);

        mediaController.initConfig(viewLayout);
        mediaController.setDanmuFragment(danmuFragment);
        videoView.setMediaController(mediaController);
        videoView.setAuxiliaryVideoView(auxiliaryVideoView);
        videoView.setPlayerBufferingIndicator(loadingProgress);
        videoView.setNoStreamIndicator(noStream);
        auxiliaryVideoView.setPlayerBufferingIndicator(auxiliaryLoadingProgress);
        auxiliaryView.setPolyvLiveVideoView(videoView);

        // ppt直播配置
        rl_container = (RelativeLayout) findViewById(R.id.rl_container);
        rl_parent = (RelativeLayout) findViewById(R.id.rl_parent);
        ppt_view = (IPolyvLivePPTView) findViewById(R.id.ppt_view);
        viewLayout.setPolyvLiveVideoView(videoView);
        // 须使用这个属性填充整个父控件，且之后不能再调用该方法
        videoView.setAspectRatio(PolyvLivePlayerScreenSize.AR_ASPECT_FILL_PARENT);
        // false表示ppt绘制控件的生命周期(pause,reume,destroy)需在activity中处理，如果为true，则交给播放器控制
        videoView.setPPTLiveDrawView(ppt_view, true);
        // param1:videoview与ppt交换位置后，videoview的之前父控件下所有子view在ppt父控件中的位置(此时pptView已移除)
        // param2:videoview与ppt交换位置后，弹幕布局(非控件)所在的位置，没有时须设置为-1。弹幕控件需在弹幕布局的顶层，请参考polyv_fragment_danmu.xml的配置。
        ppt_view.setVideoViewSwapToLayoutIndex(new int[]{0, 2, 6}, 1);
        // param1:pptview初始所在父控件中的索引
        // param2:弹幕布局(非控件)所在父控件的初始位置，没有时须设置为-1
        ppt_view.setPPTViewDefaultIndex(0, 1);
        // 设置ppt加载失败的监听器
        ppt_view.setLoadErrorListener(new PolyvLivePPTLoadErrorListener() {
            @Override
            public void onError(final String failTips, final int code) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PolyvPPTPlayerActivity.this, failTips + "-" + code, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        // 开始第一场ppt直播pptView对应调用该方法，当需要在当前播放界面切换到下一场ppt直播，需调用(setNextLive()方法和videoView.setPPTLivePlay()，无需操作聊天室实例)
        ppt_view.setFirstLive(chatManager, videoView);
        // 初始化播放器的位置
        rl_container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewGroup.MarginLayoutParams rlp = null;
                if (rl_container.getParent() instanceof RelativeLayout) {
                    rlp = (RelativeLayout.LayoutParams) rl_container.getLayoutParams();
                } else if (rl_container.getParent() instanceof LinearLayout) {
                    rlp = (LinearLayout.LayoutParams) rl_container.getLayoutParams();
                } else if (rl_container.getParent() instanceof FrameLayout) {
                    rlp = (FrameLayout.LayoutParams) rl_container.getLayoutParams();
                } else {
                    return;
                }
                rlp.leftMargin = ((View) rl_container.getParent()).getMeasuredWidth() - rl_container.getMeasuredWidth();
                rlp.topMargin = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE
                        // 若初始为横屏
                        ? 0
                        // 若初始为竖屏
                        : viewLayout.getMeasuredHeight();
                rl_container.setLayoutParams(rlp);
                if (Build.VERSION.SDK_INT >= 16)
                    rl_container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    rl_container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        rl_parent.addOnLayoutChangeListener(rl_parent_onLayoutChangeListener = new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                // 当键盘弹出导致布局发生改变时，需要对应的改变播放器的位置，键盘关闭时恢复到原来的位置
                if (bottom > 0 && oldBottom > 0 && right == oldRight) {
                    if (Math.abs(bottom - oldBottom) > PolyvScreenUtils.getNormalWH(PolyvPPTPlayerActivity.this)[1] * 0.3)
                        // 键盘关闭
                        if (bottom > oldBottom) {
                            videoView.PPTLiveLayoutResume();
                        }// 键盘弹出
                        else if (bottom < oldBottom) {
                            videoView.PPTLiveLayoutChange();
                        }
                }
            }
        });
    }

    private void initView() {
        videoView.setOpenAd(true);
        //注：ppt直播暂时不能设置预加载功能，否则ppt布局会有问题，下个版本会修复
        videoView.setNeedGestureDetector(true);

        videoView.setOnPreparedListener(new PolyvLiveVideoViewListener.OnPreparedListener() {
            @Override
            public void onPrepared() {
                Toast.makeText(PolyvPPTPlayerActivity.this, "准备完毕，可以播放", Toast.LENGTH_SHORT).show();
            }
        });

        videoView.setOnInfoListener(new PolyvLiveVideoViewListener.OnInfoListener() {
            @Override
            public void onInfo(int what, int extra) {
                switch (what) {
                    case PolyvLiveMediaInfoType.MEDIA_INFO_BUFFERING_START:
                        Toast.makeText(PolyvPPTPlayerActivity.this, "开始缓冲", Toast.LENGTH_SHORT).show();
                        break;

                    case PolyvLiveMediaInfoType.MEDIA_INFO_BUFFERING_END:
                        Toast.makeText(PolyvPPTPlayerActivity.this, "结束缓冲", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        videoView.setOnVideoPlayErrorListener(new PolyvLiveVideoViewListener.OnVideoPlayErrorListener() {
            @Override
            public void onVideoPlayError(@NonNull PolyvLivePlayErrorReason errorReason) {
                switch (errorReason.getType()) {
                    case NETWORK_DENIED:
                        Toast.makeText(PolyvPPTPlayerActivity.this, "无法连接网络，请连接网络后播放", Toast.LENGTH_SHORT).show();
                        break;

                    case START_ERROR:
                        Toast.makeText(PolyvPPTPlayerActivity.this, "播放错误，请重新播放(error code " + PolyvLivePlayErrorReason.ErrorType.START_ERROR.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;

                    case CHANNEL_NULL:
                        Toast.makeText(PolyvPPTPlayerActivity.this, "频道信息获取失败，请重新播放(error code " + PolyvLivePlayErrorReason.ErrorType.CHANNEL_NULL.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;

                    case LIVE_UID_NOT_EQUAL:
                        Toast.makeText(PolyvPPTPlayerActivity.this, "用户id错误，请重新设置(error code" + PolyvLivePlayErrorReason.ErrorType.LIVE_UID_NOT_EQUAL.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;

                    case LIVE_CID_NOT_EQUAL:
                        Toast.makeText(PolyvPPTPlayerActivity.this, "频道号错误，请重新设置(error code " + PolyvLivePlayErrorReason.ErrorType.LIVE_CID_NOT_EQUAL.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;

                    case LIVE_PLAY_ERROR:
                        Toast.makeText(PolyvPPTPlayerActivity.this, "播放错误，请稍后重试(error code " + PolyvLivePlayErrorReason.ErrorType.LIVE_PLAY_ERROR.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;

                    case RESTRICT_NULL:
                        Toast.makeText(PolyvPPTPlayerActivity.this, "限制信息错误，请稍后重试(error code " + PolyvLivePlayErrorReason.ErrorType.RESTRICT_NULL.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;

                    case RESTRICT_ERROR:
                        Toast.makeText(PolyvPPTPlayerActivity.this, errorReason.getErrorMsg() + "(error code " + PolyvLivePlayErrorReason.ErrorType.RESTRICT_ERROR.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });


        videoView.setOnAdvertisementOutListener(new PolyvLiveVideoViewListener.OnAdvertisementOutListener() {
            @Override
            public void onOut(@NonNull PolyvLiveChannelVO.ADMatter adMatter) {
                auxiliaryView.show(adMatter);
            }

            @Override
            public void onClick(@NonNull PolyvLiveChannelVO.ADMatter adMatter) {
                // 发送广告点击监测
                AdmasterSdkUtils.sendAdvertMonitor(adMatter, AdmasterSdkUtils.MONITOR_CLICK);
                if (!TextUtils.isEmpty(adMatter.getAddrUrl())) {
                    try {
                        new URL(adMatter.getAddrUrl());
                    } catch (MalformedURLException e1) {
                        Log.e(TAG, PolyvLiveSDKUtil.getExceptionFullMessage(e1, -1));
                        return;
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(adMatter.getAddrUrl()));
                    startActivity(intent);
                }
            }
        });

        videoView.setOnAdvertisementCountDownListener(new PolyvLiveVideoViewListener.OnAdvertisementCountDownListener() {
            @Override
            public void onCountDown(int num) {
                advertCountDown.setText(String.format("广告也精彩：%d秒", num));
                advertCountDown.setVisibility(View.VISIBLE);
            }

            @Override
            public void onEnd(@NonNull PolyvLiveChannelVO.ADMatter adMatter) {
                advertCountDown.setVisibility(View.GONE);
                auxiliaryView.hide();
                // 发送广告曝光监测
                AdmasterSdkUtils.sendAdvertMonitor(adMatter, AdmasterSdkUtils.MONITOR_SHOW);
            }
        });

        videoView.setOnGestureLeftUpListener(new PolyvLiveVideoViewListener.OnGestureLeftUpListener() {

            @Override
            public void callback(boolean start, boolean end) {
                Log.d(TAG, String.format("LeftUp %b %b brightness %d", start, end, videoView.getBrightness()));
                int brightness = videoView.getBrightness() + 5;
                if (brightness > 100) {
                    brightness = 100;
                }

                videoView.setBrightness(brightness);
                lightView.setViewLightValue(brightness, end);
            }
        });

        videoView.setOnGestureLeftDownListener(new PolyvLiveVideoViewListener.OnGestureLeftDownListener() {

            @Override
            public void callback(boolean start, boolean end) {
                Log.d(TAG, String.format("LeftDown %b %b brightness %d", start, end, videoView.getBrightness()));
                int brightness = videoView.getBrightness() - 5;
                if (brightness < 0) {
                    brightness = 0;
                }

                videoView.setBrightness(brightness);
                lightView.setViewLightValue(brightness, end);
            }
        });

        videoView.setOnGestureRightUpListener(new PolyvLiveVideoViewListener.OnGestureRightUpListener() {

            @Override
            public void callback(boolean start, boolean end) {
                Log.d(TAG, String.format("RightUp %b %b volume %d", start, end, videoView.getVolume()));
                // 加减单位最小为10，否则无效果
                int volume = videoView.getVolume() + 10;
                if (volume > 100) {
                    volume = 100;
                }

                videoView.setVolume(volume);
                volumeView.setViewVolumeValue(volume, end);
            }
        });

        videoView.setOnGestureRightDownListener(new PolyvLiveVideoViewListener.OnGestureRightDownListener() {

            @Override
            public void callback(boolean start, boolean end) {
                Log.d(TAG, String.format("RightDown %b %b volume %d", start, end, videoView.getVolume()));
                // 加减单位最小为10，否则无效果
                int volume = videoView.getVolume() - 10;
                if (volume < 0) {
                    volume = 0;
                }

                videoView.setVolume(volume);
                volumeView.setViewVolumeValue(volume, end);
            }
        });

        videoView.setPPTLivePlay(uid, cid);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //回来后继续播放
        if (isPlay) {
            videoView.onActivityResume();
            if (auxiliaryView.isPauseAdvert()) {
                auxiliaryView.hide();
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (PolyvScreenUtils.isLandscape(this)) {
                mediaController.changeToPortrait();
                return true;
            }
            if (PolyvScreenUtils.isPortrait(this) && polyvChatFragment.emoLayoutIsVisible()) {
                polyvChatFragment.resetEmoLayout(true);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //弹出去暂停
        isPlay = videoView.onActivityStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rl_parent.removeOnLayoutChangeListener(rl_parent_onLayoutChangeListener);
        videoView.destroy();
        auxiliaryView.hide();
        // 关闭广告监测器
        AdmasterSdk.terminateSDK();
    }
}
