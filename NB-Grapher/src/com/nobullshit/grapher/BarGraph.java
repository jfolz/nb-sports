package com.nobullshit.grapher;

import java.util.Arrays;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

public class BarGraph extends Graph {
	
	protected double[] allXs;
	protected float barSpacing = 2;

	public BarGraph(Context context) {
		this(context, null, 0);
	}

	public BarGraph(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BarGraph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		barSpacing *= ratio;
		if(attrs != null) {
			TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.BarGraph);
			barSpacing = arr.getDimension(R.styleable.BarGraph_barSpacing, barSpacing);
		}
		
		graphPaint.setStyle(Style.FILL);
	}
	
	@Override
	public void addSeries(double[] Xs, double[] Ys, int color, CharSequence label) {
		double[] nxs, temp;
		// copy supplied Xs and sort them or create dummies
		if(Xs == null) {
			nxs = new double[Ys.length];
			for(int i=0; i<nxs.length; i++) nxs[i] = i;
		}
		else {
			nxs = new double[Xs.length];
			nxs = Arrays.copyOf(Xs, Xs.length);
			Arrays.sort(nxs);
		}
		
		if(allXs == null) {
			allXs = nxs;
		}
		else {
			// merge old Xs and new values, remove duplicates
			int n=0, i=0, j=0;
			temp = new double[allXs.length + nxs.length];
			while(true) {
				if(allXs[i] < nxs[j]) temp[n++] = allXs[i++];
				else if(allXs[i] > nxs[j]) temp[n++] = nxs[j++];
				else {
					temp[n++] = allXs[i++];
					j++;
				}
				
				if(i == allXs.length || j == nxs.length || n == temp.length) break;
			}
			for(; i<allXs.length; i++) temp[n++] = allXs[i];
			for(; j<nxs.length; j++) temp[n++] = nxs[j];
			
			// trim temporary array to size and save
			allXs = new double[n];
			System.arraycopy(temp, 0, allXs, 0, n);
		}
		// proceed with normal operation
		super.addSeries(Xs, Ys, color, label);
	}
	
	@Override
	protected void measureDataSetX() {
		int n = series.size();
		if(allXs != null && n > 0) {
			xTicks = allXs;
		}
	}
	
	@Override
	protected void transformTicksX() {
		int n = series.size();
		if(n > 0) {
			if(xTicks.length != xTickPositions.length) xTickPositions = new float[xTicks.length];
			float cellWidth = (float) clip.width() / allXs.length;
			for(int i=0; i<xTicks.length; i++)
				xTickPositions[i] = clip.left + (float) (cellWidth * (i + .5));
		}
	}
	
	@Override
	protected void createGraphPath() {
		int n = series.size();
		if(n > 0 && allXs != null) {
			for(int i=0; i<n; i++) series.get(i).createBars(T, clip, allXs, n, i, barSpacing);
		}
	}

	@Override
	protected void drawGraph(Canvas canvas) {
		for(DataSet d: series) d.draw(canvas, graphPaint, clip, alpha);
	}

}