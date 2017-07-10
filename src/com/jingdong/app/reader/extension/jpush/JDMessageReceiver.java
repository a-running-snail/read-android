package com.jingdong.app.reader.extension.jpush;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import cn.jpush.android.api.CustomPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.data.JPushLocalNotification;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.BookStoreBookListActivity;
import com.jingdong.app.reader.activity.DraftsActivity;
import com.jingdong.app.reader.activity.LauncherActivity;
import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.activity.DraftsActivity.Draft;
import com.jingdong.app.reader.activity.DraftsActivity.Drafts;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.me.activity.IntegrationActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;

public class JDMessageReceiver extends BroadcastReceiver {

	private static final String TAG = "JPush";

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
		if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
			String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
			// Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
			// send the Registration Id to your server...

		} else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
			// Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() +
			// ", extras: " + printBundle(bundle));
			JPushInterface.reportNotificationOpened(context, bundle.getString(JPushInterface.EXTRA_MSG_ID));
			processCustomMessage(context, bundle);

		} else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
			Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
			int notifactionId = bundle.getInt(JPushInterface.EXTRA_NOTIFICATION_ID);
			Log.d(TAG, "[MyReceiver] 接收到推送下来的通知的ID: " + notifactionId);

			//保存推送消息
			List<JDMessage> list = LocalUserSetting.getJDMessageList(context);
			JDMessage message=new JDMessage();
			message.messageId = bundle.getString(JPushInterface.EXTRA_MSG_ID);
			message.alerMessage = bundle.getString(JPushInterface.EXTRA_ALERT);
			message.content = bundle.getString(JPushInterface.EXTRA_EXTRA);
			message.time = new Date().getTime();
			message.isShow=false;
			if(list.size()==15)
				list.remove(14);
			list.add(0,message);
			JDMessages jDMessages = new JDMessages();
			jDMessages.messages = list;
			LocalUserSetting.saveJDMessageList(context, jDMessages);
			
		} else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
			try {

				Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
//				Bundle[{cn.jpush.android.PUSH_ID=314164511,
//						cn.jpush.android.NOTIFICATION_TYPE=0,
//						app=com.jingdong.app.reader, 
//						cn.jpush.android.ALERT=特价| 中信出版社畅销书“京”选特价，11.11封顶2, 
//						cn.jpush.android.EXTRA={"lx":"0"}, 
//						cn.jpush.android.NOTIFICATION_ID=314164511, 
//						cn.jpush.android.NOTIFICATION_CONTENT_TITLE=京东阅读, 
//						cn.jpush.android.MSG_ID=314164511}]

				//设置该消息已经被查看过
				List<JDMessage> list = LocalUserSetting.getJDMessageList(context);
				if(list!=null){
					JDMessage message=new JDMessage();
					for (int i = 0; i < list.size(); i++) {
						message=list.get(i);
						if(message.messageId.equals(bundle.getString(JPushInterface.EXTRA_MSG_ID))){
							message.isShow=true;
							break;
						}
					}
					JDMessages jDMessages = new JDMessages();
					jDMessages.messages = list;
					LocalUserSetting.saveJDMessageList(context, jDMessages);
				}
				
				
				Bundle dataBundle = intent.getExtras();
				// JPushInterface.reportNotificationOpened(context,bundle.getString(JPushInterface.EXTRA_MSG_ID));
				String content = dataBundle.getString(JPushInterface.EXTRA_EXTRA);
				Log.d(TAG, "[MyReceiver] 用户点击打开了通知=" + printBundle(dataBundle));
				// if(content==null){
				// content=dataBundle.getString(JPushInterface.EXTRA_MESSAGE);
				//
				// }
				Log.d(TAG, "[MyReceiver] 用户点击打开了通知 content=" + content);
				intent = doPushBusses(context, content);
				
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
				intent = new Intent(context, LauncherActivity.class);
				intent.putExtra("lx", 0);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				context.startActivity(intent);

			}
		} else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
			Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: " + bundle.getString(JPushInterface.EXTRA_EXTRA));
			// 在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity，
			// 打开一个网页等..

		} else if (JPushInterface.ACTION_CONNECTION_CHANGE.equals(intent.getAction())) {
			boolean connected = intent.getBooleanExtra(JPushInterface.EXTRA_CONNECTION_CHANGE, false);
			Log.w(TAG, "[MyReceiver]" + intent.getAction() + " connected state change to " + connected);
		} else {
			Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
		}
	}

	// 打印所有的 intent extra 数据
	private static String printBundle(Bundle bundle) {
		StringBuilder sb = new StringBuilder();
		for (String key : bundle.keySet()) {
			if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getInt(key));
			} else if (key.equals(JPushInterface.EXTRA_CONNECTION_CHANGE)) {
				sb.append("\nkey:" + key + ", value:" + bundle.getBoolean(key));
			} else {
				sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
			}
		}
		String msg = JPushInterface.EXTRA_EXTRA;
		return sb.toString();
	}

	// //send msg to MainActivity
	// private void processCustomMessage(Context context, Bundle bundle) {
	// if (MainActivity.isForeground) {
	// String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
	// String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
	// Intent msgIntent = new Intent(MainActivity.MESSAGE_RECEIVED_ACTION);
	// msgIntent.putExtra(MainActivity.KEY_MESSAGE, message);
	// if (!ExampleUtil.isEmpty(extras)) {
	// try {
	// JSONObject extraJson = new JSONObject(extras);
	// if (null != extraJson && extraJson.length() > 0) {
	// msgIntent.putExtra(MainActivity.KEY_EXTRAS, extras);
	// }
	// } catch (JSONException e) {
	//
	// }
	//
	// }
	// context.sendBroadcast(msgIntent);
	// }
	// }

	/**
	 * @param content
	 * @return
	 * @throws JSONException
	 */
	private Intent doPushBusses(Context context, String content) throws Exception {
		JSONObject jObject = new JSONObject(content);
		Log.d(TAG, "jObject=" + content);
		if (jObject.has("lx")) {
			Intent intent = null;
			String lxStr = jObject.getString("lx");
			Log.d(TAG, "lxStr=" + lxStr);
			int lxInt = Integer.parseInt(lxStr);
			switch (lxInt) {
			case 0:// 书城
				Log.d(TAG, "jObject.has(\"showname\")=" + jObject.has("showname"));
				intent = new Intent(context, LauncherActivity.class);
				if (jObject.has("showname")) {

					int relationType = Integer.parseInt(jObject.getString("relatetype"));
					if (relationType == 1) {
						if (!isBackground(context)) {
							intent = new Intent(context, BookStoreBookListActivity.class);
						}
						intent.putExtra("lx", 0);
//						Bundle b = new Bundle();
//						b.putInt("fid", jObject.optInt("fid"));
//						b.putInt("ftype", jObject.optInt("ftype"));
//						b.putInt("relationType", relationType);
//						b.putString("showname", jObject.optString("showname"));
//						b.putInt("list_type", jObject.optInt("list_type"));
//						b.putString("bannerImg", jObject.optString("bannerimg"));
//						b.putBoolean("boolNew", jObject.optBoolean("boolnew"));
//						intent.putExtras(b);
						 intent =new
						 Intent(MZBookApplication.getContext(),BookStoreBookListActivity.class);
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
							if (!isBackground(context)) {
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
					id = Long.parseLong(jObject.optString("bookid").trim());
				}
				intent.putExtra("bookid", id);
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

	private void processCustomMessage(Context context, Bundle bundle) {
		String title = bundle.getString(JPushInterface.EXTRA_TITLE);
		String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
		String extuend = bundle.getString("cn.jpush.android.EXTRA");
		if (TextUtils.isEmpty(title)) {
			MZLog.d(TAG, "Unexpected: empty title (friend). Give up");
			title = context.getResources().getString(R.string.app_name);
		}
		CustomPushNotificationBuilder builder = new CustomPushNotificationBuilder(context, R.layout.customer_notitfication_layout, R.id.icon, R.id.title,
				R.id.text);
		// 指定定制的 Notification Layout
		builder.statusBarDrawable = R.drawable.icon;
		// 指定最顶层状态栏小图标
		builder.layoutIconDrawable = R.drawable.icon;
		builder.notificationFlags = Notification.FLAG_AUTO_CANCEL; // 设置为自动消失
		builder.notificationDefaults = Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS; // 设置为铃声与震动都要
		JPushInterface.setPushNotificationBuilder(2, builder);
		JPushLocalNotification jf = new JPushLocalNotification();
		jf.setContent(message);
		jf.setExtras(extuend);
		jf.setTitle(title);
		JPushInterface.addLocalNotification(context, jf);
	}

	public static boolean isBackground(Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (topActivity.getPackageName().equals(context.getPackageName())) {
				return false;
			}
		}
		return true;
	}
	
	public static class JDMessage implements Serializable {

		public String messageId;
		public String content;
		public String alerMessage;
		public long time;
		public boolean isShow;
	}

	public static class JDMessages {
		public List<JDMessage> messages = null;
	}
}
