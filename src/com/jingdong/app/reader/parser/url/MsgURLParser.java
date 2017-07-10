package com.jingdong.app.reader.parser.url;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;

import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.WebRequest;

public class MsgURLParser implements URLParser {
	private String baseURL;
	public static final String BEFORE_ID = "before_id";

	@Override
	public void init(Bundle envParam) {
		baseURL = envParam.getString(URLParser.BASE_URL);
	}

	@Override
	public String getBaseUrl(Map<String, String> paraMap) {
		Map<String, String> urlMap = new HashMap<String, String>();
		urlMap.put(WebRequest.AUTH_TOKEN, paraMap.get(WebRequest.AUTH_TOKEN));
		return URLBuilder.addParameter(baseURL, urlMap);
	}

	@Override
	public String getPrevBaseUrl(Map<String, String> paraMap) {
		return "";
	}

	@Override
	public String getNextBaseUrl(Map<String, String> paraMap) {
		Map<String, String> urlMap = new HashMap<String, String>();
		urlMap.put(WebRequest.AUTH_TOKEN, paraMap.get(WebRequest.AUTH_TOKEN));
		urlMap.put(MsgURLParser.BEFORE_ID, paraMap.get(ID));
		return URLBuilder.addParameter(baseURL, urlMap);
	}

}
