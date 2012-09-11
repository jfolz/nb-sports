package com.nobullshit.grapher;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.TypedValue;
import android.view.View;

import com.nobullshit.text.DecimalFormatter;
import com.nobullshit.text.Formatter;
import com.nobullshit.text.StringUtil;

public abstract class Graph extends View {
	// defaults
	protected static final float[] allowedFractions =
			new float[] {10, 8, 5, 4, 2, 1, 1/2F, 1/4F, 1/8F, 1/10F};
	protected static final int defaultAxisPadding = 6;
	protected static final int defaultAxisStrokeWidth = 2;
	protected static final float defaultGridStrokeWidth = 0.5F;
	protected static final float defaultGraphStrokeWidth = 2;
	protected static final int defaultLabelColor =
			Resources.getSystem().getColor(android.R.color.primary_text_dark);
	protected static final int defaultAxisColor =
			Resources.getSystem().getColor(android.R.color.holo_blue_light);
	protected static final int defaultGridColor =
			Resources.getSystem().getColor(android.R.color.holo_blue_light) & 0x80FFFFFF;
	protected static final float ratio =
			TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, Resources.getSystem().getDisplayMetrics());

	// settings
	protected boolean zeroBaseY = true;
	protected boolean drawArrows = true;
	protected boolean drawAxisY = true;
	protected boolean drawAxisX = true;
	protected boolean drawAxisAtZeroY = false;
	protected boolean drawAxisAtZeroX = false;
	protected boolean drawGridY = true;
	protected boolean drawGridX = true;
	protected boolean drawTickLabelsX = true;
	protected boolean drawTickLabelsY = true;
	protected float alpha = 1;
	
	// drawing parts
	protected Path triangleX;
	protected Path triangleY;
	protected Paint axisPaint;
	protected Paint graphPaint;
	protected Paint gridPaint;
	protected Paint labelPaint;
	
	// measurements
	protected float halfTextHeight;
	protected Rect axisPadding;
	protected Rect padding;
	protected Rect clip;
	protected double[] xTicks;
	protected double[] yTicks;
	protected String[] xTickLabels;
	protected String[] yTickLabels;
	protected float[] xTickPositions;
	protected float[] yTickPositions;
	protected Formatter xTickFormatter = null;
	protected Formatter yTickFormatter = null;
	
	// dynamic
	protected Transform T;
	protected List<Series> series;

	public Graph(Context context) {
		this(context, null, 0);
	}

	public Graph(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Graph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		axisPadding = new Rect();
		clip = new Rect();
		padding = new Rect();
		T = new Transform();
		
		series = new ArrayList<Series>();
		xTicks = new double[0];
		yTicks = new double[0];
		xTickFormatter = new DecimalFormatter("0.00");
		yTickFormatter = new DecimalFormatter("0.00");
		xTickLabels = new String[0];
		yTickLabels = new String[0];
		xTickPositions = new float[0];
		yTickPositions = new float[0];
		
		int axisStrokeWidth = Math.round(defaultAxisStrokeWidth * ratio);
		int gridStrokeWidth = Math.round(defaultGridStrokeWidth * ratio);
		float graphStrokeWidth = defaultGraphStrokeWidth * ratio;
		int axpad = Math.round(defaultAxisPadding * ratio);
		
		axisPaint = new Paint();
		axisPaint.setAntiAlias(true);
		axisPaint.setColor(defaultAxisColor);
		axisPaint.setStrokeWidth(axisStrokeWidth);
		axisPaint.setStrokeCap(Paint.Cap.ROUND);
		
		labelPaint = new Paint();
		labelPaint.setAntiAlias(true);
		labelPaint.setColor(defaultLabelColor);
		
		graphPaint = new Paint(axisPaint);
		graphPaint.setStyle(Paint.Style.STROKE);
		graphPaint.setStrokeCap(Paint.Cap.BUTT);
		graphPaint.setStrokeJoin(Paint.Join.ROUND);
		graphPaint.setStrokeWidth(graphStrokeWidth);
		
		gridPaint = new Paint();
		gridPaint.setColor(defaultGridColor);
		gridPaint.setStrokeWidth(gridStrokeWidth);
		gridPaint.setStrokeCap(Paint.Cap.BUTT);
		
		if(attrs != null) {
			TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.Graph);
			
			axisStrokeWidth = arr.getDimensionPixelSize(R.styleable.Graph_axisStrokeWidth, axisStrokeWidth);
			gridStrokeWidth = arr.getDimensionPixelSize(R.styleable.Graph_gridStrokeWidth, gridStrokeWidth);
			
			drawArrows = arr.getBoolean(R.styleable.Graph_drawArrows, drawArrows);
			
			boolean drawGrid = arr.getBoolean(R.styleable.Graph_drawGrid, drawGridX);
			drawGridX = arr.getBoolean(R.styleable.Graph_drawGridX, drawGrid);
			drawGridY = arr.getBoolean(R.styleable.Graph_drawGridY, drawGrid);
			
			boolean drawAxis = arr.getBoolean(R.styleable.Graph_drawAxis, drawAxisX);
			drawAxisX = arr.getBoolean(R.styleable.Graph_drawAxisX, drawAxis);
			drawAxisY = arr.getBoolean(R.styleable.Graph_drawAxisY, drawAxis);
			
			boolean drawAxisAtZero = arr.getBoolean(R.styleable.Graph_drawAxisAtZero, drawAxisAtZeroX);
			drawAxisAtZeroX = arr.getBoolean(R.styleable.Graph_drawAxisAtZeroX, drawAxisAtZero);
			drawAxisAtZeroY = arr.getBoolean(R.styleable.Graph_drawAxisAtZeroY, drawAxisAtZero);
			
			zeroBaseY = arr.getBoolean(R.styleable.Graph_zeroBaseY, zeroBaseY);
			axpad = arr.getDimensionPixelSize(R.styleable.Graph_axisPadding, axpad);
			axisPadding.top = arr.getDimensionPixelSize(R.styleable.Graph_axisPaddingTop, axpad);
			axisPadding.left = arr.getDimensionPixelSize(R.styleable.Graph_axisPaddingLeft, axpad);
			axisPadding.right = arr.getDimensionPixelSize(R.styleable.Graph_axisPaddingRight, axpad);
			axisPadding.bottom = arr.getDimensionPixelSize(R.styleable.Graph_axisPaddingBottom, axpad);
			
			graphPaint.setStrokeWidth(arr.getDimension(R.styleable.Graph_graphStrokeWidth, graphStrokeWidth));
			graphPaint.setAntiAlias(arr.getBoolean(R.styleable.Graph_antiAliasGraph, true));

			labelPaint.setTextSize(arr.getDimension(R.styleable.Graph_tickLabelSize, labelPaint.getTextSize()));
			labelPaint.setColor(arr.getColor(R.styleable.Graph_labelColor, defaultLabelColor));

			axisPaint.setColor(arr.getColor(R.styleable.Graph_axisColor, defaultAxisColor));
			
			gridPaint.setColor(arr.getColor(R.styleable.Graph_gridColor, defaultGridColor));
		}
		
		Rect temp = new Rect();
		labelPaint.getTextBounds("P", 0, 1, temp);
		halfTextHeight = -temp.top / 2.F;
	}
	
	public void setXTickFormatter(Formatter xf) {
		this.xTickFormatter = xf;
	}

	public void setYTickFormatter(Formatter yf) {
		this.yTickFormatter = yf;
	}
	
	public void addSeries(double[] Xs, double[] Ys) {
		addSeries(Xs, Ys, Colors.get(series.size()), StringUtil.trim("Series "+(series.size()+1)));
	}
	
	public void addSeries(double[] Xs, double[] Ys, CharSequence label) {
		addSeries(Xs, Ys, Colors.get(series.size()), label);
	}

	public abstract void addSeries(double[] Xs, double[] Ys, int color, CharSequence label);
	
	public void removeSeries(int i) {
		series.remove(i);
	}
	
	public boolean removeSeries(String label) {
		int n = series.size();
		for(int i=0; i<n; i++) {
			if(series.get(i).getLabel().equals(label)) {
				series.remove(i);
				return true;
			}
		}
		return false;
	}
	
	public int getSeriesCount() {
		return series.size();
	}
	
	public void refresh() {
		prepareForDrawing();
		invalidate();
	}
	
	protected void prepareForDrawing() {
		if(getWidth() > 0 && getHeight() > 0) {
			T.reset();
	
			calculateClipY();
			measureDataSetY();
			calculateTickLabelsY();
			
			calculateClipX();
			measureDataSetX();
			calculateTickLabelsX();

			transformTicksY();
			transformTicksX();			
			createGraph();
			createTriangles();
		}
	}
	
	protected void calculateClipY() {
		clip.top = axisPadding.top + getPaddingTop();
		clip.bottom = getHeight() - axisPadding.bottom - getPaddingBottom()
				- (int) FloatMath.ceil(labelPaint.getTextSize());
	}
	
	protected void calculateClipX() {
		clip.left = axisPadding.left + getPaddingLeft() + padding.left;
		clip.right = getWidth() - axisPadding.right - getPaddingRight();
	}
	
	protected void measureDataSetY() {
		double scaleY=1, min=0, max=0;

		float tickSize = halfTextHeight * 2 * 3;
		int maxTicks = (int) FloatMath.ceil((float) clip.height() / tickSize);
		
		if(series.size() > 0) {
			min = Double.MAX_VALUE;
			max = Double.MIN_VALUE;
			for(Series d: series) {
				min = Math.min(min, d.getMinY());
				max = Math.max(max, d.getMaxY());
			}
			if(zeroBaseY && min > 0) min = 0;
			scaleY = (double) clip.height() / (max - min);
			yTicks = calculateTickArray(min, max, maxTicks);
			
			T.sy = scaleY;
			T.ty = -min;
		}
	}
	
	protected void measureDataSetX() {
		double scaleX=1, min=0, max=0;
		
		// basing this on text height might be counter-intuitive, but this way
		// we get roughly the same spacing for both x and y ticks
		float tickSize = halfTextHeight * 2 * 3;
		int maxTicks = (int) FloatMath.ceil((float) clip.width() / tickSize);
		
		if(series.size() > 0) {
			min = Float.MAX_VALUE;
			max = Float.MIN_VALUE;
			for(Series d: series) {
				min = Math.min(min, d.getMinX());
				max = Math.max(max, d.getMaxX());
			}
			scaleX = (float) (clip.width() / (max - min));
			xTicks = calculateTickArray(min, max, maxTicks);
			
			T.tx = -min;
			T.sx = scaleX;
		}
	}
	
	protected double[] calculateTickArray(double min, double max, int maxTicks) {
		double[] ticks = null;
		double nearest, off;
		int high, low;
		for(float fraction: allowedFractions) {
			nearest = Math.pow(10,Math.floor(Math.log10(max-min)));
			nearest = nearest / (double) fraction;
			off = Math.floor(min / nearest) * nearest;
			high = (int) Math.floor((max - off) / nearest);
			low = (int) Math.ceil((min - off) / nearest);
			
			if(high-low+1 > maxTicks) continue;
			
			ticks = new double[high-low+1];
			for(int i=low; i<=high; i++) ticks[i-low] = i*nearest + off;
			break;
		}
		if(ticks == null) return new double[0];
		else return ticks;
	}

	protected void calculateTickLabelsY() {
		double val;
		String s;
		Rect temp = new Rect();
		if(yTicks.length != yTickLabels.length) yTickLabels = new String[yTicks.length];
		for(int i=0; i<yTicks.length; i++) {
			val = yTicks[i];
			s = yTickFormatter.format(val);
			labelPaint.getTextBounds(s, 0, s.length(), temp);
			padding.left = Math.max(padding.left,temp.right);
			yTickLabels[i] = s;
		}
	}

	protected void calculateTickLabelsX() {
		if(xTicks.length != xTickLabels.length) xTickLabels = new String[xTicks.length];
		for(int i=0; i<xTicks.length; i++) {
			xTickLabels[i] = xTickFormatter.format(xTicks[i]);
		}
	}
	
	protected void transformTicksY() {
		if(yTicks.length != yTickPositions.length) yTickPositions = new float[yTicks.length];
		int i=0;
		for(double y: yTicks) yTickPositions[i++] = (float) (clip.bottom - T.transformY(y));
	}
	
	protected void transformTicksX() {
		if(xTicks.length != xTickPositions.length) xTickPositions = new float[xTicks.length];
		int i=0;
		for(double x: xTicks) xTickPositions[i++] = (float) (clip.left + T.transformX(x));
	}
	
	protected void createTriangles() {
		triangleX = Symbols.triangleRight(axisPaint.getStrokeWidth()*5);
		float off = (float) (drawAxisAtZeroX ? T.transformY(0) : 0);
		triangleX.offset(clip.right, clip.bottom - off);
		
		triangleY = Symbols.triangleUp(axisPaint.getStrokeWidth()*5);
		off = (float) (drawAxisAtZeroY ? T.transformX(0) : 0);
		triangleY.offset(clip.left + off, clip.top);
	}

	protected abstract void createGraph();

	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		int w = MeasureSpec.getSize(widthMeasureSpec);
		int h = MeasureSpec.getSize(heightMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		switch(heightMode) {
			case MeasureSpec.UNSPECIFIED:
			case MeasureSpec.AT_MOST:
				h = (int) Math.round(w/4.*3.);
				break;
		}
		setMeasuredDimension(w,h);
		prepareForDrawing();
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		drawGrid(canvas);
		drawAxis(canvas);
		drawTickLabels(canvas);
		drawGraph(canvas);
	}
	
	protected void drawGrid(Canvas canvas) {
		if(drawGridX && xTicks != null) for(float x: xTickPositions) {
			canvas.drawLine(x, clip.top, x, clip.bottom, gridPaint);
		}
		if(drawGridY && yTicks != null) for(float y: yTickPositions) {
			canvas.drawLine(clip.left, y, clip.right, y, gridPaint);
		}
	}
	
	protected void drawTickLabels(Canvas canvas) {
		float x, y, next, last = Float.MIN_VALUE, width;
		int i;
		String s;
		
		if(drawTickLabelsX) {
			labelPaint.setTextAlign(Align.CENTER);
			for(i=0; i<xTicks.length; i++) {
				s = xTickLabels[i];
				x = xTickPositions[i];
				width = labelPaint.measureText(s)/2;
				next = x-width;
				if(last < next && x+width <= clip.right+axisPadding.right) {
					canvas.drawText(s, x, clip.bottom+axisPadding.bottom+2*halfTextHeight, labelPaint);
					last = x+width;
				}
			}
		}
		
		last = Float.MAX_VALUE;
		if(drawTickLabelsY) {
			labelPaint.setTextAlign(Align.RIGHT);
			for(i=0; i<yTicks.length; i++) {
				s = yTickLabels[i];
				y = yTickPositions[i];
				next = y+halfTextHeight;
				if(next < last && y-halfTextHeight >= clip.top-axisPadding.top) {
					canvas.drawText(s, clip.left-axisPadding.left, next, labelPaint);
					last = y-halfTextHeight;
				}
			}
		}
	}
	
	protected void drawAxis(Canvas canvas) {
		float off;
		if(drawAxisX) {
			off = (float) (drawAxisAtZeroX ? T.transformY(0) : 0);
			canvas.drawLine(clip.left, 
					clip.bottom - off, 
					clip.right, 
					clip.bottom - off, 
					axisPaint);
		}
		if(drawAxisY) {
			off = (float) (drawAxisAtZeroY ? T.transformX(0) : 0);
			canvas.drawLine(clip.left + off, 
					clip.bottom, 
					clip.left + off, 
					clip.top, 
					axisPaint);
		}
		if(drawArrows) {
			if(drawAxisX) canvas.drawPath(triangleX, axisPaint);
			if(drawAxisY) canvas.drawPath(triangleY, axisPaint);
		}
	}

	protected void drawGraph(Canvas canvas) {
		for(Series d: series) d.draw(canvas, graphPaint, clip, alpha);
	}

}
