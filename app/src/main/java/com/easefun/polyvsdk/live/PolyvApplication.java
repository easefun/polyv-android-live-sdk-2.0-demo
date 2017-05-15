package com.easefun.polyvsdk.live;

import android.app.Application;

import com.easefun.polyvsdk.live.chat.PolyvChatManager;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class PolyvApplication extends Application {
    /**
     * 登录聊天室/ppt直播所需，请填写自己的appId和appSecret，否则无法登陆
     * appId和appSecret在直播系统管理后台的用户信息页的API设置中用获取
     */
    private static final String appId = "";
    private static final String appSecret = "";

    @Override
    public void onCreate() {
        super.onCreate();
        // 创建默认的ImageLoader配置参数
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(configuration);
        initPolyvCilent();
        initPolyvChatConfig();
    }

    /**
     * 初始化聊天室配置
     */
    public void initPolyvChatConfig() {
        PolyvChatManager.initConfig(appId, appSecret);
    }

    public void initPolyvCilent() {
        PolyvLiveSDKClient client = PolyvLiveSDKClient.getInstance();
        //启动Bugly
        //client.initCrashReport(getApplicationContext());
        //启动Bugly后，在学员登录时设置学员id
        //client.crashReportSetUserId(userId);
    }
}
