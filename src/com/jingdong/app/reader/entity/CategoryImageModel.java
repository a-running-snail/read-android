package com.jingdong.app.reader.entity;

public class CategoryImageModel {

	private String image;
	private int catId;
	
	public CategoryImageModel(String image, int catId) {
		super();
		this.image = image;
		this.catId = catId;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public int getCatId() {
		return catId;
	}
	public void setCatId(int catId) {
		this.catId = catId;
	}
	
	
}
