package com.jingdong.app.reader.me.model;

import java.util.HashMap;

import android.content.Context;
import android.os.Message;

import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.user.LocalUserSetting;

public class UserFollower extends ObservableModel {
	public static final int FOLLOW = 1345;
	private Context context;
	private boolean following;
	private boolean isFans;

	public UserFollower(Context context, boolean following, boolean isFans) {
		this.context = context;
		this.following = following;
		this.isFans = isFans;
	}

	public void followUser(String userId) {
		String baseUrl;
		if (following)
			baseUrl = URLText.followCertainUser + userId + ".json";
		else
			baseUrl = URLText.unFollowCertainUser + userId + ".json";
		HashMap<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		String url = URLBuilder.addParameter(baseUrl, paramMap);
		String jsonString = WebRequest.postWebDataWithContext(context, url, "");
		boolean result = parsePostResult(jsonString);
		notifyDataChanged(FOLLOW, result, userId);
	}

	public void setFollowing(boolean followed) {
		following = followed;
	}

	public boolean isFollowing() {
		return following;
	}
	
	public void setFans(boolean isFans) {
		this.isFans = isFans;
	}
	
	public boolean isFans() {
		return isFans;
	}

	private void notifyDataChanged(int type, boolean success, String userId) {
		Message message = Message.obtain();
		message.what = type;
		message.arg1 = success ? SUCCESS_INT : FAIL_INT;
		message.obj = userId;
		setChanged();
		notifyObservers(message);
	}
}
