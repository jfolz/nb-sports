package com.nobullshit.grapher;

import android.content.Context;
import android.graphics.Canvas;
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

	@Override
	protected void drawGraph(Canvas canvas) {
		for(DataSet d: series) d.draw(canvas, graphPaint, clip, alpha);
	}

}
