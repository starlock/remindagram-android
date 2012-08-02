package com.henrikrydgard.mindsnap;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import com.henrikrydgard.mindsnap.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public class Reminder implements Serializable {
	private static final String TAG = "Reminder";
		
	// Transient so we don't serialize it, since we can't.
	transient Bitmap bitmap;
	transient Bitmap thumb;  // TODO
	
	Date timeCreated;
	String thumbFilename;  // TODO
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
		
		int inSampleSize = 4;
		
		// This stuff should be done on a background thread.
		bitmap = BitmapUtil.loadBitmapRotated(bitmapFilename, inSampleSize);
		return true;
	}

	public void Delete() {
		if (bitmapFilename != null) {			
			new File(bitmapFilename).delete();
		}
	}
}
