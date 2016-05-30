package com.alexmochalov.rybl;

import java.io.Serializable;

import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import android.content.Context;
import android.graphics.*;
import android.os.Handler;
import android.util.*;

import com.alexmochalov.rybl.Var.*;
import com.mochalov.alex.rybl.R;

public class Brush extends ImageView{
	private int radius = 0;
	private Var.Mode mode;
	
	private PixelFloat pixel;
	private int rgb;
	private int brushThickness;
	
	private Bitmap icon;
	private Rect rectS;
	private Rect rectD;
	
	private Paint paint;
	private Bitmap mask;

	private Context context;
	
	private int transparency = 100;
	private boolean main = false;
	
	private int size = 1003; // 1000...1003;
	private int code;

	private int colors[][];

	public Brush(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	public Brush(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public Brush(Context context) {
		super(context);
		init(context);
	}

	public void setMode(Var.Mode mode, Bitmap icon)
	{
		setMode(mode);
		this.icon = icon;
        rectS = new Rect(0,0,icon.getWidth(),icon.getHeight());
		invalidate();
	}

	public Var.Mode getMode(){
		return mode;
	}
	
	public void setMode(Var.Mode m)
	{
		this.mode = m;
		if (this.mode != Mode.paint)
			pixel.clear();
		invalidate();
	} 

	public Pixel getPixel()
	{
		Pixel p = new Pixel((short)pixel.red, (short)pixel.yellow, (short)pixel.blue, (short)pixel.white);
		return p;
	}

	public int getColor()
	{
		return rgb;
	}

	private void init(Context context) {
		paint = new Paint();
		this.context = context;
		brushThickness = 255;
		icon = BitmapFactory.decodeResource(getResources(), R.drawable.void_tube);
        rectS = new Rect(0,0,icon.getWidth(),icon.getHeight());
	}
	
	public void setRadius(int radius, boolean main){
		this.radius = radius;
		this.rgb = Color.WHITE;
		
		this.pixel = new PixelFloat((short)255);
		
		mask = Bitmap.createBitmap(radius * 2, radius * 2, 
				Bitmap.Config.ARGB_8888); 
		mask.eraseColor(android.graphics.Color.TRANSPARENT);;
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		Canvas canvas = new Canvas(mask);
		canvas.drawCircle(radius, radius, radius, paint);
		
		setDensity(3);
		
		this.main = main;
	}

    @Override
    protected void onDraw(Canvas canvas) {
    	Var.drawBG(canvas);
		
    	if (radius != 0){
    		if (mode != Var.Mode.paint){
        		Path path = new Path();
        		path.addCircle(Var.brushWidth, Var.brushWidth , Var.brushRadius, Path.Direction.CW);
//    	    	if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
//    	    		canvas.clipPath(path);
//                canvas.drawBitmap(icon, rectS, rectD, paint);
    	    	if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
    	    		canvas.clipPath(path);
    				canvas.drawBitmap(icon, rectS, rectD, null);
    	    	} else {
    				canvas.drawBitmap(icon, rectS, rectD, null);
    	    		canvas.drawBitmap(Var.mask, Var.rectMask, rectD, null);
    	    	}	
        		
        		
    		} else {
    			paint.setColor(rgb);
				//Log.d("","RGB === "+rgb+" radius "+radius+" paint "+paint);
    			canvas.drawCircle(Var.brushWidth, Var.brushWidth, radius, paint);
    			
    			if (main)
    				Var.drawText(canvas, transparency, false);    			
    		}
		} else {
			paint.setColor(Color.BLUE);
			canvas.drawCircle(Var.brushWidth, Var.brushWidth, 22, paint);			
		}

    }

    public void draw(Canvas canvas) {
    	Var.drawBG(canvas);
		
    	if (radius != 0){
    		if (mode != Var.Mode.paint){
        		Path path = new Path();
        		path.addCircle(Var.brushWidth, Var.brushWidth, radius, Path.Direction.CW);
    	    	if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2){
    	    		canvas.clipPath(path);
    				canvas.drawBitmap(icon, rectS, rectD, null);
    	    	} else {
    				canvas.drawBitmap(icon, rectS, rectD, null);
    	    		canvas.drawBitmap(Var.mask, Var.rectMask, rectD, null);
    	    	}	
    		} else {
    			paint.setColor(rgb);
				//Log.d("","RGB === "+rgb+" radius "+radius+" paint "+paint);
    			canvas.drawCircle(Var.brushWidth, Var.brushWidth, radius, paint);
    			
    			if (main)
    				Var.drawText(canvas, transparency, false);    			
    		}
		} else {
			paint.setColor(Color.BLUE);
			canvas.drawCircle(Var.brushWidth, Var.brushWidth, 22, paint);			
		}

    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(Var.brushWidth*2, Var.brushWidth*2);
        rectD = new Rect(0,0,Var.brushWidth*2,Var.brushWidth*2);
    }
	
