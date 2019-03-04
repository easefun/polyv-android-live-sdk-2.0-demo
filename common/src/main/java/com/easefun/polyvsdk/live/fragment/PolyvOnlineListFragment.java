package com.easefun.polyvsdk.live.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.adapter.PolyvOnlineListAdapter;
import com.easefun.polyvsdk.live.chat.PolyvChatManager;
import com.easefun.polyvsdk.live.chat.linkmic.api.PolyvMicrophoneStatus;
import com.easefun.polyvsdk.live.chat.linkmic.api.PolyvOnlineLinkMicUsers;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvJoinLeaveEntity;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvJoinRequestEntity;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvJoinResponseEntity;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvJoinSuccessEntity;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvMicrophoneEvent;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvMicrophoneStatusEntity;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvOnlineLinkMicUsersEntity;
import com.easefun.polyvsdk.live.chat.linkmic.api.listener.PolyvMicrophoneStatusListener;
import com.easefun.polyvsdk.live.chat.linkmic.api.listener.PolyvOnlineLinkMicUsersListener;
import com.easefun.polyvsdk.live.chat.linkmic.module.AGEventHandler;
import com.easefun.polyvsdk.live.chat.linkmic.module.ConstantApp;
import com.easefun.polyvsdk.live.chat.linkmic.module.PermissionListener;
import com.easefun.polyvsdk.live.chat.linkmic.module.PermissionManager;
import com.easefun.polyvsdk.live.chat.linkmic.module.PolyvLinkMicManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PolyvOnlineListFragment extends Fragment implements View.OnClickListener {
    private boolean isInitialized;
    // 未连接
    private static final int LINKMICSTATUS_UNUNITED = 3;
    // 等待连接
    private static final int LINKMICSTATUS_WAITING = 4;
    // 已连接
    private static final int LINKMICSTATUS_CONNECTED = 5;
    private static final int MICEVENT = 12;
    private static final int LINKMICUSERS = 13;
    private static final int MICSTATUS = 19;
    private static final int LINKMICNEEDTIME = 30;
    private static final int JOINREQUEST = 101, JOINLEAVE = 102, JOINSUCCESS = 103, JOINRESPONSE = 104;
    private static final int ON_CONNECTION_LOST = 201, NO_NETWORK_CONNECTION = 202, ON_REJOINCHANNEL_SUCCESS = 203, ON_ERROR = 204;
    private int linkMicStatus = LINKMICSTATUS_UNUNITED;
    private View view;
    private RelativeLayout rl_parent;
    // 在线列表recyclerView
    private RecyclerView rv_online;
    // 通话按钮
    private Button bt_speak;
    // loadingView
    private ProgressBar pb_loading;
    // viewPagerFragment
    private PolyvTabViewPagerFragment viewPagerFragment;
    // 在线列表集合
    private List<PolyvOnlineLinkMicUsersEntity.OnlineLinkMicUser> lists;
    // 在线列表适配器
    private PolyvOnlineListAdapter onlineListAdapter;
    // 获取在线列表的接口
    private PolyvOnlineLinkMicUsers onlineLinkMicUsers;
    // 获取教师端连麦状态及类型接口
    private PolyvMicrophoneStatus microphoneStatus;
    // 聊天室管理类
    private PolyvChatManager chatManager;
    // 连麦管理类
    private PolyvLinkMicManager linkMicManager;
    // 连麦监听器
    private AGEventHandler agEventHandler;
    // 是否重新获取在线人数数据成功，是否获取教师端的连麦类型成功
    private boolean isRegetDataSuccess, isGetMicTypeSuceess;
    // 提示对话框
    private PolyvDialogFragment dialogFragment;
    // 权限相关
    private final int myRequestCode = 135;
    // 需请求的权限组
    private String[] permissions = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };
    // 权限请求管理类
    private PermissionManager permissionManager;
    // 是否仅请求录音权限
    private boolean isOnlyRequestRecord;
    // 从加入连麦至连麦成功所需的时间，单位：秒
    private int linkMicTime;
    // 加入连麦成功前播放器的音量
    private int videoVolume;
    // 是否被踢
    private boolean isKicked;
    // 是否被禁言
    private boolean isShield;
    // 提示对话框
    private AlertDialog alertDialog;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (getContext() == null)
                return;
            switch (msg.what) {
                // 麦克风事件(包括老师开启/关闭通话，和结束某一个学员的通话)
                case MICEVENT:
                    isGetMicTypeSuceess = true;
                    if (isRegetDataSuccess && pb_loading.getVisibility() == View.VISIBLE)
                        pb_loading.setVisibility(View.GONE);
                    PolyvMicrophoneEvent microphoneEvent = (PolyvMicrophoneEvent) msg.obj;
                    onlineListAdapter.updateLnikMicEvent(microphoneEvent);
                    // 是否是结束某一个学员通话的事件
                    boolean isStopLinkMicEvent = microphoneEvent.isStopLinkMicEvent();
                    // 老师是否是关闭了连麦
                    boolean isCloseLinkMicEven = !isStopLinkMicEvent && microphoneEvent.isCloseStatus();
                    if (isCloseLinkMicEven)
                        updateStatusAndButton(LINKMICSTATUS_UNUNITED);
                    // 老师结束与自己的通话
                    if (isStopLinkMicEvent && microphoneEvent.userId.equals(linkMicManager.getLinkMicUid())
                            // 处于加入连麦状态，老师关闭了连麦
                            || isCloseLinkMicEven && linkMicManager.isJoinStatus()) {
                        leaveChannel();
                        if (linkMicStatus == LINKMICSTATUS_CONNECTED)
                            dialogFragment.show(getFragmentManager(), "dialogFragment", "老师已结束了与您的通话。");
                        updateStatusAndButton(LINKMICSTATUS_UNUNITED);
                    }
                    break;
                // 获取连麦的学员列表成功
                case LINKMICUSERS:
                    isRegetDataSuccess = true;
                    if (isGetMicTypeSuceess && pb_loading.getVisibility() == View.VISIBLE)
                        pb_loading.setVisibility(View.GONE);
                    PolyvOnlineLinkMicUsersEntity onlineLinkMicUsersEntity = (PolyvOnlineLinkMicUsersEntity) msg.obj;
                    for (PolyvOnlineLinkMicUsersEntity.OnlineLinkMicUser linkMicUser : onlineLinkMicUsersEntity.onlineLinkMicUsers) {
                        if (chatManager.isUsedUid(linkMicUser.uid)) {
                            if (isLinkMicConnected())
                                linkMicUser.setJoinStatus();
                            else if (isLinkMicWaiting())
                                linkMicUser.setWaitStatus();
                            else
                                linkMicUser.setDefaultStatus();
                            linkMicUser.setLinkMicUid(linkMicManager.getLinkMicUid());
                            break;
                        }
                    }
                    onlineListAdapter.updateAllData(onlineLinkMicUsersEntity.onlineLinkMicUsers);
                    break;
                // 当前的麦克风状态
                case MICSTATUS:
                    isGetMicTypeSuceess = true;
                    if (isRegetDataSuccess && pb_loading.getVisibility() == View.VISIBLE)
                        pb_loading.setVisibility(View.GONE);
                    PolyvMicrophoneStatusEntity microphoneStatusEntity = (PolyvMicrophoneStatusEntity) msg.obj;
                    onlineListAdapter.setMicType(microphoneStatusEntity);
                    break;
                // 收到请求连麦事件(包括所有学员)
                case JOINREQUEST:
                    PolyvJoinRequestEntity requestEntity = (PolyvJoinRequestEntity) msg.obj;
                    if (!requestEntity.user.userId.equals(linkMicManager.getLinkMicUid()))
                        onlineListAdapter.updateLinkMicJoinRequest(requestEntity);
                    break;
                // 收到离开连麦事件(包括所有学员)
                case JOINLEAVE:
                    PolyvJoinLeaveEntity leaveEntity = (PolyvJoinLeaveEntity) msg.obj;
                    if (!leaveEntity.user.userId.equals(linkMicManager.getLinkMicUid()))
                        onlineListAdapter.updateLinkMicJoinLeave(leaveEntity);
                    break;
                // 收到加入连麦成功事件
                case JOINSUCCESS:
                    Toast.makeText(getContext(), "加入通话成功", Toast.LENGTH_SHORT).show();
                    updateStatusAndButton(LINKMICSTATUS_CONNECTED);
                    // 获取加入连麦前的播放器音量
                    videoVolume = viewPagerFragment.getVideoVolume();
                    // 加入通话成功时，播放器静音
                    viewPagerFragment.setVideoVolume(0);
                    break;
                // 收到老师同意通话事件(仅能收到自己的)
                case JOINRESPONSE:
                    Toast.makeText(getContext(), "老师已同意通话，通话连接中...", Toast.LENGTH_SHORT).show();
                    PolyvJoinResponseEntity responseEntity = (PolyvJoinResponseEntity) msg.obj;
                    if (onlineListAdapter.isVideoType())
                        // 开启视频功能
                        linkMicManager.enableLocalVideo(true);
                    else
                        // 不开启视频功能
                        linkMicManager.enableLocalVideo(false);
                    // 加入连麦
                    linkMicManager.joinChannel(responseEntity.roomId);
                    // 验证从加入连麦至加入连麦成功的时间是否超时
                    verifyLinkMicTimeout();
                    break;
                // 通话重连失败
                case ON_CONNECTION_LOST:
                    leaveChannel(true, false);
                    dialogFragment.show(getFragmentManager(), "dialogFragment", "重连失败，已断开与老师同学间的通话。");
                    updateStatusAndButton(LINKMICSTATUS_UNUNITED);
                    break;
                // 通话由于网络问题正在重连
                case NO_NETWORK_CONNECTION:
                    Toast.makeText(getContext(), "网络异常，通话正在自动重连...", Toast.LENGTH_SHORT).show();
                    break;
                // 通话重连成功
                case ON_REJOINCHANNEL_SUCCESS:
                    Toast.makeText(getContext(), "通话重连成功", Toast.LENGTH_SHORT).show();
                    break;
                // 连麦发生异常
                case ON_ERROR:
                    leaveChannel();
                    updateStatusAndButton(LINKMICSTATUS_UNUNITED);
                    Toast.makeText(getContext(), "连接通话失败\n" + msg.arg1 + "-" + (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                // 验证从加入连麦至连麦成功所需的时间
                case LINKMICNEEDTIME:
                    linkMicTime = linkMicTime + 1;
                    // 连麦超时
                    if (linkMicTime == 10) {
                        leaveChannel();
                        updateStatusAndButton(LINKMICSTATUS_UNUNITED);
                        Toast.makeText(getContext(), "连接通话超时，请重新申请通话", Toast.LENGTH_LONG).show();
                        return;
                    }
                    handler.sendEmptyMessageDelayed(LINKMICNEEDTIME, 1000);
                    break;
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.polyv_fragment_onlinelist, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isInitialized) {
            isInitialized = true;
            findId();
            initView();
        }
    }

    private void findId() {
        rl_parent = (RelativeLayout) view.findViewById(R.id.rl_parent);
        rv_online = (RecyclerView) view.findViewById(R.id.rv_online);
        bt_speak = (Button) view.findViewById(R.id.bt_speak);
        pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
        lists = new ArrayList<>();
        onlineLinkMicUsers = new PolyvOnlineLinkMicUsers();
        microphoneStatus = new PolyvMicrophoneStatus();
        linkMicManager = new PolyvLinkMicManager();
        dialogFragment = new PolyvDialogFragment();
        viewPagerFragment = (PolyvTabViewPagerFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fl_tag_viewpager);
        permissionManager = PermissionManager.with(this)
                .addRequestCode(myRequestCode)
                .setPermissionsListener(new PermissionListener() {
                    @Override
                    public void onGranted() {
                        List<String> permissions = new ArrayList<String>();
                        int[] ops = new int[]{PermissionManager.OP_CAMERA, PermissionManager.OP_RECORD_AUDIO};
                        boolean checkOp = permissionManager.checkOp(getContext(), isOnlyRequestRecord ? Arrays.copyOfRange(ops, 1, 2) : ops, permissions);
                        if (!checkOp) {
                            showDialog("提示", "通话所需的%s权限被拒绝，请到应用设置的权限管理中恢复", true, permissions.toArray(new String[permissions.size()]));
                            return;
                        }
                        requestSpeak();
                    }

                    @Override
                    public void onDenied(String[] permissions) {
                        showDialog("提示", "需要允许%s权限才能进行通话，是否再次请求", false, permissions);
                    }

                    @Override
                    public void onShowRationale(String[] permissions) {
                        showDialog("提示", "通话所需的%s权限被拒绝，请到应用设置的权限管理中恢复", true, permissions);
                    }
                });
    }

    private void showDialog(String title, String message, final boolean isRequestSetting, String[] permissions) {
        String tipsMessage = permissions.length == 2 ? String.format(message, "录音和相机")
                : (Manifest.permission.CAMERA.equals(permissions[0]) ? String.format(message, "相机")
                : String.format(message, "录音"));
        new AlertDialog.Builder(getContext()).setTitle(title)
                .setMessage(tipsMessage)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isRequestSetting)
                            permissionManager.requestSetting();
                        else
                            permissionManager.request();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getContext(), "权限不足，申请发言失败", Toast.LENGTH_SHORT).show();
                    }
                }).setCancelable(false).show();
    }

    // 由于服务器在发送允许通话后，若10秒内客户端没有加入连麦成功就会断开连接，故这里需要验证加入连麦是否超时
    private void verifyLinkMicTimeout() {
        linkMicTime = 0;
        handler.removeMessages(LINKMICNEEDTIME);
        handler.sendEmptyMessage(LINKMICNEEDTIME);
    }

    private void initView() {
        chatManager = viewPagerFragment.getChatManager();

        // 添加连麦通话监听
        linkMicManager.addEventHandler(agEventHandler = new AGEventHandler() {
            @Override
            public void onFirstRemoteVideoDecoded(final int uid, int width, int height, int elapsed) {
            }

            @Override
            public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
                if (linkMicManager.isJoinStatus()) {
                    Message message = handler.obtainMessage(JOINSUCCESS);
                    handler.sendMessage(message);
                }
                handler.removeMessages(LINKMICNEEDTIME);
            }

            @Override
            public void onUserOffline(final int uid, int reason) {
            }

            @Override
            public void onExtraCallback(int type, Object... data) {
                switch (type) {
                    // 连接中断
                    case AGEventHandler.EVENT_TYPE_ON_APP_ERROR:
                        if ((int) data[0] == ConstantApp.AppError.ON_CONNECTION_LOST) {
                            // 连接中断，超过10秒仍然连接不上
                            handler.sendEmptyMessage(ON_CONNECTION_LOST);
                        } else if ((int) data[0] == ConstantApp.AppError.NO_NETWORK_CONNECTION) {
                            // 连接中断，正在重连中
                            handler.sendEmptyMessage(NO_NETWORK_CONNECTION);
                        }
                        break;
                    case AGEventHandler.EVENT_TYPE_ON_REJOINCHANNEL_SUCCESS:
                        // 重连成功
                        handler.sendEmptyMessage(ON_REJOINCHANNEL_SUCCESS);
                        break;
                    case AGEventHandler.EVENT_TYPE_ON_AGORA_MEDIA_ERROR:
                        if (linkMicManager.isJoinStatus()) {
                            handler.removeMessages(LINKMICNEEDTIME);
                            // 连麦失败
                            Message message = handler.obtainMessage(ON_ERROR);
                            message.arg1 = (int) data[0];
                            message.obj = data[1];
                            handler.sendMessage(message);
                        }
                        break;
                }
            }
        });
        // 设置连麦相关事件的监听
        chatManager.setLinkMicrophoneListener(new PolyvChatManager.LinkMicrophoneListener() {
            // 用户离开事件
            @Override
            public void joinLeave(PolyvJoinLeaveEntity leaveEntity) {
                Message message = handler.obtainMessage(JOINLEAVE);
                message.obj = leaveEntity;
                handler.sendMessage(message);
            }

            // 用户举手事件
            @Override
            public void joinRequest(PolyvJoinRequestEntity requestEntity) {
                Message message = handler.obtainMessage(JOINREQUEST);
                message.obj = requestEntity;
                handler.sendMessage(message);
            }

            // 教师端同意事件(只能收到自己的)
            @Override
            public void joinResponse(PolyvJoinResponseEntity responseEntity) {
                Message message = handler.obtainMessage(JOINRESPONSE);
                message.obj = responseEntity;
                handler.sendMessage(message);
            }

            @Override
            public void joinSuccess(PolyvJoinSuccessEntity successEntity) {
                //已废弃，使用onJoinChannelSuccess代替
            }

            // 教师端麦克风的状态，及结束通话事件
            @Override
            public void onMicrophoneEvent(PolyvMicrophoneEvent microphoneEvent) {
                Message message = handler.obtainMessage(MICEVENT);
                message.obj = microphoneEvent;
                handler.sendMessage(message);
            }
        });

        // 获取连麦的类型，需先获取连麦的类型再更新在线列表的数据
        getLinkMicType(chatManager.getChannelId());
        // 构建在线列表适配器
        onlineListAdapter = new PolyvOnlineListAdapter(rv_online, lists);
        // 初始化配置
        onlineListAdapter.config(bt_speak, chatManager);
        // 固定大小
        rv_online.setHasFixedSize(true);
        // 不使用嵌套滑动
        rv_online.setNestedScrollingEnabled(false);
        // 设置布局管理器
        rv_online.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        // 设置适配器
        rv_online.setAdapter(onlineListAdapter);
        // 设置点击监听
        bt_speak.setOnClickListener(this);
    }

    // 获取连麦的类型
    private void getLinkMicType(String channelId) {
        microphoneStatus.shutdown();
        microphoneStatus.getMicrophoneStatus(channelId, Integer.MAX_VALUE, new PolyvMicrophoneStatusListener() {
            @Override
            public void success(PolyvMicrophoneStatusEntity microphoneStatusEntity) {
                Message message = handler.obtainMessage(MICSTATUS);
                message.obj = microphoneStatusEntity;
                handler.sendMessage(message);
            }

            @Override
            public void fail(String failTips, int code) {
            }
        });
    }

    /**
     * 获取连麦的在线列表数据
     *
     * @param delay 延迟时间
     */
    private void getLinkMicData(int delay) {
        onlineLinkMicUsers.shutdownSchedule();
        onlineLinkMicUsers.getOnlineAndLinkMicUsers(chatManager.getChannelId(), 10000, delay, new PolyvOnlineLinkMicUsersListener() {
            @Override
            public void success(final PolyvOnlineLinkMicUsersEntity onlineLinkMicUsersEntity) {
                Message message = handler.obtainMessage(LINKMICUSERS);
                message.obj = onlineLinkMicUsersEntity;
                handler.sendMessage(message);
            }

            @Override
            public void fail(String failTips, int code) {
            }
        }, chatManager);
    }

    // 离开连麦
    private void leaveChannel() {
        leaveChannel(false, false);
    }

    /**
     * 离开连麦
     *
     * @param isConnectLost 是否是重连连麦失败
     * @param isSended      聊天室的离开事件是否已成功发送
     */
    private void leaveChannel(boolean isConnectLost, boolean isSended) {
        if (linkMicStatus == LINKMICSTATUS_CONNECTED) {
            // 离开连麦
            linkMicManager.leaveChannel();
            // 如果没有发送离开事件
            if (!isSended)
                chatManager.sendJoinLeave(linkMicManager.getLinkMicUid(), isConnectLost);
            // 播放器恢复为连麦前的音量
            viewPagerFragment.setVideoVolume(videoVolume);
            // 隐藏对话框
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.hide();
            }
        } else if (linkMicManager.isJoinStatus()) {
            // 移除延迟发送的消息
            handler.removeMessages(LINKMICNEEDTIME);
            // 离开连麦
            linkMicManager.leaveChannel();
            // 发送离开事件
            if (!isSended)
                chatManager.sendJoinLeave(linkMicManager.getLinkMicUid(), isConnectLost);
        }
    }

    /**
     * 自己被取消禁言的时候调用
     */
    public void unshield() {
        if (!isInitialized)
            return;
        isShield = false;
    }

    /**
     * 自己被踢/被禁言的时候调用
     *
     * @param isKicked true：被踢，false：被禁言
     */
    public void kickOrShield(boolean isKicked) {
        if (!isInitialized)
            return;
        if (isKicked)
            this.isKicked = true;
        else
            this.isShield = true;
        if (linkMicManager.isJoinStatus()) {
            if (linkMicStatus == LINKMICSTATUS_CONNECTED)
                Toast.makeText(getContext(), "老师已结束了与您的通话。", Toast.LENGTH_SHORT).show();
            leaveChannel();
            updateStatusAndButton(LINKMICSTATUS_UNUNITED);
        } else if (chatManager.isRequestStatus()) {
            Toast.makeText(getContext(), "您当前无法申请发言", Toast.LENGTH_SHORT).show();
            chatManager.sendJoinLeave(linkMicManager.getLinkMicUid());
            updateStatusAndButton(LINKMICSTATUS_UNUNITED);
        }
    }

    // 聊天室登录成功时调用
    public void loginSuccess() {
        if (!isInitialized)
            return;
        // 初始化连麦配置
        linkMicManager.setupParentView(rl_parent);
        isKicked = false;
        isShield = false;
        // 登录成功时再获取数据，避免获取不了自己的信息
        getLinkMicData(0);
    }

    // 聊天室重连时调用
    public void reconnecting() {
        if (!isInitialized)
            return;
        isRegetDataSuccess = false;
        isGetMicTypeSuceess = false;
        pb_loading.setVisibility(View.VISIBLE);
        // 中断获取在线列表的数据
        onlineLinkMicUsers.shutdownSchedule();
        // 如果已经发送了举手请求并且没有处于加入连麦状态
        if (chatManager.isRequestStatus() && !linkMicManager.isJoinStatus()) {
            Toast.makeText(getContext(), "已断开与聊天室的连接", Toast.LENGTH_SHORT).show();
            updateStatusAndButton(LINKMICSTATUS_UNUNITED);
        }
    }

    // 聊天室重连成功时调用
    public void reconnectSuccess() {
        if (!isInitialized)
            return;
        isKicked = false;
        isShield = false;
        // 如果已经发送了举手请求并且没有处于加入连麦状态
        if (chatManager.isRequestStatus() && !linkMicManager.isJoinStatus())
            // 发送取消连麦请求的消息
            chatManager.sendJoinLeave(linkMicManager.getLinkMicUid());
        // 获取连麦的类型(因为可能在重连的过程中老师端的连麦类型改变了)
        getLinkMicType(chatManager.getChannelId());
        // 获取连麦的数据
        getLinkMicData(500);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == myRequestCode && resultCode == Activity.RESULT_CANCELED && onlineListAdapter.isOpenStatus())
            permissionManager.request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case myRequestCode:
                permissionManager.onPermissionResult(permissions, grantResults);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 移除延迟发送的消息
        handler.removeMessages(LINKMICNEEDTIME);
        // 移除监听
        linkMicManager.removeEventHandler(agEventHandler);
        // 发送离开连麦请求
        if (chatManager.isRequestStatus())
            chatManager.sendJoinLeave(linkMicManager.getLinkMicUid());
        // 离开连麦并销毁
        linkMicManager.destroy(rl_parent);
        // 中断获取连麦数据
        onlineLinkMicUsers.shutdownSchedule();
        // 中断获取连麦状态
        microphoneStatus.shutdown();
    }

    private void updateStatusAndButton(int linkMicStatus) {
        this.linkMicStatus = linkMicStatus;
        switch (linkMicStatus) {
            case LINKMICSTATUS_UNUNITED:
                onlineListAdapter.updateLinkMicJoinLeave(linkMicManager.getLinkMicUid());
                bt_speak.setText("申请发言");
                bt_speak.setTextColor(getResources().getColor(R.color.top_layout_color_white));
                bt_speak.setBackgroundResource(R.drawable.polyv_tv_press);
                break;
            case LINKMICSTATUS_WAITING:
                onlineListAdapter.updateLinkMicJoinRequest(chatManager.getUid(), linkMicManager.getLinkMicUid(), true);
                bt_speak.setText("取消发言");
                bt_speak.setTextColor(getResources().getColor(R.color.center_view_color_blue));
                bt_speak.setBackgroundResource(R.drawable.polyv_tv_press_w_to_g);
                break;
            case LINKMICSTATUS_CONNECTED:
                onlineListAdapter.updateLinkMicJoinSuccess(chatManager.getUid(), linkMicManager.getLinkMicUid(), true);
                bt_speak.setText("结束通话");
                bt_speak.setTextColor(getResources().getColor(R.color.center_bottom_text_color_red));
                bt_speak.setBackgroundResource(R.drawable.polyv_tv_press_w_to_g);
                break;
        }
    }

    // 是否处于加入连麦成功状态
    public boolean isLinkMicConnected() {
        return linkMicStatus == LINKMICSTATUS_CONNECTED;
    }

    public boolean isLinkMicWaiting() {
        return linkMicStatus == LINKMICSTATUS_WAITING;
    }

    // 显示是否结束通话对话框
    public void showStopCallDialog(final boolean isExit) {
        alertDialog = new AlertDialog.Builder(getContext()).setTitle("确定结束通话？")
                .setMessage(String.format("您将断开与老师同学间的通话%s。", isExit ? "并退出" : ""))
                .setNegativeButton("取消", null)
                .setPositiveButton(String.format("结束通话%s", isExit ? "并退出" : ""), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isExit) {
                            getActivity().finish();
                            return;
                        }
                        boolean isSuccess = chatManager.sendJoinLeave(linkMicManager.getLinkMicUid());
                        if (isSuccess) {
                            leaveChannel(false, true);
                            updateStatusAndButton(LINKMICSTATUS_UNUNITED);
                        } else {
                            Toast.makeText(getContext(), "请登录聊天室后操作", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.center_view_color_blue));
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.center_view_color_blue));
    }

    // 申请发言
    private void requestSpeak() {
        // 可能由于在允许权限的过程中，老师关闭了连麦，故这里需要进行判断
        if (onlineListAdapter.isOpenStatus())
            if (isKicked || isShield) {
                Toast.makeText(getContext(), "您当前无法申请发言", Toast.LENGTH_SHORT).show();
            } else if (pb_loading.getVisibility() == View.VISIBLE) {
                Toast.makeText(getContext(), "请等待更新在线列表数据后操作", Toast.LENGTH_SHORT).show();
            } else {
                boolean isSuccess = chatManager.sendJoinRequest(linkMicManager.getLinkMicUid());
                if (isSuccess) {
                    updateStatusAndButton(LINKMICSTATUS_WAITING);
                } else {
                    Toast.makeText(getContext(), "请登录聊天室后操作", Toast.LENGTH_SHORT).show();
                }
            }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.bt_speak) {
            if (linkMicStatus == LINKMICSTATUS_UNUNITED) {
                // 先请求权限，获取权限成功后再申请发言
                if (onlineListAdapter.isVideoType()) {
                    isOnlyRequestRecord = false;
                    permissionManager.permissions(permissions);
                } else {
                    isOnlyRequestRecord = true;
                    permissionManager.permissions(permissions[1]);
                }
                permissionManager.request();
            } else if (linkMicStatus == LINKMICSTATUS_WAITING) {
                boolean isSuccess = chatManager.sendJoinLeave(linkMicManager.getLinkMicUid());
                if (isSuccess) {
                    leaveChannel(false, true);
                    updateStatusAndButton(LINKMICSTATUS_UNUNITED);
                } else {
                    Toast.makeText(getContext(), "请登录聊天室后操作", Toast.LENGTH_SHORT).show();
                }
            } else if (linkMicStatus == LINKMICSTATUS_CONNECTED) {
                showStopCallDialog(false);
            }

        }
    }
}
