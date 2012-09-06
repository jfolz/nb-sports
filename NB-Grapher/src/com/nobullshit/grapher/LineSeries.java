package com.nobullshit.grapher;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

public class LineSeries extends Series {

	private Path graph;
	
	public LineSeries(double[] Xs, double[] Ys, int color, CharSequence label) {
		super(Xs, Ys, color, label);
	}

	public void createGraph(Transform T, Rect clip) {
		if(Ys.length > 0) {
			graph = new Path();
			graph.moveTo((float) T.transformX(Xs[0]), (float) -T.transformY(Ys[0]));
			for(int i=1; i<Xs.length; i++)
				graph.lineTo((float) T.transformX(Xs[i]), (float) -T.transformY(Ys[i]));
			graph.offset(clip.left, clip.bottom);
		}
	}
	
	public void draw(Canvas canvas, Paint paint, Rect clip, float alpha) {
		if(graph != null) {
			paint.setColor(color);
			canvas.drawPath(graph, paint);
		}
	}
}
