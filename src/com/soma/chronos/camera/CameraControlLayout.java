package com.soma.chronos.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.soma.chronos.R;
import com.soma.chronos.root.RootLayout;
import com.soma.chronos.util.DeviceMetrics;

public class CameraControlLayout extends RootLayout implements CameraParams {
	private static final int ID_CAMERA = 1;
	private static final int ID_SETTING = 2;
	private static final int ID_ZOOM = 3;

	private static final int ID_MENU_EXPOSURE = 0x100;
	private static final int ID_MENU_ISO = 0x200;
	private static final int ID_MENU_WHITE_BALANCE = 0x300;
	private static final int ID_MENU_SCENE = 0x400;

	private int widthSetting = DeviceMetrics.getWidth() / 3;
	private static final String TITLE_EXPOSURE = "Exposure";
	private static final String TITLE_ISO = "ISO";
	private static final String TITLE_WHITE_BALANCE = "White Balance";
	private static final String TITLE_SCENE = "SCENE";

	private Handler handler = null;
	private ViewGroup zoomLayout = null;
	private ViewGroup settingLayout = null;
	private ViewGroup exposureGroup = null;
	private ViewGroup isoGroup = null;
	private ViewGroup whiteBalanceGroup = null;
	private ViewGroup sceneGroup = null;

	public CameraControlLayout(Context context) {
		super(context);

		addView(getBaseLayout(context), PARAMS_MATCH);
	}

	public CameraControlLayout(Context context, Handler handler) {
		this(context);
		this.handler = handler;
	}

