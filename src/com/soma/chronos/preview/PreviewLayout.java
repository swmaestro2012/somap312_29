package com.soma.chronos.preview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.soma.chronos.frame.FrameBitmapFactory;
import com.soma.chronos.frame.Frames;
import com.soma.chronos.root.RootLayout;
import com.soma.chronos.util.DeviceMetrics;

public class PreviewLayout extends RootLayout implements OnClickListener {

	private final int size = Frames.size();

	public final int MATCH_PARENT = LayoutParams.MATCH_PARENT;
	public final int WRAP_CONTENT = LayoutParams.WRAP_CONTENT;

	public final LayoutParams MATCH_PARAMS = new LayoutParams(MATCH_PARENT,
			MATCH_PARENT);
	public final LayoutParams WRAP_PARAMS = new LayoutParams(WRAP_CONTENT,
			WRAP_CONTENT);

	private Handler handler = null;

	public PreviewLayout(Context context) {
		super(context);
	}

	public PreviewLayout(Context context, Handler handler) {
		this(context);

		FrameLayout layout = new FrameLayout(context);
		layout.addView(getOverlayView(context));
		addView(layout, WRAP_PARAMS);
		this.handler = handler;
	}

	private ViewGroup getOverlayView(Context context) {

		LinearLayout layout = new LinearLayout(context);
		layout.setBackgroundColor(0x77000000);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setVisibility(VISIBLE);

		for (int i = 0; i < size; i++) {
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				// TODO: handle exception
			}
			layout.addView(getBitmap(context, i));
		}
		HorizontalScrollView scrollView = new HorizontalScrollView(context);
		scrollView.setHorizontalScrollBarEnabled(true);
		scrollView.setVerticalScrollBarEnabled(false);
		scrollView.setScrollBarStyle(ScrollView.SCROLLBARS_OUTSIDE_INSET);
		scrollView.setVerticalFadingEdgeEnabled(false);
		scrollView.setHorizontalFadingEdgeEnabled(true);
		scrollView.addView(layout);
		final int wrap = RelativeLayout.LayoutParams.WRAP_CONTENT;
		RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(
				wrap, wrap);

		relativeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		RelativeLayout relativeLayout = new RelativeLayout(context);
		relativeLayout.addView(scrollView, relativeParams);

		return relativeLayout;
	}

	public View getBitmap(Context context, int location) {

		final int div_w = Frames.size();
		final int div_h = 4;

		final int width = DeviceMetrics.getWidth() / div_w;
		final int height = DeviceMetrics.getHeight() / div_h;

		final Bitmap bm = FrameBitmapFactory.getBitmap(location);
		Bitmap resizedBitmap = Bitmap.createScaledBitmap(bm, width, height,
				true);
		ImageView view = new ImageView(context);
		view.setBackgroundColor(Color.WHITE);
		view.setId(location);
		view.setContentDescription(String.valueOf(location));
		view.setImageBitmap(resizedBitmap);
		view.setOnClickListener(this);
		view.setPadding(5, 5, 5, 5);

		return view;

	}

	@Override
	public void onClick(View v) {
		for (int i = 0; i < size; i++) {

			if (v.getId() == i) {
				handler.sendEmptyMessage(i << 2);
			}
		}

	}

}
