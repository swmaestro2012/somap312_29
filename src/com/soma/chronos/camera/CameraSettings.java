package com.soma.chronos.camera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class CameraSettings implements CameraParams {
	private volatile static CameraSettings instance;

	public static CameraSettings getInstance() {
		if (instance == null) {
			synchronized (CameraSettings.class) {
				if (instance == null) {
					instance = new CameraSettings();
				}
			}
		}
		return instance;
	}

	private CameraSettings() {

	}

	private static Map<String, String> map = new HashMap<String, String>();

	public static void initSetting(int minZoom, int maxZoom, int minExposure,
			int maxExposure, String isoValues) {

		map.put(KEY_MAX_ZOOM, String.valueOf(maxZoom));
		map.put(KEY_MIN_ZOOM, String.valueOf(minZoom));

		map.put(KEY_MAX_EXPOSURE, String.valueOf(maxExposure));
		map.put(KEY_MIN_EXPOSURE, String.valueOf(minExposure));

		Scanner scanner = new Scanner(isoValues);
		scanner.useDelimiter(",");

		while (scanner.hasNext()) {
			String s = scanner.next();
			map.put(s, s);

		}

		map.put(KEY_ZOOM, ZERO);
		map.put(KEY_EXPOSURE, ZERO);
		map.put(KEY_ISO, AUTO);
		map.put(KEY_SCENE, SCENE_MODE_AUTO);
		map.put(KEY_WHITE_BALANCE, WHITE_BALANCE_AUTO);
		map.put(KEY_EFFECT, EFFECT_NONE);

	}

	public static List<String> getIsoList() {
		if (map == null)
			map = new HashMap<String, String>();

		List<String> list = new ArrayList<String>();
		Set<String> set = map.keySet();

		for (String string : set) {
			if (!string.startsWith(KEY))
				list.add(string);
		}

		Collections.sort(list);

		return list;
	}

	public static String getString(String key) {
		if (map == null)
			map = new HashMap<String, String>();

		if (!map.containsKey(key))
			return "null";

		return map.get(key);

	}

	public static int getInt(String key) {
		int v = 0;
		try {

			if (map == null)
				map = new HashMap<String, String>();

			if (map.containsKey(key)) {
				if (key.equals(KEY_ISO))
					v = 0;
				else
					v = Integer.parseInt(map.get(key));
			}

		} catch (Exception e) {
			return v;
		}
		return v;

	}

	public static boolean set(String key, String value) {
		if (map == null)
			return false;

		if (!map.containsKey(key))
			return false;

		if (key.equals(KEY_ZOOM)) {
			return getInstance().setZoom(value);
		}

		if (key.equals(KEY_EXPOSURE)) {
			return getInstance().setExposure(value);
		}

		if (key.equals(KEY_ISO)) {
			return getInstance().setIso(value);
		}

		if (key.equals(KEY_WHITE_BALANCE)) {
			map.put(KEY_WHITE_BALANCE, value);
		}

		if (key.equals(KEY_SCENE)) {
			map.put(KEY_SCENE, value);
		}

		return false;
	}

	private boolean setZoom(String value) {
		try {
			String maxs = map.get(KEY_MAX_ZOOM);
			String mins = map.get(KEY_MIN_ZOOM);

			int maxi = Integer.parseInt(maxs);
			int mini = Integer.parseInt(mins);

			int v = Integer.parseInt(value);

			if (v >= maxi)
				map.put(KEY_ZOOM, maxs);
			else if (v <= mini)
				map.put(KEY_ZOOM, mins);
			else
				map.put(KEY_ZOOM, value);

		} catch (Exception e) {
			return false;
		}

		return true;

	}

	private boolean setExposure(String value) {
		try {
			String maxs = map.get(KEY_MAX_EXPOSURE);
			String mins = map.get(KEY_MIN_EXPOSURE);

			int maxi = Integer.parseInt(maxs);
			int mini = Integer.parseInt(mins);

			int v = Integer.parseInt(value);

			if (v >= mini && v <= maxi)
				map.put(KEY_EXPOSURE, value);

		} catch (Exception e) {
			return false;
		}

		return true;

	}

	private boolean setIso(String value) {
		if (map.containsKey(value)) {
			map.put(KEY_ISO, value);
			return true;
		} else {
			return false;
		}
	}
}
