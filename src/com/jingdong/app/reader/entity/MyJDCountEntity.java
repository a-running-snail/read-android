package com.jingdong.app.reader.entity;

import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

public class MyJDCountEntity {
	/*
	 * 说明：
参数名称	是否必填	描述	示例
myOrder		我的订单数量	21
myFavorites		我的收藏数量
buyedDigitProduct		购买的数字产品数量
jdECard		京东E卡数量
tryReadDigitProduct		试图电子产品数量
	 */

	public final  String KEY_MYORDER = "orderTotal";
	public final  String KEY_MYFAVORITES = "favoriteTotal";
	public final  String KEY_BUYEDDIGITPRODUCT = "buyedEbookTotal";
	public final  String KEY_JDCARD = "eCard";
	public final  String KEY_TRYREADDIGITPRODUCT = "probationEbookTotal";
	public final  String KEY_BALANCE = "balance";
    public final String  KEY_DISCOUNTCART="coupon";//优惠券
    public final String  KEY_MYONLINEBOOK="myReadBookCount";//我的畅读。待改。
    public final String  KEY_MYONLINECARD="useCardBookCount";//我的畅读卡。待改。
	public int myOrder = 0;
	public int myFavorites = 0;
	public int buyedDigitProduct = 0;
	public int jdECard = 0;
	public int tryReadDigitProduct =0;
	public double balance =0.0;
    public int discount=0;
    public int onlineBook=0;
    public int onlineCard=0;
	public static final MyJDCountEntity parse(JSONObject jsonObject) {
		MyJDCountEntity myjdCount = null;
		if (jsonObject != null) {
			try {
					myjdCount = new MyJDCountEntity();
					myjdCount.myOrder =  DataParser.getInt(jsonObject, myjdCount.KEY_MYORDER);
					myjdCount.myFavorites =  DataParser.getInt(jsonObject, myjdCount.KEY_MYFAVORITES);
					myjdCount.buyedDigitProduct =  DataParser.getInt(jsonObject, myjdCount.KEY_BUYEDDIGITPRODUCT);
					myjdCount.jdECard =  DataParser.getInt(jsonObject, myjdCount.KEY_JDCARD);
					myjdCount.tryReadDigitProduct =  DataParser.getInt(jsonObject, myjdCount.KEY_TRYREADDIGITPRODUCT);
					myjdCount.balance =  DataParser.getDouble(jsonObject, myjdCount.KEY_BALANCE);
					myjdCount.discount =  DataParser.getInt(jsonObject, myjdCount.KEY_DISCOUNTCART);
					myjdCount.onlineBook=DataParser.getInt(jsonObject, myjdCount.KEY_MYONLINEBOOK);
					myjdCount.onlineCard=DataParser.getInt(jsonObject, myjdCount.KEY_MYONLINECARD);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return myjdCount;
	}
}
