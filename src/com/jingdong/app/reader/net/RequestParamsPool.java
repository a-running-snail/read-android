package com.jingdong.app.reader.net;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DownloadManager.Request;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.JsonObject;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.data.DrmTools;
import com.jingdong.app.reader.entity.BootEntity;
import com.jingdong.app.reader.entity.LocalBook;
import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.reading.ReadingData;
import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
import com.jingdong.app.reader.timeline.actiivity.TimelinePostTweetActivity;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.CommonUtil;
import com.jingdong.app.reader.util.DeviceUtil;
import com.jingdong.app.reader.util.MD5Calculator;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.RsaEncoder;
import com.jingdong.app.reader.util.StatisticsReportUtil;
import com.loopj.android.http.RequestParams;

public class RequestParamsPool {

	public static String DBOOK_BASE_URL1 = "rights.ebook.360buy.net";
	public static String DBOOK_BASE_URL2 = "ebook-drm.360buy.net";
	public final static String DBOOK_BASE_IP_TEST = Configuration.getProperty(
			Configuration.DBOOK_HOST).trim();
	public final static HashMap<String, String> sHashMap = new HashMap<String, String>();
	static {
		sHashMap.put(DBOOK_BASE_URL1,
				Configuration.getProperty(Configuration.DBOOK_HOST).trim());
		sHashMap.put(DBOOK_BASE_URL2,
				Configuration.getProperty(Configuration.DBOOK_HOST).trim());
	}

	private static String encryptParams(String data) {
		String bodyEncoder = RsaEncoder.stringBodyEncoder(data,
				RsaEncoder.getEncodeEntity());
		MZLog.d("wangguodong", "加密body:" + bodyEncoder);
		return bodyEncoder;
	}

	public static String url2IP(String url) {

		Set<String> keys = sHashMap.keySet();
		for (String key : keys) {
			String value = sHashMap.get(key);
			url = url.replace(key, value);
		}
		return url;
	}

