package com.nobullshit.grapher;

import java.util.Arrays;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

public class BarGraph extends Graph {
	
	protected double[] allXs;
	protected float cellPadding;
	protected float barSpacing = 2;
	protected float cellWidth;
	protected float barWidth;
	protected float alpha;

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
		alpha = graphPaint.getAlpha() / 255.0F;
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
			cellWidth = (float) clip.width() / allXs.length;
			cellPadding = cellWidth / (n+1) / 2;
			barWidth = (cellWidth - 2*cellPadding - (n-1)*barSpacing) / n;
			xTicks = allXs;
		}
	}
	
	@Override
	protected void transformTicksX() {		
		if(xTicks.length != xTickPositions.length) xTickPositions = new float[xTicks.length];
		for(int i=0; i<xTicks.length; i++)
			xTickPositions[i] = clip.left + (float) (cellWidth * (i + .5));
	}
	
	@Override
	protected void createGraphPath() {}

	@Override
	protected void drawGraph(Canvas canvas) {
		int n = series.size();
		if(n > 0 && allXs != null) {
			int i, ix;
			double x;
			float left, right, top, bottom = clip.bottom;
			DataSet d;
			for(ix=0; ix<allXs.length; ix++) {
				x = allXs[ix];
				for(i=0; i<n; i++) {
					d = series.get(i);
					if(d.nextX() == x) {
						left = clip.left + cellWidth*ix + cellPadding + barWidth*i + barSpacing*i;
						right = left + barWidth;
						top = (float) -T.transformY(d.nextY()) + clip.bottom;
						graphPaint.setColor(d.getColor());
						graphPaint.setAlpha(Math.round(graphPaint.getAlpha() * alpha));
						canvas.drawRect(left, top, right, bottom, graphPaint);
					}
				}
			}
			for(i=0; i<n; i++) series.get(i).resetDrawing();
		}
	}

}
