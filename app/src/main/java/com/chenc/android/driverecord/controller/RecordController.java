package com.chenc.android.driverecord.controller;

import android.content.Context;
import android.view.Surface;

import com.chenc.android.driverecord.media.CameraWrapper;

/**
 * Controller for the Video preview and record logic, coordinate surfaces and camera.
 */
public class RecordController implements CameraWrapper.Callback {
  private Context mContext;
  private CameraWrapper mCameraWrapper;
  private boolean mIsCameraReady;
  private Surface mPreviewSurface;

  public RecordController(Context context) {
    mContext = context;

    mPreviewSurface = null;

    mIsCameraReady = false;
    mCameraWrapper = CameraWrapper.getInstance();
    mCameraWrapper.initCamera(mContext, this);
  }

  public void onCameraPreviewReady(Surface previewSurface) {
    mPreviewSurface = previewSurface;
    mCameraWrapper.registerSurface(mPreviewSurface);
    checkIfSurfaceReady();
  }

  public void onCameraReady() {
    mIsCameraReady = true;
    checkIfSurfaceReady();
  }

  private void checkIfSurfaceReady() {
    // NOTE: when we have more surface, check if all needed surface is loaded
    if (mIsCameraReady && mPreviewSurface != null) {
      mCameraWrapper.startCamera();
    }
  }
}
