package com.easefun.polyvsdk.live.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.admaster.sdk.api.AdmasterSdk;
import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.fragment.PolyvChatFragment;
import com.easefun.polyvsdk.live.fragment.PolyvDanmuFragment;
import com.easefun.polyvsdk.live.fragment.PolyvTabFragment;
import com.easefun.polyvsdk.live.fragment.PolyvTabViewPagerFragment;
import com.easefun.polyvsdk.live.playback.activity.PolyvPPTPbPlayerActivity;
import com.easefun.polyvsdk.live.playback.activity.PolyvPbPlayerActivity;
import com.easefun.polyvsdk.live.util.AdmasterSdkUtils;
import com.easefun.polyvsdk.live.util.PolyvKickAssist;
import com.easefun.polyvsdk.live.util.PolyvMarqueeUtils;
import com.easefun.polyvsdk.live.util.PolyvScreenUtils;
import com.easefun.polyvsdk.live.PolyvLiveSDKUtil;
import com.easefun.polyvsdk.live.chat.PolyvChatManager;
import com.easefun.polyvsdk.live.chat.playback.api.PolyvLive_Status;
import com.easefun.polyvsdk.live.chat.playback.api.listener.PolyvLive_StatusNorListener;
import com.easefun.polyvsdk.live.player.PolyvPlayerAuxiliaryView;
import com.easefun.polyvsdk.live.player.PolyvPlayerLightView;
import com.easefun.polyvsdk.live.player.PolyvPlayerMediaController;
import com.easefun.polyvsdk.live.player.PolyvPlayerVolumeView;
import com.easefun.polyvsdk.live.video.PolyvLiveMediaInfoType;
import com.easefun.polyvsdk.live.video.PolyvLivePlayErrorReason;
import com.easefun.polyvsdk.live.video.PolyvLiveVideoView;
import com.easefun.polyvsdk.live.video.PolyvLiveVideoViewListener;
import com.easefun.polyvsdk.live.video.auxiliary.PolyvLiveAuxiliaryVideoView;
import com.easefun.polyvsdk.live.vo.PolyvLiveBitrateVO;
import com.easefun.polyvsdk.live.vo.PolyvLiveChannelVO;
import com.easefun.polyvsdk.live.vo.PolyvLiveMarqueeVo;
import com.easefun.polyvsdk.marquee.PolyvMarqueeItem;
import com.easefun.polyvsdk.marquee.PolyvMarqueeView;

import java.net.MalformedURLException;
import java.net.URL;

public class PolyvLivePlayerActivity extends FragmentActivity {
    private static final String TAG = PolyvLivePlayerActivity.class.getSimpleName();

    private String userId, channelId, chatUserId, nickName;
    // 聊天室管理类
    private PolyvChatManager chatManager;
    // 聊天室fragment
    private PolyvChatFragment polyvChatFragment;
    private PolyvTabViewPagerFragment tabViewPagerFragment;
    private PolyvDanmuFragment danmuFragment;
    // 获取直播状态接口
    private PolyvLive_Status live_status;
    /**
     * 播放器的parentView
     */
    private RelativeLayout viewLayout = null;
    /**
     * 播放器主类
     */
    private PolyvLiveVideoView videoView = null;
    /**
     * 跑马灯控件
     */
    private PolyvMarqueeView marqueeView = null;
    private PolyvMarqueeItem marqueeItem = null;
    private PolyvMarqueeUtils marqueeUtils = null;
    //暂停时显示的20%透明度背景
    private View v_pause_bg;
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
    // 当再次直播的类型是ppt直播时，是否需要跳转到ppt直播的播放器
    private boolean isToOtherLivePlayer = true;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            savedInstanceState.putParcelable("android:support:fragments", null);
        super.onCreate(savedInstanceState);
        //检测用户是否被踢，用户被踢则不能观看直播及回放，退出应用再进入可恢复
        channelId = getIntent().getStringExtra("channelId");
        if (PolyvKickAssist.checkKickAndTips(channelId, this))
            return;
        setContentView(R.layout.polyv_activity_player_live);
        // 生成播放器父控件的宽高比为16:9的高
        PolyvScreenUtils.generateHeight16_9(this);
        // 初始化广告监测器
        AdmasterSdk.init(getApplicationContext(), "");

