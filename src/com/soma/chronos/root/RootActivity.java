package com.soma.chronos.root;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

import test.ImageProcessing.NDKManager;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import com.soma.chronos.MainActivity;
import com.soma.chronos.frame.FrameBitmapFactory;
import com.soma.chronos.frame.Frames;
import com.soma.chronos.util.RecycleUtils;

public abstract class RootActivity extends Activity implements RootInterface {

	public static final String ACTION_INTENT = "";

	public static final String MENU_CAMERA = "camera";
	public static final String MENU_GALLERY = "gallery";
	public static final String MENU_CONTACTS = "contacts";
	public static final String MENU_MOSAIC = "mosaic";
	public static final String MENU_GRID = "grid";

	private boolean isSave = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		destory(getWindow());
	}

	protected void startIntent(String action) {
		startActivity(new Intent(action));
		overridePendingTransition(0, 0);

	}

	public static void removeAll() {
		NDKManager.removeAll();
		Frames.removeAll();
		FrameBitmapFactory.removeAll();
		System.gc();
	}

	public static void destory(Window window) {
		RecycleUtils.recursiveRecycle(window.getDecorView());
	}

	protected ImageView resizedImageView(int resourceId, int viewId,
			OnClickListener listener) {

		return resizedImageView(resourceId, IMAGE_DEFAULT_WIDTH,
				IMAGE_DEFAULT_HEIGHT, viewId, listener);
	}

	protected ImageView resizedImageView(int resourceId, int width, int height,
			int viewId, OnClickListener listener) {
		ImageView view = new ImageView(getApplicationContext());
		view.setImageBitmap(resizedBitmap(getResources(), resourceId, width,
				height));
		view.setId(viewId);
		view.setOnClickListener(listener);

		return view;
	}

	protected Bitmap resizedBitmap(Bitmap bitmap, int width, int height) {
		int bmW = bitmap.getWidth();
		int bmH = bitmap.getHeight();
		float scaleWidth = ((float) width) / bmW;
		float scaleHeight = ((float) height) / bmH;

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map

		matrix.postScale(scaleWidth, scaleHeight);

		return Bitmap.createBitmap(bitmap, 0, 0, bmW, bmH, matrix, true);
	}

	protected Bitmap resizedBitmap(Resources res, int id, int width, int height) {
		Bitmap bitmap = BitmapFactory.decodeResource(res, id);
		int bmW = bitmap.getWidth();
		int bmH = bitmap.getHeight();
		float scaleWidth = ((float) width) / bmW;
		float scaleHeight = ((float) height) / bmH;

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map

		matrix.postScale(scaleWidth, scaleHeight);

		return Bitmap.createBitmap(bitmap, 0, 0, bmW, bmH, matrix, true);
	}

	protected synchronized void onImageSave(int index) {

		if (isSave == false) {
			isSave = true;

			File file = new File(getFileName());
			OutputStream out = null;

			try {
				File dir = new File(PATH + "/" + APP_NAME);
				if (!dir.mkdirs()) {
					Toast.makeText(getApplicationContext(),
							"파일을 저장 하지 못 하였습니다...", Toast.LENGTH_LONG).show();
					Log.e(MainActivity.TAG, APP_NAME + "Directory Failed");
					isSave = false;
					return;
				}
				Bitmap bitmap = FrameBitmapFactory.getBitmap(index);

				file.createNewFile();
				out = new FileOutputStream(file);
				if (file.exists()) {
					bitmap.compress(CompressFormat.PNG, 100, out);
				}
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Toast.makeText(getApplicationContext(), getFileName(),
					Toast.LENGTH_LONG).show();
			isSave = false;

		}

	}

	protected synchronized void onImageSave(Bitmap bitmap) {
		if (isSave == false) {
			isSave = true;

			File file = new File(getFileName());
			OutputStream out = null;

			try {
				File dir = new File(PATH + "/" + APP_NAME);
				if (!dir.mkdirs()) {
					Toast.makeText(getApplicationContext(),
							"파일을 저장 하지 못 하였습니다...", Toast.LENGTH_LONG).show();
					Log.e(MainActivity.TAG, APP_NAME + "Directory Failed");
					isSave = false;
					return;
				}

				file.createNewFile();
				out = new FileOutputStream(file);

				bitmap.compress(CompressFormat.PNG, 100, out);

				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Toast.makeText(getApplicationContext(), getFileName(),
					Toast.LENGTH_LONG).show();
			isSave = false;
		}

	}

	protected String getFileName() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(PATH);
		buffer.append("/");
		buffer.append(APP_NAME);
		buffer.append("/");
		buffer.append(getToday());
		buffer.append(".png");

		return buffer.toString();
	}

	protected StringBuffer getToday() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int second = calendar.get(Calendar.SECOND);

		StringBuffer buffer = new StringBuffer();
		buffer.append(String.valueOf(year));
		buffer.append(String.valueOf(month));
		buffer.append(String.valueOf(day));
		buffer.append(String.valueOf(hour));
		buffer.append(String.valueOf(minute));
		buffer.append(String.valueOf(second));

		return buffer;
	}

}
