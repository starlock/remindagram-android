package com.hackathon.remindagram;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ImageView;

public class ReminderActivity extends Activity {
	public static Reminder reminder;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
    
        ImageView iv = (ImageView)findViewById(R.id.reminderImage);
        
        iv.setImageBitmap(reminder.bitmap);
    }

}
