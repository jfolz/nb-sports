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
	protected Bounds bounds;
	
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

		this.bounds = new Bounds(Double.MAX_VALUE,Double.MIN_VALUE,
				Double.MAX_VALUE,Double.MIN_VALUE);
		
		for(double x: this.Xs) {
			bounds.minx = Math.min(bounds.minx, x);
			bounds.maxx = Math.max(bounds.maxx, x);
		}
		
		for(double y: this.Ys) {
			bounds.miny = Math.min(bounds.miny, y);
			bounds.maxy = Math.max(bounds.maxy, y);
		}
	}
	
	public double[] getXs() {
		return Xs;
	}
	
	public double[] getYs() {
		return Xs;
	}
	
	public Bounds getBounds() {
		return bounds;
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
