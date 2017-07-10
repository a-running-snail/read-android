package com.jingdong.app.reader.entity.extra;

public class JDEBook {

	// 以下用于已购列表
	
	/**
	 * 作者
	 */
	public String author;
	/**
	 * 电子书Id
	 */
	public String bookId;
	/**
	 * 图书类型名
	 */
	public String bookTypeName;
	/**
	 * 图书内容格式
	 */
	public int format;
	/**
	 * 格式,PDF(1, "pdf"), EPUB(2, "epub"), EXE(3, "exe"), SWF(4, "swf"), APK(10, "apk");
	 */
	public String formatMeaning;
	public String imgUrl;
	/**
	 * 大图适配
	 */
	public String largeSizeImgUrl;
	/**
	 * 图书名
	 */
	public String name;
	/**
	 * 订单Id
	 */
	public String orderId;
	/**
	 * 订单状态,16为订单完成
	 */
	public String orderStatusName;
	public String orderTime;
	public boolean readcard;
	public float size;
	/**
	 * 金额,订单总金额,或者在orderDetail处理时,单个图书的金额
	 */
	public float price;
	/**
	 * 图书类型1 电子书
	 */
	public int bookType;
	/**
	 * 赠送状态。1，可赠送；2，已赠送；3，不可赠送。
	 */
	public int sentStatus;
	/**
	 * 订单模式:0,支付下单,1赠送,2,免费下载
	 */
	public int orderMode;
	/**
	 * 订单状态Id,1、等待付款，2、已取消，8，对账完成，16为订单完成
	 */
	public int orderStatus;
	/**
	 * 赠书人昵称
	 */
	public String sentNickName;
	
	/*
	 * 赠书标志 true为获赠书籍 
	 */
	public boolean isReceived;
	/**
	 * 是否已上架,true为下架，false为上架
	 */
	public boolean pass;
	
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getBookId() {
		return bookId;
	}
	public void setBookId(String bookId) {
		this.bookId = bookId;
	}
	public String getBookTypeName() {
		return bookTypeName;
	}
	public void setBookTypeName(String bookTypeName) {
		this.bookTypeName = bookTypeName;
	}
	public String getFormatMeaning() {
		return formatMeaning;
	}
	public void setFormatMeaning(String formatMeaning) {
		this.formatMeaning = formatMeaning;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
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
	public boolean isReadcard() {
		return readcard;
	}
	public void setReadcard(boolean readcard) {
		this.readcard = readcard;
	}
	public float getSize() {
		return size;
	}
	public void setSize(float size) {
		this.size = size;
	}
	public float getPrice() {
		return price;
	}
	public void setPrice(float price) {
		this.price = price;
	}
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
	public int getSentStatus() {
		return sentStatus;
	}
	public void setSentStatus(int sentStatus) {
		this.sentStatus = sentStatus;
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
	public String getSentNickName() {
		return sentNickName;
	}
	public void setSentNickName(String sentNickName) {
		this.sentNickName = sentNickName;
	}
}
