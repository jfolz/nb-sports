package com.nobullshit.grapher;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

public class BarGraph extends Graph {

	public BarGraph(Context context) {
		this(context, null, 0);
	}

	public BarGraph(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public BarGraph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	protected void measureDataSetX() {
		if(series.size() > 0) {
			int n = series.get(0).getYs().length;
			float delta = (float) clip.width() / n;
			xTicks = new double[n];
			for(int i=1; i<xTicks.length; i++) xTicks[i] = delta * i + delta/2;
		}
	}

	@Override
	protected void drawGraph(Canvas canvas) {
		for(DataSet d: series) d.draw(canvas, graphPaint, clip);
	}

}
