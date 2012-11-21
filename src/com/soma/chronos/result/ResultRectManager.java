package com.soma.chronos.result;

import java.util.List;

import android.graphics.Rect;

public class ResultRectManager {

	public static void addSetRect7x(List<Rect> rects) {
		try {
			int faceW, faceH;
			Rect rect = rects.get(rects.size() - 1);
			faceW = (rect.right - rect.left) * 3;
			faceH = (rect.bottom - rect.top) * 3;
			rect.left -= faceW;
			rect.top -= faceH;
			rect.right += faceW;
			rect.bottom += faceH;
			rects.set(rects.size() - 1, rect);

		} catch (NullPointerException e) {
			e.getStackTrace();
		}
	}

	public static void setRect7x(List<Rect> rects) {
		try {
			int faceW, faceH;
			for (int i = 0; i < rects.size(); i++) {
				Rect rect = rects.get(i);
				faceW = (rect.right - rect.left) * 3;
				faceH = (rect.bottom - rect.top) * 3;
				rect.left -= faceW;
				rect.top -= faceH;
				rect.right += faceW;
				rect.bottom += faceH;
				rects.set(i, rect);
			}
		} catch (NullPointerException e) {
			e.getStackTrace();
		}
	}

	public static void setRect7x(List<Rect> rects, int index) {
		try {
			int faceW, faceH;
			Rect rect = rects.get(index);
			faceW = (rect.right - rect.left) * 3;
			faceH = (rect.bottom - rect.top) * 3;
			rect.left -= faceW;
			rect.top -= faceH;
			rect.right += faceW;
			rect.bottom += faceH;
			rects.set(index, rect);
		} catch (NullPointerException e) {
			e.getStackTrace();
		}
	}

	public static void setRect7x(Rect rect) {
		try {
			int faceW, faceH;
			faceW = (rect.right - rect.left) * 3;
			faceH = (rect.bottom - rect.top) * 3;
			rect.left -= faceW;
			rect.top -= faceH;
			rect.right += faceW;
			rect.bottom += faceH;
		} catch (NullPointerException e) {
			e.getStackTrace();
		}
	}

	public static void addSetRectRatio(List<Rect> rects, double ratio) {
		try {
			int faceW, faceH;
			Rect rect = rects.get(rects.size() - 1);
			faceW = (int) ((rect.right - rect.left) * ratio);
			faceH = (int) ((rect.bottom - rect.top) * ratio);
			rect.left += faceW;
			rect.top += faceH;
			rect.right -= faceW;
			rect.bottom -= faceH;
			rects.set(rects.size() - 1, rect);

		} catch (NullPointerException e) {
			e.getStackTrace();
		}
	}

	public static void setRectRatio(List<Rect> rects, double ratio) {
		try {
			int faceW, faceH;
			for (int i = 0; i < rects.size(); i++) {
				Rect rect = rects.get(i);
				faceW = (int) ((rect.right - rect.left) * ratio);
				faceH = (int) ((rect.bottom - rect.top) * ratio);
				rect.left += faceW;
				rect.top += faceH;
				rect.right -= faceW;
				rect.bottom -= faceH;
				if (rect.left > rect.right) {
					int temp = rect.left;
					rect.left = rect.right;
					rect.right = temp;
				}
				if (rect.top > rect.bottom) {
					int temp = rect.top;
					rect.top = rect.bottom;
					rect.bottom = temp;
				}
				rects.set(i, rect);
			}
		} catch (NullPointerException e) {
			e.getStackTrace();
		}
	}
}