	public static HttpSetting getBookverifyHttpSetting(String userId,
			String ebookId, String orderId,boolean isborrow) {
		final JSONObject json = new JSONObject();
		HttpSetting httpSetting = new HttpSetting();
		try {
			json.put("userId", userId);
			json.put("ebookId", ebookId);
			json.put("pdf", 1);
			
			if(isborrow)
				json.put("isBorrow", "true");
			
			if (!TextUtils.isEmpty(orderId) && !orderId.equals("-1")) {
				json.put("orderId", orderId);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String url = "http://"
				+ Configuration.getProperty(Configuration.DBOOK_HOST).trim()
				+ "/client.action";
		httpSetting.setUrl(url);
		httpSetting.setJsonParams(json);
		httpSetting.setFunctionId("verify");
		return httpSetting;
	}
	

	// 图书检查verify
	public static RequestParams getBookverifyParams(String userId,
			String ebookId, String orderId,boolean isborrow, boolean isBorrowBuy) {
		RequestParams params = new RequestParams();
		params.put("functionId", "verify");
		String body = HttpParamsUtil.getBookverifyJsonBody(userId, ebookId,
				orderId,isborrow, isBorrowBuy);
		params.put("body", body);
		fillRequest(params);

		return params;

	}
	
	public static RequestParams getOtherReadingParams() {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("count", 20);
		fillRequest(params);
		return params;

	}




	public static HttpUriRequest getBookUrlRequest(BootEntity boot) {
		HttpUriRequest request = null;
		String url = "";
		request = new HttpGet();
		Map<String, String> params = new HashMap<String, String>();
		try {
			JSONObject json = boot.getContentJosh();
			JSONObject body = json.getJSONObject("body");
			String hardwareUUID = DrmTools.hashDevicesInfor();
			body.put("uuid", hardwareUUID);
			url = json.getString("url");
			try {
				params.put("body", URLEncoder.encode(body.toString(), "utf-8"));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			request = getRequestWithParams(params, url);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return request;
	}

	public static HttpUriRequest getCertRequest(BootEntity boot, String uuid,
			boolean israndom,boolean isborrow, boolean isBorrowBuy) {
		HttpUriRequest request = null;
		String url = "";
		request = new HttpGet();
		Map<String, String> params = new HashMap<String, String>();
		JSONObject body = null;
		try {
			JSONObject bodyJson = boot.getCertJosh();
			body = bodyJson.getJSONObject("body");
			body.put("deviceModel", DeviceUtil.getBranchAndModel());
			body.put("uuid", uuid);
			if (israndom) {
				body.put("hasRandom", "0");
			} else {
				body.put("hasRandom", "1");
			}
			
			if(isBorrowBuy) { //用户借阅
				body.put("isBorrowBuy", true);
			}else {
				if(isborrow) //书城借阅书籍
					body.put("isBorrow", true);
			}
			
			
			body.put("hasCert", "0");
			body.put("userId", LoginUser.getpin());// 验证用户名。
			body.put("deviceType", "A");// Type A：android

			try {
				params.put("body", URLEncoder.encode(body.toString(), "utf-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			url = bodyJson.getString("url");
			request = getRequestWithParams(params, url);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return request;
	}

	public static HttpUriRequest getRequestWithParams(
			Map<String, String> params, String url) {
		StringBuilder buf = new StringBuilder(url);
		params.putAll(fillRequest());
		if (params != null) {
			boolean first = url.indexOf('?') == -1;
			for (String key : params.keySet()) {
				// if(key.equals("screen")){
				// continue;
				// }
				String value = params.get(key);
				if (first) {
					buf.append('?');
				} else {
					buf.append('&');
				}
				buf.append(key).append('=').append(value);
			}
		}
		url = buf.toString();
		url = url2IP(url);
		HttpUriRequest request = new HttpGet(url);
		setCookie(request);
		return request;
	}

	private static void setCookie(HttpUriRequest request) {
		String cookie = WebRequestHelper.getCookies();
		if (cookie != null) {
			request.addHeader("Cookie", cookie);
		}
	}

	//书城框架 子模块数据
//	参数含义：
//	fid:
//	要获取图书的父节点的id
//	ftype:
//	父节点类型：
//	1:父节点为子模块（外链）：当子模块为广告或者专题标签时; 
//	2:父节点为子模块（书）：当子模块为推荐、特辑或者显示免费时;
//	relationType： 1图书列表, 3图书类目（只有广告有3图书类目，其他都为1）;
//	page：默认为1
//	pageSize：默认为20

	public static RequestParams getBookStoreRelateParams(int fid,int ftype,int relationType,int page,int pageSize,String type) {
		RequestParams params = new RequestParams();
		params.put("functionId", "getBookRelate");
		params.put("body",HttpParamsUtil.getBookStoreRelateJsonBody(fid,ftype,relationType,page,pageSize,type));
		fillRequest(params);
		return params;
	}

		public static RequestParams checkReadCardParams(long bookid) {
			RequestParams params = new RequestParams();
			params.put("functionId", "checkReadCard");
			params.put("body", "{\"ebook_id\":\""+bookid+"\"}");
			fillRequest(params);
			return params;
		}

		//搜索用户
		
		public static RequestParams searchPeopleParams(int page,
				int pageSize,String nick_name) {
			 
			RequestParams params = new RequestParams();
			params.put("nick_name", nick_name);
			params.put("page", page);
			params.put("count", pageSize);
			params.put("jd_user_name", LoginUser.getpin());
			fillRequest(params);
			return params;
		} 
		
		
		// 纸书商城
		public static RequestParams getPaperBookStoreListParams(int page,
				int pageSize,boolean isnew) {
			 
			RequestParams params = new RequestParams();
			params.put("functionId", "topPaperBookList");
			params.put("body",HttpParamsUtil.getPaperBookStoreListJsonBody(page,pageSize,isnew));
			fillRequest(params);
			return params;
		} 

		
	
		
		// 微信支付参数
		public static RequestParams weixinPayParams(String payId,String appId) {
			 
			RequestParams params = new RequestParams();
			params.put("functionId", "weixinPay");
			params.put("appId", appId);
			params.put("payId", payId);
			fillRequest(params);
			return params;
		}
		
		public static RequestParams queryCartLoginParams() {
			RequestParams params = new RequestParams();
			params.put("body", HttpParamsUtil.payLoginBody());
			params.put("functionId", "login2");
			fillRequest(params);
			return params;
		}
		
		public static RequestParams unionPayParams(String payId,String appId) {
			RequestParams params = new RequestParams();
			params.put("payId", payId);
			params.put("appId", appId);
			params.put("functionId", "unionPayV2");
			fillRequest(params);
			return params;
		}
		
		public static RequestParams getTokenParams() {
			RequestParams params = new RequestParams();
			params.put("functionId", "genToken");
			params.put("body", HttpParamsUtil.getTokenBody());
			fillRequest(params);
			return params;
		}
		public static RequestParams getTokenParams(String id) {
			RequestParams params = new RequestParams();
			params.put("functionId", "newOrderListPay");
			params.put("body", HttpParamsUtil.getTokenBody(id));
			fillRequest(params);
			return params;
		}
		
		/**
		 * 购书送书新订单getToken
		 * @return
		 */
		public static RequestParams getSendBookTokenParams() {
			RequestParams params = new RequestParams();
			params.put("functionId", "genToken");
			params.put("body", HttpParamsUtil.getSendBookTokenBody());
			fillRequest(params);
			return params;
		}
		/**
		 * 购书送书订单getToken
		 * @param id
		 * @return
		 */
		public static RequestParams getSendBookTokenParams(String id) {
			RequestParams params = new RequestParams();
			params.put("functionId", "newOrderListPay");
			params.put("body", HttpParamsUtil.getSendBookTokenBody(id));
			fillRequest(params);
			return params;
		}

		public static RequestParams getCanUseOlineCardsHttpSetting(JSONObject json) {

			RequestParams params = new RequestParams();
			params.put("functionId", "getIsCanUseCards");
			params.put("body", json.toString());
			fillRequest(params);
			
			return params;
		}
		
		
		// 添加到畅读列表
		public static RequestParams getSplashParams() {
			RequestParams params = new RequestParams();
			params.put("functionId", "startScreenV2");
			params.put("body", "{\"clientPlatform\":\"1\"}");
			fillRequest(params);
			return params;
		}
	
		
		// 添加到畅读列表
		public static RequestParams addToChangduListParams(long ebookid) {
			RequestParams params = new RequestParams();
			params.put("functionId", "addNewReadInfoBatch");
			params.put("body", HttpParamsUtil.addToChangduListJsonBody(ebookid));
			fillRequest(params);
			return params;
		}

		public static RequestParams getSystemTimeParams() {
			RequestParams params = new RequestParams();
			params.put("functionId", "now");
//			fillRequest(params);
			return params;
		}
		
		// 下载借阅
		public static RequestParams getBorrowBookParams(long ebookid) {
			RequestParams params = new RequestParams();
			params.put("functionId", "borrowBook");
			params.put("body", HttpParamsUtil.borrowBookJsonBody(ebookid));
			fillRequest(params);
			return params;
		}

		
	
	// 智能推荐
	public static RequestParams getBookStoreRecommendParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "smartCommendList");
		fillRequest(params);
		return params;
	}

	// 限时借阅
	public static RequestParams getBookStoreBorrowingParams(int page,
			int pageSize) {
		RequestParams params = new RequestParams();
		params.put("functionId", "borrowBookList");
		params.put("body",
				HttpParamsUtil.getBookStoreBorrowingJsonBody(page, pageSize));
		fillRequest(params);
		return params;
	}
	
	
	

	//书城框架 子模块数据
	public static RequestParams getBookStoreChildModuleParams(int id,int moduleType) {
		RequestParams params = new RequestParams();
		params.put("functionId", "getModuleChildInfo");
		params.put("body",HttpParamsUtil.getChildModuleJsonBody(id, moduleType));
		fillRequest(params);
		return params;
	}
	


	//书城框架
	public static RequestParams getBookStoreFrameWorkParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "getMainThemeInfo");
		params.put("body", "{\"sysId\":\"1\"}");
		fillRequest(params);
		return params;
	}
	
	
	
	// 购物车 价格结算
	public static RequestParams getBookCartParams(List<Map<String, String>> ids) {
		RequestParams params = new RequestParams();
		params.put("functionId", "cart");
		params.put("body", HttpParamsUtil.getBookCartJsonBody(ids));
		fillRequest(params);
		return params;
	}

	// 其他人喜欢
	public static RequestParams getBookOthersLikeParams(long bookid) {
		RequestParams params = new RequestParams();
		params.put("functionId", "bookDetailRecommendListV2");
		params.put("body", HttpParamsUtil.getBookOtherLikesJsonBody(bookid));
		fillRequest(params);
		return params;
	}

	// 书评列表
	public static RequestParams getBookCommentsParams(String booktype,
			long bookid, String currentpage) {
		RequestParams params = new RequestParams();
		params.put("functionId", "newBookReview");
		params.put("body", HttpParamsUtil.getBookCommentsJsonBody(booktype,
				bookid, currentpage));
		fillRequest(params);
		return params;
	}
	
	// 获取读过本书的用户
	public static RequestParams getBookReadedParams(long bookId, String currentpage,String pageSize) {
		RequestParams params = new RequestParams();
		params.put("functionId", "getBookRecentlyUserInfos");
		params.put("body", HttpParamsUtil.getBookReadedJsonBody(bookId,currentpage,pageSize));
		fillRequest(params);
		return params;
	}

	// 图书主页列表
	public static RequestParams getBookInfoParams(long bookid,String type) {
		RequestParams params = new RequestParams();
		params.put("functionId", "newBookDetail");
		JSONObject json = new JSONObject();

		try {
			json.put("bookId", bookid);
			json.put("type", type);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
	
	// 想读状态
		public static RequestParams getWishBookParams(long bookid) {
			RequestParams params = new RequestParams();
			params.put("jd_user_name", LoginUser.getpin());
			params.put("book_id", bookid);
			fillRequest(params);
			return params;

		}

		//取消想读
		public static RequestParams unWishBookParams(long bookid) {
			RequestParams params = new RequestParams();
			params.put("jd_user_name", LoginUser.getpin());
			params.put("book_id", bookid);
			fillRequest(params);
			return params;

		}
		// 想读
		public static RequestParams wishBookParams(long bookid) {
			RequestParams params = new RequestParams();
			params.put("jd_user_name", LoginUser.getpin());
			params.put("book_id", bookid);
			fillRequest(params);
			return params;

		}
		
	
	
	// 云盘列表
	public static RequestParams getYunPanListParams(int page, int count) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("page", page);
		params.put("count", count);
		fillRequest(params);
		return params;

	}

	// 云盘搜索
	public static RequestParams getYunPanSearchParams(String query, int page,
			int count) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("page", page);
		params.put("count", count);
		params.put("q", query);
		fillRequest(params);
		return params;

	}

	// 绑定云盘书籍
	public static RequestParams bindYunPanServeridParams(String sign,
			String name) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("sign", sign);
		params.put("name", name);
		fillRequest(params);
		return params;

	}

	// 批量判断书籍是否需要上传
	public static RequestParams isNeedUploadYunPanParams(String documents) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("documents", documents);
		fillRequest(params);
		return params;

	}

	// 云盘上传
	public static RequestParams uploadYunPanParams(File file) {
		RequestParams params = new RequestParams();
		try {
			params.put("upload", file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return params;

	}
	
	//timeline
	public static RequestParams getTimelineParams(String currentpage,
			String pagesize,String before_guid,String since_guid,String recommend_guid) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("page", currentpage);
		params.put("count", pagesize);
		params.put("before_guid", before_guid);
		params.put("since_guid", since_guid);
		params.put("recommend_guid", recommend_guid);
		fillRequest(params);
		return params;
	}
	
	//at_me
	public static RequestParams getAt_meParams(String currentpage,
			String pagesize,String before_guid) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("page", currentpage);
		params.put("count", pagesize);
		params.put("before_guid", before_guid);
		fillRequest(params);
		return params;
	}
	
	//笔记
	public static RequestParams getBook_NoteParams(String currentpage,
			String pagesize,String since_guid,String user_id) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("page", currentpage);
		params.put("count", pagesize);
		params.put("user_id", user_id);
		params.put("since_guid", since_guid);
		params.put("mark", 2);
		fillRequest(params);
		return params;
	}
	
	//某人笔记
	public static RequestParams getUser_Book_NoteParams(String currentpage,
			String pagesize,String user_id,String book_id,String document_id) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("page", currentpage);
//		params.put("count", pagesize);
		params.put("user_id", user_id);
		params.put("book_id", book_id);
		params.put("order", "chapter");
		params.put("document_id", document_id);
		fillRequest(params);
		return params;
	}
	
