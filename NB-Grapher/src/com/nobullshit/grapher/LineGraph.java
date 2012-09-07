package com.nobullshit.grapher;

import android.content.Context;
import android.util.AttributeSet;

public class LineGraph extends Graph {

	public LineGraph(Context context) {
		this(context, null, 0);
	}

	public LineGraph(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LineGraph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void addSeries(double[] Xs, double[] Ys, int color, CharSequence label) {
		series.add(new LineSeries(Xs, Ys, color, label));
	}
	
	@Override
	protected void createGraph() {
		for(Series d: series) ((LineSeries) d).createGraph(T, clip);
	}

}
