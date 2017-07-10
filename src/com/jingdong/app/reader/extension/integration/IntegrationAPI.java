package com.jingdong.app.reader.extension.integration;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.jingdong.app.reader.extension.giftbag.GiftBagUtil;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;

/**
 * 
 * @ClassName: IntegrationAPI
 * @Description: 积分模块与服务器进行数据交互API
 * @author J.Beyond
 * @date 2015年5月7日 下午8:42:36
 *
 */
public class IntegrationAPI {

	public interface GrandScoreListener {
		void onGrandSuccess(SignScore signScore);

		void onGrandFail();
	}

	/**
	 * 
	 * @Title: firstLoginGrand
	 * @Description: 首次登陆赠送积分
	 * @param @param ctx
	 * @param @param grandListener
	 * @return void
	 * @throws
	 * @date 2015年5月7日 下午6:46:54
	 */
	public static void checkAndSaveLoginScore(final Context ctx,
			final GrandScoreListener grandListener) {
		if (!NetWorkUtils.isNetworkAvailable(ctx)) {
//			Toast.makeText(ctx, ctx.getString(R.string.network_connect_error),
//					Toast.LENGTH_SHORT).show();
			return;
		}
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getCheckAndSaveLoginScoreParams(), true,
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
//						Toast.makeText(ctx,
//								ctx.getString(R.string.network_connect_error),
//								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						processScoreResult(ctx,new String(responseBody),
								grandListener);
						// 绑定设备
						checkDevice(ctx);
					}
				});
	}

	/**
	 * 
	 * @Title: checkDevice
	 * @Description: 绑定设备
	 * @param @param ctx
	 * @return void
	 * @throws
	 * @date 2015年5月7日 下午6:47:17
	 */
	private static void checkDevice(final Context ctx) {
		WebRequestHelper.get(URLText.JD_BOOK_DOWNLOAD_URL,
				RequestParamsPool.getCheckDeviceParams(), true,
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
//						Toast.makeText(ctx,
//								ctx.getString(R.string.network_connect_error),
//								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						String result = new String(responseBody);
						MZLog.d("J", "onResponse=======>>" + result);
						try {
							JSONObject obj = new JSONObject(result);
							String code = obj.optString("code");
							if ("0".equals(code)) {
								MZLog.i("J",
										"IntegrationAPI#checkDevice::success!");
							} else {
								MZLog.e("J",
										"IntegrationAPI#checkDevice::error!");
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				});
	}

	public interface CheckSignScoreListener {
		void onCheck(boolean isSigned);
	}

	/**
	 * 
	 * @Title: checkSignScore
	 * @Description: 验证用户是否签到过
	 * @param @param ctx
	 * @param @param listener
	 * @return void
	 * @throws
	 * @date 2015年5月7日 下午8:29:35
	 */
	public static void checkSignScore(final Context ctx,
			final CheckSignScoreListener listener) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getCheckSignScoreParams(), true,
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
//						Toast.makeText(ctx,
//								ctx.getString(R.string.network_connect_error),
//								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						String result = new String(responseBody);
						try {
							JSONObject obj = new JSONObject(result);
							boolean isSigned = true;
							String code = obj.optString("code");
							if ("0".equals(code)) {
								isSigned = false;
							}
							if (listener != null) {
								listener.onCheck(isSigned);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				});
	}

	/**
	 * 
	 * @Title: signGetScore
	 * @Description: 未签到，签到
	 * @param @param ctx
	 * @param @param listener
	 * @return void
	 * @throws
	 * @date 2015年5月7日 下午7:16:38
	 */
	public static void signGetScore(final Context ctx,final boolean isSignInScorePage,
			final GrandScoreListener listener) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getSignGetScoreParams(), true,
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
//						Toast.makeText(ctx,
//								ctx.getString(R.string.network_connect_error),
//								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						String result = new String(responseBody);
						SignScore signScore = GsonUtils.fromJson(result, SignScore.class);
						if (signScore != null && "0".equals(signScore.getCode())) {
							if (listener != null) {
								listener.onGrandSuccess(signScore);
							}
							if (!isSignInScorePage) {
								//处理个人中心红点
								GiftBagUtil.getInstance().getUnReadMessage(ctx);
							}
						}else{
							if (listener != null) {
								listener.onGrandFail();//code=55
							}
						}
					}
				});
	}

	/**
	 * 
	 * @Title: processScoreResult
	 * @Description: 统一处理赠送积分
	 * @param @param result
	 * @param @param listener
	 * @return void
	 * @throws
	 * @date 2015年5月7日 下午8:31:04
	 */
	protected static void processScoreResult(Context ctx,String result,
			GrandScoreListener listener) {
		MZLog.d("J", "IntegrationAPI#processScoreResult::" + result);
		SignScore signScore = GsonUtils.fromJson(result, SignScore.class);
		if (signScore != null && "0".equals(signScore.getCode())) {
			if (listener != null) {
				listener.onGrandSuccess(signScore);
			}
			//处理个人中心红点
			GiftBagUtil.getInstance().getUnReadMessage(ctx);
		} else {
			if (listener != null) {
				listener.onGrandFail();
			}
		}
	}

	/**
	 * 
	 * @Title: commentGetScore
	 * @Description: 书评获得积分
	 * @param @param ctx
	 * @param @param bookId
	 * @param @param listener
	 * @return void
	 * @throws
	 * @date 2015年5月7日 下午7:59:38
	 */
	public static void commentGetScore(final Context ctx, long bookId,
			final GrandScoreListener listener) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getCommentGetScoreParams(bookId), true,
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
//						Toast.makeText(ctx,
//								ctx.getString(R.string.network_connect_error),
//								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						processScoreResult(ctx,new String(responseBody), listener);
					}
				});
	}

	/**
	 * 
	 * @Title: notesGetScore
	 * @Description: 笔记获得积分（需要登录）
	 * @param @param ctx
	 * @param @param bookId
	 * @param @param listener
	 * @return void
	 * @throws
	 * @date 2015年5月7日 下午8:03:20
	 */
	public static void notesGetScore(final Context ctx, long bookId,
			final GrandScoreListener listener) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getNotesGetScoreParams(bookId), true,
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						if (listener != null) {
							listener.onGrandFail();
						}
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						processScoreResult(ctx,new String(responseBody), listener);
					}
				});
	}

	public interface ReadGetTimeListener {
		void onGetTimeSuccess(int readTime);

		void onGetTimeFail();
	}

	/**
	 * 
	 * @Title: readGetTime
	 * @Description: 阅读获得积分需要的时长
	 * @param @param ctx
	 * @param @param listener
	 * @return void
	 * @throws
	 * @date 2015年5月7日 下午8:11:48
	 */
	public static void readGetTime(final Context ctx,
			final ReadGetTimeListener listener) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getReadGetTimeParams(), true,
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
//						Toast.makeText(ctx,
//								ctx.getString(R.string.network_connect_error),
//								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						String result = new String(responseBody);
						MZLog.d("J", "onResponse=======>>" + result);
						try {
							JSONObject obj = new JSONObject(result);
							String code = obj.optString("code");

							if ("0".equals(code)) {
								MZLog.i("J",
										"IntegrationAPI#readGetTime::success!");
								int read_time = obj.optInt("read_time");
								if (listener != null) {
									listener.onGetTimeSuccess(read_time);
								}
							} else {
								MZLog.e("J",
										"IntegrationAPI#readGetTime::error!");
								if (listener != null) {
									listener.onGetTimeFail();
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

					}
				});
	}

	/**
	 * 
	 * @Title: readTimeGetScore
	 * @Description: 阅读达到时长获得积分
	 * @param @param ctx
	 * @param @param bookId
	 * @param @param readTime
	 * @param @param listener
	 * @return void
	 * @throws
	 * @date 2015年5月7日 下午8:22:27
	 */
	public static void readTimeGetScore(final Context ctx, long bookId,
			int readTime, final GrandScoreListener listener) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getReadTimeGetScoreParams(bookId, readTime),
				true, new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
