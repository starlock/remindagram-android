package com.henrikrydgard.mindsnap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.Date;

import com.henrikrydgard.mindsnap.R;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;


public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "Remindagram";
	
    Reminder [] items;
    String bitmapDirectory;
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

	public static boolean writeFile(byte [] data, File dst)
	{
		try {
		    FileOutputStream stream = new FileOutputStream(dst);
		    stream.write(data);
		    stream.close();
		    return true;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
	}


	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    	
        bitmapDirectory = getFilesDir().getAbsolutePath();
        
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
							&& items[i].bitmapFilename.endsWith(child.getName())) {
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
    	takenFilename = bitmapDirectory + "/pic" + String.valueOf (System.currentTimeMillis()) + ".jpg";
    	Intent i = new Intent(this, CameraActivity.class);
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

		if (CameraActivity.jpegTaken != null) {
			Log.i(TAG, "Got jpeg as byte array!");
			if (writeFile(CameraActivity.jpegTaken, new File(takenFilename))) {
				// Managed to write the file, let's go ahead and add it.
				Reminder rmd = new Reminder();
				rmd.bitmapFilename = takenFilename;
				rmd.timeCreated = new Date();
				rmd.defaultImage = -1;
				rmd.LoadImage(this);
				for (int i = 5; i > 0; i--) {
					items[i] = items[i - 1];
				}
				items[0] = rmd;
				imageList.updateBitmaps();
				imageList.invalidate();
				saveReminders();
			}
		} else {
			Log.i(TAG, "Got no jpeg :(");
		}
		imageList.invalidate();
	}
}