	private RelativeLayout getBaseLayout(Context context) {

		int w = 150;
		int h = 150;
		int rightMargin = DeviceMetrics.getWidth() / 70;

		ImageView btnCamera = resizedImageView(context, R.drawable.camera,
				ID_CAMERA, w, h, null);
		btnCamera.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					handler.sendEmptyMessage(CameraActivity.HANDLER_AUTO_FOCUS);
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					handler.sendEmptyMessage(CameraActivity.HANDLER_CLICK_CAMERA);
				}
				return false;
			}
		});

		settingLayout = settingLayout(context);
		zoomLayout = zoomScrollLayout(context);

		RelativeLayout.LayoutParams lp1 = params(PARAMS_WRAP);
		lp1.addRule(RelativeLayout.CENTER_VERTICAL);
		lp1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		lp1.rightMargin = rightMargin;

		RelativeLayout.LayoutParams lp2 = params(PARAMS_WRAP);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		lp2.rightMargin = rightMargin;

		RelativeLayout.LayoutParams lp3 = params(widthSetting, WRAP_CONTENT);
		lp3.addRule(RelativeLayout.LEFT_OF, ID_CAMERA);
		lp3.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		lp3.rightMargin = rightMargin;

		RelativeLayout layout = new RelativeLayout(context);
		layout.addView(btnCamera, lp1);
		layout.addView(zoomLayout, lp2);
		layout.addView(settingLayout, lp3);
		return layout;
	}

	private ViewGroup zoomScrollLayout(Context context) {

		int l = 10;
		int t = DeviceMetrics.getWidth() / 50;
		int r = DeviceMetrics.getHeight() / 50;
		int b = 10;
		int w = 50;
		int h = 50;

		ImageView setting = resizedImageView(context,
				R.drawable.camera_setting, ID_SETTING, w, h,
				new SettingClickListener());
		setting.setPadding(l, t, r, b);

		ImageView plus = resizedImageView(context, R.drawable.camera_plus, 0,
				w, h);
		plus.setPadding(l, t, r, b);

		ImageView minus = resizedImageView(context, R.drawable.camera_minus, 0,
				w, h);
		minus.setPadding(l, t, r, b);

		SeekBar bar = new SeekBar(context);
		bar.setProgressDrawable(context.getResources().getDrawable(
				R.drawable.progress_horizontal));
		bar.setMax(CameraSettings.getInt(CameraSettings.KEY_MAX_ZOOM));
		bar.setProgress(CameraSettings.getInt(CameraSettings.KEY_MIN_ZOOM));
		bar.setOnSeekBarChangeListener(new SeekBarChangeListener());

		LinearLayout layout = new LinearLayout(context);
		layout.setId(ID_ZOOM);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setGravity(Gravity.CENTER_VERTICAL);
		layout.addView(minus, PARAMS_WRAP);
		layout.addView(bar, new LayoutParams(widthSetting, WRAP_CONTENT));
		layout.addView(plus, PARAMS_WRAP);
		layout.addView(setting, PARAMS_WRAP);

		return layout;

	}

	private ViewGroup settingLayout(Context context) {

		exposureGroup = valueExposureLayout(context);
		isoGroup = valueIsoLayout(context);
		whiteBalanceGroup = valueWhiteBalanceLayout(context);
		sceneGroup = valueSceneLayout(context);

		ViewGroup view1 = settingTitleLayout(context, TITLE_EXPOSURE,
				ID_MENU_EXPOSURE, new MenuClickListener());
		ViewGroup view2 = settingTitleLayout(context, TITLE_ISO, ID_MENU_ISO,
				new MenuClickListener());
		ViewGroup view3 = settingTitleLayout(context, TITLE_WHITE_BALANCE,
				ID_MENU_WHITE_BALANCE, new MenuClickListener());
		ViewGroup view4 = settingTitleLayout(context, TITLE_SCENE,
				ID_MENU_SCENE, new MenuClickListener());

		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.addView(view1);
		linearLayout.addView(exposureGroup);
		linearLayout.addView(view2);
		linearLayout.addView(isoGroup);
		linearLayout.addView(view3);
		linearLayout.addView(whiteBalanceGroup);
		linearLayout.addView(view4);
		linearLayout.addView(sceneGroup);

		ScrollView scrollView = new ScrollView(context);
		scrollView.setBackgroundColor(Color.argb(150, 0, 0, 0));
		scrollView.setVisibility(View.GONE);
		scrollView.addView(linearLayout);

		return scrollView;
	}

	private ViewGroup settingTitleLayout(Context context, String title, int id,
			OnClickListener listener) {
		int l = DeviceMetrics.getWidth() / 50;
		int t = DeviceMetrics.getHeight() / 50;
		int r = DeviceMetrics.getWidth() / 50;
		int b = DeviceMetrics.getHeight() / 50;

		TextView textView = new TextView(context);
		textView.setGravity(Gravity.CENTER);
		textView.setText(title);
		textView.setTextSize(15);

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setGravity(Gravity.CENTER_VERTICAL);
		layout.setPadding(l, t, r, b);
		layout.setId(id);
		layout.setOnClickListener(listener);
		layout.addView(textView, PARAMS_WRAP);

		return layout;
	}

	private ViewGroup valueExposureLayout(Context context) {
		final int max = CameraSettings.getInt(CameraSettings.KEY_MAX_EXPOSURE);
		final int min = CameraSettings.getInt(CameraSettings.KEY_MIN_EXPOSURE);
		float value = -2.0F;
		RadioGroup group = new RadioGroup(context);

		group.setVisibility(View.GONE);
		group.setOrientation(RadioGroup.VERTICAL);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				int value = 0;
				switch (checkedId) {
				case 0:
					value = min;
					CameraSettings.set(CameraSettings.KEY_EXPOSURE,
							String.valueOf(value));
					break;
				case 1:
					value = min + (max / 2);
					CameraSettings.set(CameraSettings.KEY_EXPOSURE,
							String.valueOf(value));
					break;
				case 2:
					CameraSettings.set(CameraSettings.KEY_EXPOSURE,
							String.valueOf(value));
					break;
				case 3:
					value = max + (min / 2);
					CameraSettings.set(CameraSettings.KEY_EXPOSURE,
							String.valueOf(value));
					break;
				case 4:
					value = max;
					CameraSettings.set(CameraSettings.KEY_EXPOSURE,
							String.valueOf(value));
					break;
				}

				handler.sendEmptyMessage(CameraActivity.HANDLER_SETTING);
			}
		});

		for (int i = 0; i < 5; i++) {
			final RadioButton button = new RadioButton(context);
			button.setId(i);
			button.setText(String.valueOf(value));
			group.addView(button);

			if (value == 0.0F) {
				button.post(new Runnable() {

					@Override
					public void run() {
						button.setChecked(true);
					}
				});
			}

			value += 1.0F;

		}
		return group;

	}

	private ViewGroup valueIsoLayout(Context context) {
		final List<String> list = CameraSettings.getIsoList();
		final List<Integer> listInt = new ArrayList<Integer>();
		final int size = list.size();
		int index = 0;
		final RadioButton[] button = new RadioButton[size];
		RadioGroup group = new RadioGroup(context);

		group.setVisibility(View.GONE);
		group.setOrientation(RadioGroup.VERTICAL);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

				String string = button[checkedId].getContentDescription()
						.toString();

				for (int i = 0; i < list.size(); i++) {
					if (string.startsWith(list.get(i))) {
						CameraSettings.set(CameraSettings.KEY_ISO, list.get(i));
						break;
					}
				}

				handler.sendEmptyMessage(CameraActivity.HANDLER_SETTING);
			}
		});

		for (int i = 0; i < size; i++) {
			String string = list.get(i).toLowerCase().replace("iso", "")
					.replace("_", "");
			if (!string.equals("auto")) {
				listInt.add(Integer.parseInt(string));
			} else {
				final int index1 = index;
				button[index1] = new RadioButton(context);
				button[index1].setId(index);
				button[index1].setText("ISO " + string.toUpperCase());
				button[index1].setContentDescription(string);
				button[index1].post(new Runnable() {

					@Override
					public void run() {
						button[index1].setChecked(true);

					}
				});
				group.addView(button[index]);
			}
		}

		Collections.sort(listInt);

		for (int i = 0; i < listInt.size(); i++) {
			index++;
			button[index] = new RadioButton(context);
			button[index].setId(index);
			button[index].setText("ISO " + String.valueOf(listInt.get(i)));
			button[index].setContentDescription(String.valueOf(listInt.get(i)));
			group.addView(button[index]);
		}
		return group;

	}

	private ViewGroup valueWhiteBalanceLayout(Context context) {
		RadioGroup group = new RadioGroup(context);

		group.setVisibility(View.GONE);
		group.setOrientation(RadioGroup.VERTICAL);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				CameraSettings.set(CameraSettings.KEY_WHITE_BALANCE,
						WHITE_BALANCE_ARRAY[checkedId]);
				handler.sendEmptyMessage(CameraActivity.HANDLER_SETTING);
				handler.sendEmptyMessage(CameraActivity.HANDLER_AUTO_FOCUS);
			}
		});

		for (int i = 0; i < WHITE_BALANCE_ARRAY.length; i++) {
			final RadioButton button = new RadioButton(context);
			button.setId(i);
			button.setText(WHITE_BALANCE_ARRAY[i]);
			group.addView(button);

			if (WHITE_BALANCE_ARRAY[i].equals(WHITE_BALANCE_AUTO)) {
				button.post(new Runnable() {

					@Override
					public void run() {
						button.setChecked(true);

					}
				});
			}

		}
		return group;

	}

	private ViewGroup valueSceneLayout(Context context) {
		RadioGroup group = new RadioGroup(context);

		group.setVisibility(View.GONE);
		group.setOrientation(RadioGroup.VERTICAL);
		group.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				CameraSettings.set(CameraSettings.KEY_SCENE,
						SCENE_ARRAY[checkedId]);
				handler.sendEmptyMessage(CameraActivity.HANDLER_SETTING);
				handler.sendEmptyMessage(CameraActivity.HANDLER_AUTO_FOCUS);
			}
		});

		for (int i = 0; i < SCENE_ARRAY.length; i++) {
			final RadioButton button = new RadioButton(context);
			button.setId(i);
			button.setText(SCENE_ARRAY[i]);
			group.addView(button);
			if (SCENE_ARRAY[i].equals(SCENE_MODE_AUTO)) {
				button.post(new Runnable() {

					@Override
					public void run() {
						button.setChecked(true);

					}
				});
			}

		}
		return group;

	}

	private RelativeLayout.LayoutParams params(int w, int h) {
		return new RelativeLayout.LayoutParams(w, h);
	}

	private RelativeLayout.LayoutParams params(LayoutParams params) {
		return new RelativeLayout.LayoutParams(params);
	}

	private class SettingClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case ID_SETTING:
				if (settingLayout.getVisibility() == GONE)
					settingLayout.setVisibility(VISIBLE);
				else if (settingLayout.getVisibility() == VISIBLE) {
					settingLayout.setVisibility(GONE);
					exposureGroup.setVisibility(View.GONE);
					isoGroup.setVisibility(View.GONE);
					whiteBalanceGroup.setVisibility(View.GONE);
					sceneGroup.setVisibility(View.GONE);
				}
				break;
			}

		}
	}

	private class MenuClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case ID_MENU_EXPOSURE:

				if (exposureGroup.getVisibility() == View.GONE)
					exposureGroup.setVisibility(View.VISIBLE);
				else if (exposureGroup.getVisibility() == View.VISIBLE)
					exposureGroup.setVisibility(View.GONE);

				break;
			case ID_MENU_ISO:

				if (isoGroup.getVisibility() == View.GONE)
					isoGroup.setVisibility(View.VISIBLE);
				else if (isoGroup.getVisibility() == View.VISIBLE)
					isoGroup.setVisibility(View.GONE);

				break;
			case ID_MENU_WHITE_BALANCE:

				if (whiteBalanceGroup.getVisibility() == View.GONE)
					whiteBalanceGroup.setVisibility(View.VISIBLE);
				else if (whiteBalanceGroup.getVisibility() == View.VISIBLE)
					whiteBalanceGroup.setVisibility(View.GONE);

				break;
			case ID_MENU_SCENE:

				if (sceneGroup.getVisibility() == View.GONE)
					sceneGroup.setVisibility(View.VISIBLE);
				else if (sceneGroup.getVisibility() == View.VISIBLE)
					sceneGroup.setVisibility(View.GONE);

				break;

			}

		}
	}

	private class SeekBarChangeListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			seekBar.setProgress(progress);
			CameraSettings.set(CameraSettings.KEY_ZOOM,
					String.valueOf(progress));
			handler.sendEmptyMessage(CameraActivity.HANDLER_SETTING);

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			handler.sendEmptyMessage(CameraActivity.HANDLER_AUTO_FOCUS);

		}
	}
}
