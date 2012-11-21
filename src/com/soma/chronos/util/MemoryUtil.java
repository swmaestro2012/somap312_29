package com.soma.chronos.util;

import android.os.Debug;

public class MemoryUtil {

	private static final String TAG = "Chronos::Memory";

	public static void showMemoryStatusLog() {
		android.util.Log.d(TAG,
				"Native heap size: " + (Debug.getNativeHeapSize() / (1024))
						+ "KB");
		android.util.Log.d(TAG,
				"Heap Free size : " + (Debug.getNativeHeapFreeSize() / (1024))
						+ "KB");
		android.util.Log.d(TAG,
				"Heap Allocated size : "
						+ (Debug.getNativeHeapAllocatedSize() / (1024)) + "KB");
		android.util.Log.d(TAG, "TOTAL MEMORY : "
				+ (Runtime.getRuntime().totalMemory() / (1024 * 1024)) + "MB");
		android.util.Log.d(TAG, "MAX MEMORY : "
				+ (Runtime.getRuntime().maxMemory() / (1024 * 1024)) + "MB");
		android.util.Log.d(TAG, "FREE MEMORY : "
				+ (Runtime.getRuntime().freeMemory() / (1024 * 1024)) + "MB");
		android.util.Log.d(TAG, "ALLOCATION MEMORY : "
				+ ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime()
						.freeMemory()) / (1024 * 1024)) + "MB");
	}

}
