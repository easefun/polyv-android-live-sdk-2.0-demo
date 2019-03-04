package com.easefun.polyvsdk.live.util;

import android.text.TextUtils;

import com.admaster.sdk.api.AdmasterSdk;
import com.easefun.polyvsdk.live.vo.PolyvLiveChannelVO;

public class AdmasterSdkUtils {
	public static final int MONITOR_SHOW = 0;
	public static final int MONITOR_CLICK = 1;

	/**
	 * 发送广告监测，需要在后台设置发送的地址
	 * 
	 * @param adMatter
	 * @param monitorType 0:曝光，1:点击
	 */
	public static void sendAdvertMonitor(PolyvLiveChannelVO.ADMatter adMatter, int monitorType) {
		switch (monitorType) {
		case MONITOR_SHOW:
			String advertShowUrl = null;
			if (adMatter.getLocation().equals(PolyvLiveChannelVO.ADMatter.LOCATION_FIRST)) {
				advertShowUrl = adMatter.getHeadAdvertShowUrl();
			} else if (adMatter.getLocation().equals(PolyvLiveChannelVO.ADMatter.LOCATION_PAUSE)) {
				advertShowUrl = adMatter.getStopAdvertShowUrl();
			}
			if (!TextUtils.isEmpty(advertShowUrl))
				AdmasterSdk.onExpose(advertShowUrl);
			break;

		case MONITOR_CLICK:
			String advertClickUrl = null;
			if (adMatter.getLocation().equals(PolyvLiveChannelVO.ADMatter.LOCATION_FIRST)) {
				advertClickUrl = adMatter.getHeadAdvertClickUrl();
			} else if (adMatter.getLocation().equals(PolyvLiveChannelVO.ADMatter.LOCATION_PAUSE)) {
				advertClickUrl = adMatter.getStopAdvertClickUrl();
			}
			if (!TextUtils.isEmpty(advertClickUrl))
				AdmasterSdk.onClick(advertClickUrl);
			break;
		}
	}
}
