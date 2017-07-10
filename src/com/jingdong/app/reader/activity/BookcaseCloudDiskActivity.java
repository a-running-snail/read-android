package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.client.DownloadStateManager;
import com.jingdong.app.reader.client.DownloadThreadQueue.RefreshAble;
import com.jingdong.app.reader.client.DownloadTool;
import com.jingdong.app.reader.client.DownloadTool.DownloadConfirmListener;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.common.CommonActivity;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.entity.extra.MzDocument;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.NetWorkUtils.NetworkConnectType;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BookcaseCloudDiskActivity extends CommonActivity implements
		TopBarViewListener, RefreshAble {
	private final static int MAX_SHARE_LINE = 200;
	
	private String authToken = "";
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
	private int total = 0;
	private List<DocumentItemHolder> docList = new ArrayList<DocumentItemHolder>();
	private CloudBookAdapter adapter;
	private ListView listView;
//	private TextView totalView;
	private ProgressBar bar;

	private LinearLayout uploadButton;

//	private FrameLayout totalRowFrameLayout;

	private List<Map<String, String>> localSigns = new ArrayList<Map<String, String>>();

	protected final static int STATE_LOAD_FAIL = 0;// 未下载、下载失败
	protected final static int STATE_LOADING = 1; // 下载、暂停中
	protected final static int STATE_LOADED = 2; // 下载完成

	protected final static int PROGRESS_FAILED = -1; // 下载失败的进度

	protected final static int UPDATE_UI_MESSAGE = 0;

	private TopBarView topBarView = null;
	private EditText edittext_serach;// 搜索关键字输入框
	private RelativeLayout relativeLayout = null;
	private ImageView icon;
	private TextView textView;
	private Button lackbook_button;
	private int gotoflag = -1;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			if (msg.what == UPDATE_UI_MESSAGE) {
				if (adapter != null) {
					adapter.updateItemView(msg.arg1, msg.arg2);// arg1
																// position
																// ,arg2
																// progress
				}
			}

		};
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		adapter = new CloudBookAdapter();
		setContentView(R.layout.fragment_cloud_disk);

		// 初始化topbar 开始
		topBarView = (TopBarView) findViewById(R.id.topbar);
		topBarView.setRightMenuOneVisiable(true, "上传", R.color.red_main, false);
		initTopbarView();
		// 初始化topbar 结束

		edittext_serach = (EditText) findViewById(R.id.edittext_serach);
		edittext_serach.setHint(getString(R.string.bookshelf_search_text_hit));

		edittext_serach.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == event.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					docList.clear();
//					totalRowFrameLayout.setVisibility(View.GONE);
					adapter.notifyDataSetChanged();
					isSearchCommited = true;
					inSearch = true;
					initData();
					searchCloudDiskBook(edittext_serach.getText().toString());
					return true;
				}
				return false;
			}
		});

		edittext_serach.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				MZLog.d("cj", TextUtils.isEmpty(edittext_serach.getText()
						.toString()) ? "true" : "false");

				if (isSearchCommited
						&& TextUtils.isEmpty(edittext_serach.getText()
								.toString())) {
					listView.setVisibility(View.VISIBLE);
					relativeLayout.setVisibility(View.GONE);
					docList.clear();
					adapter.notifyDataSetChanged();
					inSearch = false;
					initData();
//					totalRowFrameLayout.setVisibility(View.VISIBLE);
					getCloudDiskBook();
					isSearchCommited = false;
				}
			}
		});

		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_DOCUMENT);
		listView = (ListView) findViewById(R.id.cloud_disk_book_list);
		relativeLayout = (RelativeLayout) findViewById(R.id.search_result_container);
		icon = (ImageView) findViewById(R.id.icon);
		textView = (TextView) findViewById(R.id.text);
		lackbook_button = (Button) findViewById(R.id.lackbook_button);
		lackbook_button.setVisibility(View.GONE);
//		listView.setDivider(null);
//		listView.setDividerHeight(0);

//		totalRowFrameLayout = (FrameLayout) findViewById(R.id.total_row);

