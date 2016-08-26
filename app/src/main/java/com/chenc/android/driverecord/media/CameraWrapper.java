package com.chenc.android.driverecord.media;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Surface;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for Camera, providing only a singleton instance to RecordController.
 * Handling camera device life cycles, and capture request logic.
 */
public class CameraWrapper {
  public interface Callback {
    void onCameraReady();
  }

  private static final String TAG = "CameraWrapper";

  private static CameraWrapper sInstance = null;
  public static CameraWrapper getInstance() {
    if (sInstance == null) {
      sInstance = new CameraWrapper();
    }
    return sInstance;
  }

  private Context mContext;
  private CameraDevice mCamera;
  private List<Surface> mSurfaces;
  private Callback mCallback;
  private CaptureRequest.Builder mRequestBuilder;
  private CameraCaptureSession.StateCallback mSessionCallback;
  private HandlerThread mBackgroundThread;
  private Handler mBackgroundHandler;

  private CameraWrapper() {
    mCamera = null;
    mSurfaces = new ArrayList<>();
  }

  public void initCamera(Context context, final Callback callback) {
    mContext = context;
    mCallback = callback;
    openCamera();
  }

  public void registerSurface(Surface surface) {
    mSurfaces.add(surface);
  }

  public void startCamera() {
    setupBackgroundThread();
    try {
      setupCaptureBehavior();
      mCamera.createCaptureSession(mSurfaces, mSessionCallback, null);
    } catch (CameraAccessException e) {
      Log.e(TAG, "couldn't create capture session for camera: " + mCamera.getId(), e);
      return;
    }
  }

  private void openCamera() {
    if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA)
        != PackageManager.PERMISSION_GRANTED ) {
      return;
    }
    try {
      CameraManager cameraManager = (CameraManager) mContext
          .getSystemService(Context.CAMERA_SERVICE);
      String cameraId = cameraManager.getCameraIdList()[0];
      cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
          mCamera = camera;
          mCallback.onCameraReady();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
          mCamera = null;
        }

        @Override
        public void onError(CameraDevice camera, int errorCode) {
          mCamera = null;
        }
      }, null);
    }
    catch (Exception e){
      // TODO: handle exceptions
    }
  }

  private void setupBackgroundThread() {
    mBackgroundThread = new HandlerThread("CameraBackground");
    mBackgroundThread.start();
    mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
  }

  private void setupCaptureBehavior() {
    mSessionCallback = new CameraCaptureSession.StateCallback() {
      @Override
      public void onConfigured(CameraCaptureSession session) {
        Log.i(TAG, "capture session configured: " + session);
        try {
          mRequestBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
          for (Surface surface : mSurfaces) {
            mRequestBuilder.addTarget(surface);
          }
          // Auto focus should be continuous for camera preview.
          mRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
              CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

          session.setRepeatingRequest(mRequestBuilder.build(),
              // TODO: this needs to be implemented for Capture actions
              new CameraCaptureSession.CaptureCallback() {},
              mBackgroundHandler);
        } catch (CameraAccessException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onConfigureFailed(CameraCaptureSession session) {
        Log.e(TAG, "capture session configure failed: " + session);
      }
    };
  }
}
