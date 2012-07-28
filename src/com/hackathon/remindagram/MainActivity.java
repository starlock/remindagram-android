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
	
    Bitmap [] items;
	ImageView [] imageViews;
	String takenFilename;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    	items = new Bitmap[6];
    	imageViews = new ImageView[6];
        imageViews[0] = (ImageView)findViewById(R.id.imageView0);
        imageViews[1] = (ImageView)findViewById(R.id.imageView1);
        imageViews[2] = (ImageView)findViewById(R.id.imageView2);
        imageViews[3] = (ImageView)findViewById(R.id.imageView3);
        imageViews[4] = (ImageView)findViewById(R.id.imageView4);
        imageViews[5] = (ImageView)findViewById(R.id.imageView5);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        ((Button)findViewById(R.id.buttonSnap)).setOnClickListener(this);
        return true;
    }
    
    private void updateImageViews() {
    	for (int i = 0; i < 6; i++) {
    		if (items[i] != null)
    			imageViews[i].setImageBitmap(items[i]);
    	}
    }
	
	@Override
	public void onClick(View v) {
		Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		takenFilename = Environment.getExternalStorageDirectory() + "/testExtra" + String.valueOf (System.currentTimeMillis()) + ".jpg";
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
		
		Bitmap bmp;// = (Bitmap) data.getExtras().get("data"); 
		BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();  
		// Limit the filesize since 5MP pictures will kill you RAM  
		bitmapOptions.inSampleSize = 4;  
		Log.i(TAG, takenFilename);
		bmp = BitmapFactory.decodeFile(takenFilename, bitmapOptions);
		Log.i(TAG, "Decoded");
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		if (w == h) {
			items[0] = bmp;
		} else if (w > h) {
			items[0] = Bitmap.createBitmap(bmp, (w - h) / 2, 0, h, h);
		} else {
			items[0] = Bitmap.createBitmap(bmp, 0, (h - w) / 2, w, w);
		}
		
		updateImageViews();
	}
}
