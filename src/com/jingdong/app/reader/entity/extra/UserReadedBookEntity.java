package com.jingdong.app.reader.entity.extra;

import java.util.List;

import android.R.integer;

public class UserReadedBookEntity {

	private String code;
	private int totalCount;
	private List<UserReadedBook> resultList;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getTotalCount() {
		return totalCount;
	}
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}
	public List<UserReadedBook> getResultList() {
		return resultList;
	}
	public void setResultList(List<UserReadedBook> resultList) {
		this.resultList = resultList;
	}
	
	
}
