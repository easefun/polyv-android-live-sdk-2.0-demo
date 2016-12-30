package com.easefun.polyvsdk.live.adapter;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.chat.PolyvChatManager;
import com.easefun.polyvsdk.live.chat.PolyvChatMessage;
import com.easefun.polyvsdk.live.fragment.PolyvDanmuFragment;
import com.easefun.polyvsdk.live.util.PolyvRoundDisplayerUtils;
import com.easefun.polyvsdk.live.util.PolyvTextImageLoader;
import com.easefun.polyvsdk.live.util.PolyvTimeUtils;
import com.easefun.polyvsdk.live.util.PolyvViewHolder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

import pl.droidsonroids.gif.GifSpanTextView;

public class PolyvChatAdapter extends BaseAdapter implements OnClickListener {
    private List<PolyvChatMessage> messages;
    private LayoutInflater inflater;
    private ListView lv_chat;
    private DisplayImageOptions options;
    private PolyvDanmuFragment danmuFragment;
    private PolyvTextImageLoader textImageLoader;

    public PolyvChatAdapter(Context context, List<PolyvChatMessage> messages, ListView lv_chat) {
        this.messages = messages;
        this.inflater = LayoutInflater.from(context);
        this.lv_chat = lv_chat;
        this.options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.polyv_avatar_def) // resource
                // or
                // drawable
                .showImageForEmptyUri(R.drawable.polyv_avatar_def) // resource
                // or
                // drawable
                .showImageOnFail(R.drawable.polyv_avatar_def) // resource or
                // drawable
                .bitmapConfig(Config.RGB_565).cacheInMemory(true).cacheOnDisk(true)
                .displayer(new PolyvRoundDisplayerUtils(0)).build();
        this.textImageLoader = new PolyvTextImageLoader(context);
    }

    public void setDanmuFragment(PolyvDanmuFragment danmuFragment) {
        this.danmuFragment = danmuFragment;
    }

    public void add(PolyvChatMessage message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    public PolyvChatMessage remove(int position) {
        PolyvChatMessage chatMessage = messages.remove(position);
        notifyDataSetChanged();
        return chatMessage;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getChatType();
    }

    private View getChatView(int position) {
        return getItemViewType(position) == PolyvChatMessage.CHATTYPE_RECEIVE
                ? inflater.inflate(R.layout.polyv_listivew_chat_receive, null)
                : inflater.inflate(R.layout.polyv_listview_chat_send, null);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final PolyvChatMessage message = messages.get(position);
        final PolyvChatMessage.User user = message.getUser();
        if (convertView == null)
            convertView = getChatView(position);
        final LinearLayout ll_parent = PolyvViewHolder.get(convertView, R.id.ll_parent);
        final ImageView iv_resend = PolyvViewHolder.get(convertView, R.id.iv_resend);
        final ImageView iv_avatar = PolyvViewHolder.get(convertView, R.id.iv_avatar);
        final GifSpanTextView tv_msg = PolyvViewHolder.get(convertView, R.id.tv_msg);
        final TextView tv_time = PolyvViewHolder.get(convertView, R.id.tv_time);
        final TextView tv_name = PolyvViewHolder.get(convertView, R.id.tv_name);
        ll_parent.setOnClickListener(this);
        textImageLoader.displayTextImage(message.getValues()[0], tv_msg);
        tv_time.setText(PolyvTimeUtils.friendlyTime(message.getTime()));
        if (message.isShowTime()) {
            tv_time.setVisibility(View.VISIBLE);
        }else{
            tv_time.setVisibility(View.GONE);
        }
        switch (message.getChatType()) {
            case PolyvChatMessage.CHATTYPE_RECEIVE:
                tv_name.setText(user.getNick());
                ImageLoader.getInstance().displayImage(user.getPic(), iv_avatar, options);
                break;

            case PolyvChatMessage.CHATTYPE_SEND:
                if (!message.isSendSuccess()) {
                    iv_resend.setVisibility(View.VISIBLE);
                }else{
                    iv_resend.setVisibility(View.GONE);
                }
                iv_resend.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        iv_resend.setVisibility(View.GONE);
                        if (PolyvChatManager.getInstance().sendChatMsg(message)) {
                            danmuFragment.sendDanmaku(message.getValues()[0]);
                            updateStatusView(true, true, position);
                        } else {
                            updateStatusView(false, true, position);
                        }
                    }
                });
                break;
        }
        return convertView;
    }

    /**
     * 发送信息所需的更新发送状态view
     *
     * @param isSendSuccess 是否发送成功
     * @param isReSend      是否是重发
     * @param position      该信息的索引
     */
    public void updateStatusView(boolean isSendSuccess, boolean isReSend, int position) {
        View view = lv_chat.getChildAt(position - lv_chat.getFirstVisiblePosition());
        if (view != null) {
            if (isSendSuccess) {
                if (isReSend && messages.size() > 1) {
                    // 更新到最新
                    messages.add(remove(position));
                }
            } else {
                ImageView iv_resend = (ImageView) view.findViewById(R.id.iv_resend);
                iv_resend.setVisibility(View.VISIBLE);
            }
        }
    }

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        public void onClick(View view);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_parent:
                if (listener != null)
                    listener.onClick(v);
                break;
        }
    }
}
