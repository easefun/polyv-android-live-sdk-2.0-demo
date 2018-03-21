package com.easefun.polyvsdk.live.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.util.PolyvScreenUtils;

public class PolyvTabFragment extends Fragment implements View.OnClickListener {
    //fragmentView
    private View view;
    //viewpagerFragment
    private PolyvTabViewPagerFragment viewPagerFragment;
    private TextView tv_chat, tv_online, tv_playback, tv_question;
    // tab的导航线
    private View v_line;
    private int screenWidth;
    private int length;
    private int eLength;
    private LinearLayout.LayoutParams lp;
    private int count = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.polyv_fragment_tab, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findId();
        initView();
    }

    private void findId() {
        tv_chat = (TextView) view.findViewById(R.id.tv_chat);
        tv_online = (TextView) view.findViewById(R.id.tv_online);
        tv_playback = (TextView) view.findViewById(R.id.tv_playback);
        tv_question = (TextView) view.findViewById(R.id.tv_question);
        v_line = view.findViewById(R.id.v_line);
        viewPagerFragment = (PolyvTabViewPagerFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fl_tag_viewpager);
        if (viewPagerFragment.isLive()) {
            ((ViewGroup) tv_playback.getParent()).removeView(tv_playback);
        } else {
            ((ViewGroup) tv_online.getParent()).removeView(tv_online);
        }
        ((ViewGroup) tv_question.getParent()).removeView(tv_question);
    }

    private void initView() {
        initLineSetting();
        tv_chat.setSelected(true);
        tv_chat.setOnClickListener(this);
        tv_online.setOnClickListener(this);
        tv_playback.setOnClickListener(this);
        tv_question.setOnClickListener(this);
    }

    public void addQuestionTab(int currentIndex) {
        ((ViewGroup) tv_chat.getParent()).addView(tv_question);
        count++;
        int sLength = screenWidth / count;
        eLength = (sLength - length) / 2;
        resetViewStatus(currentIndex);
    }

    private void initLineSetting() {
        lp = (LinearLayout.LayoutParams) v_line.getLayoutParams();
        v_line.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                final DisplayMetrics dm = new DisplayMetrics();
                getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
                if (PolyvScreenUtils.isLandscape(getContext()))
                    screenWidth = dm.heightPixels;
                else
                    screenWidth = dm.widthPixels;
                length = v_line.getWidth();
                int sLength = screenWidth / count;
                eLength = (sLength - length) / 2;
                if (viewPagerFragment.getCurrentIndex() == 0)
                    resetViewStatus(0);
                if (Build.VERSION.SDK_INT >= 16)
                    getView().getViewTreeObserver().removeOnGlobalLayoutListener(this);
                else
                    getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
    }

    public void resetViewStatus(int arg0) {
        tv_chat.setSelected(false);
        tv_online.setSelected(false);
        tv_playback.setSelected(false);
        tv_question.setSelected(false);
        tv_chat.setTextColor(getResources().getColor(R.color.bottom_et_color_gray));
        tv_online.setTextColor(getResources().getColor(R.color.bottom_et_color_gray));
        tv_playback.setTextColor(getResources().getColor(R.color.bottom_et_color_gray));
        tv_question.setTextColor(getResources().getColor(R.color.bottom_et_color_gray));
        switch (arg0) {
            case 0:
                tv_chat.setSelected(true);
                tv_chat.setTextColor(getResources().getColor(R.color.center_view_color_blue));
                lp.leftMargin = arg0 * length + 1 * eLength;
                break;
            case 1:
                TextView textView;
                if (viewPagerFragment.isLive())
                    textView = tv_online;
                else
                    textView = tv_playback;
                textView.setSelected(true);
                textView.setTextColor(getResources().getColor(R.color.center_view_color_blue));
                lp.leftMargin = arg0 * length + 3 * eLength;
                break;
            case 2:
                tv_question.setSelected(true);
                tv_question.setTextColor(getResources().getColor(R.color.center_view_color_blue));
                lp.leftMargin = arg0 * length + 5 * eLength;
                break;
        }
        v_line.setLayoutParams(lp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_chat:
                viewPagerFragment.setCurrentItem(0);
                break;
            case R.id.tv_online:
            case R.id.tv_playback:
                viewPagerFragment.setCurrentItem(1);
                break;
            case R.id.tv_question:
                viewPagerFragment.setCurrentItem(2);
                break;
        }
    }
}
