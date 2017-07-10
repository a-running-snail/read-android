package com.jingdong.app.reader.security;

import org.apache.http.Header;
import org.json.JSONObject;

import android.text.TextUtils;

import com.jingdong.app.reader.entity.BodyEncodeEntity;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.DesUtil;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.RsaEncoder;
import com.loopj.android.http.AsyncHttpResponseHandler;

/**
 * 负责登录注册时请求的加密，以及相关加密接口的处理
 */
public class RequestSecurityKeyTask {

	private BodyEncodeEntity bodyEncode;
	private OnGetSessionKeyListener listener;
	

	public interface OnGetSessionKeyListener {
	    public void onGetSessionKeySucceed();
	    public void onGetSessionKeyFailed();
	}
	
	
	public RequestSecurityKeyTask(
			OnGetSessionKeyListener listener) {
		this.listener = listener;
	}

	private void complete(BodyEncodeEntity encodeEntity) {
		if (encodeEntity.isSuccess) {
			RsaEncoder.setEncodeEntity(encodeEntity);
			if(listener!=null)
				listener.onGetSessionKeySucceed();
		} else {
			if(listener!=null)
				listener.onGetSessionKeyFailed();
		}
	}

	public void excute() {

		if (RsaEncoder.checkSessionKey()) {
			bodyEncode = RsaEncoder.getEncodeEntity();
			if (bodyEncode.isSuccess) {
				RsaEncoder.setEncodeEntity(bodyEncode);
				// 安全验证成功
				if(listener!=null)
					listener.onGetSessionKeySucceed();
			}
			else {
				// 安全验证失败
				RsaEncoder.setEncodeEntity(null);
				if(listener!=null)
					listener.onGetSessionKeyFailed();
				
			}

		} else {
			bodyEncode = new BodyEncodeEntity();
			handle(bodyEncode);
		}
	}

	private void handle(BodyEncodeEntity encodeEntity) {
		if (!encodeEntity.isSuccess) {
			complete(encodeEntity);
		} else if (TextUtils.isEmpty(encodeEntity.sourcePublicKey)) {
			requestPublickey();
		} else if (TextUtils.isEmpty(encodeEntity.sessionKey)) {
			requestSessionKey();
		} else if (TextUtils.isEmpty(encodeEntity.desSessionKey)) {
			desSessionKey();
		} else {
			complete(encodeEntity);
		}
	}

	/*
	 * des SessionKey
	 */

	private void desSessionKey() {
		// BodyEncodeEntity encodeEntity = RsaEncoder.getEncodeEntity();
		bodyEncode.desSessionKey = DesUtil.decrypt(bodyEncode.sessionKey,
				bodyEncode.strEnvelope);
		if (!TextUtils.isEmpty(bodyEncode.desSessionKey)) {
			bodyEncode.isSuccess = true;
		} else {
			bodyEncode.isSuccess = false;
		}
		
		handle(bodyEncode);
	}

	/*
	 * 获取对称加密KEY
	 */
	private void requestSessionKey() {
		String key = RsaEncoder.stringEnvelope(bodyEncode);
		WebRequestHelper.post(URLText.JD_BASE_URL,
				RequestParamsPool.getSessionKeyParams(key),
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {

						String result = new String(arg2);
						
						
						boolean isSuccess = false;

						try {
							if (bodyEncode == null) {
								return;
							}
							JSONObject obj = new JSONObject(result);
							String code = obj.optString("code");
							if (code.equals("0")) {
								String sessionKey = obj.optString("sessionKey");
								bodyEncode.sessionKey = sessionKey;

								if (!TextUtils.isEmpty(sessionKey)) {
									isSuccess = true;
								}
							} 

						} catch (Exception e) {

							e.printStackTrace();
						}
						bodyEncode.isSuccess = isSuccess;
						handle(bodyEncode);
					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						bodyEncode.isSuccess = false;
						handle(bodyEncode);
					}
				});

	}

	/*
	 * 获取rsa公钥
	 */
	private void requestPublickey() {

		WebRequestHelper.post(URLText.JD_BASE_URL,
				RequestParamsPool.getRsaPublicKeyParams(),
				new AsyncHttpResponseHandler() {

					@Override
					public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
						String result = new String(arg2);
						
						MZLog.d("wangguodong", "public key###"+result);
						boolean isSuccess = false;
						try {
							JSONObject obj = new JSONObject(result);
							String code = obj.optString("code");
							if (code.equals("0")) {
								String rsaKey = obj.optString("pubKey");
								bodyEncode.sourcePublicKey = rsaKey;

								if (!TextUtils.isEmpty(rsaKey)) {
									isSuccess = true;
								}
							}

						} catch (Exception e) {
							e.printStackTrace();
						}

						bodyEncode.isSuccess = isSuccess;
						handle(bodyEncode);

					}

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2,
							Throwable arg3) {
						bodyEncode.isSuccess = false;
						handle(bodyEncode);

					}
				});

	}
}
