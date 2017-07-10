package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;

public class AlertItem implements Serializable{

	private String id;
	private int is_read;
	private String text;
	private String link;
	private String created_at;
	private String created_at_timestamp;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getIs_read() {
		return is_read;
	}
	public void setIs_read(int is_read) {
		this.is_read = is_read;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
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

	
}
