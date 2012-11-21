package com.soma.chronos.contacts;

import com.soma.chronos.processing.ProcessingActivity;

public class ContactsProcessingActivity extends ProcessingActivity {

	public static final String ACTION_INTENT = "chronos.intent.action.PROCESSING.CONTACTS";

	@Override
	public boolean isGetSURF() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTemplateMatching() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String startNextActivity() {
		return ContactsResultActivity.ACTION_INTENT;
	}

}