//		uploadButton = (LinearLayout) findViewById(R.id.upload_book);
//		totalView = (TextView) findViewById(R.id.total_book);
		
		
		bar = (ProgressBar) findViewById(R.id.refresh_progress);
//		totalView.setText(BookcaseCloudDiskActivity.this.getResources()
//				.getString(R.string.cloud_book_count, 0));

		/*uploadButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
//				startActivity(new Intent(BookcaseCloudDiskActivity.this,
//						UploadToCloudDiskActivity.class));
				startActivityForResult(new Intent(BookcaseCloudDiskActivity.this,
						UploadToCloudDiskActivity.class), 10001);
			}
		});*/

		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount == 0)
					return;
				if (firstVisibleItem + visibleItemCount == totalItemCount
						&& !noMoreBook && !inSearch) {
					BookcaseCloudDiskActivity.this
							.setProgressBarIndeterminateVisibility(true);
					if (!inLoadingMore) {
						inLoadingMore = true;
						bar.setVisibility(View.VISIBLE);
						getCloudDiskBook();
					}

				}

				if (firstVisibleItem + visibleItemCount == totalItemCount
						&& !noMoreBookOnSearch && inSearch) {
					if (!inLoadingMoreOnSearch) {
						inLoadingMoreOnSearch = true;
						String key = edittext_serach.getText().toString();
						searchCloudDiskBook(key);
					}
				}
			}

		});
		listView.setAdapter(adapter);
		
		getCloudDiskBook();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("JD_Reader", "---------requestCode="+requestCode+",resultCode"+resultCode);
		if (requestCode == 10001) {
			currentPage = 1;
			docList.clear();
			getCloudDiskBook();
		}
	}

	private void initTopbarView() {
		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topBarView.setListener(this);
		topBarView.setTitle("云盘");
	}

	@Override
	public void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_yunpan));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_yunpan));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	public void updateItemData(int position, boolean existInLocal,
			boolean inWaitingQueue, int progress) {
		if (docList != null && docList.size() > position) {

			DocumentItemHolder holder = docList.get(position);
			holder.existInLocal = existInLocal;
			holder.inWaitingQueue = inWaitingQueue;
			holder.progress = progress;
			docList.set(position, holder);

		}

	}

	public void searchCloudDiskBook(String query) {
		if(!NetWorkUtils.isNetworkConnected(BookcaseCloudDiskActivity.this))
		{
			Toast.makeText(BookcaseCloudDiskActivity.this,
					getString(R.string.network_connect_error),
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		final List<LocalDocument> list = LocalDocument.getLocalBookList();

		WebRequestHelper.get(URLText.MZ_BOOK_YUNPAN_SEARCH_URL,
				RequestParamsPool.getYunPanSearchParams(query, currentPage,
						perPageCount), true, new MyAsyncHttpResponseHandler(
						BookcaseCloudDiskActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						MZLog.d("wangguodong", new String(arg2));
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						List<MzDocument> templist = new ArrayList<MzDocument>();
						String result = new String(responseBody);
						int totalcount = 0;
						try {
							JSONObject object = new JSONObject(result);

							totalcount = object.optInt("total");

							JSONArray array = object.optJSONArray("documents");

							if (array != null) {
								if (array.length() == 0 && currentPage == 1) {
									listView.setVisibility(View.GONE);
									relativeLayout.setVisibility(View.VISIBLE);
									icon.setBackgroundResource(R.drawable.bookstore_icon_search_null);
									textView.setText("云盘列表中暂无您搜索的书籍");
								}
								currentSearchPage++;
								if (array.length() < perPageCount) {
									noMoreBookOnSearch = true;
								} else {
									noMoreBookOnSearch = false;
								}
								for (int i = 0; i < array.length(); i++) {

									MzDocument document = GsonUtils.fromJson(
											array.getString(i),
											MzDocument.class);
									MZLog.d("wangguodong", "type:"
											+ document.type + "%%serverid"
											+ document.id);
									templist.add(document);
								}

							}
						} catch (Exception e) {
							e.printStackTrace();

						}
						
						for (int i = 0; i < templist.size(); i++) {

							LocalDocument temp = MzDocument
									.MzDocumentToLocalDocument(templist.get(i));
							MZLog.d("wangguodong", "localdocument type:"
									+ temp.format);
							docList.add(checkBookState(list, temp));
						}

//						totalView.setText(BookcaseCloudDiskActivity.this
//								.getResources().getString(
//										R.string.cloud_book_count, totalcount));
						adapter.notifyDataSetChanged();

						inLoadingMoreOnSearch = false;
						bar.setVisibility(View.GONE);

					}
				});
	}

	public void getCloudDiskBook() {

		if(!NetWorkUtils.isNetworkConnected(BookcaseCloudDiskActivity.this))
		{
			Toast.makeText(BookcaseCloudDiskActivity.this,
					getString(R.string.network_connect_error),
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		final List<LocalDocument> list = LocalDocument.getLocalBookList();

		WebRequestHelper.get(URLText.MZ_BOOK_YUNPAN_URL, RequestParamsPool
				.getYunPanListParams(currentPage, perPageCount), true,
				new MyAsyncHttpResponseHandler(BookcaseCloudDiskActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						if (arg2 != null)
							MZLog.d("wangguodong", new String(arg2));
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						List<MzDocument> templist = new ArrayList<MzDocument>();
						String result = new String(responseBody);
						int totalcount = 0;
						try {
							JSONObject object = new JSONObject(result);

							totalcount = object.optInt("total");   

							JSONArray array = object.optJSONArray("documents");

							if (array != null) {
								if (array.length() == 0 && currentPage == 1) {
									listView.setVisibility(View.GONE);
									relativeLayout.setVisibility(View.VISIBLE);
									icon.setBackgroundResource(R.drawable.icon_empty);
									textView.setText("您的云盘列表暂无书籍，将第三方电子书上传到云盘后，即可多设备同步阅读");
								}
								currentPage++;
								if (array.length() < perPageCount) {
									noMoreBook = true;
								} else {
									noMoreBook = false;
								}
								for (int i = 0; i < array.length(); i++) {

									MzDocument document = GsonUtils.fromJson(
											array.getString(i),
											MzDocument.class);
									MZLog.d("wangguodong", "type:"
											+ document.type + "%%serverid"
											+ document.id);
									templist.add(document);
								}

							}
						} catch (Exception e) {
							e.printStackTrace();

						}
						
						for (int i = 0; i < templist.size(); i++) {

							LocalDocument temp = MzDocument
									.MzDocumentToLocalDocument(templist.get(i));
							MZLog.d("wangguodong", "localdocument type:"
									+ temp.format);
							docList.add(checkBookState(list, temp));
						}

//						totalView.setText(BookcaseCloudDiskActivity.this
//								.getResources().getString(
//										R.string.cloud_book_count, totalcount));
						adapter.notifyDataSetChanged();

						inLoadingMore = false;
						bar.setVisibility(View.GONE);

					}
				});
	}

	public DocumentItemHolder checkBookState(List<LocalDocument> list,
			LocalDocument entity) {

		DocumentItemHolder holder = new DocumentItemHolder();
		holder.document = entity;
		holder.existInLocal = false;
		holder.failed = false;
		holder.paused = false;
		holder.inWaitingQueue = false;

		if (null == list || list.size() == 0)
			return holder;// 本地没有任何数据
		for (int i = 0; i < list.size(); i++) {

			LocalDocument localBook = list.get(i);
			if (localBook.server_id == entity.server_id) {
//				holder.document = localBook;
				int state = DownloadStateManager
						.getLocalDocumentState(localBook);
				if (state == DownloadStateManager.STATE_LOADED) {
					holder.existInLocal = true;

				} else if (state == DownloadStateManager.STATE_LOADING) {
					if (localBook.state == LocalBook.STATE_LOADING
							|| localBook.state == LocalBook.STATE_LOAD_READY) {
						holder.inWaitingQueue = true;
					}
					if (localBook.state == LocalBook.STATE_LOAD_PAUSED) {
						holder.paused = true;
					}
				} else {
					holder.failed = true;
				}

			}
		}
		return holder;
	}

	private static class DocumentItemHolder {
		LocalDocument document;
		boolean existInLocal;
		boolean paused;
		boolean failed;
		boolean inWaitingQueue;
		int progress;
	}

	public boolean isContainsSign(String sign) {

		for (int i = 0; i < localSigns.size(); i++) {
			if (sign.equals(localSigns.get(i).get("sign"))) {
				return true;
			}
		}

		return false;

	}

	class CloudBookAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return docList == null ? 0 : docList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return docList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {

			return arg0;
		}

		class ViewHolder {

			TextView bookTitle;
			TextView bookAuthor;
			TextView bookSize;
			ImageView bookCover;
			Button statue_button;
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

		public int getIndexByEbookId(long bookid) {
			if (docList != null) {
				for (int i = 0; i < docList.size(); i++) {
					if (docList.get(i).document.server_id == bookid) {
						return i;
					}
				}
				return -1;
			} else {
				return -1;
			}
		}

		public void updateItemData(int position, boolean existInLocal,
				boolean pause, boolean failed, boolean inWaitingQueue,
				int progress) {
			if (docList != null && docList.size() > position && position >= 0) {
				DocumentItemHolder holder = docList.get(position);
				holder.existInLocal = existInLocal;
				holder.paused = pause;
				holder.failed = failed;
				holder.inWaitingQueue = inWaitingQueue;
				holder.progress = progress;
				docList.set(position, holder);
			}

		}

		public void readDocument(ViewHolder holder, final Document doc) {

			holder.statue_button
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View arg0) {
							int docId = MZBookDatabase.instance
									.getDocmentId(doc.opfMD5);
							if (docId != -1) {
								doc.documentId = docId;
								OpenBookHelper.openDocument(
										BookcaseCloudDiskActivity.this,
										doc.documentId);
							} else {
								Toast.makeText(BookcaseCloudDiskActivity.this,
										getString(R.string.can_not_open),
										Toast.LENGTH_LONG).show();
							}

						}
					});

		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final ViewHolder holder;

			if (convertView == null) {

				convertView = LayoutInflater.from(
						BookcaseCloudDiskActivity.this).inflate(
						R.layout.item_free_gifts_booklist, parent, false);
				holder = new ViewHolder();
				holder.bookTitle = (TextView) convertView
						.findViewById(R.id.user_book_name);
				holder.bookAuthor = (TextView) convertView
						.findViewById(R.id.user_book_author);
				holder.bookSize = (TextView) convertView
						.findViewById(R.id.book_size);

				holder.bookCover = (ImageView) convertView
						.findViewById(R.id.user_book_cover);
				holder.statue_button = (Button) convertView.findViewById(R.id.statueButton);

				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();

			}

			if (docList == null || docList.size() == 0)
				return convertView;

			final DocumentItemHolder item = docList.get(position);
			final LocalDocument doc = item.document;
			holder.bookTitle.setText(doc.title);
			holder.bookAuthor.setText(doc.author);
			holder.bookSize.setText(String.format("%.2f",
					(float) doc.size / 1048576) + "M");
			holder.statue_button.setText("下载");
			holder.statue_button.setTextColor(getResources().getColor(
				R.color.highlight_color));
			holder.statue_button
				.setBackgroundResource(R.drawable.border_listbtn_red_h24);

			holder.statue_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (item.existInLocal) {
						MZLog.d("wangguodong", "开始阅读");
						// OpenBookHelper.openBook(BookcaseCloudDiskActivity.this,
						// null, LocalDocument.toDocument(doc));
						readDocument(holder,LocalDocument.toDocument(doc));
					} else if (item.paused) {
//						if (!NetWorkUtils.isNetworkAvailable(BookcaseCloudDiskActivity.this)) {
//							Toast.makeText(BookcaseCloudDiskActivity.this, "网络不可用", Toast.LENGTH_LONG).show();
//							return;
//						}
//						if (NetWorkUtils.getNetworkConnectType(BookcaseCloudDiskActivity.this) == NetworkConnectType.MOBILE) {
//							String title="图书大小为"+String.format("%.2f",(float) doc.size / 1048576) + "M";
//							DialogManager.showCommonDialog(BookcaseCloudDiskActivity.this, title, "确定使用移动网络下载吗", "确定", "取消", new DialogInterface.OnClickListener() {
//								
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									switch (which) {
//									case DialogInterface.BUTTON_POSITIVE:
//										MZLog.d("wangguodong", "开始任务");
//										LocalDocument localBook = LocalDocument
//												.getLocalDocumentByServerid(doc.server_id,
//														LoginUser.getpin());
//										if(localBook==null) return ;
//										
//										int prog = 0;
//										if (localBook.size == 0)
//											prog = 0;
//										else {
//											prog = (int) (localBook.progress / localBook.size);
//										}
//
//										updateItemView(position, false, false, false, true,
//												prog);
//
//										LocalDocument
//												.start(BookcaseCloudDiskActivity.this, doc);
//										break;
//									case DialogInterface.BUTTON_NEGATIVE:
//										
//										break;
//									default:
//										break;
//									}
//									dialog.dismiss();
//								}
//							}); 
//						}else {
							MZLog.d("wangguodong", "开始任务");
							final LocalDocument localBook = LocalDocument
									.getLocalDocumentByServerid(doc.server_id,
											LoginUser.getpin());
							if(localBook==null) return ;
							
													

//							LocalDocument
//									.start(BookcaseCloudDiskActivity.this, doc);
							DownloadTool.downloadDocument(BookcaseCloudDiskActivity.this, localBook, false,new DownloadConfirmListener() {
								
								@Override
								public void onConfirm() {
									// TODO Auto-generated method stub
									int prog = 0;
									if (localBook.size == 0)
										prog = 0;
									else {
										prog = (int) (localBook.progress / localBook.size);
									}
									updateItemView(position, false, false, false, true,prog);
									LocalDocument.start(BookcaseCloudDiskActivity.this, doc);
								}
								
								@Override
								public void onCancel() {
									// TODO Auto-generated method stub
									
								}
							});
//						}
						

					} else if (item.inWaitingQueue) {
						holder.statue_button.setText("继续");
						holder.statue_button.setTextColor(getResources()
								.getColor(R.color.text_main));
						holder.statue_button
								.setBackgroundResource(R.drawable.border_listbtn_black_h24);
						updateItemView(position, false, true, false, false,
								PROGRESS_FAILED);
						Toast.makeText(BookcaseCloudDiskActivity.this, "暂停下载",
								Toast.LENGTH_SHORT).show();
						MZLog.d("wangguodong", "云盘：暂停下载");
						
						LocalDocument localBook = LocalDocument
								.getLocalDocumentByServerid(doc.server_id,
										LoginUser.getpin());
						if(localBook==null) return ;
						
						DownloadService.stop(localBook);
					} else if (item.failed) {
						if (!NetWorkUtils.isNetworkAvailable(BookcaseCloudDiskActivity.this)) {
							Toast.makeText(BookcaseCloudDiskActivity.this, "网络不可用", Toast.LENGTH_LONG).show();
							return;
						}
						if (NetWorkUtils.getNetworkConnectType(BookcaseCloudDiskActivity.this) == NetworkConnectType.MOBILE) {
							String title="图书大小为"+String.format("%.2f",(float) doc.size / 1048576) + "M";
							DialogManager.showCommonDialog(BookcaseCloudDiskActivity.this, title, "确定使用移动网络下载吗", "确定", "取消", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case DialogInterface.BUTTON_POSITIVE:
										MZLog.d("wangguodong", "云盘：重新开始下载");
										MZLog.d("wangguodong", "xxxx666666");
										holder.statue_button.setText("等待");
										holder.statue_button.setTextColor(getResources()
												.getColor(R.color.text_color));
										holder.statue_button
												.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
										updateItemView(position, false, false, true, false, 0);

										LocalDocument localBook = LocalDocument
												.getLocalDocumentByServerid(doc.server_id,
														LoginUser.getpin());
										if(localBook==null) return ;
										localBook.progress = 0;
										localBook.state = LocalBook.STATE_LOAD_PAUSED;
										localBook.save();// 修改数据库中的状态，重新下载，zhangmurui

										LocalDocument
												.start(BookcaseCloudDiskActivity.this, doc);
										break;
									case DialogInterface.BUTTON_NEGATIVE:
										
										break;
									default:
										break;
									}
									dialog.dismiss();
								}
							}); 
						}else {
							MZLog.d("wangguodong", "云盘：重新开始下载");
							MZLog.d("wangguodong", "xxxx666666");
							holder.statue_button.setText("等待");
							holder.statue_button.setTextColor(getResources()
									.getColor(R.color.text_color));
							holder.statue_button
									.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
							updateItemView(position, false, false, true, false, 0);

							LocalDocument localBook = LocalDocument
									.getLocalDocumentByServerid(doc.server_id,
											LoginUser.getpin());
							if(localBook==null) return ;
							localBook.progress = 0;
							localBook.state = LocalBook.STATE_LOAD_PAUSED;
							localBook.save();// 修改数据库中的状态，重新下载，zhangmurui

							LocalDocument
									.start(BookcaseCloudDiskActivity.this, doc);
						}
						
						

					} else {
//						if (!NetWorkUtils.isNetworkAvailable(BookcaseCloudDiskActivity.this)) {
//							Toast.makeText(BookcaseCloudDiskActivity.this, "网络不可用", Toast.LENGTH_LONG).show();
//							return;
//						}
//						if (NetWorkUtils.getNetworkConnectType(BookcaseCloudDiskActivity.this) == NetworkConnectType.MOBILE) {
//							String title="图书大小为"+String.format("%.2f",(float) doc.size / 1048576) + "M";
//							DialogManager.showCommonDialog(BookcaseCloudDiskActivity.this, title, "确定使用移动网络下载吗", "确定", "取消", new DialogInterface.OnClickListener() {
//								
//								@Override
//								public void onClick(DialogInterface dialog, int which) {
//									switch (which) {
//									case DialogInterface.BUTTON_POSITIVE:
//										// 加入等待队列
//										holder.statue_button.setText("等待");
//										holder.statue_button.setTextColor(getResources()
//												.getColor(R.color.text_color));
//										holder.statue_button
//												.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
//
//										updateItemData(position, false, false, false, true, -1);
//										DownloadTool.downloadDocument(
//												BookcaseCloudDiskActivity.this, doc, false);
//										break;
//									case DialogInterface.BUTTON_NEGATIVE:
//										
//										break;
//									default:
//										break;
//									}
//									dialog.dismiss();
//								}
//							}); 
//							
//						}else {
//							// 加入等待队列
//							holder.statue_button.setText("等待");
//							holder.statue_button.setTextColor(getResources()
//									.getColor(R.color.text_color));
//							holder.statue_button
//									.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
//
//							updateItemData(position, false, false, false, true, -1);
//							DownloadTool.downloadDocument(
//									BookcaseCloudDiskActivity.this, doc, false);
//						}
//						 加入等待队列
						DownloadTool.downloadDocument(
								BookcaseCloudDiskActivity.this, doc, false,new DownloadConfirmListener() {
									
									@Override
									public void onConfirm() {
										// TODO Auto-generated method stub
										holder.statue_button.setText("等待");
										holder.statue_button.setTextColor(getResources()
												.getColor(R.color.text_color));
										holder.statue_button
												.setBackgroundResource(R.drawable.border_listbtn_grey_h24);

										updateItemData(position, false, false, false, true, -1);
									}
									
									@Override
									public void onCancel() {
										// TODO Auto-generated method stub
										
									}
								});
						
					}

				}
			});

			if (item.existInLocal) {
				holder.statue_button.setText("阅读");
				holder.statue_button.setTextColor(getResources().getColor(
						R.color.highlight_color));
				holder.statue_button
						.setBackgroundResource(R.drawable.border_listbtn_red_h24);
			} else if (item.paused) {
				holder.statue_button.setText("继续");
				holder.statue_button.setTextColor(getResources().getColor(
						R.color.text_main));
				holder.statue_button
						.setBackgroundResource(R.drawable.border_listbtn_black_h24);
			}

			else if (item.inWaitingQueue) {
				holder.statue_button.setText("等待");
				holder.statue_button.setTextColor(getResources().getColor(
						R.color.text_color));
				holder.statue_button
						.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
			}

			else if (item.failed) {
				holder.statue_button.setText("失败");
				holder.statue_button.setTextColor(getResources().getColor(
						R.color.r_text_disable));
				holder.statue_button
						.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
			} else {
				holder.statue_button.setText("下载");
				holder.statue_button.setTextColor(getResources().getColor(
						R.color.highlight_color));
				holder.statue_button
						.setBackgroundResource(R.drawable.border_listbtn_red_h24);
			}

			String urlPath = doc.serverImageUrl;

			if (TextUtils.isEmpty(urlPath))
				ImageLoader.getInstance().displayImage(
						"drawable://" + R.drawable.bg_default_cover,
						holder.bookCover,
						GlobalVarable.getDefaultBookDisplayOptions());

			else {
				ImageLoader.getInstance().displayImage(urlPath,
						holder.bookCover,
						GlobalVarable.getCutBookDisplayOptions());
			}
			return convertView;
		}

	}

	public void initData() {

		currentPage = 1;
		noMoreBook = false;
		inLoadingMore = true;

		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;

	}

	// @Override
	// public boolean onQueryTextSubmit(String query) {
	//
	// docList.clear();
	// totalRowFrameLayout.setVisibility(View.GONE);
	// adapter.notifyDataSetChanged();
	// isSearchCommited = true;
	// inSearch = true;
	// initData();
	// searchCloudDiskBook(query);
	// return true;
	//
	// }
	//
	// @Override
	// public boolean onQueryTextChange(String newText) {
	// return false;
	// }
	//
	// @Override
	// public boolean onMenuItemActionExpand(MenuItem item) {
	// return true;
	// }
	//
	// @Override
	// public boolean onMenuItemActionCollapse(MenuItem item) {
	// if (isSearchCommited) {
	// docList.clear();
	// adapter.notifyDataSetChanged();
	// inSearch = false;
	// initData();
	// totalRowFrameLayout.setVisibility(View.VISIBLE);
	// getCloudDiskBook();
	// }
	//
	// isSearchCommited = false;
	// return true;
	// }

	@Override
	public void refresh(DownloadedAble DownloadAble) {

		MZLog.d("wangguodong", "document 进度更新");
		final LocalDocument localBook = (LocalDocument) DownloadAble;
		if (localBook == null) {
			return;
		}

		int position = adapter.getIndexByEbookId(localBook.server_id);
		int progress = (int) (100 * (localBook.progress / (localBook.size * 1.0)));

		int state = DownloadStateManager.getLocalDocumentState(localBook);
		if (state == DownloadStateManager.STATE_LOADED) {

			sendMessage(position, 100);

		} else if (state == DownloadStateManager.STATE_LOADING) {
			if (localBook.state == LocalBook.STATE_LOADING
					|| localBook.state == LocalBook.STATE_LOAD_READY) {
				sendMessage(position, progress);
			}

		} else {
			// 下载失败了
			sendMessage(position, PROGRESS_FAILED);
		}

	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void sendMessage(int position, int progress) {
		Message msg = new Message();
		msg.what = UPDATE_UI_MESSAGE;
		msg.arg1 = position;
		msg.arg2 = progress;
		handler.sendMessage(msg);
	}

	@Override
	public void refreshDownloadCache() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLeftMenuClick() {
		// TODO Auto-generated method stub
		finish();
	}


	@Override
	public void onCenterMenuItemClick(int position) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRightMenuOneClick() {
		startActivityForResult(new Intent(BookcaseCloudDiskActivity.this,
				UploadToCloudDiskActivity.class), 10001);
	}

	@Override
	public void onRightMenuTwoClick() {
		// TODO Auto-generated method stub
		
	}
	

}
