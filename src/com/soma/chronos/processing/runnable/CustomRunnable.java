package com.soma.chronos.processing.runnable;

import android.util.Log;

import com.soma.chronos.MainActivity;

public class CustomRunnable extends ProcessingRunnable {
	private int customIndex = 0;

	public CustomRunnable(int id) {
		super(id);

	}

	@Override
	public void run() {
		Log.d(MainActivity.TAG, id + " - Custom Start");
		customIndex = getCountFaceRect();
		if (customIndex > 0)
			customIndex--;
		ndkManager.templateMatching(id, customIndex);
		Log.d(MainActivity.TAG, id + " - Custom End");
	}

}
