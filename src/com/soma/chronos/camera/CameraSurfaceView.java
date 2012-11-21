package com.soma.chronos.camera;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PreviewCallback;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.soma.chronos.MainActivity;
import com.soma.chronos.frame.Frames;
import com.soma.chronos.util.DeviceMetrics;

public class CameraSurfaceView extends SurfaceView implements
		SurfaceHolder.Callback, CameraParams {

	private static final String ISO_VALUES = "iso-values";

	private byte[] frame = null;

	private CameraAutoFocus autoFocus = null;

	private HandlerThread handlerThread = null;
	private SurfaceHolder holder = null;
	private Camera camera = null;
	private Camera.Parameters params = null;

	@SuppressWarnings("deprecation")
	public CameraSurfaceView(Context context) {
		super(context);
		holder = getHolder();
		holder.addCallback(this);

		// Android 2.3.4 API Level 10 GINGERBREAD_MR1 Camera
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		camera = Camera.open();
		params = camera.getParameters();

		final String supportedIsoValues = params.get(ISO_VALUES);
		final int maxZoom = params.getMaxZoom();
		final int minZoom = 0;
		final int minExposure = params.getMinExposureCompensation();
		final int maxExposure = params.getMaxExposureCompensation();

		params.isZoomSupported();

		CameraSettings.initSetting(minZoom, maxZoom, minExposure, maxExposure,
				supportedIsoValues);

		handlerThread = new HandlerThread("AutoFocus");
		handlerThread.start();

		autoFocus = new CameraAutoFocus();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		try {

			final int w = DeviceMetrics.getWidth();
			final int h = DeviceMetrics.getHeight();

			params.setPreviewSize(w, h);
			camera.setParameters(params);
			camera.setPreviewDisplay(holder);

			camera.setPreviewCallback(new PreviewCallback() {
				public void onPreviewFrame(byte[] data, Camera camera) {
					frame = data;
				}
			});

		} catch (IOException e) {
			e.printStackTrace();
		}

		camera.startPreview();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		if (camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		startAutoFocus();
		return true;
	}

	public void startFrame() {

		Frames.addYUV(frame);
	}

	public void onSetting() {

		Log.i(MainActivity.TAG, "ZOOM " + CameraSettings.getInt(KEY_ZOOM));
		Log.i(MainActivity.TAG, "EXPO " + CameraSettings.getInt(KEY_EXPOSURE));
		Log.i(MainActivity.TAG, "ISO  " + CameraSettings.getString(KEY_ISO));

		params.setWhiteBalance(CameraSettings.getString(KEY_WHITE_BALANCE));
		params.setSceneMode(CameraSettings.getString(KEY_SCENE));
		params.setZoom(CameraSettings.getInt(KEY_ZOOM));
		params.setExposureCompensation(CameraSettings.getInt(KEY_EXPOSURE));
		params.set(KEY_ISO, CameraSettings.getString(KEY_ISO));
		try {
			camera.setParameters(params);

		} catch (Exception e) {

			Log.e(MainActivity.TAG,
					"setWhiteBalance "
							+ CameraSettings.getString(KEY_WHITE_BALANCE));
			Log.e(MainActivity.TAG,
					"setSceneMode " + CameraSettings.getString(KEY_SCENE));

			CameraSettings.set(KEY_WHITE_BALANCE, WHITE_BALANCE_AUTO);
			CameraSettings.set(KEY_SCENE, SCENE_MODE_AUTO);

			params.setWhiteBalance(CameraSettings.getString(KEY_WHITE_BALANCE));
			params.setSceneMode(CameraSettings.getString(KEY_SCENE));
			camera.setParameters(params);

		} finally {
			camera.setParameters(params);
		}

	}

	public void startAutoFocus() {
		new Handler(handlerThread.getLooper()).post(new Runnable() {

			@Override
			public void run() {
				camera.autoFocus(autoFocus);

			}
		});

	}

	private class CameraAutoFocus implements AutoFocusCallback {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {

			if (!success)
				startAutoFocus();

		}
	}

}
