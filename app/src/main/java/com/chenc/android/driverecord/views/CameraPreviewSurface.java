package com.chenc.android.driverecord.views;

import android.content.Context;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/** A basic Camera preview class */
public class CameraPreviewSurface extends SurfaceView implements SurfaceHolder.Callback {
  public interface Callback {
    void onPreviewSurfaceCreated(Surface previewSurface);
  }
  private static final String TAG = "CameraPreviewSurface";
  private SurfaceHolder mHolder;
  private Callback mCallback;

  public CameraPreviewSurface(Context context, Callback callback) {
    super(context);
    mCallback = callback;

    // Install a SurfaceHolder.Callback so we get notified when the
    // underlying surface is created and destroyed.
    mHolder = getHolder();
    mHolder.addCallback(this);
    // deprecated setting, but required on Android versions prior to 3.0
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  public void surfaceCreated(SurfaceHolder holder) {
    Surface surface = holder.getSurface();
    Log.d(TAG, "surface: " + surface);
    mCallback.onPreviewSurfaceCreated(surface);
  }

  public void surfaceDestroyed(SurfaceHolder holder) {
    // empty. Take care of releasing the Camera preview in your activity.
  }

  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
  }
}