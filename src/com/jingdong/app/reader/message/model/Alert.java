package com.jingdong.app.reader.message.model;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;

import com.jingdong.app.reader.timeline.model.core.Entity;

public class Alert extends Entity {
	public static final Creator<Alert> CREATOR=new Creator<Alert>() {

		@Override
		public Alert createFromParcel(Parcel source) {
			return new Alert(source);
		}

		@Override
		public Alert[] newArray(int size) {
			return new Alert[size];
		}
	};
	private final static String ID = "id";
	private final static String TEXT = "text";
	private final static String LINK = "link";
	private final static String TIME = "created_at_timestamp";
	private final static String JSON = ".json";
	public final static String ENTITY_LINK = "/entities/s/";
	public final static String USER_LINK = "/users/";
	private long id;
	private String text = "";
	private String link = "";
	private String linkType = "";
	private long timeStamp;

	public Alert(){

	}

	protected Alert(Parcel source){
		super(source);
		id=source.readLong();
		timeStamp=source.readLong();
		text=source.readString();
		link=source.readString();
		linkType=source.readString();
	}

	public void parseJson(JSONObject jObject, boolean topEntity) throws JSONException {
		id = jObject.getLong(ID);
		text = jObject.getString(TEXT);
		String fullLink = jObject.getString(LINK);
		if (fullLink.startsWith(ENTITY_LINK)) {
			link = fullLink.substring(fullLink.lastIndexOf('/') + 1, fullLink.length() - JSON.length());
			linkType = ENTITY_LINK;
		} else if (fullLink.startsWith(USER_LINK)) {
			link = fullLink.substring(fullLink.lastIndexOf('/') + 1, fullLink.length() - JSON.length());
			linkType = USER_LINK;
		} else {
			throw new IllegalArgumentException("Illegal type of " + fullLink);
		}
		super.setGuid(link);
		timeStamp = jObject.getLong(TIME);
	}

	@Override
	public long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String getLink() {
		return link;
	}

	public String getLinkType() {
		return linkType;
	}

	@Override
	public long getTimeStamp() {
		return timeStamp;
	}

	@Override
	protected void setId(long id) {
		this.id = id;
	}

	void setText(String text) {
		this.text = text;
	}

	void setLink(String link) {
		this.link = link;
	}

	@Override
	protected void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Override
	public String toString() {
		return text;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Alert && ((Alert) o).getId() == id;
	}

	@Override
	public int hashCode() {
		return Long.valueOf(id).hashCode();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeLong(id);
		dest.writeLong(timeStamp);
		dest.writeString(text);
		dest.writeString(link);
		dest.writeString(linkType);
	}
}
