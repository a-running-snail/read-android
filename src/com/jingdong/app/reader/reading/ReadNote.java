package com.jingdong.app.reader.reading;

import org.json.JSONObject;


public class ReadNote {
    public String userId = "";
    public long ebookId = 0;
    public int documentId = 0;
    public long docBindId = 0;
    
    public String spineIdRef;
    public String chapterName;
    public int chapterId;//笔记本地排序使用
    public int id = 0;
    public long serverId = -1;
    public String guid;//entity_guid
    public String quoteText = "";
    public String contentText = "";
    public int fromParaIndex;
    public int fromOffsetInPara;
    public int toParaIndex;
    public int toOffsetInPara;
    public long updateTime;
    public boolean isPrivate = true;
    public boolean modified = true;
    public boolean deleted = false;
    public int pdfPage = 0;
    public float pdf_yoffset = 0;
    public float pdf_xoffset= 0;
    
    //阅读他人笔记的赞相关逻辑
    public boolean isRecommanded = false;
    public boolean isSyncReccommandStatus = true;

    
    
    @Override
	public String toString() {
		return "ReadNote [userId=" + userId + ", ebookId=" + ebookId + ", documentId=" + documentId + ", docBindId=" + docBindId + ", spineIdRef=" + spineIdRef
				+ ", chapterName=" + chapterName + ", chapterId=" + chapterId + ", id=" + id + ", serverId=" + serverId + ", guid=" + guid + ", quoteText="
				+ quoteText + ", contentText=" + contentText + ", fromParaIndex=" + fromParaIndex + ", fromOffsetInPara=" + fromOffsetInPara + ", toParaIndex="
				+ toParaIndex + ", toOffsetInPara=" + toOffsetInPara + ", updateTime=" + updateTime + ", isPrivate=" + isPrivate + ", modified=" + modified
				+ ", deleted=" + deleted + ", pdfPage=" + pdfPage + ", pdf_yoffset=" + pdf_yoffset + ", pdf_xoffset=" + pdf_xoffset + ", isRecommanded="
				+ isRecommanded + ", isSyncReccommandStatus=" + isSyncReccommandStatus + "]";
	}

	public static ReadNote parseFromJson(JSONObject notesObject, int documentid){
    	ReadNote note = new ReadNote();

		note.userId = notesObject.optString("user_id");
		note.ebookId = notesObject.optLong("book_id");
		note.documentId = documentid;// 本地DocumentId
		note.serverId = notesObject.optLong("id");
		note.guid = notesObject.optString("entity_guid");

		note.chapterName = notesObject
				.optString("chapter_name");
		note.fromParaIndex = notesObject
				.optInt("from_para_index");
		note.fromOffsetInPara = notesObject
				.optInt("from_offset_in_para");
		note.toParaIndex = notesObject.optInt("to_para_index");
		note.toOffsetInPara = notesObject
				.optInt("to_offset_in_para");
		note.spineIdRef = notesObject
				.optString("chapter_itemref");

		note.quoteText = notesObject.optString("quote_text");
		note.contentText = notesObject.optString("content");
		note.updateTime = notesObject
				.optLong("updated_at_timestamp") * 1000;
		note.isPrivate = notesObject.optBoolean("is_private");
		return note;
    }
    
    public static ReadNote parseMyNoteFromJson(JSONObject notesObject, int documentid){
    	ReadNote note = new ReadNote();

		note.userId = notesObject.optString("jd_user_name");
		note.ebookId = notesObject.optLong("book_id");
		note.documentId = documentid;// 本地DocumentId
		note.serverId = notesObject.optLong("id");
		note.guid = notesObject.optString("entity_guid");

		note.chapterName = notesObject
				.optString("chapter_name");
		note.fromParaIndex = notesObject
				.optInt("from_para_index");
		note.fromOffsetInPara = notesObject
				.optInt("from_offset_in_para");
		note.toParaIndex = notesObject.optInt("to_para_index");
		note.toOffsetInPara = notesObject
				.optInt("to_offset_in_para");
		note.spineIdRef = notesObject
				.optString("chapter_itemref");

		note.quoteText = notesObject.optString("quote_text");
		note.contentText = notesObject.optString("content");
		note.updateTime = notesObject
				.optLong("updated_at_timestamp") * 1000;
		int isPrivate = notesObject.optInt("is_private", 1);
		note.isPrivate = isPrivate == 1;
		return note;
    }
}
