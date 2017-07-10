package com.jingdong.app.reader.login;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.activity.ProtocolActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.BodyEncodeEntity;
import com.jingdong.app.reader.extension.giftbag.GiftBagUtil;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.security.RequestSecurityKeyTask;
import com.jingdong.app.reader.util.DesUtil;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.LoginHelper;
import com.jingdong.app.reader.util.LoginHelper.LoginListener;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.RsaEncoder;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class VerifyPhoneActivity extends BaseActivityWithTopBar {

	private EditText mPhoneNumEt;
	private EditText mVerifyCodeEt;
	private EditText mPwdEt;
	private TextView mSendVerifyCodeTv;
	private TextView mRegistTv;
	private ImageView mShowPwdIv;
	private boolean image_flag = true;
	static int second = -1;
	Timer timer;
	TimerTask timerTask;
	private Boolean clickable = true;
	private TextView mJDProtocolTV;
	private final int MAX_LENGTH = 11;
	private int Rest_Length = MAX_LENGTH;
	// private ProgressBar progress;
	private boolean isMaxCount = false;
	private boolean isTimeCounting = false;// 标记是否在倒计时

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_verify_code);
		initView();
	}

	private void backToMain() {
		this.setResult(GlobalVarable.REQUEST_CODE_GOTO_FIRST_PAGE);
		this.finish();
	}

	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				clickable = false;
				isTimeCounting = true;
				second = 120;
				timerTask = new TimerTask() {
					@Override
					public void run() {
						mHandler.sendMessage(mHandler.obtainMessage(1));
					}
				};
				timer = new Timer();
				timer.schedule(timerTask, 0, 1000);
				break;
			case 1:
				if (second == 0) {
					mSendVerifyCodeTv.setEnabled(true);
					isTimeCounting = false;
					mSendVerifyCodeTv.setText("获取验证码");

					clickable = true;
					if (timer != null) {
						timer.cancel();
						timer = null;
					}
					if (timerTask != null) {
						timerTask = null;
					}
				} else {
					second--;
					mSendVerifyCodeTv.setText(String.valueOf(second) + "秒后获取");
					mSendVerifyCodeTv.setEnabled(false);
				}
				break;

			default:
				break;
			}
		}

	};

	public void initView() {
		mPhoneNumEt = (EditText) findViewById(R.id.phone_num);
		mVerifyCodeEt = (EditText) findViewById(R.id.code);
		mSendVerifyCodeTv = (TextView) findViewById(R.id.verification_code);
		mRegistTv = (TextView) findViewById(R.id.sign);
		mPwdEt = (EditText) findViewById(R.id.password);
		mJDProtocolTV = (TextView) findViewById(R.id.protocol);
		mShowPwdIv = (ImageView) findViewById(R.id.image);
		mShowPwdIv.setImageResource(R.drawable.btn_password_invisible);
		// progress = (ProgressBar) findViewById(R.id.progress);

		mJDProtocolTV.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(VerifyPhoneActivity.this, ProtocolActivity.class);
				startActivity(intent);
			}
		});

		mShowPwdIv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (image_flag == true) {
					mShowPwdIv.setImageResource(R.drawable.btn_password_visible);
					mPwdEt.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				} else {
					mShowPwdIv.setImageResource(R.drawable.btn_password_invisible);
					mPwdEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				image_flag = !image_flag;
				mPwdEt.postInvalidate();
				// 切换后将EditText光标置于末尾
				CharSequence charSequence = mPwdEt.getText();
				if (charSequence instanceof Spannable) {
					Spannable spanText = (Spannable) charSequence;
					Selection.setSelection(spanText, charSequence.length());
				}
			}
		});

		mSendVerifyCodeTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (clickable) {
					send();
				}

			}
		});

		mPhoneNumEt.addTextChangedListener(new TextWatcher() {
			private CharSequence temp;
			private int editStart;
			private int editEnd;

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				temp = s;
			}

			@Override
			public void afterTextChanged(Editable s) {
				editStart = mPhoneNumEt.getSelectionStart();
				editEnd = mPhoneNumEt.getSelectionEnd();
				if (temp.length() == MAX_LENGTH) {
					// 发送验证码时不能再发
					if (!isTimeCounting) {
						checkEverRegist();
					}
					if (isConditionSatisfy()) {
						mRegistTv.setTextColor(getResources().getColor(R.color.highlight_color));
						mRegistTv.setEnabled(true);
					}
				} else if (temp.length() < MAX_LENGTH) {
					mSendVerifyCodeTv.setTextColor(isTimeCounting ? getResources().getColor(R.color.highlight_color) : getResources().getColor(
							R.color.text_color));
					mSendVerifyCodeTv.setEnabled(false);
					if (isConditionSatisfy()) {
						mRegistTv.setTextColor(getResources().getColor(R.color.text_color));
						mRegistTv.setEnabled(false);
					}

				} else {
					s.delete(editStart - 1, editEnd);
					int tempSelection = editStart;
					mPhoneNumEt.setText(s);
					mPhoneNumEt.setSelection(tempSelection);
				}
			}
		});

		mPwdEt.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				if (TextUtils.isEmpty(mPwdEt.getText().toString()) || TextUtils.isEmpty(mVerifyCodeEt.getText().toString())
						|| TextUtils.isEmpty(mPhoneNumEt.getText().toString())) {
					mRegistTv.setTextColor(getResources().getColor(R.color.text_color));
					mRegistTv.setEnabled(false);
				} else {
					mRegistTv.setTextColor(getResources().getColor(R.color.highlight_color));
					mRegistTv.setEnabled(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}
		});

		mVerifyCodeEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				if (TextUtils.isEmpty(mPwdEt.getText().toString()) || TextUtils.isEmpty(mVerifyCodeEt.getText().toString())
						|| TextUtils.isEmpty(mPhoneNumEt.getText().toString())) {
					mRegistTv.setTextColor(getResources().getColor(R.color.text_color));
					mRegistTv.setEnabled(false);
				} else {
					mRegistTv.setTextColor(getResources().getColor(R.color.highlight_color));
					mRegistTv.setEnabled(true);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {

			}

			@Override
			public void afterTextChanged(Editable arg0) {

			}
		});
		mRegistTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (checkPhone(1) && checkCode() && checkPwd()) {

					RequestSecurityKeyTask task = new RequestSecurityKeyTask(new RequestSecurityKeyTask.OnGetSessionKeyListener() {

						@Override
						public void onGetSessionKeySucceed() {
							WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool.getRigisterParams(mPhoneNumEt.getText().toString(), mVerifyCodeEt
									.getText().toString(), mPwdEt.getText().toString()), new AsyncHttpResponseHandler() {

								@Override
								public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
									Toast.makeText(VerifyPhoneActivity.this, "请求出错!", Toast.LENGTH_SHORT).show();

								}

								@Override
								public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
									String result = new String(arg2);
									MZLog.d("wangguodong", "获取注册结果" + result);
									boolean iSuccess = false;
									String tip = null;
									try {
										JSONObject jsonObj = new JSONObject(result);
										if (jsonObj != null) {
											JSONObject desJsonObj = null;
											String code = jsonObj.optString("code");
											String encryptResult = jsonObj.optString("encryptResult");

											BodyEncodeEntity encodeEntity = RsaEncoder.getEncodeEntity();

											String desEncryptResult = DesUtil.decrypt(encryptResult, encodeEntity.desSessionKey);

											Log.d("cj", "解密结果" + desEncryptResult);
											if (desJsonObj == null) {
												desJsonObj = new JSONObject(desEncryptResult);
												if (code.equals("0")) {
													final String jsonPin = desJsonObj.get("pin").toString();
													Toast.makeText(VerifyPhoneActivity.this, jsonPin, Toast.LENGTH_LONG).show();
													iSuccess = true;
													Log.d("cj", "pin11=========>>" + jsonPin);
													toLogin(jsonPin);
												} else {
													tip = desJsonObj.optString("message");
												}
											}
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									if (TextUtils.isEmpty(tip)) {
										tip = "注册失败,请重新尝试！";
									}
									if (!iSuccess) {
										showDialog(tip);
									}
								}
							});

						}

						@Override
						public void onGetSessionKeyFailed() {

						}

					});

					task.excute();
				}
			}
		});

	}

	private boolean isConditionSatisfy() {
		return mVerifyCodeEt.getText().length() > 0 && mPwdEt.getText().length() > 0;
	}

	/**
	 * 
	 * @Title: checkEverRegist
	 * @Description: 检查输入的手机号是否注册过
	 * @param
	 * @return void
	 * @throws
	 * @date 2015年3月20日 下午12:24:42
	 */
	private void checkEverRegist() {
		if (!NetWorkUtils.isNetworkConnected(this)) {
			Toast.makeText(this, "网络不可用", Toast.LENGTH_LONG).show();
			return;
		}
		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getEverRegistParam(mPhoneNumEt.getText().toString()), new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				Toast.makeText(VerifyPhoneActivity.this, "请求出错!", Toast.LENGTH_SHORT).show();

			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				String result = new String(arg2);
				MZLog.d("J.Beyond", "checkEverRegist:" + result);
				try {
					JSONObject object = new JSONObject(result);
					String code = object.optString("code");

					if (code.equals("0")) {
						int isExist = object.optInt("isExist");
						if (isExist == 0) {
							// 0不存在
							mSendVerifyCodeTv.setTextColor(getResources().getColor(R.color.highlight_color));
							mSendVerifyCodeTv.setEnabled(true);
							clickable = true;
							mVerifyCodeEt.setEnabled(true);
							mPwdEt.setEnabled(true);
							mRegistTv.setClickable(true);

						} else {
							// 1存在
							mSendVerifyCodeTv.setTextColor(getResources().getColor(R.color.text_color));
							mSendVerifyCodeTv.setEnabled(false);
							clickable = false;
							isMaxCount = false;
							mVerifyCodeEt.setEnabled(false);
							mPwdEt.setEnabled(false);
							mRegistTv.setClickable(false);
							DialogManager.showToastDialog(VerifyPhoneActivity.this, "提示", "该手机号已被注册");
							// Toast.makeText(VerifyPhoneActivity.this,
							// "您的手机号已经注册过！", Toast.LENGTH_LONG).show();
						}
					} else if (code.equals("1")) {
						Log.e("JD_Reader", "参数错误");
						isMaxCount = false;
						Toast.makeText(VerifyPhoneActivity.this, "参数错误", Toast.LENGTH_LONG).show();
					} else {
						Log.e("JD_Reader", "系统异常");
						isMaxCount = false;
						Toast.makeText(VerifyPhoneActivity.this, "系统异常", Toast.LENGTH_LONG).show();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		});
	}

	private void send() {
		if (checkPhone(0)) {

			// 发送验证码
			WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool.getRigisterCode(mPhoneNumEt.getText().toString()), new AsyncHttpResponseHandler() {

				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					Toast.makeText(VerifyPhoneActivity.this, "请求出错!", Toast.LENGTH_SHORT).show();

				}

				@Override
				public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {

					MZLog.d("wangguodong", "获取验证码成功" + new String(arg2));

					String result = new String(arg2);
					try {
						JSONObject object = new JSONObject(result);
						String code = object.optString("code");
						String message = object.optString("message");

						if (code.equals("0"))
							DialogManager.showToastDialog(VerifyPhoneActivity.this, "提示", "验证码已发送，请耐心等待!");

						else {
							second = 0;
							mHandler.sendMessage(mHandler.obtainMessage(1));
							Toast.makeText(VerifyPhoneActivity.this, message, Toast.LENGTH_SHORT).show();
						}

					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			});

		}

		else {
			// Toast.makeText(VerifyPhoneActivity.this, "请输入手机号码!",
			// Toast.LENGTH_SHORT).show();
		}
	}
	
	public void toLogin(final String pin) {
		LoginHelper.doLogin(this, mPhoneNumEt.getText().toString(), EncryptPassword(mPwdEt.getText().toString()),null, false, new LoginListener() {
			
			@Override
			public void onLoginSuccess() {
				// 新用户登录领取积分
				checkNewUserToGrandScore();
				// 登入成功之后检查是否有新的消息
				GiftBagUtil.getInstance().getUnReadMessage(getBaseContext());
			}
			
			@Override
			public void onLoginFail(String errCode) {
				
			}
		});
	}

