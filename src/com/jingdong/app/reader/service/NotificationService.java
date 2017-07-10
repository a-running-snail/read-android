package com.jingdong.app.reader.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.me.activity.MessageCenterActivity;
import com.jingdong.app.reader.message.model.Notification;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;

public class NotificationService extends Service {
	
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				getInfo(getApplicationContext());
				break;

			default:
				break;
			}
		}
		
	};
	
	public void getInfo(final Context context){

				WebRequestHelper.get(URLText.Notification_num_URL, RequestParamsPool
						.getFollowParams(LoginUser.getpin()),true,
						new MyAsyncHttpResponseHandler(context,true) {

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
								String jsonString = new String(responseBody);
								
								getBorrowMessage(context,jsonString);
								
//								Notification notification = Notification
//										.getInstance();
//								if (notification.parseJson(jsonString)) {
//									Intent intent = new Intent(
//											NOTIFICATION_ACTION);
//									LocalBroadcastManager broadcastManager = LocalBroadcastManager
//											.getInstance(context);
//									broadcastManager.sendBroadcast(intent);
//								}
							}
						});
	}
	
	/**
	 * 获取用户借阅通知，并更新各种消息数目
	 * 
	 * @param context
	 * @param jsonString
	 *            原来各种消息的数据
	 */
	public void getBorrowMessage(final Context context, final String jsonString) {

		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.reqUnReadGiftBag(), true,
				new MyAsyncHttpResponseHandler(context, true) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String json = new String(responseBody);
						try {
							Notification notification = Notification.getInstance();
							JSONObject object = new JSONObject(json);
							if (object != null && object.has("code") && object.optString("code").equals("0")) {
								JSONArray messages = object.optJSONArray("unreadMessage");
								JSONObject message;
								int messageCount = 0;
								for (int i = 0; i < messages.length(); i++) {
									message = messages.optJSONObject(i);
									if(message.optString("detailType").equals("4") || message.optString("detailType").equals("5"))//借阅通知及购书赠书通知
										messageCount++;
								}
								notification.setBorrowMessageCount(messageCount);
							}
							if (notification.parseJson(jsonString)) {
								Intent intent = new Intent(NOTIFICATION_ACTION);
								LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
								broadcastManager.sendBroadcast(intent);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}

	private class NotificationTask implements Runnable {
		private Context context;

		private NotificationTask(Context context) {
			this.context = context;
		}


		@Override
		public void run() {
			// Map<String, String> paramMap = new HashMap<String, String>();
			// paramMap.put(WebRequest.AUTH_TOKEN,
			// LocalUserSetting.getToken(context));
			// String url = URLBuilder.addParameter(URLText.notificationUrl,
			// paramMap);
			// String jsonString = WebRequest.getWebDataWithContext(context,
			// url);
			mHandler.sendMessage(mHandler.obtainMessage(0));
		}
	}

	/**
	 * 向服务器发送http get请求读取通知的间隔，以毫秒为单位。
	 */
	public final static long GET_NOTIFICATION_DELAY = 1000 * 6 * 60;
	public final static String NOTIFICATION_ACTION = "com.mzbook.notification.action.NOTIFICATION";
	private final static int THREAD_POOL_SIZE = 1;
	private static ScheduledExecutorService executor;
	private Future<? extends Object> future;
	private Context context;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		ScheduledExecutorService executor = getExecutorService();
		Log.d("cj", "onBind=========>>>>>>>");
		if (future == null || future.isCancelled())
			future = executor.scheduleWithFixedDelay(new NotificationTask(
					getApplicationContext()), 0, GET_NOTIFICATION_DELAY,
					TimeUnit.MILLISECONDS);
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}
	
	/**
	 * 获得当前Application所使用的线程池，如果线程池为空，则创建一个新的线程池
	 * 
	 * @return 一个线程池
	 */
	public static ScheduledExecutorService getExecutorService() {
		if (executor == null) {
			synchronized (NotificationService.class) {
				if (executor == null)
					executor = Executors
							.newScheduledThreadPool(NotificationService.THREAD_POOL_SIZE);
			}
		}
		return executor;
	}

}
