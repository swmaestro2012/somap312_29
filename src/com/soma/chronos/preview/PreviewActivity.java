package com.soma.chronos.preview;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.soma.chronos.R;
import com.soma.chronos.camera.DefaultCameraActivity;
import com.soma.chronos.frame.FrameBitmapFactory;
import com.soma.chronos.frame.Frames;
import com.soma.chronos.processing.DefaultProcessingActivity;
import com.soma.chronos.processing.ProcessingActivity;
import com.soma.chronos.root.RootActivity;
import com.soma.chronos.util.DeviceMetrics;
import com.soma.chronos.util.RecycleUtils;

public class PreviewActivity extends RootActivity {

	public static final String ACTION_INTENT = "chronos.intent.action.PREVIEW.DEFAULT";

	public static final int ID_CAMERA = 0x00011;
	public static final int ID_NEXT = 0x00012;
	public static final int ID_SAVE = 0x00013;

	public static final int ID_FULL_VIEW = 100;

	private int prevPosition = 0;

	private FrameLayout frameLayout = null;
	private LinearLayout pagerControlLayout = null;
	private LinearLayout controlLayout = null;
	private ViewPager pager = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		pager = getViewPager();

		frameLayout = new FrameLayout(getApplicationContext());
		frameLayout.setBackgroundColor(Color.BLACK);
		frameLayout.setVisibility(View.VISIBLE);
		frameLayout.addView(pager, PARAMS_MATCH);
		frameLayout.addView(getControlView(getApplicationContext()),
				PARAMS_MATCH);

		setContentView(frameLayout);

	}

	@Override
	public void onBackPressed() {
		startCamera();
	}

	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		super.onDestroy();
	}

	private ViewPager getViewPager() {
		ViewPager pager = new ViewPager(getApplicationContext());
		pager.setAdapter(new CustomPagerAdapter());
		pager.setOnPageChangeListener(new CustomPageListener());
		pager.setFocusable(false);
		pager.setClickable(false);
		return pager;
	}

	private RelativeLayout getControlView(Context context) {

		controlLayout = getControlLayout(context);
		pagerControlLayout = getPagerControlLayout(context);

		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
				MATCH_PARENT, WRAP_CONTENT);
		lp1.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		lp1.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

		RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
				MATCH_PARENT, WRAP_CONTENT);
		lp2.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

		RelativeLayout layout = new RelativeLayout(context);
		layout.addView(controlLayout, lp1);
		layout.addView(pagerControlLayout, lp2);

		return layout;

	}

	private LinearLayout getControlLayout(Context context) {

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setGravity(Gravity.RIGHT);
		layout.setBackgroundColor(COLOR_BG);
		layout.addView(resizedImageView(R.drawable.save, ID_SAVE,
				new MenuClickListener()));
		layout.addView(resizedImageView(R.drawable.camera, ID_CAMERA,
				new MenuClickListener()));
		layout.addView(resizedImageView(R.drawable.next, ID_NEXT,
				new MenuClickListener()));

		return layout;
	}

	private LinearLayout getPagerControlLayout(Context context) {

		int id = 0;
		LinearLayout layout = new LinearLayout(getApplicationContext());
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setGravity(Gravity.CENTER);
		layout.setPadding(0, DeviceMetrics.getHeight() / 15, 0, 0);

		final int size = Frames.size();
		ImageView view = new ImageView(context);
		view.setId(id++);
		view.setContentDescription(String.valueOf(0));
		view.setImageResource(R.drawable.page_select);
		view.setFocusable(false);
		view.setClickable(false);
		layout.addView(view, PARAMS_WRAP);

		for (; id < size; id++) {
			view = new ImageView(context);
			view.setId(id);
			view.setContentDescription(String.valueOf(id));
			view.setImageResource(R.drawable.page_not);
			view.setFocusable(false);
			view.setClickable(false);
			layout.addView(view, PARAMS_WRAP);
		}

		return layout;

	}

	private void startSave() {
		onImageSave(prevPosition);
	}

	private void startCamera() {
		startActivity(new Intent(DefaultCameraActivity.ACTION_INTENT));
		finish();
	}

	private void startProcessing() {
		Intent intent = new Intent(DefaultProcessingActivity.ACTION_INTENT);
		intent.putExtra(ProcessingActivity.KEY_SELECT_FRAME, prevPosition);
		startActivity(intent);
		finish();
	}

	private class CustomPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return Frames.size();
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			ImageView view = new ImageView(getApplicationContext());
			view.setScaleType(ScaleType.FIT_XY);
			view.setImageBitmap(FrameBitmapFactory.getBitmap(position));
			view.setContentDescription(String.valueOf(position));
			view.setFocusable(false);
			view.setClickable(false);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (controlLayout.getVisibility() == View.GONE)
						controlLayout.setVisibility(View.VISIBLE);
					else if (controlLayout.getVisibility() == View.VISIBLE)
						controlLayout.setVisibility(View.GONE);

					if (pagerControlLayout.getVisibility() == View.GONE)
						pagerControlLayout.setVisibility(View.VISIBLE);
					else if (pagerControlLayout.getVisibility() == View.VISIBLE)
						pagerControlLayout.setVisibility(View.GONE);
				}
			});
			container.addView(view);
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		}

	}

	private class CustomPageListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int position) {
			ImageView prevView = (ImageView) pagerControlLayout
					.getChildAt(prevPosition);
			prevView.setImageResource(R.drawable.page_not);

			ImageView view = (ImageView) pagerControlLayout
					.getChildAt(position);
			view.setImageResource(R.drawable.page_select);

			prevPosition = position;

		}

		@Override
		public void onPageScrolled(int position, float positionOffest,
				int positionOffsetPixels) {

		}

		@Override
		public void onPageScrollStateChanged(int state) {

		}

	}

	private class MenuClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case ID_SAVE:
				startSave();
				break;
			case ID_CAMERA:
				startCamera();
				break;
			case ID_NEXT:
				startProcessing();
				break;
			}
		}
	}
}