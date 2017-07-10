package com.jingdong.app.reader.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class URLBuilder {

	public static String buildKeyValueText(Map<String, String> paramMap) {
		StringBuilder sb = new StringBuilder();
		getStringFromMap(paramMap, sb);
		return sb.toString();
	}

	public static String addParameter(String baseURL, Map<String, String> paramMap) {
		if (paramMap == null || paramMap.size() == 0) {
			return baseURL;
		}

		StringBuilder sb = new StringBuilder(baseURL);
		sb.append('?');
		getStringFromMap(paramMap, sb);
		return sb.toString();
	}

	public static void putPostParam(String key, String value, Map<String, String> paramMap)
			throws UnsupportedEncodingException {
		paramMap.put(URLEncoder.encode(key, WebRequestHelper.CHAR_SET), URLEncoder.encode(value, WebRequestHelper.CHAR_SET));
	}

	public static String getPostTextFromMap(Map<String, String> paraMap) {
		StringBuilder builder = new StringBuilder();
		if (paraMap != null) {
			getStringFromMap(paraMap, builder);
		}
		return builder.toString();
	}

	/**
	 * @param paramMap
	 * @param sb
	 */
	private static void getStringFromMap(Map<String, String> paramMap, StringBuilder sb) {
		int index = 0;
		for (Map.Entry<String, String> entry : paramMap.entrySet()) {
			sb.append(entry.getKey());
			sb.append('=');
			sb.append(entry.getValue());
			if (index != paramMap.size() - 1) {
				sb.append('&');
			}
			++index;
		}
	}
}
