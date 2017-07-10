package com.jingdong.app.reader.bookstore.search.adapter;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jingdong.app.reader.parser.BaseParserCreator;
import com.jingdong.app.reader.parser.json.TimelineJSONParser;
import com.jingdong.app.reader.parser.url.TimelineSearchURLParser;
import com.jingdong.app.reader.timeline.fragment.BaseTimeLineFragment;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.timeline.fragment.UserListFragment;

public class SearchPageAdapter extends FragmentPagerAdapter {
	private String[] tabNames;
	private List<Fragment> fragments = new ArrayList<Fragment>();
	Fragment fragment1 = null;
	Fragment fragment2 = null;

	public SearchPageAdapter(FragmentManager fragmentManager, String[] tabNames) {
		super(fragmentManager);
		this.tabNames = tabNames;
		Bundle bundle1 = new Bundle();
		fragment1 = new UserListFragment();
		bundle1.putBoolean(UserListFragment.EMPTY_INIT_PAGE, true);
		bundle1.putBoolean(UserListFragment.SHOW_RIGHT_BUTTON, false);
		fragment1.setArguments(bundle1);
		fragment2 = new TimelineFragment();
		Bundle bundle2 = new Bundle();
		bundle2.putParcelable(TimelineFragment.PARSER_CREATOR, new BaseParserCreator(TimelineSearchURLParser.class,
				TimelineJSONParser.class));
		bundle2.putBoolean(TimelineFragment.ENABLE_PULL_REFRESH, false);
		bundle2.putBoolean(TimelineFragment.SEARCHABLE, true);
		bundle2.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);
		bundle2.putBoolean(TimelineFragment.HIDE_BOTTOM, false);
		fragment2.setArguments(bundle2);
		fragments.add(fragment1);
		fragments.add(fragment2);
	}

	@Override
	public int getCount() {
		return tabNames.length;
	}

	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return tabNames[position];
	}
}
