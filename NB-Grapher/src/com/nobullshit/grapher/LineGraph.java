package com.nobullshit.grapher;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

public class LineGraph extends Graph {
	
	protected float graphStrokeWidth = 2;

	public LineGraph(Context context) {
		this(context, null, 0);
	}

	public LineGraph(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LineGraph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		graphStrokeWidth *= ratio;
		if(attrs != null) {
			TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.Graph);
			graphPaint.setStrokeWidth(arr.getDimension(R.styleable.LineGraph_graphStrokeWidth, graphStrokeWidth));
		}
	}
	
	public void addSeries(double[] Xs, double[] Ys, int color, CharSequence label) {
		series.add(new LineSeries(Xs, Ys, color, label));
	}
	
	@Override
	protected void createGraph() {
		for(Series d: series) ((LineSeries) d).createGraph(T, clip);
	}

}
