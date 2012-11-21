package com.soma.chronos.result;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.soma.chronos.R;

public class DefaultResultActivity extends ResultActivity {

	public static final String ACTION_INTENT = "chronos.intent.action.RESULT.DEFAULT";

	@Override
	public LinearLayout controlLayout(Context context) {

		ImageView view1 = resizedImageView(R.drawable.cancel, ID_CANCEL,
				new ActivityClickListener());
		view1.setPadding(5, 0, 5, 0);

		ImageView view2 = resizedImageView(R.drawable.menu_custom, ID_CUSTOM,
				new ActivityClickListener());
		view2.setPadding(5, 0, 5, 0);

		ImageView view3 = resizedImageView(R.drawable.save, ID_SAVE,
				new ActivityClickListener());
		view3.setPadding(5, 0, 5, 0);

		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setGravity(Gravity.RIGHT);
		layout.setBackgroundColor(COLOR_BG);
		layout.addView(view1);
		layout.addView(view2);
		layout.addView(view3);

		return layout;
	}

	@Override
	public FrameLayout rectControlLayout(Context context) {

		ImageView view1 = resizedImageView(R.drawable.cancel, ID_RECT_CANCEL,
				new RectClickListener());
		view1.setPadding(5, 0, 5, 0);

		ImageView view2 = resizedImageView(R.drawable.menu_modify,
				ID_RECT_MODIFY, new RectClickListener());
		view2.setPadding(5, 0, 5, 0);

		ImageView view3 = resizedImageView(R.drawable.menu_mosaic,
				ID_RECT_MOSAIC, new RectClickListener());
		view3.setPadding(5, 0, 5, 0);

		ImageView view4 = resizedImageView(R.drawable.menu_contacts,
				ID_RECT_CONTACTS, new RectClickListener());
		view4.setPadding(5, 0, 5, 0);

		LinearLayout linearLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.setGravity(Gravity.RIGHT);
		linearLayout.setBackgroundColor(COLOR_BG);
		linearLayout.addView(view1);
		linearLayout.addView(view2);
		linearLayout.addView(view3);
		linearLayout.addView(view4);

		FrameLayout layout = new FrameLayout(context);
		layout.setVisibility(View.GONE);
		layout.addView(linearLayout);
		return layout;
	}

	@Override
	public boolean isChangeButtonLayout() {
		return true;
	}

	@Override
	public void onCustom() {
		isCustomMode(true);
	}

}
