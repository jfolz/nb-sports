package com.nobullshit.grapher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public abstract class Graph extends View {
	protected static int[] allowedFractions = new int[] {1,2,4,5,8,10};
	protected static int defaultAxisPadding = 6;
	protected static int defaultAxisStrokeWidth = 2;
	protected static float defaultGridStrokeWidth = 0.5F;
	
	protected boolean zeroBaseY = true;
	protected boolean drawArrows = true;
	protected boolean drawGridY = true;
	protected boolean drawGridX = true;
	protected boolean drawTickLabelsX = true;
	protected boolean drawTickLabelsY = true;
	protected double[] xTicks;
	protected double[] yTicks;
	protected Map<Double,String> xTickLabels;
	protected Map<Double,String> yTickLabels;
	protected Map<Double,Float> xTickPositions;
	protected Map<Double,Float> yTickPositions;

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
	protected Colors colors;
	protected List<DataSet> series;

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
		colors = new Colors();
		
		series = new ArrayList<DataSet>();
		xTicks = new double[0];
		yTicks = new double[0];
		xTickFormatter = new DecimalFormatter("0.00");
		yTickFormatter = new DecimalFormatter("0.00");
		xTickLabels = new HashMap<Double, String>();
		yTickLabels = new HashMap<Double, String>();
		xTickPositions = new HashMap<Double, Float>();
		yTickPositions = new HashMap<Double, Float>();
		
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
			
			boolean drawGrid = arr.getBoolean(R.styleable.Graph_drawGrid, true);
			drawGridX = arr.getBoolean(R.styleable.Graph_drawGridX, drawGrid);
			drawGridY = arr.getBoolean(R.styleable.Graph_drawGridY, drawGrid);
			
			zeroBaseY = arr.getBoolean(R.styleable.Graph_zeroBaseY, zeroBaseY);
			axpad = arr.getDimensionPixelSize(R.styleable.Graph_axisPadding, axpad);
			axisPadding.top = arr.getDimensionPixelSize(R.styleable.Graph_axisPaddingTop, axpad);
			axisPadding.left = arr.getDimensionPixelSize(R.styleable.Graph_axisPaddingLeft, axpad);
			axisPadding.right = arr.getDimensionPixelSize(R.styleable.Graph_axisPaddingRight, axpad);
			axisPadding.bottom = arr.getDimensionPixelSize(R.styleable.Graph_axisPaddingBottom, axpad);
		
			labelPaint.setTextSize(arr.getDimension(R.styleable.Graph_tickLabelSize, labelPaint.getTextSize()));
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
		addSeries(Xs, Ys, colors.next(), "Series "+(series.size()+1));
	}
	
	public void addSeries(double[] Xs, double[] Ys, CharSequence label) {
		addSeries(Xs, Ys, colors.next(), label);
	}

	public void addSeries(double[] Xs, double[] Ys, int color, CharSequence label) {
		series.add(new DataSet(Xs,Ys,color,label));
		prepareForDrawing();
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
			
			transformTicks();			
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
		yTickLabels.clear();
		for(int i=0; i<yTicks.length; i++) {
			val = yTicks[i];
			s = yTickFormatter.format(val);
			labelPaint.getTextBounds(s, 0, s.length(), temp);
			padding.left = Math.max(padding.left,temp.right);
			yTickLabels.put(val, s);
			yTicks[i] = val;
		}
	}

	protected void calculateTickLabelsX() {
		xTickLabels.clear();
		for(int i=0; i<xTicks.length; i++) {
			xTickLabels.put(xTicks[i], xTickFormatter.format(xTicks[i]));
		}
	}
	
	protected void transformTicks() {		
		xTickPositions.clear();
		yTickPositions.clear();

		for(double x: xTicks) xTickPositions.put(x, (float) (T.transformX(x) + clip.left));
		for(double y: yTicks) yTickPositions.put(y, (float) (clip.bottom - T.transformY(y)));
		
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
		float x, y;
		if(drawGridX && xTicks != null) for(double xt: xTicks) {
			x = xTickPositions.get(xt);
			canvas.drawLine(x, clip.top, x, clip.bottom, gridPaint);
		}
		if(drawGridY && yTicks != null) for(double yt: yTicks) {
			y = yTickPositions.get(yt);
			canvas.drawLine(clip.left, y, clip.right, y, gridPaint);
		}
	}
	
	protected void drawTickLabels(Canvas canvas) {
		float x, y;
		String s;
		if(drawTickLabelsX) {
			labelPaint.setTextAlign(Align.CENTER);
			for(double xt: xTicks) {
				s = xTickLabels.get(xt);
				x = xTickPositions.get(xt);
				if(x+labelPaint.measureText(s)/2 <= clip.right+axisPadding.right)
					canvas.drawText(s, x, clip.bottom+axisPadding.bottom+2*halfTextHeight, labelPaint);
			}
		}
		if(drawTickLabelsY) {
			labelPaint.setTextAlign(Align.RIGHT);
			for(double yt: yTicks) {
				s = yTickLabels.get(yt);
				y = yTickPositions.get(yt);
				if(y-halfTextHeight >= clip.top-axisPadding.top)
					canvas.drawText(s, clip.left-axisPadding.left, y+halfTextHeight, labelPaint);
			}
		}
	}
	
	protected void drawAxis(Canvas canvas) {
		canvas.drawLine(clip.left, clip.bottom,
				clip.right, clip.bottom, axisPaint);
		canvas.drawLine(clip.left, clip.bottom,
				clip.left, clip.top, axisPaint);
	
		if(drawArrows) {
			triangleX.offset(clip.right, clip.bottom);
			canvas.drawPath(triangleX, axisPaint);
		
			triangleY.offset(clip.left, clip.top);
			canvas.drawPath(triangleY, axisPaint);
		}
	}

	protected abstract void drawGraph(Canvas canvas);

}
