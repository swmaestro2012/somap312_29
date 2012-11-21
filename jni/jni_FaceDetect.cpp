#include "FaceDetect.h"

extern "C" {
void setBrightnessValue(IplImage* source, IplImage* target) {
	__android_log_print(ANDROID_LOG_INFO, "JNI::setBrightnessValue", "Start");
	IplImage* source_dst = cvCreateImage(cvGetSize(source), 8, 3);
	IplImage* target_dst = cvCreateImage(cvGetSize(target), 8, 3);
	__android_log_print(ANDROID_LOG_INFO, "JNI::setBrightnessValue",
			"cvGetSize(source) : %d", cvGetSize(source));
	__android_log_print(ANDROID_LOG_INFO, "JNI::setBrightnessValue",
			"cvGetSize(target) : %d", cvGetSize(target));
	cvCvtColor(source, source_dst, CV_BGR2HSV);
	cvCvtColor(target, target_dst, CV_BGR2HSV);
	int length = source->roi->width * source->roi->height * 3;

	for (int i = 2; i < length; i += 3) {
		target_dst->imageData[i] = source_dst->imageData[i];
	}
	cvCvtColor(target_dst, target, CV_HSV2BGR);

	cvReleaseImage(&source_dst);
	cvReleaseImage(&target_dst);
}

void cvColorTransfer(unsigned char* _BGRMat, unsigned char* _targetBGRMat,
		int width, int height) {
	CvMat BGRMat = cvMat(height, width, CV_8UC3, _BGRMat);
	CvMat targetBGRMat = cvMat(height, width, CV_8UC3, _targetBGRMat);

	unsigned char* _labMat = new unsigned char[width * height * 3];
	CvMat labMat = cvMat(height, width, CV_8UC3, _labMat);

	unsigned char* _targetlabMat = new unsigned char[width * height * 3];
	CvMat targetlabMat = cvMat(height, width, CV_8UC3, _targetlabMat);

	cvCvtColor(&BGRMat, &labMat, CV_BGR2Lab);
	cvCvtColor(&targetBGRMat, &targetlabMat, CV_BGR2Lab);

	//타겟 lab , 소스 lab 각각의 평균 구하고 -> 분산=(data-평균)제곱의 평균 -> 표준편차 = sqrt(분산)
	int length = width * height * 3;
	int number = width * height;
	float average_l = 0, average_a = 0, average_b = 0;
	float target_average_l = 0, target_average_a = 0, target_average_b = 0;

	for (int i = 0; i < length; i += 3) {
		average_l += _labMat[i];
		average_a += _labMat[i + 1];
		average_b += _labMat[i + 2];

		target_average_l += _targetlabMat[i];
		target_average_a += _targetlabMat[i + 1];
		target_average_b += _targetlabMat[i + 2];
	}
	average_l /= number;
	average_a /= number;
	average_b /= number;

	target_average_l /= number;
	target_average_a /= number;
	target_average_b /= number;
	//분산
	float variance_l = 0, variance_a = 0, variance_b = 0;
	float target_variance_l = 0, target_variance_a = 0, target_variance_b = 0;
	for (int i = 0; i < length; i += 3) {
		variance_l += ((_labMat[i] - average_l) * (_labMat[i] - average_l)); //
		variance_a += ((_labMat[i + 1] - average_a)
				* (_labMat[i + 1] - average_a));
		variance_b += ((_labMat[i + 2] - average_b)
				* (_labMat[i + 2] - average_b));

		target_variance_l += ((_targetlabMat[i] - target_average_l)
				* (_targetlabMat[i] - target_average_l));
		target_variance_a += ((_targetlabMat[i + 1] - target_average_a)
				* (_targetlabMat[i + 1] - target_average_a));
		target_variance_b += ((_targetlabMat[i + 2] - target_average_b)
				* (_targetlabMat[i + 2] - target_average_b));
	}
	variance_l /= number;
	variance_a /= number;
	variance_b /= number;

	target_variance_l /= number;
	target_variance_a /= number;
	target_variance_b /= number;
	//표준편차
	variance_l = sqrt(variance_l);
	variance_a = sqrt(variance_a);
	variance_b = sqrt(variance_b);

	target_variance_l = sqrt(target_variance_l);
	target_variance_a = sqrt(target_variance_a);
	target_variance_b = sqrt(target_variance_b);
	//Processing
	float relative_l = target_variance_l / variance_l;
	float relative_a = target_variance_a / variance_a;
	float relative_b = target_variance_b / variance_b;
	for (int i = 0; i < length; i += 3) {
		_labMat[i] = (_labMat[i] - average_l) * relative_l + target_average_l;
		_labMat[i + 1] = (_labMat[i + 1] - average_a) * relative_a
				+ target_average_a;
		_labMat[i + 2] = (_labMat[i + 2] - average_b) * relative_b
				+ target_average_b;
	}

	//cvCvtColor(&targetlabMat, &targetBGRMat, CV_Lab2BGR);
	cvCvtColor(&labMat, &BGRMat, CV_Lab2BGR);

	free(_labMat);
	free(_targetlabMat);
}

double compareSURFDescriptors(const float* d1, const float* d2, double best,
		int length) {
	double total_cost = 0;
	assert(length % 4 == 0);
	for (int i = 0; i < length; i += 4) {
		double t0 = d1[i] - d2[i];
		double t1 = d1[i + 1] - d2[i + 1];
		double t2 = d1[i + 2] - d2[i + 2];
		double t3 = d1[i + 3] - d2[i + 3];
		total_cost += t0 * t0 + t1 * t1 + t2 * t2 + t3 * t3;
		if (total_cost > best)
			break;
	}
	return total_cost;
}

int naiveNearestNeighbor(const float* vec, int laplacian,
		const CvSeq* model_keypoints, const CvSeq* model_descriptors) {
	int length = (int) (model_descriptors->elem_size / sizeof(float));
	int i, neighbor = -1;
	double d, dist1 = 1e6, dist2 = 1e6;
	CvSeqReader reader, kreader;
	cvStartReadSeq(model_keypoints, &kreader, 0);
	cvStartReadSeq(model_descriptors, &reader, 0);

	for (i = 0; i < model_descriptors->total; i++) {
		const CvSURFPoint* kp = (const CvSURFPoint*) kreader.ptr;
		const float* mvec = (const float*) reader.ptr;
		CV_NEXT_SEQ_ELEM(kreader.seq->elem_size, kreader);
		CV_NEXT_SEQ_ELEM(reader.seq->elem_size, reader);
		//라플라시안이 다른 키포인트는 무시
		if (laplacian != kp->laplacian)
			continue;
		d = compareSURFDescriptors(vec, mvec, dist2, length);
		if (d < dist1) {
			dist2 = dist1;
			dist1 = d;
			neighbor = i;
		} else if (d < dist2)
			dist2 = d;
	}
	if (dist1 < 0.6 * dist2)
		return neighbor;
	return -1;
}
void findPairs(const CvSeq* objectKeypoints, const CvSeq* objectDescriptors,
		const CvSeq* imageKeypoints, const CvSeq* imageDescriptors,
		vector<int>& ptpairs) {
	int i;
	CvSeqReader reader, kreader;
	cvStartReadSeq(objectKeypoints, &kreader);
	cvStartReadSeq(objectDescriptors, &reader);
	ptpairs.clear();

	for (i = 0; i < objectDescriptors->total; i++) {
		const CvSURFPoint* kp = (const CvSURFPoint*) kreader.ptr;
		const float* descriptor = (const float*) reader.ptr;
		CV_NEXT_SEQ_ELEM(kreader.seq->elem_size, kreader);
		CV_NEXT_SEQ_ELEM(reader.seq->elem_size, reader);
		int nearest_neighbor = naiveNearestNeighbor(descriptor, kp->laplacian,
				imageKeypoints, imageDescriptors);
		if (nearest_neighbor >= 0) {
			ptpairs.push_back(i);
			ptpairs.push_back(nearest_neighbor);
		}
	}
}

void flannFindPairs(const CvSeq*, const CvSeq* objectDescriptors, const CvSeq*,
		const CvSeq* imageDescriptors, vector<int>& ptpairs) {
	int length = (int) (objectDescriptors->elem_size / sizeof(float));

	cv::Mat m_object(objectDescriptors->total, length, 5); //CV_32F = 5
	cv::Mat m_image(imageDescriptors->total, length, 5);

	// copy descriptors
	CvSeqReader obj_reader;
	float* obj_ptr = m_object.ptr<float>(0);
	cvStartReadSeq(objectDescriptors, &obj_reader);
	int oDtotal = objectDescriptors->total;
	for (int i = 0; i < oDtotal; i++) {
		const float* descriptor = (const float*) obj_reader.ptr;
		CV_NEXT_SEQ_ELEM(obj_reader.seq->elem_size, obj_reader);
		memcpy(obj_ptr, descriptor, length * sizeof(float));
		obj_ptr += length;
	}
	CvSeqReader img_reader;
	float* img_ptr = m_image.ptr<float>(0);
	cvStartReadSeq(imageDescriptors, &img_reader);
	int iDtotal = imageDescriptors->total;
	for (int i = 0; i < iDtotal; i++) {
		const float* descriptor = (const float*) img_reader.ptr;
		CV_NEXT_SEQ_ELEM(img_reader.seq->elem_size, img_reader);
		memcpy(img_ptr, descriptor, length * sizeof(float));
		img_ptr += length;
	}

	// find nearest neighbors using FLANN
	cv::Mat m_indices(objectDescriptors->total, 2, 4);	// CV_32S = 4
	cv::Mat m_dists(objectDescriptors->total, 2, 5);	// CV_32F = 5
	cv::flann::Index flann_index(m_image, cv::flann::KDTreeIndexParams(4)); // using 4 randomized kdtrees
	flann_index.knnSearch(m_object, m_indices, m_dists, 2,
			cv::flann::SearchParams(64)); // maximum number of leafs checked

	int* indices_ptr = m_indices.ptr<int>(0);
	float* dists_ptr = m_dists.ptr<float>(0);
	int row = m_indices.rows << 1;
	int ptpairs_i = 0;
	for (int i = 0; i < row; i += 2) {
		if (dists_ptr[i] < 0.6 * dists_ptr[i + 1]) {
			ptpairs.push_back(i >> 1);
			ptpairs.push_back(indices_ptr[i]);
		}
	}
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFface.flannFindPairs",
			"ptpairs size : %d\n", ptpairs.size() / 2);
}
/* a rough implementation for object location */

