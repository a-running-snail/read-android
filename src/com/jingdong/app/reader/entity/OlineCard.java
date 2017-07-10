package com.jingdong.app.reader.entity;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.data.db.DataProvider;
import com.jingdong.app.reader.onlinereading.OlineDesUtils;
import com.jingdong.app.reader.onlinereading.OnlineCardManeger;
import com.jingdong.app.reader.util.DesUtil;
import com.jingdong.app.reader.util.Log;

public class OlineCard {

	public final static Uri uri = DataProvider.CONTENT_URI_NAME_OLINE_CARD;
	public String cardNum;
	public int availab;
	public long start;
	public long end;

	// 输出字段
	public final static String KEY_CARD = "card";
	public final static String KEY_CARDS = "cards";

	// {"cardEndTime":"yW9WLRdeyM41fLQLSG+qIA==","cardNO":"S170493944126406","code":"0","expire":1}单本书的返回数据

	// {"cardNo":"S170493944126406","time":"yW9WLRdeyM41fLQLSG+qIA=="
	// //获取密钥时返回的，卡号信息
	// {"cardEndTime":"yW9WLRdeyM41fLQLSG+qIA==","cardNO":"S170493944126406","code":"0","expire":1}
	// 批量接口数据
	// 输入字段
	public final static String KEY_SUCCESS = "isSuccess";
	public final static String KEY_ISEXPIRE_1 = "expire";// 因为接口字段名不一致，所以解析的时候，需要判断还几个字段是否有值，太烂的接口设计。
	public final static String KEY_ISEXPIRE_2 = "isExpire";
	public final static String KEY_ENTIME_1 = "serveEndTime";
	public final static String KEY_ENTIME_2 = "cardEndTime";
	public final static String KEY_ENTIME_3 = "time";
	public final static String KEY_CARD_NO_1 = "cardNo";
	public final static String KEY_CARD_NO_2 = "cardNO";

	public final static String[] ALL_PROJECTION = new String[] { "card_num",
			"availab", "start", "end" };

	/***
	 * 保存图书，从外部保存。包括导入和从文件浏览器
	 * 
	 * @since 2013-1-15 11:13:55
	 * ******************/
	public boolean save() {
		ContentValues values = new ContentValues();
		values.put("card_num", cardNum);
		values.put("availab", availab);
		values.put("start", start);
		if (this.end > 0) {
			String end = OlineDesUtils.encrypt("" + this.end);
			values.put("end", end);
		}
		Uri tempuri = OlineCard.uri;
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		int updateNum = 0;
		if ((updateNum = mContentResolver.update(tempuri, values, "card_num"
				+ "=" + "'" + cardNum + "'", null)) == 0) {
			tempuri = mContentResolver.insert(tempuri, values);
		}
		boolean isSuccess = false;
		if (tempuri != null || updateNum > 0) {
			isSuccess = true;
		} else {
			isSuccess = false;
		}
		mContentResolver.notifyChange(uri, null);
		return isSuccess;
	}

	public static boolean save(String starttime, String endtime, String cardno,
			int availab) {
		ContentValues values = new ContentValues();
		values.put("card_num", cardno);
		values.put("availab", availab);
		values.put("start", starttime);
		values.put("end", endtime);
		Uri tempuri = OlineCard.uri;
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		int updateNum = 0;
		if ((updateNum = mContentResolver.update(tempuri, values, "card_num"
				+ "=" + "'" + cardno + "'", null)) == 0) {
			tempuri = mContentResolver.insert(tempuri, values);
		}
		boolean isSuccess = false;
		if (tempuri != null || updateNum > 0) {
			isSuccess = true;
		} else {
			isSuccess = false;
		}
		mContentResolver.notifyChange(uri, null);
		return isSuccess;
	}

	public static ArrayList<OlineCard> readOlineCardsFromDb() {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		OlineCard olineCard = null;
		ArrayList<OlineCard> listArray = new ArrayList<OlineCard>();
		String selection = null;
		Cursor cur;
		cur = mContentResolver.query(uri, ALL_PROJECTION, null, null, null);// " DESC");
		if (cur == null) {
			Log.i("", "cur==null");
			return null;
		}
		while (!cur.isAfterLast()) {
			if (cur.isBeforeFirst()) {
				cur.moveToNext();
			} else {
				olineCard = read(cur);
				listArray.add(olineCard);
				cur.moveToNext();
				// break;
			}
		}
		cur.close();
		return listArray;
	}

