package com.soma.chronos.mosaic;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class MosaicValue {

	private Bitmap bitmap;
	private Rect rect;

	public MosaicValue(Bitmap bitmap, Rect rect) {
		this.bitmap = bitmap;
		this.rect = rect;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Rect getRect() {
		return rect;
	}

	public void setRect(Rect rect) {
		this.rect = rect;
	}

}
