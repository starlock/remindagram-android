package com.hackathon.remindagram;

import java.io.File;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Reminder implements Serializable {
	private static final String TAG = "Reminder";	
	
	transient Bitmap bitmap;
	String bitmapFilename;

	public void LoadImage() {

		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();  
		bitmapOptions.inSampleSize = 4;  
		Log.i(TAG, bitmapFilename);
		bitmap = BitmapFactory.decodeFile(bitmapFilename, bitmapOptions);
		
		Log.i(TAG, "Decoded");
		
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		
		if (w > h) {
			bitmap = Bitmap.createBitmap(bitmap, (w - h) / 2, 0, h, h);
		} else if (w < h) {
			bitmap = Bitmap.createBitmap(bitmap, 0, (h - w) / 2, w, w);
		}
		
	}

	public void Delete() {
		new File(bitmapFilename).delete();
	}
}