        chatUserId = getIntent().getStringExtra("chatUserId");
        nickName = getIntent().getStringExtra("nickName");

        userId = getIntent().getStringExtra("userId");

        isToOtherLivePlayer = getIntent().getBooleanExtra("isToOtherLivePlayer", true);

        addFragment();
        findIdAndNew();
        initView();

        boolean isLandscape = getIntent().getBooleanExtra("isLandscape", false);
        if (isLandscape)
            mediaController.changeToLandscape();
        else
            mediaController.changeToPortrait();

        setLivePlay(userId, channelId);

//        String params = PolyvLiveSDKUtil.creatSignParams(PolyvApplication.appId,PolyvApplication.appSecret);
//        videoView.getVideoDefinitions(params);

    }



    private void addFragment() {
        PolyvTabFragment tabFragment = new PolyvTabFragment();
        tabViewPagerFragment = new PolyvTabViewPagerFragment();
        polyvChatFragment = new PolyvChatFragment();
        // 聊天室实例
        chatManager = new PolyvChatManager();
        polyvChatFragment.initChatConfig(chatManager, chatUserId, userId, channelId);
        tabViewPagerFragment.initConfig(polyvChatFragment);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fl_tab, tabFragment, "tabFragment")
                .add(R.id.fl_tag_viewpager, tabViewPagerFragment, "tabViewPagerFragment");


        danmuFragment = new PolyvDanmuFragment();
        fragmentTransaction.add(R.id.fl_danmu, danmuFragment, "danmuFragment").commit();

        tabViewPagerFragment.setDanmuFragment(danmuFragment);
    }

    private void findIdAndNew() {
        viewLayout = (RelativeLayout) findViewById(R.id.view_layout);
        videoView = (PolyvLiveVideoView) findViewById(R.id.polyv_live_video_view);
        marqueeView = (PolyvMarqueeView) findViewById(R.id.polyv_marquee_view);
        v_pause_bg = findViewById(R.id.v_pause_bg);
        mediaController = (PolyvPlayerMediaController) findViewById(R.id.polyv_player_media_controller);
        ProgressBar loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);
        ImageView noStream = (ImageView) findViewById(R.id.no_stream);
        auxiliaryVideoView = (PolyvLiveAuxiliaryVideoView) findViewById(R.id.polyv_live_auxiliary_video_view);
        ProgressBar auxiliaryLoadingProgress = (ProgressBar) findViewById(R.id.auxiliary_loading_progress);
        auxiliaryView = (PolyvPlayerAuxiliaryView) findViewById(R.id.polyv_player_auxiliary_view);
        lightView = (PolyvPlayerLightView) findViewById(R.id.polyv_player_light_view);
        volumeView = (PolyvPlayerVolumeView) findViewById(R.id.polyv_player_volume_view);
        advertCountDown = (TextView) findViewById(R.id.count_down);

        mediaController.initConfig(viewLayout, false);
        mediaController.setDanmuFragment(danmuFragment);
        tabViewPagerFragment.setVideoView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setAuxiliaryVideoView(auxiliaryVideoView);
        videoView.setPlayerBufferingIndicator(loadingProgress);
        videoView.setNoStreamIndicator(noStream);
        auxiliaryVideoView.setPlayerBufferingIndicator(auxiliaryLoadingProgress);
        auxiliaryView.setPolyvLiveVideoView(videoView);
        // 设置跑马灯
        videoView.setMarqueeView(marqueeView, marqueeItem = new PolyvMarqueeItem());
    }

    private void initView() {
        videoView.setOpenMarquee(true);
        videoView.setOpenNotLivePlayback(isToOtherLivePlayer);
        videoView.setOpenWait(true);
        videoView.setOpenAd(true);
        videoView.setOpenPreload(true, 2);
        videoView.setNeedGestureDetector(true);

        videoView.setAsyncDataCallback(new PolyvLiveVideoViewListener.AsyncDataCallback<PolyvLiveBitrateVO>() {
            @Override
            public void onSuccess(PolyvLiveBitrateVO data, int type) {
                if(mediaController != null){
                    mediaController.initBitList(data);
                }
            }

            @Override
            public void onFailed() {

            }
        });
        videoView.setOnPreparedListener(new PolyvLiveVideoViewListener.OnPreparedListener() {
            @Override
            public void onPrepared() {
                Toast.makeText(PolyvLivePlayerActivity.this, "准备完毕，可以播放", Toast.LENGTH_SHORT).show();
            }
        });

        videoView.setOnVideoPlayListener(new PolyvLiveVideoViewListener.OnVideoPlayListener() {
            @Override
            public void onPlay() {
                v_pause_bg.setVisibility(View.GONE);
            }
        });

        videoView.setOnVideoPauseListener(new PolyvLiveVideoViewListener.OnVideoPauseListener() {
            @Override
            public void onPause() {
                v_pause_bg.setVisibility(View.VISIBLE);
            }
        });

        videoView.setOnInfoListener(new PolyvLiveVideoViewListener.OnInfoListener() {
            @Override
            public void onInfo(int what, int extra) {
                switch (what) {
                    case PolyvLiveMediaInfoType.MEDIA_INFO_BUFFERING_START:
                        Toast.makeText(PolyvLivePlayerActivity.this, "开始缓冲", Toast.LENGTH_SHORT).show();
                        break;

                    case PolyvLiveMediaInfoType.MEDIA_INFO_BUFFERING_END:
                        Toast.makeText(PolyvLivePlayerActivity.this, "结束缓冲", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        videoView.setOnVideoPlayErrorListener(new PolyvLiveVideoViewListener.OnVideoPlayErrorListener() {
            @Override
            public void onVideoPlayError(@NonNull PolyvLivePlayErrorReason errorReason) {
                switch (errorReason.getType()) {
                    case NETWORK_DENIED:
                        Toast.makeText(PolyvLivePlayerActivity.this, "无法连接网络，请连接网络后播放", Toast.LENGTH_SHORT).show();
                        break;

                    case START_ERROR:
                        Toast.makeText(PolyvLivePlayerActivity.this, "播放错误，请重新播放(error code " + PolyvLivePlayErrorReason.ErrorType.START_ERROR.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;

                    case CHANNEL_NULL:
                        Toast.makeText(PolyvLivePlayerActivity.this, "频道信息获取失败，请重新播放(error code " + PolyvLivePlayErrorReason.ErrorType.CHANNEL_NULL.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;

                    case LIVE_UID_NOT_EQUAL:
                        Toast.makeText(PolyvLivePlayerActivity.this, "用户id错误，请重新设置(error code" + PolyvLivePlayErrorReason.ErrorType.LIVE_UID_NOT_EQUAL.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;

                    case LIVE_CID_NOT_EQUAL:
                        Toast.makeText(PolyvLivePlayerActivity.this, "频道号错误，请重新设置(error code " + PolyvLivePlayErrorReason.ErrorType.LIVE_CID_NOT_EQUAL.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;

                    case LIVE_PLAY_ERROR:
                        Toast.makeText(PolyvLivePlayerActivity.this, "播放错误，请稍后重试(error code " + PolyvLivePlayErrorReason.ErrorType.LIVE_PLAY_ERROR.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;

                    case RESTRICT_NULL:
                        Toast.makeText(PolyvLivePlayerActivity.this, "限制信息错误，请稍后重试(error code " + PolyvLivePlayErrorReason.ErrorType.RESTRICT_NULL.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;

                    case RESTRICT_ERROR:
                        Toast.makeText(PolyvLivePlayerActivity.this, errorReason.getErrorMsg() + "(error code " + PolyvLivePlayErrorReason.ErrorType.RESTRICT_ERROR.getCode() + ")", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        videoView.setOnErrorListener(new PolyvLiveVideoViewListener.OnErrorListener() {
            @Override
            public void onError() {
                Toast.makeText(PolyvLivePlayerActivity.this, "播放错误，请稍后重试(error code " + PolyvLivePlayErrorReason.ErrorType.WAIT_PLAY_ERROR.getCode() + ")", Toast.LENGTH_SHORT).show();
            }
        });

        videoView.setOnCoverImageOutListener(new PolyvLiveVideoViewListener.OnCoverImageOutListener() {
            @Override
            public void onOut(@NonNull String url, @Nullable String clickPath) {
                auxiliaryView.show(url, clickPath);
            }
        });

        videoView.setOnWillPlayWaittingListener(new PolyvLiveVideoViewListener.OnWillPlayWaittingListener() {
            @Override
            public void onWillPlayWaitting(boolean isCoverImage) {
                Toast.makeText(PolyvLivePlayerActivity.this, "当前暂无直播，将播放暖场" + (isCoverImage ? "图片" : "视频"), Toast.LENGTH_LONG).show();
                // 获取直播状态
                getLive_Status();
            }
        });

        videoView.setOnNoLiveAtPresentListener(new PolyvLiveVideoViewListener.OnNoLiveAtPresentListener() {
            @Override
            public void onNoLiveAtPresent() {
                // 获取直播状态
                getLive_Status();
            }
        });

        videoView.setOnNoLivePlaybackListener(new PolyvLiveVideoViewListener.OnNoLivePlaybackListener() {
            @Override
            public void onNoLivePlayback(String playbackUrl, String recordFileSessionId, String title, boolean isList) {
                Toast.makeText(PolyvLivePlayerActivity.this, "当前暂无直播，将播放回放视频", Toast.LENGTH_SHORT).show();
                Intent playIntent;
                if (TextUtils.isEmpty(recordFileSessionId)) {
                    playIntent = PolyvPbPlayerActivity.newUrlIntent(PolyvLivePlayerActivity.this, playbackUrl, title, false);
                    playIntent = PolyvPbPlayerActivity.addChatExtra(playIntent, userId, channelId, chatUserId, nickName, true);
                    playIntent = PolyvPbPlayerActivity.addLiveExtra(playIntent, false, isToOtherLivePlayer);
                    playIntent = PolyvPbPlayerActivity.addPlaybackParam(playIntent, videoView.getPlaybackParam());
                } else {
                    playIntent = PolyvPPTPbPlayerActivity.newUrlIntent(PolyvLivePlayerActivity.this, playbackUrl, title, recordFileSessionId, isList, false);
                    playIntent = PolyvPPTPbPlayerActivity.addChatExtra(playIntent, userId, channelId, chatUserId, nickName, true);
                    playIntent = PolyvPPTPbPlayerActivity.addLiveExtra(playIntent, false, isToOtherLivePlayer);
                    playIntent = PolyvPPTPbPlayerActivity.addPlaybackParam(playIntent, videoView.getPlaybackParam());
                }
                startActivity(playIntent);
                finish();
            }
        });

        videoView.setOnGetMarqueeVoListener(new PolyvLiveVideoViewListener.OnGetMarqueeVoListener() {
            @Override
            public void onGetMarqueeVo(PolyvLiveMarqueeVo marqueeVo) {
                if (marqueeUtils == null)
                    marqueeUtils = new PolyvMarqueeUtils();
                // 更新为后台设置的跑马灯类型
                marqueeUtils.updateMarquee(PolyvLivePlayerActivity.this, marqueeVo, marqueeItem, channelId, userId, nickName);
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
                Log.d(TAG, String.format("LeftUp %b %b brightness %d", start, end, videoView.getBrightness(PolyvLivePlayerActivity.this)));
                int brightness = videoView.getBrightness(PolyvLivePlayerActivity.this) + 5;
                if (brightness > 100) {
                    brightness = 100;
                }

                videoView.setBrightness(PolyvLivePlayerActivity.this, brightness);
                lightView.setViewLightValue(brightness, end);
            }
        });

        videoView.setOnGestureLeftDownListener(new PolyvLiveVideoViewListener.OnGestureLeftDownListener() {

            @Override
            public void callback(boolean start, boolean end) {
                Log.d(TAG, String.format("LeftDown %b %b brightness %d", start, end, videoView.getBrightness(PolyvLivePlayerActivity.this)));
                int brightness = videoView.getBrightness(PolyvLivePlayerActivity.this) - 5;
                if (brightness < 0) {
                    brightness = 0;
                }

                videoView.setBrightness(PolyvLivePlayerActivity.this, brightness);
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
    }

    private void prepare() {
        advertCountDown.setVisibility(View.GONE);
        auxiliaryView.hide();
        // 取消请求
        if (marqueeUtils != null)
            marqueeUtils.shutdown();
    }

    public static Intent newIntent(Context context, String userId, String channelId, String chatUserId, String nickName, boolean isLandscape) {
        return newIntent(context, userId, channelId, chatUserId, nickName, isLandscape, true);
    }

    public static Intent newIntent(Context context, String userId, String channelId, String chatUserId, String nickName, boolean isLandscape, boolean isToOtherLivePlayer) {
        Intent intent = new Intent(context, PolyvLivePlayerActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("channelId", channelId);
        intent.putExtra("chatUserId", chatUserId);
        intent.putExtra("nickName", nickName);
        intent.putExtra("isLandscape", isLandscape);
        intent.putExtra("isToOtherLivePlayer", isToOtherLivePlayer);
        return intent;
    }

    /**
     * 播放普通直播。<br/>
     */
    public void setLivePlay(String userId, String channelId) {
        prepare();
        videoView.setLivePlay(userId, channelId, false);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment == polyvChatFragment) {
            chatManager.login(chatUserId, channelId, nickName);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        tabViewPagerFragment.getOnlineListFragment().onActivityResult(requestCode, resultCode, data);
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
            if (PolyvScreenUtils.isPortrait(this) && tabViewPagerFragment.getQuestionFragment().emoLayoutIsVisible()) {
                tabViewPagerFragment.getQuestionFragment().resetEmoLayout(true);
                return true;
            }
            if (PolyvScreenUtils.isPortrait(this) && tabViewPagerFragment.getOnlineListFragment().isLinkMicConnected()) {
                tabViewPagerFragment.getOnlineListFragment().showStopCallDialog(true);
                return true;
            }
            if (tabViewPagerFragment.onBackPress()) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    // 获取直播状态
    private void getLive_Status() {
        if (live_status == null)
            live_status = new PolyvLive_Status();
        live_status.shutdownSchedule();
        live_status.getLive_Status(channelId, 6000, 4000, new PolyvLive_StatusNorListener() {
            @Override
            public void success(boolean isLiving, final boolean isPPTLive) {
                if (isLiving) {
                    live_status.shutdownSchedule();
                    if (!isFinishing()) {
                        Toast.makeText(PolyvLivePlayerActivity.this, "直播开始了！", Toast.LENGTH_SHORT).show();
                        if (!isPPTLive || !isToOtherLivePlayer)
                            setLivePlay(userId, channelId);
                        else {
                            startActivity(PolyvPPTLivePlayerActivity.newIntent(PolyvLivePlayerActivity.this, userId, channelId, chatUserId, nickName, false));
                            finish();
                        }
                    }
                }
            }

            @Override
            public void fail(String failTips, int code) {
            }
        });
    }

    private void clearGestureInfo() {
        if (videoView != null)
            videoView.clearGestureInfo();
        if (lightView != null)
            lightView.hide();
        if (volumeView != null)
            volumeView.hide();
    }

    @Override
    public void onResume() {
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
    public void onPause() {
        super.onPause();
        clearGestureInfo();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (videoView != null) {
            //弹出去暂停
            isPlay = videoView.onActivityStop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消请求
        if (live_status != null)
            live_status.shutdownSchedule();
        if (marqueeUtils != null)
            marqueeUtils.shutdown();
        // 退出聊天室
        if (chatManager != null)
            chatManager.disconnect();
        if (videoView != null)
            videoView.destroy();
        if (auxiliaryView != null)
            auxiliaryView.hide();
        if (mediaController != null)
            mediaController.disable();
        // 关闭广告监测器
        AdmasterSdk.terminateSDK();
    }
}
