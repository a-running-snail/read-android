package com.jingdong.app.reader.client;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.Display;
import android.view.WindowManager;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bob.util.DisplayUtil;
import com.jingdong.app.reader.bob.util.MD5Calculator;
import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.data.DrmTools;
import com.jingdong.app.reader.entity.AdEntity;
import com.jingdong.app.reader.entity.BootEntity;
import com.jingdong.app.reader.util.HttpGroup;
import com.jingdong.app.reader.util.HttpGroup.HttpSetting;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.StatisticsReportUtil;

public class ServiceProtocol {
    
	public final static String sCharset = "UTF-8";// 编码

	// 正式环境
	public static String BASE_URL = "http://gw.ebook.360buy.com/client.action";
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

	public static int RECOMMEND_TYPE_BOOK_E = 2;
	public static int RECOMMEND_TYPE_BOOK_MEIDA = 4;
	public static int RECOMMEND_TYPE_BOOK_PAPER = 3;
	public static int RECOMMEND_TYPE_BOOK_HOME = 1; // 现在的新书速递

	public static int AD_TYPE_BOOK_E = 1;
	public static int AD_TYPE_BOOK_PAPER = 2;
	public static int AD_TYPE_BOOK_MEIDA = 3;

	public final static boolean LIST_NET = false; // 本地list中无网络请求
	public final static boolean lIST_NOT_NET = true; // 本地list中有网络请求

	/*
	 * rootId Y 根节点类目ID 1：促销 2：电子书 3：网络原创 4：多媒体电子书 5：纸书 sortKey 排序字段（默认销量） 销量
	 * 价格- 好评度 上架时间 sortType 排序类-型（默认降序） 降序 升序 不排序
	 */
	public static final int CATEGORY_PROMOTION_PID = 0;// 排行的子分类
	public static final int CATEGORY_PROMOTION_RID = 1;// 排行的当前目录
	// 电子书分类接口
	public static final int CATEGORY_EBOOK_PID = 2;
	public static final int CATEGORY_EBOOK_RID = CATEGORY_EBOOK_PID;
	// 网络原创分类接口
	public static final int CATEGORY_ORIGINAL_PID = 3;
	public static final int CATEGORY_ORIGINAL_RID = CATEGORY_ORIGINAL_PID;
	// 免费分类接口
	public static final int CATEGORY_FREE_PID = 5276;
	public static final int CATEGORY_FREE_RID = CATEGORY_EBOOK_PID;

	// 特价专区接口
	public static final int BARGAIN_TYPE1 = 1; // 1元以下
	public static final int BARGAIN_TYPE2 = 2; // 2-3元
	public static final int BARGAIN_TYPE3 = 3; // 3-4元

	public static final int CATEGORY_PROMOTION_SUB = 0;// 排行子目录

	public static final int SORTKEY_XIAOLIANG = 1;
	public static final int SORTKEY_PRICE = 2;
	public static final int SORTKEY_HAOPING = 3;
	public static final int SORTKEY_SHANGJIASHIJIAN = 4;

	public static final int SORTTYPE_UP = 2;
	public static final int SORTTYPE_DOWN = 1;
	public static final int SORTTYPE_NOSORT = 3;

	public static int CURRENTPAGE = 0;
	public static int SIZE_OF_PAGE = 20;

	public static String KEY_CLIENTPLATFORM = "clientPlatform";
	public static int VALUE_CLIENTPLATFORM = 1;
	public static int VALUE_CLIENTPLATFORM_PHONE = 1;
	public static int VALUE_CLIENTPLATFORM_PAD = 4;

	//
	// public static final int TYPE_EBOOK = 1;
	// public static final int TYPE_BOOK = 2;
	// public static final int TYPE_MEDIABOOK = 3;

	// 搜索中获取热门关键词的个数
	public static final int TOTALSIZE_KEYWORD = 10;

	// public static final int TYPE_EBOOK_FREE = 4;// 免费书

	public static String TEST_EBOOKID = "10009504";

	// public static String TEST_BOOKID = "10009504";
	// public static String TEST_MEDIAID = "10009504";
	// public static String TEST_STOCKSADDRESSID = "3";

	// public static HttpPost getRefreshPostRequest(Context context,
	// GJHistoryPostsInfo postInfo, String loginId) {
	// Map<String, String> params = new HashMap<String, String>();
	// params.put("cityScriptIndex",
	// URLEncoder.encode(String.valueOf(postInfo.getCityId())));
	// params.put("categoryId",
	// URLEncoder.encode(String.valueOf(postInfo.getCategoryId())));
	// params.put("majorCategoryScriptIndex",
	// URLEncoder.encode(String.valueOf(postInfo.getMajorCategoryId())));
	// params.put("postId",
	// URLEncoder.encode(String.valueOf(postInfo.getPostId())));
	// params.put("loginId", URLEncoder.encode(loginId));
	//
	// HttpPost request = (HttpPost) getHttpRequestWithHeader(context, params,
	// "json",
	// "RefreshPost", true);
	// return request;
	// }

