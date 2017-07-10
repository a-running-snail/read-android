package com.jingdong.app.reader.bookstore.search;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.mzbook.sortview.model.BookShelfModel;
import com.android.mzbook.sortview.optimized.DragItemAdapter;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookcaseCloudActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.book.EBook;
import com.jingdong.app.reader.client.DownloadHelper;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.client.DownloadStateManager;
import com.jingdong.app.reader.client.DownloadTool;
import com.jingdong.app.reader.client.DownloadThreadQueue.RefreshAble;
import com.jingdong.app.reader.client.DownloadTool.DownloadConfirmListener;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.extra.JDEBook;
import com.jingdong.app.reader.entity.extra.LocalDocument;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ImageTool;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.util.SettingUtils;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.view.CustomProgreeDialog;
import com.jingdong.app.reader.view.SearchTopBarView;
import com.jingdong.app.reader.view.SearchTopBarView.TopBarViewListener;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BookShelfSearchActivity extends Activity implements TopBarViewListener,RefreshAble {

	private ListView mlistView;
	private ProgressDialog mypDialog;
	private BookListAdapter bookListAdapter = null;
	private MZBookDatabase mzBookDatabase = null;
	private List<BookShelfModel> bookselflist = null;
	private boolean inSearch = false;
	private boolean isSearchCommited = false;
	private String userid = null;
	private SearchTopBarView topBarView = null;
	private EditText edittext_serach;// 搜索关键字输入框
	private Button lackbook_button;
	private LinearLayout linearLayout;
	protected final static int UPDATE_UI_MESSAGE = 100;
	private boolean isResume = false;


	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			if (msg.what == UPDATE_UI_MESSAGE) {
				Bundle bundleData = (Bundle) msg.obj;
				int index = bundleData.getInt("index");
				int status = bundleData.getInt("status");
				int progress = bundleData.getInt("progress");
				MZLog.d("JD_Reader", "handleMessage-->index:"+index+",status:"+status+",progress:"+progress);
				if (bookListAdapter != null) {
					bookListAdapter.updateItemView(index, status, progress);
				}
			}
		};
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.booself_search);
		// 初始化topbar 开始
		topBarView = (SearchTopBarView) findViewById(R.id.topbar);
		initTopbarView();
		// 初始化topbar 结束
		mypDialog = CustomProgreeDialog.instace(BookShelfSearchActivity.this);
		mlistView = (ListView) findViewById(R.id.listview);
		mzBookDatabase = new MZBookDatabase(BookShelfSearchActivity.this);
		bookselflist = new ArrayList<BookShelfModel>();
		userid = LoginUser.getpin();
		edittext_serach = (EditText) findViewById(R.id.edittext_serach);
		edittext_serach.setHint(getString(R.string.bookshelf_search_text_hit));
		edittext_serach.setLongClickable(true);
		lackbook_button = (Button) findViewById(R.id.lackbook_button);
		linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
