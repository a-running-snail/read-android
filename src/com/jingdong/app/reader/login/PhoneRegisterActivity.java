package com.jingdong.app.reader.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.json.JSONObject;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.ProtocolActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.view.dialog.CommonDialog;
import com.jingdong.app.reader.view.dialog.DialogManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import jd.wjlogin_sdk.common.listener.CheckImageCodeAndPhoneNumCallBack;
import jd.wjlogin_sdk.common.listener.OnGetMessageCodeCallback;
import jd.wjlogin_sdk.common.listener.OnGetMessagePwdExpireTimeCallback;
import jd.wjlogin_sdk.common.listener.OnNeedImageCodeCallBack;
import jd.wjlogin_sdk.model.FailResult;
import jd.wjlogin_sdk.model.PicDataInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 注册
 * @author xuhongwei
 *
 */
public class PhoneRegisterActivity extends BaseActivityWithTopBar {
	/** 手机号码编辑控件 */
	private EditText mPhoneNumberEt;
	/** 协议同意选择框 */
	private CheckBox mAgreeCheckbox;
	/** 下一步按钮 */
	private Button mNextBtn;
	/** 用户协议跳转文本 */
	private TextView mGotoAgreement;
	private Context mContext; 
	/** 图片验证码信息 */
	private PicDataInfo mPicDataInfo = null;
	/** 图片验证码布局 */
	private RelativeLayout mImageVerificationLayout = null;
	/** 验证码输入框 */
	private EditText mVerificationInput = null;
	/** 图片验证码显示 */
	private ImageView mVerificationImage = null;
	/** 图片验证码位图 */
	private Bitmap mBitmap = null;
	/** 客服电话 */
	private TextView mServiceTelephone = null;
	/** 发送短信提示对话框 */
	private CommonDialog mDialog;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_phone_register);
		mContext =this;
		initView();
	}

	private void initView() {
		getTopBarView().setTitle("手机快速注册");
		
		mPhoneNumberEt = (EditText) findViewById(R.id.phone_number);
		mAgreeCheckbox = (CheckBox) findViewById(R.id.agree_checkbox);
		mNextBtn = (Button) findViewById(R.id.next_button);
		mGotoAgreement = (TextView) findViewById(R.id.goto_agreement);
		mImageVerificationLayout = (RelativeLayout) findViewById(R.id.mImageVerificationLayout);
		mVerificationInput = (EditText) findViewById(R.id.mVerificationInput);
		mVerificationImage = (ImageView) findViewById(R.id.mVerificationImage);
		mServiceTelephone = (TextView) findViewById(R.id.mServiceTelephone);
		mServiceTelephone.setText(Html.fromHtml("<u>"+"联系客服"+"</u>"));
		mAgreeCheckbox.setChecked(true);
		initListener();
	}

	/**
	 * 初始化控件监听
	 */
	private void initListener() {
		mGotoAgreement.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(PhoneRegisterActivity.this, ProtocolActivity.class);
				startActivity(intent);
			}
		});
		
		mNextBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String phoneNumber = mPhoneNumberEt.getText().toString().trim();
				if (!checkPhone(phoneNumber))
					return;
				
				checkImageCodeAndPhoneNum();
			}
		});
		
		mPhoneNumberEt.addTextChangedListener(mTextWatcher);
		mVerificationInput.addTextChangedListener(mTextWatcher);
		mAgreeCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean checked) {
				updateNextBtnStatus();
			}
		});
		
		mServiceTelephone.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_DIAL,Uri.parse("tel:4006065500")); 
			    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			    startActivity(intent);	
			}
		});
		
		mVerificationImage.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				checkIsNeedImageCode();
			}
		});
		
	}

	TextWatcher mTextWatcher = new TextWatcher() {
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

		@Override
		public void afterTextChanged(Editable edit) {
			updateNextBtnStatus();
		}
	};

	/**
	 * 更新下一步按钮状态
	 */
	private void updateNextBtnStatus() {
		String phoneNumber = mPhoneNumberEt.getText().toString().trim();
		if (null != mPicDataInfo) {
			String verification = mVerificationInput.getText().toString().trim();
			if(TextUtils.isEmpty(phoneNumber) || !mAgreeCheckbox.isChecked() || TextUtils.isEmpty(verification) ){
				enableNextButton(false);
			}else {
				enableNextButton(true);
			}
		}else {
			if(TextUtils.isEmpty(phoneNumber) || !mAgreeCheckbox.isChecked()) {
				enableNextButton(false);
			}else {
				enableNextButton(true);
			}
		}
	}
	
	/**
	 * 一下步按钮状态
	 * @param enabled
	 */
	private void enableNextButton(boolean enabled) {
		mNextBtn.setEnabled(enabled);
		if(enabled) {	
			mNextBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.button_login_bg));
			mNextBtn.setTextColor(getResources().getColor(R.color.white));
		}else {
			mNextBtn.setTextColor(getResources().getColor(R.color.hariline));
			mNextBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.login_button_unable_xbg));
		}
	}
	
	/**
	 * 检查是否需要图片验证码（进入注册页面时首先检查）
	 */
	private void checkIsNeedImageCode() {
		MZBookApplication.getWJLoginHelper().isNeedImageCode(new OnNeedImageCodeCallBack() {
			@Override
			public void onSuccess(PicDataInfo arg0) {
				if (null != arg0) {
					mImageVerificationLayout.setVisibility(View.VISIBLE);
					destoryBitmap();
					mPicDataInfo = arg0;
					mBitmap =BitmapFactory.decodeByteArray(mPicDataInfo.getsPicData(), 0, mPicDataInfo.getsPicData().length);
					mVerificationImage.setImageBitmap(mBitmap);
				} else {
					mPicDataInfo = null;
					destoryBitmap();
					mImageVerificationLayout.setVisibility(View.GONE);
				}
			}
			
			@Override
			public void onFail(FailResult failResult) {
				Toast.makeText(mContext, failResult.getMessage(), Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onError(String arg0) {
				try {
					JSONObject jsonObj = new JSONObject(arg0);
					Toast.makeText(mContext, jsonObj.optString("errMsg"), Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 检查手机号码是否注册过
	 */
	private void checkImageCodeAndPhoneNum() {
		boolean isNeedImageCode = false;
		final String phoneNumber = mPhoneNumberEt.getText().toString().trim();
		if(null != mPicDataInfo) {
			isNeedImageCode = true;
			String verification = mVerificationInput.getText().toString().trim();
			if(!TextUtils.isEmpty(verification)) {
				mPicDataInfo.setAuthCode(verification);
			}else {
				Toast.makeText(getApplicationContext(), "请您输入验证码", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		MZBookApplication.getWJLoginHelper().checkImageCodeAndPhoneNum(mPicDataInfo, phoneNumber, isNeedImageCode, new CheckImageCodeAndPhoneNumCallBack() {
			
			@Override
			public void onSuccess() {
				//手机号码未注册
				showSendMessageDialog(phoneNumber);
				
//				DialogManager.showCommonDialog(mContext, null, "我们将发验证码短信至："+phoneNumber, "确认", "取消", new DialogInterface.OnClickListener() {
//					
//					@Override
//					public void onClick(DialogInterface dialog, int which) {
//						dialog.dismiss();
//						switch (which) {
//						case DialogInterface.BUTTON_POSITIVE:
//							getMessage(phoneNumber);
//							break;
//						case DialogInterface.BUTTON_NEGATIVE:
//							break;
//						default:
//							break;
//						}
//					}
//				});
			}
			
			@Override
			public void onFail(FailResult failResult, PicDataInfo arg1) {
				byte replyCode = failResult.getReplyCode();
				//手机号码已经注册
				if(replyCode == 0x16){
					DialogManager.showCommonDialog(mContext, null, "该手机号已被使用，继续注册将会与原账号解绑。如果您希望使用原账号，请返回登录页直接登录。是否继续注册？", "确认", "取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							switch (which) {
							case DialogInterface.BUTTON_POSITIVE:
								getMessageForBindedPhoneNumber(phoneNumber);
								break;
							case DialogInterface.BUTTON_NEGATIVE:
								break;
							default:
								break;
							}
						}
					});
				}else{
					checkIsNeedImageCode();
					Toast.makeText(mContext, failResult.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
			
			@Override
			public void onError(String error) {
				try {
					JSONObject jsonObj = new JSONObject(error);
					Toast.makeText(mContext, jsonObj.optString("errMsg"), Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 检查手机号码是否合法
	 * @param phoneNumber
	 * @return
	 */
	public boolean checkPhone(String phoneNumber) {
		if (TextUtils.isEmpty(phoneNumber)) {
			return false;
		} else {
			String first = phoneNumber.substring(0, 1);
			if (first.equals("0")) {
				mPhoneNumberEt.setFilters(new InputFilter[] { new InputFilter.LengthFilter(12) });
				if (isMobileNOWith0(phoneNumber)) {
					return true ;
				} else {
					Toast.makeText(getApplicationContext(), "您输入的号码格式有误!", Toast.LENGTH_LONG).show();
					return false;
				}
			} else if (first.equals("1")) {
				mPhoneNumberEt.setFilters(new InputFilter[] { new InputFilter.LengthFilter(11) });
				if (isMobileNO(phoneNumber)) {
					return true;
				} else {
					Toast.makeText(getApplicationContext(), "您输入的号码格式有误!", Toast.LENGTH_LONG).show();
					return false;
				}
			} else {
				Toast.makeText(getApplicationContext(), "您输入的号码格式有误!", Toast.LENGTH_LONG).show();
				return false;
			}
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
	
	/**
	 * 未注册手机获取短信验证码
	 * @param phoneNumber
	 */
	private void getMessage(final String phoneNumber){
		MZBookApplication.getWJLoginHelper().getMessageCode(phoneNumber, new OnGetMessageCodeCallback() {
			
			@Override
			public void onSuccess(int pwdExpireTime) {
				gotoVerificationAcitivity(phoneNumber, pwdExpireTime,"unbind");
			}
			
			@Override
			public void onFail(FailResult failResult) {
				Toast.makeText(mContext, failResult.getMessage(), Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onError(String error) {
				try {
					JSONObject jsonObj = new JSONObject(error);
					Toast.makeText(mContext, jsonObj.optString("errMsg"), Toast.LENGTH_LONG).show();
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
	private void getMessageForBindedPhoneNumber(final String phoneNumber){
		MZBookApplication.getWJLoginHelper().unBindPhoneNum(phoneNumber, new OnGetMessagePwdExpireTimeCallback() {
			
			@Override
			public void onSuccess(int pwdExpireTime) {
				gotoVerificationAcitivity(phoneNumber, pwdExpireTime,"bind");
			}
			
			@Override
			public void onFail(FailResult failResult) {
				Toast.makeText(mContext, failResult.getMessage(), Toast.LENGTH_LONG).show();
			}
			
			@Override
			public void onError(String error) {
				try {
					JSONObject jsonObj = new JSONObject(error);
					Toast.makeText(mContext, jsonObj.optString("errMsg"), Toast.LENGTH_LONG).show();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * 进入输短信验证码界面
	 * @param pwdExpireTime
	 */
	private void gotoVerificationAcitivity(String phoneNumber,int pwdExpireTime,String type){
		Intent intent = new Intent(mContext, RegisterVerificationActivity.class);
		intent.putExtra("phoneNumber", phoneNumber);
		intent.putExtra("pwdExpireTime", pwdExpireTime);
		intent.putExtra("type", type);
		startActivity(intent);
		finish();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		checkIsNeedImageCode();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_regist));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_regist));
	}
	
	/**
	 * 释放验证码图片资源
	 */
	private void destoryBitmap() {
		mVerificationImage.setImageBitmap(null);
		if(null != mBitmap) {
			if(!mBitmap.isRecycled()) {
				mBitmap.recycle();
				mBitmap = null;
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		destoryBitmap();
	}
	
	/**
	 * 未注册手机号码提示发送短信验证码的对话框
	 * @param phoneNumber
	 */
	public void showSendMessageDialog(final String phoneNumber) {
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDialog = new CommonDialog(mContext, 
        		R.style.common_dialog_style);
        mDialog.setCanceledOnTouchOutside(false);
        View layout = inflater.inflate(R.layout.common_dialog_with_text_in_center, null);
        mDialog.addContentView(layout, new LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		((TextView) layout.findViewById(R.id.common_dialog_title_tv)).setVisibility(View.GONE);;

        ((TextView) layout.findViewById(R.id.common_dialog_button1))
                    .setText("确认");
                ((TextView) layout.findViewById(R.id.common_dialog_button1))
                        .setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                            	getMessage(phoneNumber);
                            	mDialog.dismiss();
                            }
                        });
		((TextView) layout.findViewById(R.id.common_dialog_button2)).setText("取消");
		((TextView) layout.findViewById(R.id.common_dialog_button2)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mDialog.dismiss();
			}
		});
        
        ((TextView) layout.findViewById(
            		R.id.phoneNumber)).setText(phoneNumber);
        mDialog.setContentView(layout);
        mDialog.show();
    }

}
