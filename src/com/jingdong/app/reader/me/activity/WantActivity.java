package com.jingdong.app.reader.me.activity;


import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.WantBook;
import com.jingdong.app.reader.entity.extra.WantBookList;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;
import com.nostra13.universalimageloader.core.ImageLoader;

public class WantActivity extends BaseActivityWithTopBar{

	protected static final int SHOW_EMPTY_VIEW = 0x01;
	protected static final int SHOW_RESULT_VIEW = 0x02;
	private ListView mListView;
	private static int perPageCount = 10;

	private int currentSearchPage = 1;
	private int perSearchCount = 10;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private String pin;
	private WantBook wantBook;
	private List<WantBookList> wantBookLists = new ArrayList<WantBookList>();
	private BookListAdapter bookListAdapter;

//	private Button emptyView=null;
	private String user_id;
//	private RelativeLayout empty_view;

	private Button mGoToBookStoreBtn=null;
	private RelativeLayout mEmptyRl;
	private ProgressBar mPb;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.want);
		
		pin = LoginUser.getpin();

		Intent intent = getIntent();
		user_id = intent.getStringExtra("user_id");
		mListView = (ListView) findViewById(R.id.mlistview);
//		emptyView=(Button)findViewById(R.id.empty);
//		empty_view = (RelativeLayout) findViewById(R.id.empty_view);


		mGoToBookStoreBtn=(Button)findViewById(R.id.like_goto_bookstore_btn);
		mEmptyRl = (RelativeLayout)findViewById(R.id.like_empty_promot_ll);
		mPb = (ProgressBar) findViewById(R.id.like_pb);

		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if (totalItemCount == 0)
					return;

				if (firstVisibleItem + visibleItemCount == totalItemCount
						&& !noMoreBookOnSearch) {
					if (!inLoadingMoreOnSearch) {
						inLoadingMoreOnSearch = true;
						getMyWant();
					}
				}
			}

		});
		bookListAdapter = new BookListAdapter(WantActivity.this);
		mListView.setAdapter(bookListAdapter);

//		mListView.setEmptyView(mEmptyRl);
		mGoToBookStoreBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent=new Intent(arg0.getContext(), LauncherActivity.class);
				intent.putExtra("TAB_INDEX", 0);
				startActivity(intent);
			}
		});
	}
	
	private Handler mHandler = new Handler(){
		@Override
		public void dispatchMessage(Message msg) {
			super.dispatchMessage(msg);
			switch (msg.what) {
			case SHOW_EMPTY_VIEW:
				mEmptyRl.setVisibility(View.VISIBLE);
				mListView.setVisibility(View.GONE);
				break;
			case SHOW_RESULT_VIEW:
				mEmptyRl.setVisibility(View.GONE);
				mListView.setVisibility(View.VISIBLE);
				break;

			default:
				break;
			}
		}
	};
	
	protected void onResume() {
		super.onResume();
		if (wantBookLists != null) {
			wantBookLists.clear();
		}
		initField();
		getMyWant();
	};
	

	
	public void initField() {
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}
	
	private void getMyWant(){
		WebRequestHelper.get(URLText.WANT_URL, RequestParamsPool
				.getWantParams(pin,currentSearchPage+"",perSearchCount+"",user_id),
				new MyAsyncHttpResponseHandler(WantActivity.this) {
					@Override
					public void onStart() {
						super.onStart();
						mPb.setVisibility(View.VISIBLE);
					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						mPb.setVisibility(View.GONE);
						Toast.makeText(WantActivity.this,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
						
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// TODO Auto-generated method stub
						mPb.setVisibility(View.GONE);
						String result = new String(responseBody);

						Log.d("cj", "result=======>>" + result);
						wantBook = GsonUtils.fromJson(result,
								WantBook.class);

						if (wantBook != null) {
							
							if ( null == wantBook.getBooks() ) {
								mEmptyRl.setVisibility(View.VISIBLE);
								mListView.setVisibility(View.GONE);
								return;
							}

							if ( wantBook.getBooks().size() == 0 && currentSearchPage == 1) {
								mEmptyRl.setVisibility(View.VISIBLE);
								mListView.setVisibility(View.GONE);
//								mHandler.sendEmptyMessage(SHOW_EMPTY_VIEW);
								return;
							}
							
//							mHandler.sendEmptyMessage(SHOW_RESULT_VIEW);

							currentSearchPage++;

							if (wantBook.getBooks()!= null
									&& wantBook.getBooks().size() < perPageCount) {
								noMoreBookOnSearch = true;
							} else {
								noMoreBookOnSearch = false;
							}

							List<WantBookList> all = new ArrayList<WantBookList>();
							for (int i = 0; i < wantBook.getBooks().size(); i++) {
								WantBookList booklist = wantBook.getBooks().get(i);
								all.add(booklist);
							}
							wantBookLists.addAll(all);
							bookListAdapter.notifyDataSetChanged();
						}
						else
							Toast.makeText(WantActivity.this,
									getString(R.string.network_connect_error), Toast.LENGTH_LONG).show();
						inLoadingMoreOnSearch = false;
					}
				});
	}
	
	
	private class BookListAdapter extends BaseAdapter {

		private Context context;

		class ViewHolder {

			TextView bookTitle;
			TextView bookAuthor;
			TextView bookinfo;
			ImageView bookCover;
		}

		BookListAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			if ( wantBookLists== null) {
				return 0;
			} else {
				return wantBookLists.size();
			}

		}

		@Override
		public Object getItem(int position) {
			return wantBookLists.get(position);
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
						R.layout.wantlist, parent, false);
				holder = new ViewHolder();
				holder.bookTitle = (TextView) convertView
						.findViewById(R.id.user_book_name);
				holder.bookAuthor = (TextView) convertView
						.findViewById(R.id.user_book_author);
				holder.bookinfo = (TextView) convertView
						.findViewById(R.id.bookinfo);
				holder.bookCover = (ImageView) convertView
						.findViewById(R.id.user_book_cover);
				View view=convertView.findViewById(R.id.statueButton);
				if(view!=null){
					view.setVisibility(View.GONE);
				}
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.bookTitle.setText(wantBookLists.get(position).getTitle());
			holder.bookAuthor.setText((wantBookLists.get(position).getAuthor() == null || wantBookLists.get(position).getAuthor().equals("null")) ? "佚名" : wantBookLists.get(position).getAuthor());
			holder.bookinfo.setText(wantBookLists.get(position).getTitle());
			String imgurl = wantBookLists.get(position).getImgUrl();
			ImageLoader.getInstance().displayImage(imgurl, holder.bookCover,
					GlobalVarable.getCutBookDisplayOptions(false));
			
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(WantActivity.this,
							BookInfoNewUIActivity.class);
					intent.putExtra("bookid", wantBookLists.get(position).getBookId());
					startActivity(intent);
				}
			});
			return convertView;
		}
	}

}
