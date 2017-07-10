package com.jingdong.app.reader.common;

import java.util.ArrayList;
import java.util.List;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.util.ActivityUtils;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import cn.jpush.android.api.JPushInterface;

public class BaseFragmentActivityWithTopBar extends CommonFragmentActivity  implements TopBarViewListener{

	protected String activityTag;
	private TopBarView view = null;
	private View rootView = null;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		activityTag = ActivityUtils.getClassName(this);
	}

	@Override
	public void setContentView(int layoutResID) {

		rootView = LayoutInflater.from(this).inflate(
				R.layout.activity_root_layout, null);
		view = (TopBarView) rootView.findViewById(R.id.topbar);
		initTopbarView();
		LinearLayout container = (LinearLayout) rootView
				.findViewById(R.id.container);
		LayoutInflater.from(this).inflate(layoutResID, container);
		super.setContentView(rootView);

	}

	@Override
	public void setContentView(View view) {
		View root = LayoutInflater.from(this).inflate(
				R.layout.activity_root_layout, null);
		view = (TopBarView) root.findViewById(R.id.topbar);
		initTopbarView();
		LinearLayout container = (LinearLayout) root
				.findViewById(R.id.container);
		container.addView(view);
		super.setContentView(root);
	}

	private void initTopbarView() {
		if (view == null)
			return;
		
		List<String> item = new ArrayList<String>();
		item.add(ActivityUtils.getActivityLabel(this));
		view.setTitleItem(item);
		view.setListener(this);
		view.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		view.updateTopBarView();
	}

	public TopBarView getTopBarView() {
		return view;
	}

	@Override
	public void onLeftMenuClick() {
		// TODO Auto-generated method stub
		finish();
	}

	@Override
	public void onRightMenuOneClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRightMenuTwoClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCenterMenuItemClick(int position) {
		// TODO Auto-generated method stub
		
	}

	public View getRootView() {
		return rootView;
	}
	

}
