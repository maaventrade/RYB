package com.alexmochalov.rybl;

import java.util.ArrayList;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ViewSubmenu1 extends RelativeLayout{
	Context context;
	
	int shiftY = 0;
	float y0 = 0;
	
	public ViewSubmenu1(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		this.context = context;
	}

	public ViewSubmenu1(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public ViewSubmenu1(Context context) {
		super(context);
		this.context = context;
	}

	
	boolean mDownTouch = false;
	
	@Override 
	public boolean onTouchEvent(MotionEvent event) {
		float y = event.getY(); 
		
		switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mDownTouch = true;
            y0 = y;
            return true;

        case MotionEvent.ACTION_MOVE:
        	shiftY = shiftY + (int)(y - y0);
        	
    		View view = getChildAt(0);
    		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)view
    		        .getLayoutParams();
    		layoutParams.topMargin = shiftY;
    		Log.d("", "layoutParams.topMargin "+layoutParams.topMargin);
    		view.setLayoutParams(layoutParams);
        	
            y0 = y;
            return true;
		}
		return false;
	}

	@Override
	 public boolean performClick() {
	  // Calls the super implementation, which generates an AccessibilityEvent
	        // and calls the onClick() listener on the view, if any
	        super.performClick();
	        // Handle the action for the custom click here
	        return true;
	 }
	
	@Override 
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;    
    }

	
	
}
