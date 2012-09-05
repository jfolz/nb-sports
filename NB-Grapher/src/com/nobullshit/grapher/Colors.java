package com.nobullshit.grapher;

import java.util.Iterator;

import android.graphics.Color;

public class Colors implements Iterator<Integer> {
	
	int[] colors = new int[] {
			Color.rgb(51, 181, 229),
			Color.rgb(170,102,204),
			Color.rgb(153,204,0),
			Color.rgb(255,187,51),
			Color.rgb(255,68,68),
			Color.rgb(0,153,204),
			Color.rgb(153,51,204),
			Color.rgb(102,153,0),
			Color.rgb(255,136,0),
			Color.rgb(204,0,0)
	};
		
	int index;
	
	public Colors() {
		index = 0;
	}

	public boolean hasNext() {
		return true;
	}

	public Integer next() {
		index = index % colors.length;
		return colors[index++];
	}

	public void remove() {}

}
