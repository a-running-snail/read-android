package com.jingdong.app.reader.me.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.WrapperListAdapter;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.Conversation;
import com.jingdong.app.reader.entity.extra.ConversationItem;
import com.jingdong.app.reader.message.activity.ChatActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.jingdong.app.reader.view.RoundNetworkImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

public class PrivateMessageActivity extends BaseActivityWithTopBar implements
		SwipeRefreshLayout.OnRefreshListener {

	private Conversation conversation;
	private View loading;
	private ListView mListView;
	private static int perPageCount = 10;

	// private int currentSearchPage = 1;
	private boolean noMoreBookOnSearch = false;
	private boolean inLoadingMoreOnSearch = true;
	private String before_id;
	private PrivateMessageAdapter privateMessageAdapter;
	private List<ConversationItem> conversationItem = new ArrayList<ConversationItem>();
	private static SwipeRefreshLayout mSwipeLayout;
	private final static int CHAT_WITH_USER_IN_LIST = 102;
	private RelativeLayout relativeLayout;
	private LinearLayout linearLayout;
	private TextView textView;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.fragment_notifition);
		initField();
		initView();
		getMessage("");
	}

	public void initField() {
		// currentSearchPage = 1;
		noMoreBookOnSearch = false;
		inLoadingMoreOnSearch = true;
	}

	private void initView() {
		loading = findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);
		mListView = (ListView) findViewById(R.id.mlistview);
		mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.ptr_layout);
		mSwipeLayout.setOnRefreshListener(this);
		mSwipeLayout.setColorScheme(R.color.red_main, R.color.bg_main,
				R.color.red_sub, R.color.bg_main);
		
		relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
		linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
		textView = (TextView) findViewById(R.id.text);
		registerForContextMenu(mListView);
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
						before_id = conversationItem.get(
								conversationItem.size() - 1).getId();
						getMessage(before_id);
					}
				}
			}

		});

		privateMessageAdapter = new PrivateMessageAdapter(
				PrivateMessageActivity.this);
		mListView.setAdapter(privateMessageAdapter);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = new MenuInflater(PrivateMessageActivity.this);
		inflater.inflate(R.menu.delete, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		WrapperListAdapter myAdapter = (WrapperListAdapter) mListView.getAdapter();
		switch (item.getItemId()) {
		case R.id.delete:
			popDialog(conversationItem.get(info.position).getId());
			return true;
		default:
			return super.onContextItemSelected(item);
		}

	}
	
	private void popDialog(final String id) {
		UiStaticMethod
				.createConfirmDialog(PrivateMessageActivity.this, R.string.delete_conversation, R.string.delete_conversation_confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Resources resources = getResources();
//								model.deleteConversation(id);
								dialog.dismiss();
								progressDialog = ProgressDialog.show(PrivateMessageActivity.this,
										resources.getString(R.string.delete), resources.getString(R.string.deleting),
										true, false);
							}
						}).create().show();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	private void getMessage(final String before_id) {
		WebRequestHelper.get(URLText.Message_URL, RequestParamsPool
				.getAlarmParams(10 + "", before_id), true,
				new MyAsyncHttpResponseHandler(PrivateMessageActivity.this,
						true) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// TODO Auto-generated method stub
						Toast.makeText(PrivateMessageActivity.this,
								getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						// TODO Auto-generated method stub

						String result = new String(responseBody);

						Log.d("cj", "result=======>>" + result);
						conversation = GsonUtils.fromJson(result,
								Conversation.class);
						if (conversation != null) {
							if (conversation.getConversationItem().size() > 0) {

								List<ConversationItem> all = new ArrayList<ConversationItem>();
								for (int i = 0; i < conversation
										.getConversationItem().size(); i++) {
									ConversationItem alertItem = conversation
											.getConversationItem().get(i);
									all.add(alertItem);
								}
								conversationItem.addAll(all);
								privateMessageAdapter
								.notifyDataSetChanged();
								if (before_id.equals("")) {
									noMoreBookOnSearch = false;
									mSwipeLayout.setRefreshing(false);
								} else {
									if (conversation.getConversationItem() != null
											&& conversation
													.getConversationItem()
													.size() < 10) {
										noMoreBookOnSearch = true;
										Toast.makeText(PrivateMessageActivity.this,
												getString(R.string.no_more_data),
												Toast.LENGTH_LONG).show();
									} else {
										noMoreBookOnSearch = false;
									}
								}
							} else if (conversation.getConversationItem()
									.size() == 0) {
								relativeLayout.setVisibility(View.GONE);
								linearLayout.setVisibility(View.VISIBLE);
								textView.setText("暂无私信");
								inLoadingMoreOnSearch = false;
								loading.setVisibility(View.GONE);
							}

						} else if (conversation == null)
							Toast.makeText(PrivateMessageActivity.this,
									getString(R.string.network_connect_error),
									Toast.LENGTH_LONG).show();
						inLoadingMoreOnSearch = false;
						loading.setVisibility(View.GONE);
					}

				});
	}

	private class PrivateMessageAdapter extends BaseAdapter {

		private Context context;

		private class ViewHolder {
			RelativeLayout relativeLayout;
			RoundNetworkImageView avatar_label;
			TextView timeline_user_name;
			TextView timeline_user_summary;
			ImageView imagebutton;
		}

		public PrivateMessageAdapter(Context context) {
			// TODO Auto-generated constructor stub
			this.context = context;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return conversationItem.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			// TODO Auto-generated method stub
			final ViewHolder holder;

			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.privatemessage, parent, false);
				holder = new ViewHolder();
				holder.relativeLayout = (RelativeLayout) convertView
						.findViewById(R.id.relativeLayout);
				holder.avatar_label = (RoundNetworkImageView) convertView
						.findViewById(R.id.thumb_nail);
				holder.timeline_user_name = (TextView) convertView
						.findViewById(R.id.timeline_user_name);
				holder.timeline_user_summary = (TextView) convertView
						.findViewById(R.id.timeline_user_summary);
				holder.imagebutton = (ImageView) convertView
						.findViewById(R.id.imagebutton);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			ImageLoader.getInstance().displayImage(
					conversationItem.get(position).getUser().getAvatar(),
					holder.avatar_label,
					GlobalVarable.getDefaultAvatarDisplayOptions(false));
			holder.timeline_user_name.setText(conversationItem.get(position)
					.getUser().getName());
			// holder.timeline_user_summary.setText(conversationItem.get(position).getUser().ge)
			holder.imagebutton
					.setBackgroundResource(R.drawable.icon_arrow_right);
			holder.relativeLayout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(PrivateMessageActivity.this,
							ChatActivity.class);
					Log.d("cj", "position=============>>>>" + position);
					UserInfo userInfo = conversationItem.get(position)
							.getUser();
					intent.putExtra(ChatActivity.USER_INFO, userInfo);
					intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivityForResult(intent, CHAT_WITH_USER_IN_LIST);
				}
			});

			return convertView;
		}
	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub
		if (conversationItem.size() != 0) {
			conversationItem.clear();
			getMessage("");
		} else {
			mSwipeLayout.setRefreshing(false);
		}
	}
}
