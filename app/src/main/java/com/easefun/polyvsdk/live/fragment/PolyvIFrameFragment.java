package com.easefun.polyvsdk.live.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.util.PolyvWebViewHelper;
import com.easefun.polyvsdk.live.view.PolyvSafeWebView;

public class PolyvIFrameFragment extends Fragment {
    private View view;
    private LinearLayout ll_parent;
    private PolyvSafeWebView wv_iframe;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view == null ? view = inflater.inflate(R.layout.polyv_fragment_iframe, null) : view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public PolyvSafeWebView getWebView() {
        return wv_iframe;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (wv_iframe != null) {
            wv_iframe.onResume();
            wv_iframe.resumeTimers();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (wv_iframe != null) {
            wv_iframe.onPause();
            wv_iframe.pauseTimers();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (wv_iframe == null) {
                ll_parent = (LinearLayout) view.findViewById(R.id.ll_parent);
                wv_iframe = new PolyvSafeWebView(getContext());
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, -1);
                wv_iframe.setLayoutParams(llp);
                ll_parent.addView(wv_iframe);
                PolyvWebViewHelper.initWebView(getContext(), wv_iframe);
                wv_iframe.loadUrl(getArguments().getString("url"));
            } else {
                ll_parent.addView(wv_iframe);
            }
        } else {
            if (ll_parent != null) {
                ll_parent.removeView(wv_iframe);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ll_parent != null) {
            ll_parent.removeView(wv_iframe);
        }
        if (wv_iframe != null) {
            wv_iframe.stopLoading();
            wv_iframe.clearMatches();
            wv_iframe.clearHistory();
            wv_iframe.clearSslPreferences();
            wv_iframe.clearCache(true);
            wv_iframe.loadUrl("about:blank");
            wv_iframe.removeAllViews();
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                wv_iframe.removeJavascriptInterface("AndroidNative");
            }
            wv_iframe.destroy();
        }
        wv_iframe = null;
    }
}
