package com.jingdong.app.reader.login;

import org.apache.http.Header;
import org.json.JSONObject;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.LoginHelper;
import com.jingdong.app.reader.util.LoginHelper.LoginListener;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.ToastUtil;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.jingdong.app.reader.view.dialog.ProgressHUD;
import com.loopj.android.http.AsyncHttpResponseHandler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 风险用户验证页面
 * @author xuhongwei
 *
 */
public class LoginVerificationActivity extends BaseActivityWithTopBar implements TextWatcher {
	public static final int VERIFICATION_ACTIVITY = 342;
	private Context context;
	private EditText imageET;
	private EditText phoneET;
	private ImageView imageView;
	private Button getVerificationBtn;
	private Button submitBtn;
	private Bitmap bitmap;
	private TextView showTipTv;
	private String userName;
	private String pwd;
	private String verificationType;
	private String phoneNumber;
	private int second;
	private LinearLayout imageVertifyLayout, phoneVertifyLayout, imageLayout;
	private ProgressHUD mProgressHUD;
	private boolean clickable = true;
	private final int EXIT = 1001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_verification);

		context = this;
		initView();
		loadData();
	}

	private void initView() {
		imageET = (EditText) findViewById(R.id.image_verification_et);
		phoneET = (EditText) findViewById(R.id.phone_verify_et);
		imageView = (ImageView) findViewById(R.id.image_verification);
		getVerificationBtn = (Button) findViewById(R.id.get_verification_bt);
		imageVertifyLayout = (LinearLayout) findViewById(R.id.image_verification_layout);
		phoneVertifyLayout = (LinearLayout) findViewById(R.id.phone_verification_layout);
		imageLayout = (LinearLayout) findViewById(R.id.image_layout);
		submitBtn = (Button) findViewById(R.id.submit);
		showTipTv = (TextView) findViewById(R.id.show_tip);
	}

	private void loadData() {
		if (getIntent() != null) {
			userName = getIntent().getStringExtra("userName");
			pwd = getIntent().getStringExtra("pwd");
			phoneNumber = getIntent().getStringExtra("phoneNumber");
			verificationType = getIntent().getStringExtra("verificationType");
		}

		if (TextUtils.isEmpty(userName))
			return;

		if (verificationType.equals("image")) {
			imageVertifyLayout.setVisibility(View.VISIBLE);
			phoneVertifyLayout.setVisibility(View.GONE);
			loadImageVerification();
		} else {
			imageVertifyLayout.setVisibility(View.GONE);
			phoneVertifyLayout.setVisibility(View.VISIBLE);

			if (!TextUtils.isEmpty(phoneNumber)) {
				if (phoneNumber.length() > 8) {
					String phonenumber = phoneNumber.substring(0, 3) + "****"
							+ phoneNumber.substring(7, phoneNumber.length());
					showTipTv.setText("您的账号（" + phonenumber + "）存在安全风险，请输入短信验证码进行登录");
				}
			}
		}

		imageET.addTextChangedListener(this);
		phoneET.addTextChangedListener(this);

		imageLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				loadImageVerification();
			}
		});

		getVerificationBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (clickable)
					getPhoneVerification();
			}
		});

		submitBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String verification;
				if (!TextUtils.isEmpty(verificationType) && verificationType.equals("image")) {
					verification = imageET.getText().toString();
				} else {
					verification = phoneET.getText().toString();
				}

				if (TextUtils.isEmpty(verification)) {
					Toast.makeText(LoginVerificationActivity.this, "请输入验证码", Toast.LENGTH_LONG).show();
					return;
				}

				mProgressHUD = ProgressHUD.show(context, "登录中，请稍候...", true, false, new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						if (mProgressHUD.isShowing()) {
							mProgressHUD.dismiss();
						}
					}
				});
				LoginHelper.doLogin(context, userName, pwd, verification, false, new LoginListener() {

					@Override
					public void onLoginSuccess() {
						mHandler.sendEmptyMessage(EXIT);
					}

					@Override
					public void onLoginFail(String errCode) {
						mProgressHUD.dismiss();
						if ("42".equals(errCode)) {
							Toast.makeText(LoginVerificationActivity.this, "输入的验证码不正确，请重新输入。", Toast.LENGTH_LONG)
									.show();
						} else if ("43".equals(errCode)) {
							Toast.makeText(LoginVerificationActivity.this, "验证码已失效，请点击重新获取", Toast.LENGTH_LONG).show();
							second = 0;
							mHandler.sendMessage(mHandler.obtainMessage(1));
						}
					}
				});
			}
		});

	}

	/**
	 * 加载图片验证码
	 */
	private void loadImageVerification() {
		if (!NetWorkUtils.isNetworkConnected(context)) {
			ToastUtil.showToastInThread(getResources().getString(R.string.network_not_find), 0);
			return;
		}

		WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getImageVerificationParams(userName),
				new MyAsyncHttpResponseHandler(context, true) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						bitmap = BitmapFactory.decodeByteArray(responseBody, 0, responseBody.length);
						imageView.setImageBitmap(bitmap);
					}
				});
	}

	/**
	 * 获取手机短信验证码
	 */
	private void getPhoneVerification() {
		mHandler.sendMessage(mHandler.obtainMessage(0));
		// 发送验证码
		WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool.getRigisterCode(phoneNumber),
				new AsyncHttpResponseHandler() {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						Toast.makeText(context, "请求出错!", Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {

						String result = new String(arg2);
						try {
							JSONObject object = new JSONObject(result);
							String code = object.optString("code");
							String message = object.optString("message");

							if (code.equals("0"))
								DialogManager.showToastDialog(context, "提示", "验证码已发送，请耐心等待!");
							else {
								second = 0;
								mHandler.sendMessage(mHandler.obtainMessage(1));
								Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
							}

						} catch (Exception e) {
							e.printStackTrace();
						}

					}

				});

	}

	@Override
	public void afterTextChanged(Editable edit) {
		if (edit.toString().isEmpty()) {
			submitBtn.setEnabled(false);
			submitBtn.setTextColor(getResources().getColor(R.color.hariline));
			submitBtn.setBackground(getResources().getDrawable(R.drawable.login_button_unable_xbg));
		} else {
			submitBtn.setEnabled(true);
			submitBtn.setTextColor(getResources().getColor(R.color.white));
			submitBtn.setBackground(getResources().getDrawable(R.drawable.button_login_bg));
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				clickable = false;
				submitBtn.setEnabled(false);
				second = 120;
				mHandler.sendMessage(mHandler.obtainMessage(1));
				break;
			case 1:
				mHandler.removeMessages(1);
				if (second == 0) {
					getVerificationBtn.setEnabled(true);
					getVerificationBtn.setText("获取验证码");
					getVerificationBtn.setTextColor(getResources().getColor(R.color.red_main));

					clickable = true;
				} else {
					second--;
					getVerificationBtn.setTextColor(getResources().getColor(R.color.hariline));
					getVerificationBtn.setText(String.valueOf(second) + "秒后获取");
					getVerificationBtn.setEnabled(false);
					mHandler.sendMessageDelayed(mHandler.obtainMessage(1), 1000);
				}
				break;
			case EXIT:
				second = 0;
				mHandler.removeMessages(1);
				mProgressHUD.dismiss();
				setResult(RESULT_OK);
				finish();
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_verification_code));
	}

	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_verification_code));
	}

}
