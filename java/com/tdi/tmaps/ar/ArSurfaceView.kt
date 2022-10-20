package com.tdi.tmaps.ar

import android.content.Context
import android.hardware.Camera
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.beyondar.android.util.Logger
import java.io.IOException
import java.lang.Exception

/**
 * Created by Amal Krishnan on 27-01-2017.
 */
/** A basic Camera preview class  */
class ArSurfaceView(context: Context?, private var mCamera: Camera?) :
    SurfaceView(context),
    SurfaceHolder.Callback {
    private val mHolder: SurfaceHolder
    private var mIsPreviewing = false

    //    public ArSurfaceView(Context context) {
    //        super(context);
    //        this.init(context);
    //    }
    //
    //    private void init(Context context) {
    //        this.mIsPreviewing = false;
    //        this.mHolder = this.getHolder();
    //        this.mHolder.addCallback(this);
    //        this.configureCamera();
    //    }
    //
    //    public boolean isPreviewing() {
    //        return this.mCamera != null && this.mIsPreviewing;
    //    }
    //
    //    private void configureCamera() {
    //        if(this.mCamera == null) {
    //            try {
    //                Logger.v("camera", "getTheCamera");
    //                boolean acquiredCam = false;
    //                int timePassed = 0;
    //
    //                while(!acquiredCam && timePassed < 1000) {
    //                    try {
    //                        this.mCamera = Camera.open();
    //                        Logger.v("camera", "acquired the camera");
    //                    } catch (Exception var5) {
    //                        Logger.e("camera", "Exception encountered opening camera:" + var5.getLocalizedMessage());
    //
    //                        try {
    //                            Thread.sleep(200L);
    //                        } catch (InterruptedException var4) {
    //                            Logger.e("camera", "Exception encountered sleeping:" + var4.getLocalizedMessage());
    //                        }
    //                        timePassed += 200;
    //                    }
    //                }
    //            } catch (Exception var2) {
    //                Logger.e("camera", "ERROR: Unable to open the camera", var2);
    //                return;
    //            }
    //        }
    //    }
    init {

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = holder
        mHolder.addCallback(this)
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera?.setPreviewDisplay(holder)
            mCamera?.startPreview()
        } catch (e: IOException) {
            Log.d(TAG, "Error setting camera preview: " + e.message)
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, w: Int, h: Int) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.surface == null) {
            // preview surface does not exist
            return
        }

        // stop preview before making changes
        try {
            mCamera!!.stopPreview()
        } catch (e: Exception) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera!!.setPreviewDisplay(mHolder)
            mCamera!!.startPreview()
        } catch (e: Exception) {
            Log.d(TAG, "Error starting camera preview: " + e.message)
        }
    }

    fun startPreviewCamera() {
//        if(this.mCamera == null) {
//            this.init(this.getContext());
//        }
        if (mCamera != null && !mIsPreviewing) {
            mIsPreviewing = true
            try {
                mCamera!!.setPreviewDisplay(mHolder)
                mCamera!!.startPreview()
            } catch (var2: Exception) {
                Logger.w("camera", "Cannot start preview.", var2)
                mIsPreviewing = false
            }
        }
    }

    fun releaseCamera() {
        stopPreviewCamera()
        if (mCamera != null) {
            mCamera!!.release()
            mCamera = null
        }
    }

    fun stopPreviewCamera() {
        if (mCamera != null && mIsPreviewing) {
            mIsPreviewing = false
            mCamera!!.stopPreview()
        }
    }

    companion object {
        private const val TAG = "CamSurfaceView"
    }
}