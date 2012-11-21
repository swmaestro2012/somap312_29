package com.soma.chronos.camera;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.soma.chronos.R;
import com.soma.chronos.frame.FrameBitmapFactory;
import com.soma.chronos.frame.Frames;
import com.soma.chronos.root.RootActivity;
import com.soma.chronos.util.DeviceMetrics;
import com.soma.chronos.util.RecycleUtils;

public abstract class CameraActivity extends RootActivity implements
		Handler.Callback {

	public static final String ACTION_INTENT = "chronos.intent.action.CAMERA";

	public static final int HANDLER_SETTING = 0;
	public static final int HANDLER_CLICK_CAMERA = 1;
	public static final int HANDLER_START_FRAME = 2;
	public static final int HANDLER_START_SOUND = 3;
	public static final int HANDLER_START_ACTIVITY = 4;

	public static final int HANDLER_AUTO_FOCUS = 5;

	private static final int BURSTSHOT_TIME = 500;

	private boolean isCameraClick = false;
	private int frameIndex = 0;

	private final int w = DeviceMetrics.getWidth();
	private final int h = DeviceMetrics.getHeight();

	private BlockingQueue<String> blockingQueue = null;
	private MediaPlayer cameraClickPlayer;
	private Handler handler = null;
	private ImageView previewImageView = null;

	private CameraSurfaceView cameraSurfaceView = null;
	private CameraBarAsyncTask cameraBarAsyncTask = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		Frames.init(getBurstSize());

		removeAll();

		cameraClickPlayer = MediaPlayer.create(getApplicationContext(),
				R.raw.camera_click);
		handler = new Handler(this);
		blockingQueue = new ArrayBlockingQueue<String>(2);

		cameraSurfaceView = new CameraSurfaceView(getApplicationContext());
		cameraBarAsyncTask = new CameraBarAsyncTask(getBurstSize());
		CameraControlLayout controlLayout = new CameraControlLayout(
				getApplicationContext(), handler);

		Bitmap buffer = Bitmap.createBitmap(w, h, Config.ARGB_8888);
		previewImageView = new ImageView(getApplicationContext());
		previewImageView.setScaleType(ScaleType.FIT_XY);
		previewImageView.setVisibility(View.INVISIBLE);
		previewImageView.setImageBitmap(buffer);

		FrameLayout layout = new FrameLayout(getApplicationContext());
		layout.addView(cameraSurfaceView);
		layout.addView(controlLayout, PARAMS_MATCH);
		layout.addView(previewImageView);
		layout.addView(cameraBarAsyncTask.getLayout(getApplicationContext()));

		setContentView(layout, PARAMS_MATCH);

		Toast.makeText(getApplicationContext(), "화면을 터치 하면 포커싱을 설정 합니다.",
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onBackPressed() {
		if (isCameraClick == false)
			finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_CAMERA:
			handler.sendEmptyMessage(HANDLER_CLICK_CAMERA);
			break;
		case KeyEvent.KEYCODE_VOLUME_UP:
			handler.sendEmptyMessage(CameraActivity.HANDLER_SETTING);
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			handler.sendEmptyMessage(CameraActivity.HANDLER_SETTING);
			break;
		case KeyEvent.KEYCODE_BACK:
			onBackPressed();
			break;
		}

		return true;

	}

	private ProgressDialog getProgressBar(String message, boolean cancelable) {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage(message);
		dialog.setIndeterminate(true);
		dialog.setCancelable(cancelable);

		return dialog;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cameraClickPlayer.stop();
		cameraClickPlayer.release();
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
		case HANDLER_SETTING:
			cameraSurfaceView.onSetting();
			break;

		case HANDLER_AUTO_FOCUS:
			cameraSurfaceView.startAutoFocus();
			break;

		case HANDLER_CLICK_CAMERA:
			if (isCameraClick == false)
				cameraBarAsyncTask.execute();

			isCameraClick = true;
			break;
		case HANDLER_START_SOUND:
			cameraClickPlayer.start();
			break;
		case HANDLER_START_FRAME:
			try {
				blockingQueue.take();
				cameraSurfaceView.startFrame();
				Bitmap bm = FrameBitmapFactory.getBitmap(frameIndex++);
				previewImageView.setImageBitmap(bm);
				previewImageView.setVisibility(View.VISIBLE);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			break;
		case HANDLER_START_ACTIVITY:
			new AsyncTask<Void, Void, Void>() {
				private ProgressDialog dialog;

				@Override
				public Void doInBackground(Void... params) {
					try {
						Thread.sleep(1500);
					} catch (Exception e) {
					}
					return null;
				}

				@Override
				protected void onPreExecute() {
					dialog = getProgressBar("잠시만 기다려 주세요...", false);
					dialog.show();
				};

				@Override
				protected void onPostExecute(Void result) {
					Intent intent = new Intent(startNextActivity());
					startActivity(intent);
					finish();
					dialog.dismiss();

				};

			}.execute();

			break;
		}
		return true;
	}

	public abstract int getBurstSize();

	public abstract String startNextActivity();

	private class CameraBarAsyncTask extends AsyncTask<Void, Integer, Void> {
		private ProgressBar bar = null;
		private TextView textView = null;
		private RelativeLayout layout = null;

		private int max = 0;

		public CameraBarAsyncTask(int size) {
			this.max = size;

		}

		private ViewGroup getLayout(Context context) {
			textView = showTextView(context);
			bar = showProgressBar(context, max);

			LinearLayout linearLayout = getLinearLayout(context);
			linearLayout.addView(bar, linearParams(w / 3, h / 15, 1.0f));
			linearLayout.addView(textView, linearParams(w / 10, h / 15, 1.0f));

			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
					PARAMS_WRAP);

			lp.bottomMargin = h / 5;
			lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			lp.addRule(RelativeLayout.CENTER_HORIZONTAL);

			layout = new RelativeLayout(context);
			layout.setVisibility(View.GONE);
			layout.addView(linearLayout, lp);
			return layout;

		}

		@Override
		protected void onCancelled() {
		}

		@Override
		public void onPreExecute() {
			if (max > 1) {
				layout.setVisibility(View.VISIBLE);
				bar.setVisibility(View.VISIBLE);
				textView.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(String.valueOf(values[0].intValue()));
			buffer.append(" / ");
			buffer.append(String.valueOf(max));
			textView.setText(buffer.toString());

			bar.setProgress(values[0]);

		}

		@Override
		public Void doInBackground(Void... params) {
			for (int i = 0; i < max; i++) {
				try {
					publishProgress(i + 1);
					Thread.sleep(BURSTSHOT_TIME);
					handler.sendEmptyMessage(HANDLER_START_SOUND);
					handler.sendEmptyMessage(HANDLER_START_FRAME);
					blockingQueue.put("Queue");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return null;

		}

		@Override
		public void onPostExecute(Void result) {
			try {
				blockingQueue.put("Queue");
				handler.sendEmptyMessage(HANDLER_START_ACTIVITY);
				blockingQueue.take();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		private ProgressBar showProgressBar(Context context, int max) {
			AttributeSet attributeSet = null;
			ProgressBar bar = new ProgressBar(context, attributeSet,
					android.R.attr.progressBarStyleHorizontal);
			bar.setVisibility(View.GONE);
			bar.setMax(max);
			bar.setProgress(0);
			bar.setPadding(10, 10, 10, 10);
			return bar;
		}

		private TextView showTextView(Context context) {
			TextView view = new TextView(context);
			view.setPadding(10, 10, 10, 10);
			view.setGravity(Gravity.CENTER);
			view.setTextColor(Color.WHITE);

			return view;
		}

		private LinearLayout getLinearLayout(Context context) {
			LinearLayout layout = new LinearLayout(context);
			layout.setBackgroundColor(0x77000000);
			layout.setOrientation(LinearLayout.HORIZONTAL);
			layout.setVisibility(View.GONE);

			if (max > 1)
				layout.setVisibility(View.VISIBLE);
			return layout;
		}

		private LinearLayout.LayoutParams linearParams(int width, int height,
				float weight) {
			return new LinearLayout.LayoutParams(width, height, weight);
		}
	}

}
