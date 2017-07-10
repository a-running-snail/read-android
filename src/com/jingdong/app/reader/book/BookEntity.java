package com.jingdong.app.reader.book;

import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class BookEntity implements Parcelable {
	public static final Creator<BookEntity> CREATOR = new Creator<BookEntity>() {

		@Override
		public BookEntity[] newArray(int size) {
			return new BookEntity[size];
		}

		@Override
		public BookEntity createFromParcel(Parcel source) {
			return new BookEntity(source);
		}
	};
	public int entityId;
	public int edition;
	public String updateTime;
	public long fileSize;

	private BookEntity(Parcel source) {
		entityId=source.readInt();
		edition=source.readInt();
		updateTime=source.readString();
		fileSize=source.readLong();
	}

	public BookEntity() {

	}

	public static String getK() {
		byte[] b = { 85, 68, 99, 110, 93, 118, 49, 112, 117, 44, 52, 53, 29, 109, 38, 75 };
		for (int i = 0; i < b.length; ++i) {
			b[i] = (byte) (b[i] + i);
		}
		String str = new String(b);
		return str;
	}

	public static BookEntity fromJSON(JSONObject json) {
		BookEntity bookEntity = new BookEntity();

		bookEntity.entityId = json.optInt("id");
		bookEntity.edition = json.optInt("edition", -1);
		bookEntity.updateTime = json.optString("updated_at");
		String fileSizeText = json.optString("size");
		if (!TextUtils.isEmpty(fileSizeText)) {
			try {
				bookEntity.fileSize = Long.parseLong(fileSizeText);
			} catch (NumberFormatException e) {

			}
		}
		return bookEntity;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(entityId);
		dest.writeInt(edition);
		dest.writeString(updateTime);
		dest.writeLong(fileSize);
	}
}
