package com.easefun.polyvsdk.live.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import java.util.HashMap;
import java.util.Map;

public class PolyvKickAssist {
    //当前进程记录用户是否被踢出聊天室，用户被踢后，不能观看直播及回放，退出app再进来恢复
    public static Map<String, Boolean> kickMap = new HashMap<>();

    public static void setKickValue(String channelId, boolean isKick) {
        kickMap.put(channelId, isKick);
    }

    public static boolean checkKickAndTips(String channelId, Activity activity) {
        if (kickMap.containsKey(channelId) && kickMap.get(channelId)) {
            showTipsDialog(activity);
            return true;
        }
        return false;
    }

    public static void showTipsDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("温馨提示")
                .setMessage("您未被授权观看本直播！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
}
