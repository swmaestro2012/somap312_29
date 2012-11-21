package com.soma.chronos.gallery;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.soma.chronos.root.RootLayout;

public class GalleryDrawView extends RootLayout {

	public GalleryDrawView(Context context) {
		super(context);

		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				PARAMS_WRAP_MATCH);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);

		RelativeLayout layout = new RelativeLayout(context);
		layout.addView(initLayout(context), params);
		addView(layout);
	}

	private ViewGroup initLayout(Context context) {
		LinearLayout linearLayout = controlLayout(context);

		ScrollView scrollView = new ScrollView(context);
		scrollView.addView(linearLayout);

		return scrollView;
	}

	private LinearLayout controlLayout(Context context) {
		LinearLayout layout = new LinearLayout(context);
		layout.addView(new Button(context));
		layout.addView(new Button(context));
		layout.addView(new Button(context));
		layout.addView(new Button(context));
		layout.addView(new Button(context));
		layout.addView(new Button(context));

		return layout;
	}

}
