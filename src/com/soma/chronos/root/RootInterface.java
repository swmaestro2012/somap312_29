package com.soma.chronos.root;

import android.graphics.Color;
import android.os.Environment;
import android.view.ViewGroup.LayoutParams;

public interface RootInterface {

	public static final String APP_NAME = "Chronos";
	public static final String TAG = "Chronos::Main";
	public static final String PATH = Environment.getExternalStorageDirectory()
			.getAbsolutePath();

	public static final int COLOR_BG = Color.argb(80, 243, 126, 22);
	public static final int COLOR_F5F21F = Color.rgb(245, 242, 31);
	public static final int COLOR_ALPHA_GRAY = 0x77000000;

	public static final int MATCH_PARENT = LayoutParams.MATCH_PARENT;
	public static final int WRAP_CONTENT = LayoutParams.WRAP_CONTENT;

	public static final LayoutParams PARAMS_MATCH = new LayoutParams(
			MATCH_PARENT, MATCH_PARENT);

	public static final LayoutParams PARAMS_WRAP = new LayoutParams(
			WRAP_CONTENT, WRAP_CONTENT);

	public static final LayoutParams PARAMS_MATCH_WRAP = new LayoutParams(
			MATCH_PARENT, WRAP_CONTENT);

	public static final LayoutParams PARAMS_WRAP_MATCH = new LayoutParams(
			WRAP_CONTENT, MATCH_PARENT);

	public static final int IMAGE_DEFAULT_WIDTH = 100;
	public static final int IMAGE_DEFAULT_HEIGHT = 100;
	public static final int IMAGE_SMALL_WIDTH = 50;
	public static final int IMAGE_SMALL_HEIGHT = 50;

}
