package com.nobullshit.grapher;

import android.graphics.Path;
import android.util.FloatMath;

public class Symbols {

	public static final int SYMBOL_NONE = 0x0;
	public static final int SYMBOL_PLUS = 0x1;
	public static final int SYMBOL_CROSS = 0x2;
	public static final int SYMBOL_CIRCLE = 0x4;
	public static final int SYMBOL_DOT = 0x8;
	public static final int SYMBOL_SQUARE = 0x10;
	public static final int SYMBOL_BOX = 0x20;
	public static final int SYMBOL_TRIANGLE = 0x40;
	public static final int SYMBOL_FILLTRIANGLE = 0x80;
	
	private Symbols() {}
	
	public static Path plus(float size) {
		Path p = new Path();
		float a = size / 2F;
		p.moveTo(-a, 0);
		p.lineTo(a, 0);
		p.moveTo(0, -a);
		p.lineTo(0, a);
		return p;
	}
	
	public static Path cross(float size) {
		Path p = new Path();
		float a = size / 2F;
		p.moveTo(-a, -a);
		p.lineTo(a, a);
		p.moveTo(-a, a);
		p.lineTo(a, -a);
		return p;
	}
	
	public static Path circle(float size) {
		Path p = new Path();
		float a = size / 2F;
		p.addCircle(0, 0, a, Path.Direction.CW);
		return p;
	}
	
	public static Path square(float size) {
		Path p = new Path();
		float a = size / 2F;
		p.addRect(-a, -a, a, a, Path.Direction.CW);
		return p;
	}
	
	public static Path triangleUp(float size) {
		Path p = new Path();
		float a = size / 2F;
		float h = FloatMath.sqrt(3) * a / 3F;
		p.moveTo(-a, h);
		p.lineTo(0, -2*h);
		p.lineTo(a, h);
		p.lineTo(-a, h);
		p.close();
		return p;
	}
	
	public static Path triangleDown(float size) {
		Path p = new Path();
		float a = size / 2F;
		float h = FloatMath.sqrt(3) * a / 3F;
		p.moveTo(-a, -h);
		p.lineTo(0, 2*h);
		p.lineTo(a, -h);
		p.lineTo(-a, -h);
		p.close();
		return p;
	}
	
	public static Path triangleLeft(float size) {
		Path p = new Path();
		float a = size / 2F;
		float h = FloatMath.sqrt(3) * a / 3F;
		p.moveTo(h, -a);
		p.lineTo(-2*h,0);
		p.lineTo(h, a);
		p.lineTo(h, -a);
		p.close();
		return p;
	}
	
	public static Path triangleRight(float size) {
		Path p = new Path();
		float a = size / 2F;
		float h = FloatMath.sqrt(3) * a / 3F;
		p.moveTo(-h, -a);
		p.lineTo(2*h,0);
		p.lineTo(-h, a);
		p.lineTo(-h, -a);
		p.close();
		return p;
	}

}