	/*
	 * 下单拿到电子书token
	 */
	public static HttpSetting getgenTokenHttpSetting(String bookId) 
    {
        
		// String url = "http://"
		// + Configuration.getProperty(Configuration.ORDER_HOST).trim()
		// + "/client.action";
		JSONObject json = null;
		try {
			json = new JSONObject(bookId);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		// httpSetting.setUrl(url);
		httpSetting.setFunctionId("genToken");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	public static HttpSetting getBookMarkHttpSetting(JSONObject json) {
		String url = "http://"
				+ Configuration.getProperty(Configuration.NOTE_HOST).trim()
				+ "/client.action";
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		httpSetting.setFunctionId("sendBookmark");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}
	/**
	 * 同步笔记
	 * @param json
	 * @return
	 */
	public static HttpSetting getBookNoteHttpSetting(JSONObject json){
		String url = "http://"
				+ Configuration.getProperty(Configuration.NOTE_HOST).trim()
				+ "/client.action";
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		httpSetting.setFunctionId("sendBooknote");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	public static HttpSetting getCanUseOlineCardsHttpSetting(JSONObject json) {
		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/client.action";
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setSaveCookie(false);
		httpSetting.setUrl(url);
		httpSetting.setFunctionId("getIsCanUseCards");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/**
	 * 获取某本书的价格
	 * 
	 * @param bookId
	 * @return
	 */
	public static HttpSetting getSingleBookPriceHttpSetting(String bookId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("ebookIds", bookId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		// httpSetting.setUrl(url);
		httpSetting.setFunctionId("getPriceByEbookIds");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/**
	 * 用户是否购买该书
	 * 
	 * @param bookId
	 * @return
	 */
	public static HttpSetting getIsBuyedBookHttpSetting(String bookId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("ebookId", bookId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		// httpSetting.setUrl(url);
		httpSetting.setFunctionId("isBuyedByEbookId");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/**
	 * 在线畅读，检查账户是否有可用的畅读卡。
	 * 
	 */
	public static HttpSetting getCheckHasReadCardHttpSetting(String ebook_id) {
		final JSONObject json = new JSONObject();
		HttpSetting httpSetting = new HttpSetting();
		try {
			json.put("ebook_id", ebook_id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/client.action";
		Log.i("zhang", "final url====" + url);
		// httpSetting.setSaveCookie(false);
		httpSetting.setUrl(url);
		httpSetting.setJsonParams(json);
		httpSetting.setFunctionId("checkReadCard");
		return httpSetting;
	}

	/**
	 * 
	 * 在线畅读，实现畅读卡与书绑定
	 * 
	 */
	public static HttpSetting getBindCardAndBookHttpSetting(String ebook_id) {
		final JSONObject json = new JSONObject();
		HttpSetting httpSetting = new HttpSetting();
		try {
			json.put("ebook_id", ebook_id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/client.action";
		Log.i("zhang", "final url====" + url);
		httpSetting.setUrl(url);
		httpSetting.setJsonParams(json);
		httpSetting.setFunctionId("addNewReadInfo");
		return httpSetting;
	}

	
	/**
	 * @author keshuangjie
	 * @description 免费畅读卡领取
	 */
	public static HttpSetting getFreeReadCard() {
		HttpSetting httpSetting = new HttpSetting();
		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/client.action";
		Log.i("zhang", "final url====" + url);
		final JSONObject json = new JSONObject();
		httpSetting.setUrl(url);
		httpSetting.setFunctionId("insertFreeCard");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/**
	 * 
	 * 在线畅读，获取销售的畅读卡。
	 * 
	 */
	public static HttpSetting getOnlineReadCardHttpSetting() {
		HttpSetting httpSetting = new HttpSetting();
		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/client.action";
		Log.i("zhang", "final url====" + url);
		final JSONObject json = new JSONObject();
		httpSetting.setUrl(url);
		// httpSetting.setSaveCookie(false);
		httpSetting.setFunctionId("cardInfo");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/**
	 * 
	 * 在线畅读，我的畅读书列表。
	 * 
	 */
	public static HttpSetting getMyOnlineBookHttpSetting(int currentPage,
			int pageSize) {
		final JSONObject json = new JSONObject();
		HttpSetting httpSetting = new HttpSetting();
		try {
			json.put("currentPage", currentPage);
			json.put("pageSize", pageSize);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/client.action";
		Log.i("zhang", "final url====" + url);
		// httpSetting.setSaveCookie(false);
		httpSetting.setUrl(url);
		httpSetting.setJsonParams(json);
		httpSetting.setFunctionId("myCardReadBook");
		return httpSetting;
	}

	/**
	 * 
	 * 在线畅读，我的畅读搜索。
	 * 
	 */
	public static HttpSetting getmyCardReadBookHttpSetting(int currentPage,
			int pageSize, String bookNameOrAuthor) {
		final JSONObject json = new JSONObject();
		HttpSetting httpSetting = new HttpSetting();
		try {
			json.put("currentPage", currentPage);
			json.put("pageSize", pageSize);
			if (!TextUtils.isEmpty(bookNameOrAuthor)) {
				json.put("bookNameOrAuthor", bookNameOrAuthor);
			}
			;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/client.action";
		Log.i("zhang", "final url====" + url);
		// httpSetting.setSaveCookie(false);
		httpSetting.setUrl(url);
		httpSetting.setJsonParams(json);
		httpSetting.setFunctionId("myCardReadBook");
		return httpSetting;
	}

	/**
	 * 
	 * 在线畅读，我购买的畅读卡列表。
	 * 
	 */
	public static HttpSetting getMyOnlineReadCardHttpSetting() {
		HttpSetting httpSetting = new HttpSetting();
		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/client.action";
		Log.i("zhang", "final url====" + url);
		final JSONObject json = new JSONObject();
		httpSetting.setUrl(url);
		// httpSetting.setSaveCookie(false);
		httpSetting.setFunctionId("myReadCard");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/**
	 * @author keshuangjie
	 * @time 2012-12-17 下午4:16:34
	 * @version version
	 * @description 畅读卡绑定
	 */
	public static HttpSetting getReadCardBindingHttpSetting(String cardNo,
			String cardPassword) {
		final JSONObject json = new JSONObject();
		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/client.action";
		try {
			json.put("cardNo", cardNo);
			json.put("cardPwd", cardPassword);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		httpSetting.setFunctionId("readCardBinding");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/**
	 * @author keshuangjie
	 * @time 2012-12-17 下午4:27:44
	 * @version version
	 * @description 畅读卡激活
	 */
	public static HttpSetting getReadCardActivateHttpSetting(String cardNo) {
		final JSONObject json = new JSONObject();
		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/client.action";
		try {
			json.put("cardNo", cardNo);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		httpSetting.setFunctionId("readCardActivate");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	public static HttpSetting getTestTokenHttpSetting(String order) {
		// String url = "http://"
		// + Configuration.getProperty(Configuration.ORDER_HOST)
		// .trim()+"/order_orderListPay.action";
		final JSONObject json = new JSONObject();
		try {
			json.put("order_id", order);
			json.put("isSupportJs", "true");
			if (com.jingdong.app.reader.config.Configuration
					.getBooleanProperty(
							com.jingdong.app.reader.config.Configuration.SINGLEBOOK,
							false)) {
				json.put("supportUnionPay", "false");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		// httpSetting.setUrl(url);
		httpSetting.setFunctionId("genOrderPayToken");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/****
	 * 使用Type 纸质书下单Token zOrderGetToken 下单
	 * */
	public static HttpSetting getTokenHttpSetting(String functionId,
			String tokenKey) {
		final JSONObject json = new JSONObject();
		try {
			json.put("tokenKey", tokenKey);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId(functionId);
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/****
	 * @see getShoppingCartHttpSetting 计算购物车接口
	 * */
	public static HttpSetting getShoppingCartHttpSetting(JSONObject jsonObject) {
		String url = "http://"
				+ Configuration.getProperty(Configuration.ORDER_HOST).trim()
				+ "/client.action";
		final JSONObject json = new JSONObject();
		try {
			json.put("tokenKey", jsonObject);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		httpSetting.setFunctionId("cart");
		httpSetting.setJsonParams(jsonObject);
		return httpSetting;
	}

	public static HttpSetting getMbookTokenHttpSetting(String bookId) {
       
		final JSONObject json = new JSONObject();
		try {
			json.put("ebook_id", bookId);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("genToken");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * CPS unionId:cps联盟号 unionSiteId:联盟站点ID
	 */
	public static HttpSetting pushCPSHttpSetting(String unionId,
			String unionSiteId) 
{
		final JSONObject json = new JSONObject();
		try {
			json.put("unionId", unionId);
			json.put("subunionId", unionSiteId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setJsonParams(json);
		httpSetting.setFunctionId("cps");
		
        return httpSetting;
	}

	/*
	 * 启动页面
	 */
	public static HttpSetting getStartScreenHttpSetting() {
		HttpSetting httpSetting = new HttpSetting();
		final JSONObject json = new JSONObject();
		try {
			if (DisplayUtil.isHDDisplay()) {
				json.put(KEY_CLIENTPLATFORM, VALUE_CLIENTPLATFORM_PHONE);
			} else {
				json.put(KEY_CLIENTPLATFORM, VALUE_CLIENTPLATFORM_PAD);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		httpSetting.setJsonParams(json);
		httpSetting.setFunctionId("startScreen");
		return httpSetting;
	}

	/*
	 * CPA获取校验token
	 */
	public static HttpSetting getcpaTokenHttpSetting() {
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("cpaToken");
		// httpSetting.setSaveCookie(false);
		/*
		 * 因为服务器那边记录的cpa数据，只要deviceId和mac地址是全的 旧版本会带两次uuid数据，服务器将两个uuid合成了一个，
		 * 所以为了防止重复，除了框架里的加的uuid，这个接口依然会多带一次uuid
		 */
		// StatisticsReportUtil.readDeviceUUID();
		// if (StatisticsReportUtil.getValidDeviceUUIDByInstant() != null) {
		// httpSetting.putMapParams("uuid",
		// StatisticsReportUtil.readDeviceUUID());
		// }
		return httpSetting;
	}

	/*
	 * :CPA提交数据信息
	 */
	public static HttpSetting getcpaPushDataHttpSetting(String ticket,
			String unionId, String info) {
		final JSONObject json = new JSONObject();
		try {
			json.put("token", ticket);
			json.put("unionId", unionId);
			json.put("info", info);
			// json.put("uuid", StatisticsReportUtil.readDeviceUUID());

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		if (HttpGroup.isFormSkyDriver()) {
			httpSetting.setSaveCookie(false);
		}
		httpSetting.setJsonParams(json);
		httpSetting.setFunctionId("cpaPushData");
		/*
		 * 因为服务器那边记录的cpa数据，只要deviceId和mac地址是全的 旧版本会带两次uuid数据，服务器将两个uuid合成了一个，
		 * 所以为了防止重复，除了框架里的加的uuid，这个接口依然会多带一次uuid
		 */
		StatisticsReportUtil.readDeviceUUID();
		if (StatisticsReportUtil.getValidDeviceUUIDByInstant() != null) {
			httpSetting.putMapParams("uuid",
					StatisticsReportUtil.readDeviceUUID());
		}
		return httpSetting;
	}

	/*
	 * 获取对称加密KEY
	 */
	public static HttpSetting getSessionKeyHttpSetting(String envelopeKey) {
		final JSONObject json = new JSONObject();
		try {
			json.put("envelopeKey", envelopeKey);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setJsonParams(json);
		if (HttpGroup.isFormSkyDriver()) {
			httpSetting.setSaveCookie(false);
		}
		httpSetting.setFunctionId("sessionKey");
		return httpSetting;
	}

	/*
	 * 获取RSA安全公钥
	 */
	public static HttpSetting getRsaPublicKeyHttpSetting() {
		HttpSetting httpSetting = new HttpSetting();
		// httpSetting.setJsonParams(json);
		httpSetting.setFunctionId("rsaPublicKey");
		return httpSetting;
	}

	/*
	 * 下单拿到礼品卡token
	 */
	public static HttpSetting getEcardTokenHttpSetting(JSONObject jsonObject) {

		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("genGiftToken");
		httpSetting.setJsonParams(jsonObject);
		return httpSetting;
	}

	public static HttpUriRequest getHttpRequestWithHeader(Context context,
			Map<String, String> params, String contentFormat, String method,
			boolean isPost) {
		return null;
	}

	/*
	 * 图书详情页面--获取图书详情
	 */
	public static HttpSetting getbookDetailBasicInfoHttpSetting(String bookId,
			String stockId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookId", bookId);
			json.put("stockId", stockId);
			json.put("supportPdf", true);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("bookDetailBasicInfo");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 图书详情页面 --图书库存信息
	 */
	public static HttpSetting getbookStocksAddressStatusHttpSetting(
			String bookId, String bookStocksAddressId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookId", bookId);
			json.put("bookStocksAddressId", bookStocksAddressId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("bookStocksAddressStatus");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 图书详情页面 --推荐
	 */
	public static HttpSetting getbookDetailRecommendListHttpSetting(
			String bookId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookId", bookId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("bookDetailRecommendList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 图书详情页面--促销信息。
	 */
	public static HttpSetting getbookPromotionInforHttpSetting(String bookId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("ebook_id", bookId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("getPromotionInfo");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	// /*
	// * 设备信息注册
	// */
	// public static HttpSetting getDeviceInfoRegisterHttpSetting() {
	// final JSONObject json = new JSONObject();
	// HttpSetting httpSetting = new HttpSetting();
	// httpSetting.setFunctionId("device");
	// httpSetting.setJsonParams(json);
	// httpSetting.putMapParams("brand", DeviceUtil.getBranch());
	// httpSetting.putMapParams("model", DeviceUtil.getModel());
	// return httpSetting;
	// }

	/*
	 * 登录
	 */
	public static HttpSetting getUserLoginHttpSetting(String userName,
			String passWord) {
		final JSONObject json = new JSONObject();
		try {
			json.put("loginname", userName);
			json.put("loginpwd", passWord);
			Boolean pomotionVersion = com.jingdong.app.reader.config.Configuration
					.getBooleanProperty(
							com.jingdong.app.reader.config.Configuration.DOSWITCHPOMOTIONVERSION,
							false);
			if (pomotionVersion) {
				json.put("giftCardInfo", "pomotionVersion");
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("unionLogin");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/**
	 * @author zhangmurui
	 * @time 2013-5-31
	 * @version 1.3.0
	 * @description 绑定畅读卡(为月卡)
	 */
	public static HttpSetting getBindOnlineCardHttpSetting() {
		final JSONObject json = new JSONObject();
		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/client.action";
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		httpSetting.setFunctionId("insertFreeMonthCard");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/**
	 * @author zhangmurui
	 * @time 2013-5-31
	 * @version 1.3.0
	 * @description 绑定畅读卡(为季卡)
	 */
	public static HttpSetting getBindSeansonCardHttpSetting() {
		final JSONObject json = new JSONObject();
		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/client.action";
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		httpSetting.setFunctionId("insertFreeSeasonCard");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/**
	 * @author keshuangjie
	 * @time 2012-12-5 上午10:24:08
	 * @version 1.2.2
	 * @description 绑定赠送书
	 */
	public static HttpSetting getBindPresentBookHttpSetting() {
		final JSONObject json = new JSONObject();
		try {
			json.put("vid", "701");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("batchGetFreeBook");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 注册
	 */
	public static HttpSetting getUserRegisterHttpSetting(String mail,
			String passWord, String userName, String passWord2) {
		final JSONObject json = new JSONObject();
		try {
			json.put("mail", mail);
			json.put("pwd", passWord);
			json.put("username", userName);
			json.put("pwd2", passWord2);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("register");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 资讯页面获取资讯详情
	 */
	public static HttpSetting getshowBookShortMsgHttpSetting(
			String bookShortMsgId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookShortMsgId", bookShortMsgId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("showBookShortMsg");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 推荐 rtype : 1. 电子书 2. 纸质书 3. 多媒体电子书 4. 首页推荐 currentPage : 当前页码 pageSize :
	 * 每页数量
	 */
	public static HttpSetting getRecommendListHttpSetting(int rtype,
			int currentPage, int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("rtype", rtype);
			json.put("currentPage", currentPage);
			json.put("pageSize", pageSize);
			json.put(KEY_CLIENTPLATFORM, VALUE_CLIENTPLATFORM);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("recommendList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 限时免费接口
	 */
	public static HttpSetting getLimitFreeListHttpSetting() {
		final JSONObject json = new JSONObject();
		try {
			json.put(KEY_CLIENTPLATFORM, VALUE_CLIENTPLATFORM);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("limitTimeFree");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/**
	 * 特价专区接口
	 * 
	 * @param type
	 *            1:一元以下 2:2-3元 3:3-4元
	 * @param currentPage
	 * @param pageSize
	 * @return
	 */
	public static HttpSetting getLowPriceListHttpSetting(int type,
			int currentPage, int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("type", type);
			json.put("currentPage", currentPage);
			json.put("pageSize", pageSize);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("lowPriceSection");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	public static HttpSetting getJoshHttpSetting(HttpSetting joshhttpSetting,
			int currentPage) {
		JSONObject json = joshhttpSetting.getJsonParams();
		json.remove("currentPage");
		try {
			json.put("currentPage", currentPage);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		joshhttpSetting.setJsonParams(json);
		return joshhttpSetting;
	}

	public static HttpSetting getBookHttpSetting(HttpSetting joshhttpSetting,
			int sortType, String sortKey) {
		JSONObject json = joshhttpSetting.getJsonParams();
		json.remove("sortType");
		json.remove("sortKey");
		try {
			json.put("sortType", sortType);
			json.put("sortKey", sortKey);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		joshhttpSetting.setJsonParams(json);
		return joshhttpSetting;
	}

	/*
	 * 广告接口rtype : 1. 电子书 2. 纸质书 3. 多媒体电子书 4. 分类首页
	 */
	public static HttpSetting getadListHttpSetting(int rtype) {
		final JSONObject json = new JSONObject();
		switch (rtype) {
		case 1:
			rtype = AD_TYPE_BOOK_E;
			break;
		case 0:
			rtype = AD_TYPE_BOOK_PAPER;
			break;
		case 2:
			rtype = AD_TYPE_BOOK_MEIDA;
			break;

		}
		try {
			json.put("rtype", rtype);
			json.put(KEY_CLIENTPLATFORM, VALUE_CLIENTPLATFORM);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("adList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 获取子分类列表接口pid : 当前类目IDrid ： 根节点类目ID 1：促销 2：电子书 3：网络原创 4：多媒体电子书 5：纸书
	 */
	public static HttpSetting getchildCategoryListHttpSetting(int pid, int rid) {
		final JSONObject json = new JSONObject();
		try {
			json.put("pid", pid);
			json.put("rid", rid);
			json.put(KEY_CLIENTPLATFORM, VALUE_CLIENTPLATFORM);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("childCategoryList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 获取虚拟类目下图书列表catId : 类目IDrootId ： 根节点类目IDsortKey ： 排序字段（默认销量） 1，销量 2，价格
	 * 3，好评度 4，上架时间 sortType ： 排序类型（默认降序） 1， 降序 2， 升序 3， 不排序 currentPage ： 当前页码
	 * pageSize: 每页数量
	 */
	public static HttpSetting getcategoryBookListHttpSetting(int catId,
			int rootId, int sortKey, int sortType, int currentPage, int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("catId", catId);
			json.put("rootId", rootId);
			json.put("sortKey", sortKey);
			json.put("sortType", sortType);
			json.put("currentPage", currentPage);
			json.put("pageSize", pageSize);
			json.put(KEY_CLIENTPLATFORM, VALUE_CLIENTPLATFORM);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("categoryBookList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 获取礼品卡商品接口 ,查询所有虚拟的礼品卡商品信息列表，数据更新频率低，考虑缓存30分钟
	 */
	public static HttpSetting getlpCardHttpSetting() {
		final JSONObject json = new JSONObject();

		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("lpCard");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 获取图书咨询列表信息
	 */
	public static HttpSetting getbookShortMsgListHttpSetting(int currentPage,
			int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("currentPage", currentPage);
			json.put("pageSize", pageSize);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("bookShortMsgList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 根据类目ID获取和类型获取类目下的图书信息，包括纸书列表，电子书列表、促销图书列表，数据缓存1个小时 bookId : 图书ID
	 */
	public static HttpSetting getbookIntroDetailHttpSetting(String bookId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookId", bookId);// "10009504"
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("bookIntroDetail");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 描述信息 bookId : 图书ID type : 请求类型 1：内容摘要 2：作者简介 3：图书目录
	 */
	public static HttpSetting getbookIntroInfoHttpSetting(String bookId,
			String type) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookId", bookId);
			json.put("type", type);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("bookIntroInfo");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 查询所有评论内容和发表评论，评论列表数据缓存10分钟,查询评论列表
	 * 
	 * bookId : 图书ID type : 0：全部评论 1：很喜欢评论 2：一般评论 3：不喜欢评论 currentPage : 当前页码
	 * pageSize : 每页数量
	 */
	public static HttpSetting getcommentListHttpSetting(String bookId,
			String type, int currentPage, int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookId", bookId);
			json.put("type", type);
			json.put("currentPage", currentPage);
			json.put("pageSize", pageSize);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("commentList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 加关注接口
	 * 用户对商品进行加关注操作，前置条件必须是登录用户，并且1个小时内限制操作次数200次，访问次数记录到memcached中，每次访问次数加一
	 * ，对操作次数达到上线的用户操作记录异常表 bookId : 图书ID
	 */
	public static HttpSetting getattentionHttpSetting(String bookId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookId", bookId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("attention");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 加入购物车 用户把商品加入购物车，需要判断当前商品是否可购买，主要针对纸质图书，不支持寄存的 * skuId : 纸书ID
	 */
	public static HttpSetting getcanBuyHttpSetting(int skuId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("skuId", skuId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("canBuy");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 用户取消单个收藏
	 */
	public static HttpSetting getcacelFollowHttpSetting(String followId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("fid", followId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("cancelFavorite");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 用户取消所有收藏
	 */
	public static HttpSetting getcancelAllFavorites(String bookType) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookType", bookType);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("cancelAllFavorites");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 试读接口 获取epub电子书试读章节信息和试读具体内容,数据更新频率不高，考虑缓存时间1天
	 * 
	 * 获取epub试读章节 * bookId : 图书ID
	 */
	public static HttpSetting getreadEpubHttpSetting(String bookId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookId", bookId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/read/tryReadCategory.action";

		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		// httpSetting.setFunctionId("readEpub");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	public static HttpSetting getreadEpubChapterHttpSetting(String bookId,
			String chapId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookId", bookId);
			json.put("chapterId", chapId);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String url = "http://"
				+ Configuration.getProperty(Configuration.ONLINE_READ_HOST)
						.trim() + "/read/tryReadContent.action";
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		// httpSetting.setFunctionId("readChapter");

		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 搜索图书 参数名称 是否必填 描述 keyword Y 搜索关键词 sortKey 排序字段（默认销量） 1,销量 2,价格 3,好评度
	 * 4,相关度 sortType 排序类型（默认降序） 1,降序 2,升序 3,不排序 bookType 图书所属类型 0,所有 1,促销专区
	 * 2,电子书 3,网络原创 4,多媒体电子书 5,纸质图书 currentPage 当前页（默认1） pageSize 每页数量
	 */
	public static HttpSetting getsearchBookHttpSetting(String keyword,
			String sortType, int bookType, int currentPage, int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			Log.i("zhuyang1", "keyword1 :" + keyword);
			json.put("keyword", keyword);
			// json.put("sortKey", sortKey);
			json.put("sortType", sortType);
			json.put("bookType", bookType);
			json.put("currentPage", currentPage);
			json.put("pageSize", pageSize);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("searchBook");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 筛选搜索图书 参数名称 是否必填 描述 keyword Y 搜索关键词
	 */
	public static HttpSetting getsiftSearchBookHttpSetting(String keyword) {
		final JSONObject json = new JSONObject();
		try {
			json.put("keyword", keyword);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("siftSearchBook");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 条形码搜索图书 参数名称 是否必填 描述 keyword Y 搜索关键词
	 */
	// public static HttpSetting getbarcodeSearchBookHttpSetting(String barcode)
	// {
	// final JSONObject json = new JSONObject();
	// try {
	// json.put("barcode", barcode);
	//
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// HttpSetting httpSetting = new HttpSetting();
	// httpSetting.setFunctionId("barcodeSearchBook");
	// httpSetting.setJsonParams(json);
	// return httpSetting;
	// }

	/*
	 * 我的京东数量
	 */
	public static HttpSetting getmyJdCountHttpSetting() {
		final JSONObject json = new JSONObject();
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("newSummary");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 注销接口
	 */
	public static HttpSetting getlogoutHttpSetting() {
		final JSONObject json = new JSONObject();

		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("logout");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 我的订单列表 输入参数 参数名称 是否必填 描述 示例 currentPage 当前页码 pageSize 每页数量
	 */
	public static HttpSetting getmyOrderListHttpSetting(int currentPage,
			int pageSize, boolean monthMore) {
		final JSONObject json = new JSONObject();
		try {
			json.put("page", currentPage);
			json.put("pageSize", pageSize);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		if (monthMore)
			httpSetting.setFunctionId("leBookBeforeOneMonthOrderList");
		else
			httpSetting.setFunctionId("leBookOneMonthOrderList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 查看订单 输入参数 参数名称 是否必填 描述 orderId Y 订单编号
	 */
	public static HttpSetting getshowOrderHttpSetting(int orderId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("orderId", orderId);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("showOrder");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 订单详情 输入参数 参数名称 是否必填 描述 示例 orderId Y 订单编号
	 */
	public static HttpSetting getorderDetailHttpSetting(long orderId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("orderId", orderId);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("leBookOrderDetailList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 我的收藏商品列表 输入参数 参数名称 是否必填 描述 示例 bookType Y 图书类型 电子书 纸质图书 多媒体电子书 2
	 * currentPage 当前页码 pageSize 每页数量
	 */
	public static HttpSetting getmyFavoritesListHttpSetting(int bookType,
			int currentPage, int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("bookType", bookType);
			json.put("currentPage", currentPage);
			json.put("pageSize", pageSize);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("favorites");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*****
	 * 此函数从1.3.0中开始，已购电子书-已购接口（ 合并了已购与赠送的数据）
	 * 
	 * @param pageSize
	 *            每页数量
	 * 
	 * @since 1.3.0
	 * **************/
	@SuppressWarnings("deprecation")
	public static HttpSetting getPurchedProductListHttpSetting(int page,
			int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			// json.put("orderId", bookType);
			json.put("page", page);
			json.put("pageSize", pageSize);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("newBuyedEbookAndGiveBookList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/************
	 * 已购电子书-已购-搜索接口（合并 已购与赠送）
	 * 
	 * @author ThinkinBunny
	 * @since 2013-5-14 21:44:53 1.3.0
	 * @param key
	 *            search keyword
	 * @param pageSize
	 *            返回结果大小
	 * ****/
	@SuppressWarnings("deprecation")
	public static HttpSetting getPurchedOrderSearch(String key, int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			// json.put("orderId", bookType);
			// json.put("page", page);
			json.put("pageSize", pageSize);
			json.put("key", key);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("buyedEbookAndGiveBookSearch");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 我的京东E卡列表输入参数 参数名称 是否必填 描述 示例 currentPage 当前页码 1 pageSize 每页数量 10
	 */
	public static HttpSetting getmyJdECardListHttpSetting(int currentPage,
			int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("page", currentPage);
			json.put("pageSize", pageSize);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("eCardList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/**
	 * 电子书支付包月 获取token <br>
	 * { "code": "0",//成功 "tokenKey":"xxxxxxxxx" //缓存key
	 * "url":""http://order.e.360buy.net/chargePay_charge.action" //支付页面跳转action
	 * }
	 * 
	 */
	public static HttpSetting genMonthChargeTokenSetting() {
		final JSONObject json = new JSONObject();
		try {
			json.put("chargeToken", true);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting
				.setHost(Configuration.getProperty(Configuration.ORDER_HOST));
		httpSetting.setFunctionId("genMonthChargeToken");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/***
	 * 获得服务和包月卡得接口
	 * ******************************/
	public static HttpSetting genMonthChargeServerSetting(int currentPage,
			int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("currentPage", currentPage + "");
			json.put("pageSize", pageSize + "");

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting
				.setHost(Configuration.getProperty(Configuration.ORDER_HOST));
		httpSetting.setFunctionId("chargeAndCard");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/************ 取消包月 **************************/
	public static HttpSetting genCancleMonthChargeServerSetting(int serverId) {
		final JSONObject json = new JSONObject();
		try {
			json.put("serverId", serverId);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting
				.setHost(Configuration.getProperty(Configuration.ORDER_HOST));
		httpSetting.setFunctionId("cancelServer");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 我的试读过的数字商品列表输入参数 参数名称 是否必填 描述 示例 currentPage 当前页码 pageSize 每页数量
	 */
	public static HttpSetting getmyTryReadListHttpSetting(int currentPage,
			int pageSize) {
		final JSONObject json = new JSONObject();
		try {
			json.put("page", currentPage);
			json.put("pageSize", pageSize);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("probationBookList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 获取热门搜索关键词
	 */
	public static HttpSetting getkeywordByRandHttpSetting(int totalSize) {
		JSONObject json = new JSONObject();
		try {
			json.put("total", totalSize);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("keywordByRand");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 我的账户余额
	 */
	public static HttpSetting getmyAccountBalanceHttpSetting() {
		final JSONObject json = new JSONObject();
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("balance");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 我的优惠券需要修改！
	 */
	public static HttpSetting getmyDiscountCardHttpSetting() {
		final JSONObject json = new JSONObject();
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("coupon");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 问题反馈输入参数 参数名称 是否必填 描述 示例 title 标题 content 内容 email 电子邮件地址
	 */
	public static HttpSetting getproblemFeedbackHttpSetting(String title,
			String content, String email) {
		final JSONObject json = new JSONObject();
		try {
			json.put("title", title);
			json.put("content", content);
			json.put("email", email);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("problemFeedback");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	public static HttpSetting getAdHttpSetting(AdEntity adEntity) {
		HttpSetting httpSetting = new HttpSetting();
		final JSONObject json = new JSONObject();
		try {

			json.put("adCatId", adEntity.catId);
			json.put("adClickType", adEntity.adClickType);
			json.put("currentPage", 0);
			json.put("pageSize", 20);
			json.put("support", true);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		httpSetting.setFunctionId("adBookList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 从广告页面跳转进分类页面
	 */
	public static HttpSetting getAd2CatHttpSetting(AdEntity adEntity,
			int sortKey, int sortType) {
		HttpSetting httpSetting = new HttpSetting();
		final JSONObject json = new JSONObject();
		try {

			json.put("adCatId", adEntity.catId);
			json.put("adClickType", adEntity.adClickType);
			json.put("currentPage", ServiceProtocol.CURRENTPAGE);
			json.put("pageSize", ServiceProtocol.SIZE_OF_PAGE);
			json.put("sortKey", sortKey);
			json.put("sortType", sortType);
			json.put(KEY_CLIENTPLATFORM, VALUE_CLIENTPLATFORM);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		httpSetting.setFunctionId("adBookList");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	/*
	 * 版本更新
	 */
	public static HttpSetting getgetVersionHttpSetting() {
		final JSONObject json = new JSONObject();

		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("getVersion");
		httpSetting.setJsonParams(json);
		return httpSetting;
	}

	public static HttpSetting getBookUrlHttpSetting(BootEntity boot) {
		HttpSetting httpSetting = new HttpSetting();
		try {
			JSONObject json = boot.getContentJosh();
			JSONObject body = json.getJSONObject("body");
			String hardwareUUID = DrmTools.hashDevicesInfor();
			body.put("uuid", hardwareUUID);
			httpSetting.setJsonParams(body);
			String url = json.getString("url");
			// Log.d("zhoubo",
			// "body.getString(uuid)===========" + body.getString("uuid"));
			// Log.d("zhoubo", "url===========" + url);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return httpSetting;
	}

	public static HttpSetting getUploadDownRecordHttpSetting(long bookId) {
		final JSONObject json = new JSONObject();
		String url = "http://"
				+ Configuration.getProperty(Configuration.HOST).trim()// +
				// "10.12.156.51"//"10.10.225.230:9005"//"12.10.144.79"//Configuration.getProperty(Configuration.ONLINE_READ_HOS).trim()
				+ "/downrecord/downrecord_insert.action";
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		httpSetting.setJsonParams(json);
		httpSetting.putMapParams("ebookId", "" + bookId);
		httpSetting.putMapParams("key", MD5Calculator.getKey(bookId));
		// Log.i("getUploadDownRecordHttpSetting","bookId=" + bookId);
		// Log.i("getUploadDownRecordHttpSetting", "key=" +
		// MD5Util.md5Hex(bookId + "ebook!@#$%^*()admin"));
		return httpSetting;
	}

	public static HttpSetting getBookverifyHttpSetting(String userId,
			String ebookId, String orderId) {
		final JSONObject json = new JSONObject();
		HttpSetting httpSetting = new HttpSetting();
		try {
			json.put("userId", userId);
			json.put("ebookId", ebookId);
			json.put("pdf", 1);
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
		Log.i("zhoubo", "final url====" + url);
		httpSetting.setUrl(url);
		httpSetting.setJsonParams(json);
		// httpSetting.putMapParams("client", "android1");// 支持pdf的字段
		// httpSetting.setSaveCookie(false);
		httpSetting.setFunctionId("verify");
		return httpSetting;
	}

	// public static void put(JSONObject json,String name,String value){
	// try {
	// value = URLEncoder.encode(value, sCharset);
	// json.put(name, value);
	// } catch (UnsupportedEncodingException e) {
	// throw new RuntimeException(e);
	// }catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// public static HttpSetting getBookContentHttpSetting(BootEntity boot) {
	// HttpSetting httpSetting = new HttpSetting();
	// try {
	// JSONObject json = boot.getContentJosh();
	// JSONObject body = json.getJSONObject("body");
	// httpSetting.setJsonParams(body);
	// httpSetting.putMapParams("version", json.getString("version"));
	// String url = json.getString("url");
	// httpSetting.setUrl(url);
	// //Log.d("zhoubo", "url===========" + url);
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// return httpSetting;
	// }

	// public static String EncodeCode(String string) {
	// String str = "";
	// for (int i = 0; i < string.length(); i++) {
	// int ch = string.charAt(i);
	// String s4 = Integer.toHexString(ch);
	// str = str + s4;
	// }
	// return str;
	// }

	// public static HttpSetting getBookCertHttpSetting(BootEntity boot,
	// String deviceInfo, String hardwareUUID) {
	// JSONObject body = null;
	// HttpSetting httpSetting = new HttpSetting();
	// String url = null;
	// try {
	// JSONObject bodyJson = boot.getCertJosh();
	// body = bodyJson.getJSONObject("body");
	// body.put("hasCert", "0");
	// body.put("deviceType", "A");// Type A：android
	// httpSetting.setJsonParams(body);
	// //Log.d("zhoubo", " body.toString()===========" + body.toString());
	// //Log.d("zhoubo", "body.getString(uuid)===========" +
	// body.getString("uuid"));
	// String uuid = body.getString("uuid");
	// //Log.d("zhoubo", "uuid.length===========" + uuid.length());
	// url = bodyJson.getString("url");
	//
	// } catch (JSONException e) {
	// e.printStackTrace();
	// }
	// httpSetting.setURLEncoder(false);
	// return httpSetting;
	// }

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
				params.put("body", URLEncoder.encode(body.toString(), sCharset));
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
	/*
	public static HttpUriRequest getCertRequest(BootEntity boot, String uuid,
			boolean israndom) {
		HttpUriRequest request = null;
		String url = "";
		request = new HttpGet();
		Map<String, String> params = new HashMap<String, String>();
		JSONObject body = null;
		HttpSetting httpSetting = new HttpSetting();
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
			body.put("hasCert", "0");
			body.put("userId", LoginUser.getpin());// 验证用户名。
			body.put("deviceType", "A");// Type A：android
			httpSetting.setJsonParams(body);
			// Log.d("zhoubo", " body.toString()===========" + body.toString());
			// Log.d("zhoubo",
			// "body.getString(uuid)===========" + body.getString("uuid"));
			try {
				params.put("body", URLEncoder.encode(body.toString(), sCharset));
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			url = bodyJson.getString("url");
			request = getRequestWithParams(params, url);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return request;
	}
	*/
	public static HttpUriRequest getRequestWithParams(
			Map<String, String> params, String url) {
		StringBuilder buf = new StringBuilder(url);
		fillRequest(params);
		Log.i("zhoubo", "params.size()===" + params.size());
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
		Log.i("zhoubo", "url===" + url);
		HttpUriRequest request = new HttpGet(url);
		setCookie(request);
		return request;
	}

	private static void setCookie(HttpUriRequest request) {
		String cookie = HttpGroup.getCookies();
		if (cookie != null) {
			request.addHeader("Cookie", cookie);
		}
	}

	//

	public static void fillRequest(Map<String, String> params) {
		params.put("clientVersion",
				StatisticsReportUtil.getSoftwareVersionName());
		params.put("&build=", StatisticsReportUtil.getSoftwareBuildName());
		params.put("client", "android");
		params.put("os", "android");
		params.put("osVersion", Build.VERSION.RELEASE);

		Display display = ((WindowManager) MZBookApplication.getInstance()
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		params.put("screen", display.getWidth() + "*" + display.getHeight());
	}

	public static HttpGet getLoadImageRequest(Context context, String url) {
		String finalurl = null;
		// try {
		// finalurl = URLEncoder.encode(url, sCharset);
		// } catch (UnsupportedEncodingException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// if(TextUtils.isEmpty(finalurl)){
		finalurl = url.trim();
		// }
		// if(finalurl.length()>76){
		// Log.i("ServiceProtocol", "finalurl=="+finalurl.substring(76));
		// }
		HttpGet request = new HttpGet(finalurl);
		return request;
	}

	public static String url2IP(String url) {

		Set<String> keys = sHashMap.keySet();
		for (String key : keys) {
			String value = sHashMap.get(key);
			url = url.replace(key, value);
		}
		return url;
	}

}