int locatePlanarObject(const CvSeq* objectKeypoints,
		const CvSeq* objectDescriptors, const CvSeq* imageKeypoints,
		const CvSeq* imageDescriptors,
		/*const CvPoint src_corners[4], CvPoint dst_corners[4],*/CvMat& _h) {
	__android_log_print(ANDROID_LOG_INFO, "JNI::locatePlanarObject", "Start");
	vector<int> ptpairs;
	vector<CvPoint2D32f> pt1, pt2;
	CvMat _pt1, _pt2;
	int i, n;

	flannFindPairs(objectKeypoints, objectDescriptors, imageKeypoints,
			imageDescriptors, ptpairs);
	__android_log_print(ANDROID_LOG_INFO, "JNI::locatePlanarObject",
			"flannFindPairs");
	//findPairs( objectKeypoints, objectDescriptors, imageKeypoints, imageDescriptors, ptpairs );

	n = ptpairs.size() >> 1;
	if (n < 4)
		return 0;

	pt1.resize(n);
	pt2.resize(n);
	int in_i = 0;

	for (i = 0; i < n; i++) {
		in_i = i << 1;
		pt1[i] =
				((CvSURFPoint*) cvGetSeqElem(objectKeypoints, ptpairs[in_i]))->pt;
		pt2[i] =
				((CvSURFPoint*) cvGetSeqElem(imageKeypoints, ptpairs[in_i + 1]))->pt;
	}

	_pt1 = cvMat(1, n, CV_32FC2, &pt1[0]);
	_pt2 = cvMat(1, n, CV_32FC2, &pt2[0]);
	if (!cvFindHomography(&_pt1, &_pt2, &_h, /*CV_RANSAC*/8, 5))
		return 0;
	__android_log_print(ANDROID_LOG_INFO, "JNI::locatePlanarObject",
			"cvFindHomography");
	/*for( i = 0; i < 4; i++ )
	 {
	 double x = src_corners[i].x, y = src_corners[i].y;
	 double Z = 1./(h[6]*x + h[7]*y + h[8]);
	 double X = (h[0]*x + h[1]*y + h[2])*Z;
	 double Y = (h[3]*x + h[4]*y + h[5])*Z;
	 dst_corners[i] = cvPoint(cvRound(X), cvRound(Y));
	 }*/

	return 1;
}
void TemplateMatching_init() {
	__android_log_print(ANDROID_LOG_INFO, "JNI::TemplateMatching_init", "init");
	for (int i = 10; i != 0; i--)
		faceMatchings.push_back(new FaceMatchingDatas());
}
JNIEXPORT void JNICALL Java_test_ImageProcessing_ImageProcessing_TemplateMatching
(JNIEnv* env, jobject thiz, jint width, jint height, jintArray rgba_currentFrame, jbyteArray surf_beforeFrame, jint frameNum, jint faceNum, jint rangeX, jint rangeX1)
{
	//원본데이터가 Target, SURF데이터가 source
	__android_log_print(ANDROID_LOG_INFO, "JNI::TemplateMatching", "%d - Start", frameNum);
	jbyte* jsurf_beforeFrame = env->GetByteArrayElements(surf_beforeFrame, 0);
	jint* jbgra_current = env->GetIntArrayElements(rgba_currentFrame, 0);
	Mat mbgra_current(height, width, CV_8UC4, (unsigned char *)jbgra_current);//BGRA
	Mat mbgraSurf_before(height, width, CV_8UC3, (unsigned char *)jsurf_beforeFrame);//BGRA

	//double tt = (double)cvGetTickCount();

	double min, max;
	CvPoint left_top;
	IplImage surf_img = mbgraSurf_before;
	IplImage object = mbgra_current;
	IplImage* object3 = cvCreateImage(cvGetSize(&object), 8, 3);//원본 데이터를 3채널로 변경
	cvCvtColor( &object, object3, CV_BGRA2BGR );//3채널 원본 데이터를 ROI

	int faceX = lastFrameFaceDataRect[0]->faceDatas[faceNum]->x;
	int faceY = lastFrameFaceDataRect[0]->faceDatas[faceNum]->y;
	int faceW = lastFrameFaceDataRect[0]->faceDatas[faceNum]->width;
	int faceH = lastFrameFaceDataRect[0]->faceDatas[faceNum]->height;
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFChange", " x : %d ,  y : %d , width : %d , height : %d", faceX, faceY, faceW, faceH);

	int rangeX2, rangeW, rangeY, rangeH;
	if((rangeX2 = faceX - 100) < 0)
	{
		rangeX2 = 0;
	}
	if((rangeW = faceW + 200) + rangeX2 > width)
	{
		rangeW = width - rangeX2 - 1;
	}
	if((rangeY = faceY - 100) < 0)
	{
		rangeY = 0;
	}
	if((rangeH = faceH + 200) + rangeY > height)
	{
		rangeH = height - rangeY - 1;
	}

	cvSetImageROI(&surf_img, cvRect(rangeX2, rangeY, rangeW, rangeH));
	__android_log_print(ANDROID_LOG_INFO, "JNI::TemplateMatching_init", "cvSetImageROI rangeX : %d , rangeY : %d , rangeW : %d , rangeH : %d", rangeX2, rangeY, rangeW, rangeH);
	cvSetImageROI( object3, cvRect( faceX,
					faceY,
					faceW,
					faceH));
	IplImage* obj_src = cvCreateImage(cvSize( surf_img.roi->width - object3->roi->width+1, surf_img.roi->height - object3->roi->height+1 ), IPL_DEPTH_32F, 1 );
	cvMatchTemplate(&surf_img, object3, obj_src , /*CV_TM_CCOEFF_NORMED*/5);
	cvMinMaxLoc(obj_src, &min, &max, NULL, &left_top);
	__android_log_print(ANDROID_LOG_INFO, "JNI::TemplateMatching_init", "%d - x : %d , y : %d", frameNum, faceX, faceY);
	__android_log_print(ANDROID_LOG_INFO, "JNI::TemplateMatching_init", "%d - left_top.x : %d , left_top.y : %d", frameNum, left_top.x, left_top.y);
	cvResetImageROI(&surf_img);

	left_top.x += rangeX2;
	left_top.y += rangeY;

	cvSetImageROI(&surf_img, cvRect(left_top.x, left_top.y, faceW, faceH));
	//Color Transfer
	//double tt1 = (double)cvGetTickCount();
	//setBrightnessValue(object3, &surf_img);
	cvColorTransfer((unsigned char*)surf_img.imageData, (unsigned char*)object3->imageData, lastFrameFaceDataRect[0]->faceDatas[faceNum]->width, lastFrameFaceDataRect[0]->faceDatas[faceNum]->height);
	//tt1 = (double)cvGetTickCount() - tt1;
	//__android_log_print(ANDROID_LOG_INFO, "JNI::SURFChange", "cvColorTransfer time = %gms\n", tt1/(cvGetTickFrequency()*1000.));

	cvResetImageROI(&surf_img);
	cvResetImageROI(object3);
	faceMatchings[frameNum]->faces.push_back(left_top);
	//tt = (double)cvGetTickCount() - tt;
	//__android_log_print(ANDROID_LOG_INFO, "JNI::SURFChange", "Extraction time = %gms\n", tt/(cvGetTickFrequency()*1000.));

	cvReleaseImage(&object3);
	cvReleaseImage(&obj_src);
	env->ReleaseIntArrayElements(rgba_currentFrame, jbgra_current, 0);
	env->ReleaseByteArrayElements(surf_beforeFrame, jsurf_beforeFrame, 0);
}

