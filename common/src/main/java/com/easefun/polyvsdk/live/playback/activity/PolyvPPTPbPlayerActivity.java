package com.easefun.polyvsdk.live.playback.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easefun.polyvsdk.PolyvBitRate;
import com.easefun.polyvsdk.PolyvSDKUtil;
import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.util.PolyvKickAssist;
import com.easefun.polyvsdk.live.util.PolyvScreenUtils;
import com.easefun.polyvsdk.ijk.PolyvPlayerScreenRatio;
import com.easefun.polyvsdk.live.activity.PolyvLivePlayerActivity;
import com.easefun.polyvsdk.live.activity.PolyvPPTLivePlayerActivity;
import com.easefun.polyvsdk.live.chat.IPolyvLivePPTView;
import com.easefun.polyvsdk.live.chat.PolyvChatManager;
import com.easefun.polyvsdk.live.chat.PolyvLivePPTLoadErrorListener;
import com.easefun.polyvsdk.live.chat.playback.api.PolyvLive_Status;
import com.easefun.polyvsdk.live.chat.playback.api.listener.PolyvLive_StatusNorListener;
import com.easefun.polyvsdk.live.fragment.PolyvChatFragment;
import com.easefun.polyvsdk.live.fragment.PolyvDanmuFragment;
import com.easefun.polyvsdk.live.fragment.PolyvTabFragment;
import com.easefun.polyvsdk.live.fragment.PolyvTabViewPagerFragment;
import com.easefun.polyvsdk.live.fragment.PolyvTipsFragment;
import com.easefun.polyvsdk.live.playback.player.PolyvPlayerAuditionView;
import com.easefun.polyvsdk.live.playback.player.PolyvPlayerAuxiliaryView;
import com.easefun.polyvsdk.live.playback.player.PolyvPlayerMediaController;
import com.easefun.polyvsdk.live.playback.player.PolyvPlayerPreviewView;
import com.easefun.polyvsdk.live.playback.player.PolyvPlayerProgressView;
import com.easefun.polyvsdk.live.playback.player.PolyvPlayerQuestionView;
import com.easefun.polyvsdk.live.playback.util.PolyvErrorMessageUtils;
import com.easefun.polyvsdk.live.player.PolyvPlayerLightView;
import com.easefun.polyvsdk.live.player.PolyvPlayerVolumeView;
import com.easefun.polyvsdk.live.video.PolyvPlaybackParam;
import com.easefun.polyvsdk.live.view.PolyvGestureLayout;
import com.easefun.polyvsdk.marquee.PolyvMarqueeItem;
import com.easefun.polyvsdk.marquee.PolyvMarqueeView;
import com.easefun.polyvsdk.srt.PolyvSRTItemVO;
import com.easefun.polyvsdk.video.PolyvMediaInfoType;
import com.easefun.polyvsdk.video.PolyvPlayErrorReason;
import com.easefun.polyvsdk.video.PolyvVideoView;
import com.easefun.polyvsdk.video.auxiliary.PolyvAuxiliaryVideoView;
import com.easefun.polyvsdk.video.listener.IPolyvOnAdvertisementCountDownListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnAdvertisementEventListener2;
import com.easefun.polyvsdk.video.listener.IPolyvOnAdvertisementOutListener2;
import com.easefun.polyvsdk.video.listener.IPolyvOnCompletionListener2;
import com.easefun.polyvsdk.video.listener.IPolyvOnErrorListener2;
import com.easefun.polyvsdk.video.listener.IPolyvOnGestureClickListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnGestureLeftDownListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnGestureLeftUpListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnGestureRightDownListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnGestureRightUpListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnGestureSwipeLeftListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnGestureSwipeRightListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnInfoListener2;
import com.easefun.polyvsdk.video.listener.IPolyvOnPreloadPlayListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnPreparedListener2;
import com.easefun.polyvsdk.video.listener.IPolyvOnQuestionAnswerTipsListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnQuestionOutListener2;
import com.easefun.polyvsdk.video.listener.IPolyvOnTeaserCountDownListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnTeaserOutListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnVideoPlayErrorListener2;
import com.easefun.polyvsdk.video.listener.IPolyvOnVideoSRTListener;
import com.easefun.polyvsdk.video.listener.IPolyvOnVideoStatusListener;
import com.easefun.polyvsdk.vo.PolyvADMatterVO;
import com.easefun.polyvsdk.vo.PolyvQuestionVO;

