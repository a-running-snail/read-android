package com.jingdong.app.reader.bookshelf;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.bookshelf.data.BookShelfDataHelper;
import com.jingdong.app.reader.bookshelf.data.BookShelfDataHelper.EBookItemHolder;
import com.jingdong.app.reader.bookshelf.inf.FetchChangduListListener;
import com.jingdong.app.reader.bookstore.search.BookStoreSearchActivity;
import com.jingdong.app.reader.common.CommonActivity;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.download.util.L;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.MyOnlineBookEntity;
import com.jingdong.app.reader.entity.extra.ChangduEbook;
import com.jingdong.app.reader.entity.extra.JDOnlineBookEntity;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.jingdong.app.reader.view.xlistview.XListView;
import com.jingdong.app.reader.view.xlistview.XListView.IXListViewListener;

public class ChangduListActivity extends CommonActivity implements TopBarViewListener,IXListViewListener{
	
	private TopBarView mTopBar;
	private EditText mEtSearch;
	private XListView mListView;
//	private RelativeLayout relativeLayout;
	private ImageView icon;
	private TextView textView;
	private Button lackbook_button;
	private Button changduButton;
	private int gotoflag = -1;
	private boolean noMoreBook = false;
	private int currentSearchPage = 1;
	private boolean inLoadingMore = true;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private boolean inSearch = false;
	private boolean isSearchCommited = false;
	private int currentPage = 1;
	private static int perPageCount = 10;
	private BookShelfDataHelper mDataHelper;
	private ArrayList<EBookItemHolder> mEBookItemList = new ArrayList<EBookItemHolder>();
	private DownloadableListAdapter mListAdapter;
	private EmptyLayout mEmptyLayout;

	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_changdu_list);
		initView();
		initData();
	}
	

	/**
	 * 
	 * @Title: initData
	 * @Description: 初始化数据
	 * @param 
	 * @return void
	 * @throws
	 */
	private void initData() {
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		getChangduList();
	}
	
	/**
	 * 
	 * @Title: initView
	 * @Description: 初始化View
	 * @param 
	 * @return void
	 * @throws
	 */
	private void initView() {
		//初始化topbar 开始
		setupTopbar();
		//初始化顶部搜索栏
		setupSearchBar();
		setupContentView();
	}

	/**
	 * 
	 * @Title: setupContentView
	 * @Description: 初始化内容视图
	 * @param 
	 * @return void
	 * @throws
	 */
	private void setupContentView() {
		mListView = (XListView) findViewById(R.id.list);
		mListView.setDivider(null);
		mListView.setDividerHeight(0);
		mListView.setBackgroundResource(R.color.book_store_bg);
		mListView.setSelector(R.drawable.list_longpress_transparent_bg);
		mListView.setPullLoadEnable(true);
		mListView.setPullRefreshEnable(false);
		mListView.setXListViewListener(this);
		
		//设置adapter
		mListAdapter = new DownloadableListAdapter(this,mEBookItemList);
		mListView.setAdapter(mListAdapter);
		
//		relativeLayout = (RelativeLayout) findViewById(R.id.search_result_container);
		mEmptyLayout = (EmptyLayout) findViewById(R.id.error_layout);
		mEmptyLayout.setErrorImagAndMsg(R.drawable.bookstore_icon_search_null, getString(R.string.book_sign_option));
		mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (gotoflag == 0) {
					Intent intent = new Intent(v.getContext(), LauncherActivity.class);
					intent.putExtra("lx", 0);
					startActivity(intent);
				} else if (gotoflag == 1) {
					Intent intent = new Intent(v.getContext(), BookStoreSearchActivity.class);
					startActivity(intent);
				}
			}
		});
