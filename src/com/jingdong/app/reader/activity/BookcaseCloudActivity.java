package com.jingdong.app.reader.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.Header;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookstore.search.BookStoreSearchActivity;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.client.DownloadStateManager;
import com.jingdong.app.reader.client.DownloadThreadQueue.RefreshAble;
import com.jingdong.app.reader.client.DownloadTool;
import com.jingdong.app.reader.client.DownloadTool.DownloadConfirmListener;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.LocalBook.SubBook;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.extra.BuyedEbook;
import com.jingdong.app.reader.entity.extra.JDEBook;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.util.SettingUtils;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 已购列表
 *
 */
public class BookcaseCloudActivity extends Activity implements TopBarViewListener, FragmentInterface, RefreshAble {

	private ArrayList<EBookItemHolder> eBookItemList = new ArrayList<EBookItemHolder>();
	private List<JDEBook> eBooklist = new ArrayList<JDEBook>();
	private BookListAdapter bookListAdapter = null;
	private List<String> bookinfoText = new ArrayList<String>();
	protected final static int STATE_LOAD_FAIL = 0;// 未下载、下载失败
	protected final static int STATE_LOADING = 1; // 下载、暂停中
	protected final static int STATE_LOADED = 2; // 下载完成

	protected final static int PROGRESS_FAILED = -1; // 下载失败的进度

	private int currentPage = 1;
	private static int perPageCount = 10;
	private boolean noMoreBook = false;
	private boolean inLoadingMore = true;

	private int currentSearchPage = 1;
	private int perSearchCount = 10;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;

	private boolean inSearch = false;
	private boolean isSearchCommited = false;

	private ListView listView = null;
	protected final static int UPDATE_UI_MESSAGE = 0;
	private RelativeLayout relativeLayout = null;
	private ImageView icon;
	private TextView textView;
	private Button lackbook_button;
	private int gotoflag = -1;

