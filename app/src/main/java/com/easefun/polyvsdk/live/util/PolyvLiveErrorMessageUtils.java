package com.easefun.polyvsdk.live.util;

import android.support.annotation.NonNull;

import com.easefun.polyvsdk.live.video.PolyvLivePlayErrorReason;

/**
 * 错误类型转成错误信息工具类
 * @author Lionel 2019-6-3
 */
public class PolyvLiveErrorMessageUtils {

    /**
     * 获取播放错误信息
     * @param errorReason 播放错误
     * @return 错误信息字符串
     */
    @NonNull
    public static String getPlayErrorMessage(PolyvLivePlayErrorReason errorReason) {
        switch (errorReason.getType()) {
            case NETWORK_DENIED:
                return "无法连接网络，请连接网络后播放";

            case START_ERROR:
                return "播放错误，请重新播放";

            case CHANNEL_NULL:
                return "频道信息获取失败，请重新播放";

            case LIVE_UID_NOT_EQUAL:
                return "用户id错误，请重新设置";

            case LIVE_CID_NOT_EQUAL:
                return "频道号错误，请重新设置";

            case LIVE_PLAY_ERROR:
                return "播放错误，请稍后重试";

            case RESTRICT_NULL:
                return "限制信息错误，请稍后重试";

            case RESTRICT_ERROR:
                return errorReason.getErrorMsg();

            case APP_ID_EMPTY:
                return "appId错误，请设置";

            case APP_SECRET_EMPTY:
                return "appSecret错误，请设置";

            default:
                return "播放错误，请稍后重试";
        }
    }
}
