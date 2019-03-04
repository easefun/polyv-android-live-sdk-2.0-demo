package com.easefun.polyvsdk.live.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.easefun.polyvsdk.live.R;


public class PolyvGrayImageView extends ImageView {

	public PolyvGrayImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public PolyvGrayImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PolyvGrayImageView(Context context) {
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (isPressed())
			canvas.drawColor(getResources().getColor(R.color.commom_click_color_gray_half));
	}

	@Override
	protected void dispatchSetPressed(boolean pressed) {
		super.dispatchSetPressed(pressed);
		invalidate();
	}
}
