package com.hackathon.remindagram;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
    Bitmap tiledBG;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    	
        bitmapDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        
        imageList = (CoolImageList)findViewById(R.id.imagelist);
        items = new Reminder[6];
        imageList.reminders = items;
        loadReminders();
        
        imageList.setOnClickListener(this);
    }
    
    public void onDestroy() {
    	super.onDestroy();
    	
    	saveReminders();
    	// TEMPORARY: Just kill all the images
    	//for (int i = 0; i < items.length; i++) {
    	//	if (items[i] != null)
    	//		items[i].Delete();
    	//}
    }

    public static void saveObject(Object o, String filename) {
    	FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try
        {
            fos = new FileOutputStream(filename);
            out = new ObjectOutputStream(fos);
            out.writeObject(o);
            out.close();
        }
        catch(IOException ex)
        {
    		Log.i(TAG, "IOException");
            ex.printStackTrace();
        }
    }
    
    public static Object loadObject(String filename) {
    	FileInputStream fis = null;
    	ObjectInputStream in = null;
    	Object o = null;
    	try
    	{
    	    fis = new FileInputStream(filename);
    	    in = new ObjectInputStream(fis);
    	    o = in.readObject();
    	    in.close();
        }
    	catch(IOException ex)
    	{
    		Log.i(TAG, "IOException");
    		ex.printStackTrace();
    	}
    	catch(ClassNotFoundException ex)
    	{
    		Log.i(TAG, "ClassNotFoundException");
    		ex.printStackTrace();
    	}
    	return o;
    }
    
    
    private void loadReminders() {
    	for (int i = 0; i < items.length; i++) {
    		String filename = bitmapDirectory + "/reminder" + i + ".bin";
    		File f = new File(filename);
    		if (f.exists()) {
    			Log.i(TAG,"Found: "+ filename);
    			items[i] = (Reminder)loadObject(filename);
    			if (items[i] != null) {
    				items[i].LoadImage();
    			}
    		} else {
    			items[i] = null;
    		}
    	}
    }
    
    private void saveReminders() {
    	for (int i = 0; i < items.length; i++) {
    		String filename = bitmapDirectory + "/reminder" + i + ".bin";
    		File f = new File(filename);
    		if (items[i] != null) {
    			saveObject(items[i], filename);
    		} else {
    			f.delete();
    		}
    	}
    }
    
    
  
    private void takePicture() {
    	Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		takenFilename = bitmapDirectory + "/pic" + String.valueOf (System.currentTimeMillis()) + ".jpg";
		i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File (takenFilename)));
		startActivityForResult(i, 1);
    }
    
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.imagelist) {
			CoolImageList cil = (CoolImageList)v;
			if (cil.clickAddReminder) {
				takePicture();
			} else if (cil.clickShare){
				
			} else {
				ReminderActivity.reminder = cil.clickedReminder;
				Intent i = new Intent(this, ReminderActivity.class);
				startActivity(i);
			}
		}
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
		
		rmd.bitmapFilename = takenFilename;
		rmd.LoadImage();
		items[0] = rmd;
		imageList.updateBitmaps();
		imageList.invalidate();
	}
}
