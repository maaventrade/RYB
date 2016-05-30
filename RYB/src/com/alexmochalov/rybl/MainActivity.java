package com.alexmochalov.rybl;

import android.app.*;
import android.content.*;
import android.content.SharedPreferences.*;
import android.graphics.*;
import android.os.*;
import android.preference.*;
import android.support.v4.content.*;
import android.util.*;
import android.view.*;
import android.view.ContextMenu.*;
import android.view.View.*;
import android.widget.*;
import android.widget.LinearLayout.*;

import com.mochalov.alex.rybl.R;

import java.io.*;

import android.view.View.OnClickListener;

public class MainActivity extends Activity implements View.OnClickListener
{
	// The Current Brush contains current color or tool or nothing 
	Brush brush;
	// We paint on this canvad
	ViewCanvas viewCanvas;
	
	
    SharedPreferences prefs;
    static final String PREFS_CANVAS_COLOR = "PREFS_CANVAS_COLOR";
    static final String PREFS_BRUSH_RADIUS = "PREFS_BRUSH_RADIUS";
    static final String PREFS_FILENAME = "PREFS_FILENAME";
    static final String PREFS_INIT_PATH = "PREFS_INIT_PATH";
	static final String PREFS_FIRST_START = "PREFS_FIRST_START";
	
	/** How often to repaint the contents of the window (in ms). */
    static final int REPAINT_DELAY = 10;
	/** Used as a pulse to gradually fade the contents of the window. */
    static final int REPAINT_MSG = 1;
	
	RelativeLayout root;
	LinearLayout menu;
	LinearLayout submenu;
	ViewSubmenu viewSubmenu;
	LinearLayout submenu2;
	LinearLayout submenu3;
	LinearLayout submenu4;
	LinearLayout submenuDens;
	
	Tube tube;
	Tool toolSize;
	Tool toolOpaque;
	Tool toolDens;
	
	boolean rootVisible = true;
	boolean submenuVisible = false;
	boolean submenu3Visible = false;
	boolean submenu4Visible = false;
	boolean submenuDenVisible = false;
	
	
	// The path to save and load images 
	String initPath;
	// Name of the current image (*.png)
	String fileName;
	// Image file extention
	static final String FILE_EXT[] = {".png"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActionBar().hide();
        setSize();
        
	}


	void selectMainView(){
        setContentView(R.layout.activity_main);
		prefs =  PreferenceManager.getDefaultSharedPreferences(this);
		fileName = prefs.getString(PREFS_FILENAME, "NewImage.png");
		initPath = prefs.getString(PREFS_INIT_PATH, Environment.getExternalStorageDirectory().getPath());
		
		// Load a background image.
		Var.initBG(this);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();

		selectMainView();
		
		// If it is a first start of the app let shkw Information Dialog.
		boolean firstStart = prefs.getBoolean(PREFS_FIRST_START, true);
		if (firstStart){
			DialogInfo dialogInfo = new DialogInfo(this);
			dialogInfo.show();
		}
		
		
		createMenu();
		
		viewCanvas = (ViewCanvas)this.findViewById(R.id.viewCanvas);
		viewCanvas.setBrush(brush, this);
		
		int idBG = prefs.getInt("PREFS_BG", R.drawable.canvas0);
		viewCanvas.setBG(idBG);
		
		viewCanvas.callback = new ViewCanvas.MyCallback(){
			// When some brush is used lets add it to the list (submenu)
			@Override
			public void callbackACTION_DOWN() {
				if (viewSubmenu != null)
					viewSubmenu.insertBrush(brush);
				else
					Log.d("", "viewSubmenu = NULL !!!??? ");
			}
		};
		
		// Load the list of the used brushes
		viewSubmenu.loadBrushes(brush);
		// Clear the Current Brush
		brush.setMode(Var.Mode.none);
		// APP_FOLFER is ised to cave the Canvas
		File file = new File(Var.APP_FOLDER);
		if(!file.exists()){                          
			file.mkdirs();                  
		}
		// The ViewCanvas doesnt have its own method onResume
		viewCanvas.onResume(PreferenceManager.getDefaultSharedPreferences(this));
		
		startRepainting();
	}

