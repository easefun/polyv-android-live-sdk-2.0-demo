package com.easefun.polyvsdk.live.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.easefun.polyvsdk.live.PolyvApplication;
import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.adapter.AbsRecyclerViewAdapter;
import com.easefun.polyvsdk.live.adapter.EndlessRecyclerOnScrollListener;
import com.easefun.polyvsdk.live.adapter.HeaderViewRecyclerAdapter;
import com.easefun.polyvsdk.live.adapter.PolyvPlaybackListAdapter;
import com.easefun.polyvsdk.live.chat.playback.api.PolyvLiveRecordFiles;
import com.easefun.polyvsdk.live.chat.playback.api.entity.PolyvLiveRecordFilesEntity;
import com.easefun.polyvsdk.live.chat.playback.api.listener.PolyvLiveRecordFilesListener;
import com.easefun.polyvsdk.live.playback.activity.PolyvPPTPbPlayerActivity;
import com.easefun.polyvsdk.live.playback.activity.PolyvPbPlayerActivity;

import java.util.ArrayList;
import java.util.List;

public class PolyvPlaybackListFragment extends Fragment {
    private boolean isInitialized;
    private View view;
    private View loadMoreView;
    private TextView tv_empty;
    private ProgressBar pb_loading;
    private RecyclerView rv_playback;
    private PolyvPlaybackListAdapter playbackListAdapter;
    private HeaderViewRecyclerAdapter headerViewRecyclerAdapter;
    private LinearLayoutManager linearLayoutManager;
    private EndlessRecyclerOnScrollListener onScrollListener;
    private PolyvLiveRecordFilesEntity recordFilesEntity;
    private List<PolyvLiveRecordFilesEntity.Content> contents;
    private PolyvLiveRecordFiles liveRecordFiles;
    private int page = 1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.polyv_fragment_playbacklist, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isInitialized) {
            isInitialized = true;
            findId();
            initView(getIntent());
        }
    }

    private void findId() {
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        rv_playback = (RecyclerView) view.findViewById(R.id.rv_playback);
        tv_empty = (TextView) view.findViewById(R.id.tv_empty);
        contents = new ArrayList<>();
        liveRecordFiles = new PolyvLiveRecordFiles();
    }

    private void loadPlaybackList(boolean isShowProgressBar) {
        if (isShowProgressBar)
            pb_loading.setVisibility(View.VISIBLE);
        liveRecordFiles.shutdown();
        liveRecordFiles.getRecordFiles(PolyvApplication.appId, PolyvApplication.appSecret,
                getIntent().getStringExtra("channelId"), page, Integer.MAX_VALUE, new PolyvLiveRecordFilesListener() {
                    @Override
                    public void success(PolyvLiveRecordFilesEntity recordFilesEntity) {
                        if (PolyvPlaybackListFragment.this.recordFilesEntity == null)
                            PolyvPlaybackListFragment.this.recordFilesEntity = recordFilesEntity;
                        else {
                            PolyvPlaybackListFragment.this.recordFilesEntity.data.contents.addAll(recordFilesEntity.data.contents);
                            PolyvPlaybackListFragment.this.recordFilesEntity.page = page;
                        }

                        pb_loading.setVisibility(View.GONE);
                        loadMoreView.setVisibility(View.GONE);
                        playbackListAdapter.addAll(recordFilesEntity.data.contents);
                        if (page == 1)
                            updatePlayPosition(getIntent());
                        if (recordFilesEntity.data.contents.size() == 0 && page == 1)
                            tv_empty.setVisibility(View.VISIBLE);
                        // 如果总页数和当前页数相等，说明没有更多的数据了
                        if (recordFilesEntity.data.totalPages == page)
                            headerViewRecyclerAdapter.removeFootView();
                    }

                    @Override
                    public void fail(String failTips, int code) {
                    }

                    @Override
                    public void reconnect(long count) {
                        if (count == 3) {
                            final Activity activity = getActivity();
                            if (activity != null) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(activity, "获取回放列表失败，请检查你的网络", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                    }
                });
    }

    // 更新正在播放的item索引
    private void updatePlayPosition(Intent intent) {
        String vid = intent.getStringExtra("vid");
        String playbackUrl = intent.getStringExtra("playbackUrl");
        if (!TextUtils.isEmpty(vid)) {
            for (int i = 0; i < contents.size(); i++) {
                if (vid.equals(contents.get(i).videoPoolId)) {
                    playbackListAdapter.updatePlayPosition(i);
                    break;
                }
            }
        } else if (!TextUtils.isEmpty(playbackUrl)) {
            for (int i = 0; i < contents.size(); i++) {
                if (playbackUrl.equals(contents.get(i).url)) {
                    playbackListAdapter.updatePlayPosition(i);
                    break;
                }
            }
        }
    }

    private Intent getIntent() {
        return getActivity().getIntent();
    }

    // 当前是普通回放的播放器
    private boolean isPbPlayerActivity() {
        return getActivity() instanceof PolyvPbPlayerActivity;
    }

    // 当前是ppt回放的播放器
    private boolean isPPTPbPlayerActivity() {
        return getActivity() instanceof PolyvPPTPbPlayerActivity;
    }

    public void initView(final Intent intent) {
        if (!isInitialized)
            return;
        recordFilesEntity = (PolyvLiveRecordFilesEntity) intent.getSerializableExtra("playbackList");
        if (recordFilesEntity != null) {
            contents = new ArrayList<>(recordFilesEntity.data.contents);
            page = recordFilesEntity.page;
            if (contents.size() == 0)
                tv_empty.setVisibility(View.VISIBLE);
        } else {
            loadPlaybackList(true);
        }
        playbackListAdapter = new PolyvPlaybackListAdapter(rv_playback, contents);
        if (recordFilesEntity != null) {
            updatePlayPosition(intent);
        }
        playbackListAdapter.setOnItemClickListener(new AbsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, AbsRecyclerViewAdapter.ClickableViewHolder holder) {
                PolyvLiveRecordFilesEntity.Content content = contents.get(position);
                int bitrate = Integer.parseInt(content.myBr);
                String sessionId = content.channelSessionId;
                Intent playIntent;
                // 如果sessionId为空，则说明是普通的回放
                if (TextUtils.isEmpty(sessionId)) {
                    if (isPbPlayerActivity()) {
                        playbackListAdapter.updatePlayPosition(position);
                        ((PolyvPbPlayerActivity) getActivity()).playVid(content.videoPoolId, bitrate, true, false);
                        return;
                    }
                    playIntent = PolyvPbPlayerActivity.newVidIntent(getActivity(), content.videoPoolId, bitrate, true, false, false);
                } else {
                    if (isPPTPbPlayerActivity()) {
                        playbackListAdapter.updatePlayPosition(position);
                        ((PolyvPPTPbPlayerActivity) getActivity()).playVid(content.videoPoolId, bitrate, true, false, sessionId, true);
                        return;
                    }
                    playIntent = PolyvPPTPbPlayerActivity.newVidIntent(getActivity(), content.videoPoolId, bitrate, true, false, sessionId, true, false);
                }
                playIntent = PolyvPbPlayerActivity.addChatExtra(playIntent, intent.getStringExtra("userId"), intent.getStringExtra("channelId"), intent.getStringExtra("chatUserId"), intent.getStringExtra("nickName"), intent.getBooleanExtra("isGetLiveStatus", false));
                playIntent = PolyvPbPlayerActivity.addLiveExtra(playIntent, intent.getBooleanExtra("isFromPPTLive", false), intent.getBooleanExtra("isToOtherLivePlayer", true));
                playIntent.putExtra("playbackList", recordFilesEntity);
                playIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                liveRecordFiles.shutdown();
                getActivity().startActivity(playIntent);
            }
        });
        headerViewRecyclerAdapter = new HeaderViewRecyclerAdapter(playbackListAdapter);
        // 固定大小
        rv_playback.setHasFixedSize(true);
        // 不使用嵌套滑动
        rv_playback.setNestedScrollingEnabled(false);
        // 设置布局管理器
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        if (rv_playback.getLayoutManager() == null)
            rv_playback.setLayoutManager(linearLayoutManager);
        // 设置适配器
        rv_playback.setAdapter(headerViewRecyclerAdapter);
        if (recordFilesEntity == null || recordFilesEntity.data.totalPages > page) {
            headerViewRecyclerAdapter.removeFootView();
            createLoadMoreView();
            if (onScrollListener == null)
                rv_playback.addOnScrollListener(createScrollListener());
            onScrollListener.refresh();
        }
    }

    private void createLoadMoreView() {
        if (loadMoreView == null)
            loadMoreView = LayoutInflater.from(getContext()).inflate(R.layout.polyv_bottom_loadmorelayout, rv_playback, false);
        headerViewRecyclerAdapter.addFooterView(loadMoreView);
        loadMoreView.setVisibility(View.GONE);
    }

    private EndlessRecyclerOnScrollListener createScrollListener() {
        if (onScrollListener == null)
            onScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int currentPage) {
                    if (headerViewRecyclerAdapter.getFooterCount() > 0) {
                        page++;
                        loadMoreView.setVisibility(View.VISIBLE);
                        loadPlaybackList(false);
                    }
                }
            };
        return onScrollListener;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 中断请求
        liveRecordFiles.shutdown();
    }
}
