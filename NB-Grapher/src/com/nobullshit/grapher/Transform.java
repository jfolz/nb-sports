package com.nobullshit.grapher;

public class Transform {

	double sx,sy,tx,ty;
	
	public Transform() {
		reset();
	}
	
	public void reset() {
		sx = 1;
		sy = 1;
		tx = 0;
		ty = 0;
	}
	
	public double transformX(double x) {
		return (x+tx)*sx;
	}
	
	public double transformY(double y) {
		return (y+ty)*sy;
	}
	
}