JNIEXPORT void JNICALL Java_test_ImageProcessing_ImageProcessing_SURFChange
(JNIEnv* env, jobject thiz, jint width, jint height, jintArray rgba_currentFrame, jbyteArray surf_beforeFrame, jint frameNum, jint faceNum)
{
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFChange", "Start");
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFChange", "frameNum : %d , faceNum : %d", frameNum, faceNum);

	jbyte* jsurf_beforeFrame = env->GetByteArrayElements(surf_beforeFrame, 0);
	jint* jbgra_current = env->GetIntArrayElements(rgba_currentFrame, 0);
	Mat mbgra_current(height, width, CV_8UC4, (unsigned char *)jbgra_current);//BGRA
	Mat mbgraSurf_before(height, width, CV_8UC3, (unsigned char *)jsurf_beforeFrame);//BGRA

	int y = lastFrameFaceDataRect[0]->faceDatas[faceNum]->y;
	int x = lastFrameFaceDataRect[0]->faceDatas[faceNum]->x;
	int faceWidthLength = lastFrameFaceDataRect[0]->faceDatas[faceNum]->width + x;
	int widthStep4 = width << 2;
	int widthStep3 = width * 3;
	int length = (lastFrameFaceDataRect[0]->faceDatas[faceNum]->height + y);

	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFChange", " x : %d ,  y : %d , width : %d , height : %d", x, y, lastFrameFaceDataRect[0]->faceDatas[faceNum]->width, lastFrameFaceDataRect[0]->faceDatas[faceNum]->height);
	//템플릿 매칭
	CvPoint left_top = faceMatchings[frameNum]->faces[faceNum];
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFChange", "left_top->x : %d , left_top->y : %d", left_top.x, left_top.y);

	//대입
	int faceX;
	int faceY = left_top.y;
	for(int i = y; i <length; i++)
	{
		faceX = left_top.x;
		for(int j = x; j < faceWidthLength; j++)
		{
			//mbgra_current.data[i*widthStep4 + j*4]	  = mbgraSurf_before.data[i*widthStep3 + j*3];
			//mbgra_current.data[i*widthStep4 + j*4+1] = mbgraSurf_before.data[i*widthStep3 + j*3+1];
			//mbgra_current.data[i*widthStep4 + j*4+2] = mbgraSurf_before.data[i*widthStep3 + j*3+2];
			memcpy(mbgra_current.data + i*widthStep4 + (j<<2), mbgraSurf_before.data + faceY*widthStep3 + faceX*3, sizeof(char) * 3);
			faceX++;
		}
		faceY++;
	}

	env->ReleaseIntArrayElements(rgba_currentFrame, jbgra_current, 0);
	env->ReleaseByteArrayElements(surf_beforeFrame, jsurf_beforeFrame, 0);
}

