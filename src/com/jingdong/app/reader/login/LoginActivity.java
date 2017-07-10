package com.jingdong.app.reader.login;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import jd.wjlogin_sdk.model.QQTokenInfo;
import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
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
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.ForgetPasswordActivity;
import com.jingdong.app.reader.activity.GlobalVarable;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bookstore.bookcart.BookCartManager;
import com.jingdong.app.reader.bookstore.buyborrow.BuyBorrowStatus;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.entity.LoginEvent;
import com.jingdong.app.reader.entity.ReLoginEvent;
import com.jingdong.app.reader.entity.SignEvent;
import com.jingdong.app.reader.eventbus.de.greenrobot.event.EventBus;
import com.jingdong.app.reader.extension.giftbag.GiftBagUtil;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.oauth.SinaAuth;
import com.jingdong.app.reader.service.NotificationService;
import com.jingdong.app.reader.setting.activity.RecommendUsersActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.LoginHelper;
import com.jingdong.app.reader.util.LoginHelper.LoginListener;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.unionlogin.LoginData;
import com.jingdong.app.reader.util.unionlogin.UnionLoginFactory;
import com.jingdong.app.reader.util.unionlogin.UnionLoginFactory.LoginResponsesLitener;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.dialog.ProgressHUD;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.exception.WeiboException;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * 登录
 * @author xuhongwei
 *
 */
public class LoginActivity extends BaseActivityWithTopBar {
	private static final String WXAPP_ID = "wx79f9198071040f23";
	private static final String QQAPP_ID = "100827021";
	private static final String WXAPP_SECRET = "8b1364fed802aa0987a261a1477b1b28";
	private static final String QQAPP_SECRET = "e2e6876eee6c11ce9acacc83dc8bd14c";
	public static final String WX_PACKAGE_NAME = "com.tencent.mm";
	private final static String scope = "snsapi_userinfo";
	private final static String state = "jd_reader";
	// QQ登录重试次数
	public static final int QQTryCount = 3;

	public static final int GetWXToken = 1;
	public static final int GetQQToken = 2;
	public static final int BindWX = 3;
	public static final int BindQQ = 4;
	public static final int WXLogin = 5;
	public static final int QQLogin = 6;
	public static final int EXIT = 7;

	private Button loginButton;
	private EditText mAccountInputEditText;
	private EditText mPwdInputEditText;
	private View loginSina;
	private TextView forgetTextView;
	private ImageView imageView;
	private SinaAuth sinaAuth = null;
	private boolean image_flag = true;
	private LinearLayout mQQLogin;
	private LinearLayout mWXLogin;
	private TextView mOtherLogin;
	private IWXAPI api;
	private QQTokenInfo qqTokenInfo;
	private int qqTryCount = 0;
	private boolean mIsLogout;
	private String from;
	private ProgressHUD mProgressHUD;
	private boolean isNeedCallback;
	private boolean isSignNeedCallback;
	public static Handler mHandler = null;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		mHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case EXIT:
					finish();
					break;

