package com.hackathon.remindagram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CoolImageList extends View {
	public static final String TAG = "CoolImageList";
	
	public CoolImageList(Context context, AttributeSet attr) {
		super(context, attr);
		init();
	}
	public CoolImageList(Context context) {
		super(context);
		init();
	}
	public CoolImageList(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
		init();
	}

	public void init() {
		paint = new Paint();
		emptyPaint = new Paint();
		emptyPaint.setColor(0xFF404040);
		swipeImage = -1;
		// TODO Auto-generated constructor stub
	}
	
	public Reminder [] reminders;
	
	Paint paint;
	Paint emptyPaint;
	boolean swiping;
	int swipeImage;
	int iw;
	float swipeStartX;
	float swipeX;
	static final float deleteThreshold = 0.15f;
	
	public void updateBitmaps() {
	}
	
	void paintImage(int i, Canvas canvas) {
		if (i >= reminders.length)
			return;
		
		int x = ((i & 1) == 1) ? iw : 0;
		int y = (i / 2) * iw;
		Log.i(TAG, "X: " + x + " Y: " + y);
		
		paint.setColor(0xFFFFFFFF);
		
		if (swipeImage == i) {
			x += swipeX - swipeStartX;
			if (swipeX < getWidth() * deleteThreshold) {
				int col = 0xFFFFFF;
				col |= (int)((swipeX / (getWidth() * deleteThreshold)) * 255) << 24;
				paint.setColor(col);
			}
		}
		RectF rc = new RectF(x + 2, y + 2, x + iw - 2, y + iw - 2);
		
		if (reminders[i] != null && reminders[i].bitmap != null) {
			canvas.drawBitmap(reminders[i].bitmap, null, rc, paint);
		} else {
			canvas.drawRect(rc, emptyPaint);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		iw = getWidth() / 2;
		
		for (int i = 0; i < reminders.length; i++) {
			if (i != swipeImage)
				paintImage(i, canvas);
		}
		if (swipeImage != -1)
			paintImage(swipeImage, canvas);
	}
	
	int imageFromPos(float x, float y) {
		int ix = (int)(x / iw);
		int iy = (int)(y / iw);
		int idx = iy * 2 + ix;
		if (idx >= reminders.length)
			return -1;
		return idx;
	}
	
	void endSwipe() {
		// Delete the image, etc.
		if (swipeX < getWidth() * 0.1) {
			reminders[swipeImage].Delete();
			for (int i = swipeImage; i < reminders.length - 1; i++) {
				reminders[i] = reminders[i+1];
			}
			reminders[reminders.length - 1] = null;
		}
	}
	
	public boolean onTouchEvent(final MotionEvent ev) {
		for (int i = 0; i < ev.getPointerCount(); i++) {
			final int action = ev.getActionMasked();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				int img = imageFromPos(ev.getX(), ev.getY());
				if (img != -1 && reminders[img] != null) {
					swipeImage = img;
					swipeStartX = ev.getX();
					swipeX = ev.getX();
					swiping = true;
				}
				break;
			case MotionEvent.ACTION_UP:
				endSwipe();
				
				swiping = false;
				swipeX = 0;
				swipeImage = -1;
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				swipeX = ev.getX();
				invalidate();
				break;
			default:
				break;
			
			}
		}
		return true;
	}
	
	@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int len = reminders == null ? 2 : reminders.length;
		setMeasuredDimension(
    		MeasureSpec.getSize(widthMeasureSpec),
    		MeasureSpec.getSize(widthMeasureSpec) * ((len + 1) / 2));

		/*
        int len = bitmaps == null ? 2 : bitmaps.length;
		setMeasuredDimension(
    		MeasureSpec.getSize(widthMeasureSpec),
    		MeasureSpec.getSize(heightMeasureSpec));
   */
    }
	
}
