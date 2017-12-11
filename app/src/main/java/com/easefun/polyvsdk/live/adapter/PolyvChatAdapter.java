package com.easefun.polyvsdk.live.adapter;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.chat.PolyvChatManager;
import com.easefun.polyvsdk.live.chat.PolyvChatMessage;
import com.easefun.polyvsdk.live.fragment.PolyvTabViewPagerFragment;
import com.easefun.polyvsdk.live.util.PolyvRoundDisplayerUtils;
import com.easefun.polyvsdk.live.util.PolyvTextImageLoader;
import com.easefun.polyvsdk.live.util.PolyvTimeUtils;
import com.easefun.polyvsdk.live.util.PolyvViewHolder;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.ref.WeakReference;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import pl.droidsonroids.gif.GifSpanTextView;
import pl.droidsonroids.gif.RelativeImageSpan;

public class PolyvChatAdapter extends BaseAdapter implements OnClickListener {
    private List<PolyvChatMessage> messages;
    private LayoutInflater inflater;
    private ListView lv_chat;
    private DisplayImageOptions options;
    private PolyvTabViewPagerFragment viewPagerFragment;
    private PolyvChatManager chatManager;
    private PolyvTextImageLoader textImageLoader;
    private Context context;
    private BitmapDrawable bitmapDrawable;
    private CropCircleTransformation cropCircleTransformation;
    // 重发时，是发送聊天室还是发提问信息
    private boolean isResendChatMessage = true;

    public PolyvChatAdapter(Context context, List<PolyvChatMessage> messages, ListView lv_chat) {
        this.context = context;
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
        this.cropCircleTransformation = new CropCircleTransformation(context);
        this.bitmapDrawable = new BitmapDrawable(context.getResources());
    }

    public void setViewPagerFragment(PolyvTabViewPagerFragment viewPagerFragment) {
        this.viewPagerFragment = viewPagerFragment;
    }

    public void setChatManager(PolyvChatManager chatManager) {
        this.chatManager = chatManager;
    }

    public void setResendType(boolean isResendChatMessage) {
        this.isResendChatMessage = isResendChatMessage;
    }

    public void clear() {
        messages.clear();
        notifyDataSetChanged();
    }

