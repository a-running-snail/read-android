package com.jingdong.app.reader.timeline.selected.parser;

import java.util.Map;

import android.os.Bundle;

import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.parser.url.URLParser;

public class BooksTopBannerUrlParser implements URLParser{

	private int top_banner_id;
	@Override
	public void init(Bundle envParam) {
		this.top_banner_id=envParam.getInt("top_banner_id");
	}

	@Override
	public String getBaseUrl(Map<String, String> paraMap) {
		return URLText.getBooksSelectedTopTimeline+top_banner_id+".json";
	}

	@Override
	public String getPrevBaseUrl(Map<String, String> paraMap) {
		return "";
	}

	@Override
	public String getNextBaseUrl(Map<String, String> paraMap) {
		return "";
	}

}
