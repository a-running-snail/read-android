package com.jingdong.app.reader.reading;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.CommonActivity;
import com.jingdong.app.reader.util.StringUtil;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.view.SearchTopBarView;
import com.jingdong.app.reader.view.SearchTopBarView.TopBarViewListener;

/**
 * 文内搜索
 *
 */
public class BookSearchActivity extends CommonActivity implements
		TopBarViewListener {
	
	public static final String ACTION_SEARCH = "com.jingdong.app.reader.reading.ACTION_SEARCH";
	public static final String ACTION_SEARCH_DONE = "com.jingdong.app.reader.reading.ACTION_SEARCH_DONE";
	public static final String ACTION_SEARCH_CANCEL = "com.jingdong.app.reader.reading.ACTION_SEARCH_CANCEL";
	public static final String ACTION_LOAD_SEARCH_DATA = "com.jingdong.app.reader.reading.ACTION_LOAD_SEARCH_DATA";
	
	public static final String PARA_INDEX = "paraIndex";
	public static final String CHAPTER_INDEX = "chapterIndex";
	public static final String OFFSET_IN_PARA = "offsetInPara";
	public static final String SEARCH_KEYWORDS = "search_keywords";
	public static final String LOAD_SEARCH_DATA = "load_search_data";
	public static final String SEARCH_RESULT = "search_result";
	public static final String SEARCH_DONE = "search_done";
	public static final String STOP_AND_CLEAR_SEARCH = "stop_and_clear_search";

	private static final int SEARCH_REFRESH_COUNT = 5;
	private SearchTopBarView topBarView = null;
	private EditText edittext_search;// 搜索关键字输入框
	private List<ReadSearchData> dataCache = new ArrayList<ReadSearchData>();
	private List<ReadSearchData> list = new ArrayList<ReadSearchData>();
	private SearchAdapter searchAdapter = null;
	private ListView mListView = null;
	private View searchNoResultView = null;
	private View loadMoreView = null;
	private View loadingView = null;
	private String keywords;
	private String filterSpecialWords;
	private boolean isRefreshSearch = true;
	private boolean isSearchDone = false;
	private boolean isLoadSearchData = false;
	private boolean isStopAndClearSearch = true;
	private int searchPosition = 0;
	private int paraIndex = 0;
	private int offsetInPara = 0;
	
	private SearchingReceiver receiver = new SearchingReceiver();
	
	class SearchingReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(ACTION_SEARCH_DONE)) {
				String word = intent.getStringExtra(SEARCH_KEYWORDS);
				if (keywords.equalsIgnoreCase(word)) {
					isSearchDone = intent.getBooleanExtra(SEARCH_DONE, false);
					ArrayList<ReadSearchData> array = intent.getParcelableArrayListExtra(SEARCH_RESULT);
					dataCache.addAll(array);
					if (isLoadSearchData) {
						return;
					}
					if (isRefreshSearch && dataCache.size() > SEARCH_REFRESH_COUNT) {
						mListView.setVisibility(View.VISIBLE);
						loadingView.setVisibility(View.GONE);
						isRefreshSearch = !loadSearchResult();
					}
					if (isSearchDone) {
						mListView.setVisibility(View.VISIBLE);
						loadMoreView.setVisibility(View.GONE);
						loadingView.setVisibility(View.GONE);
						if (dataCache.size() == 0) {
							mListView.setVisibility(View.GONE);
							searchNoResultView.setVisibility(View.VISIBLE);
						}
						if (list.size() < dataCache.size()) {
							loadSearchResult();
						}
					}
				}
			} else if (intent.getAction().equals(ACTION_LOAD_SEARCH_DATA)) {
				String word = intent.getStringExtra(SEARCH_KEYWORDS);
				ArrayList<ReadSearchData> array = intent.getParcelableArrayListExtra(SEARCH_RESULT);
				if (keywords.equals(word)) {
					int size = array.size();
					if (size == 0) {
						return;
					}
					if (dataCache.size() > 0) {
						int cacheIndex = dataCache.get(0).getChapterIndex();
						int last = array.get(size-1).getChapterIndex();
						if (cacheIndex <= last) {
							while (true) {
								int index = array.get(size - 1).getChapterIndex();
								if (index < cacheIndex) {
									break;
								} else {
									array.remove(size - 1);
									size--;
								}
								if (size <= 0) {
									break;
								}
							}
						}
					} else {
						isSearchDone = true;
					}
					list.addAll(array);
					searchPosition = findDataPositionInList(list, paraIndex, offsetInPara);
					mListView.setSelection(searchPosition);
					mListView.setVisibility(View.VISIBLE);
					loadingView.setVisibility(View.GONE);
					searchAdapter.notifyDataSetChanged();
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_epubbook_search);
		topBarView = (SearchTopBarView) findViewById(R.id.topbar);
		initTopbarView();
		// 初始化topbar 结束
		keywords = getIntent().getStringExtra(SEARCH_KEYWORDS);
		isLoadSearchData = getIntent().getBooleanExtra(LOAD_SEARCH_DATA, false);
		paraIndex = getIntent().getIntExtra(PARA_INDEX, -1);
		offsetInPara = getIntent().getIntExtra(OFFSET_IN_PARA, -1);
		mListView = (ListView) findViewById(R.id.list);
		edittext_search = (EditText) findViewById(R.id.edittext_serach);
		edittext_search.setHint(getString(R.string.bookshelf_search_text_hit));
		edittext_search.setText(keywords);
		edittext_search.requestFocus();
		edittext_search.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == KeyEvent.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					String text = edittext_search.getText().toString();
					if (TextUtils.isEmpty(text)) {
						ToastUtil.showToastInThread(R.string.bookshelf_search_text_hit);
						return true;
					}
					InputMethodManager inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					inputmanger.hideSoftInputFromWindow(v.getWindowToken(), 0);
					if (text.equals(keywords)) {
						return true;
					}
					if (!TextUtils.isEmpty(keywords) && !keywords.equals(text)) {
						cancelSearch();
						list.clear();
						dataCache.clear();
						isLoadSearchData = false;
						searchAdapter.notifyDataSetChanged();
					}
					keywords = text;
					startSearch();
					return true;
				}
				return false;
			}
		});
		loadingView = findViewById(R.id.loading);
		searchNoResultView = findViewById(R.id.read_search_no_result);
		View view = LayoutInflater.from(this).inflate(R.layout.listview_loadmore, null);
		loadMoreView = view.findViewById(R.id.load_more);
		mListView.addFooterView(view);
		searchAdapter = new SearchAdapter();
		searchAdapter.setHighlightColor(this.getResources().getColor(R.color.r_theme));
		mListView.setAdapter(searchAdapter);
		searchNoResultView.setVisibility(View.GONE);
		loadingView.setVisibility(View.GONE);
		//mListView.setSelection(searchPosition);
		//mListView.setSelectionFromTop(searchPosition, scrolledY);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//searchPosition = position;
				//searchPosition = mListView.getFirstVisiblePosition();
				//View v = mListView.getChildAt(0);
				//scrolledY = (v == null) ? 0 : v.getTop();
				isStopAndClearSearch = false;
				ReadSearchData data = (ReadSearchData) parent.getAdapter().getItem(position);
				Intent intent = new Intent();
				intent.putExtra(PARA_INDEX, data.getParaIndex());
				intent.putExtra(CHAPTER_INDEX, data.getChapterIndex());
				intent.putExtra(OFFSET_IN_PARA, data.getStartOffsetInPara());
				setResult(Activity.RESULT_OK, intent);
				finish();
			}
		});
		registerReceiver();
		startSearch();
	}
	
	private int findDataPositionInList(List<ReadSearchData> list, int paraIndex, int offsetInPara) {
		if (paraIndex < 0 || offsetInPara < 0) {
			return 0;
		}
		int index = 0;
		for (ReadSearchData data : list) {
			if (data.getParaIndex() == paraIndex
					&& data.getStartOffsetInPara() == offsetInPara) {
				break;
			}
			index ++;
		}
		if (index >= list.size()) {
			index = 0;
		}
		return index;
	}
	
	private void startSearch() {
		if (TextUtils.isEmpty(keywords)) {
			return;
		}
		filterSpecialWords = StringUtil.escapeExprSpecialWord(keywords);
		mListView.setVisibility(View.GONE);
		loadingView.setVisibility(View.VISIBLE);
		searchNoResultView.setVisibility(View.GONE);
		Intent intent = new Intent();
		intent.setAction(ACTION_SEARCH);
		intent.putExtra(SEARCH_KEYWORDS, keywords);
		intent.putExtra(LOAD_SEARCH_DATA, isLoadSearchData);
		LocalBroadcastManager.getInstance(BookSearchActivity.this).sendBroadcast(intent);
	}
	
	private void cancelSearch() {
		Intent intent = new Intent();
		intent.setAction(ACTION_SEARCH_CANCEL);
		intent.putExtra(STOP_AND_CLEAR_SEARCH, isStopAndClearSearch);
		LocalBroadcastManager.getInstance(BookSearchActivity.this).sendBroadcast(intent);
	}
	
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_SEARCH_DONE);
		filter.addAction(ACTION_LOAD_SEARCH_DATA);
		LocalBroadcastManager.getInstance(this).registerReceiver(receiver,
				filter);
	}

	private void initTopbarView() {
		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topBarView.setRightMenuVisiable(false);
		topBarView.setListener(this);
		topBarView.updateTopBarView(true);
	}
	
	private boolean loadSearchResult() {
		int count = dataCache.size();
		int i = list.size();
		int n = i + SEARCH_REFRESH_COUNT;
		if (n >= count) {
			if (isSearchDone) {
				n = count;
			} else {
				n = i;
			}
		}
		boolean isRefresh = false;
		for (; i < n; i++) {
			list.add(dataCache.get(i));
			isRefresh = true;
		}
		if (isRefresh) {
			searchAdapter.notifyDataSetChanged();
		}
		return isRefresh;
	}

	private class SearchAdapter extends BaseAdapter {
		
		private int highlightColor = 0;
		
		public void setHighlightColor(int color) {
			highlightColor = color;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(BookSearchActivity.this)
						.inflate(R.layout.search_epub_item, parent, false);
			}
			TextView chatpter_name = (TextView) ViewHolder.get(convertView,
					R.id.chatpter_name);
			TextView content = (TextView) ViewHolder.get(convertView,
					R.id.content);
			chatpter_name.setText(list.get(position).getTitle());
			SpannableString s = new SpannableString(list.get(position)
					.getSearchText());
			Pattern p = Pattern.compile(filterSpecialWords,Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(s);

			while (m.find()) {
				int start = m.start();
				int end = m.end();
				s.setSpan(new ForegroundColorSpan(highlightColor), start, end,
						Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			content.setText(s);
			
			if (position == list.size() - 1) {
				loadMoreView.setVisibility(isSearchDone ? View.GONE : View.VISIBLE);
				isRefreshSearch = !loadSearchResult();
			}
			return convertView;
		}

	}

	@Override
	public void onLeftMenuClick() {
		finish();
	}

	@Override
	public void onRightMenu_leftClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRightMenu_rightClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCenterMenuItemClick(int position) {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		cancelSearch();
		unregisterReceiver();
	}
	
	private void unregisterReceiver() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_yuedu_search));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_yuedu_search));
	}
	
}
