package com.soma.chronos.processing.runnable;

import android.util.Log;

import com.soma.chronos.MainActivity;
import com.soma.chronos.frame.Frames;

public class GetRunnable extends ProcessingRunnable {

	public GetRunnable(int id) {
		super(id);

	}

	@Override
	public void run() {
		Log.d(MainActivity.TAG, id + " - Get Start");
		ndkManager.getSurfFrame(Frames.getRgb(id), id);
		Log.d(MainActivity.TAG, id + " - Get End");
	}

}
