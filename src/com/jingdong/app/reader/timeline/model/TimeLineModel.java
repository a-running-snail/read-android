package com.jingdong.app.reader.timeline.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.jingdong.app.reader.message.model.Alert;
import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.parser.ParserCreator;
import com.jingdong.app.reader.parser.json.JSONParser;
import com.jingdong.app.reader.parser.url.URLParser;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.Log;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;

@SuppressWarnings("deprecation")
public class TimeLineModel extends ObservableModel {
	public final static int LOAD_INIT = 10;
	public final static int LOAD_NEW = 11;
	public final static int LOAD_MORE = 12;
	public final static int DELETE_ENTITY = 13;
	public final static int UPDATE_COMMENTS_NUMBER = 14;
	public final static int RELOAD = 15;
	public final static int LOAD_AS_INPUT = 16;
	public final static int EMPTY_INPUT = 17;
	public final static int RECOMMEND = 25;
	
	private List<Entity> entityList;
	private List<Entity> tempList;
	private boolean tail;
	private boolean timelineAdapter;
	private Context context;
	private URLParser urlParser;
	private JSONParser jsonParser;

	public TimeLineModel(boolean timelineAdapter, ParserCreator parserCreator) {
		entityList = Collections.synchronizedList(new LinkedList<Entity>());
		this.timelineAdapter = timelineAdapter;
		this.urlParser = parserCreator.createURLParser();
		this.jsonParser = parserCreator.createJsonParser();
	}

	/**
	 * 从服务器请求Json数据，进行解析，并通知观察者
	 * 
	 * @param context
	 *            数据上下文
	 * @param type
	 *            请求
	 * @param guid
	 *            指定的guid。当type为LOAD_NEW时，向服务器请求该guid发表后新发表的Entities；
	 *            当type为LOAD_MORE时，向服务器请求该guid发表前的Entities。
	 */
	String jsonString = null;

	public void loadEntities(final Context context, final int type,
			final int  currentpage, final int pagecount,final String before_guid,final String since_guid,final String recommend_guid,final int flag) {

		this.context = context;
		switch (type) {
		case RELOAD:
//			entityList.clear();
		case LOAD_INIT:
//			entityList.clear();
			((Activity) context).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WebRequestHelper.get(URLText.TimeLine_URL,
							RequestParamsPool
									.getTimelineParams(currentpage+"",pagecount+"",before_guid,since_guid,recommend_guid), true,
							new MyAsyncHttpResponseHandler(context) {

								@Override
								public void onFailure(int arg0, Header[] arg1,
										byte[] arg2, Throwable arg3) {
								}

								@Override
								public void onResponse(int statusCode,
										Header[] headers, byte[] responseBody) {
									jsonString = new String(responseBody);
									parse(type, jsonString,flag);
								}
							});
				}
			});
			break;
		case LOAD_NEW:
			((Activity) context).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WebRequestHelper.get(URLText.TimeLine_URL,
							RequestParamsPool
									.getTimelineParams(currentpage+"",pagecount+"","",since_guid,recommend_guid), true,
							new MyAsyncHttpResponseHandler(context) {

								@Override
								public void onFailure(int arg0, Header[] arg1,
										byte[] arg2, Throwable arg3) {
									Log.d("cj", "false=======>>");
								}

								@Override
								public void onResponse(int statusCode,
										Header[] headers, byte[] responseBody) {

									jsonString = new String(responseBody);
									Log.d("cj", "resultNew=======>>" + jsonString);
									parse(type, jsonString,flag);
								}
							});
				}
			});
//			jsonString = getNewJsonString(context,
//					urlParser.getPrevBaseUrl(getParamMap(guid, id)));
			break;
		case LOAD_MORE:
			((Activity) context).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WebRequestHelper.get(URLText.TimeLine_URL,
							RequestParamsPool
									.getTimelineParams(currentpage+"",pagecount+"",before_guid,"",recommend_guid), true,
							new MyAsyncHttpResponseHandler(context) {

								@Override
								public void onFailure(int arg0, Header[] arg1,
										byte[] arg2, Throwable arg3) {
									Log.d("cj", "false=======>>");
								}

								@Override
								public void onResponse(int statusCode,
										Header[] headers, byte[] responseBody) {

									jsonString = new String(responseBody);
									parse(type, jsonString,flag);
								}
							});
				}
			});
