package com.jingdong.app.reader.util;

import java.util.HashMap;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookcaseLocalFragmentListView;
import com.jingdong.app.reader.activity.MyActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.entity.BodyEncodeEntity;
import com.jingdong.app.reader.entity.extra.UserInfo;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.login.LoginVerificationActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.security.RequestSecurityKeyTask;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.view.dialog.DialogManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

public class LoginHelper {

	private static int autoLoginRetryCount = 0;
	private static boolean sessionKeyFailRetry = false;

	public interface LoginListener {
		void onLoginSuccess();
		void onLoginFail(String errCode);
	}

	/**
	 * 
	 * @Title: doLogin
	 * @Description: 通用登录接口
	 * @param @param ctx
	 * @param @param username
	 * @param @param pwd
	 * @param @param isAutoLogin：自动登录标识
	 * @param @param listener
	 * @return void
	 * @throws
	 */
	public static void doLogin(final Context ctx, final String username, final String pwd,final String verification, final boolean isAutoLogin, final LoginListener listener) {
		if (listener == null) {
			return;
		}

		RequestSecurityKeyTask task = new RequestSecurityKeyTask(new RequestSecurityKeyTask.OnGetSessionKeyListener() {

			@Override
			public void onGetSessionKeySucceed() {
				WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool.getLoginParams(username, pwd,verification), new MyAsyncHttpResponseHandler(ctx, true) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//						LoginUser.setUserState(LoginUser.USERSTATE_OUT);
//						WebRequestHelper.setCookies(null);
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						autoLoginRetryCount++;
						boolean isSuccess = false;
						String tip = null;
						String code = "";
						try {
							JSONObject jsonObj = new JSONObject(new String(responseBody));
							if (jsonObj != null) {
								JSONObject desJsonObj = null;
								code = jsonObj.optString("code");
								String encryptResult = jsonObj.optString("encryptResult");
								BodyEncodeEntity encodeEntity = RsaEncoder.getEncodeEntity();
								String desEncryptResult = DesUtil.decrypt(encryptResult, encodeEntity.desSessionKey);

								if (desJsonObj == null) {
									if (code.equals("0")) {
										desJsonObj = new JSONObject(desEncryptResult);
										LocalUserSetting.saveLoginScan(ctx, false);
										final String jsonPin = desJsonObj.get("pin").toString();
										// 保存userpin到本地
										getUserInfo(jsonPin);
										boolean isSchoolBaiTiaoUser = desJsonObj.optBoolean("isSchoolBaiTiaoUser");
										LocalUserSetting.saveIsSchoolBaiTiaoUser(MZBookApplication.getContext(), isSchoolBaiTiaoUser);
										LoginUser.setUserState(LoginUser.USERSTATE_IN);
										LoginUser.setLoginState(username, pwd, jsonPin, true, true, true);
										isSuccess = true;
										sessionKeyFailRetry = false;
										// 回调
										listener.onLoginSuccess();
										OpenBookHelper.updateSystemTime(MZBookApplication.getContext());
										if ( null != LoginActivity.mHandler ) {
											LoginActivity.mHandler.sendEmptyMessage(LoginActivity.EXIT);
										}
									} else {
										if (isAutoLogin) {
											if (("8").equals(code) && autoLoginRetryCount <= 2) {
												RsaEncoder.setEncodeEntity(null);
												// 自动登录三次
												doLogin(ctx, username, pwd,null, true, listener);
												return;
											} else if (("8").equals(code) && autoLoginRetryCount > 2) {
//												LoginUser.setUserState(LoginUser.USERSTATE_OUT);
//												WebRequestHelper.setCookies(null);
												tip = jsonObj.optString("message");
												isSuccess = false;
											}
										} else {
											//sessionKey失效，重试三次
											if (("8").equals(code) && autoLoginRetryCount <= 2) {
												RsaEncoder.setEncodeEntity(null);
												// 重新获取sessionKey,再进行登录
												doLogin(ctx, username, pwd,null, false, listener);
												return;
											}
											//本地sessionKey过期了
											if (("1").equals(code) && !sessionKeyFailRetry) {
												sessionKeyFailRetry = true;
												RsaEncoder.setEncodeEntity(null);
												// 重新获取sessionKey,再进行登录
												doLogin(ctx, username, pwd,null, false, listener);
												return;
											}
											
											if ("52".equals(code) || "1".equals(code)) {
												desJsonObj = new JSONObject(desEncryptResult);
												tip = desJsonObj.get("message").toString();
											} else {
												tip = jsonObj.optString("message");
											}
											
											//风险用户，需要输入图片或者手机短信验证码登录 code=32为图片验证码，31为短信验证码
											if ("32".equals(code) || "31".equals(code)) {
												RsaEncoder.setEncodeEntity(null);
												
												desJsonObj = new JSONObject(desEncryptResult);
												final Intent intent = new Intent(ctx,LoginVerificationActivity.class);
												if("32".equals(code))
													intent.putExtra("verificationType", "image");
												else{
													intent.putExtra("verificationType", "phone");
													intent.putExtra("phoneNumber", desJsonObj.optString("phone"));
												}
												intent.putExtra("userName", username);
												intent.putExtra("pwd", pwd);
												
												DialogManager.showCommonDialog(((MyActivity)ctx), "提示", 
														"您的账号存在风险，为了账号安全需要验证登录，是否继续？", 
														ctx.getString(R.string.ok), ctx.getString(R.string.cancel),
														new DialogInterface.OnClickListener() {	
														@Override
														public void onClick(DialogInterface dialog, int which) {
															dialog.dismiss();
															switch (which) {
															case DialogInterface.BUTTON_POSITIVE:
																((MyActivity)ctx).startActivityForResult(intent, LoginVerificationActivity.VERIFICATION_ACTIVITY);
																break;
															case DialogInterface.BUTTON_NEGATIVE:
																break;
															default:
																break;
															}
															
														}
												});
												
											}
										}
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
//							LoginUser.setUserState(LoginUser.USERSTATE_OUT);
//							WebRequestHelper.setCookies(null);
						}
						if (TextUtils.isEmpty(tip)) {
							tip = "登录失败,请重新尝试！";
						}
						if (!isSuccess) {
							if (!"32".equals(code) && !"31".equals(code) &&
									!"42".equals(code) && !"43".equals(code)) {
								Toast.makeText(ctx, tip, Toast.LENGTH_LONG).show();
							}
//							LoginUser.setUserState(LoginUser.USERSTATE_OUT);
//							WebRequestHelper.setCookies(null);
							listener.onLoginFail(code);
						}
						
						MZBookApplication app = (MZBookApplication)ctx.getApplicationContext();
						app.setLogin(isSuccess);
					}
				});
			}

			@Override
			public void onGetSessionKeyFailed() {
				Toast.makeText(ctx, ctx.getString(R.string.unknown_error), Toast.LENGTH_LONG).show();
//				LoginUser.setUserState(LoginUser.USERSTATE_OUT);
//				WebRequestHelper.setCookies(null);
				if (listener != null) {
					listener.onLoginFail("-1");
				}
			}
		});

		task.excute();
	}
	
