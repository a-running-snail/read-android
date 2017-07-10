package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;
import com.jingdong.app.reader.user.UserInfo;

public class ConversationItem implements Serializable{

	private String id;
	private UserInfo user;
	private int has_new;
	private Last_message last_message;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getHas_new() {
		return has_new;
	}

	public void setHas_new(int has_new) {
		this.has_new = has_new;
	}

	public Last_message getLast_message() {
		return last_message;
	}

	public void setLast_message(Last_message last_message) {
		this.last_message = last_message;
	}

	public UserInfo getUser() {
		return user;
	}

	public void setUser(UserInfo user) {
		this.user = user;
	}
	
}
