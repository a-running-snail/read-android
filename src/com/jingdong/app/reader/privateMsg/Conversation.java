package com.jingdong.app.reader.privateMsg;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.jingdong.app.reader.user.UserInfo;

public class Conversation implements Parcelable {
	public final static Creator<Conversation> CREATOR = new Creator<Conversation>() {

		@Override
		public Conversation createFromParcel(Parcel source) {
			return new Conversation(source);
		}

		@Override
		public Conversation[] newArray(int size) {
			return new Conversation[size];
		}
	};

	private final static String ID = "id";
	private final static String USER = "user";
	private final static String HAS_NEW = "has_new";
	private final static String LAST_MESSAGE = "last_message";
	private long id;
	private UserInfo userInfo;
	private boolean hasNew;
	private LastMessage lastMessage;

	private Conversation() {

	}

	/**
	 * 这个构造只是为了创建一个id等于指定id的conversation的stub，用来进行删除等操作，其他时候最好不要调用这个构造。
	 * 
	 * @param id
	 *            指定的id
	 */
	public Conversation(long id) {
		this.id = id;
	}

	private Conversation(Parcel source) {
		id = source.readLong();
		userInfo = source.readParcelable(UserInfo.class.getClassLoader());
		hasNew = (source.readByte() == 0) ? false : true;
		lastMessage = source.readParcelable(LastMessage.class.getClassLoader());
	}

	public static final Conversation fromJson(JSONObject jsonObject) throws JSONException {
		Conversation conversation = new Conversation();
		conversation.id = jsonObject.optLong(ID);
		conversation.userInfo = new UserInfo();
		conversation.userInfo.parseJson(jsonObject.optJSONObject(USER));
		conversation.hasNew = (jsonObject.optInt(HAS_NEW) == 0 ? false : true);
		conversation.lastMessage = LastMessage.fromJson(jsonObject.optJSONObject(LAST_MESSAGE));
		return conversation;
	}

	public long getId() {
		return id;
	}

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public boolean isHasNew() {
		return hasNew;
	}

	public void setHasNew(boolean hasNew) {
		this.hasNew = hasNew;
	}

	public LastMessage getLastMessage() {
		return lastMessage;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Conversation && ((Conversation) o).id == id;
	}

	@Override
	public int hashCode() {
		return Long.valueOf(id).hashCode();
	}

	@Override
	public String toString() {
		return lastMessage.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeParcelable(userInfo, flags);
		dest.writeByte((byte) (hasNew ? 1 : 0));
		dest.writeParcelable(lastMessage, flags);
	}
}
