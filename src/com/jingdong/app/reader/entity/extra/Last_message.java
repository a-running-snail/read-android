package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;

public class Last_message implements Serializable{

	private String created_at;
	private String created_at_timestamp;
	private String body;

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getCreated_at_timestamp() {
		return created_at_timestamp;
	}

	public void setCreated_at_timestamp(String created_at_timestamp) {
		this.created_at_timestamp = created_at_timestamp;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
	
}
