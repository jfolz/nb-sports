package com.nobullshit.grapher;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class BarSeries extends Series {
	protected RectF[] rects;
	
	public BarSeries(double[] Xs, double[] Ys, int color, CharSequence label) {		
		super(Xs, Ys, color, label);
	}
	
	public void createBars(Transform T, Rect clip, double[] allXs, int nSeries, int seriesIndex, float barSpacing) {
		if(rects == null || rects.length != Xs.length) rects = new RectF[Xs.length];
		
		float cellWidth = (float) clip.width() / allXs.length;
		float cellPadding = cellWidth / (nSeries+1) / 2;
		float barWidth = (cellWidth - 2*cellPadding - (nSeries-1)*barSpacing) / nSeries;
		
		int j=0;
		float left, top;
		double x;
		for(int i=0; i<allXs.length; i++) {
			x = allXs[i];
			if(j<Xs.length && Xs[j] == x) {
				left = clip.left
						+ cellWidth*i
						+ cellPadding
						+ barWidth*seriesIndex
						+ barSpacing*seriesIndex;
				top = (float) -T.transformY(Ys[j]) + clip.bottom;
				rects[j++] = new RectF(left, top, left+barWidth, clip.bottom);
			}
		}
	}
	
	@Override
	public void draw(Canvas canvas, Paint paint, Rect clip, float alpha) {
		if(rects != null) {
			int a = Math.round(Color.alpha(color) * alpha);
			paint.setColor((color & 0x00FFFFFF) | (a << 24));
			for(RectF rect: rects) canvas.drawRect(rect, paint);
		}
	}
}
