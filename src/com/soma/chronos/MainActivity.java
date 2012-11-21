package com.soma.chronos;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.soma.chronos.camera.DefaultCameraActivity;
import com.soma.chronos.contacts.ContactsCameraActivity;
import com.soma.chronos.gallery.GalleryActivity;
import com.soma.chronos.mosaic.MosaicCameraActivity;
import com.soma.chronos.root.RootActivity;
import com.soma.chronos.util.DeviceMetrics;

public class MainActivity extends RootActivity implements OnClickListener {

	public static final int ID_GALLERY = 0;
	public static final int ID_CONTACTS = 1;
	public static final int ID_MOSAIC = 2;
	public static final int ID_GRID = 3;
	public static final int ID_CAMERA = 4;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		startIntent(SplashActivity.ACTION_INTENT);

		initialize();
		copyAssets();

		setContentView(R.layout.activity_main);
		addContentView(getBaseGridLayout(getApplicationContext()), PARAMS_MATCH);

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "MainActivity Resume");
		removeAll();
	}

	private void initialize() {
		DisplayMetrics outMetrics = new DisplayMetrics();

		WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		display.getMetrics(outMetrics);

		// Landscape Mode -> Width = 1280 / Height = 800
		// Portrait Mode -> Width = 800 / Height = 1280
		DeviceMetrics.setWidth(outMetrics.widthPixels);
		DeviceMetrics.setHeight(outMetrics.heightPixels);

		DeviceMetrics.setXdpi(outMetrics.xdpi);
		DeviceMetrics.setYdpi(outMetrics.ydpi);

	}

	private View getMainIcon(Context context, int resId, int id, String message) {

		ImageView imageView = resizedImageView(resId, 170, 170, id, this);

		TextView textView = new TextView(context);
		textView.setTextColor(COLOR_F5F21F);
		textView.setGravity(Gravity.CENTER);
		textView.setText(message);
		textView.setTextSize(17);

		LinearLayout layout = new LinearLayout(context);
		layout.setPadding(DeviceMetrics.getWidth() / 30, 0,
				DeviceMetrics.getWidth() / 30, 0);

		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setGravity(Gravity.CENTER);
		layout.addView(imageView);
		layout.addView(textView);

		return layout;
	}

	private View getBaseGridLayout(Context context) {

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				PARAMS_WRAP);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

		RelativeLayout relativeLayout = new RelativeLayout(context);

		relativeLayout.addView(getLinearLayout(context), lp);

		return relativeLayout;

	}

	private View getLinearLayout(Context context) {

		View v0 = getMainIcon(context, R.drawable.main_gallery, ID_GALLERY,
				"Gallery");
		View v1 = getMainIcon(context, R.drawable.main_camera, ID_CAMERA,
				"Camera");
		View v2 = getMainIcon(context, R.drawable.main_mosaic, ID_MOSAIC,
				"Mosaic");
		View v3 = getMainIcon(context, R.drawable.main_contacts, ID_CONTACTS,
				"Contacts");

		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.addView(v0);
		linearLayout.addView(v1);
		linearLayout.addView(v2);
		linearLayout.addView(v3);

		return linearLayout;
	}

	private void copyAssets() {
		AssetManager assetManager = getAssets();
		String[] files = null;
		try {
			files = assetManager.list("");

			for (String filename : files) {
				Log.e(MainActivity.TAG, filename);
				InputStream in = null;
				OutputStream out = null;
				in = assetManager.open(filename);
				out = new FileOutputStream(PATH + filename);
				copyFile(in, out);
				in.close();
				in = null;
				out.flush();
				out.close();
				out = null;
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case ID_GALLERY:
			intent = new Intent(GalleryActivity.ACTION_INTENT);
			break;
		case ID_CONTACTS:
			intent = new Intent(ContactsCameraActivity.ACTION_INTENT);
			break;
		case ID_MOSAIC:
			intent = new Intent(MosaicCameraActivity.ACTION_INTENT);
			break;
		case ID_GRID:
			break;
		case ID_CAMERA:
			intent = new Intent(DefaultCameraActivity.ACTION_INTENT);
			break;
		}

		if (intent != null) {
			startActivity(intent);
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_in);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e(MainActivity.TAG, "MainActivity Destroy");
		destory(getWindow());
		removeAll();
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}

}
