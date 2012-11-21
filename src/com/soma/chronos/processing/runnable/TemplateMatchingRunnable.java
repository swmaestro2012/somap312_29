package com.soma.chronos.processing.runnable;

import android.util.Log;

import com.soma.chronos.MainActivity;

public class TemplateMatchingRunnable extends ProcessingRunnable {

	public TemplateMatchingRunnable(int id) {
		super(id);

	}

	@Override
	public void run() {
		Log.d(MainActivity.TAG, id + " - Template Matching Start");
		for (int i = 0; i < getCountFaceRect(); i++)
			ndkManager.templateMatching(id, i);
		Log.d(MainActivity.TAG, id + " - Template Matching End");
	}
}
