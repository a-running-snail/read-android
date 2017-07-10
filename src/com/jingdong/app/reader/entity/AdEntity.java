package com.jingdong.app.reader.entity;

import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

public class AdEntity extends Entity {
	public static int RTYPE_BOOK_E = 1;
	public static int RTYPE_BOOK_MEIDA = 2;
	public static int RTYPE_BOOK_PAPER = 3;
	public static int RTYPE_BOOK_CATE_HOME = 4;
	public final static String KEY_LOGO_URL = "logoUrl";
	public final static String KEY_CLICK_PARAM = "clickParam";
	public final static String KEY_IS_OUT = "isOut";
	public final static String KEY_NAME = "adName";
	public final static String KEY_FUNCTION_ID = "functionId";
	public final static String KEY_CATID = "catId";
	public final static String KEY_ADCLICK_TYPE = "adClickType";
	public final static String KEY_ADNAME = "adName";
	public final static int CILK_TYPE_BOOK = 1;
	public final static int CILK_TYPE_OUT = 2;
	public final static int CILK_TYPE_CATE = 3;
	public final static int CILK_TYPE_CARD = 4;
	public String logoUrl;
	// public String clickUrl;
	public int isOut;
	public int catId;
	public String functionId;
	public String outUrl;
	public int adClickType = 0;
	public String adName;

	public AdEntity clone() {
		AdEntity o = null;
		try {
			o = (AdEntity) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return o;
	}

	public static final AdEntity parse(JSONObject jsonObject) {
		AdEntity ad = null;
		if (jsonObject != null) {
			try {
				ad = new AdEntity();
				ad.logoUrl = DataParser.getString(jsonObject,
						AdEntity.KEY_LOGO_URL);
				ad.isOut = DataParser.getInt(jsonObject, AdEntity.KEY_IS_OUT);
				ad.adName = DataParser.getString(jsonObject, AdEntity.KEY_ADNAME);
				if (ad.isOut == 1) {
					ad.outUrl = DataParser.getString(jsonObject,
							AdEntity.KEY_CLICK_PARAM);
				} else {
					JSONObject jObject = (JSONObject) jsonObject
							.getJSONObject(AdEntity.KEY_CLICK_PARAM);
					if (jObject != null) {
						ad.adClickType = DataParser.getInt(jObject,
								AdEntity.KEY_ADCLICK_TYPE);
						ad.catId = DataParser.getInt(jObject,
								AdEntity.KEY_CATID);
						ad.functionId = DataParser.getString(jObject,
								AdEntity.KEY_FUNCTION_ID);
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ad;
	}

	@Override
	public String getImageUrl() {
		// TODO Auto-generated method stub
		return logoUrl;
	}

	@Override
	public String getHomeImageUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFilePath() {
		// TODO Auto-generated method stub
		return null;
	}

}