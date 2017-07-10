package com.jingdong.app.reader.net;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.login.LoginActivity;
import com.jingdong.app.reader.util.MZLog;
import com.loopj.android.http.AsyncHttpResponseHandler;

public abstract class MyAsyncHttpResponseHandler extends
		AsyncHttpResponseHandler {

	private Context mContext = null;

	private boolean isSaveCookies = false;

	public MyAsyncHttpResponseHandler(Context context) {
		isSaveCookies = false;
		mContext = context;
	}

	public MyAsyncHttpResponseHandler(Context context, boolean isSaveCookies) {
		mContext = context;
		setSaveCookies(isSaveCookies);
	}

	/**
	 * 处理权限以及认证相关的问题 以及coookie 请求头等
	 * 
	 * @author WANGGUODONG
	 * @param statusCode
	 *            状态码
	 * @param headers
	 *            请求头
	 * @param responseBody
	 *            请求结果
	 * @return 处理后的结果
	 */
	private byte[] processExtraTask(int statusCode, Header[] headers,
			byte[] responseBody) {
		if (isSaveCookies)
			processCookiesTask(headers);// 保存cookie

		if (mContext != null)
			processPermissionTask(mContext, responseBody);

		return responseBody;
	}

	/**
	 * 处理cookie失效时的逻辑
	 * 
	 * @param context
	 * @param responseBody
	 */
	private void processPermissionTask(Context context, byte[] responseBody) {
		try {
			String result = new String(responseBody);
			MZLog.d("J", "result::"+result);
			JSONObject object = new JSONObject(result);
			int code = object.optInt("code");
			if (code == 3 || code == 4) {
				Toast.makeText(context,
						context.getString(R.string.permission_denied),
						Toast.LENGTH_LONG).show();
				if ( context instanceof Activity ) {
					Activity activity = (Activity)context;
					Intent it = new Intent(activity, LoginActivity.class);
					it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					activity.startActivity(it);
				} else {
					Intent it = new Intent(context, LoginActivity.class);
					it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(it);
				}
				
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 保存临时的cookie数据
	 * 
	 * @param headers
	 */
	public void processCookiesTask(Header[] headers) {
		/*
		 *  cookie 内容
		 *  Set-Cookie:_applogin_=V2SBPEWU76IJM4AMB2LGSPL4FET23PNGAQBORQRS4HLW
		 *  VF6IERMXMP6ZU36K5XCIUCJZGAV7VO4LOUMNUE55GU3ZUUD5LM;Domain=.jd.com; Path=/
		 */
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < headers.length; i++) {
			String valueString = headers[i].getValue();
			if (headers[i].getName().equals("set-cookie")
					|| headers[i].getName().equals("Set-Cookie")) {
				if (i < headers.length - 1) {

					builder.append(valueString).append(",");

				} else {
					builder.append(valueString);
				}
			}
		}
		String cookie = builder.toString();
		if (!TextUtils.isEmpty(cookie))
			WebRequestHelper.setCookies(cookie);
		MZLog.d("cookie", cookie);
	}

	@Override
	public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
		byte[] result = processExtraTask(statusCode, headers, responseBody);
		onResponse(statusCode, headers, result);
	}

	/**
	 * 对请求拦截后接收返回的数据
	 * 
	 * @author WANGGUODONG
	 * 
	 * @param statusCode
	 *            状态码
	 * @param headers
	 *            请求头
	 * @param responseBody
	 *            请求结果
	 */
	public abstract void onResponse(int statusCode, Header[] headers,
			byte[] responseBody);

	public boolean isSaveCookies() {
		return isSaveCookies;
	}

	public void setSaveCookies(boolean isSaveCookies) {
		this.isSaveCookies = isSaveCookies;
	}

	@Override
	public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProgress(int bytesWritten, int totalSize) {
		// TODO Auto-generated method stub
		//super.onProgress(bytesWritten, totalSize);
	}
	
	

}