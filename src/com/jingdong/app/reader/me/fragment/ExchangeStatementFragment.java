package com.jingdong.app.reader.me.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.me.activity.IntegrationActivity;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.view.EmptyLayout;
import com.jingdong.app.reader.view.TopBarView;
import com.jingdong.app.reader.view.TopBarView.TopBarViewListener;

public class ExchangeStatementFragment extends BackHandledFragment implements TopBarViewListener {

	private static final int SHOW_LOADING = 0x01;
	private static final int SHOW_NORMAL = 0x02;
	private static final int SHOW_NET_ERROR = 0x03;

	private FragmentManager mFragmentMgr;
	private TopBarView topBarView;
	private WebView mWebView;
	private Activity mActivity;
	private EmptyLayout mEmptyLayout;
	private static final String EXCHANGE_URL = "http://e.m.jd.com/jifenshuoming.html";
//	private View mLoadingProgress;
//	private View mNetErrorView;
//	private Button mRetryButton;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.mActivity = activity;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.integration_exchange_statement_fragment, null);
		initView(view);
		return view;
	}

	private void initView(View view) {
		mFragmentMgr = getFragmentManager();
		topBarView = (TopBarView) view.findViewById(R.id.exchange_statement_topbar);
		initTopbarView();
		mWebView = (WebView) view.findViewById(R.id.exchange_statement_webview);
		String appCachePath = mActivity.getApplicationContext().getCacheDir().getAbsolutePath();
		mWebView.getSettings().setAppCachePath(appCachePath);
		mWebView.getSettings().setAllowFileAccess(true);
		mWebView.getSettings().setAppCacheEnabled(true);
		mWebView.getSettings().setJavaScriptEnabled(true);
		/*** 打开本地缓存提供JS调用 **/
		mWebView.getSettings().setDomStorageEnabled(true);
		// Set cache size to 8 mb by default. should be more than enough
		mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
		mWebView.removeJavascriptInterface("searchBoxJavaBridge_");
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				mEmptyLayout.setErrorType(EmptyLayout.NETWORK_LOADING);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				mEmptyLayout.setErrorType(EmptyLayout.HIDE_LAYOUT);
			}
		});

//		mLoadingProgress = view.findViewById(R.id.screen_loading);
//		mNetErrorView = view.findViewById(R.id.net_error);
//		mRetryButton = (Button) mNetErrorView.findViewById(R.id.neterror_retry_btn);
//		mRetryButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if (NetWorkUtils.isNetworkConnected(mActivity)) {
//					mWebView.loadUrl(EXCHANGE_URL);
//				} else {
//					switchContentView(SHOW_NET_ERROR);
//				}
//			}
//		});
		mEmptyLayout = (EmptyLayout) view.findViewById(R.id.error_layout);
		mEmptyLayout.setOnLayoutClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (NetWorkUtils.isNetworkConnected(mActivity)) {
					mWebView.loadUrl(EXCHANGE_URL);
				} else {
					mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
				}
			}
		});
		
		if (NetWorkUtils.isNetworkConnected(mActivity)) {
			mWebView.loadUrl(EXCHANGE_URL);
		} else {
			mEmptyLayout.setErrorType(EmptyLayout.NETWORK_ERROR);
		}
	}

//	private void switchContentView(int contentViewType) {
//		switch (contentViewType) {
//		case SHOW_LOADING:
//			mWebView.setVisibility(View.GONE);
//			mLoadingProgress.setVisibility(View.VISIBLE);
//			mNetErrorView.setVisibility(View.GONE);
//			break;
//
//		case SHOW_NORMAL:
//			mWebView.setVisibility(View.VISIBLE);
//			mLoadingProgress.setVisibility(View.GONE);
//			mNetErrorView.setVisibility(View.GONE);
//			break;
//
//		case SHOW_NET_ERROR:
//			mWebView.setVisibility(View.GONE);
//			mLoadingProgress.setVisibility(View.GONE);
//			mNetErrorView.setVisibility(View.VISIBLE);
//			break;
//		}
//	}

	private void initTopbarView() {
		if (topBarView == null)
			return;
		topBarView.setLeftMenuVisiable(true, R.drawable.tabbar_back);
		topBarView.setListener(this);
		topBarView.setTitle("兑换说明");
	}

	@Override
	public void onLeftMenuClick() {
		// TODO Auto-generated method stub
		if (mActivity != null) {
			((IntegrationActivity) mActivity).onBackPressed();
		}
	}

	@Override
	public void onRightMenuOneClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRightMenuTwoClick() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onCenterMenuItemClick(int position) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onBackPressed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
		mWebView.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mWebView.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		ViewGroup vg = (ViewGroup) mWebView.getParent();
		if (vg != null) {
			vg.removeView(mWebView);
		}
		mWebView.removeAllViews();
		mWebView.destroy();
	}

}
