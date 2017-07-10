package com.jingdong.app.reader.entity.extra;

import java.util.List;


public class FocusModel {
	
	private List<UsersList> users;
	private String unfollow_count;

	public List<UsersList> getUsers() {
		return users;
	}

	public void setUsers(List<UsersList> users) {
		this.users = users;
	}

	public String getUnfollow_count() {
		return unfollow_count;
	}

	public void setUnfollow_count(String unfollow_count) {
		this.unfollow_count = unfollow_count;
	}
}
