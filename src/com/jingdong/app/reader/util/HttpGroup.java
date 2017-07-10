package com.jingdong.app.reader.util;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.MyActivity;
import com.jingdong.app.reader.activity.MyActivity.DestroyListener;
import com.jingdong.app.reader.activity.MyActivity.OnReleaseHttpsetingLintenser;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.client.RequestEntry;
import com.jingdong.app.reader.client.ServiceProtocol;
import com.jingdong.app.reader.config.CacheTimeConfig;
import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.entity.BodyEncodeEntity;
import com.jingdong.app.reader.entity.CacheFile;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.pay.ExceptionDrawable;
import com.jingdong.app.reader.util.FileService.Directory;
import com.jingdong.app.reader.util.thread.PooledThread;
import com.jingdong.app.reader.util.ui.DialogController;
import com.jingdong.app.reader.util.ui.view.JdOptionDialog;



public abstract class HttpGroup implements DestroyListener,MyActivity.OnReleaseHttpsetingLintenser
{

	private static int httpIdCounter = 0;
	private static String cookies;
	private static String tempCookies;//临时cookies，用从商城跳掉电子书用的。
	private static  boolean isFormSkyDriver=false;


	private static String mMd5Key;// 密钥
	private static JSONObjectProxy mModules;// 模块
	private final boolean useCaches = false;// 缓存
	private static final int connectTimeout = Integer.parseInt(Configuration
			.getProperty(Configuration.CONNECT_TIMEOUT));// 连接超时
	private static final int readTimeout = Integer.parseInt(Configuration
			.getProperty(Configuration.READ_TIMEOUT));// 读取超时
	private final static String sCharset = ServiceProtocol.sCharset;// 编码

	private static final int attempts = Integer.parseInt(Configuration
			.getProperty(Configuration.ATTEMPTS));// 尝试次数
	private static final int attemptsTime = Integer.parseInt(Configuration
			.getProperty(Configuration.ATTEMPTS_TIME));// 尝试的间隔时间

	private static final String host = Configuration.getProperty(
			Configuration.HOST).trim();

	protected ArrayList<HttpRequest> httpList = new ArrayList<HttpRequest>();// 组的任务数量
	protected HttpGroupSetting httpGroupSetting;
	protected int priority;
	protected int type;
	private final boolean reportUserInfoFlag = true;// add by zhangjp 2011.2.13
	public static boolean mustDirectConnect = false; // 是否直连,如果wap网络连接不通，测会改为直连
	private static boolean isNeedInitAgent = true;

	public static boolean isNeedInitAgent() {
		return isNeedInitAgent;
	}

	public static void setNeedinitAgent(boolean needInit) {
		isNeedInitAgent = needInit;
	}

	// 设置是否带上client的访问信息

	private static final HashMap<MyActivity, ArrayList<HttpRequest>> alertDialogStateMap = new HashMap<MyActivity, ArrayList<HttpRequest>>();

	public static void setMd5Key(String md5Key) {
		mMd5Key = md5Key;
	}

	public static void setModules(JSONObjectProxy jsonObject) {
		mModules = jsonObject;
	}
	
	public static String getTempCookies() {
		return tempCookies;
	}

	public static void setTempCookies(String tepCookies) {
		HttpGroup.tempCookies = tepCookies;
	}
	
	public static boolean isFormSkyDriver() {
		return isFormSkyDriver;
	}

	public static void setFormSkyDriver(boolean isFormSkyDriver) {
		HttpGroup.isFormSkyDriver = isFormSkyDriver;
	}
	/**
	 * 获取密钥
	 */
	public static void queryMd5Key(CompleteListener listener) {
		HttpGroupSetting setting = new HttpGroupSetting();
		setting.setPriority(HttpGroupSetting.PRIORITY_JSON);
		setting.setType(HttpGroupSetting.TYPE_JSON);
		HttpGroup httpGroup = new HttpGroup.HttpGroupaAsynPool(setting);
		queryMd5Key(httpGroup, listener);
	}

	public static HttpGroup getHttpGroup(MyActivity myActivity) {
		HttpGroupSetting setting = new HttpGroupSetting();
		setting.setMyActivity(null);
		setting.setType(HttpGroupSetting.TYPE_JSON);
		HttpGroup httpGroup = new HttpGroup.HttpGroupaAsynPool(setting);
		return httpGroup;
	}

	// public static boolean hanldAgent(DefaultHttpClient httpClient){
	// boolean isAgent = false;
	// if(!HttpGroup.mustDirectConnect){
	// final String host = Proxy.getDefaultHost();
	// final int port = Proxy.getDefaultPort();
	//
	// if (!TextUtils.isEmpty(host) && port != -1) {
	// final HttpHost proxy = new HttpHost(host, port);
	// httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
	// proxy);
	// isAgent = true;
	// }
	// }else{
	// httpClient.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
	// isAgent = false;
	// }
	// return isAgent;
	// }

	/**
	 * 获取密钥
	 */
	public static void queryMd5Key(HttpGroup httpGroup,
			final CompleteListener listener) {
		OnAllListener onAllListener = new HttpGroup.OnAllListener() {

			@Override
			public void onStart() {

			}

			@Override
			public void onEnd(HttpResponse httpResponse) {
				try {
					String md5KeyCode = httpResponse.getJSONObject()
							.getStringOrNull("key");
					if (null == md5KeyCode) {
						return;
					}
					byte[] md5KeyBytes = Base64.decode(md5KeyCode);

					for (int i = 0, byteLength = md5KeyBytes.length; i < byteLength; i++) {
						md5KeyBytes[i] = (byte) ~md5KeyBytes[i];
					}
					String md5Key = new String(md5KeyBytes);
					if (Log.D) {
						Log.d("HttpGroup", "md5Key -->> " + md5Key);
					}
					HttpGroup.setMd5Key(md5Key);
					// 通知
					if (null != listener) {
						listener.onComplete(null);
					}
				} catch (Exception e) {
					// 通知
					if (null != listener) {
						listener.onComplete(null);
					}
				}
			}

			@Override
			public void onError(HttpError error) {
				// 通知
				if (null != listener) {
					listener.onComplete(null);
				}
			}

			@Override
			public void onProgress(long max, long progress) {
			}
		};
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId("key");
		httpSetting.setJsonParams(new JSONObject());
		httpSetting.setListener(onAllListener);
		httpSetting.setPost(true);
		httpGroup.add(httpSetting);
	}

	public interface CompleteListener {
		void onComplete(Bundle bundle);
	}

	public HttpGroup(HttpGroupSetting setting) {
		this.httpGroupSetting = setting;
		this.priority = setting.getPriority();
		this.type = setting.getType();
	}

	public static String getCookies() {
		return cookies;
	}

	abstract protected void execute(HttpRequest httpRequest);

	public HttpRequest add(String functionId, JSONObject params,
			OnAllListener listener) {// JSON 方式
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setFunctionId(functionId);
		httpSetting.setJsonParams(params);
		httpSetting.setListener(listener);
		return add(httpSetting);
	}

	public HttpRequest addWithUrl(String url, JSONObject params,
			OnAllListener listener) {// JSON 方式
		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		httpSetting.setJsonParams(params);
		httpSetting.setListener(listener);
		return add(httpSetting);
	}

	public HttpRequest add(String url, Map<String, String> paramMap,
			OnAllListener listener) {// param 方式

		HttpSetting httpSetting = new HttpSetting();
		httpSetting.setUrl(url);
		// TODO
		// 此处可能会放入没有经过URL编码的参数，如果需要可以在此处配合httpSetting.putMapParams()方法遍历一次进行处理。
		httpSetting.setMapParams(paramMap);
		httpSetting.setListener(listener);
		return add(httpSetting);
	}

	public HttpRequest constructHttpRequest(final HttpSetting httpSetting) {
		// 给每个网络请求派发一个运行时标识符
		httpIdCounter = httpIdCounter + 1;
		httpSetting.setId(httpIdCounter);

		tryEffect(httpSetting);

		if (Log.I) {
			Log.i("HttpGroup", "id:" + httpSetting.getId() + "- onStart -->> ");
		}
		httpSetting.onStart();// 通知开始（遮罩要在）

		final HttpRequest httpRequest = new HttpRequest(httpSetting);

		if (Log.I && null != httpSetting.getFunctionId()) {
			Log.i("HttpGroup", "id:" + httpSetting.getId()
					+ "- functionId -->> " + httpSetting.getFunctionId());
		}

		if (Log.I && null != httpSetting.getUrl()) {
			Log.i("HttpGroup", "id:" + httpSetting.getId() + "- url -->> "
					+ httpSetting.getUrl());
		}

		// host（因为第一个handler已经要使用host了，所以在此设置）
		if (null == httpSetting.getHost()) {
			httpSetting.setHost(host);
		}

		// 数据类型（因为优先级提前，因此数据类型也要提前）
		if (httpSetting.getType() == 0) {
			httpSetting.setType(type);
		}

		// 优先级（一定要提前到这里处理，因为要赶在加入线程池之前）
		if (httpSetting.getPriority() == 0) {
			httpSetting.setPriority(priority);
		}

		// 默认优先级
		if (httpSetting.getPriority() == 0) {// 可继承
			switch (httpSetting.getType()) {
			case HttpGroupSetting.TYPE_JSON:// 如果是 JSON
				httpSetting.setPriority(HttpGroupSetting.PRIORITY_JSON);
				break;
			case HttpGroupSetting.TYPE_IMAGE:// 如果是图片
				httpSetting.setPriority(HttpGroupSetting.PRIORITY_IMAGE);
				break;
			case HttpGroupSetting.TYPE_FILE:// 如果是文件
				httpSetting.setPriority(HttpGroupSetting.PRIORITY_FILE);
				break;
			}
		}

		httpSetting.setHttpRequest(httpRequest);

		return httpRequest;
	}

	/**
	 * 每次提供的httpSetting都应该是新的，不要同一个httpSetting多次提供给网络层
	 */
	public HttpRequest add(final HttpSetting httpSetting) {
		
		if(!httpSetting.isIntercepted()){
			// 给每个网络请求派发一个运行时标识符
			httpIdCounter = httpIdCounter + 1;
			httpSetting.setId(httpIdCounter);

			tryEffect(httpSetting);

			if (Log.I) {
				Log.i("HttpGroup", "id:" + httpSetting.getId() + "- onStart -->> ");
			}
			//httpSetting.onStart();// 通知开始（遮罩要在）
		}
// 		if(checkAutoLogin(httpSetting)) return null;

		final HttpRequest httpRequest = new HttpRequest(httpSetting);

		final OnReadyListener onReadyListener = httpSetting
				.getOnReadyListener();
		if (null != onReadyListener) {
			new Thread() {
				@Override
				public void run() {
					onReadyListener.onReady(httpSetting);
					add2(httpRequest);// 准备好参数才继续
				}
			}.start();
		} else {
			add2(httpRequest);// 直接继续
		}
		httpSetting.setHttpRequest(httpRequest);
		return httpRequest;

	}

	public void add2(HttpRequest httpRequest) {
		HttpSetting httpSetting = httpRequest.getHttpSetting();

		if (Log.I && null != httpSetting.getFunctionId()) {
			Log.i("HttpGroup", "id:" + httpSetting.getId()
					+ "- functionId -->> " + httpSetting.getFunctionId());
		}

		if (Log.I && null != httpSetting.getUrl()) {
			Log.i("HttpGroup", "id:" + httpSetting.getId() + "- url -->> "
					+ httpSetting.getUrl());
		}

		// host（因为第一个handler已经要使用host了，所以在此设置）
		if (null == httpSetting.getHost()) {
			httpSetting.setHost(host);
		}

		// 数据类型（因为优先级提前，因此数据类型也要提前）
		if (httpSetting.getType() == 0) {
			httpSetting.setType(type);
		}

		// 优先级（一定要提前到这里处理，因为要赶在加入线程池之前）
		if (httpSetting.getPriority() == 0) {
			httpSetting.setPriority(priority);
		}

		// 默认优先级
		if (httpSetting.getPriority() == 0) {// 可继承
			switch (httpSetting.getType()) {
			case HttpGroupSetting.TYPE_JSON:// 如果是 JSON
				httpSetting.setPriority(HttpGroupSetting.PRIORITY_JSON);
				break;
			case HttpGroupSetting.TYPE_IMAGE:// 如果是图片
				httpSetting.setPriority(HttpGroupSetting.PRIORITY_IMAGE);
				break;
			case HttpGroupSetting.TYPE_FILE:// 如果是文件
				httpSetting.setPriority(HttpGroupSetting.PRIORITY_FILE);
				break;
			}
		}
		execute(httpRequest);// 马上交给线程处理。在此之前还是UI线程。
	}

	/**
	 * 如果要求默认效果，而且效果状态为未处理，并且所关联 activity 非空，才加上效果。
	 */
	private void tryEffect(HttpSetting httpSetting) {
		MyActivity myActivity = httpGroupSetting.getMyActivity();
		if (HttpSetting.EFFECT_DEFAULT == httpSetting.getEffect() && // 需要默认效果
				HttpSetting.EFFECT_STATE_NO == httpSetting.getEffectState() && // 而且效果状态为未处理
				null != myActivity) 
			{// 并且所关联 activity 非空
				DefaultEffectHttpListener effectListener = new DefaultEffectHttpListener(
					httpSetting, myActivity);
			httpSetting.setListener(effectListener);
		}
	}

	@Override
	public void onDestroy() {
		onGroupStartListener = null;
		onGroupEndListener = null;
		onGroupErrorListener = null;
		onGroupProgressListener = null;
		onGroupStepListener = null;

		if (httpList != null && httpList.size() > 0) {
			for (int index = 0; index < httpList.size(); ++index)
				if (httpList.get(index).getHttpSetting() != null
						&& httpList.get(index).getHttpSetting()
								.getHttpRequest() != null) {
					HttpRequest request = httpList.get(index).getHttpSetting()
							.getHttpRequest();
					request.stop();
					PooledThread.getThreadPool().removeTask(request);
				}
			httpList.clear();
		}
	}

	/**
	 * @author lijingzuo 同步组
	 */
	public static class HttpGroupSync extends HttpGroup {

		public HttpGroupSync(HttpGroupSetting setting) {
			super(setting);
		}

		@Override
		public void execute(final HttpRequest httpRequest) {

		}

		@Override
		public void releaseHttpSettings() {
//			if(httpSettings.size()>0){
//				for(HttpSetting httpSetting : httpSettings){
//					final HttpRequest httpRequest = constructHttpRequest(httpSetting);
//					httpRequest.nextHandler();
//				}
//				httpSettings.clear();
//			}
			
		}

	}

	/**
	 * @author lijingzuo 异步池组
	 */
	public static class HttpGroupaAsynPool extends HttpGroup {

