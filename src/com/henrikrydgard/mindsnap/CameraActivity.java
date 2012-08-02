package com.henrikrydgard.mindsnap;

import com.henrikrydgard.mindsnap.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class CameraActivity extends Activity {
    @SuppressWarnings("deprecation")
    private static final String TAG = "CameraActivity";
    
    public static byte [] jpegTaken;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_camera);

        jpegTaken = null;

        Button take = (Button)findViewById(R.id.buttonTake);
        take.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CameraActivity.this.takePicture();
			}
		});
        
        Button btn = (Button)findViewById(R.id.buttonSwitchCamera);
        btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CameraActivity.this.switchCamera();				
			}
		});
    }
	

    private class HandlePictureStorage implements PictureCallback
    {
        @Override
        public void onPictureTaken(byte[] picture, Camera camera) 
        {
            //The picture can be stored or do something else with the data
            //in this callback such sharing with friends, upload to a Cloud component etc
            
            //This is invoked when picture is taken and the data needs to be processed
            Log.i(TAG, "Picture successfully taken!");

            // Save a pointer to the picture data.
            jpegTaken = picture;
            
            CameraActivity.this.setResult(RESULT_OK);
            CameraActivity.this.finish();
        }
    }
    
    private void takePicture() {
    	CameraSurfaceView camView = (CameraSurfaceView)findViewById(R.id.cameraView);
		camView.getCamera().takePicture(null, null, new HandlePictureStorage());
    }
    
    private void switchCamera() {
    	CameraSurfaceView camView = (CameraSurfaceView)findViewById(R.id.cameraView);
    	camView.switchCamera();
    }
}