				default:
					break;
				}
				super.handleMessage(msg);
			}
		};

		mAccountInputEditText = (EditText) findViewById(R.id.account);
		mPwdInputEditText = (EditText) findViewById(R.id.password);
		loginButton = (Button) findViewById(R.id.login);
		forgetTextView = (TextView) findViewById(R.id.forget_pwd);
		imageView = (ImageView) findViewById(R.id.image);
		imageView.setImageResource(R.drawable.btn_password_invisible);

		// 微信登录
		api = WXAPIFactory.createWXAPI(this, WXAPP_ID, false);
		api.registerApp(WXAPP_ID);

		// 联合登陆
		mQQLogin = (LinearLayout) findViewById(R.id.unionlogin_qq_login_ll);
		mWXLogin = (LinearLayout) findViewById(R.id.unionlogin_wx_login_ll);
		mOtherLogin = (TextView) findViewById(R.id.unionlogin_other_tv);

		Intent intent = getIntent();
		mIsLogout = intent.getBooleanExtra("isSwitchAccount", false);
		isNeedCallback = intent.getBooleanExtra("NeedCallback", false);
		isSignNeedCallback = intent.getBooleanExtra("signNeedCallback", false);
		from = intent.getStringExtra("from");
		getTopBarView().setRightMenuOneVisiable(true, getString(R.string.sign), R.color.red_sub, false);
		
		mPwdInputEditText.addTextChangedListener(mTextWatcher);
		mAccountInputEditText.addTextChangedListener(mTextWatcher);
		
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String account = mAccountInputEditText.getText().toString().trim();
				String password = mPwdInputEditText.getText().toString().trim();
				if (TextUtils.isEmpty(account) || TextUtils.isEmpty(password)) {
					Toast.makeText(getApplicationContext(), "请输入账号和密码!", Toast.LENGTH_LONG).show();
					return;
				}
				toLogin(account, EncryptPassword(password));
			}

		});

		forgetTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
				startActivity(intent);
			}
		});

		imageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (image_flag == true) {
					imageView.setImageResource(R.drawable.btn_password_visible);
					mPwdInputEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
				} else {
					imageView.setImageResource(R.drawable.btn_password_invisible);
					mPwdInputEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
				}
				image_flag = !image_flag;
				mPwdInputEditText.postInvalidate();
				// 切换后将EditText光标置于末尾
				CharSequence charSequence = mPwdInputEditText.getText();
				if (charSequence instanceof Spannable) {
					Spannable spanText = (Spannable) charSequence;
					Selection.setSelection(spanText, charSequence.length());
				}
			}
		});

		// QQ登陆
		mQQLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// if (qqTokenInfo != null && qqTryCount < QQTryCount) {
				// qqTryCount++;
				//
				// qqLogin(qqTokenInfo);
				// if (qqTryCount == QQTryCount) {
				// qqTryCount = 0;
				// qqTokenInfo = null;
				// }
				//
				// return;
				// }
				//
				// Tencent mTencent = Tencent.createInstance(QQAPP_ID,
				// getApplicationContext());
				// if (!mTencent.isSessionValid()) {
				// IUiListener listener = new BaseUiListener();
				// mTencent.login(LoginActivity.this, "get_simple_userinfo",
				// listener);
				// } else {
				// mTencent.logout(LoginActivity.this);
				// Toast.makeText(LoginActivity.this, "获取授权太频繁，请稍候再试",
				// Toast.LENGTH_SHORT).show();
				// }
				//
			}

		});
		// 微信登陆
		mWXLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// if (!api.isWXAppInstalled()) {
				// Toast.makeText(LoginActivity.this, "未安装微信，请先安装",
				// Toast.LENGTH_SHORT).show();
				// return;
				// }
				// WXEventCallback wxEventCallback =
				// WXEventCallback.getInstance();
				// /**
				// * 监听会在WXEventCallback#onResp方法回调，获取到token
				// */
				// wxEventCallback.setFetchAuthCodeListener(new
				// WXFetchAuthCodeListener() {
				//
				// @Override
				// public void onFetchAuthCodeSuccess(String code) {
				// // wxlogin(code);
				// }
				// });
				//
				// SendAuth.Req req = new SendAuth.Req();
				// req.scope = "snsapi_userinfo";
				// req.state = "wechat_sdk_demo_test";
				// api.sendReq(req);
				UnionLoginFactory.wxlogin(LoginActivity.this, new LoginResponsesLitener() {

					@Override
					public void onLoginSuccess(LoginData data) {
						MZLog.d("J.Beyond", data.toString());
					}

					@Override
					public void onLoginFail(String errorInfo) {
						MZLog.d("J.Beyond", "errorInfo：" + errorInfo);
					}

					@Override
					public void onLoginCancel() {

					}
				});
			}
		});
		// 其他登陆
		mOtherLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

			}
		});

	}
	
	TextWatcher mTextWatcher = new TextWatcher() {
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		
		@Override
		public void afterTextChanged(Editable s) {
			updateLoginBtn();
		}
	};
	
	/**
	 * 更新登录按钮状态
	 */
	private void updateLoginBtn() {
		String account = mAccountInputEditText.getText().toString().trim();
		String password = mPwdInputEditText.getText().toString().trim();
		if (!TextUtils.isEmpty(account) && !TextUtils.isEmpty(password)) {
			loginButton.setTextColor(getResources().getColor(R.color.white));
			loginButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_login_bg));
		} else {
			loginButton.setTextColor(getResources().getColor(R.color.hariline));
			loginButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_button_unable_xbg));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		String userName = LoginUser.getUserName();
		if (!TextUtils.isEmpty(userName) && !mIsLogout && mAccountInputEditText != null) {
			mAccountInputEditText.setText(userName);
		}
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_login));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_login));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == 0) {
			Intent intent = new Intent(LoginActivity.this, VerifyPhoneActivity.class);
			startActivity(intent);
		}
		return true;
	}

	public static String EncryptPassword(String str) {
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
	/**
	 * 登录方法
	 * @param username
	 * @param pwd
	 */
	public void toLogin(final String username, final String pwd) {
		mProgressHUD = ProgressHUD.show(LoginActivity.this, "登录中，请稍候...", true, false, new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				if (mProgressHUD.isShowing()) {
					mProgressHUD.dismiss();
				}
			}
		});
		LoginHelper.doLogin(this, username, pwd,null, false, new LoginListener() {

			@Override
			public void onLoginSuccess() {
				mProgressHUD.dismiss();
				loginSuccessCallback();
			}

			@Override
			public void onLoginFail(String errCode) {
				mProgressHUD.dismiss();
			}
		});
	}
	
	/**
	 * 登录成功后的数据同步操作
	 */
	private void loginSuccessCallback(){
		checkNewUserToGrandScore();
		getBuyBorrowStatus();
		// 发送登录成功本地广播
		Intent intent = new Intent("com.jdread.action.login");
		LocalBroadcastManager.getInstance(LoginActivity.this).sendBroadcast(intent);
		// 同步购物车
		BookCartManager.getInstance().syncLocalToServer(LoginActivity.this);
		// 获取未读信息
		GiftBagUtil.getInstance().getUnReadMessage(getBaseContext());
		// 如果是切换账号，需要将SP中没有跟用户pin关联的记录删除
		if (mIsLogout) {
			LocalUserSetting.removeNullPinBookCart(LoginActivity.this);
			CustomToast.showToast(LoginActivity.this, "登录成功");
		}
		backToMain();
	}
	
	/**
	* @Description: 登陆成功后获取借阅状态
	* @author xuhongwei1
	* @date 2015年12月1日 下午1:39:58 
	* @throws 
	*/ 
	private void getBuyBorrowStatus() {
		WebRequestHelper.post(URLText.JD_USER_SEARCH_URL, RequestParamsPool.getBorrowStatusParams(), 
				false, new MyAsyncHttpResponseHandler(this) {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
			}

			@Override
			public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
				JSONObject jsonObj;
				try {
					jsonObj = new JSONObject(new String(responseBody));
					if (jsonObj != null) {
						String code = jsonObj.optString("code");
						if("0".equals(code)) {
							BuyBorrowStatus b = new BuyBorrowStatus();
							b.status = jsonObj.optBoolean("status");
							b.lendStatus = jsonObj.optInt("lendStatus");
							MZBookApplication.getInstance().setBuyBorrowStatus(b);
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void checkNewUserToGrandScore() {
		IntegrationAPI.checkAndSaveLoginScore(LoginActivity.this, new GrandScoreListener() {

			@Override
			public void onGrandSuccess(SignScore score) {
				String scoreInfo = "新用户注册获得"+score.getGetScore()+"积分";
				SpannableString span = new SpannableString(scoreInfo);
				int start1 = 7;
				int end1 = start1 + String.valueOf(score.getGetScore()).length();
				span.setSpan(new ForegroundColorSpan(Color.RED), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				CustomToast.showToast(LoginActivity.this, span);
			}

			@Override
			public void onGrandFail() {
				MZLog.d("J", "新用户登录获取积分失败");
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// sso 授权回调
		if (requestCode == 32973 && sinaAuth != null) {
			sinaAuth.authCallBack(requestCode, resultCode, data);
		} else if (requestCode == GlobalVarable.REQUEST_CODE_RECOMMEND || resultCode == GlobalVarable.REQUEST_CODE_GOTO_FIRST_PAGE) {
			backToMain();
		}
		//风险用户输入验证码登录成功后的回调方法，进行同步数据操作
		else if(requestCode == LoginVerificationActivity.VERIFICATION_ACTIVITY && resultCode == RESULT_OK){
			loginSuccessCallback();
		}
	}

	private void disableScreenOperation() {
		loginButton.setEnabled(false);
		mAccountInputEditText.setEnabled(false);
		mPwdInputEditText.setEnabled(false);
		loginSina.setEnabled(false);
		setProgressBarIndeterminateVisibility(true);
	}

	private void enableScreenOperation() {
		loginButton.setEnabled(true);
		mAccountInputEditText.setEnabled(true);
		mPwdInputEditText.setEnabled(true);
		loginSina.setEnabled(true);
		setProgressBarIndeterminateVisibility(false);
	}

	private void backToMain() {
		Intent intent =new Intent();
		startService(new Intent(this,NotificationService.class));
		this.setResult(GlobalVarable.REQUEST_CODE_GOTO_FIRST_PAGE, intent);
		EventBus.getDefault().post(new ReLoginEvent());
		if (isNeedCallback) {
			EventBus.getDefault().post(new LoginEvent());
		}
		if (isSignNeedCallback) {
			EventBus.getDefault().post(new SignEvent());
		}
		
		this.finish();
	}

	/**
	 * 从个人中心页面跳转的页面返回时需处理返回动作
	 */
	@Override
	public void onLeftMenuClick() {
		if (from != null && from.equals("userActivity")) {
			Intent intent = new Intent();
			setResult(3, intent);
			finish();
		}
		super.onLeftMenuClick();
	}

	/**
	 * 从个人中心页面跳转的页面返回时需处理返回动作
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (from != null && from.equals("userActivity")) {
			Intent intent = new Intent();
			setResult(3, intent);
			finish();
		}
		return super.onKeyDown(keyCode, event);
	}

	public class LoginTask extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			disableScreenOperation();
		}

		@Override
		protected String doInBackground(String... params) {
			String result = WebRequest.postWebDataWithContext(LoginActivity.this, URLText.loginUrl, params[0]);
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			String errorString = "";
			if (TextUtils.isEmpty(result)) {
				errorString = getString(R.string.unknown_error);
			}
			MZLog.d("login", result);
			try {
				JSONObject o = new JSONObject(result);

				if (!o.isNull("errors")) {
					JSONArray a = o.getJSONArray("errors");
					for (int i = 0; i < a.length(); ++i) {
						errorString += a.getString(i);
					}
				} else {
					String token = o.getString("token");

					JSONObject userInfo = o.getJSONObject("user");
					LocalUserSetting.saveTokenAndUserInfo(LoginActivity.this, token, userInfo.toString());
					backToMain();
				}
			} catch (JSONException e) {
				errorString = getString(R.string.unknown_error);
				e.printStackTrace();
			}

			if (!TextUtils.isEmpty(errorString)) {
				Toast toast = Toast.makeText(LoginActivity.this, errorString, Toast.LENGTH_LONG);
				toast.show();
			}
			enableScreenOperation();

		}

	}

	class AuthDialogListener implements WeiboAuthListener {

		@Override
		public void onComplete(Bundle response) {
			Oauth2AccessToken accessToken = Oauth2AccessToken.parseAccessToken(response);

			if (accessToken.isSessionValid()) {
				LocalUserSetting.saveSinaAccessToken(LoginActivity.this, accessToken);
				long expiresMillsec = accessToken.getExpiresTime();
				SinaLoginTask task = new SinaLoginTask();
				String[] p = new String[] { accessToken.getToken(), accessToken.getUid(), String.valueOf(expiresMillsec / 1000) };
				task.execute(p);
			} else {
				// 以下几种情况，您会收到 Code：
				// 1. 当您未在平台上注册的应用程序的包名与签名时；
				// 2. 当您注册的应用程序包名与签名不正确时；
				// 3. 当您在平台上注册的包名和签名与您当前测试的应用的包名和签名不匹配时。
				String code = response.getString("code");
				String message = getString(R.string.weibosdk_toast_auth_failed);
				if (!TextUtils.isEmpty(code)) {
					message = message + "\nObtained the code: " + code;
				}
				Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
			}
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(LoginActivity.this, "Sina Weibo Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		@Override
		public void onCancel() {
			Toast.makeText(LoginActivity.this, R.string.weibosdk_toast_auth_canceled, Toast.LENGTH_LONG).show();
		}

	}

	private void goToRecommend() {
		Intent intent = new Intent(this, RecommendUsersActivity.class);
		intent.putExtra(RecommendUsersActivity.LOGIN_REGISTER, true);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, GlobalVarable.REQUEST_CODE_RECOMMEND);
	}

	public class SinaLoginTask extends AsyncTask<String, Void, String> {

		private ProgressDialog dialog;

		public SinaLoginTask() {
			dialog = new ProgressDialog(LoginActivity.this);
			dialog.setTitle(R.string.sina_weibo);
			dialog.setMessage(getString(R.string.logging_in));
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {
			String sinaToken = params[0];
			String uid = params[1];
			String expires = params[2];
			String userData = sinaAuth.getUserData(sinaToken, uid);
			try {
				JSONObject o = new JSONObject(userData);
				String name = o.optString("screen_name");
				String avatar = o.optString("avatar_large");
				String gender = o.optString("gender");
				int sex = -1;
				if (gender.equalsIgnoreCase("m")) {
					sex = 0;
				} else if (gender.equalsIgnoreCase("f")) {
					sex = 1;
				}

				StringBuilder sb = new StringBuilder();
				sb.append("access_token=").append(sinaToken);
				sb.append("&uid=").append(uid);
				sb.append("&source=").append("m_sina");
				sb.append("&expires_at=").append(expires);
				sb.append("&name=").append(name);
				sb.append("&avatar=").append(avatar);
				if (sex != -1) {
					sb.append("&sex=").append(sex);
				}

				String result = WebRequest.postWebDataWithContext(LoginActivity.this, URLText.saloginUrl, sb.toString());
				return result;

			} catch (JSONException e) {
				e.printStackTrace();
			}

			MZLog.d("LoginActivity", userData);
			return "";
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			dialog.dismiss();
			String errorString = "";
			if (TextUtils.isEmpty(result)) {
				errorString = getString(R.string.unknown_error);
			}
			MZLog.d("login", result);
			try {
				JSONObject o = new JSONObject(result);
				if (!o.isNull("error") || !o.isNull("errors")) {
					JSONObject error = o.getJSONObject("error");
					String message = error.optString("message");
					int errorCode = error.optInt("code");
					errorString = message + "(" + errorCode + ")";

				} else {
					String token = o.getString("auth_token");
					LocalUserSetting.saveTokenAndUserInfo(LoginActivity.this, token, result);
					boolean isRegister = o.optBoolean("register");
					if (isRegister) {
						goToRecommend();
					} else {
						backToMain();
					}
				}
			} catch (JSONException e) {
				errorString = getString(R.string.unknown_error);
				e.printStackTrace();
			}

			if (!TextUtils.isEmpty(errorString)) {
				Toast.makeText(LoginActivity.this, errorString, Toast.LENGTH_LONG).show();
			}

		}

	}

	@Override
	public void onRightMenuOneClick() {
		super.onRightMenuOneClick();
		Intent itIntent = new Intent(LoginActivity.this, PhoneRegisterActivity.class);
		startActivity(itIntent);
	}

}
