package com.jingdong.app.reader.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.album.ImageData;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.TimelineBookListCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelinePostTweetActivity;
import com.jingdong.app.reader.timeline.model.TimelineActivityModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.TimeFormat;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.util.ViewHolder;
import com.jingdong.app.reader.view.dialog.DialogManager;

public class DraftsActivity extends BaseActivityWithTopBar {

	public static final int TYPE_POST_TWEET = 101;// 随便说说
	public static final int TYPE_POST_COMMENT = 102;// 评论
	public static final int TYPE_POST_BOOK_COMMENT = 103;// 书评
	public static final int TYPE_POST_FORWARD = 104;// 转发

	public static class Draft implements Serializable {

		public int type;
		public String content;
		public long time;
		public String useridBuffer;
		public String bookidBuffer;

		public int book_id;// 书评的时候有
		public float rating;// 书评的时候有
		public String title="";

		// 评论的时候
		public String entity_guid;
		
		
		public String origin_content;
		public long replyTo;
		/** 草稿箱中选择的图片路径列表 */
		public List<ImageData> photoPath;
		//以下为转发所需字段
		public String forwardImage;
		public String forwardNickname;
		public String forwardContent;

	}

	public static class Drafts {
		public List<Draft> drafts = null;
	}

	private ListView listview = null;
	private List<Draft> draftsList = new ArrayList<Draft>();
	private DraftsAdapter adapter = null;
	private LinearLayout empty = null;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.activity_drafts);
		listview = (ListView) findViewById(R.id.listview);
		adapter = new DraftsAdapter();
		listview.setAdapter(adapter);
	
		empty =(LinearLayout) findViewById(R.id.empty);
		listview.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {

				final int po = position;

				DialogManager.showCommonDialog(DraftsActivity.this, "提示",
						"您要删除这条记录吗?", "确定", "取消", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								switch (which) {
								case DialogInterface.BUTTON_POSITIVE:

									draftsList.remove(po);
									adapter.notifyDataSetChanged();

									Drafts drafts = new Drafts();
									drafts.drafts = draftsList;
									LocalUserSetting.saveDraftsList(
											DraftsActivity.this, drafts);

									break;
								case DialogInterface.BUTTON_NEGATIVE:

									break;
								}

								dialog.dismiss();
							}
						});

				return true;
			}
		});

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				clickPosition = position;

				Draft draft = draftsList.get(position);
				int type = draft.type;

				switch (type) {
				case 101:
					// typeView.setText("随便说说");

					Intent it1 = new Intent(DraftsActivity.this,
							TimelinePostTweetActivity.class);
					it1.putExtra("title",
							getString(R.string.timeline_post_title));
					it1.putExtra("draft", draft);
					it1.putExtra("from", "drafts");
					startActivityForResult(it1, TimelineRootFragment.POST_TWEET);

					break;
				case 102:
					// typeView.setText("评论");
					Intent it3 = new Intent(DraftsActivity.this,
							TimelineCommentsActivity.class);
					it3.putExtra("draft", draft);

					Bundle tempBundle = new Bundle();
					tempBundle.putString(TimelineCommentsActivity.ENTITY_GUID,
							draft.entity_guid);
					tempBundle.putBoolean(TimelineCommentsActivity.IS_COMMENT,
							true);
					tempBundle.putString(
							TimelineCommentsActivity.ORIGIN_CONTENT,
							draft.origin_content);
					tempBundle.putLong(TimelineCommentsActivity.REPLY_TO,
							draft.replyTo);

					it3.putExtras(tempBundle);

					startActivityForResult(it3, TYPE_POST_COMMENT);

					break;
				case 103:
					// typeView.setText("书评");
					Intent it2 = new Intent(DraftsActivity.this,
							TimelineBookListCommentsActivity.class);
					it2.putExtra("type", "draft");
					it2.putExtra("draft", draft);
					startActivityForResult(it2, TYPE_POST_BOOK_COMMENT);
					break;
				case 104:
					Intent it4 = new Intent(DraftsActivity.this,
							TimelineCommentsActivity.class);
					Bundle tempBundle1 = new Bundle();
					tempBundle1.putString(TimelineCommentsActivity.ENTITY_GUID,
							draft.entity_guid);
					tempBundle1.putBoolean(TimelineCommentsActivity.IS_COMMENT,
							false);
					tempBundle1.putString(
							TimelineCommentsActivity.ORIGIN_CONTENT,
							draft.origin_content);
					tempBundle1.putLong(TimelineCommentsActivity.REPLY_TO,
							draft.replyTo);
					it4.putExtras(tempBundle1);
					it4.putExtra("draft", draft);
					startActivityForResult(it4, TYPE_POST_FORWARD);
					break;
				}

			}
		});
		
	}

	@Override
	protected void onResume() {
		getLocalData();
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_draffs));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_draffs));
	}
	
	public void getLocalData() {

		draftsList.clear();
		List<Draft> list = LocalUserSetting.getDraftsList(DraftsActivity.this);

		if (null != list&&list.size()>0) {
			draftsList.addAll(list);
			Collections.sort(draftsList, new TimeComparator());
			adapter.notifyDataSetChanged();
			
			empty.setVisibility(View.GONE);
			listview.setVisibility(View.VISIBLE);
		}
		else {
			empty.setVisibility(View.VISIBLE);
			listview.setVisibility(View.GONE);
		}

	}

	class TimeComparator implements Comparator<Draft> {

		@Override
		public int compare(Draft lhs, Draft rhs) {

			if (lhs.time - rhs.time < 0)
				return 1;
			else if (lhs.time - rhs.time > 0) {
				return -1;
			}

			return 0;
		}

	}

	class DraftsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return draftsList == null ? 0 : draftsList.size();
		}

		@Override
		public Object getItem(int position) {
			return draftsList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				convertView = LayoutInflater.from(DraftsActivity.this).inflate(
						R.layout.item_draft, null);
			}

			TextView typeView = ViewHolder.get(convertView, R.id.type);
			TextView timeView = ViewHolder.get(convertView, R.id.time);
			TextView contentView = ViewHolder.get(convertView, R.id.content);

			final Draft draft = draftsList.get(position);
			final int type = draft.type;
			switch (type) {
			case 101:
				typeView.setText("随便说说");
				break;
			case 102:
				typeView.setText("评论");
				break;
			case 103:
				typeView.setText("书评");
				break;
			case 104:
				typeView.setText("转发");
				break;

			}

			final int po = position;
			timeView.setText(TimeFormat.formatTimeByMiliSecond(getResources(),
					draft.time));
			contentView.setText(draft.content);

			return convertView;
		}

	}

	private static int clickPosition = -1;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case TimelineRootFragment.POST_TWEET:
			switch (resultCode) {
			case TimelinePostTweetActivity.POST_TWEET_WORDS:
				
//				Bundle postTweetBundle = data.getExtras();
//				new TimelineActivityModel().postTweet(DraftsActivity.this,
//						postTweetBundle);
				if (null == data)
					return;
				if (clickPosition != -1) {
					draftsList.remove(clickPosition);
					adapter.notifyDataSetChanged();

					Drafts drafts = new Drafts();
					drafts.drafts = draftsList;
					LocalUserSetting
							.saveDraftsList(DraftsActivity.this, drafts);

				}

				break;
			}
			break;

		case TYPE_POST_COMMENT:
			if (null == data)
				return;
			Bundle commentBundle = data.getExtras();
			postComment(DraftsActivity.this, commentBundle);
			if (clickPosition != -1) {
				draftsList.remove(clickPosition);
				adapter.notifyDataSetChanged();

				Drafts drafts = new Drafts();
				drafts.drafts = draftsList;
				LocalUserSetting.saveDraftsList(DraftsActivity.this, drafts);
			}

			break;

		case TYPE_POST_BOOK_COMMENT:

			if (null == data)
				return;

			Bundle postTweetBundle1 = data.getBundleExtra("data");

			RequestPostBookCommets(postTweetBundle1);

			break;
		case TYPE_POST_FORWARD:
			if (null == data)
				return;
			Bundle commentBundle2 = data.getExtras();
			postComment(DraftsActivity.this, commentBundle2);
			if (clickPosition != -1) {
				draftsList.remove(clickPosition);
				adapter.notifyDataSetChanged();

				Drafts drafts = new Drafts();
				drafts.drafts = draftsList;
				LocalUserSetting.saveDraftsList(DraftsActivity.this, drafts);
			}

			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	public void postComment(final Context context, final Bundle bundle) {

		if (null == bundle)
			return;
		WebRequestHelper.post(URLText.commmentsPostUrl,
				RequestParamsPool.getEntitysCommentsOrForwordParams(bundle),
				true, new MyAsyncHttpResponseHandler(context) {

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {

						String result = new String(responseBody);

						MZLog.d("wangguodong", result);
						ToastUtil.showToastInThread("评论发送成功了",
								Toast.LENGTH_SHORT);

					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						ToastUtil.showToastInThread("请求失败了,请重试!",
								Toast.LENGTH_SHORT);
					}
				});
	}

	public void RequestPostBookCommets(Bundle bundle) {

		if (null == bundle)
			return;

		long book_id = bundle.getLong("book_id");
		String content = bundle.getString("content");
		float rating = bundle.getFloat("rating");
		String userids = bundle.getString("userids");

		WebRequestHelper.post(URLText.bookCommentUrl, RequestParamsPool
				.getRequestPostBookComments((int) book_id, content, rating,userids),
				true, new MyAsyncHttpResponseHandler(DraftsActivity.this) {

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						String result = new String(responseBody);

						try {
							JSONObject obj = new JSONObject(result);
							int code = obj.optInt("code");

							if (code == 0) {
								Toast.makeText(
										DraftsActivity.this,
										getString(R.string.post_book_comment_ok),
										Toast.LENGTH_LONG).show();

								if (clickPosition != -1) {
									draftsList.remove(clickPosition);
									adapter.notifyDataSetChanged();

									Drafts drafts = new Drafts();
									drafts.drafts = draftsList;
									LocalUserSetting.saveDraftsList(
											DraftsActivity.this, drafts);

								}

							} else {
								Toast.makeText(
										DraftsActivity.this,
										getString(R.string.post_book_comment_error),
										Toast.LENGTH_LONG).show();
							}

						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				});
	}

}
