package com.soma.chronos.processing;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import test.ImageProcessing.NDKManager;
import android.graphics.Bitmap;
import android.graphics.Rect;

import com.soma.chronos.frame.Frames;
import com.soma.chronos.processing.runnable.FindRunnable;
import com.soma.chronos.processing.runnable.GetRunnable;
import com.soma.chronos.processing.runnable.TemplateMatchingRunnable;

public class ProcessingManager {

	private NDKManager ndkManager;

	private int size;

	private FindRunnable[] findRunnables;
	private GetRunnable[] getRunnables;
	private TemplateMatchingRunnable[] templateMatchingRunnables;

	private ExecutorService executorService;

	public ProcessingManager() {
		this.size = Frames.size();

		ndkManager = NDKManager.getInstance();
		ndkManager.initFace();

		findRunnables = new FindRunnable[size];
		getRunnables = new GetRunnable[size];
		templateMatchingRunnables = new TemplateMatchingRunnable[size];

		for (int i = 0; i < size; i++) {
			findRunnables[i] = new FindRunnable(i);
			getRunnables[i] = new GetRunnable(i);
			templateMatchingRunnables[i] = new TemplateMatchingRunnable(i);
		}

	}

	public synchronized void startFindSurf() {
		executorService = Executors.newFixedThreadPool(size);
		for (int i = 0; i < size; i++) {
			executorService.execute(findRunnables[i]);
		}
		executorService.shutdown();

	}

	public synchronized void startGetSurf() {
		executorService = Executors.newFixedThreadPool(size);
		for (int i = 0; i < size; i++) {
			executorService.execute(getRunnables[i]);
		}
		executorService.shutdown();

	}

	public synchronized void startTemplateMatching() {
		executorService = Executors.newFixedThreadPool(size);
		for (int i = 0; i < size; i++) {
			templateMatchingRunnables[i].setFaceRect(getFaceRectSize());
			executorService.execute(templateMatchingRunnables[i]);
		}
		executorService.shutdown();

	}

	public boolean isTerminated() {
		return executorService.isTerminated();
	}

	// ImageProcessing
	public Bitmap changeLastFrame(int[] storeData) {
		return ndkManager.changeLastFrame(storeData);
	}

	public void setFrame(int frame) {
		ndkManager.selectSurfObject(frame);
		ndkManager.setIndex(frame);
	}

	public boolean faceDetect() {
		if (ndkManager.faceDetect() == null) {
			return false;
		}
		return true;
	}

	public Bitmap getBitmapChange(int frameNum, int faceNum) {
		return ndkManager.changeSURFFrame(frameNum, faceNum);
	}

	public Bitmap getChagedLastFrame() {
		return ndkManager.getLastFrame();
	}

	public Rect getFaceRect(int index) {
		return ndkManager.getFaceRect(index);
	}

	public int getFaceRectSize() {
		return ndkManager.getFaceRectSize();
	}

}
