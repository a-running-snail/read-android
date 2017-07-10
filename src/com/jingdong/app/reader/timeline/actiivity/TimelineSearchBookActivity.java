package com.jingdong.app.reader.timeline.actiivity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.android.mzbook.sortview.model.BookShelfModel;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.LackBookSignActivity;
import com.jingdong.app.reader.book.Mention;
import com.jingdong.app.reader.bookstore.search.BookStoreSearchActivity;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.extra.JDBook;
import com.jingdong.app.reader.entity.extra.JDBookDetail;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.SettingUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TimelineSearchBookActivity extends BaseActivityWithTopBar {

	public static final int SELECTED_BOOK = 2100;
	// 最近阅读
	private ListView recentlyReading;
	private RecentlyReadingAdapter recAdapter;

	private boolean noMoreBook = false;
	private boolean inLoadingMore = true;
	private int currentPage = 1;
	private int currentRECPage = 1;
	private int pageCount = 7;

	private EditText serarchBook;
	private ListView searchResult;
	private LinearLayout searchImage;

	// 最近提及
	private TextView recentlyTextView;
	private ListView recentlyMention;
	private List<Mention> mentionList = new ArrayList<Mention>();
	private ScrollView recentlyData;
	private List<JDBookDetail> list = new ArrayList<JDBookDetail>();

	private List<BookShelfModel> recnetreadingModels = new ArrayList<BookShelfModel>();

	private RecentlyMentionAdapter recentlyMentionAdapter = null;
	private RecentlyLocalAdapter recentlyLocalAdapter = null;
	
	private LinearLayout mentionTitle = null;
	private LinearLayout readingTitle = null;
	private LinearLayout empty = null;
	private RelativeLayout mSearchResultContainer;
	private Button lack_button;

	class RecentlyMentionAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mentionList == null ? 0 : mentionList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return mentionList.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		@Override
		public View getView(int arg0, View arg1, ViewGroup arg2) {

			View view = LayoutInflater.from(TimelineSearchBookActivity.this)
					.inflate(R.layout.item_recently_mention, null);

			TextView bookName = (TextView) view.findViewById(R.id.book_name);

			bookName.setText(mentionList.get(arg0).getMentionBookName());

			return view;
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_timeline_search_book);

		recentlyReading = (ListView) findViewById(R.id.recently_reading);
		empty =(LinearLayout) findViewById(R.id.empty);
		
		mSearchResultContainer = (RelativeLayout) findViewById(R.id.book_store_search_result_container);
		
		lack_button = (Button) findViewById(R.id.lackbook_button);
		recentlyReading.setDivider(null);
		recentlyReading.setDividerHeight(0);
		recentlyReading.setSelector(R.drawable.list_longpress_transparent_bg);

		recentlyTextView = (TextView) findViewById(R.id.recently_mention_textview);

		recentlyMentionAdapter = new RecentlyMentionAdapter();
		recentlyMention = (ListView) findViewById(R.id.recently_mention_listview);

		recentlyMention.setAdapter(recentlyMentionAdapter);
		recentlyLocalAdapter = new RecentlyLocalAdapter(
				TimelineSearchBookActivity.this);

		serarchBook = (EditText) findViewById(R.id.search_book_name);
		
		mentionTitle=(LinearLayout) findViewById(R.id.mention_title);
	    readingTitle =(LinearLayout) findViewById(R.id.readinng_title);
		

		mentionList = MZBookDatabase.instance.getAllMention();
		
		if(mentionList==null||mentionList.size()==0)
			mentionTitle.setVisibility(View.GONE);
		else {
			mentionTitle.setVisibility(View.VISIBLE);
		}
		
		
		recentlyMentionAdapter.notifyDataSetChanged();
		setListViewHeightBasedOnChildren(recentlyMention, 0);

		recnetreadingModels = MZBookDatabase.instance
				.listAllRecentReadingBooks(LoginUser.getpin());
		if(recnetreadingModels!=null&&recnetreadingModels.size()>0)
		{
			 List<BookShelfModel> tempBookShelfModels =new ArrayList<BookShelfModel>();; 
			for(int i=0;i<recnetreadingModels.size();i++)
			{
				if(recnetreadingModels.get(i).getBookType().equals(BookShelfModel.EBOOK)||recnetreadingModels.get(i).getDocument_bookId()>0)
				{
					tempBookShelfModels.add(recnetreadingModels.get(i));	
				}
			}
			recnetreadingModels=tempBookShelfModels;
		}
		
		if(recnetreadingModels==null||recnetreadingModels.size()==0){
//			readingTitle.setVisibility(View.GONE);
//		else {
//			readingTitle.setVisibility(View.VISIBLE);
			empty.setVisibility(View.VISIBLE);
			recentlyReading.setVisibility(View.GONE);
			
		}
		else {
			recentlyReading.setVisibility(View.VISIBLE);
			empty.setVisibility(View.GONE);
		}
		
		
		recentlyReading.setAdapter(recentlyLocalAdapter);
		setListViewHeightBasedOnChildren(recentlyReading, 1);

		recentlyData = (ScrollView) findViewById(R.id.recently_data);

		serarchBook = (EditText) findViewById(R.id.search_book_name);
		searchResult = (ListView) findViewById(R.id.search_result);

		searchResult.setDivider(null);
		searchResult.setDividerHeight(0);
		searchResult.setSelector(R.drawable.list_longpress_transparent_bg);

		recAdapter = new RecentlyReadingAdapter(TimelineSearchBookActivity.this);

		searchResult.setAdapter(recAdapter);

		searchImage = (LinearLayout) findViewById(R.id.searchImage);
		
		lack_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(TimelineSearchBookActivity.this, LackBookSignActivity.class);
				intent.putExtra("bookname", serarchBook.getText().toString());
				startActivity(intent);
			}
		});
		
		recentlyMention.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				// 完成最近提到的数据保持（由于只显示最近的5条数据 所以非最新数据 一般不进行数据库更新，提升性能）
				Mention mention = new Mention();
				mention.setMentionBookId(mentionList.get(position).getMentionBookId());
				mention.setMentionBookName(mentionList.get(position).getMentionBookName());
				mention.setMentionAt(System.currentTimeMillis());
				mention.setAuthor(mentionList.get(position).getAuthor());
				mention.setBookCover(mentionList.get(position).getBookCover());
				MZBookDatabase.instance.insertMention(mention);

				// 捎带主要信息
				Intent in = new Intent();
				in.putExtra("book_id", mention.getMentionBookId() + "");
				in.putExtra("book_name", mention.getMentionBookName());
				in.putExtra("book_author", mention.getAuthor());
				in.putExtra("book_cover", mention.getBookCover());

				MZLog.d("wangguodong",
						"携带的bookId为:" + mention.getMentionBookId());
				
				setResult(SELECTED_BOOK, in);
				finish();

			}
		});

		recentlyReading.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(android.widget.AdapterView<?> arg0,
					View arg1, int position, long arg3) {

				Mention mention = new Mention();
				
				if(recnetreadingModels.get(position).getBookType().equals(BookShelfModel.EBOOK))
					mention.setMentionBookId(recnetreadingModels.get(position).getServerid());
				else
					mention.setMentionBookId(recnetreadingModels.get(position).getDocument_bookId());
				mention.setMentionBookName(recnetreadingModels.get(position).getBookName());
				mention.setMentionAt(System.currentTimeMillis());
				mention.setAuthor(recnetreadingModels.get(position).getAuthor());
				mention.setBookCover(recnetreadingModels.get(position).getBookCover());
				MZBookDatabase.instance.insertMention(mention);

				// 捎带主要信息
				Intent in = new Intent();
				in.putExtra("book_id", mention.getMentionBookId() + "");
				in.putExtra("book_name", mention.getMentionBookName());
				in.putExtra("book_author", mention.getAuthor());
				in.putExtra("book_cover", mention.getBookCover());

				MZLog.d("wangguodong",
						"携带的bookId为:" + mention.getMentionBookId());
				
				SettingUtils.getInstance().putString("author:" + mention.getMentionBookId(), mention.getAuthor());
				SettingUtils.getInstance().putString("cover:" + mention.getMentionBookId(), mention.getBookCover());
				
				setResult(SELECTED_BOOK, in);
				finish();
			}
		});

		// recentlyReading.setOnScrollListener(new OnScrollListener() {
		//
		// @Override
		// public void onScrollStateChanged(AbsListView view, int scrollState) {
		//
		// }
		//
		// @Override
		// public void onScroll(AbsListView view, int firstVisibleItem,
		// int visibleItemCount, int totalItemCount) {
		//
		// if (totalItemCount == 0)
		// return;
		// if (firstVisibleItem + visibleItemCount == totalItemCount
		// && !noMoreBook) {
		//
		// if (!inLoadingMore) {
		// inLoadingMore = true;
		//
		// List<BookShelfModel> tempBookShelfModels
		// =MZBookDatabase.instance.listRecentReadingBooks(LoginUser.getpin(),
		// currentRECPage, pageCount);
		//
		// currentRECPage++;
		//
		// if(tempBookShelfModels==null||tempBookShelfModels.size()==0)
		// noMoreBook=true;
		// else {
		// recnetreadingModels.addAll(tempBookShelfModels);
		// recentlyLocalAdapter.notifyDataSetChanged();
		// }
		// }
		//
		// }
		// }
		//
		// });
		//

		searchResult.setOnScrollListener(new OnScrollListener() {

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
						searchBooks(serarchBook.getText().toString());
					}

				}
			}

		});
		
		serarchBook.setOnKeyListener(new View.OnKeyListener() {

			@SuppressWarnings("static-access")
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == event.KEYCODE_ENTER
						&& event.getAction() == KeyEvent.ACTION_DOWN) {
					((InputMethodManager) serarchBook.getContext()
							.getSystemService(Context.INPUT_METHOD_SERVICE))
							.hideSoftInputFromWindow(TimelineSearchBookActivity.this.getCurrentFocus().getWindowToken(),
									InputMethodManager.HIDE_NOT_ALWAYS);
					if (!TextUtils.isEmpty(serarchBook.getText().toString())) {
						list.clear();
						inLoadingMore = false;
						currentPage = 1;
						recAdapter.notifyDataSetChanged();
						searchBooks(serarchBook.getText().toString());
					}
					return true;
				}
				return false;
			}
		});

