package com.soma.chronos.mosaic;

import com.soma.chronos.processing.ProcessingActivity;

public class MosaicProcessingActivity extends ProcessingActivity {

	public static final String ACTION_INTENT = "chronos.intent.action.PROCESSING.MOSAIC";

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
		// TODO Auto-generated method stub
		return MosaicResultActivity.ACTION_INTENT;
	}

}
