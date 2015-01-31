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
    }

    //Moving app pager code here to avoid state loss exceptions when using the backstack
    @Override
    protected void onPostResume() {
        super.onPostResume();
        // This would normally be in Resume, here to avoid illegal state fragment transaction errors
    	//http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
        final AppViewPager pgr = new AppViewPager(R.id.container, this);
        mDetector = pgr.getGestureDetector();
        Log.i(TAG, "created view pager");
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

    