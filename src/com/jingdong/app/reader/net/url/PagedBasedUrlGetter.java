package com.jingdong.app.reader.net.url;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.TimelineSearchPeopleActivity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.MZLog;

public class PagedBasedUrlGetter implements UrlGetter{
	private int followingPage=PagedBasedUrlGetter.FIRST_PAGE;
	private String userId;
	private final Context context;
	private final String baseUrl;
	public final static int FIRST_PAGE = 1;

	public PagedBasedUrlGetter(String id,String baseUrl) {
		this(id, baseUrl, null);
	}

	public PagedBasedUrlGetter(String id,String baseUrl,Context context) {
		this.userId=id;
		this.baseUrl=baseUrl;
		this.context=context;
	}

	@Override
	public String getInitPageUrl(String baseUrl) {
		return getFollowingPageUrl(followingPage);
	}

	@Override
	public String getNextPageUrl(String baseUrl, String id) {
		followingPage++;
		return getFollowingPageUrl(followingPage);
	}

	private String getFollowingPageUrl(int page){
		Map<String, String> map=new HashMap<String, String>();
		map.put(TimelineSearchPeopleActivity.PAGE, Integer.toString(page));
		if(context!=null)
			map.put("auth_token", LocalUserSetting.getToken(context));
		String url=URLBuilder.addParameter(baseUrl+userId+".json", map);
		return url;
	}

	public static String getQueryUrl(String baseUrl, String query, int page, boolean inParam) {
		String text = utfEncode(query);
		Map<String, String> map = new HashMap<String, String>();
		if(inParam)
			map.put(UrlGetter.QUERY, text);
		else
			baseUrl=baseUrl+text+".json";
		if (page > PagedBasedUrlGetter.FIRST_PAGE)
			map.put(TimelineSearchPeopleActivity.PAGE, Integer.toString(page));
		String url = URLBuilder.addParameter(baseUrl, map);
		return url;
	}

	/**
	 * @param query
	 * @return
	 */
	private static String utfEncode(String query) {
		String text = query;
		try {
			text = URLEncoder.encode(text, WebRequestHelper.CHAR_SET);
		} catch (UnsupportedEncodingException e) {
			MZLog.e("searchUsers", Log.getStackTraceString(e));
		}
		return text;
	}
}
