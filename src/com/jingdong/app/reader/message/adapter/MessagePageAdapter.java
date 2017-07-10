package com.jingdong.app.reader.message.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jingdong.app.reader.message.fragment.PrivateMessageFragment;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.parser.BaseParserCreator;
import com.jingdong.app.reader.parser.json.AlertJsonParser;
import com.jingdong.app.reader.parser.json.AtMeJSONParser;
import com.jingdong.app.reader.parser.json.CommentJsonParser;
import com.jingdong.app.reader.parser.url.MsgURLParser;
import com.jingdong.app.reader.parser.url.URLParser;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;

public class MessagePageAdapter extends FragmentPagerAdapter {
	private String[] tabNames;

	public MessagePageAdapter(FragmentManager manager, String[] tabNames) {
		super(manager);
		this.tabNames = tabNames;
	}

	public MessagePageAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int arg0) {
		Fragment viewFragment = new TimelineFragment();
		Bundle urlBundle = new Bundle();
		Bundle bundle = new Bundle();
		bundle.putBoolean(TimelineFragment.ENABLE_PULL_REFRESH, false);
		bundle.putBoolean(TimelineFragment.HIDE_BOTTOM, true);
		bundle.putInt(TimelineFragment.INDEX, arg0);
		Class<? extends URLParser> msgParser = MsgURLParser.class;
		switch (arg0) {
		case 0:
			urlBundle.putString(URLParser.BASE_URL, URLText.atMeUrl);
			bundle.putParcelable(TimelineFragment.PARSER_CREATOR, new BaseParserCreator(msgParser,
					AtMeJSONParser.class, urlBundle));
			bundle.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);
			break;
		case 1:
			urlBundle.putString(URLParser.BASE_URL, URLText.myCommentsUrl);
			bundle.putParcelable(TimelineFragment.PARSER_CREATOR, new BaseParserCreator(msgParser,
					CommentJsonParser.class, urlBundle));
			bundle.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);
			break;
		case 2:
			viewFragment = new PrivateMessageFragment();
			break;
		case 3:
			urlBundle.putString(URLParser.BASE_URL, URLText.generalAlertsUrl);
			bundle.putParcelable(TimelineFragment.PARSER_CREATOR, new BaseParserCreator(msgParser,
					AlertJsonParser.class, urlBundle));
			bundle.putBoolean(TimelineFragment.TIMELINE_ADAPTER, false);
			break;
		}
		viewFragment.setArguments(bundle);
		return viewFragment;
	}

	@Override
	public int getCount() {
		return tabNames.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return tabNames[position];
	}
}
