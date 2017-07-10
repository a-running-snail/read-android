package com.jingdong.app.reader.bookshelf;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.client.DownloadService;
import com.jingdong.app.reader.client.DownloadStateManager;
import com.jingdong.app.reader.client.DownloadThreadQueue.RefreshAble;
import com.jingdong.app.reader.client.DownloadTool;
import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.entity.OrderEntity;
import com.jingdong.app.reader.entity.extra.BorrowBook;
import com.jingdong.app.reader.entity.extra.JDBookDetail;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.onlinereading.OlineDesUtils;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.util.TimeFormat;
import com.nostra13.universalimageloader.core.ImageLoader;

public class StoreBorrowedBookFragment extends CommonFragment implements
		RefreshAble {

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
	private ListView listView = null;
	private RelativeLayout relativeLayout = null;
	private ImageView icon;
	private TextView textView;
	private Button lackbook_button;
	private View rootView;
	private Context context;
	private boolean haveInited =false;
	private boolean isPrepared = false;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			if (msg.what == UPDATE_UI_MESSAGE) {
				if (bookListAdapter != null) {
					bookListAdapter.updateItemView(msg.arg1, msg.arg2);// arg1 // position // ,arg2 // progress
				}
			}
		};
	};

	public void initData() {
		currentPage = 1;
		noMoreBook = false;
		inLoadingMore = true;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.activity_borrow, null);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		context = getActivity();
		listView = (ListView) rootView.findViewById(R.id.list);
		relativeLayout = (RelativeLayout)rootView. findViewById(R.id.search_result_container);
		icon = (ImageView) rootView. findViewById(R.id.icon);
		textView = (TextView) rootView. findViewById(R.id.text);
		lackbook_button = (Button) rootView. findViewById(R.id.lackbook_button);
		lackbook_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
					Intent intent = new Intent(v.getContext(),LauncherActivity.class);
					intent.putExtra("lx", 0);
					startActivity(intent);				
			}
		});

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
						&& !noMoreBook) {

					if (!inLoadingMore) {
						inLoadingMore = true;
						listBorrowedEbook();
					}
				}
			}
		});
		bookListAdapter = new BookListAdapter(context);
		listView.setAdapter(bookListAdapter);
		
		isPrepared = true;
		if (!getUserVisibleHint()) {
			return;
		}
		DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
		listBorrowedEbook();
		haveInited = true;
	}
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if(!haveInited && isVisibleToUser && isPrepared){
			haveInited =true;
			listBorrowedEbook();
		}
		if(isVisibleToUser)
			DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
		
	}
	
