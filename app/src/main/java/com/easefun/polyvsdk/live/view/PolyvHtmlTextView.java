package com.easefun.polyvsdk.live.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.easefun.polyvsdk.live.R;

import java.util.ArrayList;
import java.util.List;

public class PolyvHtmlTextView extends TextView {
    private Html.ImageGetter imageGetter;
    private List<GlideDrawable> glideDrawables;
    private int count;
    private boolean flag;

    public PolyvHtmlTextView(Context context) {
        super(context);
    }

    public PolyvHtmlTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PolyvHtmlTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setHtmlText(final String text) {
        post(new Runnable() {
            @Override
            public void run() {
                if (TextUtils.isEmpty(text)) {
                    setGravity(Gravity.CENTER);
                    SpannableStringBuilder span = new SpannableStringBuilder("p");
                    Drawable drawable = getResources().getDrawable(R.drawable.polyv_icon_empty);
                    drawable.setBounds(0, 0, getWidth() / 4, getWidth() / 4);
                    span.setSpan(new ImageSpan(drawable), 0, span.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    setText(span);
                    return;
                }
                if (glideDrawables == null) {
                    glideDrawables = new ArrayList<>();
                }
                setMovementMethod(LinkMovementMethod.getInstance());
                setText(Html.fromHtml(text, imageGetter = new Html.ImageGetter() {
                    @Override
                    public Drawable getDrawable(final String source) {
                        if (flag) {
                            return null;
                        }
                        if (count >= 0 && count < glideDrawables.size()) {
                            GlideDrawable glideDrawable = glideDrawables.get(count);
                            int w = glideDrawable.getIntrinsicWidth();
                            int h = glideDrawable.getIntrinsicHeight();
                            if (w > getWidth()) {
                                w = getWidth();
                                h = (int) (h / (glideDrawable.getIntrinsicWidth() / (float) w));
                            }
                            glideDrawable.setBounds(0, 0, w, h);//setBounds
                            count++;
                            return glideDrawable;
                        }
                        flag = true;
                        count = 0;
                        Glide.with(getContext()).load(source).diskCacheStrategy(DiskCacheStrategy.ALL).priority(Priority.HIGH).listener(new RequestListener<String, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                                return true;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                flag = false;
                                glideDrawables.add(resource);
                                setText(Html.fromHtml(text, imageGetter, null));
                                return true;
                            }
                        }).into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                        return null;
                    }
                }, null));
            }
        });
    }
}
