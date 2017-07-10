package com.jingdong.app.reader.timeline.selected.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.common.MZReadCommonActivity;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.timeline.selected.model.AttachedEntityModel;
import com.jingdong.app.reader.timeline.selected.model.BannerBooks;
import com.jingdong.app.reader.timeline.selected.model.BannersModel;
import com.jingdong.app.reader.ui.ActionBarHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

/*
 * 精选界面
 * @author wangguodong
 * 
 */
public class BooksSelectedTimelineActivity extends MZReadCommonActivity {

	private ListView listView;
	private List<BannersModel> list = new ArrayList<BannersModel>();
	private BannerAdapter adapter;
	private static final int COUNT = 6;
	private int beforeId = 0;
	private boolean noMoreBook = false;
	private boolean inLoadingMore = true;

	private View moreView;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_books_selected_timeline);
		listView = (ListView) findViewById(R.id.books_selected_timeline);

		moreView = LayoutInflater.from(BooksSelectedTimelineActivity.this)
				.inflate(R.layout.common_pullup_refresh, null);
		
		ActionBarHelper.customActionBarBack(this);

		adapter = new BannerAdapter();

		listView.addFooterView(moreView);

		listView.setAdapter(adapter);

		RequestBooksSelectedDataTask task = new RequestBooksSelectedDataTask();
		task.execute();

		listView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

				if (totalItemCount == 0)
					return;
				if (firstVisibleItem + visibleItemCount == totalItemCount - 1
						&& !noMoreBook) {
					moreView.setVisibility(View.VISIBLE);
					if (!inLoadingMore) {
						inLoadingMore = true;
						loadMoreBooksSelectedDataTask task = new loadMoreBooksSelectedDataTask();
						task.execute();
					}

				}

			}
		});
	}

	class RequestBooksSelectedDataTask extends
			AsyncTask<Void, Void, List<BannersModel>> {

		@Override
		protected List<BannersModel> doInBackground(Void... params) {

			String data = WebRequest.getWebDataWithContext(
					BooksSelectedTimelineActivity.this,
					URLText.getBooksSelectedTimeline);

			List<BannersModel> temp = new ArrayList<BannersModel>();
			try {
				JSONObject object = new JSONObject(data);
				JSONArray array = object.getJSONArray("banner");
				for (int i = 0; i < array.length(); i++) {
					BannersModel model = new BannersModel();
					model.loadBannersModel(array.getString(i));
					temp.add(model);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
			return temp;
		}

		@Override
		protected void onPostExecute(List<BannersModel> result) {
			super.onPostExecute(result);

			if (result != null && result.size() > 0) {
				list = result;
				adapter.notifyDataSetChanged();
				beforeId = list.get(list.size() - 1).getId();
			}
			inLoadingMore=false;

		}
	}

	class loadMoreBooksSelectedDataTask extends
			AsyncTask<Void, Void, List<BannersModel>> {
		@Override
		protected List<BannersModel> doInBackground(Void... params) {

			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("before_id", beforeId + "");
			paramMap.put("count", COUNT + "");
			String url = URLBuilder.addParameter(
					URLText.getBooksSelectedTimeline, paramMap);

			String data = WebRequest.getWebDataWithContext(
					BooksSelectedTimelineActivity.this, url);
			List<BannersModel> temp = new ArrayList<BannersModel>();
			try {
				JSONObject object = new JSONObject(data);
				JSONArray array = object.getJSONArray("banner");
				for (int i = 0; i < array.length(); i++) {
					BannersModel model = new BannersModel();
					model.loadBannersModel(array.getString(i));
					temp.add(model);
				}

			} catch (JSONException e) {
				e.printStackTrace();
			}
			return temp;

		}

		@Override
		protected void onPostExecute(List<BannersModel> result) {
			super.onPostExecute(result);
			if (result != null && result.size() > 0) {
				if (result.size() < COUNT) {
					noMoreBook = true;
					listView.removeFooterView(moreView);
				}
				list.addAll(result);
				beforeId = result.get(result.size() - 1).getId();
				inLoadingMore = false;
				moreView.setVisibility(View.GONE);
			}

			adapter.notifyDataSetChanged();

		}

	}

	class BannerAdapter extends BaseAdapter {

		class ViewHolder {

			ImageView image;
			TextView titleOne;
			LinearLayout layout;
			LinearLayout titles;
		}

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
		public View getView(final int arg0, View convetView, ViewGroup arg2) {

			ViewHolder holder;

			if (convetView == null) {
				convetView = LayoutInflater.from(
						BooksSelectedTimelineActivity.this).inflate(
						R.layout.item_selected_timeline, null);
				holder = new ViewHolder();

				holder.image = (ImageView) convetView
						.findViewById(R.id.selected_title_image);
				holder.titleOne = (TextView) convetView
						.findViewById(R.id.selected_title_one);
				holder.titles = (LinearLayout) convetView
						.findViewById(R.id.books_titles);
				holder.layout = (LinearLayout) convetView
						.findViewById(R.id.books_gallery_content);
				convetView.setTag(holder);
			} else {
				holder = (ViewHolder) convetView.getTag();
			}

			holder.layout.removeAllViews();
			holder.titles.removeAllViews();
			String urlPath = list.get(arg0).getImage()
					+ GlobalVarable.BOOKS_SELECTED_BANNER_IMAGE_SIZE_2X;
			ImageLoader.getInstance().displayImage(urlPath, holder.image, GlobalVarable.getDefaultBookDisplayOptions());
			

			holder.titleOne.setText(list.get(arg0).getBanner_title());

			holder.image.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent it = new Intent(BooksSelectedTimelineActivity.this,
							BooksSelectedTimelineTopBannerActivity.class);
					it.putExtra(BooksSelectedTimelineTopBannerActivity.TOP_KEY,
							list.get(arg0).getId());
					startActivity(it);
				}
			});
			holder.titleOne.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent it = new Intent(BooksSelectedTimelineActivity.this,
							BooksSelectedTimelineTopBannerActivity.class);
					it.putExtra(BooksSelectedTimelineTopBannerActivity.TOP_KEY,
							list.get(arg0).getId());
					startActivity(it);
				}
			});

			final List<AttachedEntityModel> attachedEntityModels = list.get(
					arg0).getAttached_entity();

			if (attachedEntityModels != null && attachedEntityModels.size() > 0) {
				for (int i = 0; i < attachedEntityModels.size(); i++) {
					View view = LayoutInflater.from(
							BooksSelectedTimelineActivity.this).inflate(
							R.layout.item_selected_books_titles, null);

					TextView title = (TextView) view
							.findViewById(R.id.selected_titles);

					title.setText(attachedEntityModels.get(i).getTitle());

					final int k = i;

					title.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View view) {

							Intent it = new Intent(
									BooksSelectedTimelineActivity.this,
									TimelineTweetActivity.class);

							it.putExtra(TimelineTweetActivity.TWEET_GUID,
									attachedEntityModels.get(k).getGuid());

							startActivity(it);
						}
					});
					holder.titles.addView(view);

				}
			}

			for (int i = 0; i < list.get(arg0).getBooks().size(); i++) {
				final BannerBooks book = list.get(arg0).getBooks().get(i);
				View view = LayoutInflater.from(
						BooksSelectedTimelineActivity.this).inflate(
						R.layout.item_selected_timeline_gallery, null);
				ImageView gallery_image = (ImageView) view
						.findViewById(R.id.selected_gallery_image);
				TextView textView = (TextView) view
						.findViewById(R.id.selected_gallery_title);
				textView.setText(book.getBook_name());

				
				if (!inLoadingMore) {
					String url = book.getBook_cover()
							+ GlobalVarable.bookCoverSize;
					
					ImageLoader.getInstance().displayImage(url, gallery_image, GlobalVarable.getDefaultBookDisplayOptions());
				}
				else {
				    ImageLoader.getInstance().displayImage("", gallery_image, GlobalVarable.getDefaultBookDisplayOptions());
		                
				}

				holder.layout.addView(view);

				gallery_image.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
//						Intent it = new Intent(
//								BooksSelectedTimelineActivity.this,
//								BookInfoActivity.class);
//						it.putExtra(BookInfoActivity.BookIdKey,
//								book.getBook_id());
//						it.putExtra(BookInfoActivity.BookNameKey,
//								book.getBook_name());
//						startActivity(it);
//						

						Intent intent2 = new Intent(BooksSelectedTimelineActivity.this,
								BookInfoNewUIActivity.class);
						intent2.putExtra("bookid",  book.getBook_id());
						startActivity(intent2);

					}
				});
			}

			return convetView;
		}

	}

}
