package com.jingdong.app.reader.reading;

import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.user.LoginUser;

public class BookMark {

	public int id = 0;
	public long ebookid = 0;
	public int docid = 0;
	public int offset_in_para = 0;
	public int para_index = 0;
	public long updated_at = System.currentTimeMillis()/1000;//秒
	public long server_id = 0;
	public String userid = "";
	public int operation_state = 0;// operating_state 1修改 0无修改 2添加 3删除
	public int pdf_page = 0;
	public String digest = "";
	public String chapter_title = "";
	public String chapter_itemref = "";
	public int chapterId = 0;//用于本地书签排序
	public int isSync = 0;//0-未同步  1-同步过
	public int bookType;// 书的格式 1-pdf类型 2-epub类型
	public long createTime = System.currentTimeMillis()/1000;
	
	public static BookMark fromJSON(JSONObject json) {
		BookMark bookMark = new BookMark();
		bookMark.userid = LoginUser.getpin();
		bookMark.server_id = json.optLong("id");
		bookMark.ebookid = json.optInt("ebookId");
		// XXX 后台返回的importBookId与id是一个值，所以docId客户端自己处理
		bookMark.chapter_itemref = json.optString("epubItem");
		bookMark.chapter_title = json.optString("epubChapterTitle");
		int type = json.optInt("ebookType");
        if (type == 1) {
        	bookMark.bookType = LocalBook.FORMAT_EPUB;
        } else if (type == 0) {
        	bookMark.bookType = LocalBook.FORMAT_PDF;
        }
        bookMark.pdf_page = json.optInt("offset");
		bookMark.para_index = json.optInt("x1");
		bookMark.offset_in_para = json.optInt("y1");
		bookMark.digest = json.optString("note");
		bookMark.updated_at = json.optLong("version");
		bookMark.createTime = json.optLong("deviceTime");
		return bookMark;
	}
	
	public static JSONObject toJSON(BookMark mark) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("note", mark.digest);
    	json.put("epubItem", mark.chapter_itemref);
    	json.put("valid", mark.operation_state == 3 ? 0 : 1);
    	json.put("force", 2);// XXX force为2是强制更新最后阅读位置，新接口可以不用传
    	if (mark.bookType == LocalBook.FORMAT_EPUB) {
    		json.put("ebookType", 1);
    	} else if (mark.bookType == LocalBook.FORMAT_PDF) {
    		json.put("ebookType", 0);
    	}
    	json.put("dataType", 1);
    	json.put("x1", mark.para_index);
    	json.put("y1", mark.offset_in_para);
    	json.put("deviceTime", mark.updated_at);
    	json.put("percent", "0");
    	json.put("offset", mark.pdf_page);
    	json.put("pdfScaling", "1");
    	json.put("pdfScalingLeft", "0");
    	json.put("pdfScalingTop", "0");
    	json.put("epubChapterTitle", mark.chapter_title);
    	return json;
	}
}
