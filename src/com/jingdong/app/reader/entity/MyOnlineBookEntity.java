package com.jingdong.app.reader.entity;

import org.json.JSONObject;

import android.text.TextUtils;

import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.entity.LocalBook.SubBook;

public class MyOnlineBookEntity extends Entity {
	public static final String KEY_BOOK_ID = "itemId";// 书的id。
	public static final String KEY_BOOK_NAME = "ebookName";// 书名
	public static final String KEY_BOOK_AUTHOR = "author";// 书的作者
	public static final String KEY_CARD_STATUS = "cardStatus";// 卡的状态
	public static final String KEY_IS_EFFECTIVE = "canRead";// 是否可用。
	public static final String KEY_CARD_ID = "cardNo";// 畅读卡号。
	// public static final String KEY_TIMEBEGIN = "timeBegin";//有效开始时间
	public static final String KEY_TIMEEND = "serverEndTimeStr";// 有效结束时间
	public static final String KEY_CARD_IMG = "imgUrl";// 小图
	public final static String KEY_LARGESIZEIMGURL = "largeSizeImgUrl";// 大图
	public static final String KEY_CARD_STATUS_DESC = "statusDesc";// 状态描述。
	public static final String KEY_LAST_READ_TIME = "lastReadDateStr";// 最后阅读时间
	public static final String KEY_BOOK_FORMATE = "format";// 书的格式：1 pdf。2 epub;
	public static final String KEY_BOOK_FORMATE_NAME = "formatMeaning";// 书的格式名字
	public static final String KEY_BOOK_PLAT = "plat";// 支持平台。

	public String author;
	public String canRead;
	public String cardNo;
	public int cardStatus;
	public String ebookName;
	public int format;
	public String formatMeaning;
	public String imgUrl;
	public String largeSizeImgUrl;
	public long itemId;
	public String lastReadDate;
	public String lastReadDateStr;
	public String logo;
	public String plat;
	public String serveEndTime;
	public String serverEndTimeStr;
	public float size;
	public String statusDesc;
	public boolean pass;
	public boolean supportCardRead;
	public SubBook subBook;

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

}
