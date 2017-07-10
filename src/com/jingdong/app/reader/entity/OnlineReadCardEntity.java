package com.jingdong.app.reader.entity;


import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

/**
 * 畅读卡
 * @author zhangmurui
 *
 */
public class OnlineReadCardEntity extends Entity{
	
	
	public static final String KEY_CARDID = "itemId";
	public static final String KEY_FACEPRICE = "price";
	public static final String KEY_CARD_NAME="cardName";//卡的类型。三种，季卡、半年卡、年卡。
	public static final String KEY_CARD_CONTENT="desc";//卡的优惠内容
	public static final  String KEY_CARD_IMG="imgUrl";
	
	
	public String card_Id ;
	public String card_Price;
	public String card_Name;
	public String card_Content;
 	public String card_imgUrl;
	@Override
  	public String getImageUrl() {
		// TODO Auto-generated method stub
		return card_imgUrl;
	}
	@Override
	public String getHomeImageUrl() {
		// TODO Auto-generated method stub
		return null;
	}
     
	public static final OnlineReadCardEntity parse(JSONObject jsonObject){
		OnlineReadCardEntity onlineReadCardEntity=null;
   	  if(jsonObject!=null){
   		onlineReadCardEntity=new OnlineReadCardEntity();
   		onlineReadCardEntity.card_Id=DataParser.getString(jsonObject,OnlineReadCardEntity.KEY_CARDID);
   		onlineReadCardEntity.card_Name=DataParser.getString(jsonObject, OnlineReadCardEntity.KEY_CARD_NAME);
   		onlineReadCardEntity.card_Content=DataParser.getString(jsonObject, OnlineReadCardEntity.KEY_CARD_CONTENT);
   		onlineReadCardEntity.card_Price=DataParser.getString(jsonObject, OnlineReadCardEntity.KEY_FACEPRICE);
   		onlineReadCardEntity.card_imgUrl=DataParser.getString(jsonObject, OnlineReadCardEntity.KEY_CARD_IMG);
   	  } 
		return onlineReadCardEntity;
	}
}
