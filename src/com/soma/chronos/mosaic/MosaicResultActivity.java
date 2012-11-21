package com.soma.chronos.mosaic;

import android.content.Context;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.soma.chronos.R;
import com.soma.chronos.result.ResultActivity;

public class MosaicResultActivity extends ResultActivity {
	public static final String ACTION_INTENT = "chronos.intent.action.RESULT.MOSAIC";

	@Override
	public LinearLayout controlLayout(Context context) {

		ImageView view1 = resizedImageView(R.drawable.menu_mosaic, ID_MOSAIC,
				new ActivityClickListener());
		view1.setPadding(10, 0, 10, 0);

		ImageView view2 = resizedImageView(R.drawable.menu_custom, ID_CUSTOM,
				new ActivityClickListener());
		view2.setPadding(10, 0, 10, 0);

		ImageView view3 = resizedImageView(R.drawable.cancel, ID_CANCEL,
				new ActivityClickListener());
		view3.setPadding(10, 0, 10, 0);

		ImageView view4 = resizedImageView(R.drawable.save, ID_SAVE,
				new ActivityClickListener());
		view4.setPadding(10, 0, 10, 0);

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setGravity(Gravity.RIGHT);
		layout.setBackgroundColor(COLOR_BG);
		layout.addView(view1);
		layout.addView(view2);
		layout.addView(view3);
		layout.addView(view4);

		return layout;
	}

	@Override
	public FrameLayout rectControlLayout(Context context) {

		return null;
	}

	@Override
	public boolean isChangeButtonLayout() {
		return false;
	}

	@Override
	public void onCustom() {
		isCustomMode(true);
	}

}
