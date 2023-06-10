package com.harish.test.opencvcamera;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{
    JavaCameraView mJavaCameraView;
    Mat mRGBA, mImgGrey, mImgCanny;
    BaseLoaderCallback mBaseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case BaseLoaderCallback.SUCCESS:{
                    mJavaCameraView.enableView();
                    break;
                }
                default:{
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    static {
        System.loadLibrary("MyLibs");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", NativeClass.getMessageFromJNI());
        mJavaCameraView = findViewById(R.id.java_camera_view);
        mJavaCameraView.setVisibility(SurfaceView.VISIBLE);
        mJavaCameraView.setCvCameraViewListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mJavaCameraView != null){
            mJavaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (mJavaCameraView != null){
            mJavaCameraView.disableView();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity", "OpenCV Loaded");
            mBaseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d("MainActivity", "NOT LOADED");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_9, this, mBaseLoaderCallback);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
        mImgGrey = new Mat(height, width, CvType.CV_8UC1);
        mImgCanny = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRGBA.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        int matType = 0;
        //Fix camera orientation for portrait mode
        mRGBA = inputFrame.rgba();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            Mat mRGBA_T = mRGBA.t();
            Core.transpose(mRGBA,mRGBA_T);
            Core.flip(mRGBA_T, mRGBA_T, 1);
            Imgproc.resize(mRGBA_T, mRGBA_T, mRGBA.size());

            mRGBA = mRGBA_T;
        }
        if (matType == 1){
            Imgproc.cvtColor(mRGBA, mRGBA, Imgproc.COLOR_RGB2GRAY);
            //mRGBA = mImgGrey;
        } else if (matType == 2){
            //Imgproc.cvtColor(mRGBA, mRGBA, Imgproc.COLOR_RGB2GRAY);
            Imgproc.Canny(mRGBA, mRGBA, 50, 150);
            //mRGBA = mImgCanny;
        } else if (matType == 3){
            Imgproc.cvtColor(mRGBA, mRGBA, 50, 150);
        }

        NativeClass.faceDetection(mRGBA.getNativeObjAddr());
        return mRGBA;
    }
}
