package com.nobullshit.grapher;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

public class LineSeries extends Series {

	private boolean subsample = true;
	private Path graph;
	
	public LineSeries(double[] Xs, double[] Ys, int color, CharSequence label) {
		super(Xs, Ys, color, label);
	}
	
	public void createGraph(Transform T, Rect clip) {
		if(Ys == null) return;
		else if(graph == null) graph = new Path();
		else graph.reset();
		
		double m = clip.width();
		int samplerate;
		if(subsample && Ys.length > m) samplerate = (int) Math.floor(Ys.length / m);
		else samplerate = 1;

		SeriesIterator iX;
		if(Xs != null) iX = new ArrayIterator(Xs);
		else iX = new NumberIterator(0,Ys.length);
		
		float x=0, y=0, s=(float) samplerate;
		int i = 0;
		for(; i<samplerate; i++) {
			x += T.transformX(iX.next());
			y -= T.transformY(Ys[i]);
		}
		graph.moveTo(x/s,y/s);
		x=0;
		y=0;
		
		for(; i<Ys.length; i++) {
			x += T.transformX(iX.next());
			y -= T.transformY(Ys[i]);
			if(i % samplerate == 0) {
				graph.lineTo(x/s,y/s);
				x=0;
				y=0;
			}
		}
		
		graph.offset(clip.left, clip.bottom);
	}
	
	public void draw(Canvas canvas, Paint paint, Rect clip, float alpha) {
		if(graph != null) {
			int a = Math.round(Color.alpha(color) * alpha);
			paint.setColor((color & 0x00FFFFFF) | (a << 24));
			canvas.drawPath(graph, paint);
		}
	}
}
