package com.jingdong.app.reader.activity;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import com.baidu.mobstat.StatService;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.security.RequestSecurityKeyTask;
import com.jingdong.app.reader.util.CommonUtil;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.RegexValidateUtil;
import com.jingdong.app.reader.util.RsaEncoder;
import com.jingdong.app.reader.util.ToastUtil;
import android.content.Intent;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

public class LackBookSignActivity extends BaseActivityWithTopBar {

	private EditText book_name;
	private EditText book_author;
	private EditText publishing_house;
	private EditText contact_qq;
	private EditText contact_tel;
	private int retryCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lack_book_sign);
		initView();
	}

	private void initView() {
		Intent intent = getIntent();
		String bookname = intent.getStringExtra("bookname");
		getTopBarView().setRightMenuOneVisiable(true,
				getString(R.string.submit), R.color.red_sub, false);
		book_name = (EditText) findViewById(R.id.book_name);
		book_author = (EditText) findViewById(R.id.book_author);
		publishing_house = (EditText) findViewById(R.id.publishing_house);
		contact_qq = (EditText) findViewById(R.id.contact_qq);
		contact_tel = (EditText) findViewById(R.id.contact_tel);
		book_name.setText(bookname);
		// 切换后将EditText光标置于末尾
		book_name.postInvalidate();
		CharSequence charSequence = book_name.getText();
		if (charSequence instanceof Spannable) {
			Spannable spanText = (Spannable) charSequence;
			Selection.setSelection(spanText, charSequence.length());
		}
	}

	@Override
	public void onRightMenuOneClick() {
		super.onRightMenuOneClick();
		MZLog.d("cj", "click==========>>");
		String Book_name = book_name.getText().toString();
		String Book_author = book_author.getText().toString();
		String Publishing_house = publishing_house.getText().toString();
//		StringBuilder contact = new StringBuilder();
		
		String qqEt = contact_qq.getText().toString();
		String telEt = contact_tel.getText().toString();
		
		String qq = null;
		String mobile = null;
		
		//只有QQ
		if (!TextUtils.isEmpty(qqEt) & TextUtils.isEmpty(telEt)) {
			if (RegexValidateUtil.checkQQ(qqEt)) {
				qq = contact_qq.getText().toString();
			}else {
				ToastUtil.showToastInThread("QQ号码格式不正确，请重新输入！", Toast.LENGTH_LONG);
				return;
			}
		}
		//只有电话
		else if (TextUtils.isEmpty(qqEt) & !TextUtils.isEmpty(telEt)) {
			if (RegexValidateUtil.checkCellphone(telEt) || RegexValidateUtil.checkTelephone(telEt)) {
				mobile = contact_tel.getText().toString();
			}else {
				ToastUtil.showToastInThread("手机号码格式不正确，请重新输入！", Toast.LENGTH_LONG);
				return;
			}
		}
		//QQ、电话都有
		else if(!TextUtils.isEmpty(qqEt) & !TextUtils.isEmpty(telEt)) {
			if (RegexValidateUtil.checkQQ(qqEt)) {
				qq = contact_qq.getText().toString();
			}else {
				ToastUtil.showToastInThread("QQ号码格式不正确，请重新输入！", Toast.LENGTH_LONG);
				return;
			}
			
			if (RegexValidateUtil.checkCellphone(telEt) || RegexValidateUtil.checkTelephone(telEt)) {
				mobile = contact_tel.getText().toString();
			}else {
				ToastUtil.showToastInThread("手机号码格式不正确，请重新输入！", Toast.LENGTH_LONG);
				return;
			}
		}
		//都未填写
		else {
			ToastUtil.showToastInThread("QQ、电话至少填写一项", Toast.LENGTH_LONG);
			return;
		}
		
		if (!TextUtils.isEmpty(Book_name)) {
			String memo = "缺书:"+Book_name +"\n"+ Book_author +"\n"+ Publishing_house +"\n";
			sendLockBook("6", memo,mobile,qq);
			CommonUtil.toggleSoftInput(LackBookSignActivity.this);
			finish();
		} else {
			ToastUtil.showToastInThread("请输入书名!", Toast.LENGTH_SHORT);
			
		}
	}

	/**
	 * 
	 * @Title: sendLockBook
	 * @Description: 提交缺书登记
	 * @param @param type
	 * @param @param memo
	 * @param @param mobile
	 * @param @param qq
	 * @return void
	 * @throws
	 * @date 2015年4月21日 下午5:06:53
	 */
	private void sendLockBook(final String type, final String memo, final String mobile, final String qq) {
		
		
		RequestSecurityKeyTask task = new RequestSecurityKeyTask(new RequestSecurityKeyTask.OnGetSessionKeyListener() {

			@Override
			public void onGetSessionKeySucceed() {
				WebRequestHelper.post(URLText.JD_BASE_URL, RequestParamsPool.getLackBookParams(type, memo, mobile, qq),
						true, new MyAsyncHttpResponseHandler(LackBookSignActivity.this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						// stub
						Toast.makeText(LackBookSignActivity.this, getString(R.string.network_connect_error),
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
						retryCount++;
						String result = new String(responseBody);
						MZLog.d("cj", "resultinfo=======>>" + result);
						try {
							JSONObject jsonObj = new JSONObject(result);
							String code = jsonObj.optString("code");
							if(!TextUtils.isEmpty(code) && code.equals("0")){
								ToastUtil.showToastInThread("提交成功!", Toast.LENGTH_LONG);
							}else if(!TextUtils.isEmpty(code) && code.equals("8") && retryCount < 2){
								RsaEncoder.setEncodeEntity(null);
								sendLockBook(type,memo,mobile,qq);
								return;
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
						

					}

				});
			}

			@Override
			public void onGetSessionKeyFailed() {
				ToastUtil.showToastInThread(getString(R.string.unknown_error), Toast.LENGTH_LONG);
			}
		});

		task.excute();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_search_regist));
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_search_regist));
	}
	
}