	private TopBarView topBarView = null;
	private EditText edittext_search;// 搜索关键字输入框
	private Handler handler = getUpdateViewHandler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_changdu);
		// 初始化topbar 开始
		topBarView = (TopBarView) findViewById(R.id.topbar);
		initTopbarView();
		// 初始化topbar 结束

		edittext_search = (EditText) findViewById(R.id.edittext_serach);
		edittext_search.setHint(getString(R.string.bookshelf_search_text_hit));
		edittext_search.setOnKeyListener(new View.OnKeyListener() {

			/**
			 * 监听按键事件
			 */
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				//用户输入了检索关键词
				if (keyCode == event.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					eBookItemList.clear();
					bookListAdapter.notifyDataSetChanged();
					isSearchCommited = true;
					inSearch = true;
					initData();
					//搜索已购图书
					searchBuyedBook(edittext_search.getText().toString());
					return true;
				}
				return false;
			}
		});

		//文本变化监听
		edittext_search.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				//若用户没有输入任何内容，则需要重新加载数据
				if (isSearchCommited && TextUtils.isEmpty(edittext_search.getText().toString())) {
					listView.setVisibility(View.VISIBLE);
					relativeLayout.setVisibility(View.GONE);
					eBookItemList.clear();
					bookListAdapter.notifyDataSetChanged();
					inSearch = false;
					initData();
					listBuyedEbook();
					isSearchCommited = false;
				}
			}
		});

		listView = (ListView) findViewById(R.id.list);
		relativeLayout = (RelativeLayout) findViewById(R.id.search_result_container);
		icon = (ImageView) findViewById(R.id.icon);
		textView = (TextView) findViewById(R.id.text);
		lackbook_button = (Button) findViewById(R.id.lackbook_button);
		lackbook_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (gotoflag == 0) {
					Intent intent = new Intent(v.getContext(),LauncherActivity.class);
					intent.putExtra("lx", 0);
					startActivity(intent);
				}else if (gotoflag == 1){
					Intent intent = new Intent(v.getContext(),BookStoreSearchActivity.class);
					startActivity(intent);
				}
			}
		});
		
		//列出已购书籍列表
		listBuyedEbook();
		//设置刷新
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);

		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			/**
			 * 列表滚动处理
			 */
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
				if (totalItemCount == 0)
					return;
				//非搜索结果展示
				if (firstVisibleItem + visibleItemCount == totalItemCount && !noMoreBook && !inSearch) {
					if (!inLoadingMore) {
						inLoadingMore = true;
						listBuyedEbook();
					}
				}

				if (firstVisibleItem + visibleItemCount == totalItemCount && !noMoreBookOnSearch && inSearch) {
					if (!inLoadingMoreOnSearch) {
						inLoadingMoreOnSearch = true;
						String key = edittext_search.getText().toString();
						searchBuyedBook(key);
					}
				}

			}
		});
		bookListAdapter = new BookListAdapter(this);
		listView.setAdapter(bookListAdapter);
	}

	/**
	 * 初始化顶部标题栏
	 */
	private void initTopbarView() {
		if (topBarView == null)
			return;
		if (topBarView == null)
			return;
		//显示返回
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topBarView.setListener(this);
		//显示标题
		topBarView.setTitle("已购");
	}

	@Override
	public void onResume() {
		super.onResume();
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
		// listBuyedEbook();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_buy));
	}

	@Override
	public void onPause() {
		super.onPause();
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_buy));
	}
	
	public void initData() {
		currentPage = 1;
		noMoreBook = false;
		inLoadingMore = true;
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}
	
	private static class EBookItemHolder {
		JDEBook ebook;
		/**
		 * 本地是否存在图书文件
		 */
		boolean existInLocal;
		/**
		 * 是否下载暂停
		 */
		boolean paused;
		/**
		 * 是否下载完毕
		 */
		boolean failed;
		/**
		 * 是否在排队
		 */
		boolean inWaitingQueue;
		/**
		 * 下载进度
		 */
		int progress;
	}

	protected int getLocalBookState(SubBook book) {
		final SubBook localBook = book;
		File file = null;
		String filePath = localBook.book_path;
		if (filePath != null && !filePath.trim().equals("")) {
			file = new File(filePath);
		}
		if (localBook != null) {
			if (localBook.state == LocalBook.STATE_LOADED || localBook.state == LocalBook.STATE_LOAD_READING) {
				if (file != null && file.exists()) {
					return STATE_LOADED;
				}
				return STATE_LOAD_FAIL;
			} else if (localBook.state == LocalBook.STATE_LOADING
					|| localBook.state == LocalBook.STATE_LOAD_PAUSED
					|| localBook.state == LocalBook.STATE_LOAD_READY) {
				return STATE_LOADING;
			}
		}
		return STATE_LOAD_FAIL;
	}

	protected SubBook createSubBook(LocalBook localBook) {
		if (localBook == null) {
			return null;
		}
		SubBook subBook = new SubBook();
		subBook.id = localBook.book_id;
		subBook.state = localBook.state;
		subBook.progress = localBook.progress;
		subBook.size = localBook.size;
		subBook.book_path = localBook.book_path;
		return subBook;
	}

	/**
	 * 搜索图书
	 * @param key
	 */
	private void searchBuyedBook(String key) {
		gotoflag = 1;
		if (!NetWorkUtils.isNetworkConnected(BookcaseCloudActivity.this)) {
			Toast.makeText(BookcaseCloudActivity.this,getString(R.string.network_connect_error),Toast.LENGTH_SHORT).show();
			return;
		}

		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSearchBuyedEbookParams(key, currentSearchPage + "",perSearchCount + ""), true,
				new MyAsyncHttpResponseHandler(BookcaseCloudActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,Throwable arg3) {
						Toast.makeText(BookcaseCloudActivity.this,getString(R.string.network_connect_error),Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,byte[] responseBody) {

						String result = new String(responseBody);
						BuyedEbook ebook = GsonUtils.fromJson(result,BuyedEbook.class);
						if (ebook != null && ebook.code == 0) {
							if (ebook.resultList.size() == 0 && currentPage == 1) {
								listView.setVisibility(View.GONE);
								relativeLayout.setVisibility(View.VISIBLE);
								icon.setBackgroundResource(R.drawable.bookstore_icon_search_null);
								textView.setText("已购列表中暂无您搜索的书籍");
								lackbook_button.setText("去书城搜索");
							}
							currentSearchPage++;
							
							if (ebook.resultList != null && ebook.resultList.size() < perPageCount)
								noMoreBookOnSearch = true;
							else {
								noMoreBookOnSearch = false;
							}
							if (ebook.resultList != null && ebook.resultList.size() > 0) {
								String[] book_ids = new String[ebook.resultList.size()];
								for (int i=0;i<ebook.resultList.size();i++) {
									JDEBook book = ebook.resultList.get(i);
									book_ids[i] = book.bookId;
								}
								final List<LocalBook> allLocalBooks = MZBookDatabase.instance.getLocalBooks(null,book_ids);
								List<EBookItemHolder> all = new ArrayList<EBookItemHolder>();
								for (JDEBook book : ebook.resultList) {
									if(!TextUtils.isEmpty(book.sentNickName))
										book.isReceived = true;
									all.add(checkBookState(allLocalBooks, book));
								}
	
								eBookItemList.addAll(all);
							}
							if (eBookItemList.size() == 0) {
								Toast.makeText(getApplicationContext(),"抱歉,没找到该书!", Toast.LENGTH_LONG).show();
							}
							bookListAdapter.notifyDataSetChanged();
						}
						else if (ebook == null)
							Toast.makeText(BookcaseCloudActivity.this, getString(R.string.network_connect_error),Toast.LENGTH_SHORT).show();
						inLoadingMoreOnSearch = false;
					}
				});
	}

	private void listBuyedEbook() {
		gotoflag = 0;
		//检查网络连接
		if (!NetWorkUtils.isNetworkConnected(BookcaseCloudActivity.this)) {
			Toast.makeText(BookcaseCloudActivity.this,getString(R.string.network_connect_error),Toast.LENGTH_SHORT).show();
			return;
		}

		//所有本地图书列表
		final List<LocalBook> allLocalBooks = LocalBook.getLocalBookList(null,null);
		WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool
				.getBuyedEbookParams(currentPage + "", perPageCount + ""),
				true,
				new MyAsyncHttpResponseHandler(BookcaseCloudActivity.this) {

					/**
					 * 查询已购列表接口失败
					 */
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,Throwable arg3) {
						Toast.makeText(BookcaseCloudActivity.this,getString(R.string.network_connect_error),Toast.LENGTH_SHORT).show();
					}

					/**
					 * 查询已购信息列表成功
					 */
					@Override
					public void onResponse(int statusCode, Header[] headers,byte[] responseBody) {

						String result = new String(responseBody);
						BuyedEbook ebook = GsonUtils.fromJson(result,BuyedEbook.class);
						if (ebook != null) {//网络错误时，ebook可能会为null
							eBooklist = ebook.resultList;
							if (ebook != null && ebook.code == 0) {
								//图书列表
								if (ebook.resultList.size() == 0 && currentPage == 1) {//没有已购图书
									listView.setVisibility(View.GONE);
									relativeLayout.setVisibility(View.VISIBLE);
									icon.setBackgroundResource(R.drawable.icon_empty);
									textView.setText("您的已购列表暂无书籍，快去书城选购吧");
									lackbook_button.setText("去书城");
								}
								//为下一次加载准备
								currentPage++;
								//检查当前页是否超出总页码，若没有超，则可以接着加载更多的页面
								if (ebook.resultList != null && currentPage > ebook.totalPage)
									noMoreBook = true;
								else {
									noMoreBook = false;
								}

								//图书列表
								List<EBookItemHolder> all = new ArrayList<BookcaseCloudActivity.EBookItemHolder>();
								for (JDEBook book : ebook.resultList) {
									if(!TextUtils.isEmpty(book.sentNickName))
										book.isReceived = true;
									all.add(checkBookState(allLocalBooks, book));
								}
								eBookItemList.addAll(all);
								bookListAdapter.notifyDataSetChanged();
							}
						}
						else
							Toast.makeText(BookcaseCloudActivity.this, getString(R.string.network_connect_error),Toast.LENGTH_SHORT).show();
						inLoadingMore = false;
					}
				});

	}

	public EBookItemHolder checkBookState(List<LocalBook> list, JDEBook entity) {

		EBookItemHolder holder = new EBookItemHolder();
		holder.ebook = entity;
		holder.existInLocal = false;
		holder.failed = false;
		holder.paused = false;
		holder.inWaitingQueue = false;

		if (null == list || list.size() == 0)
			return holder;// 本地没有任何数据
		for (int i = 0; i < list.size(); i++) {
			LocalBook localBook = list.get(i);
			if (localBook.book_id == Long.parseLong(entity.bookId)) {
				if (localBook.source.equals(LocalBook.SOURCE_BUYED_BOOK)) {
					int state = DownloadStateManager.getLocalBookState(localBook);
					if (state == DownloadStateManager.STATE_LOADED) {
						holder.existInLocal = true;

					} else if (state == DownloadStateManager.STATE_LOADING) {
						if (localBook.state == LocalBook.STATE_LOADING || localBook.state == LocalBook.STATE_LOAD_READY) {
							holder.inWaitingQueue = true;
						}
						if (localBook.state == LocalBook.STATE_LOAD_PAUSED) {
							holder.paused = true;
						}
					} else {
						holder.failed = true;
					}
				} else {// 本地书籍有试读版本或借阅版本，购买版本
					holder.existInLocal = false;
					holder.failed = false;
					holder.paused = false;
					holder.inWaitingQueue = false;
				}
			}
		}
		return holder;
	}

	@Override
	public void fragmentBecameVisible() {

	}

	@Override
	public void refresh(final DownloadedAble downloadAble) {
		final LocalBook localBook = (LocalBook) downloadAble;
		if (localBook == null) {
			return;
		}
		List<Integer> positions = bookListAdapter.getIndexByEbookId(localBook.book_id);
		int progress = (int) (100 * (localBook.progress / (localBook.size * 1.0)));
		int state = DownloadStateManager.getLocalBookState(localBook);

		MZLog.d("cj", "state:" + state + "进度：" + localBook.progress + "/"+ localBook.size);

		if (state == DownloadStateManager.STATE_LOADED) {
			if(localBook!=null){
				SettingUtils.getInstance().putBoolean("Buyed:" + localBook.book_id, false);
				SettingUtils.getInstance().putBoolean("file_error:" + localBook.book_id, false);
			}
		} else if (state == DownloadStateManager.STATE_LOADING) {

			if (localBook.state == LocalBook.STATE_LOADING
					|| localBook.state == LocalBook.STATE_LOAD_READY) {
				for (int i = 0; i < positions.size(); i++) {
					sendMessage(positions.get(i), progress);
				}
			}
		} else {
			MZLog.d("wangguodong", "畅读:下载失败");
			for (int i = 0; i < positions.size(); i++) {
				sendMessage(positions.get(i), PROGRESS_FAILED);
			}
		}
	}
	private Handler getUpdateViewHandler() {
		return new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (msg.what == UPDATE_UI_MESSAGE) {
					if (bookListAdapter != null) {
						bookListAdapter.updateItemView(msg.arg1, msg.arg2);
					}
				}
			};
		};
	}
	
	public void sendMessage(int position, int progress) {
		Message msg = new Message();
		msg.what = UPDATE_UI_MESSAGE;
		msg.arg1 = position;
		msg.arg2 = progress;
		handler.sendMessage(msg);
	}

	@Override
	public int getType() {
		return 0;
	}

	@Override
	public void refreshDownloadCache() {

	}

	@Override
	public void onLeftMenuClick() {
		finish();
	}


	@Override
	public void onCenterMenuItemClick(int position) {

	}

	@Override
	public void onRightMenuOneClick() {
		
	}

	@Override
	public void onRightMenuTwoClick() {
		
	}

	/**
	 * 已购列表的Adapter
	 */
	private class BookListAdapter extends BaseAdapter {
		private Context context;
		
		BookListAdapter(Context context) {
			this.context = context;
		}

		@Override
		public View getView(final int position, View convertView,ViewGroup parent) {

			final ViewHolder holder;
			if (convertView == null) {
				//列表项的布局
				convertView = LayoutInflater.from(context).inflate(R.layout.item_free_gifts_booklist, parent, false);
				holder = new ViewHolder();
				//书名
				holder.bookTitle = (TextView) convertView.findViewById(R.id.user_book_name);
				//作者
				holder.bookAuthor = (TextView) convertView.findViewById(R.id.user_book_author);
				holder.statue_button = (Button) convertView.findViewById(R.id.statueButton);
				//封面
				holder.bookCover = (ImageView) convertView.findViewById(R.id.user_book_cover);
				//赠送者
				holder.sentNickName = (TextView) convertView.findViewById(R.id.sent_nickname);
				holder.bookSize = (TextView) convertView.findViewById(R.id.book_size);
				holder.unsupport_info = (TextView) convertView.findViewById(R.id.unsupport_format);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			//图书信息
			final EBookItemHolder item = eBookItemList.get(position);
			final JDEBook eBook = item.ebook;
			//图书标题
			holder.bookTitle.setText(eBook.name);
			//图书作者
			holder.bookAuthor.setText("null".equals(eBook.author) ? getString(R.string.author_unknown) : eBook.author);
			//图书文件大小
			holder.bookSize.setText(eBook.size + "MB");
			//赠送者
			if(!TextUtils.isEmpty(eBook.sentNickName)){
				holder.sentNickName.setVisibility(View.VISIBLE);
				holder.sentNickName.setText(eBook.sentNickName+" 赠送");
			}else
				holder.sentNickName.setVisibility(View.GONE);
				
			//图书文件格式
			if (eBook.format == LocalBook.FORMAT_EPUB || eBook.bookType == LocalBook.FORMAT_PDF) {
				holder.statue_button.setVisibility(View.VISIBLE);
				holder.statue_button.setText("下载");
				holder.statue_button.setTextColor(getResources().getColor(R.color.highlight_color));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_red_h24);
				holder.bookSize.setVisibility(View.VISIBLE);
				holder.unsupport_info.setText("");
				holder.unsupport_info.setVisibility(View.GONE);
			} else {
				holder.unsupport_info.setText("请在PC上阅读");
				holder.unsupport_info.setVisibility(View.VISIBLE);
				holder.statue_button.setVisibility(View.GONE);
			}

			//图书列表条目被点击
			convertView.setOnClickListener(new OnClickListener() {

				/**
				 * 条目被点击，进入图书详情页
				 */
				@Override
				public void onClick(View v) {
					//下架图书无法进入详情页
					if(!item.ebook.pass){
						Toast.makeText(BookcaseCloudActivity.this, "很抱歉，出版社把这本书下架了，原因您懂的！", Toast.LENGTH_SHORT).show();
						return ;
					}

					EBookItemHolder item = eBookItemList.get(position);
					JDEBook ebook = item.ebook;
					Intent intent2 = new Intent(BookcaseCloudActivity.this,BookInfoNewUIActivity.class);
					intent2.putExtra("bookid", Long.parseLong(ebook.bookId));
					startActivity(intent2);
				}
			});

			//按钮被点击
			holder.statue_button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//检查图书状态
					//存在本地图书，则直接打开
					if (item.existInLocal) {
						OpenBookHelper.openEBook(BookcaseCloudActivity.this,Long.parseLong(eBook.bookId));
					} else if (item.paused) {//已经在下载中，继续下载	
						MZLog.d("wangguodong", "已购：继续下载");
						Toast.makeText(BookcaseCloudActivity.this, "继续下载",Toast.LENGTH_SHORT).show();
						LocalBook localBook = LocalBook.getLocalBook(Long.parseLong(eBook.bookId),LoginUser.getpin());
						if(localBook==null) return ;
						int prog = 0;
						if (localBook.size == 0)
							prog = 0;
						else {
							prog = (int) (localBook.progress / localBook.size);
						}
						updateItemView(position, false, false, false, true,
								prog);
						localBook.mod_time = System.currentTimeMillis();
						localBook.saveModTime();
						MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), localBook.book_id, localBook.source);
						localBook.start(BookcaseCloudActivity.this);
					}
					//等待下载
					else if (item.inWaitingQueue) {
						holder.statue_button.setText("继续");
						holder.statue_button.setTextColor(getResources().getColor(R.color.text_main));
						holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_black_h24);
						updateItemView(position, false, true, false, false,PROGRESS_FAILED);
						Toast.makeText(BookcaseCloudActivity.this, "暂停下载",Toast.LENGTH_SHORT).show();
						MZLog.d("wangguodong", "已购：暂停下载");
						
						LocalBook localBook = LocalBook.getLocalBook(Long.parseLong(eBook.bookId),LoginUser.getpin());
						if(localBook==null) 
							return ;
						//停止下载
						DownloadService.stop(localBook);
					} else if (item.failed) {
						MZLog.d("wangguodong", "已购：重新开始下载");
						holder.statue_button.setText("等待");
						holder.statue_button.setTextColor(getResources().getColor(R.color.text_color));
						holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
						updateItemView(position, false, false, true, false, 0);

						LocalBook localBook = LocalBook.getLocalBook(Long.parseLong(eBook.bookId),LoginUser.getpin());
						
						if(localBook==null) return ;
						
						localBook.progress = 0;
						localBook.state = LocalBook.STATE_LOAD_PAUSED;
						localBook.save();// 修改数据库中的状态，重新下载，zhangmurui
						MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), localBook.book_id, localBook.source);
						localBook.start(BookcaseCloudActivity.this); // 下载失败重新开始下载
					} else {
						// 开始下载
						LocalBook localBook = LocalBook.getLocalBook(Long.parseLong(eBook.bookId),LoginUser.getpin());
						if (localBook != null) {
							int state = getLocalBookState(createSubBook(localBook));
							if (localBook.source.equals(LocalBook.SOURCE_TRYREAD_BOOK)) /*当前文件为试读，下载已购替换*/{
								String pathString = localBook.dir;
								//若本地存在文件，先删除
								if (!TextUtils.isEmpty(pathString)) {
									IOUtil.deleteFile(new File(pathString));
								}

								//进度为0
								localBook.progress = 0;
								//文件打下
								localBook.size=0;
								localBook.type_id = eBook.bookType;
								//下载状态
								localBook.state = LocalBook.STATE_LOAD_PAUSED;
								state = LocalBook.STATE_LOAD_PAUSED;
								//订单号
								localBook.order_code = eBook.orderId;
								//图书文件下载地址
								localBook.bookUrl = "";
								//引导文件
								localBook.boot = null;
								//已购图书
								localBook.source = LocalBook.SOURCE_BUYED_BOOK;
								//保存信息
								localBook.save();
							}
							else if (localBook.source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {
								//已畅读的书将本地书本类型更改为已购
								localBook.source = LocalBook.SOURCE_BUYED_BOOK;
								localBook.save();
								item.existInLocal=true;
								bookListAdapter.notifyDataSetChanged();
								return ;
							} else {
								MZLog.d("wangguodong","本地存在借阅或者内置版本 直接修改source字段");
								localBook.order_code = eBook.orderId;
								localBook.source = LocalBook.SOURCE_BUYED_BOOK;
								localBook.save();
							}

							if (state == STATE_LOADED) {//已经下载完毕，直接打开图书
								OpenBookHelper.openEBook(BookcaseCloudActivity.this, localBook.book_id);
							} else if (state == STATE_LOADING) {
								// 不能点击
							} else {
								// 加入等待队列
								holder.statue_button.setText("等待");
								holder.statue_button.setTextColor(getResources().getColor(R.color.text_color));
								holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
								updateItemData(position, false, false, false,true, -1);

								localBook.progress = 0;
								localBook.state = LocalBook.STATE_LOAD_PAUSED;
								localBook.save();// 修改数据库中的状态，重新下载，zhangmurui
								MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), localBook.book_id, localBook.source);
								localBook.start(BookcaseCloudActivity.this); // 下载失败重新开始下载
							}

						} else {
							//图书信息
							JDEBook bookEntity = eBook;
							OrderEntity orderEntity = OrderEntity.FromJDBooK2OrderEntity(bookEntity);
							//下载图书
							DownloadTool.download((Activity) BookcaseCloudActivity.this,orderEntity, null, false,LocalBook.SOURCE_BUYED_BOOK, 0, false,new DownloadConfirmListener() {
										
										@Override
										public void onConfirm() {
											holder.statue_button.setText("等待");
											holder.statue_button.setTextColor(getResources().getColor(R.color.text_color));
											holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
											updateItemData(position, false, false, false, true,-1);
										}
										
										/**
										 * 取消下载
										 */
										@Override
										public void onCancel() {
											
										}
									},false);
						}
					}
				}

				
			});

			//若本地存在图书内容文件，文本修改为阅读
			if (item.existInLocal) {
				holder.statue_button.setText("阅读");
				holder.statue_button.setTextColor(getResources().getColor(R.color.highlight_color));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_red_h24);
			} else if (item.paused)	/*下载暂停的情况*/ {
				holder.statue_button.setText("继续");
				holder.statue_button.setTextColor(getResources().getColor(R.color.text_main));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_black_h24);
			}
			//下载等待的状态
			else if (item.inWaitingQueue) {
				holder.statue_button.setText("等待");
				holder.statue_button.setTextColor(getResources().getColor(R.color.text_color));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
			}
			//下载失败
			else if (item.failed) {
				holder.statue_button.setText("失败");
				holder.statue_button.setTextColor(getResources().getColor(R.color.r_text_disable));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
			} else {
				holder.statue_button.setText("下载");
				holder.statue_button.setTextColor(getResources().getColor(R.color.highlight_color));
				holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_red_h24);
			}

			String urlPath = eBook.imgUrl;
			ImageLoader.getInstance().displayImage(urlPath, holder.bookCover,GlobalVarable.getCutBookDisplayOptions(false));
			return convertView;
		}

		public List<Integer> getIndexByEbookId(long bookid) {
			List<Integer> list = new ArrayList<Integer>();
			if (eBookItemList != null) {
				for (int i = 0; i < eBookItemList.size(); i++) {
					if (Long.parseLong(eBookItemList.get(i).ebook.bookId) == bookid) {
						list.add(i);
					}
				}
				return list;
			} else {
				return list;
			}
		}

		public void updateItemData(int position, boolean existInLocal,
				boolean pause, boolean failed, boolean inWaitingQueue,
				int progress) {
			if (eBookItemList != null && eBookItemList.size() > position
					&& position >= 0) {
				EBookItemHolder holder = eBookItemList.get(position);
				holder.existInLocal = existInLocal;
				holder.paused = pause;
				holder.failed = failed;
				holder.inWaitingQueue = inWaitingQueue;
				holder.progress = progress;
				eBookItemList.set(position, holder);
			}

		}

		public void updateItemView(int position, boolean existInLocal,
				boolean pause, boolean failed, boolean inWaitingQueue,
				int progress) {
			updateItemData(position, existInLocal, pause, failed,
					inWaitingQueue, progress);
			int visiblePos = listView.getFirstVisiblePosition();
			int offset = position - visiblePos;
			if (offset < 0)
				return;
			View view = listView.getChildAt(offset);
			if (view != null) {
				ViewHolder holder = (ViewHolder) view.getTag();
				if (progress != PROGRESS_FAILED) {
					holder.statue_button.setText(progress + "%");
					holder.statue_button.setTextColor(getResources().getColor(
							R.color.r_text_disable));
					holder.statue_button
							.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
				} else {
					holder.statue_button.setText("继续");
					holder.statue_button.setTextColor(getResources().getColor(
							R.color.text_main));
					holder.statue_button
							.setBackgroundResource(R.drawable.border_listbtn_black_h24);
				}
			}
		}

		public void updateItemView(final int index, int progress) {
			// 更新列表数据
			if (progress <= PROGRESS_FAILED) {
				updateItemData(index, false, false, true, false,
						PROGRESS_FAILED);
			} else if (progress < 100)
				updateItemData(index, false, false, false, true, progress);
			else {
				updateItemData(index, true, false, false, false,
						PROGRESS_FAILED);
			}

			int visiblePos = listView.getFirstVisiblePosition();
			int offset = index - visiblePos;
			if (offset < 0)
				return;
			View view = listView.getChildAt(offset);
			if (view != null) {
				ViewHolder holder = (ViewHolder) view.getTag();

				if (progress <= PROGRESS_FAILED) {
					holder.statue_button.setText("失败");
					holder.statue_button.setTextColor(getResources().getColor(
							R.color.r_text_disable));
					holder.statue_button
							.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
				} else if (progress < 100) {
					holder.statue_button.setText(progress + "%");
					holder.statue_button.setTextColor(getResources().getColor(
							R.color.r_text_disable));
					holder.statue_button
							.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
				}
				if (progress >= 100) {
					// 更新ui
					// 更新ui
					holder.statue_button.setText("阅读");
					holder.statue_button.setTextColor(getResources().getColor(
							R.color.highlight_color));
					holder.statue_button
							.setBackgroundResource(R.drawable.border_listbtn_red_h24);
				}
			}

		}
		
		@Override
		public int getCount() {
			return eBookItemList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		
		class ViewHolder {
			TextView bookTitle;
			TextView bookAuthor;
			TextView bookSize;
			Button statue_button;
			ImageView bookCover;
			TextView unsupport_info;
			TextView sentNickName;
		}

	}

}
