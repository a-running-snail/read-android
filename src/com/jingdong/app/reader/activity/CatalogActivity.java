package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.view.View;

import com.jingdong.app.reader.bookmark.BookCatalogFragment;
import com.jingdong.app.reader.bookmark.BookMarksFragment;
import com.jingdong.app.reader.common.BaseFragmentActivityWithTopBar;
import com.jingdong.app.reader.plugin.pdf.outline.OutlineItem;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.ThemeUtils;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.jingdong.app.reader.R;

public class CatalogActivity extends BaseFragmentActivityWithTopBar implements TopBarViewListener{

	public static final String TOCLabelListKey = "TOCLabelListKey";
    public static final String BookNameKey = "BookNameKey";
    public static final String AuthorNameKey = "AuthorNameKey";
    public static final String ChapterIndexKey = "ChapterIndexKey";
    public static final String EbookIdKey = "EbookIdKey";
    public static final String DocumentIdKey = "DocumentIdKey";
    public static final String DOCUMENTSIGN = "DocumentSign";
	public static final String TOCSelectedIndexKey = "TOCSelectedIndexKey";
	public static final String TOCSelectedPageKey = "TOCSelectedPageKey";
	public static final String PageCalculatorFinish = "PageCalculatorFinish";
	public static final String PAGE_INDEX = "pageIndex";
	
	public static final int RESULT_CHANGE_PAGE = Activity.RESULT_FIRST_USER + 100;
    
	private ViewPager pager;
	private TopBarView topBarView;
	private CatalogAdapter adapter;
	
	private ArrayList<OutlineItem> outlineList;
    private String bookName;
    private String author;
    private int chapterIndex;
    private long ebookid;
    private int docid;
    private boolean isPageCalculatorFinish = false;
    private boolean isLandscape = false;
    
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		ThemeUtils.prepareTheme(this);
		setContentView(R.layout.activity_chapters_bookmarks_notes);
		topBarView = this.getTopBarView();
		topBarView.setTopBarTheme(ThemeUtils.getTopbarTheme());
		topBarView.setLeftMenuVisiable(false, 0);
		Display d = getWindowManager().getDefaultDisplay();
		Point s = new Point();
		d.getSize(s);
		if (s.x > s.y) {
			isLandscape = true;
			FrameLayout.LayoutParams frameParams = new FrameLayout.LayoutParams(s.x * 2 / 3, s.y);
			this.getRootView().setLayoutParams(frameParams);
		}
		initTopbarView();
		Intent intent = getIntent();
		outlineList = intent.getParcelableArrayListExtra(TOCLabelListKey);
        bookName = intent.getStringExtra(BookNameKey);
        author = intent.getStringExtra(AuthorNameKey);
        chapterIndex = intent.getIntExtra(ChapterIndexKey, 0);
        ebookid = intent.getLongExtra(EbookIdKey, 0);
        docid = intent.getIntExtra(DocumentIdKey, 0);
        isPageCalculatorFinish = intent.getBooleanExtra(PageCalculatorFinish, false);
 
		adapter=new CatalogAdapter(getSupportFragmentManager());
				
		pager = (ViewPager) findViewById(R.id.tabPager);
		pager.setAdapter(adapter);
		int position =LocalUserSetting.getLastCatalogPosition(CatalogActivity.this);
		pager.setCurrentItem(0);
		
		if(position!=0)
			{
			 topBarView.dotMoveToPosition(position);
			 topBarView.setCurrentPosition(position);
			 pager.setCurrentItem(position);
			}
		
		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				topBarView.dotMoveToPosition(position);
				LocalUserSetting.saveLastCatalogPosition(CatalogActivity.this,position);
			}
		});

	}
	
	private void initTopbarView() {
		if (topBarView == null)
			return;
		List<String> item = new ArrayList<String>();
		item.add("目录");
		item.add("书签");
		topBarView.setTitleItem(item);
		topBarView.setListener(this);
		topBarView.updateTopBarView();
	}
	
	
	class CatalogAdapter extends FragmentPagerAdapter{

		public CatalogAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
		
			if (i == 0) {//目录
				BookCatalogFragment fragment = new BookCatalogFragment();
				Bundle bundle=new Bundle();
				bundle.putParcelableArrayList(TOCLabelListKey, outlineList);
				bundle.putBoolean("isPageFinish", isPageCalculatorFinish);
				bundle.putInt("chapterIndex", chapterIndex);
				bundle.putString("bookname", bookName);
				bundle.putString("author", author);
				bundle.putInt("docid", docid);
				fragment.setArguments(bundle);
				return fragment;
			} else if (i == 1)// 书签
			{
				BookMarksFragment fragment = new BookMarksFragment();
				Bundle bundle=new Bundle();
				bundle.putParcelableArrayList(TOCLabelListKey, outlineList);
				bundle.putLong("ebookid", ebookid);
				bundle.putInt("docid", docid);
				fragment.setArguments(bundle);
				
				return fragment;
			}
			return null;
		}

		@Override
		public int getCount() {

			return 2;//目录和书签
		}
	}

	@Override
	public void onLeftMenuClick() {
	}

	

	@Override
	public void onCenterMenuItemClick(int position) {
		pager.setCurrentItem(position);
	}

	@Override
	public void onRightMenuOneClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRightMenuTwoClick() {
		// TODO Auto-generated method stub
		
	};
}