		public HttpGroupaAsynPool(HttpGroupSetting setting) {
			super(setting);
		}

		@Override
		public void execute(final HttpRequest httpRequest) {

			PooledThread.getThreadPool().offerTask(
					//
					// new Runnable() {
					// @Override
					// public void run() {
					// // onReady
					//
					// if (httpList.size() < 1) {// 通知组开始
					// HttpGroupaAsynPool.this.onStart();
					// }
					// httpList.add(httpRequest);
					// httpRequest.nextHandler();
					//
					// }
					// },//
					new HttpRequestRunnable(httpRequest),
					httpRequest.getHttpSetting().getPriority());
		}

		public class HttpRequestRunnable implements Runnable {
			private HttpRequest httpRequest;

			public HttpRequestRunnable(HttpRequest httpRequest) {
				this.httpRequest = httpRequest;
			}

			public HttpRequest getHttpRequest() {
				return httpRequest;
			}

			public void setHttpRequest(HttpRequest httpRequest) {
				this.httpRequest = httpRequest;
			}

			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (httpList.size() < 1) {// 通知组开始
					HttpGroupaAsynPool.this.onStart();
				}
				httpList.add(httpRequest);
				httpRequest.nextHandler();
			}
			
		}

		@Override
		public void releaseHttpSettings() {
//			if(httpSettings.size()>0){
//				for(HttpSetting httpSetting : httpSettings){
//					final HttpRequest httpRequest = constructHttpRequest(httpSetting);
//					httpRequest.nextHandler();
//				}
//				httpSettings.clear();
//			}
		}
	}

	interface Handler {
		void run();
	}

	/**
	 * 停止控制器
	 */
	public interface StopController {
		void stop();

		boolean isStop();
	}

	/**
	 * 请求
	 */
	public class HttpRequest implements StopController {

		// 停止控制器
		private boolean stopFlag;

		@Override
		public boolean isStop() {
			return stopFlag;
		}

		@Override
		public void stop() {
			stopFlag = true;
			// try{
			try {
				if (conn != null) {
					conn.disconnect();
					conn = null;
				}
				httpSetting.setListener(null);

				Log.i("zhoubo", "add  33333333333333333");
				if (inputStream != null) {
					inputStream.close();
				}
				if (httpResponse != null) {
					InputStream inputStream0 = httpResponse.getInputStream();
					if (inputStream0 != null) {
						inputStream0.close();
					}
				}
				// httpResponse = null;
				// httpSetting = null;

				this.notifyAll();
				// httpResponse.get
			} catch (Exception e) {
				// 只好这样了，不然现在老是打异常堆栈
				if (Log.D)
					e.printStackTrace();
			}
			// }catch(Exception e){
			//
			// }
			Log.i("zhoubo", "stopLoadBook()....stopFlag==" + stopFlag);
		}

		// 停止控制器

		protected HttpSetting httpSetting;

		protected HttpURLConnection conn;
		protected InputStream inputStream;

		protected HttpResponse httpResponse;

		protected ArrayList<HttpError> errorList;

		protected boolean manualRetry;

		/**
		 * 代表着本次连接是失败的，不可用。
		 */
		protected boolean connectionRetry;

		private int currentHandlerIndex = 0;

		// private String thirdHost;

		private ArrayList<HttpError> getErrorList() {
			if (null == errorList) {
				errorList = new ArrayList<HttpError>();
			}
			return errorList;
		}

		private HttpError getLastError() {
			ArrayList<HttpError> errorList = getErrorList();
			int size = errorList.size();
			if (size > 0) {
				return errorList.get(size - 1);
			}
			return null;
		}

		public void setHttpSetting(HttpSetting httpSetting) {
			this.httpSetting = httpSetting;
		}

		private void clearErrorList() {
			getErrorList().clear();
		}

		public boolean isLastError() {// 判断是否多次尝试失败
			boolean result = null != errorList
					&& !(errorList.size() < attempts);
			if (!result) {
				HttpError lastError = getLastError();
				if (null != lastError && lastError.isNoRetry()) {
					result = true;
				}
			}
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- isLastError() -->> " + result);
			}
			return result;
		}

		public void throwError(HttpError error) {
			if (isStop())
				return;
			ArrayList<HttpError> errorList = getErrorList();
			errorList.add(error);
			error.setTimes(errorList.size());
			if (Log.I) {
				Log.i("HttpGroup", "id:" + httpSetting.getId()
						+ "- HttpError -->> " + error);
			}
			// 检查用户交互
			checkErrorInteraction();
		}

		/**
		 * 检查用户交互
		 */
		public void checkErrorInteraction() {
			/*
			 * 存在2种需要与用户交互的异常
			 */
			HttpError lastError = getLastError();
			if (null != lastError && // 认证WIFI
					HttpError.EXCEPTION == lastError.getErrorCode() && //
					HttpError.EXCEPTION_MESSAGE_ATTESTATION_WIFI
							.equals(lastError.getException().getMessage())) {
				alertAttestationWIFIDialog();
			} else if (isLastError()) {// 如果已经达到自动尝试次数就弹出通知窗口
				alertErrorDialog();
			}
		}

		/**
		 * 非线程安全
		 */
		class HttpDialogController extends DialogController {

			protected ArrayList<HttpRequest> httpRequestList;
			protected MyActivity myActivity;
			protected HttpSetting httpSetting;

			/**
			 * 初始化
			 */
			public void init(ArrayList<HttpRequest> httpRequestList,
					MyActivity myActivity, HttpSetting httpSetting) {
				this.myActivity = myActivity;
				this.httpRequestList = httpRequestList;
				this.httpSetting = httpSetting;
				init(myActivity);
			}

			/**
			 * 重试
			 */
			protected void actionRetry() {
				actionCommon(true);
			}

			/**
			 * 取消
			 */
			protected void actionCancel() {
				actionCommon(false);
			}

			protected void actionCommon(boolean isRetry) {
				alertDialog.dismiss();
				if (Log.D) {
					Log.d("HttpGroup",
							"id:"
									+ httpSetting.getId()
									+ "- notifyUser() retry -->> httpRequestList.size() = "
									+ httpRequestList.size());
				}
				synchronized (alertDialogStateMap) {
					for (int i = 0; i < httpRequestList.size(); i++) {
						HttpRequest httpRequest = httpRequestList.get(i);
						if (isRetry) {
							httpRequest.manualRetry = true;
						} else {
							if (httpSetting.failedRunnable != null) {
								httpSetting.failedRunnable.run();
							} else {
								myActivity.post(new Runnable() {

									@Override
									public void run() {
										// TODO Auto-generated method stub
										// 在firstActivity中，点击畅读书会调用网络，点击取消不能finish。@zhangmurui
										// 12/10/23.
										// if (!(myActivity instanceof
										// MainActivity) && !(myActivity
										// instanceof FirstActivity))
										// myActivity.finish();
									}
								});
							}
						}
						synchronized (httpRequest) {
							httpRequest.notify();
						}
					}
					alertDialogStateMap.remove(myActivity);
				}
			}

		}

		/**
		 * 弹出对话窗
		 */
		private void notifyUser(final HttpDialogController httpDialogController) {

			//final MyActivity myActivity = httpGroupSetting.getMyActivity();
			return;
			/*
			if (null == myActivity) {// 跟界面无关的连接不弹窗
				return;
			}

			boolean result = false;// 用于控制不要同一界面连续弹窗
			ArrayList<HttpRequest> httpRequestList = null;
			synchronized (alertDialogStateMap) {
				httpRequestList = alertDialogStateMap.get(myActivity);// 该页面所关联的需弹窗网络异常通知
				if (null == httpRequestList) {// 如果没有任何需弹窗网络异常通知
					httpRequestList = new ArrayList<HttpRequest>();
					alertDialogStateMap.put(myActivity, httpRequestList);
					result = true;
				}
				httpRequestList.add(this);
			}

			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- notifyUser() -->> result = " + result);
			}

			if (result) {
				// 弹出对话框
				// 初始化
				httpDialogController.init(httpRequestList, myActivity,
						httpSetting);

				myActivity.post(new Runnable() {
					@Override
					public void run() {
						httpDialogController.show();
					}
				});

			}

			// 本线程工作暂停，等待UI线程接受用户选择。
			synchronized (HttpRequest.this) {
				try {
					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId()
								+ "- dialog wait start -->> ");
					}
					HttpRequest.this.wait();
					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId()
								+ "- dialog wait end -->> ");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}*/
		}

		/**
		 * 一般异常对话框
		 */
		private void alertErrorDialog() {

			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- alertErrorDialog() -->> ");
			}

			// 是否禁止通知用户处理
			if (!httpSetting.isNotifyUser()) {
				return;
			}

			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- alertErrorDialog() -->> true");
			}

			// 弹窗通知用户
			HttpDialogController httpDialogController = new HttpDialogController() {

				@Override
				public void onClick(JdOptionDialog  dialog, int which) {
					switch (which) {
					case JdOptionDialog.JDOPTONDIALOG_POS_BUTTON:// （左边的按钮）重试
						actionRetry();
						break;
					case JdOptionDialog.JDOPTONDIALOG_NEG_BUTTON:// （右边的按钮）取消或退出
						actionCancel();
						break;
					}
				}
			};
			HttpError lastError = getLastError();
			if (null != lastError
					&& HttpError.JSON_CODE == lastError.getErrorCode()) {
				httpDialogController.setTitle(MZBookApplication.getInstance()
						.getText(R.string.alert_title_poor_network2));
				httpDialogController.setMessage(MZBookApplication.getInstance()
						.getText(R.string.alert_message_poor_network2));
			} else {
				httpDialogController.setTitle(MZBookApplication.getInstance()
						.getText(R.string.alert_title_poor_network));
				httpDialogController.setMessage(MZBookApplication.getInstance()
						.getText(R.string.alert_message_poor_network));
			}
			// 重试按钮
			httpDialogController.setPositiveButton(MZBookApplication.getInstance()
					.getText(R.string.retry));
			// 退出或取消按钮
			httpDialogController.setNegativeButton(MZBookApplication.getInstance()
					.getText(
							httpSetting.isNotifyUserWithExit() ? R.string.exit
									: R.string.cancel));
			notifyUser(httpDialogController);

		}

		/**
		 * 认证 WIFI 对话框
		 */
		private void alertAttestationWIFIDialog() {
			// 弹出窗口
			HttpDialogController httpDialogController = new HttpDialogController() {

				private int state;

				@Override
				public void onClick(JdOptionDialog dialog, int which) {
					switch (which) {
					case JdOptionDialog.JDOPTONDIALOG_POS_BUTTON:// （左边的按钮）确定
						switch (state) {
						case 0:// 第一次
							if (Log.D) {
								Log.d("HttpGroup",
										"http dialog BUTTON_POSITIVE -->> " + 1);
							}
							// 改变界面和功能
							state = 1;
							myActivity.post(new Runnable() {// 让窗口关闭后重新显示
										@Override
										public void run() {
											if (Log.D) {
												Log.d("HttpGroup",
														"http dialog change -->> ");
											}
											setMessage("现在是否重试？");
											setPositiveButton("重试");
											if (!alertDialog.isShowing()) {
												alertDialog.show();
											}
											// 打开浏览器（这里要确保在所有UI操作之后执行）
											Intent intent = new Intent(
													Intent.ACTION_VIEW,
													Uri.parse("http://app.360buy.com/"));
											intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											MZBookApplication.getInstance()
													.startActivity(intent);
										}
									});
							break;
						case 1:// 第二次
							if (Log.D) {
								Log.d("HttpGroup",
										"http dialog BUTTON_POSITIVE -->> " + 2);
							}
							actionRetry();
							break;
						}
						break;
					case JdOptionDialog.JDOPTONDIALOG_NEG_BUTTON:// （右边的按钮）取消
						if (Log.D) {
							Log.d("HttpGroup",
									"http dialog BUTTON_NEGATIVE -->> " + 1);
						}
						actionCancel();
						break;
					}
				}
			};
			httpDialogController.setTitle("WIFI认证");
			httpDialogController.setMessage("您所连接的网络可能需要验证，现在打开浏览器进行验证？");
			// （左边的按钮）重试
			httpDialogController.setPositiveButton("确定");
			// （右边的按钮）取消或退出
			httpDialogController.setNegativeButton("取消");
			notifyUser(httpDialogController);
		}

		private final ArrayList<Handler> handlers = new ArrayList<Handler>();

		public HttpRequest(HttpSetting httpSetting) {
			this.httpSetting = httpSetting;

			handlers.add(proxyHandler);
			handlers.add(paramHandler);
			handlers.add(firstHandler);
			handlers.add(testHandler);
			handlers.add(cacheHandler);
			handlers.add(connectionHandler);
			handlers.add(contentHandler);
		}

		public HttpSetting getHttpSetting() {
			return httpSetting;
		}

		public void nextHandler() {
//			if(currentHandlerIndex==0 && checkAutoLogin(httpSetting)) return;
			if (isStop())
				return;
			int i = currentHandlerIndex;
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- nextHandler() i -->> " + currentHandlerIndex);
			}
			currentHandlerIndex++;
			if (i < handlers.size()) {
				handlers.get(i).run();
				currentHandlerIndex = i;// 恢复层次指针到本层
			}
		}

		private File findCachesFileByMd5() {
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- findCachesFileByMd5() -->> ");
			}

			com.jingdong.app.reader.util.FileService.Directory directory = null;

			// 1. 确定类型
			switch (httpSetting.getType()) {

			case HttpGroupSetting.TYPE_JSON: {// JSON
				directory = FileService.getDirectory(FileService.JSON_DIR);
				break;
			}

			case HttpGroupSetting.TYPE_IMAGE: {// IMAGE
				directory = FileService.getDirectory(FileService.IMAGE_DIR);
				break;
			}

			}

			// 2. 查找文件
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- findCachesFileByMd5() directory -->> " + directory);
			}
			if (null == directory) {
				return null;
			}
			File dir = directory.getDir();
			if (Log.D) {
				Log.d("HttpGroup",
						"id:" + httpSetting.getId()
								+ "- findCachesFileByMd5() dir.exists() -->> "
								+ dir.exists());
			}
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- findCachesFileByMd5() dir.isDirectory() -->> "
						+ dir.isDirectory());
			}
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- findCachesFileByMd5() dir -->> " + dir);
			}
			File[] fileList = dir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					String md5 = httpSetting.getMd5();
					if (null == md5) {
						return false;
					}
					return filename.startsWith(md5);
				}
			});
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- findCachesFileByMd5() fileList -->> " + fileList);
			}
			if (fileList != null) {
				if (fileList.length > 0) {
					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId()
								+ "- can find caches file by md5 -->> ");
					}
					return fileList[0];
				}
			}

			return null;

		}

		/**
		 * 参数
		 */
		private final Handler paramHandler = new Handler() {
			@Override
			public void run() {
				if (null != httpSetting.getFunctionId() && httpSetting.isSuffix) {
					httpSetting.putMapParams("functionId",
							httpSetting.getFunctionId());
				}
				if (httpSetting.getJsonParams() != null && httpSetting.isSuffix) {
					String body = httpSetting.getJsonParams().toString();
					if (Log.I) {
						Log.i("HttpGroup", "id:" + httpSetting.getId()
								+ "- body -->> " + body);
					}
					if (httpSetting.getIsEncoder()) {
						String bodyEncoder = RsaEncoder.stringBodyEncoder(body,
								httpSetting.encodeEntity);
						httpSetting.putMapParams("body", bodyEncoder);
					} else {
						httpSetting.putMapParams("body", body);
					}
				}
				nextHandler();
			}
		};

		/**
		 * WAP
		 */
		private final Handler proxyHandler = new Handler() {
			@Override
			public void run() {
				initProxy();
				nextHandler();
				return;
			}
		};

		// /**
		// * WAP
		// */
		// private final Handler reinitProxyHandler = new Handler() {
		// @Override
		public void initProxy() {
			Log.i("HttpGroup", "HttpGroup" + "id:" + httpSetting.getId()
					+ " reInitFinalUrlProxy()");
			if (null != httpSetting.getFunctionId()
					&& httpSetting.getUrl() == null) {
				httpSetting.setUrl("http://" + httpSetting.getHost()
						+ "/client.action");
			}

			// 是否走代理 End
			String proxyHost = NetUtils.getProxyHost();

			// 是否走代理 Start
			String url = httpSetting.getUrl();
			if (null != url) {
				// String tempHost = NetUtils.getHostAndPortByUrl(url);
				// httpSetting.setHost(tempHost);
				if (!TextUtils.isEmpty(proxyHost) && !mustDirectConnect) {
					// if (null != tempHost) {
					// httpSetting.setUrl(url.replace(tempHost, proxyHost));
					httpSetting.setProxyed(true);
					Log.i("HttpGroup",
							"HttpGroup" + "id:" + httpSetting.getId()
									+ "httpSetting.isproxyed====="
									+ httpSetting.isproxyed);
					// }
				}
			}
			String finalUrl = httpSetting.getFinalUrl();
			if (finalUrl != null) {
				String tempHost = NetUtils.getHostAndPortByUrl(url);
				// String host = httpSetting.getHost();
				if (!TextUtils.isEmpty(host) && !TextUtils.isEmpty(tempHost)
						&& !tempHost.equals(host) && mustDirectConnect) {
					// httpSetting.setFinalUrl(finalUrl.replace(tempHost,
					// host));
					httpSetting.setProxyed(false);
					Log.i("HttpGroup",
							"HttpGroup" + "id:" + httpSetting.getId()
									+ "httpSetting.isproxyed====="
									+ httpSetting.isproxyed);
				} else if (!TextUtils.isEmpty(proxyHost)
						&& !TextUtils.isEmpty(tempHost)
						&& !tempHost.equals(proxyHost) && !mustDirectConnect) {
					// httpSetting.setFinalUrl(finalUrl.replace(tempHost,
					// proxyHost));
					httpSetting.setProxyed(true);
					// Log.i("httpSetting",
					// "httpSetting.isproxyed====="+httpSetting.isproxyed);
					Log.i("HttpGroup",
							"HttpGroup" + "id:" + httpSetting.getId()
									+ "httpSetting.isproxyed====="
									+ httpSetting.isproxyed);
				}

			}
		}

		public void reInitFinalUrlProxy() {
			Log.i("HttpGroup", "HttpGroup" + "id:" + httpSetting.getId()
					+ " reInitFinalUrlProxy()");
			String proxyHost = NetUtils.getProxyHost() + ":80";
			String finalUrl = httpSetting.getFinalUrl();
			Log.i("HttpGroup", "HttpGroup" + "id:" + httpSetting.getId()
					+ "444444444444444444444");
			if (finalUrl != null) {
				Log.i("HttpGroup", "HttpGroup" + "id:" + httpSetting.getId()
						+ "333333333333333333333333");
				String tempHost = NetUtils.getHostAndPortByUrl(finalUrl);
				Log.i("HttpGroup", "HttpGroup" + "id:" + httpSetting.getId()
						+ " tempHost==" + tempHost);
				String host = httpSetting.getHost();
				Log.i("HttpGroup", "HttpGroup" + "id:" + httpSetting.getId()
						+ " host==" + host);
				if (!TextUtils.isEmpty(host) && !TextUtils.isEmpty(tempHost)
						&& mustDirectConnect) {
					if (!tempHost.equals(host)) {
						// httpSetting.setFinalUrl(finalUrl.replace(tempHost,
						// host));
					}
					httpSetting.setProxyed(false);
					Log.i("HttpGroup",
							"HttpGroup" + "id:" + httpSetting.getId()
									+ "httpSetting.isproxyed====="
									+ httpSetting.isproxyed);
					Log.i("HttpGroup",
							"HttpGroup" + "id:" + httpSetting.getId()
									+ "2222222222222222222");
				} else if (!TextUtils.isEmpty(proxyHost)
						&& !TextUtils.isEmpty(tempHost) && !mustDirectConnect) {
					if (!tempHost.equals(proxyHost)) {
						// httpSetting.setFinalUrl(finalUrl.replace(tempHost,
						// proxyHost));
					}
					httpSetting.setProxyed(true);
					// Log.i("httpSetting",
					// "httpSetting.isproxyed====="+httpSetting.isproxyed);
					Log.i("HttpGroup",
							"HttpGroup" + "id:" + httpSetting.getId()
									+ "httpSetting.isproxyed====="
									+ httpSetting.isproxyed);
				} else {
				}
			}
			Log.i("HttpGroup", "HttpGroup" + "id:" + httpSetting.getId()
					+ httpSetting.getFinalUrl());
		}

		//
		// };
		/**
		 * 设置纠正
		 */
		private final Handler firstHandler = new Handler() {
			@Override
			public void run() {

				// 继承组设置：

				// 连接等待时间
				if (httpSetting.getConnectTimeout() == 0) {
					httpSetting.setConnectTimeout(connectTimeout);
				}
				// 读取等待时间
				if (httpSetting.getReadTimeout() == 0) {
					httpSetting.setReadTimeout(readTimeout);
				}

				// 使用 GET 的方式发出请求
				if (httpSetting.getType() == HttpGroupSetting.TYPE_IMAGE// 如果是图片
						|| httpSetting.getType() == HttpGroupSetting.TYPE_FILE) {// 如果是文件
					httpSetting.setPost(false);
				}

				// 默认连接时间

				// 默认读取时间
				if (httpSetting.getType() == HttpGroupSetting.TYPE_IMAGE) {// 如果是文件
					httpSetting.setReadTimeout(1000 * 60 * 60);// 读取超时（1个小时）
				}

				// 默认缓存
				if (httpSetting.getType() == HttpGroupSetting.TYPE_IMAGE) {// 如果是图片
					httpSetting.setLocalFileCache(true);
					httpSetting.setLocalFileCacheTime(CacheTimeConfig.IMAGE);// 图片默认缓存一天
				}

				// 全局初始化
				if (httpSetting.getType() == HttpGroupSetting.TYPE_IMAGE) {// 如果是图片
					httpSetting.setNeedGlobalInitialization(false);
					httpSetting.setNeedUUID(false);
				}

//				if (httpSetting.isNeedGlobalInitialization()) {
//					GlobalInitialization.initNetwork(true);
//					// 全局初始化（设备注册、版本升级检测等等）失败后取消所有的httprequest
//					if (!GlobalInitialization.getInstance()
//							.isGlobalInitialized()) {
//						HttpError error = new HttpError();
//						error.setErrorCode(HttpError.RESPONSE_CODE);
//						error.setResponseCode(200);// 目前就当做404处理
//						for (int i = 0; i < attempts; ++i)
//							throwError(error);
//						httpSetting.onError(getLastError());// 通知失败
//						return;
//					}
//				}

				// 组里面的连接累计量
				addMaxStep(1);

				
                urlParam();

				if (checkModule(MODULE_STATE_DISABLE)) {// 检查接口是否禁用
					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId()
								+ "- functionId close -->> ");
					}
					return;
				}

				if ((TextUtils.isEmpty(httpSetting.getUrl()) && TextUtils
						.isEmpty(httpSetting.getFunctionId()))
						|| //
						httpSetting.getUrl().endsWith(".gif")
						|| httpSetting.getUrl().endsWith(".bmp")) {
					HttpError error = new HttpError();
					error.setErrorCode(HttpError.RESPONSE_CODE);
					error.setResponseCode(404);// 目前就当做404处理
					throwError(error);
					httpSetting.onError(getLastError());// 通知失败
					return;// 根本没执行后来的？
				} else {
					nextHandler();
					if (isLastError()) {
						if (Log.I) {
							Log.i("HttpGroup", "id:" + httpSetting.getId()
									+ "- onError -->> ");
						}
						httpSetting.onError(getLastError());// 通知失败
					} else {
						if (Log.I) {
							Log.i("HttpGroup", "id:" + httpSetting.getId()
									+ "- onEnd -->> ");
						}
						addCompletesCount();
						addStep(1);
						httpSetting.onEnd(httpResponse);// 通知成功
						if (httpSetting.successRunnable != null)
							httpSetting.successRunnable.run();
					}

					return;
				}
			}
		};

		// interface HttpTestMappers {
		//
		// boolean
		//
		// }

		/**
		 * 测试
		 */
		private final Handler testHandler = new Handler() {
			@Override
			public void run() {
				if (Configuration.getBooleanProperty(Configuration.TEST_MODE,
						false)) {// 当前是否以测试模式运行

					if ("viewActivity".equals(httpSetting.getFunctionId())) {
						httpResponse = new HttpResponse();
						String str = "{\"adword\":\"引爆红六月！直降314元！加赠韩国精品餐具套装！\",\"book\":\"false\",\"imageurl\":\"http://img10.360buyimg.com/n5/2417/17c1667a-ab1d-47d9-9d2d-7c44ded749b2.jpg\",\"jdPrice\":\"899.00\",\"wareId\":\"318157\",\"wmaprice\":\"http://price.360buy.com/P40FB32EE41477A324FFC16FEAA74891F,1.png\",\"wname\":\"直降314！加赠精品餐具套装！科沃斯智能机器人吸尘器地宝520（FR馨喜红）\"},{\"adword\":\"\",\"book\":\"false\",\"imageurl\":\"http://img10.360buyimg.com/n5/4515/5af248ad-f84a-4db7-a124-3fe1fb30c5da.jpg\",\"jdPrice\":\"359.00\",\"wareId\":\"108001\",\"wmaprice\":\"http://price.360buy.com/P7E50A4D7F3680EC11BD8D2251465E4BC,1.png\",\"wname\":\"瑞士军刀工作冠军0.9064功能齐全，完美品质！\"},{\"adword\":\"直降96，简直超实惠！\",\"book\":\"false\",\"imageurl\":\"http://img10.360buyimg.com/n5/1629/c1bedf11-3450-4029-ba5a-0706c61866ca.jpg\",\"jdPrice\":\"89.00\",\"wareId\":\"261923\",\"wmaprice\":\"http://price.360buy.com/P4A467FE6B0EB74674C81374307B9986B,1.png\",\"wname\":\"好宜佳高效纳米改性炭家庭装HZN2000\"},{\"adword\":\"出行更方便！下单立省20元！\",\"book\":\"false\",\"imageurl\":\"http://img10.360buyimg.com/n5/4493/9fad1aa4-3ac6-4d7c-8b14-074e707ff5ed.jpg\",\"jdPrice\":\"99.00\",\"wareId\":\"326802\",\"wmaprice\":\"http://price.360buy.com/P1E2CA69C3EFEECEA27C2A26A93D4AE49,1.png\",\"wname\":\"步行者防水袋（颜色随机）！！下单立省20元！！\"},{\"adword\":\"直降101元！抢购时间：13点-17点\",\"book\":\"false\",\"imageurl\":\"http://img10.360buyimg.com/n5/7478/302f0b63-d793-4249-b6d4-e8d0f725d595.jpg\",\"jdPrice\":\"469.00\",\"wareId\":\"1000201612\",\"wmaprice\":\"http://price.360buy.com/PFFBB0AE38B210E68D6F2C55D311B53F4,1.png\",\"wname\":\"E路航gps导航仪LH980N升级版高清5寸3D实景内置4G车载导航官方标配\"},{\"adword\":\"引爆红六月！直降100元！3.2英寸WQVGA高清靓屏，高清RMVB视频直播，320万像素，支持后台QQ！\",\"book\":\"false\",\"imageurl\":\"http://img10.360buyimg.com/n5/4149/bc3553ee-410e-4b58-8777-e1b1e24bd69f.jpg\",\"jdPrice\":\"699.00\",\"wareId\":\"226753\",\"wmaprice\":\"http://price.360buy.com/P6F7733F4CAC0DB064764D91F9B1E5A91,1.png\",\"wname\":\"直降100！联想i61影音手机！3.2英寸WQVGA高清靓屏，高清RMVB视频直播！\"},{\"adword\":\"疯抢优惠300！特价促销就在今天！~抢完即止！全不锈钢错位形双层油网，全不锈钢壳体加钢化玻璃装饰\",\"book\":\"false\",\"imageurl\":\"http://img10.360buyimg.com/n5/2198/39d00199-2a62-4fab-aab6-fc46834ee18a.jpg\",\"jdPrice\":\"998.00\",\"wareId\":\"362238\",\"wmaprice\":\"http://price.360buy.com/P16479663FCCB36E351597EEC5E3A4AA0,1.png\",\"wname\":\"万和CXW-200-J02C钢化玻璃近吸式烟机疯抢优惠300元！特价出击，限量50台\"},{\"adword\":\"好礼送不停,现在购买送原装包鼠,百宝箱还有高档插板!!!给你电脑最好的保护!!!\",\"book\":\"false\",\"imageurl\":\"http://img10.360buyimg.com/n5/42/8e230abc-07fd-445f-bfb9-4107323f6ece.jpg\",\"jdPrice\":\"3499.00\",\"wareId\":\"408037\",\"wmaprice\":\"http://price.360buy.com/P0637C82DD11C3E8DD81EC4B19EDC4372,1.png\",\"wname\":\"华硕X42EI38JZ(i3-380+HD6470),主流机型冰点价,送高档插板,原装包鼠,大礼包!\"},{\"adword\":\"\",\"book\":\"false\",\"imageurl\":\"http://img10.360buyimg.com/n5/4878/71a2fb53-7dc0-414e-a4c0-4dbf41fcd5f5.jpg\",\"jdPrice\":\"1119.00\",\"wareId\":\"348595\",\"wmaprice\":\"http://price.360buy.com/PE57C181E89ADEDA1AEBE2604187D2852,1.png\",\"wname\":\"三星ST951610万大广角大触摸屏！时尚相机直降200元，再送4G卡，限时限量抢购！\"},{\"adword\":\"电脑只买最新的！新平台i5，本周特价啦！\",\"book\":\"false\",\"imageurl\":\"http://img10.360buyimg.com/n5/778/ff284d0a-256d-4fd2-965e-4d978becf00a.jpg\",\"jdPrice\":\"4699.00\",\"wareId\":\"355888\",\"wmaprice\":\"http://price.360buy.com/PBE806E20BEE3D450022A37D49D8FDB7F,1.png\",\"wname\":\"买电脑就要喜新厌旧！新平台i5+1G独显，联想G470特价啦！\"}";
						httpResponse.setString(str);
						try {
							httpResponse.setJsonObject(new JSONObjectProxy(
									new JSONObject(httpResponse.getString())));
						} catch (JSONException e) {
							e.printStackTrace();
						}

						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- test json file -->> ");
						}
					} else if ("listActivity".equals(httpSetting
							.getFunctionId())) {
						httpResponse = new HttpResponse();
						String str = "{\"activityList\":[{\"activityId\":14215100,\"horizontalImag\":\"http://img11.360buyimg.com/da/20110531/766_120_WAYiog.jpg\",\"verticalImage\":\"http://img11.360buyimg.com/da/20110531/766_120_WAYiog.jpg\"},{\"activityId\":14215100,\"horizontalImag\":\"http://img11.360buyimg.com/da/20110531/766_120_WAYiog.jpg\",\"verticalImage\":\"http://img11.360buyimg.com/da/20110531/766_120_WAYiog.jpg\"}],\"code\":\"0\"}";
						httpResponse.setString(str);
						try {
							httpResponse.setJsonObject(new JSONObjectProxy(
									new JSONObject(httpResponse.getString())));
						} catch (JSONException e) {
							e.printStackTrace();
						}

						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- test json file -->> ");
						}
						// } else if
						// ("yinLianPay".equals(httpSetting.getFunctionId())) {
						// httpResponse = new HttpResponse();
						// String str =
						// "{\"code\":\"0\",\"message\":\"<?xml version=\\\"1.0\\\" encoding=\\\"utf-8\\\" ?><upomp application=\\\"UpPay.Req\\\"><pluginVersion><\\/pluginVersion><merchantName>北京京东世纪商贸有限公司<\\/merchantName><merchantId>320100048991001<\\/merchantId><merchantOrderId>130189936<\\/merchantOrderId><merchantOrderTime>20111221110610<\\/merchantOrderTime><merchantOrderAmt>24900<\\/merchantOrderAmt><merchantOrderDesc><\\/merchantOrderDesc><transTimeout>5000<\\/transTimeout><backEndUrl>pay.m360buy.co.cc\\/unionPay\\/callBack<\\/backEndUrl><sign>JK0cwM\\/68rrLwy3+d3htmooTCh6xPHs7xSUeMMOjyP4LPra6TF+\\/D97sUyB\\/LbJTw13BOusPi4IZ1POTdtctU0mM6W7l4f3DIWbHGNNLCFxvJQAzvbr8+Re3lMpIeCUJBWnnrvnOzH7d1tNbhJX6Z+gVf7eAX\\/gqduoolY63Sgc=<\\/sign><merchantPublicCert>MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCm1sLf1A+e8bIuX\\/FBvikITeD3lDzqX8YTV12o6tdHmiN9d6wEsd7Xsg00inBWSog39cMymm2NuR3h4oS7By511OkbXBvM+X9hnfdQnRuQwUgEE6OYxm9UQCNouqXRw1YSeTBuEePP4P8zsqewKhHz9IEiW1uDgOCscYgaWmCdMwIDAQAB<\\/merchantPublicCert><cupsQid><\\/cupsQid><cupsTraceNum><\\/cupsTraceNum><cupsTraceTime><\\/cupsTraceTime><cupsRespCode><\\/cupsRespCode><respCode><\\/respCode><respDesc><\\/respDesc><\\/upomp>\"}";
						// httpResponse.setString(str);
						// try {
						// httpResponse.setJsonObject(new JSONObjectProxy(new
						// JSONObject(httpResponse.getString())));
						// } catch (JSONException e) {
						// e.printStackTrace();
						// }
						//
						// if (Log.D) {
						// Log.d("HttpGroup", "id:" + httpSetting.getId() +
						// "- test json file -->> ");
						// }
					} else {
						// 没有合适的映射
						nextHandler();
					}

				} else {

					// 并非测试模式
					nextHandler();

				}
			}
		};

		/**
		 * 缓存
		 */
		private final Handler cacheHandler = new Handler() {
			@Override
			public void run() {

				File cachesFile = null;
				// 内存缓存
				// JSONObjectProxy cachesJsonObject = null;
				// if (httpSetting.isLocalMemoryCache() && null !=
				// (cachesJsonObject = JsonCache.get(httpSetting.getMd5()))) {//
				// 内存缓存
				// httpResponse = new HttpResponse();
				// httpResponse.setJsonObject(cachesJsonObject);
				// } else
				if (httpSetting.getCacheMode() != HttpSetting.CACHE_MODE_ONLY_NET
						&& httpSetting.isLocalFileCache()
						&& null != (cachesFile = findCachesFileByMd5())) {// 如果有缓存文件就走缓存

					long localFileCacheTime = httpSetting
							.getLocalFileCacheTime();

					if (localFileCacheTime != 0) {// 超出有效期
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- local file cache time out -->> ");
						}
						doNetAndCache();
						return;
					}

					httpResponse = new HttpResponse();

					switch (httpSetting.getType()) {

					case HttpGroupSetting.TYPE_JSON: {// JSON

						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- read json file -->> ");
						}
						FileInputStream inputStream = null;
						try {
							inputStream = new FileInputStream(cachesFile);
							httpResponse.setString(IOUtil.readAsString(
									inputStream, sCharset));
							httpResponse.setJsonObject(new JSONObjectProxy(
									new JSONObject(httpResponse.getString())));

						} catch (Exception e) {
							e.printStackTrace();
							cachesFile.delete();
							httpResponse = null;
							doNetAndCache();
						} finally {
							if (null != inputStream) {
								try {
									inputStream.close();
								} catch (Exception e) {
								}
							}
						}

						break;
					}

					case HttpGroupSetting.TYPE_XML: {// JSON

						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- read json file -->> ");
						}
						FileInputStream inputStream = null;
						try {
							inputStream = new FileInputStream(cachesFile);
							DocumentBuilderFactory factory = DocumentBuilderFactory
									.newInstance();
							DocumentBuilder builder = factory
									.newDocumentBuilder();
							Document dom = builder.parse(inputStream);
							Element root = dom.getDocumentElement();
							httpResponse.setRoot(root);
							// httpResponse.setString(IOUtil.readAsString(inputStream,
							// charset));
							// httpResponse.setJsonObject(new
							// JSONObjectProxy(new
							// JSONObject(httpResponse.getString())));

						} catch (Exception e) {
							e.printStackTrace();
							cachesFile.delete();
							httpResponse = null;
							doNetAndCache();
						} finally {
							if (null != inputStream) {
								try {
									inputStream.close();
								} catch (Exception e) {
								}
							}
						}

						break;
					}

					case HttpGroupSetting.TYPE_STREAM: {// JSON
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- read json file -->> ");
						}
						FileInputStream inputStream = null;
						try {
							inputStream = new FileInputStream(cachesFile);
							httpResponse.setByteArrayInputStream(StreamToolBox
									.flushInputStream(inputStream));
							// httpResponse.setJsonObject(new
							// JSONObjectProxy(new
							// JSONObject(httpResponse.getString())));

						} catch (Exception e) {
							e.printStackTrace();
							cachesFile.delete();
							httpResponse = null;
							doNetAndCache();
						} finally {
							if (null != inputStream) {
								try {
									inputStream.close();
								} catch (Exception e) {
								}
							}
						}

						break;
					}
					case HttpGroupSetting.TYPE_IMAGE: {// IMAGE

						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- read image file -->> ");
						}
						try {
							httpResponse.setLength(cachesFile.length());
							// FileInputStream fileInputStream=new
							// FileInputStream(cachesFile);
							// httpResponse.setInputData(StreamToolBox.getByteArray(fileInputStream));
							// httpResponse.setInputStream(StreamToolBox.loadStreamFromFile(cachesFile));
							Bitmap bitmap = BitmapFactory.decodeFile(
									cachesFile.getAbsolutePath(),
									getBitmapOpt());
							bitmap = ImageUtils.CropForExtraWidth(bitmap);
							httpResponse.setBitmap(bitmap);
							httpResponse
									.setDrawable(new BitmapDrawable(bitmap));
						} catch (Throwable e) {
							cachesFile.delete();
							httpResponse = null;
							doNetAndCache();
						}

						break;
					}
					}

				} else {
					doNetAndCache();
				}
			}
		};

		/**
		 * 访问网络后进行缓存
		 */
		private void doNetAndCache() {

			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- doNetAndCache() -->> ");
			}

			// 如果要求只依靠缓存，那么直接走onError。
			if (HttpSetting.CACHE_MODE_ONLY_CACHE == httpSetting.getCacheMode()) {
				HttpError httpError = new HttpError(new Exception(
						HttpError.EXCEPTION_MESSAGE_NO_CACHE));
				httpError.setNoRetry(true);
				throwError(httpError);
				return;
			}

			nextHandler();

			if (isLastError()) {
				return;
			}

			save();
		}

		/**
		 * 保存
		 */
		private void save() {
			// 存储
			if (httpSetting.isLocalFileCache()) {
				switch (httpSetting.getType()) {

				case HttpGroupSetting.TYPE_JSON: {// JSON

					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId()
								+ "- save json file start -->> ");
					}
					Directory directory = FileService
							.getDirectory(FileService.JSON_DIR);
					if (null != directory) {
						String fileName = httpSetting.getMd5() + ".json";
						if (null == httpResponse) {
							return;
						}
						String fileContent = httpResponse.getString();
						boolean result = FileService.saveToSDCard(
								FileService.getDirectory(FileService.JSON_DIR),
								fileName, fileContent);
						if (result) {
							CacheFile cacheFile = new CacheFile(fileName,
									httpSetting.getLocalFileCacheTime());
							cacheFile.setDirectory(directory);
							// CacheFileTable.insertOrUpdate(cacheFile);
						}
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- save json file -->> " + result);
						}
					}
					break;
				}
				case HttpGroupSetting.TYPE_XML: {// JSON

					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId()
								+ "- save json file start -->> ");
					}
					Directory directory = FileService
							.getDirectory(FileService.XML_DIR);
					if (null != directory) {
						String fileName = httpSetting.getMd5() + ".XML";
						if (null == httpResponse) {
							return;
						}
						String fileContent = httpResponse.getString();
						boolean result = FileService.saveToSDCard(
								FileService.getDirectory(FileService.XML_DIR),
								fileName, fileContent);
						if (result) {
							CacheFile cacheFile = new CacheFile(fileName,
									httpSetting.getLocalFileCacheTime());
							cacheFile.setDirectory(directory);
							// CacheFileTable.insertOrUpdate(cacheFile);
						}
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- save json file -->> " + result);
						}
					}
					break;
				}
				case HttpGroupSetting.TYPE_IMAGE: {// IMAGE

					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId()
								+ "- save image file start -->> ");
					}
					Directory directory = FileService
							.getDirectory(FileService.IMAGE_DIR);
					if (null != directory) {
						String fileName = httpSetting.getMd5() + ".image";
						if (null == httpResponse) {
							return;
						}
						byte[] fileContent = httpResponse.getInputData();
						boolean result = FileService.saveToSDCard(directory,
								fileName, fileContent);
						if (result) {
							CacheFile cacheFile = new CacheFile(fileName,
									httpSetting.getLocalFileCacheTime());
							cacheFile.setDirectory(directory);
							// CacheFileTable.insertOrUpdate(cacheFile);
						}
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- save image file -->> " + result);
						}
					}
					break;
				}

				}
			}
		}

		/**
		 * 连接
		 */
		private final Handler connectionHandler = new Handler() {

			@Override
			public void run() {
				for (int i = 0; (i < attempts) && !isStop();) {// 重试N次

					boolean retry = false;
					try {

						if (i >= 1 && isNeedInitAgent()) {
							if (httpSetting.isproxyed()) {
								mustDirectConnect = true;
							} else if (!httpSetting.isproxyed()) {
								mustDirectConnect = false;
							}
						}
						// 加密工作
						beforeConnection();
						if (i >= 1) {
							reInitFinalUrlProxy();
						}
						// 加密Url和普通Url都在最后拼接成finalUrl
						String urlStr = httpSetting.getFinalUrl();
						URL url = new URL(urlStr);
						Log.d("HttpGroup", "urlStr--------------------->"
								+ urlStr);

						Log.d("HttpGroup",
								"url.getDefaultPort():" + url.getDefaultPort()
										+ "");
						// 开始创建连接对象
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- url.openConnection() -->> ");
						}
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- finalUrl -->> " + url);
						}
						// MyFileWriter.WriterInlog("HttpGroup"+"id:" +
						// httpSetting.getId() + "- finalUrl -->> " +urlStr);

						// conn = (HttpURLConnection) url.openConnection();
						// 是否走代理 Start
						if (httpSetting.isproxyed()) {
							java.net.Proxy proxy = new java.net.Proxy(
									java.net.Proxy.Type.HTTP,
									new InetSocketAddress(
											android.net.Proxy.getDefaultHost(),
											android.net.Proxy.getDefaultPort()));
							conn = (HttpURLConnection) url
									.openConnection(proxy);
							// conn.setRequestProperty("X-Online-Host",
							// httpSetting.getHost());
							Log.i("HttpGroup", "HttpGroup" + "id:"
									+ httpSetting.getId() + "X-Online-Host==="
									+ httpSetting.getHost());
						} else {
							conn = (HttpURLConnection) url.openConnection();
						}
						// 是否走代理 End
						conn.setRequestProperty("Accept", "*/*");
						conn.setConnectTimeout(httpSetting.getConnectTimeout());
						conn.setReadTimeout(httpSetting.getReadTimeout());
						conn.setUseCaches(useCaches);
						conn.setRequestProperty("Charset", sCharset);
						conn.setRequestProperty("Connection", "Keep-Alive");// 保持长连接
						conn.setRequestProperty("Accept-Encoding",
								"gzip,deflate");// 客户端支持gzip
						if (!TextUtils.isEmpty(cookies)) {
							if (Log.D) {
								Log.d("HttpGroup", "id:" + httpSetting.getId()
										+ "- cookies set -->> " + cookies);
							}
							conn.setRequestProperty("Cookie", cookies);// Cookie
							// 发出

							SharedPreferences jdSharedPreferences = CommonUtil
									.getJdSharedPreferences();
							jdSharedPreferences.edit()
									.putString("cookies", cookies).commit();
							// 谁添加的cookies持久化？有何用？
						}
						if(!TextUtils.isEmpty(tempCookies)&&httpSetting.isUserTempCookies()){
							conn.setRequestProperty("Cookie", tempCookies);// 设置临时
							if (Log.D) {
								Log.d("HttpGroup", "id:" + httpSetting.getId()
										+ "- tempCookies set -->> " + tempCookies);
							}
						}
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- handleGetOrPost() -->> ");
						}
						handleGetOrPost();
						if (connectionRetry) {// 连接不成功重试
							connectionRetry = false;
							retry = true;
						}

					} catch (Exception e) {
						HttpError httpError = new HttpError(e);
						throwError(httpError);
						retry = true;
					}

					if (retry) {
						if (i < attempts - 1) {
							try {// 隔一段时间再尝试
								if (Log.D) {
									Log.d("HttpGroup",
											"id:" + httpSetting.getId()
													+ "- sleep -->> "
													+ attemptsTime);
								}
								Thread.sleep(attemptsTime);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if (Log.D) {
							Log.d("HttpGroup", "id:" + httpSetting.getId()
									+ "- onRetry -->> " + " manualRetry = "
									+ manualRetry);
						}

						if (manualRetry) {
							// 手动尝试
							manualRetry = false;
							clearErrorList();
							i = 0;
						} else {
							// 自动尝试，无需操作
							i++;
						}
					} else {
						break;
					}
				}
			}
		};

		private void urlParam() {

			if (httpSetting.isPost()) {
				// POST
				if (null != this.httpSetting.getMapParams()) {

					if (reportUserInfoFlag) 
                    {
                        String  functionId=this.httpSetting.getMapParams().get("functionId");
						String repore = StatisticsReportUtil.getReportString(httpSetting.isNeedUUID(),httpSetting.isNeedBaseParamStr());
	                    httpSetting.setSemiUrl(this.httpSetting.getUrl()
								+ "?"
								+ "functionId="
								+ functionId 
								+ repore);
                        


                        /*this.httpSetting.setSemiUrl(this.httpSetting.getUrl()
								+ "?"
								+ "functionId="
								+ this.httpSetting.getMapParams().get(
										"functionId")
								+ StatisticsReportUtil.getReportString(
										httpSetting.isNeedUUID(),
										httpSetting.isNeedBaseParamStr()));
                        */
					} else {
						this.httpSetting.setSemiUrl(this.httpSetting.getUrl()
								+ "?"
								+ "functionId="
								+ this.httpSetting.getMapParams().get(
										"functionId"));
					}

				}
			} else {
				// GET
				if (null != this.httpSetting.getMapParams()) {

					StringBuilder url = new StringBuilder(
							this.httpSetting.getUrl());
					url.append("?");

					Map<String, String> mapParams = this.httpSetting
							.getMapParams();
					Set<String> keySet = mapParams.keySet();
					for (Iterator<String> iterator = keySet.iterator(); iterator
							.hasNext();) {
						String key = iterator.next();
						String value = mapParams.get(key);
						url.append(key).append("=").append(value);
						if (iterator.hasNext()) {
							url.append("&");
						}
					}

					if (reportUserInfoFlag) {
						this.httpSetting.setSemiUrl(url.toString()
								+ StatisticsReportUtil.getReportString(
										httpSetting.isNeedUUID(),
										httpSetting.isNeedBaseParamStr()));
					} else {
						this.httpSetting.setSemiUrl(url.toString());
					}

				}
			}

		}

		private void beforeConnection() {
			// 判断是否需要指纹
			if (checkModule(MODULE_STATE_ENCRYPT)) {
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- encrypt -->> ");
				}
				if (null == mMd5Key) {
					queryMd5Key(continueListener);

					// 本线程工作暂停，等待网络线程获取Md5Key。
					synchronized (HttpRequest.this) {
						try {
							if (Log.D) {
								Log.d("HttpGroup", "id:" + httpSetting.getId()
										+ "- encrypt wait start -->> ");
							}
							HttpRequest.this.wait();
							if (Log.D) {
								Log.d("HttpGroup", "id:" + httpSetting.getId()
										+ "- encrypt wait end -->> ");
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				this.httpSetting.setFinalUrl(httpSetting.getSemiUrl()
						+ "&hash="
						+ Md5Encrypt.md5(httpSetting.getJsonParams().toString()
								+ mMd5Key));
			} else
				this.httpSetting.setFinalUrl(httpSetting.getSemiUrl());
		}

		private void handleGetOrPost() throws Exception {
			if (httpSetting.isPost()) {
				post();
			} else {
				get();
			}
			connectionHandler2();
		}

		/**
		 * GET 请求
		 */
		private void get() throws Exception {
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- get() -->> ");
			}
			httpResponse = new HttpResponse(conn);
			conn.setRequestMethod("GET");
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- get() -->> ok");
			}
		}

		/**
		 * POST 请求
		 */
		private void post() throws Exception {
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- post() -->> ");
			}
			httpResponse = new HttpResponse(conn);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			byte[] data = null;
			if (this.httpSetting.getMapParams() == null) {
				data = "body=".getBytes();
			} else {
				StringBuilder sb = new StringBuilder();
				Map<String, String> mapParams = this.httpSetting.getMapParams();
				Set<String> keySet = mapParams.keySet();
				for (Iterator<String> iterator = keySet.iterator(); iterator
						.hasNext();) {
					String key = iterator.next();
					if ("functionId".equals(key)) {
						continue;
					}
					String value = mapParams.get(key);
					if (Log.I) {
						Log.i("HttpGroup", "id:" + httpSetting.getId()
								+ "- param key and value -->> " + key + "："
								+ URLDecoder.decode(value,"UTF-8"));
					}
					sb.append(key).append("=").append(value);
					if (iterator.hasNext()) {
						sb.append("&");
					}
				}

				data = sb.toString().getBytes();
				Log.i("HttpGroup", "id:" + httpSetting.getId()
						+ "finalUrl -->data===" + sb.toString());
			}
			conn.setRequestProperty("Content-Length",
					String.valueOf(data.length));
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded;_charset_=utf-8");

			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- post() -->> 1");
			}
			DataOutputStream outStream = new DataOutputStream(
					conn.getOutputStream());
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- post() -->> 2");
			}
			outStream.write(data);
			/*
			 * conn.setRequestProperty("Content-Length",
			 * String.valueOf(data.length));
			 * conn.setRequestProperty("Content-Type",
			 * "application/x-www-form-urlencoded"); DataOutputStream outStream
			 * = new DataOutputStream(conn.getOutputStream());
			 * outStream.write(data); outStream.flush();
			 */
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- post() -->> ready");
			}
			outStream.flush();
			if (Log.D) {
				Log.d("HttpGroup", "id:" + httpSetting.getId()
						+ "- post() -->> ok");
			}
		}

		/**
		 *
		 */
		protected void connectionHandler2() {
			try {
				if (httpSetting.start > 0) {
					Log.i("HttpGroup", "httpSetting.start======"
							+ httpSetting.start);
					Log.i("HttpGroup", "httpSetting.end======"
							+ httpSetting.end);
					conn.setRequestProperty("Range", "bytes="
							+ httpSetting.start + "-" + httpSetting.end);
				}
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- connectionHandler2() -->> ");
				}
				conn.connect();
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- ResponseCode() -->> " + conn.getResponseCode());
				}
				// MyFileWriter.WriterInlog("HttpGroup__"+"id:" +
				// httpSetting.getId() +
				// "- ResponseCode() -->> "+conn.getResponseCode());
				// 保存头字段
				httpResponse.setHeaderFields(conn.getHeaderFields());
				// 打印所有头字段
				if (Log.D) {
					Map<String, List<String>> headerFields = conn
							.getHeaderFields();
					Set<Entry<String, List<String>>> entrySet = headerFields
							.entrySet();
					JSONObject jsonObject = new JSONObject();
					for (Entry<String, List<String>> entry : entrySet) {
						String name = (null == entry.getKey() ? "<null>"
								: entry.getKey());
						String value = new JSONArray(entry.getValue())
								.toString();
						jsonObject.put(name, value);
					}
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- headerFields -->> " + jsonObject.toString());
				}
				// int code = conn.getResponseCode();

				httpResponse.setCode(conn.getResponseCode());

				// 长度
				httpResponse.setLength(conn.getContentLength());
				HttpGroup.this.addMaxProgress(httpSetting.start
						+ Long.valueOf(httpResponse.getLength()).longValue());// 更新组进度上限
				// 类型
				httpResponse.setType(conn.getContentType());
				// if (httpSetting.getType() == HttpGroupSetting.TYPE_JSON) {
				// // 头字段所示类型与期望不符时作以下处理：
				// if (null == httpResponse.getType() ||
				// !httpResponse.getType().contains("application/json")) {
				// // 认证WIFI判断
				// String customHeaderField =
				// httpResponse.getHeaderField("X_Power_By");
				// if (null == customHeaderField ||
				// !customHeaderField.equals("gw.360buy.com")) {
				// Exception e = new
				// Exception(HttpError.EXCEPTION_MESSAGE_ATTESTATION_WIFI);
				// throwError(e);
				// connectionRetry = true;// 重试
				// return;
				// }
				// }
				// }

				if (httpResponse.getCode() != HttpURLConnection.HTTP_OK
						&& httpResponse.getCode() != HttpURLConnection.HTTP_PARTIAL) {
					HttpError error = new HttpError();
					error.setErrorCode(HttpError.RESPONSE_CODE);
					error.setResponseCode(httpResponse.getCode());
					throwError(error);
					connectionRetry = true;// 重试
					return;
				}

				if (httpSetting.isproxyed) {
					HttpGroup.mustDirectConnect = false;
				} else if (!httpSetting.isproxyed) {
					HttpGroup.mustDirectConnect = true;
				}
				setNeedinitAgent(false);

				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- ResponseCode() -->> ok");
				}
				// 保存或更新Cookies
				if (httpSetting.isSaveCookie()) {
					Log.i("HttpGroup", "id:" + httpSetting.getId()+" -->saveCookie");
					/*
					 * Map<String, List<String>> headerFields =
					 * conn.getHeaderFields(); Set<Entry<String, List<String>>>
					 * entrySet = headerFields.entrySet(); JSONObject jsonObject
					 * = new JSONObject(); for (Entry<String, List<String>>
					 * entry : entrySet) { String name = (null == entry.getKey()
					 * ? "<null>" : entry.getKey()); String value = new
					 * JSONArray(entry.getValue()).toString();
					 * jsonObject.put(name, value); }
					 */
					Map<String, List<String>> map = conn.getHeaderFields();

					List<String> list = map.get("set-cookie");
					if (list == null) {
						list = map.get("Set-Cookie");
					}
					if (list != null && list.size() != 0) {
						String cookie = null;
						StringBuilder builder = new StringBuilder();
						int i = 0;
						for (String str : list) {
							if (i < list.size() - 1) {
								cookie = builder.append(str).append(",")
										.toString();
							} else {
								cookie = builder.append(str).toString();
							}
							i++;
						}
						// String cookie =cookies;//
						// conn.getHeaderField("Set-Cookie");
						if (!TextUtils.isEmpty(cookie)) {
							if (Log.D) {
								Log.d("HttpGroup", "id:" + httpSetting.getId()
										+ "- cookies get -->> " + cookie);
							}
							cookies = cookie;// cookie.substring(0,
												// cookie.indexOf(";"));
						}
					}
				}
				/*
				 * String cookie=conn.getHeaderField("Set-Cookie"); if
				 * (!TextUtils.isEmpty(cookie)) { if (Log.D) {
				 * Log.d("HttpGroup", "id:" + httpSetting.getId() +
				 * "- cookies get -->> " + cookie); } cookies = cookie;//
				 * cookie.substring(0, // cookie.indexOf(";")); }
				 * 
				 * }
				 */

				// 输入流
				InputStream is = null;
				// 支持gzip
				String encoding = conn.getHeaderField("Content-Encoding");
				if ("gzip".equals(encoding)) {
					is = new GZIPInputStream(conn.getInputStream());
				} else {
					is = conn.getInputStream();
				}
				httpResponse.setInputStream(is);
				// try 为了保证释放 InputStream
				try {
					// 下一步
					if (Log.D) {
						Log.d("HttpGroup", "id:" + httpSetting.getId()
								+ "- ResponseCode() -->> ok nextHandler()");
					}
					nextHandler();
				} finally {
					try {
						if (null != httpResponse.getInputStream()) {
							httpResponse.getInputStream().close();
							httpResponse.setInputStream(null);// 去掉这个唯一的
							// InputStream
							// 持有
						}
						if (null != conn) {
							conn.disconnect();
							conn = null;
							// HttpResponse 里的 conn 暂时留着，用于方便事后排错或查询里面设定的属性 TODO
						}
					} catch (Exception e) {
					}
				}
			} catch (Exception e) {
				if (e instanceof SocketTimeoutException) {// 连接超时
					HttpError error = new HttpError();
					error.setErrorCode(HttpError.TIME_OUT);
					throwError(error);
				} else {// 其它
					HttpError httpError = new HttpError(e);
					throwError(httpError);
				}
				connectionRetry = true;// 重试
				return;
			}
		}

		/**
		 * 派发内容处理
		 */
		private final Handler contentHandler = new Handler() {
			@Override
			public void run() {
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- contentHandler -->>");
				}
				try {
					if (httpSetting.getType() == HttpGroupSetting.TYPE_JSON) {
						jsonContent();
					} else if (httpSetting.getType() == HttpGroupSetting.TYPE_IMAGE) {
						imageContent();
					} else if (httpSetting.getType() == HttpGroupSetting.TYPE_FILE) {
						fileContent();
					} else if (httpSetting.getType() == HttpGroupSetting.TYPE_XML) {
						xmlContent();
					} else if (httpSetting.getType() == HttpGroupSetting.TYPE_STREAM) {
						streamContent();
					}
					httpResponse.clean();
				} catch (Exception e) {
					HttpError httpError = new HttpError(e);
					throwError(httpError);
					connectionRetry = true;// 重试
					return;
				}
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- contentHandler -->> ok");
				}
			}
		};

		// 读取进度监听器
		private final IOUtil.ProgressListener ioProgressListener = new IOUtil.ProgressListener() {
			@Override
			public void notify(int incremental, long cumulant) {
				addProgress(incremental);// 组进度
				httpSetting.onProgress(
						httpSetting.start
								+ Long.valueOf(httpResponse.getLength())
										.longValue(), cumulant);// 请求进度
			}
		};

		// 连接完毕继续监听器
		private final HttpGroup.CompleteListener continueListener = new HttpGroup.CompleteListener() {
			@Override
			public void onComplete(Bundle bundle) {
				synchronized (HttpRequest.this) {
					HttpRequest.this.notify();
				}
			}
		};

		/**
		 * XML内容处理
		 */
		private void xmlContent() throws Exception {
			// // 头字段所示类型与期望不符时作以下处理：
			// if (null == httpResponse.getType() ||
			// !httpResponse.getType().contains("application/json")) {
			// HttpError error = new HttpError();
			// error.setErrorCode(HttpError.RESPONSE_CODE);
			// error.setResponseCode(404);
			// throwError(error);
			// connectionRetry = true;// 重试
			// return;
			// }

			// 走网络
			try {
				httpResponse.setInputData(IOUtil.readAsBytes(
						httpResponse.getInputStream(), ioProgressListener));
				if (Log.I) {
					Log.i("HttpGroup",
							"id:" + httpSetting.getId()
									+ "- response string -->> "
									+ httpResponse.getString());
				}
				// MyFileWriter.WriterInlog("HttpGroup" + "id:" +
				// httpSetting.getId() + "- response string -->> " +
				// httpResponse.getString());
			} catch (Exception e) {// 读取过程出错
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- json content connection read error -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// 重试
				return;
			}
			try {
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder builder = factory.newDocumentBuilder();
					byte[] bytes = httpResponse.getInputData();
					Log.i("HttpGroup",
							"id:"
									+ httpSetting.getId()
									+ "- response string -->> "
									+ StreamToolBox.loadStringFromStream(StreamToolBox
											.getByteArrayInputStream(bytes)));
					String xml = StreamToolBox
							.loadStringFromStream(StreamToolBox
									.getByteArrayInputStream(bytes));
					httpResponse.setString(xml);
					Document dom = builder.parse(
							StreamToolBox.getByteArrayInputStream(bytes),
							"UTF-8");
					Element root = dom.getDocumentElement();
					httpResponse.setRoot(root);
				} catch (Exception e) {
					// SAXParseException
					byte[] bytes = httpResponse.getInputData();
					String xml = StreamToolBox
							.loadStringFromStream(StreamToolBox
									.getByteArrayInputStream(bytes));
					jsonContent(xml);
				}
			} catch (Exception e) {// 根本不是 json 格式
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- Can not format json -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// 重试
				return;
			}

		}

		/**
		 * XML内容处理
		 */
		private void streamContent() throws Exception {
			// // 头字段所示类型与期望不符时作以下处理：
			// if (null == httpResponse.getType() ||
			// !httpResponse.getType().contains("application/json")) {
			// HttpError error = new HttpError();
			// error.setErrorCode(HttpError.RESPONSE_CODE);
			// error.setResponseCode(404);
			// throwError(error);
			// connectionRetry = true;// 重试
			// return;
			// }

			// 走网络
			try {
				httpResponse.setInputData(IOUtil.readAsBytes(
						httpResponse.getInputStream(), ioProgressListener));
				if (Log.I) {
					Log.i("HttpGroup",
							"id:" + httpSetting.getId()
									+ "- response string -->> "
									+ httpResponse.getString());
				}
			} catch (Exception e) {// 读取过程出错
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- json content connection read error -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// 重试
				return;
			}
			try {
				httpResponse.setByteArrayInputStream(new ByteArrayInputStream(
						httpResponse.getInputData()));
			} catch (Exception e) {// 根本不是 json 格式
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- Can not format json -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// 重试
				return;
			}
		}

		/**
		 * json 内容处理
		 */
		private void jsonContent() throws Exception {
			// 头字段所示类型与期望不符时作以下处理：
			// if (null == httpResponse.getType() ||
			// !(httpResponse.getType().contains("application/json") ||
			// httpResponse.getType().contains("text/plain")||httpResponse.getType().contains("text/html")))
			// {
			// HttpError error = new HttpError();
			// error.setErrorCode(HttpError.RESPONSE_CODE);
			// error.setResponseCode(404);
			// throwError(error);
			// connectionRetry = true;// 重试
			// return;
			// }

			// 走网络
			try {
				// charset = "gbk";
				httpResponse.setString(IOUtil.readAsString(
						httpResponse.getInputStream(), sCharset,
						ioProgressListener));

				if (Log.I) {
					Log.i("HttpGroup",
							"id:" + httpSetting.getId()
									+ "- response string -->> "
									+ httpResponse.getString());
				}
				// MyFileWriter.WriterInlog("HttpGroup"+"id:" +
				// httpSetting.getId() + "- response string -->> " +
				// httpResponse.getString());

			} catch (Exception e) {// 读取过程出错
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- json content connection read error -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// 重试
				return;
			}

			jsonContent(httpResponse.getString());
		}

		private void jsonContent(String content) throws Exception {
			try {
				httpResponse.setJsonObject(new JSONObjectProxy(new JSONObject(
						content)));
			} catch (JSONException e) {// 根本不是 json 格式
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- Can not format json -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// 重试
				return;
			}
			Integer jsonCode = null;
			try {
				jsonCode = Integer.valueOf(httpResponse.getJSONObject()
						.getString("code"));
			} catch (NumberFormatException e) {// jsonCode 数字格式错误
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- Can not format jsonCode -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;
				return;
			} catch (JSONException e) {// jsonCode 不存在错误
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- not find jsonCode -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// 重试
				return;
			}
			if (null != jsonCode && jsonCode != 0 && jsonCode != 52
					&& jsonCode != 1 && jsonCode != 60) {// jsonCode
				// 分支：指纹
				if (jsonCode.equals(9)) {
					queryMd5Key(continueListener);

					// 本线程工作暂停，等待网络线程获取Md5Key。
					synchronized (HttpRequest.this) {
						try {
							if (Log.D) {
								Log.d("HttpGroup", "id:" + httpSetting.getId()
										+ "- encrypt wait start -->> "
										+ httpSetting.getUrl());
							}
							HttpRequest.this.wait();
							if (Log.D) {
								Log.d("HttpGroup", "id:" + httpSetting.getId()
										+ "- encrypt wait end -->> "
										+ httpSetting.getUrl());
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}

					// 重试开头就会重新加密了
					connectionRetry = true;// 重试
					return;
				}
				/*
				 * // 分支：指纹 if (jsonCode.equals(10)) {
				 * 
				 * // 标记为需要加密 setModule(MODULE_STATE_ENCRYPT);
				 * 
				 * // 重试开头就会重新加密了 connectionRetry = true;// 重试 return; }
				 */
				if (jsonCode == -1 || // 属于出错并可重试的jsonCode
						jsonCode == -2) {
					// 其它不正确错误
					HttpError error = new HttpError();
					error.setErrorCode(HttpError.JSON_CODE);
					error.setJsonCode(jsonCode);
					error.setHttpResponse(httpResponse);
					throwError(error);
					connectionRetry = true;// 重试
					return;
				}

				if (jsonCode == 30 || jsonCode == 1 || jsonCode == 2) {
					final MyActivity myActivity = httpGroupSetting
							.getMyActivity();
					final String message = httpResponse.getJSONObject()
							.getStringOrNull("message");
					if (null != myActivity) {
						if (httpSetting.isShowToast()) {
							myActivity.post(new Runnable() {
								@Override
								public void run() {
									if (message == null)
										Toast.makeText(myActivity,
												R.string.server_busy,
												Toast.LENGTH_LONG).show();
									else
										Toast.makeText(myActivity, message,
												Toast.LENGTH_LONG).show();
								}
							});
						}
					}
					// 其它不正确错误
					HttpError error = new HttpError();
					error.setErrorCode(HttpError.JSON_CODE);
					error.setJsonCode(jsonCode);
					error.setHttpResponse(httpResponse);
					error.setNoRetry(true);
					throwError(error);
					// connectionRetry = true;// 不重试
					return;
				}
			}
		}

		/**
		 * image 内容处理
		 */
		private void imageContent() throws Exception {
			// 头字段所示类型与期望不符时作以下处理：
			if (null == httpResponse.getType()
					|| !httpResponse.getType().contains("image/")) {
				HttpError error = new HttpError();
				error.setErrorCode(HttpError.RESPONSE_CODE);
				error.setResponseCode(404);
				throwError(error);
				connectionRetry = true;// 重试
				return;
			}
			// 走网络
			try {
				httpResponse.setInputData(IOUtil.readAsBytes(
						httpResponse.getInputStream(), ioProgressListener));
				// Bitmap bitmap =
				// BitmapFactory.decodeByteArray(httpResponse.getInputData(), 0,
				// httpResponse.getInputData().length, getBitmapOpt());
				Bitmap bitmap = ImageTool.getImage(new ByteArrayInputStream(
						httpResponse.getInputData()));
				bitmap = ImageUtils.CropForExtraWidth(bitmap);
				if (bitmap != null) {
					httpResponse.setBitmap(bitmap);
					httpResponse.setDrawable(new BitmapDrawable(bitmap));
				}
			} catch (Throwable e) {// 读取过程出错
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- image content connection read error -->> ");
				}
				HttpError httpError = new HttpError(e);
				httpError.setNoRetry(true);
				throwError(httpError);
				return;
			}
			// 走缓存
		}

		/**
		 * file 内容处理
		 */
		private void fileContent() {
			// 所示类型与期望不符时作以下处理：
			// 管它是什么东西都尝试写到文件里面去
			try {
				FileGuider savePath = httpSetting.getSavePath();

				if (null != savePath) {
					// 确定保存路径
				}

				// TODO 应该判断如果 savePath 为 null
				// TODO 可以而提供绝对路径，也可以提供相对路径，应该有多种方式。
				savePath.setAvailableSize(httpSetting.start
						+ httpResponse.getLength());// 所需空间大小
				File file = new File(savePath.getFilePath());
				// IOUtil.readAsFile(httpResponse.getInputStream(),
				// fileOutputStream, ioProgressListener, this);
				Log.i("zhoubo", "11111111111111111111");
				//IOUtil.readAsFile(httpResponse.getInputStream(), file,
				//		ioProgressListener, httpSetting.start, this);
				IOUtil.readAsFile(httpResponse.getInputStream(), file,
						ioProgressListener, httpSetting.start, this);
				
				
				
				Log.i("zhoubo", "2222222222222222222");
				// File dir = MyApplication.getInstance().getFilesDir();
				File apkFilePath = new File(savePath.getFilePath());
				// if (Log.D) {
				// Log.d("HttpGroup", "id:" + httpSetting.getId() +
				// "- download() apkFilePath -->> " + apkFilePath);
				// }
				// if (isStop()) {
				// apkFilePath.delete();
				// }
				httpResponse.setSaveFile(apkFilePath);
			} catch (Exception e) {// 读取过程出错
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- file content connection read error -->> ", e);
				}
				HttpError httpError = new HttpError(e);
				throwError(httpError);
				connectionRetry = true;// 重试
				return;
			}
		}

		/**
		 * 类型定位
		 */
		public void typeHandler() {
			nextHandler();
		}

		private Options getBitmapOpt() {
			BitmapFactory.Options opt = new BitmapFactory.Options();
			if (httpResponse.getLength() > 1024 * 64) {
				if (Log.D) {
					Log.d("HttpGroup", "id:" + httpSetting.getId()
							+ "- opt.inSampleSize -->> " + 2);
				}
				opt.inSampleSize = 2;
			}
			return opt;
		}

		protected static final int MODULE_STATE_DISABLE = 0;// 禁用
		protected static final int MODULE_STATE_ENCRYPT = 3;// 加密

		/**
		 * 检查状态
		 */
		protected boolean checkModule(int state) {
			if (null != httpSetting.getFunctionId() && //
					null != mModules) {
				Integer state_ = mModules.getIntOrNull(httpSetting
						.getFunctionId());
				if (null != state_ && state == state_) {
					return true;
				}
			}
			return false;
		}

		/**
		 * 设置状态
		 */
		protected void setModule(int state) {
			if (null != httpSetting.getFunctionId() && //
					null != mModules) {
				try {
					mModules.put(httpSetting.getFunctionId(), state);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}

	}

	/**
	 * 响应封装类
	 */
	public class HttpResponse {

		private InputStream inputStream;
		private byte[] inputData;
		private InputStream byteArrayInputStream;
		// private SoftReference<byte[]> softReferenceInputData;
		private Bitmap bitmap;
		private SoftReference<Bitmap> softReferenceBitmap;
		private Drawable drawable;
		private SoftReference<Drawable> softReferenceDrawable;
		private File saveFile;
		private String string;
		private JSONObjectProxy jsonObject;
		private Element root;

		private HttpURLConnection httpURLConnection;
		private Map<String, List<String>> headerFields;
		HttpSetting httpSetting;
		// public HttpSetting getHttpSetting() {
		// return this.httpSetting;
		// }

		private int code;// 响应码
		private long length;// 数据量
		private String type;// 媒体类型

		// 清理
		private void imageClean() {
			// softReferenceInputData = new SoftReference<byte[]>(inputData);
			softReferenceBitmap = new SoftReference<Bitmap>(bitmap);
			softReferenceDrawable = new SoftReference<Drawable>(drawable);
			inputData = null;
			bitmap = null;
			drawable = null;
		}

		public Element getRoot() {
			return root;
		}

		public void setRoot(Element root) {
			this.root = root;
		}

		public InputStream getByteArrayInputStream() {
			return byteArrayInputStream;
		}

		public void setByteArrayInputStream(InputStream byteArrayInputStream) {
			this.byteArrayInputStream = byteArrayInputStream;
		}

		/**
		 * 当直接从缓存中取得数据而无需网络连接时，可能会使用此构造函数
		 */
		public HttpResponse() {
		}

		/**
		 * 当直接从缓存中取得数据而无需网络连接时，可能会使用此构造函数
		 */
		public HttpResponse(Drawable drawable) {
			this.drawable = drawable;
		}

		public HttpResponse(HttpURLConnection httpConnection) {
			this.httpURLConnection = httpConnection;
		}

		public void clean() {
			this.httpURLConnection = null;
		}

		public void setInputStream(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		public InputStream getInputStream() {
			return inputStream;
		}

		public void setJsonObject(JSONObjectProxy jsonObject) {
			this.jsonObject = jsonObject;
		}

		public JSONObjectProxy getJSONObject() {
			return jsonObject;
		}

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}

		public int getCode() {
			return code;
		}

		public void setCode(int code) {
			this.code = code;
		}

		public long getLength() {
			return length;
		}

		public void setLength(long length) {
			this.length = length;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public Drawable getDrawable() {
			if (null != drawable) {
				Drawable drawable = this.drawable;
				imageClean();
				return drawable;
			} else {
				if (softReferenceDrawable == null)
					
					return new ExceptionDrawable(MZBookApplication.getInstance(),
							MZBookApplication.getInstance().getString(
									R.string.no_image));
				return softReferenceDrawable.get();
			}
		}

		public Drawable getThumbDrawable(float targetWidth, float targetHeight) {

			Bitmap sourceBitmap = getBitmap();

			// 计算缩放比例1（保持比例）
			int sourceWidth = sourceBitmap.getWidth();
			int sourceHeight = sourceBitmap.getHeight();
			float scale;
			if (sourceWidth > sourceHeight) {
				scale = targetWidth / sourceWidth;
			} else {
				scale = targetHeight / sourceHeight;
			}

			// 需要缩小
			if (scale < 1) {
				// 计算缩放比例2（保持比例）
				int width = Math.round(scale * sourceWidth);
				int height = Math.round(scale * sourceHeight);

				// 有损缩放
				Bitmap bitmap = Bitmap.createScaledBitmap(sourceBitmap, width,
						height, false);
				setBitmap(bitmap);
				sourceBitmap.recycle();
				setDrawable(new BitmapDrawable(bitmap));
				return getDrawable();// 这里会清理资源了
			}

			// 不需要缩小
			return getDrawable();// 这里会清理资源了
		}

		public void setDrawable(Drawable drawable) {
			this.drawable = drawable;
		}

		public byte[] getInputData() {
			return inputData;
		}

		public void setInputData(byte[] inputData) {
			this.inputData = inputData;
		}

		public Bitmap getBitmap() {
			if (null != bitmap) {
				Bitmap bitmap = this.bitmap;
				imageClean();
				return bitmap;
			} else {
				if (softReferenceBitmap == null)
					return BitmapFactory.decodeResource(MZBookApplication
							.getInstance().getResources(),
							R.drawable.image_logo);
				return softReferenceBitmap.get();
			}
		}

		public void setBitmap(Bitmap bitmap) {
			if (null == bitmap) {
				throw new RuntimeException("bitmap is null");
			}
			this.bitmap = bitmap;
		}

		private String imagePath = null;

		public void setImagePath(String imagePath) {
			this.imagePath = imagePath;
		}

		public String getImagePath() {
			return imagePath;
		}

		public File getSaveFile() {
			return saveFile;
		}

		public void setSaveFile(File saveFile) {
			this.saveFile = saveFile;
		}

		public Map<String, List<String>> getHeaderFields() {
			return headerFields;
		}

		public void setHeaderFields(Map<String, List<String>> headerFields) {
			this.headerFields = headerFields;
		}

		public String getHeaderField(String key) {
			if (null == headerFields) {
				return null;
			}
			List<String> listStr = headerFields.get(key);
			if (null == listStr || listStr.size() < 1) {
				return null;
			}
			return listStr.get(0);
		}

	}

	/* 组的被子调用事件 */
	protected void onStart() {
		if (null != onGroupStartListener)
			onGroupStartListener.onStart();
		onGroupStartListener = null;
	}

	// 为了 end -->>
	protected void onEnd() {
		if (null != onGroupEndListener)
			onGroupEndListener.onEnd();
		onGroupEndListener = null;
	}

	private int completesCount = 0;

	protected void addCompletesCount() {
		this.completesCount += 1;
		if (completesCount == httpList.size())
			onEnd();
	}

	// <<-- 为了 end

	protected void onError() {
		if (null != onGroupErrorListener)
			onGroupErrorListener.onError();
		onGroupErrorListener = null;
	}

	// 为了 progress -->>
	private void onProgress(long maxProgress, long progress) {
		if (null != onGroupProgressListener)
			onGroupProgressListener.onProgress(maxProgress, progress);
	}

	private long maxProgress = 0;
	private long progress = 0;

	protected void addMaxProgress(long maxProgress) {// TODO
														// 多线程调用这个方法，这里可能会有线程问题
		this.maxProgress += maxProgress;
		onProgress(this.maxProgress, this.progress);
	}

	protected void addProgress(int progress) {// TODO 多线程调用这个方法，这里可能会有线程问题
		this.progress += progress;
		onProgress(this.maxProgress, this.progress);
	}

	// <<-- 为了 progress

	// 为了 step -->>
	private void onStep(int maxStep, int step) {
		if (null != onGroupStepListener)
			onGroupStepListener.onStep(maxStep, step);
	}

	private int maxStep = 0;
	private int step = 0;

	protected void addMaxStep(int maxStep) {// TODO 多线程调用这个方法，这里可能会有线程问题
		this.maxStep += maxStep;
		onStep(this.maxStep, this.step);
	}

	protected void addStep(int step) {// TODO 多线程调用这个方法，这里可能会有线程问题
		this.step += step;
		onStep(this.maxStep, this.step);
	}

	// <<-- 为了 step

	/* 组监听器 - 存放 */
	private OnGroupStartListener onGroupStartListener;
	private OnGroupEndListener onGroupEndListener;
	private OnGroupErrorListener onGroupErrorListener;
	private OnGroupProgressListener onGroupProgressListener;
	private OnGroupStepListener onGroupStepListener;

	public void setOnGroupStartListener(
			OnGroupStartListener onGroupStartListener) {
		this.onGroupStartListener = onGroupStartListener;
	}

	public void setOnGroupEndListener(OnGroupEndListener onGroupEndListener) {
		this.onGroupEndListener = onGroupEndListener;
	}

	public void setOnGroupErrorListener(
			OnGroupErrorListener onGroupErrorListener) {
		this.onGroupErrorListener = onGroupErrorListener;
	}

	public void setOnGroupProgressListener(
			OnGroupProgressListener onGroupProgressListener) {
		this.onGroupProgressListener = onGroupProgressListener;
	}

	public void setOnGroupStepListener(OnGroupStepListener onGroupStepListener) {
		this.onGroupStepListener = onGroupStepListener;
	}

	/* 组监听器 - 定义 */
	public interface OnGroupStartListener {
		void onStart();
	}

	public interface OnGroupEndListener {
		void onEnd();
	}

	public interface OnGroupErrorListener {
		void onError();
	}

	public interface OnGroupProgressListener {
		void onProgress(long max, long progress);
	}

	public interface OnGroupStepListener {
		void onStep(int max, int step);
	}

	/* HttpTask监听器 - 定义 */
	public interface HttpTaskListener {

	}

	public interface OnStartListener extends HttpTaskListener {

		void onStart();

	}

	public interface OnEndListener extends HttpTaskListener {

		void onEnd(HttpResponse httpResponse);

	}

	public interface OnErrorListener extends HttpTaskListener {

		void onError(HttpError error);

	}

	public interface OnReadyListener extends HttpTaskListener {

		void onReady(HttpSettingParams httpSettingParams);

	}

	public interface OnProgressListener extends HttpTaskListener {

		void onProgress(long max, long progress);

	}

	public interface OnCommonListener extends OnEndListener, OnErrorListener,
			OnReadyListener {

	}

	public interface OnAllListener extends OnStartListener, OnEndListener,
			OnErrorListener, OnProgressListener {

	}

	public interface CustomOnAllListener extends OnAllListener {
		@Override
		void onStart();

		@Override
		void onEnd(HttpResponse httpResponse);

		@Override
		void onError(HttpError error);
	}

	/**
	 * 错误信息封装
	 */
	public static class HttpError {

		public static final int EXCEPTION = 0;
		public static final int TIME_OUT = 1;
		public static final int RESPONSE_CODE = 2;
		public static final int JSON_CODE = 3;

		public static final String EXCEPTION_MESSAGE_ATTESTATION_WIFI = "attestation WIFI";
		public static final String EXCEPTION_MESSAGE_NO_CACHE = "no cache";

		/**
		 * 出错的方向
		 */
		private int errorCode;

		/**
		 * 被捕获的responseCode
		 */
		private int responseCode;

		/**
		 * 被捕获的jsonCode
		 */
		private int jsonCode;

		/**
		 * 备用
		 */
		private String message;

		/**
		 * 被捕获的异常
		 */
		private Throwable exception;

		/**
		 * 第几次尝试
		 */
		private int times;

		/**
		 * 无需重试
		 */
		private boolean noRetry;

		private HttpResponse httpResponse;

		public HttpError() {

		}

		public HttpError(Throwable exception) {
			this.errorCode = EXCEPTION;
			this.exception = exception;
		}

		public int getErrorCode() {
			return errorCode;
		}

		public String getErrorCodeStr() {
			switch (errorCode) {
			case EXCEPTION:
				return "EXCEPTION";
			case TIME_OUT:
				return "TIME_OUT";
			case RESPONSE_CODE:
				return "RESPONSE_CODE";
			case JSON_CODE:
				return "JSON_CODE";
			default:
				return "UNKNOWN";
			}
		}

		public void setErrorCode(int errorCode) {
			this.errorCode = errorCode;
		}

		public int getResponseCode() {
			return responseCode;
		}

		public void setResponseCode(int responseCode) {
			this.responseCode = responseCode;
		}

		public int getJsonCode() {
			return jsonCode;
		}

		public void setJsonCode(int jsonCode) {
			this.jsonCode = jsonCode;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public Throwable getException() {
			return exception;
		}

		public void setException(Throwable exception) {
			this.exception = exception;
		}

		public int getTimes() {
			return times;
		}

		public void setTimes(int times) {
			this.times = times;
		}

		public HttpResponse getHttpResponse() {
			return httpResponse;
		}

		public void setHttpResponse(HttpResponse httpResponse) {
			this.httpResponse = httpResponse;
		}

		@Override
		public String toString() {
			if (null != getException()) {
				if (Log.D) {
					Log.d("HttpGroup", "HttpError Exception -->> ",
							getException());
				}
			}
			return "HttpError [errorCode=" + getErrorCodeStr() + ", exception="
					+ exception + ", jsonCode=" + jsonCode + ", message="
					+ message + ", responseCode=" + responseCode + ", time="
					+ times + "]";
		}

		/**
		 * 是否无需重试
		 */
		public boolean isNoRetry() {
			return noRetry;
		}

		/**
		 * 设置是否无需重试
		 */
		public void setNoRetry(boolean noRetry) {
			this.noRetry = noRetry;
		}

	}

	/**
	 * Copyright 2011 Jingdong Android Mobile Application
	 * 
	 * @author lijingzuo
	 * 
	 *         Time: 2011-1-10 下午12:52:06
	 * 
	 *         Name:
	 * 
	 *         Description: 连接组设置封装
	 */
	public static class HttpGroupSetting {

		public static final int PRIORITY_FILE = 500;
		public static final int PRIORITY_JSON = 1000;
		public static final int PRIORITY_IMAGE = 5000;
		public static final int PRIORITY_XML = 10000;
		public static final int PRIORITY_STREAM = 20000;

		public static final int TYPE_FILE = RequestEntry.TYPE_FILE;
		public static final int TYPE_JSON = RequestEntry.TYPE_JOSN;
		public static final int TYPE_IMAGE = RequestEntry.TYPE_IMAGE;
		public static final int TYPE_XML = RequestEntry.TYPE_XML;
		public static final int TYPE_STREAM = RequestEntry.TYPE_STREAM;

		private MyActivity myActivity;
		private int priority;
		private int type;

		public MyActivity getMyActivity() {
			return myActivity;
		}

		public void setMyActivity(MyActivity myActivity) {
			this.myActivity = myActivity;
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
			if (0 == priority) {
				switch (type) {
				case TYPE_JSON:
					setPriority(PRIORITY_JSON);
					break;
				case TYPE_IMAGE:
					setPriority(PRIORITY_IMAGE);
					break;
				}
			}
		}

	}

	public interface HttpSettingParams 
    {
		void putJsonParam(String key, Object value);
		void putMapParams(String key, String value);
	}

	
	/**
	 * Copyright 2010 Jingdong Android Mobile Application
	 * 
	 * @author lijingzuo
	 * 
	 *         Time: 2010-12-27 下午05:26:55
	 * 
	 *         Name:
	 * 
	 *         Description: 连接信息封装
	 */
	public static class HttpSetting implements HttpSettingParams, Cloneable 
    {

		public static final int EFFECT_NO = 0;// 不要效果
		public static final int EFFECT_DEFAULT = 1;// 默认效果

		public static final int EFFECT_STATE_NO = 0;
		public static final int EFFECT_STATE_YES = 1;

		public static final int CACHE_MODE_AUTO = 0;
		public static final int CACHE_MODE_ONLY_CACHE = 1;
		public static final int CACHE_MODE_ONLY_NET = 2;
		private boolean isShowToast = true;
		private boolean isUserTempCookies=false;
		private int id;
		private String host;
		private String functionId;
		private String url; // firstHandler()会进行url的组装，组装完成后赋值给semiUrl，然后再beforeConnection()函数里把semi设置成finalurl
		private String semiUrl;
		private String finalUrl; // 加密Url和普通Url都在最后拼接成finalUrl
		private FileGuider savePath;
		private JSONObject jsonParams;
		private Map<String, String> mapParams;
		private OnStartListener onStartListener;
		private OnProgressListener onProgressListener;
		private OnEndListener onEndListener;
		private OnErrorListener onErrorListener;
		private OnReadyListener onReadyListener;
		private int connectTimeout;
		private int readTimeout;
		private String md5;
		private int type;
		private int priority;// 0:继承
		private boolean post = "post".equals(Configuration.getProperty(
				Configuration.REQUEST_METHOD, "post"));
		private boolean notifyUser = false;
		private boolean notifyUserWithExit = false;// 仅仅控制了一下文字，退出的逻辑由监听器处理。
		private boolean localMemoryCache = false;
		private boolean localFileCache = false;
		private boolean isURLEncoder = true;
		private long localFileCacheTime = CacheTimeConfig.DEFAULT;// 0:永久保存（不允许出现，因为有无限膨胀的危险）
		private boolean needGlobalInitialization = true;
		private boolean isNeedUUID = true;
		private boolean isNeedBaseParamStr = true;
		private int effect = 1;// 0:不要效果,1:默认效果
		private int effectState = 0;// 0:未处理,1:已处理
		private int cacheMode = 0;// 缓存模式。0:自动模式（有缓存用缓存，没缓存用网络）,1:只使用缓存,2:只使用网络
		private HttpRequest httpRequest;
		private long start; // 下载起点
		private long end; // 下载起点
		private boolean isSuffix = true; //
		private boolean isSaveCookie = true;
		private boolean isEncoder = false; // body 是否加密 true 为加密 false 为不加密
		private BodyEncodeEntity encodeEntity;
		private Runnable successRunnable;
		private Runnable failedRunnable;
		private boolean isproxyed;
		private boolean isIntercepted=false;
//		private boolean isSingleBook=false;//是否是单本书。
//		public boolean isSingleBook() {
//			return isSingleBook;
//		}
//
//		public void setSingleBook(boolean isSingleBook) {
//			this.isSingleBook = isSingleBook;
//		}

        public HttpSetting()
        {
        
            
        } 
		public boolean isIntercepted() {
			return isIntercepted;
		}

		public void setIntercepted(boolean isIntercepted) {
			this.isIntercepted = isIntercepted;
		}

		private OnReleaseHttpsetingLintenser releaseHttpsettings;
 		public OnReleaseHttpsetingLintenser getReleaseHttpsettings() {
			return releaseHttpsettings;
		}

		public void setReleaseHttpsettings(
				OnReleaseHttpsetingLintenser releaseHttpsettings) {
			this.releaseHttpsettings = releaseHttpsettings;
		}

		public boolean isproxyed() {
			return isproxyed;
		}

		public void setProxyed(boolean isproxyed) {
			this.isproxyed = isproxyed;
		}

		public boolean isNeedBaseParamStr() {
			return isNeedBaseParamStr;
		}

		public void setNeedBaseParamStr(boolean isNeedBaseParamStr) {
			this.isNeedBaseParamStr = isNeedBaseParamStr;
		}

		public Runnable getSuccessRunnable() {
			return successRunnable;
		}

		public void setSuccessRunnable(Runnable successRunnable) {
			this.successRunnable = successRunnable;
		}

		public Runnable getFailedRunnable() {
			return failedRunnable;
		}

		public void setFailedRunnable(Runnable failedRunnable) {
			this.failedRunnable = failedRunnable;
		}

		public boolean isShowToast() {
			return isShowToast;
		}

		public void setShowToast(boolean isShowToast) {
			this.isShowToast = isShowToast;
		}
		
		public boolean isUserTempCookies() {
			return isUserTempCookies;
		}

		public void setUserTempCookies(boolean isUserTempCookies) {
			this.isUserTempCookies = isUserTempCookies;
		}
		
		@Override
		public Object clone() {
			HttpSetting httpSetting = null;

			try {
				httpSetting = (HttpSetting) super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}

			if (httpSetting != null) {
				httpSetting.setJsonParams(this.getJsonParams());
			}

			return httpSetting;
		}

		public BodyEncodeEntity getEncodeEntity() {
			return encodeEntity;
		}

		
		public String creatFinalUrl(){
			if(getHost()==null){
				this.setHost(Configuration.getProperty(Configuration.HOST));
			}
			if (null != getFunctionId()){
				putMapParams("functionId",getFunctionId());
			}
	          if(getUrl() == null) {
				setUrl("http://" + getHost()
						+ "/client.action");
			}
			if (isPost()) {
				// POST
				if (null != this.getMapParams()) {
						this.setSemiUrl(this.getUrl()
								+ "?"
								+ "functionId="
								+ this.getMapParams().get(
										"functionId"));
				}
			} else {
				// GET
				if (null != this.getMapParams()) {

					StringBuilder url = new StringBuilder(
							this.getUrl());
					url.append("?");

					Map<String, String> mapParams = this.getMapParams();
					Set<String> keySet = mapParams.keySet();
					for (Iterator<String> iterator = keySet.iterator(); iterator
							.hasNext();) {
						String key = iterator.next();
						String value = mapParams.get(key);
						url.append(key).append("=").append(value);
						if (iterator.hasNext()) {
							url.append("&");
						}
					}
						this.setSemiUrl(url.toString());
					}
				}
			
			if(this.getSemiUrl()==null){
				setSemiUrl(getUrl());
			}
			Log.i("HttpSetting", " getSemiUrl()========================="+getSemiUrl());
			return this.getSemiUrl();
		}
		
		public void setEncodeEntity(BodyEncodeEntity encodeEntity) {
			this.encodeEntity = encodeEntity;
		}

		public void setIsEncoder(boolean isEncoder) {
			this.isEncoder = isEncoder;
		}

		public boolean getIsEncoder() {
			return isEncoder;
		}

		public boolean isSaveCookie() {
			return isSaveCookie;
		}

		public void setSaveCookie(boolean isSaveCookie) {
			this.isSaveCookie = isSaveCookie;
		}

		public long getEnd() {
			return end;
		}

		public void setEnd(long end) {
			this.end = end;
		}

		public boolean isSuffix() {
			return isSuffix;
		}

		public void setSuffix(boolean isSuffix) {
			this.isSuffix = isSuffix;
		}

		public HttpRequest getHttpRequest() {
			return httpRequest;
		}

		public void setHttpRequest(HttpRequest httpRequest) {
			this.httpRequest = httpRequest;
		}

		public long getStart() {
			return start;
		}

		public void setStart(long start) {
			this.start = start;
		}

		public String getFunctionId() {
			return functionId;
		}

		public void setFunctionId(String functionId) {
			this.functionId = functionId;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
             
			this.url = url;
		}

		public String getSemiUrl() {
			return semiUrl;
		}

		public void setSemiUrl(String semiUrl) 
		{
//           __b.s("semUrl:"+semiUrl);
            this.semiUrl = semiUrl;
		}

		public String getFinalUrl() {
			return finalUrl;
		}

		public void setFinalUrl(String finalUrl) 
        {
			
//           __b.s("finalUrl:"+finalUrl);
            this.finalUrl = finalUrl;
		}

		public JSONObject getJsonParams() {
			return jsonParams;
		}

		public boolean isURLEncoder() {
			return isURLEncoder;
		}

		public void setURLEncoder(boolean isURLEncoder) {
			this.isURLEncoder = isURLEncoder;
		}

		/**
		 * 批量添加参数
		 */
		@Deprecated
		public void setJsonParams(JSONObject params) {
			if (null == params) {
				return;
			}
			try {
				this.jsonParams = new JSONObject(params.toString());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void putJsonParam(String key, Object value) {
			if (null == this.jsonParams) {
				this.jsonParams = new JSONObject();
			}
			try {
				this.jsonParams.put(key, value);
			} catch (JSONException e) {
				if (Log.D) {
					Log.d("HttpGroup", "JSONException -->> ", e);
				}
			}
		}

		public Map<String, String> getMapParams() {
			return mapParams;
		}

		/**
		 * 批量添加参数
		 */
		@Deprecated
		public void setMapParams(Map<String, String> mapParams) {
			if (null == mapParams) {
				return;
			}
			Set<String> keySet = mapParams.keySet();
			for (String key : keySet) {
				putMapParams(key, mapParams.get(key));
			}
		}

		@Override
		public void putMapParams(String key, String value) {
			if (null == this.mapParams) {
				this.mapParams = new HashMap<String, String>();
			}
			if (isURLEncoder) {
				try {
					value = URLEncoder.encode(value, sCharset);
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException(e);
				}
			}
			this.mapParams.put(key, value);
		}

		public int getConnectTimeout() {
			return connectTimeout;
		}

		public void setConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

		public OnStartListener getOnStartListener() {
			return onStartListener;
		}

		public OnProgressListener getOnProgressListener() {
			return onProgressListener;
		}

		public OnEndListener getOnEndListener() {
			return onEndListener;
		}

		public OnErrorListener getOnErrorListener() {
			return onErrorListener;
		}

		public OnReadyListener getOnReadyListener() {
			return onReadyListener;
		}

		public void setListener(HttpTaskListener httpTaskListener) {
			if (httpTaskListener == null) {
				this.onErrorListener = null;
				this.onStartListener = null;
				this.onProgressListener = null;
				this.onEndListener = null;
				this.onReadyListener = null;
			}
			if (httpTaskListener instanceof CustomOnAllListener) {
				setEffect(0);// 没有效果
			}
			if (httpTaskListener instanceof DefaultEffectHttpListener) {
				setEffectState(1);// 已处理
			}
			if (httpTaskListener instanceof OnErrorListener) {
				this.onErrorListener = (OnErrorListener) httpTaskListener;
			}
			if (httpTaskListener instanceof OnStartListener) {
				this.onStartListener = (OnStartListener) httpTaskListener;
			}
			if (httpTaskListener instanceof OnProgressListener) {
				this.onProgressListener = (OnProgressListener) httpTaskListener;
			}
			if (httpTaskListener instanceof OnEndListener) {
				this.onEndListener = (OnEndListener) httpTaskListener;
			}
			if (httpTaskListener instanceof OnReadyListener) {
				this.onReadyListener = (OnReadyListener) httpTaskListener;
			}
		}

		public void onStart() {
			if (null != onStartListener) {
				onStartListener.onStart();
			}
		}
		
		public void onEnd(HttpResponse httpResponse) {
			if (null != onEndListener) 
			{
				onEndListener.onEnd(httpResponse);
			}
			if(releaseHttpsettings!=null)
			{
				releaseHttpsettings.releaseHttpSettings();
			}
		}

		public void onError(HttpError httpError) {
			if (null != onErrorListener) {
				onErrorListener.onError(httpError);
			}
			if(releaseHttpsettings!=null){
				releaseHttpsettings.releaseHttpSettings();
			}
		}

		public void onProgress(long max, long progress) {
			if (null != onProgressListener) {
				onProgressListener.onProgress(max, progress);
			}
		}

		public String getMd5() {
			if (null == md5) {
				String urlTempStr = getSemiUrl();
				if (null == urlTempStr) {
					return null;
				}
				int start = 0;
				for (int i = 0; i < 3; i++) {
					start = urlTempStr.indexOf("/", start + 1);
				}
				if (start == -1) {
					return null;
				}
				String urlPath = getSemiUrl().substring(start);
				if (isPost()) {
					md5 = Md5Encrypt.md5(urlPath + getJsonParams());
				} else {
					md5 = Md5Encrypt.md5(urlPath);
				}
				if (Log.D) {
					Log.d("HttpGroup", "urlPath -->> " + urlPath + " md5 -->> "
							+ md5);
				}
			}
			return md5;
		}

		public void setMd5(String md5) {
			this.md5 = md5;
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public boolean isPost() {
			return post;
		}

		public void setPost(boolean post) {
			this.post = post;
		}

		public int getReadTimeout() {
			return readTimeout;
		}

		public void setReadTimeout(int readTimeout) {
			this.readTimeout = readTimeout;
		}

		public boolean isNotifyUser() {
			return notifyUser;
		}

		public void setNotifyUser(boolean notifyUser) {
			this.notifyUser = notifyUser;
		}

		public boolean isLocalMemoryCache() {
			return localMemoryCache;
		}

		public void setLocalMemoryCache(boolean localMemoryCache) {
			this.localMemoryCache = localMemoryCache;
		}

		public boolean isLocalFileCache() {
			return localFileCache;
		}

		public void setLocalFileCache(boolean localFileCache) {
			this.localFileCache = localFileCache;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public long getLocalFileCacheTime() {
			return localFileCacheTime;
		}

		public void setLocalFileCacheTime(long localFileCacheTime) {
			this.localFileCacheTime = localFileCacheTime;
		}

		public FileGuider getSavePath() {
			return savePath;
		}

		/**
		 * 注意不要把同一个对象给多个网络连接
		 */
		public void setSavePath(FileGuider savePath) {
			this.savePath = savePath;
		}

		public boolean isNotifyUserWithExit() {
			return notifyUserWithExit;
		}

		public void setNotifyUserWithExit(boolean notifyUserOrExit) {
			this.notifyUserWithExit = notifyUserOrExit;
		}

		public boolean isNeedUUID() {
			return isNeedUUID;
		}

		public void setNeedUUID(boolean isNeedUUID) {
			this.isNeedUUID = isNeedUUID;
		}

		public boolean isNeedGlobalInitialization() {
			return needGlobalInitialization;
		}

		public void setNeedGlobalInitialization(boolean needGlobalInitialization) {
			this.needGlobalInitialization = needGlobalInitialization;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getEffect() {
			return effect;
		}

		public void setEffect(int effect) {
			this.effect = effect;
		}

		public int getEffectState() {
			return effectState;
		}

		public void setEffectState(int effectState) {
			this.effectState = effectState;
		}

		public int getCacheMode() {
			return cacheMode;
		}

		/**
		 * 缓存模式。0:自动模式（有缓存用缓存，没缓存用网络）,1:只使用缓存,2:只使用网络
		 * CACHE_MODE_AUTO、CACHE_MODE_ONLY_CACHE、CACHE_MODE_ONLY_NET
		 */
		public void setCacheMode(int cacheMode) {
			this.cacheMode = cacheMode;
		}

	}

	public static String mergerUrlAndParams(String urlStr,
			Map<String, String> params) {

		if (null == params) {
			return urlStr;
		}

		Set<String> keySet = params.keySet();
		if (null == keySet || keySet.isEmpty()) {
			return urlStr;
		}

		StringBuilder url = new StringBuilder(urlStr);
		int i = urlStr.indexOf("?");
		if (i == -1) {
			url.append("?");
		} else {
			String queryString = urlStr.substring(i + 1);
			if (!TextUtils.isEmpty(queryString) && !queryString.endsWith("&")) {
				url.append("&");
			}
		}

		for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext();) {
			String key = iterator.next();
			String value = params.get(key);
			url.append(key).append("=").append(value);
			if (iterator.hasNext()) {
				url.append("&");
			}
		}

		return url.toString();
	}

	// public static void cleanCookies() {
	// cookies = null;
	// }

	public static void setCookies(String str) {
		cookies = str;
	}
	/**
	 * 当设置为自动登录时，需要登录完成后，在去处理其他网络请求。
	 */
//	private  static ArrayList<HttpSetting>  httpSettings=new ArrayList<HttpGroup.HttpSetting>();
// 	public   boolean checkAutoLogin(HttpSetting httpSetting) {
//		 //如果是自动登录：
//		boolean autoLogin = LoginUser.getInstance().isAutoLogin;
//		final String userName = LoginUser.getInstance().userName;
//		final String password = LoginUser.getInstance().psw;
//		if (!autoLogin || userName.length() <= 0 || password.length() <= 0 || LoginUser.isLogin())
//		{
//			return false;
//		};
// 		if(!httpSetting.getFunctionId().equals("sessionKey") && !httpSetting.getFunctionId().equals("unionLogin")
// 				&& !httpSetting.getFunctionId().equals("startScreen") && !httpSetting.getFunctionId().equals("rsaPublicKey")){
//			httpSetting.setIntercepted(true);
// 			httpSettings.add(httpSetting);
//			return true;
//		}else if(httpSetting.getFunctionId().equals("unionLogin")){
//			httpSetting.setReleaseHttpsettings(this);
//			return false;
//		}
//		return false;
//	}
	
	
 	public static void clearHttpSettings(){
// 		httpSettings.clear();
  	}
	
}
