package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;
import java.util.List;

public class CategoryList implements Serializable{

	private static final long serialVersionUID = -5137250594635191619L;
	private long catId;
	private long amount;
	private String catName;
	private int catType;
	private int isLeaf;
	private List<ChildList> childList;
	private String shortName;
	
	
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public long getCatId() {
		return catId;
	}
	public void setCatId(long catId) {
		this.catId = catId;
	}
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}
	public String getCatName() {
		return catName;
	}
	public void setCatName(String catName) {
		this.catName = catName;
	}
	public int getCatType() {
		return catType;
	}
	public void setCatType(int catType) {
		this.catType = catType;
	}
	public int getIsLeaf() {
		return isLeaf;
	}
	public void setIsLeaf(int isLeaf) {
		this.isLeaf = isLeaf;
	}
	public List<ChildList> getChildList() {
		return childList;
	}
	public void setChildList(List<ChildList> childList) {
		this.childList = childList;
	}
}
