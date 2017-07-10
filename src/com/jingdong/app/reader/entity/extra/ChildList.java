package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;

public class ChildList implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1428670246930615289L;
	private long amount;
	private long catId;
	private String catName;
	private int catType;
	private int isLeaf;
	private String image;
	
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public long getAmount() {
		return amount;
	}
	public void setAmount(long amount) {
		this.amount = amount;
	}
	public long getCatId() {
		return catId;
	}
	public void setCatId(long catId) {
		this.catId = catId;
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
}
