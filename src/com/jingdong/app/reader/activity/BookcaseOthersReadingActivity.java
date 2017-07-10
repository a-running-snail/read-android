package com.jingdong.app.reader.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.jingdong.app.reader.book.EBook;
import com.jingdong.app.reader.book.SerializableBook;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.me.activity.UserActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.service.PurchaseBookService;
import com.jingdong.app.reader.service.download.DownloadService;
import com.jingdong.app.reader.service.download.ExtraDetail;
import com.jingdong.app.reader.service.download.TaskInfo;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.BorrowHelper;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.OpenBookHelper;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.CustomPieProgress;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.loopj.android.http.RequestParams;
import com.nostra13.universalimageloader.core.ImageLoader;

public class BookcaseOthersReadingActivity extends BaseActivityWithTopBar {

	private ListView listview;
	private BookAdapter adapter = new BookAdapter();

	private ArrayList<EBookItemHolder> booklist = new ArrayList<EBookItemHolder>();
	private List<UserInfo> userList = new ArrayList<UserInfo>();

	private class EBookItemHolder {
		SerializableBook book;
		boolean existInLocal;
		boolean hasUpdate;
		boolean borrow;
		boolean isDocument;
		boolean inWaitingQueue;
		int progress;
		int edition;
		int entityid;
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_others_reading);

		listview = (ListView) findViewById(R.id.listview);
//		listview.setDivider(null);
//		listview.setDividerHeight(0);
		listview.setAdapter(adapter);

		booklist.clear();
		userList.clear();
		getData();

	}

	public void getData() {
		WebRequestHelper.get(URLText.getOthersReadingUrlNew, RequestParamsPool
				.getOtherReadingParams(), true, new MyAsyncHttpResponseHandler(
				BookcaseOthersReadingActivity.this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				super.onFailure(arg0, arg1, arg2, arg3);
				MZLog.d("wangguodong", "=============d=dd=d=d=d=");
			}

			@Override
			public void onResponse(int statusCode, Header[] headers,
					byte[] responseBody) {

				String result = new String(responseBody);

				MZLog.d("wangguodong", result);
//
//				List<EBook> localEBookList = MZBookDatabase.instance
//						.listLocalEBook(LocalUserSetting
//								.getUserId(BookcaseOthersReadingActivity.this));

				try {
					JSONArray array = new JSONArray(result);
					for (int i = 0; i < array.length(); i++) {
						JSONObject object = (JSONObject) array.get(i);
						JSONObject book = (JSONObject) object.get("book");
						JSONObject user = (JSONObject) object.get("user");

						EBookItemHolder holder = new EBookItemHolder();
						holder.book = SerializableBook.fromJSONNew(book);
						holder.existInLocal = false;
						holder.hasUpdate = false;
						holder.progress = -1;

						
//						for (EBook localEbook : localEBookList) {
//							if (localEbook.ebookId == holder.book.ebookId) {
//								holder.existInLocal = true;
//								holder.edition = localEbook.edition;
//								holder.entityid = localEbook.entityId;
//							}
//						}
//
//						if (holder.book.borrowable
//								&& null != holder.book.getSimpleDocument()) {
//							holder.borrow = true;
//						}
//
//						if (null != holder.book.getSimpleDocument()) {
//							holder.isDocument = true;
//						}

						booklist.add(holder);
						userList.add(UserInfo.fromJSON(user));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				adapter.notifyDataSetChanged();

			}
		});
	}

	class BookAdapter extends BaseAdapter {

		class ViewHolder {

			ImageView bookcover;
			TextView bookTitle;
			TextView authorName;
			RoundNetworkImageView userAvatar;
			TextView username;
			CustomPieProgress progress;
			ImageView actionView;
			FrameLayout iconAreaFrameLayout;

		}

		@Override
		public int getCount() {
			return userList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(
						BookcaseOthersReadingActivity.this).inflate(
						R.layout.item_others_reading_booklist, null);
				holder = new ViewHolder();
				holder.bookcover = (ImageView) convertView
						.findViewById(R.id.user_book_cover);
				holder.bookTitle = (TextView) convertView
						.findViewById(R.id.user_book_name);
				holder.authorName = (TextView) convertView
						.findViewById(R.id.user_book_author);
				holder.actionView = (ImageView) convertView
						.findViewById(R.id.action_button);
				holder.userAvatar = (RoundNetworkImageView) convertView
						.findViewById(R.id.user_avatar);
				holder.username = (TextView) convertView
						.findViewById(R.id.user_name);
				holder.progress = (CustomPieProgress) convertView
						.findViewById(R.id.pie_progress);
				holder.iconAreaFrameLayout = (FrameLayout) convertView
						.findViewById(R.id.icon_area);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (booklist != null && booklist.size() > position) {
				final EBookItemHolder item = booklist.get(position);
				final SerializableBook book = item.book;

				holder.bookTitle.setText(book.getTitle());
				String authorName = book.getAuthorName();
				if (TextUtils.isEmpty(authorName)) {
					authorName = "佚名";
				}
				holder.authorName.setText(authorName);
				holder.username.setText(userList.get(position).getName());

				holder.progress.setDefaultImageResource(R.drawable.transparent);
				holder.progress.setVisibility(View.GONE);
				holder.actionView.setVisibility(View.VISIBLE);
				final int indexPosition = position;

				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						Intent intent2 = new Intent(
								BookcaseOthersReadingActivity.this,
								BookInfoNewUIActivity.class);
						intent2.putExtra("bookid", book.getBookId());
						startActivity(intent2);
					}
				});

				holder.actionView.setVisibility(View.GONE);

				holder.userAvatar.setImageResource(R.drawable.avata_male);
				holder.userAvatar.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(
								BookcaseOthersReadingActivity.this,
								UserActivity.class);
						intent.putExtra(UserActivity.USER_ID,
								userList.get(indexPosition).getId());
						startActivity(intent);

					}
				});
				if (!UiStaticMethod.isEmpty(userList.get(indexPosition)
						.getAvatar()))
					ImageLoader
							.getInstance()
							.displayImage(
									userList.get(indexPosition).getAvatar(),
									holder.userAvatar,
									GlobalVarable
											.getDefaultAvatarDisplayOptions(false));

				String urlPath = book.cover;
				holder.bookcover.setImageResource(R.drawable.bg_default_cover);

				ImageLoader.getInstance().displayImage(urlPath,
						holder.bookcover,
						GlobalVarable.getCutBookDisplayOptions(false));

			}
			return convertView;
		}

	}

	public String getTargetJson(EBookItemHolder holder, int edtion) {
		try {

			JSONObject entite = new JSONObject();
			entite.put("id", holder.book.getEntityIdWithEdition(edtion));
			entite.put("edition", edtion);
			JSONArray entites = new JSONArray();
			entites.put(0, entite);

			JSONObject ebook = new JSONObject();
			ebook.put("id", holder.book.ebookId);
			ebook.put("price", 0.0);
			ebook.put("entities", entites);

			JSONObject object = new JSONObject();
			object.put("id", holder.book.bookId);
			object.put("cover", holder.book.cover);
			object.put("name", holder.book.title);
			object.put("author_name", holder.book.authorName);
			object.put("ebook", ebook);
			return object.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return "{}";
		}

	}

	public SerializableBook getTargetBook(String json) {
		try {
			JSONObject object = new JSONObject(json);
			SerializableBook bookInfo = SerializableBook.fromJSON(object);
			if (bookInfo.ebookId == 0) {
				return null;
			}
			return bookInfo;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_near_read));
	}
	
	@Override
	public void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookshelf_near_read));
	}
	
}
