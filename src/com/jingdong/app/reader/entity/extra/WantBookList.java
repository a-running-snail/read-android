package com.jingdong.app.reader.entity.extra;

public class WantBookList {

	private Long bookId;
	private String title;
	private String author;
	private String imgUrl;
	private String largeSizeImgUrl;
	private double price;
	private boolean readcard;
	public Long getBookId() {
		return bookId;
	}
	public void setBookId(Long bookId) {
		this.bookId = bookId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getLargeSizeImgUrl() {
		return largeSizeImgUrl;
	}
	public void setLargeSizeImgUrl(String largeSizeImgUrl) {
		this.largeSizeImgUrl = largeSizeImgUrl;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public boolean isReadcard() {
		return readcard;
	}
	public void setReadcard(boolean readcard) {
		this.readcard = readcard;
	}
}
