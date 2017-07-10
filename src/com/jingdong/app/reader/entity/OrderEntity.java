package com.jingdong.app.reader.entity;

import org.json.JSONObject;

import android.text.TextUtils;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.entity.extra.JDBookInfo;
import com.jingdong.app.reader.entity.extra.JDEBook;
import com.jingdong.app.reader.entity.extra.OpenTaskDownloadEntity;
import com.jingdong.app.reader.util.MZLog;

public class OrderEntity extends MyBookEntity {
	public final static String KEY_ORDERID = "orderId";
	public final static String KEY_ORDERTIME = "orderTime";
	public final static String KEY_PRICE = "price";
	public final static String KEY_BOOK_TYPE = "bookTypeName";
	public final static String KEY_ORDERSTATUS = "orderStatus";
	public final static String KEY_orderStatusName = "orderStatusName";

	public final static String KEY_FORMAT = "format";
	public final static String KEY_FORMAT_NAME = "formatMeaning";
	public final static String KEY_IS_READCARD = "readcard";
	// public int orderId;
	/*
	 * EBOOK_ORDER_STATUS_WAIT(1, "等待付款", "等待付款"), EBOOK_ORDER_STATUS_CANCEL(2,
	 * "已取消", "已取消"), EBOOK_ORDER_STATUS_WAIT_CONFIRM(4, "等待付款确认", "等待付款"),
	 * EBOOK_ORDER_STATUS_STOP(8, "对账完成", "等待付款"),
	 * EBOOK_ORDER_STATUS_SUCEFULL(16, "支付完成", "完成");
	 */

	/**
	 * 图书文件大小
	 */
	public String book_size;
	public int orderStatus;// 判断是下载还是支付
	public String formatName;// 书的类型：pdf or epub
	public int format;
	
	public String tryDownLoadUrl;//试读下载地址
	
	public String borrowEndTime;//借阅截止时间
	/** 用户借阅结束时间 */
	public String userBuyBorrowEndTime;

	public boolean isBorrowBuy = false;
	
	public boolean isReceived = false;
	
	/******
	 * @param jsonObject
	 *            数据格式如下 { "orderStatusName": "支付完成", "bookId": 30039409,
	 *            "orderTime": "2012-03-31 18:32:24", "format": 2, "bookType":
	 *            1, "orderStatus": 16, "size": 0.42, "imgUrl":
	 *            "http://img10.360buyimg.com/n2/19677/1810533a-0c9b-480e-8f00-711eee628af5.jpg"
	 *            , "author": "34", "price": 1, "readcard": false, "name":
	 *            "jyd4", "formatMeaning": "epub", "bookTypeName": "电子书",
	 *            "sentStatus": 0, "orderId": 99049000, "largeSizeImgUrl":
	 *            "http://img10.360buyimg.com/n1/19677/1810533a-0c9b-480e-8f00-711eee628af5.jpg"
	 *            , "orderMode": 0 }
	 * @since 1.3.0 2013年5月15日10:11:56
	 * ***********************/
	public static OrderEntity parse(JSONObject jsonObject) {
		MZLog.d("zhoubo", "jsonObject.toString()===" + jsonObject.toString());
		OrderEntity order = null;
		if (jsonObject != null) {
			try {
				// ,"format":2,"formatMeaning":"epub"
				order = new OrderEntity();
				order.name = DataParser.getString(jsonObject,
						BookInforEntity.KEY_NAME);
				order.author = DataParser.getString(jsonObject,
						BookInforEntity.KEY_AUTHOR);
				if (TextUtils.isEmpty(order.author)) {
					order.author = MZBookApplication.getInstance().getString(
							R.string.authorNameDefault);
				}
				order.book_size = DataParser.getString(jsonObject,
						BookInforEntity.KEY_SIZE) + "M";
				order.bookId = DataParser.getLong(jsonObject,
						BookInforEntity.KEY_BOOKID);

				order.bookType = DataParser.getInt(jsonObject,
						BookInforEntity.KEY_BOOK_TYPE);
				order.picUrl = DataParser.getString(jsonObject,
						BookInforEntity.KEY_IMGURL);
				order.bigPicUrl = DataParser.getString(jsonObject,
						BookInforEntity.KEY_BIG_IMGURL);

				order.orderId = DataParser.getLong(jsonObject,
						OrderEntity.KEY_ORDERID);
				order.orderStatus = DataParser.getInt(jsonObject,
						OrderEntity.KEY_ORDERSTATUS);
				order.format = DataParser.getInt(jsonObject,
						OrderEntity.KEY_FORMAT);
				order.downloadAble = isDownloadable(DataParser.getInt(
						jsonObject, OrderEntity.KEY_FORMAT));

			} catch (Exception e) {

				e.printStackTrace();
				MZLog.d("xiawei", order.toString());
			}
		}
		return order;
	}

