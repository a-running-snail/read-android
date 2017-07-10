package com.jingdong.app.reader.me.activity;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.WrapperListAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.CommonFragment;
import com.jingdong.app.reader.extension.jpush.JDMessageReceiver;
import com.jingdong.app.reader.extension.jpush.JDMessageReceiver.JDMessage;
import com.jingdong.app.reader.extension.jpush.JDMessageReceiver.JDMessages;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.privateMsg.Conversation;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.UiStaticMethod;

@SuppressLint("InflateParams")
public class JDMessageFragment extends CommonFragment {

	private View loading;
	private ListView mListView;
	private List<JDMessage> messageItems = new ArrayList<JDMessage>();
	private MessageAdapter messageAdapter;
	private LinearLayout linearLayout;
	private RelativeLayout relativeLayout;
	private TextView textView;
	private Context context;
	private View rootView;
	ProgressDialog progressDialog; 
	
	public JDMessageFragment() {
		super();
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		rootView = (View) inflater.inflate(R.layout.fragment_jdmessage, null);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		context=getActivity();
		initView();
		getMessageItems();
	}

	@SuppressWarnings("deprecation")
	private void initView() {
		loading =rootView.findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);
		mListView = (ListView) rootView.findViewById(R.id.mlistview);

		registerForContextMenu(mListView);
		linearLayout = (LinearLayout) rootView.findViewById(R.id.linearLayout);
		relativeLayout = (RelativeLayout) rootView.findViewById(R.id.relativeLayout);
		textView = (TextView) rootView.findViewById(R.id.text);

		messageAdapter = new MessageAdapter(context);
		mListView.setAdapter(messageAdapter);
		setHasOptionsMenu(true);
		
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				if(!messageItems.get(position).isShow){
					messageItems.get(position).isShow=true;
					JDMessages jDMessages = new JDMessages();
					jDMessages.messages = messageItems;
					LocalUserSetting.saveJDMessageList(context, jDMessages);
					messageAdapter.notifyDataSetChanged();
				}
				String content = messageItems.get(position).content;
				try {
					Intent intent = doPushBusses(getActivity(),content);
					context.startActivity(intent);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void getMessageItems() {
		messageItems = LocalUserSetting.getJDMessageList(context);
		messageAdapter.notifyDataSetChanged();
		loading.setVisibility(View.GONE);
		if(messageItems.size()==0){
			linearLayout.setVisibility(View.VISIBLE);
			textView.setText("暂无通知");
			relativeLayout.setVisibility(View.GONE);
		}
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

			holder.content.setText(messageItems.get(position).alerMessage);
			holder.time.setText(TimeFormat.formatTimeByMiliSecond(getResources(), messageItems.get(position).time));
			holder.imView.setVisibility(View.VISIBLE);
			if (messageItems.get(position).isShow)
				holder.dot.setVisibility(View.GONE);
			else
				holder.dot.setVisibility(View.VISIBLE);

			return convertView;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = new MenuInflater(getActivity());
		inflater.inflate(R.menu.delete, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
		case R.id.delete:
			messageItems.remove(info.position);
			JDMessages jDMessages = new JDMessages();
			jDMessages.messages = messageItems;
			LocalUserSetting.saveJDMessageList(context, jDMessages);
			messageAdapter.notifyDataSetChanged();
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}
	
	/**
	 * @param content
	 * @return
	 * @throws JSONException
	 */
	private Intent doPushBusses(Context context, String content) throws Exception {
		JSONObject jObject = new JSONObject(content);
		if (jObject.has("lx")) {
			Intent intent = null;
			String lxStr = jObject.getString("lx");
			int lxInt = Integer.parseInt(lxStr);
			switch (lxInt) {
			case 0:// 书城
				intent = new Intent(context, LauncherActivity.class);
				if (jObject.has("showname")) {
					int relationType = Integer.parseInt(jObject.getString("relatetype"));
					if (relationType == 1) {
						if (!JDMessageReceiver.isBackground(context)) {
							intent = new Intent(context, BookStoreBookListActivity.class);
						}
						intent.putExtra("lx", 0);
						intent =new Intent(MZBookApplication.getContext(),BookStoreBookListActivity.class);
						if (jObject.has("fid")) {
							intent.putExtra("fid", Integer.parseInt(jObject.getString("fid")));
						}
						if (jObject.has("ftype"))
							intent.putExtra("ftype", Integer.parseInt(jObject.getString("ftype")));
						intent.putExtra("relationType", relationType);
						if (jObject.has("showname"))
							intent.putExtra("showName", jObject.getString("showname"));
						if (jObject.has("list_type"))
							intent.putExtra("list_type", Integer.parseInt(jObject.getString("list_type")));
						if (jObject.has("bannerimg"))
							intent.putExtra("bannerImg", jObject.getString("bannerimg"));
						if (jObject.has("boolnew"))
							intent.putExtra("boolNew", Boolean.parseBoolean(jObject.getString("boolnew")));
					} else {
						if (jObject.has("url")) {
							if (!JDMessageReceiver.isBackground(context)) {
								intent = new Intent(context, WebViewActivity.class);
							}
							String url = jObject.getString("url");
							intent.putExtra(WebViewActivity.UrlKey, url);
							intent.putExtra(WebViewActivity.TitleKey, jObject.getString("showname"));
							intent.putExtra(WebViewActivity.TopbarKey, false);
							intent.putExtra("lx", lxInt);
						} else {
							intent.putExtra("lx", 0);
						}
					}
				} else {
					intent.putExtra("lx", 0);
				}
				return intent;
			case 1:// 书架
				intent = new Intent(context, LauncherActivity.class);
				intent.putExtra("lx", 1);
				return intent;
			case 2:// 社区
				intent = new Intent(context, LauncherActivity.class);
				intent.putExtra("lx", 2);
				return intent;
			case 3:// 我的主页
				
				String subIndex = jObject.optString("sub_page");
				if (!TextUtils.isEmpty(subIndex)) {
					if ("2".equals(subIndex)) {
						intent = new Intent(context,IntegrationActivity.class);
					}
				}else{
					intent = new Intent(context, LauncherActivity.class);
					intent.putExtra("lx", 3);
				}
				break;
			case 4:// 图书详情页
				intent = new Intent(context, BookInfoNewUIActivity.class);
				intent.putExtra("lx", lxInt);
				long id = 0;
				if (jObject.has("bookid")) {
					id = Long.parseLong(jObject.getString("bookid"));
				}
				intent.putExtra("bookid", id);
				intent.putExtra("type", "jdmessage");
				break;
			case 5:// 活动M页
			case 6:// 专题页
				if (jObject.has("url")) {
					String url = jObject.getString("url");
					intent = new Intent(context, WebViewActivity.class);
					intent.putExtra(WebViewActivity.UrlKey, url);
					intent.putExtra(WebViewActivity.TopbarKey, false);
					intent.putExtra("lx", lxInt);
				} else {
					intent = null;
				}
				break;
			case 12:// 优惠
				intent = new Intent(context, LauncherActivity.class);
				intent.putExtra("lx", 0);
				intent.putExtra("SUB_PAGE", 1);
				break;
			default:
				return null;
			}
			return intent;
		} else {
			return null;
		}
	}
}
