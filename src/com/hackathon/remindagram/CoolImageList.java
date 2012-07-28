package com.hackathon.remindagram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.style.TypefaceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class CoolImageList extends View {
	public static final String TAG = "CoolImageList";
	public Reminder [] reminders;
	public static Bitmap [] defaultBitmap = new Bitmap[6];
	Paint paint;
	Paint emptyPaint;
	boolean swipingX;
	boolean swipingY;
	
	int swipeImage;
	int iw;
	
	float swipeStartX;
	float swipeX;
	float swipeStartY;
	float swipeY;
	float swipeXDelta;
	float swipeYDelta;
	
	float cmdBarSize = 60.0f;
	Paint textPaint = new Paint();
	
	static final float deleteThreshold = 0.15f;
	OnClickListener listener;
	public Reminder clickedReminder;

	boolean clickAddReminder;
	boolean clickShare;

	Context context;
	
	public CoolImageList(Context context, AttributeSet attr) {
		super(context, attr);
		init(context);
	}
	public CoolImageList(Context context) {
		super(context);
		init(context);
	}
	public CoolImageList(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
		init(context);
	}

	public void init(Context context) {
		this.context = context;
		paint = new Paint();
		emptyPaint = new Paint();
		emptyPaint.setColor(0xFF404040);
		swipeImage = -1;
		// TODO Auto-generated constructor stub
		textPaint.setColor(0xFFFFFFFF);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextSize(cmdBarSize/2);
		Typeface tf = Typeface.DEFAULT_BOLD;
		textPaint.setTypeface(tf);
	}
	
	
	public void setOnClickListener(OnClickListener listener) {
		this.listener = listener;
	}
	
	public void updateBitmaps() {
	}
	
	
	Bitmap getDefaultBitmap(int i) {
		if (defaultBitmap[i] == null) {
			switch (i) {
			case 0:	defaultBitmap[0] = BitmapFactory.decodeResource(context.getResources(), R.drawable.default1); break;
			case 1:	defaultBitmap[1] = BitmapFactory.decodeResource(context.getResources(), R.drawable.default2); break;
			case 2:	defaultBitmap[2] = BitmapFactory.decodeResource(context.getResources(), R.drawable.default3); break;
			case 3:	defaultBitmap[3] = BitmapFactory.decodeResource(context.getResources(), R.drawable.default4); break;
			case 4:	defaultBitmap[4] = BitmapFactory.decodeResource(context.getResources(), R.drawable.default5); break;
			case 5: defaultBitmap[5] = BitmapFactory.decodeResource(context.getResources(), R.drawable.default6); break;
			}
		}
		return defaultBitmap[i];
	}
	
	
	void paintImage(int i, Canvas canvas) {
		if (i >= reminders.length)
			return;
		
		int x = ((i & 1) == 1) ? iw : 0;
		int y = (int)((i / 2) * iw + swipeYDelta);
		
		paint.setColor(0xFFFFFFFF);
		
		float fade = 1.0f;
		
		if (swipeImage == i && swipingX) {
			x += swipeX - swipeStartX;
			if (swipeX < getWidth() * deleteThreshold) {
				fade = swipeX / (getWidth() * deleteThreshold);
			}
			else if (swipeX > getWidth() * (1.0f - deleteThreshold)) {
				fade = 1.0f - ((swipeX - getWidth() * (1.0f - deleteThreshold)) / (getWidth() * deleteThreshold));
				if (fade < 0) fade = 0;
			}
		}
		
		if (swipingY) {
			fade = 1.0f - Math.abs(swipeY - swipeStartY) / cmdBarSize;
			if (fade < 0.4f) fade = 0.4f;
		}
		
		int col = 0xFFFFFF;
		col |= (int)(fade * 255) << 24;
		paint.setColor(col);
	
		Bitmap bitmap = null;
		if (reminders[i] != null && reminders[i].bitmap != null) {
			bitmap = reminders[i].bitmap;
		} else {
			bitmap = getDefaultBitmap(i);
		}
		
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		
		Rect srcRc = null;
		if (w > h) {
			srcRc = new Rect((w - h) / 2, 0, (w - h) / 2 + h, h);
		} else {
			srcRc = new Rect(0, (h - w) / 2, w, (h - w) / 2 + w);
		}
		
		RectF rc = new RectF(x + 2, y + 2, x + iw - 2, y + iw - 2);
		canvas.drawBitmap(bitmap, srcRc, rc, paint);
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
		
		float fade = 1.0f;
		if (swipingY) {
			fade = Math.abs(swipeY - swipeStartY) / cmdBarSize;
			if (fade > 1.0f) fade = 1.0f;
		}
		fade = 1.0f;

		int col = 0xFFFFFF;
		col |= (int)(fade * 255) << 24;
		textPaint.setColor(col);
	
		int topY = (int)swipeYDelta;
		canvas.drawText("Share", getWidth()/2, topY - cmdBarSize/2, textPaint);
		
		int bottomY = getHeight();
		bottomY += (int)swipeYDelta;
		
		canvas.drawText("Add Snap!", getWidth()/2, bottomY+cmdBarSize/2, textPaint);
	}
	
	int imageFromPos(float x, float y) {
		int ix = (int)(x / iw);
		int iy = (int)(y / iw);
		int idx = iy * 2 + ix;
		if (idx >= reminders.length)
			return -1;
		return idx;
	}
	
	// Returns true if a swipe action was detected and acted upon.
	boolean endSwipe() {
		if (swipingX) {
			if (swipeImage == -1)
				return false;
			// Delete the image, etc.
			if (swipeX < getWidth() * deleteThreshold || swipeX > getWidth() * (1.0f - deleteThreshold)) {
				reminders[swipeImage].Delete();
				for (int i = swipeImage; i < reminders.length - 1; i++) {
					reminders[i] = reminders[i+1];
				}
				reminders[reminders.length - 1] = null;
				return true;
			}
		}
		else if (swipingY) {
			if (swipeYDelta == -cmdBarSize) {
				clickAddReminder = true;
				listener.onClick(this);
			} else if (swipeYDelta == cmdBarSize) {
				clickShare = true;
				listener.onClick(this);
			}
		}
		return false;
	}
	
	void onImageClick() {
		clickedReminder = reminders[swipeImage];
		if (listener != null)
			listener.onClick(this);
	}
	
	public boolean onTouchEvent(final MotionEvent ev) {
		for (int i = 0; i < ev.getPointerCount(); i++) {
			final int action = ev.getActionMasked();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				clickAddReminder = false;
				clickShare = false;
				int img = imageFromPos(ev.getX(), ev.getY());
				if (img != -1 && reminders[img] != null) {
					swipeImage = img;
				}
				swipeStartX = ev.getX();
				swipeX = ev.getX();
				swipeStartY = ev.getY();
				swipeY = ev.getY();
				break;
				
			case MotionEvent.ACTION_UP:
				if (!endSwipe()) {
					Log.i(TAG, "Hello");
					if (swipeImage != -1) {
						Log.i(TAG, "Hello2");
							
						if (Math.abs(swipeX - swipeStartX) < 15)
							onImageClick();
					}
				}
				swipingX = false;
				swipingY = false;
				swipeX = 0;
				swipeImage = -1;
				swipeXDelta = 0;
				swipeYDelta = 0;
				
				invalidate();
				break;
				
			case MotionEvent.ACTION_MOVE:
				swipeX = ev.getX();
				swipeY = ev.getY();
				float yd = Math.abs(swipeY - swipeStartY);
				float xd = Math.abs(swipeX - swipeStartX);
				if (!swipingX && !swipingY) {
					if (yd > 5)
						swipingY = true;
					else if (xd > 5)
						swipingX = true;
				}
				if (swipingX) {
					swipeXDelta = swipeX - swipeStartX;
				}
				if (swipingY) {
					swipeYDelta = swipeY - swipeStartY;
					if (swipeYDelta > cmdBarSize)
						swipeYDelta = cmdBarSize;
					if (swipeYDelta < -cmdBarSize)
						swipeYDelta = -cmdBarSize;
				}
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
        //int len = reminders == null ? 2 : reminders.length;
		//setMeasuredDimension(
    //		MeasureSpec.getSize(widthMeasureSpec),
    	//	MeasureSpec.getSize(widthMeasureSpec) * ((len + 1) / 2));

		
      //  int len = bitmaps == null ? 2 : bitmaps.length;
		setMeasuredDimension(
    		MeasureSpec.getSize(widthMeasureSpec),
    		MeasureSpec.getSize(heightMeasureSpec));
    }
	
}
