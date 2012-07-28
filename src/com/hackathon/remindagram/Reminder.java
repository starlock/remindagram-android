package com.hackathon.remindagram;

import java.io.File;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Reminder implements Serializable {
	private static final String TAG = "Reminder";	
	
	// Transient so we don't serialize it.
	transient Bitmap bitmap;
	
	String bitmapFilename;

	public void LoadImage() {
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();  
		bitmapOptions.inSampleSize = 4;  
		Log.i(TAG, bitmapFilename);
		bitmap = BitmapFactory.decodeFile(bitmapFilename, bitmapOptions);
		Log.i(TAG, "Decoded");
	}

	public void Delete() {
		new File(bitmapFilename).delete();
	}
}
