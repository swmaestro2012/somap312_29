package com.soma.chronos;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.soma.chronos.root.RootActivity;

@SuppressLint("HandlerLeak")
public class SplashActivity extends RootActivity {
	public static final String ACTION_INTENT = "chronos.intent.action.SPLASH";

	private ImageView imageView = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		imageView = new ImageView(getApplicationContext());
		imageView.setScaleType(ScaleType.FIT_XY);
		imageView.setImageResource(R.drawable.splash_bg);

		setContentView(imageView, PARAMS_MATCH);

		AlphaAnimation alphaFadeOut = new AlphaAnimation(5, 0); // fade-in

		AnimationSet animationSet = new AnimationSet(true);
		animationSet.addAnimation(alphaFadeOut);
		animationSet.setRepeatMode(Animation.RESTART);
		animationSet.setDuration(3000);
		imageView.startAnimation(animationSet);

		initialize();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

	}

	private void initialize() {

		new Handler() {
			@Override
			public void handleMessage(Message msg) {
				overridePendingTransition(0, 0);
				finish();
			}
		}.sendEmptyMessageDelayed(0, 3000);

	}
}
