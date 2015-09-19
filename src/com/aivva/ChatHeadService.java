package com.aivva;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognizerIntent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class ChatHeadService extends Service implements OnClickListener,
		OnTouchListener {

	int initialX;
	int initialY;
	float initialTouchX;
	float initialTouchY;
	WindowManager.LayoutParams params;
	private GestureDetector gestureDetector;

	private WindowManager windowManager;
	private ImageView chatHead;
	boolean goneFlag = false;
	static final int check = 111;

	@Override
	public IBinder onBind(Intent intent) {
		// Not used
		return null;
	}

	private void sendMessage() {
		  Intent intent = new Intent("my-event");
		  // add data
		  intent.putExtra("message", "data");
		  LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		chatHead = new ImageView(this);
		chatHead.setImageResource(R.drawable.custom2);
		chatHead.setOnTouchListener(this);
		gestureDetector = new GestureDetector(this, new SingleTapConfirm());
		gestureDetector.setIsLongpressEnabled(true);

		params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_PHONE,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP | Gravity.LEFT;
		params.x = 400;
		params.y = 100;

		windowManager.addView(chatHead, params);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		windowManager.removeView(chatHead);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Its being opened",
				Toast.LENGTH_SHORT).show();
		Intent i3 = new Intent(ChatHeadService.this, Voice.class);
		i3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i3);
	}

	final Handler handler = new Handler(); 
	Runnable mLongPressed = new Runnable() { 
	    public void run() { 
	    	goneFlag = true;
	    	stopSelf();
	    }   
	};

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {	
		case MotionEvent.ACTION_DOWN:
			handler.postDelayed(mLongPressed, 1000);
			initialX = params.x;
			initialY = params.y;
			initialTouchX = event.getRawX();
			initialTouchY = event.getRawY();
			break;
		case MotionEvent.ACTION_UP:
			handler.removeCallbacks(mLongPressed);
			if(Math.abs(event.getRawX() - initialTouchX) <= 2 && !goneFlag) {
				performClick();
				return false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(Math.abs(event.getRawX() - initialTouchX) <= 2 && !goneFlag) {

			}
			else{
				handler.removeCallbacks(mLongPressed);
			}
			params.x = initialX + (int) (event.getRawX() - initialTouchX);
			params.y = initialY + (int) (event.getRawY() - initialTouchY);
			windowManager.updateViewLayout(v, params);
			break;
		}
		return true;
	}

	private void performClick() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Its being opened...",
				Toast.LENGTH_SHORT).show();
		Intent i3 = new Intent(ChatHeadService.this, Voice.class);
		i3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(i3);
	}
	
	
	private class SingleTapConfirm extends SimpleOnGestureListener {

		@Override
		public boolean onSingleTapConfirmed(MotionEvent event) {
			return true;
		}
	}

}
