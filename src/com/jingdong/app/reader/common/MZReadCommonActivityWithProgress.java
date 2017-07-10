package com.jingdong.app.reader.common;

import android.os.Bundle;
import android.view.Window;

import com.jingdong.app.reader.ui.ActionBarHelper;
import com.jingdong.app.reader.util.ActivityUtils;

public class MZReadCommonActivityWithProgress extends CommonActivity{
	


	
	@Override
	protected void onCreate(Bundle arg0) {
		//设置actionbar
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		ActionBarHelper.customActionBarBack(this);
		ActionBarHelper.customActionBarBack(this,ActivityUtils.getActivityLabel(this));
		super.onCreate(arg0);

        
	}

}
