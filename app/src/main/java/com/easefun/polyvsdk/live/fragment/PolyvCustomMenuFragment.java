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

public class PolyvCustomMenuFragment extends Fragment {
    private View view;
    private LinearLayout ll_parent;
    private PolyvSafeWebView wv_desc;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return view == null ? view = inflater.inflate(R.layout.polyv_fragment_custommenu, null) : view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
                ll_parent = (LinearLayout) view.findViewById(R.id.ll_parent);
                wv_desc = new PolyvSafeWebView(getContext());
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(-1, -1);
                wv_desc.setLayoutParams(llp);
                ll_parent.addView(wv_desc);
                PolyvWebViewHelper.initWebView(getContext(), wv_desc);
                wv_desc.loadData(getArguments().getString("text"), "text/html; charset=UTF-8", null);
            } else {
                ll_parent.addView(wv_desc);
            }
        } else {
            if (ll_parent != null) {
                ll_parent.removeView(wv_desc);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ll_parent != null) {
            ll_parent.removeView(wv_desc);
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
