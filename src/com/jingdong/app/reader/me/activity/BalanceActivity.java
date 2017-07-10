package com.jingdong.app.reader.me.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.common.MZReadCommonActivity;

public class BalanceActivity extends Activity{
	private WebView webview;  
	private String url = null;
	private ProgressBar progress;
	
	 @Override 
	    public void onCreate(Bundle savedInstanceState) {  
	        super.onCreate(savedInstanceState);  
	        setContentView(R.layout.forget_password);
	        webview = (WebView) findViewById(R.id.webview);
	        progress=(ProgressBar) findViewById(R.id.progress);
	        
	        //设置WebView属性，能够执行Javascript脚本  
	        webview.getSettings().setJavaScriptEnabled(true);  
	        url = "https://passport.m.jd.com/findloginpassword/fillAccountName.action"; 
	        //加载需要显示的网页  
	        webview.loadUrl(url);  
	        //设置Web视图  
	        webview.setWebViewClient(new HelloWebViewClient ());  
	        webview.removeJavascriptInterface("searchBoxJavaBridge_");
	    }  
	      
	    
	    //Web视图  
	    private class HelloWebViewClient extends WebViewClient {  
	        @Override 
	        public boolean shouldOverrideUrlLoading(WebView view, String url) {  
	            view.loadUrl(url);  
	            return true;  
	        }

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				progress.setVisibility(0);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				progress.setVisibility(8);
			}  
	    }

}
