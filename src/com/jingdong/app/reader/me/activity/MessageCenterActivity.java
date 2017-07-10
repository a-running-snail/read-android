package com.jingdong.app.reader.me.activity;


import java.util.List;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.extra.Conversation;
import com.jingdong.app.reader.extension.jpush.JDMessageReceiver.JDMessage;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.CommentActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.Log;


public class MessageCenterActivity extends BaseActivityWithTopBar{

	private RelativeLayout me_layout;
	private RelativeLayout comment_layout;
	private RelativeLayout message_layout;
	private RelativeLayout notification_layout;
//	private TextView notifity_txt;
	private LinearLayout notivfity_number;
	private Conversation conversation;
	private String alert_count;
	private String atme_count;
	private String comments_count;
	private String message_count;
	private LinearLayout atme_layout;
//	private TextView atme_text;
	private LinearLayout comments_number;
//	private TextView comments_txt;
	private LinearLayout message_number;
//	private TextView message_txt;
	
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.messagecenter);
		initView();
	}
	
	private void initView(){
		me_layout = (RelativeLayout) findViewById(R.id.me_layout);
		comment_layout = (RelativeLayout) findViewById(R.id.comment_layout);
		message_layout = (RelativeLayout) findViewById(R.id.message_layout);
		notification_layout = (RelativeLayout) findViewById(R.id.notification_layout);
//		notifity_txt = (TextView) findViewById(R.id.notifity_txt);
		notivfity_number = (LinearLayout) findViewById(R.id.notivfity_number);
		atme_layout = (LinearLayout) findViewById(R.id.atme_layout);
		comments_number = (LinearLayout) findViewById(R.id.comments_number);
		message_number = (LinearLayout) findViewById(R.id.message_number);
//		atme_text = (TextView) findViewById(R.id.atme_text);
//		comments_txt = (TextView) findViewById(R.id.comments_txt);
//		message_txt = (TextView) findViewById(R.id.message_txt);
		
		me_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MessageCenterActivity.this,CommentActivity.class);
				intent.putExtra("title", "提到我");
				startActivity(intent);
			}
		});
		
		comment_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MessageCenterActivity.this,CommentActivity.class);
				intent.putExtra("title", "评论");
				startActivity(intent);
			}
		});

		message_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MessageCenterActivity.this,CommentActivity.class);
				intent.putExtra("title", "私信");
				startActivity(intent);
			}
		});
		
		notification_layout.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
					Intent intent = new Intent(MessageCenterActivity.this,NotificationMainActivity.class);
					startActivity(intent);
			}
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getInfo();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_message_center));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_message_center));
	}
	
	private void getInfo(){
		WebRequestHelper.get(URLText.Notification_num_URL, RequestParamsPool
		.getFollowParams(LoginUser.getpin()),true,
		new MyAsyncHttpResponseHandler(MessageCenterActivity.this,true) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				Toast.makeText(MessageCenterActivity.this,
						getString(R.string.network_connect_error),
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onResponse(int statusCode, Header[] headers,
					byte[] responseBody) {

				String result = new String(responseBody);

				Log.d("cj", "result=======>>" + result);
				try {
					JSONObject jsonObject = new JSONObject(result);
					if (jsonObject != null) {
						atme_count = jsonObject.optString("atme_count");
						alert_count = jsonObject.optString("alert_count");
						comments_count = jsonObject.optString("comments_count");
						message_count = jsonObject.optString("messages_count");
						mHandler.sendMessage(mHandler.obtainMessage(0));
					}
				} catch (JSONException e1) {
					e1.printStackTrace();
				}
				

			}
		});
	}
	
	private void setContent(){
		if (Integer.parseInt(atme_count) == 0) {
			atme_layout.setVisibility(View.GONE);
		}else {
			atme_layout.setVisibility(View.VISIBLE);
		}
		
		// 计算未读的系统消息数量
		List<JDMessage> jdMessages = LocalUserSetting.getJDMessageList(this);
		int count = 0;
		for (int i = 0; i < jdMessages.size(); i++) {
			if (!jdMessages.get(i).isShow)
				count++;
		}
		if (Integer.parseInt(alert_count)+ count  == 0) {
			notivfity_number.setVisibility(View.GONE);
		}else {
			notivfity_number.setVisibility(View.VISIBLE);
		}
		
		
		if (Integer.parseInt(message_count)== 0) {
			message_number.setVisibility(View.GONE);
		}else {
			message_number.setVisibility(View.VISIBLE);
		}
		
		if (Integer.parseInt(comments_count) == 0) {
			comments_number.setVisibility(View.GONE);
		}else {
			comments_number.setVisibility(View.VISIBLE);
		}
	}
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				Notification.getInstance().setAtMeCount(0);
				Notification.getInstance().setAlertsCount(0);
				Notification.getInstance().setMessagesCount(0);
				Notification.getInstance().setCommentsCount(0);
				Notification.getInstance().setFollowersCount(0);
				setContent();
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	public void onLeftMenuClick() {
		Log.d("cj", "finish=============>>");
		clear();
		finish();
	}
	
	private void clear(){
		WebRequestHelper.get(URLText.Clear_Message, RequestParamsPool
		.getFollowParams(LoginUser.getpin()),true,
		new MyAsyncHttpResponseHandler(MessageCenterActivity.this,true) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				Toast.makeText(MessageCenterActivity.this,
						getString(R.string.network_connect_error),
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onResponse(int statusCode, Header[] headers,
					byte[] responseBody) {

				String result = new String(responseBody);

				Log.d("cj", "resultsssssss=======>>" + result);
			
			}
		});
	}
}
