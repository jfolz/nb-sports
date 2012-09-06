package com.nobullshit.grapher;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

public class ScatterGraph extends Graph {
	
	protected float pointSize = 3;

	public ScatterGraph(Context context) {
		this(context, null, 0);
	}

	public ScatterGraph(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScatterGraph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		pointSize *= ratio;
		if(attrs != null) {
			TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.ScatterGraph);
			pointSize = arr.getDimension(R.styleable.BarGraph_barSpacing, pointSize);
		}
		
		graphPaint.setStyle(Style.FILL);
	}

	@Override
	public void addSeries(double[] Xs, double[] Ys, int color,
			CharSequence label) {
		series.add(new ScatterSeries(Xs, Ys, color, label));
	}

	@Override
	protected void createGraph() {
		for(Series d: series) ((ScatterSeries) d).createPoints(T, clip, pointSize);
	}

}
