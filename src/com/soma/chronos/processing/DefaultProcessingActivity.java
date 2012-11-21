package com.soma.chronos.processing;

import com.soma.chronos.result.DefaultResultActivity;

public class DefaultProcessingActivity extends ProcessingActivity {
	public static final String ACTION_INTENT = "chronos.intent.action.PROCESSING.DEFAULT";

	@Override
	public boolean isGetSURF() {
		return true;
	}

	@Override
	public boolean isTemplateMatching() {
		return true;
	}

	@Override
	public String startNextActivity() {
		return DefaultResultActivity.ACTION_INTENT;
	}

}
