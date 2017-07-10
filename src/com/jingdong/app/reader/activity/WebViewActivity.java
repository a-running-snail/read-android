package com.jingdong.app.reader.activity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.baidu.mobstat.StatService;
import com.iflytek.cloud.TextUnderstander;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.bob.util.WebViewBridge;
import com.jingdong.app.reader.common.BaseActivityWithTopBar;
import com.jingdong.app.reader.myInterface.WebviewLoadInterface;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.service.OpdsBookDownloadService;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.StatisticsReportUtil;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.TopBarView;
import com.loopj.android.http.RequestParams;

public class WebViewActivity extends BaseActivityWithTopBar {

	public class DetectEpubUrlTask extends AsyncTask<String, Void, String[]> {

		@Override
		protected String[] doInBackground(String... params) {
			String urlText = params[0];
			if (!TextUtils.isEmpty(urlText)) {
				try {
					URL url = new URL(urlText);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					Map<String, List<String>> map = conn.getHeaderFields();
					if (map == null) {
						return null;
					}
					List<String> head = map.get("Content-Disposition");
					if (head != null) {
						for (String h : head) {
							if (!TextUtils.isEmpty(h) && h.toLowerCase(Locale.getDefault()).contains("epub")) {
								h = new String(h.getBytes("iso8859-1"), "utf-8");
								String fileName = URLUtil.guessFileName(urlText, URLDecoder.decode(h, "GB2312"), null);
								if (!TextUtils.isEmpty(fileName)) {
									return new String[] { urlText, fileName };
								}
							}
						}
					}

					// conn.getheaders("Content-Disposition");
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(String[] result) {
			super.onPostExecute(result);
			if (result != null) {
				String urlText = result[0];
				String fileName = result[1];
				if (fileName.toLowerCase(Locale.getDefault()).endsWith("epub")) {
					Intent intent = new Intent(WebViewActivity.this, OpdsBookDownloadService.class);
					intent.putExtra(OpdsBookDownloadService.OpdsBookUrlPathKey, urlText);
					intent.putExtra(OpdsBookDownloadService.OpdsBookNameKey, fileName);
					startService(intent);
					Toast.makeText(WebViewActivity.this, getString(R.string.start_download) + "\"" + fileName + "\"" + getString(R.string.to_3rd_bookcase),
							Toast.LENGTH_LONG).show();
				}
			}
		}

	}

	public static final String TYPE_SENDBOOK = "sendbook";
	public static final int SEND_BOOK_LOGIN = 125;
	public static final String TYPE_ONE_CLICK_BUY = "oneClickBuy";
	public static final int ONE_CLICK_BUY_LOGIN = 126;
	
	
	public static final String UrlKey = "UrlKey";
	public static final String TopbarKey = "TopbarKey";
	public static final String BrowserKey = "BrowserKey";
	public static final String BackKey = "BackKey";
	public static final String TitleKey = "TitleKey";
	public static final String TypeKey = "TypeKey";
	private boolean needTopbar;
	private boolean needBrowserOpen;
	private boolean needBackPage;
	public WebView webView;
	private String mergeurl = "";
	private WebViewBridge webViewBridge;
	private EmptyLayout mEmptyLayout;
	private String title;
	private TopBarView topbar;
	private String type;//活动类型

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		CookieSyncManager.createInstance(this);
		CookieManager.getInstance().setAcceptCookie(true);
		setContentView(R.layout.activity_webview);
		topbar = getTopBarView();

		String urlText = getIntent().getStringExtra(UrlKey);
		// String urlText = "http://e.m.jd.com/yiyuan.html";
		needTopbar = getIntent().getBooleanExtra(TopbarKey, true);
		needBrowserOpen = getIntent().getBooleanExtra(BrowserKey, true);
		needBackPage = getIntent().getBooleanExtra(BackKey, false);
		title = getIntent().getStringExtra(TitleKey);
		
		type = getIntent().getStringExtra(TypeKey);
		
		// final String urlText =
		// "http://pan.baidu.com/share/link?shareid=2674785736&uk=1177107085";
		// final String urlText =
		// "http://ishare.iask.sina.com.cn/f/33411872.html";

		webView = (WebView) findViewById(R.id.webView1);
		webView.getSettings().setJavaScriptEnabled(true);
		/*** 打开本地缓存提供JS调用 **/
		webView.getSettings().setDomStorageEnabled(true);
		// Set cache size to 8 mb by default. should be more than enough
		webView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
		// This next one is crazy. It's the DEFAULT location for your app's
		// cache
		// But it didn't work for me without this line.
		// UPDATE: no hardcoded path. Thanks to Kevin Hawkins
		String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
		webView.getSettings().setAppCachePath(appCachePath);
		webView.getSettings().setAllowFileAccess(true);
		webView.getSettings().setAppCacheEnabled(true);
		// 自定义UserAgent
		String version = StatisticsReportUtil.getSoftwareVersionName();
		String build = StatisticsReportUtil.getSoftwareBuildName();
		String osVersion = URLEncoder.encode(Build.VERSION.RELEASE);
		// JDRead 3.1.0 rv:0515 (iPhone; iPhone OS 8.3; zh_CN)
		String myUserAgentString = "JDRead " + version + " rv:" + build + " (android; android OS " + osVersion + "; zh_CN)";
		webView.getSettings().setUserAgentString(myUserAgentString);
		
		//词典调用百度百科时，单独设置useragetnt，使之能支持H5属性
		if(!TextUtils.isEmpty(type) && type.equals("baike"))
			webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; U; Android 4.4.4; zh-cn; MI 3 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/42.0.0.0 Mobile Safari/537.36 XiaoMi/MiuiBrowser/2.1.1"+myUserAgentString);
		
		webViewBridge = new WebViewBridge(this,new WebviewLoadInterface() {
			
			@Override
			public void loadWebviewUrl(String url) {
				webView.loadUrl(url);
			}
		});
		
		if ( android.os.Build.VERSION.SDK_INT >= 17 ) {
			webView.addJavascriptInterface(webViewBridge, WebViewBridge.interfaceNameString);
		}
		
		webView.removeJavascriptInterface("searchBoxJavaBridge_");
		mEmptyLayout = (EmptyLayout) findViewById(R.id.error_layout);
		mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
				webView.loadUrl(mergeurl);
			}
		});

