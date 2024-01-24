package com.example.opencv;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class CameraFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {

    public static final int CAMERA_ID_BACK  = 99;
    public static final int CAMERA_ID_FRONT = 98;
    private int mCameraId = CAMERA_ID_BACK;
    private JavaCameraView javaCameraView;
    private Mat mRGBA;

    public CameraFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        view.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                switchCamera();
                v.performClick(); // Should perform click for accessibility compatibility
                return true;
            }
            return false;
        });

        javaCameraView = view.findViewById(R.id.java_camera_view);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);

        if (getActivity() != null) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            setupCamera();
            javaCameraView.setCameraIndex(mCameraId);
            javaCameraView.setCameraPermissionGranted();
        }

        return view;
    }

    private void setupCamera() {
        CameraManager manager;
        manager = (getContext() != null) ? (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE) : null;
        try {
            assert manager != null;
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    mCameraId = CameraBridgeViewBase.CAMERA_ID_BACK;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void switchCamera() {
        mCameraId = (mCameraId == CAMERA_ID_BACK) ? CAMERA_ID_FRONT : CAMERA_ID_BACK;
        javaCameraView.disableView();
        javaCameraView.setCameraIndex(mCameraId);
        javaCameraView.enableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(javaCameraView != null) {
            javaCameraView.enableView();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        if (mRGBA != null) {
            mRGBA.release();
        }
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRGBA = inputFrame.rgba();

        if (getActivity() instanceof MainActivity) {
            boolean isTrained = ((MainActivity) getActivity()).isTrained();
            if (isTrained) {
                long addrFrame = mRGBA.getNativeObjAddr();
                NativeClass.compareAndMarkOwner(addrFrame);
            }
        }
        //adjustImageOrientation();
        return mRGBA;
    }

    private void adjustImageOrientation() {
        int currentDeviceOrientation = getResources().getConfiguration().orientation;

        if (currentDeviceOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            Core.flip(mRGBA, mRGBA, -1);
        } else if (currentDeviceOrientation == Configuration.ORIENTATION_PORTRAIT) {
            Core.rotate(mRGBA, mRGBA, Core.ROTATE_90_CLOCKWISE);
        }
    }
}