package com.android.mzbook.sortview.model;

import java.util.ArrayList;
import java.util.List;
/**
 * 书架拖动布局
 * @author WANGGUODONG 
 * time:2014 -7 17
 */
public class SelectedModel {
	
	private int id;//bookshelf_id
	private String type;//[book]/[folder]
	private List<BookShelfModel> list =new ArrayList<BookShelfModel>();//data
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<BookShelfModel> getList() {
		return list;
	}
	public void setList(List<BookShelfModel> list) {
		this.list = list;
	}
}
