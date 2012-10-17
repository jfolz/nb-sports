package com.nobullshit.stopwatch.tools;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.nobullshit.stopwatch.R;

public class SimpleNumberPicker extends LinearLayout
		implements OnClickListener, TextWatcher {
	
	private int value;
	private int minValue = 0;
	private int maxValue = 1;
	private EditText picker;
	private boolean wrap = true;
	private int maxLength;

	public SimpleNumberPicker(Context context) {
		this(context, null, 0);
	}

	public SimpleNumberPicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SimpleNumberPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		setOrientation(LinearLayout.VERTICAL);
		
		if(attrs != null) {
			TypedArray arr = context.obtainStyledAttributes(
					attrs, R.styleable.SimpleNumberPicker);

			minValue = arr.getInt(R.styleable.SimpleNumberPicker_minValue, minValue);
			maxValue = arr.getInt(R.styleable.SimpleNumberPicker_maxValue, maxValue);
			value = arr.getInt(R.styleable.SimpleNumberPicker_value, minValue);
		}
		calculateMaxlength();
	}
 
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		inflate(getContext(), R.layout.simple_number_picker, this);

		this.findViewById(R.id.picker_inc).setOnClickListener(this);
		this.findViewById(R.id.picker_dec).setOnClickListener(this);
		picker = (EditText) this.findViewById(R.id.picker);
		picker.addTextChangedListener(this);
		
		setValue(value);
	}
	
	public void setMinValue(int minValue) {
		this.minValue = minValue;
		if(minValue > value) setValue(minValue);
		calculateMaxlength();
	}
	
	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
		if(maxValue < value) setValue(maxValue);
		calculateMaxlength();
	}
	
	public void setValue(int value) {
		if(value < minValue) this.value = wrap ? maxValue : minValue;
		else if(value > maxValue) this.value = wrap ? minValue : maxValue;
		else this.value = value;
		
		picker.setText(this.value+"");
	}
	
	public void setValueFromInput(int value) {
		if(value < minValue) this.value = minValue;
		else if(value > maxValue) this.value = maxValue;
		else this.value = value;
		
		picker.setText(this.value+"");
	}
	
	private void calculateMaxlength() {
		int lenPos = 1, lenNeg = 1;
		
		if(maxValue > 0) lenPos = (int) Math.floor(Math.log10((double) maxValue)) + 1;
		if(minValue > 0) lenNeg = (int) Math.floor(Math.log10((double) minValue)) + 1;
		if(minValue < 0) lenNeg = (int) Math.floor(Math.log10((double) -minValue)) + 2;
		
		maxLength = lenPos > lenNeg ? lenPos : lenNeg;
	}
	
	public int getValue() {
		return value;
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.picker_inc:
			setValue(value+1);
			break;
		case R.id.picker_dec:
			setValue(value-1);
			break;
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		int pos = picker.getSelectionStart();
		int l = s.length();
		if(l > maxLength) {
			s.delete(pos, l);
			if(pos - maxLength > 0) s.delete(0, pos - maxLength);
		}
		try { 
			int newValue = Integer.parseInt(s.toString(),10);
			if(newValue != value) setValueFromInput(newValue);
		} catch(NumberFormatException e) {
			setValue(value);
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {}

}
