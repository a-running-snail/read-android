package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;
import java.util.List;

public class JDCategoryBook implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String code;
	private List<CategoryList> catList;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<CategoryList> getCatList() {
		return catList;
	}

	public void setCatList(List<CategoryList> catList) {
		this.catList = catList;
	}
}