	@Override
	public boolean isDownloadAble() {

		return this.downloadAble;
	}

	public static boolean isDownloadable(int format) {

		if (format == 2 || format == 1) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	public String getImageUrl() {
		return picUrl;
	}
	
	
	
	
	public static OrderEntity FromOpenTaskDownloadEntity2OrderEntity(OpenTaskDownloadEntity bookEntity) {
		OrderEntity orderEntity = new OrderEntity();
		orderEntity.name = bookEntity.bookName;
		orderEntity.bookId = bookEntity.bookId;
		orderEntity.bookType = bookEntity.bookType;
		orderEntity.author = bookEntity.author;
		orderEntity.orderId =bookEntity.orderId;
		orderEntity.orderStatus = 16;
		orderEntity.picUrl = bookEntity.picUrl;
		orderEntity.bigPicUrl = bookEntity.bigPicUrl;
		orderEntity.format =bookEntity.format;
		return orderEntity;

	}
	
	
	public static JDEBook FromJDBooKInfo2JDEBook(JDBookInfo.Detail bookinfo) {
		JDEBook jdebook = new JDEBook();
		jdebook.author = bookinfo.author;
		jdebook.bookId = Long.toString(bookinfo.bookId);
//		jdebook.bookTypeName = bookinfo.;
		jdebook.formatMeaning = bookinfo.format;
		jdebook.imgUrl = bookinfo.logo;
		jdebook.largeSizeImgUrl = bookinfo.largeLogo;
		jdebook.name = bookinfo.bookName;
		jdebook.orderId = Long.toString(bookinfo.orderId);
//		jdebook.orderStatusName = bookinfo.or
//		jdebook.orderTime = bookinfo.bookName;
//		jdebook.readcard = bookinfo.r;
		jdebook.size = (float)(bookinfo.size);
		jdebook.price = (float)bookinfo.jdPrice;
//		jdebook.bookType = bookinfo.boo;
//		jdebook.format = ;
//		jdebook.sentStatus = bookinfo.sentStatus;
//		jdebook.orderMode = bookinfo.orderMode;
//		jdebook.orderStatus = bookinfo.orderStatus;
		jdebook.isReceived = bookinfo.isReceived;
		return jdebook;
	}
	
	/**
	 * 图书相关实体
	 * @param bookEntity
	 * @return
	 */
	public static OrderEntity FromJDBooK2OrderEntity(JDEBook bookEntity) {
		OrderEntity orderEntity = new OrderEntity();
		//书名
		orderEntity.name = bookEntity.name;
		//图书Id
		orderEntity.bookId = Long.parseLong(bookEntity.bookId);
		//图书类型，1为电子书，其他为非电子书
		orderEntity.bookType = bookEntity.bookType;
		//作者
		orderEntity.author = bookEntity.author;
		orderEntity.orderId = Long.parseLong(bookEntity.orderId);
		orderEntity.orderStatus = 16;
		orderEntity.book_size = bookEntity.size+"M";
		// orderEntity.orderTime = "";
		orderEntity.price = bookEntity.price;
		orderEntity.bookTypeName = bookEntity.bookTypeName;
		orderEntity.picUrl = bookEntity.imgUrl;
		orderEntity.bigPicUrl = bookEntity.largeSizeImgUrl;
		orderEntity.formatName = bookEntity.formatMeaning;
		orderEntity.isReceived = bookEntity.isReceived;
		return orderEntity;
	}
	
	public static OrderEntity FromJDBooKInfo2OrderEntity(JDBookInfo.Detail bookinfo) {
		OrderEntity orderEntity = new OrderEntity();
		orderEntity.name = bookinfo.bookName;
		orderEntity.bookId = bookinfo.bookId;
		orderEntity.tryDownLoadUrl =bookinfo.tryDownLoadUrl;
		if (bookinfo.isEBook)
			orderEntity.bookType = LocalBook.TYPE_EBOOK;
		orderEntity.author = bookinfo.author;
		if(bookinfo.isAlreadyBuy)
			orderEntity.orderId = bookinfo.orderId;
		else
			orderEntity.orderId = bookinfo.bookId;//借阅时bookId即为orderId,否则无法下载
		orderEntity.orderStatus = 16;
		orderEntity.price =bookinfo.jdPrice;
		orderEntity.picUrl =bookinfo.logo;
		orderEntity.bigPicUrl = bookinfo.largeLogo;
		orderEntity.formatName =bookinfo.format;
		orderEntity.book_size = bookinfo.size+"M";
		orderEntity.borrowEndTime=bookinfo.userBorrowEndTime;
		orderEntity.userBuyBorrowEndTime=bookinfo.userBuyBorrowEndTime;
		orderEntity.isReceived = bookinfo.isReceived;
		return orderEntity;
	}
	
	public static OrderEntity FromJDBooKInfo2OrderEntityWithOrderid(JDBookInfo.Detail bookinfo) {
		OrderEntity orderEntity = new OrderEntity();
		orderEntity.name = bookinfo.bookName;
		orderEntity.bookId = bookinfo.bookId;
		orderEntity.tryDownLoadUrl =bookinfo.tryDownLoadUrl;	
		if (bookinfo.isEBook)
			orderEntity.bookType = LocalBook.TYPE_EBOOK;
		orderEntity.author = bookinfo.author;
		orderEntity.orderId =bookinfo.orderId;
		orderEntity.orderStatus = 16;
		orderEntity.price =bookinfo.jdPrice;
		orderEntity.picUrl =bookinfo.logo;
		orderEntity.bigPicUrl = bookinfo.largeLogo;
		orderEntity.formatName =bookinfo.format;
		orderEntity.borrowEndTime=bookinfo.userBorrowEndTime;
		orderEntity.isReceived = bookinfo.isReceived;
		return orderEntity;
		
	}
	

	public static OrderEntity FromJDBooKInfo2OrderEntityWithoutOrderid(JDBookInfo.Detail bookinfo) {
		OrderEntity orderEntity = new OrderEntity();
		orderEntity.name = bookinfo.bookName;
		orderEntity.bookId = bookinfo.bookId;
		orderEntity.tryDownLoadUrl =bookinfo.tryDownLoadUrl;	
		if (bookinfo.isEBook)
			orderEntity.bookType = LocalBook.TYPE_EBOOK;
		orderEntity.author = bookinfo.author;
		orderEntity.orderId =-1;
		orderEntity.orderStatus = 16;
		orderEntity.price =bookinfo.jdPrice;
		orderEntity.picUrl =bookinfo.logo;
		orderEntity.bigPicUrl = bookinfo.largeLogo;
		orderEntity.formatName =bookinfo.format;
		orderEntity.borrowEndTime=bookinfo.userBorrowEndTime;
		orderEntity.isReceived = bookinfo.isReceived;
		return orderEntity;

	
		
	}
	
	

	public static OrderEntity FromBooK2OrderEntity(MyBookEntity bookEntity) {
		OrderEntity orderEntity = new OrderEntity();
		orderEntity.name = bookEntity.name;
		orderEntity.bookId = bookEntity.bookId;
		orderEntity.bookType = bookEntity.bookType;
		orderEntity.author = bookEntity.author;
		orderEntity.orderId = bookEntity.orderId;
		orderEntity.orderStatus = 16;
		// orderEntity.orderTime = "";
		orderEntity.price = bookEntity.price;
		orderEntity.bookTypeName = bookEntity.bookTypeName;
		orderEntity.picUrl = bookEntity.picUrl;
		orderEntity.bigPicUrl = bookEntity.bigPicUrl;
		// orderEntity.orderStatusName = "";
		// orderEntity.alreadyDownload = false;

		orderEntity.formatName = bookEntity.bookFormat;
		return orderEntity;

	}

}
