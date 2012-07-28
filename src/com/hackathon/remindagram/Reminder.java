package com.hackathon.remindagram;

import java.io.File;

import android.graphics.Bitmap;

public class Reminder {
	Bitmap bitmap;
	String bitmapFilename;
	
	public void Delete() {
		new File(bitmapFilename).delete();
	}
}
