package com.jingdong.app.reader.me.activity;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.security.RequestSecurityKeyTask;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.dialog.ProgressHUD;

public class BindReadingCardActivity extends BaseActivityWithTopBar {

	private EditText mAccount;
	private RelativeLayout mBindView;
	private Button mKamiBindBtn;
	private EditText mPwdPart1;
	private EditText mPwdPart2;
	private EditText mPwdPart3;
	private EditText mPwdPart4;

	private String mText1;
	private String mText2;
	private String mText3;
	private String mText4;
	private EditText mExchangeEt;
	private Button mExchangeBindBtn;
	private ProgressBar mProgressBar;
	private ProgressHUD mProgressHUD;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.bindreadingcard);
		initView();
		operatingEditText();
	}

	private void initView() {
		// 兑换码绑定
		mExchangeEt = (EditText) findViewById(R.id.exchange_code_et);
		mExchangeBindBtn = (Button) findViewById(R.id.exchange_bind_btn);
		mExchangeBindBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				exchangeBind();
			}
		});
		// 账号密码绑定方式
		mAccount = (EditText) findViewById(R.id.readingcard_num);
		mPwdPart1 = (EditText) findViewById(R.id.read_card_pwd_part1);
		mPwdPart2 = (EditText) findViewById(R.id.read_card_pwd_part2);
		mPwdPart3 = (EditText) findViewById(R.id.read_card_pwd_part3);
		mPwdPart4 = (EditText) findViewById(R.id.read_card_pwd_part4);

		mKamiBindBtn = (Button) findViewById(R.id.kami_bind_btn);
		mKamiBindBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String readingcard = mAccount.getText().toString();
				StringBuilder sb = new StringBuilder();
				sb.append(mText1).append("-").append(mText2).append("-").append(mText3).append("-").append(mText4);
				String readingcardPwd = sb.toString();

				if (!TextUtils.isEmpty(readingcard)) {

					if (!TextUtils.isEmpty(readingcardPwd)) {
						bindSubmit(readingcard, readingcardPwd);
					} else {
						Toast.makeText(getApplicationContext(), "请填写畅读卡密码!", 1).show();
					}
				} else {
					Toast.makeText(getApplicationContext(), "请填写畅读卡号!", 1).show();
				}
			}
		});
		mKamiBindBtn.setClickable(false);
	}

	/**
	 * 
	 * @Title: OperatingEditText
	 * @Description: 获得EditText中的内容,当每个Edittext的字符达到四位时,自动跳转到下一个EditText
	 * @param @param context
	 * @return void
	 * @throws
	 * @date 2015年4月2日 下午11:50:56
	 */
	private void operatingEditText() {
		mExchangeEt.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if (s != null) {
					if (s.length() >= 8) {
						mExchangeBindBtn.setTextColor(getResources().getColor(R.color.white));
						mExchangeBindBtn.setBackgroundResource(R.drawable.login_button_enable_xbg);
						mExchangeBindBtn.setClickable(true);
					}else{
						mExchangeBindBtn.setTextColor(getResources().getColor(R.color.text_sub));
						mExchangeBindBtn.setBackgroundResource(R.drawable.login_button_unable_xbg);
						mExchangeBindBtn.setClickable(false);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		mAccount.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (!TextUtils.isEmpty(mAccount.getText().toString()) && isPwdAviable()) {
					mKamiBindBtn.setTextColor(getResources().getColor(R.color.white));
					mKamiBindBtn.setBackgroundResource(R.drawable.login_button_enable_xbg);
					mKamiBindBtn.setClickable(true);
				} else {
					mKamiBindBtn.setTextColor(getResources().getColor(R.color.text_sub));
					mKamiBindBtn.setBackgroundResource(R.drawable.login_button_unable_xbg);
					mKamiBindBtn.setClickable(false);
				}
			}
		});

		mPwdPart1.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if (s != null && s.length() > 0) {
					MZLog.d("JD_Reader", "s.length:" + s.length());
					if (s.length() > 3) {
						mText1 = s.toString().trim().toUpperCase();
						MZLog.d("JD_Reader", "mText1:" + mText1);
						mPwdPart2.setFocusable(true);
						mPwdPart2.requestFocus();
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}
		});
		mPwdPart2.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if (s != null && s.length() > 0) {
					if (s.length() > 3) {
						mText2 = s.toString().trim().toUpperCase();
						MZLog.d("JD_Reader", "mText2:" + mText2);

						mPwdPart3.setFocusable(true);
						mPwdPart3.requestFocus();
					}
				}
				if (TextUtils.isEmpty(s)) {
					mPwdPart1.setFocusable(true);
					mPwdPart1.requestFocus();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}
		});
		mPwdPart3.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				if (s != null && s.length() > 0) {
					if (s.length() > 3) {
						mText3 = s.toString().trim().toUpperCase();
						MZLog.d("JD_Reader", "mText3:" + mText3);

						mPwdPart4.setFocusable(true);
						mPwdPart4.requestFocus();
					}
				}
				if (TextUtils.isEmpty(s)) {
					mPwdPart2.setFocusable(true);
					mPwdPart2.requestFocus();
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}
		});
		mPwdPart4.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				MZLog.d("JD_Reader", "s::" + s);
				// TODO Auto-generated method stub
				if (s != null && s.length() > 0) {
					if (s.length() > 3) {
						mText4 = s.toString().trim().toUpperCase();
						MZLog.d("JD_Reader", "mText4:" + mText4);
						mPwdPart4.setFocusable(true);

						// 设置绑定按钮属性
						if (!TextUtils.isEmpty(mAccount.getText().toString())) {
							mKamiBindBtn.setTextColor(getResources().getColor(R.color.white));
							mKamiBindBtn.setBackgroundResource(R.drawable.login_button_enable_xbg);
							mKamiBindBtn.setClickable(true);
						} else {
							mKamiBindBtn.setTextColor(getResources().getColor(R.color.text_sub));
							mKamiBindBtn.setBackgroundResource(R.drawable.login_button_unable_xbg);
							mKamiBindBtn.setClickable(false);
						}
					}
				}
				if (TextUtils.isEmpty(s)) {
					mPwdPart3.setFocusable(true);
					mPwdPart3.requestFocus();
				}
				if (s != null && s.length() < 4) {
					mKamiBindBtn.setTextColor(getResources().getColor(R.color.text_sub));
					mKamiBindBtn.setClickable(true);
				}

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	/**
	 * 
	 * @Title: isPwdAviable
	 * @Description: 判断密码框是否设置密码了
	 * @param @return
	 * @return boolean
	 * @throws
	 * @date 2015年4月3日 上午12:20:13
	 */
	private boolean isPwdAviable() {
		if (mText1 == null && TextUtils.isEmpty(mText1)) {
			return false;
		}
		if (mText2 == null && TextUtils.isEmpty(mText2)) {
			return false;
		}
		if (mText3 == null && TextUtils.isEmpty(mText3)) {
			return false;
		}
		if (mText4 == null && TextUtils.isEmpty(mText4)) {
			return false;
		}
		return true;
	}

	protected void exchangeBind() {
		mProgressHUD = ProgressHUD.show(BindReadingCardActivity.this,"绑定中...", true,false,new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if (mProgressHUD.isShowing()) {
					mProgressHUD.dismiss();
				}
			}
		});
		final String exchangeCode = mExchangeEt.getText().toString().toUpperCase();
		RequestSecurityKeyTask task = new RequestSecurityKeyTask(new RequestSecurityKeyTask.OnGetSessionKeyListener() {

			@Override
			public void onGetSessionKeySucceed() {
				WebRequestHelper.get(URLText.JD_BOOK_CHANGDU_URL, RequestParamsPool.exchangeBind(exchangeCode), true,
						new MyAsyncHttpResponseHandler(BindReadingCardActivity.this) {

							@Override
							public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
								mProgressHUD.dismiss();
								Toast.makeText(BindReadingCardActivity.this, getString(R.string.network_connect_error), 
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
								String result = new String(responseBody);
								MZLog.d("cj", "resultinfo=======>>" + result);
								String code = null;
								String message = null;
								JSONObject jsonObj;
								try {
									jsonObj = new JSONObject(new String(responseBody));
									if (jsonObj != null) {
										JSONObject desJsonObj = null;
										code = jsonObj.optString("code");
									}
								} catch (JSONException e) {
									e.printStackTrace();
								}
								mProgressHUD.dismiss();
								if (Integer.parseInt(code) == 0) {
									finish();
									CustomToast.showToast(BindReadingCardActivity.this, "绑定成功!");
//									Toast.makeText(BindReadingCardActivity.this, "绑定成功!", Toast.LENGTH_LONG).show();
								} else if (Integer.parseInt(code) == 15) {
//									Toast.makeText(BindReadingCardActivity.this, "畅读卡不存在！", Toast.LENGTH_LONG).show();
									CustomToast.showToast(BindReadingCardActivity.this, "畅读卡不存在！");
								} else if (Integer.parseInt(code) == 16) {
//									Toast.makeText(BindReadingCardActivity.this, "畅读卡未授权!", Toast.LENGTH_LONG).show();
									CustomToast.showToast(BindReadingCardActivity.this, "畅读卡未授权！");
								} else if (Integer.parseInt(code) == 17) {
//									Toast.makeText(BindReadingCardActivity.this, "畅读卡服务时间已过!", Toast.LENGTH_LONG).show();
									CustomToast.showToast(BindReadingCardActivity.this, "畅读卡服务时间已过！");
								}else{
//									Toast.makeText(BindReadingCardActivity.this, "畅读卡不存在或兑换码输入有误", Toast.LENGTH_LONG).show();
									CustomToast.showToast(BindReadingCardActivity.this, "畅读卡不存在或兑换码输入有误！");
								}
							}
						});
			}

			@Override
			public void onGetSessionKeyFailed() {
				Toast.makeText(BindReadingCardActivity.this, getString(R.string.unknown_error), Toast.LENGTH_LONG).show();

			}
		});
		task.excute();

	}

	/**
	 * 
	 * @Title: bindSubmit
	 * @Description: request server to bind.
	 * @param @param cardNo
	 * @param @param cardPwd
	 * @return void
	 * @throws
	 * @date 2015年4月3日 上午12:24:32
	 */
	private void bindSubmit(final String cardNo, final String cardPwd) {
		mProgressHUD = ProgressHUD.show(BindReadingCardActivity.this,"绑定中...", true,false,new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface dialog) {
				if (mProgressHUD.isShowing()) {
					mProgressHUD.dismiss();
				}
			}
		});
		RequestSecurityKeyTask task = new RequestSecurityKeyTask(new RequestSecurityKeyTask.OnGetSessionKeyListener() {

			@Override
			public void onGetSessionKeySucceed() {
				WebRequestHelper.get(URLText.JD_BOOK_CHANGDU_URL, RequestParamsPool.putReadingCard(cardNo, cardPwd), true,
						new MyAsyncHttpResponseHandler(BindReadingCardActivity.this) {

							@Override
							public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
								// TODO Auto-generated method
								// stub
								if (mProgressHUD.isShowing()) {
									mProgressHUD.dismiss();
								}
								Toast.makeText(BindReadingCardActivity.this, getString(R.string.network_connect_error), Toast.LENGTH_SHORT)
										.show();
							}

							@Override
							public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
								// TODO Auto-generated method
								// stub
								if (mProgressHUD.isShowing()) {
									mProgressHUD.dismiss();
								}
								String result = new String(responseBody);
								MZLog.d("cj", "resultinfo=======>>" + result);
								String code = null;
								String message = null;
								JSONObject jsonObj;
								try {
									jsonObj = new JSONObject(new String(responseBody));
									if (jsonObj != null) {
										JSONObject desJsonObj = null;
										code = jsonObj.optString("code");
									}
								} catch (JSONException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								if (Integer.parseInt(code) == 0) {
									finish();
									CustomToast.showToast(BindReadingCardActivity.this, "绑定成功!");
//									Toast.makeText(BindReadingCardActivity.this, "绑定成功!", Toast.LENGTH_LONG).show();
								} else if (Integer.parseInt(code) == 15) {
//									Toast.makeText(BindReadingCardActivity.this, "畅读卡不存在！", Toast.LENGTH_LONG).show();
									CustomToast.showToast(BindReadingCardActivity.this, "畅读卡不存在！");
								} else if (Integer.parseInt(code) == 16) {
//									Toast.makeText(BindReadingCardActivity.this, "畅读卡未授权!", Toast.LENGTH_LONG).show();
									CustomToast.showToast(BindReadingCardActivity.this, "畅读卡未授权！");
								} else if (Integer.parseInt(code) == 17) {
//									Toast.makeText(BindReadingCardActivity.this, "畅读卡服务时间已过!", Toast.LENGTH_LONG).show();
									CustomToast.showToast(BindReadingCardActivity.this, "畅读卡服务时间已过！");
								}else{
//									Toast.makeText(BindReadingCardActivity.this, "畅读卡不存在或兑换码输入有误", Toast.LENGTH_LONG).show();
									CustomToast.showToast(BindReadingCardActivity.this, "畅读卡不存在或密码输入有误！");
								}
							}

						});
			}

			@Override
			public void onGetSessionKeyFailed() {
				Toast.makeText(BindReadingCardActivity.this, getString(R.string.unknown_error), Toast.LENGTH_LONG).show();

			}
		});

		task.excute();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_bindreadingcard));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_bindreadingcard));
	}

}
