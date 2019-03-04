package com.easefun.polyvsdk.live.adapter;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.chat.playback.api.entity.PolyvLiveRecordFilesEntity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.util.List;

public class PolyvPlaybackListAdapter extends AbsRecyclerViewAdapter {
    private List<PolyvLiveRecordFilesEntity.Content> contents;
    private DisplayImageOptions options;
    public int playPosition = -1;

    public PolyvPlaybackListAdapter(RecyclerView recyclerView, List<PolyvLiveRecordFilesEntity.Content> contents) {
        super(recyclerView);
        this.contents = contents;
        this.options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.polyv_pic_demo) // resource
                // or
                // drawable
                .showImageForEmptyUri(R.drawable.polyv_pic_demo) // resource or drawable
                .showImageOnFail(R.drawable.polyv_pic_demo) // resource or drawable
                .displayer(new SimpleBitmapDisplayer())
                .bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory(true).cacheOnDisk(true).build();
    }

    @Override
    public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        bindContext(parent.getContext());
        return new ItemViewHolder(LayoutInflater.from(getContext()).inflate(R.layout.polyv_recyclerview_playback_item, parent, false));
    }

    public void addAll(List<PolyvLiveRecordFilesEntity.Content> contents) {
        this.contents.addAll(contents);
        notifyDataSetChanged();
    }

    public void clear() {
        contents.clear();
        notifyDataSetChanged();
    }

    public void updatePlayPosition(int position) {
        if (playPosition == position)
            return;
        int temp = playPosition;
        this.playPosition = position;
        notifyItemChanged(temp);
        notifyItemChanged(playPosition);
    }

    @Override
    public void onBindViewHolder(ClickableViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            PolyvLiveRecordFilesEntity.Content content = contents.get(position);
            ImageLoader.getInstance().displayImage(content.firstImage, itemViewHolder.iv_cover, options);
            itemViewHolder.tv_title.setText(content.title);
            itemViewHolder.tv_time.setText(content.duration);
            itemViewHolder.tv_playing.setVisibility(View.GONE);
            if (position == playPosition)
                itemViewHolder.tv_playing.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(content.channelSessionId)) {
                itemViewHolder.tv_type.setText("normal");
            } else {
                itemViewHolder.tv_type.setText("ppt");
            }
        }
        super.onBindViewHolder(holder, position);
    }

    private class ItemViewHolder extends AbsRecyclerViewAdapter.ClickableViewHolder {
        private ImageView iv_cover;
        private TextView tv_title, tv_time, tv_playing, tv_type;

        public ItemViewHolder(View itemView) {
            super(itemView);
            iv_cover = $(R.id.iv_cover);
            tv_title = $(R.id.tv_title);
            tv_time = $(R.id.tv_time);
            tv_playing = $(R.id.tv_playing);
            tv_type = $(R.id.tv_type);
        }
    }

    @Override
    public int getItemCount() {
        return contents.size();
    }
}
