package com.jingdong.app.reader.client;


import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.content.Context;
import android.net.Proxy;
import android.os.Environment;
import android.text.TextUtils;

import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.net.HttpSetting;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.NetWorkUtils;
import com.jingdong.app.reader.util.RsaEncoder;
import com.jingdong.app.reader.util.StatisticsReportUtil;
import com.jingdong.app.reader.util.StreamToolBox;
import com.jingdong.app.reader.util.TimeFormat;


public class ServiceClient {
    public static boolean mustDirectConnect = false; //是否直连,如果wap网络连接不通，测会改为直连
	public final static boolean isTestNetwork = false;
	private static boolean isNeedInitAgent = true;

	public static boolean isNeedInitAgent() {
		return isNeedInitAgent;
	}

	
	public static void setNeedinitAgent(boolean needInit) {
		isNeedInitAgent = needInit;
	}
	/**
	 * 测试方法，可删除
	 */
	public static void writeNetworkStat(String str_log) {
		if (!isTestNetwork) {
			return;
		}
		FileWriter fw = null;
		try {
			fw = new FileWriter(Environment.getExternalStorageDirectory() + "/"
					+ "ganjilog_network", true);
			String log = TimeFormat.getTimeStamp() + " : ," + str_log + "\r\n";
			fw.write(log);
			MZLog.d("network", log);
		} catch (Exception e) {
			MZLog.d("", "书写日志发生错误：" + e.toString());
		} finally {
			try {
				if (fw != null) {
					fw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	
	public void isRequestHttpSetting(HttpSetting httpSetting) {
		RequestEntry entry = HttpSettingToRequestEntry(httpSetting);
		issueRequestEntry(entry);
	}
	
	public static RequestEntry execute(HttpSetting httpSetting) {
		RequestEntry entry = HttpSettingToRequestEntry(httpSetting);
		execute(entry);
		return entry;
	}
	
	public static RequestEntry HttpSettingToRequestEntry(HttpSetting httpSetting){
		JSONObject object = httpSetting.getJsonParams();// .toString();
		String body = null;
		if (object != null) {
			body = object.toString();
		}
		if (httpSetting.getIsEncoder() && !TextUtils.isEmpty(body)) {
			MZLog.d("isRequestHttpSetting", "body = " + body);
			String bodyEncoder = RsaEncoder.stringBodyEncoder(body,
					httpSetting.getEncodeEntity());
			httpSetting.putMapParams("body", bodyEncoder);
			MZLog.d("isRequestHttpSetting", "bodyEncoder = " + bodyEncoder);
		} else if (!TextUtils.isEmpty(body)) {
			httpSetting.putMapParams("body", body);
			MZLog.d("isRequestHttpSetting", "body = " + body);
		}
		if (httpSetting.getFinalUrl() == null) {
			httpSetting.setFinalUrl(httpSetting.creatFinalUrl());
		}
		HttpUriRequest httpUriRequest = null;
		if (httpSetting.isPost()) {
			httpUriRequest = new HttpPost(httpSetting.getFinalUrl());

			if (null != httpSetting.getMapParams()) {
				StringBuilder postData = new StringBuilder();
				Map<String, String> mapParams = httpSetting.getMapParams();
				Set<String> keySet = mapParams.keySet();
				for (Iterator<String> iterator = keySet.iterator(); iterator
						.hasNext();) {
					String key = iterator.next();
					String value = mapParams.get(key);
					postData.append(key).append("=").append(value);
					if (iterator.hasNext()) {
						postData.append("&");
					}
				}

				postData.append(StatisticsReportUtil
						.getReportString(true, true));
				StringEntity reqEntity = null;
				try {
					reqEntity = new StringEntity(postData.toString(), "utf-8");
					reqEntity
							.setContentType("application/x-www-form-urlencoded");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				((HttpPost) httpUriRequest).setEntity(reqEntity);
			}
		} else {
			httpUriRequest = new HttpGet(httpSetting.getFinalUrl());
		}
		RequestEntry entry = new RequestEntry(httpSetting.getType(),
				httpUriRequest);
		return entry;
	}
	
	
	
	public static boolean hanldAgent(DefaultHttpClient httpClient){
		boolean isAgent = false;
		if(!mustDirectConnect){
			 final String host = Proxy.getHost(MZBookApplication.getInstance());
	            final int port = Proxy.getPort(MZBookApplication.getInstance());
	            MZLog.d("ServiceClient", "ServiceClient _____host==="+host);
	            MZLog.d("ServiceClient", "ServiceClient _____port==="+port);
	            if (!TextUtils.isEmpty(host) && port != -1) {
	                final HttpHost proxy = new HttpHost(host,port);
	                httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
	                isAgent = true;
	            }
		}
		if(!isAgent){
		     httpClient.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
		     isAgent = false;
		}
		return isAgent;
	}

	
	class ThreadSend extends Thread {
		int  agentState = -1;// 0未代理,1代理
		
		private DefaultHttpClient httpClient;
		@Override
		public void run() {
			while (!isStop) {

				RequestEntry requestEntry = null;
				try {
					requestEntry = poll(100, TimeUnit.MILLISECONDS);

				} catch (InterruptedException e1) {
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (requestEntry == null||requestEntry.isStop()) {
					synchronized (this) {
						try {
							this.wait(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					continue;
				}

				if (httpClient == null) {
					httpClient = createHttpClient();
				}

				requestEntry._logId = RequestEntry.creatId();
				// 重试次数
				int retryIndex = 0;
				boolean retrying = true;
				while (retrying) {
					
					if(retryIndex>=1&&ServiceClient.isNeedInitAgent){
						if(agentState==1){
							mustDirectConnect = true;
						}else if(agentState==0){
							mustDirectConnect = false;
						}
					}
					boolean isAgent =	hanldAgent(httpClient);
					if(!isAgent){
						agentState = 0;
						MZLog.d("ServiceClient", "final"+"id:"+requestEntry._logId+ "url=="+requestEntry._request.getURI());
					}else{
						agentState = 1;
					}
					MZLog.d("ServiceClient", "final"+"id:"+requestEntry._logId+"isAgent==="+isAgent);
					
					boolean isSucceed = false;
					try {
//						writeNetworkStat("1. 发起连接," + requestEntry + ","
//								+ System.currentTimeMillis() + ","
//								+ requestEntry._type);
						MZLog.d("ServiceClient", "final"+"id:"+requestEntry._logId+ "url=="+requestEntry._request.getURI());
						// requestEntry._request.
						requestEntry._response = httpClient
								.execute(requestEntry._request);
						MZLog.d("ServiceClient", "start!");
						int code = requestEntry._response.getStatusLine()
						.getStatusCode();
						MZLog.d("ServiceClient", "final"+"id:"+requestEntry._logId+ "code=="+ code);
						if (code != 200) {
							if (code == 304) {
								requestEntry._statusCode = 5;
								// 304情况失败后，需要关闭才能再次创建连接请求
								if (mHttpClient != null) {
									final DefaultHttpClient tempHttpClient = mHttpClient;
									mHttpClient = createHttpClient();
									tempHttpClient.getConnectionManager()
											.shutdown();
								}
							} else {
								requestEntry._statusCode = 2;
							}
							isSucceed = false;
							if (requestEntry._type == RequestEntry.REQUEST_TYPE_USER_REGIST) {
								retrying = false;
							} else {
								retrying = true;
							}
						} else {
							if(agentState==1){
								mustDirectConnect = false;
							}else if(agentState==0){
								mustDirectConnect = true;
							}
							ServiceClient.setNeedinitAgent(false);
							
							retrying = false;
							isSucceed = true;
						}
					} catch (NullPointerException e) {
						e.printStackTrace();
						requestEntry._statusCode = 1;
						// apache bug抛出空指针 ，将重试
						retrying = true;
					} catch (SocketException e) {
						e.printStackTrace();
						requestEntry._statusCode = 1;
						// 由于部分手机出现移动数据网络中断问题，出现该异常时，将重试
						retrying = true;
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
						requestEntry._statusCode = 3;
						retrying = true;
					} catch (ClientProtocolException e) {
						e.printStackTrace();
						requestEntry._statusCode = 1;
						retrying = true;
					} catch (IOException e) {
						e.printStackTrace();
						requestEntry._statusCode = 1;
						retrying = true;
					} catch (Exception e) {
						e.printStackTrace();
						requestEntry._statusCode = 1;
						retrying = true;
					}
					MZLog.d("ServiceClient", "end!");
					if (isSucceed == false) {// 失败
						if (retrying == true) {
							if (retryIndex >= 2) {
								retrying = false;
								onRequestFailed(requestEntry);
							} else {
								retryIndex++;
								try {
									// 休息半秒后再重试
									Thread.sleep(500);
								} catch (InterruptedException e2) {
									e2.printStackTrace();
								}
							}
						} else {
							onRequestFailed(requestEntry);
						}
					} else {// 成功
						onRequestFinished(requestEntry);
					}
				}
			}
			// 线程关闭，关闭连接池
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
				httpClient = null;
//				httpClient.getConnectionManager().closeExpiredConnections()
			}
		}

		public void close( HttpClient httpClient){
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
				httpClient = null;
			}
		}
		void startrequest() {
			synchronized (this) {
				this.notify();
			}
		}
	}

	//private static ServiceClient instance;

	private static ServiceClient imageInstance;

	private final static int WORKER_THREAD_NUM = 1;

//	public static synchronized ServiceClient getInstance() {
//		if (instance == null || instance.isStop) {
//			instance = new ServiceClient();
//		}
//		return instance;
//	}

	public static synchronized ServiceClient getImageInstance() {
		if (imageInstance == null || imageInstance.isStop) {
			imageInstance = new ServiceClient();
		}
		return imageInstance;
	}

	public static synchronized void setInstanceNull() {
		//instance = null;
		if (mHttpClient != null) {
			mHttpClient.getConnectionManager().shutdown();
			mHttpClient = null;
		}
	}




	private PriorityBlockingQueue<RequestEntry> _pendingRequests = new PriorityBlockingQueue<RequestEntry>(
			20, new RequestEntry(0, null));

	private ThreadSend[] _workerThreads = new ThreadSend[WORKER_THREAD_NUM];

	private boolean isStop = false;

	public ServiceClient() {
		initWorkerThreads();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized void cleanRequest(int RequestType) {
		Object[] requestEntryList = _pendingRequests.toArray();
		_pendingRequests.clear();
		Vector List = new Vector();
		for (Object obj : requestEntryList) {
			RequestEntry requestEntry = (RequestEntry) obj;
			if (requestEntry._type == RequestType) {

			} else {
				List.add(requestEntry);
			}
		}
		for (Object obj : List) {
			RequestEntry requestEntry = (RequestEntry) obj;
			_pendingRequests.offer(requestEntry);
		}
		for (int i = 0; i < _workerThreads.length; i++) {
			_workerThreads[i].startrequest();
		}
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	public synchronized void cleanRequest(String mark) {
		Object[] requestEntryList = _pendingRequests.toArray();
		_pendingRequests.clear();
		Vector List = new Vector();
		for (Object obj : requestEntryList) {
			RequestEntry requestEntry = (RequestEntry) obj;
			if (requestEntry._mark.equals(mark)) {

			} else {
				List.add(requestEntry);
			}
		}
		for (Object obj : List) {
			RequestEntry requestEntry = (RequestEntry) obj;
			_pendingRequests.offer(requestEntry);
		}
		for (int i = 0; i < _workerThreads.length; i++) {
			_workerThreads[i].startrequest();
		}
	}




	private void initWorkerThreads() {
		for (int i = 0; i < WORKER_THREAD_NUM; ++i) {
			_workerThreads[i] = new ThreadSend();
			_workerThreads[i].start();
		}
	}






	// ----- Methods for issuing requests
	public synchronized void issueRequestEntry(RequestEntry entry) {
		_pendingRequests.offer(entry);
		for (int i = 0; i < _workerThreads.length; i++) {
			synchronized (this) {
				_workerThreads[i].startrequest();
			}
		}
	}


	public void issueUserloadImageRequest(Context context,
			OnDownloadListener downloadListener, String url, Object obj,String path,String mark,RequestManager manager) {
		try {
			HttpUriRequest request = RequestParamsPool.getLoadImageRequest(context,
					url);
			RequestEntry entry = new RequestEntry(
					RequestEntry.REQUEST_TYPE_LOAD_IMAGE, request);
			entry.path = path;
			entry.isImage = true;
			entry._tag = obj;
			entry._context = context;
			entry.setManager(manager);
			entry._mark = mark;
			entry.setDownloadListener(downloadListener);
			issueRequestEntry(entry);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}




	public void onRequestFailed(final RequestEntry requestEntry) {
		// 新线程中下载数据、解析，节省主线程时间
		new Thread() {
			public void run() {
				MZLog.d("serviceClient", "requestEntry._statusCode"
						+ requestEntry._statusCode);
				if (requestEntry._statusCode == 1) {
					requestEntry._stateNotice = "网络错误";
				} else if (requestEntry._statusCode == 2) {
					requestEntry._stateNotice = "服务器错误";
				} else if (requestEntry._statusCode == 3) {
					requestEntry._stateNotice = "连接超时";
				} else if (requestEntry._statusCode == 5) {
					requestEntry._stateNotice = "没有数据";
				} else if (requestEntry._statusCode == 0) {
					requestEntry._stateNotice = "authfailed";
				}
				if (requestEntry._response != null
						&& requestEntry._response.getStatusLine() != null) {

				} else {

				}
				try {
					// 实体需要消耗完全为了重复keep-alive联系
					if (requestEntry != null && requestEntry._response != null
							&& requestEntry._response.getEntity() != null) {
						requestEntry._response.getEntity().consumeContent();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				// 解决空指针错误
				if (requestEntry._downloadListener != null) {
					if(!requestEntry.isStop()){
						requestEntry._downloadListener
						.onDownloadCompleted(requestEntry);
					}
				}
			}
		}.start();
	}

	public void onRequestFinished(final RequestEntry requestEntry) {
		// 新线程中下载数据、解析，节省主线程时间
		new Thread() {
			public void run() {
				if (requestEntry._response == null) {
					requestEntry._statusCode = 1;
					onRequestFailed(requestEntry);
					return;
				}
				MZLog.d("serviceClient", "StatusCode =="
						+ requestEntry._response.getStatusLine()
								.getStatusCode());
				if (requestEntry._response.getStatusLine().getStatusCode() != 200) {
					if (requestEntry._response.getStatusLine().getStatusCode() == 304) {
						requestEntry._statusCode = 5;
					} else {
						requestEntry._statusCode = 2;
					}
					onRequestFailed(requestEntry);
					return;
				} else {
					requestEntry._statusCode = 0;
//					writeNetworkStat("2. 收到服务器返回200," + requestEntry + ","
//							+ System.currentTimeMillis() + ","
//							+ requestEntry._type);
				}

				try {
					writeNetworkStat("3. 发起请求数据," + requestEntry + ","
							+ System.currentTimeMillis() + ","
							+ requestEntry._type);
					InputStream inputStream = requestEntry._response
							.getEntity().getContent();
					Header[] headers = requestEntry._response
							.getHeaders("Content-Encoding");
					if (headers != null && headers.length > 0) {
						if (requestEntry._response
								.getHeaders("Content-Encoding")[0].getValue()
								.equals("gzip")) {
							// 解压缩
							if (inputStream != null) {
								inputStream = new GZIPInputStream(inputStream);
							}
						}
					}
					inputStream = StreamToolBox.flushInputStream(inputStream);
					try {
						// 数据下载完后，实体需要消耗完全为了重复keep-alive联系
						if (requestEntry != null
								&& requestEntry._response != null) {
							requestEntry._response.getEntity().consumeContent();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (requestEntry.isImage) {
						inputStream.mark(0);
						String imagePath = "";
						imagePath = requestEntry.path;
						inputStream.reset();
						StreamToolBox.saveStreamToFile(inputStream, imagePath);
						requestEntry._userData = imagePath;
						requestEntry.isSuccess = true;
					} else if (requestEntry.isParser) {
						MZLog.d("serviceClient", "requestEntry.isParser");
						writeNetworkStat("5. 开始解析," + requestEntry + ","
								+ System.currentTimeMillis() + ","
								+ requestEntry._type);
						requestEntry._userData = GJParser.parse(inputStream);
						writeNetworkStat("2. 结束解析," + requestEntry + ","
								+ System.currentTimeMillis() + ","
								+ requestEntry._type);
					} else {
						requestEntry._userData = inputStream;
					}
				} catch (SocketTimeoutException e) {
					MZLog.d("ServiceClient", "Socket timeout");
					e.printStackTrace();
					requestEntry._statusCode = 3;
					onRequestFailed(requestEntry);
					return;
				} catch (IllegalStateException e) {
					e.printStackTrace();
					requestEntry._statusCode = 1;
					onRequestFailed(requestEntry);
					return;
				} catch (IOException e) {
					e.printStackTrace();
					requestEntry._statusCode = 1;
					onRequestFailed(requestEntry);
					return;
				} catch (Exception e) {
					e.printStackTrace();
					requestEntry._statusCode = 1;
					onRequestFailed(requestEntry);
					return;
				}
				MZLog.d("serviceClient", "requestEntry._userData == "
						+ requestEntry._userData);
				// 解决空指针错误
				if (requestEntry._downloadListener != null) {
					if(!requestEntry.isStop()){
						requestEntry._downloadListener
						.onDownloadCompleted(requestEntry);
					}
				}
			}
		}.start();
	}

	private synchronized RequestEntry poll(long timeout, TimeUnit unit)
			throws InterruptedException {

		return _pendingRequests.poll(100, TimeUnit.MILLISECONDS);

	}

	public void stopThread() {
		isStop = true;
	};
	
	// public void stop(){
		//
		// }
		/**
		 * 创建新的DefaultHttpClient，带线程池管理
		 * 
		 * @return
		 */
		private static DefaultHttpClient createHttpClient() {
			System.out.println("DDDDDDDD===ServiceClient=====createHttpClient=====111======");
			final DefaultHttpClient httpClient;
			final BasicHttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setStaleCheckingEnabled(httpParameters, false);
			HttpConnectionParams.setConnectionTimeout(httpParameters, 15000);
			HttpConnectionParams.setSoTimeout(httpParameters, 30000);
			HttpConnectionParams.setSocketBufferSize(httpParameters, 8192);
			/**
			 * 以下增加线程池管理
			 */
			final SchemeRegistry schemeRegistry = new SchemeRegistry();
			schemeRegistry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			schemeRegistry.register(new Scheme("https", SSLSocketFactory
					.getSocketFactory(), 443));
			final ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(
					httpParameters, schemeRegistry);
			// 线程池管理
			httpClient = new DefaultHttpClient(ccm, httpParameters);
			NetWorkUtils.setupNetwork(MZBookApplication.getInstance(), httpClient);
			return httpClient;
		}

		/**
		 * 专门针对性 execute(RequestEntry requestEntry)方法的连接
		 */
		private static DefaultHttpClient mHttpClient;

		public static RequestEntry execute(RequestEntry requestEntry) {
			System.out.println("DDDDDDDD===ServiceClient=====execute=====111======");
			int  agentState = -1;// 0未代理,1代理
			// 重试次数
			int retryCount = 0;
			int retryIndex = 0;
			requestEntry._logId = requestEntry.creatId();
			while (retryIndex<=retryCount) {
				boolean isSucceed = false;
				try {
					if (mHttpClient == null) {
						mHttpClient = createHttpClient();
					}
					MZLog.d("ServiceClient", "final"+"id:"+requestEntry._logId + "- finalUrl -->> " + requestEntry._request.getURI());
					requestEntry._client = mHttpClient;
					if(retryIndex>=1&&ServiceClient.isNeedInitAgent()){
						if(agentState==1){
							ServiceClient.mustDirectConnect = true;
						}else if(agentState==0){
							ServiceClient.mustDirectConnect = false;
						}
					}
					boolean isAgent = ServiceClient.hanldAgent(requestEntry._client);
					if(!isAgent){
						agentState = 0;
					}else{
						agentState = 1;
					}
					MZLog.d("ServiceClient", "final"+"id:"+requestEntry._logId+"isAgent==="+isAgent);
					try{
						retryIndex++;
						requestEntry._request.setHeader("Cookie", WebRequestHelper.getCookies());
						requestEntry._response = mHttpClient.execute(requestEntry._request);
					}catch (Exception e) {
						if(retryCount<1){
							retryCount++;	
							continue;
						}else{
							throw e;
						}
					}
					
					if (requestEntry.isStop()) {
						return requestEntry;
					}
					
					if (requestEntry._response.getStatusLine().getStatusCode() != 200 //非正常返回
							&& requestEntry._response.getStatusLine().getStatusCode() != 206/*非部分内容正常返回*/) {
						if (requestEntry._response.getStatusLine().getStatusCode() == 304) {
							requestEntry._statusCode = 5;
							// 304情况失败后，需要关闭才能再次创建连接请求
							if (mHttpClient != null) {
								final DefaultHttpClient tempHttpClient = mHttpClient;
								mHttpClient = createHttpClient();
								requestEntry._client = mHttpClient;
								tempHttpClient.getConnectionManager().shutdown();
							}
							//重试
							if(retryCount<1){
								retryCount++;	
								continue;
							}
						} else {
							requestEntry._statusCode = 2;
						}
						isSucceed = false;
					} else {//返回状态码正常
						if(agentState==1){
							ServiceClient.mustDirectConnect = false;
						}else if(agentState==0){
							ServiceClient.mustDirectConnect = true;
						}
						ServiceClient.setNeedinitAgent(false);
						
						// 先把数据包下载下来，再赋值
						InputStream inputStream = requestEntry._response.getEntity().getContent();
						long length = 0;
						try {
							length = requestEntry._response.getEntity().getContentLength();
						} catch (Exception e) {
							e.printStackTrace();
						}
						MZLog.d("DownloadThread", "DownloadThread.....length==" + length);
						requestEntry._inputStream = inputStream;
						//如果返回状态为200，但起始位置不为0，将其值为0.@zhangmurui 2013、4、24防止出现百分比超过100%。
						if(requestEntry._response.getStatusLine().getStatusCode() == 200){
							 if(requestEntry.start!=0){
								requestEntry.start=0;
							 }
						}
						
						//分块下载
						if(requestEntry._response.getStatusLine().getStatusCode() == 206){
							Header header= requestEntry._response.getFirstHeader("Content-Range");
							if(header!=null){
								String value= header.getValue();
								if(!TextUtils.isEmpty(value)&&value.length()>2){
									String[]  strs1= value.split(" ");
										MZLog.d("ServiceClient", "final"+"id:"+requestEntry._logId+"___"+"返回206之后 ，strs1[1]="+strs1[1]);
									String[]  strs2  = strs1[1].split("-");
										MZLog.d("ServiceClient", "final"+"id:"+requestEntry._logId+"___"+"返回206之后 ，strs2[0]="+strs2[0]);
									long start  =Long.valueOf(strs2[0]);
									requestEntry.start=start;
										MZLog.d("ServiceClient", "final"+"id:"+requestEntry._logId+"___"+"返回206之后 ，本地保存文件的起始点 start="+requestEntry.start);
								}
							}
						}
						
						//下载的是文件
						if (requestEntry._type == RequestEntry.TYPE_FILE) {
							Header header = requestEntry._response.getEntity().getContentType();
							String Value = null;
							if (header != null) {
								Value = header.getValue();
							}
							
							//此处会调用回调更新进度
							IOUtil.readAsFile(inputStream,requestEntry.fileGuider.getFile(),requestEntry._downloadListener, requestEntry,length);
							requestEntry._statusCode = 0;
							isSucceed = true;
						} else if (requestEntry._type == RequestEntry.TYPE_STRING) {
							String result = StreamToolBox.loadStringFromStream(inputStream);
							MZLog.d("HttpGroup", "id:" + requestEntry._id+ "- response string -->> " + result);
							requestEntry._userData = result;
							requestEntry._statusCode = 0;
							isSucceed = true;
						} else if (requestEntry._type == RequestEntry.TYPE_IMAGE) {
							isSucceed = true;
						}
						else if (requestEntry._type == RequestEntry.TYPE_XML) {
							InputStream tempIs = StreamToolBox.flushInputStream(inputStream);
							String string = StreamToolBox.loadStringFromStream(tempIs);
							requestEntry._tag = string;
							tempIs.reset();
							requestEntry._userData = isToXmlContent(tempIs);
							requestEntry._statusCode = 0;
							isSucceed = true;
						}
						if (!requestEntry.isStop()) {
							requestEntry.isSuccess = isSucceed;
						}
					}
				} catch (NullPointerException e) {
					e.printStackTrace();
					requestEntry._statusCode = 1;
				} catch (SocketException e) {
					e.printStackTrace();
					requestEntry._statusCode = 1;
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
					requestEntry._statusCode = 1;
				} catch (ClientProtocolException e) {
					e.printStackTrace();
					requestEntry._statusCode = 1;
				} catch (FileNotFoundException e) {
					e.printStackTrace(); 
					requestEntry._statusCode = 1;
				}catch (IOException e) {
					e.printStackTrace();
					requestEntry._statusCode = 1;
				} catch (Exception e) {
					e.printStackTrace();
					requestEntry._statusCode = 1;
				} finally {
					if (requestEntry._response != null) {
						HttpEntity httpEntity = requestEntry._response.getEntity();
						try {
							if (httpEntity != null) {
								httpEntity.consumeContent();
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
	
				}
			}
			return requestEntry;
		}
		
		
		/**
		 * XML内容处理
		 */
		private static Element isToXmlContent(InputStream inputStream) throws Exception {
			Element root = null;
			try{
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
					Document dom = builder.parse(inputStream,
							"UTF-8");
					root = dom.getDocumentElement();
			}catch(Exception e){
				
			}
             return root;
		}

}
