package com.jingdong.app.reader.me.activity;

import java.util.ArrayList;
import java.util.List;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseFragmentActivityWithTopBar;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;


public class OrderActivity extends BaseFragmentActivityWithTopBar  implements TopBarViewListener {

	private ViewPager pager;
	private BookstorePagerAdapter pagerAdapter;
	private TopBarView view = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.order);
		initTopbarView();
		pager = (ViewPager) findViewById(R.id.vPager);
		pagerAdapter = new BookstorePagerAdapter(getSupportFragmentManager());
		pager.setAdapter(pagerAdapter);
		pager.setOffscreenPageLimit(2);

		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				view.dotMoveToPosition(position);
			}
		});
	}

	private void initTopbarView() {
		view = getTopBarView();
		if (view==null)
			return;
		List<String> item = new ArrayList<String>();
		item.add("近一个月");
		item.add("一个月前");
		view.setTitleItem(item);
		view.setListener(this);
		view.updateTopBarView();
	}

	public class BookstorePagerAdapter extends FragmentPagerAdapter {

		private int[] titleResIds = new int[] { R.string.recommend,
				R.string.free};// R.string.publisher

		public BookstorePagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0: {// 推荐
				OneMonthFragment fragment = new OneMonthFragment();
				return fragment;
			}
			case 1: {// 免费
				OneMonthAgoFragment fragment = new OneMonthAgoFragment();
				return fragment;
			}
			default:
				return new OneMonthFragment();
			}
		}

		@Override
		public int getCount() {
			return titleResIds.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			String title = getResources().getString(titleResIds[position]);
			return title;
		}
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
		if(pager!=null)
			pager.setCurrentItem(position);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_dingdan));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_dingdan));
	}
	
}