JNIEXPORT void JNICALL Java_test_ImageProcessing_ImageProcessing_SURFobjectSelect
(JNIEnv* env, jobject thiz, jint lastFrameNum)
{
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFobjectSelect", "Start");

	objectKeypoints = imageKeypointsDatas[lastFrameNum];
	objectDescriptors = imageDescriptorsDatas[lastFrameNum];
}

JNIEXPORT void JNICALL Java_test_ImageProcessing_ImageProcessing_SURFframeFind
(JNIEnv* env, jobject thiz, jint width, jint height, jintArray beforeFrame)
{
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFframeFind", "Start");
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFframeFind", "width : %d , height : %d", width, height);
	//double tt = (double)cvGetTickCount();
	jint* jbgra_before = env->GetIntArrayElements(beforeFrame, 0);
	//if(jintArr == NULL)
	//{
	//jintArray jintArr1 = (jintArray)env->NewIntArray(width*height);
	//__android_log_print(ANDROID_LOG_INFO, "JNI::SURFframeFind", "env->NewIntArray");
	//}
	//jint* jbgra_before = env->GetIntArrayElements(jintArr1, 0);
	//Mat myuv_before(height + height/2, width, CV_8UC1, (unsigned char *)jyuv_before);//data
	Mat mbgra_before(height, width, CV_8UC4, (unsigned char *)jbgra_before);//BGRA
	//cvtColor(myuv_before, mbgra_before, CV_YUV420sp2BGR, 4);

	IplImage before_img = mbgra_before;

	IplImage* before_gray = cvCreateImage(cvGetSize(&before_img), 8, 1);
	cvCvtColor( &before_img, before_gray, CV_BGRA2GRAY );

	if(params.hessianThreshold != 500)
	{
		params = cvSURFParams(500, 1);
	}
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFframeFind", "cvSURFParams(500, 1);");

	CvSeq* imageKeypoints, *imageDescriptors;
	cvSetImageROI(before_gray, cvRect(0, 0, SURFrange, height));
	cvExtractSURF( before_gray, 0, &imageKeypoints, &imageDescriptors, storage, params );
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFframeFind", "Image Descriptors: %d\n", imageDescriptors->total);
	cvResetImageROI( before_gray );

	imageKeypointsDatas.push_back(imageKeypoints);
	imageDescriptorsDatas.push_back(imageDescriptors);

	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFframeFind", "imageKeypoints: %d\n", imageKeypoints);
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFframeFind", "imageDescriptors: %d\n", imageDescriptors);

	cvReleaseImage(&before_gray);
	env->ReleaseIntArrayElements(beforeFrame, jbgra_before, 0);
}
JNIEXPORT void JNICALL Java_test_ImageProcessing_ImageProcessing_SURFgetFrame
(JNIEnv* env, jobject thiz, jint width, jint height, jintArray beforeFrame, jbyteArray surf_beforeFrame, jint frameNum )
{
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFgetFrame", "%d - Start", frameNum);
	//double tt = (double)cvGetTickCount();
	jbyte* jsurf_beforeFrame = env->GetByteArrayElements(surf_beforeFrame, 0);
	//jint* jyuv_before  = env->GetByteArrayElements(yuv_beforeFrame, 0);
	Mat mbgrSurf_before(height, width, CV_8UC3, (unsigned char *)jsurf_beforeFrame);//BGRA
	//Mat myuv_before(height + height/2, width, CV_8UC1, (unsigned char *)jyuv_before);//data
	//if(jintArr == NULL)
	//{
	//jintArray jintArr1 = (jintArray)env->NewIntArray(width*height);
	//__android_log_print(ANDROID_LOG_INFO, "JNI::SURFgetFrame", "env->NewIntArray");
	//}
	jint* jbgra_before = env->GetIntArrayElements(beforeFrame, 0);
	Mat mbgra_before(height, width, CV_8UC4, (unsigned char *)jbgra_before);//BGRA
	//cvtColor(myuv_before, mbgra_before, CV_YUV420sp2BGR, 4);

	IplImage before_img = mbgra_before;
	IplImage surf_img3 = mbgrSurf_before;

	double h[9];
	CvMat _h;
	_h = cvMat(3, 3, /*CV_64F*/6, h);

	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFgetFrame", "%d - before locatePlanarObject", frameNum);
	if( locatePlanarObject( objectKeypoints, objectDescriptors, imageKeypointsDatas[frameNum], imageDescriptorsDatas[frameNum], _h ))
	{
		__android_log_print(ANDROID_LOG_INFO, "JNI::SURFgetFrame", "%d - locatePlanarObject OK", frameNum);
	}
	IplImage* dst1 = cvCreateImage(cvGetSize(&before_img), 8, 3);
	cvCvtColor( &before_img, dst1, CV_BGRA2BGR );
	CvMat* warp_mat = cvCreateMat(3,3,/*CV_64F*/6);
	cvInvert(&_h, warp_mat, CV_SVD);		//이미지 복원을 위한 역행렬 구하기
	cvWarpPerspective(dst1, &surf_img3, warp_mat);//1
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFgetFrame", "%d - cvWarpPerspective", frameNum);

	//tt = (double)cvGetTickCount() - tt;
	//__android_log_print(ANDROID_LOG_INFO, "JNI::SURFgetFrame", "%d - Extraction time = %gms\n", frameNum, tt/(cvGetTickFrequency()*1000.));

	cvReleaseImage(&dst1);
	env->ReleaseIntArrayElements(beforeFrame, jbgra_before, 0);
	env->ReleaseByteArrayElements(surf_beforeFrame, jsurf_beforeFrame, 0);
	__android_log_print(ANDROID_LOG_INFO, "JNI::SURFgetFrame", "End");
}