//						Toast.makeText(ctx,
//								ctx.getString(R.string.network_connect_error),
//								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						processScoreResult(ctx,new String(responseBody), listener);
					}
				});
	}

	/**
	 * 
	 * @Title: shareGetScore
	 * @Description: 分享获得积分（需要登录）
	 * @param @param ctx
	 * @param @param listener
	 * @return void
	 * @throws
	 * @date 2015年5月7日 下午8:37:38
	 */
	public static void shareGetScore(final Context ctx,
			final GrandScoreListener listener) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getShareGetScoreParams(), true,
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
//						Toast.makeText(ctx,
//								ctx.getString(R.string.network_connect_error),
//								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						processScoreResult(ctx,new String(responseBody), listener);
					}
				});
	}

	public interface GetGiftByScoreListener {
		void onSuccess();
		void onFail(String errorInfo);
	}

	/**
	 * 积分兑换接口（需要登录）
	 * @param ctx
	 * @param giftId
	 * @param listener
	 */
	public static void getGiftByScore(final Context ctx, int giftId,
			final GetGiftByScoreListener listener) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,
				RequestParamsPool.getGiftByScoreParams(giftId), true,
				new MyAsyncHttpResponseHandler(ctx) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
//						Toast.makeText(ctx,
//								ctx.getString(R.string.network_connect_error),
//								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers,
							byte[] responseBody) {
						String result = new String(responseBody);
						MZLog.d("J", "onResponse=======>>" + result);
						try {
							JSONObject obj = new JSONObject(result);
							String code = obj.optString("code");

							if ("0".equals(code)) {
								if (listener != null) {
									listener.onSuccess();
								}
							} else {
								String errorInfo = "";
								switch (code) {
								case "56":
									errorInfo = "积分兑换礼物不存在";
									break;
								case "57":
									errorInfo = "积分不足不能兑换礼物";
									break;
								case "58":
								case "55":
									errorInfo = "礼物已经兑换过";
									break;
								default:
									break;
								}
								if (listener != null) {
									listener.onFail(errorInfo);
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}

}
