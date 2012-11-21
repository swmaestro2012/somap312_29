package com.soma.chronos.camera;

import com.soma.chronos.preview.PreviewActivity;

public class DefaultCameraActivity extends CameraActivity {

	public static final String ACTION_INTENT = "chronos.intent.action.CAMERA.DEFAULT";

	@Override
	public int getBurstSize() {
		return 5;
	}

	@Override
	public String startNextActivity() {
		return PreviewActivity.ACTION_INTENT;
	}

}