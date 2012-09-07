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
				top = (float) -T.transformY(Ys[j++]) + clip.bottom;
				rects.addRect(left, top, left+barWidth, clip.bottom,Path.Direction.CW);
				rects.close();
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
	
	public void draw(Canvas canvas, Paint paint, Rect clip, Paint.Style style, float alpha, float fillAlpha) {
		if(rects != null) {	
			int a;
			
			if(style == Paint.Style.FILL || style == Paint.Style.FILL_AND_STROKE) {
				a = Math.round(Color.alpha(color) * fillAlpha);
				paint.setColor((color & 0x00FFFFFF) | (a << 24));
				paint.setStyle(Style.FILL);
				canvas.drawPath(rects, paint);
			}

			if(style == Paint.Style.STROKE || style == Paint.Style.FILL_AND_STROKE) {
				a = Math.round(Color.alpha(color) * alpha);
				paint.setColor((color & 0x00FFFFFF) | (a << 24));
				paint.setStyle(Style.STROKE);
				canvas.drawPath(rects, paint);
			}
		}
	}
}
