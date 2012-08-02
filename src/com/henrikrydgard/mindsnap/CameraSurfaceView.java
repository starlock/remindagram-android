package com.henrikrydgard.mindsnap;

import java.io.IOException;
import java.util.List;

import android.annotation.SuppressLint;
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
	private static int curCameraId = 0;
	private boolean error;  // Camera error of some sort
	private Activity activity;  // Track the rotation through this activity. Must be set.
	
	@SuppressWarnings("deprecation")
	public CameraSurfaceView(Context context, AttributeSet attr) {
		super(context, attr);
		this.context = context;
		
		numCameras = Camera.getNumberOfCameras();
	    
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

	int cameraDisplayOrientation;
	int degrees;
	int surfaceWidth;
	int surfaceHeight;
	boolean facingFront;
	
	private void setupCamera(Camera camera) {
		camera.setDisplayOrientation(cameraDisplayOrientation);
		Log.i(TAG, "Display rotation: " + degrees + /*"  Fixed cam orientation: " + camOrientation +*/ "  Camera display rotation set: " + cameraDisplayOrientation);
		
		// Now that the orientation is known, let's start setting up the parameters.
		Camera.Parameters parameters = camera.getParameters();
		parameters.setPictureFormat(ImageFormat.JPEG);
		parameters.setJpegQuality(80);

		// Set the orientation of the saved JPEG. Not quite sure how this makes sense, but meh.
		if (facingFront) {
			parameters.setRotation((360 - 90 + degrees) % 360);
		} else {
			parameters.setRotation((360 + 90 - degrees) % 360);
		}

		// We need to choose a preview size from the allowed sizes.
		
		Log.i(TAG, "Wanted preview size: " + surfaceWidth + " x " + surfaceHeight);
		List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
		
		float aspect = (float)surfaceWidth/surfaceHeight;
		
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
			float sizeScore = Math.abs(surfaceWidth - size.width) + Math.abs(surfaceHeight - size.height);  
			Log.i(TAG, "Allowed preview size: " + size.width + " x " + size.height + ", score: " + sizeScore);
			if (sizeScore < bestScore) {
				bestWidth = size.width;
				bestHeight = size.height;
				bestScore = sizeScore;
			}
			
			// Then consider 90 degree orientation
			float sizeScore90 = Math.abs(surfaceHeight - size.width) + Math.abs(surfaceWidth - size.height);  
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
		Log.i(TAG, "Setting preview size: " + bestWidth + " x " + bestHeight);
		parameters.setPreviewSize(bestWidth, bestHeight);
		
	    camera.setParameters(parameters);
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		// First, figure out and set the camera orientation correctly.
		
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(curCameraId, info);

		facingFront = info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
		
		int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation(); 
		int camOrientation = info.orientation;
		degrees = 0;
		switch (displayRotation) {
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
		if (facingFront) {
			result = (camOrientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (camOrientation - degrees + 360) % 360;
		}
		
		cameraDisplayOrientation = result;
		surfaceWidth = width;
		surfaceHeight = height;
		
		setupCamera(camera);
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
	@SuppressLint("NewApi")
	public void switchCamera() {
		// Just increment the camera ID and restart the activity.
		curCameraId = (curCameraId + 1) % numCameras;
		activity.recreate();
		
		// activity.startActivity(activity.getIntent()); activity.finish(); 
	}


}