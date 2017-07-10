package com.jingdong.app.reader.entity;

import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

public class CateEntity {
//	{"code":"","catList":[{"logo":"","catId":"","catName":"","isLeaf":""}]}
//	说明：
//	参数名称	是否必填	描述	示例
//	logo	Y	类目图片地址
//	catId	Y	类目ID	10
//	catName	Y	类目名称	小说
//	isLeaf	Y	是否最后一级(0：否 1：是)	0


	public final static String  KEY_LOGO_URL = "logo";
	public final static String  KEY_CAT_ID = "catId";
	public final static String  KEY_CATNAME = "catName";
	public final static String  KEY_IS_LEAF = "isLeaf";
	public final static String KEY_CATTYPE = "catType";
	public final static String KEY_AMOUNT="amount";//数量。

	public String logoUrl;
	public int catId;
	public String catName;
	public String isLeaf;
	public int catType = 0;   //0:默认  1：免费   2：冬瓜片

	public int drawableId = -1;
	public int amount=0;

	public int rootId;
	public static final CateEntity parse(JSONObject jSONObject) {
		CateEntity entity = null;
		if (jSONObject != null) {
			try {
				entity = new CateEntity();
				entity.catId = DataParser.getInt(jSONObject, CateEntity.KEY_CAT_ID);
				entity.catName =  DataParser.getString(jSONObject, CateEntity.KEY_CATNAME);
				entity.isLeaf =  DataParser.getString(jSONObject, CateEntity.KEY_IS_LEAF);
				entity.logoUrl =  DataParser.getString(jSONObject, CateEntity.KEY_LOGO_URL);
				entity.catType = DataParser.getInt(jSONObject, CateEntity.KEY_CATTYPE);
				entity.amount=DataParser.getInt(jSONObject, CateEntity.KEY_AMOUNT);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return entity;
	}

//	private RefreshImageItem refreshImageItem;
//	@Override
//	public RefreshImageItem getAndCreatRefreshItem(RefreshItemAble refreshImageAbler, MyActivity myActivity) {
//		if(refreshImageItem==null){
//			refreshImageItem = new 	RefreshImageItem(logoUrl,refreshImageAbler,myActivity);
//		}
//		return refreshImageItem;
//	}
//
//	@Override
//	public RefreshImageItem getRefreshItem() {
//		// TODO Auto-generated method stub
//		return refreshImageItem;
//	}
}
