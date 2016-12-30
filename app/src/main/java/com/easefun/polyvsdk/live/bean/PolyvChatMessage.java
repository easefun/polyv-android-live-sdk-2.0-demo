package com.easefun.polyvsdk.live.bean;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Locale;

/**
 * 聊天信息实体类
 */
public class PolyvChatMessage {
    private String event;
    private String id;
    private long time;
    private User user;
    private String[] values;
    // 自定义字段
    public static final int CHATTYPE_SEND = 0;
    // <getViewTypeCount()
    public static final int CHATTYPE_RECEIVE = 1;
    private int chatType;
    private boolean isSendSuccess;
    private boolean isShowTime;
    // 由于服务端的时间可能与客户端不一致，所以这里分开发送信息的时间与接收信息的时间
    private static long sendLastTime;
    private static long receiveLastTime;

    /**
     * 重置最后发送/接收信息的时间
     */
    public static void resetLastTime() {
        sendLastTime = 0;
        receiveLastTime = 0;
    }

    /**
     * 发送信息的构造器
     */
    public PolyvChatMessage(String sendChatMsg) {
        this.values = new String[1];
        this.values[0] = sendChatMsg;
        this.chatType = CHATTYPE_SEND;
    }

    /**
     * 接收信息的构造器
     */
    public PolyvChatMessage(String event, String id, long time, User user, String[] values) {
        this.event = event;
        this.id = id;
        this.time = time;
        this.user = user;
        this.values = values;
        this.chatType = CHATTYPE_RECEIVE;
        // 由于时间是固定的，在这里初始化即可
        initIsShowTime(this.time, true);
    }

    /**
     * 初始化信息是否要显示时间
     */
    private void initIsShowTime(long time, boolean isReceiveLastTime) {
        if (!isReceiveLastTime) {
            if (sendLastTime != 0 && time - sendLastTime > 120000)
                this.isShowTime = true;
            sendLastTime = time;
        } else {
            if (receiveLastTime != 0 && time - receiveLastTime > 120000)
                this.isShowTime = true;
            receiveLastTime = time;
        }
    }

    public static PolyvChatMessage fromJsonObject(JSONObject jsonObject) {
        if (jsonObject != null) {
            String event = jsonObject.optString("EVENT");
            String id = jsonObject.optString("id");
            long time = jsonObject.optLong("time");
            User user = null;
            JSONObject jUser = jsonObject.optJSONObject("user");
            if (jUser != null) {
                String clientIp = jUser.optString("clientIp");
                String nick = jUser.optString("nick");
                String pic = jUser.optString("pic");
                if (!pic.toLowerCase(Locale.getDefault()).startsWith("http")) {
                    if (pic.startsWith("//")) {
                        pic = "http:" + pic;
                    }
                }
                String roomId = jUser.optString("roomId");
                String uid = jUser.optString("uid");
                String userId = jUser.optString("userId");
                String userType = jUser.optString("userType");
                user = new User(clientIp, nick, pic, roomId, uid, userId, userType);
            }
            String[] values = null;
            JSONArray jValues = jsonObject.optJSONArray("values");
            if (jValues != null && jValues.length() >= 0) {
                values = new String[jValues.length()];
                for (int i = 0; i < jValues.length(); i++) {
                    values[i] = jValues.optString(i);
                }
            }
            return new PolyvChatMessage(event, id, time, user, values);
        }
        return null;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
        // 发送成功并设置时间之后再初始化
        initIsShowTime(this.time, false);
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String[] getValues() {
        return values;
    }

    /**
     * 获取聊天信息的类型
     *
     * @return {@link #CHATTYPE_SEND}：发送，{@link #CHATTYPE_RECEIVE}：接收
     */
    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public boolean isSendSuccess() {
        return isSendSuccess;
    }

    public void setSendSuccess(boolean isSendSuccess) {
        this.isSendSuccess = isSendSuccess;
    }

    public boolean isShowTime() {
        return isShowTime;
    }

    public void setShowTime(boolean isShowTime) {
        this.isShowTime = isShowTime;
    }

    public static class User {
        private String clientIp;
        private String nick;
        private String pic;
        private String roomId;
        private String uid;
        private String userId;
        private String userType;

        public User(String clientIp, String nick, String pic, String roomId, String uid, String userId,
                    String userType) {
            this.clientIp = clientIp;
            this.nick = nick;
            this.pic = pic;
            this.roomId = roomId;
            this.uid = uid;
            this.userId = userId;
            this.userType = userType;
        }

        public String getClientIp() {
            return clientIp;
        }

        public void setClientIp(String clientIp) {
            this.clientIp = clientIp;
        }

        public String getNick() {
            return nick;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }

        public String getPic() {
            return pic;
        }

        public void setPic(String pic) {
            this.pic = pic;
        }

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        @Override
        public String toString() {
            return "User [clientIp=" + clientIp + ", nick=" + nick + ", pic=" + pic + ", roomId=" + roomId + ", uid="
                    + uid + ", userId=" + userId + ", userType=" + userType + "]";
        }
    }

    @Override
    public String toString() {
        return "PolyvChatMessage [event=" + event + ", id=" + id + ", time=" + time + ", user=" + user + ", values="
                + Arrays.toString(values) + ", chatType=" + chatType + ", isSendSuccess=" + isSendSuccess
                + ", isShowTime=" + isShowTime + "]";
    }
}
