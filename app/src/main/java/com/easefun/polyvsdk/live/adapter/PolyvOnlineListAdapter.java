package com.easefun.polyvsdk.live.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.chat.PolyvChatManager;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvJoinLeaveEntity;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvJoinRequestEntity;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvJoinSuccessEntity;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvMicrophoneEvent;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvMicrophoneStatusEntity;
import com.easefun.polyvsdk.live.chat.linkmic.api.entity.PolyvOnlineLinkMicUsersEntity;
import com.easefun.polyvsdk.live.util.PolyvRoundDisplayerUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Collections;
import java.util.List;

public class PolyvOnlineListAdapter extends AbsRecyclerViewAdapter {
    private List<PolyvOnlineLinkMicUsersEntity.OnlineLinkMicUser> lists;
    private DisplayImageOptions options;
    private PolyvChatManager chatManager;
    private Button bt_speak;
    // 老师开启连麦的类型
    private String micType = PolyvMicrophoneEvent.TYPE_VIDEO;
    // 老师是否开启了连麦
    private boolean isOpenStatus;

    public PolyvOnlineListAdapter(RecyclerView recyclerView, List<PolyvOnlineLinkMicUsersEntity.OnlineLinkMicUser> lists) {
        super(recyclerView);
        this.lists = lists;
        this.options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.polyv_avatar_def) // resource
                // or
                // drawable
                .showImageForEmptyUri(R.drawable.polyv_avatar_def) // resource or
                // drawable
                .showImageOnFail(R.drawable.polyv_avatar_def) // resource or drawable
                .bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory(true).cacheOnDisk(true)
                .displayer(new PolyvRoundDisplayerUtils(0)).build();
    }

    // 配置需使用的参数
    public void config(Button bt_speak, PolyvChatManager chatManager) {
        this.bt_speak = bt_speak;
        this.chatManager = chatManager;
    }

    // 更新数据
    public void updateAllData(List<PolyvOnlineLinkMicUsersEntity.OnlineLinkMicUser> onlineLinkMicUsers) {
        lists.clear();
        lists.addAll(onlineLinkMicUsers);
        updateStatusAndButton();
        Collections.sort(lists, PolyvOnlineLinkMicUsersEntity.linkMicComparator);
        notifyDataSetChanged();
    }

    // 是否是视频连麦类型
    public boolean isVideoType() {
        return PolyvMicrophoneEvent.TYPE_VIDEO.equals(micType);
    }

    // 老师是否处于开启连麦状态
    public boolean isOpenStatus() {
        return isOpenStatus;
    }

    // 设置连麦的类型
    public void setMicType(PolyvMicrophoneStatusEntity statusEntity) {
        this.micType = statusEntity.type;
        if (lists != null && lists.size() > 0)
            notifyDataSetChanged();
    }

    /**
     * 更新加入连麦成功对应的item信息
     *
     * @param successEntity
     */
    public void updateLinkMicJoinSuccess(PolyvJoinSuccessEntity successEntity) {
        updateLinkMicJoinSuccess(successEntity.user.uid, successEntity.user.userId, false);
    }

    /**
     * 更新离开连麦对应的item信息
     *
     * @param leaveEntity
     */
    public void updateLinkMicJoinLeave(PolyvJoinLeaveEntity leaveEntity) {
        updateLinkMicJoinLeave(leaveEntity.user.userId);
    }

    public void updateLinkMicJoinLeave(String linkMicUid) {
        if (lists != null && lists.size() > 0) {
            for (PolyvOnlineLinkMicUsersEntity.OnlineLinkMicUser onlineLinkMicUser : lists) {
                // 比较连麦id是否相同
                if (onlineLinkMicUser.linkMicUid.equals(linkMicUid)) {
                    // 清除连麦状态
                    onlineLinkMicUser.setDefaultStatus();
                    break;
                }
            }
            Collections.sort(lists, PolyvOnlineLinkMicUsersEntity.linkMicComparator);
            notifyDataSetChanged();
        }
    }

    public void updateLinkMicJoinSuccess(String uid, String linkMicUid, boolean isMyself) {
        if (lists != null && lists.size() > 0) {
            for (PolyvOnlineLinkMicUsersEntity.OnlineLinkMicUser onlineLinkMicUser : lists) {
                if (onlineLinkMicUser.uid.equals(uid) || (isMyself && chatManager.isUsedUid(onlineLinkMicUser.uid))) {
                    // 把连麦成功时的userId(连麦id)赋给在线列表的连麦id
                    onlineLinkMicUser.setLinkMicUid(linkMicUid);
                    onlineLinkMicUser.setJoinStatus();
                    break;
                }
            }
            // 重新排序
            Collections.sort(lists, PolyvOnlineLinkMicUsersEntity.linkMicComparator);
            notifyDataSetChanged();
        }
    }

    /**
     * 更新请求连麦对应的item的信息
     *
     * @param requestEntity
     */
    public void updateLinkMicJoinRequest(PolyvJoinRequestEntity requestEntity) {
        updateLinkMicJoinRequest(requestEntity.user.uid, requestEntity.user.userId, false);
    }

    public void updateLinkMicJoinRequest(String uid, String linkMicUid, boolean isMyself) {
        if (lists != null && lists.size() > 0) {
            for (PolyvOnlineLinkMicUsersEntity.OnlineLinkMicUser onlineLinkMicUser : lists) {
                if (onlineLinkMicUser.uid.equals(uid) || (isMyself && chatManager.isUsedUid(onlineLinkMicUser.uid))) {
                    // 把请求连麦时的userId(连麦id)赋给在线列表的连麦id
                    onlineLinkMicUser.setLinkMicUid(linkMicUid);
                    // 状态设置为等待
                    onlineLinkMicUser.setWaitStatus();
                    break;
                }
            }
            Collections.sort(lists, PolyvOnlineLinkMicUsersEntity.linkMicComparator);
            notifyDataSetChanged();
        }
    }

    /**
     * 更新连麦的状态及类型对应的老师item信息
     *
     * @param microphoneEvent
     */
    public void updateLnikMicEvent(PolyvMicrophoneEvent microphoneEvent) {
        // 是否是老师的打开连麦/关闭连麦事件
        boolean isTeacherEvent = microphoneEvent.isTeacherEvent();
        if (isTeacherEvent) {
            micType = microphoneEvent.type;
            isOpenStatus = microphoneEvent.isOpenStatus();
            if (isOpenStatus)
                bt_speak.setVisibility(View.VISIBLE);
            else {
                bt_speak.setText("申请发言");
                bt_speak.setTextColor(bt_speak.getContext().getResources().getColor(R.color.top_layout_color_white));
                bt_speak.setBackgroundResource(R.drawable.polyv_tv_press);
                bt_speak.setVisibility(View.INVISIBLE);
            }
            if (lists != null && lists.size() > 0) {
                for (PolyvOnlineLinkMicUsersEntity.OnlineLinkMicUser onlineLinkMicUser : lists) {
                    if (onlineLinkMicUser.userId.equals(microphoneEvent.teacherId)) {
                        if (isOpenStatus) {
                            // 设置老师的item为加入连麦状态
                            onlineLinkMicUser.setJoinStatus();
                        } else {
                            // 设置老师的item为默认状态
                            onlineLinkMicUser.setDefaultStatus();
                        }
                    } else if (!isOpenStatus) {
                        // 老师关闭连麦时，把每一个User的状态都清空
                        onlineLinkMicUser.setDefaultStatus();
                    }
                }
                if (!isOpenStatus)
                    Collections.sort(lists, PolyvOnlineLinkMicUsersEntity.linkMicComparator);
                notifyDataSetChanged();
            }
        }
    }

    // 更新状态及底部按钮的可见性
    private void updateStatusAndButton() {
        boolean isOpenStatus = false;
        for (PolyvOnlineLinkMicUsersEntity.OnlineLinkMicUser onlineLinkMicUser : lists) {
            // 如果是连麦状态并且是老师
            if (onlineLinkMicUser.isJoinStatus() && onlineLinkMicUser.isTeacherType()) {
                isOpenStatus = true;
                break;
            }
        }
        bt_speak.setVisibility(isOpenStatus ? View.VISIBLE : View.INVISIBLE);
        this.isOpenStatus = isOpenStatus;
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
        return new ItemViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.polyv_recyclerview_onlinelist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            viewHolder.tv_usertype.setVisibility(View.GONE);
            viewHolder.iv_mictype.setVisibility(View.GONE);
            viewHolder.tv_speak_status.setVisibility(View.GONE);
            PolyvOnlineLinkMicUsersEntity.OnlineLinkMicUser onlineLinkMicUser = lists.get(position);
            ImageLoader.getInstance().displayImage(onlineLinkMicUser.pic, viewHolder.iv_avatar, options);
            // 如果是开启视频通话
            if (isVideoType())
                viewHolder.iv_mictype.setSelected(true);
            else
                viewHolder.iv_mictype.setSelected(false);
            // 如果是自己登录聊天室用过的uid
            if (chatManager.isUsedUid(onlineLinkMicUser.uid))
                viewHolder.tv_nickname.setText(onlineLinkMicUser.nick + "(我)");
            else
                viewHolder.tv_nickname.setText(onlineLinkMicUser.nick);
            if (onlineLinkMicUser.isTeacherType()) {
                viewHolder.tv_usertype.setText("讲师");
                viewHolder.tv_usertype.setVisibility(View.VISIBLE);
            } else if (onlineLinkMicUser.isManagerType()) {
                viewHolder.tv_usertype.setText("管理员");
                viewHolder.tv_usertype.setVisibility(View.VISIBLE);
            } else if (onlineLinkMicUser.isAssistantType()) {
                viewHolder.tv_usertype.setText("助教");
                viewHolder.tv_usertype.setVisibility(View.VISIBLE);
            }
            if (onlineLinkMicUser.isJoinStatus()) {
                viewHolder.iv_mictype.setVisibility(View.VISIBLE);
                viewHolder.tv_speak_status.setText("发言中");
                viewHolder.tv_speak_status.setVisibility(View.VISIBLE);
            } else if (onlineLinkMicUser.isWaitStatus()) {
                viewHolder.tv_speak_status.setText("等待发言");
                viewHolder.tv_speak_status.setVisibility(View.VISIBLE);
            }
        }
        super.onBindViewHolder(holder, position);
    }

    private class ItemViewHolder extends AbsRecyclerViewAdapter.ClickableViewHolder {
        private ImageView iv_avatar, iv_mictype;
        private TextView tv_nickname, tv_usertype, tv_speak_status;

        public ItemViewHolder(View itemView) {
            super(itemView);
            iv_avatar = $(R.id.iv_avatar);
            iv_mictype = $(R.id.iv_mictype);
            tv_nickname = $(R.id.tv_nickname);
            tv_usertype = $(R.id.tv_usertype);
            tv_speak_status = $(R.id.tv_speak_status);
        }
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }
}
