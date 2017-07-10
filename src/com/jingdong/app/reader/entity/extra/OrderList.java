package com.jingdong.app.reader.entity.extra;

public class OrderList {

	private int bookType;
	private int format;
	private long orderId;
	private int orderMode;
	private int orderStatus;
	private String orderTime;
	private double price;
	private boolean readcard;
	private int sentStatus;
	public int getBookType() {
		return bookType;
	}
	public void setBookType(int bookType) {
		this.bookType = bookType;
	}
	public int getFormat() {
		return format;
	}
	public void setFormat(int format) {
		this.format = format;
	}
	public long getOrderId() {
		return orderId;
	}
	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}
	public int getOrderMode() {
		return orderMode;
	}
	public void setOrderMode(int orderMode) {
		this.orderMode = orderMode;
	}
	public int getOrderStatus() {
		return orderStatus;
	}
	public void setOrderStatus(int orderStatus) {
		this.orderStatus = orderStatus;
	}
	public String getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
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
	public int getSentStatus() {
		return sentStatus;
	}
	public void setSentStatus(int sentStatus) {
		this.sentStatus = sentStatus;
	}
}
