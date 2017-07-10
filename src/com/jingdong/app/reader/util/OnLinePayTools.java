package com.jingdong.app.reader.util;

import android.app.Activity;
import android.content.Context;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.pay.PayOrder;
import com.jingdong.app.reader.util.CommonUtil.BrowserUrlListener;

public class OnLinePayTools {
	// cyr add
	public static void gotoEbookPay(Context context, String id) {
		//正常购书businessType为空
		CommonUtil.businessType = 0;
		if (id == null) {
			CommonUtil.queryBrowserUrl(context, new BrowserUrlListener() {
				@Override
				public void onGenToken(final String token) {
					PayOrder.toOrder(token, MZBookApplication.getInstance().getCurrentMyActivity());
				}

				@Override
				public void onComplete() {
					// TODO Auto-generated method stub
				}
			});
		} else {
			CommonUtil.queryBrowserUrl(context, new BrowserUrlListener() {
				@Override
				public void onGenToken(final String token) {
					PayOrder.toOrder(token, MZBookApplication.getInstance().getCurrentMyActivity());
				}

				@Override
				public void onComplete() {
					// TODO Auto-generated method stub
				}
			}, id);
		}
	}

	public static void gotoOrderPay(final Context context, final String id) {
		//正常购书businessType为空
		CommonUtil.businessType = 0;
		CommonUtil.queryBrowserUrl(context, new BrowserUrlListener() {
			@Override
			public void onGenToken(final String token) {
				PayOrder.toOrder((Activity)context, token, null);
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
			}
		}, id);
	}
	
	/**
	 * 购买送书支付打通接口
	 * @param context
	 * @param id
	 */
	public static void gotoSendBookPay(Context context, String id) {
		//购书送书businessType为2
		CommonUtil.businessType = 2;
		if (id == null) {
			WebRequestHelper.get(URLText.JD_BOOK_STORE_URL, RequestParamsPool.getSendBookTokenParams(), true,
					new MyAsyncHttpResponseHandler(context) {

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						}

						@Override
						public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
							String result = new String(responseBody);
							try {
								JSONObject jsonObj = new JSONObject(result);
								JSONObjectProxy obj = new JSONObjectProxy(jsonObj);
								String token = obj.getStringOrNull("tokenKey");
								String code = obj.getStringOrNull("code");
								if (null == token) {
									return;
								}
								String url = obj.getStringOrNull("url");
								if (null == url) {
									return;
								}
								PayOrder.toOrder(token, MZBookApplication.getInstance().getCurrentMyActivity());
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
		} else {
			WebRequestHelper.get(URLText.JD_BOOK_ORDER_URL, RequestParamsPool.getSendBookTokenParams(id), true,
					new MyAsyncHttpResponseHandler(context) {
						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						}

						@Override
						public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
							String result = new String(responseBody);
							try {
								JSONObject jsonObj = new JSONObject(result);
								JSONObjectProxy obj = new JSONObjectProxy(jsonObj);
								String token = obj.getStringOrNull("tokenKey");
								String code = obj.getStringOrNull("code");
								String url = obj.getStringOrNull("payUrl");
								if (null == url) {
									return;
								}
								PayOrder.toOrder(token, MZBookApplication.getInstance().getCurrentMyActivity());
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
		}
	}
	
	/**
	 * 购书赠书订单支付打通接口
	 * @param context
	 * @param id
	 */
	public static void gotoSendBookOrderPay(final Context context, final String id) {
		// 购书送书businessType为2
		CommonUtil.businessType = 2;

		WebRequestHelper.get(URLText.JD_BOOK_ORDER_URL, RequestParamsPool.getSendBookTokenParams(id), true,
				new MyAsyncHttpResponseHandler(context) {
					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						String result = new String(responseBody);
						try {
							JSONObject jsonObj = new JSONObject(result);
							JSONObjectProxy obj = new JSONObjectProxy(jsonObj);
							String token = obj.getStringOrNull("tokenKey");
							String code = obj.getStringOrNull("code");
							String url = obj.getStringOrNull("payUrl");
							if (null == url) {
								return;
							}
							PayOrder.toOrder((Activity)context, url, null);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

	}

}