		webView.setDownloadListener(new DownloadListener() {

			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
				String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
				if (!TextUtils.isEmpty(fileName) && fileName.toLowerCase(Locale.getDefault()).endsWith("epub")) {
					Intent intent = new Intent(WebViewActivity.this, OpdsBookDownloadService.class);
					intent.putExtra(OpdsBookDownloadService.OpdsBookUrlPathKey, url);
					intent.putExtra(OpdsBookDownloadService.OpdsBookNameKey, fileName);
					startService(intent);
					Toast.makeText(WebViewActivity.this, getString(R.string.start_download) + "\"" + fileName + "\"" + getString(R.string.to_3rd_bookcase),
							Toast.LENGTH_LONG).show();
				}else{
					 Uri uri = Uri.parse(url);
			         Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			         startActivity(intent);
				}
			}

		});

		webView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				String cookie = CookieManager.getInstance().getCookie(url);
				if (cookie != null)
					MZLog.d(activityTag, cookie);
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
			}
		});

		if(type!=null){
			if(type.equals(TYPE_SENDBOOK))
				webViewBridge.requestCode = SEND_BOOK_LOGIN;
			else if(type.equals(TYPE_SENDBOOK))
				webViewBridge.requestCode = ONE_CLICK_BUY_LOGIN;
		}
		
		
		mergeurl = urlText;
		if (!TextUtils.isEmpty(urlText) && (urlText.contains("m.jd.com") || urlText.contains("m.360buy.com"))) {

			//旧的打通登录接口
//			WebRequestHelper.get(URLText.JD_WEBVIEW__URL, RequestParamsPool.getUnionLoginParams(urlText), true, new MyAsyncHttpResponseHandler(
//					WebViewActivity.this) {
//
//				@Override
//				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
//					mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
//				}
//
//				@Override
//				public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
//					try {
//						String result = new String(responseBody);
//						JSONObject ret = new JSONObject(result);
//						if (ret != null && ret.getString("code").equals("0")) {
//							String token = ret.getString("tokenKey");
//							String url = ret.getString("url");
//							mergeurl = url + "?tokenKey=" + token + "&to=" + mergeurl;
//						}
//						MZLog.d("wangguodong", "mergeurl === " + mergeurl);
//					} catch (JSONException e) {
//						MZLog.d("wangguodong", e.getMessage());
//						e.printStackTrace();
//					}
//
//					mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
//					webView.loadUrl(mergeurl);
//				}
//
//			});
			
			//TODO M站登录打通接口测试接口
			WebRequestHelper.get(URLText.JD_BASE_URL, RequestParamsPool.getMLoginParams(), true, new MyAsyncHttpResponseHandler(
					WebViewActivity.this) {

				@Override
				public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
				}

				@Override
				public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {
					try {
						String result = new String(responseBody);
						JSONObject ret = new JSONObject(result);
						if (ret != null && ret.optString("code").equals("0")) {
							String token = ret.getString("tokenKey");
							mergeurl = mergeurl + "?tokenKey=" + token;
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}

					mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
					webView.loadUrl(mergeurl);
				}

			});
		} else {
			mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
			//若为好评送书活动
			if(mergeurl.contains("type=sendbook")){
				webViewBridge.operationSource=1;
				webViewBridge.requestCode = SEND_BOOK_LOGIN;
				type = TYPE_SENDBOOK;
				activityLogin(true,false);
			}else if(mergeurl.contains("type=oneClickBuy")){
				//一键购买活动
				webViewBridge.operationSource = 4;
				webViewBridge.requestCode = ONE_CLICK_BUY_LOGIN;
				type = TYPE_ONE_CLICK_BUY;
				activityLogin(true,false);
			}else{
				webView.loadUrl(mergeurl);
			}
		}
		if (!this.needTopbar)
			topbar.setVisibility(View.GONE);
		else {
			if (this.needBrowserOpen)
				topbar.setRightMenuOneVisiable(true, getString(R.string.open_in_browser), R.color.red_main, false);
			if (!TextUtils.isEmpty(title))
				topbar.setTitle(title);
		}
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		//好评送书活动登录后回调方法
		case WebViewActivity.SEND_BOOK_LOGIN:
			if (resultCode == GlobalVarable.REQUEST_CODE_GOTO_FIRST_PAGE) {
				if (!NetWorkUtils.isNetworkConnected(this))
					return;
				activityLogin(false,true);
			}
			break;
		case WebViewActivity.ONE_CLICK_BUY_LOGIN:
			if (resultCode == GlobalVarable.REQUEST_CODE_GOTO_FIRST_PAGE) {
				if (!NetWorkUtils.isNetworkConnected(this))
					return;
				activityLogin(false,false);
			}
			break;
		default:
			break;
		}
	}
	
	/**
	 * 各种活动打通登录方法
	 */
	private void activityLogin(final boolean haveOperation,final boolean haveParams){
		RequestParams params = null;
		if(type.equals(TYPE_SENDBOOK))
			params = RequestParamsPool.getSendBookLoginParams();
		else if(type.equals(TYPE_ONE_CLICK_BUY)){
			webViewBridge.operationSource = 4;
			params = RequestParamsPool.getOneClickBuyLoginParams();
		}
			
		WebRequestHelper.get(URLText.JD_BASE_URL, params, true,
				new MyAsyncHttpResponseHandler(this) {

					@Override
					public void onFailure(int arg0, Header[] arg1, byte[] arg2, Throwable arg3) {
					}

					@Override
					public void onResponse(int statusCode, Header[] headers, byte[] responseBody) {

						String result = new String(responseBody);
						try {
							JSONObject json = new JSONObject(result);

							String url = json.optString("url");
							title = json.optString("title");
							if(title!=null)
								topbar.setTitle(title);
							if (url != null && !"".equals(url)) {
								if(haveOperation)
									url = url + "&operationSource="+webViewBridge.operationSource;
								if(haveParams)
									url =url+webViewBridge.params;
								if (webView != null)
									webView.loadUrl(url);
								
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}
	
	@Override
	public void onRightMenuOneClick() {

		Uri uri = Uri.parse(mergeurl);
		if (uri != null && !uri.isRelative()) {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(browserIntent);
		}
	}

	@Override
	public void onLeftMenuClick() {
		onBackPressed();
	}

	@Override
	public void onBackPressed() {
		if (webView.canGoBack()) {
			webView.goBack();
		} else {
			if (getIntent() != null
					&& (getIntent().getIntExtra("LX", 0) == 5 || getIntent().getIntExtra("lx", 0) == 5 || getIntent().getIntExtra("LX", 0) == 6 || getIntent()
							.getIntExtra("lx", 0) == 6)) {
				Intent intent = new Intent(this, LauncherActivity.class);
				intent.putExtra("LX", 1);
				intent.putExtra("lx", 1);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
				finish();
			} else {
				super.onBackPressed();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		webView.onResume();
		StatService.onPageStart(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_webview) + title);
	}

	@Override
	protected void onPause() {
		super.onPause();
		webView.onPause();
		StatService.onPageEnd(this, MZBookApplication.getInstance().getString(R.string.mtj_bookstore_webview) + title);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ViewGroup vg = (ViewGroup) webView.getParent();
		if (vg != null) {
			vg.removeView(webView);
		}
		webView.removeAllViews();
		webView.destroy();
	}

}
