package com.soma.chronos.result;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import test.ImageProcessing.NDKManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.soma.chronos.MainActivity;
import com.soma.chronos.R;
import com.soma.chronos.contacts.ContactsListActivity;
import com.soma.chronos.frame.FrameBitmapFactory;
import com.soma.chronos.frame.Frames;
import com.soma.chronos.mosaic.MosaicFactory;
import com.soma.chronos.processing.ProcessingActivity;
import com.soma.chronos.processing.runnable.CustomRunnable;
import com.soma.chronos.root.RootActivity;

public abstract class ResultActivity extends RootActivity {
	public static final String ACTION_INTENT = "chronos.intent.action.FACE";

	public static final int ID_CANCEL = 0x10000;
	public static final int ID_SAVE = 0x20000;
	public static final int ID_CUSTOM = 0x30000;
	public static final int ID_EXIT = 0x40000;
	public static final int ID_MOSAIC = 0x50000;
	public static final int ID_CONTACTS = 0x60000;

	public static final int ID_RECT_CANCEL = 0x0000;
	public static final int ID_RECT_MODIFY = 0x0001;
	public static final int ID_RECT_MOSAIC = 0x0002;
	public static final int ID_RECT_CONTACTS = 0x0003;

	public static final int ID_BTN_LEFT = 1;
	public static final int ID_BTN_RIGHT = 2;

	public static final int REQUEST_CONTACTS = 1;

	private static final String TOAST_MESSAGE = "영역을 선택 해 주세요...";

	private ImageView btnLeft = null;
	private ImageView btnRight = null;
	private LinearLayout controlLayout = null;
	private FrameLayout rectControlLayout = null;
	private FrameLayout changeControlLayout = null;

	private int touchCount = 0;
	private int[] tX = new int[2];
	private int[] tY = new int[2];

	private int selectIndex = 0;
	private int size = Frames.size();

	private boolean isCustomMode = false;
	private boolean isSelecter = false;

	private Rect rect = null;

	private ResultView resultView = null;
	private ResultRect resultRect = null;
	private ResultRectView faceView = null;
	private NDKManager ndkManager = NDKManager.getInstance();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		resultRect = bundle.getParcelable(ProcessingActivity.KEY_FACE);

		faceView = new ResultRectView(getApplicationContext(), resultRect);

		if (resultRect.size() > 0)
			ResultRectManager.setRectRatio(resultRect.getRects(), 0.745);

