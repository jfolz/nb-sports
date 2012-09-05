package com.nobullshit.gpstracker;

import java.io.File;

import android.app.Activity;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nobullshit.gpstracker.location.AccurateLocator;
import com.nobullshit.gpstracker.location.LocationProvider;
import com.nobullshit.gpstracker.location.Locator;
import com.nobullshit.gpstracker.location.WindowLocator;

public class SportsTrackerActivity
extends Activity implements LocationListener, OnClickListener {
	public static String HEADER_STRING = "SPORTSTRACKER BINARY FORMAT V1 " +
	"TIME/LONG/LAT/ALT/ACC\n";
	private static final int MODE_INACTIVE = 0;
	private static final int MODE_INITIALIZING = 1;
	private static final int MODE_LOGGING = 2;
	private LocationProvider loc;
	private Locator locator;
	private ViewGroup layout;
	private TextView infobox;
	private int mode = MODE_INACTIVE;
	private CountdownTask task;
	private Statistics stats;
	private LocationWriter writer;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		layout = (ViewGroup) getLayoutInflater().inflate(R.layout.main, null);
		setContentView(layout);
		infobox = (TextView) findViewById(R.id.infobox);
		findViewById(R.id.control).setOnClickListener(this);        
		loc = new LocationProvider(getApplicationContext(), this);
		locator = new AccurateLocator();
	}
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        loc.stopListening();
        if(writer != null) writer.close();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }

	public void onLocationChanged(Location location) {
		switch(mode) {
		case MODE_INACTIVE:
			break;
		case MODE_INITIALIZING:
			locator.update(location);
			if(locator.isAccurate() && task == null) {
				task = new CountdownTask();
				task.execute(3);
			}
			break;
		case MODE_LOGGING:	
			locator.update(location);
			Location cur = locator.getEstimate();
			stats.update(cur);
			updateVerbose(cur);
			writer.write(cur);
			break;
		}
	}
	public void onProviderDisabled(String provider) {}
	public void onProviderEnabled(String provider) {}
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	public void onClick(View v) {
		switch(v.getId()) {
			case R.id.control:
				ToggleButton t = (ToggleButton) v;
				if(t.isChecked()) startLogging();
				else stopLogging();
		}
	}

	private void startLogging() {
		mode = MODE_INITIALIZING;
		showProgress(true);
		File outdir;
		if(Environment.getExternalStorageState().equals(
		Environment.MEDIA_MOUNTED)) {
			outdir = getExternalFilesDir(null);
		}
		else {
			appendInfobox("external storage not mounted, " +
				"writing to interal storage instead");
			outdir = getFilesDir();
		}
        File outfile = new File(outdir, "sample.track");
        writer = new LocationWriter(outfile);
        loc.startListening();
    	appendInfobox("writing to " + outfile.getName());
	}
	
	private void stopLogging() {
		mode = MODE_INACTIVE;
		showProgress(false);
		loc.stopListening();
		if(writer != null) writer.close();
		appendInfobox("stopped logging");
	}

	private void updateVerbose(Location location) {
		TextView text = (TextView) findViewById(R.id.coords);
		String s = String.format(
				"DUR: %s\n" +
				"DST: %9.3f\n" +
				"ALT: %9.3f m\n" +
				"ACC: %9.3f m\n" +
				"SPD: %9.3f km/h\n",
				Time.format(stats.getDuration()),
				stats.getDistance(),
				stats.getAvgAltitude(),
				location.getAccuracy(),
				location.getSpeed() * 3.6);
		text.setText(s);
	}
	
	private void appendInfobox(String s) {
		infobox.append(s+"\n");
		Log.v("SportsTracker", s);
		final ScrollView v = (ScrollView) findViewById(R.id.scrollInfobox);
		v.postDelayed(new Runnable() {
			public void run() {v.smoothScrollTo(0, 999999999);}},200);
	}
	
	private void showProgress(boolean show) {
		ViewGroup frame = (ViewGroup) layout.findViewById(R.id.progressLayout);
		if(show) {
			frame.setVisibility(View.VISIBLE);
			frame.findViewById(R.id.progressWait).setVisibility(View.VISIBLE);
			frame.findViewById(R.id.progressText).setVisibility(View.GONE);
		}
		else {
			frame.setVisibility(View.GONE);
		}
	}
	
	private void setCountdown(CharSequence s) {
		TextView t = (TextView) findViewById(R.id.progressText);
		t.setText(s);
	}
	
	private void setCountdown(int n) {
		setCountdown(n+"");
	}
	
	private void showCountdown(boolean show) {
		ViewGroup frame = (ViewGroup) layout.findViewById(R.id.progressLayout);
		if(show) {
			frame.setVisibility(View.VISIBLE);
			frame.findViewById(R.id.progressWait).setVisibility(View.GONE);
			frame.findViewById(R.id.progressText).setVisibility(View.VISIBLE);
		}
		else {
			frame.setVisibility(View.GONE);
		}
	}

	private class CountdownTask extends AsyncTask<Integer, Integer, Void> {
		private MediaPlayer beeper;
	
		@Override
		protected Void doInBackground(Integer... params) {
			long next = System.currentTimeMillis() + 1000;
			int i = params[0];
			beeper = MediaPlayer.create(SportsTrackerActivity.this,
					R.raw.snd_countdown_beep);
			//beeper.setAudioStreamType(AudioManager.STREAM_ALARM);
			while(i > 0) {
				beeper.start();
				publishProgress(i);
				try { Thread.sleep(next - System.currentTimeMillis()); }
				catch (InterruptedException e) {}
				next += 1000;
				beeper.seekTo(0);
				i--;
			}
			beeper.release();
			beeper = MediaPlayer.create(SportsTrackerActivity.this,
					R.raw.snd_countdown_go);
			//beeper.setAudioStreamType(AudioManager.STREAM_ALARM);
			beeper.start();
			publishProgress(i);
			try { Thread.sleep(next - System.currentTimeMillis()); }
			catch (InterruptedException e) {}
			beeper.release();
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			appendInfobox("starting countdown");
		}
		
		@Override
		protected void onProgressUpdate(Integer... params) {
			showCountdown(true);
			int i = params[0];
			if(i == 0) setCountdown("GO");
			else setCountdown(i);
		}
		
		@Override
		protected void onPostExecute(Void v) {
			stats = new Statistics(locator.getEstimate());
			locator = new WindowLocator(locator.getEstimate());
			task = null;
			showCountdown(false);
			mode = MODE_LOGGING;
		}
		
	}
}





