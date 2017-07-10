package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseFragmentActivityWithTopBar;
import com.jingdong.app.reader.util.ThemeUtils;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;


public class BookNoteActivity extends BaseFragmentActivityWithTopBar implements TopBarViewListener {

	public static final String TOCLabelListKey = "TOCLabelListKey";
	public static final String CHAPTER_ITEM_REF = "chapterItemRef";
	public static final String PARA_INDEX = "paraIndex";
	public static final String OFFSET_IN_PARA = "offsetInPara";
	public static final String BOOK_NAME = "bookName";
	public static final String USER_ID = "userId";
	public static final String EBOOK_ID = "ebookId";
	public static final String DOCUMENT_ID = "documentId";
	public static final String DOCUMENT_SIGN = "documentSign";
	
	public static final int ShowNoteListRequest = 100;
	
	private ViewPager pager;
	private TopBarView topBarView;
	private NoteAdapter adapter;
    
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		ThemeUtils.prepareTheme(this);
		setContentView(R.layout.activity_chapters_bookmarks_notes);
		topBarView = this.getTopBarView();
		topBarView.setTopBarTheme(ThemeUtils.getTopbarTheme());
		topBarView.setLeftMenuVisiable(false, 0);
		initTopbarView();
 
		adapter = new NoteAdapter(getSupportFragmentManager());
				
		pager = (ViewPager) findViewById(R.id.tabPager);
		pager.setAdapter(adapter);
		pager.setCurrentItem(0);
		pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				topBarView.dotMoveToPosition(position);
			}
		});
	}
	
	private void initTopbarView() {
		if (topBarView == null)
			return;
		List<String> item = new ArrayList<String>();
		item.add("社区笔记");
		item.add("我的笔记");
		topBarView.setTitleItem(item);
		topBarView.setListener(this);
		topBarView.updateTopBarView();
	}
	
	class NoteAdapter extends FragmentPagerAdapter{

		public NoteAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
		
			if (i == 0) {//社区笔记
				BookNoteForCommunity fragment = new BookNoteForCommunity();
				return fragment;
			} else if (i == 1) {//我的笔记
				BookNoteForMe fragment = new BookNoteForMe();
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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == ShowNoteListRequest && resultCode == RESULT_OK) {
			this.setResult(BookPageViewActivity.RESULT_OK, data);
			this.finish();
			this.overridePendingTransition(R.anim.right_in, R.anim.left_out);
		}
	}
	
	@Override
	public void onLeftMenuClick() {
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
	public void onCenterMenuItemClick(int position) {
		// TODO Auto-generated method stub
		pager.setCurrentItem(position);
	}

}
