package com.jingdong.app.reader.bookstore.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jd.voice.jdvoicesdk.JdVoiceConfig;
import com.jd.voice.jdvoicesdk.JdVoiceRecogner;
import com.jd.voice.jdvoicesdk.JdVoiceRecognitionLintener;
import com.jd.voice.jdvoicesdk.entity.ResultEntity;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.LackBookSignActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookstore.search.adapter.BookSearchListAdapter;
import com.jingdong.app.reader.bookstore.search.adapter.SearchKeyWordAdapter;
import com.jingdong.app.reader.entity.extra.JDBook;
import com.jingdong.app.reader.entity.extra.JDBookDetail;
import com.jingdong.app.reader.entity.extra.SearchKeyWord;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.Base64;
import com.jingdong.app.reader.util.CommonUtil;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.KeyBoardUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.ScreenUtils;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.util.UserGuiderUtil;
import com.jingdong.app.reader.view.SearchTopBarView;
import com.jingdong.app.reader.view.SearchTopBarView.TopBarViewListener;
import com.jingdong.app.reader.view.xlistview.XListView;
import com.jingdong.app.reader.view.xlistview.XListView.IXListViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;

@SuppressWarnings("deprecation")
public class BookStoreSearchActivity extends Activity implements TopBarViewListener, JdVoiceRecognitionLintener, IXListViewListener {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
	private SearchTopBarView topBarView = null;
	private AlertDialog.Builder dialogBuilder;
	private Handler handler;
	private static final int SCAN = 0;
	private AlertDialog listDialog;
	// private AutoCompleteTextView autoCompleteTextView;// 搜索框关键字
	private ArrayList<String> historyList = new ArrayList<String>(3);// 搜索历史记录
	private ListView mListView = null;
	private SearchKeyWordAdapter searchKeyWordAdapter = null;
	private List<SearchKeyWord> searchKeyWordslist = null;
	private LinearLayout.LayoutParams lp1 = null;
	private EditText edittext_serach;// 搜索关键字输入框
	private List<String> hotkeylist = null;// 热词
	private int total = 10;
	private static int perPageCount = 10;

	private int currentSearchPage = 1;
	private int perSearchCount = 10;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;

	private XListView booklist = null;
	private boolean inSearch = false;

	private List<JDBookDetail> jdBookList = new ArrayList<JDBookDetail>();
	private List<JDBookDetail> jdBookList2 = new ArrayList<JDBookDetail>();
	private BookSearchListAdapter mListAdapter;
	private LinearLayout linearLayout;
	private ListView book_title_listView;
	private int searchflag = -1;// 判断是何种搜索
	private List<String> stringList = new ArrayList<String>();
	private JDBook ebook = null;


	private final static int SCANNIN_GREQUEST_CODE = 1;
	private static final int VOICE_TAG = 100;
	private final String BOOKSTORE_HISTORY = "bookstore_history"; 

	private Button lack_button;

	private Button statueButton;

	public static int foucesFlag = -1;// 判断是否触发EditText改变事件

	private String bookshelfkey;
	private Button voiceBtn;
	// private TextView voiceText;
	private View rcChat_popup;