	public void addColor(Pixel pixel){
		mode = Var.Mode.paint;
		this.pixel.add1(pixel, false);
		rgb = Var.ryb2rgb(this.pixel);
		invalidate();
	}

	public void clear() {
		rgb = Color.WHITE;
		pixel.clear();
		//Log.d("","CLEAR "+rgb);
		invalidate();
	}

	public Bitmap getMask() {
		return mask;
	}

	public int getThickness() {
		if (brushThickness > 0)
			return brushThickness--;
		else return 0;
	}

	public void restoreThickness() {
		brushThickness = 255;
	}

	public void copy(Brush brush) {
		radius = brush.radius;
		pixel.copy(brush.pixel);
		rgb = brush.rgb;
		brushThickness = brush.brushThickness;
		setMode(Var.Mode.paint);
		//Math.ra
		//Log.d("", "Copy"+pixel.red+" "+pixel.yellow+"  "+pixel.blue+"  "+pixel.white+"  rgb "+rgb);
		
		invalidate();
	}

	public boolean equal(Brush brush) {
		return rgb == brush.rgb;
	}

	public void setPixel(PixelFloat readObject) {
		pixel = readObject;
		rgb = Var.ryb2rgb(this.pixel);
		invalidate();
	}

	public String pixelToString() {
		return ""+pixel.red+"\n"+pixel.yellow+"\n"+pixel.blue+"\n"+pixel.white+"\n";
	}

	public void setPixel(String str1, String str2, String str3, String str4) {
		pixel.red = Float.parseFloat(str1);
		pixel.yellow = Float.parseFloat(str2);
		pixel.blue = Float.parseFloat(str3);
		pixel.white = Float.parseFloat(str4);
		rgb = Var.ryb2rgb(this.pixel);
		
		invalidate();
	}

	public boolean isEmpty() {
		return pixel.red == 0 && pixel.yellow == 0 && pixel.blue == 0 && pixel.white == 0;
	}

	public void setTransparency(int t) {
		transparency = t;
		invalidate();
	}

	public int getTransparency() {
		return transparency;
	}

	public void setSize(int code, int width) {
		this.code = code;
		switch (code){
		case 1000:
			size = width/10;
			break;
		case 1001:
			size = width/5;
			break;
		case 1002:
			size = width/4;
			break;
		default:	
			size = width/3;
		};
		
		invalidate();
	}

	public int getSize() {
		return size;
	}

	public int getSize0() {
		return code;
	}

	public void setColor(float r, float y, float b, float w) {
		this.pixel = new PixelFloat(r, y, b, w);
		rgb = Var.ryb2rgb(this.pixel);
	}

	public int getColor(int i, int j, int radius) {
		return colors[128-radius+i][128-radius+j];
	}

	public void setDensity(int density) {
		if (density == 4){
			colors = new int[255][255];
			for (int i = 0; i < 255 ; i++)
				for (int j = 0; j < 255 ; j++)
					colors[i][j] = 1;
		} else {
			colors = new int[255][255];
			for (int i = 0; i < 255 ; i++)
				for (int j = 0; j < 255 ; j++)
					colors[i][j] = -1;
			
			for (int k = 0; k < density*10 ; k++)
				for (int l = 0; l < 255 ; l+= 20){
					int i = (int)(Math.random()*l);
					int j = (int)(Math.random()*l);
					
					int r = (int)(Math.random()*5+1);
					
					for (int i1 = 0; i1<=r; i1++)
						for (int j1 = 0; j1<=r; j1++)
							if (Math.sqrt(i1*i1+j1*j1) <= r)
								colors[i+128-l/2+i1-r/2][j+128-l/2+j1-r/2] = Color.BLUE;
					
					
					
				}
			
			
			
		}
	}
}
