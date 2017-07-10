package com.jingdong.app.reader.extension.giftbag;

import java.util.HashMap;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.ACache;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.view.dialog.DialogManager;

/**
 * 礼包获取工具类
 * 
 * @author quda
 *
 */
public class GiftBagUtil {
	private static GiftBagUtil giftBag = new GiftBagUtil();
	/**
	 * 查询是否有礼包
	 */
	public static final int GIFTBAG_INIT = 0x10;
	/**
	 * 获取礼包
	 */
	public static final int GIFTBAG_REQ = 0x11;
	/**
	 * 礼包是否读取
	 */
	public static final int GIFTBAG_UNREAD = 0x12;
	/**
	 * 更新读取礼包状态
	 */
	public static final int GIFTBAG_UPDATE_STATUS = 0x13;

	public static final String KEY = "GIFTBAG";

	public static final String advDesc_key = "advDesc";
	public static final String MESSAGE_KEY = "message";
	public static final String MESSAGE_CODE = "message_code";
	public static int id = 0;
	public static String message;
	public static final String SUCCESS_MESSAGE = "恭喜您!\n成功领取礼包\n";
	public static boolean isHaveShow = false;
	private boolean IS_HAVE_UNREAD_CHANGDUCARD = false;
	private boolean IS_HAVE_DISCOUNT = false;
	private boolean IS_HAVE_INTEGRATION = false;
	private static HashMap<String, Boolean> changducardCache = null;
	private static HashMap<String, Boolean> disCountCache = null;
	private static HashMap<String, Boolean> scoreCountCache = null;
	/**
	 * 畅读卡
	 */
	public static final String CHANGDUCART = "1";
	/**
	 * 优惠券
	 */
	public static final String DISCOUNT = "2";
	/**
	 * 积分
	 */
	public static final String INTEGRATION = "3";
	

	private GiftBagUtil() {
		// TODO Auto-generated constructor stub
	}

	public static GiftBagUtil getInstance() {
		if (changducardCache == null) {
			changducardCache = new HashMap<>();
			disCountCache = new HashMap<>();
			scoreCountCache = new HashMap<>();
		}
		return giftBag;
	}

	/**
	 * 请求是否有礼包
	 * 
	 * @param context
	 */
	public void reqIsHaveGiftBag(Context context) {
		if (context == null) {
			context = MZBookApplication.getContext();
		}

		if (!isHaveShow) {
			Intent service = new Intent(context, GiftBagService.class);
			service.putExtra(KEY, GIFTBAG_INIT);
			context.startService(service);
			isHaveShow = true;
		}else{
//			final Context mContext=context;
//			// 好评送书活动--tmj
//			if (!LocalUserSetting.isSendBookDialogShow(context)) {
//				if (!NetWorkUtils.isNetworkConnected(context))
//					return;
//
//				WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getIsExistCommentParams(), true,
//						new MyAsyncHttpResponseHandler(mContext) {
//
//							@Override
//							public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//							}
//
//							@Override
//							public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
//
//								String result = new String(responseBody);
//								try {
//									JSONObject json = new JSONObject(result);
//
//									int code = json.optInt("code");
//									if (code == 0) {
//										DialogManager.showCommonDialog(mContext, "提示", "好评送好书，壕礼免费拿！", "查看活动", "不再提示",
//												new DialogInterface.OnClickListener() {
//											@Override
//											public void onClick(DialogInterface dialog, int which) {
//												switch (which) {
//												case DialogInterface.BUTTON_POSITIVE:
//
//													WebRequestHelper.get(URLText.JD_BASE_URL,
//															RequestParamsPool.getSendBookLoginParams(), true,
//															new MyAsyncHttpResponseHandler(mContext) {
//														@Override
//														public void onFailure(int arg0, Header[] arg1, byte[] arg2,
//																Throwable arg3) {
//														}
//
//														@Override
//														public void onResponse(int statusCode, Header[] headers,
//																byte[] responseBody) {
//
//															String result = new String(responseBody);
//															try {
//																JSONObject json = new JSONObject(result);
//
//																String url = json.optString("url");
//																String title = json.optString("title");
//																if (url != null && !"".equals(url)) {
//																	Intent intent = new Intent(mContext,
//																			WebViewActivity.class);
//																	intent.putExtra(WebViewActivity.UrlKey, url);
//																	intent.putExtra(WebViewActivity.TopbarKey, true);
//																	intent.putExtra(WebViewActivity.BrowserKey, false);
//																	intent.putExtra(WebViewActivity.TitleKey, title);
//																	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//																	mContext.startActivity(intent);
//																}
//															} catch (JSONException e) {
//																e.printStackTrace();
//															}
//
//														}
//													});
//													break;
//												case DialogInterface.BUTTON_NEGATIVE:
//													LocalUserSetting.saveSendBookDialogShow(mContext);
//													break;
//												default:
//													break;
//												}
//												dialog.dismiss();
//											}
//										});
//									}
//								} catch (JSONException e) {
//									e.printStackTrace();
//								}
//							}
//						});
//			}
			
		}
	}

