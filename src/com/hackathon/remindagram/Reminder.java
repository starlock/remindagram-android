package com.hackathon.remindagram;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public class Reminder implements Serializable {
	private static final String TAG = "Reminder";
	
	
	// Transient so we don't serialize it, since we can't.
	transient Bitmap bitmap;
	
	Date timeCreated;	
	String bitmapFilename;
	int defaultImage;
	
	
	Bitmap getDefaultBitmap(Context context, int i) {
		switch (i) {
		case 0:	return BitmapFactory.decodeResource(context.getResources(), R.drawable.default1); 
		case 1:	return BitmapFactory.decodeResource(context.getResources(), R.drawable.default2); 
		case 2:	return BitmapFactory.decodeResource(context.getResources(), R.drawable.default3); 
		case 3:	return BitmapFactory.decodeResource(context.getResources(), R.drawable.default4); 
		case 4:	return BitmapFactory.decodeResource(context.getResources(), R.drawable.default5); 
		case 5: return BitmapFactory.decodeResource(context.getResources(), R.drawable.default6); 
		}
		return null;
	}

	public boolean LoadImage(Context context) {
		
		if (defaultImage != -1) {
			bitmap = getDefaultBitmap(context, defaultImage);
			return true;
		}
		
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();  
		bitmapOptions.inSampleSize = 4;  
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
		
			Log.i(TAG, "Detected orientation " + orientation);
			if (rotate != 0) {
				Log.i(TAG, "Rotating... " + rotate);
				bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
				Matrix matrix = new Matrix();
				matrix.postRotate(-rotate);
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, 
						bitmap.getWidth(), bitmap.getHeight(), 
				                              matrix, true);
			}
		} catch (IOException io) {
			Log.i(TAG, "EXIF failed: " + io.toString());
		}
		return true;
	}

	public void Delete() {
		if (bitmapFilename != null) {			
			new File(bitmapFilename).delete();
		}
	}
}
