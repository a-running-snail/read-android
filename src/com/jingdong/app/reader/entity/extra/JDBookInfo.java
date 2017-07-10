package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;

public class JDBookInfo implements Serializable {

	public class Detail implements Serializable {
		/** 作者简介 */
		public String authorInfo;
		public long bookId;
		
		public String catalog;
		/** 格式 */
		public String format;
		/** 图书介绍 */
		public String info;
		/** 是否已经借阅 */
		public boolean isAlreadyBorrow;
		/** 是否已经被购买 */
		public boolean isAlreadyBuy;
		/** 是否能借阅 */
		public boolean isBorrow;
		/** 是否能购买 */
		public boolean isBuy;
		/** 是否是多媒体电子书,false表示是多媒体电子书,因其不能购买,故用 */
		public boolean isCanBuy;
		/** 是否能免费领取,用于控制7天畅读卡的按钮 */
		public boolean isCanFreeGet;
		/** 是否是电子书 */
		public boolean isEBook;
		/** 图书是否支持畅读 */
		public boolean isFluentRead;
		/** 是否是免费图书 */
		public boolean isFree;
		/** 图书是否支持试读 */
		public boolean isTryRead;
		/** 用户是否能畅读 */
		public boolean isUserCanFluentRead;
		
		public boolean isAddToCart;
		/** 京东价 */
		public double jdPrice;
		/** 定价 */
		public double price;
		/** 京东原价 */
		public double orgJdPrice;
		/** 纸书Id */
		public long paperBookId;
		/** 电子书Id */
		public long ebookId;
		/** 价格文案信息 */
		public String priceMessage;
		/** 广告语 */
		public String adWords;
		/** 促销信息 */
		public String promotion;
		public int star;
		/** 订单id，已经购买的情况下，订单Id */
		public long orderId;
		/** 文件大小 */
		public double size;
		/** 页面地址 */
		public String logo;
		/** 大页面地址 */
		public String largeLogo;
		/** 书名 */
		public String bookName;
		/** 作者  */
		public String author;
		/** epub格式书试读下载地址，长度1024字节 */
		public String tryDownLoadUrl;
		/** 出版社信息 */
		public String publisher;
		
		public String userBorrowEndTime;
		
		public String userBorrowStartTime;
		/** 书城借阅结束时间 */
		public String borrowEndTime;
		/** 书城借阅开始时间 */
		public String borrowStartTime;
		/** 用户是否已经将当前书增加到了畅读列表 */
		public boolean isUserFluentReadAddToCard;
		/** 版次 */
		public String edition;
		public String isbn;
		/** 出版时间 */
		public String publishTime;
		/** 借阅天数 */
		public int canBorrowDays;
		
		
		/** 是否允许用户借阅 */
		public boolean isBuyBorrow;
		/** 用户是否已经借阅 */
		public boolean isAlreadyUserBuyBorrow;
		/** 用户借阅结束时间 */
		public String userBuyBorrowEndTime;
		/** 用户借阅开始时间 */
		public String userBuyBorrowStartTime;
		/** 用户借阅天数 */
		public int canBuyBorrowDays;
		//赠书标志 true为获赠书籍 
		public boolean isReceived;
		/** 服务器当前时间 */
		public String currentTime;
	}



	public int code;
	public Detail detail;
	
	public static SimplifiedDetail simplifyDetail(Detail detail){
		SimplifiedDetail simplifiedDetail =new SimplifiedDetail();
		simplifiedDetail.bookId =detail.bookId;
		simplifiedDetail.jdPrice =detail.jdPrice;
		simplifiedDetail.price =detail.price;
		simplifiedDetail.orgJdPrice =detail.orgJdPrice;
		simplifiedDetail.paperBookId =detail.paperBookId;
		simplifiedDetail.ebookId =detail.ebookId;
		simplifiedDetail.logo =detail.logo;
		simplifiedDetail.largeLogo =detail.largeLogo;
		simplifiedDetail.bookName =detail.bookName;
		simplifiedDetail.author =detail.author;
		return simplifiedDetail;
	}

}
