package com.hackathon.remindagram;

import java.io.File;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "Remindagram";
	
    Reminder [] items;
    String bitmapDirectory;
	String takenFilename;
	CoolImageList imageList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    	
        bitmapDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        
        imageList = (CoolImageList)findViewById(R.id.imagelist);
        items = new Reminder[6];
        imageList.reminders = items;
        for (int i = 0; i < 6; i++) {
        	items[i] = null;
        }
        ((Button)findViewById(R.id.buttonSnap)).setOnClickListener(this);
    }
    
    public void onDestroy() {
    	super.onDestroy();
    	// TEMPORARY: Just kill all the images
    	for (int i = 0; i < items.length; i++) {
    		if (items[i] != null)
    			items[i].Delete();
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
  
	@Override
	public void onClick(View v) {
		Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		takenFilename = bitmapDirectory + "/pic" + String.valueOf (System.currentTimeMillis()) + ".jpg";
		i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File (takenFilename)));

		startActivityForResult(i, 1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_CANCELED)
			return;
		for (int i = 5; i > 0; i--) {
			items[i] = items[i - 1];
		}
		
		Reminder rmd = new Reminder();
		
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();  
		bitmapOptions.inSampleSize = 4;  
		Log.i(TAG, takenFilename);
		rmd.bitmap = BitmapFactory.decodeFile(takenFilename, bitmapOptions);
		rmd.bitmapFilename = takenFilename;
		Log.i(TAG, "Decoded");
		int w = rmd.bitmap.getWidth();
		int h = rmd.bitmap.getHeight();
		
		if (w > h) {
			rmd.bitmap = Bitmap.createBitmap(rmd.bitmap, (w - h) / 2, 0, h, h);
		} else if (w < h) {
			rmd.bitmap = Bitmap.createBitmap(rmd.bitmap, 0, (h - w) / 2, w, w);
		}
		items[0] = rmd;
		
		imageList.updateBitmaps();
		imageList.invalidate();
	}
}
