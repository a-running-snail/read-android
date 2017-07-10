package com.jingdong.app.reader.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

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
import android.view.MenuItem;
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
import com.jingdong.app.reader.client.DownloadTool.DownloadConfirmListener;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.common.CommonActivity;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.download.util.L;
import com.jingdong.app.reader.entity.BookInforEDetail;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.MyOnlineBookEntity;
import com.jingdong.app.reader.entity.extra.ChangduEbook;
import com.jingdong.app.reader.entity.extra.JDOnlineBookEntity;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.onlinereading.OnlineReadManager;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;

public class ChangDuActivity extends CommonActivity implements TopBarViewListener, RefreshAble {

	private ArrayList<EBookItemHolder> eBookItemList = new ArrayList<EBookItemHolder>();
	private BookListAdapter bookListAdapter = null;
	private List<String> bookinfoText = new ArrayList<String>();
	protected final static int STATE_LOAD_FAIL = 0;// 未下载、下载失败
	protected final static int STATE_LOADING = 1; // 下载、暂停中
	protected final static int STATE_LOADED = 2; // 下载完成

	protected final static int PROGRESS_FAILED = -1; // 下载失败的进度

	protected final static int UPDATE_UI_MESSAGE = 0;

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
	private TopBarView topBarView = null;
	private EditText edittext_search;// 搜索关键字输入框
	private RelativeLayout relativeLayout = null;
	private ImageView icon;
	private TextView textView;
	private Button lackbook_button;
	private Button changduButton;
	private int gotoflag = -1;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			if (msg.what == UPDATE_UI_MESSAGE) {
				if (bookListAdapter != null) {
					bookListAdapter.updateItemView(msg.arg1, msg.arg2);
				}
			}
		};
	};

	public void initData() {

		currentPage = 1;
		noMoreBook = false;
		inLoadingMore = true;

		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;

	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.activity_changdu);
		// 初始化topbar 开始
		topBarView = (TopBarView) findViewById(R.id.topbar);
		initTopbarView();
		// 初始化topbar 结束
		edittext_search = (EditText) findViewById(R.id.edittext_serach);
		edittext_search.setHint(getString(R.string.bookshelf_search_text_hit));
		edittext_search.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == event.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					eBookItemList.clear();
					bookListAdapter.notifyDataSetChanged();
					isSearchCommited = true;
					inSearch = true;
					initData();
					searchBuyedBook(edittext_search.getText().toString());
					return true;
				}
				return false;
			}
		});

		edittext_search.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				MZLog.d("cj", TextUtils.isEmpty(edittext_search.getText().toString()) ? "true" : "false");

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
		changduButton = (Button) findViewById(R.id.chandu_button);
		lackbook_button.setOnClickListener(new OnClickListener() {

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

		changduButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ChangDuActivity.this, WebViewActivity.class);
				intent.putExtra(WebViewActivity.UrlKey, "http://e.m.jd.com/readCard.html");
				intent.putExtra(WebViewActivity.TopbarKey, true);
				intent.putExtra(WebViewActivity.BrowserKey, false);
				intent.putExtra(WebViewActivity.TitleKey, "购买畅读卡");
				startActivity(intent);
			}
		});

		listBuyedEbook();

