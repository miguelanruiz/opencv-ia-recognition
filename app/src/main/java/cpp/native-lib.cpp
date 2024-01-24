//
// Created by Dell on 1/22/2024.
//
#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <opencv2/core.hpp>
#include <opencv2/imgcodecs.hpp>
#include <vector>
#include <fstream>
#include "native-lib.h"

#define NUM_COMPONENTS 5
#define PCA_MODEL_FILENAME "pca_model.xml"
#define HAAR_CASCADE_PATH "/haarcascade_frontalface_default.xml"

using namespace std;
using namespace cv;

static PCA globalPCA;
static Mat globalOwnerProjection;
static CascadeClassifier globalFaceCascade;

Mat asRowMatrix(const vector<Mat>& images, int rtype, double alpha=1, double beta=0);

extern "C" JNIEXPORT void JNICALL
Java_com_example_opencv_NativeClass_openCVVersion(
        JNIEnv* env,
        jclass thiz /* this */) {
    string opencvVersion = getVersionString();
    LOGI("OpenCV version from C++: %s", opencvVersion.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_opencv_NativeClass_train(JNIEnv* env, jclass, jlongArray imageMats, jint numImages, jstring internalDirPath) {
    const char* internalPathCStr = env->GetStringUTFChars(internalDirPath, nullptr);
    string internalPath(internalPathCStr);
    env->ReleaseStringUTFChars(internalDirPath, internalPathCStr);

    LOGI("Internal path: %s", internalPath.c_str());

    string haarCascadePath = internalPath;
    CascadeClassifier face_cascade;
    if (!face_cascade.load(haarCascadePath)) {
        // Error to load the cascade
        LOGI("Error loading cascade");
        return;
    }

    // Convert the imageMats jlongArray to a vector of Mat
    jlong* matAddrs = env->GetLongArrayElements(imageMats, JNI_FALSE);
    vector<Mat> images(numImages);
    for (int i = 0; i < numImages; ++i) {
        images[i] = *(Mat*)(matAddrs[i]);
    }
    env->ReleaseLongArrayElements(imageMats, matAddrs, JNI_ABORT); // Use JNI_ABORT to not copy back changes (no changes were made)

    vector<Mat> faceImages;
    for (Mat& image : images) {
        vector<Rect> faces;
        Mat grayMat;

        cvtColor(image, grayMat, COLOR_BGR2GRAY);
        face_cascade.detectMultiScale(grayMat, faces, 1.1, 2, 0|CASCADE_SCALE_IMAGE, Size(30, 30));

        for (const Rect& face : faces) {
            Mat faceROI = grayMat(face);
            Mat resizedFace;
            resize(faceROI, resizedFace, Size(200, 200)); // Normalize size.
            faceImages.push_back(resizedFace);
        }
    }

    // Check if enough faces were detected for PCA
    if (faceImages.size() < static_cast<size_t>(NUM_COMPONENTS)) {
        // Error, not enough images.
        LOGI("Not enough faces detected for PCA");
        return;
    }

    // Convert face images to row matrix
    Mat data = asRowMatrix(faceImages, CV_32FC1);

    // Perform PCA
    PCA pca(data, Mat(), PCA::DATA_AS_ROW, NUM_COMPONENTS);

    // Calculate the average projection (assuming all faces are of the owner)
    Mat avgProjection = Mat::zeros(1, NUM_COMPONENTS, CV_32F);
    for (int i = 0; i < faceImages.size(); ++i) {
        avgProjection += pca.project(faceImages[i].reshape(1, 1));
    }
    avgProjection /= faceImages.size();

    // Save the PCA model to a file in the internal storage
    string pcaFilename = internalPath + PCA_MODEL_FILENAME;
    FileStorage fs(pcaFilename, FileStorage::WRITE);
    pca.write(fs);
    fs.release();

    // Save the global variables
    globalPCA = pca;
    globalOwnerProjection = avgProjection;
    globalFaceCascade = face_cascade;

    LOGI("PCA model saved to: %s", pcaFilename.c_str());
}

extern "C" JNIEXPORT jlong JNICALL
Java_com_example_opencv_NativeClass_compareAndMarkOwner(JNIEnv* env, jclass, jlong addrFrame) {
    Mat& frame = *(Mat*)addrFrame;
    Mat gray;
    cvtColor(frame, gray, COLOR_BGR2GRAY);
    vector<Rect> faces;
    globalFaceCascade.detectMultiScale(gray, faces, 1.1, 2, 0|CASCADE_SCALE_IMAGE, Size(30, 30));

    double threshold = 12000;

    for (const Rect& face : faces) {
        Mat faceROI = gray(face);
        Mat resizedFace;
        resize(faceROI, resizedFace, Size(200, 200));

        Mat projected = globalPCA.project(resizedFace.reshape(1, 1));
        double distance = norm(projected - globalOwnerProjection);

        if (distance < threshold) {
            rectangle(frame, face, Scalar(0, 255, 0), 2); // Draw a rectangle if similar
        }
        else {
            LOGI("Distance: %f", distance);
            LOGI("Threshold: %f", threshold);
        }
    }

    return (jlong)(&frame);
}

Mat asRowMatrix(const vector<Mat>& images, int rtype, double alpha, double beta) {
    size_t total = images.size() * images[0].total() * images[0].elemSize();
    Mat data((int)images.size(), (int)(total / images.size()), rtype);
    for (size_t i = 0; i < images.size(); i++) {
        Mat xi = data.row((int)i);
        images[i].reshape(1, 1).convertTo(xi, rtype, alpha, beta);
    }
    return data;
}
