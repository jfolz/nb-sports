package com.nobullshit.stopwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context c, Intent intent) {
    	PowerManager m = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
    	WakeLock wakeLock = m.newWakeLock(
    			PowerManager.FULL_WAKE_LOCK
    		  | PowerManager.ACQUIRE_CAUSES_WAKEUP
    		  | PowerManager.ON_AFTER_RELEASE, "RecorderApplication");
		wakeLock.acquire(StopwatchActivity.DEFAULT_ALARM_DURATION);
		
		Intent startIntent = new Intent(c, StopwatchActivity.class);
		startIntent.setAction(Intent.ACTION_MAIN);
		startIntent.setFlags(
				Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
			  | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
			  | Intent.FLAG_ACTIVITY_NEW_TASK);
		startIntent.putExtras(intent.getExtras());
		
		/*startIntent.putExtra(
    			StopwatchActivity.KEY_STATE, 
    			intent.getIntExtra(StopwatchActivity.KEY_STATE,StopwatchActivity.STATE_IDLE));
		startIntent.putExtra(
    			StopwatchActivity.KEY_SELECTED_TIME, 
    			intent.getLongExtra(StopwatchActivity.KEY_SELECTED_TIME,0));
		startIntent.putExtra(
    			StopwatchActivity.KEY_START_TIME, 
    			intent.getLongExtra(StopwatchActivity.KEY_START_TIME,0));
		startIntent.putExtra(
    			StopwatchActivity.KEY_ELAPSED_TIME, 
    			intent.getLongExtra(StopwatchActivity.KEY_ELAPSED_TIME,0));*/
		
		c.startActivity(startIntent);
	}
	
}
