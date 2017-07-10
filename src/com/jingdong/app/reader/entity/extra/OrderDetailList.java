package com.jingdong.app.reader.entity.extra;

import android.R.integer;

public class OrderDetailList {

	private String author;
	private long bookId;
	private int bookType;
	private String bookTypeName;
	private int format;
	private String imgUrl;
	private String name;
	private long orderId;
	private int orderMode;
	private int orderStatus;
	private String orderStatusName;
	private String orderTime;
	private String price;
	private int sentStatus;
	private double size;
	
	private String acceptNickName;	
	private String acceptTime;
	
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public long getBookId() {
		return bookId;
	}
	public void setBookId(long bookId) {
		this.bookId = bookId;
	}
	public int getBookType() {
		return bookType;
	}
	public void setBookType(int bookType) {
		this.bookType = bookType;
	}
	public String getBookTypeName() {
		return bookTypeName;
	}
	public void setBookTypeName(String bookTypeName) {
		this.bookTypeName = bookTypeName;
	}
	public int getFormat() {
		return format;
	}
	public void setFormat(int format) {
		this.format = format;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getOrderId() {
		return orderId;
	}
	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	public int getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}
	public String getOrderStatusName() {
		return orderStatusName;
	}
	public void setOrderStatusName(String orderStatusName) {
		this.orderStatusName = orderStatusName;
	}
	public String getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public int getSentStatus() {
		return sentStatus;
	}
	public void setSentStatus(int sentStatus) {
		this.sentStatus = sentStatus;
	}
	public double getSize() {
		return size;
	}
	public void setSize(double size) {
		this.size = size;
	}
	public int getOrderMode() {
		return orderMode;
	}
	public void setOrderMode(int orderMode) {
		this.orderMode = orderMode;
	}
	public String getAcceptNickName() {
		return acceptNickName;
	}
	public void setAcceptNickName(String acceptNickName) {
		this.acceptNickName = acceptNickName;
	}
	public String getAcceptTime() {
		return acceptTime;
	}
	public void setAcceptTime(String acceptTime) {
		this.acceptTime = acceptTime;
	}
	
}
