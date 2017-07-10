package com.jingdong.app.reader.parser.url;

import java.util.Map;

import android.os.Bundle;

/**
 * URLParer负责实现实现一个URL地址获取器。所有实现这个接口的类，必须包含一个空的构造函数，
 * 且默认的ParserCreator只会调用这个空的构造函数。
 * 
 * @author Alexander
 * 
 */
public interface URLParser {
	/**
	 * 如果请求下一页列表的时候，需要使用id作为索引，则需要以String的形式把这个参数传入Map中。
	 */
	public final static String ID = "id";
	/**
	 * 如果请求下一页列表的时候，需要使用guid作为索引，则需要以String的形式把这个参数传入Map中。
	 */
	public final static String GUID = "guid";
	/**
	 * 如果请求下以讹列表的时候，需要使用页码作为索引，则需要以String的形式把这个参数传入Map中。
	 */
	public final static String PAGE = "page";
	/**
	 * 如果开启了搜索功能，需要传入这个参数，这个参数表示搜索关键词。
	 */
	public final static String QUERY = "q";
	/**
	 * 如果需要在调用init方法的时候，动态设置BASE_URL，则需要把这个参数以String的形式传入到Bundle中。
	 */
	public final static String BASE_URL = "baseURL";

	/**
	 * 初始化环境变量，这个方法，仅会要在ParserCreator中被调用一次。如不需要实现，可以直接返回。
	 * 
	 * @param envParam
	 *            环境变量。
	 */
	public void init(Bundle envParam);

	/**
	 * 根据参数列表，得到初始URL，这个URL是Timeline的第一页（包括搜索结果的第一页）。
	 * 
	 * @param paraMap
	 *            参数列表，如果是搜索界面，则包括QUERY字段，否则为一个空Map
	 * @return 第一页的URL
	 */
	public String getBaseUrl(Map<String, String> paraMap);

	/**
	 * 根据参数列表，得到前一页的URL，目前只有社区动态支持这个功能。
	 * 
	 * @param paraMap
	 *            参数列表，如果是社区动态，则包括GUID字段，走则为空
	 * @return 前一页的URL
	 */
	public String getPrevBaseUrl(Map<String, String> paraMap);

	/**
	 * 根据参数列表，得到后一页的URL。
	 * 
	 * @param paraMap
	 *            参数列表，可能包含id，guid，page中的一项，如果是搜索界面，还包含QUERY字段。
	 * @return 后一页的URL
	 */
	public String getNextBaseUrl(Map<String, String> paraMap);
}
