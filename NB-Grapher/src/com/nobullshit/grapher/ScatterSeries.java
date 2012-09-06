package com.nobullshit.grapher;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

public class ScatterSeries extends Series {
	
	protected Path graph;

	public ScatterSeries(double[] Xs, double[] Ys, int color, CharSequence label) {
		super(Xs, Ys, color, label);
	}

	public void createPoints(Transform T, Rect clip, float pointSize) {
		if(Ys.length > 0) {
			if(graph == null) graph = new Path();
			else graph.reset();
			
			float radius = pointSize / 2.F;
			for(int i=0; i<Xs.length; i++)
				graph.addCircle((float) T.transformX(Xs[i]), (float) -T.transformY(Ys[i]), 
						radius, Path.Direction.CW);
			
			graph.offset(clip.left, clip.bottom);
		}
	}

	@Override
	public void draw(Canvas canvas, Paint paint, Rect clip, float alpha) {
		if(graph != null) {
			int a = Math.round(Color.alpha(color) * alpha);
			paint.setColor((color & 0x00FFFFFF) | (a << 24));
			canvas.drawPath(graph, paint);
		}
	}

}