//		serarchBook.setOnEditorActionListener(new OnEditorActionListener() {
//
//			@Override
//			public boolean onEditorAction(TextView v, int actionId,
//					KeyEvent event) {
//				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//					((InputMethodManager) serarchBook.getContext()
//							.getSystemService(Context.INPUT_METHOD_SERVICE))
//							.hideSoftInputFromWindow(
//									TimelineSearchBookActivity.this
//											.getCurrentFocus().getWindowToken(),
//									InputMethodManager.HIDE_NOT_ALWAYS);
//
//					if (!TextUtils.isEmpty(serarchBook.getText().toString())) {
//
//						list.clear();
//						inLoadingMore = false;
//						currentPage = 1;
//						recAdapter.notifyDataSetChanged();
//						searchBooks(serarchBook.getText().toString());
//					}
//
//					return true;
//				}
//				return false;
//			}
//		});

		serarchBook.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				if (arg1) {
					searchResult.setVisibility(View.VISIBLE);
					searchImage.setVisibility(View.GONE);
					recentlyData.setVisibility(View.GONE);
					mSearchResultContainer.setVisibility(View.GONE);
				}

			}
		});

		searchResult.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				Mention mention = new Mention();
				mention.setMentionBookId(list.get(position).getEbookId());
				mention.setMentionBookName(list.get(position).getName());
				mention.setMentionAt(System.currentTimeMillis());
				mention.setAuthor(list.get(position).getAuthor());
				mention.setBookCover(list.get(position).getImageUrl());
				MZBookDatabase.instance.insertMention(mention);

				// 捎带主要信息
				Intent in = new Intent();
				
				in.putExtra("book_id", mention.getMentionBookId() + "");
				in.putExtra("book_name", mention.getMentionBookName());
				in.putExtra("book_author", mention.getAuthor());
				in.putExtra("book_cover", mention.getBookCover());

				MZLog.d("wangguodong",
						"携带的bookId为:" + mention.getMentionBookId());
				
				SettingUtils.getInstance().putString("author:" + mention.getMentionBookId(), mention.getAuthor());
				SettingUtils.getInstance().putString("cover:" + mention.getMentionBookId(), mention.getBookCover());
				
				setResult(SELECTED_BOOK, in);
				finish();

			}
		});

	}

	public void setListViewHeightBasedOnChildren(ListView listView, int type) {

		BaseAdapter adapter = null;

		switch (type) {
		case 0:
			adapter = (RecentlyMentionAdapter) listView.getAdapter();
			break;
		case 1:
			adapter = (RecentlyLocalAdapter) listView.getAdapter();
			break;

		case 2:
			adapter = (RecentlyReadingAdapter) listView.getAdapter();
			break;

		}

		if (adapter == null) {
			return;
		}
		int totalHeight = 0;
		for (int i = 0; i < adapter.getCount(); i++) {
			View listItem = adapter.getView(i, null, listView);
			if (listItem != null) {
				listItem.setLayoutParams(new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));
				listItem.measure(MeasureSpec.UNSPECIFIED,
						MeasureSpec.UNSPECIFIED);
				totalHeight += listItem.getMeasuredHeight();
			}
		}
		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight
				+ (listView.getDividerHeight() * (adapter.getCount() - 1))
				+ listView.getPaddingTop() + listView.getPaddingBottom();
		listView.setLayoutParams(params);
	}

	class RecentlyReadingAdapter extends BaseAdapter {
		private Context context;

		class ViewHolder {

			TextView bookTitle;
			TextView bookAuthor;
			TextView bookSize;
			Button statueButton;
			ImageView bookCover;
		}

		RecentlyReadingAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return list == null ? 0 : list.size();
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
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			final ViewHolder holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.item_free_gifts_booklist, parent, false);
				holder = new ViewHolder();
				holder.bookTitle = (TextView) convertView
						.findViewById(R.id.user_book_name);
				holder.bookAuthor = (TextView) convertView
						.findViewById(R.id.user_book_author);

				holder.bookCover = (ImageView) convertView
						.findViewById(R.id.user_book_cover);

				holder.bookSize = (TextView) convertView
						.findViewById(R.id.book_size);

				holder.statueButton = (Button) convertView
						.findViewById(R.id.statueButton);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final JDBookDetail eBook = list.get(position);
			holder.bookTitle.setText(eBook.getName());
			holder.bookAuthor.setText("");
			holder.bookSize
					.setText("null".equals(eBook.getAuthor()) ? getString(R.string.author_unknown)
							: eBook.getAuthor());
			//holder.bookSize.setText(eBook.getInfo().replaceAll("^[　 ]*", ""));
			holder.bookSize.setVisibility(View.VISIBLE);
			holder.statueButton.setVisibility(View.GONE);
			ImageLoader.getInstance().displayImage(eBook.getImageUrl(),
					holder.bookCover, GlobalVarable.getCutBookDisplayOptions());
			return convertView;
		}

	}

	class RecentlyLocalAdapter extends BaseAdapter {
		private Context context;

		class ViewHolder {

			TextView bookTitle;
			TextView bookAuthor;
			TextView bookSize;
			ImageView bookCover;
		}

		RecentlyLocalAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return recnetreadingModels == null ? 0 : recnetreadingModels.size();
		}

		@Override
		public Object getItem(int position) {
			return recnetreadingModels.get(position);
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
						R.layout.item_recentreading_booklist, parent, false);
				holder = new ViewHolder();
				holder.bookTitle = (TextView) convertView
						.findViewById(R.id.user_book_name);
				holder.bookAuthor = (TextView) convertView
						.findViewById(R.id.user_book_author);

				holder.bookCover = (ImageView) convertView
						.findViewById(R.id.user_book_cover);

				holder.bookSize = (TextView) convertView
						.findViewById(R.id.book_size);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final BookShelfModel eBook = recnetreadingModels.get(position);
			holder.bookTitle.setText(eBook.getBookName());
			holder.bookAuthor
					.setText("");
			holder.bookSize
			.setText("null".equals(eBook.getAuthor()) ? getString(R.string.author_unknown)
					: eBook.getAuthor());
			// holder.bookSize.setText(eBook.getBook_size().replaceAll("^[　 ]*",
			// ""));
			 holder.bookSize.setVisibility(View.VISIBLE);
			  
			if(isUrl(eBook.getBookCover()))
			{
				ImageLoader.getInstance().displayImage(eBook.getBookCover(),
						holder.bookCover, GlobalVarable.getCutBookDisplayOptions());
			}
			else {
				ImageLoader.getInstance().displayImage("file://" +eBook.getBookCover(),
						holder.bookCover, GlobalVarable.getCutBookDisplayOptions());
			}
		
			return convertView;
		}

	}
	
	public boolean isUrl(String url){
		
		if(TextUtils.isEmpty(url))
			return false;
		
		String regex = "^(https|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]" ;
		Pattern patt = Pattern. compile(regex );
		Matcher matcher = patt.matcher(url);
		boolean isMatch = matcher.matches();
		if (!isMatch) {
		    return false;
		} else {
			 return true;
		}
	}
	

	private JDBook ebook = null;

	private synchronized void searchBooks(final String key) {

		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool
				.getSearchStoreEbookParams(currentPage + "", pageCount + "",
						key,""), new MyAsyncHttpResponseHandler(
				TimelineSearchBookActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				Toast.makeText(TimelineSearchBookActivity.this,
						getString(R.string.network_connect_error),
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onResponse(int statusCode, Header[] headers,
					byte[] responseBody) {
				// TODO Auto-generated method stub

				String result = new String(responseBody);

				ebook = GsonUtils.fromJson(result, JDBook.class);
				mSearchResultContainer.setVisibility(View.GONE);
				searchResult.setVisibility(View.VISIBLE);
				if (ebook != null
						&& ((ebook.getCode().equals("51") || ebook
								.getCode().equals("0")) && ebook
								.getResultCount() == 0)) {
					searchPaperBooks(key);
					return;
				} 
				if (ebook != null) {
					currentPage++;

					if (ebook.getBookList() != null && ebook.getBookList().size() < pageCount) {
						noMoreBook = true;
					} else {
						noMoreBook = false;
					}

					if(list == null){
						list = new ArrayList<JDBookDetail>();
					}
					
					if(null != ebook.bookList) {
						list.addAll(ebook.bookList);
					}

					recAdapter.notifyDataSetChanged();
				//	setListViewHeightBasedOnChildren(searchResult, 2);
					inLoadingMore = false;

				} else if (ebook == null)
					Toast.makeText(TimelineSearchBookActivity.this,
							getString(R.string.network_connect_error),
							Toast.LENGTH_LONG).show();

			}

		});
	}
	
	private synchronized void searchPaperBooks(String key) {

		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool
				.getSearchStoreEbookParams(currentPage + "", pageCount + "",
						key,""), new MyAsyncHttpResponseHandler(
				TimelineSearchBookActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				Toast.makeText(TimelineSearchBookActivity.this,
						getString(R.string.network_connect_error),
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onResponse(int statusCode, Header[] headers,
					byte[] responseBody) {
				// TODO Auto-generated method stub

				String result = new String(responseBody);

				ebook = GsonUtils.fromJson(result, JDBook.class);
				
				if (ebook != null
						&& ((ebook.getCode().equals("51") || ebook.getCode().equals("0")) && (ebook.getResultCount() + list.size()) == 0)) {
					searchResult.setVisibility(View.GONE);
					mSearchResultContainer.setVisibility(View.VISIBLE);
					return;
				}
				
				if (ebook != null) {
					currentPage++;

					if (ebook.getBookList() != null
							&& ebook.getBookList().size() < pageCount) {
						noMoreBook = true;
					} else {
						noMoreBook = false;
					}

					if(null != ebook.bookList) {
						list.addAll(ebook.bookList);
					}

					recAdapter.notifyDataSetChanged();
				//	setListViewHeightBasedOnChildren(searchResult, 2);
					inLoadingMore = false;

				} else if (ebook == null)
					Toast.makeText(TimelineSearchBookActivity.this,
							getString(R.string.network_connect_error),
							Toast.LENGTH_LONG).show();

			}

		});
	}

}
