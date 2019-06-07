package com.easefun.polyvsdk.live.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.adapter.PolyvChatAdapter;
import com.easefun.polyvsdk.live.adapter.PolyvChatAdapter.OnItemClickListener;
import com.easefun.polyvsdk.live.adapter.PolyvEmoGridViewAdapter;
import com.easefun.polyvsdk.live.adapter.PolyvEmoPagerAdapter;
import com.easefun.polyvsdk.live.chat.PolyvChatManager;
import com.easefun.polyvsdk.live.chat.PolyvChatMessage;
import com.easefun.polyvsdk.live.chat.api.PolyvChatBadword;
import com.easefun.polyvsdk.live.chat.api.PolyvChatHistory;
import com.easefun.polyvsdk.live.chat.api.listener.PolyvChatBadwordListener;
import com.easefun.polyvsdk.live.chat.api.listener.PolyvChatHistoryListener;
import com.easefun.polyvsdk.live.util.PolyvFaceManager;
import com.easefun.polyvsdk.live.util.PolyvKickAssist;
import com.easefun.polyvsdk.live.view.PolyvLikeIconView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jp.wasabeef.glide.transformations.CropCircleTransformation;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifEditText;
import pl.droidsonroids.gif.GifImageSpan;
import pl.droidsonroids.gif.RelativeImageSpan;

/**
 * 聊天Fragment
 */
public class PolyvChatFragment extends Fragment implements OnClickListener {
    private boolean isInitialized;
    private static final int DISCONNECT = 5;
    private static final int RECEIVEMESSAGE = 6;
    private static final int LOGINING = 12;
    private static final int LOGINSUCCESS = 13;
    private static final int RECONNECTING = 19;
    private static final int RECONNECTSUCCESS = 30;
    private static final int GETHISTORYSUCCESS = 31;
    private static final int GETHISTORYFAIL = 32;
    // fragmentView
    private View view;
    // 聊天listView
    private ListView lv_chat;
    // 聊天listView的适配器
    private PolyvChatAdapter polyvChatAdapter;
    // 聊天信息列表集合
    private LinkedList<PolyvChatMessage> messages;

    //listview是否滚到最后
    private boolean isScrollEnd = true;
    //已看聊天信息的最后一个item
    private int lastPreviewItem = -1;
    // 聊天室状态
    private TextView tv_status, tv_read;
    // 信息编辑控件
    private GifEditText et_talk;
    // 聊天管理类
    private PolyvChatManager chatManager;
    // 登录聊天室的学员id，频道所属的用户id，频道id
    private String chatUserId, userId, channelId;
    private Animation collapseAnimation;

    // 点赞
    private PolyvLikeIconView liv_like;

