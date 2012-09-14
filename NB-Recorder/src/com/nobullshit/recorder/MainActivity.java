package com.nobullshit.recorder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.hardware.SensorEvent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.nobullshit.recorder.io.FileUtils;
import com.nobullshit.sensor.SensorReader;
import com.nobullshit.sensor.SensorReaderListener;

public class MainActivity extends Activity implements SensorReaderListener, OnClickListener {
	
	public static final int SEND_RETURN_CODE = 23456;
	public static final String FANCY_DATE_FORMAT = "EEEE, dd. MMMM yyyy, hh:mm";
	
	private static final String[] MAIL_ADDRESSES = 
			new String[]{ "theriddling@gmail.com", "stefan.hemmer86@googlemail.com" };
	
	private RecorderApplication app;
	private TextView statusAcceleration;
	private TextView statusLocation;
	private TextView status;
	//private Graph graph;
	private Button buttonRec, buttonLock, buttonSend;
	private int colorOK;
	private int colorUnknown;
	private int colorError;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (RecorderApplication) getApplication();
        
        setContentView(R.layout.activity_main);
        
        //app = (RecorderApplication) getApplication();
        statusAcceleration = (TextView) findViewById(R.id.status_acceleration);
        statusLocation = (TextView) findViewById(R.id.status_location);
        status = (TextView) findViewById(R.id.status);
        
        buttonRec = (Button) findViewById(R.id.button_rec);
        buttonRec.setOnClickListener(this);
        
        buttonLock = (Button) findViewById(R.id.lock);
        buttonLock.setOnClickListener(this);
        
        buttonSend = (Button) findViewById(R.id.button_send);
        buttonSend.setOnClickListener(this);
        
        Resources r = getResources();
        colorOK = r.getColor(R.color.holo_dark_green);
        colorUnknown = r.getColor(R.color.holo_dark_orange);
        colorError = r.getColor(R.color.holo_dark_red);
        
