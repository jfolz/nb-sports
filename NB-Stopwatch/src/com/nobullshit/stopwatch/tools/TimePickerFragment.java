package com.nobullshit.stopwatch.tools;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.NumberPicker;

import com.nobullshit.stopwatch.R;

public class TimePickerFragment extends DialogFragment
		implements OnClickListener {
	
	public static final String KEY_SELECTED_TIME = "SELECTED_TIME";
	public static final int BUTTON_POSITIVE = AlertDialog.BUTTON_POSITIVE;
	public static final int BUTTON_NEGATIVE = AlertDialog.BUTTON_NEGATIVE;

	private OnClickListener listener;
	private long selectedTime;
	private Dialog v;
	
    /**
     * Create a new instance of TimePickerFragment with some preset time.
     */
    public static TimePickerFragment newInstance(OnClickListener listener, long millis) {
    	TimePickerFragment f = new TimePickerFragment();
    	
    	Bundle b = new Bundle();
    	b.putLong(KEY_SELECTED_TIME,millis);
    	f.setArguments(b);
        return f;
    }

	public long getSelectedTime() {
    	return selectedTime;
    }
    
    private long getSelectedTime(Dialog v) {
    	if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
    		return getSelectedTime11(v);
    	else
    		return getSelectedTime10(v);
    }
    
    @TargetApi(10)
	private long getSelectedTime10(Dialog v) {
    	long seconds = 0;
    	
        seconds += ((SimpleNumberPicker) v.findViewById(R.id.spinner_h1)).getValue() * 36000;
        seconds += ((SimpleNumberPicker) v.findViewById(R.id.spinner_h2)).getValue() * 3600;
        seconds += ((SimpleNumberPicker) v.findViewById(R.id.spinner_m1)).getValue() * 600;
        seconds += ((SimpleNumberPicker) v.findViewById(R.id.spinner_m2)).getValue() * 60;
        seconds += ((SimpleNumberPicker) v.findViewById(R.id.spinner_s1)).getValue() * 10;
        seconds += ((SimpleNumberPicker) v.findViewById(R.id.spinner_s2)).getValue();
    	
    	return seconds * 1000;
    }
    
    @TargetApi(11)
	private long getSelectedTime11(Dialog v) {
		long seconds = 0;
		
	    seconds += ((NumberPicker) v.findViewById(R.id.spinner_h1)).getValue() * 36000;
	    seconds += ((NumberPicker) v.findViewById(R.id.spinner_h2)).getValue() * 3600;
	    seconds += ((NumberPicker) v.findViewById(R.id.spinner_m1)).getValue() * 600;
	    seconds += ((NumberPicker) v.findViewById(R.id.spinner_m2)).getValue() * 60;
	    seconds += ((NumberPicker) v.findViewById(R.id.spinner_s1)).getValue() * 10;
	    seconds += ((NumberPicker) v.findViewById(R.id.spinner_s2)).getValue();
		
		return seconds * 1000;
	}
    
    private void setSelectedTime(long millis) {
    	selectedTime = millis;
    }
    
    private void setPickerTime(long millis, Dialog v) {
    	if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
    		setPickerTime11(millis, v);
    	else
    		setPickerTime10(millis, v);
    }
    
    @TargetApi(10)
    private void setPickerTime10(long millis, Dialog v) {
    	long seconds = millis / 1000;
        int num = (int) (seconds % 60);
        seconds /= 60;
        ((SimpleNumberPicker) v.findViewById(R.id.spinner_s2)).setValue(num % 10);
        ((SimpleNumberPicker) v.findViewById(R.id.spinner_s1)).setValue(num / 10);
        
        num = (int) (seconds % 60);
        seconds /= 60;
        ((SimpleNumberPicker) v.findViewById(R.id.spinner_m2)).setValue(num % 10);
        ((SimpleNumberPicker) v.findViewById(R.id.spinner_m1)).setValue(num / 10);
        
        num = (int) (seconds % 100);
        ((SimpleNumberPicker) v.findViewById(R.id.spinner_h2)).setValue(num % 10);
        ((SimpleNumberPicker) v.findViewById(R.id.spinner_h1)).setValue(num / 10);
    }
    
    @TargetApi(11)
    private void setPickerTime11(long millis, Dialog v) {
    	long seconds = millis / 1000;
        int num = (int) (seconds % 60);
        seconds /= 60;
        ((NumberPicker) v.findViewById(R.id.spinner_s2)).setValue(num % 10);
        ((NumberPicker) v.findViewById(R.id.spinner_s1)).setValue(num / 10);
        
        num = (int) (seconds % 60);
        seconds /= 60;
        ((NumberPicker) v.findViewById(R.id.spinner_m2)).setValue(num % 10);
        ((NumberPicker) v.findViewById(R.id.spinner_m1)).setValue(num / 10);
        
        num = (int) (seconds % 100);
        ((NumberPicker) v.findViewById(R.id.spinner_h2)).setValue(num % 10);
        ((NumberPicker) v.findViewById(R.id.spinner_h1)).setValue(num / 10);
    }

    private void init(Dialog v) {
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
			init11(v);
		else
			init10(v);
	}

	@TargetApi(10)
    private void init10(Dialog v) {
    	/*Integer[] numbers5 = {0,1,2,3,4,5};
    	Integer[] numbers9 = {0,1,2,3,4,5,6,7,8,9};
    	
        ((Spinner) v.findViewById(R.id.spinner_s2)).setAdapter(new ArrayAdapter<Integer>(
        		getActivity(), R.layout.item_timepicker, numbers9));
        ((Spinner) v.findViewById(R.id.spinner_s1)).setAdapter(new ArrayAdapter<Integer>(
        		getActivity(), R.layout.item_timepicker, numbers9));
        
        ((Spinner) v.findViewById(R.id.spinner_m2)).setAdapter(new ArrayAdapter<Integer>(
        		getActivity(), R.layout.item_timepicker, numbers9));
        ((Spinner) v.findViewById(R.id.spinner_m1)).setAdapter(new ArrayAdapter<Integer>(
        		getActivity(), R.layout.item_timepicker, numbers5));
        
        ((Spinner) v.findViewById(R.id.spinner_h2)).setAdapter(new ArrayAdapter<Integer>(
        		getActivity(), R.layout.item_timepicker, numbers9));
        ((Spinner) v.findViewById(R.id.spinner_h1)).setAdapter(new ArrayAdapter<Integer>(
        		getActivity(), R.layout.item_timepicker, numbers5));*/
    	
    }
    
    @TargetApi(11)
    private void init11(Dialog v) {    	
    	NumberPicker p;
        
        p = (NumberPicker) v.findViewById(R.id.spinner_h1);
    	p.setMinValue(0);
    	p.setMaxValue(9);
    	p.setWrapSelectorWheel(true);
        p = (NumberPicker) v.findViewById(R.id.spinner_h2);
    	p.setMinValue(0);
    	p.setMaxValue(9);
    	p.setWrapSelectorWheel(true);

        p = (NumberPicker) v.findViewById(R.id.spinner_m1);
    	p.setMinValue(0);
    	p.setMaxValue(5);
    	p.setWrapSelectorWheel(true);
        p = (NumberPicker) v.findViewById(R.id.spinner_m2);
    	p.setMinValue(0);
    	p.setMaxValue(9);
    	p.setWrapSelectorWheel(true);

        p = (NumberPicker) v.findViewById(R.id.spinner_s1);
    	p.setMinValue(0);
    	p.setMaxValue(5);
    	p.setWrapSelectorWheel(true);
    	p = (NumberPicker) v.findViewById(R.id.spinner_s2);
    	p.setMinValue(0);
    	p.setMaxValue(9);
    	p.setWrapSelectorWheel(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	Bundle b = getArguments();
    	if(b != null)
    		selectedTime = b.getLong(KEY_SELECTED_TIME, selectedTime);
        if(savedInstanceState != null)
        	selectedTime = savedInstanceState.getLong(KEY_SELECTED_TIME, selectedTime);
        
        setStyle(STYLE_NORMAL, 0);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
        v = getDialog();
        
        init(v);
        
        setPickerTime(selectedTime, v);
    }
    
    @Override
    public void onSaveInstanceState(Bundle b) {
    	super.onSaveInstanceState(b);
    	selectedTime = getSelectedTime(v);
    	b.putLong(KEY_SELECTED_TIME,selectedTime);
    }
    
    @Override
    public void onAttach(Activity activity) {
    	super.onAttach(activity);
    	try { listener = (OnClickListener) activity; }
    	catch(ClassCastException e) {}
    }
    
    @Override
    public void onDetach() {
    	super.onDetach();
    	listener = null;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    	
    	builder
    		.setTitle("Zeit einstellen")
    		.setPositiveButton(android.R.string.ok, this)
    		.setNegativeButton(android.R.string.cancel, this)
    		.setView(getActivity().getLayoutInflater()
            		.inflate(R.layout.fragment_timepicker, null, false));
    	
    	Dialog d = builder.create();
    	return d;
    }
    
    @Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which) {
		case BUTTON_POSITIVE:
			setSelectedTime(getSelectedTime(v));
			if(listener != null) listener.onClick(dialog, which);
			break;
		}
	}
}