//	@Override
//	public void onHiddenChanged(boolean hidden) {
//		super.onHiddenChanged(hidden);
//		// true表示该fragment被隐藏了
//		if (hidden) {
//		} else {// false，表示该fragment正在显示
//			DownloadService.setRefreshAbler(this, DownloadedAble.TYPE_BOOK);
//		}
//	}

	@Override
	public void onResume() {
		super.onResume();
		StatService.onPageStart(context, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_jieyue));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		StatService.onPageEnd(context, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_jieyue));
	}

	private static class EBookItemHolder {
		JDBookDetail ebook;
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
			TextView bookSize;
			TextView borrow_lasttime;
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
					holder.statue_button.setText("阅读");
					holder.statue_button.setTextColor(getResources().getColor(
							R.color.highlight_color));
					holder.statue_button
							.setBackgroundResource(R.drawable.border_listbtn_red_h24);
				}
			}

		}

		public int getIndexByEbookId(long bookid) {
			if (eBookItemList != null) {
				for (int i = 0; i < eBookItemList.size(); i++) {
					if (eBookItemList.get(i).ebook.getEbookId() == bookid) {
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			final ViewHolder holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.borrow_booklist, parent, false);
				holder = new ViewHolder();
				holder.bookTitle = (TextView) convertView
						.findViewById(R.id.user_book_name);
				holder.bookAuthor = (TextView) convertView
						.findViewById(R.id.user_book_author);
				holder.statue_button = (Button) convertView
						.findViewById(R.id.statueButton);

				holder.bookCover = (ImageView) convertView
						.findViewById(R.id.user_book_cover);

				holder.bookSize = (TextView) convertView
						.findViewById(R.id.book_size);

				holder.borrow_lasttime = (TextView) convertView
						.findViewById(R.id.borrow_lasttime);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final EBookItemHolder item = eBookItemList.get(position);
			final JDBookDetail eBook = item.ebook;
			holder.bookTitle.setText(eBook.getName());
			holder.bookAuthor
					.setText("null".equals(eBook.getAuthor()) ? getString(R.string.author_unknown)
							: eBook.getAuthor());
			holder.bookSize.setText(eBook.getFileSize() + "MB");

			String endtime = eBook.getUserBorrowEndTime();
			String starttime = eBook.getCurrentTime();
			long endDate = TimeFormat.formatStringTime(endtime);
			long startDate = TimeFormat.formatStringTime(starttime);

			MZLog.d("cj", eBook.getName() + ",starttime=" + starttime
					+ ",endtime=" + endtime);
			long time = startDate - endDate;
			if (time >= 0) {
				holder.borrow_lasttime.setText("到期");
				holder.borrow_lasttime.setTextColor(getResources().getColor(
						R.color.red_main));
			} else {
				holder.borrow_lasttime.setTextColor(getResources()
						.getColor(R.color.text_sub));
				int days =(int)( Math.ceil((double)(-time) / (1000 * 60 * 60 * 24)));
				if (TimeFormat.DateCompare(endDate, startDate)) {
					holder.borrow_lasttime.setText("还可借阅" + days + "天");
				} else {
					int hour =(int)(Math.ceil(((double)-time) / (1000 * 60 * 60)));
					holder.borrow_lasttime.setText("还可借阅" + hour + "小时");
				}
			}
			holder.statue_button.setText("下载");
			holder.statue_button.setTextColor(getResources().getColor(
					R.color.highlight_color));
			holder.statue_button
					.setBackgroundResource(R.drawable.border_listbtn_red_h24);
			if (eBook.getBorrowStatus() == 1)
				holder.statue_button.setVisibility(View.VISIBLE);
			else {
				holder.borrow_lasttime.setTextColor(getResources().getColor(
						R.color.red_main));
				holder.borrow_lasttime.setText("到期");
				holder.statue_button.setVisibility(View.GONE);
			}
			holder.bookSize.setVisibility(View.VISIBLE);

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					EBookItemHolder item = eBookItemList.get(position);
					JDBookDetail ebook = item.ebook;
					Intent intent2 = new Intent(context,BookInfoNewUIActivity.class);
					intent2.putExtra("bookid", ebook.getEbookId());
					startActivity(intent2);
				}
			});

			holder.statue_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (item.existInLocal) {
						MZLog.d("wangguodong", "xxxx333333");
						OpenBookHelper.openEBook((Activity)context,eBook.getEbookId());
					} else if (item.paused) {
						MZLog.d("wangguodong", "xxxx4444444");
						Toast.makeText(context, "继续下载",Toast.LENGTH_SHORT).show();
						MZLog.d("wangguodong", "畅读：继续下载");

						LocalBook localBook = LocalBook.getLocalBook(
								eBook.getEbookId(), LoginUser.getpin());

						if (localBook == null)
							return;
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
						MZBookDatabase.instance.updateOtherEbookState(
								LoginUser.getpin(), localBook.book_id,
								localBook.source);
						localBook.start((Activity)context);
					} else if (item.inWaitingQueue) {
						holder.statue_button.setText("继续");
						holder.statue_button.setTextColor(getResources()
								.getColor(R.color.text_main));
						holder.statue_button
								.setBackgroundResource(R.drawable.border_listbtn_black_h24);
						updateItemView(position, false, true, false, false,
								PROGRESS_FAILED);
						Toast.makeText(context, "暂停下载",
								Toast.LENGTH_SHORT).show();
						MZLog.d("wangguodong", "借阅：暂停下载");

						LocalBook localBook = LocalBook.getLocalBook(
								eBook.getEbookId(), LoginUser.getpin());

						if (localBook == null)
							return;

						DownloadService.stop(localBook);
					} else if (item.failed) {
						MZLog.d("wangguodong", "借阅：重新开始下载");
						holder.statue_button.setText("等待");
						holder.statue_button.setTextColor(getResources()
								.getColor(R.color.text_color));
						holder.statue_button
								.setBackgroundResource(R.drawable.border_listbtn_grey_h24);
						updateItemView(position, false, false, true, false, 0);

						LocalBook localBook = LocalBook.getLocalBook(
								eBook.getEbookId(), LoginUser.getpin());

						if (localBook == null)
							return;
						localBook.progress = 0;
						localBook.state = LocalBook.STATE_LOAD_PAUSED;
						localBook.save();// 修改数据库中的状态，重新下载，zhangmurui
						MZBookDatabase.instance.updateOtherEbookState(
								LoginUser.getpin(), localBook.book_id,
								localBook.source);
						localBook.start((Activity)context); // 下载失败重新开始下载
					} else {
						// 开始下载
						MZLog.d("wangguodong", " 点击下载....");
						// 加入等待队列
						holder.statue_button.setText("等待");
						holder.statue_button.setTextColor(getResources()
								.getColor(R.color.text_color));
						holder.statue_button
								.setBackgroundResource(R.drawable.border_listbtn_grey_h24);

						updateItemData(position, false, false, false, true, -1);

						LocalBook localBook = LocalBook.getLocalBook(
								eBook.getEbookId(), LoginUser.getpin());
						if (localBook != null) {
							int state = DownloadStateManager
									.getLocalBookState(localBook);
							if (state == STATE_LOADED) {
								if (!LocalBook.SOURCE_BUYED_BOOK
										.equals(localBook.source)) {
									if (LocalBook.SOURCE_TRYREAD_BOOK
											.equals(localBook.source)) {
										IOUtil.deleteFile(new File(
												localBook.dir));
										localBook.progress = 0;
										localBook.state = LocalBook.STATE_LOAD_PAUSED;
										localBook.source = LocalBook.SOURCE_BORROWED_BOOK;
										localBook.save();
										MZBookDatabase.instance
												.updateOtherEbookState(
														LoginUser.getpin(),
														localBook.book_id,
														localBook.source);
										localBook
												.start((Activity)context);
									} else {
										localBook.source = LocalBook.SOURCE_BORROWED_BOOK;
										localBook.borrowEndTime = OlineDesUtils.encrypt(eBook
												.getUserBorrowEndTime());
										localBook.save();
										OpenBookHelper.openEBook(
												(Activity)context,
												eBook.getEbookId());
									}

								} else
									OpenBookHelper.openEBook(
											(Activity)context,
											eBook.getEbookId());

							} else if (state == STATE_LOADING) {

								// 不能点击

							} else {
								localBook.progress = 0;
								localBook.state = LocalBook.STATE_LOAD_PAUSED;
								if (!LocalBook.SOURCE_BUYED_BOOK
										.equals(localBook.source)) {
									localBook.source = LocalBook.SOURCE_BORROWED_BOOK;
								}
								localBook.save();
								MZBookDatabase.instance.updateOtherEbookState(
										LoginUser.getpin(), localBook.book_id,
										localBook.source);
								localBook.start((Activity)context);
							}

						} else {
							borrowBook(eBook);
						}
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

			String urlPath = eBook.getImageUrl();

			ImageLoader.getInstance().displayImage(urlPath, holder.bookCover,
					GlobalVarable.getCutBookDisplayOptions());

			return convertView;
		}

	}

	private void borrowBook(JDBookDetail onlineBookEntity) {
		OrderEntity orderEntity = new OrderEntity();
		orderEntity.name = onlineBookEntity.getName();
		orderEntity.bookId = onlineBookEntity.getEbookId();
		// orderEntity.bookType = LocalBook.TYPE_EBOOK;
		orderEntity.orderId = onlineBookEntity.getEbookId();
		orderEntity.formatName = onlineBookEntity.getFormat();
		orderEntity.author = onlineBookEntity.getAuthor();
		orderEntity.book_size = onlineBookEntity.getFileSize() + "";
		orderEntity.orderStatus = 16;
		orderEntity.price = onlineBookEntity.getJdPrice();
		orderEntity.picUrl = onlineBookEntity.getImageUrl();
		orderEntity.bigPicUrl = onlineBookEntity.getLargeImageUrl();
		orderEntity.borrowEndTime = OlineDesUtils.encrypt(onlineBookEntity
				.getUserBorrowEndTime());

		DownloadTool.download((Activity)context,
				orderEntity, null, false, LocalBook.SOURCE_BORROWED_BOOK, 0,
				true, null, false);
	}

	private void listBorrowedEbook() {

		if (!NetWorkUtils.isNetworkConnected(context)) {
			Toast.makeText(context,context.
					getString(R.string.network_connect_error),
					Toast.LENGTH_SHORT).show();
			return;
		}

		final List<LocalBook> allLocalBooks = LocalBook.getLocalBookList(null,
				null);
		WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool
				.getBorrowedEbookParams(currentPage + "", perPageCount + ""),
				true,
				new MyAsyncHttpResponseHandler(context) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(context,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						String result = new String(responseBody);
						BorrowBook ebook = GsonUtils.fromJson(result,
								BorrowBook.class);

						if (ebook != null && ebook.code == 0) {
							if (ebook.resultList.size() == 0 && currentPage == 1) {
								listView.setVisibility(View.GONE);
								relativeLayout.setVisibility(View.VISIBLE);
								icon.setBackgroundResource(R.drawable.icon_empty);
								textView.setText("您还没借过书哦，去书城挑选借书吧");
								lackbook_button.setText("去书城");
							}
							currentPage++;

							if (ebook.resultList != null
									&& ebook.totalPage < currentPage)
								noMoreBook = true;
							else {
								noMoreBook = false;
							}

							List<EBookItemHolder> all = new ArrayList<EBookItemHolder>();
							for (JDBookDetail book : ebook.resultList) {
								all.add(checkBookState(allLocalBooks, book));
							}
							eBookItemList.addAll(all);
							
							bookListAdapter.notifyDataSetChanged();
						} else if (ebook == null)
							Toast.makeText(context,
									getString(R.string.network_connect_error),
									Toast.LENGTH_SHORT).show();
						inLoadingMore = false;
					}

				});

	}

	public EBookItemHolder checkBookState(List<LocalBook> list,
			JDBookDetail entity) {

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
			if (localBook.book_id == entity.getEbookId()) {

				int state = DownloadStateManager.getLocalBookState(localBook);
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

	@Override
	public void refresh(DownloadedAble DownloadAble) {
		final LocalBook localBook = (LocalBook) DownloadAble;
		if (localBook == null) {
			return;
		}

		int position = bookListAdapter.getIndexByEbookId(localBook.book_id);
		int progress = (int) (100 * (localBook.progress / (localBook.size * 1.0)));
		int state = DownloadStateManager.getLocalBookState(localBook);
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

			MZLog.d("wangguodong", "畅读:正在下载，进度:" + progress * 100 / 360.0 + "%"
					+ "下载文件id=" + DownloadAble.getId());
			if (localBook.state == LocalBook.STATE_LOADING
					|| localBook.state == LocalBook.STATE_LOAD_READY) {
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

}