        /*graph = (Graph) findViewById(R.id.graph);
        graph.setYTickFormatter(new DecimalFormatter(".00"));
        graph.setOnClickListener(this);*/
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	app.addListener(this);
    	if(app.isRecording() && app.isLocked()) lock();
    	else unlock();
    	setFinishedRecordings();
    	setRecordingMode();
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	app.removeListener(this);
    	if(app.isRecording() && app.isLocked()) lock();
    	else unlock();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.button_rec:
			try { app.toggleRecording(); }
			catch (Exception e) { displayError(e); }
			setRecordingMode();
			break;
		/*case R.id.graph:
			graphLastRecording();
			break;*/
		case R.id.lock:
			unlock();
			break;
		case R.id.button_send:
			shareFiles();
			break;
		}
	}
	
	private void setRecordingMode() {
		if(app.isRecording()) {
	        // now recording, so show activity in front of lockscreen
			statusAcceleration.setVisibility(View.VISIBLE);
			statusLocation.setVisibility(View.VISIBLE);
			
			buttonRec.setText(R.string.button_rec_stop);
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

			checkSensors();
		}
		else {
			statusAcceleration.setVisibility(View.INVISIBLE);
			statusLocation.setVisibility(View.INVISIBLE);
			// show lockscreen
			buttonRec.setText(R.string.button_rec);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			setFinishedRecordings();
		}
	}
	
	private void checkSensors() {
		int color = 0;
		
    	if(app.getSensorReading(SensorReader.TYPE_ACCELEROMETER)) color = colorOK;
    	else if(app.getSensorEnabled(SensorReader.TYPE_ACCELEROMETER)) color = colorUnknown;
    	else color = colorError;
    	statusAcceleration.setTextColor(color);

    	if(app.getSensorReading(SensorReader.TYPE_FINE_LOCATION)) color = colorOK;
    	if(app.getSensorEnabled(SensorReader.TYPE_FINE_LOCATION)) color = colorUnknown;
    	else color = colorError;
    	statusLocation.setTextColor(color);
	}
	
	private void setFinishedRecordings() {
		TextView statusRecordings = (TextView) findViewById(R.id.status_finished_recordings);
		
		File dir = app.getRecordingDirectory();
		if(dir.isDirectory()) {
			File[] files = dir.listFiles();
			if(files.length > 0) {
				StringBuilder b = new StringBuilder();
				for(File f: files) {
					String name = f.getName();
					long time = Long.parseLong(name.substring(name.indexOf("_")+1));
					b.append(DateFormat.format(FANCY_DATE_FORMAT, time));
					b.append("\n");
				}
				statusRecordings.setText(b);
				statusRecordings.setEnabled(true);
				return;
			}
		}
		
		// restore default
		statusRecordings.setText(R.string.status_no_recordings);
		statusRecordings.setEnabled(false);
	}

	private void displayError(Exception e) {
		StringWriter w = new StringWriter();
		e.printStackTrace(new PrintWriter(w));
		status.append(new String(e.getMessage()) + "\n" + w.toString());
		e.printStackTrace();
	}
	
	private void addStatus(CharSequence msg) {
		status.append(msg + "\n");
	}
	
	/*private void graphLastRecording() {
		if(graph.getSeriesCount() > 0) graph.removeSeries(0);
        File dir = new File(getExternalFilesDir(null),RecorderApplication.RECORDING_DIRECTORY);
		if(dir.isDirectory()) {
			File[] files = dir.listFiles();
			if(files.length > 0) {
				File in = files[files.length-1];
				BinaryReader reader;
				try {
					reader = new BinaryReader(in);
					status.setText(reader.getHeader());
					double[] Ts = readTimestamps(reader);
					graph.addSeries(null, Ts);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		graph.refresh();
	}
	
	private double[] readTimestamps(BinaryReader reader) {
		List<Double> temp = new ArrayList<Double>(32*1024);
		try {
			byte series;
			long last=0, next=0;
			while(true) {
				series = reader.readByte();
				switch(series) {
				case RecorderApplication.INDEX_ACCELERATION: // acceleration
					next = reader.readLong();
					reader.readFloat();
					reader.readFloat();
					reader.readFloat();
					if(last > 0) temp.add((double) (next - last) );
					last = next;
					break;
				case RecorderApplication.INDEX_LOCATION: // location
					reader.readLong();
					reader.readDouble();
					reader.readDouble();
					reader.readDouble();
					reader.readFloat();
					break;
				}
			}
		} catch(IOException e) {}
		int i=0;
		double[] out = new double[temp.size()];
		for(double d: temp) out[i++] = d;
		return out;
	}*/
	
	private void lock() {
		buttonLock.setVisibility(View.VISIBLE);
		buttonRec.setEnabled(false);
		buttonSend.setEnabled(false);
		// show in front of lockscreen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	}
	
	private void unlock() {
		buttonLock.setVisibility(View.GONE);
		buttonRec.setEnabled(true);
		buttonSend.setEnabled(true);
		// show lockscreen
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	}
	
	private void shareFiles() {
		File dir = app.getRecordingDirectory();
		if(dir.isDirectory()) {
			File[] files = dir.listFiles();
			if(files.length > 0) {
				try {
					File outdir = app.getOutgoingDirectory();
					File dest = new File(outdir,
							"send_" + System.currentTimeMillis() + ".zip");
					if(!outdir.exists()) outdir.mkdirs();
					FileUtils.ZipFiles(files, dest);
					for(File f: files) f.delete();
					
					Uri uri = Uri.parse("file://" + dest);

					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_EMAIL, MAIL_ADDRESSES);
					intent.putExtra(Intent.EXTRA_SUBJECT, "Meine Aufnahmen");
					intent.putExtra(Intent.EXTRA_STREAM, uri);
					
					startActivityForResult(Intent.createChooser(intent, "Aufnahmen senden"),
							SEND_RETURN_CODE);
				} catch (IOException e) {
					addStatus("error sharing recordings: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		setFinishedRecordings();
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case SEND_RETURN_CODE:
			break;
		}
	}

	public void onSensorChanged(SensorEvent event) {
		/*float x=event.values[0], y=event.values[1], z=event.values[2];
		text.setText(
				"x: " + x + "\n"
			  + "y: " + y + "\n"
			  + "z: " + z + "\n"
			  + "n: " + app.getCount() + "\n"
			  + String.format("%.5f updates/s", (double) app.getCount() / app.getRuntime()));*/
		statusAcceleration.setTextColor(colorOK);
	}

	public void onLocationChanged(Location location) {
		/*double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		double altitude = location.getAltitude();
		float accuracy = location.getAccuracy();
		
		status.setText(
				"lat:  " + latitude + "\n"
			  + "long: " + longitude + "\n"
			  + "alt:  " + altitude + "\n"
			  + "acc:  " + accuracy);*/
		statusLocation.setTextAppearance(getApplicationContext(), R.style.textOK);
	}
	
	public void onProviderDisabled(String provider) {
		statusLocation.setTextColor(colorError);
	}

	@Override
	public void onSensorStateChanged(int sensor, int state) {
		switch(sensor) {
		case SensorReader.TYPE_ACCELEROMETER:
			if(state == SensorReader.STATE_ENABLED
			|| state == SensorReader.STATE_PROCRASTINATING)
					statusAcceleration.setTextColor(colorUnknown);
			else if(state == SensorReader.STATE_DISABLED)
					statusAcceleration.setTextColor(colorError);
			else if(state == SensorReader.STATE_READING)
					statusAcceleration.setTextColor(colorOK);
			break;
		case SensorReader.TYPE_FINE_LOCATION:
			if(state == SensorReader.STATE_ENABLED
			|| state == SensorReader.STATE_PROCRASTINATING)
					statusLocation.setTextColor(colorUnknown);
			else if(state == SensorReader.STATE_DISABLED)
					statusLocation.setTextColor(colorError);
			else if(state == SensorReader.STATE_READING)
					statusLocation.setTextColor(colorOK);
			break;
		}
	}

	@Override
	public void onSensorReading(int sensor, Object reading) {}
}