	/**
	 * 获取未读消息
	 * 
	 * @param context
	 */
	public void getUnReadMessage(Context context) {
		if (context == null) {
			context = MZBookApplication.getContext();
		}
		Intent service = new Intent(context, GiftBagService.class);
		service.putExtra(KEY, GIFTBAG_UNREAD);
		context.startService(service);
	}

	/**
	 * 获取未读消息
	 * 
	 * @param context
	 */
	public void getUpdateReadMessage(Context context, String msg) {
		if (context == null) {
			context = MZBookApplication.getContext();
		}
		Intent service = new Intent(context, GiftBagService.class);
		service.putExtra(KEY, GIFTBAG_UPDATE_STATUS);
		service.putExtra("params", msg);
		context.startService(service);
	}

	public void getGiftBag(Context context) {
		if (context == null) {
			context = MZBookApplication.getContext();
		}
		Intent service = new Intent(context, GiftBagService.class);
		service.putExtra(KEY, GIFTBAG_REQ);
		context.startService(service);
	}

	/**
	 * 当前礼包信息读取状态
	 */
	public synchronized void saveGiftBagInfoReadStatus(String giftRead) {
		if (giftRead == null) {
			IS_HAVE_UNREAD_CHANGDUCARD = false;
			IS_HAVE_DISCOUNT = false;
			IS_HAVE_INTEGRATION = false;
		}
		if (CHANGDUCART.equals(giftRead)) {
			IS_HAVE_UNREAD_CHANGDUCARD = true;
		}
		if (DISCOUNT.equals(giftRead)) {
			IS_HAVE_DISCOUNT = true;
		}
		if (INTEGRATION.equals(giftRead)) {
			IS_HAVE_INTEGRATION = true;
		}
		changducardCache.put(LoginUser.getpin(), IS_HAVE_UNREAD_CHANGDUCARD);
		disCountCache.put(LoginUser.getpin(), IS_HAVE_DISCOUNT);
		scoreCountCache.put(LoginUser.getpin(), IS_HAVE_INTEGRATION);
	}

	public void isClickChangduCard() {
		IS_HAVE_UNREAD_CHANGDUCARD = false;
		changducardCache.put(LoginUser.getpin(), IS_HAVE_UNREAD_CHANGDUCARD);
	}

	public void isClickDisCount() {
		IS_HAVE_DISCOUNT = false;
		disCountCache.put(LoginUser.getpin(), IS_HAVE_DISCOUNT);
	}
	public void isClickIntegration() {
		IS_HAVE_DISCOUNT = false;
		scoreCountCache.put(LoginUser.getpin(), IS_HAVE_DISCOUNT);
	}

	public boolean isCurentUserHaveNewChangDuCard() {
		return changducardCache.get(LoginUser.getpin()) == null;
	}

	public boolean isCurentUserHaveDisCard() {
		return disCountCache.get(LoginUser.getpin()) == null;
	}
	
	public boolean isCurentUserHaveIntegration() {
		return scoreCountCache.get(LoginUser.getpin()) == null;
	}

	public boolean IsHaveNewChangDuCart() {
		return changducardCache.get(LoginUser.getpin()) != null ? changducardCache
				.get(LoginUser.getpin()) : true;
	}

	public boolean IsHaveDiscount() {
		return disCountCache.get(LoginUser.getpin()) != null ? disCountCache
				.get(LoginUser.getpin()) : true;
	}
	public boolean IsHaveIntegration() {
		return scoreCountCache.get(LoginUser.getpin()) != null ? scoreCountCache
				.get(LoginUser.getpin()) : true;
	}
	

	public void clearCache() {
		disCountCache.clear();
		changducardCache.clear();
	}

	/**
	 * 保存礼包领取状态
	 * 
	 * @param key
	 *            礼包ID
	 */
	public void saveGiftBagStatus(int id, int count) {
		ACache acache = MZBookApplication.getInstance().getGiftCache();
		GiftBean gb = new GiftBean();
		gb.id = id;
		gb.showCount = count;
		acache.put(String.valueOf(id), gb, ACache.TIME_DAY * 7);
	}

	/**
	 * 更新礼包状态
	 * 
	 * @param id
	 * @param count
	 */
	public void updateGiftBagStatus(int id, int count) {
		ACache acache = MZBookApplication.getInstance().getGiftCache();
		String key = String.valueOf(id);
		GiftBean gb = getGiftBagStatus(id);
		if (gb != null) {
			gb.id = id;
			gb.showCount = gb.showCount + count;
			acache.remove(key);
		} else {
			gb = new GiftBean();
			gb.id = id;
			gb.showCount = count;
		}
		acache.put(key, gb, ACache.TIME_DAY * 7);
	}

	/**
	 * 获取礼包状态
	 * 
	 * @param key
	 *            uuid+" "+礼包ID
	 * @return GiftBean 领取过礼包 null 未领取过礼包
	 */
	public GiftBean getGiftBagStatus(int id) {
		return (GiftBean) MZBookApplication.getInstance().getGiftCache()
				.getAsObject(String.valueOf(id));
	}

	public boolean isNewVersion(boolean flag) {
		boolean updateVersion = flag;

		return updateVersion;
	}
}
