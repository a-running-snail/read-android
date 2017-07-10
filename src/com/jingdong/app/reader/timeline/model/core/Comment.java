package com.jingdong.app.reader.timeline.model.core;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.ToastUtil;

public class Comment implements Parcelable{
	public static final Creator<Comment> CREATOR=new Creator<Comment>() {

		@Override
		public Comment createFromParcel(Parcel source) {
			return new Comment(source);
		}

		@Override
		public Comment[] newArray(int size) {
			return new Comment[size];
		}
	};
	private final static String ID = "id";
	private final static String CONTENT = "content";
	private final static String TIME_STAMP = "created_at_timestamp";
	private final static String TIME_STAMP2 = "created_at";
	private final static String USER = "user";
	private long id;
	private long timeStamp;
	private String content;
	private UserInfo user;

	private Comment(Parcel source){
		id=source.readLong();
		timeStamp=source.readLong();
		content=source.readString();
		user=source.readParcelable(UserInfo.class.getClassLoader());
	}

	public Comment() {
		user = new UserInfo();
	}

	/**
	 * 从JSONObject中取得数据，并将结果保存到Comment中
	 *
	 * @param jObject
	 *            数据源
	 * @throws JSONException
	 *             解析JSON时发生异常
	 */
	public void parseJson(JSONObject jObject) throws JSONException {
		if (jObject != null) {
			setId(jObject.getLong(ID));
			if (jObject.has(CONTENT)) {
				setContent(jObject.getString(CONTENT));
			}
			if (jObject.has(TIME_STAMP)) {
				setTimeStamp(jObject.getLong(TIME_STAMP));
			} else if (jObject.has(TIME_STAMP2)) {
				setTimeStamp(jObject.getLong(TIME_STAMP2));
			}
			getUser().parseJson(jObject.getJSONObject(USER));
		}
	}

	
	public boolean isCommentAuthor(Context context) {
		return user.getId() == LocalUserSetting.getUserId(context);
	}

	public String getContent() {
		return content;
	}

	public long getId() {
		return id;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public UserInfo getUser() {
		return user;
	}

	void setContent(String content) {
		this.content = content;
	}

	void setId(long id) {
		this.id = id;
	}

	void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	void setUser(UserInfo user) {
		this.user = user;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Comment && ((Comment) o).getId() == id;
	}

	@Override
	public int hashCode() {
		return Long.valueOf(id).hashCode();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(timeStamp);
		dest.writeString(content);
		dest.writeParcelable(user, flags);
	}
}
