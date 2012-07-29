package com.hackathon.remindagram;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.Date;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ContextWrapper;
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
    String captureDirectory;
	String takenFilename;
	String captureFilename;
	CoolImageList imageList;

	
	public static void copyFile(File src, File dst) throws IOException
	{
	    FileChannel inChannel = new FileInputStream(src).getChannel();
	    FileChannel outChannel = new FileOutputStream(dst).getChannel();
	    try
	    {
	        inChannel.transferTo(0, inChannel.size(), outChannel);
	    }
	    finally
	    {
	        if (inChannel != null)
	            inChannel.close();
	        if (outChannel != null)
	            outChannel.close();
	    }
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    	
        bitmapDirectory = getFilesDir().getAbsolutePath();
        captureDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();
        
        Log.i(TAG, "User Directory: " + bitmapDirectory);
        
        imageList = (CoolImageList)findViewById(R.id.imagelist);
        items = new Reminder[6];
        for (int i = 0; i < 6; i++) {
        	items[i] = new Reminder();
        	items[i].defaultImage = i;
        }
        
        imageList.reminders = items;
        loadReminders();
        cleanPictures();
 
        for (int i = 0; i < 6; i++) {
        	if (items[i] != null && items[i].bitmap == null)
        		items[i].LoadImage(this);
        }
        
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
    
	public void cleanPictures() {
		File dir = new File(bitmapDirectory);
		for (File child : dir.listFiles()) {
			if (".".equals(child.getName()) || "..".equals(child.getName())) {
				continue; // Ignore the self and parent aliases.
			}
			if (child.getName().startsWith("pic")) {
				boolean found = false;
				for (int i = 0; i < items.length; i++) {
					if (items[i] != null
							&& items[i].bitmapFilename != null
							&& items[i].bitmapFilename
									.endsWith(child.getName())) {
						found = true;
					}
				}

				if (!found) {
					Log.i(TAG, "Deleting orphaned picture " + child.getName());
					child.delete();
				}
			}
		}
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
    				if (!items[i].LoadImage(this)) {
    					items[i].Delete();
    					items[i].defaultImage = i;
    					items[i].bitmap = null;
       				}
    			}
    		}
    	}
    }
    
    private void saveReminders() {
    	for (int i = 0; i < items.length; i++) {
    		String filename = bitmapDirectory + "/reminder" + i + ".bin";
    		File f = new File(filename);
    		saveObject(items[i], filename);
    	}
    }

    private void takePicture() {
    	Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		captureFilename = captureDirectory + "/pic" + String.valueOf (System.currentTimeMillis()) + ".jpg";
    	takenFilename = bitmapDirectory + "/pic" + String.valueOf (System.currentTimeMillis()) + ".jpg";
		i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(captureFilename)));
		startActivityForResult(i, 1);
    }
    
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.imagelist) {
			CoolImageList cil = (CoolImageList)v;
			if (cil.clickedAddReminder) {
				imageList.blackened = true;
				imageList.invalidate();
				takePicture();
			} else if (cil.clickedShare){
				
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
		Log.i(TAG, "onActivityResult");
		imageList.blackened = false;
		if (resultCode == Activity.RESULT_CANCELED)
		{
			imageList.invalidate();
			return;
		}
		for (int i = 5; i > 0; i--) {
			items[i] = items[i - 1];
		}
		try {
			copyFile(new File(captureFilename), new File(takenFilename));
			new File(captureFilename).delete();
			Reminder rmd = new Reminder();
			rmd.bitmapFilename = takenFilename;
			rmd.timeCreated = new Date();
			rmd.defaultImage = -1;
			rmd.LoadImage(this);
			items[0] = rmd;
			imageList.updateBitmaps();
			imageList.invalidate();
			saveReminders();
		} catch (IOException io) {
			Log.i(TAG, io.toString());
		}
	}
}
