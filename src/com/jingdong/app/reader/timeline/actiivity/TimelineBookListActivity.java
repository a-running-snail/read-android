package com.jingdong.app.reader.timeline.actiivity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.timeline.model.core.RenderBody;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.nostra13.universalimageloader.core.ImageLoader;

public class TimelineBookListActivity extends BaseActivityWithTopBar {

	public static final String BOOK_LIST_TITLE = "booklist_title";
	public static final String BOOK_LIST_URL = "booklist";
	public static final String BOOK_LIST_CREATE_AT = "book_list_add_at";
	public static final String[] type = { "FORWARD", "INVITIED", "COMMENT",
			"BOOKCOMMENT", "BOOKLIST" };

	private TextView bookListName;
	private TextView bookListAuthorTime;
	private TextView forward;
	private FrameLayout invited;
	private FrameLayout recomments;
	private FrameLayout commonArea;
	private LinearLayout divider;
	
	private String userName;
	private String bookListTitleString;
	private String booklistUrlString;
	private String booklistAddTime;
	private List<Entity> list = new ArrayList<Entity>();
	private ListView lisView;
	private ListBooksAdapter adapter;

	private int list_id = -1;
	private String guid = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_book_list);
		Intent it = getIntent();
		userName = it.getStringExtra(UserFragment.USER_NAME);
		bookListTitleString = it.getStringExtra(BOOK_LIST_TITLE);
		booklistUrlString = it.getStringExtra(BOOK_LIST_URL);
		booklistAddTime = it.getStringExtra(BOOK_LIST_CREATE_AT);
		init();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (list != null)
			list.removeAll(list);
		RequestBookListTask task = new RequestBookListTask();
		task.execute();
	}

	private void init() {

		bookListName = (TextView) findViewById(R.id.book_list_name);
		bookListAuthorTime = (TextView) findViewById(R.id.book_list_author_time);
		lisView = (ListView) findViewById(R.id.recom_books);
		invited = (FrameLayout) findViewById(R.id.invited);
		recomments = (FrameLayout) findViewById(R.id.recomments);
		commonArea = (FrameLayout) findViewById(R.id.commonArea);
		forward = (TextView) findViewById(R.id.forward);
		adapter = new ListBooksAdapter();
		divider=(LinearLayout) findViewById(R.id.divider);
		
		getTopBarView().setTitle(bookListTitleString);
		
		bookListName.setText(bookListTitleString);
		bookListAuthorTime.setText(userName
				+ " "
				+ TimeFormat.formatTime(getResources(),
						Long.parseLong(booklistAddTime)));
		list.clear();

		invited.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent intent = new Intent(TimelineBookListActivity.this,
						TimelineBookListCommentsActivity.class);
				intent.putExtra("type", type[1]);
				intent.putExtra("guid", guid);
				intent.putExtra("book_list_id", list_id);
				startActivity(intent);

			}
		});
		recomments.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TimelineBookListActivity.this,
						TimelineBookListCommentsActivity.class);
				intent.putExtra("type", type[2]);
				intent.putExtra("guid", guid);
				intent.putExtra("book_list_id", list_id);
				startActivity(intent);

			}
		});
		forward.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(TimelineBookListActivity.this,
						TimelineBookListCommentsActivity.class);
				intent.putExtra("type", type[0]);
				intent.putExtra("guid", guid);
				intent.putExtra("book_list_id", list_id);
				startActivity(intent);
			}
		});

	}

	class RequestBookListTask extends AsyncTask<Void, Void, Boolean> {

		boolean isprivate=false;
		@Override
		protected void onPostExecute(Boolean result) {

			if (result) {
				lisView.setAdapter(adapter);
			}
			if (isprivate&&userName!=null) {
				if (userName.equals(LocalUserSetting
						.getUserName(TimelineBookListActivity.this))) {
					invited.setVisibility(View.GONE);
					divider.setVisibility(View.GONE);
				} else {
					commonArea.setVisibility(View.GONE);
				}
			}
			super.onPostExecute(result);
		}

		@Override
		protected Boolean doInBackground(Void... params) {

			String urlText = URLBuilder.addParameter(URLText.baseUrl
					+ booklistUrlString + ".json?auth_token="
					+ LocalUserSetting.getToken(TimelineBookListActivity.this),
					null);
			String result = WebRequest.getWebDataWithContext(
					TimelineBookListActivity.this, urlText);
			try {
				JSONObject object = new JSONObject(result);

				list_id = object.getInt("id");

				isprivate = object.optBoolean("is_private");


				JSONArray array = object.getJSONArray("entities");

				for (int i = 0; i < array.length(); i++) {
					Entity entity = new Entity();
					entity.parseJson(array.getJSONObject(i), false);
					list.add(entity);
				}
				JSONObject object2 = object.getJSONObject("top_entity");
				guid = object2.getString("guid");

			} catch (JSONException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
	}

	class ListBooksAdapter extends BaseAdapter {

		@Override
		public int getCount() {

			return null == list ? 0 : list.size();
		}

		@Override
		public Object getItem(int arg0) {

			return list.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {

			return arg0;
		}

		@Override
		public View getView(final int arg0, View covertview, ViewGroup arg2) {

			View view = LayoutInflater.from(TimelineBookListActivity.this)
					.inflate(R.layout.item_book_list, null);

			ImageView imageView = (ImageView) view
					.findViewById(R.id.book_list_image);
			final TextView likeView = (TextView) view
					.findViewById(R.id.like_count);

			TextView listAuthor = (TextView) view
					.findViewById(R.id.book_list_author);
			TextView listTime = (TextView) view
					.findViewById(R.id.book_list_time);

			RatingBar bar = (RatingBar) view.findViewById(R.id.rating);

			TextView listName = (TextView) view
					.findViewById(R.id.book_list_name);

			TextView listComment = (TextView) view
					.findViewById(R.id.book_list_comment);

			// 设置数据

			final Entity entity = list.get(arg0);

			final Book book = entity.getBook();

			RenderBody body = entity.getRenderBody();

			final UserInfo user = entity.getUser();

			final boolean[] isRecomended = { entity.isViewerRecommended() };

			float rating = (float) body.getRating();
			if (!Double.isNaN(rating)) {
				bar.setVisibility(View.VISIBLE);
				bar.setRating(rating);
			}

			listAuthor.setText(user.getName());

			listTime.setText(TimeFormat.formatTime(getResources(),
					entity.getTimeStamp()));

			listName.setText(book.getTitle());

			likeView.setText(entity.getRecommendsCount() + "");

			listComment.setText(UiStaticMethod.formatListItem(body.getContent()));

			  ImageLoader.getInstance().displayImage(book.cover + GlobalVarable.BOOK_COVER_SIZE_1X,imageView, GlobalVarable.getDefaultBookDisplayOptions());
              
			if (isRecomended[0]) {
				Drawable img = getResources().getDrawable(
						R.drawable.toolbar_like_hl);
				img.setBounds(0, 0, img.getMinimumWidth(),
						img.getMinimumHeight());
				likeView.setCompoundDrawables(img, null, null, null); // 设置左图标
			}

			listName.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

//					Intent intent = new Intent(TimelineBookListActivity.this,
//							BookInfoActivity.class);
//					intent.putExtra(BookInfoActivity.BookIdKey,
//							((Long) book.getBookId()).intValue());
//					intent.putExtra(BookInfoActivity.PurchaseTimeKey, 0);
//					intent.putExtra(BookInfoActivity.BookNameKey,
//							book.getTitle());
//					startActivity(intent);
					
					Intent intent2 = new Intent(TimelineBookListActivity.this,
							BookInfoNewUIActivity.class);
					intent2.putExtra("bookid",  ((Long) book.getBookId()).intValue());
					startActivity(intent2);
				}
			});
			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
//					Intent intent = new Intent(TimelineBookListActivity.this,
//							BookInfoActivity.class);
//					intent.putExtra(BookInfoActivity.BookIdKey,
//							((Long) book.getBookId()).intValue());
//					intent.putExtra(BookInfoActivity.PurchaseTimeKey, 0);
//					intent.putExtra(BookInfoActivity.BookNameKey,
//							book.getTitle());
//					startActivity(intent);
					
					Intent intent2 = new Intent(TimelineBookListActivity.this,
							BookInfoNewUIActivity.class);
					intent2.putExtra("bookid",  ((Long) book.getBookId()).intValue());
					startActivity(intent2);
				}
			});

			likeView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View ar) {

					if (user.getName()
							.equals(LocalUserSetting
									.getUserName(TimelineBookListActivity.this))) {
						return;
					}

					if (!isRecomended[0]) {
						int likeCount = Integer.valueOf((likeView.getText()
								.toString()));
						int count = ++likeCount;
						likeView.setText(count + "");
						Drawable img = getResources().getDrawable(
								R.drawable.toolbar_like_hl);
						img.setBounds(0, 0, img.getMinimumWidth(),
								img.getMinimumHeight());
						likeView.setCompoundDrawables(img, null, null, null); // 设置左图标
						// 发送赞请求
						RequestRecommends recommends = new RequestRecommends();
						recommends.execute(entity.getGuid());
						isRecomended[0] = true;
						entity.setViewerRecommended(true);
						entity.setRecommendsCount(count);

					}

					else {
						int likeCount = Integer.valueOf((likeView.getText()
								.toString()));
						int count = --likeCount;
						likeView.setText(count + "");

						Drawable img = getResources().getDrawable(
								R.drawable.toolbar_like);
						img.setBounds(0, 0, img.getMinimumWidth(),
								img.getMinimumHeight());
						likeView.setCompoundDrawables(img, null, null, null); // 设置左图标
						// 取消赞请求
						RequestCanceledRecommends canceledRecommends = new RequestCanceledRecommends();

						canceledRecommends.execute(entity.getGuid());
						isRecomended[0] = false;
						entity.setViewerRecommended(false);
						entity.setRecommendsCount(count);
					}

					if (list != null)
						list.set(arg0, entity);

				}
			});
			listComment.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					Intent intent = new Intent(TimelineBookListActivity.this,
							TimelineTweetActivity.class);
					intent.putExtra(TimelineTweetActivity.TWEET_GUID,
							entity.getGuid());
					startActivity(intent);
				}
			});
			listAuthor.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					Intent intent = new Intent(TimelineBookListActivity.this,
							TimelineTweetActivity.class);
					intent.putExtra(TimelineTweetActivity.TWEET_GUID,
							entity.getGuid());
					startActivity(intent);
				}
			});
			listTime.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {

					Intent intent = new Intent(TimelineBookListActivity.this,
							TimelineTweetActivity.class);
					intent.putExtra(TimelineTweetActivity.TWEET_GUID,
							entity.getGuid());
					startActivity(intent);
				}
			});

			return view;
		}

	}

	class RequestRecommends extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {

			String postData = "entity_guid=" + params[0];
			String url = URLText.recommandUrl + "?auth_token="
					+ LocalUserSetting.getToken(TimelineBookListActivity.this);
			String result = WebRequest.postWebDataWithContext(
					TimelineBookListActivity.this, url, postData);
			MZLog.d("wangguodong", result);
			try {
				JSONObject object = new JSONObject(result);
				return object.optBoolean("success", false);

			} catch (Exception e) {

			}
			return null;

		}

	}

	class RequestCanceledRecommends extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {

			String postData = "entity_guid=" + params[0];
			String url = URLText.unrecommandUrl + "?auth_token="
					+ LocalUserSetting.getToken(TimelineBookListActivity.this);
			String result = WebRequest.postWebDataWithContext(
					TimelineBookListActivity.this, url, postData);
			MZLog.d("wangguodong", result);
			try {
				JSONObject object = new JSONObject(result);
				return object.optBoolean("success", false);

			} catch (Exception e) {

			}
			return null;
		}
	}

}
