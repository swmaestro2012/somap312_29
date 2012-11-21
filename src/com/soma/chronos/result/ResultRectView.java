package com.soma.chronos.result;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

public class ResultRectView extends View {

	private Paint paint;
	private Paint selectPaint;
	private ResultRect rect;

	private ResultRectView(Context context) {
		super(context);
		initPaint();
	}

	public ResultRectView(Context context, ResultRect rect) {
		this(context);
		this.rect = rect;
	}

	private void initPaint() {
		paint = new Paint();
		paint.setColor(Color.WHITE);
		selectPaint = new Paint();
		selectPaint.setColor(Color.RED);
	}

	public void drawRectLine(Canvas canvas, Rect rect, Paint paint) {

		canvas.drawLine(rect.left, rect.top, rect.right, rect.top, paint);
		canvas.drawLine(rect.right, rect.top, rect.right, rect.bottom, paint);
		canvas.drawLine(rect.right, rect.bottom, rect.left, rect.bottom, paint);
		canvas.drawLine(rect.left, rect.bottom, rect.left, rect.top, paint);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		try {
			if (rect.isFaceDectect()) {
				for (int i = 0; i < rect.size(); i++) {

					if (rect.getSelectFaceNum() == i) {
						drawRectLine(canvas, rect.get(i), selectPaint);
					} else {
						drawRectLine(canvas, rect.get(i), paint);
					}
				}

			}

			if (rect.isTouchMode())
				canvas.drawCircle(rect.getRx(), rect.getRy(), 10, selectPaint);

		} catch (NullPointerException e) {
			e.getStackTrace();
		}
	}

}