	public JSONObject toJSONObject() {
		JSONObject object = new JSONObject();
		try {
			object.put(KEY_CARD, cardNum);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return object;
	}

	public static OlineCard readOlineCardFromDb(int cardNum) {
		ContentResolver mContentResolver = MZBookApplication.getInstance()
				.getContentResolver();
		OlineCard olineCard = null;
		String selection = null;
		Cursor cur;
		if (cardNum >= 0) {
			selection = "card_num" + " = " + "'" + cardNum + "'";
		}
		// String sortOrder = "mod_time" + " ASC";
		cur = mContentResolver
				.query(uri, ALL_PROJECTION, selection, null, null);// " DESC");
		if (cur == null) {
			Log.i("", "cur==null");
			return null;
		}
		while (!cur.isAfterLast()) {
			if (cur.isBeforeFirst()) {
				cur.moveToNext();
			} else {
				olineCard = read(cur);
				cur.moveToNext();
				break;
			}
		}
		cur.close();
		return olineCard;
	}

	public static OlineCard read(Cursor cur) {
		OlineCard olineCard = new OlineCard();
		int i = -1;
		olineCard.cardNum = cur.getString(++i);
		olineCard.availab = cur.getInt(++i);
		olineCard.start = cur.getInt(++i);
		String strEnd = cur.getString(++i);
		if (!TextUtils.isEmpty(strEnd)) {
			olineCard.end = Long.valueOf(OlineDesUtils.decrypt(strEnd));
		}

		return olineCard;
	}

	public static OlineCard parserOLineCard(JSONObject jsonObj) {
		OlineCard olineCard = new OlineCard();
		olineCard.cardNum = DataParser.getString(jsonObj, KEY_CARD_NO_1);
		if (TextUtils.isEmpty(olineCard.cardNum)) {
			olineCard.cardNum = DataParser.getString(jsonObj, KEY_CARD_NO_2);
		}
		String strEnd = DataParser.getString(jsonObj, OlineCard.KEY_ENTIME_1);
		if (TextUtils.isEmpty(strEnd)) {
			strEnd = DataParser.getString(jsonObj, OlineCard.KEY_ENTIME_2);
		}
		if (TextUtils.isEmpty(strEnd)) {
			strEnd = DataParser.getString(jsonObj, OlineCard.KEY_ENTIME_3);
		}
		if (!TextUtils.isEmpty(strEnd)) {
			olineCard.end = Long.valueOf(DesUtil.decrypt(strEnd,
					OnlineCardManeger.KEY));
		}
		olineCard.availab = DataParser.getInt(jsonObj, KEY_ISEXPIRE_1);

		return olineCard;
	}

	public static OlineCard parserOLineCardFromCert(JSONObject jsonObj) {
		OlineCard olineCard = new OlineCard();
		//畅读卡号
		olineCard.cardNum = DataParser.getString(jsonObj, KEY_CARD_NO_1);
		if (TextUtils.isEmpty(olineCard.cardNum)) {
			olineCard.cardNum = DataParser.getString(jsonObj, KEY_CARD_NO_2);
		}
		String strEnd = DataParser.getString(jsonObj, OlineCard.KEY_ENTIME_1);
		if (TextUtils.isEmpty(strEnd)) {
			strEnd = DataParser.getString(jsonObj, OlineCard.KEY_ENTIME_2);
		}
		if (TextUtils.isEmpty(strEnd)) {
			strEnd = DataParser.getString(jsonObj, OlineCard.KEY_ENTIME_3);
		}
		if (!TextUtils.isEmpty(strEnd)) {
			String timeEnd = DesUtil.decrypt(strEnd, OnlineCardManeger.KEY);
			olineCard.end = Long.valueOf(timeEnd);
		}

		boolean isExpire = false;
		boolean isParserExpired = false;
		if (!jsonObj.isNull(KEY_ISEXPIRE_1)) {
			isExpire = DataParser.getBoolean(jsonObj, KEY_ISEXPIRE_1);
			isParserExpired = true;
		}
		// 因为字段名太多
		if (!isParserExpired && !jsonObj.isNull(KEY_ISEXPIRE_2)) {
			isExpire = DataParser.getBoolean(jsonObj, KEY_ISEXPIRE_2);
		}

		if (isExpire) {
			olineCard.availab = 1;
		} else {
			olineCard.availab = 0;
		}

		return olineCard;
	}

}
