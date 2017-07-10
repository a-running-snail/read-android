package com.jingdong.app.reader.entity.extra;

public class JDBookDetail {

	private long ebookId;

	private String name;// 图书名称

	private String author;// 作者

	private String format;// 格式

	private String publisher;// 出版社

	private String categoryPath;

	private String imageUrl;// 封面

	private String largeImageUrl;// 封面

	private float fileSize;// 文件大小

	private int good;// 好评度

	private int star;// 星级

	private String info;// 简介

	private boolean isFree;// 是否免费

	private boolean isBuy = false;// 是否购买

	private boolean isBorrow = false;// 是否能借阅

	private boolean isFluentRead = false;// 图书是否支持畅读

	private boolean isAlreadyBuy = false;// 是否已经被购买

	private String orderId;// 订单id，已经购买的情况下，订单Id

	private String priceMessage;// 价格文案信息

	private double price;// 定价

	private double jdPrice;// 京东价

	private boolean isEBook;// 是否是电子书

	private long paperBookId;// 纸书id
	
	private String borrowStartTime;   /*借阅开始时间*/
	
	private String borrowEndTime;   /*借阅结束时间*/
	
	private String canBorrowDays;   /*借阅天数*/
	
	private int borrowStatus;  /*0，借阅中，1，已经过期，2，已经还书*/
	
	private String borrowTime;  /*创建时间*/
	
	private String returnTime;  /*还书时间*/
	
	private boolean isReturn;     /*是否已经还书*/
	
	private String userBorrowEndTime;//借阅结束时间
	
	private String userBorrowStartTime;//借阅开始时间
	private String currentTime;//服务器当前始时间
	
	//用户借阅能借阅的天数
	private int canBuyBorrowDays;
	//用户借阅是否已经借阅
	private boolean isAlreadyUserBuyBorrow;
	//是否可以用户借阅
	private boolean isBuyBorrow;
	//用户借阅的结束时间
	private String userBuyBorrowEndTime;
	//用户借阅的开始时间
	private String userBuyBorrowStartTime;
	
	public String getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(String currentTime) {
		this.currentTime = currentTime;
	}

	private boolean isAlreadyBorrow = false;
 

	public long getEbookId() {
		return ebookId;
	}

	public void setEbookId(long ebookId) {
		this.ebookId = ebookId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getCategoryPath() {
		return categoryPath;
	}

	public void setCategoryPath(String categoryPath) {
		this.categoryPath = categoryPath;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getLargeImageUrl() {
		return largeImageUrl;
	}

	public void setLargeImageUrl(String largeImageUrl) {
		this.largeImageUrl = largeImageUrl;
	}

	public float getFileSize() {
		return fileSize;
	}

	public void setFileSize(float fileSize) {
		this.fileSize = fileSize;
	}

	public int getGood() {
		return good;
	}

	public void setGood(int good) {
		this.good = good;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public boolean isFree() {
		return isFree;
	}

	public void setFree(boolean isFree) {
		this.isFree = isFree;
	}

	public boolean isBuy() {
		return isBuy;
	}

	public void setBuy(boolean isBuy) {
		this.isBuy = isBuy;
	}

	public boolean isBorrow() {
		return isBorrow;
	}

	public void setBorrow(boolean isBorrow) {
		this.isBorrow = isBorrow;
	}

	public boolean isFluentRead() {
		return isFluentRead;
	}

	public void setFluentRead(boolean isFluentRead) {
		this.isFluentRead = isFluentRead;
	}

	public boolean isAlreadyBuy() {
		return isAlreadyBuy;
	}

	public void setAlreadyBuy(boolean isAlreadyBuy) {
		this.isAlreadyBuy = isAlreadyBuy;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPriceMessage() {
		return priceMessage;
	}

	public void setPriceMessage(String priceMessage) {
		this.priceMessage = priceMessage;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getJdPrice() {
		return jdPrice;
	}

	public void setJdPrice(double jdPrice) {
		this.jdPrice = jdPrice;
	}

	public boolean isEBook() {
		return isEBook;
	}

	public void setEBook(boolean isEBook) {
		this.isEBook = isEBook;
	}

	public long isPaperBookId() {
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

	public String getCanBorrowDays() {
		return canBorrowDays;
	}

	public void setCanBorrowDays(String canBorrowDays) {
		this.canBorrowDays = canBorrowDays;
	}

	public int getBorrowStatus() {
		return borrowStatus;
	}

	public void setBorrowStatus(int borrowStatus) {
		this.borrowStatus = borrowStatus;
	}

	public String getBorrowTime() {
		return borrowTime;
	}

	public void setBorrowTime(String borrowTime) {
		this.borrowTime = borrowTime;
	}

	public String getReturnTime() {
		return returnTime;
	}

	public void setReturnTime(String returnTime) {
		this.returnTime = returnTime;
	}

	public boolean isReturn() {
		return isReturn;
	}

	public void setIsReturn(boolean isReturn) {
		this.isReturn = isReturn;
	}

	public long getPaperBookId() {
		return paperBookId;
	}

	public String getUserBorrowEndTime() {
		return userBorrowEndTime;
	}

	public void setUserBorrowEndTime(String userBorrowEndTime) {
		this.userBorrowEndTime = userBorrowEndTime;
	}

	public String getUserBorrowStartTime() {
		return userBorrowStartTime;
	}

	public void setUserBorrowStartTime(String userBorrowStartTime) {
		this.userBorrowStartTime = userBorrowStartTime;
	}

	public int getCanBuyBorrowDays() {
		return canBuyBorrowDays;
	}

	public void setCanBuyBorrowDays(int canBuyBorrowDays) {
		this.canBuyBorrowDays = canBuyBorrowDays;
	}

	public boolean isAlreadyUserBuyBorrow() {
		return isAlreadyUserBuyBorrow;
	}

	public void setAlreadyUserBuyBorrow(boolean isAlreadyUserBuyBorrow) {
		this.isAlreadyUserBuyBorrow = isAlreadyUserBuyBorrow;
	}

	public boolean isBuyBorrow() {
		return isBuyBorrow;
	}

	public void setBuyBorrow(boolean isBuyBorrow) {
		this.isBuyBorrow = isBuyBorrow;
	}

	public String getUserBuyBorrowEndTime() {
		return userBuyBorrowEndTime;
	}

	public void setUserBuyBorrowEndTime(String userBuyBorrowEndTime) {
		this.userBuyBorrowEndTime = userBuyBorrowEndTime;
	}

	public String getUserBuyBorrowStartTime() {
		return userBuyBorrowStartTime;
	}

	public void setUserBuyBorrowStartTime(String userBuyBorrowStartTime) {
		this.userBuyBorrowStartTime = userBuyBorrowStartTime;
	}

	public boolean isAlreadyBorrow() {
		return isAlreadyBorrow;
	}

	public void setAlreadyBorrow(boolean isAlreadyBorrow) {
		this.isAlreadyBorrow = isAlreadyBorrow;
	}

	public void setReturn(boolean isReturn) {
		this.isReturn = isReturn;
	}
}


