package com.jingdong.app.reader.common;

import android.os.Bundle;

import com.jingdong.app.reader.ui.ActionBarHelper;
import com.jingdong.app.reader.util.ActivityUtils;

public class MZReadCommonFragmentActivity extends CommonFragmentActivity {

	
	@Override
	protected void onCreate(Bundle arg0) {
	    
		//设置actionbar
		ActionBarHelper.customActionBarBack(this,ActivityUtils.getActivityLabel(this));
		super.onCreate(arg0);

        
	}

	
	@Override
	protected void onDestroy() {
		 super.onDestroy();
	}

}
