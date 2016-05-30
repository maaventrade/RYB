package com.alexmochalov.rybl;

import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class Tube extends ImageView{

	private Pixel pixel;
	private Paint paint;
	private Brush brush;
	
	public Tube(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public Tube(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public Tube(Context context) {
		super(context);
		init();
	}

	@Override
	public void buildDrawingCache(boolean autoScale)
	{
		// TODO: Implement this method
		super.buildDrawingCache(autoScale);
	}

	@Override
	public void buildDrawingCache()
	{
		// TODO: Implement this method
		super.buildDrawingCache();
	}

	public void setColor(short r, short y, short b, short w)
	{
		pixel = new Pixel(r, y, b, w); 
	}


	public void setBrush(Brush brush)
	{
		this.brush = brush;
	}

	private void init() {
		paint = new Paint();
	}
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d("","Var.brushWidth  "+Var.brushWidth);
        setMeasuredDimension(Var.brushWidth*2, Var.brushWidth*2);
    }

	public void setColor(short[] rgb2ryb) {
    	this.pixel = new Pixel((short)255, rgb2ryb);
    	invalidate();
	}
    

    @Override
    protected void onDraw(Canvas canvas) {
    	Var.drawBG(canvas);
    
        paint.setColor(Var.ryb2rgb(pixel));
        canvas.translate(Var.brushWidth, Var.brushWidth);
        canvas.drawCircle(0, 0, Var.brushRadius, paint);

    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - Var.brushWidth;
        float y = event.getY() - Var.brushWidth;

        switch (event.getAction()) {
        	case MotionEvent.ACTION_DOWN:
        		brush.addColor(pixel);
        		return true;
        	case MotionEvent.ACTION_MOVE:
        		brush.addColor(pixel);
        		return true;
            default:
            	return true;
        }    	
    }
} 