//		edittext_serach.setCursorVisible(true);
		edittext_serach.setFocusable(true);
		edittext_serach.setFocusableInTouchMode(true);  
		edittext_serach.requestFocus();
		mlistView.setVisibility(View.VISIBLE);
		linearLayout.setVisibility(View.GONE);
		
		lackbook_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(BookShelfSearchActivity.this,BookStoreSearchActivity.class);
				intent.putExtra("key", edittext_serach.getText().toString());
				startActivity(intent);
				finish();
			}
		});
		
		edittext_serach.setOnKeyListener(new View.OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (keyCode == event.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					String query = edittext_serach.getText().toString();
					if (query != null && !query.toString().equals("")) {
						searchBuyedBook(query);
					} else{
						Toast.makeText(BookShelfSearchActivity.this, "请输入关键字！", Toast.LENGTH_LONG).show();
					}
					return true;
				}
				return false;
			}
		});
		
		mlistView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				BookShelfModel eBook = bookselflist.get(position);
				MZLog.d("cj", "eBook========>>"+eBook.getBookName() + "type====>>"+ eBook.getBookType());
				if (eBook.getBookType().equals(BookShelfModel.EBOOK)) {
					EBook ebook = MZBookDatabase.instance.getEBook(eBook.getBookid());
					OpenBookHelper.openEBook(BookShelfSearchActivity.this, ebook.bookId);

				} else if (eBook.getBookType().equals(BookShelfModel.DOCUMENT)) {
					Document doc = MZBookDatabase.instance.getDocument(eBook
							.getBookid());
					OpenBookHelper.openDocument(BookShelfSearchActivity.this, doc.documentId);

				}
			}
		});
	}
	
	
	
	private void initTopbarView() {
		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topBarView.setRightMenuVisiable(false);
		topBarView.setListener(this);
		topBarView.updateTopBarView(true);
	}

	// 第一次进入 OnPageChangeListener没有执行 searchview没有设置 所以不可以查询 修复不能查询错误
	public void searchBuyedBook(String query) {
		bookselflist = null;
		bookselflist = mzBookDatabase.getBookShelfBooksByTitle(
				query.toString(), userid);
		if (bookselflist != null) {
			bookListAdapter = new BookListAdapter(
					BookShelfSearchActivity.this);
			mlistView.setAdapter(bookListAdapter);
		} else {
			if (bookListAdapter != null) {
				bookListAdapter.notifyDataSetChanged();
			}
			mlistView.setVisibility(View.GONE);
			linearLayout.setVisibility(View.VISIBLE);
		}
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_DOCUMENT);
		
		String query = edittext_serach.getText().toString();
		if (query != null && !query.toString().equals("")) {
			searchBuyedBook(query);
		} 
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_sousuo));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		// unregisterReceiver();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_sousuo));
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	private class BookListAdapter extends BaseAdapter {

		private Context context;

		class ViewHolder {

			TextView bookTitle;
			TextView bookAuthor;
			TextView progresstxt;
			TextView readTime;
			Button statueButton;
			ImageView bookCover;
		}

		BookListAdapter(Context context) {
			this.context = context;
		}
		public void updateItemData(int position, boolean isDownloaded) {
			if (bookselflist != null && bookselflist.size() > position && position > -1) {
				BookShelfModel item = bookselflist.get(position);
				item.setDownloaded(isDownloaded);
				bookselflist.set(position, item);
			}
		}

		public void updateItemView(final int index, int download_status,
				int progress) {
			// TODO Auto-generated method stub
			MZLog.d("JD_Reader", "updateItemView-->progress::"+progress);

			if (download_status == DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED) {
				updateItemData(index, true);
			} else {
				updateItemData(index, false);
			}

			if (mlistView != null) {
				int visiblePos = mlistView.getFirstVisiblePosition();
				int offset = index - visiblePos;
				if (offset < 0 )  return;
				View view = mlistView.getChildAt(offset);
				MZLog.d("JD_Reader", "offset::"+offset+",view::"+view);
				if (view != null && getResources() != null) {
					BookShelfModel model = bookselflist.get(index);
					ViewHolder holder = (ViewHolder) view.getTag();

					if (download_status == DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED) {
						MZLog.d("JD_Reader", "this========>>" + index);
						holder.statueButton.setText("阅读");
						holder.statueButton.setTextColor(getResources()
								.getColor(R.color.highlight_color));
						holder.statueButton
								.setBackgroundResource(R.drawable.border_listbtn_red_h24);
						model.setDownload_state(LocalBook.STATE_LOAD_READING);
					} else if (download_status == DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING) {
						holder.statueButton.setText(progress + "%");
						holder.statueButton.setTextColor(getResources()
								.getColor(R.color.r_text_disable));
						holder.statueButton
								.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
					} else if (download_status == DragItemAdapter.DOWNLOAD_STATUS_FAILED) {
						holder.statueButton.setText("下载失败");
						holder.statueButton.setTextColor(getResources()
								.getColor(R.color.r_text_disable));
						holder.statueButton
								.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
					} else if (download_status == DragItemAdapter.DOWNLOAD_STATUS_PAUSED) {
						holder.statueButton.setText("继续");
						holder.statueButton.setTextColor(getResources()
								.getColor(R.color.text_main));
						holder.statueButton
								.setBackgroundResource(R.drawable.border_listbtn_black_h24);
					}
				}
			}
		}

		@Override
		public int getCount() {
			if (bookselflist == null) {
				return 0;
			} else {
				return bookselflist.size();
			}

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
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			final ViewHolder holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.item_free_gifts_booklist_one, parent, false);
				holder = new ViewHolder();
				holder.bookTitle = (TextView) convertView
						.findViewById(R.id.user_book_name);
				holder.bookAuthor = (TextView) convertView
						.findViewById(R.id.user_book_author);
				holder.progresstxt = (TextView) convertView
						.findViewById(R.id.read);
				holder.readTime = (TextView) convertView
						.findViewById(R.id.readtime);

				holder.bookCover = (ImageView) convertView
						.findViewById(R.id.user_book_cover);
				holder.statueButton = (Button) convertView.findViewById(R.id.statueButton);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final BookShelfModel eBook = bookselflist.get(position);
			holder.progresstxt.setVisibility(View.VISIBLE);
			holder.readTime.setVisibility(View.VISIBLE);
			holder.bookTitle.setText(eBook.getBookName());
			String author = eBook.getAuthor();
			if (author == null || TextUtils.isEmpty(author) || author.equals("null")) {
				author = "佚名";
			}
			
			
			holder.bookAuthor.setText(author);
			if (eBook.getBookPercent() == 0) {
				holder.progresstxt.setText("未读");
				Drawable drawable = context.getResources().getDrawable(R.drawable.red_dot);
				holder.progresstxt.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
			}else{
				holder.progresstxt.setText("已读到" + (int)(eBook.getBookPercent()*100)
						+ "%,笔记" + eBook.getNote_num() + "条");
				holder.progresstxt.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
			}
			
			holder.readTime.setText("上次阅读时间"
					+ TimeFormat.formatTime(getResources(),
							(long) eBook.getPercentTime()));
			MZLog.d("quda", "上次阅读时间"
					+ TimeFormat.formatTimeByMiliSecond(getResources(),
							(long) eBook.getPercentTime()));
			updateStatusButton(holder.statueButton,eBook,position);
			String urlPath = eBook.getBookCover();
			if(urlPath!=null&&urlPath.startsWith("http")){
				
				ImageLoader.getInstance().displayImage(urlPath, holder.bookCover,
						GlobalVarable.getCutBookDisplayOptions());
			}else{
				holder.bookCover.setImageBitmap(ImageTool.getImage(urlPath));
			}
			MZLog.d("quda", "serarch img ="+urlPath);
			holder.statueButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					if (eBook == null) return;
					stateBtnClick(position);
				}
			});
			holder.bookCover.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					stateBtnClick(position);
				}
			});
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					stateBtnClick(position);
				}
			});
			return convertView;
		}
	}
	
	/**
	 * 
	 * @Title: stateBtnClick
	 * @Description: Item上按钮点击
	 * @param @param position
	 * @return void
	 * @throws
	 * @date 2015年3月27日 下午7:42:40
	 */
	private void stateBtnClick(int position){
		DownloadedAble downloadedAble = null;
		BookShelfModel eBook = bookselflist.get(position);
		if (!eBook.isDownloaded() && eBook.getDownload_state() != LocalBook.STATE_LOAD_READING) {
			if (eBook.getBookType().equals(BookShelfModel.EBOOK)) {
				downloadedAble = LocalBook.getLocalBookByIndex(eBook
						.getBookid());

			} else if (eBook.getBookType().equals(
					BookShelfModel.DOCUMENT)) {
				downloadedAble = MZBookDatabase.instance
						.getLocalDocument(eBook.getBookid());
				MZLog.d("wangguodong", "点击document");
			} else {
				return;
			}

			if(eBook.getDownload_state() ==  LocalBook.STATE_UNLOAD){
//				JDEBook bookEntity = eBook;
//				OrderEntity orderEntity = OrderEntity
//						.FromJDBooK2OrderEntity(bookEntity);
//				DownloadTool.download(
//						(Activity) BookcaseCloudActivity.this,
//						orderEntity, null, false,
//						LocalBook.SOURCE_BUYED_BOOK, 0, false,new DownloadConfirmListener() {
//							
//							@Override
//							public void onConfirm() {
//							}
//							
//							@Override
//							public void onCancel() {
//							}
//						},false);
				eBook.setDownload_state(LocalBook.STATE_LOADING);
				bookselflist.set(position, eBook);

				Bundle bundle = new Bundle();
				bundle.putInt("index", position);
				bundle.putInt("status",
						DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
				bundle.putInt("progress", 0);
				sendMessage(bundle);
				DownloadHelper.restartDownload(BookShelfSearchActivity.this,
						eBook.getBookType(), downloadedAble);
			}else if (eBook.getDownload_state() ==  LocalBook.STATE_FAILED
					|| eBook.getDownload_state() == LocalBook.STATE_LOAD_FAILED) {
				MZLog.d("JD_Reader", "重新开始下载书籍...");
				eBook.setDownload_state(LocalBook.STATE_LOADING);
				bookselflist.set(position, eBook);

				Bundle bundle = new Bundle();
				bundle.putInt("index", position);
				bundle.putInt("status",
						DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
				bundle.putInt("progress", 0);
				sendMessage(bundle);
				DownloadHelper.restartDownload(BookShelfSearchActivity.this,
						eBook.getBookType(), downloadedAble);
			}else if (eBook.getDownload_state() == LocalBook.STATE_LOAD_PAUSED) {

				MZLog.d("JD_Reader", "继续下载书籍...");
				eBook.setDownload_state(LocalBook.STATE_LOADING);
				bookselflist.set(position, eBook);

				Bundle bundle = new Bundle();
				bundle.putInt("index", position);
				bundle.putInt("status",
						DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
				bundle.putInt("progress", -1);
				sendMessage(bundle);
				DownloadHelper.resumeDownload(BookShelfSearchActivity.this,
						eBook.getBookType(), downloadedAble);
			} else if (eBook.getDownload_state() == LocalBook.STATE_LOADING) {

				MZLog.d("JD_Reader", "暂停下载书籍...");
				eBook.setDownload_state(LocalBook.STATE_LOAD_PAUSED);
				bookselflist.set(position, eBook);

				Bundle bundle = new Bundle();
				bundle.putInt("index", position);
				bundle.putInt("status",
						DragItemAdapter.DOWNLOAD_STATUS_PAUSED);
				bundle.putInt("progress", -1);
				sendMessage(bundle);
				DownloadHelper.stopDownload(BookShelfSearchActivity.this,
						eBook.getBookType(), downloadedAble);
			} else if (eBook.getDownload_state() == LocalBook.STATE_LOADED
					|| eBook.getDownload_state() == LocalBook.STATE_LOAD_READING) {

				eBook.setDownload_state(LocalBook.STATE_LOADED);
				bookselflist.set(position, eBook);
				Bundle bundle = new Bundle();
				bundle.putInt("index", position);
				bundle.putInt("status",
						DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED);
				bundle.putInt("progress", -1);
				sendMessage(bundle);
				
			} else {
				Toast.makeText(BookShelfSearchActivity.this, "无法继续下载，书籍有问题...",
						Toast.LENGTH_LONG).show();
			}
			return;
		}
		
		if (eBook.getBookType().equals(BookShelfModel.EBOOK)) {
			EBook ebook = MZBookDatabase.instance.getEBook(eBook
					.getBookid());
			OpenBookHelper.openEBook(BookShelfSearchActivity.this, ebook.bookId);

		} else if (eBook.getBookType().equals(
				BookShelfModel.DOCUMENT)) {
			// FIXME liqiang
			Document doc = MZBookDatabase.instance
					.getDocument(eBook.getBookid());
			OpenBookHelper.openDocument(BookShelfSearchActivity.this,
					doc.documentId);

		}
		
	}
	
	public void sendMessage(Bundle bundle) {
		Message msg = new Message();
		msg.what = UPDATE_UI_MESSAGE;
		msg.obj = bundle;
		handler.sendMessage(msg);
	}
	
	public void updateStatusButton(Button statueButton, BookShelfModel eBook,int position) {
		if (eBook.getDownload_state() == DownloadedAble.STATE_LOADED) { 
			//updateItemData(position, true);
			statueButton.setText("阅读");
			statueButton.setTextColor(getResources().getColor(
					R.color.highlight_color));
			statueButton
					.setBackgroundResource(R.drawable.border_listbtn_red_h24);
		} else if (eBook.getDownload_state() == DownloadedAble.STATE_LOADING) {
			//updateItemData(position, false);
			int progress = 0;
			if (eBook.getBook_size() > 0)
				progress = (int) (eBook
						.getDownload_progress() * 100 / bookselflist
						.get(position).getBook_size());
			statueButton.setText(progress + "%");
			statueButton.setTextColor(getResources().getColor(
					R.color.r_text_disable));
			statueButton
					.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
		} else if (eBook.getDownload_state() == DownloadedAble.STATE_LOAD_FAILED) {
			//updateItemData(position, false);
			statueButton.setText("下载失败");
			statueButton.setTextColor(getResources().getColor(
					R.color.r_text_disable));
			statueButton
					.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
		} else if (eBook.getDownload_state() == DownloadedAble.STATE_LOAD_PAUSED) {
			//updateItemData(position, false);
			statueButton.setText("继续");
			statueButton.setTextColor(getResources().getColor(
					R.color.text_main));
			statueButton
					.setBackgroundResource(R.drawable.border_listbtn_black_h24);
		}else if (eBook.getDownload_state() == DownloadedAble.STATE_UNLOAD) {
			//updateItemData(position, false);
			statueButton.setText("下载");
			statueButton.setTextColor(getResources().getColor(
					R.color.red_main));
			statueButton
				.setBackgroundResource(R.drawable.border_listbtn_red_h24);
		}
		else {
			statueButton.setText("阅读");
			statueButton.setTextColor(getResources().getColor(
					R.color.highlight_color));
			statueButton
					.setBackgroundResource(R.drawable.border_listbtn_red_h24);
		}
	}

	@Override
	public void onLeftMenuClick() {
		// TODO Auto-generated method stub
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
	// 获得指定type id 的item 的位置
	public int getPositionByTypeAndId(String type, int id) {
		if (bookselflist == null)
			return -1;
		else {
			for (int i = 0; i < bookselflist.size(); i++) {

				BookShelfModel model = bookselflist.get(i);

				if (model.getBookType().equals(type)) {
					if (model.getBookid() == id) {
						return i;
					}
				}
			}
			return -1;
		}
	}

	@Override
	public void refresh(DownloadedAble downloadAble) {
		int index = -1;
		if (downloadAble.getType() == DownloadedAble.TYPE_BOOK) {
			LocalBook localBook = (LocalBook) downloadAble;
			index = getPositionByTypeAndId(BookShelfModel.EBOOK, localBook._id);
			int state = DownloadStateManager.getLocalBookState(localBook);
			int progress = 0;
			if (localBook.size > 0)
				progress = (int) (localBook.progress * 100 / localBook.size);
			MZLog.d("JD_Reader", "refresh-->progress:"+progress);
			Bundle bundle = new Bundle();
			bundle.putInt("index", index);

			if (state == DownloadStateManager.STATE_LOADED) {
				if(localBook!=null){
					SettingUtils.getInstance().putBoolean("Buyed:" + localBook.book_id, false);
					SettingUtils.getInstance().putBoolean("file_error:" + localBook.book_id, false);
				}
				bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED);
				bundle.putInt("progress", -1);
				sendMessage(bundle);

			} else if (state == DownloadStateManager.STATE_LOADING) {
				if (localBook.state == LocalBook.STATE_LOADING
						|| localBook.state == LocalBook.STATE_LOAD_READY) {
					bundle.putInt("status",
							DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
					bundle.putInt("progress", progress);
					sendMessage(bundle);
				}
				if (localBook.state == LocalBook.STATE_LOAD_PAUSED) {

					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_PAUSED);
					bundle.putInt("progress", progress);
					sendMessage(bundle);
				}
			} else {
				bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_FAILED);
				bundle.putInt("progress", 0);
				sendMessage(bundle);
			}

		} else if (downloadAble.getType() == DownloadedAble.TYPE_DOCUMENT) {
			LocalDocument localBook = (LocalDocument) downloadAble;
			index = getPositionByTypeAndId(BookShelfModel.DOCUMENT,
					localBook._id);
			int state = DownloadStateManager.getLocalDocumentState(localBook);

			Bundle bundle = new Bundle();
			bundle.putInt("index", index);

			int progress = 0;
			if (localBook.size > 0)
				progress = (int) (localBook.progress * 100 / localBook.size);

			if (state == DownloadStateManager.STATE_LOADED) {
				bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADED);
				bundle.putInt("progress", -1);
				sendMessage(bundle);

			} else if (state == DownloadStateManager.STATE_LOADING) {
				if (localBook.state == LocalBook.STATE_LOADING
						|| localBook.state == LocalBook.STATE_LOAD_READY) {
					bundle.putInt("status",
							DragItemAdapter.DOWNLOAD_STATUS_DOWNLOADING);
					bundle.putInt("progress", progress);
					sendMessage(bundle);
				}
				if (localBook.state == LocalBook.STATE_LOAD_PAUSED) {
					bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_PAUSED);
					bundle.putInt("progress", progress);
					sendMessage(bundle);
				}
			} else {
				bundle.putInt("status", DragItemAdapter.DOWNLOAD_STATUS_FAILED);
				bundle.putInt("progress", 0);
				sendMessage(bundle);
			}
		}
	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void refreshDownloadCache() {
		// TODO Auto-generated method stub
		
	}

}
