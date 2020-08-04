# 推荐使用新版[云课堂SDK](https://github.com/polyv/polyv-android-cloudClass-sdk-demo)，兼容纯视频和文档视频两种直播场景！
polyv-android-live-sdk-2.0-demo
===
[![build passing](https://img.shields.io/badge/build-passing-brightgreen.svg)](#)
[![GitHub release](https://img.shields.io/badge/release-v2.7.1-blue.svg)](https://github.com/easefun/polyv-android-live-sdk-2.0-demo/releases/tag/v2.7.1)
#### _polyv-android-live-sdk-2.0_（以下简称**liveSDK2.0**）是什么？
liveSDK2.0是Polyv为开发者用户提供的直播观看SDK ，是jar文件。易于集成，内部包含`直播播放` `聊天室`功能。首先需要在[Polyv官网](http://www.polyv.net)注册账户并开通直播功能，然后集成LiveSDK2.0到你的项目中。
#### _polyv-android-live-sdk-2.0-demo_（以下简称**liveSDK2.0demo**）是什么？
liveSDK2.0demo是liveSDK2.0的demo示例Android studio项目工程，其中包含了最新liveSDK2.0并且演示了如何在项目中集成liveSDK2.0。
***
#### 运行环境
* JDK 1.7 或以上
* Android SDK 14 或以上
* Android Studio 2.2.0 或以上
***
#### 支持功能
* 普通功能
  * 播放
  * 暂停
* 跑马灯
* 全屏
* 广告
* 秒播(图片广告，视频广告播放过程中提前加载直播)
* 手势滑动
  * 左向上
  * 左向下
  * 右向上
  * 右向下
  * 往左滑
  * 往右滑
* 获取播放时长
* 获取停留时长
* 开启关闭声音
* 声音调节
* 亮度调节
* 聊天室
***
### 如果未接触过_polyv-android-live-sdk-demo(1.0)_（以下简称**liveSDK1.0demo**）和_polyv-android-live-sdk(1.0)_（以下简称**liveSDK1.0**）可以跳过liveSDK1.0相关部分。

#### liveSDK2.0和liveSDK1.0是什么关系？
liveSDK2.0是liveSDK1.0的升级版，在liveSDK1.0的基础上，进行升级，重构，优化。
#### 如果你接触过liveSDK1.0，那么你可能想了解liveSDK2.0里面优化了什么。
* 点播逻辑和直播逻辑完全分离开来
  * 使SDK包体积更小
  * 使接口更加精简
  * 有独立的API文档
* 抽象接口方法
  * 提供接口源码，能在IDE中直接浏览接口描述
* 统一的监听事件类
  * 所有监听事件一目了然
* 全新优美的播放器界面。
* Android Studio项目。

#### 为什么要升级到liveSDK2.0
集成门槛和开发难度大大降低。
liveSDK1.0demo和liveSDK1.0不再进行功能更新，只维护bug。
#### 集成liveSDK2.0较liveSDK1.0最大的改变是什么？
* 第一点是由于直播观看逻辑和点播逻辑完全分离开，但又要兼容可以同时在项目中集成直播观看SDK和点播SDK，所以直播观看SDK的类名全部有变化，但是遵从一个原则，在liveSDK1.0类名前增加了PolyvLive的前缀。
* 第二点是所有监听回调统一由PolyvLiveVideoViewListener类定义，并且都在主线程中回调。
* 第三点是广告视频和直播视频使用不同的播放器进行播放，从而为实现视频预加载提供可能，因此增加了PolyvLiveAuxiliaryVideoView，用于播放广告视频。逻辑已全部由播放器控制。

更多细节请在liveSDK2.0 demo中查看。

***
#### 更多关于liveSDK2.0demo和liveSDK2.0的详细介绍请看[Wiki](https://github.com/easefun/polyv-android-live-sdk-2.0-demo/wiki)。
2.0.1版API文档请看[v2.0.1 API](http://repo.polyv.net/android/live/sdk/2.0.1/api/index.html)。<br/>
2.0.2版API文档请看[v2.0.2 API](http://repo.polyv.net/android/live/sdk/2.0.2/api/index.html)。<br/>
2.0.3版API文档请看[v2.0.3 API](http://repo.polyv.net/android/live/sdk/2.0.3/api/index.html)。<br/>
2.0.4版API文档请看[v2.0.4 API](http://repo.polyv.net/android/live/sdk/2.0.4/api/index.html)。<br/>
2.0.5版API文档请看[v2.0.5 API](http://repo.polyv.net/android/live/sdk/2.0.5/api/index.html)。<br/>
2.0.6版API文档请看[v2.0.6 API](http://repo.polyv.net/android/live/sdk/2.0.6/api/index.html)。<br/>
2.0.7版API文档请看[v2.0.7 API](http://repo.polyv.net/android/live/sdk/2.0.7/api/index.html)。<br/>
2.0.8版API文档请看[v2.0.8 API](http://repo.polyv.net/android/live/sdk/2.0.8/api/index.html)。<br/>
2.2.1版API文档请看[v2.2.1 API](http://repo.polyv.net/android/live/sdk/2.2.1/api/index.html)。<br/>
2.3.0版API文档请看[v2.2.1 API](http://repo.polyv.net/android/live/sdk/2.2.1/api/index.html)。<br/>
2.3.1版API文档请看[v2.3.1 API](http://repo.polyv.net/android/live/sdk/2.3.1/api/index.html)。<br/>
2.5.0版API文档请看[v2.5.0 API](http://repo.polyv.net/android/live/sdk/2.5.0/api/index.html)。<br/>
2.5.1版API文档请看[v2.5.1 API](http://repo.polyv.net/android/live/sdk/2.5.1/api/index.html)。<br/>
2.5.2版API文档请看[v2.5.2 API](http://repo.polyv.net/android/live/sdk/2.5.2/api/index.html)。<br/>
2.5.3版API文档请看[v2.5.3 API](http://repo.polyv.net/android/live/sdk/2.5.3/api/index.html)。<br/>
2.5.4版API文档请看[v2.5.4 API](http://repo.polyv.net/android/live/sdk/2.5.4/api/index.html)。<br/>
2.5.5版API文档请看[v2.5.5 API](http://repo.polyv.net/android/live/sdk/2.5.5/api/index.html)。<br/>
2.6.0版API文档请看[v2.6.0 API](http://repo.polyv.net/android/live/sdk/2.6.0/api/index.html)。<br/>
2.7.0版API文档请看[v2.7.0 API](http://repo.polyv.net/android/live/sdk/2.7.0/api/index.html)。<br/>
2.7.1版API文档请看[v2.7.1 API](http://repo.polyv.net/android/live/sdk/2.7.1/api/index.html)。


