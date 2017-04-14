package com.easefun.polyvsdk.live.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.util.PolyvFaceManager;
import com.easefun.polyvsdk.live.util.PolyvViewHolder;

import java.util.List;

public class PolyvEmoGridViewAdapter extends BaseAdapter {
    private List<String> lists;
    private Context context;
    private LayoutInflater inflater;

    public PolyvEmoGridViewAdapter(List<String> lists, Context context) {
        this.lists = lists;
        this.context = context;
        this.inflater = LayoutInflater.from(this.context);
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public Bitmap eraseColor(Bitmap src, int color) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap b = src.copy(Bitmap.Config.ARGB_8888, true);
        b.setHasAlpha(true);
        int[] pixels = new int[width * height];
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < width * height; i++) {
            if (pixels[i] == color) {
                pixels[i] = 0;
            }
        }
        b.setPixels(pixels, 0, width, 0, 0, width, height);
        return b;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = inflater.inflate(R.layout.polyv_gridview_emo_item, null);
        ImageView iv_emo = PolyvViewHolder.get(convertView, R.id.iv_emo);
        if (iv_emo.getTag() == null) {
            int id = PolyvFaceManager.getInstance().getFaceId(lists.get(position));
            Drawable drawable = context.getResources().getDrawable(id);
            if (drawable instanceof BitmapDrawable) {
                Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                bitmap = eraseColor(bitmap, Color.WHITE);
                bitmap = eraseColor(bitmap, Color.rgb(230, 230, 230));
                if (id != R.drawable.polyv_33 && id != R.drawable.polyv_71)
                    bitmap = eraseColor(bitmap, Color.BLACK);
                iv_emo.setImageBitmap(bitmap);
            } else {
                iv_emo.setImageDrawable(drawable);
            }
            iv_emo.setTag(iv_emo.getId());
        }
        return convertView;
    }
}
