package com.easefun.polyvsdk.live.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.util.PolyvScreenUtils;
import com.easefun.polyvsdk.live.adapter.PolyvTabVPFragmentAdapter;
import com.easefun.polyvsdk.live.chat.PolyvChatManager;
import com.easefun.polyvsdk.live.chat.api.PolyvClassDetail;
import com.easefun.polyvsdk.live.chat.api.entity.PolyvClassDetailEntity;
import com.easefun.polyvsdk.live.chat.api.listener.PolyvClassDetailListener;
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
    private PolyvClassDetail classDetail;

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

    //获取菜单列表
    private void getClassDetail(final String channelId) {
        classDetail.getClassDetail(channelId, Integer.MAX_VALUE, new PolyvClassDetailListener() {
            @Override
            public void success(PolyvClassDetailEntity entity) {
                for (PolyvClassDetailEntity.ChannelMenu channelMenu : entity.channelMenus) {
                    if (PolyvClassDetailEntity.ChannelMenu.MENUTYPE_QUIZ.equals(channelMenu.menuType)) {
                        addFragment(questionFragment);
                        tabFragment.addQuestionTab(currentIndex);
                        break;
                    }
                }
                for (PolyvClassDetailEntity.ChannelMenu channelMenu : entity.channelMenus) {
                    Fragment fragment = null;
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("classDetail", entity);
                    if (PolyvClassDetailEntity.ChannelMenu.MENUTYPE_DESC.equals(channelMenu.menuType)) {
                        fragment = new PolyvLiveIntroduceFragment();
                    } else if (PolyvClassDetailEntity.ChannelMenu.MENUTYPE_IFRAME.equals(channelMenu.menuType)) {
                        fragment = new PolyvIFrameFragment();
                        bundle.putString("url", channelMenu.content);
                    } else if (PolyvClassDetailEntity.ChannelMenu.MENUTYPE_TEXT.equals(channelMenu.menuType)) {
                        fragment = new PolyvCustomMenuFragment();
                        bundle.putString("text", channelMenu.content);
                    }
                    if (fragment != null) {
                        fragment.setArguments(bundle);
                        addFragment(fragment);
                        tabFragment.addTab(channelMenu.name, currentIndex);
                    }
                }
            }

            @Override
            public void fail(String failTips, int code) {
            }
        });
    }

    private void addFragment(Fragment fragment) {
        fragmentLists.add(fragment);
        tabVPFragmentAdapter.notifyDataSetChanged();
        vp_tab.setOffscreenPageLimit(fragmentLists.size() - 1);
    }

    private void findId() {
        vp_tab = (ViewPager) view.findViewById(R.id.vp_tab);
        tabFragment = (PolyvTabFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fl_tab);
    }

    private void initView() {
        classDetail = new PolyvClassDetail();
        onlineListFragment = new PolyvOnlineListFragment();
        playbackListFragment = new PolyvPlaybackListFragment();
        questionFragment = new PolyvQuestionFragment();
        fragmentLists = new ArrayList<>();
        fragmentLists.add(chatFragment);
        if (isLive())
            fragmentLists.add(onlineListFragment);
        else
            fragmentLists.add(playbackListFragment);
        getClassDetail(getActivity().getIntent().getStringExtra("channelId"));
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

    public boolean onBackPress() {
        if (PolyvScreenUtils.isPortrait(getActivity())) {
            Fragment fragment = tabVPFragmentAdapter.getItem(currentIndex);
            if (fragment instanceof PolyvIFrameFragment) {
                PolyvIFrameFragment iFrameFragment = (PolyvIFrameFragment) fragment;
                if (iFrameFragment.getWebView() != null && iFrameFragment.getWebView().canGoBack()) {
                    iFrameFragment.getWebView().goBack();
                    return true;
                }
            } else if (fragment instanceof PolyvLiveIntroduceFragment) {
                PolyvLiveIntroduceFragment liveIntroduceFrameFragment = (PolyvLiveIntroduceFragment) fragment;
                if (liveIntroduceFrameFragment.getWebView() != null && liveIntroduceFrameFragment.getWebView().canGoBack()) {
                    liveIntroduceFrameFragment.getWebView().goBack();
                    return true;
                }
            } else if (fragment instanceof PolyvCustomMenuFragment) {
                PolyvCustomMenuFragment customMenuFrameFragment = (PolyvCustomMenuFragment) fragment;
                if (customMenuFrameFragment.getWebView() != null && customMenuFrameFragment.getWebView().canGoBack()) {
                    customMenuFrameFragment.getWebView().goBack();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        classDetail.shutdown();
    }
}
