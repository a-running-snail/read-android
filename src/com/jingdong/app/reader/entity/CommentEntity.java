package com.jingdong.app.reader.entity;

import org.json.JSONObject;

import com.jingdong.app.reader.data.DataParser;

public class CommentEntity {
	public final static String KEY_TITLE= "title";
	public final static String KEY_STAR= "star";
	public final static String KEY_USERPIN = "userPin";
	public final static String KEY_TIME = "time";
	public final static String KEY_CONTENT = "content";
	public String title;
	public int star;
	public String userPin;
	public String time;
	public String content;


	public static final CommentEntity parse(JSONObject jsonObject) {
		CommentEntity entity = null;
		if (jsonObject != null) {
			       try {
			    	   entity = new CommentEntity();
			    	   entity.title = DataParser.getString(jsonObject, CommentEntity.KEY_TITLE);
			    	   entity.star = DataParser.getInt(jsonObject, CommentEntity.KEY_STAR);
			    	   entity.userPin = DataParser.getString(jsonObject, CommentEntity.KEY_USERPIN);
			    	   entity.time  = DataParser.getString(jsonObject, CommentEntity.KEY_TIME);
			    	   entity.content = DataParser.getString(jsonObject, CommentEntity.KEY_CONTENT);
			       } catch (Exception e) {
						e.printStackTrace();
					}
        }
		return entity;
         }
}
