
package com.jingdong.app.reader.book;

import java.io.Serializable;

import org.json.JSONObject;

import android.text.TextUtils;

public class SerializableBookEntity implements Serializable {

	public int entityId;
	public int edition;
	public String updateTime;
	public long fileSize;


	public static String getK() {
		byte[] b = { 85, 68, 99, 110, 93, 118, 49, 112, 117, 44, 52, 53, 29, 109, 38, 75 };
		for (int i = 0; i < b.length; ++i) {
			b[i] = (byte) (b[i] + i);
		}
		String str = new String(b);
		return str;
	}

	public static SerializableBookEntity fromJSON(JSONObject json) {
		SerializableBookEntity bookEntity = new SerializableBookEntity();

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


}
