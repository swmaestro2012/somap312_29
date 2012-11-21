package com.soma.chronos.processing.runnable;

import test.ImageProcessing.NDKManager;

public abstract class ProcessingRunnable implements Runnable {

	protected NDKManager ndkManager = NDKManager.getInstance();
	protected int id = -1;
	private int countFaceRect = -1;

	public ProcessingRunnable(int id) {
		this.id = id;

	}

	public void setFaceRect(int count) {
		this.countFaceRect = count;
	}

	protected int getCountFaceRect() {
		return countFaceRect;
	}

}
