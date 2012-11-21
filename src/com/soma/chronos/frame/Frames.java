package com.soma.chronos.frame;

import java.util.ArrayList;
import java.util.List;

import com.soma.chronos.util.DeviceMetrics;

public class Frames {

	private volatile static Frames instance;

	public static Frames getInstance() {
		if (instance == null) {
			synchronized (Frames.class) {
				if (instance == null) {
					instance = new Frames();
				}
			}
		}
		return instance;
	}

	private int frameSize;

	private List<byte[]> yuvFrames;
	private List<int[]> rgbFrames;
	private List<byte[]> surfFrames;

	private Frames() {
		yuvFrames = new ArrayList<byte[]>();
		rgbFrames = new ArrayList<int[]>();
		surfFrames = new ArrayList<byte[]>();
	}

	public static int size() {
		return getInstance().frameSize;
	}

	public static void init(int frameSize) {
		getInstance().clearAll();
		getInstance().setFrameSize(frameSize);

	}

	private void setFrameSize(int frameSize) {
		this.frameSize = frameSize;
	}

	public List<byte[]> getYuvList() {
		return yuvFrames;
	}

	public List<int[]> getRgbList() {
		return rgbFrames;
	}

	public List<byte[]> getSurfList() {
		return surfFrames;
	}

	private byte[] getYuvFrame(int location) {
		if (yuvFrames != null) {
			if (location < yuvFrames.size())
				return yuvFrames.get(location);
		} else {
			return null;
		}
		return null;
	}

	public static byte[] getYuv(int location) {
		return getInstance().getYuvFrame(location);
	}

	public static int[] getRgb(int location) {
		return getInstance().getRgbFrame(location);
	}

	private int[] getRgbFrame(int location) {
		if (rgbFrames != null) {
			if (location < rgbFrames.size())
				return rgbFrames.get(location);
		} else {
			return null;
		}
		return null;
	}

	public static byte[] getSurf(int location) {
		return getInstance().getSurfFrame(location);
	}

	private byte[] getSurfFrame(int location) {
		if (surfFrames != null) {
			if (location < surfFrames.size())
				return surfFrames.get(location);
		} else {
			return null;
		}
		return null;
	}

	public static void addYUV(byte[] frame) {
		getInstance().addYuvFrame(frame);
	}

	public void addYuvFrame(byte[] frame) {
		if (yuvFrames != null) {
			if (yuvFrames.size() >= frameSize) {
				yuvFrames.clear();
			}
			yuvFrames.add(frame);
		} else {
			yuvFrames = new ArrayList<byte[]>();
			yuvFrames.add(frame);
		}
	}

	public static void addRGB(byte[] yuv) {

		final int w = DeviceMetrics.getWidth();
		final int h = DeviceMetrics.getHeight();

		int[] rgb = null;

		if (w > 0 && h > 0) {
			rgb = FrameConvert.decodeYUV420SP(yuv, w, h);
		}
		getInstance().addRGBFrame(rgb);
	}

	public static void addRGB(int[] rgb) {
		getInstance().addRGBFrame(rgb);
	}

	public void addRGBFrame(int[] rgb) {
		if (rgbFrames != null) {
			if (rgbFrames.size() >= frameSize) {
				rgbFrames.clear();
			}
			rgbFrames.add(rgb);
		} else {
			rgbFrames = new ArrayList<int[]>();
			rgbFrames.add(rgb);
		}
	}

	public static void addSURF(byte[] frame) {
		getInstance().addSurfFrame(frame);
	}

	public void addSurfFrame(byte[] frame) {
		if (surfFrames != null) {
			if (surfFrames.size() >= frameSize) {
				surfFrames.clear();
			}
			surfFrames.add(frame);
		} else {
			surfFrames = new ArrayList<byte[]>();
			surfFrames.add(frame);
		}
	}

	public static void setSURF(int location, byte[] frame) {
		getInstance().setSurfFrame(location, frame);
	}

	public void setSurfFrame(int location, byte[] frame) {
		if (surfFrames != null) {
			if (surfFrames.size() >= frameSize) {
				surfFrames.clear();
			}
			surfFrames.set(location, frame);
		}
	}

	public static void clear() {
		Frames.getInstance().clearAll();
	}

	public static void removeAll() {
		Frames.getInstance().clearAll();
		Frames.getInstance().remove();
	}

	private void clearAll() {
		if (yuvFrames != null) {
			yuvFrames.clear();
		}

		if (rgbFrames != null) {
			rgbFrames.clear();
		}

		if (surfFrames != null) {
			surfFrames.clear();
		}
	}

	private void remove() {
		yuvFrames = null;
		rgbFrames = null;
		surfFrames = null;

	}

}
