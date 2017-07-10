package com.jingdong.app.reader.entity.extra;

import java.util.List;

public class SearchKeyWord {

	private String title;
	private List<String> data = null;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getData() {
		return data;
	}

	public void setData(List<String> data) {
		this.data = data;
	}

}
