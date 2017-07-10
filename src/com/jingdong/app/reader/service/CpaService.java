package com.jingdong.app.reader.service;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.data.DataParser;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.RsaEncoder;
import com.jingdong.app.reader.util.StatisticsReportUtil;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class CpaService extends IntentService {

	private String mUserToken;

	public CpaService() {
		super("com.jingdong.app.reader.service.CpaService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		requestCPAToken();
	}

	private void requestCPAToken() {
		Boolean doCPASwitch = Configuration.getBooleanProperty(Configuration.DOSWITCHCPA, false);
		if (doCPASwitch) {
			getCPAToken();
		}
	}

	private void getCPAToken() {
		WebRequestHelper.get(URLText.JD_USER_SEARCH_URL, RequestParamsPool.getCPAToken(), new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] responseBody) {
				// TODO Auto-generated method stub
				String response = new String(responseBody);
				MZLog.d("JD_Reader", "getcpaToken:" + response);
				try {
					JSONObject obj = new JSONObject(response);
					String code = DataParser.getString(obj, "code");
					if (code.equals("0")) {
						String token = DataParser.getString(obj, "token");
						if (TextUtils.isEmpty(token)) {
							return;
						}
						MZLog.i("JD_Reader", "getcpaToken success!");
						mUserToken = token;
						getCPAPush(token);
					} else {
						mUserToken = "";
						MZLog.e("JD_Reader", "getcpaToken error");
					}
				} catch (JSONException e) {
					mUserToken = "";
					e.printStackTrace();
				}

			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				// TODO Auto-generated method stub
				MZLog.i("JD_Reader", "getCPAToken onFailure");
			}
		});

	}

	protected void getCPAPush(String token) {
		String subParterID = String.valueOf(Configuration.getProperty(Configuration.SUBPARTNERID, ""));
		String info = StatisticsReportUtil.getCpaPushInfoStr(subParterID);
		String result = "";
		try {
			result = RsaEncoder.stringRSAEncoder(info);
			if (result == "") {
				return;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// cpa合作者ID,电子书测试环境值为000001
		String parterID = String.valueOf(Configuration.getProperty(Configuration.PARTNERID,
				"10005"));
		WebRequestHelper.get(URLText.JD_USER_SEARCH_URL, RequestParamsPool.getPushData(token, parterID, result), new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
				Log.i("JD_Reader", "getCPACPAPush onFailure");
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] responseBody) {
				// TODO Auto-generated method stub
				String response = new String(responseBody);
				MZLog.i("JD_Reader", "getcpaPush::" + response);
				try {
					JSONObject obj = new JSONObject(response);
					String code = DataParser.getString(obj, "code");
					if (code.equals("0")) {
						if (DataParser.getInt(obj, "resultCode") == 0) {
							MZLog.i("JD_Reader", "savecpaPush true!");
							LocalUserSetting.savecpaPushState(mUserToken, true);
						} else {
							MZLog.i("JD_Reader", "savecpaPush false! --1");
							LocalUserSetting.savecpaPushState(mUserToken, false);
						}
					} else {
						MZLog.i("JD_Reader", "savecpaPush false! --2");
						LocalUserSetting.savecpaPushState(mUserToken, false);
						return;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

		});

	}

}
