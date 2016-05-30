package com.alexmochalov.rybl;

public class PointOfLine {
	int x = 0;
	int y = 0;
	PointOfLine next = null;
	boolean connect; // TRUE - the point connects to the Next point;
	// FALSE - it is the last point in the chain.
	
	public PointOfLine(int x1, int y1, boolean connect) {
		this.x = x1;
		this.y = y1;
		this.connect = connect;
	}
	
}
