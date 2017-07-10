package com.jingdong.app.reader.parser.url;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;

import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.timeline.actiivity.TimelineActivity;

public class TimelineURLParser implements URLParser {
	private final static String BASE_URL = URLText.timelineUrl;

	@Override
	public void init(Bundle envParam) {

	}

	@Override
	public String getBaseUrl(Map<String, String> paraMap) {
		Map<String, String> urlParamMap = new HashMap<String, String>();
		urlParamMap.put(WebRequest.AUTH_TOKEN, paraMap.get(WebRequest.AUTH_TOKEN));
		return URLBuilder.addParameter(BASE_URL, urlParamMap);
	}

	@Override
	public String getPrevBaseUrl(Map<String, String> paraMap) {
		Map<String, String> urlParamMap = new HashMap<String, String>();
		urlParamMap.put(WebRequest.AUTH_TOKEN, paraMap.get(WebRequest.AUTH_TOKEN));
		urlParamMap.put(TimelineActivity.SINCE, paraMap.get(GUID));
		return URLBuilder.addParameter(BASE_URL, urlParamMap);//return "";
	}

	@Override
	public String getNextBaseUrl(Map<String, String> paraMap) {
		Map<String, String> urlParamMap = new HashMap<String, String>();
		urlParamMap.put(WebRequest.AUTH_TOKEN, paraMap.get(WebRequest.AUTH_TOKEN));
		urlParamMap.put(TimelineActivity.BEFORE_GUID, paraMap.get(GUID));
		return URLBuilder.addParameter(BASE_URL, urlParamMap);//return "";
	}

}