	private static void getUserInfo(String pin) {
		WebRequestHelper.get(URLText.JD_BOOK_STORE_URL,RequestParamsPool.getUserInfoParams(pin),
				true, new MyAsyncHttpResponseHandler(MZBookApplication.getInstance()) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				
				String result = new String(responseBody);
				UserInfo userinfo = GsonUtils.fromJson(result, UserInfo.class);
				
				if (userinfo != null && userinfo.getList() != null
						&& userinfo.getList().size() > 0) {
					
					String nickName = userinfo.getList().get(0).getNickName();
					String imgUrl = userinfo.getList().get(0).getYunBigImageUrl();
					//保存头像Url、用户昵称
					if(MZBookApplication.getInstance() == null){
						LocalUserSetting.saveUserHeaderUrl(MZBookApplication.getContext(), imgUrl);
						LocalUserSetting.saveUserNickName(MZBookApplication.getContext(), nickName);
					}else{
						LocalUserSetting.saveUserHeaderUrl(MZBookApplication.getInstance(), imgUrl);
						LocalUserSetting.saveUserNickName(MZBookApplication.getInstance(), nickName);
					}
				} else{
				}
			}

		});

	}

	public interface CheckBindListener {
		void onSuccess();
		void onFailure();
	}

	public static void CheckBind(final Context ctx, final CheckBindListener listener) {
		try {
			WebRequestHelper.get(URLText.BIND_URL, RequestParamsPool.getBindParams(), true, new MyAsyncHttpResponseHandler(ctx) {

				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					Toast.makeText(ctx, ctx.getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
					String result = new String(responseBody);
					JSONObject jsonObj;
					try {
						jsonObj = new JSONObject(new String(responseBody));
						if (jsonObj != null) {
							JSONObject desJsonObj = null;
							String code = jsonObj.optString("code");
							String message = jsonObj.optString("message");
							
							boolean isNormal = false;
							if (Integer.parseInt(code) == 108) {
								Toast.makeText(ctx, "此设备已加入黑名单，无法使用", 1).show();
							} else if (Integer.parseInt(code) == 100) {
//								Toast.makeText(ctx, "绑定设备超限制，请至设置解绑", 1).show();
							} else if (Integer.parseInt(code) == 196) {
								isNormal = true;
							} else if (Integer.parseInt(code) == 198) {
								LoginUser.unBindUserAndClearEbookData();
							} else {
								Toast.makeText(ctx, "此设备绑定异常", 1).show();
							}
							
							if (isNormal) {
								if (listener != null) {
									listener.onSuccess();
								}
							}else{
								if (listener != null) {
									listener.onFailure();
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});

		} catch (Exception e) {
		}
	}

	
}
