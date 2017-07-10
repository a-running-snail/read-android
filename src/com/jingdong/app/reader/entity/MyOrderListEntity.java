package com.jingdong.app.reader.entity;


import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

public class MyOrderListEntity {

	/*
	 我的订单列表
	  参数名称	是否必填	描述	示例
	orderId	Y	订单编号	8382913
	status		订单状态

	totalPrice		总价
	orderTime		下单时间
	dealOrderUrl		处理订单URL
	 */

	public final  String KEY_ORDERID = "orderId";//[{"amount":"1000","status":"1","orderId":"1000","orderTime":"test"}
	public final  String KEY_STATUS = "orderStatus";
	public final  String KEY_TOTALPRICE = "price";
	public final  String KEY_ORDERTIME = "orderTime";
	public final  String KEY_DEALORDERURL = "dealOrderUrl";
	public final  String KEY_SENTSTATUS="sentStatus";//赠送状态 1，可赠送；2，已赠送；3，不可赠送。 在这没用到
	public final  String KEY_FILEFORMATE = "format";
	public final  String KEY_BOOKTYPE="bookType";
	//public final  String KEY_BOOKID = "bookId";
	
	public long orderId;
	public int status;//1:等待付款   2：已取消    4：等待付款确认   8：对账完成    16：支付完成
	public double totalPrice;
	public String orderTime;
	public String dealOrderUrl;
	public int orderFormat;
	//public int bookId;
    public int booktype;//在这没用到
	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}


	public int getStatus() {
		return status;
	}


	public void setStatus(int status) {
		this.status = status;
	}


	public double getTotalPrice() {
		return totalPrice;
	}


	public void setTotalPrice(double totalPrice) {
		this.totalPrice = totalPrice;
	}


	public String getOrderTime() {
		return orderTime;
	}


	public void setOrderTime(String orderTime) {
		this.orderTime = orderTime;
	}
   

	public String getDealOrderUrl() {
		return dealOrderUrl;
	}

	public void setDealOrderUrl(String dealOrderUrl) {
		this.dealOrderUrl = dealOrderUrl;
	}

	public static final MyOrderListEntity parse(JSONObject jsonObject) {

		MyOrderListEntity myOrderList = null;
		if (jsonObject != null) {
			try {
				myOrderList = new MyOrderListEntity();
				//myOrderList.bookId =  DataParser.getInt(jsonObject, myOrderList.KEY_BOOKID);
				myOrderList.orderId =  DataParser.getLong(jsonObject, myOrderList.KEY_ORDERID);
				myOrderList.status =  DataParser.getInt(jsonObject, myOrderList.KEY_STATUS);
				myOrderList.totalPrice =  DataParser.getDouble(jsonObject, myOrderList.KEY_TOTALPRICE);
				myOrderList.orderTime =  DataParser.getString(jsonObject, myOrderList.KEY_ORDERTIME);
				//myOrderList.dealOrderUrl =  DataParser.getString(jsonObject, myOrderList.KEY_DEALORDERURL);
				myOrderList.orderFormat=DataParser.getInt(jsonObject, myOrderList.KEY_FILEFORMATE);
				myOrderList.booktype=DataParser.getInt(jsonObject, myOrderList.KEY_BOOKTYPE);
			} catch (Exception e) {
				e.printStackTrace(); 
			}
		}
		return myOrderList;
	}
}
