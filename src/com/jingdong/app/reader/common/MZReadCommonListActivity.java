package com.jingdong.app.reader.common;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.Window;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.ui.ActionBarHelper;
import com.jingdong.app.reader.util.ActivityUtils;
import com.tendcloud.tenddata.TCAgent;

public class MZReadCommonListActivity extends ListActivity {

	//protected String activityTag;

	@Override
	protected void onCreate(Bundle arg0) {
	    
		//activityTag = ActivityUtils.getClassName(this);
		//设置actionbar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		ActionBarHelper.customActionBarBack(this,ActivityUtils.getActivityLabel(this));
		MZBookApplication.addToActivityStack(this);
		super.onCreate(arg0);

        
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