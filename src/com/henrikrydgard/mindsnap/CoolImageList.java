package com.henrikrydgard.mindsnap;

import java.util.Timer;
import java.util.TimerTask;

import com.henrikrydgard.mindsnap.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
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
	Bitmap dragIcon;
	Bitmap emptyImage;
	Paint paint;
	Paint emptyPaint;
	boolean swipingX;
	boolean swipingY;
	
	private static final int MODE_TILES = 0;
	private static final int MODE_ZOOM = 1;
	
	int mode = MODE_TILES;
	
	int swipeImage;
	int iw;
	
	boolean erroring;
	int errorImage;
	int zoomImage;
	
	float swipeStartX;
	float swipeX;
	float swipeStartY;
	float swipeY;
	float swipeXDelta;
	float swipeYDelta;
	
	static final float cmdBarSize = 60.0f;
	Paint textPaint;
	
	static final float deleteThreshold = 0.15f;
	OnClickListener listener;
	public Reminder clickedReminder;

	boolean clickedAddReminder;
	boolean clickedShare;
	boolean blackened;
	
	Timer dragTimer;
	boolean dragging = false;
	int dragImage;

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
		textPaint = new Paint();
		emptyPaint.setColor(0xFF404040);
		swipeImage = -1;
		errorImage = -1;
		dragImage = -1;
		mode = MODE_TILES;
		// TODO Auto-generated constructor stub
		textPaint.setColor(0xFFFFFFFF);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setTextSize(cmdBarSize/2);
		Typeface tf = Typeface.DEFAULT_BOLD;
		textPaint.setTypeface(tf);
		dragIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.drag_icon);
		Log.i(TAG, "CoolImageList");
	}
	
	
	public void setOnClickListener(OnClickListener listener) {
		this.listener = listener;
	}
	
	public void updateBitmaps() {
	}
	
	
	
	
	void paintImage(int i, Canvas canvas) {
		if (i >= reminders.length)
			return;
		
		if (emptyImage == null)
		{
			emptyImage = Bitmap.createBitmap(iw, iw, Config.ARGB_8888);
			emptyImage.eraseColor(0xFF202020);
		}
		
		int draw_w = iw;
		int draw_h = iw;
		
		if (mode == MODE_ZOOM) {
			if (zoomImage != i)
				return;
			draw_w = getWidth();
			draw_h = getHeight();
		}
		
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
		} else if (dragging && dragImage == i) {
			x += swipeX - swipeStartX;
			y += swipeY - swipeStartY;
			int dragZoom = 40;
			x -= dragZoom;
			y -= dragZoom;
			draw_w += dragZoom*2;
			draw_h += dragZoom*2;
		}
		
		if (swipingY) {
			fade = 1.0f - Math.abs(swipeYDelta) / cmdBarSize * 0.6f;
			if (fade < 0.4f) fade = 0.4f;
		}
		
		int col = 0xFFFFFF;
		col |= (int)(fade * 255) << 24;
		paint.setColor(col);
	
		if (errorImage == i) {
			paint.setColor(0xe0FF3030);
		}
		
		Bitmap bitmap = null;
		if (reminders[i] != null)
			bitmap = reminders[i].bitmap;
		
		if (bitmap == null) {
			bitmap = emptyImage;
		}
		int w = bitmap.getWidth();
		int h = bitmap.getHeight();
		
		Rect srcRc = null;
		if (w > h) {
			srcRc = new Rect((w - h) / 2, 0, (w - h) / 2 + h, h);
		} else {
			srcRc = new Rect(0, (h - w) / 2, w, (h - w) / 2 + w);
		}
		
		RectF rc = new RectF(x + 2, y + 2, x + draw_w - 2, y + draw_h - 2);
		
		
		canvas.drawBitmap(bitmap, srcRc, rc, paint);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (blackened)
			return;
		
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
		
		canvas.drawBitmap(dragIcon, getWidth()/2-dragIcon.getWidth()/2, (int)(bottomY - dragIcon.getHeight()*1.5f), textPaint);
		
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
				clickedAddReminder = true;
				listener.onClick(this);
			} else if (swipeYDelta == cmdBarSize) {
				clickedShare = true;
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
				clickedAddReminder = false;
				clickedShare = false;
				dragging = false;
				int img = imageFromPos(ev.getX(), ev.getY());
				if (img != -1 && reminders[img] != null) {
					swipeImage = img;
					dragImage = img;
				}
				swipeStartX = ev.getX();
				swipeX = ev.getX();
				swipeStartY = ev.getY();
				swipeY = ev.getY();
				dragTimer = new Timer();
				dragTimer.schedule(new TimerTask() {
					
					@Override
					public void run() {
						if (dragImage != -1) {
							swipingY = false;
							swipingX = false;
							dragging = true;
							
							CoolImageList.this.post(new Runnable() {
								@Override
								public void run() {
									invalidate();
								}
							});
						}
					}
				}, 400);
				break;
				
			case MotionEvent.ACTION_UP:
				if (!endSwipe()) {
					if (swipeImage != -1) {
						if (dragging) {
							// Swap
							int target = imageFromPos(ev.getX(), ev.getY());
							if (target != -1 && reminders[target] != null) {
								Reminder temp = reminders[target];
								reminders[target] = reminders[dragImage];
								reminders[dragImage] = temp;
							}
						}
						else {
							if (Math.abs(swipeX - swipeStartX) < 15 && !swipingX && !swipingY)
								onImageClick();
						}
					}
				}
				swipingX = false;
				swipingY = false;
				dragging = false;
				swipeX = 0;
				swipeImage = -1;
				errorImage = -1;
				dragImage = -1;
				swipeXDelta = 0;
				swipeYDelta = 0;
				
				invalidate();
				if (dragTimer != null) {
					dragTimer.cancel();
					dragTimer = null;
				}
				break;
				
			case MotionEvent.ACTION_MOVE:
				swipeX = ev.getX();
				swipeY = ev.getY();
				float yd = Math.abs(swipeY - swipeStartY);
				float xd = Math.abs(swipeX - swipeStartX);
				if (!swipingX && !swipingY && !dragging) {
					if (yd > 12) {
						swipingY = true;
						if (dragTimer != null) {
							dragTimer.cancel();
							dragTimer = null;
						}
					}
					else if (xd > 7) {
						int imgg = imageFromPos(ev.getX(), ev.getY());
						if (reminders[imgg] == null)
							errorImage = imgg;
						swipingX = true;
						if (dragTimer != null) {
							dragTimer.cancel();
							dragTimer = null;
						}
					}
				}
				if (swipingX) {
					swipeXDelta = swipeX - swipeStartX;
				}
				if (swipingY) {
					swipeYDelta = (swipeY - swipeStartY)*0.5f;
					if (swipeYDelta > cmdBarSize)
						swipeYDelta = cmdBarSize;
					if (swipeYDelta < -cmdBarSize)
						swipeYDelta = -cmdBarSize;
					
					// Block swiping downwards for now
					if (swipeYDelta > 0)
						swipeYDelta = 0;
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
        setMeasuredDimension(
    		MeasureSpec.getSize(widthMeasureSpec),
    		MeasureSpec.getSize(heightMeasureSpec));
    }
	
}
