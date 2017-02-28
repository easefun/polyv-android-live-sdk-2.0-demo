package com.easefun.polyvsdk.live.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Map;

/**
 * 与屏幕相关的工具类
 */
public class PolyvScreenUtils {

	private static final int HIDE_STATUSBAR = 1;

	private static final Handler HANDLER = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HIDE_STATUSBAR:
				reSetStatusBar((Activity) msg.obj);
				break;
			}
		}
	};

	// 是否竖屏
	public static boolean isPortrait(Context context) {
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
	}

	// 是否横屏
	public static boolean isLandscape(Context context) {
		return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	// 设置竖屏
	public static void setPortrait(Activity activity) {
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	// 设置横屏
	public static void setLandscape(Activity activity) {
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	}

	// 获取是否存在虚拟NavigationBar(NavigationBar可能是实体)，也可根据普通高度和总高度判断
	public static boolean hasNavigationBar(Context context) {
		boolean hasNavigationBar = false;
		Resources rs = context.getResources();
		int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
		if (id > 0) {
			hasNavigationBar = rs.getBoolean(id);
		}
		try {
			Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");
			Method m = systemPropertiesClass.getMethod("get", String.class);
			String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
			if ("1".equals(navBarOverride)) {
				hasNavigationBar = false;
			} else if ("0".equals(navBarOverride)) {
				hasNavigationBar = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return hasNavigationBar;
	}

	// 获取控件在窗体中的位置,注意：View.getY()获取到的位置不包括状态栏的高度
	public static int[] getLocation(View view) {
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		return location;
	}

	// 获取包含状态栏和导航栏的屏幕宽度和高度
	public static int[] getTotalWH(Activity activity) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getNormalWH(activity);
		} else {
			WindowManager mWindowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
			Display mDisplay = mWindowManager.getDefaultDisplay();
			DisplayMetrics mDisplayMetrics = new DisplayMetrics();
			mDisplay.getRealMetrics(mDisplayMetrics);
			return new int[] { mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels };
		}
	}

	/**
	 * 获取包含状态栏的屏幕宽度和高度
	 * 
	 * @param activity
	 * @return {宽,高}
	 */
	public static int[] getNormalWH(Activity activity) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			DisplayMetrics dm = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			return new int[] { dm.widthPixels, dm.heightPixels };
		} else {
			Point point = new Point();
			WindowManager wm = activity.getWindowManager();
			wm.getDefaultDisplay().getSize(point);
			return new int[] { point.x, point.y };
		}
	}

	// 获取导航栏的高度,导航栏可能在屏幕外
	public static int getNavigationBarHeight(Context context) {
		Resources resources = context.getResources();
		int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
		if (resourceId > 0) {
			int height = resources.getDimensionPixelSize(resourceId);
			return height;
		} else {
			return 0;
		}
	}

	// 获取状态栏的高度
	public static int getStatusBarH(Context context) {
		Resources resources = context.getResources();
		int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			int height = resources.getDimensionPixelSize(resourceId);
			return height;
		} else {
			return 0;
		}
	}

	// 通过反射，获取状态栏的高度
	public static int getStatusBarHeight(Context context) {
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, statusBarHeight = 0;
		try {
			c = Class.forName("com.android.internal.R$dimen");
			obj = c.newInstance();
			field = c.getField("status_bar_height");
			x = Integer.parseInt(field.get(obj).toString());
			statusBarHeight = context.getResources().getDimensionPixelSize(x);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return statusBarHeight;
	}

	// 通过反射，获取包含虚拟键的整体屏幕高度，已包含状态栏
	public static int getTotalHeight(Activity activity) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return getNormalWH(activity)[1];
		} else {
			int height = 0;
			Display display = activity.getWindowManager().getDefaultDisplay();
			DisplayMetrics dm = new DisplayMetrics();
			@SuppressWarnings("rawtypes")
			Class c;
			try {
				c = Class.forName("android.view.Display");
				@SuppressWarnings("unchecked")
				Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
				method.invoke(display, dm);
				height = dm.heightPixels;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return height;
		}
	}

	/**
	 * 设置隐藏状态栏的监听器
	 * 
	 * @param activity
	 * @param millissecond
	 *            隐藏间隔，毫秒
	 */
	public static void setHideStatusBarListener(final Activity activity, final long millissecond) {
		View decorView = activity.getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {

			@Override
			public void onSystemUiVisibilityChange(int visibility) {
				if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
					Message message = HANDLER.obtainMessage();
					message.obj = activity;
					message.what = HIDE_STATUSBAR;
					HANDLER.sendMessageDelayed(message, millissecond);
				} else {
				}
			}
		});
		// int visible
		// Note that system bars will only be "visible" if none of the
		// LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
		// if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
		// TODO: The system bars are visible. Make any desired
		// adjustments to your UI, such as showing the action bar or
		// other navigational controls.
		// } else {
		// TODO: The system bars are NOT visible. Make any desired
		// adjustments to your UI, such as hiding the action bar or
		// other navigational controls.
		// }
	}

	// 设置系统UI的显示或隐藏的监听事件
	public static void setOnSystemUiVisibilityChange(Activity activity, OnSystemUiVisibilityChangeListener listener) {
		View decorView = activity.getWindow().getDecorView();
		decorView.setOnSystemUiVisibilityChangeListener(listener);
		// int visible
		// Note that system bars will only be "visible" if none of the
		// LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
		// if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
		// TODO: The system bars are visible. Make any desired
		// adjustments to your UI, such as showing the action bar or
		// other navigational controls.
		// } else {
		// TODO: The system bars are NOT visible. Make any desired
		// adjustments to your UI, such as hiding the action bar or
		// other navigational controls.
		// }
	}

	// 重置状态栏
	public static void reSetStatusBar(Activity activity) {
		if (isLandscape(activity)) {
			hideStatusBar(activity);
		} else {
			setDecorVisible(activity);
		}
	}

	// 隐藏状态栏
	public static void hideStatusBar(Activity activity) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			View decorView = activity.getWindow().getDecorView();
			activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
					| View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			decorView.setSystemUiVisibility(uiOptions);
		}
	}

	// 设置全屏(仅在decorView显示时有效)，当触摸事件发生时，导航栏会显示
	public static void setFullScreen(Activity activity) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			View decorView = activity.getWindow().getDecorView();
			// Hide both the navigation bar and the status bar.
			// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and
			// higher, but as
			// a general rule, you should design your app to hide the status bar
			// whenever you
			// hide the navigation bar.
			int uiOptions = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN
					| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
		}
	}

	// 恢复为不全屏状态
	public static void setDecorVisible(Activity activity) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			View decorView = activity.getWindow().getDecorView();
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
			decorView.setSystemUiVisibility(uiOptions);
		}
	}

	// 截取只在DecorView上的视图，不可截取surfaceview
	public static void getDrawingCache(Activity activity, String savePath) {
		View decorView = activity.getWindow().getDecorView();
		decorView.setDrawingCacheEnabled(true);
		Bitmap bitmap = decorView.getDrawingCache();
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(new File(savePath));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 永久关闭Android导航栏和状态栏，实现全屏,导航栏会有白边，加上setFullScreen()可以达到全屏效果
	 */
	public static void closeBar() {
		try {
			String command;
			command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib service call activity 42 s16 com.android.systemui";
			ArrayList<String> envlist = new ArrayList<String>();
			Map<String, String> env = System.getenv();
			for (String envName : env.keySet()) {
				envlist.add(envName + "=" + env.get(envName));
			}
			String[] envp = envlist.toArray(new String[0]);
			Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", command }, envp);
			proc.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(getApplicationContext(), ex.getMessage(),
			// Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * 显示导航栏和状态栏，用root关闭时用普通的方法无法显示
	 */
	public static void showBar() {
		try {
			String command;
			command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib am startservice -n com.android.systemui/.SystemUIService";
			ArrayList<String> envlist = new ArrayList<String>();
			Map<String, String> env = System.getenv();
			for (String envName : env.keySet()) {
				envlist.add(envName + "=" + env.get(envName));
			}
			String[] envp = envlist.toArray(new String[0]);
			Process proc = Runtime.getRuntime().exec(new String[] { "su", "-c", command }, envp);
			proc.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
