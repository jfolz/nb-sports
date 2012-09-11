package com.nobullshit.grapher;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;

public class BarSeries extends Series {
	protected Path rects;
	
	public BarSeries(double[] Xs, double[] Ys, int color, CharSequence label) {		
		super(Xs, Ys, color, label);
	}
	
	public void createBars(Transform T, Rect clip, double[] allXs, int nSeries, int seriesIndex, float barSpacing) {
		if(rects == null) rects = new Path();
		else rects.reset();
		
		if(Ys.length == 0) return;
		
		float cellWidth = (float) clip.width() / allXs.length;
		float cellPadding = cellWidth / (nSeries+1) / 2;
		float barWidth = (cellWidth - 2*cellPadding - (nSeries-1)*barSpacing) / nSeries;
		
		int j=0;
		float left, top, bottom, temp;
		SeriesIterator iX;
		if(Xs == null) iX = new NumberIterator(0,Ys.length);
		else iX = new ArrayIterator(Xs);
		double ax, x = iX.next();
		
		for(int i=0; i<allXs.length; i++) {
			ax = allXs[i];
			if(x == ax) {
				left = clip.left
						+ cellWidth*i
						+ cellPadding
						+ barWidth*seriesIndex
						+ barSpacing*seriesIndex;
				top = (float) -T.transformY(Ys[j++]) + clip.bottom;
				bottom = (float) -T.transformY(0) + clip.bottom;
				if(bottom < top) {
					temp = top;
					top = bottom;
					bottom = temp;
				}
				rects.addRect(left, top, left+barWidth, bottom, Path.Direction.CW);
				rects.close();

				if(!iX.hasNext()) break;
				else x = iX.next();
			}
		}
	}
	
	@Override
	public void draw(Canvas canvas, Paint paint, Rect clip, float alpha) {
		if(rects != null) {
			int a = Math.round(Color.alpha(color) * alpha);
			paint.setColor((color & 0x00FFFFFF) | (a << 24));
			paint.setStyle(Style.FILL);
			canvas.drawPath(rects, paint);
		}
	}
}
