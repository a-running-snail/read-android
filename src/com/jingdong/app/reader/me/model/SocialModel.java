package com.jingdong.app.reader.me.model;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

public class SocialModel implements Parcelable {
	public static final Creator<SocialModel> CREATOR = new Creator<SocialModel>() {

		@Override
		public SocialModel createFromParcel(Parcel source) {
			return new SocialModel(source);
		}

		@Override
		public SocialModel[] newArray(int size) {
			return new SocialModel[size];
		}
	};
	private static final String ID = "id";
	private static final String UID = "uid";
	private static final String NAME = "name";
	private static final String SOURCE = "source";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String EXPIRES_AT = "expires_at";
	public static final String SINA = "sina";
	private long id;
	private long uid;
	private String name;
	private String source;
	private String access_token;
	private long expires_at;

	private SocialModel(Parcel source) {
		id = source.readLong();
		uid = source.readLong();
		name = source.readString();
		this.source = source.readString();
		access_token = source.readString();
		expires_at = source.readLong();
	}

	public SocialModel() {

	}

	public void parseJson(JSONObject jObject) {
		if (jObject != null) {
			id = jObject.optLong(ID);
			uid = jObject.optLong(UID);
			name = jObject.optString(NAME);
			source = jObject.optString(SOURCE);
			access_token = jObject.optString(ACCESS_TOKEN);
			expires_at = jObject.optLong(EXPIRES_AT);
		}
	}

	public long getId() {
		return id;
	}

	public long getUid() {
		return uid;
	}

	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	public String getAccess_token() {
		return access_token;
	}

	public long getExpires_at() {
		return expires_at;
	}

	void setId(long id) {
		this.id = id;
	}

	void setUid(long uid) {
		this.uid = uid;
	}

	void setName(String name) {
		this.name = name;
	}

	void setSource(String source) {
		this.source = source;
	}

	void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	void setExpires_at(long expires_at) {
		this.expires_at = expires_at;
	}

	@Override
	public String toString() {
		return Long.toString(uid);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SocialModel) {
			SocialModel socialModel = (SocialModel) o;
			if (socialModel.getUid() == uid) {
				if (socialModel.source != null && source != null) {
					if (socialModel.source.equals(source))
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Long.valueOf(uid).hashCode();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeLong(uid);
		dest.writeString(name);
		dest.writeString(source);
		dest.writeString(access_token);
		dest.writeLong(expires_at);
	}
}
