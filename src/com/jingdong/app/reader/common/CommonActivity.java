package com.jingdong.app.reader.common;

import android.os.Bundle;
import android.os.Handler;

import com.jingdong.app.reader.activity.MyActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.tendcloud.tenddata.TCAgent;

public class CommonActivity extends MyActivity {

	//public String activityTag;
	private Handler handler;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		//activityTag = ActivityUtils.getClassName(this);
		handler = new Handler();
		MZBookApplication.addToActivityStack(this);
	}

	@Override
	protected void onDestroy() {
		MZBookApplication.removeFormActivityStack(this);
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		TCAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		TCAgent.onPause(this);
	}
	

	public Handler getHandler() {
		return handler;
	}

	/**
	 * 统一 post 接口
	 */
	public void post(final Runnable action) {
		// Log.i("zhoubo", "handler==="+handler);
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (CommonActivity.this.isFinishing()) {
					return;
				}
				action.run();
			}
		});
	}

	/**
	 * 统一 post 接口
	 */
	public void post(final Runnable action, int delayMillis) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (CommonActivity.this.isFinishing()) {
					return;
				}
				action.run();
			}
		}, delayMillis);
	}
}