		setContentView(initLayout(getApplicationContext()), PARAMS_MATCH);
	}

	private View initLayout(Context context) {

		controlLayout = controlLayout(context);
		rectControlLayout = rectControlLayout(context);
		changeControlLayout = changeButton(context);

		FrameLayout layout = new FrameLayout(context);
		layout.addView(resultView(context));
		layout.addView(faceView);
		layout.addView(controlLayout, PARAMS_MATCH_WRAP);

		if (rectControlLayout != null)
			layout.addView(rectControlLayout, PARAMS_MATCH_WRAP);

		if (isChangeButtonLayout())
			layout.addView(changeControlLayout, PARAMS_MATCH);

		return layout;
	}

	private FrameLayout changeButton(Context context) {
		btnLeft = resizedImageView(R.drawable.left_frame, ID_BTN_LEFT,
				new FrameChangeListener());

		btnRight = resizedImageView(R.drawable.right_frame, ID_BTN_RIGHT,
				new FrameChangeListener());

		FrameLayout layout = new FrameLayout(context);
		layout.setVisibility(View.INVISIBLE);
		layout.addView(btnLeft, PARAMS_WRAP);
		layout.addView(btnRight, PARAMS_WRAP);

		return layout;
	}

	@Override
	public void onBackPressed() {
		destory(getWindow());
		finish();

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_UP:

			if (isSelecter == false) {
				if (isCustomMode == false)
					return true;

				for (int i = 0; i < resultRect.size(); i++) {
					if (resultRect.get(i).contains(x, y) == true)
						return true;
				}
				touchCreateRect(x, y);
			}
			break;
		case MotionEvent.ACTION_DOWN:
			for (int i = 0; i < resultRect.size(); i++) {
				if (resultRect.get(i).contains(x, y) == true) {
					isSelecter = true;
					rect = resultRect.get(i);
					moveFaceButton(rect.left, rect.top, rect.right, rect.bottom);
					setTouchSelectFace(i);
					faceView.invalidate();
					break;
				} else {
					isSelecter = false;
				}

			}

			break;

		case MotionEvent.ACTION_MOVE:
			if (isSelecter)
				Log.i(MainActivity.TAG, "Move");

			faceView.invalidate();
			break;
		}

		return true;
	}

	private void touchCreateRect(int x, int y) {
		if (touchCount < 1) {
			tX[touchCount] = x;
			tY[touchCount] = y;
			touchCount++;
			resultRect.setTouchMode(true, x, y);
			faceView.invalidate();
		} else {
			tX[touchCount] = x;
			tY[touchCount] = y;
			Arrays.sort(tX);
			Arrays.sort(tY);
			touchCount = 0;

			ndkManager.customSet(tX[0], tY[0], tX[1] - tX[0], tY[1] - tY[0]);
			resultRect.set(tX[0], tY[0], tX[1], tY[1]);
			resultRect.setTouchMode(false, x, y);
			resultRect.setFaceDectect(true);

			faceView.invalidate();

			isCustomMode = false;

			startCustom();

		}
	}

	private void startCustom() {

		new AsyncTask<Void, Void, Void>() {
			private ProgressDialog dialog;

			@Override
			public Void doInBackground(Void... params) {
				try {
					ExecutorService executorService = Executors
							.newFixedThreadPool(size);
					CustomRunnable[] runnables = new CustomRunnable[size];
					for (int i = 0; i < size; i++) {
						runnables[i] = new CustomRunnable(i);

						runnables[i].setFaceRect(ndkManager.getFaceRectSize());
						executorService.execute(runnables[i]);
					}
					executorService.shutdown();
					while (executorService.isTerminated() == false) {
						try {
							TimeUnit.MILLISECONDS.sleep(100);
						} catch (Exception e) {
						}
					}
				} catch (Exception e) {
				}
				return null;
			}

			@Override
			protected void onPreExecute() {
				dialog = getProgressBar("Waiting...", false);
				dialog.show();
			};

			@Override
			protected void onPostExecute(Void result) {
				dialog.dismiss();

			};

		}.execute();

	}

	protected void setTouchSelectFace(int index) {
		if (-1 == index || resultRect.getSelectFaceNum() == index) {
			resultRect.setSelectFaceNum(-1);
			visibleControlLayout();

		} else {
			resultRect.setSelectFaceNum(index);
			visibleRectControlLayout();
		}
	}

	private void visibleControlLayout() {
		if (rectControlLayout != null) {
			controlLayout.setVisibility(View.VISIBLE);
			changeControlLayout.setVisibility(View.INVISIBLE);
			rectControlLayout.setVisibility(View.GONE);
		}
	}

	private void visibleRectControlLayout() {
		if (rectControlLayout != null) {
			controlLayout.setVisibility(View.GONE);
			changeControlLayout.setVisibility(View.INVISIBLE);
			rectControlLayout.setVisibility(View.VISIBLE);
		}
	}

	private ProgressDialog getProgressBar(String message, boolean cancelable) {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setMessage(message);
		dialog.setIndeterminate(true);
		dialog.setCancelable(cancelable);

		return dialog;
	}

	private void moveFaceButton(int left, int top, int right, int bottom) {
		LayoutParams leftLayoutParams = btnLeft.getLayoutParams();
		FrameLayout.LayoutParams FleftLayoutParams = new FrameLayout.LayoutParams(
				leftLayoutParams);

		FleftLayoutParams.leftMargin = left - btnLeft.getWidth() - 20;
		FleftLayoutParams.topMargin = top + ((bottom - top) >> 1)
				- (btnLeft.getHeight() >> 1);
		FleftLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		btnLeft.setLayoutParams(FleftLayoutParams);

		LayoutParams rightLayoutParams = btnRight.getLayoutParams();
		FrameLayout.LayoutParams FrightLayoutParams = new FrameLayout.LayoutParams(
				rightLayoutParams);

		FrightLayoutParams.leftMargin = right + 20;
		FrightLayoutParams.topMargin = top + ((bottom - top) >> 1)
				- (btnRight.getHeight() >> 1);
		FrightLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
		btnRight.setLayoutParams(FrightLayoutParams);

		btnLeft.invalidate();
		btnRight.invalidate();
	}

	private View resultView(Context context) {

		final Bitmap bm = FrameBitmapFactory.getBitmap(resultRect
				.getSelectFrame());

		resultView = new ResultView(context, bm);

		FrameLayout layout = new FrameLayout(context);
		layout.setBackgroundColor(Color.BLACK);
		layout.addView(resultView);
		return layout;

	}

	private Bitmap getResultViewBitmap() {
		resultView.buildDrawingCache();
		return resultView.getDrawingCache();
	}

	private void rectChange(int index) {
		resultView.setBackgroundBitmap(ndkManager.changeSURFFrame(index,
				resultRect.getSelectFaceNum()));
	}

	public int getRectSize() {
		return resultRect.size();
	}

	public void isCustomMode(boolean custom) {
		this.isCustomMode = custom;

		if (isCustomMode == true) {
			Toast.makeText(ResultActivity.this, TOAST_MESSAGE,
					Toast.LENGTH_SHORT).show();
		}
	}

	private void startImageFileSave() {

		onImageSave(getResultViewBitmap());
	}

	private void startContacts() {
		if (rect == null) {
			isCustomMode(true);

		} else {
			if (resultRect.getSelectFaceNum() != -1) {
				Bitmap bitmap = getResultViewBitmap();
				Bitmap resized = Bitmap.createBitmap(bitmap, rect.left,
						rect.top, rect.width(), rect.height());

				Intent intent = new Intent(ContactsListActivity.ACTION_INTENT);
				intent.putExtra(ContactsListActivity.KEY_RECT, resized);
				startActivityForResult(intent, REQUEST_CONTACTS);
			}
		}
	}

	private void startMosaic() {

		if (resultRect.size() < 1) {
			isCustomMode(true);
			return;
		}

		resultView.drawMosaic(MosaicFactory.getMosaic(getResultViewBitmap(),
				rect));

		resultView.invalidate();
	}

	private void onContacts() {
		startContacts();

	}

	private void onMosaic() {
		startMosaic();

	}

	private boolean onSave() {
		startImageFileSave();
		sendMessage(ID_EXIT);
		return true;
	}

	public LinearLayout getControlLayout() {
		return controlLayout;
	}

	protected void sendMessage(int message) {
		switch (message) {
		case ID_CUSTOM:
			onCustom();
			break;
		case ID_CANCEL:
			onBackPressed();
			break;
		case ID_SAVE:
			onSave();
			break;
		case ID_EXIT:
			onBackPressed();
			break;
		case ID_MOSAIC:
			onMosaic();
			break;
		case ID_CONTACTS:
			onContacts();
			break;
		}
	}

	public abstract LinearLayout controlLayout(Context context);

	public abstract FrameLayout rectControlLayout(Context context);

	public abstract boolean isChangeButtonLayout();

	public abstract void onCustom();

	protected class ActivityClickListener implements OnClickListener {

		public ActivityClickListener() {
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case ID_CUSTOM:
				sendMessage(ID_CUSTOM);
				break;
			case ID_CANCEL:
				sendMessage(ID_CANCEL);
				break;
			case ID_SAVE:
				sendMessage(ID_SAVE);
				break;
			case ID_EXIT:
				sendMessage(ID_EXIT);
				break;
			case ID_MOSAIC:
				sendMessage(ID_MOSAIC);
				break;
			case ID_CONTACTS:
				sendMessage(ID_CONTACTS);
				break;
			}

		}
	}

	protected class RectClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case ID_RECT_CANCEL:
				setTouchSelectFace(-1);
				break;
			case ID_RECT_MODIFY:
				if (changeControlLayout.getVisibility() == View.INVISIBLE)
					changeControlLayout.setVisibility(View.VISIBLE);
				else
					changeControlLayout.setVisibility(View.INVISIBLE);
				break;
			case ID_RECT_MOSAIC:
				startMosaic();
				break;
			case ID_RECT_CONTACTS:
				startContacts();

				break;
			}

		}
	}

	private class FrameChangeListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case ID_BTN_LEFT:
				if (selectIndex > 0)
					selectIndex--;
				else
					selectIndex = size - 1;
				rectChange(selectIndex);
				break;
			case ID_BTN_RIGHT:
				if (selectIndex < size - 1)
					selectIndex++;
				else
					selectIndex = 0;
				rectChange(selectIndex);
				break;
			}
		}

	}

}
