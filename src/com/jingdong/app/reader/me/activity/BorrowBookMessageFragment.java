package com.jingdong.app.reader.me.activity;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.me.model.UnreadMessage;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.TimeFormat;

@SuppressLint("InflateParams")
public class BorrowBookMessageFragment extends CommonFragment {

	private View loading;
	private ListView mListView;
	private List<UnreadMessage> messageItems = new ArrayList<UnreadMessage>();
	private MessageAdapter messageAdapter;
	private LinearLayout linearLayout;
	private RelativeLayout relativeLayout;
	private TextView textView;
	private Context context;
	private View rootView;
	ProgressDialog progressDialog; 
	private boolean haveShowed=false;
	
	public BorrowBookMessageFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		rootView = (View) inflater.inflate(R.layout.fragment_userborrow_message, null);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		context=getActivity();
		initView();
	}

	private void initView() {
		loading =rootView.findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);
		mListView = (ListView) rootView.findViewById(R.id.mlistview);

		linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
		relativeLayout = (RelativeLayout) rootView.findViewById(R.id.relativeLayout);
		textView = (TextView) rootView.findViewById(R.id.text);

		messageAdapter = new MessageAdapter(context);
		mListView.setAdapter(messageAdapter);
	}
	
	
	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if(isVisibleToUser && !haveShowed){
			haveShowed=true;
			getSendBookMessage();
		}
		super.setUserVisibleHint(isVisibleToUser);
	}
	
	/**
	 * 获取借阅通知
	 */
	private void getMessageItems() {

		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getBorrowMessageParams(), true,
				new MyAsyncHttpResponseHandler(context, true) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String json = new String(responseBody);
						try {
							JSONObject object = new JSONObject(json);
							if (object == null || !object.has("code") || !object.optString("code").equals("0"))
								return;
							JSONArray messages = object.optJSONArray("unreadMessage");
							if (messages == null)
								return;
							UnreadMessage message;
							for (int i = 0; i < messages.length(); i++) {
								message = GsonUtils.fromJson(messages.getString(i), UnreadMessage.class);
								messageItems.add(message);
							}
							messageAdapter.notifyDataSetChanged();

							loading.setVisibility(View.GONE);
							if (messageItems.size() == 0) {
								linearLayout.setVisibility(View.VISIBLE);
								textView.setText("暂无通知");
								relativeLayout.setVisibility(View.GONE);
							}
							updateUnreadMessage();
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}
	
	/**
	 * 获取购书赠书通知
	 */
	private void getSendBookMessage(){
		
		if (!NetWorkUtils.isNetworkConnected(context)) {
			linearLayout.setVisibility(View.VISIBLE);
			textView.setText("暂无通知");
			relativeLayout.setVisibility(View.GONE);
			return;
		}
		
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getSendBookMessageParams(), true,
				new MyAsyncHttpResponseHandler(context, true) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String json = new String(responseBody);
						try {
							JSONObject object = new JSONObject(json);
							if (object == null || !object.has("code") || !object.optString("code").equals("0"))
								return;
							JSONArray messages = object.optJSONArray("unreadMessage");
							if (messages == null)
								return;
							UnreadMessage message;
							for (int i = 0; i < messages.length(); i++) {
								message = GsonUtils.fromJson(messages.getString(i), UnreadMessage.class);
								messageItems.add(message);
							}
							messageAdapter.notifyDataSetChanged();
							
							getMessageItems();
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}
	
	/**
	 * 更新信息的未读状态
	 */
	private void updateUnreadMessage(){
		if (!NetWorkUtils.isNetworkConnected(context)) 
			return;
		
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.updateGiftBagStatus("4"), true,
				new MyAsyncHttpResponseHandler(context, true) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					}
					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
					}
				});
		
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.updateGiftBagStatus("5"), true,
				new MyAsyncHttpResponseHandler(context, true) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					}
					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
					}
				});
	}

	private class MessageAdapter extends BaseAdapter {

		private Context context;

		private class ViewHolder {
			RelativeLayout relativeLayout;
			ImageView imView;
			TextView time;
			TextView content;
			ImageView dot;
		}

		public MessageAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getCount() {
			return messageItems.size();
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
						R.layout.userborrow_notifityitem, parent, false);
				holder = new ViewHolder();
				holder.relativeLayout = (RelativeLayout) convertView
						.findViewById(R.id.relativeLayout);
				holder.time = (TextView) convertView.findViewById(R.id.time);
				holder.content = (TextView) convertView
						.findViewById(R.id.content);
				holder.imView = (ImageView) convertView.findViewById(R.id.image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			String timeStr = TimeFormat.formatTimeByMiliSecond(context.getResources(), TimeFormat.formatStringTime(messageItems.get(position).createdStr));
			holder.content.setText(messageItems.get(position).fromDesc);
			holder.time.setText(timeStr);
			
			if (messageItems.get(position).detailType.equals("5")) {
				holder.imView.setVisibility(View.VISIBLE);
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(context, OrderActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						context.startActivity(intent);
					}
				});
			}else{
				holder.imView.setVisibility(View.GONE);
				convertView.setClickable(false);
			}
				
			return convertView;
		}
	}

}
