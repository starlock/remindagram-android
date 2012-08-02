package com.henrikrydgard.mindsnap;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.SurfaceHolder;
import android.graphics.ImageFormat;
import android.hardware.Camera;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	private static final String TAG = "CameraSurfaceView"; 
	private Context context;
	private SurfaceHolder holder;
	private Camera camera;
	private int numCameras;
	private int curCameraId;
	private boolean error;  // Camera error of some sort
	private Activity activity;  // Track the rotation through this activity. Must be set.
	
	@SuppressWarnings("deprecation")
	public CameraSurfaceView(Context context, AttributeSet attr) {
		super(context, attr);
		this.context = context;
		
		numCameras = Camera.getNumberOfCameras();
		curCameraId = 0;
        
		// Initiate the Surface Holder properly
		this.holder = this.getHolder();
		this.holder.addCallback(this);
		this.holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		activity = (Activity)context;
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			// Open the Camera in preview mode
			this.camera = Camera.open(curCameraId);
			this.camera.setPreviewDisplay(this.holder);
			
			// API 14
			// this.camera.setRecordingHint(false);  // Optimize for still pictures
		} catch (IOException ioe) {
			ioe.printStackTrace(System.out);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = camera.getParameters();
		
		// We need to choose a preview size from the allowed sizes.
		
		Log.i(TAG, "Wanted preview size: " + width + " x " + height);
		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
		
		float aspect = (float)width/height;
		
		int bestWidth = 0;
		int bestHeight = 0;
		float bestScore = 99999.f;
		
		for (Camera.Size size : previewSizes) {
			float sizeAspect = (float)size.width / size.height;
			
			// Throw out too small previews
			if (size.width < 300 || size.height < 300)
				continue;
			
			// float aspectScore = Math.abs(sizeAspect - aspect);

			// First consider same orientation
			float sizeScore = Math.abs(width - size.width) + Math.abs(height - size.height);  
			Log.i(TAG, "Allowed preview size: " + size.width + " x " + size.height + ", score: " + sizeScore);
			if (sizeScore < bestScore) {
				bestWidth = size.width;
				bestHeight = size.height;
				bestScore = sizeScore;
			}
			
			// Then consider 90 degree orientation
			float sizeScore90 = Math.abs(height - size.width) + Math.abs(width - size.height);  
			if (sizeScore90 < bestScore) {
				bestWidth = size.width;
				bestHeight = size.height;
				bestScore = sizeScore90;
			}
		}
		if (bestWidth == 0) {
			error = true;
			return;
		}
		
		parameters.setPreviewSize(bestWidth, bestHeight);
		// parameters.setPictureFormat(ImageFormat.JPEG);
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(curCameraId, info);

		int degrees = 0;
		switch (activity.getWindowManager().getDefaultDisplay().getRotation()) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);

		int orientation = activity.getWindowManager().getDefaultDisplay().getRotation();
		orientation = (orientation + 45) / 90 * 90;
		
		int rotation = 0;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			rotation = (info.orientation - orientation + 360) % 360;
		} else { // back-facing camera
			rotation = (info.orientation + orientation) % 360;
		}
		parameters.setRotation(rotation);
		parameters.setJpegQuality(80);
		
	    camera.setParameters(parameters);
		setCameraDisplayOrientation(activity, curCameraId, camera);
		camera.startPreview();
		error = false;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when replaced with a new screen
		// Always make sure to release the Camera instance
		camera.stopPreview();
		camera.release();
		camera = null;
	}

	public Camera getCamera() {
		return this.camera;
	}
	
	public int getCameraId() {
		return curCameraId;
	}

	// Want to be able to switch to front camera too!
	public void switchCamera() {
		// TODO
	}


	public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
}
		

}