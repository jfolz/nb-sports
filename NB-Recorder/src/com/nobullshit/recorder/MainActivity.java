package com.nobullshit.recorder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.nobullshit.binaryio.BinaryReader;
import com.nobullshit.grapher.Graph;
import com.nobullshit.recorder.io.FileUtils;
import com.nobullshit.text.DecimalFormatter;

public class MainActivity extends Activity implements ListenerListener, OnClickListener {
	
	public static final int SEND_RETURN_CODE = 23456;
	
	private static final String[] MAIL_ADDRESSES = 
			new String[]{ "theriddling@gmail.com", "stefan.hemmer86@googlemail.com" };
	
	private RecorderApplication app;
	private TextView text;
	private TextView status;
	private Graph graph;
	private List<File> recentlySent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (RecorderApplication) getApplication();
        
        setContentView(R.layout.activity_main);
        
        //app = (RecorderApplication) getApplication();
        text = (TextView) findViewById(R.id.text);
        status = (TextView) findViewById(R.id.status);
        
        Button rec = (Button) findViewById(R.id.button1);
        rec.setOnClickListener(this);
        
        Button lock = (Button) findViewById(R.id.lock);
        lock.setOnClickListener(this);
        
        Button send = (Button) findViewById(R.id.button_send);
        send.setOnClickListener(this);
        
        graph = (Graph) findViewById(R.id.graph);
        graph.setYTickFormatter(new DecimalFormatter(".00"));
        graph.setOnClickListener(this);
		
        recentlySent = new ArrayList<File>();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	app.addListener(this);
    	if(app.isRecording() && app.isLocked()) lock();
    	else unlock();
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
		case R.id.button1:
			try { app.toggleRecording(); }
			catch (Exception e) { displayError(e); }
			if(app.isRecording()) {
		        // now recording, so show activity in front of lockscreen
		        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			}
			else {
				// show lockscreen
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
			}
			break;
		case R.id.graph:
			graphLastRecording();
			break;
		case R.id.lock:
			unlock();
			break;
		case R.id.button_send:
			shareFiles();
			break;
		}
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
	
	private void graphLastRecording() {
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
	}
	
	private void lock() {
		findViewById(R.id.lock).setEnabled(true);
		findViewById(R.id.button1).setEnabled(false);
		findViewById(R.id.button_send).setEnabled(false);
		// show in front of lockscreen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
	}
	
	private void unlock() {
		findViewById(R.id.lock).setEnabled(false);
		findViewById(R.id.button1).setEnabled(true);
		findViewById(R.id.button_send).setEnabled(true);
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
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		switch(requestCode) {
		case SEND_RETURN_CODE:
			recentlySent.clear();
			break;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		status.append(accuracy + "\n");
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float x=event.values[0], y=event.values[1], z=event.values[2];
		text.setText(
				"x: " + x + "\n"
			  + "y: " + y + "\n"
			  + "z: " + z + "\n"
			  + "n: " + app.getCount() + "\n"
			  + String.format("%.5f updates/s", (double) app.getCount() / app.getRuntime()));
	}

	@Override
	public void onLocationChanged(Location location) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();
		double altitude = location.getAltitude();
		float accuracy = location.getAccuracy();
		
		status.setText(
				"lat:  " + latitude + "\n"
			  + "long: " + longitude + "\n"
			  + "alt:  " + altitude + "\n"
			  + "acc:  " + accuracy);
	}

	@Override
	public void onProviderDisabled(String provider) {}

	@Override
	public void onProviderEnabled(String provider) {}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
}