//		listView.setDivider(null);
//		listView.setDividerHeight(0);
//		listView.setBackgroundResource(R.color.book_store_bg);
//		listView.setSelector(R.drawable.list_item_color);
		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

				if (totalItemCount == 0)
					return;
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

	private void initTopbarView() {
		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topBarView.setListener(this);
		topBarView.setTitle("畅读");
	}

	@Override
	public void onResume() {
		super.onResume();
		// listBuyedEbook();
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_changdu));
	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_changdu));
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return super.onOptionsItemSelected(item);

	}

	private static class EBookItemHolder {
		MyOnlineBookEntity ebook;
		boolean existInLocal;
		boolean paused;
		boolean failed;
		boolean inWaitingQueue;
		int progress;
	}

	private class BookListAdapter extends BaseAdapter {

		private Context context;

		class ViewHolder {
			TextView bookTitle;
			TextView bookAuthor;
			Button statue_button;
			TextView bookSize;
			ImageView bookCover;
		}

		public void updateItemView(int position, boolean existInLocal, boolean pause, boolean failed, boolean inWaitingQueue, int progress) {
			updateItemData(position, existInLocal, pause, failed, inWaitingQueue, progress);
			int visiblePos = listView.getFirstVisiblePosition();
			int offset = position - visiblePos;
			if (offset < 0)
				return;
			View view = listView.getChildAt(offset);
			if (view != null) {
				ViewHolder holder = (ViewHolder) view.getTag();
				if (progress != PROGRESS_FAILED) {
					holder.statue_button.setText(progress + "%");
					holder.statue_button.setTextColor(getResources().getColor(R.color.r_text_disable));
					holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
				} else {
					holder.statue_button.setText("继续");
					holder.statue_button.setTextColor(getResources().getColor(R.color.text_main));
					holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_black_h24);
				}
			}
		}

		public void updateItemView(final int index, int progress) {
			// 更新列表数据
			if (progress <= PROGRESS_FAILED) {
				updateItemData(index, false, false, true, false, PROGRESS_FAILED);
			} else if (progress < 100)
				updateItemData(index, false, false, false, true, progress);
			else {
				updateItemData(index, true, false, false, false, PROGRESS_FAILED);
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
					holder.statue_button.setTextColor(getResources().getColor(R.color.r_text_disable));
					holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
				} else if (progress < 100) {
					holder.statue_button.setText(progress + "%");
					holder.statue_button.setTextColor(getResources().getColor(R.color.r_text_disable));
					holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
				}
				if (progress >= 100) {
					// 更新ui
					holder.statue_button.setText("阅读");
					holder.statue_button.setTextColor(getResources().getColor(R.color.highlight_color));
					holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_red_h24);
				}
			}
		}

		public int getIndexByEbookId(long bookid) {
			if (eBookItemList != null) {
				for (int i = 0; i < eBookItemList.size(); i++) {
					if (eBookItemList.get(i).ebook.itemId == bookid) {
						return i;
					}
				}
				return -1;
			} else {
				return -1;
			}
		}

		public void updateItemData(int position, boolean existInLocal, boolean pause, boolean failed, boolean inWaitingQueue, int progress) {
			if (eBookItemList != null && eBookItemList.size() > position && position >= 0) {
				EBookItemHolder holder = eBookItemList.get(position);
				holder.existInLocal = existInLocal;
				holder.paused = pause;
				holder.failed = failed;
				holder.inWaitingQueue = inWaitingQueue;
				holder.progress = progress;
				eBookItemList.set(position, holder);
			}

		}

		BookListAdapter(Context context) {
			this.context = context;
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

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.item_free_gifts_booklist, parent, false);
				holder = new ViewHolder();
				holder.bookTitle = (TextView) convertView.findViewById(R.id.user_book_name);
				holder.bookAuthor = (TextView) convertView.findViewById(R.id.user_book_author);
				holder.statue_button = (Button) convertView.findViewById(R.id.statueButton);

				holder.bookCover = (ImageView) convertView.findViewById(R.id.user_book_cover);

				holder.bookSize = (TextView) convertView.findViewById(R.id.book_size);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final EBookItemHolder item = eBookItemList.get(position);
			final MyOnlineBookEntity eBook = item.ebook;
			holder.bookTitle.setText(eBook.ebookName);
			holder.bookAuthor.setText("null".equals(eBook.author) ? getString(R.string.author_unknown) : eBook.author);
			holder.bookSize.setText(eBook.size + "MB");
			// holder.statue_button.setText("下载");
			// updateStatusButton(holder.statue_button,item,position);
			holder.statue_button.setTextColor(getResources().getColor(R.color.highlight_color));
			// holder.statue_button
			// .setBackgroundResource(R.drawable.border_listbtn_red_h24);
			holder.bookSize.setVisibility(View.VISIBLE);

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					//下架图书无法进入详情页
					if(!item.ebook.pass){
						Toast.makeText(ChangDuActivity.this, "很抱歉，出版社把这本书下架了，原因您懂的！", Toast.LENGTH_SHORT).show();
						return ;
					}
					
					EBookItemHolder item = eBookItemList.get(position);
					MyOnlineBookEntity ebook = item.ebook;
					Intent intent2 = new Intent(ChangDuActivity.this, BookInfoNewUIActivity.class);
					intent2.putExtra("bookid", ebook.itemId);
					startActivity(intent2);
				}
			});
			MZLog.d("wangguodong", item.ebook.canRead);
			if (item.ebook.pass && item.ebook.supportCardRead) {
				holder.statue_button.setVisibility(View.VISIBLE);
				holder.statue_button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						if (item.existInLocal) {
							MZLog.d("wangguodong", "xxxx333333");
//							onlineRead(eBook, null);
							OpenBookHelper.openEBook(ChangDuActivity.this, item.ebook.itemId, null);
						} else if (item.paused) {
							MZLog.d("wangguodong", "xxxx4444444");
							Toast.makeText(ChangDuActivity.this, "继续下载", Toast.LENGTH_SHORT).show();
							MZLog.d("wangguodong", "畅读：继续下载");

							LocalBook localBook = LocalBook.getLocalBook(eBook.itemId, LoginUser.getpin());

							if (localBook == null)
								return;

							int prog = 0;
							if (localBook.size == 0)
								prog = 0;
							else {
								prog = (int) (localBook.progress / localBook.size);
							}

							updateItemView(position, false, false, false, true, prog);
							localBook.mod_time = System.currentTimeMillis();
							localBook.saveModTime();
							MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), localBook.book_id, localBook.source);
							localBook.start(ChangDuActivity.this);
						} else if (item.inWaitingQueue) {

							MZLog.d("wangguodong", "xxxx555555");
							holder.statue_button.setText("继续");
							holder.statue_button.setTextColor(getResources().getColor(R.color.text_main));
							holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_black_h24);
							updateItemView(position, false, true, false, false, PROGRESS_FAILED);
							Toast.makeText(ChangDuActivity.this, "暂停下载", Toast.LENGTH_SHORT).show();
							MZLog.d("wangguodong", "畅读：暂停下载");

							LocalBook book = LocalBook.getLocalBook(eBook.itemId, LoginUser.getpin());
							if (book == null)
								return;
							else
								DownloadService.stop(book);

						} else if (item.failed) {
							MZLog.d("wangguodong", "畅读：重新开始下载");
							MZLog.d("wangguodong", "xxxx666666");
							holder.statue_button.setText("等待");
							holder.statue_button.setTextColor(getResources().getColor(R.color.text_color));
							holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
							updateItemView(position, false, false, true, false, 0);

							LocalBook localBook = LocalBook.getLocalBook(eBook.itemId, LoginUser.getpin());
							if (localBook == null)
								return;

							localBook.progress = 0;
							localBook.state = LocalBook.STATE_LOAD_PAUSED;
							localBook.save();// 修改数据库中的状态，重新下载，zhangmurui
							MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), localBook.book_id, localBook.source);
							localBook.start(ChangDuActivity.this); // 下载失败重新开始下载
						} else {
							// 开始下载
							MZLog.d("wangguodong", " 点击下载....");

							LocalBook localBook = LocalBook.getLocalBook(eBook.itemId, LoginUser.getpin());
							if (localBook != null) {
								int state = DownloadStateManager.getLocalBookState(localBook);

								if (localBook.source.equals(LocalBook.SOURCE_TRYREAD_BOOK)) {
									String pathString = localBook.dir;
									if (!TextUtils.isEmpty(pathString)) {
										IOUtil.deleteFile(new File(pathString));
									}

									localBook.progress = 0;
									localBook.state = LocalBook.STATE_LOAD_PAUSED;
									localBook.source = LocalBook.SOURCE_ONLINE_BOOK;
									localBook.save();
								}

								else if (localBook.source.equals(LocalBook.SOURCE_BUYED_BOOK)) {
									Toast.makeText(ChangDuActivity.this, "本书您已经购买了，请在书架打开", Toast.LENGTH_LONG).show();
									return;
								} else {
									MZLog.d("wangguodong", "本地存在借阅或者内置版本 直接修改source字段");
									localBook.source = LocalBook.SOURCE_ONLINE_BOOK;
									localBook.save();
								}

								if (state == STATE_LOADED) {
									onlineRead(eBook, null);
								} else if (state == STATE_LOADING) {
									// 不能点击
								} else {
									// 加入等待队列
									// 加入等待队列
									holder.statue_button.setText("等待");
									holder.statue_button.setTextColor(getResources().getColor(R.color.text_color));
									holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);

									updateItemData(position, false, false, false, true, -1);

									localBook.progress = 0;
									localBook.state = LocalBook.STATE_LOAD_PAUSED;
									localBook.save();
									MZBookDatabase.instance.updateOtherEbookState(LoginUser.getpin(), localBook.book_id, localBook.source);
									localBook.start(ChangDuActivity.this);
								}

							} else {
								// 加入等待队列
								// 加入等待队列
								//

								onlineRead(eBook, new DownloadConfirmListener() {

									@Override
									public void onConfirm() {
										holder.statue_button.setText("等待");
										holder.statue_button.setTextColor(getResources().getColor(R.color.text_color));
										holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);

										updateItemData(position, false, false, false, true, -1);
									}

									@Override
									public void onCancel() {
										// stub

									}

								});
							}
						}

					}
				});

				if (item.existInLocal) {
					holder.statue_button.setText("阅读");
					holder.statue_button.setTextColor(getResources().getColor(R.color.highlight_color));
					holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_red_h24);
				} else if (item.paused) {
					holder.statue_button.setText("继续");
					holder.statue_button.setTextColor(getResources().getColor(R.color.text_main));
					holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_black_h24);
				}else if (item.inWaitingQueue) {
					holder.statue_button.setText("等待");
					holder.statue_button.setTextColor(getResources().getColor(R.color.text_color));
					holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
				}else if (item.failed) {
					holder.statue_button.setText("失败");
					holder.statue_button.setTextColor(getResources().getColor(R.color.r_text_disable));
					holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
				} else {
					holder.statue_button.setText("下载");
					holder.statue_button.setTextColor(getResources().getColor(R.color.highlight_color));
					holder.statue_button.setBackgroundResource(R.drawable.border_listbtn_red_h24);
				}

			} else {
				holder.statue_button.setVisibility(View.GONE);
				holder.statue_button.setText("无法下载");
				holder.bookSize.setText("暂不支持畅读");
			}
			
			

			String urlPath = eBook.imgUrl;

			ImageLoader.getInstance().displayImage(urlPath, holder.bookCover, GlobalVarable.getCutBookDisplayOptions(false));

			return convertView;
		}

	}

	private void onlineRead(MyOnlineBookEntity onlineBookEntity, DownloadConfirmListener listener) {
		BookInforEDetail bookE = new BookInforEDetail();// 下载实体
		bookE.bookid = onlineBookEntity.itemId;// bookid
		bookE.picUrl = onlineBookEntity.imgUrl;// 小图
		bookE.size = onlineBookEntity.size + "";
		bookE.largeSizeImgUrl = onlineBookEntity.largeSizeImgUrl;// 大图
		bookE.bookType = LocalBook.TYPE_EBOOK;
		// 书的类型:电子书or多媒体书
		bookE.formatName = onlineBookEntity.formatMeaning;// 图书格式。
		bookE.author = onlineBookEntity.author;// 作者
		bookE.bookName = onlineBookEntity.ebookName;// 书名
		OnlineReadManager.requestServer2ReadOnline(bookE, ChangDuActivity.this, null, false, listener);
	}

	public void updateStatusButton(Button statue_button, EBookItemHolder item, int position) {
		MZLog.d("J.Beyond", "position::" + position + "----item.progress::" + item.progress);
		if (item.progress <= PROGRESS_FAILED) {
			statue_button.setText("失败");
			statue_button.setTextColor(getResources().getColor(R.color.r_text_disable));
			statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
		} else if (item.progress < 100) {
			statue_button.setText(item.progress + "%");
			statue_button.setTextColor(getResources().getColor(R.color.r_text_disable));
			statue_button.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
		}
		if (item.progress >= 100) {
			// 更新ui
			statue_button.setText("阅读");
			statue_button.setTextColor(getResources().getColor(R.color.highlight_color));
			statue_button.setBackgroundResource(R.drawable.border_listbtn_red_h24);
		}
	}

	private void searchBuyedBook(String key) {
		gotoflag = 1;
		if (!NetWorkUtils.isNetworkConnected(ChangDuActivity.this)) {
			Toast.makeText(ChangDuActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
			return;
		}

		final List<LocalBook> allLocalBooks = LocalBook.getLocalBookList(null, null);
		WebRequestHelper.get(URLText.JD_BOOK_CHANGDU_URL, RequestParamsPool.searchChangduEbookParams(currentSearchPage + "", perSearchCount + "", key), true,
				new MyAsyncHttpResponseHandler(ChangDuActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						Toast.makeText(ChangDuActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

						String result = new String(responseBody);

						MZLog.d("wangguodong", result);

						ChangduEbook ebook = GsonUtils.fromJson(result, ChangduEbook.class);
						if (ebook != null && ebook.code == 0) {
							if (ebook.resultList.size() == 0 && currentSearchPage == 1) {
								listView.setVisibility(View.GONE);
								relativeLayout.setVisibility(View.VISIBLE);
								changduButton.setVisibility(View.GONE);
								icon.setBackgroundResource(R.drawable.bookstore_icon_search_null);
								textView.setText("畅读列表中暂无您搜索的书籍");
								lackbook_button.setText("去书城搜索");
							}
							currentSearchPage++;

							if (ebook.resultList != null && currentSearchPage > ebook.totalPage)
								noMoreBookOnSearch = true;
							else {
								noMoreBookOnSearch = false;
							}

							List<EBookItemHolder> all = new ArrayList<EBookItemHolder>();
							for (JDOnlineBookEntity book : ebook.resultList) {
								MyOnlineBookEntity entity = book.toMyOnlineBookEntity(book);
								all.add(checkBookState(allLocalBooks, entity));
							}

							eBookItemList.addAll(all);
							if (eBookItemList.size() == 0) {
								Toast.makeText(getApplicationContext(), "抱歉,没找到该书!", Toast.LENGTH_LONG).show();
							}
							bookListAdapter.notifyDataSetChanged();
						} else if (ebook == null)
							Toast.makeText(ChangDuActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
						inLoadingMoreOnSearch = false;
					}

				});

	}

	private void listBuyedEbook() {
		gotoflag = 0;
		if (!NetWorkUtils.isNetworkConnected(ChangDuActivity.this)) {
			MZLog.d("wangguodong", "111111");
			Toast.makeText(ChangDuActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
			return;
		}

		// MZLog.d("wangguodong", "本地书籍ebook个数:"+allLocalBooks.size()+"个");
		WebRequestHelper.post(URLText.JD_BOOK_CHANGDU_URL, RequestParamsPool.getChangduEbookParams(currentPage + "", perPageCount + ""), true,
				new MyAsyncHttpResponseHandler(ChangDuActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						MZLog.d("wangguodong", "22222222");
						Toast.makeText(ChangDuActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

						String result = new String(responseBody);
						MZLog.d("cj", "result========>>" + result);
						ChangduEbook ebook = GsonUtils.fromJson(result, ChangduEbook.class);
						if (ebook != null && ebook.code == 0) {
							if (ebook.resultList.size() == 0 && currentPage == 1) {
								listView.setVisibility(View.GONE);
								relativeLayout.setVisibility(View.VISIBLE);
								changduButton.setVisibility(View.VISIBLE);
								icon.setBackgroundResource(R.drawable.icon_empty);
								textView.setText("您的畅读列表暂无书籍，如果您已经拥有畅读卡，就快去书城选购吧");
								lackbook_button.setText("去书城");
							}
							currentPage++;
//							MZLog.d("cj", "resultist======>>>>>" + ebook.resultList.size());
							if (ebook.resultList != null && currentPage > ebook.totalPage)
								noMoreBook = true;
							else {
								noMoreBook = false;
							}

							List<EBookItemHolder> all = new ArrayList<EBookItemHolder>();
							if (ebook.resultList.size() > 0) {
								String[] book_ids = new String[ebook.resultList.size()];
								for (int i = 0; i < ebook.resultList.size(); i++) {
									JDOnlineBookEntity book = ebook.resultList.get(i);
									book_ids[i] = String.valueOf(book.itemId);
								}

								final List<LocalBook> allLocalBooks = MZBookDatabase.instance.getLocalBooks(null, book_ids);
								for (JDOnlineBookEntity book : ebook.resultList) {
									MyOnlineBookEntity entity = book.toMyOnlineBookEntity(book);
									all.add(checkBookState(allLocalBooks, entity));
								}
								eBookItemList.addAll(all);
							}

							bookListAdapter.notifyDataSetChanged();
						} else if (ebook == null) {
							Toast.makeText(ChangDuActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
						}

						inLoadingMore = false;
					}

				});

	}

	public EBookItemHolder checkBookState(List<LocalBook> list, MyOnlineBookEntity entity) {

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
			if (localBook.book_id == entity.itemId) {

				if (localBook.source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {
					int state = DownloadStateManager.getLocalBookState(localBook);
					if (state == DownloadStateManager.STATE_LOADED || holder.existInLocal) {
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
	public void refresh(DownloadedAble DownloadAble) {
		final LocalBook localBook = (LocalBook) DownloadAble;
		if (localBook == null) {
			return;
		}

		int position = bookListAdapter.getIndexByEbookId(localBook.book_id);
		int progress = (int) (100 * (localBook.progress / (localBook.size * 1.0)));
		int state = DownloadStateManager.getLocalBookState(localBook);
		MZLog.d("Download", "download progress::" + progress);
		// bookListAdapter.updateItemData(position, existInLocal, pause, failed,
		// inWaitingQueue, progress);
		if (state == DownloadStateManager.STATE_LOADED) {
			if (!localBook.source.equals(LocalBook.SOURCE_ONLINE_BOOK)) {
				// 只显示阅读按钮
				// OnlineButtonShowOnly(localBook,true);
			} else {

				// OnlineButtonShowOnly(localBook,false);
				// layoutButtonWithServer(bookE, true, true, false, false,
				// true);
			}
		} else if (state == DownloadStateManager.STATE_LOADING) {

			MZLog.d("wangguodong", "畅读:正在下载，进度:" + progress * 100 / 360.0 + "%" + "下载文件id=" + DownloadAble.getId());
			if (localBook.state == LocalBook.STATE_LOADING || localBook.state == LocalBook.STATE_LOAD_READY) {
				sendMessage(position, progress);

			}

		} else {
			// 下载失败了
			MZLog.d("wangguodong", "畅读:下载失败");
			sendMessage(position, PROGRESS_FAILED);
		}

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

}
