package com.easefun.polyvsdk.live.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.widget.TextView;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageSpan;
import pl.droidsonroids.gif.RelativeImageSpan;

public class PolyvTextImageLoader {
    private Context context;

    public PolyvTextImageLoader(Context context) {
        this.context = context;
    }

    /**
     * 显示带本地图片的图文混排
     */
    public void displayTextImage(CharSequence charSequence, TextView textView) {
        float size = textView.getTextSize();
        int reqWidth;
        int reqHeight;
        reqWidth = reqHeight = (int) size;
        SpannableStringBuilder span = new SpannableStringBuilder(charSequence);
        int start = 0;
        int end = 0;
        Pattern pattern = Pattern.compile("\\[[^\\[]{1,5}\\]");
        Matcher matcher = pattern.matcher(charSequence);
        while (matcher.find()) {
            start = matcher.start();
            end = matcher.end();
            String group = matcher.group();
            GifDrawable gifDrawable;
            try {
                gifDrawable = new GifDrawable(context.getResources(), PolyvFaceManager.getInstance().getFaceId(group));
                gifDrawable.setBounds(0, 0, reqWidth + 8, reqHeight + 8);
                GifImageSpan imageSpan = new GifImageSpan(gifDrawable, RelativeImageSpan.ALIGN_CENTER);
                span.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            } catch (NotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
        textView.setText(span);
    }
}
