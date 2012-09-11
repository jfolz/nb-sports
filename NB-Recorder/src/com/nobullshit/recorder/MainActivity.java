package com.nobullshit.recorder;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.nobullshit.binaryio.BinaryReader;
import com.nobullshit.grapher.Graph;
import com.nobullshit.text.DecimalFormatter;

public class MainActivity extends Activity implements ListenerListener, OnClickListener {
	
	private RecorderApplication app;
	private TextView text;
	private TextView status;
	private BenchmarkTask task;
	private Graph graph;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        app = (RecorderApplication) getApplication();
        text = (TextView) findViewById(R.id.text);
        status = (TextView) findViewById(R.id.status);
        Button rec = (Button) findViewById(R.id.button1);
        rec.setOnClickListener(this);
        if(!app.isRecording()) {
        	task = new BenchmarkTask();
            task.execute();
        }

        graph = (Graph) findViewById(R.id.graph);
        graph.setYTickFormatter(new DecimalFormatter(".00"));
        graph.setOnClickListener(this);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	app.addListener(this);
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	app.removeListener(this);
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	if(task != null) task.cancel(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.button1:
			if(task != null) task.cancel(true);
			try { app.toggleRecording(); }
			catch (IOException e) { displayError(e); }
			if(app.isRecording()) graphLastRecording();
			break;
		case R.id.graph:
			graphLastRecording();
		}
	}

	private void displayError(Exception e) {
		StringWriter w = new StringWriter();
		e.printStackTrace(new PrintWriter(w));
		text.setText(new String(e.getMessage()) + "\n" + w.toString());
		e.printStackTrace();
	}
	
	private void graphLastRecording() {
		if(graph.getSeriesCount() > 0) graph.removeSeries(0);
        File dir = new File(getExternalFilesDir(null),RecorderApplication.APP_DIRECTORY);
		if(dir.isDirectory()) {
			File[] files = dir.listFiles();
			if(files.length > 0) {
				File in = files[files.length-1];
				double[] Ts = readTimestamps(in);
				graph.addSeries(null, Ts);
			}
		}
		graph.refresh();
	}
	
	private double[] readTimestamps(File in) {
		List<Double> temp = new ArrayList<Double>(32*1024);
		try {
			BinaryReader reader = new BinaryReader(in);
			long last = reader.readLong(), next;
			while(true) {
				reader.readFloat();
				reader.readFloat();
				reader.readFloat();
				next = reader.readLong();
				temp.add((double) (next - last) );
				last = next;
			}
		} catch(IOException e) {}
		int i=0;
		double[] out = new double[temp.size()];
		for(double d: temp) out[i++] = d;
		return out;
	}
	

	
	private class BenchmarkTask extends AsyncTask<Void, String, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			measureRecordings();
			return null;
		}
		
		private void measureRecordings() {
	        File dir = new File(getExternalFilesDir(null),RecorderApplication.APP_DIRECTORY);
			if(dir.isDirectory()) {
				File[] files = dir.listFiles();
				for(File in: files) {
					if(isCancelled()) break;

					String header = "???";
					long size = in.length();
					long entries = 0;
					long start = System.currentTimeMillis();
					try {
						BinaryReader reader = new BinaryReader(in);
						header = reader.getHeader();
						while(true) {
							reader.readLong();
							for(int i=0; i<3; i++) reader.readFloat();
							entries++;
						}
					} catch(IOException e) {}
				
					double delta = (System.currentTimeMillis()-start) / 1000D;
					publishProgress(in.getName()
							+ ", "
							+ String.format("%7.2f kB", (double) size / 1024)
							+ "\n"
							+ header
							+ "\n"
							+ String.format("%10.2f b/entry",(double) size / entries)
							+ "\n"
							+ String.format("%10d entries, ", entries)
							+ "\n"
							+ String.format("%10.2f entries/s",entries / delta)
							+ "\n"
							+ "\n");
				}
			}
		}
		
		protected void onProgressUpdate(String... update) {
			text.append(update[0]);
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
		// TODO Auto-generated method stub
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
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