//		changduButton = (Button) findViewById(R.id.chandu_button);
//		changduButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				Intent intent = new Intent(ChangduListActivity.this, WebViewActivity.class);
//				intent.putExtra(WebViewActivity.UrlKey, "http://e.m.jd.com/readCard.html");
//				intent.putExtra(WebViewActivity.TopbarKey, true);
//				intent.putExtra(WebViewActivity.BrowserKey, false);
//				intent.putExtra(WebViewActivity.TitleKey, "购买畅读卡");
//				startActivity(intent);
//			}
//		});

	}


	/**
	 * 
	 * @Title: getChangduList
	 * @Description:获取畅读卡列表数据
	 * @param 
	 * @return void
	 * @throws
	 */
	private void getChangduList() {
		BookShelfDataHelper.getInstance().fetchChangduList(this, currentPage, perPageCount, new FetchChangduListListener() {
			
			@Override
			public void onSuccess(ChangduEbook ebook) {
				//隐藏进度条
				mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
				if (ebook.resultList.size() == 0 && currentPage == 1) {
					mEmptyLayout.setErrorType(EmptyLayout.EMPTY_BOOKSHELF_SEARCH_RESULT);
//					mListView.setVisibility(View.GONE);
//					relativeLayout.setVisibility(View.VISIBLE);
//					changduButton.setVisibility(View.VISIBLE);
//					icon.setBackgroundResource(R.drawable.icon_empty);
//					textView.setText("您的畅读列表暂无书籍，如果您已经拥有畅读卡，就快去书城选购吧");
//					lackbook_button.setText("去书城");
				}
				currentPage++;
				if (ebook.resultList != null && currentPage > ebook.totalPage){
					//没有下一页数据了
					noMoreBook = true;
					mListView.setPullLoadEnable(false);
				}else {
					noMoreBook = false;
					mListView.setPullLoadEnable(true);
				}
				mListView.stopLoadMore();
				List<EBookItemHolder> all = new ArrayList<EBookItemHolder>();
				if (ebook.resultList.size() > 0) {
					String[] book_ids = new String[ebook.resultList.size()];
					for (int i = 0; i < ebook.resultList.size(); i++) {
						JDOnlineBookEntity book = ebook.resultList.get(i);
						book_ids[i] = String.valueOf(book.itemId);
					}
					List<LocalBook> allLocalBooks = MZBookDatabase.instance.getLocalBooks(null, book_ids);
					for (JDOnlineBookEntity book : ebook.resultList) {
						MyOnlineBookEntity entity = book.toMyOnlineBookEntity(book);
						EBookItemHolder itemHolder = BookShelfDataHelper.getInstance().checkBookState(allLocalBooks, entity);
						all.add(itemHolder);
					}
					mEBookItemList.addAll(all);
					mListAdapter.notifyDataSetChanged();
				}
			}
			
			@Override
			public void onFailure(String errorInfo) {
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			}
		});
	}

	/**
	 * 
	 * @Title: setupSearchBar
	 * @Description: 初始化搜索栏
	 * @param 
	 * @return void
	 * @throws
	 */
	private void setupSearchBar() {
		mEtSearch = (EditText) findViewById(R.id.edittext_serach);
		mEtSearch.setHint(getString(R.string.bookshelf_search_text_hit));
		mEtSearch.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == event.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					mEBookItemList.clear();
					mListAdapter.notifyDataSetChanged();
					isSearchCommited = true;
					inSearch = true;
					reset();
					executeSearch();
					return true;
				}
				return false;
			}
		});

		mEtSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (isSearchCommited && TextUtils.isEmpty(mEtSearch.getText().toString())) {
					mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
//					mListView.setVisibility(View.VISIBLE);
//					relativeLayout.setVisibility(View.GONE);
					mEBookItemList.clear();
					mListAdapter.notifyDataSetChanged();
					inSearch = false;
					reset();
					executeSearch();
					isSearchCommited = false;
				}
			}
		});
	}
	
	/**
	 * 
	 * @Title: executeSearch
	 * @Description: 执行搜索
	 * @param 
	 * @return void
	 * @throws
	 */
	protected void executeSearch() {
		String keyword = mEtSearch.getText().toString();
		BookShelfDataHelper.getInstance().searchChangduBook(ChangduListActivity.this, keyword, currentSearchPage, perPageCount, new FetchChangduListListener() {
			
			@Override
			public void onSuccess(ChangduEbook changduEntity) {
				if (changduEntity.resultList.size() == 0 && currentSearchPage == 1) {
					mEmptyLayout.setErrorType(EmptyLayout.EMPTY_BOOKSHELF_SEARCH_RESULT);
					return;
				}
				currentSearchPage++;
				if (changduEntity.resultList != null && currentSearchPage > changduEntity.totalPage){
					noMoreBookOnSearch = true;
					mListView.setPullLoadEnable(false);
				}else {
					noMoreBookOnSearch = false;
					mListView.setPullLoadEnable(true);
				}
				mListView.stopLoadMore();
				List<EBookItemHolder> all = new ArrayList<EBookItemHolder>();
				List<LocalBook> allLocalBooks = LocalBook.getLocalBookList(null, null);
				for (JDOnlineBookEntity book : changduEntity.resultList) {
					MyOnlineBookEntity entity = book.toMyOnlineBookEntity(book);
					EBookItemHolder itemHolder = BookShelfDataHelper.getInstance().checkBookState(allLocalBooks, entity);
					all.add(itemHolder);
				}
				mEBookItemList.addAll(all);
				if (mEBookItemList.size() == 0) {
					Toast.makeText(getApplicationContext(), "抱歉,没找到该书!", Toast.LENGTH_LONG).show();
				}
				mListAdapter.notifyDataSetChanged();
			}
			
			@Override
			public void onFailure(String errorInfo) {
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
			}
		});
	}


	private void reset() {
		currentPage = 1;
		noMoreBook = false;
		inLoadingMore = true;
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}


	/**
	 * 
	 * @Title: setupTopbar
	 * @Description: 初始化顶部栏
	 * @param 
	 * @return void
	 * @throws
	 */
	private void setupTopbar() {
		mTopBar = (TopBarView) findViewById(R.id.topbar);
		mTopBar.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		mTopBar.setListener(this);
		mTopBar.setTitle("畅读");
	}

	@Override
	public void onLeftMenuClick() {
		// TODO Auto-generated method stub
		finish();
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
		
	}


	@Override
	public void onRefresh() {
//		L.d("onRefresh");
	}


	@Override
	public void onLoadMore() {
		L.d("onLoadMore");
		getChangduList();
	}

}