//			jsonString = WebRequest.getWebDataWithContext(context,
//					urlParser.getNextBaseUrl(getParamMap(guid, id)));
			break;
		default:
			throw new IllegalArgumentException("没有对应类型的操作");
		}
		// parse(type, jsonString);
	}
	
	public void loadAt_users(final Context context, final int type,
			final int  currentpage, final int pagecount,final String before_guid,final String url,final int flag) {

		this.context = context;
		switch (type) {
		case RELOAD:
			entityList.clear();
		case LOAD_INIT:

			((Activity) context).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WebRequestHelper.get(url,
							RequestParamsPool
									.getAt_meParams(currentpage+"",pagecount+"",""), true,
							new MyAsyncHttpResponseHandler(context) {

								@Override
								public void onFailure(int arg0, Header[] arg1,
										byte[] arg2, Throwable arg3) {
									Log.d("cj", "false=======>>");
								}

								@Override
								public void onResponse(int statusCode,
										Header[] headers, byte[] responseBody) {

									jsonString = new String(responseBody);
									Log.d("cj", "result=======>>" + jsonString);
									parse(type, jsonString,flag);
								}
							});
				}
			});
			break;
		case LOAD_NEW:
			((Activity) context).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WebRequestHelper.get(url,
							RequestParamsPool
									.getAt_meParams(currentpage+"",pagecount+"",""), true,
							new MyAsyncHttpResponseHandler(context) {

								@Override
								public void onFailure(int arg0, Header[] arg1,
										byte[] arg2, Throwable arg3) {
									Log.d("cj", "false=======>>");
								}

								@Override
								public void onResponse(int statusCode,
										Header[] headers, byte[] responseBody) {

									jsonString = new String(responseBody);
									Log.d("cj", "result=======>>" + jsonString);
									parse(type, jsonString,flag);
								}
							});
				}
			});
//			jsonString = getNewJsonString(context,
//					urlParser.getPrevBaseUrl(getParamMap(guid, id)));
			break;
		case LOAD_MORE:
			((Activity) context).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WebRequestHelper.get(url,
							RequestParamsPool
									.getAt_meParams(currentpage+"",pagecount+"",""), true,
							new MyAsyncHttpResponseHandler(context) {

								@Override
								public void onFailure(int arg0, Header[] arg1,
										byte[] arg2, Throwable arg3) {
									Log.d("cj", "false=======>>");
								}

								@Override
								public void onResponse(int statusCode,
										Header[] headers, byte[] responseBody) {
									jsonString = new String(responseBody);
									parse(type, jsonString,flag);
								}
							});
				}
			});
//			jsonString = WebRequest.getWebDataWithContext(context,
//					urlParser.getNextBaseUrl(getParamMap(guid, id)));
			break;
		default:
			throw new IllegalArgumentException("没有对应类型的操作");
		}
		// parse(type, jsonString);
	}
	
	public void loadEntity(final Context context, final int type,
			final int  currentpage, final int pagecount,final String before_guid,final String url,final String user_id,final String user_name,final int flag) {

		this.context = context;
		switch (type) {
		case RELOAD:
			entityList.clear();
		case LOAD_INIT:

			((Activity) context).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WebRequestHelper.get(url,
							RequestParamsPool
									.getBook_commentsParams(currentpage+"",pagecount+"","",user_id,user_name), true,
							new MyAsyncHttpResponseHandler(context) {

								@Override
								public void onFailure(int arg0, Header[] arg1,
										byte[] arg2, Throwable arg3) {
									Log.d("cj", "false=======>>");
								}

								@Override
								public void onResponse(int statusCode,
										Header[] headers, byte[] responseBody) {

									jsonString = new String(responseBody);
									Log.d("cj", "result=======>>" + jsonString);
									parse(type, jsonString,flag);
								}
							});
				}
			});
			break;
		case LOAD_NEW:
			((Activity) context).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WebRequestHelper.get(url,
							RequestParamsPool
									.getBook_commentsParams(currentpage+"",pagecount+"","",user_id,user_name), true,
							new MyAsyncHttpResponseHandler(context) {

								@Override
								public void onFailure(int arg0, Header[] arg1,
										byte[] arg2, Throwable arg3) {
									Log.d("cj", "false=======>>");
								}

								@Override
								public void onResponse(int statusCode,
										Header[] headers, byte[] responseBody) {

									jsonString = new String(responseBody);
									Log.d("cj", "result=======>>" + jsonString);
									parse(type, jsonString,flag);
								}
							});
				}
			});