import java.net.MalformedURLException;
import java.net.URL;

public class PolyvPPTPbPlayerActivity extends FragmentActivity {
    private static final String TAG = PolyvPPTPbPlayerActivity.class.getSimpleName();
    private String userId, channelId, chatUserId, nickName, title;
    private PolyvPlaybackParam playbackParam;
    // 聊天室管理类
    private PolyvChatManager chatManager;
    // 聊天室fragment
    private PolyvChatFragment polyvChatFragment;
    private PolyvTabViewPagerFragment tabViewPagerFragment;
    private PolyvDanmuFragment danmuFragment;
    // 获取直播状态接口
    private PolyvLive_Status live_status;
    // 提示对话框
    private PolyvTipsFragment tipsFragment;
    /**
     * ppt回放视图
     */
    private IPolyvLivePPTView ppt_view;
    private RelativeLayout rl_container, rl_parent;
    private View.OnLayoutChangeListener rl_parent_onLayoutChangeListener;
    /**
     * 播放器的parentView
     */
    private PolyvGestureLayout viewLayout = null;
    /**
     * 播放主视频播放器
     */
    private PolyvVideoView videoView = null;
    /**
     * 跑马灯控件
     */
    private PolyvMarqueeView marqueeView = null;
    private PolyvMarqueeItem marqueeItem = null;
    /**
     * 视频控制栏
     */
    private PolyvPlayerMediaController mediaController = null;
    /**
     * 字幕文本视图
     */
    private TextView srtTextView = null;
    /**
     * 普通问答界面
     */
    private PolyvPlayerQuestionView questionView = null;
    /**
     * 语音问答界面
     */
    private PolyvPlayerAuditionView auditionView = null;
    /**
     * 用于播放广告片头的播放器
     */
    private PolyvAuxiliaryVideoView auxiliaryVideoView = null;
    /**
     * 视频广告，视频片头加载缓冲视图
     */
    private ProgressBar auxiliaryLoadingProgress = null;
    /**
     * 图片广告界面
     */
    private PolyvPlayerAuxiliaryView auxiliaryView = null;
    /**
     * 广告倒计时
     */
    private TextView advertCountDown = null;
    /**
     * 缩略图界面
     */
    private PolyvPlayerPreviewView firstStartView = null;
    /**
     * 手势出现的亮度界面
     */
    private PolyvPlayerLightView lightView = null;
    /**
     * 手势出现的音量界面
     */
    private PolyvPlayerVolumeView volumeView = null;
    /**
     * 手势出现的进度界面
     */
    private PolyvPlayerProgressView progressView = null;
    /**
     * 视频加载缓冲视图
     */
    private ProgressBar loadingProgress = null;

    private int fastForwardPos = 0;
    private boolean isPlay = false;
    // 是否是播放url
    private boolean isPlaybackUrl = false;
    // 是否定期获取直播状态
    private boolean isGetLiveStatus = false;
    // 是否是从ppt直播跳转到回放的
    private boolean isFromPPTLive = false;
    // 当再次直播的类型不是之前的，是否需要跳转到对应直播类型的播放器
    private boolean isToOtherLivePlayer = true;

