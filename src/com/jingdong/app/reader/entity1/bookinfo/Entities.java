package com.jingdong.app.reader.entity1.bookinfo;

import org.json.JSONObject;


public class Entities {

	String id;
	String edition;
	String size;
	String updated_at;
	String updated_at_timestamp;

	public void fromJson(JSONObject object) {

		if (object != null) {

			setId(object.optString("id"));
			setEdition(object.optString("edition"));
			setSize(object.optString("size"));
			setUpdated_at(object.optString("updated_at"));
			setUpdated_at_timestamp(object.optString("updated_at_timestamp"));
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}

	public String getUpdated_at_timestamp() {
		return updated_at_timestamp;
	}

	public void setUpdated_at_timestamp(String updated_at_timestamp) {
		this.updated_at_timestamp = updated_at_timestamp;
	}

}