void faceInpaint(int width, Mat& mbgra_current, int faceX, int faceY,
		int faceWidth, int faceHeight) {
	__android_log_print(ANDROID_LOG_INFO, "JNI::faceInpaint", "Start");
	int maskRange = 2;
	int maskX = faceX - maskRange;
	int maskY = faceY - maskRange;
	int maskX1 = faceX + faceWidth;
	int maskY1 = faceY + faceHeight;
	int maskLine = maskRange * 2;
	int widthStep3 = width * 3;
	int widthStep4 = width * 4;

	Mat mbgr_current;
	cvtColor(mbgra_current, mbgr_current, CV_BGRA2BGR, 3);
	IplImage img_src = mbgr_current;

	IplImage* img_mask = cvCreateImage(cvGetSize(&img_src), IPL_DEPTH_8U, 1);
	cvRectangle(img_mask, cvPoint(maskX, maskY), cvPoint(maskX1, maskY1),
			cvScalarAll(255), maskLine);

	__android_log_print(ANDROID_LOG_INFO, "JNI::faceInpaint", "Face inpaint");
	cvInpaint(&img_src, img_mask, &img_src, 2, CV_INPAINT_TELEA);//CV_INPAINT_TELEA , CV_INPAINT_NS

	__android_log_print(ANDROID_LOG_INFO, "JNI::faceInpaint",
			"mbgra_current  row : %d , col : %d", mbgra_current.rows,
			mbgra_current.cols);
	__android_log_print(ANDROID_LOG_INFO, "JNI::faceInpaint",
			" mbgr_current  row : %d , col : %d", mbgr_current.rows,
			mbgr_current.cols);
	for (int j = maskY; j < maskY1 + maskLine; j++) {
		for (int i = maskX; i < maskX1 + maskLine; i++) {
			memcpy(mbgra_current.data + j * widthStep4 + i * 4,
					mbgr_current.data + j * widthStep3 + i * 3,
					sizeof(char) * 3);
			/*mbgra_current.data[j*widthStep4 + i*4]		= mbgr_current.data[j*widthStep3 + i*3];
			 mbgra_current.data[j*widthStep4 + i*4 +1]	= mbgr_current.data[j*widthStep3 + i*3 +1];
			 mbgra_current.data[j*widthStep4 + i*4 +2]	= mbgr_current.data[j*widthStep3 + i*3 +2];*/
		}
	}
	cvReleaseImage(&img_mask);
}

int FaceFilter(IplImage* DestFrame, CvRect* r) {
	int i, j;
	unsigned char R, G, B;
	unsigned char Cr, Cb, Y;
	int channel = 4;
	int count = 0;
	float rate = r->height * r->width;
	__android_log_print(ANDROID_LOG_INFO, "JNI::FaceFilter",
			"x : %d , y : %d , width : %d , height : %d", r->x, r->y, r->width,
			r->height);
	for (i = r->y; i < r->y + r->height; i++) {
		for (j = r->x; j < r->x + r->width; j++) {
			B = (unsigned char) DestFrame->imageData[DestFrame->widthStep * i
					+ j * channel];
			G = (unsigned char) DestFrame->imageData[DestFrame->widthStep * i
					+ j * channel + 1];
			R = (unsigned char) DestFrame->imageData[DestFrame->widthStep * i
					+ j * channel + 2];

			Y = (unsigned char) (0.2999 * R + 0.587 * G + 0.114 * B);
			Cr = (unsigned char) ((0.5 * R - 0.4187 * G - 0.0813 * B + 128.0));
			Cb = (unsigned char) ((-0.1687 * R - 0.3313 * G + 0.5 * B + 128.0));

			if ((Cb >= 77) && (Cb <= 127) && (Cr >= 133) && (Cr <= 173)) {
				count++;
			} else {
				//DestFrame->imageData[DestFrame->widthStep*i+j*channel]=0;
				//DestFrame->imageData[DestFrame->widthStep*i+j*channel+1]=0;
				//DestFrame->imageData[DestFrame->widthStep*i+j*channel+2]=0;
			}
		}
	}
	rate = count / rate * 100;
	__android_log_print(ANDROID_LOG_INFO, "JNI::FaceFilter",
			"count : %d , rate : %f", count, rate);
	if (rate > 40) {
		return 1;
	} else {
		return 0;
	}
}

void CvRectEdit(CvRect* r, int rateNum) {
	r->x *= rateNum;
	r->y *= rateNum;
	r->width *= rateNum;
	r->height *= rateNum;
}

