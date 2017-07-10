package com.jingdong.app.reader.entity1.bookinfo;

import org.json.JSONObject;

public class Publisher {

	String name;
	String url_name;

	public void fromJson(JSONObject object) {

		if (object != null) {

			setName(object.optString("name"));
			setUrl_name(object.optString("url_name"));
		}

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl_name() {
		return url_name;
	}

	public void setUrl_name(String url_name) {
		this.url_name = url_name;
	}

}
