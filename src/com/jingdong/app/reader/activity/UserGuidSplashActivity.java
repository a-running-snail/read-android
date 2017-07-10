package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.CommonActivity;
import com.jingdong.app.reader.util.DisplayUtil;

public class UserGuidSplashActivity extends CommonActivity {
	private ViewPager viewPager;
	private ArrayList<View> pageViews;
	private int[] guis = new int[] { R.drawable.gui_1, R.drawable.gui_2, R.drawable.gui_3};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_user_guid);
		pageViews = new ArrayList<View>();

		for (int i = 0; i < guis.length; i++) {
			pageViews.add(viewGenerator(i));
		}

		viewPager = (ViewPager) findViewById(R.id.guidePages);
		viewPager.setAdapter(new GuidePageAdapter());
	}

	public View viewGenerator(int index) {
		View layout = (View) LayoutInflater.from(UserGuidSplashActivity.this).inflate(R.layout.item_user_guid, null);
		ImageView image = (ImageView)layout.findViewById(R.id.image);
		ImageView enterBtn = (ImageView)layout.findViewById(R.id.enterBtn);
		
		image.setBackgroundResource(guis[index]);
		enterBtn.setVisibility(View.GONE);
		if(guis.length == (index + 1)) {
//			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(DisplayUtil.dip2px(200), DisplayUtil.dip2px(46.66f));
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(DisplayUtil.dip2px(200), DisplayUtil.dip2px(60f));
			params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			params.addRule(RelativeLayout.CENTER_HORIZONTAL);
			int padding = (int)(DisplayUtil.getHeight()/7.7);
			params.setMargins(0, 0, 0, padding);
			enterBtn.setLayoutParams(params);
			enterBtn.setVisibility(View.VISIBLE);
			enterBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent();
					intent.setClass(UserGuidSplashActivity.this, LauncherActivity.class);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
				}
			});
		}

		return layout;
	}

	class GuidePageAdapter extends PagerAdapter {

		@Override
		public void destroyItem(View v, int position, Object arg2) {
			((ViewPager) v).removeView(pageViews.get(position));

		}

		@Override
		public int getCount() {
			return pageViews.size();
		}

		@Override
		public Object instantiateItem(View v, int position) {
			((ViewPager) v).addView(pageViews.get(position));
			return pageViews.get(position);
		}

		@Override
		public boolean isViewFromObject(View v, Object arg1) {
			return v == arg1;
		}

		@Override
		public void startUpdate(View arg0) {

		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

	}

}