void SaveSubFaces(IplImage* img_base, CvSeq* faces, int i, int width,
		int height) {
	//IplImage* img_clone = cvCloneImage(img_base);
	__android_log_print(ANDROID_LOG_INFO, "JNI::SaveSubFaces", "Start");
	CvRect* r = (CvRect*) cvGetSeqElem(faces, i);

	CvRectEdit(r, rateNum);
	//Filter
	if (FaceFilter(img_base, r) == 0) {
		return;
	}
	__android_log_print(ANDROID_LOG_INFO, "JNI::SaveSubFaces",
			"after FaceFilter");
	if ((r->x -= r->width / 2) < 0) {
		r->width *= 2;
		r->width += r->x;
		r->x = 0;
	} else {
		r->width *= 2;
		if (r->x + r->width > width) {
			r->width = width - r->x;
		}
	}
	if ((r->y -= r->height / 2) < 0) {
		r->height *= 2;
		r->height += r->y;
		r->y = 0;
	} else {
		r->height *= 2;
		if (r->y + r->height > height) {
			r->height = height - r->y;
		}
	}

	__android_log_print(ANDROID_LOG_INFO, "JNI::SaveSubFaces",
			"(before sort)%d. x : %d,  y : %d,  width : %d,  height : %d", i,
			r->x, r->y, r->width, r->height);
	//CvMat* subMat = cvCreateMat(r->height, r->width, CV_8UC4);
	//CvRect subRect = cvRect(r->x, r->y, r->width, r->height);
	//subMat = cvGetSubRect(img_base, subMat, subRect);

	//cvRectangle(img_base, cvPoint(r->x, (r->y)), cvPoint(r->x + r->width, r->y + r->height), Scalar(0, 255, 0, 255), 5);

	//faceSubData->faceDatas.push_back(r);
	if (faceSortStack == NULL) {
		faceSortStack = new FaceSortStack();
		__android_log_print(ANDROID_LOG_INFO, "JNI::SaveSubFaces",
				"new FaceSortStack();");
	}
	faceSortStack->push(r);

	//__android_log_print(ANDROID_LOG_INFO, "JNI_Face", "faceDataRect.size() : %d", faceDataRect.size());
	//__android_log_print(ANDROID_LOG_INFO, "JNI_Face", "x : %d,  y : %d,  width : %d,  height : %d", faceData->FaceRect->x, faceData->FaceRect->y, faceData->FaceRect->width, faceData->FaceRect->height);
}

int FindFaceDetectHaar(IplImage* img_src, IplImage* img_grey, int flag,
		int width, int height) {
	__android_log_print(ANDROID_LOG_INFO, "JNI::FindFaceDetectHaar", "Start");
	if (!img_src) {
		//printf("no file");
		__android_log_print(ANDROID_LOG_INFO, "JNI::FindFaceDetectHaar",
				"Image Load Fail");
	}
	//__android_log_print(ANDROID_LOG_INFO, "JNI_Face", "Image Load OK");

	if (cascade == NULL) {
		cascade = (CvHaarClassifierCascade*) cvLoad(
				"/sdcard/haarcascade_frontalface_alt2.xml", 0, 0, 0);
		if (!cascade) {
			__android_log_print(ANDROID_LOG_INFO, "JNI::FindFaceDetectHaar",
					"cascade Load Fail");
		}
		__android_log_print(ANDROID_LOG_INFO, "JNI::FindFaceDetectHaar",
				"cascade OK");
	}
	if (storage == NULL) {
		storage = cvCreateMemStorage(0);
		__android_log_print(ANDROID_LOG_INFO, "JNI::FindFaceDetectHaar",
				"storage Load OK");
	}

	__android_log_print(ANDROID_LOG_INFO, "JNI::FindFaceDetectHaar",
			"cvHaarDetectObjects Start");
	faces = cvHaarDetectObjects(img_grey, cascade, storage, 1.1, 2, 0,
			cvSize(80, 80));
	__android_log_print(ANDROID_LOG_INFO, "JNI::FindFaceDetectHaar",
			"cvHaarDetectObjects OK - Faces : %d", faces->total);

	if (faces->total == 0) {
		__android_log_print(ANDROID_LOG_INFO, "JNI::FindFaceDetectHaar",
				"faces->total == 0");
		return 0;
	}

	faceSubData = new FacesSubData();
	for (int i = 0; i < faces->total; i++) {
		//if(필더링) 필요
		SaveSubFaces(img_src, faces, i, width, height);		//현재 프레임에서 얼굴 저장
	}

	//copy
	//__android_log_print(ANDROID_LOG_INFO, "JNI_copy", "faceSortStack->faceDatas.size : %d,  faceSortStack->faceDatas[0]->x : %d", faceSortStack->faceDatas.size(), faceSortStack->faceDatas[0]->x);
	faceSubData->faceDatas.resize(faceSortStack->faceDatas.size());
	copy(faceSortStack->faceDatas.begin(), faceSortStack->faceDatas.end(),
			faceSubData->faceDatas.begin());
	//faceSortStack->faceDatas.clear();
	//__android_log_print(ANDROID_LOG_INFO, "JNI_copy", "faceSortStack->faceDatas.size : %d", faceSortStack->faceDatas.size());
	//__android_log_print(ANDROID_LOG_INFO, "JNI_copy", "faceSubData->faceDatas.size : %d,  faceSubData->faceDatas[0]->x : %d", faceSubData->faceDatas.size(), faceSubData->faceDatas[0]->x);
	//

	//sort
	//__android_log_print(ANDROID_LOG_INFO, "JNI_Face", "(after sort)0. x : %d,  y : %d,  width : %d,  height : %d",  faceSubData->faceDatas[0]->x,  faceSubData->faceDatas[0]->y,  faceSubData->faceDatas[0]->width,  faceSubData->faceDatas[0]->height);
	//__android_log_print(ANDROID_LOG_INFO, "JNI_Face", "(after sort)1. x : %d,  y : %d,  width : %d,  height : %d",  faceSubData->faceDatas[1]->x,  faceSubData->faceDatas[1]->y,  faceSubData->faceDatas[1]->width,  faceSubData->faceDatas[1]->height);

	__android_log_print(ANDROID_LOG_INFO, "JNI::FindFaceDetectHaar",
			"faceSubData->faceDatas.size() : %d",
			faceSubData->faceDatas.size());
	//데이터 저장 프레임단위 얼굴들 저장
	if (flag == 1) {
		if (lastFrameFaceDataRect.empty()) {
			lastFrameFaceDataRect.push_back(faceSubData);
		} else {
			for (int i = 0; i < faceSubData->faceDatas.size(); i++) {
				lastFrameFaceDataRect[0]->faceDatas.push_back(
						faceSubData->faceDatas[i]);
			}
		}

		__android_log_print(ANDROID_LOG_INFO, "JNI::FindFaceDetectHaar",
				"lastFrameFaceDataRect : %d", lastFrameFaceDataRect.size());
	}

	return 1;
}

