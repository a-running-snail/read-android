package com.jingdong.app.reader.privateMsg;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class LastMessage implements Parcelable {
	public static final Creator<LastMessage> CREATOR = new Creator<LastMessage>() {

		@Override
		public LastMessage[] newArray(int size) {
			return new LastMessage[size];
		}

		@Override
		public LastMessage createFromParcel(Parcel source) {
			return new LastMessage(source);
		}
	};

	private final static String TIMESTAMP = "created_at_timestamp";
	private final static String BODY = "body";
	private long timeStamp;
	private String body;

	protected LastMessage() {

	}

	protected LastMessage(Parcel source) {
		timeStamp = source.readLong();
		body = source.readString();
	}

	public static LastMessage fromJson(JSONObject jsonObject) {
		LastMessage lastMessage = new LastMessage();
		paresJson(jsonObject, lastMessage);
		return lastMessage;
	}

	public void parseJson(JSONObject jsonObject) {
		paresJson(jsonObject, this);
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public String getBody() {
		return body;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof LastMessage && ((LastMessage) o).getBody() == getBody()
				&& ((LastMessage) o).getTimeStamp() == getTimeStamp();
	}

	@Override
	public int hashCode() {
		int result = 17;
		result = 37 * result + Long.valueOf(timeStamp).hashCode();
		result = 37 * result + body.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return body;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(timeStamp);
		dest.writeString(body);
	}

	private static void paresJson(JSONObject jsonObject, LastMessage lastMessage) {
		lastMessage.timeStamp = jsonObject.optLong(TIMESTAMP);
		lastMessage.body = jsonObject.optString(BODY);
	}
}
