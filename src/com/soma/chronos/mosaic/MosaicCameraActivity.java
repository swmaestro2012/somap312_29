package com.soma.chronos.mosaic;

import com.soma.chronos.camera.CameraActivity;

public class MosaicCameraActivity extends CameraActivity {

	public static final String ACTION_INTENT = "chronos.intent.action.CAMERA.MOSAIC";

	@Override
	public int getBurstSize() {
		return 1;
	}

	@Override
	public String startNextActivity() {
		// TODO Auto-generated method stub
		return MosaicProcessingActivity.ACTION_INTENT;
	}
}
