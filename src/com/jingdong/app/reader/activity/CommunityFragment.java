package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import java.util.List;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.R.array;
import com.jingdong.app.reader.album.FunctionPopView;
import com.jingdong.app.reader.bookstore.search.SearchActivity;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.community.CommunitySearchActivity;
import com.jingdong.app.reader.community.FindFragment;
import com.jingdong.app.reader.community.FriendCircleFragment;
import com.jingdong.app.reader.community.square.SquareFragment;
import com.jingdong.app.reader.me.activity.UserReadingBookFragment;
import com.jingdong.app.reader.parser.BaseParserCreator;
import com.jingdong.app.reader.parser.json.TimelineJSONParser;
import com.jingdong.app.reader.parser.url.TimelineURLParser;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelinePostTweetActivity;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.view.TopBarPopupWindow;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommunityFragment extends CommonFragment implements TopBarViewListener,
		OnItemClickListener, TopBarPopupWindow.onPopupWindowItemClickListener {

	public CommunityFragment() {
		super();
	}

	@Override
	public void onDestroy() {
		MZLog.d("life-cycle", "社区onDestroy");
		super.onDestroy();
	}

	private TopBarView topBarView = null;
	private TopBarPopupWindow rightPopupWindow = null;
	private FragmentManager fragmentManager;
	private FragmentTransaction fragmentTransaction;
	private FriendCircleFragment friendCircleFragment;
	private ViewPager pager;
	private CommunityPagerAdapter pagerAdapter;
	
	private TextView tabSquare,tabFriendcircle,tabFind;
	private ImageView tabBottomImage;
	private List<TextView> tabs = new ArrayList<TextView>(); 
	private ImageView bottomImageView;
	private List<View> dotViewList=new ArrayList<View>(3);
	
//	private PostTweetInterface postTweetInterface;

	
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		MZLog.d("life-cycle", "社区onCreateView");

		View rootView = inflater.inflate(R.layout.activity_timeline, null);
		topBarView = (TopBarView) rootView.findViewById(R.id.topbar);

		List<String> rightItemSubmenu = new ArrayList<String>();
		rightItemSubmenu.add("写书评");
		rightItemSubmenu.add("随便说说");
		rightPopupWindow = new TopBarPopupWindow(getActivity(), rightItemSubmenu, "101");
		rightPopupWindow.setListener(this);
		
		tabSquare = (TextView) rootView.findViewById(R.id.tab_square);
		tabFriendcircle = (TextView) rootView.findViewById(R.id.tab_friend_circle);
		tabFind = (TextView) rootView.findViewById(R.id.tab_find);
		tabs.add(0,tabSquare);
		tabs.add(1,tabFriendcircle);
		tabs.add(2,tabFind);
		
		dotViewList.add(rootView.findViewById(R.id.square_dot));
		dotViewList.add(rootView.findViewById(R.id.friend_dot));
		dotViewList.add(rootView.findViewById(R.id.find_dot));
		
		for (int i = 0; i < tabs.size(); i++) {
			final int postion = i;
			tabs.get(i).setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					onTabItemClick(postion);
				}
			});
		}
		