//			jsonString = getNewJsonString(context,
//					urlParser.getPrevBaseUrl(getParamMap(guid, id)));
			break;
		case LOAD_MORE:
			((Activity) context).runOnUiThread(new Runnable() {

				@Override
				public void run() {
					WebRequestHelper.get(url,
							RequestParamsPool
									.getBook_commentsParams(currentpage+"",pagecount+"","",user_id,user_name), true,
							new MyAsyncHttpResponseHandler(context) {

								@Override
								public void onFailure(int arg0, Header[] arg1,
										byte[] arg2, Throwable arg3) {
									Log.d("cj", "false=======>>");
								}

								@Override
								public void onResponse(int statusCode,
										Header[] headers, byte[] responseBody) {

									jsonString = new String(responseBody);
									parse(type, jsonString,flag);
								}
							});
				}
			});
//			jsonString = WebRequest.getWebDataWithContext(context,
//					urlParser.getNextBaseUrl(getParamMap(guid, id)));
			break;
		default:
			throw new IllegalArgumentException("没有对应类型的操作");
		}
		// parse(type, jsonString);
	}

	public void search(final Context context, final int type, final String query,final int  currentpage, final int pagecount,final int flag) {
//		Map<String, String> map = new HashMap<String, String>();
//		map.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
//		map.put(URLParser.QUERY, query);
//		String jsonString = WebRequest.getWebDataWithContext(context,
//				urlParser.getBaseUrl(map));
//		parse(type, jsonString);
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				WebRequestHelper.get(URLText.TimeLine_Search_URL,
						RequestParamsPool
								.getTimelineSearchParams(currentpage+"",pagecount+"",query), true,
						new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								Log.d("cj", "false=======>>");
							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								jsonString = new String(responseBody);
								parse(type, jsonString,flag);
							}
						});
			}
		});
	}

	public void searchNextPage(final Context context, final int type, final String query,final int  currentpage, final int pagecount,final int flag) {
//		Map<String, String> map = new HashMap<String, String>();
//		map.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
//		map.put(URLParser.QUERY, query);
//		map.put(URLParser.PAGE, Integer.toString(page));
//		String jsonString = WebRequest.getWebDataWithContext(context,
//				urlParser.getNextBaseUrl(map));
//		parse(type, jsonString);
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				WebRequestHelper.get(URLText.TimeLine_Search_URL,
						RequestParamsPool
								.getTimelineSearchParams(currentpage+"",pagecount+"",query), true,
						new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								jsonString = new String(responseBody);
								parse(type, jsonString,flag);
							}
						});
			}
		});
	}

	/**
	 * 得到Entity列表的长度
	 * 
	 * @return entity列表的长度
	 */
	public int getLength() {
		return entityList.size();
	}

	/**
	 * 得到指定位置上的Entity对象
	 * 
	 * @param position
	 *            指定的位置
	 * @return 在指定位置上的Entity对象
	 */
	public Entity getEntityAt(int position) {
		return entityList.get(position);
	}

	/**
	 * 查找指定guid在线性表中的位置
	 * 
	 * @param guid
	 *            待查找的guid
	 * @return 该guid所代表的对象在线性表中的位置
	 */
	public int indexOf(String guid) {
		Entity entity = new Entity();
		entity.setGuid(guid);
		return entityList.indexOf(entity);
	}

	/**
	 * 删除guid所指定的实体
	 * 
	 * @param guid
	 *            实体的guid
	 * @return true代表删除成功，false代表无此对象。
	 */
	public boolean delete(String guid) {
		Entity entity = new Entity();
		entity.setGuid(guid);
		return entityList.remove(entity);
	}

	public void clear() {
		entityList.clear();
	}

	public void refreshData() {
		if (tempList != null) {
			removeDeletedItem(tempList);
			if (!entityList.containsAll(tempList)) {
				if (tail)
					entityList.addAll(tempList);
				else
					entityList.addAll(0, tempList);
			} else
				removeDeletedItem(entityList);
		}
	}

	@Override
	public boolean equals(Object o) {
		boolean result = false;
		if (o instanceof TimeLineModel) {
			TimeLineModel timeLine = (TimeLineModel) o;
			if (timeLine.getLength() == entityList.size()) {
				int i;
				for (i = 0; i < entityList.size(); i++)
					if (timeLine.getEntityAt(i).equals(entityList.get(i)))
						;
				if (i == entityList.size())
					result = true;
			}
		}
		return result;
	}

	@Override
	public int hashCode() {
		return entityList.hashCode();
	}

	@Override
	public String toString() {
		if (entityList != null)
			return entityList.toString();
		else
			return "";
	}

	private Map<String, String> getParamMap(String guid, long id) {
		Map<String, String> map = new HashMap<String, String>();
		map.put(URLParser.GUID, guid);
		map.put(URLParser.ID, Long.toString(id));
		map.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		return map;
	}

	/**
	 * 获取指定的Entity发表后新发表的Entity，这些Entity将会被填充到entityList的头部。由于服务器一次只返回25个数据，
	 * 本方法将递归调用自身，直到没有新数据为止。返回值表示是否有新数据。
	 * 
	 * @param context
	 *            数据上下文
	 * @param url
	 *            指定的URL
	 * @return 含有json数据的字符串
	 */
	private String getNewJsonString(Context context, String url) {
		String jsonString = WebRequest.getWebDataWithContext(context, url);
		if (UiStaticMethod.isNetWorkConnected(jsonString)) {
			try {
				JSONArray array = jsonParser.getJsonArrayFromString(jsonString);
				if (array.length() != 0) {
					String head = getNewJsonString(context,
							urlParser.getPrevBaseUrl(getParamMap(array
									.getJSONObject(0).getString(Entity.GUID),
									UiStaticMethod.ILLEGAL_INDEX)));
					JSONArray headArray = new JSONArray(head);
					if (headArray.length() != 0) {
						headArray.put(array);
						return headArray.toString();
					} else
						return array.toString();
				}
			} catch (JSONException e) {
				Log.e(url, jsonString);
			}
		}
		return jsonString;
	}

	/**
	 * 解析Json数据，并通知观察者
	 * 
	 * @param type
	 *            数据变化的类型
	 * @param jsonString
	 *            待解析的Json字符串
	 */
	private void parse(int type, String jsonString,int flag) {
		boolean connected, tail, deleteOrigin, hasContent = false;
		if (connected = UiStaticMethod.isNetWorkConnected(jsonString)) {
			if (type == LOAD_NEW)
				tail = false;
			else
				tail = true;
			if (type == LOAD_AS_INPUT)
				deleteOrigin = true;
			else
				deleteOrigin = false;
			this.tail = tail;
			hasContent = parseJSON(jsonString, tail, deleteOrigin,flag);
		}
		LocalBroadcastManager.getInstance(context).sendBroadcast(
				new Intent("com.mzread.action.dialog.canceled"));
		notifyDataChanged(type, connected, hasContent);
	}

	/**
	 * 本方法负责解析JSON数据，并将结果用Entity，Book，User，RenderBody表示。
	 * 
	 * @param jsonString
	 *            待解析的Json字符串
	 * @param tail
	 *            true表示新的数据放到链表的尾部 ，false表示新的数据放到链表的头部
	 * @return true表示解析成功，false表示解析失败。如果解析失败，异常信息会打印到LogCat上，错误级别为error
	 */

	private boolean parseJSON(String jsonString, boolean tail,
			boolean deleteOrigin,int flag) {
		this.tempList = null;
		boolean result = true;
		if (jsonString == null){
			result = false;
		}
		else {
			try {
				JSONObject jsonObj = new JSONObject(jsonString);
				JSONArray jsonArray = jsonParser
						.getJsonArrayFromString(jsonString);
				if (jsonArray == null) {
					result = false;
				}else {
					int arrayLength = jsonArray.length();
					if (arrayLength != 0) {
						result = generateTempList(jsonArray, deleteOrigin,
								arrayLength, result);
						if (flag == 0 && jsonObj != null) {
							String recommend_guid = "";
							String since_guid = "";
							if (jsonObj.optString("recommend_guid").endsWith("null")) {
								recommend_guid = "";
							}else {
								recommend_guid = jsonObj.optString("recommend_guid");
								LocalUserSetting.saveRecommend_Guid(context, recommend_guid, LoginUser.getpin());
							}
							if (jsonObj.optString("since_guid").endsWith("null")) {
								since_guid = "";
							}else {
								since_guid = jsonObj.optString("since_guid");
								LocalUserSetting.saveCommunity_Since_Guid(context, since_guid, LoginUser.getpin());
							}
						}
						
					} else{
						result = false;
					}
				}
			} catch (JSONException e) {
				Log.e("timeline", Integer.toString(jsonString.length()) + ' '
						+ jsonString);
				result = false;
			}
		}
		return result;
	}

	/**
	 * @param jsonString
	 * @param deleteOrigin
	 * @param arrayLength
	 * @throws JSONException
	 */
	private boolean generateTempList(JSONArray jsonArray, boolean deleteOrigin,
			int arrayLength, boolean originResult) throws JSONException {
		List<Entity> newEntities = new LinkedList<Entity>();
		for (int i = 0; i < arrayLength; i++) {
			Entity entity = generateEntity(jsonArray, i);
			newEntities.add(entity);
		}
		this.tempList = newEntities;
		if (deleteOrigin) {
			entityList.clear();
			this.tail = false;
		} else{
			originResult = hasContent(newEntities);
			Log.d("cj", "deleteOrigin========>>>" + deleteOrigin + "=======originResult=======>>>>" + originResult);
		}
		return originResult;
	}

	/**
	 * @param jsonString
	 * @param i
	 * @return
	 * @throws JSONException
	 */
	private Entity generateEntity(JSONArray jsonArray, int i)
			throws JSONException {
		Entity entity;
		if (timelineAdapter)
			entity = new Entity();
		else
			entity = new Alert();
		JSONObject jsonObject = jsonParser.getJsonObjectFromArray(jsonArray, i);
		entity.parseJson(jsonObject, true);
		return entity;
	}

	/**
	 * @param newEntities
	 */
	private boolean hasContent(List<Entity> newEntities) {
		boolean result;
//		if (!entityList.containsAll(newEntities)) {
//			result = true;
//		} else
//			result = false;
		if(newEntities !=null && newEntities.size()>0)
			result =true;
		else
			result=false;
		return result;
	}

	/**
	 * 清除列表中被标明为deleted的项目
	 * 
	 * @param list
	 *            带处理的列表
	 */
	private void removeDeletedItem(List<Entity> list) {
		Entity entity;
		for (int i = 0; i < list.size(); i++) {
			entity = list.get(i);
			if (entity.getRenderBody().isDeleted()) {
				list.remove(i);
				i--;
			}
		}
	}
}
