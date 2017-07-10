package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;

public class StoreBook implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String author;
	public long ebookId;
	public double fileSize;
	public String format;
	public int good;
	public String imageUrl;
	public String info;
	public boolean isAlreadyBuy;
	public boolean isBorrow;
	public boolean isBuy;
	public boolean isEBook;
	public boolean isFluentRead;
	public boolean isFree;
	public double jdPrice;
	public String largeImageUrl;
	public String name;
	public double price;
	public String priceMessage;
	public String publisher;
	public int star;
	
	public String picUrl;//分类列表返回数据才有
	public String bookContent;//分类列表返回数据才有
	public long bookId;//分类列表返回数据才有
	
	
	public String categoryPath;//借阅列表才有的字段
	public long orderId;//借阅列表才有的字段
	public long paperBookId;//借阅列表才有的字段
	public String borrowStartTime;/*借阅开始时间*///借阅列表才有的字段
	public String borrowEndTime;/*借阅结束时间*///借阅列表才有的字段
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public long getEbookId() {
		return ebookId;
	}
	public void setEbookId(long ebookId) {
		this.ebookId = ebookId;
	}
	public double getFileSize() {
		return fileSize;
	}
	public void setFileSize(double fileSize) {
		this.fileSize = fileSize;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public int getGood() {
		return good;
	}
	public void setGood(int good) {
		this.good = good;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	public boolean isAlreadyBuy() {
		return isAlreadyBuy;
	}
	public void setAlreadyBuy(boolean isAlreadyBuy) {
		this.isAlreadyBuy = isAlreadyBuy;
	}
	public boolean isBorrow() {
		return isBorrow;
	}
	public void setBorrow(boolean isBorrow) {
		this.isBorrow = isBorrow;
	}
	public boolean isBuy() {
		return isBuy;
	}
	public void setBuy(boolean isBuy) {
		this.isBuy = isBuy;
	}
	public boolean isEBook() {
		return isEBook;
	}
	public void setEBook(boolean isEBook) {
		this.isEBook = isEBook;
	}
	public boolean isFluentRead() {
		return isFluentRead;
	}
	public void setFluentRead(boolean isFluentRead) {
		this.isFluentRead = isFluentRead;
	}
	public boolean isFree() {
		return isFree;
	}
	public void setFree(boolean isFree) {
		this.isFree = isFree;
	}
	public double getJdPrice() {
		return jdPrice;
	}
	public void setJdPrice(double jdPrice) {
		this.jdPrice = jdPrice;
	}
	public String getLargeImageUrl() {
		return largeImageUrl;
	}
	public void setLargeImageUrl(String largeImageUrl) {
		this.largeImageUrl = largeImageUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String getPriceMessage() {
		return priceMessage;
	}
	public void setPriceMessage(String priceMessage) {
		this.priceMessage = priceMessage;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}
	public int getStar() {
		return star;
	}
	public void setStar(int star) {
		this.star = star;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public String getBookContent() {
		return bookContent;
	}
	public void setBookContent(String bookContent) {
		this.bookContent = bookContent;
	}
	public long getBookId() {
		return bookId;
	}
	public void setBookId(long bookId) {
		this.bookId = bookId;
	}
	public String getCategoryPath() {
		return categoryPath;
	}
	public void setCategoryPath(String categoryPath) {
		this.categoryPath = categoryPath;
	}
	public long getOrderId() {
		return orderId;
	}
	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	public long getPaperBookId() {
		return paperBookId;
	}
	public void setPaperBookId(long paperBookId) {
		this.paperBookId = paperBookId;
	}
	public String getBorrowStartTime() {
		return borrowStartTime;
	}
	public void setBorrowStartTime(String borrowStartTime) {
		this.borrowStartTime = borrowStartTime;
	}
	public String getBorrowEndTime() {
		return borrowEndTime;
	}
	public void setBorrowEndTime(String borrowEndTime) {
		this.borrowEndTime = borrowEndTime;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
