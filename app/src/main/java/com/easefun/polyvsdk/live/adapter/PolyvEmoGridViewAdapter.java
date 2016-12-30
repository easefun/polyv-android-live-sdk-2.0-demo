package com.easefun.polyvsdk.live.adapter;

import java.util.List;

import com.easefun.polyvsdk.live.R;
import com.easefun.polyvsdk.live.util.PolyvFaceManager;
import com.easefun.polyvsdk.live.util.PolyvViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = inflater.inflate(R.layout.polyv_gridview_emo_item, null);
		ImageView iv_emo = PolyvViewHolder.get(convertView, R.id.iv_emo);
		iv_emo.setImageDrawable(
				context.getResources().getDrawable(PolyvFaceManager.getInstance().getFaceId(lists.get(position))));
		return convertView;
	}
}