	@Override
	  protected void onPause(){
		  super.onPause();
		  Editor editor = prefs.edit();
		  // The ViewCanvas doesnt have its own method onPause
		  viewCanvas.onPause(this, editor);
		  // Save the list of the used brushes
		  viewSubmenu.saveBrushes();
		  viewSubmenu.clearBrushes();
		 
		  //editor.putInt(PREFS_CANVAS_COLOR, palette.getCanvasColor());
		  editor.putInt(PREFS_BRUSH_RADIUS, Var.brushRadius);
		  editor.putString(PREFS_FILENAME, fileName);
		  editor.putString(PREFS_INIT_PATH, initPath);
		  editor.putBoolean(PREFS_FIRST_START, false);
		  editor.putInt("PREFS_BG", viewCanvas.getIdBG());
		  
		  editor.apply();
		  // To avoid memory leaking
		  if (mHandler != null)
			  mHandler.removeCallbacksAndMessages(null);
		  viewSubmenu = null;
		  viewCanvas = null;
		  
		  brush = null;
		  viewCanvas = null;
		  
		  root = null;
		  menu = null;
		  submenu = null;
		  viewSubmenu = null;
		  submenu2 = null;
		  submenu3 = null;
		  submenu4 = null;
			
		  /////  Tube tube; tube = null;
		  toolSize = null;
		  toolOpaque = null;
		  
	  }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
	    switch (id)
	     {
	     case R.id.action_clear_canvas_color:
				viewCanvas.clear();
	    	    return true;
			 case R.id.action_colors:
				DialogColors dialogColors = new DialogColors(this, brush);
				dialogColors.show();
				break;
		 case R.id.action_save_as:
				if (viewCanvas.save(Var.APP_FOLDER+"/"+fileName))
					Toast.makeText(this, "File saved <"+fileName+">", Toast.LENGTH_LONG).show();
				return true;
		 case R.id.action_save_with:
			 /*
			 	final SelectFileDialog selectFileDialog = new SelectFileDialog(this, initPath, FILE_EXT, "", true, "" );
			 	selectFileDialog.callback = new SelectFileDialog.MyCallback() {
					@Override
					public void callbackACTION_SELECTED(String fileName) {
						if (fileName.equals("send picture by email")){
							viewCanvas.saveWithCanvas(fileName, true);
							return;
					    } else if (!fileName.endsWith(".png"))
							fileName = fileName + ".png";
						if (viewCanvas.saveWithCanvas(fileName, false));
							Toast.makeText(MainActivity.this, "File saved ", Toast.LENGTH_LONG).show();
					}
				};
			 	
			 	selectFileDialog.show();
			 	*/
				return true;
		 case R.id.action_file_information:
			 AlertDialog.Builder builder = new AlertDialog.Builder(this);
			 
			 builder.setMessage(fileName+"\n("+viewCanvas.getImageWidth()+"x"+viewCanvas.getImageHeight()+")")
			        .setTitle(this.getResources().getString(R.string.action_file_information))
			        .setCancelable(false)
			        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int id) {
			                 //do things
			            }
			        });
			 AlertDialog alert = builder.create();
			 alert.show();
			 return true;
		 case R.id.action_load:
						DialogSelectImage dialog = new DialogSelectImage(this, 
						viewCanvas.getWidth(), 
						viewCanvas.getHeight());
			 
				 dialog.callback = new DialogSelectImage.MyCallback(){

					@Override
					public void callbackACTION_OPEN(String name) {
						viewCanvas.clear(); //?????
						int i = name.lastIndexOf("/");
						if (i > 0)
							fileName = name.substring(i+1);
						else fileName = name;
							
						viewCanvas.load(name, false);
						
					}

					@Override
					public void callbackACTION_NEW(int w, int h, String name) {
						viewCanvas.clear(); //????? 
						fileName = name;
						viewCanvas.newImage(w, h, name);
					}
					 
				 };

				dialog.show();
				
				return true;
		 //case R.id.action:
		 //		palette.refresh();
		 //		return true;
		 case R.id.action_information:
				DialogInfo dialogInfo = new DialogInfo(this);
				
				dialogInfo.show();
				break;
			 case R.id.action_select_canvas:
				DialogCanvas dialogCanvas = new DialogCanvas(this);
				 dialogCanvas.callback = new DialogCanvas.MyCallback(){
					 @Override
					 public void callbackACTION_DOWN(int idBG)
					 {
						 viewCanvas.setBG(idBG);
					 }
				 };
				dialogCanvas.show();
				break;
			case R.id.action_exit:
				finish();
		 default:	
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
	                            ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	}	
	
    /**
     * Start up the pulse to repaint the screen, clearing any existing pulse to
     * ensure that we don't have multiple pulses running at a time.
     */
    void startRepainting() {
        mHandler.removeMessages(REPAINT_MSG);
        mHandler.sendMessageDelayed(
			mHandler.obtainMessage(REPAINT_MSG), REPAINT_DELAY);
    }

    /**
     * Stop the pulse to repaint the screen.
     */
    void stopRepainting() {
        mHandler.removeMessages(REPAINT_MSG);
    }
	
	Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case REPAINT_MSG: {
						if (brush.getMode() == Var.Mode.spread)
							 viewCanvas.spread();
						else if (brush.getMode() == Var.Mode.erase)
							viewCanvas.erase();
						else viewCanvas.paint();
						
						mHandler.sendMessageDelayed(
                            mHandler.obtainMessage(REPAINT_MSG), REPAINT_DELAY);
						break;
					}
                default:
                    super.handleMessage(msg);
            }
        }
    };
    
	@Override public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (rootVisible){
				root.setVisibility(View.INVISIBLE);
			}else{
				root.setVisibility(View.VISIBLE);
			}
			rootVisible = !rootVisible;
			return true; 
		} return super.onKeyDown(keyCode, event);
	}
	
	void createMenu(){
		root = (RelativeLayout) findViewById(R.id.root);
		menu = (LinearLayout) findViewById(R.id.menu);
	    submenu = (LinearLayout) findViewById(R.id.submenu);
		submenu.setVisibility(View.INVISIBLE);

		viewSubmenu = (ViewSubmenu) findViewById(R.id.submenu1);
		viewSubmenu.initValues(this);
		
		submenu2 = (LinearLayout) findViewById(R.id.submenu2);
		//read();

		// Current brush
		brush = new Brush(this);
		Var.brushRadius = prefs.getInt(PREFS_BRUSH_RADIUS, 30);
		brush.setRadius(Var.brushRadius, true);
		
		menu.addView(brush);
		brush.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Log.d("", brush.pixelToString());
				if (!submenuVisible)
					submenu.setVisibility(View.VISIBLE);
				else submenu.setVisibility(View.INVISIBLE);
				submenuVisible = !submenuVisible;
			}});
		
		// Dropdown menu (with used colors) 
		final Tool tool = new Tool(this);
		tool.setMode(Var.Mode.none, BitmapFactory.decodeResource(getResources(), R.drawable.void_tube));
		tool.setOnClickListener(this);
		menu.addView(tool);
		
		// Colors of the palette 
		tube = new Tube(this);
		tube.setColor(hexToRYB(ContextCompat.getColor(this, R.color.ryb_red)));
		tube.setBrush(brush);
		menu.addView(tube);

		tube = new Tube(this);
		tube.setColor(hexToRYB(ContextCompat.getColor(this, R.color.ryb_yellow)));
		tube.setBrush(brush);
		menu.addView(tube);
		
		tube = new Tube(this);
		tube.setColor(hexToRYB(ContextCompat.getColor(this, R.color.ryb_blue)));
		tube.setBrush(brush);
		menu.addView(tube);
		
		tube = new Tube(this);
		tube.setColor(hexToRYB(ContextCompat.getColor(this, R.color.ryb_umbra)));
		tube.setBrush(brush);
		menu.addView(tube);

		tube = new Tube(this);
		tube.setColor(hexToRYB(ContextCompat.getColor(this, R.color.ryb_white)));
		tube.setBrush(brush);
		menu.addView(tube);

		//if (!ViewConfiguration.get(this).hasPermanentMenuKey()){
			final Tool optionsSubmenu = new Tool(this);
			optionsSubmenu.setMode(2, BitmapFactory.decodeResource(getResources(), R.drawable.menu));
			optionsSubmenu.setOnClickListener(this);
		
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT); 
			params.weight = 1.0f; 
			params.gravity = Gravity.RIGHT;
			optionsSubmenu.setLayoutParams(params);
			
			menu.addView(optionsSubmenu);
		//}
		
		viewSubmenu.callback = new ViewSubmenu.MyCallback() {
			@Override
			public void callbackSELECTED(Brush v) {
				submenu.setVisibility(View.INVISIBLE);
				submenuVisible = false;
				brush.copy((Brush)v);
			}
		};

		final Tool toolSpread = new Tool(this);
		toolSpread.setMode(Var.Mode.spread, BitmapFactory.decodeResource(getResources(), R.drawable.spread));
		toolSpread.setOnClickListener(this);
		submenu2.addView(toolSpread);

		final Tool toolErase = new Tool(this);
		toolErase.setMode(Var.Mode.erase, BitmapFactory.decodeResource(getResources(), R.drawable.erase));
		toolErase.setOnClickListener(this);
		submenu2.addView(toolErase);

		//-------------- SUBMENU opaque -----------------------
		submenu3 = new LinearLayout(this);
		submenu3.setOrientation(LinearLayout.HORIZONTAL);
		submenu2.addView(submenu3);

		toolOpaque = new Tool(this);
		toolOpaque.setMode(100, null);
		submenu3.addView(toolOpaque);

		final Tool toolOpaque1 = new Tool(this);
		toolOpaque1.setMode(100, null);
		toolOpaque1.setOnClickListener(this);
		submenu3.addView(toolOpaque1);

		final Tool toolOpaque4 = new Tool(this);
		toolOpaque4.setMode(80, null);
		toolOpaque4.setOnClickListener(this);
		submenu3.addView(toolOpaque4);

		final Tool toolOpaque2 = new Tool(this);
		toolOpaque2.setMode(50, null);
		toolOpaque2.setOnClickListener(this);
		submenu3.addView(toolOpaque2);

		final Tool toolOpaque3 = new Tool(this);
		toolOpaque3.setMode(10, null);
		toolOpaque3.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					brush.setTransparency(toolOpaque3.getCode());
					submenu.setVisibility(View.INVISIBLE);
					submenuVisible = false;
				}});
		submenu3.addView(toolOpaque3);

		toolOpaque1.setVisibility(View.INVISIBLE);
		toolOpaque2.setVisibility(View.INVISIBLE);
		toolOpaque3.setVisibility(View.INVISIBLE);
		toolOpaque4.setVisibility(View.INVISIBLE);

		toolOpaque.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					if (submenu3Visible){
						toolOpaque1.setVisibility(View.INVISIBLE);
						toolOpaque2.setVisibility(View.INVISIBLE);
						toolOpaque3.setVisibility(View.INVISIBLE);
						toolOpaque4.setVisibility(View.INVISIBLE);
					} else {
						toolOpaque1.setVisibility(View.VISIBLE);
						toolOpaque2.setVisibility(View.VISIBLE);
						toolOpaque3.setVisibility(View.VISIBLE);
						toolOpaque4.setVisibility(View.VISIBLE);
					}
					submenu3Visible = !submenu3Visible;
				}

			});
		
	//-------------- SUBMENU size -----------------------
	submenu4 = new LinearLayout(this);
	submenu4.setOrientation(LinearLayout.HORIZONTAL);
    //LayoutParams linLayoutParam = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); 		
	submenu2.addView(submenu4);
	

	toolSize = new Tool(this);
	toolSize.setMode(Var.Mode.erase, 
		BitmapFactory.decodeResource(getResources(), R.drawable.size2));
	submenu4.addView(toolSize);
	//submenu4.setVisibility(View.INVISIBLE);
	
	final Tool toolSize1 = new Tool(this);
	toolSize1.setMode(1000, BitmapFactory.decodeResource(getResources(), R.drawable.size1));
	toolSize1.setOnClickListener(this);
	submenu4.addView(toolSize1);
	
	final Tool toolSize2 = new Tool(this);
	toolSize2.setMode(1001, BitmapFactory.decodeResource(getResources(), R.drawable.size2));
	toolSize2.setOnClickListener(this);
	submenu4.addView(toolSize2);
	
	final Tool toolSize3 = new Tool(this);
	toolSize3.setMode(1002, BitmapFactory.decodeResource(getResources(), R.drawable.size3));
	toolSize3.setOnClickListener(this);
	submenu4.addView(toolSize3);
	
	final Tool toolSize4 = new Tool(this);
	toolSize4.setMode(1003, BitmapFactory.decodeResource(getResources(), R.drawable.size4));
	toolSize4.setOnClickListener(this);
	submenu4.addView(toolSize4);
	
	toolSize1.setVisibility(View.INVISIBLE);
	toolSize2.setVisibility(View.INVISIBLE);
	toolSize3.setVisibility(View.INVISIBLE);
	toolSize4.setVisibility(View.INVISIBLE);
	
	toolSize.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (submenu4Visible){
					toolSize1.setVisibility(View.INVISIBLE);
					toolSize2.setVisibility(View.INVISIBLE);
					toolSize3.setVisibility(View.INVISIBLE);
					toolSize4.setVisibility(View.INVISIBLE);
				} else {
					toolSize1.setVisibility(View.VISIBLE);
					toolSize2.setVisibility(View.VISIBLE);
					toolSize3.setVisibility(View.VISIBLE);
					toolSize4.setVisibility(View.VISIBLE);
				}
				submenu4Visible = !submenu4Visible;
			} 

		});
	
	//-------------- SUBMENU density -----------------------
	submenuDens = new LinearLayout(this);
	submenuDens.setOrientation(LinearLayout.HORIZONTAL);
	submenu2.addView(submenuDens);
	
	toolDens = new Tool(this);
	toolDens.setMode(Var.Mode.erase, 
		BitmapFactory.decodeResource(getResources(), R.drawable.den4));
	submenuDens.addView(toolDens);
	//submenu4.setVisibility(View.INVISIBLE);
	
	final Tool toolDens1 = new Tool(this);
	toolDens1.setMode(1011, BitmapFactory.decodeResource(getResources(), R.drawable.den1));
	toolDens1.setOnClickListener(this);
	submenuDens.addView(toolDens1);
	
	final Tool toolDens2 = new Tool(this);
	toolDens2.setMode(1012, BitmapFactory.decodeResource(getResources(), R.drawable.den2));
	toolDens2.setOnClickListener(this);
	submenuDens.addView(toolDens2);
	
	final Tool toolDens3 = new Tool(this);
	toolDens3.setMode(1013, BitmapFactory.decodeResource(getResources(), R.drawable.den3));
	toolDens3.setOnClickListener(this);
	submenuDens.addView(toolDens3);
	
	final Tool toolDens4 = new Tool(this);
	toolDens4.setMode(1014, BitmapFactory.decodeResource(getResources(), R.drawable.den4));
	toolDens4.setOnClickListener(this);
	submenuDens.addView(toolDens4);
	
	
	toolDens1.setVisibility(View.INVISIBLE);
	toolDens2.setVisibility(View.INVISIBLE);
	toolDens3.setVisibility(View.INVISIBLE);
	toolDens4.setVisibility(View.INVISIBLE);
	
	toolDens.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				if (submenuDenVisible){
					toolDens1.setVisibility(View.INVISIBLE);
					toolDens2.setVisibility(View.INVISIBLE);
					toolDens3.setVisibility(View.INVISIBLE);
					toolDens4.setVisibility(View.INVISIBLE);
				} else {
					toolDens1.setVisibility(View.VISIBLE);
					toolDens2.setVisibility(View.VISIBLE);
					toolDens3.setVisibility(View.VISIBLE);
					toolDens4.setVisibility(View.VISIBLE);
				}
				submenuDenVisible = !submenuDenVisible;
			} 

		});
	
	}

	short[] hexToRYB(int color)
	{
		short[] ret = { (short)(color >> 24), 
			(short)(color >> 16 & 0x00ff), 
			(short)(color >> 8 & 0x00ff), 
			(short)(color & 0x00ff) };
		return ret;
	}
	
	@Override
	public void onClick(View v) {
		Tool tool = (Tool)v;
		
		if (tool.getCode() <= 0)
			brush.setMode(tool.getMode(), tool.getIcon());
		else if (tool.getCode() == 2)
			openOptionsMenu();
		else if (tool.getCode() < 1000){
			toolOpaque.setCode(tool.getCode());
			brush.setTransparency(tool.getCode());
		} else if (tool.getCode() < 1010){
			toolSize.setIcon(tool.getIcon());
			brush.setSize(tool.getCode(), viewCanvas.getWidth());
		} else if (tool.getCode() < 1020){
			toolDens.setIcon(tool.getIcon());
			brush.setDensity(tool.getCode()-1010);
		}
		
		submenu.setVisibility(View.INVISIBLE);
		submenuVisible = false;
	}
	
	void setSize(){
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		float yInches= metrics.heightPixels/metrics.ydpi; 
		float xInches= metrics.widthPixels/metrics.xdpi;
		double diagonalInches = Math.sqrt(xInches*xInches + yInches*yInches); 
		//Toast.makeText(this, "suze "+diagonalInches, Toast.LENGTH_LONG).show();
		//double k = Math.sqrt(4.795f/diagonalInches);
		//Var.brushWidth = (int)(50f*k);
		//Var.brushRadius = (int)(32f*k);
		Var.brushWidth = Math.min(50, (int)(metrics.widthPixels/16f));
		Var.brushRadius = (int)(Var.brushWidth*0.64f);
		
		//Toast.makeText(this, "Var.brushWidth "+Var.brushWidth, Toast.LENGTH_LONG).show();
	}
	
}