int FindYCrCbSkinColorFilter(IplImage* DestFrame) {
	int i, j;
	unsigned char R, G, B;
	unsigned char Cr, Cb, Y;
	int channel = 4;
	int count = 0;
	float rate = DestFrame->roi->height * DestFrame->roi->width;
	__android_log_print(ANDROID_LOG_INFO, "FrameJNI",
			"width : %d , height : %d", DestFrame->roi->width,
			DestFrame->roi->height);
	for (i = DestFrame->roi->yOffset; i < DestFrame->roi->height; i++) {
		for (j = DestFrame->roi->xOffset; j < DestFrame->roi->width; j++) {
			B = (unsigned char) DestFrame->imageData[DestFrame->widthStep * i
					+ j * channel];
			G = (unsigned char) DestFrame->imageData[DestFrame->widthStep * i
					+ j * channel + 1];
			R = (unsigned char) DestFrame->imageData[DestFrame->widthStep * i
					+ j * channel + 2];

			Y = (unsigned char) (0.2999 * R + 0.587 * G + 0.114 * B);
			Cr = (unsigned char) ((0.5 * R - 0.4187 * G - 0.0813 * B + 128.0));
			Cb = (unsigned char) ((-0.1687 * R - 0.3313 * G + 0.5 * B + 128.0));

			if ((Cb >= 77) && (Cb <= 127) && (Cr >= 133) && (Cr <= 173)) {
				count++;
			}
		}
	}
	rate = count / rate * 100;
	__android_log_print(ANDROID_LOG_INFO, "FrameJNI", "count : %d , rate : %f",
			count, rate);
	if (rate > 60) {
		return 1;
	} else {
		return 0;
	}
}

JNIEXPORT void JNICALL Java_test_ImageProcessing_ImageProcessing_InitFace
(JNIEnv* env, jobject thiz)
{
	__android_log_print(ANDROID_LOG_INFO, "JNI_Face", "Init_face");

	if(faceSortStack == NULL)
	{
		faceSortStack = new FaceSortStack();
	}

	if(cascade == NULL) {		//"/sdcard/haarcascade_frontalface_alt2.xml"
		cascade = (CvHaarClassifierCascade*) cvLoad("/sdcard/haarcascade_frontalface_alt2.xml", 0, 0, 0);
		if(!cascade) {
			//printf("no file");
			__android_log_print(ANDROID_LOG_INFO, "JNI_Face", "cascade Load Fail");
		}
		else {
			__android_log_print(ANDROID_LOG_INFO, "JNI_Face", "cascade OK");
		}
	}
	if(storage == NULL) {
		storage = cvCreateMemStorage(0);
		__android_log_print(ANDROID_LOG_INFO, "JNI_Face", "storage Load OK");
	}

	params = cvSURFParams(500, 1);

	TemplateMatching_init();
}

JNIEXPORT void JNICALL Java_test_ImageProcessing_ImageProcessing_DisposeNative
(JNIEnv* env, jobject thiz)
{
	dispose(env);
}
void dispose(JNIEnv* env) {
	delete faceSortStack;
	faceSortStack = NULL;

	int i = lastFrameFaceDataRect.size();
	while (i != 0) {
		i--;
		FacesSubData* temp = lastFrameFaceDataRect[i];
		delete temp;
	}
	lastFrameFaceDataRect.clear();

	i = faceMatchings.size();
	while (i != 0) {
		i--;
		FaceMatchingDatas* temp = faceMatchings[i];
		delete temp;
	}
	faceMatchings.clear();

	__android_log_print(ANDROID_LOG_INFO, "JNI::dispose",
			"imageKeypointsDatas");
	__android_log_print(ANDROID_LOG_INFO, "JNI::dispose",
			"imageKeypointsDatas : %d", imageKeypointsDatas[0]);
	__android_log_print(ANDROID_LOG_INFO, "JNI::dispose",
			"imageKeypointsDatas : %d", imageKeypointsDatas[1]);
	i = imageKeypointsDatas.size();
	while (i != 0) {
		i--;
		CvSeq* temp = imageKeypointsDatas[i];
		cvClearSeq(temp);
		imageKeypointsDatas[i] = NULL;
	}
	imageKeypointsDatas.clear();
	__android_log_print(ANDROID_LOG_INFO, "JNI::dispose",
			"imageKeypointsDatas : %d", imageKeypointsDatas[0]);
	__android_log_print(ANDROID_LOG_INFO, "JNI::dispose",
			"imageKeypointsDatas : %d", imageKeypointsDatas[1]);

	__android_log_print(ANDROID_LOG_INFO, "JNI::dispose",
			"imageDescriptorsDatas");
	__android_log_print(ANDROID_LOG_INFO, "JNI::dispose",
			"imageDescriptorsDatas : %d", imageDescriptorsDatas[0]);
	__android_log_print(ANDROID_LOG_INFO, "JNI::dispose",
			"imageDescriptorsDatas : %d", imageDescriptorsDatas[1]);
	i = imageDescriptorsDatas.size();
	while (i != 0) {
		i--;
		CvSeq* temp = imageDescriptorsDatas[i];
		cvClearSeq(temp);
		imageDescriptorsDatas[i] = NULL;
	}
	imageDescriptorsDatas.clear();
	__android_log_print(ANDROID_LOG_INFO, "JNI::dispose",
			"imageDescriptorsDatas : %d", imageDescriptorsDatas[0]);
	__android_log_print(ANDROID_LOG_INFO, "JNI::dispose",
			"imageDescriptorsDatas : %d", imageDescriptorsDatas[1]);

	faces = NULL;
	//cvClearSeq(faces);

	//__android_log_print(ANDROID_LOG_INFO, "JNI::dispose", "imageKeypointsDatas : %d , %d", imageKeypointsDatas[0], imageKeypointsDatas[0]->total);
	//__android_log_print(ANDROID_LOG_INFO, "JNI::dispose", "imageKeypointsDatas : %d , %d", imageKeypointsDatas[1] , imageKeypointsDatas[1]->total);
	cvReleaseHaarClassifierCascade(&cascade);
	cvClearMemStorage(storage);
	cvReleaseMemStorage(&storage);

	//__android_log_print(ANDROID_LOG_INFO, "JNI::dispose", "imageKeypointsDatas : %d , %d", imageKeypointsDatas[0], imageKeypointsDatas[0]->total);
	//__android_log_print(ANDROID_LOG_INFO, "JNI::dispose", "imageKeypointsDatas : %d , %d", imageKeypointsDatas[1] , imageKeypointsDatas[1]->total);

	//__android_log_print(ANDROID_LOG_INFO, "JNI::dispose", "faces : %d", faces->total);
	//env->DeleteLocalRef(storage);
	__android_log_print(ANDROID_LOG_INFO, "JNI::dispose", "NativeDispose");
}

