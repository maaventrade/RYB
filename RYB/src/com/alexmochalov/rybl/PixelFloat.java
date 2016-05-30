package com.alexmochalov.rybl;

import java.io.Serializable;

public class PixelFloat {
	// percent of red
	float red;
	// percent of yellow
	float yellow;
	// percent of blue
	float blue;
	float white;
	// red + yellow + blue + white = 100 
	boolean modified = false;
	
	public PixelFloat(short thickness){
		this.red = 0;
		this.yellow = 0;
		this.blue = 0;
		this.white = 0;
	}

	public void add(PixelFloat p)
	{
		// TODO: Implement this method
	}

	public void setModified(boolean p0)
	{
		modified = true;
	}

	public PixelFloat(short thickness, short[] ryb) {
		this.red = ryb[0];
		this.yellow = ryb[1];
		this.blue = ryb[2];
		this.white = 0;
	}
	
	public PixelFloat(float r, float y, float b, float w) {
		this.red = r;
		this.yellow = y;
		this.blue = b;
		this.white = w;
	}
	
	public PixelFloat(float r, float y, float b) {
		float k = 100/(r+y+b);
		this.red = r*k;
		this.yellow = y*k;
		this.blue = b*k;
		this.white = 0;
	}
	
	public PixelFloat(float r, float y, float b, float w, boolean percent) {
		float k = 100/(r+y+b);
		this.red = r*k;
		this.yellow = y*k;
		this.blue = b*k;
		this.white = w;
	}
	
	public PixelFloat(short r, short y, short b, short w) {
		this.red = r;
		this.yellow = y;
		this.blue = b;
		this.white = w;
	}

	public boolean isZero()
	{
		return red + yellow + blue + white == 0;
	}

	public void clear()
	{
		this.red = 0;
		this.yellow = 0;
		this.blue = 0;
		this.white = 0;
//		this.color = Var.ryb2rgb(this);
	}
	
	public void set(short t, PixelFloat pixel, boolean modified)
	{
		if (pixel == null) return;

		red = pixel.red;
		yellow = pixel.yellow;
		blue = pixel.blue;
		white = pixel.white;
		
		this.modified = modified;
	}
	
	public void add1(Pixel pixel, boolean modified)
	{
		if (pixel == null) return;
		this.modified = modified;
		
		float rr = 100/ Var.mPercent * red + Var.mPercentAdd/Var.mPercent * pixel.red ;   
		float yy = 100/ Var.mPercent * yellow + Var.mPercentAdd/Var.mPercent * pixel.yellow ;   
		float bb = 100/ Var.mPercent * blue + Var.mPercentAdd/Var.mPercent * pixel.blue ; 
		float ww = 100/ Var.mPercent * white + Var.mPercentAdd/Var.mPercent * pixel.white ; 
		
		red = (rr / (rr + yy + bb + ww) * Var.mPercent);
		yellow = (yy / (rr + yy + bb + ww) * Var.mPercent);
		blue = (bb / (rr + yy + bb + ww) * Var.mPercent);
		white = (ww / (rr + yy + bb + ww) * Var.mPercent);
	}
	
	public void add(PixelFloat pixel, boolean modified)
	{
		if (pixel == null) return;
		this.modified = modified;
		
		float rr = 100/ Var.mPercent * red + 50/Var.mPercent * pixel.red ;   
		float yy = 100/ Var.mPercent * yellow + 50/Var.mPercent * pixel.yellow ;   
		float bb = 100/ Var.mPercent * blue + 50/Var.mPercent * pixel.blue ; 
		float ww = 100/ Var.mPercent * white + 50/Var.mPercent * pixel.white ; 
		
		red = (rr / (rr + yy + bb + ww) * Var.mPercent);
		yellow = (yy / (rr + yy + bb + ww) * Var.mPercent);
		blue = (bb / (rr + yy + bb + ww) * Var.mPercent);
		white = (ww / (rr + yy + bb + ww) * Var.mPercent);
	}
	
	public boolean isModified() {
		return modified;
	}
	
	public void clearModified() {
		modified = false;
	}
	public String toStr() {
		// TODO Auto-generated method stub
		return ""+" red "+red+" yellow "+yellow+" blue "+blue+" white "+white;
	}

	public void copy(PixelFloat pixel) {
		this.red = pixel.red;
		this.yellow = pixel.yellow;
		this.blue = pixel.blue;
		this.white = pixel.white;
		
	}
}