//		bottomImageView = (ImageView) rootView.findViewById(R.id.tab_bottom_image);
		
		pager = (ViewPager)rootView.findViewById(R.id.vPager);
		pagerAdapter = new CommunityPagerAdapter(getChildFragmentManager());
		pager.setAdapter(pagerAdapter);
		pager.setOffscreenPageLimit(2);
		
		pager.setCurrentItem(1);
		moveToPosition(1);

		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				moveToPosition(position);
			}
		});
		
		return rootView;
	}

	public void initTopbarView() {
		if (getActivity() == null || topBarView == null)
			return;
		List<String> item = new ArrayList<String>();
		item.add("社区");
		if (LoginUser.isLogin()) {
			topBarView.setLeftMenuVisiable(true, R.drawable.topbar_search);
			topBarView.setRightMenuOneVisiable(true, R.drawable.btn_bar_compose, false);
		} else {
		}
		topBarView.setTitleItem(item);
		topBarView.setListener(this);
		topBarView.updateTopBarView();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		initTopbarView();
	}

	@Override
	public void onDestroyView() {
		MZLog.d("life-cycle", "社区onDestroyView");
		super.onDestroyView();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		switch (requestCode) {
//		case TimelineRootFragment.POST_TWEET:
//			if(postTweetInterface!=null){
//				postTweetInterface.postTweet();
//			}
//			break;
//		default:
//			break;
//		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onLeftMenuClick() {
		Intent intent = new Intent(getActivity(), CommunitySearchActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public void onRightMenuOneClick() {
//		if (rightPopupWindow != null) {
//			rightPopupWindow.show(topBarView);
//		}
		
		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		int left = (int)(dm.widthPixels - 115 * dm.density);
		
		FunctionPopView a = new FunctionPopView(getRootFragment());
		a.showAsDropDown(topBarView, left, 0);
	}
	@Override
	public void onCenterMenuItemClick(int position) {}
	@Override
	public void onRightMenuTwoClick() {}

	@Override
	public void onPopupWindowItemClick(String type, int position) {
//		if (type.equals("101")) {
//
//			switch (position) {
//			case 0:
//				Intent it2 = new Intent(getActivity(), TimelineBookListCommentsActivity.class);
//				it2.putExtra("type", TimelineBookListActivity.type[3]);
//				getRootFragment().startActivityForResult(it2, TimelineRootFragment.POST_TWEET);
//				break;
//			case 1:
//
//				Intent it1 = new Intent(getActivity(), TimelinePostTweetActivity.class);
//				it1.putExtra("title", getString(R.string.timeline_post_title));
//				getRootFragment().startActivityForResult(it1, TimelineRootFragment.POST_TWEET);
//				break;
//			}
//		}
	}
	
	/**
	  * 得到根Fragment
	  * 
	  * @return
	  */
	 public Fragment getRootFragment() {
	  Fragment fragment = getParentFragment();
	  if(fragment!=null){
		  while (fragment.getParentFragment() != null) {
			   fragment = fragment.getParentFragment();
			  }
	  }else
		  fragment=this;
	  return fragment;

	 }

	@Override
	public void onPopupWindowSubmenuItemCheck(String type, int checkid) {

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
	}
	
	
//	public PostTweetInterface getPostTweetInterface() {
//		return postTweetInterface;
//	}
//
//	public void setPostTweetInterface(PostTweetInterface postTweetInterface) {
//		this.postTweetInterface = postTweetInterface;
//	}

	/**
	 * 红点移动到某个位置
	 * 
	 * @param index
	 *            位置索引
	 */
	public void moveToPosition(int index) {

		if (tabs != null) {
			for (int i = 0; i < tabs.size(); i++) {
				TextView view = tabs.get(i);
				View dot=dotViewList.get(i);
				if (i != index) {
					view.setTextColor(getActivity()
									.getResources()
									.getColor(R.color.n_text_sub));
					TextPaint tpaint = view.getPaint();
					tpaint.setFakeBoldText(false);
					dot.setVisibility(View.GONE);
				} else {
					view.setTextColor(getActivity().getResources().getColor(
							R.color.red_sub));
					TextPaint tpaint = view.getPaint();
					tpaint.setFakeBoldText(true);
					dot.setVisibility(View.VISIBLE);
				}
			}
		}

	}
	
	public class CommunityPagerAdapter extends FragmentPagerAdapter {

		public CommunityPagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0: {// 广场
				SquareFragment fragment = new SquareFragment();
				return fragment;
			}
			case 1: {// 朋友圈
				friendCircleFragment = new FriendCircleFragment();
				friendCircleFragment.setRetainInstance(true);
				Bundle bundle = new Bundle();
				bundle.putBoolean(TimelineFragment.ENABLE_PULL_REFRESH, true);
				bundle.putBoolean(TimelineFragment.TIMELINE_ADAPTER, true);
				bundle.putParcelable(TimelineFragment.PARSER_CREATOR, new BaseParserCreator(TimelineURLParser.class, TimelineJSONParser.class));
				friendCircleFragment.setArguments(bundle);
				return friendCircleFragment;
			}
			case 2: {// 发现
				FindFragment fragment = new FindFragment();
				return fragment;
			}
			default:
				return new SquareFragment();
			}
		}

		@Override
		public int getCount() {
			return 3;
		}

	}

	
	public void onTabItemClick(int position) {
		if(pager!=null)
			pager.setCurrentItem(position);
	}
}
