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
	protected static int[] allowedFractions = new int[] {1,2,4,5,8,10};
	protected static int defaultAxisPadding = 6;
	protected static int defaultAxisStrokeWidth = 2;
	protected static float defaultGridStrokeWidth = 0.5F;
	
	protected boolean zeroBaseY = true;
	protected boolean drawArrows = true;
	protected boolean drawAxisY = true;
	protected boolean drawAxisX = true;
	protected boolean drawGridY = true;
	protected boolean drawGridX = true;
	protected boolean drawTickLabelsX = true;
	protected boolean drawTickLabelsY = true;
	protected double[] xTicks;
	protected double[] yTicks;
	protected String[] xTickLabels;
	protected String[] yTickLabels;
	protected float[] xTickPositions;
	protected float[] yTickPositions;
	protected float alpha = 1;

	protected Formatter xTickFormatter = null;
	protected Formatter yTickFormatter = null;
	
	// drawing parts
	protected Path triangleX;
	protected Path triangleY;
	protected Paint axisPaint;
	protected Paint graphPaint;
	protected Paint gridPaint;
	protected Paint labelPaint;
	
	// measurements
	protected float ratio;
	protected float halfTextHeight;
	protected Rect axisPadding;
	protected Rect padding;
	protected Rect clip;
	
	// dynamic
	protected Transform T;
	protected List<DataSet> series;

	public Graph(Context context) {
		this(context, null, 0);
	}

	public Graph(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Graph(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		System.out.println(isHardwareAccelerated());
		System.out.println(getLayerType());
		
		axisPadding = new Rect();
		clip = new Rect();
		padding = new Rect();
		T = new Transform();
		
		series = new ArrayList<DataSet>();
		xTicks = new double[0];
		yTicks = new double[0];
		xTickFormatter = new DecimalFormatter("0.00");
		yTickFormatter = new DecimalFormatter("0.00");
		xTickLabels = new String[0];
		yTickLabels = new String[0];
		xTickPositions = new float[0];
		yTickPositions = new float[0];
		
		Resources r = getResources();
		ratio = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, r.getDisplayMetrics());
		int axisStrokeWidth = Math.round(defaultAxisStrokeWidth * ratio);
		int gridStrokeWidth = Math.round(defaultGridStrokeWidth * ratio);
		int axpad = Math.round(defaultAxisPadding * ratio);
		
		axisPaint = new Paint();
		axisPaint.setAntiAlias(true);
		axisPaint.setARGB(255, 51, 181, 229);
		axisPaint.setStrokeWidth(axisStrokeWidth);
		axisPaint.setStrokeCap(Paint.Cap.ROUND);
		
		labelPaint = new Paint();
		labelPaint.setAntiAlias(true);
		labelPaint.setARGB(255, 255, 255, 255);
		
		graphPaint = new Paint(axisPaint);
		graphPaint.setStyle(Paint.Style.STROKE);
		graphPaint.setStrokeCap(Paint.Cap.BUTT);
		graphPaint.setStrokeJoin(Paint.Join.ROUND);
		
		gridPaint = new Paint();
		gridPaint.setARGB(128, 51, 181, 229);
		gridPaint.setStrokeWidth(gridStrokeWidth);
		gridPaint.setStrokeCap(Paint.Cap.BUTT);

		float depth = Math.round(4.5*ratio);
		float girth = Math.round(5*ratio);
		triangleX = new Path();
		triangleX.moveTo(-depth, -girth);
		triangleX.lineTo(-depth, girth);
		triangleX.lineTo(depth, 0);
		triangleX.lineTo(-depth, -girth);
		
		triangleY = new Path();
		triangleY.moveTo(-girth, depth);
		triangleY.lineTo(girth, depth);
		triangleY.lineTo(0, -depth);
		triangleY.lineTo(-girth, depth);
		
		if(attrs != null) {
			TypedArray arr = context.obtainStyledAttributes(attrs, R.styleable.Graph);
			
			axisStrokeWidth = arr.getDimensionPixelSize(R.styleable.Graph_axisStrokeWidth, axisStrokeWidth);
			gridStrokeWidth = arr.getDimensionPixelSize(R.styleable.Graph_gridStrokeWidth, gridStrokeWidth);
			
			drawArrows = arr.getBoolean(R.styleable.Graph_drawArrows, drawArrows);
			
			boolean drawGrid = arr.getBoolean(R.styleable.Graph_drawGrid, drawAxisX);
			drawGridX = arr.getBoolean(R.styleable.Graph_drawGridX, drawGrid);
			drawGridY = arr.getBoolean(R.styleable.Graph_drawGridY, drawGrid);
			
			boolean drawAxis = arr.getBoolean(R.styleable.Graph_drawAxis, drawAxisX);
			drawAxisX = arr.getBoolean(R.styleable.Graph_drawAxisX, drawAxis);
			drawAxisY = arr.getBoolean(R.styleable.Graph_drawAxisY, drawAxis);
			
			zeroBaseY = arr.getBoolean(R.styleable.Graph_zeroBaseY, zeroBaseY);
			axpad = arr.getDimensionPixelSize(R.styleable.Graph_axisPadding, axpad);
			axisPadding.top = arr.getDimensionPixelSize(R.styleable.Graph_axisPaddingTop, axpad);
			axisPadding.left = arr.getDimensionPixelSize(R.styleable.Graph_axisPaddingLeft, axpad);
			axisPadding.right = arr.getDimensionPixelSize(R.styleable.Graph_axisPaddingRight, axpad);
			axisPadding.bottom = arr.getDimensionPixelSize(R.styleable.Graph_axisPaddingBottom, axpad);
		
			labelPaint.setTextSize(arr.getDimension(R.styleable.Graph_tickLabelSize, labelPaint.getTextSize()));
			alpha = arr.getFloat(R.styleable.Graph_graphOpacity, alpha);
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

	public void addSeries(double[] Xs, double[] Ys, int color, CharSequence label) {
		if(Ys == null)
			throw new IllegalArgumentException("Ys must not be null");
		if(Xs != null && Xs.length != Ys.length)
			throw new IllegalArgumentException("Xs and Ys must be same length");
		
		series.add(new DataSet(Xs,Ys,color,label));
	}
	
	public void refresh() {
		prepareForDrawing();
		invalidate();
	}
	
	protected void prepareForDrawing() {
		if(getWidth() > 0 && getHeight() > 0) {
			T.reset();
			
			clip.top = axisPadding.top + getPaddingTop();
			clip.bottom = getHeight() - axisPadding.bottom - getPaddingBottom()
					- (int) FloatMath.ceil(labelPaint.getTextSize());
	
			measureDataSetY();
			calculateTickLabelsY();
			
			clip.left = axisPadding.left + getPaddingLeft() + padding.left;
			clip.right = getWidth() - axisPadding.right - getPaddingRight();
			
			measureDataSetX();
			calculateTickLabelsX();

			transformTicksY();
			transformTicksX();			
			createGraphPath();
		}
	}
	
	protected void measureDataSetY() {
		double scaleY=1, min=0, max=0;
		int numYTicks = 4; //TODO calculate number of ticks based on screen space
		
		if(series.size() > 0) {
			min = Float.MAX_VALUE;
			max = Float.MIN_VALUE;
			for(DataSet d: series) {
				Bounds b = d.getBounds();
				min = Math.min(min, b.miny);
				max = Math.max(max, b.maxy);
			}
			if(zeroBaseY && min > 0) min = 0;
			scaleY = (float) (clip.height() / (max - min));
			yTicks = calculateTickArray(min, max, numYTicks);
			
			T.sy = scaleY;
			T.ty = -min;
		}
	}
	
	protected void measureDataSetX() {
		double scaleX=1, min=0, max=0;
		int numXTicks = 5; //TODO calculate number of ticks based on screen space
		
		if(series.size() > 0) {
			min = Float.MAX_VALUE;
			max = Float.MIN_VALUE;
			for(DataSet d: series) {
				Bounds b = d.getBounds();
				min = Math.min(min, b.minx);
				max = Math.max(max, b.maxx);
			}
			scaleX = (float) (clip.width() / (max - min));
			xTicks = calculateTickArray(min, max, numXTicks);
			
			T.tx = -min;
			T.sx = scaleX;
		}
	}
	
	protected double[] calculateTickArray(double min, double max, int numticks) {
		double[] ticks = null;
		double nearest, off;
		int high, low;
		for(int fraction: allowedFractions) {
			nearest = Math.pow(10,Math.floor(Math.log10(max-min)));
			nearest = nearest / fraction;
			off = Math.floor(min / nearest) * nearest;
			high = (int) Math.floor((max - off) / nearest);
			low = (int) Math.ceil((min - off) / nearest);
			
			if(high-low+1 < numticks) continue;
			
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

	protected void createGraphPath() {
		for(DataSet d: series) d.createGraph(T);
	}

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
		float x, y;
		int i;
		String s;
		if(drawTickLabelsX) {
			labelPaint.setTextAlign(Align.CENTER);
			for(i=0; i<xTicks.length; i++) {
				s = xTickLabels[i];
				x = xTickPositions[i];
				if(x+labelPaint.measureText(s)/2 <= clip.right+axisPadding.right)
					canvas.drawText(s, x, clip.bottom+axisPadding.bottom+2*halfTextHeight, labelPaint);
			}
		}
		if(drawTickLabelsY) {
			labelPaint.setTextAlign(Align.RIGHT);
			for(i=0; i<yTicks.length; i++) {
				s = yTickLabels[i];
				y = yTickPositions[i];
				if(y-halfTextHeight >= clip.top-axisPadding.top)
					canvas.drawText(s, clip.left-axisPadding.left, y+halfTextHeight, labelPaint);
			}
		}
	}
	
	protected void drawAxis(Canvas canvas) {
		if(drawAxisX) canvas.drawLine(
				clip.left, clip.bottom, clip.right, clip.bottom, axisPaint);
		if(drawAxisY) canvas.drawLine(
				clip.left, clip.bottom, clip.left, clip.top, axisPaint);
		if(drawArrows) {
			if(drawAxisX) {
				triangleX.offset(clip.right, clip.bottom);
				canvas.drawPath(triangleX, axisPaint);
			}
			if(drawAxisY) {
				triangleY.offset(clip.left, clip.top);
				canvas.drawPath(triangleY, axisPaint);
			}
		}
	}

	protected abstract void drawGraph(Canvas canvas);

}
