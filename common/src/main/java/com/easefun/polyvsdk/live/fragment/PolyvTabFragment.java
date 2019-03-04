package com.easefun.polyvsdk.live.fragment;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easefun.polyvsdk.live.R;


public class PolyvTabFragment extends Fragment implements View.OnClickListener {
    //fragmentView
    private View view;
    //viewpagerFragment
    private PolyvTabViewPagerFragment viewPagerFragment;
    private LinearLayout ll_tab;
    private TextView tv_chat, tv_online, tv_playback, tv_question;
    // tab的导航线
    private View v_line;
    private LinearLayout.LayoutParams lp;

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
        ll_tab = (LinearLayout) view.findViewById(R.id.ll_tab);
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
        addTab(tv_question, currentIndex);
    }

    public void addTab(String text, int currentIndex) {
        addTab(generateTabView(text), currentIndex);
    }

    private void addTab(TextView textView, int currentIndex) {
        ll_tab.addView(textView);
        final int index = ll_tab.getChildCount() - 1;
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPagerFragment.setCurrentItem(index);
            }
        });
        resetLineLength();
        resetViewStatus(currentIndex);
    }

    private void resetLineLength() {
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v_line.getLayoutParams();
        lp.width = ll_tab.getWidth() / ll_tab.getChildCount();
        v_line.setLayoutParams(lp);
    }

    private TextView generateTabView(String text) {
        TextView textView = new TextView(getActivity());
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.polyv_commom_click_color_gray));
        textView.setText(text);
        textView.setTextSize((getResources().getDimension(R.dimen.tv_textsize) / getResources().getDisplayMetrics().density) + 0.5f);
        textView.setTextColor(getResources().getColor(R.color.bottom_et_color_gray));
        textView.setClickable(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -1);
        lp.weight = 1;
        textView.setLayoutParams(lp);
        return textView;
    }

    private void initLineSetting() {
        lp = (LinearLayout.LayoutParams) v_line.getLayoutParams();
        v_line.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                resetLineLength();
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
        for (int i = 0; i < ll_tab.getChildCount(); i++) {
            TextView view = (TextView) ll_tab.getChildAt(i);
            view.setSelected(false);
            view.setTextColor(getResources().getColor(R.color.bottom_et_color_gray));
        }
        TextView textView = (TextView) ll_tab.getChildAt(arg0);
        textView.setSelected(true);
        textView.setTextColor(getResources().getColor(R.color.center_view_color_blue));
        lp.leftMargin = textView.getLeft();
        v_line.setLayoutParams(lp);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_chat) {
            viewPagerFragment.setCurrentItem(0);

        } else if (i == R.id.tv_online || i == R.id.tv_playback) {
            viewPagerFragment.setCurrentItem(1);

        } else if (i == R.id.tv_question) {
            viewPagerFragment.setCurrentItem(2);

        }
    }
}
