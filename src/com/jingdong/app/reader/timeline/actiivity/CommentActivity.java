package com.jingdong.app.reader.timeline.actiivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.message.fragment.PrivateMessageFragment;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.parser.BaseParserCreator;
import com.jingdong.app.reader.parser.json.AtMeJSONParser;
import com.jingdong.app.reader.parser.json.CommentJsonParser;
import com.jingdong.app.reader.parser.json.TimelineJSONParser;
import com.jingdong.app.reader.parser.url.TimelineURLParser;
import com.jingdong.app.reader.timeline.fragment.BaseTimeLineFragment;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;

public class CommentActivity extends FragmentActivity implements TopBarViewListener{
	
	private FragmentPagerAdapter adapter;
	private ViewPager viewPager;
	private TopBarView topbar;
	private String title;
	private int flag = -1;
	private String user_id;
	private String user_name;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.comment);
		Intent intent = getIntent();
		title = intent.getStringExtra("title");
		topbar = (TopBarView) findViewById(R.id.topbar);
		viewPager = (ViewPager) findViewById(R.id.pager);
		user_id = intent.getStringExtra("user_id");
		user_name = intent.getStringExtra(UserActivity.JD_USER_NAME);

		if (user_id == null) {
			user_id = "";
		}
		
		if (user_name == null) {
			user_name = "";
		}
		
		initTopBar();
		if (title.equals("提到我")) {
			flag = 0;
		}else if(title.equals("评论")){
			flag = 1;
		}else if(title.equals("随便说说")){
			flag = 2;
		}else if(title.equals("书评")){
			flag = 3;
		}else if (title.equals("收藏")) {
			flag = 4;
		}else if (title.equals("私信")) {
			flag = 5;
		}
		
		adapter = new BasePageAdapter(getSupportFragmentManager());
		viewPager.setAdapter(adapter);
	}
	
	private void initTopBar(){
		if (topbar == null)
			return;
		topbar.setTitle(title);
		topbar.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topbar.setListener(this);
		topbar.updateTopBarView();
	}
	
	public class BasePageAdapter extends FragmentPagerAdapter {

		public BasePageAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			Bundle bundle = new Bundle();
			switch (flag) {
			case 0:
				fragment = new BaseTimeLineFragment();
				bundle.putBoolean(TimelineFragment.ENABLE_PULL_REFRESH, true);
				bundle.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);
				bundle.putParcelable(TimelineFragment.PARSER_CREATOR, new BaseParserCreator(TimelineURLParser.class,
						AtMeJSONParser.class));
				bundle.putBoolean(TimelineFragment.HIDE_BOTTOM, true);//隐藏底部操作栏 tmj
				bundle.putString("url", URLText.Atme_URL);
				bundle.putInt("flag", 0);
				bundle.putInt("flags", 0);
				bundle.putString("user_id", user_id);
				break;
			case 1:
				fragment = new BaseTimeLineFragment();
				bundle.putBoolean(TimelineFragment.ENABLE_PULL_REFRESH, true);
				bundle.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);
				bundle.putParcelable(TimelineFragment.PARSER_CREATOR, new BaseParserCreator(TimelineURLParser.class,
						CommentJsonParser.class));
				bundle.putBoolean(TimelineFragment.HIDE_BOTTOM, true);//隐藏底部操作栏 tmj
				bundle.putString("url", URLText.Comment_URL);
				bundle.putInt("flag", 0);
				bundle.putInt("flags", 1);
				bundle.putString("user_id", user_id);
				break;
			case 2:
				fragment = new BaseTimeLineFragment();
				bundle.putBoolean(TimelineFragment.ENABLE_PULL_REFRESH, true);
				bundle.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);
				bundle.putParcelable(TimelineFragment.PARSER_CREATOR, new BaseParserCreator(TimelineURLParser.class,
						TimelineJSONParser.class));
				bundle.putString("url", URLText.User_tweets);
				bundle.putInt("flag", 1);
				bundle.putInt("flags", 2);
				bundle.putString("user_id", user_id);
				bundle.putString(UserActivity.JD_USER_NAME, user_name);
				break;
			case 3:
				fragment = new BaseTimeLineFragment();
				bundle.putBoolean(TimelineFragment.ENABLE_PULL_REFRESH, true);
				bundle.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);
				bundle.putParcelable(TimelineFragment.PARSER_CREATOR, new BaseParserCreator(TimelineURLParser.class,
						TimelineJSONParser.class));
				bundle.putString("url", URLText.Book_Comment_URL);
				bundle.putInt("flag", 1);
				bundle.putInt("flags", 3);
				bundle.putString("user_id", user_id);
				bundle.putString(UserActivity.JD_USER_NAME, user_name);
				break;
			case 4:
				fragment = new BaseTimeLineFragment();
				bundle.putBoolean(TimelineFragment.ENABLE_PULL_REFRESH, true);
				bundle.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);
				bundle.putParcelable(TimelineFragment.PARSER_CREATOR, new BaseParserCreator(TimelineURLParser.class,
						TimelineJSONParser.class));
				bundle.putString("url", URLText.Favourites_URL);
				bundle.putInt("flag", 1);
				bundle.putInt("flags", 4);
				bundle.putString("user_id", user_id);
				break;
			case 5:
				fragment = new PrivateMessageFragment();
				break;
			}
			if (fragment != null)
				fragment.setArguments(bundle);
			return fragment;
		}
	}


	@Override
	public void onLeftMenuClick() {
		// TODO Auto-generated method stub
		finish();
	}

	@Override
	public void onCenterMenuItemClick(int position) {
		// TODO Auto-generated method stub
		
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
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_message_center_comment));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_message_center_comment));
	}

}
