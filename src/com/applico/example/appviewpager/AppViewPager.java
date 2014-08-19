package com.applico.example.appviewpager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * This object is an alternative to the ViewPager in the support library
 * It creates a cache of Fragments in the FragmentManager and animates transitions with object animators
 * Note that the FragmentManager is lost when the activity leaves the screen
 * ViewPager requires the support library, hides fragment transactions and calls GC - this design is FAST
 */
public class AppViewPager {
	public static final String TAG = "AppViewPager";
	
	//scratch variables built with the activity
	FragmentManager mMgr;
	//we can reuse the same block of space in the heap instead of allocating and tossing out transactions
	FragmentTransaction mXaction;
	//the active fragment (replace this class with your fragment definition)
	FragmentExample mCF;
	//the fragment container that is animated with fragment transactions
	View mFragmentView;
	//a simple gesture detector that implements fling/swipe interaction
	GestureDetector mDetector;
	//*challenge to implement a halfway-slide animation, partial display of a fragment with in/out transition
	
	//the currently visible fragment's index in the mBackground fragment id array 
	int mSelected = 0;	
	
	//The following are static so values are retained and ready each time this screen is called
    static int mDuration, mWidth = -1;
	//animation duration and the translation distance
    
    //animations for the sliding part of this panel
    static ObjectAnimator moveLeft, moveLeft2, moveRight, moveRight2;
    
    //This is the id key passed into the constructor for each Fragment in the pager, keep it flat and simple for best performance
    //try setting tags on views in the Fragment to hold the fragment's data 
	int[] mBackgrounds = { R.drawable.blue_background, R.drawable.green_background, R.drawable.purple_background };
	
	public AppViewPager(int containerId, Activity a){
    	//activities cannot retain the fragment manager...
        mFragmentView = a.findViewById(containerId);
        mMgr = a.getFragmentManager();
        //Swipe listener
        mDetector = new GestureDetector(a, new GD());
    	mXaction = mMgr.beginTransaction();
    	mCF = new FragmentExample(mBackgrounds[0], 0);
		mXaction.add(containerId, mCF, mSelected +"");
		//put new fragments on the backstack so the Fragment manager will cache it
		mXaction.addToBackStack(null);
    	mXaction.commit();
    	Log.i(TAG, "committed first fragment");
    	mDuration = a.getResources().getInteger(android.R.integer.config_shortAnimTime);
    }
	
	public GestureDetector getGestureDetector() {
		//pass the gestureDetector back to the activity, touch interaction works better that way
		return mDetector;
	}

    /**
     * This method will instantiate the fragment transition animation
     * It sets a listener that will create and add fragment that is new OR
     * find and show a fragment the FragmentManager already has
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
     * the fragment transaction custom animations were not working in a circular fashion
     * enter animation showed only with add, not on show
     * got the circular animation transitions I wanted with manual animations set on the view
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
     * This is similar to the click listener in the SliderExample
     * It navigates through the fragments, through the mBackgrounds array
     * Navigation is circular, go through left and right swiping or using a button
     * Keep the mFragment array lightweight for best performance
     * 
     * @param arrow, the pressed button
     */
	public void slide(boolean right) {
		//width and animation duration are set in the object, they can be abstracted to become parameters 
		//the variable is static, and only need to be defined once
		//remove the static declaration if the viewpager is set in different activities, different sizes
    	if(mWidth < 0) {
    		//this is static and needs to be set only once
    		mWidth = mFragmentView.getWidth();
    	}
    	//selected is going to be the animation, left or right
    	final ObjectAnimator selected;
    	if(right) {

    		mSelected--;
    		if(mSelected < 0) {
    			mSelected = mBackgrounds.length-1;
    		} 
    		//instantiate if this is our first call
    		if(moveRight == null) {
    			rightArrow();
    		}
    		selected = moveRight;

    	} else {
    		mSelected++;
    		if(mSelected == mBackgrounds.length) {
    			mSelected = 0;
    		}
    		//instantiate the animation if this is our first call
    		if(moveLeft == null) {
    			leftArrow();
    		}
    		selected = moveLeft;
    		
    	} //mSelected is now reset and in range
    	Log.d(TAG, "new mSelected " + mSelected + " : " + 
    			mBackgrounds[mSelected] + " current fragment id= " + mBackgrounds[mSelected]);
    	
		selected.start();
		//setup, so kick off the animation
    }

    
    /******************************/
    class GD extends GestureDetector.SimpleOnGestureListener {
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    //Challenge? implement onScroll for partial display of the incoming/outgoing fragments
    	
    	@Override
        public boolean onDown(MotionEvent event) { 
    		// This is not used, returning true so that we get the rest of the gesture
    		// This gesture detector return won't stop the touchEvent, it just goes first
    		Log.d(TAG,"on down");
    		return false;
        }
    	
		@Override
		public boolean onFling (MotionEvent start, MotionEvent finish, float velocityX, float velocityY) {
			Log.d(TAG,"on fling");
    		super.onFling(start, finish, velocityX, velocityY);
    		if(Math.abs(velocityX) < SWIPE_THRESHOLD_VELOCITY) {
    			return false;
    		}
    		if (start.getRawX() < finish.getRawX()) {
    			//swipe is going from left to right 
    			slide(true);
    		} else {
    			//swipe is from right to left
    			slide(false);
    		}
    		return true;
    	}
    	
    }
    
  
}

    