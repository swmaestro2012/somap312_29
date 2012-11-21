package com.soma.chronos.frame;

import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;

public class FrameBitmapFactory {

	private static Map<String, FrameBitmapImpl> map = null;

	static {
		map = new HashMap<String, FrameBitmapImpl>();
	}

	public synchronized static Bitmap getBitmap(int key) {
		return get(String.valueOf(key));
	}

	public synchronized static Bitmap getBitmap(String key) {
		return get(key);
	}

	public synchronized static void removeAll() {

		if (map != null) {
			map.clear();
			map = null;
		}

	}

	private static Bitmap get(String key) {

		if (map == null)
			map = new HashMap<String, FrameBitmapImpl>();

		if (!map.containsKey(key)) {
			map.put(key, new FrameBitmap(key));
		}
		return map.get(key).getBitmap();
	}

}
