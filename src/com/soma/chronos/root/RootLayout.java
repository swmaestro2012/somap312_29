package com.soma.chronos.root;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews.RemoteView;

import com.soma.chronos.frame.FrameBitmapFactory;

@RemoteView
public abstract class RootLayout extends ViewGroup implements
		RootInterface {

	public RootLayout(Context context) {
		super(context);
	}

	protected ImageView resizedImageView(Context context, int resourceId,
			int viewId, int width, int height) {

		return resizedImageView(context, resourceId, viewId, width, height,
				null);
	}

	protected ImageView resizedImageView(Context context, int resourceId,
			int viewId) {
		final int w = 100;
		final int h = 100;

		return resizedImageView(context, resourceId, viewId, w, h, null);
	}

	protected ImageView resizedImageView(Context context, int resourceId,
			int viewId, OnClickListener listener) {
		final int w = 100;
		final int h = 100;

		return resizedImageView(context, resourceId, viewId, w, h, listener);
	}

	protected ImageView resizedImageView(Context context, int resourceId,
			int viewId, int width, int height, OnClickListener listener) {
		ImageView view = new ImageView(context);
		view.setImageBitmap(resizedBitmap(context.getResources(), resourceId,
				width, height));
		view.setId(viewId);
		view.setOnClickListener(listener);

		return view;
	}

	protected Bitmap resizedBitmap(Resources res, int id, int width, int height) {
		Bitmap bitmap = BitmapFactory.decodeResource(res, id);
		int bmW = bitmap.getWidth();
		int bmH = bitmap.getHeight();
		float scaleWidth = ((float) width) / bmW;
		float scaleHeight = ((float) height) / bmH;

		// createa matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the bit map

		matrix.postScale(scaleWidth, scaleHeight);

		return Bitmap.createBitmap(bitmap, 0, 0, bmW, bmH, matrix, true);
	}

	protected void onImageSave(String filename) {
		File copyFile = new File(PATH + filename);
		OutputStream out = null;

		try {
			final int index = 0;
			Bitmap bitmap = FrameBitmapFactory.getBitmap(index);

			copyFile.createNewFile();
			out = new FileOutputStream(copyFile);

			bitmap.compress(CompressFormat.PNG, 100, out);

			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int childLeft = 0;
		final int childCount = getChildCount();

		for (int i = 0; i < childCount; i++) {
			final View childView = getChildAt(i);

			final int childWidth = childView.getMeasuredWidth();
			final int childHeight = childView.getMeasuredHeight();
			childView.layout(childLeft, 0, childLeft + childWidth, childHeight);

			childLeft += childWidth;
		}

	}

}
