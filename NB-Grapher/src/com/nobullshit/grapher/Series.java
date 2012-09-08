package com.nobullshit.grapher;

import java.util.Arrays;
import java.util.Comparator;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public abstract class Series {
	protected double[] Xs;
	protected double[] Ys;
	protected int color;
	protected CharSequence label;
	protected double minx=Double.MAX_VALUE, maxx=Double.MIN_VALUE, 
			miny=Double.MAX_VALUE, maxy=Double.MIN_VALUE;
	
	public Series(double[] Xs, double[] Ys, int color, CharSequence label) {		
		this.color = color;
		this.label = label;
		
		if(Xs != null) {
			int[] sorted = new Argsort(Xs).argsort();
			this.Xs = new double[Xs.length];
			this.Ys = new double[Ys.length];
			for(int i=0; i<Xs.length; i++) {
				this.Xs[i] = Xs[sorted[i]];
				this.Ys[i] = Ys[sorted[i]];
			}
		}
		else {
			this.Ys = Ys;
			this.Xs = new double[Ys.length];
			for(int i=0; i<this.Xs.length; i++) this.Xs[i] = i;
		}
		
		for(double x: this.Xs) {
			minx = Math.min(minx, x);
			maxx = Math.max(maxx, x);
		}
		
		for(double y: this.Ys) {
			miny = Math.min(miny, y);
			maxy = Math.max(maxy, y);
		}
	}
	
	public double[] getXs() {
		return Xs;
	}
	
	public double[] getYs() {
		return Xs;
	}
	
	public double getMinX() {
		return minx;
	}
	
	public double getMaxX() {
		return maxx;
	}
	
	public double getMinY() {
		return miny;
	}
	
	public double getMaxY() {
		return maxy;
	}
	
	public CharSequence getLabel() {
		return label;
	}
	
	public int getColor() {
		return color;
	}
	
	public abstract void draw(Canvas canvas, Paint paint, Rect clip, float alpha);

	private class Argsort implements Comparator<Integer> {
		private double[] toSort;
		
		public Argsort(double[] toSort) {
			this.toSort = toSort;
		}

		public int compare(Integer lhs, Integer rhs) {
			if(toSort[lhs] < toSort[rhs]) return -1;
			else if(toSort[lhs] > toSort[rhs]) return 1;
			else return 0;
		}
		
		public int[] argsort() {
			Integer[] indices = new Integer[toSort.length];
			for(int i=0; i<indices.length; i++) indices[i] = i;
			Arrays.sort(indices, this);
			int[] out = new int[indices.length];
			for(int i=0; i<indices.length; i++) out[i] = indices[i].intValue();
			return out;
		}
	}
}
