package com.easefun.polyvsdk.live.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import android.webkit.WebView;

import java.lang.reflect.Method;

public class PolyvSafeWebView extends WebView {

    public PolyvSafeWebView(Context context) {
        super(context);
        removeSearchBox();
    }

    public PolyvSafeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        removeSearchBox();
    }

    public PolyvSafeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        removeSearchBox();
    }

    /**
     * 移除系统注入的对象，避免js漏洞
     * <p>
     * https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2014-1939
     * https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2014-7224
     */
    private void removeSearchBox() {
        super.removeJavascriptInterface("searchBoxJavaBridge_");
        super.removeJavascriptInterface("accessibility");
        super.removeJavascriptInterface("accessibilityTraversal");
    }

    public static void disableAccessibility(Context context) {
        if (Build.VERSION.SDK_INT == 17/*4.2 (Build.VERSION_CODES.JELLY_BEAN_MR1)*/) {
            if (context != null) {
                try {
                    AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
                    if (!am.isEnabled()) {
                        //Not need to disable accessibility
                        return;
                    }

                    Method setState = am.getClass().getDeclaredMethod("setState", int.class);
                    setState.setAccessible(true);
                    setState.invoke(am, 0);/**{@link AccessibilityManager#STATE_FLAG_ACCESSIBILITY_ENABLED}*/
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
        }
    }

    @Override
    public void setOverScrollMode(int mode) {
//        try {
//            super.setOverScrollMode(mode);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }

        try {
            super.setOverScrollMode(mode);
        } catch (Throwable e) {
            String trace = Log.getStackTraceString(e);
            if (trace.contains("android.content.pm.PackageManager$NameNotFoundException")
                    || trace.contains("java.lang.RuntimeException: Cannot load WebView")
                    || trace.contains("android.webkit.WebViewFactory$MissingWebViewPackageException: Failed to load WebView provider: No WebView installed")) {
                e.printStackTrace();
            } else {
                throw e;
            }
        }
    }

    @Override
    public boolean isPrivateBrowsingEnabled() {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 && getSettings() == null) {
            return false;
        } else {
            return super.isPrivateBrowsingEnabled();
        }
    }
}
