package com.jingdong.app.reader.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import com.jingdong.app.reader.util.FileGuider;

import android.content.Context;


public class RequestEntry implements Comparator<RequestEntry> {
	public final static int REQUEST_TYPE_REGISTER = 0;
	public final static int REQUEST_TYPE_GETLASTGEOGRAPHY = 1;
	public final static int REQUEST_TYPE_GETLASTCATEGORIES = 2;
	public final static int REQUEST_TYPE_SEARCH_POST_BY_JSON = 3;
	public final static int REQUEST_TYPE_MAJOR_CATEGORY_FITERS = 4;
	public final static int REQUEST_TYPE_LOAD_IMAGE = 5;
	public final static int REQUEST_TYPE_USER_REGIST = 6;

	// public String postdata ;
	public long _priority = 0;
	public long _size = 0;
	public int _type = 0;
	public boolean isImage = false;
	public boolean isParser = true;
	public String _id = null;
	public String _mark = null;
	/**
	 * 下载请求
	 */
	public HttpUriRequest _request = null;
	/**
	 * 下载响应对象
	 */
	public HttpResponse _response = null;
	public InputStream _inputStream = null;
	public DefaultHttpClient _client = null;
	public Object _userData = null;
	public Object _tag = null;
	public Context _context;
	public OnDownloadListener _downloadListener;
	public int _statusCode = 0;// 0正常，1网络连接错误，2服务器错误,3连接超时,5不需要更新
	public String _stateNotice = "";
	private int _requestCode = 0;
	private RequestManager manager;
	public long start; // 下载起点
	public long end; // 下载起点
	private boolean isStop = false;
	public HttpClient httpClient;
	public final static int TYPE_FILE = 0;
	public final static int TYPE_STRING = 1;
	public final static int TYPE_IMAGE = 2;
	public final static int TYPE_XML = 3;
	public final static int TYPE_JOSN = 4;
	public final static int TYPE_STREAM = 5;
	public String path;
	public FileGuider fileGuider;
	public boolean isSuccess;
	public boolean isDestroy;
	public int _logId = 0;
	private static int sLogId = 0;
	
	public RequestEntry(int type, HttpUriRequest request) {
		this(type, null, request, 100, null);
	}

	public RequestEntry(int type, String id, HttpUriRequest request) {
		this(type, id, request, 100, null);
	}
	
	public RequestEntry(int type, String id, HttpUriRequest request,int priority, Object userData) {
		_type = type;
		_id = id;
		_request = request;
		_priority = priority;
		_userData = userData;
	}
	
	public static int creatId() {
		return sLogId++;
	}

	public void close() {
		if (_response != null) {
			try {
				_response.getEntity().consumeContent();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public int compare(RequestEntry lhs, RequestEntry rhs) {
		if (rhs._priority > lhs._priority) {
			return 1;
		} else if (rhs._priority < lhs._priority) {
			return -1;
		}
		return 0;
	}
	
	/**
	 * 下载过程监听器，即进度回调
	 * @param downloadListener
	 */
	public void setDownloadListener(OnDownloadListener downloadListener) {
		_downloadListener = downloadListener;
	}

	public int getRequestCode() {
		return _requestCode;
	}

	public void setRequestCode(int _requestCode) {
		this._requestCode = _requestCode;
	}
	
	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}

	public RequestManager getManager() {
		return manager;
	}

	public void setManager(RequestManager manager) {
		this.manager = manager;
	}
	
	public boolean isStop() {
		if (manager != null && manager.isStop()) {
			return true;
		}
		return isStop;
	}
}