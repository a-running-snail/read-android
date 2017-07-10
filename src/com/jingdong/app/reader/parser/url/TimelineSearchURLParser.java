package com.jingdong.app.reader.parser.url;

import java.util.Map;

import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.url.PagedBasedUrlGetter;

import android.os.Bundle;

public class TimelineSearchURLParser implements URLParser {
	public final static String BASE_URL=URLText.searchEntityUrl;
	private int queryPage = PagedBasedUrlGetter.FIRST_PAGE;
	@Override
	public void init(Bundle envParam) {
		
	}

	@Override
	public String getBaseUrl(Map<String, String> paraMap) {
		return PagedBasedUrlGetter.getQueryUrl(BASE_URL,
				paraMap.get(QUERY), PagedBasedUrlGetter.FIRST_PAGE, true);
	}

	@Override
	public String getPrevBaseUrl(Map<String, String> paraMap) {
		return "";
	}

	@Override
	public String getNextBaseUrl(Map<String, String> paraMap) {
		queryPage++;
		return PagedBasedUrlGetter.getQueryUrl(BASE_URL,
				paraMap.get(QUERY), queryPage, true);
	}

}
