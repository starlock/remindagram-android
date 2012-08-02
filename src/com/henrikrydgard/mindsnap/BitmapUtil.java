package com.henrikrydgard.mindsnap;

import java.io.File;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public class BitmapUtil {
	private static final String TAG = "ImageUtil";
		
	// Loads a bitmap, sampling it down, and rotates it according to its EXIF data.
	public static Bitmap loadBitmapRotated(String bitmapFilename, int inSampleSize) {
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();  
		bitmapOptions.inSampleSize = inSampleSize;  
		Bitmap bitmap = BitmapFactory.decodeFile(bitmapFilename, bitmapOptions);
		if (bitmap == null)
			return null;
		Log.i(TAG, "Decoded " + bitmap.getWidth() + " x " + bitmap.getHeight());
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
		
			Log.i(TAG, "Detected orientation " + orientation);
			if (rotate != 0) {
				Log.i(TAG, "Rotating... " + rotate);
				bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
				Matrix matrix = new Matrix();
				matrix.postRotate(-rotate);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
						bitmap.getHeight(), matrix, true);
			}
		} catch (IOException io) {
			Log.i(TAG, "EXIF failed: " + io.toString());
		}
		return bitmap;
	}


}