//	public void toLogin(final String pin) {
//
//		// progress.setVisibility(View.VISIBLE);
//		WebRequestHelper.post(URLText.JD_BASE_URL,
//				RequestParamsPool.getLoginParams(mPhoneNumEt.getText().toString(), EncryptPassword(mPwdEt.getText().toString())),
//				new AsyncHttpResponseHandler() {
//
//					@Override
//					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
//						try {
//							JSONObject jsonObj = new JSONObject(new String(arg2));
//							String code = jsonObj.optString("code");
//							JSONObject desJsonObj = null;
//							String encryptResult = jsonObj.optString("encryptResult");
//							BodyEncodeEntity encodeEntity = RsaEncoder.getEncodeEntity();
//							String desEncryptResult = DesUtil.decrypt(encryptResult, encodeEntity.desSessionKey);
//							desJsonObj = new JSONObject(desEncryptResult);
//
//							if ("52".equals(code) || "1".equals(code)) {
//								String tip = desJsonObj.optString("message");
//								// progress.setVisibility(View.GONE);
//								Toast.makeText(VerifyPhoneActivity.this, tip, Toast.LENGTH_SHORT).show();
//							} else if (code.equals("0")) {
//								// progress.setVisibility(View.GONE);
//								LocalUserSetting.saveLoginScan(VerifyPhoneActivity.this, false);
//								Toast.makeText(VerifyPhoneActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
//								final String jsonPin = desJsonObj.get("oldpin").toString();
//								Log.d("cj", "jsonPin222=========>>" + jsonPin);
//								LoginUser.setLoginState(mPhoneNumEt.getText().toString(), mPwdEt.getText().toString(), pin, true, true, true);
//								LoginUser.setUserState(LoginUser.USERSTATE_IN);
//								// 新用户登录领取积分
//								checkNewUserToGrandScore();
//
//								CheckBind();
//								// 登入成功之后检查是否有新的消息
//								GiftBagUtil.getInstance().getUnReadMessage(getBaseContext());
//							}
//						} catch (Exception e) {
//							// progress.setVisibility(View.GONE);
//							Toast.makeText(VerifyPhoneActivity.this, "出错了，请重试!", Toast.LENGTH_SHORT).show();
//						}
//
//					}
//
//					private void checkNewUserToGrandScore() {
//						IntegrationAPI.checkAndSaveLoginScore(VerifyPhoneActivity.this, new GrandScoreListener() {
//
//							@Override
//							public void onGrandSuccess(int score) {
//								CustomToast.showToast(VerifyPhoneActivity.this, "恭喜您获得" + score + "积分");
//							}
//
//							@Override
//							public void onGrandFail(String code) {
//								MZLog.d("J", "新用户登录获取积分失败，code=" + code);
//							}
//						});
//					}
//
//					@Override
//					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//						// progress.setVisibility(View.GONE);
//						Toast.makeText(VerifyPhoneActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
//
//					}
//				});
//
//	}
	
