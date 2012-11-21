package com.soma.chronos.contacts;

import com.soma.chronos.camera.CameraActivity;

public class ContactsCameraActivity extends CameraActivity {
	public static final String ACTION_INTENT = "chronos.intent.action.CONTACTS.CAMERA";

	@Override
	public int getBurstSize() {
		return 1;
	}

	@Override
	public String startNextActivity() {
		return ContactsProcessingActivity.ACTION_INTENT;
	}

}
