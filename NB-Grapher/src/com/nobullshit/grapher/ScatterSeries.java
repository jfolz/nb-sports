package com.nobullshit.grapher;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

public class ScatterSeries extends Series {
	
	protected int symbol = 0;
	protected Path graph;

	public ScatterSeries(double[] Xs, double[] Ys, int color, CharSequence label) {
		super(Xs, Ys, color, label);
	}

	public ScatterSeries(double[] Xs, double[] Ys, int color, CharSequence label, int symbol) {
		super(Xs, Ys, color, label);
		this.symbol = symbol;
	}

	public void createPoints(Transform T, Rect clip, float pointSize) {
		if(Ys == null) return;
		else if(graph == null) graph = new Path();
		else graph.reset();
		
		Path s;
		switch(symbol) {
		case Symbols.SYMBOL_CROSS: s = Symbols.cross(pointSize); break;
		case Symbols.SYMBOL_CIRCLE:
		case Symbols.SYMBOL_DOT: s = Symbols.circle(pointSize); break;
		case Symbols.SYMBOL_SQUARE:
		case Symbols.SYMBOL_BOX: s = Symbols.square(pointSize); break;
		case Symbols.SYMBOL_TRIANGLE:
		case Symbols.SYMBOL_FILLTRIANGLE: s = Symbols.triangleUp(pointSize); break;
		case Symbols.SYMBOL_PLUS:
		default: s = Symbols.plus(pointSize); break;
		}

		for(int i=0; i<Xs.length; i++)
			graph.addPath(s, (float) T.transformX(Xs[i]), (float) -T.transformY(Ys[i]));

		graph.offset(clip.left, clip.bottom);
	}

	@Override
	public void draw(Canvas canvas, Paint paint, Rect clip, float alpha) {
		if(graph != null) {
			switch(symbol) {
				case Symbols.SYMBOL_CIRCLE:
				case Symbols.SYMBOL_SQUARE:
				case Symbols.SYMBOL_TRIANGLE: paint.setStyle(Paint.Style.STROKE); break;
				case Symbols.SYMBOL_DOT:
				case Symbols.SYMBOL_BOX:
				case Symbols.SYMBOL_FILLTRIANGLE: paint.setStyle(Paint.Style.STROKE); break;
			}
			int a = Math.round(Color.alpha(color) * alpha);
			paint.setColor((color & 0x00FFFFFF) | (a << 24));
			canvas.drawPath(graph, paint);
		}
	}

}
