package com.nobullshit.grapher;

import java.util.Arrays;
import java.util.Comparator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

public class DataSet {
	private double[] Xs;
	private double[] Ys;
	private Path graph;
	private int color;
	private CharSequence label;
	private Bounds bounds;
	private RectF[] rects;
	
	public DataSet(double[] Xs, double[] Ys, int color, CharSequence label) {		
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

	public void createGraph(Transform T) {
		graph = new Path();
		
		if(Ys.length > 0) {
			if(Xs.length == 0) {
				graph.moveTo(0, (float) -T.transformY(Ys[0]));
				float x = 0;
				for(double y: Ys) {
					graph.lineTo((float) T.transformX(x), (float) -T.transformY(y));
					x++;
				}
			}
			else {
				graph.moveTo((float) T.transformX(Xs[0]), (float) -T.transformY(Ys[0]));
				for(int i=1; i<Xs.length; i++) {
					graph.lineTo((float) T.transformX(Xs[i]), (float) -T.transformY(Ys[i]));
				}
			}
		}
	}
	
	public void createBars(Transform T, Rect clip, double[] allXs, int nSeries, int seriesIndex, float barSpacing) {
		if(rects == null || rects.length != Xs.length) rects = new RectF[Xs.length];
		
		float cellWidth = (float) clip.width() / allXs.length;
		float cellPadding = cellWidth / (nSeries+1) / 2;
		float barWidth = (cellWidth - 2*cellPadding - (nSeries-1)*barSpacing) / nSeries;
		
		int j=0;
		float left, top;
		double x;
		for(int i=0; i<allXs.length; i++) {
			x = allXs[i];
			if(j<Xs.length && Xs[j] == x) {
				left = clip.left
						+ cellWidth*i
						+ cellPadding
						+ barWidth*seriesIndex
						+ barSpacing*seriesIndex;
				top = (float) -T.transformY(Ys[j]) + clip.bottom;
				rects[j++] = new RectF(left, top, left+barWidth, clip.bottom);
			}
		}
	}
	
	public void draw(Canvas canvas, Paint paint, Rect clip, float alpha) {
		if(graph != null) {
			graph.offset(clip.left, clip.bottom);
			paint.setColor(color);
			canvas.drawPath(graph, paint);
		}
		if(rects != null) {
			int a = Math.round(Color.alpha(color) * alpha);
			paint.setColor((color & 0x00FFFFFF) | (a << 24));
			for(RectF rect: rects) canvas.drawRect(rect, paint);
		}
	}

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
