package com.jingdong.app.reader.parser.json;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * URLParer负责实现实现一个JSON解析器，将不同形式的JSON数据，变成同一种形式的JSON数据。所有实现这个接口的类，必须包含一个空的构造函数，
 * 且默认的ParserCreator只会调用这个空的构造函数。
 * 
 * @author Alexander
 * 
 */
public interface JSONParser {
	/**
	 * 根据JSON字符串，生成一个JSON数组。
	 * 
	 * @param jsonString
	 *            待处理的JSON字符串。
	 * @return 从JSON字符串中获得的数组。
	 */
	public JSONArray getJsonArrayFromString(String jsonString);

	/**
	 * 根据JSON数组和索引，获得JSON数组索引所指定位置的JSONObject
	 * 
	 * @param jsonArray
	 *            待处理的JSON数组
	 * @param index
	 *            待获取的JSONObject在JSON数组中的索引
	 * @return JSON数组中指定位置的JSON对象。
	 */
	public JSONObject getJsonObjectFromArray(JSONArray jsonArray, int index);
}
