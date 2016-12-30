package com.easefun.polyvsdk.live;

import android.app.Application;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class PolyvApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("tag", "应用初始化");
        // 创建默认的ImageLoader配置参数
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(configuration);
		initPolyvCilent();
    }

	public void initPolyvCilent() {
		PolyvLiveSDKClient client = PolyvLiveSDKClient.getInstance();
        //启动Bugly
		//client.initCrashReport(getApplicationContext());
        //启动Bugly后，在学员登录时设置学员id
        //client.crashReportSetUserId(userId);
	}
}
