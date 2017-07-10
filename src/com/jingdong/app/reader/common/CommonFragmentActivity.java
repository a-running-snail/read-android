package com.jingdong.app.reader.common;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.jingdong.app.reader.application.MZBookApplication;
import com.tendcloud.tenddata.TCAgent;

public class CommonFragmentActivity extends FragmentActivity {
	//public  String activityTag;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		//activityTag = ActivityUtils.getClassName(this);
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
	
}
