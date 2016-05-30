package com.alexmochalov.rybl;

import android.content.SharedPreferences.Editor;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.view.MotionEvent.PointerCoords;
import android.widget.Toast;
import android.net.Uri;
import android.os.Environment;
import android.preference.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class ViewCanvas extends View
{
	private Bitmap bgBitmap;
	private Bitmap mBitmap;
	private Rect rectBG;
	
	int width;
	int height;
	
	private int offsetX = 0;
	private int offsetY = 0;
	private double kZooming = 1;
	
	private byte r[][];
	private byte y[][];
	private byte b[][];
	private byte w[][];
	private byte t[][];
	private boolean modified[][];
	
	private Canvas mCanvas;
	private Brush brush;
	private Context context;
	
	private int idBG;
	
	ArrayList<Item> items = new ArrayList<Item>();
	
	private Rect mRect = new Rect();
	private Rect mRectBitmap = new Rect();
	private final Paint mPaint;
	
	private int mCurX;
	private int mCurY;
	private float mCurPressure;
	private float mCurSize;
	private int mCurWidth;
	
	MyCallback callback = null;
	
	private SharedPreferences prefs;
	private static final String PREFS_OFFSETX = "PREFS_OFFSETX";
	private static final String PREFS_OFFSETY = "PREFS_OFFSETY";
	private static final String PREFS_K = "PREFS_K";
    private static final String PREFS_BRUSH_TRANSP = "PREFS_BRUSH_TRANSP";
    private static final String PREFS_BRUSH_SIZE = "PREFS_BRUSH_SIZE";

	public void newImage(int w, int h, String name)
	{
		width = w;
		height = h;
		load(name, true);
	}

	
	public int getIdBG()
	{
		// TODO: Implement this method
		return idBG;
	}

	public void refresh()
	{
		mCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		for (int x = 0; x < width; x++)
			for (int z = 0; z < height; z++)
				mBitmap.setPixel(x, z, Var.ryb2rgb(r[x][z], 
													 y[x][z],
													 b[x][z],
													 w[x][z]));

		invalidate();
	}
	interface MyCallback {
		void callbackACTION_DOWN(); 
	} 
	

	public ViewCanvas(Context c) {
		super(c);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setARGB(255, 255, 255, 255);
	}

	public ViewCanvas(Context c, AttributeSet attrs) {
		super(c, attrs);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setARGB(255, 255, 255, 255);
	}
	
	public void setBrush(Brush brush, Context context)
	{
		this.brush = brush;
		this.context = context;
	}
	
	public void setBG(int idBG)
	{
		this.idBG = idBG;
		bgBitmap = BitmapFactory.decodeResource(context.getResources(), idBG);
		rectBG = new Rect(0, 0,  bgBitmap.getWidth(), bgBitmap.getHeight());
	}
	
	
	@Override 
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.BLACK);
		
		if (isInEditMode()) return;
		
		canvas.drawBitmap(bgBitmap, rectBG, mRect, null);
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, mRectBitmap, mRect, null);
		}
	}
	
	public void spread()
	{
		if (items.size() > 1){
			Item item = items.get(0); // FROM this item
			for (int n = 1; n < items.size(); n++){
				Item item2 = items.get(n);  // TO this
				//item2.x = 360;
				//item2.y = 1200;
				
				for (int i = -item.radius; i <= item.radius ; i++)
					for (int j = -item.radius; j <= item.radius ; j++){
						int x1 = item.x+i;
						int y1 = item.y+j;
						int x2 = item2.x+i;
						int y2 = item2.y+j;
						if (x2 >= 0 && y2 >=0 && x2 < width && y2 < height 
						&&  x1 >= 0 && y1 >=0 && x1 < width && y1 < height	
								&& !modified[x2][y2]){
							if ((i)*(i)+(j)*(j) < item.radius2
								&& ! isNull(x1, y1)){

								add(x2, y2, x1, y1);
									mPaint.setColor(Var.ryb2rgb(r[x2][y2], 
																y[x2][y2],
																b[x2][y2],
																w[x2][y2]));
								mPaint.setAlpha(t[x2][y2]);
									mCanvas.drawPoint(x2, y2, mPaint);
								//}
							}
						}
					}
				//break;
			}
		}
		items.clear();
		invalidate();
	}

	
	public void paint()
	{
		//for (Item item : items){
		//	mCanvas.drawBitmap(brush.getStump(), item.x, item.y, mPaint);
		//}

		for (Item item : items){
			for (int i = -item.radius; i <= item.radius ; i++)
				for (int j = -item.radius; j <= item.radius ; j++){
					int x1 = item.x+i;
					int y1 = item.y+j;
					if (x1 >= 0 && y1 >=0 && x1 < width && y1 < height && !modified[x1][y1]){
						
						int brushColor = brush.getColor(i+item.radius, j+item.radius, item.radius);
						
						if (brushColor != -1 && (i)*(i)+(j)*(j) < item.radius2){
							set(x1, y1, item.pixel, item.alpha);
							
							mPaint.setColor(item.rgb);
							mPaint.setAlpha(item.alpha);
							mCanvas.drawPoint(x1, y1, mPaint);
							//mBitmap.setPixel(x1, y1, Color.rgb(0,0,50));
						}
					}
				}
		}
		
		items.clear();
		invalidate();
	}
	
	public void paint1()
	{
		for (Item item : items){
			for (int i = -item.radius; i <= item.radius ; i++)
				for (int j = -item.radius; j <= item.radius ; j++){
					int x1 = item.x+i;
					int y1 = item.y+j;
					if (x1 >= 0 && y1 >=0 && x1 < width && y1 < height && !modified[x1][y1]){
						if ((i)*(i)+(j)*(j) < item.radius2){
							set(x1, y1, item.pixel, item.alpha);
							
							//mPaint.setColor(item.rgb);
							//mPaint.setAlpha(item.alpha);
							//mCanvas.drawPoint(x1, y1, mPaint);
						}
					}
				}
		}
		items.clear();
		invalidate();
	}
	
	public void erase()
	{
		for (Item item : items){
			for (int i = -item.radius; i <= item.radius ; i++)
				for (int j = -item.radius; j <= item.radius ; j++){
					int x1 = item.x+i;
					int y1 = item.y+j;
					if (x1 >= 0 && y1 >=0 && x1 < width && y1 < height && !modified[x1][y1]){
					//if (! modified[item.x+i][item.y+j]){
						if ((i)*(i)+(j)*(j) < item.radius2){
							set(x1, y1, item.pixel, item.alpha);
							mBitmap.setPixel(x1, y1, 0);
						}
					}
				}
		}
		items.clear();
		invalidate();
	}
	
	
	public void clear() {
		if (mCanvas != null) {
			mCanvas.drawColor(Color.BLUE, PorterDuff.Mode.CLEAR);
			for (int x = 0; x < width; x++)
				for (int z = 0; z < height; z++){
					r[x][z] = 0;
					y[x][z] = 0;
					b[x][z] = 0;
					w[x][z] = 0;
					t[x][z] = 0;
				}
					
			invalidate();
		}
	}

	@Override protected void onSizeChanged(int wd, int h, int oldw,
										   int oldh) {
		super.onSizeChanged(wd, h, oldw,oldh);
		
		width = Math.max(wd, h);
		height = Math.max(wd, h);
		
		if (isInEditMode()) return;

		load(Var.APP_FOLDER+"/screen.png", false);
		
		if (offsetX > width) offsetX = 0;
		if (offsetY > height) offsetY = 0;
		
		mRect.set(0, 0, (int)(width * kZooming), (int)(height * kZooming));
		mRect.offset(offsetX, offsetY);
		
		int i = prefs.getInt(PREFS_BRUSH_TRANSP, 100);
		int j = prefs.getInt(PREFS_BRUSH_SIZE, 1003);
		
		brush.setTransparency(i);
		brush.setSize(j, width);
	}


	private int x0; 
	private int y0; 
	private double distance = 0; 
	 
	private Point center0 = new Point();
	private Point center1 = new Point();
	private boolean resize = false;
	private boolean pointerUp = false;
	
	private double distance(PointerCoords center, PointerCoords coord){
		Float minX = Math.min(center.x, coord.x);
		Float maxX = Math.max(center.x, coord.x);
		Float x2 = (maxX-minX)*(maxX-minX);
		Float minY = Math.min(center.y, coord.y);
		Float maxY = Math.max(center.y, coord.y);
		Float y2 = (maxY-minY)*(maxY-minY);

		return  Math.sqrt(x2 + y2);
	}
	
	@Override public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		int x = (int)event.getX();
		int y = (int)event.getY();
		
		Var.Mode mode = brush.getMode(); 
		
		switch (action){
			case MotionEvent.ACTION_DOWN:
	        	callback.callbackACTION_DOWN();
				break;
			case MotionEvent.ACTION_MOVE:
				//if (mode == Var.Mode.none){
					// One finger
					if (event.getPointerCount() == 1){
						/*if (pointerUp){
							x0 = x;
							y0 = y;
							pointerUp = false;
							return true;
						}
						offsetX = offsetX + (x - x0);
						offsetY = offsetY + (y - y0);
						x0 = x;
						y0 = y;
						mRect.set(0, 0, (int)(width * kZooming), (int)(height * kZooming));
						mRect.offset(offsetX, offsetY);
						*/
						break;
					} else {
						// Two fingers
						
						PointerCoords coord0 = new PointerCoords();
						PointerCoords coord1 = new PointerCoords();
						event.getPointerCoords(0, coord0);
						event.getPointerCoords(1, coord1);
						
						double distance1 = distance(coord0, coord1)/10;

						center1.x = (int) ((coord0.x+coord1.x)/2);
						center1.y = (int) ((coord0.y+coord1.y)/2);
						
						if (!resize){
							center0.x = (int)((coord0.x+coord1.x)/2);
							center0.y = (int)((coord0.y+coord1.y)/2);
							resize = true;
							
							distance = distance1/kZooming;

							x0 = center1.x;
							y0 = center1.y;
						}
						
						double k1 = kZooming;
						
						if (distance != 0) // && k * distance1/distance > 0.3 && k * distance1/distance < 5
							kZooming = distance1/distance;
						
						x = center1.x;
						y = center1.y;
						
						offsetX = offsetX + (x - x0);
						offsetY = offsetY + (y - y0);
						
						offsetX = offsetX + (int)((-width*kZooming + width*k1)/2);
						offsetY = offsetY + (int)((-height*kZooming + height*k1)/2);
						
						mRect.set(0, 0, (int)(width * kZooming), (int)(height * kZooming));
						mRect.offset(offsetX, offsetY);
						
						center0.x = center1.x;
						center0.y = center1.y;
						
						if (distance == 0)
							distance = distance1;
						x0 = x;
						y0 = y;
						return true;
					}
				//}
				//break;
			case MotionEvent.ACTION_POINTER_UP:
				PointerCoords coord0 = new PointerCoords();
				event.getPointerCoords(0, coord0);
				x0 = 0;//(int) coord0.x;
				y0 = 0;// (int) coord0.y;
				resize = false;
				pointerUp = true;
				distance = 0;
				return true;
			case MotionEvent.ACTION_UP:
				Log.d("","UP ");
				clearLayerModified();
				brush.restoreThickness();
				x0 = x;
				y0 = y;
				resize = false;
				distance = 0;
				return true;
		}
		x0 = x;
		y0 = y;
		if (brush.getMode() == Var.Mode.none) 
			return true;
		
		int N = event.getHistorySize();
		
		for (int i=0; i<N; i++) {
			drawPoint((int)((event.getHistoricalX(i)-offsetX)/kZooming), (int)((event.getHistoricalY(i)-offsetY)/kZooming),
					  event.getHistoricalPressure(i),
					  event.getHistoricalSize(i));
		}
		
		drawPoint((int)((event.getX()-offsetX)/kZooming), (int)((event.getY()-offsetY)/kZooming), event.getPressure(),
				  event.getSize());
		connectPoints();
		return true;
	}

	private void connectPoints() {
		int n = items.size();
		
		for (int i = 0; i < n-1 ; i++){
			Item item0 = items.get(i);
			Item item1 = items.get(i+1);
			if (Math.abs(item0.x - item1.x) > item0.radius*2 || Math.abs(item0.y - item1.y) > item0.radius*2){
				items.add(i, new Item(item0, item1));
				i++;
				n++;
			}
				
		}
	}

	public void clearLayerModified() {
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				modified[i][j] = false;
	}
	
	private void drawPoint(float x, float y, float pressure, float size) {
		mCurX = (int)x;
		mCurY = (int)y;
		
		if (size == 0 && pressure != 1)
			size = pressure/2;
		
		mCurPressure = pressure;
		mCurSize = size;
		
		mCurWidth = (int)(mCurSize*(brush.getSize()));
		if (mCurWidth < 1) mCurWidth = 1;
		
		int n = mCurWidth*2;
		
		mPaint.setColor(Color.BLUE);
		if (mBitmap != null)
			items.add(new Item(mCurX, mCurY, mCurWidth, brush));
	
	}
	
	public class Item {
		int x;
		int y;
		int radius;
		int radius2;
		Pixel pixel;
		int alpha;
		int rgb;
		
		Item(int x, int y, int radius, Brush brush){
			this.x = x;
			this.y = y;
			
			this.radius = radius;
			this.radius2 = radius*radius;
			this.pixel = brush.getPixel();
			int t = brush.getThickness();
			if (t > 200)
				this.alpha = 255;
			else
				this.alpha = t;
			
			rgb = brush.getColor();

			//Log.d("", "this.alpha  "+this.alpha+"  "+brush.getTransparency()+"  "+(this.alpha/100f*brush.getTransparency()));
			this.alpha = (int)(this.alpha/100f*brush.getTransparency());
		};

		public Item(Item item0, Item item1) {
			this.x = (item0.x + item1.x)/2;
			this.y = (item0.y + item1.y)/2;
			
			this.radius = item0.radius;
			this.radius2 = item0.radius2;
			this.pixel = item0.pixel;
			this.alpha = item0.alpha;
			this.rgb = item0.rgb;
		}
		
	}

	////////////////////////////////

	private void set(int i, int j, Pixel pixel, int alpha) {
		if (pixel == null) return;
		
		r[i][j] = (byte)pixel.red;
		y[i][j] = (byte)pixel.yellow;
		b[i][j] = (byte)pixel.blue;
		w[i][j] = (byte)pixel.white;
		t[i][j] = (byte)alpha;
		
		modified[i][j] = true;
	}
	
	private boolean isNull(int i, int j) {
		return false;
		//return r[i][j] + y[i][j] + b[i][j] + w[i][j] == 0; 
	}
	
	private void add(int i, int j, int i1, int j1) {
		modified[i][j] = true;
		
		//Log.d("", ""+t[i][j]);
		
		float rr = 100/ Var.mPercent * r[i][j] + 100/Var.mPercent * r[i1][j1];   
		float yy = 100/ Var.mPercent * y[i][j] + 100/Var.mPercent * y[i1][j1];   
		float bb = 100/ Var.mPercent * b[i][j] + 100/Var.mPercent * b[i1][j1]; 
		float ww = 100/ Var.mPercent * w[i][j] + 100/Var.mPercent * w[i1][j1]; 

		r[i][j] = (byte) (rr / (rr + yy + bb + ww) * Var.mPercent);
		y[i][j] = (byte) (yy / (rr + yy + bb + ww) * Var.mPercent);
		b[i][j] = (byte) (bb / (rr + yy + bb + ww) * Var.mPercent);
		w[i][j] = (byte) (ww / (rr + yy + bb + ww) * Var.mPercent);
		int t1 = (int)t[i][j]&255;
		int t2 = (int)t[i1][j1]&255;
		
		t[i][j] = (byte)((t1 + t2)>>1);
		
		//t[i][j] = (byte)((t[i][j]&255+t[i1][j1]&255)>>1);
		//Log.d("", ""+t[i][j]);
	}

	public Bitmap getBitmap() {
		return mBitmap;
	}

	public boolean save(String fileName) {
		FileOutputStream out = null;
		byte[] a = new byte[width*height];
		
		Log.d("","SAVE ...");
		try {
			if (mBitmap == null)
				return true;
		    out = new FileOutputStream(fileName);
		    mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
		    // PNG is a lossless format, the compression factor (100) is ignored
		    String fileName1 = fileName.replaceFirst("[.][^.]+$", "")+".plt";
		    
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName1));

			for (int x = 0; x < width; x++)
				for (int z = 0; z < height; z++)
					a[x*height + z]   = r[x][z];
			bos.write(a);
			for (int x = 0; x < width; x++)
				for (int z = 0; z < height; z++)
					a[x*height + z]   = y[x][z];
			bos.write(a);
			for (int x = 0; x < width; x++)
				for (int z = 0; z < height; z++)
					a[x*height + z]   = b[x][z];
			bos.write(a);
			for (int x = 	0; x < width; x++)
				for (int z = 0; z < height; z++)
					a[x*height + z]   = w[x][z];
			bos.write(a);
			for (int x = 0; x < width; x++)
				for (int z = 0; z < height; z++)
					a[x*height + z]   = t[x][z];
			bos.write(a);
			
			bos.flush();
			bos.close();
			a = null;
			Log.d("","SAVED.");
		} catch (Exception e) {
			Log.d("", "ERROR "+e);
		    e.printStackTrace();
			return false;
		} finally {
		    try {
		        if (out != null) {
		            out.close();
		        }
		    } catch (IOException e) {
				Log.d("", "ERROR "+e);
		        e.printStackTrace();
				return false;
		    }
		}	
		return true;
	}
	
	public void load(String fileName, boolean newImage) {
		Bitmap newBitmap;
		
		File file = new File(fileName);
		if(file.exists() || !newImage){                 
			//Log.d("","------------->>>> "+fileName);
			Bitmap bmp = BitmapFactory.decodeFile(fileName);
			try{
				newBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
				width = newBitmap.getWidth(); 
				height = newBitmap.getHeight(); 
				
				bmp = null;
			} catch (Exception e) {
				newBitmap = Bitmap.createBitmap(width, height,
						Bitmap.Config.ARGB_8888);
			};
		} else
			newBitmap = Bitmap.createBitmap(width, height,
					Bitmap.Config.ARGB_8888);

		Canvas newCanvas = new Canvas();
		newCanvas.setBitmap(newBitmap);
		if (mBitmap != null) {
			newCanvas.drawBitmap(mBitmap, 0, 0, null);
		}
		
		width = newBitmap.getWidth(); 
		height = newBitmap.getHeight(); 
		
		mBitmap = newBitmap;
		mCanvas = newCanvas;
		mRectBitmap = new Rect(0,0, width, height);
		//mBitmap.setHasAlpha(true);
		r = new byte[width][height];
		y = new byte[width][height];
		b = new byte[width][height];
		w = new byte[width][height];
		t = new byte[width][height];
		modified = new boolean[width][height];

		for (int x = 0; x < width; x++)
			for (int z = 0; z < height; z++){
				r[x][z] = 0;
				y[x][z] = 0;
				b[x][z] = 0;
				w[x][z] = 0;
				t[x][z] = 0;
				modified[x][z] = false;
			}
		
		if (!newImage){
			Log.d("","LOAD...");
			String fileName1 = fileName.replaceFirst("[.][^.]+$", "")+".plt";
			file = new File(fileName1);
			if(file.exists()){                  
				byte[] a = new byte[width*height];
				try {
					BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName1));
					bis.read(a, 0, width*height);
					for (int x = 0; x < width; x++)
						for (int z = 0; z < height; z++)
							r[x][z] = a[x*height + z];
					bis.read(a, 0, width*height);
					for (int x = 0; x < width; x++)
						for (int z = 0; z < height; z++)
							y[x][z] = a[x*height + z];
					bis.read(a, 0, width*height);
					for (int x = 0; x < width; x++)
						for (int z = 0; z < height; z++)
							b[x][z] = a[x*height + z];
					bis.read(a, 0, width*height);
					for (int x = 0; x < width; x++)
						for (int z = 0; z < height; z++)
							w[x][z] = a[x*height + z];
					bis.read(a, 0, width*height);
					for (int x = 0; x < width; x++)
						for (int z = 0; z < height; z++)
							t[x][z] = a[x*height + z];
					Log.d("","LOADED.");

					bis.close();
					a = null;
				} catch (FileNotFoundException e) {
					Log.d("", "ERROR "+e);
				} catch (IOException e) {
					Log.d("", "ERROR "+e);
				} catch (Exception e) {
					Log.d("", "ERROR "+e);
				}
			}
		}
		
		// Reset offset and zooming 
		offsetX = 0;
		offsetY = 0;
		kZooming = 1;
		mRect.set(0, 0, width, height);
		mRect.offset(offsetX, offsetY);
		
		invalidate();
	}

	public boolean saveWithCanvas(String fileName, boolean mail) {
		Bitmap bmp= Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Rect rect = new Rect(0,0,width, height);
		Canvas newCanvas = new Canvas();
		newCanvas.setBitmap(bmp);
		newCanvas.drawBitmap(bgBitmap, rectBG, rect, null);
		
		if (mBitmap != null) {
			newCanvas.drawBitmap(mBitmap, 0, 0, null);
		}
		
		String f = "";
		FileOutputStream out = null;
		if (mail){
			f = Var.APP_FOLDER+"/picture.png";
		} else {
			f = fileName;
			if (!f.contains("_all.png"))
				f = fileName.replace(".png", "_all.png");
		}
		
		try {
		    out = new FileOutputStream(f);
		    bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
			Log.d("","SAVED file <"+f+">");
		} catch (Exception e) {
			Log.d("", "ERROR "+e);
		    e.printStackTrace();
			return false;
		}
		
		if (mail){
			Intent emailIntent = new Intent(Intent.ACTION_SEND);
			emailIntent.setType("text/plain");
			emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"email@example.com"});
			emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Image from RYB Painter");
			emailIntent.putExtra(Intent.EXTRA_TEXT, "Image was created in RYB Painter by Alexey Mochalov.");
			File file = new File(f);
			if (!file.exists() || !file.canRead()) {
				return true;
			}
			Uri uri = Uri.fromFile(file);
			emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
			context.startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));		}
		return true;
	}

	
	protected void onResume(SharedPreferences prefs){
		offsetX = prefs.getInt(PREFS_OFFSETX, 0);
		offsetY = prefs.getInt(PREFS_OFFSETY, 0);
		kZooming = prefs.getFloat(PREFS_K, 1);
		this.prefs = prefs;
	}

	@Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
		modified = null;
		r = null;
		y = null;
		b = null;
		w = null;
		t = null;
		mBitmap = null;
    }
	
	
	public void onPause(Context context, Editor editor) {
		editor.putInt(PREFS_OFFSETX, offsetX);
		editor.putInt(PREFS_OFFSETY, offsetY);
		editor.putFloat(PREFS_K, (float)kZooming);

		editor.putInt(PREFS_BRUSH_TRANSP, brush.getTransparency());
		editor.putInt(PREFS_BRUSH_SIZE, brush.getSize0());
		
		save(Var.APP_FOLDER+"/screen.png");
	}


	public int getImageWidth() {
		return width;
	}

	public int getImageHeight() {
		return height;
	}
	
	
}
