package com.easefun.polyvsdk.live.util;

import android.app.Activity;
import android.graphics.Color;
import android.widget.Toast;

import com.easefun.polyvsdk.live.chat.util.Md5Util;
import com.easefun.polyvsdk.live.chat.util.NetUtilApiH1;
import com.easefun.polyvsdk.live.chat.util.NetUtilApiListener;
import com.easefun.polyvsdk.live.chat.util.NetUtilCallListener;
import com.easefun.polyvsdk.live.vo.PolyvLiveMarqueeVo;
import com.easefun.polyvsdk.marquee.PolyvMarqueeItem;

import org.json.JSONObject;

/**
 * 跑马灯工具类
 */
public class PolyvMarqueeUtils extends NetUtilApiH1 {
    // 请参考http://dev.polyv.net/2017/liveproduct/l-functionintro/page-setup/bfqsz/diy_marquee/，编写好自定义url之后再开启该功能。
    // 否则请不要开启，避免一些参数解析不正确，或者sign验证不通过。
    private boolean usediyurl = false;

    /**
     * 更新为后台设置的跑马灯类型
     *
     * @param activity    活动窗口
     * @param marqueeVo   跑马灯实例
     * @param marqueeItem 跑马灯item
     * @param channelId   频道号
     * @param userId      用户id
     * @param nickname    用户昵称
     */
    public void updateMarquee(final Activity activity, PolyvLiveMarqueeVo marqueeVo, final PolyvMarqueeItem marqueeItem, final String channelId, final String userId, String nickname) {
        marqueeItem.setStyle(PolyvMarqueeItem.STYLE_ROLL); //样式
        marqueeItem.setHasStroke(false); //描边
        marqueeItem.setDuration(10000); //时长
        marqueeItem.setText(""); //文本
        // 固定值类型
        if (PolyvLiveMarqueeVo.MARQUEETYPE_FIXED.equals(marqueeVo.marqueeType)) {
            setMarqueeParams(marqueeVo, marqueeItem);
            marqueeItem.setText(marqueeVo.marquee);
        } else if (PolyvLiveMarqueeVo.MARQUEETYPE_NICKNAME.equals(marqueeVo.marqueeType))/*用户名类型*/ {
            setMarqueeParams(marqueeVo, marqueeItem);
            marqueeItem.setText(nickname);
        } else if (PolyvLiveMarqueeVo.MARQUEETYPE_DIYURL.equals(marqueeVo.marqueeType))/*自定义url类型*/ {
            if (!usediyurl)
                return;
            final String code = "";
            final long time = System.currentTimeMillis();
            // 可以参考自行请求及解析 http://dev.polyv.net/2017/liveproduct/l-functionintro/page-setup/bfqsz/diy_marquee/
            String url = marqueeVo.marquee; // 自定义跑马灯的url
            String params = new StringBuilder("?vid=").append(channelId).append("&uid=").append(userId).append("&code=").append(code).append("&t=").append(time).toString();
            init(url + params, "GET", Integer.MAX_VALUE, false, true);
            getData(new NetUtilApiListener() {
                @Override
                public void fail(String failTips, int code) {
                    // 获取数据失败
                }
            }, new NetUtilCallListener() {
                @Override
                public void success(String data) throws Exception {
                    JSONObject jsonObject = new JSONObject(data);
                    final String show = jsonObject.optString("show");
                    String sign = jsonObject.optString("sign");
                    final String username = jsonObject.optString("username");
                    final String msg = jsonObject.optString("msg");
                    final String fontSize = jsonObject.optString("fontSize");
                    final String fontColor = jsonObject.optString("fontColor");
                    final String speed = jsonObject.optString("speed");
                    final String filter = jsonObject.optString("filter");
                    final String setting = jsonObject.optString("setting");
                    final String alpha = jsonObject.optString("alpha");
                    final String filterAlpha = jsonObject.optString("filterAlpha");
                    final String filterColor = jsonObject.optString("filterColor");
                    final String blurX = jsonObject.optString("blurX");
                    final String blurY = jsonObject.optString("blurY");
                    final String interval = jsonObject.optString("interval");
                    final String lifeTime = jsonObject.optString("lifeTime");
                    final String tweenTime = jsonObject.optString("tweenTime");
                    final String strength = jsonObject.optString("strength");
                    String str = new StringBuilder("vid=").append(channelId).append("&uid=").append(userId).append("&username=").append(username)
                            .append("&code=").append(code).append("&t=").append(time).append("&msg=").append(msg).append("&fontSize=").append(fontSize)
                            .append("&fontColor=").append(fontColor).append("&speed=").append(speed).append("&filter=").append(filter).append("&setting=").append(setting)
                            .append("&alpha=").append(alpha).append("&filterAlpha=").append(filterAlpha).append("&filterColor=").append(filterColor).append("&blurX=").append(blurX)
                            .append("&blurY=").append(blurY).append("&interval=").append(interval).append("&lifeTime=").append(lifeTime).append("&tweenTime=").append(tweenTime)
                            .append("&strength=").append(strength).append("&show=").append(show).toString();
                    String generateSign = Md5Util.getMd5(str).toLowerCase();
                    // 跑马灯sign验证失败
                    if (!sign.equals(generateSign)) {
                        if (activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, msg.equals("") ? "跑马灯验证失败" : msg, Toast.LENGTH_SHORT).show();
                                    activity.finish();
                                }
                            });
                        }
                        return;
                    }
                    if (activity != null)
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String tshow = show;
                                String tfontColor = fontColor;
                                String tfilterColor = filterColor;
                                String tfilter = filter;
                                // 默认值
                                if (tshow.equals(""))
                                    tshow = "on";
                                if (!tshow.equals("on"))
                                    return;
                                if (tfontColor.equals(""))
                                    tfontColor = "0x000000";
                                if (tfilterColor.equals(""))
                                    tfilterColor = "0x000000";
                                if (tfilter.equals(""))
                                    tfilter = "off";
                                int ifontSize = 30;
                                int ispeed = 200;
                                int isetting = 1;
                                float falpha = 1f;
                                float ffilterAlpha = 1f;
                                int iblurX = 2;
                                int iblurY = 2;
                                int iinterval = 5;
                                int ilifeTime = 3;
                                int itweenTime = 1;
                                int istrength = 4;
                                try {
                                    ifontSize = Integer.parseInt(fontSize);
                                } catch (Exception e) {
                                }
                                try {
                                    ispeed = Integer.parseInt(speed);
                                } catch (Exception e) {
                                }
                                try {
                                    isetting = Integer.parseInt(setting);
                                } catch (Exception e) {
                                }
                                try {
                                    falpha = Float.parseFloat(alpha);
                                } catch (Exception e) {
                                }
                                try {
                                    ffilterAlpha = Float.parseFloat(filterAlpha);
                                } catch (Exception e) {
                                }
                                try {
                                    iblurX = Integer.parseInt(blurX);
                                } catch (Exception e) {
                                }
                                try {
                                    iblurY = Integer.parseInt(blurY);
                                } catch (Exception e) {
                                }
                                try {
                                    iinterval = Integer.parseInt(interval);
                                } catch (Exception e) {
                                }
                                try {
                                    ilifeTime = Integer.parseInt(lifeTime);
                                } catch (Exception e) {
                                }
                                try {
                                    itweenTime = Integer.parseInt(tweenTime);
                                } catch (Exception e) {
                                }
                                try {
                                    istrength = Integer.parseInt(strength);
                                } catch (Exception e) {
                                }
                                switch (isetting) {
                                    case 1:
                                        marqueeItem.setStyle(PolyvMarqueeItem.STYLE_ROLL);
                                        break;
                                    case 2:
                                        marqueeItem.setStyle(PolyvMarqueeItem.STYLE_FLICK);
                                        break;
                                    case 3:
                                        marqueeItem.setStyle(PolyvMarqueeItem.STYLE_ROLL_FLICK);
                                        break;
                                    case 4:
                                        marqueeItem.setStyle(PolyvMarqueeItem.STYLE_ROLL_15PERCENT);
                                        break;
                                    case 5:
                                        marqueeItem.setStyle(PolyvMarqueeItem.STYLE_FLICK_15PERCENT);
                                        break;
                                }
                                marqueeItem.setText(username);
                                marqueeItem.setColor(Color.parseColor(tfontColor.replace("0x", "#")));
                                marqueeItem.setSize(Math.min(ifontSize, 66));
                                marqueeItem.setDuration(ispeed / 10 * 1000);
                                marqueeItem.setTextAlpha((int) (255 * falpha));
                                if (tfilter.equals("on")) {
                                    marqueeItem.setHasStroke(true);
                                    marqueeItem.setStrokeAlpha((int) (255 * ffilterAlpha));
                                    marqueeItem.setStrokeWidth(istrength);
                                    marqueeItem.setStrokeColor(Color.parseColor(tfilterColor.replace("0x", "#")));
                                    if (iblurX > 0 || iblurY > 0)
                                        marqueeItem.setBlurStroke(true);
                                }
                                marqueeItem.setInterval(iinterval * 1000);
                                marqueeItem.setLifeTime(ilifeTime * 1000);
                                marqueeItem.setTweenTime(itweenTime * 1000);
                            }
                        });
                }
            }, new int[]{1, 2, 3, 4, 5});
        }
    }

    /**
     * 设置跑马灯的属性
     *
     * @param marqueeVo
     */
    private static void setMarqueeParams(PolyvLiveMarqueeVo marqueeVo, PolyvMarqueeItem marqueeItem) {
        marqueeItem.setSize(Math.min(marqueeVo.marqueeFontSize, 66)); //字体大小
        marqueeItem.setColor(Color.parseColor(marqueeVo.marqueeFontColor)); //字体颜色
        float opacity = Float.parseFloat(marqueeVo.marqueeOpacity.replace("%", "")) * 0.01f;
        marqueeItem.setTextAlpha((int) (255 * opacity)); //文本透明度
    }
}
