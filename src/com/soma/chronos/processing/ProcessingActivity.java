package com.soma.chronos.processing;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.soma.chronos.MainActivity;
import com.soma.chronos.R;
import com.soma.chronos.frame.Frames;
import com.soma.chronos.result.ResultRect;
import com.soma.chronos.root.RootActivity;
import com.soma.chronos.util.DeviceMetrics;
import com.soma.chronos.util.RecycleUtils;
import com.soma.chronos.util.StopWatch;

public abstract class ProcessingActivity extends RootActivity {

	public static final String ACTION_INTENT = "chronos.intent.action.PROCESSING";
	public static final String KEY_FACE = "face";
	public static final String KEY_SELECT_FRAME = "select";

	public static final String SPACE = "       ";

	private ProcessingManager processingManager = null;
	private ResultRect faceRect = null;
	private StopWatch stopWatch = null;

	private int selectFrame = 0;
	private int progress = 0;
	private int max = 10;

	private ProcessingAsyncTask asyncTask = null;

	private StringBuffer buffer = null;
	private ScrollView scrollView = null;
	private TextView logTextView = null;
	private ImageView imageView = null;
	private Intent intent = null;
	private ProgressBar bar = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		scrollView = getScrollView(getApplicationContext());
		FrameLayout layout = new FrameLayout(getApplicationContext());
		layout.setBackgroundResource(R.drawable.main_bg1);
		layout.addView(scrollView);
		layout.addView(getLayout(getApplicationContext()));
		layout.addView(getBarLayout(getApplicationContext()));
		setContentView(layout);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null)
			selectFrame = bundle.getInt(KEY_SELECT_FRAME);

		buffer = new StringBuffer();
		intent = new Intent(startNextActivity());
		stopWatch = new StopWatch();

		asyncTask = new ProcessingAsyncTask();
		asyncTask.execute();

	}

	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
	}

	private ScrollView getScrollView(Context context) {
		logTextView = new TextView(context);
		logTextView.setTextColor(Color.WHITE);
		ScrollView scrollView = new ScrollView(context);
		scrollView.setFillViewport(true);
		scrollView.setPadding(0, DeviceMetrics.getHeight() / 10, 0, 0);
		scrollView.addView(logTextView);
		return scrollView;

	}

	private ViewGroup getLayout(Context context) {
		imageView = new ImageView(context);
		imageView.setImageResource(R.drawable.soma_1);

		BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
		Bitmap bitmap = drawable.getBitmap();

		bitmap = Bitmap.createScaledBitmap(bitmap,
				DeviceMetrics.getWidth() / 3, DeviceMetrics.getHeight() / 6,
				true);

		imageView.setImageBitmap(bitmap);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				PARAMS_WRAP);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

		RelativeLayout layout = new RelativeLayout(context);
		layout.addView(imageView, params);
		return layout;
	}

	private View getBarLayout(Context context) {
		bar = showProgressBar(context);

		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
				MATCH_PARENT, DeviceMetrics.getHeight() / 15);
		lp1.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

		RelativeLayout layout = new RelativeLayout(context);
		layout.addView(bar, lp1);
		return layout;
	}

	private ProgressBar showProgressBar(Context context) {
		AttributeSet attributeSet = null;
		ProgressBar bar = new ProgressBar(context, attributeSet,
				android.R.attr.progressBarStyleHorizontal);

		bar.setMax(max);
		bar.setProgress(progress++);
		bar.setPadding(10, 10, 10, 10);
		return bar;
	}

	private void setLogText(String string) {
		if (logTextView != null) {
			logTextView.setText(getLogText(string));
			scrollView.post(new Runnable() {

				@Override
				public void run() {
					scrollView.fullScroll(View.FOCUS_DOWN);
				}
			});
		}
		progress();
	}

	private StringBuffer getLogText(String string) {
		return buffer.append(" [ ").append(stopWatch.getThisTime())
				.append(" ] ").append(string).append("\n");
	}

	private void progress() {
		if (bar != null)
			bar.setProgress(progress++);

	}

	private class ProcessingAsyncTask extends AsyncTask<Void, String, Void> {
		private StringBuffer string = null;
		final int size = Frames.size();

		@Override
		protected Void doInBackground(Void... params) {
			string = new StringBuffer();
			stopWatch.start();
			publishProgress("Processing Start");
			boolean isFaceDetect = false;

			processingYuvToRgb();
			processingInit();
			processingFindSurf();

			isFaceDetect = processingFaceDetect();

			processingSetFaceRect(isFaceDetect);

			if (isGetSURF())
				processingGetSurf();

			if (isTemplateMatching() && isFaceDetect)
				processingTemplateMatching();

			publishProgress("Processing End");
			intent.putExtra(KEY_FACE, faceRect);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			bar.setProgress(max);
			Log.i(MainActivity.TAG,
					"ProcessingTime : > " + stopWatch.toString());
			startActivity(intent);
			finish();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			Log.d(MainActivity.TAG, values[0]);
			setLogText(values[0]);
		}

		private void processingYuvToRgb() {
			publishProgress("Frame Convert Yuv to RGB");
			for (int i = 0; i < size; i++) {
				Frames.addRGB(Frames.getYuv(i));
			}
		}

		private void processingInit() {
			string = new StringBuffer();
			string.append("Processing Manager Initialization");
			string.append("\n");
			for (int i = 0; i < size; i++)
				string.append("     ").append("Thread Initialization : " + i)
						.append("\n");
			publishProgress(string.toString());
			faceRect = new ResultRect();
			processingManager = new ProcessingManager();
		}

		private void processingFindSurf() {
			string = new StringBuffer();
			string.append("SURF Descriptors");
			string.append("\n");
			for (int i = 0; i < size; i++) {
				string.append(SPACE);
				string.append("SURF Descriptors Thread Start : " + i);
				string.append("\n");
				string.append(SPACE);
				string.append("Width : ").append(DeviceMetrics.getWidth())
						.append(" ");
				string.append(" Height : ").append(DeviceMetrics.getHeight())
						.append(" ");
				string.append("\n");
				string.append(SPACE);
				string.append("cvSURFParams(500, 1)");
				string.append("\n");
			}

			publishProgress(string.toString());
			processingManager.startFindSurf();
			while (processingManager.isTerminated() == false) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {
				}
			}
		}

		private void processingGetSurf() {
			string = new StringBuffer();
			string.append("Add SURF Descriptors");
			string.append("\n");
			publishProgress(string.toString());

			processingManager.startGetSurf();
			while (processingManager.isTerminated() == false) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {
				}
			}
		}

		private void processingTemplateMatching() {
			string = new StringBuffer();
			string.append("TemplateMatching Start");
			string.append("\n");

			publishProgress(string.toString());

			processingManager.startTemplateMatching();
			while (processingManager.isTerminated() == false) {
				try {
					TimeUnit.MILLISECONDS.sleep(100);
				} catch (Exception e) {
				}
			}
		}

		private boolean processingFaceDetect() {
			string = new StringBuffer();
			for (int i = 0; i < size; i++) {
				string.append("SURF Descriptors Thread End : " + i);
				string.append("\n");
			}
			string.append("Face Detect").append("\n");
			string.append("NDK Haar Detect Objects Start").append("\n");
			publishProgress(buffer.toString());

			processingManager.setFrame(selectFrame);
			faceRect.setSelectFrame(selectFrame);
			synchronized (this) {
				return processingManager.faceDetect();
			}

		}

		private void processingSetFaceRect(boolean isFaceDetect) {
			string = new StringBuffer();
			string.append("Face Detect Result ");
			string.append(String.valueOf(isFaceDetect));
			publishProgress(string.toString());

			if (isFaceDetect) {
				faceRect.setFaceDectect(isFaceDetect);
				for (int i = 0; i < processingManager.getFaceRectSize(); i++)
					faceRect.set(processingManager.getFaceRect(i));
			} else {
				faceRect.setFaceDectect(isFaceDetect);
			}

		}

	}

	public abstract boolean isGetSURF();

	public abstract boolean isTemplateMatching();

	public abstract String startNextActivity();
}
