package com.jingdong.app.reader.net;

import java.net.URLEncoder;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.StatisticsReportUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

/**
 * AsyncHttpClient 请求类
 * 
 * @author WANGGUODONG
 * @see 使用文档参考 :http://loopj.com/android-async-http/
 */

public class WebRequestHelper {

	public static final String CHAR_SET = "UTF-8";
	public static final String AUTH_TOKEN = "auth_token";

	private static String cookies;
	private static AsyncHttpClient asyncclient = new AsyncHttpClient();
	private static SyncHttpClient syncclient = new SyncHttpClient();
	private static String version =  StatisticsReportUtil.getSoftwareVersionName();
	private static String build = StatisticsReportUtil.getSoftwareBuildName();
	private static String osVersion = URLEncoder.encode(Build.VERSION.RELEASE);
	
	

	public static void deletes(Context context,String url,RequestParams params,
			boolean addCookies, AsyncHttpResponseHandler responseHandler) {
		
        //清空headers
		removeAllHeaders();
		//添加cookies
		if (!TextUtils.isEmpty(cookies)) {
			syncclient.addHeader("Cookie", cookies);
			syncclient.delete(context,url,null,params, responseHandler);
			
		}
		else {
			Log.d("wangguodong", "cookies not exists!");
			syncclient.post(url, responseHandler);
		}
		Log.d("network", "#######request url(post) is " + url);

	}
	
	/**
	 * 异步POST请求 携带参数并带上cookie
	 * 
	 * @param url
	 * @param params
	 * @param responseHandler
	 * 请使用MyAsyncHttpResponseHandler 对象实例 方便请求控制
	 */
	public static void posts(String url, RequestParams params,
			boolean addCookies, AsyncHttpResponseHandler responseHandler) {
		
        //清空headers
		removeAllHeaders();;
		//添加cookies
		if (!TextUtils.isEmpty(cookies)) {
			syncclient.addHeader("Cookie", cookies);
			syncclient.post(url, params, responseHandler);
		}
		else {
			Log.d("wangguodong", "cookies not exists!");
			syncclient.post(url, params, responseHandler);
		}
		Log.d("network", "#######request url(post) is " + url);
		Log.d("network", "#######request params is " + params.toString());

	}
	
	/**
	 * 异步GET请求 携带参数
	 * 
	 * @param url
	 * @param params
	 * @param responseHandler
	 *            请使用MyAsyncHttpResponseHandler 对象实例 方便请求控制
	 */
	public static void gets(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		removeAllHeaders();
		syncclient.get(url, params, responseHandler);
		Log.d("network", "#######request url(get) is " + url);
		Log.d("network", "#######request params is " + params.toString());
	}

	/**
	 * 异步GET请求 携带参数并带上cookie
	 * 
	 * @param url
	 * @param params
	 * @param responseHandler
	 *            请使用MyAsyncHttpResponseHandler 对象实例 方便请求控制
	 */
	public static void gets(String url, RequestParams params,
			boolean addCookies, AsyncHttpResponseHandler responseHandler) {
		
        //清空headers
		removeAllHeaders();
		//添加cookies
		if (!TextUtils.isEmpty(cookies)) {
			syncclient.addHeader("Cookie", cookies);
			syncclient.get(url, params, responseHandler);
		}
		else {
			MZLog.d("wangguodong", "cookies not exists!");
			syncclient.get(url, params, responseHandler);
		}
		Log.d("network", "#######request url(get) is " + url);
		Log.d("network", "#######request params is " + params.toString());
		Log.d("network", "#######cookie is " + cookies);
	}
	
	/**
	 * 异步GET请求 携带参数
	 * 
	 * @param url
	 * @param params
	 * @param responseHandler
	 *            请使用MyAsyncHttpResponseHandler 对象实例 方便请求控制
	 */
	public static void get(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		setTimeout();
		removeAllHeaders();
		asyncclient.get(url, params, responseHandler);
		Log.d("network", "#######request url(get) is " + url);

		Log.d("network", "#######request params is " + params.toString());
	}
	
	/**
	 * 异步GET请求 携带参数并带上cookie
	 * 
	 * @param url
	 * @param params
	 * @param responseHandler
	 *            请使用MyAsyncHttpResponseHandler 对象实例 方便请求控制
	 */
	public static void get(String url, RequestParams params,
			boolean addCookies, AsyncHttpResponseHandler responseHandler) {
		
        //清空headers
		setTimeout();
		removeAllHeaders();;
		//添加cookies
		if (!TextUtils.isEmpty(cookies)) {
			asyncclient.addHeader("Cookie", cookies);
			//String version =  StatisticsReportUtil.getSoftwareVersionName();
			//String build = StatisticsReportUtil.getSoftwareBuildName();
			//String osVersion = URLEncoder.encode(Build.VERSION.RELEASE);
			//asyncclient.setUserAgent("JDRead "+version+" rv:"+build+" (android; android OS "+osVersion+"; zh_CN)");
			asyncclient.get(url, params, responseHandler);
		}
		else {
			MZLog.d("wangguodong", "cookies not exists!");
			asyncclient.get(url, params, responseHandler);
		}
		Log.d("network", "#######request url(get) is " + url);
		Log.d("network", "#######request params is " + params.toString());
		Log.d("network", "#######cookie is " + cookies);
	}

	
	/**
	 * 异步GET请求 携带参数并带上cookie（带上context参数，可以取消get请求）
	 * 
	 * @param url
	 * @param params
	 * @param responseHandler
	 *            请使用MyAsyncHttpResponseHandler 对象实例 方便请求控制
	 */
	public static void getWithContext(Context context,String url, RequestParams params,
			boolean addCookies, AsyncHttpResponseHandler responseHandler) {
        //清空headers
		setTimeout();
		removeAllHeaders();;
		//添加cookies
		if (!TextUtils.isEmpty(cookies)) {
			asyncclient.addHeader("Cookie", cookies);
			asyncclient.get(context,url, params, responseHandler);
		}
		else {
			asyncclient.get(context,url, params, responseHandler);
		}
	}


	

