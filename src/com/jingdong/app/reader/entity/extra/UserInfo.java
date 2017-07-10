package com.jingdong.app.reader.entity.extra;

import java.util.List;

public class UserInfo {

	private String code;
	private List<UserList> list;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public List<UserList> getList() {
		return list;
	}
	public void setList(List<UserList> list) {
		this.list = list;
	}
}
