package com.hackathon.remindagram;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.media.ExifInterface;
import android.util.Log;

public class Reminder implements Serializable {
	private static final String TAG = "Reminder";	
	
	// Transient so we don't serialize it, since we can't.
	transient Bitmap bitmap;
	
	String bitmapFilename;

	public boolean LoadImage() {
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();  
		bitmapOptions.inSampleSize = 4;  
		Log.i(TAG, bitmapFilename);
		bitmap = BitmapFactory.decodeFile(bitmapFilename, bitmapOptions);
		if (bitmap == null)
			return false;
		Log.i(TAG, "Decoded");
		File imageFile = new File(bitmapFilename.toString());
		try {
			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			int rotate = 0;
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate -= 90;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate -= 90;
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate -= 90;
			}
		
			if (rotate != 0) {
				Log.i(TAG, "Rotating...");
				bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
				Canvas canvas = new Canvas(bitmap);
				canvas.rotate(rotate);
			}
		} catch (IOException io) {
			Log.i(TAG, "EXIF failed: " + io.toString());
		}
		return true;
	}

	public void Delete() {
		new File(bitmapFilename).delete();
	}
}
