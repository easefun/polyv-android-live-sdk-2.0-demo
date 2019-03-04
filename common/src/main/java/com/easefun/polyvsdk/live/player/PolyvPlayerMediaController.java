package com.easefun.polyvsdk.live.player;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.adapter.PolyvBitAdapter;
import com.easefun.polyvsdk.live.fragment.PolyvDanmuFragment;
import com.easefun.polyvsdk.live.util.PolyvScreenUtils;
import com.easefun.polyvsdk.live.video.IPolyvLiveVideoView;
import com.easefun.polyvsdk.live.video.PolyvLiveMediaController;
import com.easefun.polyvsdk.live.video.PolyvLiveVideoView;
import com.easefun.polyvsdk.live.vo.PolyvLiveDefinitionVO;
import com.easefun.polyvsdk.live.vo.PolyvLiveBitrateVO;

import java.util.ListIterator;

public class PolyvPlayerMediaController extends PolyvLiveMediaController implements View.OnClickListener {
    private static final String TAG = PolyvPlayerMediaController.class.getSimpleName();    //控制栏显示的时间
    private static final int longTime = 5000;
    private static final int HIDE = 12;
    private static final int UPDATE_PLAY_BUTTON = 13;
    private static final int MESSAGE_HIDE_TOAST = 14;

    private static final int TOAST_SHOW_TIME = 5*1000;


    private Context mContext = null;
    private PolyvLiveVideoView videoView = null;
    private PolyvDanmuFragment danmuFragment;
    //播放器所在的activityl
    private Activity videoActivity;
    //controllerView,播放器的ParentView
    private View view, parentView;
    private ImageView iv_play, iv_land, iv_dmswitch;

    //显示的状态
    private boolean isShowing;

    private PopupWindow popupWindow;
    // 是否使用popupWindow弹出控制栏，非ppt直播/回放用false即可。
    // (注：如果是ppt直播/回放时，true：可移动的播放器层级<弹幕层级<控制栏层级，false：控制栏<可移动的播放器<弹幕)
    private boolean usePopupWindow;

    //清晰度选择view
    private RelativeLayout liveControllerBottom;
    private LinearLayout liveBitLayout;
    private RecyclerView newBitStyleLayout;
    private PolyvBitAdapter polyvBitAdapter;
    private TextView liveBitrateTitle;
    //选择播放码率的控件
    private TextView tv_bit;
    private volatile int currentBitratePos;
    private boolean hasData;
    private PopupWindow bitRatePopupWindow;
    private PolyvLiveBitrateVO polyvLiveBitrateVO;
    private ListIterator<PolyvLiveDefinitionVO> iterator;

