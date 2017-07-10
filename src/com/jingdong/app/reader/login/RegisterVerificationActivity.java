package com.jingdong.app.reader.login;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.MyActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;
import com.jingdong.app.reader.view.dialog.DialogManager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;
import jd.wjlogin_sdk.common.listener.OnCommonCallback;
import jd.wjlogin_sdk.common.listener.OnGetMessageCodeCallback;
import jd.wjlogin_sdk.common.listener.OnGetMessagePwdExpireTimeCallback;
import jd.wjlogin_sdk.model.FailResult;

/** 
 * 注册验证码验证界面
 * @author xuhongwei
 *
 */
public class RegisterVerificationActivity extends BaseActivityWithTopBar implements TopBarViewListener, TextWatcher {
	private Context context;
	/** 验证码编辑框 */
	private EditText phoneET;
	/** 获取验证码按钮 */
	private Button getVerificationBtn;
	/** 下一步按钮 */
	private Button nextBtn;
	/** 手机号码 */
	private String phoneNumber;
	/** 获取验证码倒计时 */
	private int second;
	/** 注册类型 */
	private String type;
	private boolean clickable = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_verification);

		context = this;
		initView();
		loadData();
	}

	private void initView() {
		getTopBarView().setTitle("手机快速注册");
		phoneET = (EditText) findViewById(R.id.phone_verify_et);
		getVerificationBtn = (Button) findViewById(R.id.get_verification_bt);
		nextBtn = (Button) findViewById(R.id.next_button);
	}
	
	private void loadData() {
		if(getIntent()!=null){
			phoneNumber = getIntent().getStringExtra("phoneNumber");
			second = getIntent().getIntExtra("pwdExpireTime", 120);
			type = getIntent().getStringExtra("type");
		}
		
		mHandler.sendMessage(mHandler.obtainMessage(0));
		
		if (!TextUtils.isEmpty(phoneNumber)) {
			TextView showTipTv = (TextView) findViewById(R.id.show_tip);
			showTipTv.setText("请输入" + phoneNumber + "收到的短信验证码");
		}

		phoneET.addTextChangedListener(this);
		
		getVerificationBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(clickable){
					if(!TextUtils.isEmpty(type) && type.equals("unbind")){
						getMessage(phoneNumber);
					}else{
						getMessageForBindedPhoneNumber(phoneNumber);
					}
				}
			}
		});
		
		nextBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String verification = phoneET.getText().toString();
				if (!TextUtils.isEmpty(verification)) {
					checkMessageCode(verification);
				}else {
					Toast.makeText(RegisterVerificationActivity.this, "请输入验证码", Toast.LENGTH_LONG).show();
				}
			}
		});
		
	}
	
	/**
	 *  检查验证码
	 */
	private void checkMessageCode(String verification) {
		MZBookApplication.getWJLoginHelper().checkMessageCode(phoneNumber, verification, new OnCommonCallback() {
			@Override
			public void onSuccess() {
				Intent intent = new Intent(context, RegisterPasswordSetActivity.class);
				intent.putExtra("type", type);
				intent.putExtra("phoneNumber", phoneNumber);
				((MyActivity)context).startActivityForResult(intent, LoginVerificationActivity.VERIFICATION_ACTIVITY);
				finish();
			}
			
			@Override
			public void onFail(FailResult failResult) {
				Toast.makeText(context, failResult.getMessage(), Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onError(String error) {
				try {
					JSONObject jsonObj = new JSONObject(error);
					Toast.makeText(context, jsonObj.optString("errMsg"), Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 未注册手机获取短信验证码
	 * @param phoneNumber
	 */
	private void getMessage(String phoneNumber){
		MZBookApplication.getWJLoginHelper().getMessageCode(phoneNumber, new OnGetMessageCodeCallback() {
			
			@Override
			public void onSuccess(int pwdExpireTime) {
				second = pwdExpireTime;
				mHandler.removeMessages(0);
				mHandler.sendEmptyMessage(0);
			}
			
			@Override
			public void onFail(FailResult failResult) {
				Toast.makeText(context, failResult.getMessage(), Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onError(String error) {
				try {
					JSONObject jsonObj = new JSONObject(error);
					Toast.makeText(context, jsonObj.optString("errMsg"), Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 已注册手机解绑并获取短信验证码
	 * @param phoneNumber
	 */
	private void getMessageForBindedPhoneNumber(String phoneNumber){
		MZBookApplication.getWJLoginHelper().unBindPhoneNum(phoneNumber, new OnGetMessagePwdExpireTimeCallback() {
			
			@Override
			public void onSuccess(int pwdExpireTime) {
				second = pwdExpireTime;
				mHandler.removeMessages(0);
				mHandler.sendEmptyMessage(0);
			}
			
			@Override
			public void onFail(FailResult failResult) {
				Toast.makeText(context, failResult.getMessage(), Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onError(String error) {
				try {
					JSONObject jsonObj = new JSONObject(error);
					Toast.makeText(context, jsonObj.optString("errMsg"), Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public void afterTextChanged(Editable edit) {
		if(edit.toString().isEmpty()){
			enableNextButton(false);
		}else{
			enableNextButton(true);
		}
	}
	
	/**
	 * 一下步按钮状态
	 * @param enabled
	 */
	private void enableNextButton(boolean enabled) {
		nextBtn.setEnabled(enabled);
		if(enabled) {	
			nextBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_login_bg));
			nextBtn.setTextColor(getResources().getColor(R.color.white));
		}else {
			nextBtn.setTextColor(getResources().getColor(R.color.hariline));
			nextBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_button_unable_xbg));
		}
	}

	@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
	@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				clickable = false;
				getVerificationBtn.setEnabled(false);
				mHandler.sendEmptyMessage(1);
				break;
			case 1:
				mHandler.removeMessages(1);
				if (second == 0) {
					getVerificationBtn.setTextColor(getResources().getColor(R.color.red_main));
					getVerificationBtn.setEnabled(true);
					getVerificationBtn.setText("获取验证码");
					clickable = true;
				} else {
					second--;
					getVerificationBtn.setTextColor(getResources().getColor(R.color.subtitle_color));
					getVerificationBtn.setText("重新发送(" + String.valueOf(second) + ")");
					getVerificationBtn.setEnabled(false);
					mHandler.sendEmptyMessageDelayed(1, 1000);
				}
				break;

			default:
				break;
			}
		}
	};
	
	public void onLeftMenuClick() {
		DialogManager.showCommonDialog(RegisterVerificationActivity.this, "点击 “返回” 将中断注册，确定返回？", "返回", "取消", new DialogInterface.OnClickListener() {
			
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
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_regist_verification));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_regist_verification));
	}

}