    // 表情ViewPager
    private ViewPager vp_emo;
    // 表情的父布局
    private RelativeLayout rl_bot;
    // 表情页的下方圆点...，表情开关，发送按钮
    private ImageView iv_page1, iv_page2, iv_page3, iv_page4, iv_page5, iv_emoswitch, iv_send;
    // 表情的文本长度
    private int emoLength;
    // 列
    private int columns = 7;
    // 行
    private int rows = 3;
    // 页
    private int pages = 5;
    private PolyvTabViewPagerFragment viewPagerFragment;
    // 加载更多按钮
    private TextView tv_loadmore;
    // 获取聊天室历史信息接口
    private PolyvChatHistory chatHistory;
    // 获取严禁词列表接口
    private PolyvChatBadword chatBadword;
    // 获取的历史记录条数
    private int messageCount = 20;
    // 获取历史记录的次数
    private int count = 0;
    // 严禁词列表
    private List<String> badwords = new ArrayList<>();

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (getContext() == null)
                return;
            switch (msg.what) {
                case RECEIVEMESSAGE:
                    final PolyvChatMessage chatMessage = (PolyvChatMessage) msg.obj;
                    boolean syncAdd = true;
                    if (chatMessage.getChatType() == PolyvChatMessage.CHATTYPE_RECEIVE) { // 发言信息
                        viewPagerFragment.getDanmuFragment().sendDanmaku(chatMessage.getValues()[0]);
                        if (!isScrollEnd) {
                            tv_read.setText("有更多的信息，点击查看");
                            if (tv_read.getVisibility() == View.GONE) {
                                lastPreviewItem = lv_chat.getCount();
                                tv_read.setVisibility(View.VISIBLE);
                            }
                        }
                    } else if (chatMessage.getChatType() == PolyvChatMessage.CHATTYPE_RECEIVE_QUESTION) { // 提问/回复信息
                        if (PolyvChatMessage.EVENT_T_ANSWER.equals(chatMessage.getEvent())) {
                            if (chatMessage.getS_userId().equals(chatManager.getUserId()))
                                viewPagerFragment.getQuestionFragment().receiveAnswer(chatMessage);
                        }
                    } else if (chatMessage.getChatType() == PolyvChatMessage.CHATTYPE_RECEIVE_NOTICE) { // 其他的通知信息
                        switch (chatMessage.getEvent()) {
                            // 用户被踢，不能发送信息，再次连接聊天室可恢复
                            case PolyvChatMessage.EVENT_KICK:
                                String nick = chatMessage.getUser().getNick();
                                if (chatMessage.getUser().getUid().equals(chatManager.getUid())) {
                                    nick = nick + "(我)";
                                    viewPagerFragment.getOnlineListFragment().kickOrShield(true);
                                    //被踢之后不能再观看直播，需要退出app再次进入才行
                                    PolyvKickAssist.setKickValue(channelId, true);
                                    PolyvKickAssist.checkKickAndTips(channelId, getActivity());
                                }
                                // 这里需要自定义显示的信息
                                chatMessage.setValues(new String[]{nick + "被踢"});
                                break;
                            // 用户被禁言，不能发送信息，不能再次连接聊天室，需要在直播后台恢复
                            case PolyvChatMessage.EVENT_BANIP:
                                List<PolyvChatMessage.User> banLists = chatMessage.banLists;
                                StringBuilder stringBuilder = new StringBuilder();
                                for (PolyvChatMessage.User user : banLists) {
                                    String nickName = user.getNick();
                                    if (user.getUid().equals(chatManager.getUid())) {
                                        nickName = nickName + "(我)";
                                        viewPagerFragment.getOnlineListFragment().kickOrShield(false);
                                    }
                                    stringBuilder.append(nickName).append("、");
                                }
                                // 这里需要自定义显示的信息
                                chatMessage.setValues(new String[]{stringBuilder.substring(0, stringBuilder.length() - 1) + "被禁言"});
                                break;
                            // 取消禁言
                            case PolyvChatMessage.EVENT_UNSHIELD:
                                List<PolyvChatMessage.User> unBanLists = chatMessage.banLists;
                                StringBuilder stringBuilder1 = new StringBuilder();
                                for (PolyvChatMessage.User user : unBanLists) {
                                    String nickName = user.getNick();
                                    if (user.getUid().equals(chatManager.getUid())) {
                                        nickName = nickName + "(我)";
                                        viewPagerFragment.getOnlineListFragment().unshield();
                                    }
                                    stringBuilder1.append(nickName).append("、");
                                }
                                // 这里需要自定义显示的信息
                                chatMessage.setValues(new String[]{stringBuilder1.substring(0, stringBuilder1.length() - 1) + "被取消禁言"});
                                break;
                            // 聊天室关闭时，不能接收或发送信息
                            case PolyvChatMessage.EVENT_CLOSEROOM:
                                boolean isClose = chatMessage.getValue().isClosed();
                                if (isClose)
                                    chatMessage.setValues(new String[]{"聊天室关闭"});
                                else
                                    chatMessage.setValues(new String[]{"聊天室开启"});
                                break;
                            // 公告(这里实则是管理员的最后一次发言)
                            case PolyvChatMessage.EVENT_GONGGAO:
                                chatMessage.setValues(new String[]{"管理员发言：" + chatMessage.getContent()});
                                break;
                            // 公告
                            case PolyvChatMessage.EVENT_BULLETIN:
                                chatMessage.setValues(new String[]{"公告：" + chatMessage.getContent().replaceAll("<br/>", "\n")});
                                break;
                            // 送花事件
                            case PolyvChatMessage.EVENT_FLOWERS:
                                String sendNick = chatMessage.getUser().getNick();
                                SpannableStringBuilder span = new SpannableStringBuilder(sendNick + " 赠送了鲜花p");
                                Drawable drawable = getResources().getDrawable(R.drawable.polyv_gift_flower);
                                int textSize = (int) getResources().getDimension(R.dimen.tv_textsize);
                                drawable.setBounds(0, 0, textSize * 2, textSize * 2);
                                span.setSpan(new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER), span.length() - 1, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                chatMessage.setValues(new CharSequence[]{span});
                                break;
                            // 打赏事件
                            case PolyvChatMessage.EVENT_REWARD:
                                final PolyvChatMessage.Content content = chatMessage.getRewardContent();
                                final int textSize1 = (int) getResources().getDimension(R.dimen.tv_textsize);
                                // 现金打赏
                                if (content.isMoneyReward()) {
                                    SpannableStringBuilder span1 = new SpannableStringBuilder("p" + content.getUnick() + " 打赏了 " + content.getRewardContent() + "元");
                                    Drawable drawable1 = getResources().getDrawable(R.drawable.polyv_icon_money);
                                    drawable1.setBounds(0, 0, textSize1 * 2, textSize1 * 2);
                                    span1.setSpan(new RelativeImageSpan(drawable1, RelativeImageSpan.ALIGN_CENTER), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    chatMessage.setValues(new CharSequence[]{span1});
                                }// 道具打赏
                                else {
                                    syncAdd = false;
                                    Glide.with(getContext()).load(content.getGimg()).bitmapTransform(new CropCircleTransformation(getContext())).listener(new RequestListener<String, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                            return true;
                                        }

                                        @Override
                                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                            SpannableStringBuilder span = new SpannableStringBuilder(content.getUnick() + " 赠送了" + content.getRewardContent() + " p");
                                            resource.setBounds(0, 0, textSize1 * 2, textSize1 * 2);
                                            span.setSpan(new RelativeImageSpan(resource, RelativeImageSpan.ALIGN_CENTER), span.length() - 1, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                            chatMessage.setValues(new CharSequence[]{span});
                                            polyvChatAdapter.add(chatMessage);
                                            return true;
                                        }
                                    }).into(textSize1 * 2, textSize1 * 2);
                                }
                                break;
                            // 发送红包事件
                            case PolyvChatMessage.EVENT_REDPAPER:
                                String sendNick2 = chatMessage.getUser().getNick();
                                SpannableStringBuilder span2 = new SpannableStringBuilder(sendNick2 + " 发送了" + chatMessage.getNumber() + "个红包，赶紧上微信领取吧");
                                span2.setSpan(new ForegroundColorSpan(Color.rgb(255, 140, 0)), 0, span2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                chatMessage.setValues(new CharSequence[]{span2});
                                break;
                            // 接收红包事件
                            case PolyvChatMessage.EVENT_GET_REDPAPER:
                                String receiveNick = chatMessage.getUser().getNick();
                                chatMessage.setValues(new CharSequence[]{receiveNick + " 收到了" + chatMessage.getSenderNick() + "的1个红包"});
                                break;
                            // 点赞
                            case PolyvChatMessage.EVENT_LIKES:
//                                String sendNick3 = chatMessage.getUser().getNick();
//                                SpannableStringBuilder span3 = new SpannableStringBuilder(sendNick3 + "觉得主持人讲得很棒！");
//                                span3.setSpan(new ForegroundColorSpan(Color.rgb(255, 140, 0)), 0, sendNick3.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                                chatMessage.setValues(new CharSequence[]{span3});
                                liv_like.addLoveIcon();
                                break;
                            // 登录
                            case PolyvChatMessage.EVENT_LOGIN:
                                chatMessage.setValues(new String[]{"欢迎" + chatMessage.getUser().getNick() + "加入！"});
                                break;
                            // 移除某条聊天信息
                            case PolyvChatMessage.EVENT_REMOVE_CONTENT:
                                polyvChatAdapter.remove(chatMessage.getId());
                                break;
                            // 清空所有聊天信息
                            case PolyvChatMessage.EVENT_REMOVE_HISTORY:
                                chatHistory.shutdown();
                                tv_loadmore.setVisibility(View.GONE);
                                polyvChatAdapter.clear();
                                break;
                            // 自定义信息
                            case PolyvChatMessage.EVENT_CUSTOMER_MESSAGE:
                                String content1 = chatMessage.getContent();//文本内容，可能为空
                                String image = chatMessage.getImage();//图片url，可能为空
                                String showCusMessage = "自定义信息：";
                                if (!TextUtils.isEmpty(content1))
                                    showCusMessage += content1;
                                if (!TextUtils.isEmpty(image))
                                    showCusMessage += image;//这里只是显示image url文本，如果有image，请自行加载图片显示
                                chatMessage.setValues(new String[]{showCusMessage});
                                break;
                        }
                    }
                    if (syncAdd && chatMessage.getValues() != null)
                        polyvChatAdapter.add(chatMessage);
                    break;
                case DISCONNECT:
                    tv_status.setText("连接失败(" + ((PolyvChatManager.ConnectStatus) msg.obj).getDescribe() + ")");
                    tv_status.setVisibility(View.VISIBLE);
                    break;
                case LOGINING:
                    tv_status.setText("正在登录中...");
                    tv_status.setVisibility(View.VISIBLE);
                    break;
                case LOGINSUCCESS:
                    viewPagerFragment.getQuestionFragment().loginSuccess();
                    viewPagerFragment.getOnlineListFragment().loginSuccess();
                    tv_status.setText("登录成功");
                    tv_status.clearAnimation();
                    tv_status.startAnimation(collapseAnimation);
                    break;
                case RECONNECTING:
                    viewPagerFragment.getOnlineListFragment().reconnecting();
                    tv_status.setText("正在重连中...");
                    tv_status.setVisibility(View.VISIBLE);
                    break;
                case RECONNECTSUCCESS:
                    viewPagerFragment.getOnlineListFragment().reconnectSuccess();
                    tv_status.setText("重连成功");
                    tv_status.clearAnimation();
                    tv_status.startAnimation(collapseAnimation);
                    break;
                case GETHISTORYSUCCESS:
                    final List<PolyvChatMessage> lists = (List<PolyvChatMessage>) msg.obj;
                    if (lists.size() == messageCount) {
                        tv_loadmore.setText("加载更多...");
                        tv_loadmore.setTextColor(getResources().getColor(R.color.center_view_color_blue));
                    } else
                        tv_loadmore.setVisibility(View.GONE);
                    if (count == 1 && lists.size() > 0) {
                        // 添加一条以上是历史消息的信息
                        String emptyMsg = "────────";
                        SpannableStringBuilder span = new SpannableStringBuilder(emptyMsg + " 以上是历史消息 " + emptyMsg);
                        PolyvChatMessage historyMsg = new PolyvChatMessage("", "", "", 0, null, null, new CharSequence[]{span});
                        historyMsg.setChatType(PolyvChatMessage.CHATTYPE_RECEIVE_NOTICE);
                        lists.add(historyMsg);
                    }
                    polyvChatAdapter.addAll(lists);
                    if (count == 1 && lists.size() > 0 && messages.size() > 0)
                        lv_chat.smoothScrollToPosition(messages.size() - 1);
                    else
                        lv_chat.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (lists.size() > 0 && messages.size() > 0) {
                                    lv_chat.setSelection(0);
                                    isScrollEnd = false;
                                }
                            }
                        }, 300);
                    break;
                case GETHISTORYFAIL:
                    count--;
                    Toast.makeText(getContext(), "加载历史信息失败\n" + msg.obj + "-" + msg.arg1, Toast.LENGTH_SHORT).show();
                    tv_loadmore.setText("加载更多...");
                    tv_loadmore.setTextColor(getResources().getColor(R.color.center_view_color_blue));
                    break;
            }
        }
    };

    /**
     * 初始化聊天室的配置
     *
     * @param chatManager 聊天室实例
     * @param chatUserId  登录聊天室的学员id(注：相同chatUserId之间不会收到对方的信息)
     * @param userId      频道所属的用户id
     * @param channelId   频道id
     */
    public void initChatConfig(@NonNull PolyvChatManager chatManager, String chatUserId, String userId, String channelId) {
        this.chatUserId = chatUserId;
        this.userId = userId;
        this.channelId = channelId;
        this.chatManager = chatManager;
        this.chatManager.setOnChatManagerListener(new PolyvChatManager.ChatManagerListener() {

            @Override
            public void connectStatus(PolyvChatManager.ConnectStatus connect_status) {
                switch (connect_status) {
                    case DISCONNECT:
                        handler.sendMessage(handler.obtainMessage(DISCONNECT, connect_status.DISCONNECT));
                        break;
                    case LOGINING:
                        handler.sendEmptyMessage(LOGINING);
                        break;
                    case LOGINSUCCESS:
                        handler.sendEmptyMessage(LOGINSUCCESS);
                        break;
                    case RECONNECTING:
                        handler.sendEmptyMessage(RECONNECTING);
                        break;
                    case RECONNECTSUCCESS:
                        handler.sendEmptyMessage(RECONNECTSUCCESS);
                        break;
                }
            }

            @Override
            public void receiveChatMessage(PolyvChatMessage chatMessage) {
                Message message = handler.obtainMessage();
                message.obj = chatMessage;
                message.what = RECEIVEMESSAGE;
                handler.sendMessage(message);
            }
        });
    }

    // 获取聊天室管理类
    public PolyvChatManager getChatManager() {
        return this.chatManager;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null)
            view = inflater.inflate(R.layout.polyv_fragment_chat, null);
        return view;
    }

    //根据索引获取表情页的view
    private View getEmoGridView(int position) {
        GridView gv_emo = (GridView) LayoutInflater.from(getContext()).inflate(R.layout.polyv_gridview_emo, null)
                .findViewById(R.id.gv_emo);
        List<String> lists = new ArrayList<String>();
        lists.addAll(PolyvFaceManager.getInstance().getFaceMap().keySet());
        final List<String> elists = lists.subList(position * (columns * rows), (position + 1) * (columns * rows));
        PolyvEmoGridViewAdapter emoGridViewAdapter = new PolyvEmoGridViewAdapter(elists, getContext());
        gv_emo.setAdapter(emoGridViewAdapter);
        gv_emo.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (position == elists.size() - 1)
                    deleteEmoText();
                else
                    appendEmo(elists.get(position));
            }
        });
        return gv_emo;
    }

    private void findIdAndNew() {
        lv_chat = (ListView) view.findViewById(R.id.lv_chat);
        tv_status = (TextView) view.findViewById(R.id.tv_status);
        tv_read = (TextView) view.findViewById(R.id.tv_read);
        iv_send = (ImageView) view.findViewById(R.id.iv_send);
        et_talk = (GifEditText) view.findViewById(R.id.et_talk);
        vp_emo = (ViewPager) view.findViewById(R.id.vp_emo);
        iv_page1 = (ImageView) view.findViewById(R.id.iv_page1);
        iv_page2 = (ImageView) view.findViewById(R.id.iv_page2);
        iv_page3 = (ImageView) view.findViewById(R.id.iv_page3);
        iv_page4 = (ImageView) view.findViewById(R.id.iv_page4);
        iv_page5 = (ImageView) view.findViewById(R.id.iv_page5);
        iv_emoswitch = (ImageView) view.findViewById(R.id.iv_emoswitch);
        rl_bot = (RelativeLayout) view.findViewById(R.id.rl_bot);
        tv_loadmore = (TextView) view.findViewById(R.id.tv_loadmore);
        liv_like = (PolyvLikeIconView) view.findViewById(R.id.liv_like);
        chatHistory = new PolyvChatHistory();
        chatBadword = new PolyvChatBadword();
        viewPagerFragment = (PolyvTabViewPagerFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fl_tag_viewpager);
        messages = new LinkedList<>();
        collapseAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.polyv_collapse);
        collapseAnimation.setAnimationListener(new ViewAnimationListener(tv_status));
    }

    private class ViewAnimationListener implements AnimationListener {
        private View view;

        public ViewAnimationListener(View view) {
            this.view = view;
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            view.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    // 删除表情
    private void deleteEmoText() {
        int start = et_talk.getSelectionStart();
        int end = et_talk.getSelectionEnd();
        if (end > 0) {
            if (start != end) {
                et_talk.getText().delete(start, end);
            } else if (isEmo(end)) {
                et_talk.getText().delete(end - emoLength, end);
            } else {
                et_talk.getText().delete(end - 1, end);
            }
        }
    }

    //判断是否是表情
    private boolean isEmo(int end) {
        String preMsg = et_talk.getText().subSequence(0, end).toString();
        int regEnd = preMsg.lastIndexOf("]");
        int regStart = preMsg.lastIndexOf("[");
        if (regEnd == end - 1 && regEnd - regStart >= 2) {
            String regex = preMsg.substring(regStart);
            emoLength = regex.length();
            if (PolyvFaceManager.getInstance().getFaceId(regex) != -1)
                return true;
        }
        return false;
    }

    //添加表情
    private void appendEmo(String emoKey) {
        SpannableStringBuilder span = new SpannableStringBuilder(emoKey);
        int textSize = (int) et_talk.getTextSize();
        Drawable drawable = null;
        ImageSpan imageSpan = null;
        try {
            drawable = new GifDrawable(getResources(), PolyvFaceManager.getInstance().getFaceId(emoKey));
            imageSpan = new GifImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
        } catch (NotFoundException | IOException e) {
            drawable = getResources().getDrawable(PolyvFaceManager.getInstance().getFaceId(emoKey));
            imageSpan = new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
        }
        drawable.setBounds(0, 0, (int) (textSize * 1.5), (int) (textSize * 1.5));
        span.setSpan(imageSpan, 0, span.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        int selectionStart = et_talk.getSelectionStart();
        int selectionEnd = et_talk.getSelectionEnd();
        if (selectionStart != selectionEnd)
            et_talk.getText().replace(selectionStart, selectionEnd, span);
        else
            et_talk.getText().insert(selectionStart, span);
    }

    // 获取严禁词
    public List<String> getBadwords() {
        return badwords;
    }

    // 添加严禁词
    private void addBadwords() {
        // 添加默认的严禁词
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream in = getActivity().getResources().openRawResource(R.raw.default_badword);
                byte[] buf = new byte[1024];
                int len = 0;
                StringBuilder stringBuilder = new StringBuilder();
                try {
                    while ((len = in.read(buf)) != -1) {
                        stringBuilder.append(new String(buf, 0, len));
                    }
                    String[] badwordArr = stringBuilder.toString().substring(1, stringBuilder.length() - 2).replaceAll("\"", "").split(",");
                    for (int i = 0; i < badwordArr.length; i++) {
                        if (!badwordArr[i].trim().equals(""))
                            badwords.add(badwordArr[i]);
                    }
                } catch (IOException e) {
                }
            }
        }).start();
        // 添加后台设置的严禁词
        chatBadword.getBadwordList(userId, channelId, Integer.MAX_VALUE, new PolyvChatBadwordListener() {
            @Override
            public void success(List<String> lists) {
                badwords.addAll(lists);
            }

            @Override
            public void fail(String failTips, int code) {
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void initView() {
        // 添加严禁词
        addBadwords();
        // 获取聊天室历史信息
        loadMoreChatMessage(channelId, chatUserId);
        polyvChatAdapter = new PolyvChatAdapter(getContext(), messages, lv_chat);
        polyvChatAdapter.setResendType(true);
        polyvChatAdapter.setChatManager(chatManager);
        polyvChatAdapter.setViewPagerFragment(viewPagerFragment);
        polyvChatAdapter.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onClick(View view) {
                closeKeybordAndEmo(et_talk, getContext());
            }
        });
        lv_chat.setAdapter(polyvChatAdapter);
        lv_chat.setOnScrollListener(new AbsListView.OnScrollListener() {
            int height;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        isScrollEnd = false;
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        if (lv_chat.getLastVisiblePosition() == (lv_chat.getCount() - 1)) {
                            View lastView = lv_chat.getChildAt(lv_chat.getLastVisiblePosition() - lv_chat.getFirstVisiblePosition());
                            isScrollEnd = lastView.getBottom() <= lv_chat.getHeight();
                        }
                        if (lv_chat.getLastVisiblePosition() >= lastPreviewItem) {
                            tv_read.setVisibility(View.GONE);
                            lastPreviewItem = -1;
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int itemsHeight = 0;
                for (int i = 0; i < visibleItemCount - firstVisibleItem; i++) {
                    itemsHeight += lv_chat.getChildAt(i).getMeasuredHeight();
                }
                if (itemsHeight > (height = Math.max(height, lv_chat.getMeasuredHeight()))) {
                    if (!lv_chat.isStackFromBottom()) {
                        lv_chat.setStackFromBottom(true);
                        lv_chat.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                polyvChatAdapter.notifyDataSetChanged();
                            }
                        }, 300);
                    }
                } else if (visibleItemCount == totalItemCount && lv_chat.isStackFromBottom()) {
                    lv_chat.setStackFromBottom(false);
                    lv_chat.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            polyvChatAdapter.notifyDataSetChanged();
                        }
                    }, 300);
                }
            }
        });
        lv_chat.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    closeKeybordAndEmo(et_talk, getContext());
                }
                return false;
            }
        });
        iv_send.setOnClickListener(this);
        // 表情
        List<View> lists = new ArrayList<>();
        for (int i = 0; i < pages; i++) {
            lists.add(getEmoGridView(i));
        }
        PolyvEmoPagerAdapter emoPagerAdapter = new PolyvEmoPagerAdapter(lists, getContext());
        vp_emo.setAdapter(emoPagerAdapter);
        vp_emo.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                resetPageImageView();
                switch (arg0) {
                    case 0:
                        iv_page1.setSelected(true);
                        break;
                    case 1:
                        iv_page2.setSelected(true);
                        break;
                    case 2:
                        iv_page3.setSelected(true);
                        break;
                    case 3:
                        iv_page4.setSelected(true);
                        break;
                    case 4:
                        iv_page5.setSelected(true);
                        break;
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        iv_page1.setSelected(true);
        iv_page1.setOnClickListener(this);
        iv_page2.setOnClickListener(this);
        iv_page3.setOnClickListener(this);
        iv_page4.setOnClickListener(this);
        iv_page5.setOnClickListener(this);
        iv_emoswitch.setOnClickListener(this);
        et_talk.setOnClickListener(this);
        tv_loadmore.setOnClickListener(this);
        tv_read.setOnClickListener(this);
        liv_like.setOnButtonClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                chatManager.sendLikes();
            }
        });
    }

    private void resetPageImageView() {
        iv_page1.setSelected(false);
        iv_page2.setSelected(false);
        iv_page3.setSelected(false);
        iv_page4.setSelected(false);
        iv_page5.setSelected(false);
    }

    /**
     * 重置表情布局的可见性
     *
     * @param isgone 以隐藏方式
     */
    public void resetEmoLayout(boolean isgone) {
        if (rl_bot.getVisibility() == View.VISIBLE || isgone) {
            rl_bot.setVisibility(View.GONE);
        } else {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            rl_bot.setVisibility(View.VISIBLE);
            closeKeybord(et_talk, getContext());
            et_talk.requestFocus();
        }
    }

    // 发送聊天信息
    private void sendMsg() {
        String msg = et_talk.getText().toString();
        if (msg.trim().length() == 0) {
            Toast.makeText(getContext(), "发送信息不能为空!", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < badwords.size(); i++) {
            if (msg.contains(badwords.get(i))) {
                Toast.makeText(getContext(), "您的聊天信息中含有违规词", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        final PolyvChatMessage message = new PolyvChatMessage(msg);
        polyvChatAdapter.add(message);
        final int lastPosition = polyvChatAdapter.getCount() - 1;
        if (chatManager.sendChatMessage(message)) {
            viewPagerFragment.getDanmuFragment().sendDanmaku(message.getValues()[0]);
            polyvChatAdapter.updateStatusView(true, false, lastPosition);
        } else {
            polyvChatAdapter.updateStatusView(false, false, lastPosition);
        }
        lv_chat.setSelection(lastPosition);
        tv_read.setVisibility(View.GONE);
        lastPreviewItem = -1;
        isScrollEnd = true;
        et_talk.setText("");
        closeKeybordAndEmo(et_talk, getContext());
    }

    //关闭键盘和表情布局
    private void closeKeybordAndEmo(EditText mEditText, Context mContext) {
        closeKeybord(mEditText, mContext);
        resetEmoLayout(true);
    }

    //关闭键盘
    private void closeKeybord(EditText mEditText, Context mContext) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    //表情布局是否可见
    public boolean emoLayoutIsVisible() {
        return rl_bot.getVisibility() == View.VISIBLE && viewPagerFragment.getCurrentIndex() == 0;
    }

    //加载聊天室的历史信息
    private void loadMoreChatMessage(String channelId, String chatUserId) {
        tv_loadmore.setText("加载中...");
        tv_loadmore.setTextColor(getResources().getColor(R.color.bottom_et_color_gray));
        count++;
        chatHistory.getChatHistory(channelId, chatUserId, count * messageCount - messageCount, count * messageCount - 1, new PolyvChatHistoryListener() {
            @Override
            public void success(List<PolyvChatMessage> lists) {
                Message message = handler.obtainMessage(GETHISTORYSUCCESS);
                message.obj = lists;
                handler.sendMessage(message);
            }

            @Override
            public void fail(String failTips, int code) {
                Message message = handler.obtainMessage(GETHISTORYFAIL);
                message.obj = failTips;
                message.arg1 = code;
                handler.sendMessage(message);
            }
        });
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (!isInitialized) {
            isInitialized = true;
            findIdAndNew();
            initView();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            closeKeybordAndEmo(et_talk, getContext());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 中断请求
        chatHistory.shutdown();
        chatBadword.shutdown();
    }

    @Override
    public void onStop() {
        super.onStop();
        // 隐藏表情布局
        resetEmoLayout(true);
        // 清除焦点
        et_talk.clearFocus();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_send:
                sendMsg();
                break;
            case R.id.iv_page1:
                vp_emo.setCurrentItem(0);
                break;
            case R.id.iv_page2:
                vp_emo.setCurrentItem(1);
                break;
            case R.id.iv_page3:
                vp_emo.setCurrentItem(2);
                break;
            case R.id.iv_page4:
                vp_emo.setCurrentItem(3);
                break;
            case R.id.iv_page5:
                vp_emo.setCurrentItem(4);
                break;
            case R.id.iv_emoswitch:
                resetEmoLayout(false);
                break;
            case R.id.et_talk:
                resetEmoLayout(true);
                break;
            case R.id.tv_loadmore:
                if ("加载更多...".equals(tv_loadmore.getText()))
                    loadMoreChatMessage(channelId, chatUserId);
                break;
            case R.id.tv_read:
                tv_read.setVisibility(View.GONE);
                lastPreviewItem = -1;
                lv_chat.setSelection(lv_chat.getCount() - 1);
                isScrollEnd = true;
        }
    }
}
