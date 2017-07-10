package com.jingdong.app.reader.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

public class MyJdECardEntity extends Entity {

	/*
	 * 我的京东E卡列表 返回参数格式
	 * {"code":"","cardList":[{"cardId":"","picUrl":"","name":"",
	 * "facePrice":""}]} 说明： 参数名称 是否必填 描述 示例 cardId Y 礼品卡编号 30000001 picUrl
	 * 礼品卡封面地址 name 礼品卡名称 facePrice Y 面值
	 */

	public final String KEY_CARDID = "cardId";
	public final String KEY_FACEPRICE = "faceValue";
	public final String KEY_QUOTA = "quota";// 限额
	public final String KEY_STYLE = "style";// 类型:0电子,1实体
	public final String KEY_TYPE = "type";// 类型:0京券,1东券
	public final String KEY_TIMEBEGIN = "timeBegin";
	public final String KEY_TIMEEND = "timeEnd";

	public long cardId;
	public int facePrice;
	public int quota;
	public String style;
	public String type;
	public String timeBegin;
	public String timeEnd;

	// 分类中的京东e卡
	public String logo;
	public double price;

	public final String KEY_LOGO = "logo";
	public final String KEY_PRICE = "price";

	public static final MyJdECardEntity parse(JSONObject jsonObject) {

		MyJdECardEntity myjdCard = null;
		if (jsonObject != null) {
			try {
				SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				myjdCard = new MyJdECardEntity();
				myjdCard.cardId = DataParser.getLong(jsonObject,
						myjdCard.KEY_CARDID);
				myjdCard.facePrice = DataParser.getInt(jsonObject,
						myjdCard.KEY_FACEPRICE);
				myjdCard.quota = DataParser.getInt(jsonObject,
						myjdCard.KEY_QUOTA);
				myjdCard.style = DataParser.getInt(jsonObject,
						myjdCard.KEY_STYLE) == 0 ? "电子书刊" : "实体书刊";
				myjdCard.type = DataParser
						.getInt(jsonObject, myjdCard.KEY_TYPE) == 0 ? "京劵"
						: "东劵";
				
				myjdCard.timeBegin = date.format(new Date(DataParser.getLong(jsonObject,
						myjdCard.KEY_TIMEBEGIN)));
				myjdCard.timeEnd = date.format(new Date(DataParser.getLong(jsonObject,
						myjdCard.KEY_TIMEEND)));;

				myjdCard.logo = DataParser.getString(jsonObject,
						myjdCard.KEY_LOGO);
				myjdCard.price = DataParser.getDouble(jsonObject,
						myjdCard.KEY_PRICE);
			} catch (Exception e) {
				e.printStackTrace();

				e.printStackTrace();
			}
		}

		return myjdCard;
	}

	@Override
	public String getImageUrl() {
		// TODO Auto-generated method stub
		return logo;
	}

	@Override
	public String getHomeImageUrl() {
		// TODO Auto-generated method stub
		return null;
	}

}
