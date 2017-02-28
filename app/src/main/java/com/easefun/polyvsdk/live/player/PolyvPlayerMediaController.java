package com.easefun.polyvsdk.live.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.fragment.PolyvDanmuFragment;
import com.easefun.polyvsdk.live.util.PolyvScreenUtils;
import com.easefun.polyvsdk.live.video.IPolyvLiveVideoView;
import com.easefun.polyvsdk.live.video.PolyvLiveMediaController;
import com.easefun.polyvsdk.live.video.PolyvLiveVideoView;

public class PolyvPlayerMediaController extends PolyvLiveMediaController implements View.OnClickListener {
    private static final String TAG = PolyvPlayerMediaController.class.getSimpleName();
    private Context mContext = null;
    private PolyvLiveVideoView videoView = null;
    private PolyvDanmuFragment danmuFragment;
    //播放器所在的activity
    private Activity videoActivity;
    //controllerView,播放器的ParentView
    private View view, parentView;
    private ImageView iv_play, iv_land, iv_dmswitch;
    //显示的状态
    private boolean isShowing;
    //控制栏显示的时间
    private static final int longTime = 5000;
    private static final int HIDE = 12;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE:
                    hide();
                    break;
            }
        }
    };

    public PolyvPlayerMediaController(Context context) {
        this(context, null);
    }

    public PolyvPlayerMediaController(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PolyvPlayerMediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
        this.videoActivity = (Activity) context;
        this.view = LayoutInflater.from(getContext()).inflate(R.layout.polyv_controller_media_port, this);
        findIdAndNew();
        initView();
    }

    private void findIdAndNew() {
        iv_play = (ImageView) view.findViewById(R.id.iv_play);
        iv_land = (ImageView) view.findViewById(R.id.iv_land);
        iv_dmswitch = (ImageView) view.findViewById(R.id.iv_dmswitch);
    }

    private void initView() {
        iv_play.setOnClickListener(this);
        iv_land.setOnClickListener(this);
        iv_dmswitch.setOnClickListener(this);
    }

    /**
     * 初始化控制栏的配置
     *
     * @param parentView 播放器的父控件
     */
    public void initConfig(ViewGroup parentView) {
        this.parentView = parentView;
    }

    public void setDanmuFragment(PolyvDanmuFragment danmuFragment) {
        this.danmuFragment = danmuFragment;
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
        // 获取横屏下的屏幕宽高
        int[] wh = PolyvScreenUtils.getNormalWH(videoActivity);
        //这里的LayoutParams为parentView的父类的LayoutParams
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(wh[0], wh[1]);
        parentView.setLayoutParams(lp);
    }

    /**
     * 切换到竖屏
     */
    public void changeToPortrait() {
        PolyvScreenUtils.setPortrait(videoActivity);
    }

    //根据屏幕状态改变控制栏布局
    private void resetControllerLayout() {
        hide();
        PolyvScreenUtils.reSetStatusBar(videoActivity);
        if (PolyvScreenUtils.isLandscape(mContext)) {
            initLandScapeWH();
            iv_land.setSelected(true);
        } else {
            //这里宽高设置是polyv_fragment_player.xml布局文件中parentView的初始值
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.top_center_player_height));
            parentView.setLayoutParams(lp);
            iv_land.setSelected(false);
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetControllerLayout();
    }

    @Override
    public void setMediaPlayer(IPolyvLiveVideoView player) {
        videoView = (PolyvLiveVideoView) player;
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
            isShowing = !isShowing;
            setVisibility(View.GONE);
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

    @Override
    public void show(int timeout) {
        if (!isShowing) {
            isShowing = !isShowing;
            setVisibility(View.VISIBLE);
        }
        handler.removeMessages(HIDE);
        handler.sendMessageDelayed(handler.obtainMessage(HIDE), timeout);
    }

    @Override
    public void show() {
        show(longTime);
    }

    //根据视频的播放状态去暂停或播放
    private void playOrPause() {
        if (videoView != null) {
            if (videoView.isPlaying()) {
                videoView.pause();
                iv_play.setSelected(true);
            } else {
                videoView.start();
                iv_play.setSelected(false);
            }
        }
    }

    //重置显示/隐藏弹幕的控件
    private void resetDmSwitchView() {
        if (iv_dmswitch.isSelected()) {
            iv_dmswitch.setSelected(false);
            danmuFragment.show();
        } else {
            iv_dmswitch.setSelected(true);
            danmuFragment.hide();
        }
    }

    //重置切换横竖屏的view
    private void resetOrientationView() {
        if (iv_land.isSelected()) {
            changeToPortrait();
        } else {
            changeToLandscape();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_play:
                playOrPause();
                break;
            case R.id.iv_dmswitch:
                resetDmSwitchView();
                break;
            case R.id.iv_land:
                resetOrientationView();
                break;
        }
    }
}
