package com.soma.chronos.processing.runnable;

import android.util.Log;

import com.soma.chronos.MainActivity;
import com.soma.chronos.frame.Frames;

public class FindRunnable extends ProcessingRunnable {

	public FindRunnable(int id) {
		super(id);

	}

	@Override
	public void run() {
		Log.d(MainActivity.TAG, id + " - Find Start");
		ndkManager.findSurfFrame(Frames.getRgb(id));
		Log.d(MainActivity.TAG, id + " - Find End");
	}

}
