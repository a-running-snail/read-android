package com.jingdong.app.reader.parser.url;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;

import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.timeline.actiivity.TimelineActivity;

public class TweetURLParser implements URLParser {

	private String baseURL;

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
		urlMap.put(TimelineActivity.BEFORE_GUID, paraMap.get(GUID));
		return URLBuilder.addParameter(baseURL, urlMap);
	}

}
