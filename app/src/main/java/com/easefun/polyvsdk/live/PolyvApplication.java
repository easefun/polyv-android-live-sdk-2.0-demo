package com.easefun.polyvsdk.live;

import android.os.AsyncTask;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;

import com.easefun.polyvsdk.PolyvSDKClient;
import com.easefun.polyvsdk.PolyvSDKUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

//继承的类是为了解决64K 引用限制
public class PolyvApplication extends MultiDexApplication {
    /**
     * 初始化直播SDK，需要用到的appId。
     * appId可以在直播系统管理后台的用户信息页的API设置中获取。
     */
    public static final String appId = "";
    /**
     * 初始化直播SDK，需要用到的appSecret。
     * appSecret可以在直播系统管理后台的用户信息页的API设置中获取。
     */
    public static final String appSecret = "";

    @Override
    public void onCreate() {
        super.onCreate();
        // 创建默认的ImageLoader配置参数
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(configuration);

        initPolyvClient();
        initPolyvLiveClient();
    }

    /**
     * 初始化直播的配置
     */
    public void initPolyvLiveClient() {
//        // 初始化实例
//        PolyvLiveSDKClient.getInstance();
//        // 初始化聊天室配置
//        PolyvChatManager.initConfig(appId, appSecret);
//        // 初始化连麦配置
//        PolyvLinkMicManager.init(this);
        //上面三个初始化方法不再需要手动调用，在initSetting方法中会初始化。
        PolyvLiveSDKClient.getInstance().initSetting(getApplicationContext(), appId, appSecret);
    }

    //加密秘钥和加密向量，在点播后台->设置->API接口中获取，用于解密SDK加密串
    //值修改请参考https://github.com/easefun/polyv-android-sdk-demo/wiki/10.%E5%85%B3%E4%BA%8E-SDK%E5%8A%A0%E5%AF%86%E4%B8%B2-%E4%B8%8E-%E7%94%A8%E6%88%B7%E9%85%8D%E7%BD%AE%E4%BF%A1%E6%81%AF%E5%8A%A0%E5%AF%86%E4%BC%A0%E8%BE%93
    /** 加密秘钥，可以在点播后台获取 */
    private String aeskey = "VXtlHmwfS2oYm0CZ";
    /** 加密向量，可以在点播后台获取 */
    private String iv = "2u9gDPKdX6GyQJKU";
    /** SDK加密串，可以在点播后台获取 */
    private String config = "";

    /**
     * 初始化回放的配置
     */
    public void initPolyvClient() {
        //网络方式取得SDK加密串，（推荐）
        //网络获取到的SDK加密串可以保存在本地SharedPreference中，下次先从本地获取
//		new LoadConfigTask().execute();
        PolyvSDKClient client = PolyvSDKClient.getInstance();
        //使用SDK加密串来配置
        client.setConfig(config, aeskey, iv, getApplicationContext());
        //初始化SDK设置
        client.initSetting(getApplicationContext());
        //启动Bugly
        client.initCrashReport(getApplicationContext());
        //启动Bugly后，在学员登录时设置学员id
//		client.crashReportSetUserId(userId);
        //TODO 如果需要使用点播中的下载功能，还需要初始化下载目录，请看点播demo。地址：https://github.com/easefun/polyv-android-sdk-2.0-demo/releases
    }

    private class LoadConfigTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            String config = PolyvSDKUtil.getUrl2String("http://demo.polyv.net/demo/appkey.php");
            if (TextUtils.isEmpty(config)) {
                try {
                    throw new Exception("没有取到数据");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return config;
        }

        @Override
        protected void onPostExecute(String config) {
            PolyvSDKClient client = PolyvSDKClient.getInstance();
            client.setConfig(config, aeskey, iv);
        }
    }
}
