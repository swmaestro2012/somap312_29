package com.soma.chronos.util;

public class DeviceMetrics {

	private volatile static DeviceMetrics instance;

	private static DeviceMetrics getInstance() {
		if (instance == null) {
			synchronized (DeviceMetrics.class) {
				if (instance == null) {
					instance = new DeviceMetrics();
				}
			}
		}
		return instance;
	}

	private DeviceMetrics() {

	}

	private int width;
	private int height;

	private float xdpi;
	private float ydpi;

	public static int getWidth() {
		return getInstance().width;
	}

	public static void setWidth(int width) {
		getInstance().width = width;
	}

	public static int getHeight() {
		return getInstance().height;
	}

	public static void setHeight(int height) {
		getInstance().height = height;
	}

	public static float getXdpi() {
		return getInstance().xdpi;
	}

	public static void setXdpi(float xdpi) {
		getInstance().xdpi = xdpi;
	}

	public static float getYdpi() {
		return getInstance().ydpi;
	}

	public static void setYdpi(float ydpi) {
		getInstance().ydpi = ydpi;
	}

}
