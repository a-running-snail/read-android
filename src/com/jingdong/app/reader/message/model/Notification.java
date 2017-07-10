package com.jingdong.app.reader.message.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.lib.zxing.client.android.LocaleManager;

public class Notification {
	private static final String NORMAL_COUNT = "normal_count";
	private static final String BOOK_COUNT = "book_count";
	private static final String ALERT_COUNT = "alert_count";
	private static final String GENERAL_ALERTS_COUNT = "general_alerts_count";
	private static final String ATME_COUNT = "atme_count";
	private static final String COMMENTS_COUNT = "comments_count";
	private static final String MESSAGES_COUNT = "messages_count";
	private static final String FOLLOWERS_COUNT = "followers_count";
	private int normalCount;
	private int bookCount;
	private int alertsCount;
	private int generalAlertsCount;
	private int atMeCount;
	private int commentsCount;
	private int messagesCount;
	private int followersCount;
	private volatile static Notification uniqueInstance;
	private int meCount;
	private int messageCenterCount;
	
	private int jdmessageCount;//系统推送信息
	private int borrowMessageCount; //用户借阅通知信息及购书赠书信息

	/**
	 * 当前类实现了单例模式，所以构造被禁用了，可以通过getInstance获取当前类的唯一实例
	 */
	private Notification() {

	}

	/**
	 * 获取当前类的实例
	 * 
	 * @return 当前类的一个实例
	 */
	public static Notification getInstance() {
		if (uniqueInstance == null) {
			synchronized (Notification.class) {
				if (uniqueInstance == null) {
					Log.d("cj", "this================>>>>>>>>>>");
					uniqueInstance = new Notification();
				}
			}
		}
		return uniqueInstance;
	}

	public boolean parseJson(String jsonString) {
		boolean result = false;
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			setNormalCount(jsonObject.optInt(NORMAL_COUNT));
			setBookCount(jsonObject.optInt(BOOK_COUNT));
			setAlertsCount(jsonObject.optInt(ALERT_COUNT));
			setGeneralAlertsCount(jsonObject.optInt(GENERAL_ALERTS_COUNT));
			setAtMeCount(jsonObject.optInt(ATME_COUNT));
			setCommentsCount(jsonObject.optInt(COMMENTS_COUNT));
			setMessagesCount(jsonObject.optInt(MESSAGES_COUNT));
			setFollowersCount(jsonObject.optInt(FOLLOWERS_COUNT));
			setJdmessageCount();
			setMeCount(atMeCount + commentsCount + messagesCount + alertsCount + followersCount + jdmessageCount+ borrowMessageCount);
			setMessageCenterCount(atMeCount + commentsCount + messagesCount + alertsCount + jdmessageCount+ borrowMessageCount);
			result = true;
		} catch (JSONException e) {
			if (jsonString.isEmpty())
				MZLog.e("Notification", "jsonString is empty");
			else
				MZLog.e("Notification", jsonString);
			MZLog.e("Notification", Log.getStackTraceString(e));
			result = true;
		}
		return result;
	}

	public int getMESum(){
		return atMeCount + commentsCount + messagesCount + alertsCount + followersCount + jdmessageCount + borrowMessageCount;
	}
	
	public int getMessageCenterSum(){
		return atMeCount + commentsCount + messagesCount + alertsCount + jdmessageCount +borrowMessageCount;
	}
	
	public int getSum() {
		return normalCount + alertsCount + atMeCount + commentsCount + jdmessageCount +borrowMessageCount;
	}
	
	public int getMessageSum() {
		return alertsCount + atMeCount + commentsCount + messagesCount + jdmessageCount +borrowMessageCount;
	}

	public  int getNormalCount() {
		return normalCount;
	}

	public synchronized int getBookCount() {
		return bookCount;
	}

	public synchronized int getAlertsCount() {
		return alertsCount;
	}

	public synchronized int getGeneralAlertsCount() {
		return generalAlertsCount;
	}

	public synchronized int getAtMeCount() {
		return atMeCount;
	}

	public synchronized int getCommentsCount() {
		return commentsCount;
	}

	public synchronized int getMessagesCount() {
		return messagesCount;
	}

	public synchronized int getFollowersCount() {
		return followersCount;
	}

	public synchronized void clear() {
		normalCount = 0;
		bookCount = 0;
		alertsCount = 0;
		generalAlertsCount = 0;
		atMeCount = 0;
		commentsCount = 0;
		messagesCount = 0;
		followersCount = 0;
		jdmessageCount = 0;
		borrowMessageCount = 0;
	}

	public int getBorrowMessageCount() {
		return borrowMessageCount;
	}

	public void setBorrowMessageCount(int borrowMessageCount) {
		this.borrowMessageCount = borrowMessageCount;
	}

	public void setJdmessageCount() {
		this.jdmessageCount = LocalUserSetting.getJDMessageList(MZBookApplication.getContext()).size();
	}

	public synchronized void setNormalCount(int normalCount) {
		this.normalCount = normalCount;
	}

	public synchronized void setBookCount(int bookCount) {
		this.bookCount = bookCount;

	}

	public synchronized void setMeCount(int meCount) {
		this.meCount = meCount;
	}

	public synchronized void setMessageCenterCount(int messageCenterCount) {
		this.messageCenterCount = messageCenterCount;
	}
	
	public void setAlertsCount(int alertsCount) {
		this.alertsCount = alertsCount;
	}

	public synchronized void setGeneralAlertsCount(int generalAlertsCount) {
		this.generalAlertsCount = generalAlertsCount;
	}

	public void setAtMeCount(int atMeCount) {
		this.atMeCount = atMeCount;

	}

	public void setCommentsCount(int commentsCount) {
		this.commentsCount = commentsCount;
	}

	public void setMessagesCount(int messagesCount) {
		this.messagesCount = messagesCount;
	}

	public void setFollowersCount(int followersCount) {
		this.followersCount = followersCount;
	}

	public synchronized void setReadNormalCount(Context context) {
		normalCount = 0;
		sendBroadCast(context);
	}
	
	public synchronized void setMESum(Context context) {
		meCount = 0;
		sendBroadCast(context);
	}

	public synchronized void setReadBookCount(Context context) {
		bookCount = 0;
		sendBroadCast(context);
	}

	public synchronized void setReadAlertsCount(Context context) {
		alertsCount = 0;
		sendBroadCast(context);
	}

	public synchronized void setReadGeneralAlertsCount(Context context) {
		generalAlertsCount = 0;
		sendBroadCast(context);
	}

	public synchronized void setReadAtMeCount(Context context) {
		atMeCount = 0;
		sendBroadCast(context);
	}

	public synchronized void setReadCommentsCount(Context context) {
		commentsCount = 0;
		sendBroadCast(context);
	}

	public synchronized void setReadMessagesCount(Context context) {
		messagesCount = 0;
		sendBroadCast(context);
	}

	public synchronized void setReadFollowersCount(Context context) {
		followersCount = 0;
		sendBroadCast(context);
	}
	
	public synchronized void setMessageCenterCount(Context context) {
		messageCenterCount = 0;
		sendBroadCast(context);
	}

	private void sendBroadCast(Context context) {
		Intent intent = new Intent(NotificationService.NOTIFICATION_ACTION);
		LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(context);
		broadcastManager.sendBroadcast(intent);
	}
}
