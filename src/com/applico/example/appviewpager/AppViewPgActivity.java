package com.applico.example.appviewpager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * This activity creates the ViewPager and is free to do other interesting stuff with the rest of the screen
 * Simpler implementation, no buttons, AppViewPager object is not embedded
 */
public class AppViewPgActivity extends Activity {
	public static final String TAG = "AppViewPgActivity";
	//this is from the AppViewPager to handle swiping
	private GestureDetector mDetector;
		
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_pg_activity);
        final AppViewPager pgr = new AppViewPager(R.id.container, this);
        mDetector = pgr.getGestureDetector();
    	Log.i(TAG, "committed first fragment");
    }
    
    @Override 
    public boolean onTouchEvent(MotionEvent event){ 
		//first look for a swipe, then process the touch as usual
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    
    @Override
    public void onBackPressed() {
      finish();
    }
  
}

    