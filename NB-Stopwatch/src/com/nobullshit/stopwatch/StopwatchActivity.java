package com.nobullshit.stopwatch;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONTokener;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.nobullshit.stopwatch.tools.AnimationHandler;
import com.nobullshit.stopwatch.tools.Time;
import com.nobullshit.stopwatch.tools.TimePickerFragment;

public class StopwatchActivity extends FragmentActivity
		implements OnClickListener, DialogInterface.OnClickListener {

	public static final int ALARM_REQUEST_CODE = 12345;
	public static final int NOTIFICATION_CODE = 209348675;
	public static final String ACTION_ALARM = "com.nobullshit.stopwatch.action.ALARM";
	public static final String TAG_TIMEPICKER = "TIMEPICKER";
	public static final String KEY_TIMES_FAVORITE = "TIMES_FAVORITE";
	public static final String KEY_TIMES_RECENT = "TIMES_RECENT";
	public static final String KEY_MODE = "MODE";
	public static final String KEY_STATE = "STATE";
	public static final String KEY_ELAPSED_TIME = "REMAINING_TIME";
	public static final String KEY_START_TIME = "START_TIME";
	public static final String KEY_SELECTED_TIME = "SELECTED_TIME";
	public static final int MODE_STOPWATCH = 1;
	public static final int MODE_TIMER = 2;
	public static final int STATE_IDLE = 1;
	public static final int STATE_RUNNING = 2;
	public static final int STATE_PAUSED = 4;

	public static final int DEFAULT_ALARM_DURATION = 6000;
	public static final int DEFAULT_TRANSITION_DURATION = 500;
	public static final String DEFAULT_FAVORITE_TIMES = "[0]";
	public static final String EMPTY_JSON_ARRAY = "[]";
	
	private TextView bigclock;
	private int fragmentID;
	private long selectedTime = 0;
	private long startTime = 0;
	private long elapsedTime = 0;
	private int maxRecentTimes = 5;
	private int mode = MODE_STOPWATCH;
	private int state = STATE_IDLE;
	private ImageButton button_startstop;
	private ViewGroup list_favorite;
	private ViewGroup list_recent;
	private List<Long> times_favorite;
	private List<Long> times_recent;
	private WatchTask watchTask;
	private Ringtone ringtoneAlarm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        ringtoneAlarm = RingtoneManager.getRingtone(getApplicationContext(), notification);
        ringtoneAlarm.setStreamType(AudioManager.STREAM_ALARM);
        
        setContentView(R.layout.activity_stopwatch);
        
        bigclock = (TextView) findViewById(R.id.bigclock);
        bigclock.setOnClickListener(this);
        
        ToggleButton button_mode = (ToggleButton) findViewById(R.id.button_mode);
        button_mode.setOnClickListener(this);
        
        button_startstop = (ImageButton) findViewById(R.id.button_startstop);
        button_startstop.setOnClickListener(this);
        
        Button button_reset = (Button) findViewById(R.id.button_reset);
        button_reset.setOnClickListener(this);
        
        list_favorite = (ViewGroup) findViewById(R.id.list_favorites);
        list_recent = (ViewGroup) findViewById(R.id.list_recent);
        
        times_favorite = new LinkedList<Long>();
        times_recent = new LinkedList<Long>();
    	
    	SharedPreferences prefs = getPreferences(MODE_PRIVATE);

    	state = prefs.getInt(KEY_STATE, STATE_IDLE);
        startTime = prefs.getLong(KEY_START_TIME, 0);
        selectedTime = prefs.getLong(KEY_SELECTED_TIME, 0);
        elapsedTime = prefs.getLong(KEY_ELAPSED_TIME, 0);
    	mode = prefs.getInt(KEY_MODE, MODE_STOPWATCH);
        
        setBigClock(selectedTime - elapsedTime);
    	
    	button_mode.setChecked(mode == MODE_TIMER);
    	if(state == STATE_RUNNING)
    		button_startstop.setImageResource(android.R.drawable.ic_media_pause);
    	else if(state == STATE_IDLE || state == STATE_PAUSED)
    		button_startstop.setImageResource(android.R.drawable.ic_media_play);
    	
    	try {
        	JSONTokener json = new JSONTokener(prefs.getString(KEY_TIMES_FAVORITE,
        			DEFAULT_FAVORITE_TIMES));
			JSONArray array = (JSONArray) json.nextValue();
			for(int i=array.length()-1; i>=0; i--) {
				addTime(list_favorite, array.getLong(i), false);
			}
		} catch (JSONException e) {}
    	
    	try {
        	JSONTokener json = new JSONTokener(prefs.getString(KEY_TIMES_RECENT,
        			EMPTY_JSON_ARRAY));
			JSONArray array = (JSONArray) json.nextValue();
			for(int i=array.length()-1; i>=0; i--) {
				addTime(list_recent, array.getLong(i), false);
			}
		} catch (JSONException e) {}
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	if(state == STATE_RUNNING) {
    		startCountdown();
        	AlarmManager m = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        	m.cancel(createAlarmIntent());
    	}
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	if(state == STATE_RUNNING) {
    		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
    		stopCountdown();
        	
    		
        	if(selectedTime - elapsedTime > 0) {
        		PendingIntent intent = createAlarmIntent();
        		AlarmManager m = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        		m.cancel(intent);
            	m.set(
            			AlarmManager.RTC_WAKEUP,
            			System.currentTimeMillis() + selectedTime - elapsedTime,
            			intent);
            	Log.v("BLA","alarm set");
        	}
    	}
    	
    	SharedPreferences prefs = getPreferences(MODE_PRIVATE);
    	Editor editor = prefs.edit();

    	editor.putInt(KEY_STATE, state);
    	editor.putLong(KEY_START_TIME, startTime);
    	editor.putLong(KEY_SELECTED_TIME, selectedTime);
    	editor.putLong(KEY_ELAPSED_TIME, elapsedTime);
    	editor.putInt(KEY_MODE, mode);
    	
    	try {
        	JSONStringer json = new JSONStringer();
			json.array();
	    	for(long time : times_favorite) json.value(time);
	    	json.endArray();
	    	editor.putString(KEY_TIMES_FAVORITE, json.toString());
		} catch (JSONException e1) {}
    	
    	try {
        	JSONStringer json = new JSONStringer();
			json.array();
	    	for(long time : times_recent) json.value(time);
	    	json.endArray();
	    	editor.putString(KEY_TIMES_RECENT, json.toString());
		} catch (JSONException e1) {}
    	
    	editor.commit();
    }
    
    /*@Override
    public void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);

    	outState.putInt(KEY_STATE, state);
    	outState.putLong(KEY_SELECTED_TIME, selectedTime);
    	outState.putLong(KEY_START_TIME, startTime);
    	outState.putLong(KEY_ELAPSED_TIME, elapsedTime);
    }*/
    
    @Override
	public void onClick(View v) {
    	long millis;
    	
		switch(v.getId()) {
		
		case View.NO_ID:
			if(state != STATE_RUNNING) {
				millis = (Long) v.getTag();
				selectedTime = millis;
				setBigClock(millis);
			}
			break;
		
		case R.id.bigclock:
			if(state != STATE_RUNNING) {
				showTimePicker();
			}
			break;
		
		case R.id.favorite:
			millis = (Long) ((View) v.getParent()).getTag();
			ImageButton favorite = (ImageButton) v.findViewById(R.id.favorite);
			if(times_favorite.contains(millis)) {
				favorite.setImageResource(android.R.drawable.btn_star_big_off);
				favoriteToRecent(millis);
			}
			else {
				favorite.setImageResource(android.R.drawable.btn_star_big_on);
				recentToFavorite(millis);
			}
			break;
		
		case R.id.button_mode:
			if(mode == MODE_STOPWATCH) mode = MODE_TIMER;
			else if(mode == MODE_TIMER) mode = MODE_STOPWATCH;
			break;
		
		case R.id.button_startstop:
			if(state == STATE_IDLE || state == STATE_PAUSED) start();
			else if(state == STATE_RUNNING) stop();
			break;
		
		case R.id.button_reset:
			reset();
			break;
		}
	}

	private void showTimePicker() {
	    DialogFragment fragment = TimePickerFragment.newInstance(this, selectedTime);
	    fragmentID = fragment.getId();
	    fragment.show(getSupportFragmentManager(), TAG_TIMEPICKER);
	}

	private void start() {
		if(mode == MODE_TIMER && elapsedTime >= selectedTime) return;
		
		startTime = System.currentTimeMillis();
		state = STATE_RUNNING;
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		button_startstop.setImageResource(android.R.drawable.ic_media_pause);
		startCountdown();
		showNotification();
	}

	private void stop() {
		state = STATE_PAUSED;
		long now = System.currentTimeMillis();
		elapsedTime += now - startTime;
		startTime = now;

		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		button_startstop.setImageResource(android.R.drawable.ic_media_play);
		stopCountdown();
		removeNotification();
	}

	private void reset() {
		elapsedTime = 0;
		state = STATE_IDLE;

		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		button_startstop.setImageResource(android.R.drawable.ic_media_play);
		stopCountdown();
		removeNotification();
		setBigClock(selectedTime);
	}
	
	private void startCountdown() {		
		watchTask = new WatchTask();
		watchTask.execute();
		
		list_favorite.setEnabled(false);
		list_recent.setEnabled(false);
	}
	
	private void stopCountdown() {		
		if(watchTask != null) {
			watchTask.cancel(true);
			watchTask = null;
		}
		
		list_favorite.setEnabled(true);
		list_recent.setEnabled(true);
	}

	private void showNotification() {
		NotificationManager m = 
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		m.notify(NOTIFICATION_CODE, createNotification());
	}

	private void removeNotification() {
		NotificationManager m = 
				(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		m.cancel(NOTIFICATION_CODE);
	}

	private Notification createNotification() {
		Intent startingIntent = new Intent(Intent.ACTION_MAIN);
	    startingIntent.setClass(this, this.getClass());
	    
	    PendingIntent contentIntent = PendingIntent.getActivity(
	    		this, 0, startingIntent, Notification.FLAG_ONGOING_EVENT);
		
	    Notification not = new NotificationCompat.Builder(this)
	    	.setContentTitle("NB-Stopwatch")
			.setContentText("Aktiv")
			.setSmallIcon(R.drawable.ic_launcher)
			.setContentIntent(contentIntent)
			.build();
	    not.flags = Notification.FLAG_ONGOING_EVENT;
	    
	    return not;
	}

	private PendingIntent createAlarmIntent() {
		Intent alarm = new Intent(ACTION_ALARM);
		alarm.setClass(this, AlarmReceiver.class);
		
		PendingIntent pendingAlarm = PendingIntent.getBroadcast(
				this,
				ALARM_REQUEST_CODE,
				alarm,
				PendingIntent.FLAG_ONE_SHOT
			  | PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingAlarm;
	}

	private void favoriteToRecent(long time) {
		int n = times_favorite.size();
		int pos = times_favorite.indexOf(time);
		View item = list_favorite.getChildAt(pos);
		
		if(times_recent.size() > maxRecentTimes - 1) {
			int last = list_recent.getChildCount() - 1;
			times_recent.remove(last);
			list_recent.removeViewAt(last);
		}
		
		times_favorite.remove(pos);
		times_recent.add(0,time);
		
		if(pos < n-1) animateItem(item, list_favorite, list_recent, 0);
		else {
			list_favorite.removeViewAt(pos);
			list_recent.addView(item,0);
		}
	}

	private void recentToFavorite(long time) {
		int n = times_favorite.size();
		int pos = times_recent.indexOf(time);
		View item = list_recent.getChildAt(pos);
		
		times_recent.remove(pos);
		
		int newpos = Collections.binarySearch(times_favorite, time);
		if(newpos < 0) {
			newpos = -newpos-1;
			times_favorite.add(newpos,time);
		}
		
		if(pos > 0 || newpos < n)
			animateItem(item, list_recent, list_favorite, newpos);
		else {
			list_recent.removeViewAt(pos);
			list_favorite.addView(item,newpos);
		}
	}
	
	private void animateItem(View item, ViewGroup from, ViewGroup to, int newpos) {
		AnimationHandler.animate(this, item, from, to, newpos);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch(which) {
		case TimePickerFragment.BUTTON_POSITIVE:
			FragmentManager m = getSupportFragmentManager();
			TimePickerFragment fragment =
					(TimePickerFragment) m.findFragmentById(fragmentID);
	        selectedTime = fragment.getSelectedTime();
	        fragment = null;
	        addTime(list_recent, selectedTime, true);
	        reset();
			break;
		}
	}

	private void addTime(ViewGroup list, long millis, boolean animate) {
		// time is already in favorites
		if(times_favorite.contains(millis)){
			return;
		}
		
		// time is already in recents, so just reorder
		int pos = times_recent.indexOf(millis);
		if(pos >= 0) {
			View v = list_recent.getChildAt(pos);
			list_recent.removeViewAt(pos);
			list_recent.addView(v,0);
			times_recent.remove(pos);
			times_recent.add(0, millis);
			return;
		}
		
		// need to create a new item
		View item = getLayoutInflater().inflate(R.layout.time_item, null);
		item.setTag(millis);
		TextView value = (TextView) item.findViewById(R.id.value);
		ImageButton favorite = (ImageButton) item.findViewById(R.id.favorite);
		
		value.setText(Time.millisToString(millis));
		item.setOnClickListener(this);
		favorite.setOnClickListener(this);
		
		if(list.getId() == list_favorite.getId()) {
			pos = -Collections.binarySearch(times_favorite, millis) - 1;
			times_favorite.add(pos, millis);
			list_favorite.addView(item, pos);
			favorite.setImageResource(android.R.drawable.btn_star_big_on);
		}
		else {
			times_recent.add(0,millis);
			list_recent.addView(item, 0);
			favorite.setImageResource(android.R.drawable.btn_star_big_off);
			if(list_recent.getChildCount() > maxRecentTimes) {
				list_recent.removeViewAt(maxRecentTimes);
				times_recent.remove(maxRecentTimes);
			}
		}

		if(animate) animateItem(item, null, list, -1);
	}

	private void setBigClock(long millis) {
		long validValue = millis;
		
		if(mode == MODE_STOPWATCH && selectedTime > 0 && millis > selectedTime)
			validValue = selectedTime;
		else if(mode == MODE_TIMER && millis < 0)
			validValue = 0;
		
		bigclock.setText(Time.millisToString(validValue));
    }
    
    private class WatchTask extends AsyncTask<Void, Long, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			long endTime = startTime + selectedTime - elapsedTime;
			boolean indefinite = mode == MODE_STOPWATCH && selectedTime <= 0;
			
			long now = System.currentTimeMillis();;
			long i= (now - startTime) / 1000;
			do {
				i++;
				now = System.currentTimeMillis();
				
				if(mode == MODE_STOPWATCH)
					publishProgress(now - startTime + elapsedTime);
				else if(mode == MODE_TIMER)
					publishProgress(endTime - now);
				
				if(!indefinite && now >= endTime) break;
				
				try { Thread.sleep(startTime + i*1000 - now); }
				catch (InterruptedException e) { return null; }
			} while(true);
			
			if(!isCancelled()) {
				ringtoneAlarm.play();
				try { Thread.sleep(DEFAULT_ALARM_DURATION); }
				catch (InterruptedException e) {}
				ringtoneAlarm.stop();
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Long... times) {
			setBigClock(times[0].longValue());
		}
		
		@Override
		protected void onPostExecute(Void result) {
			reset();
		}
    	
    }
}
