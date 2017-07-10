package com.jingdong.app.reader.net.url;

public interface QueryUrlGetter extends UrlGetter{
	/**
	 * 根据指定关键词，取得待搜索资源第一页的URL
	 * @param query 待搜索的关键词
	 * @return 待加载资源所在URL，包含参数与参数值
	 */
	public String getQueryInitPageUrl(String query);
	/**
	 * 根据指定关键词，取得待搜索资源下一页的URL
	 * @param query 待搜索的关键词
	 * @return 待加载资源所在URL，包含参数与参数值
	 */
	public String getQueryNextPageUrl(String query);

}
