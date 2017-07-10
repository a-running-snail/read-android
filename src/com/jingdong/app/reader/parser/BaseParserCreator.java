package com.jingdong.app.reader.parser;

import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;

import com.jingdong.app.reader.parser.json.JSONParser;
import com.jingdong.app.reader.parser.url.URLParser;
import com.jingdong.app.reader.util.MZLog;

public class BaseParserCreator implements ParserCreator {
	public final static Creator<BaseParserCreator> CREATOR = new Creator<BaseParserCreator>() {

		@Override
		public BaseParserCreator createFromParcel(Parcel source) {
			return new BaseParserCreator(source);
		}

		@Override
		public BaseParserCreator[] newArray(int size) {
			return new BaseParserCreator[size];
		}
	};
	public final static String ENV_MAP = "environmentMap";
	private final String urlParserFullName;
	private final String jsonParserFullName;
	private final Bundle envBundle;

	/**
	 * 这个构造函数仅在序列化的时候被调用。
	 * 
	 * @param parcel
	 *            数据源
	 */
	protected BaseParserCreator(Parcel parcel) {
		urlParserFullName = parcel.readString();
		jsonParserFullName = parcel.readString();
		envBundle = parcel.readBundle();
	}

	/**
	 * Parser的一个构造函数，调用者需要提供URLParser.class和JSONParser.class提供给这个构造函数，
	 * 使得构造函数可以使用反射方法创建URLParser和JSONParser的对象
	 * 。URLParser和JSONParser必须提供一个空的构造函数，否则无法使用反射成功创建对象。
	 * 
	 * @param urlParserClass
	 *            一个URLParser的子类，这个类必须含有一个空的构造函数。
	 *            BaseParserCreator只使用这个空的构造函数创建一个新的URLParser对象
	 *            ，URLParser的其他的构造将被忽略。
	 * @param jsonParserClass
	 *            一个JSONParser的子类，这个类必须含有一个空的构造函数。
	 *            BaseParserCreator只使用这个空的构造函数创建一个新的JSONParser对象
	 *            ，JSONParser的其他的构造将被忽略。
	 */
	public BaseParserCreator(Class<? extends URLParser> urlParserClass, Class<? extends JSONParser> jsonParserClass) {
		this(urlParserClass, jsonParserClass, new Bundle());
	}

	/**
	 * Parser的一个构造函数，调用者需要提供URLParser.class和JSONParser.class提供给这个构造函数，
	 * 使得构造函数可以使用反射方法创建URLParser和JSONParser的对象
	 * 。URLParser和JSONParser必须提供一个空的构造函数，否则无法使用反射成功创建对象。
	 * 
	 * @param urlParserClass
	 *            一个URLParser的子类，这个类必须含有一个空的构造函数。
	 *            BaseParserCreator只使用这个空的构造函数创建一个新的URLParser对象
	 *            ，URLParser的其他的构造将被忽略。
	 * @param jsonParserClass
	 *            一个JSONParser的子类，这个类必须含有一个空的构造函数。
	 *            BaseParserCreator只使用这个空的构造函数创建一个新的JSONParser对象
	 *            ，JSONParser的其他的构造将被忽略。
	 * @param envBundle 环境变量，这个环境变量将提供给urlParser的init方法，以便urlParser成功初始化。
	 */
	public BaseParserCreator(Class<? extends URLParser> urlParserClass, Class<? extends JSONParser> jsonParserClass,
			Bundle envBundle) {
		this.urlParserFullName = urlParserClass.getName();
		this.jsonParserFullName = jsonParserClass.getName();
		this.envBundle = envBundle;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(urlParserFullName);
		dest.writeString(jsonParserFullName);
		dest.writeBundle(envBundle);
	}

	@Override
	public URLParser createURLParser() {
		URLParser urlParser = createInstance(urlParserFullName, URLParser.class);
		urlParser.init(envBundle);
		return urlParser;
	}

	@Override
	public JSONParser createJsonParser() {
		return createInstance(jsonParserFullName, JSONParser.class);
	}

	private <T> T createInstance(String className, Class<T> targetClass) {
		T t = null;
		if (className != null) {
			try {
				Class<?> soureClass = Class.forName(className);
				t = soureClass.asSubclass(targetClass).newInstance();
			} catch (ClassCastException e) {
				MZLog.e("ParserCreator", Log.getStackTraceString(e));
			} catch (ClassNotFoundException e) {
				MZLog.e("ParserCreator", Log.getStackTraceString(e));
			} catch (InstantiationException e) {
				MZLog.e("ParserCreator", Log.getStackTraceString(e));
			} catch (IllegalAccessException e) {
				MZLog.e("ParserCreator", Log.getStackTraceString(e));
			}
		}
		return t;
	}

}
