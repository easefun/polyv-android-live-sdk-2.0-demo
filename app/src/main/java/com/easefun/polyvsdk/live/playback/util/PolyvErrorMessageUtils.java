package com.easefun.polyvsdk.live.playback.util;

import com.easefun.polyvsdk.video.PolyvPlayErrorReason;

/**
 * 错误类型转成错误信息工具类
 * @author Lion 2017-8-3
 */
public class PolyvErrorMessageUtils {

    /**
     * 获取播放错误信息
     * @param playErrorReason 播放错误类型
     * @return 错误信息字符串
     */
    public static String getPlayErrorMessage(@PolyvPlayErrorReason.PlayErrorReason int playErrorReason) {
        switch (playErrorReason) {
            case PolyvPlayErrorReason.NETWORK_DENIED:
                return "无法连接网络，请连接网络后播放";

            case PolyvPlayErrorReason.OUT_FLOW:
                return "流量超标，请向管理员反馈";

            case PolyvPlayErrorReason.TIMEOUT_FLOW:
                return "账号过期，请向管理员反馈";

            case PolyvPlayErrorReason.LOCAL_VIEWO_ERROR:
                return "本地视频文件损坏，请重新下载";

            case PolyvPlayErrorReason.START_ERROR:
                return "播放异常，请重新播放";

            case PolyvPlayErrorReason.NOT_PERMISSION:
                return "非法播放，请向管理员反馈";

            case PolyvPlayErrorReason.USER_TOKEN_ERROR:
                return "请先设置播放凭证，再进行播放";

            case PolyvPlayErrorReason.VIDEO_STATUS_ERROR:
                return "视频状态异常，无法播放，请向管理员反馈";

            case PolyvPlayErrorReason.VID_ERROR:
                return "视频id不正确，请设置正确的视频id进行播放";

            case PolyvPlayErrorReason.BITRATE_ERROR:
                return "清晰度不正确，请设置正确的清晰度进行播放";

            case PolyvPlayErrorReason.VIDEO_NULL:
                return "视频信息加载失败，请尝试切换网络重新播放";

            case PolyvPlayErrorReason.MP4_LINK_NUM_ERROR:
                return "当前视频无法播放，请向管理员反馈";

            case PolyvPlayErrorReason.M3U8_LINK_NUM_ERROR:
                return "当前视频无法播放，请向管理员反馈";

            case PolyvPlayErrorReason.HLS_SPEED_TYPE_NULL:
                return "播放速度不正确，请设置正确的播放速度进行播放";

            case PolyvPlayErrorReason.NOT_LOCAL_VIDEO:
                return "找不到缓存的视频文件，请连网后重新下载";

            case PolyvPlayErrorReason.HLS_15X_INDEX_EMPTY:
                return "当前视频无法播放，请向管理员反馈";

            case PolyvPlayErrorReason.HLS_15X_ERROR:
                return "当前视频无法播放，请向管理员反馈";

            case PolyvPlayErrorReason.HLS_15X_URL_ERROR:
                return "当前视频无法播放，请向管理员反馈";

            case PolyvPlayErrorReason.M3U8_15X_LINK_NUM_ERROR:
                return "当前视频无法播放，请向管理员反馈";

            case PolyvPlayErrorReason.CHANGE_EQUAL_BITRATE:
                return "切换清晰度相同，请选择其它清晰度";

            case PolyvPlayErrorReason.CHANGE_EQUAL_HLS_SPEED:
                return "切换播放速度相同，请选择其它播放速度";

            case PolyvPlayErrorReason.CAN_NOT_CHANGE_BITRATE:
                return "未开始播放视频不能切换清晰度，请先播放视频";

            case PolyvPlayErrorReason.CAN_NOT_CHANGE_HLS_SPEED:
                return "未开始播放视频不能切换播放速度，请先播放视频";

            case PolyvPlayErrorReason.QUESTION_ERROR:
                return "视频问答数据加载失败，请重新播放或者切换网络重新播放";

            case PolyvPlayErrorReason.CHANGE_BITRATE_NOT_EXIST:
                return "视频没有这个清晰度，请切换其它清晰度";

            case PolyvPlayErrorReason.HLS_URL_ERROR:
                return "当前视频无法播放，请向管理员反馈";

            case PolyvPlayErrorReason.LOADING_VIDEO_ERROR:
                return "当前视频无法播放，请向管理员反馈";

            case PolyvPlayErrorReason.HLS2_URL_ERROR:
                return "当前视频无法播放，请向管理员反馈";

            case PolyvPlayErrorReason.TOKEN_NULL:
                return "播放授权获取失败，请重新播放或者切换网络重新播放或者向管理员反馈";

            case PolyvPlayErrorReason.EXCEPTION_COMPLETION:
                return "视频异常结束，请重新播放或者向管理员反馈";

            case PolyvPlayErrorReason.WRITE_EXTERNAL_STORAGE_DENIED:
                return "检测到拒绝读取存储设备，请先为应用程序分配权限，再重新播放";

            case PolyvPlayErrorReason.SOURCE_URL_EMPTY:
                return "当前视频无法播放，请向管理员反馈";

            default:
                return "当前视频无法播放，请向管理员反馈";
        }
    }
}
