package com.applico.example.appviewpager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * This activity demonstrates an alternative to the ViewPager in the support library
 * It creates a cache of Fragments in the FragmentManager and animates transitions with object animators
 * Note that the FragmentManager is lost when the activity leaves the screen
 * an alternate implementation might create a different, sustained cache of the fragments 
 * ViewPager requires the support library, hides fragment transactions and calls GC - this style is FAST
 */
public class SliderExample extends Activity {
	public static final String TAG = "SliderExample";
	
	//scratch variables to re use through out the activity
	FragmentManager mMgr;
	//we can reuse the same block of space in the heap instead of allocating and tossing out transactions
	FragmentTransaction mXaction;
	//replace this class with your fragment
	FragmentExample mCF;
	//the fragment container that is animated
	View mFragmentView;
	//the simple gesture detector that implements fling/swipe interaction
	GestureDetector mDetector;
	//*challenge to implement a halfway-slide animation, partial display of a fragment with in/out transition
	
	//the currently visible fragment's index in the mBackground fragment id array 
	int mSelected = 0;	
	
	//The following are static so values are retained and ready each time this screen is called
    static int mDuration, mWidth = -1;
	//animation duration and the translation distance
    
    //animations for the sliding part of this panel
    ObjectAnimator moveLeft, moveLeft2, moveRight, moveRight2;
    
    //This is the id key for each Fragment in the array, keep it flat and simple for speed
	int[] mBackgrounds = { R.drawable.blue_background, R.drawable.green_background, R.drawable.purple_background };
		
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        mFragmentView = findViewById(R.id.container);
        mMgr = getFragmentManager();
        //Swipe listener
        mDetector = new GestureDetector(this, new GD());
    	mXaction = mMgr.beginTransaction();
    	mCF = new FragmentExample(mBackgrounds[0], 0);

		mXaction.add(R.id.container, mCF, mSelected +"");
		//put new fragments on the backstack so the Fragment manager will cache it
		mXaction.addToBackStack(null);
    	mXaction.commit();
    	Log.i(TAG, "committed first fragment");
    }
    
    
    @Override 
    public boolean onTouchEvent(MotionEvent event){ 
    	//first look for a swipe, then process the touch as usual
        mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }


    /**
     * This method will instantiate the fragment transition animation
     * It sets a listener that will create and add fragment that not been seen before OR
     * find and show a fragment the FragmentManager has already added
     */
    void leftArrow() {
    	moveLeft = ObjectAnimator.ofFloat(mFragmentView, "translationX", 0f, -mWidth);
		moveLeft.setDuration(mDuration);
		//instantiate the second animation here, instead of everytime the listener runs
		moveLeft2 = ObjectAnimator.ofFloat(mFragmentView, "translationX", mWidth, 0);
		moveLeft2.setDuration(mDuration);
		
    	moveLeft.addListener(new AnimatorListenerAdapter() {
    		@Override
    	    public void onAnimationEnd(Animator animation) {
    			//after animating current fragment offscreen, hide it
    			setFragment();
    	    	//now animate the new fragment on to the screen
    	    	moveLeft2.start();
    	    	
    		}
    		
    	});    			
    }
    
    /**
     * same as above, except for the other side
     * TRIED mXaction.setCustomAnimations(R.animator.out_left, R.animator.in_left);
     * as well as mXaction.setCustomAnimations(enter, exit, popEnter, popExit);
     * the fragment transaction custom animations were not working as expected
     * enter animation showed only with add, not on show
     * better results with manual animations set on the view
     */
    void rightArrow() {    	
    	moveRight = ObjectAnimator.ofFloat(mFragmentView, "translationX", 0, 2*mWidth);
		moveRight.setDuration(mDuration);
		moveRight2 = ObjectAnimator.ofFloat(mFragmentView, "translationX", -mWidth, 0);
		moveRight2.setDuration(mDuration);
		
    	moveRight.addListener(new AnimatorListenerAdapter() {
    		@Override
    	    public void onAnimationEnd(Animator animation) {
    			setFragment();
    	    	moveRight2.start();
    		}
    		
    	});
    	
    }
    
    /**
     * code that is common to both animation listeners, hide the existing and then add or show
     */
    void setFragment() {
    	mXaction = mMgr.beginTransaction(); 
		mXaction.hide(mCF);
    	mXaction.commit();
    	        	    	
    	mXaction = mMgr.beginTransaction();
    	
    	mCF = (FragmentExample) mMgr.findFragmentByTag(mSelected+"");
    	if(mCF == null) {
    		Log.i(TAG, "current fragment not found?!?");
    		mCF = new FragmentExample(mBackgrounds[mSelected], mSelected);
    		//always add to backstack so that the Fragment Manager keeps state
    		mXaction.add(R.id.container, mCF, mSelected +"");
    		mXaction.addToBackStack(null);
    	} else {
    		Log.d(TAG, "found fragment to show");
    		mXaction.show(mCF);
    		//you can update the fragment here, if needed, using mCF.getView().findViewById
    	}
    	
    	mXaction.commit();
    }

    /**
     * This click listener is set in the xml
     * It navigates through the fragments, through the mBackgrounds array
     * Navigation is circular, go through left and right swiping or using a button
     * Keep the mFragment array lightweight for best performance
     * 
     * @param arrow, the pressed button
     */
	public void arrowButton(View arrow) {
		//width and animation duration are set the first time the user navigates to another fragment
		//the variables are static, and only need to be defined once
    	if(mWidth < 0) {
    		//this is static and needs to be set only once
    		mWidth = mFragmentView.getWidth();
    		mDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);
    	}
    	//selected is going to be the animation, left or right
    	final ObjectAnimator selected;
    	if(arrow.getId() == R.id.catBtnRight) {
    		mSelected++;
    		if(mSelected == mBackgrounds.length) {
    			mSelected = 0;
    		}
    		//instantiate if this is our first call
    		if(moveRight == null) {
    			rightArrow();
    		}
    		selected = moveRight;

    	} else {
    		mSelected--;
    		if(mSelected < 0) {
    			mSelected = mBackgrounds.length-1;
    		} 
    		//instantiate the animation if this is our first call
    		if(moveLeft == null) {
    			leftArrow();
    		}
    		selected = moveLeft;
    		
    	} //mSelected is now reset and in range
    	Log.d(TAG, "new mSelected " + mSelected + " : " + mBackgrounds[mSelected] + " current fragment id= " + mBackgrounds[mSelected]);
    	
		selected.start();

    }

    
    /******************************/
    class GD extends GestureDetector.SimpleOnGestureListener {
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    //Challenge? implement onScroll for partial display of the incoming/outgoing fragments
    	
    	@Override
        public boolean onDown(MotionEvent event) { 
    		// This is not used, returning true so that we get the rest of the gesture
    		// This gesture detector return won't stop the touchEvent, it just goes first
    		return true;
        }
    	
		@Override
		public boolean onFling (MotionEvent start, MotionEvent finish, float velocityX, float velocityY) {
    		super.onFling(start, finish, velocityX, velocityY);
    		if(Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY) {
    			return false;
    		}
    		if (start.getRawX() < finish.getRawX()) {
    			//swipe is going from left to right 
    			arrowButton(findViewById(R.id.catBtnRight));
    		} else {
    			//swipe is from right to left
    			arrowButton(findViewById(R.id.catBtnLeft));
    		}
    		return true;
    	}
    	
    }
    
  
}

    