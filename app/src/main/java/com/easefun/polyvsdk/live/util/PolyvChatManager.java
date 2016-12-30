package com.easefun.polyvsdk.live.util;

import com.easefun.polyvsdk.live.bean.PolyvChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.IO.Options;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * 聊天室管理类
 */
public class PolyvChatManager {
    private static final String TAG = PolyvChatManager.class.getSimpleName();
    private static final String CHAT_SERVER_URL = "http://chat.polyv.net:8000";
    private static PolyvChatManager chatManager;
    private ConnectStatus connect_status = ConnectStatus.DISCONNECT;
    private String loginJson;
    private Socket socket;
    private String roomId;

    public enum ConnectStatus {
        DISCONNECT("未连接"), LOGINING("登录中"), LOGINSUCCESS("登录成功"), RECONNECTING("重连中"), RECONNECTSUCCESS("重连成功");
        private final String describe;

        private ConnectStatus(String describe) {
            this.describe = describe;
        }

        public String getDescribe() {
            return describe;
        }
    }

    private PolyvChatManager() {
        try {
//            Options options = new Options();
//            options.query = "token=logintoken_541ab304cb88abc0bb5279762a063c09";
//            options.transports = new String[]{"websocket"};
            socket = IO.socket(CHAT_SERVER_URL);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public ConnectStatus getConnectStatus() {
        return connect_status;
    }

    /** 发送信息至聊天室 */
    public boolean sendChatMsg(PolyvChatMessage chatMessage) {
        if (connect_status == ConnectStatus.LOGINSUCCESS || connect_status == ConnectStatus.RECONNECTSUCCESS) {
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            try {
                jsonObject.put("EVENT", "SPEAK");
                jsonArray.put(0, chatMessage.getValues()[0].replaceAll("\n", "\\\\n").replaceAll("\"", "''"));
                jsonObject.put("values", jsonArray);
                jsonObject.put("roomId", roomId);
            } catch (JSONException e) {
                return false;
            }
            long sendTime = System.currentTimeMillis();
            socket.emit("message", jsonObject.toString());
            chatMessage.setTime(sendTime);
            chatMessage.setSendSuccess(true);
            return true;
        }
        return false;
    }

    public static PolyvChatManager getInstance() {
        if (chatManager == null) {
            synchronized (PolyvChatManager.class) {
                if (chatManager == null)
                    chatManager = new PolyvChatManager();
            }
        }
        return chatManager;
    }

    /** 退出聊天室 */
    public void disconnect() {
        connect_status = ConnectStatus.DISCONNECT;
        PolyvChatMessage.resetLastTime();
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnect);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.off("message", onNewMessage);
    }

    /**
     * 登录聊天室
     * @param userId 用户id
     * @param roomId 频道id
     * @param nickName 昵称
     */
    public void login(String userId, String roomId, String nickName) {
        this.roomId = roomId;
        if (connect_status == ConnectStatus.DISCONNECT) {
            connect_status = ConnectStatus.LOGINING;
            callConnectStatus(connect_status);
        }
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            jsonObject.put("EVENT", "LOGIN");
            jsonArray.put(0, nickName);
            jsonArray.put(1, "http://www.polyv.net/images/effect/effect-device.png");
            jsonArray.put(2, userId);
            jsonObject.put("values", jsonArray);
            jsonObject.put("roomId", roomId);
            jsonObject.put("type", "slice");
            loginJson = jsonObject.toString();
        } catch (JSONException e) {
            return;
        }
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on("message", onNewMessage);
        socket.connect();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            socket.emit("message", loginJson);
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            // 已连接后，断开连接会触发一次
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if (connect_status == ConnectStatus.DISCONNECT) {
                connect_status = ConnectStatus.LOGINING;
                callConnectStatus(connect_status);
            } else if (connect_status != ConnectStatus.RECONNECTING && connect_status != ConnectStatus.LOGINING) {
                connect_status = ConnectStatus.RECONNECTING;
                callConnectStatus(connect_status);
            }
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            String newMessage = (String) args[0];
            try {
                JSONObject jsonObject = new JSONObject(newMessage);
                String event = jsonObject.optString("EVENT");
                if (event.equals("onSliceID")) {
                    if (connect_status == ConnectStatus.DISCONNECT || connect_status == ConnectStatus.LOGINING) {
                        connect_status = ConnectStatus.LOGINSUCCESS;
                        callConnectStatus(connect_status);
                    } else if (connect_status == ConnectStatus.RECONNECTING) {
                        connect_status = ConnectStatus.RECONNECTSUCCESS;
                        callConnectStatus(connect_status);
                    }
                } else if (event.equals("SPEAK")) {
                    callReceiveChatMessage(PolyvChatMessage.fromJsonObject(jsonObject));
                }
            } catch (JSONException e) {
                // ignore
            }
        }
    };

    private void callConnectStatus(ConnectStatus connect_status) {
        if (chatManagerListener != null)
            chatManagerListener.connectStatus(connect_status);
    }

    private void callReceiveChatMessage(PolyvChatMessage chatMessage) {
        if (chatManagerListener != null && chatMessage.getValues() != null)
            chatManagerListener.receiveChatMessage(chatMessage);
    }

    private ChatManagerListener chatManagerListener;

    public interface ChatManagerListener {
        public void connectStatus(ConnectStatus connect_status);

        public void receiveChatMessage(PolyvChatMessage chatMessage);
    }

    public void setOnChatManagerListener(ChatManagerListener chatManagerListener) {
        this.chatManagerListener = chatManagerListener;
    }
}
