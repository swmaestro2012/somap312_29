package com.soma.chronos.mosaic;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Rect;

public class MosaicFactory {

	public static MosaicValue getMosaic(Bitmap bitmap, Rect rect) {

		Bitmap a = bitmap.copy(Config.ARGB_8888, true);
		Bitmap b = Bitmap.createBitmap(a, rect.left, rect.top, rect.width(),
				rect.height());
		int[] rgb = mosaic(b);
		Bitmap c = Bitmap.createBitmap(rgb, b.getWidth(), b.getHeight(),
				Config.ARGB_8888);

		return new MosaicValue(c, rect);

	}

	public static List<MosaicValue> getMosaic(Bitmap bitmap, List<Rect> rects) {

		List<MosaicValue> list = new ArrayList<MosaicValue>();

		for (Rect rect : rects) {

			Bitmap a = bitmap.copy(Config.ARGB_8888, true);
			Bitmap b = Bitmap.createBitmap(a, rect.left, rect.top,
					rect.width(), rect.height());
			int[] rgb = mosaic(b);
			Bitmap c = Bitmap.createBitmap(rgb, b.getWidth(), b.getHeight(),
					Config.ARGB_8888);

			list.add(new MosaicValue(c, rect));
		}

		return list;

	}

	public static int[] mosaic(Bitmap bitmap) {
		Bitmap bm = bitmap.copy(Config.ARGB_8888, true);
		int picw = bm.getWidth();
		int pich = bm.getHeight();

		int[] pix = new int[picw * pich];
		int[] mosaic = new int[picw * pich];

		int rect = 20;
		int rectH = 20;
		int rectW = 20;
		int count = 0;
		int index = 0;
		int R = 0;
		int G = 0;
		int B = 0;

		bm.getPixels(pix, 0, picw, 0, 0, picw, pich);

		for (int y = 0; y < pich; y += rect)
			for (int x = 0; x < picw; x += rect) {

				count = 0;
				R = 0;
				G = 0;
				B = 0;

				if ((rectH + y) >= pich)
					rectH = pich % rect;
				else
					rectH = rect;

				if ((rectW + x) >= picw)
					rectW = picw % rect;
				else
					rectW = rect;

				for (int n = 0; n < rectH; n++) {

					for (int m = 0; m < rectW; m++) {
						index = (n + y) * (picw) + (m + x);
						R += Color.red(pix[index]);
						G += Color.green(pix[index]);
						B += Color.blue(pix[index]);

						count++;
					}
				}

				R /= count;
				G /= count;
				B /= count;

				for (int n = 0; n < rectH; n++) {

					for (int m = 0; m < rectW; m++) {

						try {

							index = (n + y) * (picw) + (m + x);

							mosaic[index] = Color.rgb(R, G, B);
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				}
			}

		return mosaic;

	}
}