//	private void checkNewUserToGrandScore() {
//		IntegrationAPI.checkAndSaveLoginScore(VerifyPhoneActivity.this, new GrandScoreListener() {
//
//			@Override
//			public void onGrandSuccess(int score) {
//				CustomToast.showToast(VerifyPhoneActivity.this, "恭喜您获得" + score + "积分");
//			}
//
//			@Override
//			public void onGrandFail(String code) {
//				MZLog.d("J", "新用户登录获取积分失败，code=" + code);
//			}
//		});
//	}
	
	private void checkNewUserToGrandScore() {
		IntegrationAPI.checkAndSaveLoginScore(VerifyPhoneActivity.this, new GrandScoreListener() {

			@Override
			public void onGrandSuccess(SignScore score) {
//				CustomToast.showToast(LoginActivity.this, "恭喜您获得" + score + "积分");
				String scoreInfo = "新用户注册获得"+score.getGetScore()+"积分";
				SpannableString span = new SpannableString(scoreInfo);
				int start1 = 7;
				int end1 = start1 + String.valueOf(score.getGetScore()).length();
				span.setSpan(new ForegroundColorSpan(Color.RED), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				CustomToast.showToast(VerifyPhoneActivity.this, span);
			}

			@Override
			public void onGrandFail() {
				MZLog.d("J", "新用户登录获取积分失败");
			}
		});
	}

//	private void CheckBind() {
//
//		WebRequestHelper.get(URLText.BIND_URL, RequestParamsPool.getBindParams(), true, new MyAsyncHttpResponseHandler(VerifyPhoneActivity.this) {
//
//			@Override
//			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//				Toast.makeText(VerifyPhoneActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT).show();
//			}
//
//			@Override
//			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
//
//				String result = new String(responseBody);
//
//				MZLog.d("cj", "divice=======>>" + result);
//				JSONObject jsonObj;
//				try {
//					jsonObj = new JSONObject(new String(responseBody));
//					if (jsonObj != null) {
//						JSONObject desJsonObj = null;
//						String code = jsonObj.optString("code");
//						String message = jsonObj.optString("message");
//						if (Integer.parseInt(code) == 108) {
//							Toast.makeText(getApplicationContext(), "此设备已加入黑名单", 1).show();
//						} else if (Integer.parseInt(code) == 100) {
//							Toast.makeText(getApplicationContext(), "绑定设备超限制，请至设置解绑", 1).show();
//						} else if (Integer.parseInt(code) == 196) {
//
//						} else if (Integer.parseInt(code) == 198) {
//							LoginUser.unBindUserAndClearEbookData();
//						} else {
//							Toast.makeText(getApplicationContext(), "此设备绑定异常", 1).show();
//						}
//						backToMain();
//					}
//				} catch (JSONException e) {
//					e.printStackTrace();
//				}
//			}
//		});
//
//	}

	private String EncryptPassword(String str) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(str.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			System.err.println("NoSuchAlgorithmException caught!");
			System.exit(-1);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		byte[] byteArray = messageDigest.digest();

		StringBuffer md5StrBuff = new StringBuffer();

		for (int i = 0; i < byteArray.length; i++) {
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)
				md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
			else
				md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));
		}

		return md5StrBuff.toString().toUpperCase();
	}

	public boolean checkPhone(int flag) {

		if (TextUtils.isEmpty(mPhoneNumEt.getText().toString())) {
			return false;
		} else {
			String phoneNum = mPhoneNumEt.getText().toString();
			String first = phoneNum.substring(0, 1);
			if (first.equals("0")) {
				mPhoneNumEt.setFilters(new InputFilter[] { new InputFilter.LengthFilter(12) });
				if (isMobileNOWith0(phoneNum)) {
					if (flag == 0) {
						mHandler.sendMessage(mHandler.obtainMessage(0));
					}
				} else {
					Toast.makeText(getApplicationContext(), "您输入的号码格式有误!", Toast.LENGTH_LONG).show();
					return false;
				}
			} else if (first.equals("1")) {
				mPhoneNumEt.setFilters(new InputFilter[] { new InputFilter.LengthFilter(11) });
				if (isMobileNO(phoneNum)) {
					if (flag == 0) {
						mHandler.sendMessage(mHandler.obtainMessage(0));
					}
				} else {
					Toast.makeText(getApplicationContext(), "您输入的号码格式有误!", Toast.LENGTH_LONG).show();
					return false;
				}
			} else {
				Toast.makeText(getApplicationContext(), "您输入的号码格式有误!", Toast.LENGTH_LONG).show();
				return false;
			}
			return true;
		}
	}

	// 验证带0号码开头
	public static boolean isMobileNOWith0(String mobiles) {
		String telRegex = "[0][1][3578]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
		Pattern p = Pattern.compile(telRegex);
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	// 验证号码开头
	public static boolean isMobileNO(String mobiles) {
		String telRegex = "[1][3578]\\d{9}";// "[1]"代表第1位为数字1，"[358]"代表第二位可以为3、5、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
		Pattern p = Pattern.compile(telRegex);
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	public boolean checkCode() {

		if (TextUtils.isEmpty(mVerifyCodeEt.getText().toString())) {
			mRegistTv.setEnabled(false);
			Toast.makeText(getApplicationContext(), "请输入验证码!", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	public boolean checkPwd() {

		String password = mPwdEt.getText().toString();
		if (TextUtils.isEmpty(password)) {
			mRegistTv.setEnabled(false);
			Toast.makeText(getApplicationContext(), "请输入密码!", Toast.LENGTH_LONG).show();
			return false;
		} else if (password.length() < 6) {
			mRegistTv.setEnabled(false);
			Toast.makeText(getApplicationContext(), "您输入的密码小于6位!", Toast.LENGTH_LONG).show();
			return false;
		} else if (isNumeric(password)) {
			mRegistTv.setEnabled(false);
			Toast.makeText(getApplicationContext(), "您输入的密码为纯数字!", Toast.LENGTH_LONG).show();
			return false;
		} else if (isLetter(password)) {
			mRegistTv.setEnabled(false);
			Toast.makeText(getApplicationContext(), "您输入的密码为纯字母!", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	// 判断输入密码是否为纯数字
	public static boolean isNumeric(String str) {

		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	// 判断输入密码是否为纯字母
	public static boolean isLetter(String str) {
		Pattern pattern = Pattern.compile("[a-zA-Z]+");
		Matcher m = pattern.matcher(str);
		return m.matches();
	}

	private void showDialog(String str) {
		if (!isFinishing()) {
			DialogManager.showToastDialog(this, "提示", str);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_regist));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_regist));
	}

}
