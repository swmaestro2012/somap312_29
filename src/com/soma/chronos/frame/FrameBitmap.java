package com.soma.chronos.frame;

import android.graphics.Bitmap;

import com.soma.chronos.util.DeviceMetrics;

public class FrameBitmap extends FrameBitmapImpl {

	private Bitmap bitmap = null;

	public FrameBitmap(int key) {
		byte[] yuv = Frames.getYuv(key);

		if (yuv == null) {
			try {
				throw new NullPointerException();
			} catch (Exception e) {
			}
		}

		final int w = DeviceMetrics.getWidth();
		final int h = DeviceMetrics.getHeight();

		int[] rgb = FrameConvert.decodeYUV420SP(yuv, w, h);

		bitmap = Bitmap.createBitmap(rgb, w, h, Bitmap.Config.ARGB_8888);

	}

	public FrameBitmap(String key) {
		this(Integer.parseInt(key));
	}

	@Override
	public Bitmap getBitmap() {
		return bitmap;
	}

}