    public void add(PolyvChatMessage message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    // 添加历史记录
    public void addAll(List<PolyvChatMessage> lists) {
        messages.addAll(0, lists);
        notifyDataSetChanged();
    }

    public PolyvChatMessage remove(int position) {
        PolyvChatMessage chatMessage = messages.remove(position);
        notifyDataSetChanged();
        return chatMessage;
    }

    /**
     * 根据聊天id移除聊天信息
     *
     * @param chatId
     */
    public PolyvChatMessage remove(String chatId) {
        PolyvChatMessage chatMessage = null;
        for (int i = 0; i < messages.size(); i++) {
            if (chatId.equals(messages.get(i).getId())) {
                chatMessage = messages.remove(i);
                break;
            }
        }
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
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getChatType();
    }

    private View getChatView(int position) {
        switch (getItemViewType(position)) {
            case PolyvChatMessage.CHATTYPE_RECEIVE:
                return inflater.inflate(R.layout.polyv_listivew_chat_receive, null);
            case PolyvChatMessage.CHATTYPE_RECEIVE_NOTICE:
                return inflater.inflate(R.layout.polyv_listview_chat_receive_notice, null);
            case PolyvChatMessage.CHATTYPE_SEND:
                return inflater.inflate(R.layout.polyv_listview_chat_send, null);
        }
        return null;
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
        final TextView tv_notice = PolyvViewHolder.get(convertView, R.id.tv_notice);
        ll_parent.setOnClickListener(this);
        if (message.getChatType() != PolyvChatMessage.CHATTYPE_RECEIVE_NOTICE) {
            textImageLoader.displayTextImage(message.getValues()[0], tv_msg);
            tv_time.setText(PolyvTimeUtils.friendlyTime(message.getTime()));
            if (message.isShowTime()) {
                tv_time.setVisibility(View.VISIBLE);
            } else {
                tv_time.setVisibility(View.GONE);
            }
        }
        switch (message.getChatType()) {
            // 其他用户发送的信息
            case PolyvChatMessage.CHATTYPE_RECEIVE:
                tv_name.setText(user.getNick());
                ImageLoader.getInstance().displayImage(user.getPic(), iv_avatar, options);
                break;
            // 聊天室的一些通知信息
            case PolyvChatMessage.CHATTYPE_RECEIVE_NOTICE:
                tv_notice.setTag(tv_notice);
                // 历史记录里的发送红包信息
                if (message.isHistorySendRedPaper()) {
                    String sendNick2 = message.getUser().getNick();
                    SpannableStringBuilder span2 = new SpannableStringBuilder(sendNick2 + " 发送了" + message.getNumber() + "个红包，赶紧上微信领取吧");
                    span2.setSpan(new ForegroundColorSpan(Color.rgb(255, 140, 0)), 0, span2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tv_notice.setText(span2);
                }// 历史记录里的接收红包信息
                else if (message.isHistoryGetRedPaper()) {
                    String receiveNick = message.getUser().getNick();
                    tv_notice.setText(receiveNick + " 收到了" + message.getSenderNick() + "的1个红包");
                }//历史记录里的打赏信息
                else if (message.isHistoryReward()) {
                    final PolyvChatMessage.Content content = message.getRewardContent();
                    final int textSize1 = (int) context.getResources().getDimension(R.dimen.tv_textsize);
                    // 现金打赏
                    if (content.isMoneyReward()) {
                        SpannableStringBuilder span1 = new SpannableStringBuilder("p" + content.getUnick() + " 打赏了 " + content.getRewardContent() + "元");
                        Drawable drawable1 = context.getResources().getDrawable(R.drawable.polyv_icon_money);
                        drawable1.setBounds(0, 0, textSize1 * 2, textSize1 * 2);
                        span1.setSpan(new RelativeImageSpan(drawable1, RelativeImageSpan.ALIGN_CENTER), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tv_notice.setText(span1);
                    }// 道具打赏
                    else {
                        String imgUrl = content.getGimg();
                        String space = "图片";
                        final SpannableStringBuilder span = new SpannableStringBuilder(content.getUnick() + " 赠送了" + content.getRewardContent() + " " + space);
                        bitmapDrawable.setBounds(0, 0, textSize1 * 2, textSize1 * 2);
                        span.setSpan(new RelativeImageSpan(bitmapDrawable, RelativeImageSpan.ALIGN_CENTER), span.length() - space.length(), span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        tv_notice.setTag(imgUrl);
                        tv_notice.setText(span);
                        Glide.with(context).load(imgUrl).bitmapTransform(cropCircleTransformation)
                                .listener(new MyRequestListener(span, tv_notice, textSize1, space.length(), imgUrl)).into(textSize1 * 2, textSize1 * 2);
                    }
                } else {
                    tv_notice.setText(message.getValues()[0]);
                }
                break;
            // 自己发送的信息
            case PolyvChatMessage.CHATTYPE_SEND:
                if (!message.isSendSuccess()) {
                    iv_resend.setVisibility(View.VISIBLE);
                } else {
                    iv_resend.setVisibility(View.GONE);
                }
                iv_resend.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        iv_resend.setVisibility(View.GONE);
                        if (isResendChatMessage) {
                            if (chatManager.sendChatMessage(message)) {
                                viewPagerFragment.getDanmuFragment().sendDanmaku(message.getValues()[0]);
                                updateStatusView(true, true, position);
                            } else {
                                updateStatusView(false, true, position);
                            }
                        } else {
                            if (chatManager.sendQuestionMsg(message)) {
                                updateStatusView(true, true, position);
                            } else {
                                updateStatusView(false, true, position);
                            }
                        }
                    }
                });
                break;
        }
        return convertView;
    }

    private static class MyRequestListener implements RequestListener<String, GlideDrawable> {
        private SpannableStringBuilder span;
        private WeakReference<TextView> wr_textView;
        private int textSize;
        private int perchLength;
        private String tag;

        public MyRequestListener(SpannableStringBuilder span, TextView textView, int textSize, int perchLength, String tag) {
            this.span = span;
            this.wr_textView = new WeakReference<TextView>(textView);
            this.textSize = textSize;
            this.perchLength = perchLength;
            this.tag = tag;
        }

        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
            return true;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            TextView textView = wr_textView.get();
            if (textView != null && textView.getTag() != null && textView.getTag().equals(tag)) {
                resource.setBounds(0, 0, textSize * 2, textSize * 2);
                span.setSpan(new RelativeImageSpan(resource, RelativeImageSpan.ALIGN_CENTER), span.length() - perchLength, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                textView.setText(span);
            }
            return true;
        }
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
