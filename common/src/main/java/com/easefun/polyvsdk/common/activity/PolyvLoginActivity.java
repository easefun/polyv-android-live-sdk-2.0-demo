package com.easefun.polyvsdk.common.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.easefun.polyvsdk.common.PolyvConstant;
import com.easefun.polyvsdk.common.R;
import com.easefun.polyvsdk.common.permission.PolyvPermission;
import com.easefun.polyvsdk.common.playback.activity.PolyvPPTPbPlayerActivity;
import com.easefun.polyvsdk.common.playback.activity.PolyvPbPlayerActivity;
import com.easefun.polyvsdk.live.chat.playback.api.PolyvLivePlayback;
import com.easefun.polyvsdk.live.chat.playback.api.PolyvLiveRestrict;
import com.easefun.polyvsdk.live.chat.playback.api.entity.PolyvLivePlaybackEntity;
import com.easefun.polyvsdk.live.chat.playback.api.listener.PolyvLivePlaybackListener;
import com.easefun.polyvsdk.live.chat.playback.api.listener.PolyvLiveRestrictListener;
import com.easefun.polyvsdk.common.view.PolyvPopupWindow;

/**
 * 登陆 activity
 */
public class PolyvLoginActivity extends Activity implements OnClickListener {
    private PolyvPopupWindow popupWindow;
    private RelativeLayout parent;
    private EditText et_userid, et_channelid;
    private TextView tv_login, tv_playtype;
    private ProgressDialog progress;
    private PolyvPermission polyvPermission = null;
    private PolyvLivePlayback livePlayback;
    private PolyvLiveRestrict liveRestrict;
    // 登录聊天室的参数
    // chatUserId可以使用学员的Id代替(注：相同的chatUserId不能相互接收对方的信息)
    private String chatUserId = Build.SERIAL;
    // 登录聊天室的昵称
    private String nickName = "游客" + Build.SERIAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.polyv_activity_login);
        findId();
        initView();

        polyvPermission = new PolyvPermission();
        polyvPermission.setResponseCallback(new PolyvPermission.ResponseCallback() {
            @Override
            public void callback() {
                gotoPlay();
            }
        });
    }

    private void findId() {
        et_userid = (EditText) findViewById(R.id.et_userid);
        et_channelid = (EditText) findViewById(R.id.et_channelid);
        tv_login = (TextView) findViewById(R.id.tv_login);
        tv_playtype = (TextView) findViewById(R.id.tv_playtype);
        parent = (RelativeLayout) findViewById(R.id.rl_parent);
    }

    private void initView() {
        livePlayback = new PolyvLivePlayback();
        liveRestrict = new PolyvLiveRestrict();
        progress = new ProgressDialog(this);
        progress.setMessage("正在登录中，请稍等...");
        progress.setCanceledOnTouchOutside(false);
        progress.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                liveRestrict.shutdown();
                livePlayback.shutdown();
            }
        });

        et_userid.requestFocus();
        et_userid.setText("");
        et_channelid.setText("");
        tv_login.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String userId = et_userid.getText().toString();
                if (TextUtils.isEmpty(userId)) {
                    Toast.makeText(PolyvLoginActivity.this, "请输入用户id", Toast.LENGTH_SHORT).show();
                    return;
                }

                String channelId = et_channelid.getText().toString();
                if (TextUtils.isEmpty(channelId)) {
                    Toast.makeText(PolyvLoginActivity.this, "请输入频道号", Toast.LENGTH_SHORT).show();
                    return;
                }

                polyvPermission.applyPermission(PolyvLoginActivity.this, PolyvPermission.OperationType.play);
            }
        });
        popupWindow = new PolyvPopupWindow(this);
        popupWindow.setOnButtonClickListener(this);
        tv_playtype.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
            }
        });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.tv_auto) {
            tv_playtype.setText(PolyvPopupWindow.autoText);

        } else if (i == R.id.tv_live) {
            tv_playtype.setText(PolyvPopupWindow.liveText);

        } else if (i == R.id.tv_ppt_live) {
            tv_playtype.setText(PolyvPopupWindow.pptLiveText);

        } else if (i == R.id.tv_playbacklist) {
            tv_playtype.setText(PolyvPopupWindow.playbackListText);

        }
    }

    private void gotoPlay() {
        final String userId = et_userid.getText().toString();
        final String channelId = et_channelid.getText().toString();
        switch (popupWindow.getPlaytype()) {
            case PolyvPopupWindow.live:
                startActivity(PolyvLivePlayerActivity.newIntent(PolyvLoginActivity.this, userId, channelId, chatUserId, nickName, false, false));
                break;
            case PolyvPopupWindow.pptLive:
                startActivity(PolyvPPTLivePlayerActivity.newIntent(PolyvLoginActivity.this, userId, channelId, chatUserId, nickName, false, false));
                break;
            case PolyvPopupWindow.playbacklist:
                progress.show();
                liveRestrict.getLiveRestrict(userId, channelId, new PolyvLiveRestrictListener() {
                    @Override
                    public void success(boolean canWatch, String errorCode) {
                        if (!progress.isShowing())
                            return;
                        progress.dismiss();
                        if (PolyvLiveRestrict.ERRORCODE_4.equals(errorCode)) {
                            Toast.makeText(PolyvLoginActivity.this, liveRestrict.getTips(errorCode), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        startActivity(PolyvPlaybackListActivity.newIntent(PolyvLoginActivity.this, userId, channelId, chatUserId, nickName));
                    }

                    @Override
                    public void fail(String failTips, int code) {
                        if (!progress.isShowing())
                            return;
                        progress.dismiss();
                        Toast.makeText(PolyvLoginActivity.this, "获取直播数据失败(" + code + ")\n" + failTips, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case PolyvPopupWindow.auto:
                progress.show();
                // 获取直播/回放信息
                livePlayback.getLivePlayback(PolyvConstant.appId, PolyvConstant.appSecret, userId, channelId, new PolyvLivePlaybackListener() {
                    @Override
                    public void success(PolyvLivePlaybackEntity livePlaybackEntity) {
                        if (!progress.isShowing())
                            return;
                        progress.dismiss();
                        Intent intent;
                        // 播放类型
                        switch (livePlaybackEntity.playType) {
                            /**
                             * 	最近一场直播是非ppt直播，当前是否在直播可以用{@link PolyvLivePlaybackEntity.isLiving}判断
                             */
                            case PolyvLivePlaybackEntity.PLAYTYPE_LIVE:
                                intent = PolyvLivePlayerActivity.newIntent(PolyvLoginActivity.this, userId, channelId, chatUserId, nickName, false);
                                startActivity(intent);
                                break;
                            /**
                             * 	最近一场直播是ppt直播，当前是否在直播可以用{@link PolyvLivePlaybackEntity.isLiving}判断
                             */
                            case PolyvLivePlaybackEntity.PLAYTYPE_PPT_LIVE:
                                intent = PolyvPPTLivePlayerActivity.newIntent(PolyvLoginActivity.this, userId, channelId, chatUserId, nickName, false);
                                startActivity(intent);
                                break;
                            /**
                             * 非ppt回放，当前没有直播且回看地址获取成功
                             */
                            case PolyvLivePlaybackEntity.PLAYTYPE_PLAYBACK:
                                String vid = livePlaybackEntity.channelJsonEntity.vid;
                                int bitrate = livePlaybackEntity.channelJsonEntity.bitrate;
                                if (vid != null)
                                    intent = PolyvPbPlayerActivity.newVidIntent(PolyvLoginActivity.this, vid, bitrate, true, false, false);
                                else {
                                    String playbackUrl = livePlaybackEntity.channelJsonEntity.playbackUrl;
                                    String title = livePlaybackEntity.channelJsonEntity.title;
                                    intent = PolyvPbPlayerActivity.newUrlIntent(PolyvLoginActivity.this, playbackUrl, title, false);
                                    intent = PolyvPbPlayerActivity.addPlaybackParam(intent, livePlaybackEntity.playbackParam);
                                }
                                startActivity(PolyvPbPlayerActivity.addChatExtra(intent, userId, channelId, chatUserId, nickName, true));
                                break;
                            /**
                             * ppt回放，当前没有直播且回看地址获取成功
                             */
                            case PolyvLivePlaybackEntity.PLAYTYPE_PPT_PLAYBACK:
                                String ppt_vid = livePlaybackEntity.channelJsonEntity.vid;
                                int ppt_bitrate = livePlaybackEntity.channelJsonEntity.bitrate;
                                String sessionId = livePlaybackEntity.channelJsonEntity.recordFileSessionId;
                                boolean isList = livePlaybackEntity.recordFilesEntity.data.contents.size() > 0;
                                if (ppt_vid != null)
                                    intent = PolyvPPTPbPlayerActivity.newVidIntent(PolyvLoginActivity.this, ppt_vid, ppt_bitrate, true, false, sessionId, isList, false);
                                else {
                                    String playbackUrl = livePlaybackEntity.channelJsonEntity.playbackUrl;
                                    String title = livePlaybackEntity.channelJsonEntity.title;
                                    intent = PolyvPPTPbPlayerActivity.newUrlIntent(PolyvLoginActivity.this, playbackUrl, title, sessionId, isList, false);
                                    intent = PolyvPPTPbPlayerActivity.addPlaybackParam(intent, livePlaybackEntity.playbackParam);
                                }
                                startActivity(PolyvPPTPbPlayerActivity.addChatExtra(intent, userId, channelId, chatUserId, nickName, true));
                                break;
                        }
                    }

                    @Override
                    public void fail(final String failTips, final int code) {
                        if (!progress.isShowing())
                            return;
                        progress.dismiss();
                        Toast.makeText(PolyvLoginActivity.this, "获取直播数据失败(" + code + ")\n" + failTips, Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 中断请求
        livePlayback.shutdown();
        liveRestrict.shutdown();
    }

    /**
     * This is the method that is hit after the user accepts/declines the
     * permission you requested. For the purpose of this example I am showing a "success" header
     * when the user accepts the permission and a snackbar when the user declines it.  In your application
     * you will want to handle the accept/decline in a way that makes sense.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (polyvPermission.operationHasPermission(requestCode)) {
            gotoPlay();
        } else {
            polyvPermission.makePostRequestSnack();
        }
    }
}