    private  Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE:
                    hide();
                    break;
                case UPDATE_PLAY_BUTTON:
                    updatePlayButton();
                    break;
                case MESSAGE_HIDE_TOAST:
                    hideBitPopup();
                    break;
            }
        }
    };

    private void hideBitPopup() {
        if(bitRatePopupWindow != null){
            bitRatePopupWindow.dismiss();
        }
    }


    private void updatePlayButton() {
        if (isShowing && videoView != null) {
            if (videoView.isPlaying()) {
                iv_play.setSelected(false);
            } else {
                iv_play.setSelected(true);
            }
            handler.sendMessageDelayed(handler.obtainMessage(UPDATE_PLAY_BUTTON), 1000);
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
        this.videoActivity = (Activity) context;
    }

    private void findIdAndNew() {
        iv_play = (ImageView) view.findViewById(R.id.iv_play);
        iv_land = (ImageView) view.findViewById(R.id.iv_land);
        iv_dmswitch = (ImageView) view.findViewById(R.id.iv_dmswitch);

        liveControllerBottom = (RelativeLayout) view.findViewById(R.id.live_controller_bottom);
        liveBitLayout = (LinearLayout) view.findViewById(R.id.live_float_bitrate_select_layout);
        liveBitrateTitle = (TextView) view.findViewById(R.id.live_bitrate_title);
        newBitStyleLayout = (RecyclerView) view.findViewById(R.id.newBitStyleLayout);
        tv_bit = (TextView) view.findViewById(R.id.tv_bit);
    }

    private void initView() {
        iv_play.setOnClickListener(this);
        iv_land.setOnClickListener(this);
        iv_dmswitch.setOnClickListener(this);

        iniBitListenser();
    }

    private void iniBitListenser() {
        tv_bit.setOnClickListener(this);
        liveBitLayout.setOnClickListener(this);
    }

    //信息获取完成以后刷新列表
    public void initBitList(PolyvLiveBitrateVO polyvLiveBitrateVO){
        if(polyvLiveBitrateVO == null){
            return;
        }
        try {
            this.polyvLiveBitrateVO = polyvLiveBitrateVO;
            if(polyvBitAdapter == null){
                polyvBitAdapter = new PolyvBitAdapter(polyvLiveBitrateVO,getContext());
                newBitStyleLayout.setLayoutManager(new LinearLayoutManager(getContext()));
                newBitStyleLayout.setAdapter(polyvBitAdapter);
                polyvBitAdapter.setOnClickListener(this);
                tv_bit.setText(polyvLiveBitrateVO.getDefaultDefinition());

            }else {
                polyvBitAdapter.updateBitrates(polyvLiveBitrateVO);
            }

            locateDefaultDefPos();

            polyvBitAdapter.notifyDataSetChanged();
            hasData = true;

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void locateDefaultDefPos() {

        if(polyvLiveBitrateVO == null || polyvLiveBitrateVO.getDefinitions() == null){
            return;
        }

        //找到默认分辨率得位置
        if(iterator == null){
            iterator = polyvLiveBitrateVO.getDefinitions().listIterator();
            while (iterator.hasNext()){
                PolyvLiveDefinitionVO definitionVO = iterator.next();
                if(definitionVO.definition != null && definitionVO.definition.equals(polyvLiveBitrateVO.getDefaultDefinition())){
                    definitionVO.hasSelected = true;
                    currentBitratePos = iterator.nextIndex()-1;
                    break;
                }
            }
        }


        PolyvLiveDefinitionVO definitionVO = polyvLiveBitrateVO.getDefinitions().get(currentBitratePos);
        definitionVO.hasSelected = true;
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
        this.view = LayoutInflater.from(getContext()).inflate(R.layout.polyv_controller_media_port, usePopupWindow ? null : this);
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

    /**
     * 切换到横屏
     */
    public void changeToLandscape() {
        PolyvScreenUtils.setLandscape(videoActivity);
        //初始为横屏时，状态栏需要隐藏
        PolyvScreenUtils.hideStatusBar(videoActivity);
        //初始为横屏时，控制栏的宽高需要设置
        initLandScapeWH();

        resetAdapter();
    }

    private void resetAdapter() {
        liveBitLayout.setVisibility(GONE);
        if(!hasData){
            return;
        }
        if (polyvBitAdapter == null && polyvLiveBitrateVO != null) {
            polyvBitAdapter = new PolyvBitAdapter(polyvLiveBitrateVO, getContext());
            newBitStyleLayout.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        if(newBitStyleLayout != null){
            newBitStyleLayout.setAdapter(polyvBitAdapter);
        }
    }

    private void initLandScapeWH() {
        ViewGroup.LayoutParams vlp = parentView.getLayoutParams();
        vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        vlp.height = ViewGroup.LayoutParams.MATCH_PARENT;

        ViewGroup.LayoutParams layoutParams = newBitStyleLayout.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

        LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) liveBitrateTitle.getLayoutParams();
        titleParams.topMargin = PolyvScreenUtils.dip2px(getContext(),30);
        titleParams.bottomMargin = PolyvScreenUtils.dip2px(getContext(),15);

        iv_land.setSelected(true);
    }

    /**
     * 切换到竖屏
     */
    public void changeToPortrait() {
        PolyvScreenUtils.setPortrait(videoActivity);
        initPortraitWH();
        resetAdapter();
    }

    private void initPortraitWH() {
        ViewGroup.LayoutParams vlp = parentView.getLayoutParams();
        ViewGroup.LayoutParams layoutParams = liveBitLayout.getLayoutParams();
        vlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        vlp.height = PolyvScreenUtils.getHeight16_9();

        LinearLayout.LayoutParams titleParams = (LinearLayout.LayoutParams) liveBitrateTitle.getLayoutParams();
        titleParams.bottomMargin = PolyvScreenUtils.dip2px(getContext(),5);
        titleParams.topMargin = PolyvScreenUtils.dip2px(getContext(),15);
        iv_land.setSelected(false);
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
    public void onLongBuffering(String tip) {
        showBitrateChangeView();
        show();
    }

    @Override
    public void hide() {
        if (isShowing) {
            handler.removeMessages(HIDE);
            handler.removeMessages(UPDATE_PLAY_BUTTON);
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

    @Override
    public void show(int timeout) {
        if(liveControllerBottom != null){
            liveControllerBottom.setVisibility(VISIBLE);
        }
        if (!isShowing) {
            handler.removeMessages(UPDATE_PLAY_BUTTON);
            handler.sendEmptyMessage(UPDATE_PLAY_BUTTON);
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
        handler.removeMessages(HIDE);
        handler.sendMessageDelayed(handler.obtainMessage(HIDE), timeout);
    }

    @Override
    public void show() {
        show(longTime);
    }

    private void creatBitrateChangeWindow(){

        View child = View.inflate(getContext(), R.layout.polyv_live_bitrate_popu_layout, null);
        bitRatePopupWindow = new PopupWindow(child, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup
                .LayoutParams.WRAP_CONTENT, true);
        bitRatePopupWindow.setFocusable(true);//这里必须设置为true才能点击区域外或者消失
        bitRatePopupWindow.setTouchable(true);//这个控制PopupWindow内部控件的点击事件
        bitRatePopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bitRatePopupWindow.setOutsideTouchable(true);
        bitRatePopupWindow.update();

        bitRatePopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                bitRatePopupWindow = null;
                handler.removeMessages(MESSAGE_HIDE_TOAST);
            }
        });
    }

    private void showBitrateChangeView() {
        if( polyvLiveBitrateVO == null ||polyvLiveBitrateVO.getDefinitions() == null ||
                currentBitratePos == polyvLiveBitrateVO.getDefinitions().size()-1){
            return;
        }
        if (bitRatePopupWindow == null) {
            creatBitrateChangeWindow();
        }
        //获取需要在其上方显示的控件的位置信息
        int[] location = new int[2];
        iv_play.getLocationOnScreen(location);
        //在控件上方显示
        View child = bitRatePopupWindow.getContentView();
        TextView definition = (TextView) child.findViewById(R.id.live_bitrate_popup_definition);

        PolyvLiveDefinitionVO definitionVO = polyvLiveBitrateVO.getDefinitions().get(Math.max(0,currentBitratePos+1));
        definition.setText(definitionVO.definition);

        definition.setOnClickListener(this);

        child.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupHeight = child.getMeasuredHeight();
        int popupWidth = child.getMeasuredWidth();
        bitRatePopupWindow.showAtLocation(iv_play, Gravity.NO_GRAVITY, (location[0]+10 ), location[1] - popupHeight-10 );
        handler.sendEmptyMessageDelayed(MESSAGE_HIDE_TOAST,TOAST_SHOW_TIME);


    }
    //根据视频的播放状态去暂停或播放
    private void playOrPause() {
        handler.removeMessages(UPDATE_PLAY_BUTTON);
        if (videoView != null) {
            if (videoView.isPlaying()) {
                videoView.pause();
                iv_play.setSelected(true);
            } else {
                videoView.onStart();
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
        int i = view.getId();
        if (i == R.id.iv_play) {
            playOrPause();

        } else if (i == R.id.iv_dmswitch) {
            resetDmSwitchView();

        } else if (i == R.id.iv_land) {
            resetOrientationView();

        } else if (i == R.id.tv_bit) {
            if (hasData) {
                showBitSelect();
            }

        } else if (i == R.id.live_bit_name) {
            if (setLiveBit(view)) {
                updateBitSelectedView(((TextView) view).getText().toString());
            }
            ;

            liveBitLayout.setVisibility(GONE);

        } else if (i == R.id.live_float_bitrate_select_layout) {
            updateBottomView();

        } else if (i == R.id.live_bitrate_popup_definition) {
            changeBitrateByPopup();
            bitRatePopupWindow.dismiss();

        }
    }

    private void updateBottomView() {
        liveBitLayout.setVisibility(GONE);
        if(liveControllerBottom != null){
            liveControllerBottom.setVisibility(VISIBLE);
        }
    }

    /**
     * 根据缓冲提示切换码率，切换到前一个低码率得清晰度
     */
    private void changeBitrateByPopup() {
        if(polyvLiveBitrateVO == null || polyvLiveBitrateVO.getDefinitions() == null||
                currentBitratePos >= (polyvLiveBitrateVO.getDefinitions().size()-1)){//已经是流畅清晰度
            return;
        }
        //置位上一个分辨率的选中状态
        polyvLiveBitrateVO.getDefinitions().get(currentBitratePos).hasSelected = false;

        //更新到上一个清晰度
        currentBitratePos = currentBitratePos +1;
        PolyvLiveDefinitionVO definitionVO = polyvLiveBitrateVO.getDefinitions().get(currentBitratePos);
        definitionVO.hasSelected = true;

        polyvBitAdapter.notifyDataSetChanged();

        //修改码率
        videoView.changeBitRate(currentBitratePos);
        //修改底部显示文字
        tv_bit.setText(definitionVO.definition);

    }

    private void updateBitSelectedView(String content) {
        tv_bit.setText(content);
        polyvBitAdapter.notifyDataSetChanged();
    }

    private void showBitSelect() {
        liveBitLayout.setVisibility(VISIBLE);
        liveControllerBottom.setVisibility(GONE);
    }


    private boolean setLiveBit( View view) {

        try {
            int pos =  (int) view.getTag();
            if(currentBitratePos == pos){
                return false;
            }
            currentBitratePos = pos;
            polyvBitAdapter.notifyDataSetChanged();

            if (videoView != null) {
                videoView.changeBitRate(pos);
            }
        } catch (Exception e) {
            Log.e(TAG, "setLiveBit: ", e);
        }

        return true;
    }

    public interface PopupWindowDismissListener {
        void dismiss();
    }

    private PopupWindowDismissListener popupWindowDismissListener;

    public void setPopupWindowDismissListener(PopupWindowDismissListener listener) {
        this.popupWindowDismissListener = listener;
    }
}
