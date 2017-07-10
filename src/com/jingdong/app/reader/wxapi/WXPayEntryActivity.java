package com.jingdong.app.reader.wxapi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookInfoNewUIActivity;
import com.jingdong.app.reader.activity.MyActivity;
import com.jingdong.app.reader.bob.util.WebViewBridge;
import com.jingdong.app.reader.entity.extra.JDBookInfo;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.CommonUtil;
import com.jingdong.app.reader.util.GsonUtils;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.dialog.DialogManager;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends MyActivity implements IWXAPIEventHandler {

	private IWXAPI api;
	private static final String WXAPP_ID = "wx79f9198071040f23";
	private WebView mWebView;
	private WebViewBridge mWebViewBridge;
	private EmptyLayout mEmptyLayout;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MZLog.i("J.Beyond", "WXPayEntryActivity#onCreate");
		setContentView(R.layout.activity_webview);
		api = WXAPIFactory.createWXAPI(this, WXAPP_ID, false);
		api.handleIntent(getIntent(), this);
		
	}

	private void initWebView() {
		mWebViewBridge = new WebViewBridge(this,null);
		mWebView = (WebView) findViewById(R.id.webView1);
		mWebView.getSettings().setJavaScriptEnabled(true);
		/*** 打开本地缓存提供JS调用 **/
		mWebView.getSettings().setDomStorageEnabled(true);
		// Set cache size to 8 mb by default. should be more than enough
		mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
		// This next one is crazy. It's the DEFAULT location for your app's
		// cache
		// But it didn't work for me without this line.
		// UPDATE: no hardcoded path. Thanks to Kevin Hawkins
		String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
		mWebView.getSettings().setAppCachePath(appCachePath);
		mWebView.getSettings().setAllowFileAccess(true);
		mWebView.getSettings().setAppCacheEnabled(true);
		mEmptyLayout = (EmptyLayout) findViewById(R.id.error_layout);
		mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
		if ( android.os.Build.VERSION.SDK_INT >= 17 ) {
			mWebView.addJavascriptInterface(mWebViewBridge, WebViewBridge.interfaceNameString);
		}
		
		mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onLoadResource(WebView view, String url) {
				/*
				 * if (url.contains("ishare.iask.sina.com.cn")) {
				 * DetectEpubUrlTask task = new DetectEpubUrlTask();
				 * task.execute(url); }
				 */
				super.onLoadResource(view, url);
			}

			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
				return super.shouldInterceptRequest(view, url);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				String cookie = CookieManager.getInstance().getCookie(url);
				if (cookie != null)
					// MZLog.d(activityTag, cookie);
					super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				if (mEmptyLayout != null) {
					WXPayEntryActivity.this.runOnUiThread(new Runnable() {
						public void run() {
							mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
						}
					});
				}
			}
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResp(BaseResp resp) {
		Log.d("WXAPI", "onPayFinish, errCode = " + resp.errCode+",resp.getType()="+resp.getType());
//		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			
			Log.d("WXAPI", "pay success!");
			initWebView();
			// String payResultUrl =
			// "http://order.e.jd.com/ebookOrder_newOnlinePaySucess.action?client=android&isSupportJs=true&code=1&info=weixin_success&payId=123556&&orders=[{\"orderId\":\"\",\"orderPrice\":\"\"}]";
			// if (mWebView!= null) {
			// MZLog.i("J.Beyond", "loadUrl:"+payResultUrl);
			// mWebView.loadUrl(payResultUrl);
			// }
			String errormsg = "";
			boolean isError = true;
			switch (resp.errCode) {

			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				errormsg = "error:auth denied";
				break;
			case BaseResp.ErrCode.ERR_COMM:
				errormsg = "error:common";
				break;
			case BaseResp.ErrCode.ERR_OK:
				errormsg = "正确返回";
				isError = false;
				break;
			case BaseResp.ErrCode.ERR_SENT_FAILED:
				errormsg = "error:sent failed";
				break;
			case BaseResp.ErrCode.ERR_UNSUPPORT:
				errormsg = "error:unsupport";
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				errormsg = "error:user cancel";
				// 用户取消不应该显示支付错误界面
				break;
			}
			
			
			String payId = CommonUtil.weixinPayId;
			//测试
			CommonUtil.writeFile("jdreader_log.txt", "微信支付log--payId="+payId+" errormsg"+errormsg);
			
			final String payResultErrorUrl = "http://order.e.jd.com/ebookOrder_newOnlinePaySucess.action?client=android&isSupportJs=true&code=1&info=weixin_" + errormsg
					+ "&payId=123556&&orders=[{\"orderId\":\"\",\"orderPrice\":\"\"}]";
			if (mWebView == null) 
				return ;
			
			if (isError || (payId ==null || payId.equals(""))) {
				mWebView.loadUrl(payResultErrorUrl);
			} else {
				WebRequestHelper.get(URLText.JD_PAY_URL, RequestParamsPool.getWXPaySuccessUrlParams(payId), true,
					new MyAsyncHttpResponseHandler(WXPayEntryActivity.this) {

						@Override
						public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
						}

						@Override
						public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

							String result = new String(responseBody);

							try {
								JSONObject json = new JSONObject(result);
								String codeStr = json.optString("code");
								if (codeStr != null && codeStr.equals("0")) {
									String url = json.optString("data");
									if (url != null && !url.equals("")) {
										mWebView.loadUrl(url);
									}
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}

						}
					});

		}
	}
}