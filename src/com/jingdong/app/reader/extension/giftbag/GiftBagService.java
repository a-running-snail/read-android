package com.jingdong.app.reader.extension.giftbag;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.jingdong.app.reader.activity.WebViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.entity.BodyEncodeEntity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.DesUtil;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.RsaEncoder;
import com.jingdong.app.reader.util.UpdateUtil;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class GiftBagService extends IntentService {
	private String tag = "GiftBagService";
	private boolean isTest = !false;
	private static final String SYS_ERROR_MESSAGE = "系统异常，请稍后再试";

	public GiftBagService() {
		super("GiftBagService");
	}

	public GiftBagService(String name) {
		super(name);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent == null) {
			return;
		}
		if (UpdateUtil.updateInfo == null) {
			return;
		}

		int commond = intent.getIntExtra(GiftBagUtil.KEY,
				GiftBagUtil.GIFTBAG_INIT);
		switch (commond) {
		case GiftBagUtil.GIFTBAG_INIT:
			requestIsHaveGiftBag();
			break;
		case GiftBagUtil.GIFTBAG_REQ:
			requestGiftBag(String.valueOf(GiftBagUtil.id));
			break;
		case GiftBagUtil.GIFTBAG_UNREAD:
			requestUnReadGiftBag();
			break;
		case GiftBagUtil.GIFTBAG_UPDATE_STATUS:
			reqUpdateMessageStatus(intent.getStringExtra("params"));
			break;
		}

	}

	/**
	 * 请求服务器来判断是否显示礼包
	 */
	private void requestIsHaveGiftBag() {
		WebRequestHelper.post(URLText.JD_BASE_URL,
				RequestParamsPool.reqIsHaveGiftBag(),
				new AsyncHttpResponseHandler(getMainLooper()) {

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {

						String result = new String(arg2);

						MZLog.d(tag, result);
						boolean isSuccess = false;

						try {
							// if (bodyEncode == null) {
							// return;
							// }
							JSONObject obj = new JSONObject(result);
							String code = obj.getString("code");
							GiftBagUtil bagUtil = GiftBagUtil.getInstance();

							if (code.equals("0")) {
								JSONArray giftPacksList = null;
								if (obj.has("giftPacksList")) {
									giftPacksList = obj
											.getJSONArray("giftPacksList");
									for (int i = 0; i < giftPacksList.length(); i++) {
										JSONObject jsonObject = giftPacksList
												.getJSONObject(i);
										GiftBagUtil.id = jsonObject
												.getInt("id");
										GiftBean flag = bagUtil
												.getGiftBagStatus(GiftBagUtil.id);
										// flag=false;
										if (flag != null && flag.showCount > 2
												&& isTest) {
											continue;
										} else {
											Intent uiIntent = new Intent(
													getApplicationContext(),
													GiftBagAcitivity.class);
											GiftBagUtil.message = jsonObject
													.getString("userGetDesc");
											// GiftBagUtil.message="请到“我-畅读卡”中领取";
											uiIntent.putExtra(
													GiftBagUtil.advDesc_key,
													jsonObject
															.getString("advDesc"));
											uiIntent.putExtra("id",
													GiftBagUtil.id);
											// uiIntent.putExtra("userGetDesc",
											// jsonObject.getString("userGetDesc"));
											uiIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											getApplicationContext()
													.startActivity(uiIntent);
											break;
										}
									}
								}
							}

						} catch (Exception e) {

							e.printStackTrace();
						}

					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
					}
				});

	}

	/**
	 * 获取未读的信息
	 */
	private void requestUnReadGiftBag() {
		WebRequestHelper.post(URLText.JD_BASE_URL,
				RequestParamsPool.reqUnReadGiftBag(), true,
				new AsyncHttpResponseHandler(getMainLooper()) {

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {

						String result = new String(arg2);
						try {
							JSONObject obj = new JSONObject(result);
							if (!obj.has("code")) {
								return;
							}
							String code = obj.getString("code");
							MZLog.d(tag, "unread=" + result);
							if (code == null) {
								return;
							}
							if ("0".equals(code)) {
								JSONArray arra = obj
										.getJSONArray("unreadMessage");
								JSONObject tempObj = null;
								String msg = null;
								int length = arra.length();
								for (int i = 0; i < length; i++) {
									tempObj = arra.getJSONObject(i);
									if (tempObj.has("detailType")) {
										GiftBagUtil
												.getInstance()
												.saveGiftBagInfoReadStatus(
														tempObj.getString("detailType"));
									}
								}
								if (length > 0) {

									String action = NewMessageRecivier.ACTION_ADD;
									Intent intent = new Intent(action);
									intent.putExtra(
											NewMessageRecivier.MESSAGE_COUNT,
											length);
									intent.putExtra(
											NewMessageRecivier.PAGE_INDEX, 3);
									((MZBookApplication) getApplication())
											.sendLocalBroadcastMessage(intent);
								}
							}
						} catch (Exception e) {

						}

					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// Intent brodcast=new Intent(GifitBagRecivier.ACTION);
						// brodcast.putExtra(GiftBagUtil.MESSAGE_KEY,"<br>抱歉!<br>"+SYS_ERROR_MESSAGE+"<br>");
						// brodcast.putExtra(GifitBagRecivier.COMMOND_KEY,
						// GifitBagRecivier.LIVE_GIFTBAG);
						// getApplication().sendBroadcast(brodcast);
					}
				});
	}

	private void reqUpdateMessageStatus(String detailType) {
		WebRequestHelper.post(URLText.JD_BASE_URL,
				RequestParamsPool.updateGiftBagStatus(detailType),
				new AsyncHttpResponseHandler(getMainLooper()) {
					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
						String result = new String(arg2);
						MZLog.d(tag, result);
					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						// Intent brodcast=new Intent(GifitBagRecivier.ACTION);
						// brodcast.putExtra(GiftBagUtil.MESSAGE_KEY,"<br>抱歉!<br>"+SYS_ERROR_MESSAGE+"<br>");
						// brodcast.putExtra(GifitBagRecivier.COMMOND_KEY,
						// GifitBagRecivier.LIVE_GIFTBAG);
						// getApplication().sendBroadcast(brodcast);
					}
				});
	}

	/**
	 * 获取礼包结果
	 */
	private void requestGiftBag(final String giftPacksId) {
		WebRequestHelper.post(URLText.JD_BASE_URL,
				RequestParamsPool.reqGetGiftBag(giftPacksId),
				new AsyncHttpResponseHandler(getMainLooper()) {

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {

						String result = new String(arg2);
						MZLog.d(tag, "requestGiftBag=" + result);
						try {
							Intent brodcast = new Intent(
									GifitBagRecivier.ACTION);

							JSONObject obj = new JSONObject(result);
							if (!obj.has("code")) {
								return;
							}
							String code = obj.getString("code");

							if (code == null) {
								return;
							}
							brodcast.putExtra(GiftBagUtil.MESSAGE_CODE, code);
							String data = null;
							if (obj.has("encryptResult")) {

								data = obj.getString("encryptResult");
								BodyEncodeEntity encodeEntity = RsaEncoder
										.getEncodeEntity();

								String desEncryptResult = DesUtil.decrypt(data,
										encodeEntity.desSessionKey);
								MZLog.d(tag, desEncryptResult);
								obj = new JSONObject(desEncryptResult);
								// code="0";
								int key = getCommon_key(code);
								if (code.equals("0")) {
									brodcast.putExtra(
											GifitBagRecivier.COMMOND_KEY, key);
									brodcast.putExtra(
											GiftBagUtil.MESSAGE_KEY,
											GiftBagUtil.SUCCESS_MESSAGE
													+ obj.getString("message")
													+ "\n"
													+ GiftBagUtil.message);
									requestUnReadGiftBag();
								} else {
									if (obj.has("message")) {
										brodcast.putExtra(
												GiftBagUtil.MESSAGE_KEY,
												obj.getString("message"));
									}
									brodcast.putExtra(
											GifitBagRecivier.COMMOND_KEY, key);
								}
								if (code.equals("25") || code.equals("0")
										|| code.equals("26")
										|| code.equals("23")
										|| code.equals("22")) {
									GiftBagUtil util = GiftBagUtil
											.getInstance();
									util.updateGiftBagStatus(GiftBagUtil.id, 3);
								}
							} else {
								brodcast.putExtra(GiftBagUtil.MESSAGE_KEY,
										GiftBagUtil.message);
								brodcast.putExtra(GifitBagRecivier.COMMOND_KEY,
										GifitBagRecivier.HAVE_GIFTBAG);
							}

							getApplication().sendBroadcast(brodcast);
						} catch (Exception e) {

							Intent brodcast = new Intent(
									GifitBagRecivier.ACTION);
							brodcast.putExtra(GiftBagUtil.MESSAGE_KEY,
									"<br>抱歉!<br>" + SYS_ERROR_MESSAGE + "<br>");
							brodcast.putExtra(GifitBagRecivier.COMMOND_KEY,
									GifitBagRecivier.LIVE_GIFTBAG);
							getApplication().sendBroadcast(brodcast);
						}

					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						Intent brodcast = new Intent(GifitBagRecivier.ACTION);
						brodcast.putExtra(GiftBagUtil.MESSAGE_KEY,
								"<br>抱歉!<br>" + SYS_ERROR_MESSAGE + "<br>");
						brodcast.putExtra(GifitBagRecivier.COMMOND_KEY,
								GifitBagRecivier.LIVE_GIFTBAG);
						getApplication().sendBroadcast(brodcast);
					}
				});

	}

	private int getCommon_key(String code) {
		int key = GifitBagRecivier.GIFTBAG_ERR_MSG;
		if ("-1".equals(code) || "1".equals(code) || "3".equals(code)) {
			key = GifitBagRecivier.LIVE_GIFTBAG;
		} else if ("0".equals(code)) {
			key = GifitBagRecivier.HAVE_GIFTBAG;
		}

		return key;
	}
}
