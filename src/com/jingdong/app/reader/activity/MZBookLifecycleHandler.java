package com.jingdong.app.reader.activity;

import com.jingdong.app.reader.application.MZBookApplication;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.os.Bundle;

/**
 * ActivityLifecycleCallbacks 适用于Android 4.0 及更高版本
 * 可以监控所有activity的状态，第三方的除外
 * MZBookLifecycleHandler可以知道App运行在前台还是后台，并对生命周期进行控制
 * App切到后台半小时后退出
 * @author liqiang (jansen)
 *
 */
public class MZBookLifecycleHandler implements ActivityLifecycleCallbacks {

	private static MZBookLifecycleHandler instance;
	private static final int EXIT_COUNT_DOWN = 60 * 30 * 5;
	private int resumed;
	private int paused;
	private int started;
	private int stopped;
	private CountDown countDown;

	public static MZBookLifecycleHandler getInstance() {
		if (instance == null) {
			instance = new MZBookLifecycleHandler();
		}
		return instance;
	}

	private MZBookLifecycleHandler() {
	}

	@Override
	public void onActivityCreated(Activity activity, Bundle bundle) {

	}

	@Override
	public void onActivityDestroyed(Activity activity) {

	}

	@Override
	public void onActivityPaused(Activity activity) {
		paused ++;
	}

	@Override
	public void onActivityResumed(Activity activity) {
		resumed ++;
		cancelCounDown();
	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

	}

	@Override
	public void onActivityStarted(Activity activity) {
		started ++;
	}

	@Override
	public void onActivityStopped(Activity activity) {
		stopped ++;
		if (!isApplicationInForeground()) {
			exitMZBookAppCountDown(activity.getApplicationContext());
		}
	}

    public boolean isApplicationVisible() {
        return started > stopped;
    }

    public boolean isApplicationInForeground() {
        return resumed > paused;
    }
    
    public void cancelCounDown() {
    	if (countDown != null) {
			countDown.isRunning = false;
		}
    }
    
	public void exitMZBookAppCountDown(Context context) {
		if (countDown != null) {
			countDown.isRunning = false;
		}
		countDown = new CountDown(context);
		new Thread(countDown).start();
	}
    
	private class CountDown implements Runnable {

		private int count;
		boolean isRunning;
		Context context;
		
		CountDown(Context context){
			this.context = context;
			this.isRunning = true;
			this.count = EXIT_COUNT_DOWN;
		}
		
		@Override
		public void run() {
			while(isRunning){
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				count --;
				if (count <= 0) {
					isRunning = false;
				}
			}
			if (count <= 0 && !isApplicationInForeground()) {
				MZBookApplication.exitApplication();
			}
		}
		
	};
    
	/*
	public static boolean isBackground(Context context) {
		ActivityManager activityManager = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> appProcesses = activityManager
				.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				if (appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
					//background
					return true;
				} else {
					//foreground
					return false;
				}
			}
		}
		return false;
	}
	*/
}
