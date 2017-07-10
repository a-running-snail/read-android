package com.jingdong.app.reader.login;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.extension.giftbag.GiftBagUtil;
import com.jingdong.app.reader.extension.integration.IntegrationAPI;
import com.jingdong.app.reader.extension.integration.IntegrationAPI.GrandScoreListener;
import com.jingdong.app.reader.me.model.SignScore;
import com.jingdong.app.reader.util.LoginHelper;
import com.jingdong.app.reader.util.LoginHelper.LoginListener;
import com.jingdong.app.reader.view.CustomToast;
import com.jingdong.app.reader.view.dialog.DialogManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import jd.wjlogin_sdk.common.listener.OnCommonCallback;
import jd.wjlogin_sdk.model.FailResult;

/**
 * 注册密码设置界面
 * @author xuhongwei
 *
 */
public class RegisterPasswordSetActivity extends BaseActivityWithTopBar implements TextWatcher {
	private Context context;
	/** 密码编辑框 */
	private EditText mPwdEt;
	/** 完成按钮 */
	private Button finishBtn;
	/** 密码明文切换按钮 */
	private ImageView mShowPwdIv;
	/** 手机号 */
	private String phoneNumber;
	/** 注册类型 bind unbind */
	private String type;
	/** 显示隐藏密码标识 */
	private boolean image_flag = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_pwd_set);

		context = this;
		initView();
		loadData();
	}

	private void initView() {
		getTopBarView().setTitle("手机快速注册");
		mPwdEt = (EditText) findViewById(R.id.pwd_et);
		finishBtn = (Button) findViewById(R.id.finish_button);
		mShowPwdIv = (ImageView) findViewById(R.id.image);
	}
	
	private void loadData() {
		if (getIntent() != null) {
			phoneNumber = getIntent().getStringExtra("phoneNumber");
			type = getIntent().getStringExtra("type");
		}
		
		mShowPwdIv.setImageResource(R.drawable.btn_password_invisible);
		mPwdEt.setTransformationMethod(PasswordTransformationMethod.getInstance());
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
		
		mPwdEt.addTextChangedListener(this);
		finishBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final String password = mPwdEt.getText().toString();
				if(!checkPwd(password))
					return ;
				
				if(!TextUtils.isEmpty(type) && type.equals("unbind")){
					regist(password);
				}else {
					DialogManager.showCommonDialog(RegisterPasswordSetActivity.this, "确认解绑并注册", 
							"1、确认注册将解除" + phoneNumber + "与原账号的绑定，并注册一个与原来账号无关的新账号；\n" +
							"2、原账号不能再使用该手机号登录，可能造成订单、资产无法查看及使用，若忘记用户名请联系京东客服；\n" +
							"3、若原账号为您本人所有，建议直接登录或找回密码；", 
							"确认", "取消", new DialogInterface.OnClickListener() {
					
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								regist(password);
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
		});
	}
	
	private boolean registing = false;
	
	/**
	 * 注册
	 * @param password
	 */
	private void regist(final String password) {
		if(registing) {
			return;
		}
		registing = true;
		MZBookApplication.getWJLoginHelper().setLoginPassword(phoneNumber, password, new OnCommonCallback() {
			
			@Override
			public void onSuccess() {
				registing = false;
				LoginHelper.doLogin(context, phoneNumber, EncryptPassword(mPwdEt.getText().toString()),null, false, new LoginListener() {
					
					@Override
					public void onLoginSuccess() {
						// 新用户登录领取积分
						checkNewUserToGrandScore();
						// 登入成功之后检查是否有新的消息
						GiftBagUtil.getInstance().getUnReadMessage(getBaseContext());
						if(null != LoginActivity.mHandler) {
							LoginActivity.mHandler.sendEmptyMessage(LoginActivity.EXIT);
						}
						setResult(RESULT_OK);
						finish();
					}
					
					@Override
					public void onLoginFail(String errCode) {
						
					}
				});
				
			}
			
			@Override
			public void onFail(FailResult arg0) {
				registing = false;
				Toast.makeText(context, arg0.getMessage(), Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onError(String arg0) {
				registing = false;
				try {
					JSONObject jsonObj = new JSONObject(arg0);
					Toast.makeText(context, jsonObj.optString("errMsg"), Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					Toast.makeText(context, arg0, Toast.LENGTH_LONG).show();
				}
			}
		});
	}
	
	private void checkNewUserToGrandScore() {
		IntegrationAPI.checkAndSaveLoginScore(RegisterPasswordSetActivity.this, new GrandScoreListener() {

			@Override
			public void onGrandSuccess(SignScore score) {
				String scoreInfo = "新用户注册获得"+score.getGetScore()+"积分";
				SpannableString span = new SpannableString(scoreInfo);
				int start1 = 7;
				int end1 = start1 + String.valueOf(score.getGetScore()).length();
				span.setSpan(new ForegroundColorSpan(Color.RED), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				CustomToast.showToast(MZBookApplication.getInstance(), span);
			}

			@Override
			public void onGrandFail() {
			}
		});
	}
	
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
	
	/**
	 * 检查密码格式
	 * @param password
	 * @return
	 */
	public boolean checkPwd(String password) {
		if (TextUtils.isEmpty(password)) {
			Toast.makeText(getApplicationContext(), "请输入密码!", Toast.LENGTH_LONG).show();
			return false;
		} else if (password.length() < 6) {
			Toast.makeText(getApplicationContext(), "您输入的密码小于6位!", Toast.LENGTH_LONG).show();
			return false;
		} else if (isNumeric(password)) {
			Toast.makeText(getApplicationContext(), "您输入的密码为纯数字!", Toast.LENGTH_LONG).show();
			return false;
		} else if (isLetter(password)) {
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
	
	@Override
	public void afterTextChanged(Editable edit) {
		if(edit.toString().isEmpty()){
			enableNextButton(false);
		}else{
			enableNextButton(true);
		}
	}
	
	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
	@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

	/**
	 * 设置完成按钮状态
	 * @param enabled
	 */
	private void enableNextButton(boolean enabled) {
		finishBtn.setEnabled(enabled);
		if(enabled) {	
			finishBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_login_bg));
			finishBtn.setTextColor(getResources().getColor(R.color.white));
		}else {
			finishBtn.setTextColor(getResources().getColor(R.color.hariline));
			finishBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_button_unable_xbg));
		}
	}
	
	public void onLeftMenuClick() {
		DialogManager.showCommonDialog(RegisterPasswordSetActivity.this, "点击 “返回” 将中断注册，确定返回？", "返回", "取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					finish();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					break;
				default:
					break;
				}
			}
		});
	};
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_regist_PasswordSet));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_regist_PasswordSet));
	}

}
