package com.jingdong.app.reader.me.activity;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.entity.extra.Alert;
import com.jingdong.app.reader.entity.extra.AlertItem;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.TimelineTweetActivity;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.UiStaticMethod;

@SuppressLint("InflateParams")
public class NotificationFragment extends CommonFragment  {

	private Alert alerts;
	private View loading;
	private ListView mListView;
	private static int perPageCount = 10;

	private int currentSearchPage = 1;
	private int perSearchCount = 10;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private List<AlertItem> alertItems = new ArrayList<AlertItem>();
	private NotivityAdapter notivityAdapter;
	private String before_id;
	private LinearLayout linearLayout;
	private RelativeLayout relativeLayout;
	private TextView textView;
	private int isread;
	private Context context;
	private View rootView;
	
	public NotificationFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		rootView = (View) inflater.inflate(R.layout.fragment_notifition, null);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		context=getActivity();
		Notification.getInstance().setReadAlertsCount(getActivity());
		initField();
		initView();
		getNotifitionInfo("");
	}

	public void initField() {
		currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}

	@SuppressWarnings("deprecation")
	private void initView() {
		loading =rootView.findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);
		mListView = (ListView) rootView.findViewById(R.id.mlistview);

		linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
		relativeLayout = (RelativeLayout) rootView.findViewById(R.id.relativeLayout);
		textView = (TextView) rootView.findViewById(R.id.text);

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
						before_id = alertItems.get(alertItems.size() - 1)
								.getId();
						getNotifitionInfo(before_id);
					}
				}
			}

		});

		notivityAdapter = new NotivityAdapter(context);
		mListView.setAdapter(notivityAdapter);
	}
	
	@SuppressWarnings("deprecation")
	private void getNotifitionInfo(final String before_id) {
		WebRequestHelper.get(URLText.Alerts_URL,
				RequestParamsPool.getAlarmParams(10 + "", before_id), true,
				new MyAsyncHttpResponseHandler(context, true) {

					
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
						alerts = GsonUtils.fromJson(result, Alert.class);
						if (alerts != null) {
							if (alerts.getAlerts().size() > 0) {
								List<AlertItem> all = new ArrayList<AlertItem>();
								for (int i = 0; i < alerts.getAlerts().size(); i++) {
									AlertItem alertItem = alerts.getAlerts()
											.get(i);
									all.add(alertItem);
								}
								alertItems.addAll(all);
								notivityAdapter.notifyDataSetChanged();

								if (alerts.getAlerts() != null
										&& alerts.getAlerts().size() < perPageCount) {
									noMoreBookOnSearch = true;
								} else {
									noMoreBookOnSearch = false;
								}

							} else if (alerts.getAlerts().size() == 0) {
								relativeLayout.setVisibility(View.GONE);
								linearLayout.setVisibility(View.VISIBLE);
								textView.setText("暂无通知");
								inLoadingMoreOnSearch = false;
								loading.setVisibility(View.GONE);
							}
						} else if (alerts == null)
							Toast.makeText(context,
									getString(R.string.network_connect_error),
									Toast.LENGTH_LONG).show();
						inLoadingMoreOnSearch = false;
						loading.setVisibility(View.GONE);
					}
				});
	}

	private class NotivityAdapter extends BaseAdapter {

		private Context context;

		private class ViewHolder {
			RelativeLayout relativeLayout;
			ImageView imView;
			TextView time;
			TextView content;
			ImageView dot;
		}

		public NotivityAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return alertItems.size();
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
						R.layout.notifityitem, parent, false);
				holder = new ViewHolder();
				holder.relativeLayout = (RelativeLayout) convertView
						.findViewById(R.id.relativeLayout);
				holder.time = (TextView) convertView.findViewById(R.id.time);
				holder.content = (TextView) convertView
						.findViewById(R.id.content);
				holder.imView = (ImageView) convertView
						.findViewById(R.id.image);
				holder.dot = (ImageView) convertView.findViewById(R.id.dot);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.content.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (!alertItems.get(position).getLink().equals("")) {
						String str = alertItems.get(position).getLink()
								.substring(10);
						String[] guid = str.split("\\.");
						Intent intent = new Intent(context,TimelineTweetActivity.class);
						intent.putExtra(TimelineTweetActivity.TWEET_GUID,
								guid[0]);
						startActivity(intent);
					}
				}
			});
//			holder.content.setText(alertItems.get(position).getText());
			UiStaticMethod.setAtUrlClickable(context, holder.content, alertItems.get(position).getText());
			holder.time.setText(TimeFormat.formatTime(getResources(), Long
					.parseLong(alertItems.get(position)
							.getCreated_at_timestamp())));
			if (alertItems.get(position).getLink().equals(""))
				holder.imView.setVisibility(View.GONE);
			else
				holder.imView.setVisibility(View.VISIBLE);
//			if (alertItems.get(position).getIs_read() == 0) {
//				holder.dot.setVisibility(View.VISIBLE);
//			} else {
				holder.dot.setVisibility(View.GONE);
//			}

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if (!alertItems.get(position).getLink().equals("")) {
						String str = alertItems.get(position).getLink()
								.substring(10);
						String[] guid = str.split("\\.");
						Intent intent = new Intent(context,TimelineTweetActivity.class);
						intent.putExtra(TimelineTweetActivity.TWEET_GUID,
								guid[0]);
						startActivity(intent);
					}
				}
			});

			return convertView;
		}
	}
}
