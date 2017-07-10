package com.jingdong.app.reader.net.url;

public interface UrlGetter {

	public final static String QUERY = "q";

	/**
	 * 取得待加载资源第一页的URL
	 * @param baseUrl 这个String即为Fragment.setArgument()传入的String。如果没有传入，则为空。
	 * @return 待加载资源所在URL，包含参数与参数值
	 */
	public String getInitPageUrl(String baseUrl);

	/**
	 * 取得待加载资源下一页的URL
	 * @param baseUrl 这个String即为Fragment.setArgument()传入的String。如果没有传入，则为空。
	 * @param id 当前链表中最后一项的id，可能为guid，userid等不同形式的id
	 * @return 待加载资源所在URL，包含参数与参数值
	 */
	public String getNextPageUrl(String baseUrl, String id);
}
