package com.easefun.polyvsdk.live.util;

import android.util.SparseArray;
import android.view.View;

/**
 * ViewHolder的简化
 */
@SuppressWarnings("unchecked")
public class PolyvViewHolder {
	public static <T extends View> T get(View view, int id) {
		SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
		if (viewHolder == null) {
			viewHolder = new SparseArray<View>();
			view.setTag(viewHolder);
		}
		View childView = viewHolder.get(id);
		if (childView == null) {
			childView = view.findViewById(id);
			viewHolder.put(id, childView);
		}
		return (T) childView;
	}
}