JNIEXPORT jintArray JNICALL Java_test_ImageProcessing_ImageProcessing_GetLastFrame(
		JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray yuv) {
	jintArray bgra = env->NewIntArray(width * height);
	jbyte* _yuv = env->GetByteArrayElements(yuv, 0);
	jint* _bgra = env->GetIntArrayElements(bgra, 0);

	Mat myuv(height + height / 2, width, CV_8UC1, (unsigned char *) _yuv);//data
	Mat mbgra(height, width, CV_8UC4, (unsigned char *) _bgra);		//BGRA

	cvtColor(myuv, mbgra, CV_YUV420sp2BGR, 4);

	env->ReleaseIntArrayElements(bgra, _bgra, 0);
	env->ReleaseByteArrayElements(yuv, _yuv, 0);

	return bgra;
}

JNIEXPORT jintArray JNICALL Java_test_ImageProcessing_ImageProcessing_LastFrameFace(
		JNIEnv* env, jobject thiz, jint width, jint height, jintArray bgra) {
	if (jbyteArr == NULL) {
		jbyteArr = (jbyteArray) env->NewByteArray(width * height);
	}
	jbyte* _gray = env->GetByteArrayElements(jbyteArr, 0);
	jint* _bgra = env->GetIntArrayElements(bgra, 0);
	Mat mbgra(height, width, CV_8UC4, (unsigned char *) _bgra);		//BGRA
	Mat mgray(height, width, CV_8UC1, (unsigned char *) _gray);
	cvtColor(mbgra, mgray, CV_BGR2GRAY, 1);
	IplImage img_color = mbgra;
	IplImage img_gray = mgray;
	IplImage* img_gray_half = cvCreateImage(
			cvSize(img_gray.width / rateNum, img_gray.height / rateNum),
			IPL_DEPTH_8U, 1);
	cvResize(&img_gray, img_gray_half, CV_INTER_CUBIC);
	__android_log_print(ANDROID_LOG_INFO, "JNI::LastFrameFace",
			"img_gray_half width : %d , height : %d", img_gray_half->width,
			img_gray_half->height);

	jintArray faceRect = NULL;
	if (FindFaceDetectHaar(&img_color, img_gray_half, 1, width, height)) {
		//LastFrame
		__android_log_print(ANDROID_LOG_INFO, "JNI::LastFrameFace", "Face : %d",
				lastFrameFaceDataRect[0]->faceDatas.size());
		faceRect = env->NewIntArray(
				lastFrameFaceDataRect[0]->faceDatas.size() * 4);
		__android_log_print(ANDROID_LOG_INFO, "JNI::LastFrameFace",
				"NewIntArray");
		jint* faceRectData = env->GetIntArrayElements(faceRect, 0);

		for (int i = 0; i < lastFrameFaceDataRect[0]->faceDatas.size(); i++) {
			faceRectData[i * 4] = lastFrameFaceDataRect[0]->faceDatas[i]->x;
			faceRectData[i * 4 + 1] = lastFrameFaceDataRect[0]->faceDatas[i]->y;
			faceRectData[i * 4 + 2] =
					lastFrameFaceDataRect[0]->faceDatas[i]->width;
			faceRectData[i * 4 + 3] =
					lastFrameFaceDataRect[0]->faceDatas[i]->height;
			__android_log_print(ANDROID_LOG_INFO, "JNI::LastFrameFace",
					"faceRectData[i].x : %d , faceRectData[i].y : %d , faceRectData[i].w : %d , faceRectData[i].h : %d",
					faceRectData[i * 4], faceRectData[i * 4 + 1],
					faceRectData[i * 4 + 2], faceRectData[i * 4 + 3]);
		}

		env->ReleaseIntArrayElements(faceRect, faceRectData, 0);
	}

	cvReleaseImage(&img_gray_half);
	env->ReleaseIntArrayElements(bgra, _bgra, 0);
	env->ReleaseByteArrayElements(jbyteArr, _gray, 0);

	return faceRect;
}

JNIEXPORT void JNICALL Java_test_ImageProcessing_ImageProcessing_SetCustomRect
(JNIEnv* env, jobject thiz, jint faceX, jint faceY, jint faceWidth, jint faceHeight)
{
	__android_log_print(ANDROID_LOG_INFO, "JNI::SetCustomRect", "Start");
	//int size = lastFrameFaceDataRect[0]->faceDatas.size();
	CvRect* r = new CvRect;
	r->x = faceX; r->y = faceY; r->width = faceWidth; r->height = faceHeight;
	//CvRect r = cvRect(faceX, faceY, faceWidth, faceHeight);
	__android_log_print(ANDROID_LOG_INFO, "JNI::SetCustomRect", "push_back");
	if(lastFrameFaceDataRect.empty())
	{
		faceSubData = new FacesSubData();
		lastFrameFaceDataRect.push_back(faceSubData);
	}
	lastFrameFaceDataRect[0]->faceDatas.push_back(r);
	__android_log_print(ANDROID_LOG_INFO, "JNI::SetCustomRect", "x : %d , y : %d , width : %d , height : %d", lastFrameFaceDataRect[0]->faceDatas[0]->x, lastFrameFaceDataRect[0]->faceDatas[0]->y, lastFrameFaceDataRect[0]->faceDatas[0]->width, lastFrameFaceDataRect[0]->faceDatas[0]->height);
}

}
