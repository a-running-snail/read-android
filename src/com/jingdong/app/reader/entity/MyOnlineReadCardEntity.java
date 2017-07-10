package com.jingdong.app.reader.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

public class MyOnlineReadCardEntity extends Entity{
	
	
	public static final String KEY_CARDID = "cardNo";
	public static final String KEY_FACEPRICE = "faceMoney";
	public static final String KEY_STATE = "cardStatus";// 状态：1、激活，2、使用中，3、已过期，4、新卡
	public static final String KEY_TIMEBEGIN = "serveStartTime";//服务开始时间
	public static final String KEY_TIMEEND = "serveEndTime";//服务结束时间。
	public static final String KEY_SURPLUS_BOOK="canReadCount";//剩余书籍。
	public static final String KEY_TOTAL_BOOK="topCount";//允许阅读的书的总量。
	public static final String KEY_EXPIRY_DATE="expiryDate";//有效期
	public static final String KEY_STATUS_DESC="statusDesc";//状态描述。
	//畅读卡的状态，待服务器确定。
	public static final int CARD_STATE_USING = 2;
	public static final int CARD_STATE_OVERDUE = 3;
	public static final int CARD_STATE_ACTIVATION = 1;
	public static final int CARD_STATE_NEW = 0;
	
	public String cardId ;
	public String facePrice;
	public int state;
	public String timeBegin;
	public String timeEnd;
	public int surplusBook;//剩余书籍
	public int totalBook;
	public String expiryDate;//有效期
	public String statusDesc;//状态描述。
	
	@Override
 	public String getImageUrl() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getHomeImageUrl() {
		// TODO Auto-generated method stub
		return null;
	}
     
	public static final MyOnlineReadCardEntity parse(JSONObject jsonObject){
		MyOnlineReadCardEntity readCardEntity=null;
		if (jsonObject!=null) {
			 SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
			readCardEntity=new MyOnlineReadCardEntity();
			readCardEntity.cardId=DataParser.getString(jsonObject, MyOnlineReadCardEntity.KEY_CARDID);
			readCardEntity.facePrice=DataParser.getString(jsonObject, MyOnlineReadCardEntity.KEY_FACEPRICE);
			readCardEntity.state=DataParser.getInt(jsonObject, MyOnlineReadCardEntity.KEY_STATE);
//			readCardEntity.timeBegin = date.format(new Date(DataParser.getLong(jsonObject,
//					MyOnlineReadCardEntity.KEY_TIMEBEGIN)));
//			readCardEntity.timeEnd = date.format(new Date(DataParser.getLong(jsonObject,
//					MyOnlineReadCardEntity.KEY_TIMEEND)));
			readCardEntity.timeBegin = DataParser.getString(jsonObject,MyOnlineReadCardEntity.KEY_TIMEBEGIN);
			readCardEntity.timeEnd = DataParser.getString(jsonObject,MyOnlineReadCardEntity.KEY_TIMEEND);
			readCardEntity.surplusBook=DataParser.getInt(jsonObject, MyOnlineReadCardEntity.KEY_SURPLUS_BOOK);
			readCardEntity.totalBook=DataParser.getInt(jsonObject, MyOnlineReadCardEntity.KEY_TOTAL_BOOK);
			readCardEntity.expiryDate=DataParser.getString(jsonObject, MyOnlineReadCardEntity.KEY_EXPIRY_DATE);
			readCardEntity.statusDesc=DataParser.getString(jsonObject, MyOnlineReadCardEntity.KEY_STATUS_DESC);
		}
		return readCardEntity;
	}
}
