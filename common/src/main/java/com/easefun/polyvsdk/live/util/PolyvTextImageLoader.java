package com.easefun.polyvsdk.live.util;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.widget.TextView;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageSpan;
import pl.droidsonroids.gif.RelativeImageSpan;

public class PolyvTextImageLoader {
    private Context context;

    public PolyvTextImageLoader(Context context) {
        this.context = context;
    }

    /**
     * 显示带本地表情图片的图文混排
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
            Drawable drawable = null;
            ImageSpan imageSpan = null;
            try {
                drawable = new GifDrawable(context.getResources(), PolyvFaceManager.getInstance().getFaceId(group));
                imageSpan = new GifImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
            } catch (NotFoundException | IOException e) {
                try {
                    drawable = context.getResources().getDrawable(PolyvFaceManager.getInstance().getFaceId(group));
                    imageSpan = new RelativeImageSpan(drawable, RelativeImageSpan.ALIGN_CENTER);
                } catch (Exception e1) {
                    continue;
                }
            }
            drawable.setBounds(0, 0, (int) (reqWidth * 1.6), (int) (reqHeight * 1.6));
            span.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textView.setText(span);
    }
}