	//阅读记录
	public static RequestParams getReading_data(String user_id,String book_id,String document_id) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("user_id", user_id);
		params.put("book_id", book_id);
		params.put("document_id", document_id);
		fillRequest(params);
		return params;
	}
	
	//书评
	public static RequestParams getBook_commentsParams(String currentpage,
			String pagesize,String before_guid,String user_id,String user_name) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("page", currentpage);
		params.put("count", pagesize);
		params.put("user_id", user_id);
		params.put("before_guid", before_guid);
		params.put("name", user_name);
		fillRequest(params);
		return params;
	}
	
	// 写书评
	public static RequestParams writeBookCommentsParams(String bookid,String rating,String content,String users) {
		 
		RequestParams params = new RequestParams();
		params.put("book_comment[book_id]", bookid);
		params.put("book_comment[rating]", rating);
		params.put("book_comment[content]", content);
		params.put("at_user_ids", users);
		
		params.put("jd_user_name", LoginUser.getpin());
		fillRequest(params);
		return params;
	}
	
	
	public static RequestParams getRequestPostBookComments(int bookid,String content,float rating,String users) {
		RequestParams params=new RequestParams();
		params.put("book_comment[book_id]", bookid);
		params.put("book_comment[content]",content);
		params.put("book_comment[rating]", rating);
		params.put("at_user_ids", users);
		
		params.put("jd_user_name", LoginUser.getpin());
		fillRequest(params);
		return params;
	}
	
	public static RequestParams getRequestInvitedOrForward(String guid,String conternt,int state) {
		RequestParams params=new RequestParams();
		params.put("entity_comment[entity_guid]", guid);
		params.put("entity_comment[content]",conternt);
		params.put("entity_comment[repost]", state);
	
		params.put("jd_user_name", LoginUser.getpin());
		fillRequest(params);
		return params;
	}
	
	public static RequestParams getRequestPostCommets(long bookid,String content,float rating,int listid) {
		RequestParams params=new RequestParams();
		params.put("book_comment[book_id]", bookid);
		params.put("book_comment[content]",content);
		params.put("book_comment[rating]", rating);
		params.put("book_comment[book_list_id]", listid);
	
		params.put("jd_user_name", LoginUser.getpin());
		fillRequest(params);
		return params;
	}

	//empty
		public static RequestParams getEmptyParams() {
			RequestParams params = new RequestParams();
			
			return params;
		}
		
	
	//动态搜索
	public static RequestParams getTimelineSearchParams(String currentpage,
			String pagesize,String query) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("page", currentpage);
		params.put("count", pagesize);
		params.put("query", query);
		fillRequest(params);
		return params;
	}
	
	//用户搜索
	public static RequestParams getUSerSearchParams(String currentpage,
			String pagesize,String query) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("page", currentpage);
		params.put("count", pagesize);
		params.put("nick_name", query);
		fillRequest(params);
		return params;
	}
	
	//动态详情
	public static RequestParams getTimelineDetailParams() {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		fillRequest(params);
		return params;
	}
	
	//设置笔记状态
	public static RequestParams getBookNoteStatueParams(String id,String action) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("id", id);
		params.put("note_action", action);
		fillRequest(params);
		return params;
	}
	
	//获取笔记动态
	public static RequestParams getBookNoteTimelineParams(String book_id) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("id", book_id);
		fillRequest(params);
		return params;
	}
	
	//私信
	public static RequestParams getPrivateMessageParams(String before_id,String user_id,String count,String name) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("user_id", user_id);
		params.put("before_id", before_id);
		params.put("count", count);
		params.put("name", name);
		fillRequest(params);
		return params;
	}
	
	
	//post tweet
	public static RequestParams postTweetParams(Bundle bundle) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put(TimelinePostTweetActivity.BOOK_LIST, Boolean.toString(false));
		params.put("content",
				bundle.getString(TimelinePostTweetActivity.TWEET_CONTENT));
		params.put("book_ids",
				bundle.getString(TimelinePostTweetActivity.TWEET_BOOKS));
		params.put("at_user_ids",
				bundle.getString(TimelinePostTweetActivity.TWEET_USERS));
		
		List<String> path = bundle.getStringArrayList("image_urls");
		if(null != path && path.size() > 0) {
			JSONArray json = new JSONArray();
			for(int i=0; i<path.size(); i++) {
				if(!"null".equals(path.get(i))) {
					json.put(path.get(i));
				}	
			}
			
			params.put("image_urls", json.toString());
		}
		
		fillRequest(params);
		return params;
	}
	
	
	//favorite 
	public static RequestParams getTimelineFavoriteParams(String entityid) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put(TweetModel.ENTITY_ID, entityid);
		fillRequest(params);
		return params;
	}
	
	//favorite  list /like list
		public static RequestParams getRecommendsParams(String entityid) {
			RequestParams params = new RequestParams();
			params.put("jd_user_name", LoginUser.getpin());
			params.put(TweetModel.ENTITY_ID, entityid);
			fillRequest(params);
			return params;
		}
		
		//评论 转发
		public static RequestParams getEntitysCommentsOrForwordParams(Bundle bundle) {
			RequestParams params = new RequestParams();
			params.put("jd_user_name", LoginUser.getpin());
			
			params.put("at_user_ids", bundle.getString(TimelinePostTweetActivity.TWEET_USERS));
			
			params.put(TweetModel.COMMENT_GUID, bundle.getString(TimelineCommentsActivity.ENTITY_GUID));
			
			String temp, post = "";
			long commentId = bundle.getLong(TimelineCommentsActivity.REPLY_TO,
					TweetModel.DEFAULT_ERROR_VALUE);
			if (commentId != TweetModel.DEFAULT_ERROR_VALUE)
				params.put(TweetModel.COMMENT_REPLY_ID, Long.toString(commentId));
			
			if ((temp = (bundle
					.getString(TimelineCommentsActivity.ORIGIN_CONTENT))) != null)
				params.put(TweetModel.COMMENT_ORIGIN_CONTENT,temp);
			
			params.put(TweetModel.COMMENT_TYPE, TweetModel.setRepostArea(
					bundle.getBoolean(TimelineCommentsActivity.IS_COMMENT),
					bundle.getBoolean(TimelineCommentsActivity.CHECKED)));

			temp = bundle.getString(TimelineCommentsActivity.USER_COMMENT);
			params.put(TweetModel.COMMENT_CONTENT, temp);
			fillRequest(params);
			return params;
		}
		
	
	//delete 动态
	public static RequestParams deleteTimelineEntityParams(String entityid) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put(TweetModel.ENTITY_ID, entityid);
		fillRequest(params);
		return params;
	}
	
	// 喜欢
	public static RequestParams getWantParams(String pin,String currentpage,
			String pagesize,String user_id) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", pin);
		params.put("page", currentpage);
		params.put("count", pagesize);
		params.put("mark", 1);
		params.put("user_id", user_id);
		fillRequest(params);
		return params;
	}
	
	// 关注
	public static RequestParams getFocusParams(String user_id,String currentpage,
			String pagesize) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("user_id", user_id);
		params.put("page", currentpage);
		params.put("count", pagesize);
		fillRequest(params);
		return params;
	}
	
	// 推荐用户
	public static RequestParams getRecommendUserParams(String currentpage,
			String pagesize) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("page", currentpage);
		params.put("count", pagesize);
		fillRequest(params);
		return params;
	}
	
	
	
	// 关注
		public static RequestParams getFocusParams(String currentpage,
				String pagesize) {
			RequestParams params = new RequestParams();
			params.put("jd_user_name", LoginUser.getpin());
			params.put("page", currentpage);
			params.put("count", pagesize);
			fillRequest(params);
			return params;
		}
		
	
	// 关注某人
	public static RequestParams getFollowSomeParams(String id) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("ids", id);
		fillRequest(params);
		return params;
	}
	
	// 关注某人
	public static RequestParams getFollowPinParams(String pin) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		try {
			params.put("names", new String(pin.getBytes(), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		fillRequest(params);
		return params;
	}
	
	// 取消关注
	public static RequestParams getUNFollowParams(String id) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("id", id);
		fillRequest(params);
		return params;
	}
	
	//关注统计
	public static RequestParams getFollowParams(String pin) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", pin);
		fillRequest(params);
		return params;
	}
	
	// 分享回调
	public static RequestParams getShareParams(String pin, String sourceId, String sourceType) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", pin);
		params.put("share[source_id]", sourceId);
		params.put("share[source_type]", sourceType);
		fillRequest(params);
		return params;
	}
	
	// 分享回调
	public static RequestParams getSendMessageParams(String body , String receiver_id) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("message[body]", body);
		params.put("message[receiver_id]", receiver_id);
		fillRequest(params);
		return params;
	}

	// 畅读卡
	public static RequestParams getReadingCardParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "myReadCard");
		fillRequest(params);
		return params;
	}
	
	// 云盘上传回掉
	public static RequestParams uploadYunPanRebackParams(String sign, long id,
			String url, String size, String type) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("url", url);
		params.put("document_id", id);// 服务器接受ServerId
		params.put("document_sign", sign);
		params.put("size", size);
		params.put("document_type", type);
		return params;

	}

	public static RequestParams getUploadDownRecordParams(long bookId) {
		RequestParams params = new RequestParams();
		params.put("body", "{}");
		params.put("ebookId", "" + bookId);
		params.put("key", MD5Calculator.getKey(bookId));
		// fillRequest(params);

		return params;

	}

	// 搜索已购图书参数
	public static RequestParams getSearchBuyedEbookParams(String key,
			String currentpage, String pagesize) {
		RequestParams params = new RequestParams();
		params.put("functionId", "buyedEbookAndGiveBookSearch");
		String body = HttpParamsUtil.getSearchBuyedEbookJsonBody(key,
				currentpage, pagesize);
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	// 用户设置参数
	public static RequestParams getSettingParams(String body) {
		RequestParams params = new RequestParams();
		params.put("functionId", "userSettingStatistics");
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	//解绑
	public static RequestParams getBindParams() {
		
		RequestParams params = new RequestParams();
		params.put("functionId", "checkDevice");
		String body = HttpParamsUtil.getBindJsonBody();
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	//包月卡
	public static RequestParams getMonthlyParams() {
		
		RequestParams params = new RequestParams();
		params.put("functionId", "chargeAndCard");
		String body = HttpParamsUtil.getMonthlyJsonBody();
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	//取消包月服务
	public static RequestParams getCancleServiceParams(Integer serviceid) {
		
		RequestParams params = new RequestParams();
		params.put("functionId", "cancelServer");
		String body = HttpParamsUtil.getCancleServiceJsonBody(serviceid);
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	//获取提醒信息
	public static RequestParams getAlarmParams(String count,String before_id) {
		
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("count", count);
		params.put("before_id", before_id);
		fillRequest(params);
		return params;
	}
	
	//删除会话
	public static RequestParams getDeleteMessageParams() {
		
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		fillRequest(params);
		return params;
	}
	
	//取消包月服务
	public static RequestParams getContinueMoneyParams() {
		
		RequestParams params = new RequestParams();
		params.put("functionId", "genMonthChargeToken");
		String body = HttpParamsUtil.getBuyJsonBody();
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	// 获取用户信息参数
	public static RequestParams getUserInfoParams(String pin) {
		RequestParams params = new RequestParams();
		params.put("functionId", "userBasicInfo");
		String body = HttpParamsUtil.getUserInfoJsonBody(pin);
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	// 获取个人主页信息
	public static RequestParams getPersonalParams(String userid,String user_nick_name,String user_name) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("user_id", userid);
		params.put("nick_name", user_nick_name);
		params.put("name", user_name);
		fillRequest(params);
		return params;
	}

	// 搜索书城图书参数
	public static RequestParams getSearchStoreEbookParams(String currentpage,
			String pagesize, String key,String searchPaperBook) {
		RequestParams params = new RequestParams();
		params.put("functionId", "searchBookV2");
		String body = HttpParamsUtil.getSearchStoreEbookJsonBody(currentpage,
				pagesize, key,searchPaperBook);
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	/**
	* @Description: 搜索关键字日志记录信息参数
	* @param String type 1:书城搜索 2:社区搜索
	* @return RequestParams
	* @author xuhongwei1
	* @date 2015年11月19日 下午3:42:01 
	* @throws 
	*/ 
	public static RequestParams getSearchWordReportParams(String keyword, String type) {
		RequestParams params = new RequestParams();
		params.put("functionId", "addSearchKeyLog");
		String body = HttpParamsUtil.getSearchStoreEbookJsonBody(keyword, type);
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	/**
	* @Description: 搜索关键字列表书籍记录信息参数
	* @return RequestParams
	* @author xuhongwei1
	* @date 2015年11月19日 下午4:05:23 
	* @throws 
	*/ 
	public static RequestParams getSearchBookReportParams(String keyword, String bookid) {
		RequestParams params = new RequestParams();
		params.put("functionId", "addSearchBookLog");
		String body = HttpParamsUtil.getSearchStoreEbookJsonBody(keyword, bookid, "1");
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	// 订单
	public static RequestParams getOrderParams(String currentpage,
			String pagesize) {
		RequestParams params = new RequestParams();
		params.put("functionId", "leBookOneMonthOrderList");
		String body = HttpParamsUtil.getOrderJsonBody(currentpage,
				pagesize);
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	/**
	 *  阅历-用户读过的书
	 * @param currentpage 
	 * @param pagesize
     * @param supportDate (week：一周内         month：一个月内         earlier：更早 )
	 * @param screen 屏幕大小 如180*90
	 * @return
	 */
	public static RequestParams getUserReadedBookParams(String currentpage, String pagesize, String supportDate,
			String screen) {
		RequestParams params = new RequestParams();
		params.put("functionId", "userReadEBook");
		String body = HttpParamsUtil.getUserReadedBookJsonBody(currentpage, pagesize, supportDate);
		params.put("body", body);
		params.put("screen", screen);
		fillRequest(params);
		return params;
	}
	
	/**
	 * 获取阅历-用户读过的书超过用户百分比
	 * @return
	 */
	public static RequestParams getUserReadedBookPercent() {
		RequestParams params = new RequestParams();
		params.put("functionId", "userReadScale");
		fillRequest(params);
		return params;
	} 
	
	// 一个月前订单
	public static RequestParams getOneMonthAgoOrderParams(String currentpage,
			String pagesize) {
		RequestParams params = new RequestParams();
		params.put("functionId", "leBookBeforeOneMonthOrderList");
		String body = HttpParamsUtil.getOrderJsonBody(currentpage,
				pagesize);
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	// 订单详情
	public static RequestParams getOrderDetailParams(String orderid,
			String currentpage) {
		RequestParams params = new RequestParams();
		params.put("functionId", "leBookOrderDetailList");
		String body = HttpParamsUtil.getOrderDetailJsonBody(orderid,
				currentpage);
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	// 缺书登记参数
	public static RequestParams getLackBookParams(String type, String memo, String mobile, String qq) {
		RequestParams params = new RequestParams();
		params.put("functionId", "feedback");
		String body = HttpParamsUtil.getLockBookJsonBody(type, memo,mobile,qq);
		params.put("body", encryptParams(body));
		fillRequest(params);
		return params;
	}
	
	// 提交用户头像参数
	public static RequestParams putUserImage(String imgString) {
		RequestParams params = new RequestParams();
		params.put("functionId", "headPhotoUpdate");
		String body = HttpParamsUtil.putUserImageJsonBody(imgString);
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	// 提交修改个人信息参数
	public static RequestParams putUserInfo(String usex, String nickname,String uremark) {
		RequestParams params = new RequestParams();
		params.put("functionId", "userInfoUpdate");
		String body = HttpParamsUtil.putUserInfopJsonBody(usex,nickname,uremark);
		params.put("body", body);
		fillRequest(params);
		return params;
	}

	// 提交畅读卡信息参数
	public static RequestParams putReadingCard(String cardNo,String cardPwd) {
		RequestParams params = new RequestParams();
		params.put("functionId", "readCardBinding");
		String body = HttpParamsUtil.putReadingCardJsonBody(cardNo,cardPwd);
		params.put("body", encryptParams(body));
		fillRequest(params);
		return params;
	}
	
	// 条形码扫描结果
	public static RequestParams getRecommendList(int rtype, int currentPage,
			int pageSize) {
		RequestParams params = new RequestParams();
		params.put("functionId", "recommendList");
		String body = HttpParamsUtil.getRecommendListJsonBody(rtype,
				currentPage, pageSize);
		params.put("body", body);
		fillRequest(params);
		return params;
	}

	// 条形码扫描结果
	public static RequestParams getadList(int rtype) {
		RequestParams params = new RequestParams();
		params.put("functionId", "adList");
		String body = HttpParamsUtil.getadListJsonBody(rtype);
		params.put("body", body);
		fillRequest(params);
		return params;
	}

	// 分类参数
	public static RequestParams getCategoryBookParams(int clientPlatform) {
		RequestParams params = new RequestParams();
		params.put("functionId", "CategoryList");
		String body = HttpParamsUtil.getCategoryBookJsonBody(clientPlatform);
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	// 分类图片参数
	public static RequestParams getCategoryImageParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "categoryImageList");
		params.put("body", "{}");
		
//		fillRequest(params);
		return params;
	}

	// 搜索热词参数
	public static RequestParams getHotKeywordParams(String total,String type) {
		RequestParams params = new RequestParams();
		params.put("functionId", "keywordByRand");
		String body = HttpParamsUtil.getHotKeyWordJsonBody(total,type);
		params.put("body", body);
		fillRequest(params);
		return params;
	}
	
	// 版本更新
	public static RequestParams getUpdateParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "version");
		params.put("appId", 1);
		fillRequest(params);
		return params;
	}

	// 分类书列表
	public static RequestParams getCategoryBookListParams(String pageSize,
			String catId, String currentPage) {
		RequestParams params = new RequestParams();
		params.put("functionId", "categoryBookListV2");
		String body = HttpParamsUtil.getCategoryBookListJsonBody(pageSize,
				catId, currentPage);
		params.put("body", body);
		fillRequest(params);
		return params;
	}

	// 畅读图书参数
	public static RequestParams getChangduEbookParams(String currentpage,
			String pagesize) {

		RequestParams params = new RequestParams();
		params.put("functionId", "myNewCardReadBook");
		params.put("body",
				HttpParamsUtil.getChangduEbookJsonBody(currentpage, pagesize));
		fillRequest(params);
		return params;

	}
	
	// 借阅图书参数
	public static RequestParams getBorrowedEbookParams(String currentpage,
				String pagesize) {

		RequestParams params = new RequestParams();
		params.put("functionId", "myBorrowBookList");
		params.put("body",
				HttpParamsUtil.getStoreBorrowedEbookJsonBody(currentpage, pagesize));
		fillRequest(params);
		return params;
	}
	
	/**
	 *  用户借阅图书参数
	 * @param currentpage
	 * @param pagesize
	 * @return
	 */
	public static RequestParams getUserBorrowedEbookParams(String currentpage, String pagesize) {
		RequestParams params = new RequestParams();
		params.put("functionId", "getBuyBorrowEbooks");
		params.put("body", HttpParamsUtil.getBorrowedEbookJsonBody(currentpage, pagesize));
		fillRequest(params);
		return params;

	}
	
	// 同步书城书籍进度书签
	public static RequestParams getSyncEBookReadProgressBookMark(LocalBook book) {
		RequestParams params = new RequestParams();
		params.put("functionId", "sendMzBookmark");
		params.put("body", HttpParamsUtil.syncEBookReadProgressBookMark(book));
		fillRequest(params);
		return params;
	}
	
	// 上传书城书籍进度书签
	public static RequestParams getUploadEBookReadProgressBookMark(LocalBook book) {
		RequestParams params = new RequestParams();
		params.put("functionId", "sendMzBookmark");
		params.put("body", HttpParamsUtil.uploadEBookReadProgressBookMark(book));
		fillRequest(params);
		return params;
	}
	
	// 同步导入书籍进度书签
	public static RequestParams getSyncDocumentReadProgressBookMark(Document doc) {
		RequestParams params = new RequestParams();
		params.put("functionId", "sendMzBookmark");
		params.put("body", HttpParamsUtil.syncDocumentReadProgressBookMark(doc));
		fillRequest(params);
		return params;
	}
	
	// 上传导入书籍进度书签
	public static RequestParams getUploadDocumentReadProgressBookMark(Document doc) {
		RequestParams params = new RequestParams();
		params.put("functionId", "sendMzBookmark");
		params.put("body", HttpParamsUtil.uploadDocumentReadProgressBookMark(doc));
		fillRequest(params);
		return params;
	}
	
	// 导入书籍绑定
	public static RequestParams getDocBindParams(String title, String bookSign) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("name", title);
		params.put("sign", bookSign);
		fillRequest(params);
		return params;
	}
	
	// 同步书城书籍笔记
	public static RequestParams getEBookReadNoteSyncParams(long bookId, long syncTime) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("since_updated_at", syncTime);
		params.put("book_id", bookId);
		params.put("count", 20);
		fillRequest(params);
		return params;
	}
	
	// 同步导入书籍笔记
	public static RequestParams getDocumentReadNoteSyncParams(long bookId, long syncTime) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("since_updated_at", syncTime);
		params.put("document_id", bookId);
		params.put("count", 20);
		fillRequest(params);
		return params;
	}
	
	// 上传笔记
	public static RequestParams getUploadBookReadNote(List<ReadNote> noteList) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("notes", HttpParamsUtil.uploadReadNote(noteList));
		fillRequest(params);
		return params;
	}
	
	// 用户阅读统计
	public static RequestParams getMZReadStatistics() {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		fillRequest(params);
		return params;
	}
	
	// 京东读书统计数量
	public static RequestParams getJDReadStatistics() {
		RequestParams params = new RequestParams();
		params.put("functionId", "userReadEBookScale");
		params.put("jd_user_name", LoginUser.getpin());
		fillRequest(params);
		return params;
	}
	
	// 获取某书阅读时长统计
	public static RequestParams getReadingData(long bookId, long documentId) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("book_id", bookId);
		params.put("document_id", documentId);
		fillRequest(params);
		return params;
	}
	
	// 批量上传阅读时长
	public static RequestParams getUploadBatchReadingData(List<ReadingData> list, long bookId, float percent) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("data", HttpParamsUtil.uploadBatchReadingData(list));
		if (bookId != 0 && percent >= 0.95f) {
			JSONArray arr = new JSONArray();
			JSONObject obj = new JSONObject();
			try {
				obj.put("book_id", bookId);
				obj.put("percent", percent);
				arr.put(obj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			params.put("book_percent", arr.toString());
		}
		fillRequest(params);
		return params;
	}


	/**
	 * 在线畅读，检查账户是否有可用的畅读卡。
	 * 
	 */
	public static RequestParams getCheckHasReadCardHttpSetting(String ebook_id) {

		RequestParams params = new RequestParams();
		params.put("functionId", "checkReadCard");
		JSONObject json = new JSONObject();

		try {
			json.put("ebook_id", ebook_id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;

	}

	/**
	 * @description 免费畅读卡领取
	 */
	public static RequestParams getFreeReadCard() {

		RequestParams params = new RequestParams();
		params.put("functionId", "insertFreeCard");
		params.put("body", "{}");
		fillRequest(params);
		return params;
	}

	/**
	 * 
	 * 在线畅读，实现畅读卡与书绑定
	 * 
	 */
	public static RequestParams getBindCardAndBookHttpSetting(String ebook_id) {
		JSONObject json = new JSONObject();
		try {
			json.put("ebook_id", ebook_id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		RequestParams params = new RequestParams();
		params.put("functionId", "addNewReadInfo");
		params.put("body", json.toString());
		fillRequest(params);
		return params;

	}

	// 畅读图书搜索参数
	public static RequestParams searchChangduEbookParams(String currentpage,
			String pagesize, String bookNameOrAuthor) {

		RequestParams params = new RequestParams();
		params.put("functionId", "myNewCardReadBook");
		params.put("body", HttpParamsUtil.searchChangduEbookJsonBody(
				currentpage, pagesize, bookNameOrAuthor));
		fillRequest(params);
		return params;

	}

	// 已购图书参数
	public static RequestParams getBuyedEbookParams(String currentpage,
			String pagesize) {

		RequestParams params = new RequestParams();
		params.put("functionId", "newBuyedEbookOrderList");
		params.put("body",
				HttpParamsUtil.getBuyedEbookJsonBody(currentpage, pagesize));
		fillRequest(params);
		return params;

	}

	// 登录参数
	public static RequestParams getLoginParams(String userName, String passWord,String verification) {
       RequestParams params = new RequestParams();
		params.put("functionId", "unionLoginNew");
		params.put("body", encryptParams(HttpParamsUtil.getUserLoginJsonBody(
				userName, passWord,verification)));
		fillRequest(params);
		return params;
	}

	/**
	 * 获取登录图片验证码
	 * @param userPin
	 * @return
	 */
	public static RequestParams getImageVerificationParams(String userPin) {
		RequestParams params = new RequestParams();
		params.put("functionId", "getValidateImgCode");
		JSONObject json = new JSONObject();
		try {
			json.put("loginName", URLEncoder.encode(userPin, "utf-8"));
		} catch (JSONException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}

	// 获取注册参数
	public static RequestParams getRigisterParams(String phone, String code,
			String pwd) {

		RequestParams params = new RequestParams();
		params.put("functionId", "regedit");
		String body = HttpParamsUtil.getRegisterJsonBody(phone, code, pwd);
		MZLog.d("wangguodong", "body=" + body);
		params.put("body", encryptParams(body));
		fillRequest(params);
		return params;
	}

	// 获取手机验证码
	public static RequestParams getRigisterCode(String phone) {

		RequestParams params = new RequestParams();
		params.put("functionId", "sendSmsMsg");
		params.put("body", HttpParamsUtil.getVerifyCodeJsonBody(phone));
		fillRequest(params);
		return params;

	}

	/*
	 * 获取对称加密KEY
	 */
	public static RequestParams getSessionKeyParams(String envelopeKey) {
		MZLog.d("req", envelopeKey);
		RequestParams params = new RequestParams();
		params.put("functionId", "sessionKey");

		String body = HttpParamsUtil.getSessionKeyJsonBody(envelopeKey);

		params.put("body", body);
		fillRequest(params);
		return params;
	}
	public static RequestParams reqIsHaveGiftBag(){
		RequestParams params = new RequestParams();
		params.put("body", HttpParamsUtil.getIsHaveGiftBagBody())	;
		params.put("functionId","checkGiftPacks");
		fillRequest(params);
		return params;
	}
	public static RequestParams reqGetGiftBag(String giftPacksId){
		RequestParams params = new RequestParams();
		params.put("body",encryptParams( HttpParamsUtil.getGiftBagBody(giftPacksId)))	;
		params.put("functionId","receiveGiftPacks");
		fillRequest(params);
		return params;
	}
	public static RequestParams reqUnReadGiftBag(){
		RequestParams params = new RequestParams();
		params.put("functionId","getUnread");
		fillRequest(params);
		return params;
	}
	public static RequestParams updateGiftBagStatus(String msg){
		RequestParams params = new RequestParams();
		params.put("functionId","updateUnread");
		JsonObject jo=new JsonObject();
		jo.addProperty("detailType", msg);
		params.put("body",jo.toString());
		fillRequest(params);
		return params;
	}
	public static RequestParams getRsaPublicKeyParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "rsaPublicKey");
		fillRequest(params);
		return params;
	}
    
    /**
     *
     * 登录打通参数
     */
	public static RequestParams getUnionLoginParams(String url) {
		
        RequestParams params = new RequestParams();
		params.put("functionId", "genToken");
		params.put("body", HttpParamsUtil.getMergeUrlJsonBody(url));
		fillUnionLoginRequest(params);
		return params;
	}
	
    /**
    *
    * m站的登录打通参数
    */
	public static RequestParams getMLoginParams() {
		
       RequestParams params = new RequestParams();
		params.put("functionId", "loginBridge");
		JSONObject json = new JSONObject();
		try {
			json.put("urlKey", "ebook-m");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}

	/**
	 * 返回公用参数
	 * 
	 * @return
	 */
	public static Map<String, String> fillRequest() 
    {
		Map<String, String> params = new HashMap<String, String>();
		params.put("clientVersion",
				StatisticsReportUtil.getSoftwareVersionName());
		params.put("build", StatisticsReportUtil.getSoftwareBuildName());
		params.put("client", "android");
		params.put("os", "android");
		params.put("osVersion", URLEncoder.encode(Build.VERSION.RELEASE));
		Display display = ((WindowManager) MZBookApplication.getInstance()
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		if (null != display) {
			params.put("screen", display.getWidth() + "*" + display.getHeight());
		}
		
		params.put("jds", MZBookApplication.jds);
		return params;
	}
	
	/**
	 * 返回公用参数
	 * 
	 * @return
	 */
	public static void fillRequest(RequestParams params) 
	{
		// StatisticsReportUtil.getSoftwareVersionName());
		params.put("build", StatisticsReportUtil.getSoftwareBuildName());
        params.put("clientVersion", StatisticsReportUtil.getSoftwareVersionName());
		//params.put("build", StatisticsReportUtil.getSoftwareBuildName());
		params.put("client", "android");
		params.put("os", "android");
		params.put("osVersion", URLEncoder.encode(Build.VERSION.RELEASE));
		Display display = ((WindowManager) MZBookApplication.getInstance()
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		if(null != display) {
			params.put("screen", display.getWidth() + "*" + display.getHeight());
		}
		
		if (!params.has("partnerID")) {
			params.put("unionId", CommonUtil.getPropertiesValue("partnerID"));
		}
		if (!params.has("subPartnerID")) {
			params.put("subunionId", CommonUtil.getPropertiesValue("subPartnerID"));
		}
		String uuid= StatisticsReportUtil.readDeviceUUID();
		params.put("uuid",uuid);
		params.put("brand",DeviceUtil.getBranch());
		params.put("model",DeviceUtil.getModel());
		params.put(Configuration.APP_ID,"1");
		params.put("jds", MZBookApplication.jds);
		MZLog.d("req", "uuid="+uuid);
	}
	public static void fillUnionLoginRequest(RequestParams params) 
	{
		// StatisticsReportUtil.getSoftwareVersionName());
		params.put("build", StatisticsReportUtil.getSoftwareBuildName());
		params.put("clientVersion", StatisticsReportUtil.getSoftwareVersionName());
		//params.put("build", StatisticsReportUtil.getSoftwareBuildName());
		params.put("client", "ebook_android");
		params.put("os", "android");
		params.put("osVersion", URLEncoder.encode(Build.VERSION.RELEASE));
		Display display = ((WindowManager) MZBookApplication.getInstance()
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		if(null != display) {
			params.put("screen", display.getWidth() + "*" + display.getHeight());
		}
		
		if (!params.has("partnerID")) {
			params.put("unionId", CommonUtil.getPropertiesValue("partnerID"));
		}
		if (!params.has("subPartnerID")) {
			params.put("subunionId", CommonUtil.getPropertiesValue("subPartnerID"));
		}
		String uuid= StatisticsReportUtil.readDeviceUUID();
		params.put("uuid",uuid);
		params.put("brand",DeviceUtil.getBranch());
		params.put("model",DeviceUtil.getModel());
		params.put(Configuration.APP_ID,"1");
		params.put("jds", MZBookApplication.jds);
		MZLog.d("req", "uuid="+uuid);
	}
	
//	public static String getCpaValue(String key) {
//		InputStream asset_is = null;
//		try {
//			asset_is = MZBookApplication.getInstance().getAssets()
//					.open("cpa.properties");
//			Properties properties = new Properties();
//			properties.load(asset_is);
//
//			return properties.getProperty(key);
//			
//
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (asset_is != null) {
//					asset_is.close();
//					asset_is = null;
//				}
//			} catch (Exception e2) {
//			}
//		}
//		return null;
//	}

	public static HttpGet getLoadImageRequest(Context context, String url) 
    {
		String finalurl = null;
		finalurl = url.trim();
		HttpGet request = new HttpGet(finalurl);
		return request;
	}

	public static RequestParams getCPAToken() {
		RequestParams params = new RequestParams();
		params.put("functionId", "cpaToken");
		params.put("body","");
		fillRequest(params);
		return params;
	}

	public static RequestParams getPushData(String token, String unionId,
			String info) {
		RequestParams params = new RequestParams();
		params.put("body", HttpParamsUtil.getCPABody(token, unionId, info));
		params.put("functionId", "cpaPushData");
		fillRequest(params);
		return params;
	}

	public static RequestParams getCPSParams(String unionId, String unionSiteId) {
		RequestParams params = new RequestParams();
		params.put("body", HttpParamsUtil.getCPSBody(unionId, unionSiteId));
		params.put("functionId", "cps");
		fillRequest(params);
		return params;
	}

	public static RequestParams getWalletParams() {
		RequestParams params = new RequestParams();
		params.put("body", HttpParamsUtil.getWalletBody());
		params.put("functionId", "walletInfo");
		fillRequest(params);
		return params;
	}

	public static RequestParams getEverRegistParam(String phone) {
		RequestParams params = new RequestParams();
		params.put("body", HttpParamsUtil.getPhoneNumBody(phone));
		params.put("functionId", "checkPhoneNumber");
		fillRequest(params);
		return params;
	}


	public static RequestParams getShopingCartParams(String bookList, String type) {
		RequestParams params = new RequestParams();
		params.put("body", HttpParamsUtil.getShoppingCartBody(bookList, type));
		params.put("functionId", "shoppingCart");
		fillRequest(params);
		return params;
	}

	public static RequestParams getIntegrationParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "myScoreIndex");
		fillRequest(params);
		return params;
	}

	public static RequestParams getLastWeekScoreParams(int currentPage, int pageSize) {
		RequestParams params = new RequestParams();
		params.put("functionId", "lastWeekScore");
		params.put("body", HttpParamsUtil.getLastWeekScoreBody(currentPage, pageSize));
		fillRequest(params);
		return params;
	}

	public static RequestParams getCheckAndSaveLoginScoreParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "checkAndSaveLoginScore");
		fillRequest(params);
		return params;
	}

	public static RequestParams getCheckDeviceParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "checkDevice");
		params.put("body", HttpParamsUtil.getBindJsonBody());
		fillRequest(params);
		return params;
	}

	public static RequestParams getCheckSignScoreParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "checkSignScore");
		fillRequest(params);
		return params;
	}

	public static RequestParams getSignGetScoreParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "signGetScore");
		fillRequest(params);
		return params;
	}

	public static RequestParams getCommentGetScoreParams(long bookId) {
		RequestParams params = new RequestParams();
		params.put("functionId", "commentGetScore");
		params.put("body", HttpParamsUtil.getCommentGetScoreBody(bookId));
		fillRequest(params);
		return params;
	}

	public static RequestParams getNotesGetScoreParams(long bookId) {
		RequestParams params = new RequestParams();
		params.put("functionId", "notesGetScore");
		params.put("body", HttpParamsUtil.getCommentGetScoreBody(bookId));
		fillRequest(params);
		return params;
	}

	public static RequestParams getReadGetTimeParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "readGetTime");
		fillRequest(params);
		return params;
	}

	public static RequestParams getReadTimeGetScoreParams(long bookId,int readTime) {
		RequestParams params = new RequestParams();
		params.put("functionId", "readTimeGetScore");
		params.put("body", HttpParamsUtil.getReadTimeGetScoreBody(bookId,readTime));
		fillRequest(params);
		return params;
	}

	public static RequestParams getShareGetScoreParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "shareGetScore");
		fillRequest(params);
		return params;
	}
	
	public static RequestParams getBorrowShareGetScoreParams(String ebookId) {
		RequestParams params = new RequestParams();
		params.put("functionId", "buyBorrowShareGetScore");
		JSONObject json = new JSONObject();
		try {
			json.put("ebookId", ebookId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
	
	public static RequestParams getGiftByScoreParams(int giftId) {
		RequestParams params = new RequestParams();
		params.put("functionId", "getGiftByScore");
		params.put("body", HttpParamsUtil.getGiftByScoreBody(giftId));
		fillRequest(params);
		return params;
	}

	public static RequestParams exchangeBind(String exchangeCode) {
		RequestParams params = new RequestParams();
		params.put("functionId", "readCardActivateByCdkey");
		String body = HttpParamsUtil.exchangeBindBody(exchangeCode);
		params.put("body", encryptParams(body));
		fillRequest(params);
		return params;
	}
	
	public static RequestParams getAllNotesAuthors(long bookId, String documentSign) {
		RequestParams params = new RequestParams();
		if (bookId != 0) {
			params.put("book_id", bookId);
		}
		if (!TextUtils.isEmpty(documentSign)) {
			params.put("document_sign", documentSign);
		}
		fillRequest(params);
		return params;
	}
	
	public static RequestParams getChapterAllNotes(long bookId,
			String documentSign, String chapterItemRef, String pin) {
		RequestParams params = new RequestParams();
		if (bookId != 0) {
			params.put("book_id", bookId);
		}
		if (!TextUtils.isEmpty(documentSign)) {
			params.put("document_sign", documentSign);
		}
		params.put("chapter_itemref", chapterItemRef);
		params.put("jd_user_name", pin);
		fillRequest(params);
		return params;
	}
	
	public static RequestParams getSomeoneAllNotes(String userId, long bookId,
			String documentSign, int pageIndex, int count) {
		RequestParams params = new RequestParams();
		if (bookId != 0) {
			params.put("book_id", bookId);
		}
		if (!TextUtils.isEmpty(documentSign)) {
			params.put("document_sign", documentSign);
		}
		params.put("user_id", userId);
		params.put("page", pageIndex);
		params.put("count", count);
		fillRequest(params);
		return params;
	}
	
	public static RequestParams getNotesRecommanded(long noteId) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("id", noteId);
		fillRequest(params);
		return params;
	}
	
	public static RequestParams getNotesLikeOrUnlike(String guid) {
		RequestParams params = new RequestParams();
		params.put("jd_user_name", LoginUser.getpin());
		params.put("entity_guid", guid);
		fillRequest(params);
		return params;
	}

	public static RequestParams getuessYouLikeParams(int currentPage) {
		RequestParams params = new RequestParams();
		params.put("functionId", "guessYouLike");
		params.put("body", HttpParamsUtil.getGuessYouLikeBody(currentPage));
//		fillRequest(params);
		return params;
	}
	
	/**
	* @Description: 获取请求图书文件更新参数
	* @param @param bookids 多个图书id
	* @return RequestParams 请求参数
	* @author xuhongwei1
	* @date 2015年10月9日 下午1:56:36 
	* @throws 
	*/ 
	public static RequestParams getEBookUpdateParams(List<String> ebookids) {
		RequestParams params = new RequestParams();
		params.put("functionId", "ebookFileUpdate");
		String ids = "";
		if(ebookids != null && ebookids.size() > 0) {
			ids = "{\"ids\":\"";
			String idsStr = "";
			for(int i=0; i<ebookids.size(); i++) {
				String ebookid = ebookids.get(i);
				idsStr += ebookid;
				if(i < (ebookids.size() - 1)) {
					idsStr += ",";
				}
			}
			ids += idsStr + "\",\"uuid\":\"" + DrmTools.hashDevicesInfor() +"\"}";
		}
		params.put("body", ids);
		return params;
	}
	
	public static RequestParams getTodayBuyedEbookOrderListParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "todayBuyedEbookOrderList");
		fillRequest(params);
		return params;
	}
	
	/**
	* @Description: 请求上传图片参数
	* @author xuhongwei1
	* @date 2015年10月26日 下午3:09:31 
	* @throws 
	*/ 
	public static RequestParams getUploadImageParams(String filename) {
		RequestParams params = new RequestParams();
		params.put("name", filename);
		params.put("key", "upload");
		params.put("contentType", "image/jpeg");
		return params;
	}
	
	/**
	* @Description: 获取广场请求参数
	* @param int currentPage 当前页
	* @param int pageSize 每页显示条数
	* @param int sortType 0 按动态创建的时间倒序， 1 按照热度（评论数量）倒序
	* @author xuhongwei1
	* @date 2015年11月2日 上午11:14:08 
	* @throws 
	*/ 
	public static RequestParams getSquareParams(int currentPage, int pageSize, int sortType) {
		RequestParams params = new RequestParams();
		params.put("functionId", "eBookCommuntiyComments");
		JSONObject json = new JSONObject();

		try {
			json.put("currentPage", currentPage);
			json.put("pageSize", pageSize);
			json.put("sortType", sortType);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
	
	/**
	 * 好评送书打通登录接口param
	 *
	 */
	public static RequestParams getSendBookLoginParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "sentBookLogin");
		JSONObject json = new JSONObject();
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
	
	/**
	 * 获取好评送书是否已经参加过的接口params
	 * @return
	 */
	public static RequestParams getIsExistCommentParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "isExistComment");
		fillRequest(params);
		return params;
	}
	
	/**
	 * 一键购买打通登录接口param
	 *
	 */
	public static RequestParams getOneClickBuyLoginParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "loginBridge");
		JSONObject json = new JSONObject();
		try {
			json.put("urlKey","oneClickBuy");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
	/**
	* @Description: 获取借阅状态接口参数
	* @return RequestParams
	* @author xuhongwei1
	* @date 2015年12月1日 下午1:45:02 
	* @throws 
	*/ 
	public static RequestParams getBorrowStatusParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "buyBorrowStatus");
		fillRequest(params);
		return params;
	}
	
	/**
	* @Description: 获取借阅状态接口参数
	* @param String lendStatus 0关闭借书功能；1开通借书功能 
	* @return RequestParams
	* @author xuhongwei1
	* @date 2015年12月1日 下午1:45:02 
	* @throws 
	*/ 
	public static RequestParams getUpdateBorrowStatusParams(String lendStatus) {
		RequestParams params = new RequestParams();
		params.put("functionId", "updateLendStatus");
		JSONObject json = new JSONObject();
		try {
			json.put("lendStatus", lendStatus);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
	
	/**
	 * 获取用户借阅通知接口参数
	 * @return
	 */
	public static RequestParams getBorrowMessageParams(){
		RequestParams params = new RequestParams();
		params.put("functionId", "getBuyBorrowMsgList");
		JSONObject json = new JSONObject();
		try {
			json.put("unReadType", "4");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
	
	/**
	 * 获取购书送书通知接口参数
	 * @return
	 */
	public static RequestParams getSendBookMessageParams(){
		RequestParams params = new RequestParams();
		params.put("functionId", "getBuyBorrowMsgList");
		JSONObject json = new JSONObject();
		try {
			json.put("unReadType", "5");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
	
	/**
	* @Description: 获取可借阅人数接口参数
	* @return RequestParams
	* @author xuhongwei1
	* @date 2015年12月1日 下午1:45:02 
	* @throws 
	*/ 
	public static RequestParams getLendUserCountParams(String ebookId) {
		RequestParams params = new RequestParams();
		params.put("functionId", "getLendUserCount");
		JSONObject json = new JSONObject();
		try {
			json.put("ebookId", ebookId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
	
	/**
	* @Description: 获取用户借阅请求参数
	* @return RequestParams
	* @author xuhongwei1
	* @date 2015年12月3日 下午5:34:49 
	*/ 
	public static RequestParams getUserBorrowBookParams(String ebookId, String ebookName) {
		RequestParams params = new RequestParams();
		params.put("functionId", "buyBorrow");
		JSONObject json = new JSONObject();
		try {
			json.put("ebookId", ebookId);
			json.put("ebookName", new String(ebookName.getBytes(), "UTF-8"));
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e){
			e.printStackTrace();
		}
System.out.println("BBBBBBB=====getUserBorrowBookParams=======11===body=" + json.toString());
		params.put("body", encryptParams(json.toString()));
System.out.println("BBBBBBB=====getUserBorrowBookParams=======2222==encryptParams==body=" + encryptParams(json.toString()));		
		fillRequest(params);
		return params;
	}
	
	/**
	 * 获取微信支付成功结果页url
	 * @return
	 */
	public static RequestParams getWXPaySuccessUrlParams(String payId){
		RequestParams params = new RequestParams();
		params.put("functionId", "getSuccessUrl");
		JSONObject json = new JSONObject();
		try {
			json.put("payId", payId);
			json.put("appId", "jdpay_ebook");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
	
	/**
	 * 购书赠书统计接口
	 * @return
	 */
	public static RequestParams getSendBookAnalysisParams() {
		RequestParams params = new RequestParams();
		params.put("functionId", "sendButtonClickAnalysis");
		fillRequest(params);
		return params;
	}
	
	/**
	 * 购书赠书状态回滚接口
	 * @return
	 */
	public static RequestParams getRollBackSentStatusParams(String orderId,String ebookId,String t) {
		RequestParams params = new RequestParams();
		params.put("functionId", "rollBackSentStatus");
		JSONObject json = new JSONObject();
		try {
			json.put("orderId", orderId);
			json.put("ebookId", ebookId);
			json.put("t", t);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
	
	
	/**
	* @Description: 获取获赠图书赠言信息
	* @return RequestParams
	* @author xuhongwei1
	* @date 2015年12月1日 下午1:45:02 
	* @throws 
	*/ 
	public static RequestParams getReceiveInfoParams(String ebookId) {
		RequestParams params = new RequestParams();
		params.put("functionId", "getReceiveInfo");
		JSONObject json = new JSONObject();
		try {
			json.put("ebookId", ebookId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
	
	/**
	 * 获取某用户某本书可继续赠送订单列表
	 * @return
	 */
	public static RequestParams getSendableOrderListParams(String ebookId) {
		RequestParams params = new RequestParams();
		params.put("functionId", "sendableOrderList");
		JSONObject json = new JSONObject();
		try {
			json.put("ebookId", ebookId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		params.put("body", json.toString());
		fillRequest(params);
		return params;
	}
}
