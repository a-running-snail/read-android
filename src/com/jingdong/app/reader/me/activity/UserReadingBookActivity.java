package com.jingdong.app.reader.me.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseFragmentActivityWithTopBar;
import com.jingdong.app.reader.entity.UserReadScale;
import com.jingdong.app.reader.entity.extra.UserReadedBook;
import com.jingdong.app.reader.entity.extra.UserReadedBookEntity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.view.TopBarView;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class UserReadingBookActivity extends BaseFragmentActivityWithTopBar{

	private ViewPager pager;
	private BookstorePagerAdapter pagerAdapter;
	private Context mContext;
	LinearLayout itemContainer;
	private TextView percentTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_user_readingbook);
		
		mContext= this;
		
		this.getTopBarView().setTitle("我读过的图书");
		
		initReadedBookPercentView();
		
		List<String> tabs = new ArrayList<String>();
		tabs.add("近一周书单");
		tabs.add("近一月书单");
		tabs.add("更早");
		
		itemContainer= (LinearLayout) findViewById(R.id.viewpaperTitle);
		
		LinearLayout view;
		for (int i = 0; i < tabs.size(); i++) {
			view = (LinearLayout) LayoutInflater.from(mContext)
					.inflate(R.layout.user_readingbook_tab, null);
			
			TextView itemTitle = (TextView) view
					.findViewById(R.id.item_text);
			ImageView itemDot = (ImageView) view
					.findViewById(R.id.item_dot);
			itemDot.setImageResource(R.drawable.red_dot);
			itemTitle.setText(tabs.get(i));
			final int postion = i;
			view.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					onTabItemClick(postion);
				}
			});
			itemContainer.addView(view);
		}
		dotMoveToPosition(0);
		
		pager = (ViewPager) findViewById(R.id.vPager);
		pagerAdapter = new BookstorePagerAdapter(getSupportFragmentManager());
		pager.setAdapter(pagerAdapter);
		pager.setOffscreenPageLimit(2);

		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				dotMoveToPosition(position);
			}
		});
	}
	
	/**
	 * 获取百分比并显示
	 */
	private void initReadedBookPercentView() {
		percentTextView = (TextView) findViewById(R.id.percent);
		
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool
				.getUserReadedBookPercent(),new MyAsyncHttpResponseHandler(mContext) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(mContext,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);
						UserReadScale userReadScale = GsonUtils.fromJson(result,UserReadScale.class);
						if(userReadScale!=null && userReadScale.getCode().equals("0")){
							percentTextView.setText(userReadScale.getScale()+"%");
						}
					}
				});
	}
	
//	private class UserReadScale{
//		private String	code;
//		private String scale;
//	}

	/**
	 * 红点移动到某个位置
	 * 
	 * @param index
	 *            位置索引
	 */
	public void dotMoveToPosition(int index) {

		if (itemContainer != null) {

			for (int i = 0; i < itemContainer.getChildCount(); i++) {
				View view = itemContainer.getChildAt(i);
				ImageView itemDot = (ImageView) view
						.findViewById(R.id.item_dot);
				TextView itemTitle = (TextView) view
						.findViewById(R.id.item_text);
				if (i != index) {
					itemTitle
							.setTextColor(mContext
									.getResources()
									.getColor(R.color.n_text_sub));
					TextPaint tpaint = itemTitle.getPaint();
					tpaint.setFakeBoldText(false);
					itemDot.setVisibility(View.INVISIBLE);
				} else {
					itemTitle.setTextColor(mContext.getResources().getColor(
							R.color.red_sub));
					TextPaint tpaint = itemTitle.getPaint();
					tpaint.setFakeBoldText(true);
					itemDot.setVisibility(View.VISIBLE);
				}
			}
		}

	}

	public class BookstorePagerAdapter extends FragmentPagerAdapter {

		public BookstorePagerAdapter(FragmentManager fragmentManager) {
			super(fragmentManager);
		}

		@Override
		public Fragment getItem(int i) {
			switch (i) {
			case 0: {// 近一周
				UserReadingBookFragment fragment = new UserReadingBookFragment("week");
				return fragment;
			}
			case 1: {// 近一月
				UserReadingBookFragment fragment = new UserReadingBookFragment("month");
				return fragment;
			}
			case 2: {// 更早
				UserReadingBookFragment fragment = new UserReadingBookFragment("earlier");
				return fragment;
			}
			default:
				return new UserReadingBookFragment("week");
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
