package com.jingdong.app.reader.activity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.JDBookComments;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListCommentsActivity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.DateUtil;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.jingdong.app.reader.view.SearchTopBarView.TopBarViewListener;
import com.jingdong.app.reader.view.xlistview.XListView;
import com.jingdong.app.reader.view.xlistview.XListView.IXListViewListener;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BookCommentsListActivity extends BaseActivityWithTopBar implements
		IXListViewListener,TopBarViewListener{

	private XListView listView = null;
	private BookCommentsAdapter adapter = null;
	private int currentPage = 1;
	private int currentPaperBookPage = 1;

	private boolean noMore = false;
	private long bookid = 0;
	private long paperbookid = 0;
	private String booktype = "ebook";
	private String currentBookType = "ebook";
	private String currentTime=null;
	private String bookName;
	private String bookAuthor;
	private String bookCover;
	private int ebookTotalCount;

	private List<JDBookComments> list = new ArrayList<JDBookComments>();

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.activity_book_comments_list);
		bookid = getIntent().getLongExtra("bookid", 0);
		paperbookid = getIntent().getLongExtra("paperbookid", 0);
		booktype = getIntent().getStringExtra("booktype");
		currentBookType = booktype;

		if (bookid != 0) {
			if (booktype.equals("paperBook"))
				requestData("paperBook", paperbookid, currentPage);
			else {
				requestData("ebook", bookid, currentPage);
			}
		} else {
			Toast.makeText(this, "抱歉,您要看的书评不存在哦!", Toast.LENGTH_LONG).show();
			finish();
		}
		listView = (XListView) findViewById(R.id.listview);
		listView.setPullLoadEnable(true);
		listView.setPullRefreshEnable(false);
		listView.setXListViewListener(this);

		adapter = new BookCommentsAdapter();
		listView.setAdapter(adapter);
		
		boolean isShowBookCommentBtn = getIntent().getBooleanExtra("isShowBookCommentBtn", false);
		bookName = getIntent().getStringExtra("bookName");
		bookAuthor = getIntent().getStringExtra("book_author");
		bookCover = getIntent().getStringExtra("book_cover");
		if(isShowBookCommentBtn){
			if(bookid != 0 && booktype.equals("ebook"))
				getTopBarView().setRightMenuOneVisiable(true, "写书评", R.color.red_sub, false);
		}

	}

	@Override
	public void onRefresh() {

	}

	@Override
	public void onLoadMore() {
		if (!noMore) {

			if (currentBookType.equals("ebook")) {
				currentPage++;
				requestData("ebook", bookid, currentPage);
			} else {

				if (booktype.equals("ebook")) {
					requestData("paperBook", paperbookid, currentPaperBookPage);
					currentPaperBookPage++;
				} else {
					currentPaperBookPage++;
					requestData("paperBook", paperbookid, currentPaperBookPage);
				}
			}

		}

		else {
			onLoadComplete();
		}

	}

	private void onLoadComplete() {
		listView.stopRefresh();
		listView.stopLoadMore();
	}

	public void requestData(String type, long bookid, int page) {

		RequestParams params = null;

		if (type.equals("ebook"))
			params = RequestParamsPool.getBookCommentsParams("ebook", bookid,
					page + "");
		else {
			params = RequestParamsPool.getBookCommentsParams("paperBook",
					paperbookid, page + "");
		}

		WebRequestHelper.get(URLText.JD_BASE_URL, params, true,
				new MyAsyncHttpResponseHandler(BookCommentsListActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Toast.makeText(BookCommentsListActivity.this,
								"网络连接失败了,请检查网络!", Toast.LENGTH_LONG).show();
						onLoadComplete();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);
						MZLog.d("wangguodong", result);
						List<JDBookComments> temp = new ArrayList<JDBookComments>();
						try {
							JSONObject object = new JSONObject(result);

							int code = object.optInt("code");

							if (code != 0) {
								onLoadComplete();
								noMore = true;
								listView.setPullLoadEnable(false);
								return;
							}
							currentTime = object.getString("currentTime");//服务器当前时间
							JSONObject reviews = object
									.optJSONObject("reviews");

							JSONArray array = reviews.optJSONArray("list");
							
							int totalCount = reviews.optInt("totalCount");
							
							if (array != null && array.length() > 0) {

								for (int i = 0; i < array.length(); i++) {
									JDBookComments comments = GsonUtils
											.fromJson(array.getString(i),
													JDBookComments.class);
									temp.add(comments);
								}
							}
							list.addAll(temp);
							if(booktype.equals("paperBook") || currentBookType.equals("paperBook")){
								if(list !=null && ebookTotalCount + totalCount == list.size()){
									noMore = true;
									listView.setPullLoadEnable(false);
								}
							}else{
								ebookTotalCount = totalCount;
								if(list !=null && totalCount == list.size() ){
									currentBookType = "paperBook";
									if (!noMore) {
										if (paperbookid >0) {
											Toast.makeText(
													BookCommentsListActivity.this,
													"电子书评论加载完毕，上拉将加载对应纸书评论!",
													Toast.LENGTH_LONG).show();
										}
									}
								}
							}
							
//							if (temp.size() < 10) {
//								if (booktype.equals("paperBook")
//										|| currentBookType.equals("paperBook")) {
//									noMore = true;
//									listView.setPullLoadEnable(false);
//								} else {
//
//									currentBookType = "paperBook";
//									if (!noMore) {
//										if (paperbookid >0) {
//											Toast.makeText(
//													BookCommentsListActivity.this,
//													"电子书评论加载完毕，上拉将加载对应纸书评论!",
//													Toast.LENGTH_LONG).show();
//										}
//									}
//								}
//							} else {
//								listView.setPullLoadEnable(true);
//							}

						} catch (Exception e) {
							e.printStackTrace();
						}
						adapter.notifyDataSetChanged();
						onLoadComplete();
					}
				});

	}

	class BookCommentsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list.size();
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
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null)
				convertView = LayoutInflater
						.from(BookCommentsListActivity.this).inflate(
								R.layout.bookstore_bookinfo_comments_style,
								null);

			TextView title = ViewHolder.get(convertView, R.id.title);
			TextView content = ViewHolder.get(convertView, R.id.content);
			RatingBar mbar = ViewHolder.get(convertView, R.id.rating);
			RoundNetworkImageView avatar = ViewHolder.get(convertView, R.id.thumb_nail);
			TextView time=ViewHolder.get(convertView, R.id.time);

			final JDBookComments bookComments = list.get(position);

			if(null!=bookComments.userHead && !bookComments.userHead.equals(""))
				ImageLoader.getInstance().displayImage("http://"+bookComments.userHead, avatar);
			if(null != currentTime && null != bookComments.creationTime){
				try {
					time.setText(DateUtil.daytimeBetweenText(currentTime,bookComments.creationTime));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			title.setText(bookComments.nickname);
			content.setText(Html.fromHtml(bookComments.contents));
			mbar.setRating((float) bookComments.score);
			//点击头像转到个人主页
			avatar.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(BookCommentsListActivity.this, UserActivity.class);
					intent.putExtra(UserActivity.NAME, bookComments.pin);
					startActivity(intent);
				}
			});
			
			//点击名称转到个人主页
			title.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(BookCommentsListActivity.this, UserActivity.class);
					intent.putExtra(UserActivity.NAME, bookComments.pin);
					startActivity(intent);
				}
			});

			return convertView;
		}

	}
	
	@Override
	public void onRightMenuOneClick() {
//		Intent intent = new Intent(BookCommentsListActivity.this, BookCommentNewuiActivity.class);
//		intent.putExtra(BookCommentNewuiActivity.BookIdKey, bookid);
//		intent.putExtra("bookname", "《" + bookName + "》");
//		startActivity(intent);
		
		
		Intent it2 = new Intent(BookCommentsListActivity.this, TimelineBookListCommentsActivity.class);
		it2.putExtra("type", "direct_comment");
		it2.putExtra("book_id", bookid+"");
		it2.putExtra("book_name", bookName);
		it2.putExtra("book_author", bookAuthor);
		it2.putExtra("book_cover", bookCover);
		startActivity(it2);
		
	}


	@Override
	public void onRightMenu_leftClick() {
	}

	@Override
	public void onRightMenu_rightClick() {
	}
}
