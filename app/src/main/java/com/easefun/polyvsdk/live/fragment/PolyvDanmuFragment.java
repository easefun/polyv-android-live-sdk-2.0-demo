package com.easefun.polyvsdk.live.fragment;

import com.easefun.polyvsdk.danmaku.DanmakuInfo;
import com.easefun.polyvsdk.danmaku.DanmakuManager;
import com.easefun.polyvsdk.live.R;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import master.flame.danmaku.controller.DrawHandler.Callback;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;

/**
 * 弹幕Fragment
 */
public class PolyvDanmuFragment extends Fragment {
    private static boolean status_canauto_resume = true;
    private static boolean status_pause_fromuser = true;
    // danmuLayoutView
    private View view;
    private IDanmakuView iDanmakuView;
    private DanmakuManager danmakuManager;
    private DanmakuInfo danmakuInfo;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.polyv_fragment_danmu, container, false);
        return view;
    }

    private void findIdAndNew() {
        iDanmakuView = (IDanmakuView) view.findViewById(R.id.dv_danmaku);
        danmakuInfo = new DanmakuInfo();
    }

    private void initView() {
        danmakuManager = DanmakuManager.getInstance();
        danmakuManager.initConfig(iDanmakuView);
        iDanmakuView.setCallback(new Callback() {

            @Override
            public void updateTimer(DanmakuTimer arg0) {
            }

            @Override
            public void prepared() {
                if (iDanmakuView != null)
                    iDanmakuView.start();
            }

            @Override
            public void drawingFinished() {
            }

            @Override
            public void danmakuShown(BaseDanmaku arg0) {
            }
        });
        iDanmakuView.prepare(danmakuManager.getDanmakuParser(), danmakuManager.getDanmakuContext());
    }

    //隐藏
    public void hide() {
        if (iDanmakuView != null) {
            iDanmakuView.hide();
        }
    }

    //显示
    public void show() {
        if (iDanmakuView != null) {
            iDanmakuView.show();
        }
    }

    //暂停
    public void pause() {
        pause(true);
    }

    public void pause(boolean fromuser) {
        if (!fromuser)
            status_pause_fromuser = false;
        else
            status_canauto_resume = false;
        if (iDanmakuView != null && iDanmakuView.isPrepared()) {
            iDanmakuView.pause();
        }
    }

    //恢复
    public void resume() {
        resume(true);
    }

    public void resume(boolean fromuser) {
        if (status_pause_fromuser && fromuser || (!status_pause_fromuser && !fromuser)) {
            if (iDanmakuView != null && iDanmakuView.isPrepared() && iDanmakuView.isPaused()) {
                if (!status_pause_fromuser) {
                    status_pause_fromuser = true;
                    if (status_canauto_resume)
                        iDanmakuView.resume();
                } else {
                    status_canauto_resume = true;
                    iDanmakuView.resume();
                }
            }
        }
    }

    //发送
    public void sendDanmaku(String msg) {
        danmakuInfo.setMsg(msg);
        danmakuInfo.setFontColor(Color.WHITE);
        danmakuInfo.setFontSize(18);
        danmakuManager.setSendDanmaku(danmakuInfo);
        danmakuManager.showDanmaku(false, true);
    }

    //释放
    private void release() {
        if (iDanmakuView != null) {
            iDanmakuView.release();
            iDanmakuView = null;
        }
        danmakuManager.release();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findIdAndNew();
        initView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        release();
    }
}
