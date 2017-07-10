package com.jingdong.app.reader.pay;

import java.util.List;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.MyActivity;
import com.jingdong.app.reader.bob.util.ShowTools;
import com.jingdong.app.reader.bob.util.WebViewBridge;
import com.jingdong.app.reader.opentask.InterfaceBroadcastReceiver;
import com.jingdong.app.reader.opentask.InterfaceBroadcastReceiver.Command;
import com.jingdong.app.reader.util.AndroidVersion;
import com.jingdong.app.reader.util.CommonUtil;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.TalkingDataUtil;
import com.jingdong.app.reader.util.WebViewHelper;

public class OnlinePayActivity extends MyActivity {
	public static List<String> payidList = null;

	private WebView mWebView;

	private String url;
	private View network_retry_layout;

	public static int FromBookInfor = 0;
	public static int FromBookPurchasedActivity = 1;
	public static int FromJDSpecialColumnActivity = 2;
	public static int FromJDMyDetail4OrderActivity = 3;
	public static int FromJDMyOrderFormActivity = 4;
	public static int FromEShoppingCarActivity = 5;
	public static final int FromOrderListActivity = 6;
	private static int sFromActivity = FromBookPurchasedActivity;
	private WebViewBridge webViewBridge;
	boolean isShowDefaultEffect = true;
	private String from;

	private static MyActivity sInstance;

	public static MyActivity getInstance() {
		return sInstance;
	}

