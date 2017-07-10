package com.jingdong.app.reader.me.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.entity.extra.UserReadedBook;
import com.jingdong.app.reader.entity.extra.UserReadedBookEntity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.GsonUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.tendcloud.tenddata.TCAgent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
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

public class UserReadingBookFragment extends CommonFragment {

	private View loading;
	private ListView mListView;
	private static int perPageCount = 10;

	private int currentSearchPage = 1;
	private int perSearchCount = 10;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private ReadingBookAdapter adapter;
	private List<UserReadedBook> booklist = new ArrayList<UserReadedBook>();
	private boolean inSearch = false;
	private UserReadedBookEntity bookEntity;
	private RelativeLayout emptylLayout;
	private Button emptybutton;
	private View topView;
	private int mScreenWidth;
	private int mScreenHeight;
	private String supportDate = "week";

	public UserReadingBookFragment(String supportDate) {
		super();
		if (supportDate != null)
			this.supportDate = supportDate;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentTag = "BookStoreLightReadingFragment";
		booklist.clear();

		DisplayMetrics dm = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;// 获取屏幕分辨率宽度
		mScreenHeight = dm.heightPixels;

		initField();
		searchOrder();
	}

	public void initField() {
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

		// Talking-Data
		TCAgent.onPageStart(getActivity(), "阅历-我读过的书");

		ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.user_readingbook_list, null);
		loading = rootView.findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);
		emptylLayout = (RelativeLayout) rootView.findViewById(R.id.emptylLayout);
		emptybutton = (Button) rootView.findViewById(R.id.emptybutton);
		mListView = (ListView) rootView.findViewById(R.id.mlistview);

		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (totalItemCount == 0)
					return;

				if (firstVisibleItem + visibleItemCount == totalItemCount && !noMoreBookOnSearch && !inSearch) {
					if (!inLoadingMoreOnSearch) {
						inLoadingMoreOnSearch = true;
						searchOrder();
					}
				}
			}

		});

		View header = inflater.inflate(R.layout.listview_empty_header, null);
		mListView.addHeaderView(header);
		adapter = new ReadingBookAdapter(getActivity());
		mListView.setAdapter(adapter);
		return rootView;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// Talking-Data
		TCAgent.onPageEnd(getActivity(), "阅历-我读过的书");
	}

	private void searchOrder() {
		WebRequestHelper.get(URLText.JD_BASE_URL,
				RequestParamsPool.getUserReadedBookParams(currentSearchPage + "", perSearchCount + "", supportDate, mScreenHeight + "*" + mScreenWidth),
				new MyAsyncHttpResponseHandler(getActivity()) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						Toast.makeText(getActivity(), getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

						String result = new String(responseBody);

						bookEntity = GsonUtils.fromJson(result, UserReadedBookEntity.class);

						if (bookEntity != null && bookEntity.getCode().equals("0")) {
							if (bookEntity.getResultList() == null) {
								mListView.setVisibility(View.GONE);
								emptylLayout.setVisibility(View.VISIBLE);
								return;
							}
							if (bookEntity != null && bookEntity.getCode().equals("0")) {
								if (bookEntity.getResultList().size() == 0) {
									mListView.setVisibility(View.GONE);
									emptylLayout.setVisibility(View.VISIBLE);
								} else {
									currentSearchPage++;

									List<UserReadedBook> all = new ArrayList<UserReadedBook>();
									for (int i = 0; i < bookEntity.getResultList().size(); i++) {
										UserReadedBook userReadedBook = bookEntity.getResultList().get(i);
										all.add(userReadedBook);
									}
									booklist.addAll(all);
									adapter.notifyDataSetChanged();

									if (bookEntity.getResultList() != null && bookEntity.getTotalCount() <= booklist.size()) {
										noMoreBookOnSearch = true;
									} else if (bookEntity.getResultList() != null && bookEntity.getResultList().size() < perPageCount) {
										noMoreBookOnSearch = true;
									} else {
										noMoreBookOnSearch = false;
									}
								}
							} else if (bookEntity == null)
								Toast.makeText(getActivity(), getString(R.string.network_connect_error), Toast.LENGTH_LONG).show();
							inLoadingMoreOnSearch = false;
							loading.setVisibility(View.GONE);
						}
					}
				});
	}

	private class ReadingBookAdapter extends BaseAdapter {

		private Context context;

		class ViewHolder {

			TextView bookName;
			ImageView bookImage;
		}

		ReadingBookAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return booklist.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public boolean areAllItemsEnabled(){
			return false;
		}

		public boolean isEnabled(int position){
			return false;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.readingbook_item, parent, false);
				holder = new ViewHolder();
				holder.bookName = (TextView) convertView.findViewById(R.id.name);
				holder.bookImage = (ImageView) convertView.findViewById(R.id.image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			UserReadedBook readingbook = booklist.get(position);
			holder.bookName.setText("读过了《" + readingbook.getEbookName() + "》");
			ImageLoader.getInstance().displayImage(readingbook.getPicUrl(), holder.bookImage, GlobalVarable.getCutBookBigViewDisplayOptions());

			return convertView;
		}
	}
}
