package test.ImageProcessing;


public class ImageProcessing {

	static {
		System.loadLibrary("native_sample");
	}


	public native int Test(int m_value);

	public native void YUVtoRGB(int width, int height, byte[] storeHumanData,
			int[] m_Result);

	public native int GaussainModelHumanInsert(int width, int height,
			byte[] m_storeHumanData, int[] m_output);

	public native void Test2(int width, int height, byte[] m_storeHumanData,
			int[] m_output);

	public native void Test3(int width, int height, byte[] m_firstData,
			byte[] m_lastData, int[] m_output, int[] m_output2);

	public native int HOG(int width, int height, byte[] m_input, int[] m_output);

	// ///////////////////////////////////////////////////
	// Commend Image Processing
	// //////////////////////////////////////////////////

	public native void FindFeatures1(int width, int height, byte[] yuv,
			int[] rgba, int[] yuv1);

	public native void FindFeatures2(int width, int height, byte[] yuv,
			int[] rgba);

	public native void FindFeatures3(int width, int height, byte[] yuv,
			int[] rgba);

	// YUV to RGB
	public native void YUVtoRGB(int width, int height, byte[] m_yunv,
			int[] m_rgb, int[] r, int[] g, int[] b);

	// Fuzzy Contrast
	public native void FuzzyContrast(int width, int height, int[] img_R,
			int[] img_G, int[] img_B, int[] img_Test);

	public native void Histogram_of_Oriendted_gradient(int width, int height,
			int[] img_input, int[] img_output);

	public native void FuzzySkinColorConvert(int width, int height,
			int[] img_input, int[] img_output);

	// Internal ImageProcessing

	public native void RGBInsert(int width, int height, byte[] m_Test,
			byte[] R, byte[] G, byte[] B);

	public native int[] GetLastFrame(int width, int height, byte[] yuv);

	public native int[] LastFrameFace(int width, int height, int[] rgba);

	public native void InitFace();

	public native void DisposeNative();

	public native void CustomChange(int width, int height, byte[] before_yuv,
			int[] current_bgra, int rectX, int rectY, int rectWidth,
			int rectHeight);

	public native void SetCustomRect(int faceX, int faceY, int width, int height);

	public native void SURFobjectSelect(int lastFrameNum);

	public native void SURFframeFind(int width, int height, int[] beforeFrame);

	public native void SURFgetFrame(int width, int height, int[] beforeFrame,
			byte[] surf_beforeFrame, int frameNum);

	public native void SURFChange(int width, int height,
			int[] rgba_currentFrame, byte[] surf_beforeFrame, int frameNum,
			int faceNum);

	public native void SURFface(int width, int height, int[] rgba_currentFrame,
			byte[] yuv_beforeFrame, byte[] surf_beforeFrame, int frameNum,
			int faceNum);

	// Template Matching
	public native void TemplateMatching(int width, int height,
			int[] rgba_currentFrame, byte[] surf_beforeFrame, int frameNum,
			int faceNum, int rangeX, int rangeX1);

}