	public static void setInstance(MyActivity instance) {
		sInstance = instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		sInstance = this;
		setContentView(R.layout.activity_pay_ebook);
		mWebView = (WebView) findViewById(R.id.pay_ebook_webview);
		network_retry_layout = findViewById(R.id.include2);

		sFromActivity = getIntent().getIntExtra("key",
				FromBookPurchasedActivity);
		network_retry_layout.setVisibility(View.GONE);
		((TextView) network_retry_layout.findViewById(R.id.error_title))
				.setText(getString(R.string.network_error));
		Intent intent = getIntent();

		url = intent.getStringExtra("url");

		isShowDefaultEffect = intent.getBooleanExtra("isShowDefaultEffect",
				true);
		webViewBridge = new WebViewBridge(this,null);
		initWebView();
		network_retry_layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				network_retry_layout.setOnClickListener(null);
				finish();
			}
		});

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// mWebView.loadUrl("file:///android_asset/demo.html");
				mWebView.loadUrl(url);
			}

		};

		if (!beforeLoadUrl(url, mWebView)) {
			post(runnable);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jingdong.app.reader.util.MyActivity#onStop()
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.jingdong.app.reader.util.MyActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// finish();
		sInstance = null;
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mWebView != null) {
			if (!AndroidVersion.hasICE_CREAM_SANDWICH_MR1()) {
				WebViewHelper.enablePlatformNotifications();
			}

			// WebView.enablePlatformNotifications();
		}
	}

	@Override
	protected void onStop() {
		// OnlinePayActivity.this.finish();
		super.onStop();
		if (mWebView != null) {
			if (!AndroidVersion.hasICE_CREAM_SANDWICH_MR1()) {
				WebViewHelper.disablePlatformNotifications();

			}
			// WebView.disablePlatformNotifications();
		}
	}

	@SuppressWarnings("deprecation")
	private void initWebView() {
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.requestFocus();// 使页面获得焦点
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);// 不显示滚动条
		/*** 打开本地缓存提供JS调用 **/
		mWebView.getSettings().setDomStorageEnabled(true);
		// Set cache size to 8 mb by default. should be more than enough
		mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
		// This next one is crazy. It's the DEFAULT location for your app's
		// cache
		// But it didn't work for me without this line.
		// UPDATE: no hardcoded path. Thanks to Kevin Hawkins
		String appCachePath = getApplicationContext().getCacheDir()
				.getAbsolutePath();
		mWebView.getSettings().setAppCachePath(appCachePath);
		mWebView.getSettings().setAllowFileAccess(true);
		mWebView.getSettings().setAppCacheEnabled(true);
		// 缩放把柄
		mWebView.getSettings().setBuiltInZoomControls(true);
		if ( android.os.Build.VERSION.SDK_INT >= 17 ) {
			mWebView.addJavascriptInterface(webViewBridge,
					WebViewBridge.interfaceNameString);
		}
		mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
		mWebView.setWebChromeClient(new WebChromeClient() {

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				return super.onJsAlert(view, url, message, result);
			}

		});
		mWebView.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageFinished(WebView view, String url) {
				if (mWebView.getVisibility() == View.INVISIBLE) {
					// scroll.setVisibility(View.GONE);
					mWebView.setVisibility(View.VISIBLE);
					mWebView.setFocusable(true);
					mWebView.setFocusableInTouchMode(true);
				}
				// Message msgover = mHandler.obtainMessage(1);
				// mHandler.sendMessage(msgover);

				// mWebView.setVisibility(View.VISIBLE);
				TalkingDataUtil.onBookDetailEvent(OnlinePayActivity.this,
						"确认支付_去支付_PV");
				super.onPageFinished(view, url);
			}

			/**
			 * （非 Javadoc） 下午5:50:20
			 * 
			 * 
			 * @see android.webkit.WebViewClient#onReceivedSslError(android.webkit.WebView,
			 *      android.webkit.SslErrorHandler, android.net.http.SslError)
			 */

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				handler.proceed();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * android.webkit.WebViewClient#onReceivedError(android.webkit.WebView
			 * , int, java.lang.String, java.lang.String)
			 */
			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				ShowTools.toastInThread("网络异常");
				network_retry_layout.setVisibility(View.VISIBLE);
				mWebView.setVisibility(View.GONE);
				super.onReceivedError(view, errorCode, description, failingUrl);
				if (mWebView != null) {
					if (!AndroidVersion.hasICE_CREAM_SANDWICH_MR1()) {
						WebViewHelper.enablePlatformNotifications();

					}
					// WebView.enablePlatformNotifications();
				}
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				if (Log.D) {
					Log.d("Temp", "onPageStarted() -->> " + url);
				}
				if (clientListener != null) {
					if (clientListener.onPageStarted(view, url, favicon)) {
						return;
					}
				}

				// Message msgMessage = mHandler.obtainMessage(0);
				// mHandler.sendMessage(msgMessage);
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				// if(Log.D){
				// Log.d("Temp", " shouldOverrideUrlLoading url-->> " + url);
				// }
				// Log.i("xiawei", url);
				if (clientListener != null) {
					if (clientListener.shouldOverrideUrlLoading(view, url)) {
						return true;
					}
				}

				Uri uri = Uri.parse(url);
				MZLog.d("JD_Reader", "shouldOverrideUrlLoading::" + url);
				if (uri.getScheme().equals("openapp.jdebook")) {

					uri = Uri.parse(url.replace("openapp.jdebook",
							"openApp.jdebook"));
				}
				if (uri.getScheme().equals("openapp.mobile")) {

					uri = Uri.parse(url.replace("openapp.mobile",
							"openApp.jdebook"));
				}

				Intent i = new Intent(Intent.ACTION_VIEW, uri);

				String scheme = uri.getScheme();
				i.addCategory(Intent.CATEGORY_BROWSABLE);
				i.addCategory(Intent.CATEGORY_DEFAULT);
				i.setPackage("com.android.browser");
				if (!scheme.equalsIgnoreCase("http")
						&& !scheme.equalsIgnoreCase("https")) {
					if (!CommonUtil.isIntentAvailable(i)) {// 如果google浏览器无法处理，交给系统处理

						// i.setPackage(null);
						// startActivity(i);
						Intent intent = new Intent(
								InterfaceBroadcastReceiver.ACTION);
						Command command = Command.createCommand(i);
						Bundle bundle = command.getBundle();
						intent.putExtras(bundle);
						sendBroadcast(intent);
						return true;
					}
				}
				return super.shouldOverrideUrlLoading(view, url);
			}

		});
	}

	public static int getFromActivity() {
		return sFromActivity;
	}

	public static void setFromActivity(int fromActivity) {
		sFromActivity = fromActivity;
	}

	WebViewClientListener clientListener = null;

	public interface WebViewClientListener {
		public boolean shouldOverrideUrlLoading(WebView view, String url);

		public boolean onPageStarted(WebView view, String url, Bitmap favicon);
	}

	public WebViewClientListener getClientListener() {
		return clientListener;
	}

	public void setClientListener(WebViewClientListener clientListener) {
		this.clientListener = clientListener;
	}

	public WebView getWebView() {
		return mWebView;
	}

	public boolean beforeLoadUrl(String url, WebView view) {
		return false;
	}

	public void toAndroidBrowser(Uri uri) {
		Intent i = new Intent(Intent.ACTION_VIEW, uri);
		i.addCategory(Intent.CATEGORY_BROWSABLE);
		i.addCategory(Intent.CATEGORY_DEFAULT);
		i.setPackage("com.android.browser");
		if (CommonUtil.isIntentAvailable(i)) {
			try {
				startActivity(i);
			} catch (Exception e) {
			}

		} else {
			i.setPackage(null);
			try {
				startActivity(i);
			} catch (Exception e) {
			}

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!isShowDefaultEffect) {
			if (keyCode == KeyEvent.KEYCODE_BACK) {
				if (mWebView != null && mWebView.canGoBack()) {
					mWebView.goBack();
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
