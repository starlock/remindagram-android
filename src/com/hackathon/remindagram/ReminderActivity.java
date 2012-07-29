package com.hackathon.remindagram;

import android.os.Bundle;
import android.app.Activity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class ReminderActivity extends Activity {
	public static Reminder reminder;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
    
        ImageView iv = (ImageView)findViewById(R.id.reminderImage);
        
        iv.setImageBitmap(reminder.bitmap);
        
        if (reminder.timeCreated != null) {
	        TextView dv = (TextView)findViewById(R.id.textReminderDate);
	        String dateString = DateFormat.getMediumDateFormat(this).format(reminder.timeCreated);
	        String timeString = DateFormat.getTimeFormat(this).format(reminder.timeCreated);
	        dv.setText(timeString + "\n" + dateString);
        }
    }

}
