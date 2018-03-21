package com.easefun.polyvsdk.live.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.adapter.PolyvTabVPFragmentAdapter;
import com.easefun.polyvsdk.live.chat.PolyvChatManager;
import com.easefun.polyvsdk.live.chat.api.PolyvChatQuiz;
import com.easefun.polyvsdk.live.chat.api.listener.PolyvChatQuizListener;
import com.easefun.polyvsdk.live.video.PolyvLiveVideoView;
import com.easefun.polyvsdk.video.PolyvVideoView;

import java.util.ArrayList;
import java.util.List;

public class PolyvTabViewPagerFragment extends Fragment {
    private View view;
    private ViewPager vp_tab;
    private PolyvTabFragment tabFragment;
    private PolyvChatFragment chatFragment;
    private PolyvDanmuFragment danmuFragment;
    private PolyvOnlineListFragment onlineListFragment;
    private PolyvPlaybackListFragment playbackListFragment;
    private PolyvQuestionFragment questionFragment;
    private PolyvTabVPFragmentAdapter tabVPFragmentAdapter;
    private Object videoView;
    private int currentIndex;

    private List<Fragment> fragmentLists;
    private PolyvChatQuiz chatQuiz;

    public void initConfig(PolyvChatFragment chatFragment) {
        this.chatFragment = chatFragment;
    }

    public void setDanmuFragment(PolyvDanmuFragment danmuFragment) {
        this.danmuFragment = danmuFragment;
    }

    public PolyvChatFragment getChatFragment() {
        return chatFragment;
    }

    public PolyvDanmuFragment getDanmuFragment() {
        return danmuFragment;
    }

    public PolyvQuestionFragment getQuestionFragment() {
        return questionFragment;
    }

    /**
     * 设置所使用的播放器
     *
     * @param videoView {@link PolyvLiveVideoView} or {@link PolyvVideoView}
     */
    public void setVideoView(Object videoView) {
        this.videoView = videoView;
    }

    public PolyvOnlineListFragment getOnlineListFragment() {
        return onlineListFragment;
    }

    public PolyvPlaybackListFragment getPlaybackListFragment() {
        return playbackListFragment;
    }

    public PolyvChatManager getChatManager() {
        return chatFragment.getChatManager();
    }

    public void setVideoVolume(int volume) {
        if (videoView instanceof PolyvLiveVideoView)
            ((PolyvLiveVideoView) videoView).setVolume(volume);
        else if (videoView instanceof PolyvVideoView)
            ((PolyvVideoView) videoView).setVolume(volume);
    }

    public int getVideoVolume() {
        if (videoView instanceof PolyvLiveVideoView)
            return ((PolyvLiveVideoView) videoView).getVolume();
        else if (videoView instanceof PolyvVideoView)
            return ((PolyvVideoView) videoView).getVolume();
        return 0;
    }

    public boolean isLive() {
        return videoView instanceof PolyvLiveVideoView;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.polyv_fragment_tab_viewpager, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        findId();
        initView();
    }

    // 获取后台设置的提问功能是否开启
    private void getChatQuiz(String channelId) {
        chatQuiz.hasQuiz(channelId, Integer.MAX_VALUE, new PolyvChatQuizListener() {
            @Override
            public void success(boolean isOpen) {
                if (isOpen) {
                    fragmentLists.add(questionFragment);
                    tabVPFragmentAdapter.notifyDataSetChanged();
                    vp_tab.setOffscreenPageLimit(fragmentLists.size() - 1);
                    tabFragment.addQuestionTab(currentIndex);
                }
            }

            @Override
            public void fail(String failTips, int code) {
            }
        });
    }

    private void findId() {
        vp_tab = (ViewPager) view.findViewById(R.id.vp_tab);
        tabFragment = (PolyvTabFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fl_tab);
    }

    private void initView() {
        chatQuiz = new PolyvChatQuiz();
        onlineListFragment = new PolyvOnlineListFragment();
        playbackListFragment = new PolyvPlaybackListFragment();
        questionFragment = new PolyvQuestionFragment();
        fragmentLists = new ArrayList<>();
        fragmentLists.add(chatFragment);
        if (isLive())
            fragmentLists.add(onlineListFragment);
        else
            fragmentLists.add(playbackListFragment);
        getChatQuiz(getActivity().getIntent().getStringExtra("channelId"));
        tabVPFragmentAdapter = new PolyvTabVPFragmentAdapter(getActivity().getSupportFragmentManager(), fragmentLists);
        vp_tab.setAdapter(tabVPFragmentAdapter);
        vp_tab.setOffscreenPageLimit(fragmentLists.size() - 1);
        vp_tab.setCurrentItem(0);
        currentIndex = 0;
        vp_tab.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            // arg0:移动后的位置
            public void onPageSelected(int arg0) {
                currentIndex = arg0;
                tabFragment.resetViewStatus(arg0);
            }

            @Override
            // arg0:当前位置(右移)/下一位置(左移)
            // arg1:页面偏移百分比
            // arg2:页面偏移像素
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            // arg0:移动状态
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    public void setCurrentItem(int index) {
        vp_tab.setCurrentItem(index);
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        chatQuiz.shutdown();
    }
}