	private LinearLayout voice_rcd_hint_loading, voice_rcd_hint_rcding, voice_rcd_hint_tooshort;
	private ImageView volume;
	JdVoiceRecogner recogner = null;
	private int bookType = -1;
	private int Flag = -1;
	private boolean isVisibleBtns = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_store_search);
		handler = new Handler();
		// 初始化topbar 开始
		topBarView = (SearchTopBarView) findViewById(R.id.topbar);
		initTopbarView();
		// 初始化topbar 结束
		initView();
		final JdVoiceConfig config = new JdVoiceConfig();
		config.setType(JdVoiceConfig.TYPE_RECOGTION_SEARCH);
		config.setAutoRecognition(true);
		// config.setType(10005);
		config.setTimeOut(50000);
		recogner = JdVoiceRecogner.getInstance(this);
		recogner.setRecognitionLintener(this, config);
	}

	@SuppressWarnings("unused")
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		if (!LocalUserSetting.isBookStoreSearchGuidShow(BookStoreSearchActivity.this)) {
			ImageView imageView = new ImageView(BookStoreSearchActivity.this);
			imageView.setImageResource(R.drawable.search_barcode_guide);
			UserGuiderUtil userGuiderUtil = new UserGuiderUtil(BookStoreSearchActivity.this, imageView, false,false, true, false, null);
			LocalUserSetting.saveBookStoreSearchGuidShow(BookStoreSearchActivity.this);
		}
		super.onWindowFocusChanged(hasFocus);
	}

	public void initData() {
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}

	private void initView() {
		// autoCompleteTextView = (AutoCompleteTextView)
		// findViewById(R.id.searchtext_autoComplete);

		book_title_listView = (ListView) findViewById(R.id.book_title_listview);
		booklist = (XListView) findViewById(R.id.booklist);
		mListView = (ListView) findViewById(R.id.mlistview);
		mLoadingView = findViewById(R.id.book_store_search_loading_fl);
		mPb = (ProgressBar) findViewById(R.id.book_store_search_pb);
		mSearchResultContainer = (RelativeLayout) findViewById(R.id.book_store_search_result_container);

		linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
		lack_button = (Button) findViewById(R.id.lackbook_button);

		edittext_serach = (EditText) findViewById(R.id.edittext_serach);
		// 设置输入框获取焦点
		edittext_serach.setFocusable(true);
		edittext_serach.setFocusableInTouchMode(true);
		edittext_serach.requestFocus();
		

		// edittext_serach.requestFocus();
		Intent intent = getIntent();
		bookshelfkey = intent.getStringExtra("key");

		// header = LayoutInflater.from(getApplicationContext()).inflate(
		// R.layout.book_list_item, null);
		// bookTitle = (TextView) header.findViewById(R.id.user_book_name);
		// bookAuthor = (TextView) header.findViewById(R.id.user_book_author);
		// bookCover = (ImageView) header.findViewById(R.id.user_book_cover);
		// bookInfo = (TextView) header.findViewById(R.id.book_info);
		// statueButton = (Button) header.findViewById(R.id.statueButton);
		// book_title_listView.addHeaderView(header);

		if (bookshelfkey != null) {
			setEditText(bookshelfkey);
			searchSubmit(bookshelfkey, 0, false, true);
		} else {
			initLists();
		}

		// header.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// if (jdBookList2.size() > 0) {
		// storeHistory(jdBookList2.get(0).getName(), false);
		// Intent intent = new Intent(BookStoreSearchActivity.this,
		// BookInfoNewUIActivity.class);
		// intent.putExtra("bookid", jdBookList2.get(0).getEbookId());
		// startActivity(intent);
		// }
		// }
		// });

		book_title_listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				setEditText(jdBookList2.get(position).getName());
				searchSubmit(edittext_serach.getText().toString(), 0, false, true);
				// Intent intent = new Intent(BookStoreSearchActivity.this,
				// BookInfoNewUIActivity.class);
				// intent.putExtra("bookid", jdBookList2.get(position)
				// .getEbookId());
				// startActivity(intent);
			}
		});

		dialogBuilder = new AlertDialog.Builder(this);

		lack_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(BookStoreSearchActivity.this, LackBookSignActivity.class);
				intent.putExtra("bookname", edittext_serach.getText().toString());
				startActivity(intent);
			}
		});

		edittext_serach.setOnKeyListener(new View.OnKeyListener() {

			@SuppressWarnings("static-access")
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == event.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					searchWordReport(edittext_serach.getText().toString());
					searchSubmit(edittext_serach.getText().toString(), 0, false, true);
					return true;
				}
				return false;
			}
		});

		edittext_serach.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					if (!historyList.isEmpty()) {
						// refreshSearchAdapter(edittext_serach.getText()
						// .toString());
					}
				}
			}
		});
		// edittext_serach.set
		edittext_serach.setLongClickable(true);
		edittext_serach.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					if (TextUtils.isEmpty(edittext_serach.getText().toString())) {
						if (historyList != null)
							historyList.clear();
						initLists();
//						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//						imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
					} else {
						searchSubmit(edittext_serach.getText().toString(), 1, !isVoiceSearch, false);
						if (isVoiceSearch) {
							isVoiceSearch = false;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		rcChat_popup = this.findViewById(R.id.rcChat_popup);
		voice_rcd_hint_rcding = (LinearLayout) this.findViewById(R.id.voice_rcd_hint_rcding);
		voice_rcd_hint_loading = (LinearLayout) this.findViewById(R.id.voice_rcd_hint_loading);
		voice_rcd_hint_tooshort = (LinearLayout) this.findViewById(R.id.voice_rcd_hint_tooshort);
		volume = (ImageView) this.findViewById(R.id.volume);
	}

	public void initLists() {
		searchKeyWordslist = new ArrayList<SearchKeyWord>();
		historyList = readSearchHistory();
		getHotKeyWords();
	}

	private void initTopbarView() {
		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topBarView.setRightMenuVisiable(true, R.drawable.icon_voice, R.drawable.btn_bar_scan);
		topBarView.setListener(this);
		topBarView.updateTopBarView(false);
	}

	private void initSearchKeyWorddapter() {
		lp1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) ScreenUtils.getWidthJust(getApplicationContext()) * 2 / 19);
		// 判断是否隐藏删除控件
		if (historyList != null && historyList.size() > 0) {
			searchKeyWordAdapter = new SearchKeyWordAdapter(BookStoreSearchActivity.this, searchKeyWordslist, lp1, true);
		} else {
			searchKeyWordAdapter = new SearchKeyWordAdapter(BookStoreSearchActivity.this, searchKeyWordslist, lp1, false);
		}
		mListView.setAdapter(searchKeyWordAdapter);
	}

	// 获取搜索热词
	private void getHotKeyWords() {
		WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool.getHotKeywordParams(String.valueOf(total),null), new MyAsyncHttpResponseHandler(
				BookStoreSearchActivity.this) {

			@Override
			public void onStart() {
				super.onStart();
				mLoadingView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				Toast.makeText(BookStoreSearchActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
				mLoadingView.setVisibility(View.GONE);
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				mLoadingView.setVisibility(View.GONE);
				String result = new String(responseBody);
				try {
					JSONObject jsonObj = new JSONObject(result);
					JSONArray array = null;
					if (jsonObj != null) {
						String code = jsonObj.optString("code");
						if (code.equals("0")) {
							array = jsonObj.getJSONArray("keywords");
							hotkeylist = new ArrayList<String>();
							for (int i = 0; i < array.length(); i++) {
								hotkeylist.add(array.getString(i));
							}
							mHandler.sendMessage(mHandler.obtainMessage(0));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	/*******
	 * @author ThinkinBunny
	 * @since 2013年6月9日16:53:44
	 * @return historyList 读取搜索历史，在加载页面的第一次读取
	 * ********/
	private ArrayList<String> readSearchHistory() {

		String history = LocalUserSetting.getStringValueByKey(getApplicationContext(), getBookstoreHistoryKey());
		String[] historyArray = history.split(" ");
		for (int i = 0; i < historyArray.length; i++) {
			try {
				String string = new String(Base64.decode(historyArray[i]));
				if (!TextUtils.isEmpty(string)) {
					historyList.add(string);
				}

			} catch (IOException e) {
			}
		}
		return historyList;
	};
	
	private String getBookstoreHistoryKey(){
		if(!LoginUser.isLogin())
			return "noUserLogin"+"_"+ BOOKSTORE_HISTORY;
		return LoginUser.getpin()+"_"+ BOOKSTORE_HISTORY;
	}

	/******
	 * 保存历史搜索，按照先进先出的原则保存10条
	 * 
	 * @author ThinkinBunny
	 * @since 2013年6月9日17:53:51
	 * @param keyWord可以为null
	 *            ，为null是强制保存
	 * ********/
	public void storeHistory(String keyWord, boolean isRemoveKW) {
		historyList.trimToSize();
		if (isRemoveKW) {
			historyList.remove(keyWord);
		} else {
			if (historyList.contains(keyWord)) {
				String tmpKeyWordString = keyWord;
				historyList.remove(keyWord);
				historyList.add(0, tmpKeyWordString);
			} else if (historyList.size() < 10) {
				historyList.add(0, keyWord);
			} else {
				historyList.remove(historyList.size() - 1);
				historyList.add(0, keyWord);
			}
		}

		String historyString = "";
		for (int i = 0; i < historyList.size(); i++) {
			historyString = historyString + " " + Base64.encodeBytes(historyList.get(i).getBytes());
		}
		LocalUserSetting.saveStringValueByKey(BookStoreSearchActivity.this, getBookstoreHistoryKey(), historyString);

		if (historyList.isEmpty()) {
			mListView.setVisibility(View.GONE);
		}
		if (bookshelfkey == null && searchKeyWordAdapter != null) {
			searchKeyWordAdapter.notifyDataSetChanged();// 更新完数据通知数据变化
		}
	}

	/**
	 * 
	 * Name: 搜索提交，参数为搜索关键字
	 * 
	 * @return: null
	 * 
	 *          Description:
	 */
	public void searchSubmit(String keyWord, int flag, boolean isSave, boolean isVisibleBtn) {
		if (NetWorkUtils.isNetworkConnected(BookStoreSearchActivity.this)) {
			if (TextUtils.isEmpty(keyWord) && keyWord.trim().length() == 0) {// 空判断
				Toast.makeText(getApplicationContext(), "请输入您要搜索的内容!", Toast.LENGTH_LONG).show();
				return;
			}
			
			TalkingDataUtil.onBookStoreEvent(this, "搜索", "关键字:" + keyWord);
			if (foucesFlag == -1) {
				storeHistory(keyWord, isSave);
			}
			if (flag == 0) {//搜索图书
				initStoreBook(keyWord, flag, isVisibleBtn);
			} else if (flag == 1) {//搜索热词
				if (foucesFlag == -1) {
					search_Title(keyWord, flag, isVisibleBtn);
				}
				foucesFlag = -1;
			}
		} else {
			Toast.makeText(getApplicationContext(), "请检查您的网络是否可用!", Toast.LENGTH_LONG).show();
		}
	}

	private void search_Title(String key, final int flag, boolean isVisibleBtn) {

		if (stringList != null) {
			stringList.clear();
		}
		if (jdBookList2 != null) {
			jdBookList2.clear();
		}
		inSearch = true;
		initData();
		searchBooks(key, flag, isVisibleBtn);
	}

	/**
	 * 搜索书城图书
	 * @param key
	 * @param flag
	 * @param isVisibleBtn
	 */
	private void initStoreBook(String key, final int flag, final boolean isVisibleBtn) {

		if (jdBookList != null) {
			jdBookList.clear();
		}
		inSearch = true;
		initData();
		searchBooks(key, flag, isVisibleBtn);

		booklist.setVisibility(View.VISIBLE);
		mListView.setVisibility(View.GONE);
		linearLayout.setVisibility(View.GONE);
		book_title_listView.setVisibility(View.GONE);
		booklist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String bookid = "";
				JDBookDetail eBook = jdBookList.get(position - 1);
				Intent intent = new Intent(BookStoreSearchActivity.this, BookInfoNewUIActivity.class);
				if (eBook.isEBook()) {
					bookid = "" + eBook.getEbookId();
					intent.putExtra("bookid", eBook.getEbookId());
				} else {
					bookid = "" + eBook.getPaperBookId();
					intent.putExtra("bookid", eBook.getPaperBookId());
				}
				searchBookReport(edittext_serach.getText().toString(), bookid);
				startActivity(intent);
			}
		});

		Flag = flag;
		isVisibleBtns = isVisibleBtn;

		booklist.setPullLoadEnable(false);
		booklist.setPullRefreshEnable(false);
		booklist.setXListViewListener(this);
		// bookListAdapter = new BookListAdapter(BookStoreSearchActivity.this);
		mListAdapter = new BookSearchListAdapter(BookStoreSearchActivity.this, jdBookList);
		booklist.setAdapter(mListAdapter);
		KeyBoardUtils.closeKeybord(edittext_serach, BookStoreSearchActivity.this);
	}

	/**
	 * 
	 * @param key
	 * @param flag
	 * @param isVisibleBtn
	 */
	private synchronized void searchBooks(final String key, final int flag, final boolean isVisibleBtn) {

		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSearchStoreEbookParams(currentSearchPage + "", perSearchCount + "", key, ""),
				new MyAsyncHttpResponseHandler(BookStoreSearchActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						Toast.makeText(BookStoreSearchActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

						String result = new String(responseBody);

						ebook = GsonUtils.fromJson(result, JDBook.class);
						
						if (ebook != null && ((ebook.getCode().equals("51") || ebook.getCode().equals("0")) && ebook.getResultCount() == 0)) {
							if (flag == 0) {
								bookType = 1;
								initData();
								searchPaperBooks(key, flag, isVisibleBtn);
							} else if (flag == 1) {
								if (isVisibleBtn) {
									mHandler.sendMessage(mHandler.obtainMessage(2));
								} else {
									mHandler.sendMessage(mHandler.obtainMessage(5));
								}
							}
							return;
						} else if (ebook == null) {
							onLoadComplete();
							return;
						}

						if (ebook != null && ebook.getCode().equals("0")) {
							bookType = 0;
							currentSearchPage++;

							if (ebook.getBookList() != null && currentSearchPage >= ebook.totalPage) {
								if (flag == 0) {
									Toast.makeText(BookStoreSearchActivity.this, "继续向上滑动搜索纸书", Toast.LENGTH_LONG).show();
									booklist.setPullLoadEnable(true);
									bookType = 1;
									initData();
								} else if (flag == 1) {
									noMoreBookOnSearch = true;
									booklist.setPullLoadEnable(false);
								}
							} else {
								noMoreBookOnSearch = false;
								booklist.setPullLoadEnable(true);
							}

							if (flag == 0) {
								List<JDBookDetail> all = new ArrayList<JDBookDetail>();
								for (int i = 0; i < ebook.getBookList().size(); i++) {
									JDBookDetail book = ebook.getBookList().get(i);
									all.add(book);
								}
								jdBookList.addAll(all);
								mListAdapter.notifyDataSetChanged();
								onLoadComplete();
								mHandler.sendMessage(mHandler.obtainMessage(1));
							} else if (flag == 1) {
								List<String> str = new ArrayList<String>();
								if (jdBookList2 != null) {
									jdBookList2.clear();
								}
								List<JDBookDetail> all2 = new ArrayList<JDBookDetail>();
								for (int j = 0; j < ebook.getBookList().size(); j++) {
									JDBookDetail book = ebook.getBookList().get(j);
									all2.add(book);
								}

								jdBookList2.addAll(all2);
								if (stringList != null) {
									stringList.clear();
								}
								for (int i = 0; i < ebook.getBookList().size(); i++) {
									String booktitle = ebook.getBookList().get(i).getName();
									str.add(booktitle);
								}
								stringList.addAll(str);
								mHandler.sendMessage(mHandler.obtainMessage(4));
							}
						}
						inLoadingMoreOnSearch = false;
//						//如果输入法在窗口上已经显示，则隐藏，反之则显示
//						CommonUtil.toggleSoftInput(BookStoreSearchActivity.this);
//						KeyBoardUtils.closeKeybord(edittext_serach, BookStoreSearchActivity.this);
					}
				});
	}

	private synchronized void searchPaperBooks(String key, final int flag, final boolean isVisibleBtn) {

		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSearchStoreEbookParams(currentSearchPage + "", perSearchCount + "", key, 1 + ""),
				new MyAsyncHttpResponseHandler(BookStoreSearchActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						Toast.makeText(BookStoreSearchActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

						String result = new String(responseBody);
						ebook = GsonUtils.fromJson(result, JDBook.class);

						if (ebook != null
								&& (ebook.getCode().equals("51") || (ebook.getCode().equals("0") && (ebook.getResultCount() + jdBookList.size()) == 0))) {

							if (isVisibleBtn) {
								mHandler.sendMessage(mHandler.obtainMessage(2));
							} else {
								mHandler.sendMessage(mHandler.obtainMessage(5));
							}
							return;
						} else if (ebook == null) {
							onLoadComplete();
							return;
						}

						if (ebook != null && ebook.getCode().equals("0")) {

							currentSearchPage++;

							if (ebook.getBookList() != null && currentSearchPage >= ebook.totalPage) {
								noMoreBookOnSearch = true;
								booklist.setPullLoadEnable(false);
							} else {
								noMoreBookOnSearch = false;
								booklist.setPullLoadEnable(true);
							}

							if (flag == 0) {
								List<JDBookDetail> all = new ArrayList<JDBookDetail>();
								for (int i = 0; i < ebook.getBookList().size(); i++) {
									JDBookDetail book = ebook.getBookList().get(i);
									all.add(book);
								}
								jdBookList.addAll(all);
								mListAdapter.notifyDataSetChanged();
								onLoadComplete();
								mHandler.sendMessage(mHandler.obtainMessage(1));
							} else if (flag == 1) {
								List<String> str = new ArrayList<String>();
								if (jdBookList2 != null) {
									jdBookList2.clear();
								}
								List<JDBookDetail> all2 = new ArrayList<JDBookDetail>();
								for (int j = 0; j < ebook.getBookList().size(); j++) {
									JDBookDetail book = ebook.getBookList().get(j);
									all2.add(book);
								}

								jdBookList2.addAll(all2);
								if (stringList != null) {
									stringList.clear();
								}
								for (int i = 0; i < ebook.getBookList().size(); i++) {
									String booktitle = ebook.getBookList().get(i).getName();
									str.add(booktitle);
								}
								stringList.addAll(str);
								mHandler.sendMessage(mHandler.obtainMessage(4));
							}
						}
						inLoadingMoreOnSearch = false;
					}

				});
	}

	/**********
	 * 在搜索框输入时刷新列表，进用于搜索历史刷新
	 * 
	 * @author ThinkinBunny
	 * @since 2013年6月15日9:30:24
	 * @param str
	 *            edittext接受的关键词
	 * ****/
	public void refreshSearchAdapter(String str) {

		ArrayList<String> tmpArrayList = new ArrayList<String>();
		for (String packageItem : historyList) {
			if (packageItem.contains(str)) {
				tmpArrayList.add(packageItem);
			}
		}
		searchKeyWordAdapter.notifyDataSetInvalidated();
		if (tmpArrayList != null) {
			tmpArrayList.clear();
		}
		searchKeyWordAdapter.notifyDataSetChanged();
	}

	@Override
	public void onLeftMenuClick() {
		finish();
	}

	boolean isRecording = false;

	// 语音搜索
	@Override
	public void onRightMenu_leftClick() {
		// ShowTools.toastInThread(JMAHelper.KEY_BookStore_VOICE_SEARCH_CLICK);
		TalkingDataUtil.onBookStoreEvent(this, "搜索", "语音搜索");
		// showDialog();

		if (!isRecording) {
			recogner.start();
			voice_rcd_hint_loading.setVisibility(View.GONE);
			isRecording = true;
		} else {
			recogner.stop();
			rcChat_popup.setVisibility(View.GONE);
			isRecording = false;
		}

		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen = imm.isActive();
		if (isOpen) {
			imm.hideSoftInputFromWindow(edittext_serach.getWindowToken(), 0); // 强制隐藏键盘
		}

	}

	@Override
	protected void onDestroy() {
		JdVoiceRecogner.releaseInstance();
		super.onDestroy();
	}

	// 条形码搜索
	@Override
	public void onRightMenu_rightClick() {
		TalkingDataUtil.onBookStoreEvent(this, "搜索", "条形码搜索");
		scan();
	}

	@Override
	public void onCenterMenuItemClick(int position) {

	}

	private void showDialog() {
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			startVoiceRecognitionActivity();
		} else {
			dialogBuilder.setTitle(R.string.voice_search_title);
			dialogBuilder.setMessage(R.string.voice_search_message_hint);
			dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}

			});
			BookStoreSearchActivity.this.post(new Runnable() {

				@Override
				public void run() {

					dialogBuilder.show();

				}

			});
		}
	}

	/**
	 * Fire an intent to start the speech recognition activity.
	 */
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "");
		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	/**
	 * 统一 post 接口
	 */
	public void post(final Runnable action) {
		// Log.i("zhoubo", "handler==="+handler);
		handler.post(new Runnable() {
			@Override
			public void run() {
				if (BookStoreSearchActivity.this.isFinishing()) {
					return;
				}
				action.run();
			}
		});
	}

	/**
	 * Handle the results from the recognition activity.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			// Fill the list view with the strings the recognizer thought it
			// could have heard
			ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			final String[] items = new String[matches.size()];
			for (int i = 0; i < matches.size(); i++) {
				items[i] = matches.get(i);
			}
			dialogBuilder.setTitle(R.string.voice_search_please_choose);
			dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					MZLog.d("cj", "content:" + which + " " + items[which] + " ");

					String keyWord = items[which];
					edittext_serach.setText(keyWord);
					// 切换后将EditText光标置于末尾
					edittext_serach.postInvalidate();
					CharSequence charSequence = edittext_serach.getText();
					if (charSequence instanceof Spannable) {
						Spannable spanText = (Spannable) charSequence;
						Selection.setSelection(spanText, charSequence.length());
					}
					searchSubmit(keyWord, 0, false, true);

					listDialog.dismiss();
				}

			});
			BookStoreSearchActivity.this.post(new Runnable() {

				@Override
				public void run() {

					listDialog = dialogBuilder.show();

				}

			});
		} else if (requestCode == SCANNIN_GREQUEST_CODE && resultCode == Activity.RESULT_OK) {
			Bundle bundle = data.getExtras();

			String query = bundle.getString("result");
			edittext_serach.setText(query);
			searchSubmit(query, 0, false, true);
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	// 条形码扫描
	private void scan() {
		Intent intent = new Intent();
		intent.setClass(BookStoreSearchActivity.this, MipcaCaptureActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, SCANNIN_GREQUEST_CODE);
	}


	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				if (hotkeylist != null) {

					mSearchResultContainer.setVisibility(View.VISIBLE);

					if (historyList != null && historyList.size() > 0) {
						SearchKeyWord searchKeyWord1 = new SearchKeyWord();
						searchKeyWord1.setTitle("历史搜索");
						searchKeyWord1.setData(historyList);
						searchKeyWordslist.add(searchKeyWord1);
					}
					SearchKeyWord searchKeyWord2 = new SearchKeyWord();
					searchKeyWord2.setTitle("热门搜索");
					searchKeyWord2.setData(hotkeylist);
					searchKeyWordslist.add(searchKeyWord2);

					mListView.setVisibility(View.VISIBLE);
					booklist.setVisibility(View.GONE);
					linearLayout.setVisibility(View.GONE);
					book_title_listView.setVisibility(View.GONE);

					initSearchKeyWorddapter();
//					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//					imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				}

				break;
			case 1:
				mListView.setVisibility(View.GONE);
				booklist.setVisibility(View.VISIBLE);
				linearLayout.setVisibility(View.GONE);
				book_title_listView.setVisibility(View.GONE);
				// mListAdapter.notifyDataSetChanged();
				break;
			case 2:
				mListView.setVisibility(View.GONE);
				booklist.setVisibility(View.GONE);
				linearLayout.setVisibility(View.VISIBLE);
				lack_button.setVisibility(View.VISIBLE);
				book_title_listView.setVisibility(View.GONE);
				break;
			case 3:
				mListView.setVisibility(View.VISIBLE);
				booklist.setVisibility(View.GONE);
				linearLayout.setVisibility(View.GONE);
				book_title_listView.setVisibility(View.GONE);
				break;
			case 4:
				if (!TextUtils.isEmpty(edittext_serach.getText().toString()) && ebook != null) {
					if (ebook.getBookList().size() > 0) {
						// bookTitle.setText(ebook.getBookList().get(0).getName());
						// bookAuthor
						// .setText("null".equals(ebook.getBookList()
						// .get(0).getAuthor()) ?
						// getString(R.string.author_unknown)
						// : ebook.getBookList().get(0)
						// .getAuthor());
						// String info = ebook.getBookList().get(0).getInfo();
						// if (info != null) {
						// info = info.replaceAll("^[　 ]*", "");
						// info = info.replaceAll("\\s+", "");
						// bookInfo.setText(info);
						// } else {
						// bookInfo.setText("");
						// }
						//
						// ImageLoader.getInstance().displayImage(
						// ebook.getBookList().get(0).getImageUrl(),
						// bookCover,
						// GlobalVarable.getCutBookDisplayOptions());
						if (stringList != null && stringList.size() > 0)
							// stringList.remove(0);
							mListView.setVisibility(View.GONE);
						booklist.setVisibility(View.GONE);
						linearLayout.setVisibility(View.GONE);
						book_title_listView.setVisibility(View.VISIBLE);
						book_title_listView.setAdapter(new ArrayAdapter<String>(BookStoreSearchActivity.this, R.layout.book_title_string, stringList));
					}
				}
				break;
			case 5:
				mListView.setVisibility(View.GONE);
				booklist.setVisibility(View.GONE);
				linearLayout.setVisibility(View.VISIBLE);
				lack_button.setVisibility(View.VISIBLE);
				book_title_listView.setVisibility(View.GONE);
				break;
			default:
				break;
			}
		}

	};
	private View mLoadingView;
	private ProgressBar mPb;
	private RelativeLayout mSearchResultContainer;
	private boolean isVoiceSearch = false;

	public void setEditText(String str) {
		edittext_serach.setText(str);
		// 切换后将EditText光标置于末尾
		edittext_serach.postInvalidate();
		CharSequence charSequence = edittext_serach.getText();
		if (charSequence instanceof Spannable) {
			Spannable spanText = (Spannable) charSequence;
			Selection.setSelection(spanText, charSequence.length());
		}
	}

	@Override
	public void onBeginOfSpeech() {
		// voiceText.setText("请讲话");
		voice_rcd_hint_loading.setVisibility(View.GONE);
		rcChat_popup.setVisibility(View.VISIBLE);
		voice_rcd_hint_rcding.setVisibility(View.VISIBLE);
	}

	@Override
	public void onEndOfSpeech() {
		voice_rcd_hint_rcding.setVisibility(View.GONE);
		voice_rcd_hint_loading.setVisibility(View.VISIBLE);
	}

	@Override
	public void onError(String arg0) {
		rcChat_popup.setVisibility(View.GONE);
		voice_rcd_hint_loading.setVisibility(View.GONE);
		voice_rcd_hint_rcding.setVisibility(View.GONE);
		isRecording = false;
	}

	@Override
	public void onRecognitionStart() {
		voice_rcd_hint_loading.setVisibility(View.VISIBLE);
	}

	@Override
	public void onResult(int arg0, ResultEntity arg1) {
		StringBuilder builder = new StringBuilder("识别结果：" + "\n");
		builder.append("识别结果------->").append(arg1 != null ? arg1.jsonString : "").append("\n");
		MZLog.d("quda", "speech=" + builder.toString());
		isRecording = false;

		isVoiceSearch = true;
		try {
			JSONObject obj = new JSONObject(arg1.jsonString);
			JSONObject objdata = (JSONObject) obj.get("value");
			setEditText(objdata.getString("text"));

		} catch (JSONException e) {
			e.printStackTrace();
		}
		rcChat_popup.setVisibility(View.GONE);
		voice_rcd_hint_loading.setVisibility(View.GONE);
	}

	@Override
	public void onVoiceServiceUnavailable() {

	}

	@Override
	public void onVolumeChanged(int arg0) {

		int level = (arg0 - 20) / 4;
		if (level >= 0) {
			updateDisplay(level);
		} else {
			volume.setImageDrawable(null);
		}

	}

	private void updateDisplay(int signalEMA) {
		switch (signalEMA) {
		case 0:
			volume.setImageResource(R.drawable.amp1);
			break;
		case 1:
			volume.setImageResource(R.drawable.amp2);
			break;
		case 2:
			volume.setImageResource(R.drawable.amp3);
			break;
		case 3:
			volume.setImageResource(R.drawable.amp4);
			break;
		case 4:
			volume.setImageResource(R.drawable.amp5);
			break;
		case 5:
			volume.setImageResource(R.drawable.amp6);
			break;
		default:
			volume.setImageResource(R.drawable.amp7);
			break;
		}
	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadMore() {
		if (!noMoreBookOnSearch) {
			if (!inLoadingMoreOnSearch) {
				inLoadingMoreOnSearch = true;
				String key = edittext_serach.getText().toString();
				if (bookType == 0) {
					searchBooks(key, Flag, true);
				} else if (bookType == 1) {
					searchPaperBooks(key, Flag, isVisibleBtns);
				}
			}
		} else {
			onLoadComplete();
		}
	}

	private void onLoadComplete() {
		booklist.stopRefresh();
		booklist.stopLoadMore();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_search));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_search));
	}
	
	/**
	* @Description: 搜索关键字日志记录信息
	* @param String keyWord 搜索关键字
	* @author xuhongwei1
	* @date 2015年11月19日 下午3:31:40 
	* @throws 
	*/ 
	private void searchWordReport(String keyWord) {
		WebRequestHelper.get(URLText.JD_USER_SEARCH_URL, RequestParamsPool.getSearchWordReportParams(keyWord, "1"),
				true, new MyAsyncHttpResponseHandler(BookStoreSearchActivity.this) {
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						
			}
					
			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
//				String result = new String(responseBody);
//		System.out.println("BBBBB====searchWordReport===="+result);
			}
		});
	}
	
	/**
	* @Description: 搜索图书日志记录信息
	* @param String keyWord 搜索关键字
	* @param String bookid 图书ID
	* @author xuhongwei1
	* @date 2015年11月19日 下午4:10:26 
	* @throws 
	*/ 
	private void searchBookReport(String keyWord, String bookid) {
		WebRequestHelper.get(URLText.JD_USER_SEARCH_URL, RequestParamsPool.getSearchBookReportParams(keyWord, bookid),
				true, new MyAsyncHttpResponseHandler(BookStoreSearchActivity.this) {
			
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						
			}
					
			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
//				String result = new String(responseBody);
//		System.out.println("BBBBB===1111=searchBookReport===="+result);
			}
		});
	}
	
}
