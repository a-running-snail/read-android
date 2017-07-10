package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.R;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FollowedUserReadingActivity extends MZReadCommonActivity {

	private View loading;
	private List<Book> bookList;
	private List<UserInfo> userList;
	private FollowedUserReadingAdapter adapter;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_followed_user_reading);
		getActionBar().setTitle(R.string.followed_user_reading);

		loading = getLayoutInflater().inflate(R.layout.view_loading, null, false);
		ViewGroup rootView = (ViewGroup) findViewById(R.id.followed_users_reading_layout);
		rootView.addView(loading);

		bookList = new ArrayList<Book>();
		userList = new ArrayList<UserInfo>();

		adapter = new FollowedUserReadingAdapter();
		ListView listview = (ListView) findViewById(R.id.followed_users_list);

		DisplayMetrics metric = this.getResources().getDisplayMetrics();
		float scale = metric.density;
		View header = new View(this);
		header.setMinimumHeight((int) (6 * scale));
		listview.addHeaderView(header);
		listview.setAdapter(adapter);

		loading.setVisibility(View.VISIBLE);
		String authToken = LocalUserSetting.getToken(this);
		String url = URLText.followedUserReading + "?auth_token=" + authToken;
		RequestUserReadingTask request = new RequestUserReadingTask();
		request.execute(url);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = new MenuInflater(this);
		inflater.inflate(R.menu.finish, menu);
		MenuItem Item = menu.findItem(R.id.finish);
		View actionView = Item.getActionView();
		TextView view = (TextView) actionView.findViewById(R.id.finish_action);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
		return super.onCreateOptionsMenu(menu);
	}

	public class RequestUserReadingTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {
			String result = WebRequest.getWebDataWithContext(FollowedUserReadingActivity.this, params[0]);
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			String errorString = "";
			if (TextUtils.isEmpty(result)) {
				errorString = getString(R.string.unknown_error);
			}
			try {
				JSONArray array = new JSONArray(result);
				for (int i = 0; i < array.length(); i++) {
					JSONObject object = (JSONObject) array.get(i);
					JSONObject book = (JSONObject) object.get("book");
					JSONObject user = (JSONObject) object.get("user");
					bookList.add(Book.fromJSON(book));
					userList.add(UserInfo.fromJSON(user));
				}
				if (array.length() > 0) {
					loading.setVisibility(View.GONE);
					adapter.notifyDataSetChanged();
				} else {
					errorString = getString(R.string.followed_user_book_not_exist);
				}
			} catch (JSONException e) {
				errorString = getString(R.string.unknown_error);
				e.printStackTrace();
			}

			if (!TextUtils.isEmpty(errorString)) {
				loading.setVisibility(View.GONE);
				Toast toast = Toast.makeText(FollowedUserReadingActivity.this, errorString, Toast.LENGTH_LONG);
				toast.show();
			}

		}
	}

	private class FollowedUserReadingAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return userList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = View.inflate(FollowedUserReadingActivity.this, R.layout.item_user_readings, null);
				convertView.findViewById(R.id.user_info_layout).setOnClickListener(new UserInfoClickListener());
				convertView.findViewById(R.id.reading_book_layout).setOnClickListener(new ReadingBookClickListener());
			}
			Book book = bookList.get(position);
			UserInfo user = userList.get(position);
			ImageView userThumbNail = (ImageView) convertView.findViewById(R.id.followed_user_avatar);
			UiStaticMethod.loadThumbnail(FollowedUserReadingActivity.this, userThumbNail, user.getThumbNail(), user.isFemale());
			TextView userName = (TextView) convertView.findViewById(R.id.followed_user_name);
			userName.setText(user.getName());
			String urlPath = book.cover + GlobalVarable.bookCoverSize;
			ImageView bookCover = (ImageView) convertView.findViewById(R.id.reading_book_cover);
		     ImageLoader.getInstance().displayImage(urlPath, bookCover, GlobalVarable.getDefaultBookDisplayOptions());
			TextView bookName = (TextView) convertView.findViewById(R.id.reading_book_name);
			TextView author = (TextView) convertView.findViewById(R.id.reading_book_author);
			bookName.setText(book.getTitle());
			author.setText(book.getAuthorName());

			TextView inBookStore = (TextView) convertView.findViewById(R.id.in_bookstore);
			if (book.isEbook()) {
				inBookStore.setVisibility(View.VISIBLE);
				double price = book.getWebPrice();
				inBookStore.setText(price != 0 ? R.string.can_buy : R.string.free);
			} else if (book.isBorrowable()) {
				inBookStore.setVisibility(View.VISIBLE);
				inBookStore.setText(R.string.can_borrow);
			} else {
				inBookStore.setVisibility(View.GONE);
			}

			convertView.findViewById(R.id.user_info_layout).setTag(Long.valueOf(user.getId()));
			convertView.findViewById(R.id.reading_book_layout).setTag(R.id.reading_book_layout,
					Integer.valueOf(book.bookId));
			convertView.findViewById(R.id.reading_book_layout).setTag(R.id.reading_book_name, book.getTitle());
			return convertView;
		}

	}

	private class UserInfoClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			Intent intent = new Intent(FollowedUserReadingActivity.this, UserActivity.class);
			intent.putExtra(UserFragment.USER_ID, ((Long) v.getTag()).longValue());
			startActivity(intent);
		}

	}

	private class ReadingBookClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
//			Intent intent = new Intent(FollowedUserReadingActivity.this, BookInfoActivity.class);
//			intent.putExtra(BookInfoActivity.BookIdKey, ((Integer) v.getTag(R.id.reading_book_layout)).intValue());
//			intent.putExtra(BookInfoActivity.BookNameKey, (String) v.getTag(R.id.reading_book_name));
//			startActivity(intent);
			
			Intent intent2 = new Intent(FollowedUserReadingActivity.this,
					BookInfoNewUIActivity.class);
			intent2.putExtra("bookid",  ((Integer) v.getTag(R.id.reading_book_layout)).intValue());
			startActivity(intent2);
		}

	}
}
