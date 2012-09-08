package com.nobullshit.grapher;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.nobullshit.text.StringUtil;

public class ScatterGraph extends Graph {
	
	protected float symbolSize = 3;

	public ScatterGraph(Context context) {
		this(context, null, 0);
	}

	public ScatterGraph(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScatterGraph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		symbolSize *= ratio;
		if(attrs != null) {
			TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.ScatterGraph);
			symbolSize = arr.getDimension(R.styleable.ScatterGraph_symbolSize, symbolSize);
		}
		
		graphPaint.setStrokeJoin(Paint.Join.BEVEL);
	}

	@Override
	public void addSeries(double[] Xs, double[] Ys, int color,
			CharSequence label) {
		series.add(new ScatterSeries(Xs, Ys, color, label));
	}

	public void addSeries(double[] Xs, double[] Ys, int color,
			CharSequence label, int symbol) {
		if(color == 0) color = Colors.get(series.size());
		if(label == null) label = StringUtil.trim("Series "+(series.size()+1));
		series.add(new ScatterSeries(Xs, Ys, color, label, symbol));
	}

	@Override
	protected void createGraph() {
		for(Series d: series) ((ScatterSeries) d).createPoints(T, clip, symbolSize);
	}

}
