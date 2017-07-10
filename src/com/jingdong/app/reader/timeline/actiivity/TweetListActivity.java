package com.jingdong.app.reader.timeline.actiivity;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.jingdong.app.reader.common.MZReadCommonFragmentActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.R;

public class TweetListActivity extends MZReadCommonFragmentActivity {
	
	public static final String RequestUrlKey = "RequestUrlKey";
	public static final String NextPageKey = "NextPageKey";
	public static final String JsonNameKey = "JsonNameKey";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_me_timeline);
		if (savedInstanceState == null)
			initFragment();
	}

	private void initFragment() {
		Fragment fragment = new TimelineFragment();
		Bundle bundle = new Bundle();
		bundle.putBoolean(TimelineFragment.ENABLE_PULL_REFRESH, false);
		bundle.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);
		bundle.putParcelable(TimelineFragment.PARSER_CREATOR,
				getIntent().getParcelableExtra(TimelineFragment.PARSER_CREATOR));
		bundle.putLong(UserFragment.USER_ID, getIntent().getLongExtra(UserFragment.USER_ID, 0));
		fragment.setArguments(bundle);
		getSupportFragmentManager().beginTransaction().add(R.id.user_container, fragment).commit();
	}

}
