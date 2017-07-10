package com.jingdong.app.reader.me.fragment;

import java.lang.ref.WeakReference;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.me.activity.ReadingDataActivity;
import com.jingdong.app.reader.me.adapter.BookListAdapter;
import com.jingdong.app.reader.me.model.BookListModel;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.timeline.fragment.TimelineFragment;
import com.jingdong.app.reader.timeline.model.TimeLineModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.UiStaticMethod;

public class BookListFragment extends CommonFragment implements Observer,
		OnScrollListener, OnItemClickListener {

	private static class MyHandler extends Handler {
		private WeakReference<BookListFragment> reference;

		public MyHandler(BookListFragment fragment) {
			reference = new WeakReference<BookListFragment>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			BookListFragment fragment = reference.get();
			if (fragment != null) {
				switch (msg.what) {
				case SEARCH:
					fragment.loading.setVisibility(View.GONE);
					fragment.screenLoading.setVisibility(View.GONE);
					if (msg.arg1 == TimeLineModel.SUCCESS_INT) {
						fragment.model.refreshData();
						fragment.adapter.notifyDataSetInvalidated();
						if (msg.arg2 == TimeLineModel.SUCCESS_INT)
							fragment.listView.setVisibility(View.VISIBLE);
					} else
						Toast.makeText(fragment.getActivity(),
								R.string.loading_fail, Toast.LENGTH_SHORT)
								.show();
					break;
				case LOAD_INIT:
					if (msg.arg1 == TimeLineModel.SUCCESS_INT) {
						if (msg.arg2 == TimeLineModel.SUCCESS_INT) {
							fragment.listView.setVisibility(View.VISIBLE);
							fragment.screenLoading.setVisibility(View.GONE);
							fragment.model.refreshData();
							fragment.adapter.notifyDataSetChanged();
						} else{
							fragment.relativeLayout.setVisibility(View.GONE);
							fragment.empty_view.setVisibility(View.VISIBLE);
						}
					} else{
						fragment.relativeLayout.setVisibility(View.GONE);
						fragment.empty_view.setVisibility(View.VISIBLE);
						Toast.makeText(fragment.getActivity(),
								R.string.loading_fail, Toast.LENGTH_SHORT)
								.show();
					}
					break;
				case LOAD_MORE:
					fragment.loading.setVisibility(View.GONE);
					fragment.screenLoading.setVisibility(View.GONE);
					if (msg.arg1 == TimeLineModel.SUCCESS_INT) {
						if (msg.arg2 == TimeLineModel.SUCCESS_INT) {
							fragment.model.refreshData();
							fragment.adapter.notifyDataSetChanged();
						} else if (fragment.getActivity() != null)
							Toast.makeText(fragment.getActivity(),
									R.string.user_no_more, Toast.LENGTH_SHORT)
									.show();
					} else
						Toast.makeText(fragment.getActivity(),
								R.string.loading_fail, Toast.LENGTH_SHORT)
								.show();
					break;
				case EMPTY_INPUT:
					fragment.model.clear();
					fragment.loading.setVisibility(View.GONE);
					fragment.screenLoading.setVisibility(View.GONE);
					fragment.adapter.notifyDataSetInvalidated();
					break;
				case CHANGE_BOOK_VISIBILITY:
					if (msg.arg1 == BookListModel.SUCCESS_INT)
						if (msg.arg2 == BookListModel.SUCCESS_INT) {
							Book book = (Book) msg.obj;
							book.setHidden(!book.isHidden());
						}
					fragment.adapter.notifyDataSetChanged();
					break;
				default:
					break;
				}
			}
		}
	}

	private class Task implements Runnable {
		private int type;
		private boolean hide;
		private String query;
		private Book book;
		private String url;

		private Task(int type) {
			this(type, null);
		}

		private Task(int type, String query) {
			this.type = type;
			this.query = query;
		}

		private Task(int type, boolean hide, Book book) {
			this.type = type;
			this.hide = hide;
			this.book = book;
		}

		@Override
		public void run() {
			if (bookListType == BookListModel.NOTES_BOOKS) {
				url = URLText.Books_Notes;
			} else if (bookListType == BookListModel.IMPORT_BOOKS) {
				url = URLText.Import_book;
			} else if (bookListType == BookListModel.READ_BOOKS) {
				url = URLText.WANT_URL;
			}
			switch (type) {
			case LOAD_INIT:
				page = 1;
				model.loadBooks(url, userId, type, page + "", count + "");
				break;
			case LOAD_MORE:
				// if (query == null)
				page++;
				model.loadBooks(url, userId, type, page + "", count + "");
				// else
				// model.searchMoreBook(type, query);
				break;
			case SEARCH:
				model.searchBook(type, query);
				break;
			case CHANGE_BOOK_VISIBILITY:
				model.changeBookVisibility(hide, book);
				break;
			}
		}
	}

	public static final String JUMP_READING_DATA = "reading_data";
	public static final String BOOKLIST_TYPE = "type";
	public static final String QUERY = "query";
	public static final int LOAD_INIT = 1;
	public static final int LOAD_MORE = 2;
	public static final int SEARCH = 3;
	public static final int EMPTY_INPUT = 4;
	public static final int CHANGE_BOOK_VISIBILITY = 5;
	private Future<?> moreLoad;
	private String userName;
	private String currentQuery;
	private boolean jumpToReadingData;
	private boolean searchable;
	private int bookListType;
	private String userId;
	private Handler handler;
	private BookListModel model;
	private BookListAdapter adapter;
	private ListView listView;
	private View loading;
	private SearchView searchView;
	private int lastItemIndex;
	private ScheduledExecutorService executor;
	private String query = "";
	private int page = 0;
	private int count = 10;
	private RelativeLayout empty_view;
	private ImageView icon;
	private TextView text;
	private Button emptybutton;
	private View screenLoading;
	private RelativeLayout relativeLayout;

	public BookListFragment() {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		fragmentTag = "BookListFragment";
		View view = inflater.inflate(R.layout.fragment_book_list,
				null);
		listView = (ListView) view.findViewById(R.id.me_book_list);
		empty_view = (RelativeLayout) view.findViewById(R.id.empty_view);
		emptybutton = (Button) view.findViewById(R.id.empty);
		icon = (ImageView) view.findViewById(R.id.icon);
		text = (TextView) view.findViewById(R.id.text);
		loading = inflater.inflate(R.layout.view_loading, null,false);
		screenLoading = view.findViewById(R.id.screen_loading);
		relativeLayout = (RelativeLayout) view
				.findViewById(R.id.relativeLayout);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		initBundleField();
		initField();
		if (bookListType == BookListModel.IMPORT_BOOKS) {
			icon.setBackgroundResource(R.drawable.icon_empty);
			text.setText("暂无外部导入的书籍");
			emptybutton.setVisibility(View.GONE);
		} else if (bookListType == BookListModel.NOTES_BOOKS) {
			icon.setBackgroundResource(R.drawable.icon_emptynote);
			text.setText("暂无笔记\n阅读时长按以选中文字可添加笔记");
			emptybutton.setVisibility(View.VISIBLE);
		} else if (bookListType == BookListModel.READ_BOOKS) {
			icon.setBackgroundResource(R.drawable.icon_empty);
			text.setText("暂无阅读记录");
			emptybutton.setVisibility(View.GONE);
		}

		model.addObserver(this);
		
		listView.addFooterView(UiStaticMethod.getFooterParent(getActivity(),
				loading));
		screenLoading.setVisibility(View.VISIBLE);
		listView.setDivider(null);
		listView.setAdapter(adapter);
		listView.setOnScrollListener(this);
		listView.setOnItemClickListener(this);
		registerForContextMenu(listView);
		if (!searchable) {
			executor.execute(new Task(LOAD_INIT));
			loading.setVisibility(View.GONE);
		}
		query = getArguments().getString(QUERY);
		// onQueryTextChange(query);

		emptybutton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(arg0.getContext(),
						LauncherActivity.class);
				intent.putExtra("TAB_INDEX", 2);
				startActivity(intent);
			}
		});
	}

	@Override
	public void onDestroyView() {
		model.deleteObserver(this);
		handler.removeCallbacksAndMessages(null);
		super.onDestroyView();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		if (userId.equals(LoginUser.getpin())
				&& bookListType == BookListModel.IMPORT_BOOKS) {
			MenuInflater inflater = new MenuInflater(getActivity());
			inflater.inflate(R.menu.hide, menu);
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			WrapperListAdapter myAdapter = (WrapperListAdapter) listView
					.getAdapter();
			Book book = (Book) myAdapter.getItem(info.position);
			if (book.isHidden())
				menu.removeItem(R.id.hide);
			else
				menu.removeItem(R.id.unhide);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo menuInfo;
		ContextMenuInfo contextMenuInfo = item.getMenuInfo();
		WrapperListAdapter myAdapter = (WrapperListAdapter) listView
				.getAdapter();
		switch (item.getItemId()) {
		case R.id.hide:
			menuInfo = (AdapterContextMenuInfo) contextMenuInfo;
			executor.execute(new Task(CHANGE_BOOK_VISIBILITY, true,
					(Book) myAdapter.getItem(menuInfo.position)));
			return true;
		case R.id.unhide:
			menuInfo = (AdapterContextMenuInfo) contextMenuInfo;
			executor.execute(new Task(CHANGE_BOOK_VISIBILITY, false,
					(Book) myAdapter.getItem(menuInfo.position)));
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position < adapter.getCount()) {
			Book bookDetail = (Book) parent.getAdapter().getItem(position);
			if (!bookDetail.isDocument()) {
				Intent intent = new Intent();
				if (jumpToReadingData) {
					String url = UiStaticMethod.getReadingDataUrl(userId,
							bookDetail.getBookId());
					intent.setClass(getActivity(), ReadingDataActivity.class);
					intent.putExtra(ReadingDataActivity.READING_DATA_URL, url);
					intent.putExtra("user_id", userId);
					intent.putExtra("book_id", bookDetail.getBookId() + "");
					intent.putExtra(UserFragment.USER_NAME, userName);
					startActivity(intent);
				} else {

					Intent intent2 = new Intent(getActivity(),
							BookInfoNewUIActivity.class);
					intent2.putExtra("bookid", bookDetail.getBookId());
					startActivity(intent2);
				}
			} else {
				Intent intent = new Intent();
				if (jumpToReadingData) {
					String url = URLText.getDocumentNotesUrl.replace(
							":document_id", bookDetail.getBookId() + "")
							.replace(":user_id", userId + "");
					intent.setClass(getActivity(), ReadingDataActivity.class);
					intent.putExtra(
							ReadingDataActivity.READING_DATA_URL,
							url + "?auth_token="
									+ LocalUserSetting.getToken(getActivity()));
					intent.putExtra(UserFragment.USER_NAME, userName);
					intent.putExtra("is_document", true);
					intent.putExtra("documentId", bookDetail.getBookId());
					intent.putExtra("user_id", userId);

					startActivity(intent);
				} 
			}
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (searchView != null)
			searchView.clearFocus();
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& lastItemIndex == adapter.getCount() - 1) {
			if (moreLoad == null || moreLoad.isDone()) {
				loading.setVisibility(View.VISIBLE);
				if (searchable)
					moreLoad = executor
							.submit(new Task(LOAD_MORE, currentQuery));
				else
					moreLoad = executor.submit(new Task(LOAD_MORE));
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		lastItemIndex = firstVisibleItem + visibleItemCount - 1 - 1;
	}

	@Override
	public void update(Observable observable, Object data) {
		handler.sendMessage((Message) data);
	}

	/**
	 * 
	 */
	private void initField() {
		executor = NotificationService.getExecutorService();
		handler = new MyHandler(this);
		model = new BookListModel(getActivity(), bookListType);
		if (bookListType == BookListModel.WISH_BOOKS)
			adapter = new BookListAdapter(getActivity(), bookListType, model,
					userId, false, false, false, false, false);
		else if (bookListType == BookListModel.IMPORT_BOOKS) {
			// boolean showBorrowButton = (!userId .equals(LoginUser.getpin()));
			adapter = new BookListAdapter(getActivity(), bookListType, model,
					userId, false, false, false, false, true);
		} else if (bookListType == BookListModel.SEARCH_BOOKS
				|| bookListType == BookListModel.SEARCH_EBOOKS) {
			adapter = new BookListAdapter(getActivity(), bookListType, model,
					userId, true, false, true, false, false);
		} else if (bookListType == BookListModel.NOTES_BOOKS) {
			adapter = new BookListAdapter(getActivity(), bookListType, model,
					userId, false, false, false, false, true);
		} else if (bookListType == BookListModel.READ_BOOKS)
			adapter = new BookListAdapter(getActivity(), bookListType, model,
					userId, true, false, false, false, false);
	}

	/**
	 * 
	 */
	private void initBundleField() {
		bookListType = getArguments().getInt(BOOKLIST_TYPE, 1);
		searchable = getArguments().getBoolean(TimelineFragment.SEARCHABLE);
		jumpToReadingData = getArguments().getBoolean(JUMP_READING_DATA);
		userId = getArguments().getString(UserFragment.USER_ID);
		userName = getArguments().getString(UserFragment.USER_NAME);
	}

}
