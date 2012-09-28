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

		if(Ys == null || Ys.length == 0 || (Xs != null && Ys.length != Xs.length)) {
			minx = 0;
			maxx = 0;
			miny = 0;
			maxy = 0;
			return;
		}
		else if(Xs != null) {
			this.Xs = Xs;
			this.Ys = Ys;
			
			for(double x: this.Xs) {
				if(x < minx) minx = x;
				if(x > maxx) maxx = x;
			}
		}
		else {
			this.Ys = Ys;
			minx = 0;
			maxx = Ys.length - 1;
		}
		
		for(double y: this.Ys) {
			if(y < miny) miny = y;
			if(y > maxy) maxy = y;
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
	
	protected interface SeriesIterator {
		
		public boolean hasNext();
		public double next();
		
	}
	
	protected class ArrayIterator implements SeriesIterator {
		
		private int index;
		private double[] arr;
		
		public ArrayIterator(double[] arr) {
			this.arr = arr;
		}

		@Override
		public boolean hasNext() {
			return index < arr.length;
		}

		@Override
		public double next() {
			return arr[index++];
		}
		
	}
	
	protected class NumberIterator implements SeriesIterator {
		
		private int next;
		int minx, maxx;
		
		public NumberIterator(int minx, int maxx) {
			this.minx = minx;
			this.maxx = maxx;
		}

		@Override
		public boolean hasNext() {
			return next < maxx;
		}

		@Override
		public double next() {
			return next++;
		}
		
	}
	
	public static class Argsort implements Comparator<Integer> {
		private double[] toSort;
		private int[] sortedIndices;
		
		public Argsort(double[] toSort) {
			this.toSort = toSort;
		}

		public int compare(Integer lhs, Integer rhs) {
			if(toSort[lhs] < toSort[rhs]) return -1;
			else if(toSort[lhs] > toSort[rhs]) return 1;
			else return 0;
		}
		
		public int[] getIndices() {
			Integer[] indices = new Integer[toSort.length];
			for(int i=0; i<indices.length; i++) indices[i] = i;
			Arrays.sort(indices, this);
			int[] out = new int[indices.length];
			for(int i=0; i<indices.length; i++) out[i] = indices[i].intValue();
			return out;
		}
		
		public double[] sort(double[] in) {
			if(sortedIndices == null) sortedIndices = getIndices();
			double[] out = new double[in.length];
			for(int i=0; i<in.length; i++) out[i] = in[sortedIndices[i]];
			return out;
		}
	}
}