    public static final String FINISH_ACTIVITY = "finish_PolyvPPTPbPlayerActivity";
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(FINISH_ACTIVITY))
                finish();
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        boolean isList = intent.getBooleanExtra("isList", true);
        String sessionId = intent.getStringExtra("sessionId");
        String playbackUrl = intent.getStringExtra("playbackUrl");
        if (TextUtils.isEmpty(playbackUrl)) {
            String vid = intent.getStringExtra("vid");
            int bitrate = intent.getIntExtra("bitrate", PolyvBitRate.ziDong.getNum());
            boolean startNow = intent.getBooleanExtra("startNow", false);
            boolean isMustFromLocal = intent.getBooleanExtra("isMustFromLocal", false);
            playVid(vid, bitrate, startNow, isMustFromLocal, sessionId, isList);
        } else {
            String title = intent.getStringExtra("title");
            playUrl(playbackUrl, title, sessionId, isList);
        }
        tabViewPagerFragment.getPlaybackListFragment().initView(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null)
            savedInstanceState.putParcelable("android:support:fragments", null);
        super.onCreate(savedInstanceState);
        //检测用户是否被踢，用户被踢则不能观看直播及回放，退出应用再进入可恢复
        channelId = getIntent().getStringExtra("channelId");
        if (PolyvKickAssist.checkKickAndTips(channelId, this))
            return;
        setContentView(R.layout.polyv_activity_player_pptplayback);
        // 生成播放器父控件的宽高比为16:9的高
        PolyvScreenUtils.generateHeight16_9(this);

        chatUserId = getIntent().getStringExtra("chatUserId");
        nickName = getIntent().getStringExtra("nickName");

        userId = getIntent().getStringExtra("userId");

        isGetLiveStatus = getIntent().getBooleanExtra("isGetLiveStatus", false);
        isFromPPTLive = getIntent().getBooleanExtra("isFromPPTLive", false);
        isToOtherLivePlayer = getIntent().getBooleanExtra("isToOtherLivePlayer", true);
        playbackParam = getIntent().getParcelableExtra("playbackParam");

        addFragment();
        findIdAndNew();
        initView();

        boolean isLandscape = getIntent().getBooleanExtra("isLandscape", false);
        if (isLandscape)
            mediaController.changeToLandscape();
        else
            mediaController.changeToPortrait();

        boolean isList = getIntent().getBooleanExtra("isList", true);
        String sessionId = getIntent().getStringExtra("sessionId");
        String playbackUrl = getIntent().getStringExtra("playbackUrl");
        if (TextUtils.isEmpty(playbackUrl)) {
            String vid = getIntent().getStringExtra("vid");
            int bitrate = getIntent().getIntExtra("bitrate", PolyvBitRate.ziDong.getNum());
            boolean startNow = getIntent().getBooleanExtra("startNow", false);
            boolean isMustFromLocal = getIntent().getBooleanExtra("isMustFromLocal", false);
            playVid(vid, bitrate, startNow, isMustFromLocal, sessionId, isList);
        } else {
            String title = getIntent().getStringExtra("title");
            playUrl(playbackUrl, title, sessionId, isList);
        }

        // 注册广播接收器
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(FINISH_ACTIVITY));
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
        viewLayout = (PolyvGestureLayout) findViewById(R.id.view_layout);
        videoView = (PolyvVideoView) findViewById(R.id.polyv_video_view);
        marqueeView = (PolyvMarqueeView) findViewById(R.id.polyv_marquee_view);
        mediaController = (PolyvPlayerMediaController) findViewById(R.id.polyv_player_media_controller);
        srtTextView = (TextView) findViewById(R.id.srt);
        questionView = (PolyvPlayerQuestionView) findViewById(R.id.polyv_player_question_view);
        auditionView = (PolyvPlayerAuditionView) findViewById(R.id.polyv_player_audition_view);
        auxiliaryVideoView = (PolyvAuxiliaryVideoView) findViewById(R.id.polyv_auxiliary_video_view);
        auxiliaryLoadingProgress = (ProgressBar) findViewById(R.id.auxiliary_loading_progress);
        auxiliaryView = (PolyvPlayerAuxiliaryView) findViewById(R.id.polyv_player_auxiliary_view);
        advertCountDown = (TextView) findViewById(R.id.count_down);
        firstStartView = (PolyvPlayerPreviewView) findViewById(R.id.polyv_player_first_start_view);
        lightView = (PolyvPlayerLightView) findViewById(R.id.polyv_player_light_view);
        volumeView = (PolyvPlayerVolumeView) findViewById(R.id.polyv_player_volume_view);
        progressView = (PolyvPlayerProgressView) findViewById(R.id.polyv_player_progress_view);
        loadingProgress = (ProgressBar) findViewById(R.id.loading_progress);

        mediaController.initConfig(viewLayout, true);
        mediaController.setDanmuFragment(danmuFragment);
        tabViewPagerFragment.setVideoView(videoView);
        questionView.setPolyvVideoView(videoView);
        auditionView.setPolyvVideoView(videoView);
        auxiliaryVideoView.setPlayerBufferingIndicator(auxiliaryLoadingProgress);
        auxiliaryView.setPolyvVideoView(videoView);
        videoView.setMediaController(mediaController);
        videoView.setAuxiliaryVideoView(auxiliaryVideoView);
        videoView.setPlayerBufferingIndicator(loadingProgress);
        // 设置跑马灯
        videoView.setMarqueeView(marqueeView, marqueeItem = new PolyvMarqueeItem()
                .setStyle(PolyvMarqueeItem.STYLE_ROLL_FLICK) //样式
                .setDuration(10000) //时长
                .setText("POLYV Android SDK") //文本
                .setSize(16) //字体大小
                .setColor(Color.YELLOW) //字体颜色
                .setTextAlpha(70) //字体透明度
                .setInterval(1000) //隐藏时间
                .setLifeTime(1000) //显示时间
                .setTweenTime(1000) //渐隐渐现时间
                .setHasStroke(true) //是否有描边
                .setBlurStroke(true) //是否模糊描边
                .setStrokeWidth(3) //描边宽度
                .setStrokeColor(Color.MAGENTA) //描边颜色
                .setStrokeAlpha(70)); //描边透明度

        // ppt回放配置
        mediaController.setPopupWindowDismissListener(new PolyvPlayerMediaController.PopupWindowDismissListener() {
            @Override
            public void dismiss() {
                clearGestureInfo();
            }
        });
        rl_container = (RelativeLayout) findViewById(R.id.rl_container);
        rl_parent = (RelativeLayout) findViewById(R.id.rl_parent);
        ppt_view = (IPolyvLivePPTView) findViewById(R.id.ppt_view);
        viewLayout.setPolyvLiveVideoView(videoView);
        // 须使用这个属性填充整个父控件，且之后不能再调用该方法
        videoView.setAspectRatio(PolyvPlayerScreenRatio.AR_ASPECT_FILL_PARENT);
        // param2:false表示ppt绘制控件的生命周期(pause,reume,destroy)需在activity中处理，如果为true，则交给播放器控制
        videoView.setPPTLiveDrawView(ppt_view, true);
        // param1:videoview与ppt交换位置后，videoview的之前父控件下所有子view在ppt父控件中的位置(此时pptView已移除)
        // param2:videoview与ppt交换位置后，弹幕布局(非控件)所在的位置，没有时须设置为-1。弹幕控件需在弹幕布局的顶层，请参考polyv_fragment_danmu.xml的配置。
        ppt_view.setVideoViewSwapToLayoutIndex(new int[]{0, 9}, 2);
        // param1:pptview初始所在父控件中的索引
        // param2:弹幕布局(非控件)所在父控件的初始位置，没有时须设置为-1
        ppt_view.setPPTViewDefaultIndex(0, 2);
        // 设置ppt加载失败的监听器
        ppt_view.setLoadErrorListener(new PolyvLivePPTLoadErrorListener() {
            @Override
            public void onError(final String failTips, final int code) {
                Toast.makeText(PolyvPPTPbPlayerActivity.this, failTips + "-" + code, Toast.LENGTH_SHORT).show();
            }
        });
        // 初始化pptview的配置
        ppt_view.initParams(videoView);
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
                    if (Math.abs(bottom - oldBottom) > PolyvScreenUtils.getNormalWH(PolyvPPTPbPlayerActivity.this)[1] * 0.3)
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
        videoView.setOpenTeaser(true);
        videoView.setOpenQuestion(true);
        videoView.setOpenSRT(true);
        videoView.setOpenPreload(true, 2);
        videoView.setOpenMarquee(true);
        videoView.setAutoContinue(true);
        videoView.setNeedGestureDetector(true);

        videoView.setOnPreparedListener(new IPolyvOnPreparedListener2() {
            @Override
            public void onPrepared() {
                mediaController.preparedView(isPlaybackUrl, true, title);
                progressView.setViewMaxValue(videoView.getDuration());
            }
        });

        videoView.setOnPreloadPlayListener(new IPolyvOnPreloadPlayListener() {
            @Override
            public void onPlay() {
            }
        });

        videoView.setOnInfoListener(new IPolyvOnInfoListener2() {
            @Override
            public boolean onInfo(int what, int extra) {
                switch (what) {
                    case PolyvMediaInfoType.MEDIA_INFO_BUFFERING_START:
                        break;
                    case PolyvMediaInfoType.MEDIA_INFO_BUFFERING_END:
                        break;
                }

                return true;
            }
        });

        videoView.setOnVideoStatusListener(new IPolyvOnVideoStatusListener() {
            @Override
            public void onStatus(int status) {
                if (status < 60) {
                    Toast.makeText(PolyvPPTPbPlayerActivity.this, "状态错误 " + status, Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, String.format("状态正常 %d", status));
                }
            }
        });

        videoView.setOnVideoPlayErrorListener(new IPolyvOnVideoPlayErrorListener2() {
            @Override
            public boolean onVideoPlayError(@PolyvPlayErrorReason.PlayErrorReason int playErrorReason) {
                String message = PolyvErrorMessageUtils.getPlayErrorMessage(playErrorReason);
                message += "(error code " + playErrorReason + ")";

//                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(PolyvPPTPbPlayerActivity.this);
                builder.setTitle("错误");
                builder.setMessage(message);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });

                if (videoView.getWindowToken() != null)
                    builder.show();
                return true;
            }
        });

        videoView.setOnErrorListener(new IPolyvOnErrorListener2() {
            @Override
            public boolean onError() {
                Toast.makeText(PolyvPPTPbPlayerActivity.this, "当前视频无法播放，请向管理员反馈(error code " + PolyvPlayErrorReason.VIDEO_ERROR + ")", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        videoView.setOnAdvertisementOutListener(new IPolyvOnAdvertisementOutListener2() {
            @Override
            public void onOut(@NonNull PolyvADMatterVO adMatter) {
                auxiliaryView.show(adMatter);
            }
        });

        videoView.setOnAdvertisementCountDownListener(new IPolyvOnAdvertisementCountDownListener() {
            @Override
            public void onCountDown(int num) {
                advertCountDown.setText("广告也精彩：" + num + "秒");
                advertCountDown.setVisibility(View.VISIBLE);
            }

            @Override
            public void onEnd() {
                advertCountDown.setVisibility(View.GONE);
                auxiliaryView.hide();
            }
        });

        videoView.setOnAdvertisementEventListener(new IPolyvOnAdvertisementEventListener2() {
            @Override
            public void onShow(PolyvADMatterVO adMatter) {
                Log.i(TAG, "开始播放视频广告");
            }

            @Override
            public void onClick(PolyvADMatterVO adMatter) {
                if (!TextUtils.isEmpty(adMatter.getAddrUrl())) {
                    try {
                        new URL(adMatter.getAddrUrl());
                    } catch (MalformedURLException e1) {
                        Log.e(TAG, PolyvSDKUtil.getExceptionFullMessage(e1, -1));
                        return;
                    }

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(adMatter.getAddrUrl()));
                    startActivity(intent);
                }
            }
        });

        videoView.setOnQuestionOutListener(new IPolyvOnQuestionOutListener2() {
            @Override
            public void onOut(@NonNull PolyvQuestionVO questionVO) {
                switch (questionVO.getType()) {
                    case PolyvQuestionVO.TYPE_QUESTION:
                        questionView.show(questionVO);
                        break;

                    case PolyvQuestionVO.TYPE_AUDITION:
                        auditionView.show(questionVO);
                        break;
                }
            }
        });

        videoView.setOnTeaserOutListener(new IPolyvOnTeaserOutListener() {
            @Override
            public void onOut(@NonNull String url) {
                auxiliaryView.show(url);
            }
        });

        videoView.setOnTeaserCountDownListener(new IPolyvOnTeaserCountDownListener() {
            @Override
            public void onEnd() {
                auxiliaryView.hide();
            }
        });

        videoView.setOnQuestionAnswerTipsListener(new IPolyvOnQuestionAnswerTipsListener() {

            @Override
            public void onTips(@NonNull String msg) {
                questionView.showAnswerTips(msg);
            }

            @Override
            public void onTips(@NonNull String msg, int seek) {
                questionView.showAnswerTips(msg);
            }
        });

        videoView.setOnCompletionListener(new IPolyvOnCompletionListener2() {
            @Override
            public void onCompletion() {
            }
        });

        videoView.setOnVideoSRTListener(new IPolyvOnVideoSRTListener() {
            @Override
            public void onVideoSRT(@Nullable PolyvSRTItemVO subTitleItem) {
                if (subTitleItem == null) {
                    srtTextView.setText("");
                } else {
                    srtTextView.setText(subTitleItem.getSubTitle());
                }

                srtTextView.setVisibility(View.VISIBLE);
            }
        });

        videoView.setOnGestureLeftUpListener(new IPolyvOnGestureLeftUpListener() {

            @Override
            public void callback(boolean start, boolean end) {
                Log.d(TAG, String.format("LeftUp %b %b brightness %d", start, end, videoView.getBrightness(PolyvPPTPbPlayerActivity.this)));
                int brightness = videoView.getBrightness(PolyvPPTPbPlayerActivity.this) + 5;
                if (brightness > 100) {
                    brightness = 100;
                }

                videoView.setBrightness(PolyvPPTPbPlayerActivity.this, brightness);
                lightView.setViewLightValue(brightness, end);
            }
        });

        videoView.setOnGestureLeftDownListener(new IPolyvOnGestureLeftDownListener() {

            @Override
            public void callback(boolean start, boolean end) {
                Log.d(TAG, String.format("LeftDown %b %b brightness %d", start, end, videoView.getBrightness(PolyvPPTPbPlayerActivity.this)));
                int brightness = videoView.getBrightness(PolyvPPTPbPlayerActivity.this) - 5;
                if (brightness < 0) {
                    brightness = 0;
                }

                videoView.setBrightness(PolyvPPTPbPlayerActivity.this, brightness);
                lightView.setViewLightValue(brightness, end);
            }
        });

        videoView.setOnGestureRightUpListener(new IPolyvOnGestureRightUpListener() {

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

        videoView.setOnGestureRightDownListener(new IPolyvOnGestureRightDownListener() {

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

        videoView.setOnGestureSwipeLeftListener(new IPolyvOnGestureSwipeLeftListener() {

            @Override
            public void callback(boolean start, boolean end) {
                // 左滑事件
                Log.d(TAG, String.format("SwipeLeft %b %b", start, end));
                if (fastForwardPos == 0) {
                    fastForwardPos = videoView.getCurrentPosition();
                }

                if (end) {
                    if (fastForwardPos < 0)
                        fastForwardPos = 0;
                    videoView.seekTo(fastForwardPos);
                    if (videoView.isCompletedState()) {
                        videoView.start();
                    }
                    fastForwardPos = 0;
                } else {
                    fastForwardPos -= 10000;
                    if (fastForwardPos <= 0)
                        fastForwardPos = -1;
                }
                progressView.setViewProgressValue(fastForwardPos, videoView.getDuration(), end, false);
            }
        });

        videoView.setOnGestureSwipeRightListener(new IPolyvOnGestureSwipeRightListener() {

            @Override
            public void callback(boolean start, boolean end) {
                // 右滑事件
                Log.d(TAG, String.format("SwipeRight %b %b", start, end));
                if (fastForwardPos == 0) {
                    fastForwardPos = videoView.getCurrentPosition();
                }

                if (end) {
                    if (fastForwardPos > videoView.getDuration())
                        fastForwardPos = videoView.getDuration();
                    if (!videoView.isCompletedState()) {
                        videoView.seekTo(fastForwardPos);
                    } else if (videoView.isCompletedState() && fastForwardPos != videoView.getDuration()) {
                        videoView.seekTo(fastForwardPos);
                        videoView.start();
                    }
                    fastForwardPos = 0;
                } else {
                    fastForwardPos += 10000;
                    if (fastForwardPos > videoView.getDuration())
                        fastForwardPos = videoView.getDuration();
                }
                progressView.setViewProgressValue(fastForwardPos, videoView.getDuration(), end, true);
            }
        });

        videoView.setOnGestureClickListener(new IPolyvOnGestureClickListener() {
            @Override
            public void callback(boolean start, boolean end) {
                if (videoView.isInPlaybackState() && mediaController != null && videoView.canShowControllerToPPTPlayback())
                    if (mediaController.isShowing())
                        mediaController.hide();
                    else
                        mediaController.show();
            }
        });
    }

    private void prepare() {
        videoView.release();
        srtTextView.setVisibility(View.GONE);
        mediaController.hide();
        loadingProgress.setVisibility(View.GONE);
        questionView.hide();
        auditionView.hide();
        auxiliaryVideoView.hide();
        auxiliaryLoadingProgress.setVisibility(View.GONE);
        auxiliaryView.hide();
        advertCountDown.setVisibility(View.GONE);
        firstStartView.hide();
        progressView.resetMaxValue();
        // 获取直播状态
        if (isGetLiveStatus)
            getLive_Status();
    }

    public static Intent newUrlIntent(Context context, final String playbackUrl, final String title, final String sessionId, boolean isList, boolean isLandscape) {
        Intent intent = new Intent(context, PolyvPPTPbPlayerActivity.class);
        intent.putExtra("playbackUrl", playbackUrl);
        intent.putExtra("title", title);
        intent.putExtra("sessionId", sessionId);
        intent.putExtra("isList", isList);
        intent.putExtra("isLandscape", isLandscape);
        return intent;
    }

    public static Intent newVidIntent(Context context, final String vid, final int bitrate, boolean startNow, final boolean isMustFromLocal, final String sessionId, boolean isList, boolean isLandscape) {
        Intent intent = new Intent(context, PolyvPPTPbPlayerActivity.class);
        intent.putExtra("vid", vid);
        intent.putExtra("bitrate", bitrate);
        intent.putExtra("startNow", startNow);
        intent.putExtra("isMustFromLocal", isMustFromLocal);
        intent.putExtra("sessionId", sessionId);
        intent.putExtra("isList", isList);
        intent.putExtra("isLandscape", isLandscape);
        return intent;
    }

    public static Intent addChatExtra(Intent intent, String userId, String channelId, String chatUserId, String nickName) {
        return addChatExtra(intent, userId, channelId, chatUserId, nickName, false);
    }

    // 添加登录聊天室及一些接口请求所需的参数
    public static Intent addChatExtra(Intent intent, String userId, String channelId, String chatUserId, String nickName, boolean isGetLiveStatus) {
        intent.putExtra("userId", userId);
        intent.putExtra("channelId", channelId);
        intent.putExtra("chatUserId", chatUserId);
        intent.putExtra("nickName", nickName);
        intent.putExtra("isGetLiveStatus", isGetLiveStatus);
        return intent;
    }

    // 从暂无直播跳转到回放时所需添加的一些参数
    public static Intent addLiveExtra(Intent intent, boolean isFromPPTLive, boolean isToOtherLivePlayer) {
        // 是否是从ppt直播跳转到回放的
        intent.putExtra("isFromPPTLive", isFromPPTLive);
        // 当再次直播的类型不是之前的，是否需要跳转到对应直播类型的播放器
        intent.putExtra("isToOtherLivePlayer", isToOtherLivePlayer);
        return intent;
    }

    // 添加回放参数，使用url播放回放时必须添加
    public static Intent addPlaybackParam(Intent intent, PolyvPlaybackParam playbackParam) {
        intent.putExtra("playbackParam", playbackParam);
        return intent;
    }

    /**
     * 使用url播放ppt回放。<br/>
     */
    public void playUrl(final String playbackUrl, final String title, final String sessionId, boolean isList) {
        if (TextUtils.isEmpty(playbackUrl)) return;
        this.isPlaybackUrl = true;
        this.title = title;
        prepare();
        // 于videoView.setPPTVideoURI之前调用，另外需要在主线程调用
        ppt_view.setPPTPlaybackData(channelId, sessionId, isList);
        videoView.setPlackbackParam(playbackParam);
        videoView.setPPTVideoURI(Uri.parse(playbackUrl));
    }

    /**
     * 使用vid播放ppt回放。<br/>
     */
    public void playVid(final String vid, final int bitrate, boolean startNow, final boolean isMustFromLocal, final String sessionId, final boolean isList) {
        if (TextUtils.isEmpty(vid)) return;
        this.isPlaybackUrl = false;
        prepare();
        if (startNow) {
            // 于videoView.setPPTVid之前调用
            ppt_view.setPPTPlaybackData(channelId, sessionId, isList);
            // setPPTVid
            videoView.setPPTVid(vid, bitrate, isMustFromLocal);
        } else {
            firstStartView.setCallback(new PolyvPlayerPreviewView.Callback() {

                @Override
                public void onClickStart() {
                    // 于videoView.setPPTVid之前调用，另外需要在主线程调用
                    ppt_view.setPPTPlaybackData(channelId, sessionId, isList);
                    // setPPTVid
                    videoView.setPPTVid(vid, bitrate, isMustFromLocal);
                }
            });
            firstStartView.show(vid);
        }
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
                    if (tipsFragment == null)
                        tipsFragment = new PolyvTipsFragment();
                    if (!isFinishing())
                        tipsFragment.show(getSupportFragmentManager(), "直播开始了，赶紧去观看吧！", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (isFromPPTLive) {
                                    if (isPPTLive || !isToOtherLivePlayer) {
                                        startActivity(PolyvPPTLivePlayerActivity.newIntent(PolyvPPTPbPlayerActivity.this, userId, channelId, chatUserId, nickName, false, isToOtherLivePlayer));
                                        finish();
                                    } else {
                                        startActivity(PolyvLivePlayerActivity.newIntent(PolyvPPTPbPlayerActivity.this, userId, channelId, chatUserId, nickName, false, isToOtherLivePlayer));
                                        finish();
                                    }
                                } else {
                                    if (!isPPTLive || !isToOtherLivePlayer) {
                                        startActivity(PolyvLivePlayerActivity.newIntent(PolyvPPTPbPlayerActivity.this, userId, channelId, chatUserId, nickName, false, isToOtherLivePlayer));
                                        finish();
                                    } else {
                                        startActivity(PolyvPPTLivePlayerActivity.newIntent(PolyvPPTPbPlayerActivity.this, userId, channelId, chatUserId, nickName, false, isToOtherLivePlayer));
                                        finish();
                                    }
                                }
                            }
                        });
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
        if (progressView != null)
            progressView.hide();
        if (volumeView != null)
            volumeView.hide();
        if (lightView != null)
            lightView.hide();
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
        //弹出去暂停
        if (videoView != null)
            isPlay = videoView.onActivityStop();
    }

    @Override
    public void finish() {
        // 注销广播接收器
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        // 发送广播
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(PolyvPbPlayerActivity.FINISH_ACTIVITY));
        super.finish();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 取消请求
        if (live_status != null)
            live_status.shutdownSchedule();
        // 退出聊天室
        if (chatManager != null)
            chatManager.disconnect();
        if (rl_parent != null)
            rl_parent.removeOnLayoutChangeListener(rl_parent_onLayoutChangeListener);
        if (videoView != null)
            videoView.destroy();
        if (questionView != null)
            questionView.hide();
        if (auditionView != null)
            auditionView.hide();
        if (auxiliaryView != null)
            auxiliaryView.hide();
        if (firstStartView != null)
            firstStartView.hide();
        if (mediaController != null)
            mediaController.disable();
    }
}