	/**
	 * 异步POST请求 携带参数并带上cookie
	 * 
	 * @param url
	 * @param params
	 * @param responseHandler
	 * 请使用MyAsyncHttpResponseHandler 对象实例 方便请求控制
	 */
	public static void post(String url, RequestParams params,
			boolean addCookies, AsyncHttpResponseHandler responseHandler) {
		
        //清空headers
		setTimeout();
		removeAllHeaders();;
		//添加cookies
		if (!TextUtils.isEmpty(cookies)) {
			asyncclient.addHeader("Cookie", cookies);
			asyncclient.post(url, params, responseHandler);
		}
		else {
			Log.d("wangguodong", "cookies not exists!");
			asyncclient.post(url, params, responseHandler);
		}
		Log.d("network", "#######request url(post) is " + url);
		Log.d("network", "#######request params is " + params.toString());

	}

	/**
	 * 异步POST请求 携带参数
	 * 
	 * @param url
	 * @param params
	 * @param responseHandler
	 *            请使用MyAsyncHttpResponseHandler 对象实例 方便请求控制
	 */
	public static void post(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler) {
		setTimeout();
		removeAllHeaders();
		asyncclient.post(url, params, responseHandler);
		Log.d("network", "#######request ur(post)l is " + url);
		Log.d("network", "#######request params is " + params.toString());
	}

	/**
	 * 异步GET请求 未携带参数
	 * 
	 * @param url
	 * @param params
	 * @param responseHandler
	 *            请使用MyAsyncHttpResponseHandler 对象实例 方便请求控制
	 */
	public static void get(String url, AsyncHttpResponseHandler responseHandler) {
		setTimeout();
		removeAllHeaders();
		asyncclient.get(url, responseHandler);
		Log.d("network", "#######request url(get) is " + url);
	}

	/**
	 * 异步POST请求 未携带参数
	 * 
	 * @param url
	 * @param params
	 * @param responseHandler
	 *            请使用MyAsyncHttpResponseHandler 对象实例 方便请求控制
	 */
	public static void post(String url, AsyncHttpResponseHandler responseHandler) {
		setTimeout();
		removeAllHeaders();
		asyncclient.post(url, responseHandler);
		Log.d("network", "#######request url(post) is " + url);
	}

	/**
	 * 异步DELETE请求
	 * 
	 * @param url
	 * @param params
	 * @param responseHandler
	 *            请使用MyAsyncHttpResponseHandler 对象实例 方便请求控制
	 */
	public static void delete(String url,
			AsyncHttpResponseHandler responseHandler) {
		setTimeout();
		removeAllHeaders();
		asyncclient.delete(url, responseHandler);
		Log.d("network", "#######request url(delete) is " + url);
	}


	public static void delete(String url,boolean addCookies, AsyncHttpResponseHandler responseHandler) {
		
        //清空headers
		removeAllHeaders();;
		//添加cookies
		if (!TextUtils.isEmpty(cookies)) {
			asyncclient.addHeader("Cookie", cookies);
			asyncclient.delete(url, responseHandler);
			
		}
		else {
			Log.d("wangguodong", "cookies not exists!");
			asyncclient.post(url, responseHandler);
		}
		Log.d("network", "#######request url(post) is " + url);

	}
	
	
	
	
	
	/**
	 * 取消上下文相关的请求
	 * 
	 * @param url
	 * @param params
	 * @param responseHandler
	 */
	public static void cancleRequest(Context context) {
		asyncclient.cancelRequests(context, true);
	}

	/**
	 * 设置cookie 字符串
	 */
	public static void setCookies(String str) {
		cookies = str;
	}

	/**
	 * 获取cookie 字符串
	 * 
	 * @return cookie
	 */
	public static String getCookies() {
		return cookies;
	}
	
	/**
	 * 设置超时时间
	 * 
	 */
	public static void setTimeout() {
		if(NetWorkUtils.isWifiConnected(MZBookApplication.getInstance()))
			asyncclient.setTimeout(20000);
		else
			asyncclient.setTimeout(60000);
	}
	
	/**
	 * 给请求添加UserAgent
	 */
	public static void removeAllHeaders() {
		synchronized(asyncclient){
			try{
				syncclient.removeAllHeaders();
			}catch(Exception e){
				
			}
			
			try{
				asyncclient.removeAllHeaders();
			}catch(Exception e){
				
			}
			
			try{
				asyncclient.setUserAgent("JDRead "+version+" rv:"+build+" (android; android OS "+osVersion+"; zh_CN)");
			}catch(Exception e){
				
			}
			
			try{
				syncclient.setUserAgent("JDRead "+version+" rv:"+build+" (android; android OS "+osVersion+"; zh_CN)");
			}catch(Exception e){
				
			}
		}
	}

}
