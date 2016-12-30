package com.easefun.polyvsdk.live.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.permission.PolyvPermission;

/**
 * 登陆 activity
 */
public class PolyvLoginActivity extends Activity {
	private EditText et_userid, et_channelid;
	private TextView tv_login;
	private PolyvPermission polyvPermission = null;
	private static final int SETTING = 1;

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
				requestPermissionWriteSettings();
			}
		});
	}

	private void findId() {
		et_userid = (EditText) findViewById(R.id.et_userid);
		et_channelid = (EditText) findViewById(R.id.et_channelid);
		tv_login = (TextView) findViewById(R.id.tv_login);
	}

	private void initView() {
		et_userid.requestFocus();
//		et_userid.setText("");
//		et_channelid.setText("");
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
	}

	private void gotoPlay() {
		Intent playUrl = new Intent(PolyvLoginActivity.this, PolyvPlayerActivity.class);
		playUrl.putExtra("uid", et_userid.getText().toString());
		playUrl.putExtra("cid", et_channelid.getText().toString());
		startActivity(playUrl);
	}

	/**
	 * 请求写入设置的权限
	 */
	@SuppressLint("InlinedApi")
	private void requestPermissionWriteSettings() {
		if (!PolyvPermission.canMakeSmores()) {
			gotoPlay();
		} else if (Settings.System.canWrite(this)) {
			gotoPlay();
		} else {
			Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + this.getPackageName()));
			startActivityForResult(intent, SETTING);
		}
	}

	@Override
	@SuppressLint("InlinedApi")
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SETTING) {
			if (PolyvPermission.canMakeSmores()) {
				if (Settings.System.canWrite(this)) {
					gotoPlay();
				} else {
					new AlertDialog.Builder(this)
							.setTitle("showPermissionInternet")
							.setMessage(Settings.ACTION_MANAGE_WRITE_SETTINGS + " not granted")
							.setPositiveButton(android.R.string.ok, null)
							.show();
				}
			}
		}
	}

	/**
	 * This is the method that is hit after the user accepts/declines the
	 * permission you requested. For the purpose of this example I am showing a "success" header
	 * when the user accepts the permission and a snackbar when the user declines it.  In your application
	 * you will want to handle the accept/decline in a way that makes sense.
	 * @param requestCode
	 * @param permissions
	 * @param grantResults
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (polyvPermission.operationHasPermission(requestCode)) {
			requestPermissionWriteSettings();
		} else {
			polyvPermission.makePostRequestSnack();
		}
	}
}
