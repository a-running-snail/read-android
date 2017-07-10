package com.jingdong.app.reader.parser;

import android.os.Parcelable;

import com.jingdong.app.reader.parser.json.JSONParser;
import com.jingdong.app.reader.parser.url.URLParser;

/**
 * OtherActivity调用TimelineActivity时，需要提供一个实现此接口的对象，可以使用默认实现(BaserParserCreator)。
 * 若自行实现此接口，需要保证同时实现Parcelable接口，因为这个对象会在两个Activity中进行传递。
 * 
 * @author Alexander
 * 
 */
public interface ParserCreator extends Parcelable {

	/**
	 * 这个方法负责使用反射创建一个URLParser，URLParser是一个用来处理URL地址的对象。
	 * @return 一个刚创建的URLParser对象。
	 */
	public abstract URLParser createURLParser();

	/**
	 * 这个方法负责使用反射创建一个JSONParser。JSONParser用来把不同的JSON数据以同样的方式返回给Activity。
	 * @return 一个刚创建的JSONParser对象。
	 */
	public abstract JSONParser createJsonParser();

}