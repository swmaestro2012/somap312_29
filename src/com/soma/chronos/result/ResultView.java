package com.soma.chronos.result;

import java.util.List;

import com.soma.chronos.mosaic.MosaicValue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public class ResultView extends View {

	private Bitmap bitmap = null;
	private MosaicValue mosaicValue = null;
	private List<MosaicValue> mosaicValues = null;

	public ResultView(Context context) {
		super(context);
	}

	public ResultView(Context context, Bitmap bitmap) {
		this(context);
		this.bitmap = bitmap;

	}

	public void setBackgroundBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
		invalidate();

	}

	public void drawMosaic(MosaicValue mosaicValue) {
		this.mosaicValue = mosaicValue;

		invalidate();
	}

	public void drawMosaic(List<MosaicValue> mosaicValues) {
		this.mosaicValues = mosaicValues;

		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {

		canvas.drawBitmap(bitmap, 0, 0, null);
		if (mosaicValues != null) {
			for (MosaicValue value : mosaicValues) {
				Bitmap b = value.getBitmap();
				Rect r = value.getRect();
				canvas.drawBitmap(b, r.left, r.top, null);
			}
		}

		if (mosaicValue != null) {
			Bitmap b = mosaicValue.getBitmap();
			Rect r = mosaicValue.getRect();
			canvas.drawBitmap(b, r.left, r.top, null);
		}

	}
}
