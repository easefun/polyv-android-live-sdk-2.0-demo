package com.easefun.polyvsdk.live.fragment;


import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.chat.api.entity.PolyvClassDetailEntity;
import com.easefun.polyvsdk.live.util.PolyvWebViewHelper;
import com.easefun.polyvsdk.live.view.PolyvSafeWebView;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class PolyvLiveIntroduceFragment extends Fragment {
    private boolean isInitialized;
    private View view;
    private PolyvSafeWebView wv_desc;
    private RelativeLayout rl_parent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view == null ? view = inflater.inflate(R.layout.polyv_fragment_liveintroduce, null) : view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isInitialized) {
            isInitialized = true;
            initView();
        }
    }

    private void initView() {
        final PolyvClassDetailEntity classDetailEntity = getArguments().getParcelable("classDetail");

        TextView tv_title = (TextView) view.findViewById(R.id.tv_title);
        tv_title.setText(classDetailEntity.name);

        final ImageView iv_livecover = (ImageView) view.findViewById(R.id.iv_livecover);
        Glide.with(this).load(classDetailEntity.coverImage).bitmapTransform(new RoundedCornersTransformation(getContext(), 6, 0)).into(iv_livecover);

        TextView tv_publisher = (TextView) view.findViewById(R.id.tv_publisher);
        tv_publisher.setText(TextUtils.isEmpty(classDetailEntity.publisher) ? "主持人" : classDetailEntity.publisher);

        TextView tv_viewer = (TextView) view.findViewById(R.id.tv_viewer);
        tv_viewer.setText(classDetailEntity.pageView + "");

        TextView tv_likes = (TextView) view.findViewById(R.id.tv_likes);
        tv_likes.setText(classDetailEntity.likes + "");

        TextView tv_starttime = (TextView) view.findViewById(R.id.tv_starttime);
        tv_starttime.setText("直播时间：" + (TextUtils.isEmpty(classDetailEntity.startTime) ? "无" : classDetailEntity.startTime));

        TextView tv_status = (TextView) view.findViewById(R.id.tv_status);
        tv_status.setText("Y".equals(classDetailEntity.status) ? "正在直播" : "暂无直播");
    }

    public PolyvSafeWebView getWebView() {
        return wv_desc;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wv_desc != null) {
            wv_desc.onResume();
            wv_desc.resumeTimers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (wv_desc != null) {
            wv_desc.onPause();
            wv_desc.pauseTimers();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (wv_desc == null) {
                rl_parent = (RelativeLayout) view.findViewById(R.id.rl_parent);
                wv_desc = new PolyvSafeWebView(getContext());
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(-1, -1);
                rlp.addRule(RelativeLayout.BELOW, R.id.v_s2);
                wv_desc.setLayoutParams(rlp);
                rl_parent.addView(wv_desc);
                PolyvWebViewHelper.initWebView(getContext(), wv_desc);
                wv_desc.loadData(((PolyvClassDetailEntity) getArguments().getParcelable("classDetail")).desc, "text/html; charset=UTF-8", null);
            } else {
                rl_parent.addView(wv_desc);
            }
        } else {
            if (rl_parent != null) {
                rl_parent.removeView(wv_desc);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (rl_parent != null) {
            rl_parent.removeView(wv_desc);
        }
        if (wv_desc != null) {
            wv_desc.stopLoading();
            wv_desc.clearMatches();
            wv_desc.clearHistory();
            wv_desc.clearSslPreferences();
            wv_desc.clearCache(true);
            wv_desc.loadUrl("about:blank");
            wv_desc.removeAllViews();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                wv_desc.removeJavascriptInterface("AndroidNative");
            }
            wv_desc.destroy();
        }
        wv_desc = null;
    }
}
