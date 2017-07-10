package com.jingdong.app.reader.notes;

import org.json.JSONObject;


public class NotesModel {
	
	
	public static final int DOWNLOAD = 0;
	public static final int DOWNLOADING = 1;
	public static final int DOWNLOADED = 2;
	
	public static final int SELECTED = 3;
	public static final int UNSELECTED = 4;

	public String userid;
	public int role;
	public String avatarUrl;
	public String userName;
	public String jd_user_name;
	public int noteCount;
	public int contentSize;
	public int state;
	
	//用于跳转阅历
	public int documentid;
	public long bookid;
	
	
	public static NotesModel parseFromJson(JSONObject object) {
		NotesModel model = new NotesModel();
		model.noteCount = object.optInt("note_count");
		model.contentSize = object.optInt("content_size");
		JSONObject userObject = object.optJSONObject("user");
		model.jd_user_name = userObject.optString("jd_user_name");
		model.userName = userObject.optString("name");
		model.role = userObject.optInt("role");
		model.userid = userObject.optString("id");//为了兼容拇指老数据，用id而不是jd_user_name
		model.avatarUrl = userObject.optString("avatar");
		model.documentid = object.optInt("document_id");//FIXME
		model.bookid = object.optLong("book_id");
		return model;
	}
}
