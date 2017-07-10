package com.jingdong.app.reader.entity1.bookinfo;

import org.json.JSONObject;

public class Ebooks {
	String id;
	String cover;

	public void fromJson(JSONObject object) {

		if (object != null) {
			setId(object.optString("id"));
			setCover(object.optString("cover"));
		}

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

}