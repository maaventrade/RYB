package com.alexmochalov.rybl;

import android.content.*;
import android.graphics.*;
import android.graphics.Paint.*;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.alexmochalov.rybl.Var.*;

public class Tool extends ImageView{
	
	private Var.Mode mode;
	private Paint paint;
	private Rect rectS;
	private Rect rectD;
	private Bitmap icon;

	private int code = 0;
	
	//MyCallback callback = null;

	public Var.Mode getMode()
	{
		return mode;
	}
	
	public Bitmap getIcon()
	{
		return icon;
	}
	
	public int getCode()
	{
		return code;
	}
	
	//interface MyCallback {
	//	void callbackACTION_DOWN(Tool tool); 
	//} 
	
	public Tool(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public Tool(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Tool(Context context) {
		super(context);
		init();
	}

	public void setMode(Var.Mode m, Bitmap icon)
	{
		this.mode = m;
		this.icon = icon;
        rectS = new Rect(0,0,icon.getWidth(),icon.getHeight());
	}

	
	public void setMode(int i, Bitmap icon) {
		code = i;
		if (icon != null){
			this.icon = icon;
	        rectS = new Rect(0,0,icon.getWidth(),icon.getHeight());
		}
	}
	
	private void init() {
		paint = new Paint();
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Var.brushWidth*2, Var.brushWidth*2);
        rectD = new Rect(0,0,Var.brushWidth*2,Var.brushWidth*2);
    }
	
    @Override
    protected void onDraw(Canvas canvas) {
    	Var.drawBG(canvas, Color.GRAY);
    		
		if (icon != null){
			Path path = new Path();
	    	path.addCircle(Var.brushWidth, Var.brushWidth, Var.brushRadius, Path.Direction.CW);
	    	if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
	    		canvas.clipPath(path);
				canvas.drawBitmap(icon, rectS, rectD, null);
	    	} else {
				canvas.drawBitmap(icon, rectS, rectD, null);
	    		canvas.drawBitmap(Var.mask, Var.rectMask, rectD, null);
	    	}	
			/*
			if (mode == Var.Mode.none){
				paint.setColor(Color.BLACK);
				paint.setStyle(Style.STROKE);
				paint.setStrokeWidth(4);
		    	canvas.drawCircle(Var.brushWidth, Var.brushWidth, Var.brushRadius, paint);			
			}
			*/
		}
		else {
			
			Var.drawText(canvas, code, true);
		}
        
    }

	public void setIcon(Bitmap icon) {
		this.icon = icon;
		invalidate();
	}

	public void setCode(int code) {
		this.code = code;
		invalidate();
	}

} 
