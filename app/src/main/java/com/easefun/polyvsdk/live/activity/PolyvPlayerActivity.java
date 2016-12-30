package com.easefun.polyvsdk.live.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
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
import com.easefun.polyvsdk.live.PolyvLiveSDKUtil;
import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.fragment.PolyvChatFragment;
import com.easefun.polyvsdk.live.fragment.PolyvDanmuFragment;
import com.easefun.polyvsdk.live.player.PolyvPlayerAuxiliaryView;
import com.easefun.polyvsdk.live.player.PolyvPlayerLightView;
import com.easefun.polyvsdk.live.player.PolyvPlayerMediaController;
import com.easefun.polyvsdk.live.player.PolyvPlayerVolumeView;
import com.easefun.polyvsdk.live.util.AdmasterSdkUtils;
import com.easefun.polyvsdk.live.util.PolyvScreenUtils;
import com.easefun.polyvsdk.live.video.PolyvLivePlayErrorReason;
import com.easefun.polyvsdk.live.video.PolyvLiveVideoView;
import com.easefun.polyvsdk.live.video.PolyvLiveVideoViewListener;
import com.easefun.polyvsdk.live.video.auxiliary.PolyvLiveAuxiliaryVideoView;
import com.easefun.polyvsdk.live.vo.PolyvLiveChannelVO;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 播放器 activity，视频显示主类，{@link com.easefun.polyvsdk.live.video.PolyvLiveVideoView} 演示主类
 */
public class PolyvPlayerActivity extends FragmentActivity {
    private static final String TAG = PolyvPlayerActivity.class.getSimpleName();
    private PolyvChatFragment polyvChatFragment;
    private PolyvDanmuFragment danmuFragment;
    private String uid, cid;
    private RelativeLayout viewLayout = null;
    /** 播放器主类 */
    private PolyvLiveVideoView videoView = null;
    /** 播放器控制栏 */
    private PolyvPlayerMediaController mediaController = null;
    /** 辅助播放器类，用于播放视频片头广告 */
    private PolyvLiveAuxiliaryVideoView auxiliaryVideoView = null;
    /** 辅助显示类，用于显示图片广告 */
    private PolyvPlayerAuxiliaryView auxiliaryView = null;
    /** 手势亮度指示标志 */
    private PolyvPlayerLightView lightView = null;
    /** 手势音量指示标志 */
    private PolyvPlayerVolumeView volumeView = null;
    /** 用于显示广告倒计时 */
    private TextView advertCountDown = null;
    private boolean isPlay = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.polyv_activity_player);

        uid = getIntent().getStringExtra("uid");
        cid = getIntent().getStringExtra("cid");
        // 初始化广告监测器
        AdmasterSdk.init(this, "");

        addFragment();
        findIdAndNew();
        initView();
    }

    private void addFragment() {
        polyvChatFragment = new PolyvChatFragment();
        // 初始化聊天室的配置
        polyvChatFragment.initChatConfig(uid, cid, "游客");
        danmuFragment = new PolyvDanmuFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fl_chat, polyvChatFragment, "chatFragment")
                .add(R.id.fl_danmu, danmuFragment, "danmuFragment").commit();
    }

    private void findIdAndNew() {
        viewLayout = (RelativeLayout) findViewById(R.id.view_layout);
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
    }

    private void initView() {
        videoView.setOpenAd(true);
        videoView.setOpenPreload(true, 2);
        videoView.setNeedGestureDetector(true);

        videoView.setOnPreparedListener(new PolyvLiveVideoViewListener.OnPreparedListener() {
            @Override
            public void onPrepared() {

            }
        });

        videoView.setOnInfoListener(new PolyvLiveVideoViewListener.OnInfoListener() {
            @Override
            public void onInfo(int what, int extra) {

            }
        });

        videoView.setOnVideoPlayErrorListener(new PolyvLiveVideoViewListener.OnVideoPlayErrorListener() {
            @Override
            public void onVideoPlayError(@NonNull PolyvLivePlayErrorReason errorReason) {
                switch (errorReason.getType()) {
                    case NETWORK_DENIED:
                        Toast.makeText(PolyvPlayerActivity.this, "无法连接网络，请连接网络后再试", Toast.LENGTH_SHORT).show();
                        break;

                    case START_ERROR:
                        Toast.makeText(PolyvPlayerActivity.this, "视频开始播放错误，请重新播放", Toast.LENGTH_SHORT).show();
                        break;

                    case CHANNEL_NULL:
                        Toast.makeText(PolyvPlayerActivity.this, "频道信息为空", Toast.LENGTH_SHORT).show();
                        break;

                    case LIVE_UID_NOT_EQUAL:
                        Toast.makeText(PolyvPlayerActivity.this, "userId与服务器userId不匹配，请重新设置", Toast.LENGTH_SHORT).show();
                        break;

                    case LIVE_CID_NOT_EQUAL:
                        Toast.makeText(PolyvPlayerActivity.this, "channelId与服务器channelId不匹配，请重新设置", Toast.LENGTH_SHORT).show();
                        break;

                    case LIVE_PLAY_ERROR:
                        Toast.makeText(PolyvPlayerActivity.this, "播放错误，请稍后重试", Toast.LENGTH_SHORT).show();
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

        videoView.setLivePlay(uid, cid, false);
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
        videoView.destroy();
        auxiliaryView.hide();
        // 关闭广告监测器
        AdmasterSdk.terminateSDK();
    }
}
