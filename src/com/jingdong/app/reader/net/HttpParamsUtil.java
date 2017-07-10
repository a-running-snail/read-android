package com.jingdong.app.reader.net;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.jingdong.app.reader.bob.util.MD5Calculator;
import com.jingdong.app.reader.book.DocBind;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.data.DrmTools;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.pay.OnlinePayActivity;
import com.jingdong.app.reader.reading.BookMark;
import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.reading.ReadProgress;
import com.jingdong.app.reader.reading.ReadingData;
import com.jingdong.app.reader.service.ReadNoteSyncService;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.CommonUtil;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MD5Util;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.StatisticsReportUtil;

public class HttpParamsUtil {

	public static String KEY_CLIENTPLATFORM = "clientPlatform";
	public static int VALUE_CLIENTPLATFORM = 1;

	
	
	
	
	/*
	 *获取纸书商城
	 */
	public static String getPaperBookStoreListJsonBody(int page,
			int pageSize,boolean isnew) {

		final JSONObject json = new JSONObject();
		try {
			json.put("currentPage",page);
			json.put("pageSize", pageSize);
			json.put("boolNew", isnew);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();

}
	
	
	/*
	 *添加畅读列表
	 */
	public static String addToChangduListJsonBody(long ebookid) {

		return new String("{\"ebook_ids\":[{\"ebook_id\":" + ebookid + "}]}");

}

	/*
	 * 阅读书签同步
	 */
	public static String syncEBookReadProgressBookMark(LocalBook book) {
		if(null == book) {
			return "";
		}
		
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		JSONObject jbook = new JSONObject();
		try {
			json.put("userId", LoginUser.getpin());
			json.put("uuid", StatisticsReportUtil.readDeviceUUID());
			// XXX force为1是获取最后阅读位置，force为2是强制更新最后阅读位置
			jbook.put("force", 1);// XXX 新接口可以不用传
			jbook.put("ebookId", book.book_id);
			jbook.put("importBookId", 0);
			jbook.put("totalOffset", 0);
			if (LocalBook.SOURCE_BUYED_BOOK.equals(book.source)) {
				jbook.put("orderId", book.order_code);
			} else {
				jbook.put("orderId", book.book_id);
			}
			// XXX 特别注意，这里的ebookType:1epub，0为PDF
			if (book.format == LocalBook.FORMAT_EPUB) {
				jbook.put("ebookType", 1);
			} else if (book.format == LocalBook.FORMAT_PDF) {
				jbook.put("ebookType", 0);
			}
			long version = MZBookDatabase.instance.getBookMarksSyncTime(
					LoginUser.getpin(), book.book_id, 0);
			jbook.put("version", version);
			array.put(jbook);
			json.put("bookList", array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json.toString();
	}
	
	/*
	 * 阅读书签上传
	 */
	public static String uploadEBookReadProgressBookMark(LocalBook book) {
		JSONObject json = new JSONObject();
		if(null == book) {
			return json.toString();
		}
		
		JSONArray array = new JSONArray();
		JSONObject jbook = new JSONObject();
		JSONArray list = new JSONArray();
		ReadProgress progress = MZBookDatabase.instance.getEbookReadProgress(
				LoginUser.getpin(), book.book_id);
		List<BookMark> bookMarks = MZBookDatabase.instance.getAllUnsyncBookMarksOfBook(
				LoginUser.getpin(), book.book_id, 0);
		try {
			json.put("userId", LoginUser.getpin());
			json.put("uuid", StatisticsReportUtil.readDeviceUUID());
			// XXX force为1是获取最后阅读位置，force为2是强制更新最后阅读位置
			jbook.put("force", 2);// XXX 新接口可以不用传
			jbook.put("ebookId", book.book_id);
			jbook.put("importBookId", 0);
			jbook.put("totalOffset", 0);
			if (LocalBook.SOURCE_BUYED_BOOK.equals(book.source)) {
				jbook.put("orderId", book.order_code);
			} else {
				jbook.put("orderId", book.book_id);
			}
			// XXX 特别注意，这里的ebookType:1epub，0为PDF
			if (book.format == LocalBook.FORMAT_EPUB) {
				jbook.put("ebookType", 1);
			} else if (book.format == LocalBook.FORMAT_PDF) {
				jbook.put("ebookType", 0);
			}
			long version = MZBookDatabase.instance.getBookMarksSyncTime(
					LoginUser.getpin(), book.book_id, 0);
			jbook.put("version", version);
			if (progress != null) {
				JSONObject jp = ReadProgress.toJSON(progress);
				jp.put("ebookId", book.book_id);
				if (LocalBook.SOURCE_BUYED_BOOK.equals(book.source)) {
					jp.put("orderId", book.order_code);
				} else {
					jp.put("orderId", book.book_id);
				}
				list.put(jp);
			}
			if (bookMarks != null && bookMarks.size() > 0) {
				for (BookMark mark : bookMarks) {
					JSONObject jb = BookMark.toJSON(mark);
					jb.put("ebookId", book.book_id);
					if (LocalBook.SOURCE_BUYED_BOOK.equals(book.source)) {
						jb.put("orderId", book.order_code);
					} else {
						jb.put("orderId", book.book_id);
					}
					list.put(jb);
				}
			}
			jbook.put("list", list);
			array.put(jbook);
			json.put("bookList", array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json.toString();
	}
	
	/*
	 * 阅读书签同步
	 */
	public static String syncDocumentReadProgressBookMark(Document doc) {
		DocBind docBind = MZBookDatabase.instance.getDocBind(doc.documentId,
				LoginUser.getpin());
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		JSONObject jbook = new JSONObject();
		try {
			json.put("userId", LoginUser.getpin());
			json.put("uuid", StatisticsReportUtil.readDeviceUUID());
			// XXX force为1是获取最后阅读位置，force为2是强制更新最后阅读位置
			jbook.put("force", 1);// XXX 新接口可以不用传
			jbook.put("ebookId", 0);
			jbook.put("importBookId", String.valueOf(docBind.serverId));
			jbook.put("totalOffset", 0);
			jbook.put("orderId", docBind.serverId);
			// XXX 特别注意，这里的ebookType:1epub，0为PDF
			if (doc.format == LocalBook.FORMAT_EPUB) {
				jbook.put("ebookType", 1);
			} else if (doc.format == LocalBook.FORMAT_PDF) {
				jbook.put("ebookType", 0);
			}
			long version = MZBookDatabase.instance.getBookMarksSyncTime(
					LoginUser.getpin(), 0, doc.documentId);
			jbook.put("version", version);
			array.put(jbook);
			json.put("bookList", array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json.toString();
	}
	
	/*
	 * 阅读书签上传
	 */
	public static String uploadDocumentReadProgressBookMark(Document doc) {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		JSONObject jbook = new JSONObject();
		JSONArray list = new JSONArray();
		DocBind docBind = MZBookDatabase.instance.getDocBind(doc.documentId,
				LoginUser.getpin());
		ReadProgress progress = MZBookDatabase.instance.getDocReadProgress(
				LoginUser.getpin(), doc.documentId);
		List<BookMark> bookMarks = MZBookDatabase.instance.getAllUnsyncBookMarksOfBook(
				LoginUser.getpin(), 0, doc.documentId);
		try {
			json.put("userId", LoginUser.getpin());
			json.put("uuid", StatisticsReportUtil.readDeviceUUID());
			// XXX force为1是获取最后阅读位置，force为2是强制更新最后阅读位置
			jbook.put("force", 2);// XXX 新接口可以不用传
			jbook.put("ebookId", 0);
			jbook.put("importBookId", String.valueOf(docBind.serverId));
			jbook.put("totalOffset", 0);
			jbook.put("orderId", docBind.serverId);
			// XXX 特别注意，这里的ebookType:1epub，0为PDF
			if (doc.format == LocalBook.FORMAT_EPUB) {
				jbook.put("ebookType", 1);
			} else if (doc.format == LocalBook.FORMAT_PDF) {
				jbook.put("ebookType", 0);
			}
			long version = MZBookDatabase.instance.getBookMarksSyncTime(
					LoginUser.getpin(), 0, doc.documentId);
			jbook.put("version", version);
			if (progress != null) {
				JSONObject jp = ReadProgress.toJSON(progress);
				jbook.put("importBookId", String.valueOf(docBind.serverId));
				jbook.put("orderId", docBind.serverId);
				list.put(jp);
			}
			if (bookMarks != null && bookMarks.size() > 0) {
				for (BookMark mark : bookMarks) {
					JSONObject jb = BookMark.toJSON(mark);
					jbook.put("importBookId", docBind.serverId);
					jbook.put("orderId", docBind.serverId);
					list.put(jb);
				}
			}
			jbook.put("list", list);
			array.put(jbook);
			json.put("bookList", array);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return json.toString();
	}
	
	// 上传笔记
	public static String uploadReadNote(List<ReadNote> noteList) {
		JSONObject o = new JSONObject();
		if (noteList != null && noteList.size() > 0) {
			try {
				JSONArray arr = new JSONArray();
				for (ReadNote note : noteList) {
					arr.put(ReadNoteSyncService.buildActionNode(note));
				}
				o.put("notes", arr);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return o.toString();
	}
	
	// 批量上传阅读时间
	public static String uploadBatchReadingData(List<ReadingData> list) {
		JSONArray arr = new JSONArray();
		try {
			for (ReadingData read : list) {
				if (read.getEbook_id() == 0 && read.getDocBindId() == 0) {
					continue;
				}
				JSONObject obj = new JSONObject();
				obj.put("book_id", read.getEbook_id());
				obj.put("document_id", read.getDocBindId());
				obj.put("start_time", read.getStart_time());
				obj.put("start_chapter", read.getStart_chapter());
				obj.put("start_para_idx", read.getStart_para_idx());
				obj.put("start_pdf_page", read.getStart_pdf_page());
				obj.put("end_time", read.getEnd_time());
				obj.put("end_chapter", read.getEnd_chapter());
				obj.put("end_para_idx", read.getEnd_para_idx());
				obj.put("end_pdf_page", read.getEnd_pdf_page());
				obj.put("length", read.getLength());
				arr.put(obj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return arr.toString();
	}
	
	/*
	 *借阅
	 */
	public static String borrowBookJsonBody(long ebookid) {
		final JSONObject json = new JSONObject();
		try {
			json.put("ebookId", ebookid);
			json.put("pin", LoginUser.getpin());

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();

	}
	
	public static String payLoginBody() {
		final JSONObject json = new JSONObject();
		try {
			json.put("loginname", LoginUser.getUserName());
			json.put("loginpwd", LoginUser.getpsw());

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	
	/*
	 * 限时借阅接口
	 */
	public static String getBookStoreBorrowingJsonBody(int page, int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("platformId", 1);
			json.put("currentPage", page);
			json.put("pageSize", pageSize);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();

	}

	/*
	 * 获取模块更多数据
	 */
	public static String getBookStoreRelateJsonBody(int fid, int ftype,
			int relationType, int page, int pageSize,String type) {
		final JSONObject json = new JSONObject();
		try {
			json.put("fid", fid);
			json.put("ftype", ftype);
			json.put("relationType", relationType);
			json.put("page", page);
			json.put("pageSize", pageSize);
			json.put("type", type);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();

	}

	/*
	 * 获取子模块
	 */
	public static String getChildModuleJsonBody(int id, int moduleType) {

		final JSONObject json = new JSONObject();
		try {
			json.put("id", id);
			json.put("moduleType", moduleType);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();

	}

	/*
	 * 价格检查
	 */
	public static String getBookCartJsonBody(List<Map<String, String>> list) {

		String result = GsonUtils.toJson(list);

		return new String("{\"TheSkus\":" + result + "}");
	}

	/*
	 * 获取其他人购买喜欢
	 */
	public static String getBookOtherLikesJsonBody(long ebookid) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookId", ebookid);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 获取书评列表
	 */
	public static String getBookCommentsJsonBody(String booktype, long ebookid,
			String currentpage) {
		final JSONObject json = new JSONObject();
		try {
			if (booktype.equals("ebook"))
				json.put("eBookId", ebookid);
			else {
				json.put("paperBookId", ebookid);
			}
			json.put("currentPage", currentpage);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 获取读过本书用户列表
	 */
	public static String getBookReadedJsonBody(long bookId,String currentpage,String pageSize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookId", bookId);
			json.put("currentPage", currentpage);
			json.put("pageSize", pageSize);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	/*
	 * 获取用户
	 */
	public static String getUserJsonBody(String searchType,String pin) {
		final JSONObject json = new JSONObject();
		try {
			json.put("searchType", searchType);
			json.put("pin", pin);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	/*
	 * 书籍检查
	 */
	public static String getBookverifyJsonBody(String userId, String ebookId,
			String orderId,boolean isborrow, boolean isBorrowBuy) {
		JSONObject json = new JSONObject();
		try {
			json.put("userId", userId);
			json.put("ebookId", ebookId);
			json.put("pdf", 1);
			if(isBorrowBuy) {
				json.put("isBorrowBuy", true);
			}else {
				if(isborrow)
					json.put("isBorrow", "true");
			}
			
			if (!TextUtils.isEmpty(orderId) && !orderId.equals("-1")) {
				json.put("orderId", orderId);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json.toString();
	}

	/*
	 * 登录
	 */
	public static String getUserLoginJsonBody(String userName, String passWord,String verification) {
		JSONObject json = new JSONObject();
		try {
			json.put("loginname", userName);
			json.put("loginpwd", passWord);
			if(!TextUtils.isEmpty(verification))
				json.put("validateCode", verification);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json.toString();
	}

	public static String getTokenBody() {
		JSONObject json = new JSONObject();
		try {
			JSONArray bodyjsonarray = new JSONArray();
			for (int i = 0; i < OnlinePayActivity.payidList.size(); i++) 
			{
				JSONObject tempitemobject = new JSONObject();
				tempitemobject.put("num", "1");// 购买畅读卡的数量。
				tempitemobject.put("Id",OnlinePayActivity.payidList.get(i));
				bodyjsonarray.put(i, tempitemobject);
			}
			json.put("TheSkus", bodyjsonarray);
			json.put("singleUnionId", "");     
			json.put("singleSubUnionId", "");  
			json.put("isSupportJs", "true"); 

		} catch (JSONException e) {
			e.printStackTrace();
		}
		MZLog.d("J.Beyond", json.toString());
		return json.toString();
	}
	public static String getTokenBody(String id) {
		try {

			JSONObject tempitemobject = new JSONObject();
			tempitemobject.put("order_id",id);
			return tempitemobject.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 购书赠书接口body
	 * @return
	 */
	public static String getSendBookTokenBody() {
		JSONObject json = new JSONObject();
		try {
			JSONArray bodyjsonarray = new JSONArray();
			for (int i = 0; i < OnlinePayActivity.payidList.size(); i++) 
			{
				JSONObject tempitemobject = new JSONObject();
				tempitemobject.put("num", "1");// 购买数量。
				tempitemobject.put("Id",OnlinePayActivity.payidList.get(i));
				bodyjsonarray.put(i, tempitemobject);
			}
			json.put("TheSkus", bodyjsonarray);
			json.put("singleUnionId", "");     
			json.put("singleSubUnionId", "");  
			json.put("isSupportJs", "true"); 
			json.put("businessType", 2); 

		} catch (JSONException e) {
			e.printStackTrace();
		}
		MZLog.d("J.Beyond", json.toString());
		return json.toString();
	}
	/**
	 * 购书赠书接口body
	 * @return
	 */
	public static String getSendBookTokenBody(String id) {
		try {

			JSONObject tempitemobject = new JSONObject();
			tempitemobject.put("order_id",id);
			tempitemobject.put("businessType", 2); 
			return tempitemobject.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String getCPABody(String token,String unionId,String info) {
		JSONObject jsobj = new JSONObject();
		try {
			jsobj.put("token", token);
			jsobj.put("unionId", unionId);
			jsobj.put("info", info);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsobj.toString();
	}
	/*
	 * 获取验证码
	 */

	/*
	 * 获取对称加密KEY
	 */
	public static String getSessionKeyJsonBody(String envelopeKey) {
		final JSONObject json = new JSONObject();
		try {
			json.put("envelopeKey", envelopeKey);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 获取验证码
	 */
	public static String getVerifyCodeJsonBody(String phone) {
		final JSONObject json = new JSONObject();
		try {
			json.put("phoneNum", phone);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	/*
	 * 获取验证码
	 */
	public static String getPhoneNumBody(String phone) {
		final JSONObject json = new JSONObject();
		try {
			json.put("phoneNumber", phone);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 获取注册参数
	 */
	public static String getRegisterJsonBody(String phone, String code,
			String pwd) {
		final JSONObject json = new JSONObject();
		try {
			json.put("emInfo", phone);
			json.put("validateCode", code);
			json.put("pwd", pwd);
			json.put("osType", "14");
			json.put("terminalType", "02");
			json.put("uuid", StatisticsReportUtil.readDeviceUUID());

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 获取已购列表
	 */
	public static String getBuyedEbookJsonBody(String currentpage,
			String pagesize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("currentPage", currentpage);
			json.put("pageSize", pagesize);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 包月卡
	 */
	public static String getMonthlyJsonBody() {
		final JSONObject json = new JSONObject();
		try {
			json.put("currentPage", 1);
			json.put("pageSize", 1);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	/*
	 * 取消服务
	 */
	public static String getBuyJsonBody() {
		final JSONObject json = new JSONObject();
		try {
			json.put("charge", "true");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	/*
	 * 取消服务
	 */
	public static String getCancleServiceJsonBody(Integer serviceid) {
		final JSONObject json = new JSONObject();
		try {
			json.put("serverId", serviceid);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	/*
	 * 获取畅读列表
	 */
	public static String getChangduEbookJsonBody(String currentpage,
			String pagesize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("currentPage", currentpage);
			json.put("pageSize", pagesize);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 搜索畅读列表
	 */
	public static String searchChangduEbookJsonBody(String currentpage,
			String pagesize, String bookNameOrAuthor) {
		final JSONObject json = new JSONObject();
		try {
			json.put("currentPage", currentpage);
			json.put("pageSize", pagesize);
			if (!TextUtils.isEmpty(bookNameOrAuthor)) {
				json.put("bookNameOrAuthor", bookNameOrAuthor);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 获取搜索已购列表
	 */
	public static String getSearchBuyedEbookJsonBody(String key,
			String currentpage, String pagesize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("key", key);
			json.put("currentPage", currentpage);
			json.put("pageSize", pagesize);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	/*
	 * 设备绑定
	 */
	public static String getBindJsonBody() {
		final JSONObject json = new JSONObject();
		try {
			String hardwareUUID = DrmTools.hashDevicesInfor();
			Build bd = new Build();  
			String model = bd.MODEL; 
			try {
				json.put("userId", URLEncoder.encode(LoginUser.getpin(), "GBK"));
				json.put("hardwareUUID", hardwareUUID);
				json.put("deviceModel", model);
				json.put("deviceType", "A");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 获取搜索书城列表
	 */
	public static String getSearchStoreEbookJsonBody(String currentpage,
			String pagesize, String key,String searchPaperBook) {
		final JSONObject json = new JSONObject();
		try {
			json.put("sortType", "sort_sale_desc");
			json.put("pageSize", pagesize);
			json.put("bookType", 1 + "");
			json.put("currentPage", currentpage);
			Log.d("cj", "currentSearchPage===========>>>>>>>" +currentpage);
			json.put("keyword", key);
			json.put("searchPaperBook", searchPaperBook);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	
	public static String getSearchStoreEbookJsonBody(String keyword, String type) {
		final JSONObject json = new JSONObject();
		try {
			String keywordUTF8 = new String(keyword.getBytes(), "UTF-8");
			json.put("keyWord", keywordUTF8);
			json.put("type", type);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return json.toString();
	}
	
	public static String getSearchStoreEbookJsonBody(String keyword, String bookid, String type) {
		final JSONObject json = new JSONObject();
		try {
			String keywordUTF8 = new String(keyword.getBytes(), "UTF-8");
			json.put("keyWord", keywordUTF8);
			json.put("bookId", bookid);
			json.put("type", type);
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		return json.toString();
	}

	/*
	 * 获取订单列表
	 */
	public static String getOrderJsonBody(String currentpage, String pagesize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("pageSize", pagesize);
			;
			json.put("currentPage", currentpage);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 获取阅历-用户读过的书
	 */
	public static String getUserReadedBookJsonBody(String currentpage, String pagesize,String supportDate) {
		final JSONObject json = new JSONObject();
		try {
			json.put("pageSize", pagesize);
			json.put("currentPage", currentpage);
			json.put("supportDate", supportDate);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

    
    /*
     * 获取用户信息
     */
    public static String getUserInfoJsonBody(String pin) {
        final JSONObject json = new JSONObject();
        try {
            json.put("listPin", pin);   
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
    
    /*
     * 获取用户借阅列表
     */
    public static String getBorrowedEbookJsonBody(String currentpage,String pagesize) {
        final JSONObject json = new JSONObject();
        try {
            json.put("page", currentpage);
            json.put("pageSize", pagesize);          
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
    
    /*
     * 获取书城借阅列表
     */
    public static String getStoreBorrowedEbookJsonBody(String currentpage,String pagesize) {
        final JSONObject json = new JSONObject();
        try {
            json.put("currentPage", currentpage);
            json.put("pageSize", pagesize);          
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }

  
    
    /*
     * 获取订单详情列表
     */
    public static String getOrderDetailJsonBody(String orderid,
			String currentpage) {
        final JSONObject json = new JSONObject();
        try {
            json.put("orderId", orderid);;
            json.put("currentPage", currentpage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
    
    //缺书登记
    public static String getLockBookJsonBody(String type,String memo, String mobile, String qq) {
        final JSONObject json = new JSONObject();
        try {
            json.put("memo", memo);
            json.put("qtype", type);
            json.put("mobile", mobile);
            json.put("qq", qq);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
    
    //用户头像
    public static String putUserImageJsonBody(String imageString) {
        final JSONObject json = new JSONObject();
        try {
            json.put("imgStr", imageString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
    
    //修改信息
    public static String putUserInfopJsonBody(String usex,String nickname,String uremark) {
        final JSONObject json = new JSONObject();
        try {
        	json.put("usex", usex);
        	json.put("nickname", nickname);
        	json.put("uremark", uremark);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
    
    //绑定畅读卡
    public static String putReadingCardJsonBody(String cardNo,String cardPwd) {
        final JSONObject json = new JSONObject();
        try {
        	json.put("cardNo", cardNo);
        	json.put("cardPwd", MD5Calculator.calculateMD5(cardPwd.toUpperCase()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
    }
    
    //获取条形码搜索列表
    public static String getRecommendListJsonBody(int rtype,
			int currentPage, int pageSize) {
        final JSONObject json = new JSONObject();
        try {
			json.put("rtype", rtype);
			json.put("currentPage", currentPage);
			json.put("pageSize", pageSize);
			json.put(KEY_CLIENTPLATFORM, VALUE_CLIENTPLATFORM);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	public static String getadListJsonBody(int rtype) {
		final JSONObject json = new JSONObject();
		try {
			json.put("rtype", rtype);
			json.put(KEY_CLIENTPLATFORM, VALUE_CLIENTPLATFORM);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 获取分类列表
	 */
	public static String getCategoryBookListJsonBody(String pageSize,
			String catId, String currentPage) {
		final JSONObject json = new JSONObject();
		try {
			json.put("sortType", 1);
			json.put("pageSize", pageSize);
			json.put("catId", catId);
			json.put("currentPage", currentPage);
			json.put("sortKey", 1);
			json.put("clientPlatform", 1);
			json.put("rootId", 2);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 获取热词列表
	 */
	public static String getHotKeyWordJsonBody(String total,String type) {
		final JSONObject json = new JSONObject();
		try {
			json.put("total", total);
			json.put("type", type);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	

	/*
	 * 获取分类列表
	 */
	public static String getCategoryBookJsonBody(int clientPlatform) {
		final JSONObject json = new JSONObject();
		try {
			json.put("clientPlatform", clientPlatform);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

	/*
	 * 登陆打通
	 */
	public static String getMergeUrlJsonBody(String url) {
		final JSONObject json = new JSONObject();
		try {
			json.put("to", url);
			json.put("action", "to");
//			json.put("pin", LoginUser.getpin());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}


	public static String getCPSBody(String unionId, String unionSiteId) {
		JSONObject jsobj = new JSONObject();
		try {
			jsobj.put("unionId", unionId);
			jsobj.put("subunionId", unionSiteId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsobj.toString();
	}
	public static String getIsHaveGiftBagBody() {
		JSONObject jsobj = new JSONObject();
		try {
			String unionId =String.valueOf(Configuration.getProperty(Configuration.UNION_ID, ""));
			String unionSiteId =String.valueOf(Configuration.getProperty(Configuration.UNION_SITE_ID, ""));
			jsobj.put("unionId", CommonUtil.getPropertiesValue("partnerID"));
			jsobj.put("subunionId", CommonUtil.getPropertiesValue("subPartnerID"));
			jsobj.put("uuid", StatisticsReportUtil.readDeviceUUID());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsobj.toString();
	}
	public static String getGiftBagBody(String giftPacksId) {
		JSONObject jsobj = new JSONObject();
		try {
			String unionId =String.valueOf(Configuration.getProperty(Configuration.UNION_ID, ""));
			String unionSiteId =String.valueOf(Configuration.getProperty(Configuration.UNION_SITE_ID, ""));
			jsobj.put("unionId", CommonUtil.getPropertiesValue("partnerID"));
			jsobj.put("subunionId", CommonUtil.getPropertiesValue("subPartnerID"));
			jsobj.put("uuid", StatisticsReportUtil.readDeviceUUID());
			jsobj.put("giftPacksId",giftPacksId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String message=jsobj.toString();
		return message;
	}


	public static String getWalletBody() {
		JSONObject jsobj = new JSONObject();
		try {
			jsobj.put("walletType", 0);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsobj.toString();
	}
	
	public static String getShoppingCartBody(String bookList,String type) {
		JSONObject jsobj = new JSONObject();
		try {
			jsobj.put("bookList", bookList);
			jsobj.put("type", type);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsobj.toString();
	}


	public static String getLastWeekScoreBody(int currentPage, int pageSize) {
		JSONObject jsobj = new JSONObject();
		try {
			jsobj.put("currentPage", currentPage);
			jsobj.put("pageSize", pageSize);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsobj.toString();
	}


	public static String getCommentGetScoreBody(long bookId) {
		JSONObject jsobj = new JSONObject();
		try {
			jsobj.put("bookId", bookId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsobj.toString();
	}


	public static String getReadTimeGetScoreBody(long bookId, int readTime) {
		JSONObject jsobj = new JSONObject();
		try {
			jsobj.put("bookId", bookId);
			jsobj.put("readTime", readTime);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsobj.toString();
	}


	public static String getGiftByScoreBody(int giftId) {
		JSONObject jsobj = new JSONObject();
		try {
			jsobj.put("giftId", giftId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsobj.toString();
	}


	public static String exchangeBindBody(String exchangeCode) {
		final JSONObject json = new JSONObject();
        try {
        	json.put("cdKey", exchangeCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
	}


	public static String getGuessYouLikeBody(int currentPage) {
		final JSONObject json = new JSONObject();
        try {
        	json.put("currentPage", currentPage+"");
        	json.put("pageSize", "27");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json.toString();
	}

	



}
