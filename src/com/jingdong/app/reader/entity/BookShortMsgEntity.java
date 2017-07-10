package com.jingdong.app.reader.entity;

import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

public class BookShortMsgEntity {
/*
 * 参数名称	是否必填	描述	示例
id	Y	资讯ID
title	Y	资讯标题
time	Y	发表咨询时间	2011-10-24 08:24:11
 */

	public  final static String  KEY_ID = "id";
	public  final static String  KEY_TITLE = "title";
	public  final static String  KEY_TIME = "time";
	public  final static String  KEY_URL = "url";

	public String id;
	public String title;
	public String time;
	public String url;
	public static final BookShortMsgEntity parse(JSONObject jSONObject) {
		BookShortMsgEntity entity = null;
		if (jSONObject != null) {
			try {
				entity = new BookShortMsgEntity();
				entity.id = DataParser.getString(jSONObject, BookShortMsgEntity.KEY_ID);
				entity.title =  DataParser.getString(jSONObject, BookShortMsgEntity.KEY_TITLE);
				entity.time =  DataParser.getString(jSONObject, BookShortMsgEntity.KEY_TIME);
				entity.url =  DataParser.getString(jSONObject, BookShortMsgEntity.KEY_URL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return entity;
	}

}
