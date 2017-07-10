package com.jingdong.app.reader.me.activity;

import org.apache.http.Header;

import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.security.RequestSecurityKeyTask;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.RegexValidateUtil;
import com.jingdong.app.reader.util.ToastUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FeedBackActivity extends BaseActivityWithTopBar {

	private RelativeLayout feedback_type;
	private EditText content;
	private EditText chaText_phone;
	private EditText chaText_qq;
	private TextView type;
	private int Type = -1;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		setContentView(R.layout.feedback);
		getTopBarView().setRightMenuOneVisiable(true,
				getString(R.string.submit), R.color.text_main, false);

		initView();
	}

	private void initView() {
		type = (TextView) findViewById(R.id.type);
		feedback_type = (RelativeLayout) findViewById(R.id.feedback_type);
		content = (EditText) findViewById(R.id.content);
		chaText_phone = (EditText) findViewById(R.id.chat_phone);
		chaText_qq = (EditText) findViewById(R.id.chat_qq);
		String model = LocalUserSetting.getFeedBackType(FeedBackActivity.this);
		if (model.endsWith("功能意见")) {
			type.setText(model);
		} else if (model.endsWith("界面意见")) {
			type.setText(model);
		} else if (model.endsWith("您的新需求")) {
			type.setText(model);
		} else if (model.endsWith("操作意见")) {
			type.setText(model);
		} else if (model.endsWith("流量问题")) {
			type.setText(model);
		} else if (model.endsWith("其他意见")) {
			type.setText(model);
		} else {
			type.setText("功能意见");
		}
		feedback_type.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(FeedBackActivity.this,
						FeedBackTypeActivity.class);
				startActivityForResult(intent, 1000);
			}
		});

		final int mMaxLenth = 1000;
		content.addTextChangedListener(new TextWatcher() {
			private int cou = 0;
			int selectionEnd = 0;
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				cou = before + count;
				content.setSelection(content.length());
				cou = content.length();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					 int after) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (cou > mMaxLenth) {
					selectionEnd = content.getSelectionEnd();
					s.delete(mMaxLenth, selectionEnd);
					Toast.makeText(FeedBackActivity.this, "已达到最大字数限制",
							 Toast.LENGTH_SHORT).show();
				}
				
				if (!TextUtils.isEmpty(content.getText().toString())
						&& (!TextUtils.isEmpty(chaText_phone.getText()
								.toString()) || !TextUtils.isEmpty(chaText_qq
								.getText().toString()))) {
					getTopBarView().getSubmenurightOneText().setTextColor(
							getResources().getColor(R.color.red_main));
				} else {
					getTopBarView().getSubmenurightOneText().setTextColor(
							getResources().getColor(R.color.text_main));
				}
			}
		});

		chaText_phone.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (!TextUtils.isEmpty(content.getText().toString())
						&& (!TextUtils.isEmpty(chaText_phone.getText()
								.toString()) || !TextUtils.isEmpty(chaText_qq
								.getText().toString()))) {
					getTopBarView().getSubmenurightOneText().setTextColor(
							getResources().getColor(R.color.red_main));
				} else {
					getTopBarView().getSubmenurightOneText().setTextColor(
							getResources().getColor(R.color.text_main));
				}
			}
		});

		chaText_qq.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable arg0) {
				// TODO Auto-generated method stub
				if (!TextUtils.isEmpty(content.getText().toString())
						&& (!TextUtils.isEmpty(chaText_phone.getText()
								.toString()) || !TextUtils.isEmpty(chaText_qq
								.getText().toString()))) {
					getTopBarView().getSubmenurightOneText().setTextColor(
							getResources().getColor(R.color.red_main));
				} else {
					getTopBarView().getSubmenurightOneText().setTextColor(
							getResources().getColor(R.color.text_main));
				}
			}
		});
	}

	/**
	 * 所有的Activity对象的返回值都是由这个方法来接收 requestCode:
	 * 表示的是启动一个Activity时传过去的requestCode值
	 * resultCode：表示的是启动后的Activity回传值时的resultCode值
	 * data：表示的是启动后的Activity回传过来的Intent对象
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1000 && resultCode == 1001) {
			String result_value = data.getStringExtra("result");
			type.setText(result_value);
		}
	}

	/**
	 * 
	 * @Title: sendFeedBack
	 * @Description: 提交反馈
	 * @param @param type
	 * @param @param memo
	 * @param @param mobile
	 * @param @param qq
	 * @return void
	 * @throws
	 * @date 2015年4月21日 下午4:54:09
	 */
	private void sendFeedBack(final String type, final String memo, final String mobile, final String qq) {
		RequestSecurityKeyTask task = new RequestSecurityKeyTask(
				new RequestSecurityKeyTask.OnGetSessionKeyListener() {

					@Override
					public void onGetSessionKeySucceed() {
						WebRequestHelper
								.post(URLText.JD_BASE_URL, RequestParamsPool
										.getLackBookParams(type, memo,mobile,qq), true,
										new MyAsyncHttpResponseHandler(
												FeedBackActivity.this) {

											@Override
											public void onFailure(int arg0,
													Header[] arg1, byte[] arg2,
													Throwable arg3) {
												// TODO Auto-generated method
												// stub
												Toast.makeText(
														FeedBackActivity.this,
														getString(R.string.network_connect_error),
														Toast.LENGTH_SHORT)
														.show();
											}

											@Override
											public void onResponse(
													int statusCode,
													Header[] headers,
													byte[] responseBody) {
												// TODO Auto-generated method
												// stub

												String result = new String(
														responseBody);
												MZLog.d("cj", "result=======>>"
														+ result);
												Toast.makeText(
														getApplicationContext(),
														"提交成功!", 1).show();

											}

										});
					}

					@Override
					public void onGetSessionKeyFailed() {
						Toast.makeText(FeedBackActivity.this,
								getString(R.string.unknown_error),
								Toast.LENGTH_LONG).show();

					}
				});

		task.excute();
	}

	@Override
	public void onRightMenuOneClick() {
		// TODO Auto-generated method stub
		super.onRightMenuOneClick();

		String contenttext = content.getText().toString().trim();
		if (contenttext.length() < 5) {
			Toast.makeText(FeedBackActivity.this, "反馈内容字数应大于5", Toast.LENGTH_LONG).show();
			return;
		}
		String telEt = chaText_phone.getText().toString();
		String qqEt = chaText_qq.getText().toString();
		String typetext = type.getText().toString();

		if (typetext.endsWith("功能意见")) {
			Type = 0;
		} else if (typetext.endsWith("界面意见")) {
			Type = 1;
		} else if (typetext.endsWith("您的新需求")) {
			Type = 2;
		} else if (typetext.endsWith("操作意见")) {
			Type = 3;
		} else if (typetext.endsWith("流量问题")) {
			Type = 4;
		} else if (typetext.endsWith("其他意见")) {
			Type = 5;
		} else {
			Type = -1;
		}

		if (Type != -1) {
			if (!TextUtils.isEmpty(contenttext)) {
//				if (!TextUtils.isEmpty(chattext_phone) || !TextUtils.isEmpty(chattext_qq)) {
//					MZLog.d("cj", "Type======>>" + Type);
//					String memo = contenttext + chattext_phone + chattext_qq;
//					sendFeedBack(Type + "", memo);
//					finish();
//				} else {
//					Toast.makeText(getApplicationContext(), "请填写联系方式!", 1)
//							.show();
//				}
//				StringBuilder contact = new StringBuilder();
				String qq = null;
				String mobile = null;
				//只有QQ
				if (!TextUtils.isEmpty(qqEt) & TextUtils.isEmpty(telEt)) {
					if (RegexValidateUtil.checkQQ(qqEt)) {
						qq= chaText_qq.getText().toString();
//						contact.append(qq);
					}else {
						ToastUtil.showToastInThread("QQ号码格式不正确，请重新输入！", Toast.LENGTH_LONG);
						return;
					}
				}
				//只有电话
				else if (TextUtils.isEmpty(qqEt) & !TextUtils.isEmpty(telEt)) {
					if (RegexValidateUtil.checkCellphone(telEt) || RegexValidateUtil.checkTelephone(telEt)) {
						mobile = chaText_phone.getText().toString();
//						contact.append(tel);
					}else {
						ToastUtil.showToastInThread("手机号码格式不正确，请重新输入！", Toast.LENGTH_LONG);
						return;
					}
				}
				//QQ、电话都有
				else if(!TextUtils.isEmpty(qqEt) & !TextUtils.isEmpty(telEt)) {
					
					if (RegexValidateUtil.checkQQ(qqEt)) {
						qq = chaText_qq.getText().toString();
					}else {
						ToastUtil.showToastInThread("QQ号码格式不正确，请重新输入！", Toast.LENGTH_LONG);
						return;
					}
					
					if (RegexValidateUtil.checkCellphone(telEt) || RegexValidateUtil.checkTelephone(telEt)) {
						mobile = chaText_phone.getText().toString();
					}else {
						ToastUtil.showToastInThread("手机号码格式不正确，请重新输入！", Toast.LENGTH_LONG);
						return;
					}
				}
				//都未填写
				else {
					ToastUtil.showToastInThread("QQ、手机至少填写一项", Toast.LENGTH_LONG);
					return;
				}
				
				String memo ="反馈内容："+ contenttext ;
				MZLog.d("J.BEYOND", memo);
				sendFeedBack(Type + "", memo,mobile,qq);
				finish();
				
			} else {
				Toast.makeText(getApplicationContext(), "请填写反馈内容!", 1).show();
			}
		} else {
			Toast.makeText(getApplicationContext(), "请选择反馈类型!", 1).show();
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_my_feedback));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_my_feedback));
	}
	
}
