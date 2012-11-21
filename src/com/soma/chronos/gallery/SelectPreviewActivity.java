package com.soma.chronos.gallery;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samsung.sdraw.CanvasView;
import com.samsung.sdraw.SDrawLibrary;
import com.samsung.sdraw.SettingView;
import com.soma.chronos.MainActivity;
import com.soma.chronos.R;
import com.soma.chronos.root.RootActivity;

public class SelectPreviewActivity extends RootActivity {

	public static final String ACTION_INTENT = "chronos.intent.action.SELECT";
	public static final String KEY_SELECT = "select";

	private static final int ID_PEN = 1;
	private static final int ID_ERASER = 2;
	private static final int ID_SAVE = 3;
	private static final int ID_CLOSE = 4;

	private CanvasView canvas;
	private SettingView settings;

	private FrameLayout frameLayout = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getIntent().getExtras();
		String path = bundle.getString(KEY_SELECT);

		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Bitmap bm = BitmapFactory.decodeStream(fis);

		ImageView imageView = new ImageView(getApplicationContext());
		imageView.setScaleType(ScaleType.FIT_XY);
		imageView.setImageBitmap(bm);

		frameLayout = new FrameLayout(getApplicationContext());
		frameLayout.addView(imageView);
		frameLayout.addView(initCanvasView(getApplicationContext()));
		frameLayout.addView(getControlView(getApplicationContext()));
		setContentView(frameLayout, PARAMS_MATCH);

	}

	private ViewGroup initCanvasView(final Context context) {

		canvas = new CanvasView(context);
		settings = new SettingView(context);

		canvas.setSettingView(settings);

		if (!SDrawLibrary.isSupportedModel()) {
			Log.e(MainActivity.TAG, "S-Pen Not supported model.");
			Toast.makeText(getApplicationContext(),
					"S-Pen Not supported model.", Toast.LENGTH_SHORT).show();
			finish();
		}

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				PARAMS_WRAP_MATCH);
		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);

		RelativeLayout layout = new RelativeLayout(context);

		layout.addView(canvas, PARAMS_MATCH);
		layout.addView(settings, PARAMS_MATCH);
		return layout;
	}

	private RelativeLayout getControlView(Context context) {

		LinearLayout controlLayout = getControlLayout(context);

		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
				MATCH_PARENT, WRAP_CONTENT);
		lp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

		RelativeLayout layout = new RelativeLayout(context);
		layout.addView(controlLayout, lp);

		return layout;

	}

	private LinearLayout getControlLayout(Context context) {

		ImageView view1 = resizedImageView(R.drawable.menu_spen, ID_PEN,
				new MenuClickListener());
		view1.setPadding(10, 0, 10, 0);

		ImageView view2 = resizedImageView(R.drawable.menu_eraser, ID_ERASER,
				new MenuClickListener());
		view2.setPadding(10, 0, 10, 0);

		ImageView view3 = resizedImageView(R.drawable.cancel, ID_CLOSE,
				new MenuClickListener());
		view3.setPadding(10, 0, 10, 0);

		ImageView view4 = resizedImageView(R.drawable.save, ID_SAVE,
				new MenuClickListener());
		view4.setPadding(10, 0, 10, 0);

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setGravity(Gravity.RIGHT);
		layout.addView(view1);
		layout.addView(view2);
		layout.addView(view3);
		layout.addView(view4);

		return layout;
	}

	private class MenuClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case ID_SAVE:
				frameLayout.buildDrawingCache();
				Bitmap bitmap = frameLayout.getDrawingCache();
				onImageSave(bitmap);
				break;
			case ID_CLOSE:
				finish();
				break;
			case ID_PEN:
				canvas.changeModeTo(CanvasView.PEN_MODE);
				if (settings.isShown(SettingView.PEN_SETTING_VIEW)) {
					settings.closeView();
				} else {
					settings.showView(SettingView.PEN_SETTING_VIEW);
				}
				break;
			case ID_ERASER:
				canvas.changeModeTo(CanvasView.ERASER_MODE);
				if (settings.isShown(SettingView.ERASER_SETTING_VIEW)) {
					settings.closeView();
				} else {
					settings.showView(SettingView.ERASER_SETTING_VIEW);
				}
				break;
			}
		}
	}
}
