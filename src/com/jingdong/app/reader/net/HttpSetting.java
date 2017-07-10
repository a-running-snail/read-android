package com.jingdong.app.reader.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.client.RequestEntry;
import com.jingdong.app.reader.config.Configuration;
import com.jingdong.app.reader.entity.BodyEncodeEntity;
import com.jingdong.app.reader.util.HttpGroup.OnCommonListener;
import com.jingdong.app.reader.util.Md5Encrypt;

public class HttpSetting {
	
	public static final int TYPE_FILE = RequestEntry.TYPE_FILE;
	public static final int TYPE_JSON = RequestEntry.TYPE_JOSN;
	public static final int TYPE_IMAGE = RequestEntry.TYPE_IMAGE;
	public static final int TYPE_XML = RequestEntry.TYPE_XML;
	public static final int TYPE_STREAM = RequestEntry.TYPE_STREAM;
	
	public static final int EFFECT_NO = 0;// 不要效果
	public static final int EFFECT_DEFAULT = 1;// 默认效果

	public static final int EFFECT_STATE_NO = 0;
	public static final int EFFECT_STATE_YES = 1;

	public static final int CACHE_MODE_AUTO = 0;
	public static final int CACHE_MODE_ONLY_CACHE = 1;
	public static final int CACHE_MODE_ONLY_NET = 2;
	private boolean isShowToast = true;
	private boolean isUserTempCookies = false;
	private int id;
	private String host;
	private String functionId;
	private String url; // firstHandler()会进行url的组装，组装完成后赋值给semiUrl，然后再beforeConnection()函数里把semi设置成finalurl
	private String semiUrl;
	private String finalUrl; // 加密Url和普通Url都在最后拼接成finalUrl
	private JSONObject jsonParams;
	private Map<String, String> mapParams;
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
	private boolean isIntercepted = false;

	public boolean isIntercepted() {
		return isIntercepted;
	}

	public void setIntercepted(boolean isIntercepted) {
		this.isIntercepted = isIntercepted;
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

	public void putMapParams(String key, String value) 
    {
		if (null == this.mapParams) 
        {
			this.mapParams = new HashMap<String, String>();
		}
		if (isURLEncoder) {
			try {
				value = URLEncoder.encode(value, "utf-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		this.mapParams.put(key, value);
	}

	public String creatFinalUrl() {
		if (getHost() == null) {
			this.setHost(Configuration.getProperty(Configuration.HOST));
		}
		if (null != getFunctionId()) {
			putMapParams("functionId", getFunctionId());
		}
		if (getUrl() == null) {
			setUrl("http://" + getHost() + "/client.action");
		}
		if (isPost()) {
			// POST
			if (null != this.getMapParams()) {
				this.setSemiUrl(this.getUrl() + "?" + "functionId="
						+ this.getMapParams().get("functionId"));
			}
		} else {
			// GET
			if (null != this.getMapParams()) {

				StringBuilder url = new StringBuilder(this.getUrl());
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

		if (this.getSemiUrl() == null) {
			setSemiUrl(getUrl());
		}
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

	public void setSemiUrl(String semiUrl) {
		this.semiUrl = semiUrl;
	}

	public String getFinalUrl() {
		return finalUrl;
	}

	public void setFinalUrl(String finalUrl) {
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

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
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
