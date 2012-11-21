package test.ImageProcessing;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;

import com.soma.chronos.frame.Frames;
import com.soma.chronos.util.DeviceMetrics;

public class NDKManager {

	private volatile static NDKManager instance;

	public static NDKManager getInstance() {
		if (instance == null) {
			synchronized (NDKManager.class) {
				if (instance == null) {
					instance = new NDKManager();
				}
			}
		}
		return instance;
	}

	private NDKManager() {
	}

	// Java Code -- Face --
	private FaceRectCollection collection;

	private ImageProcessing imageProcessing;
	private int index;
	private Bitmap bitmap = null;

	private int width;
	private int height;

	public void initFace() {
		this.width = DeviceMetrics.getWidth();
		this.height = DeviceMetrics.getHeight();
		collection = new FaceRectCollection();
		imageProcessing = new ImageProcessing();
		bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

		final int size = Frames.size();
		for (int i = 0; i < size; i++) {
			Frames.addSURF(new byte[width * height * 3]);

		}

		imageProcessing.InitFace();

	}

	public static void removeAll() {
		getInstance().dispose();
	}

	private void dispose() {

		if (bitmap != null)
			bitmap.recycle();
		bitmap = null;

		if (imageProcessing != null)
			imageProcessing.DisposeNative();
		imageProcessing = null;

		if (collection != null)
			collection.clear();
		collection = null;

	}

	public int getFaceRectSize() {
		return collection.getFaceRectSize();

	}

	public Rect getFaceRect(int index) {
		return collection.getFaceRect(index);
	}

	public Bitmap changeLastFrame(int[] frameData) {
		try {
			bitmap.setPixels(frameData, 0, width, 0, 0, width, height);
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public void setIndex(int frame) {
		index = frame;
	}

	public int[] getFrame(byte[] frameData) {
		return imageProcessing.GetLastFrame(width, height, frameData);
	}

	public Bitmap getLastFrame() {
		return SetBitmap();
	}

	public Bitmap faceDetect() {

		int[] faceData = imageProcessing.LastFrameFace(width, height,
				Frames.getRgb(index));

		if (faceData == null) {
			return null;
		} else {
			collection.setFaceRect(faceData);
			return SetBitmap();
		}
	}

	public Bitmap SetBitmap() {

		bitmap.setPixels(Frames.getRgb(index), 0/* offset */,
				width /* stride */, 0, 0, width, height);
		return bitmap;
	}

	public void customSet(int x, int y, int width, int height) {
		imageProcessing.SetCustomRect(x, y, width, height);
		collection.setFaceRect(x, y, width, height);
	}

	// Template Matching
	public void templateMatching(int frameNum, int faceNum) {
		// System.arraycopy(lastFrameOriginal, 0, lastFrame, 0,
		// lastFrameOriginal.length);
		imageProcessing.TemplateMatching(this.width, this.height,
				Frames.getRgb(index), Frames.getSurf(frameNum), frameNum,
				faceNum, 0, 0);
	}

	public Bitmap changeSURFFrame(int frameNum, int faceNum) {
		imageProcessing.SURFChange(width, height, Frames.getRgb(index),
				Frames.getSurf(frameNum), frameNum, faceNum);
		return SetBitmap();
	}

	public void findSurfFrame(int[] rgb) {
		imageProcessing.SURFframeFind(width, height, rgb);

	}

	public void getSurfFrame(int[] rgb, int frameNum) {
		imageProcessing.SURFgetFrame(width, height, rgb,
				Frames.getSurf(frameNum), frameNum);
	}

	private class FaceRectCollection {

		private List<Rect> rects;

		public FaceRectCollection() {
			rects = new ArrayList<Rect>();
		}

		public void setFaceRect(int[] faceData) {

			for (int i = 0; i < faceData.length; i += 4) {
				rects.add(new Rect(faceData[i], faceData[i + 1], faceData[i]
						+ faceData[i + 2], faceData[i + 1] + faceData[i + 3]));
			}
		}

		public void setFaceRect(int x, int y, int width, int height) {
			rects.add(new Rect(x, y, x + width, y + height));
		}

		public Rect getFaceRect(int index) {
			Rect rect = null;
			try {
				rect = rects.get(index);
			} catch (ArrayIndexOutOfBoundsException e) {
				e.getStackTrace();
			}
			return rect;
		}

		public int getFaceRectSize() {
			return rects.size();
		}

		public void clear() {
			rects.clear();
		}
	}

	public void selectSurfObject(int frame) {
		imageProcessing.SURFobjectSelect(frame);

	}

}
