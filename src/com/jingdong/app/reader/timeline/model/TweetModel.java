package com.jingdong.app.reader.timeline.model;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.jingdong.app.reader.net.MyAsyncHttpResponseHandler;
import com.jingdong.app.reader.net.RequestParamsPool;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequestHelper;
import com.jingdong.app.reader.timeline.actiivity.TimelineCommentsActivity;
import com.jingdong.app.reader.timeline.model.core.Comment;
import com.jingdong.app.reader.timeline.model.core.Entity;
import com.jingdong.app.reader.user.LoginUser;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ToastUtil;

@SuppressWarnings("deprecation")
public class TweetModel extends Entity {
	public static final Creator<TweetModel> CREATOR = new Creator<TweetModel>() {

		@Override
		public TweetModel createFromParcel(Parcel source) {
			return new TweetModel(source);
		}

		@Override
		public TweetModel[] newArray(int size) {
			return new TweetModel[size];
		}
	};
	private final static String GUID = "guid";
	private final static String ERROR_CODE = "code";
	public final static String ENTITY_ID = "entity_guid";
	private final static String RECOMMAND_COUNT = "recommends_count";
	public final static String RECOMMAND = "viewer_recommended";
	private final static String FAVOURITE = "viewer_favourited";
	private final static String ENTITY_COMMENTS = "entity_comments";
	private final static String ENTITY_FORWARDS = "entity_forwards";
	public final static String ENTITY_RECOMMENDS = "recommends";
	public final static String TOTAL_FORWARDS = "total_forwards_count";
	public final static String TOTAL_COMMENTS = "total_comments_count";
	public final static String TOTAL_RECOMMENDS = "total_recommends_count";
	public final static String COMMENT_GUID = "entity_comment[entity_guid]";
	public final static String COMMENT_CONTENT = "entity_comment[content]";
	public final static String COMMENT_REPLY_ID = "entity_comment[reply_to_entity_comment_id]";
	public final static String COMMENT_TYPE = "entity_comment[repost]";
	public final static String COMMENT_ORIGIN_CONTENT = "entity_comment[orig_content]";
	public final static String WITHOUT_COMMENTS = "without_comments";
	public final static String BEFORE_ID = "before_id";
	private final static int ONLY_COMMENT = 0;
	private final static int COMMENT_FORWARD = 1;
	private final static int FORWARD_COMMAND = 2;
	private final static int ONLY_FORWARD = 3;
	public final static int MODEL_COMMENT = 1;
	public final static int MODEL_FORWARD = 2;
	public final static int MODEL_RECOMMEND = 3;
	public final static String ERROR = "error";
	public final static int ERROR_NOT_FOUND = 404;
	public final static long DEFAULT_ERROR_VALUE = -1;
	public final static int INIT_LOAD = 10;
	public final static int TAB_CHANGED = 11;
	public final static int CLICK_FAVOURITE = 12;
	public final static int CLICK_RECOMMAND = 13;
	public final static int LOAD_COMMENT = 14;
	public final static int LOAD_FORWARD = 15;
	public final static int DELETE_ENTITY = 16;
	public final static int DELETE_COMMENT = 17;
	public final static int POST_COMMENT = 20;
	public final static int POST_FORWARD = 21;
	public final static int LOAD_NEXT_COMMENT = 22;
	public final static int LOAD_NEXT_FORWARD = 23;
	public final static int LOAD_MOCK = 24;
	public final static int LOAD_NOTE_AS_ENTITY = 25;
	public final static int LOAD_RECOMMEND = 26;
	public final static int LOAD_NEXT_RECOMMEND = 27;
	private List<Comment> recommends;
	private List<Comment> comments;
	private List<Comment> forwardComments;
	private List<Comment> tempRecommends = new LinkedList<Comment>();
	private List<Comment> tempComments = new LinkedList<Comment>();
	private List<Comment> tempForwards = new LinkedList<Comment>();
	private String guid;
	private int modelType;
	private int totalComments;
	private int totalForwards;
	private int totalRecommends;
	private boolean recommand;
	private boolean favourite;
	private boolean comment = true;
	private boolean loading = true;

	private TweetModel(Parcel source) {
		super(source);
		if (comments == null)
			comments = new LinkedList<Comment>();
		source.readTypedList(comments, Comment.CREATOR);
		if (forwardComments == null)
			forwardComments = new LinkedList<Comment>();
		source.readTypedList(forwardComments, Comment.CREATOR);
		if (recommends == null)
			recommends = new LinkedList<Comment>();
		source.readTypedList(recommends, Comment.CREATOR);
		totalComments = source.readInt();
		totalForwards = source.readInt();
		totalRecommends = source.readInt();
		modelType = source.readInt();
		recommand = (source.readByte() == 0) ? false : true;
		favourite = (source.readByte() == 0) ? false : true;
		comment = (source.readByte() == 0) ? false : true;
		loading = (source.readByte() == 0) ? false : true;
	}

	public TweetModel() {
		super();
		comments = new LinkedList<Comment>();
		forwardComments = new LinkedList<Comment>();
		recommends = new LinkedList<Comment>();
	}

	/**
	 * 从服务器获得用户的单条动态
	 * 
	 * @param guid
	 *            单条动态的标志符。
	 */
	public void initTweet(String guid, Context context, int type) {
		parseJson(guid, context, type);
	}

	public void initTweet(long noteId, Context context, int type) {
		parseJson(noteId, context, type);
	}

	/**
	 * 当前动态是否包含评论
	 * 
	 * @return
	 */
	public boolean hasComments() {
		return comments.size() != 0;
	}

	/**
	 * 当前动态是否包含转发
	 * 
	 * @return
	 */
	public boolean hasForwardComments() {
		return forwardComments.size() != 0;
	}

	/**
	 * 得到指定的评论
	 * 
	 * @param index
	 *            第几条评论
	 * @return 所得到的评论，如果评论列表为不包含内容，则返回空指针
	 */
	public Comment getCommentAt(int index) {
		return comments.get(index);
	}

	/**
	 * 得到指定的转发
	 * 
	 * @param index
	 *            第几条转发
	 * @return 所得到的转发，如果转发列表为不包含内容，则返回空指针
	 */
	public Comment getForwardAt(int index) {
		return forwardComments.get(index);
	}

	public Comment getRecommendAt(int index) {
		return recommends.get(index);
	}

	/**
	 * 当前显示的评论列表是comments还是forwardsComments
	 * 
	 * @return true表示为comments，false表示为forwardsComments
	 */
	public boolean isComments() {
		return comment;
	}

	/**
	 * 设置当前评论列表显示的内容。
	 * 
	 * @param isComments
	 *            true表示为comments，false表示为forwardsComments
	 */
	public void setComments(boolean isComments) {
		if (isComments != this.comment) {
			this.comment = isComments;
			notifyDataChanged(TAB_CHANGED, true);
		}
	}

	public int getModelType() {
		return this.modelType;
	}

	public void setModelType(int type) {
		if (this.modelType != type) {
			this.modelType = type;
			notifyDataChanged(TAB_CHANGED, true);
		}
	}

	/**
	 * 用户点击favourite按钮后，本方法负责向服务器提交结果
	 * 
	 * @param context
	 *            当前数据上下文
	 */
	public void clickFavourite(Context context) {
		boolean nextState = !favourite;
		String urlString;
		if (nextState)
			urlString = URLText.favouriteUrl;
		else
			urlString = URLText.unFavouriteUrl;
		postRecommandFavourite(context, nextState, urlString, getGuid(), 1);

	}

	/**
	 * 用户点击recommend按钮后，本方法负责向服务器提交结果
	 * 
	 * @param context
	 *            当前数据上下文
	 */
	public void clickRecommand(Context context) {
		boolean nextState = !recommand;
		String urlString;
		if (nextState)
			urlString = URLText.likeEntityUrl;
		else
			urlString = URLText.unlikeEntityUrl;
		postRecommandFavourite(context, nextState, urlString, getGuid(), 2);

	}

	/**
	 * 重新解析Json数据，如果有数据，则通知观察者数据变化。
	 * 
	 * @param guid
	 *            当前动态的guid
	 * @param type
	 *            以何种方式解析数据
	 */
	public void loadCommentOrForward(final Context context, String guid,
			final int type) {
		final String url = getUrl(context, guid, type);
		final String jsonKey = getJsonKey(type);
		MZLog.d("wangguodong", "loadCommentOrForward....." + url);
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				WebRequestHelper.get(url, RequestParamsPool.getEmptyParams(),
						true, new MyAsyncHttpResponseHandler(context) {
							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								boolean hasContent = false;
								try {
									String jsonString = new String(responseBody);

									// MZLog.d("wangguodong",
									// "请求数据:"+jsonString);
									JSONObject jObject = new JSONObject(
											jsonString);
									totalComments = jObject.optInt(
											TOTAL_COMMENTS, totalComments);
									totalForwards = jObject.optInt(
											TOTAL_FORWARDS, totalForwards);
									totalRecommends = jObject.optInt(
											TOTAL_RECOMMENDS, totalRecommends);
									recommand = jObject.optBoolean(RECOMMAND,recommand);
									JSONArray array = jObject
											.getJSONArray(jsonKey);
									List<Comment> temp = new ArrayList<Comment>(
											array.length());
									if (array.length() != 0) {
										hasContent = true;
										fillComments(array, temp);
									}
									addCommentsToTempField(temp, type);
								} catch (Exception e) {
									MZLog.e("wangguodong", e.getMessage());
								}
								notifyDataChanged(type, true, hasContent);
							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								MZLog.d("wangguodong", "请求出错了");
								notifyDataChanged(type, false, false);
							}
						});
			}
		});

	}

	public void refreshData(int type) {
		switch (type) {
		case LOAD_COMMENT:
			comments.clear();
		case LOAD_NEXT_COMMENT:
			if (tempComments != null)
				comments.addAll(tempComments);
			break;
		case LOAD_FORWARD:
			forwardComments.clear();
		case LOAD_NEXT_FORWARD:
			if (tempForwards != null)
				forwardComments.addAll(tempForwards);
			break;
		case LOAD_RECOMMEND:
			recommends.clear();
		case LOAD_NEXT_RECOMMEND:
			if (tempRecommends != null)
				recommends.addAll(tempRecommends);
			break;
		}
	}

	/**
	 * 向服务器发送一条评论或转发
	 * 
	 * @param context
	 *            数据上下文
	 * @param bundle
	 *            创建一条评论所需要的数据。
	 */
	public void postComment(final Context context, final Bundle bundle) {

		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {

				WebRequestHelper.post(URLText.commmentsPostUrl,
						RequestParamsPool
								.getEntitysCommentsOrForwordParams(bundle),
						true, new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								String result = new String(responseBody);

								MZLog.d("wangguodong", result);

								if (bundle
										.getBoolean(TimelineCommentsActivity.IS_COMMENT))
									notifyDataChanged(POST_COMMENT,
											parsePostResult(result));
								else
									notifyDataChanged(POST_FORWARD,
											parsePostResult(result));

							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								ToastUtil.showToastInThread("请求失败了,请重试!",
										Toast.LENGTH_SHORT);
							}
						});
			}
		});

	}

	/**
	 * 判断当前用户是否是这条动态的作者
	 * 
	 * @param context
	 *            数据上下文
	 * @return true表示当前登录用户是这条动态的作者，false表示当前登录的用户不是这条动态的作者。
	 */
	public boolean isTweetAuthor(Context context) {
		return LoginUser.getpin().equals(getUser().getUserPin());
	}

	/**
	 * 删除当前动态，并通知观察者动态已删除
	 * 
	 * @param context
	 *            数据上下文
	 */
	public void delteTweet(final Context context) {

		final String url = URLText.deleteEntityUrl + getGuid()
				+ "?jd_user_name=" + LoginUser.getpin();

		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {

				WebRequestHelper.delete(url, true,
						new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								String result = new String(responseBody);
								boolean flag = parsePostResult(result);
								notifyDataChanged(DELETE_ENTITY, flag);

							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								ToastUtil.showToastInThread("请求失败了，请重试!",
										Toast.LENGTH_SHORT);

							}

						});

			}
		});

	}

	/**
	 * 删除指定评论，并通知观察者评论已删除。
	 * 
	 * @param context
	 *            数据上下文
	 * @param comment
	 *            待删除评论
	 */
	public void deleteComment(final Context context, Comment comment) {

		final String url = URLText.deleteCommentUrl + comment.getId()
				+ "?jd_user_name=" + LoginUser.getpin();
		;
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {

				WebRequestHelper.delete(url, true,
						new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								String result = new String(responseBody);

								MZLog.d("wangguodong", result);

								boolean flag = ObservableModel
										.parsePostResult(result);
								notifyDataChanged(TweetModel.DELETE_COMMENT,
										flag);

							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
								ToastUtil.showToastInThread("请求失败了，请重试!",
										Toast.LENGTH_SHORT);

							}

						});

			}
		});

	}

	public List<Comment> getComments() {
		return comments;
	}

	public boolean isRecommand() {
		return recommand;
	}

	public boolean isFavourite() {
		return favourite;
	}
	
	public void setFavourite(boolean favourite){
		this.favourite = favourite;
	}

	/**
	 * 得到原始评论列表的总大小，这个大小是保存在父类中
	 * 
	 * @return
	 */
	public int getOriginCommentNumber() {
		return super.getCommentNumber();
	}

	/**
	 * 得到原始转发列表的总大小，这个大小是保存在父类中
	 * 
	 * @return
	 */
	public int getOriginForwardNumber() {
		return super.getForwardNumber();
	}

	/**
	 * 得到评论列表的总大小，这个大小是根据json数据中total_comments_count计算的，可能比当前内存中的评论数量多。
	 */
	@Override
	public int getCommentNumber() {
		return totalComments;
	}

	/**
	 * 得到转发列表的总大小，这个大小是根据json数据中total_forwards_count计算的，可能比当前内存中的转发数量多。
	 */
	@Override
	public int getForwardNumber() {
		return totalForwards;
	}

	/**
	 * 得到评论数组的大小，这个大小是根据当前内存中的数据来计算的，不包含未加载的数据。
	 * 
	 * @return 评论数组的大小
	 */
	public int getCurrentCommentNumber() {
		return comments.size();
	}

	/**
	 * 得到转发数组的大小，这个大小是根据当前内存中的数据来计算的，不包含未加载的数据。
	 * 
	 * @return 转发数组的大小
	 */
	public int getCurrentForwardNumber() {
		return forwardComments.size();
	}

	public int getCurrentRecommendsCount() {
		return recommends.size();
	}

	public int getRecommendsCount() {
		return totalRecommends;
	}

	void setComments(List<Comment> comments) {
		this.comments = comments;
	}

	public boolean isLoading() {
		return loading;
	}

	public void setLoading(boolean loading) {
		this.loading = loading;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeTypedList(comments);
		dest.writeTypedList(forwardComments);
		dest.writeTypedList(recommends);
		dest.writeInt(totalComments);
		dest.writeInt(totalForwards);
		dest.writeInt(totalRecommends);
		dest.writeInt(modelType);
		dest.writeByte((byte) (recommand ? 1 : 0));
		dest.writeByte((byte) (favourite ? 1 : 0));
		dest.writeByte((byte) (comment ? 1 : 0));
		dest.writeByte((byte) (loading ? 1 : 0));
	}

	public static boolean resourceNotFound(JSONObject jsonObject, int errorCode){
		try {
			if (Integer.parseInt(jsonObject.getString(ERROR_CODE)) == errorCode - 403)
				return true;
			else
				return false;
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
	}

	private void notifyDataSetChanged(int phraseResult, int type) {
		boolean connceted = false, hasContent = false;
		switch (phraseResult) {
		case SUCCESS_INT:
			Log.d("cj", "SUCCESS_INT======>>");
			connceted = true;
			hasContent = true;
			break;
		case FAIL_INT:
			connceted = false;
			hasContent = false;
			break;
		case ERROR_NOT_FOUND:
			connceted = true;
			hasContent = false;
			break;
		}
		notifyDataChangedWithGuid(type, connceted, hasContent);
	}

	private void notifyDataChangedWithGuid(int type, boolean connected,
			boolean hasContent) {

		Message message = Message.obtain();
		message.what = type;
		message.arg1 = connected ? SUCCESS_INT : FAIL_INT;
		message.arg2 = hasContent ? SUCCESS_INT : FAIL_INT;
		message.obj = guid;
		setChanged();
		notifyObservers(message);
	}

	/**
	 * 设置entity_comment[repost]这个属性
	 * 
	 * @param comment
	 *            true表示当前留言为评论，false表示当前留言为转发
	 * @param checked
	 *            true表示用户点击了底下的checkedbox，false表示用户取消选择了checkedbox
	 * @return 0只评论，1评论并转发 2转发并评论 3只转发
	 */
	public static String setRepostArea(boolean comment, boolean checked) {
		int result;
		if (comment)
			if (checked)
				result = COMMENT_FORWARD;
			else
				result = ONLY_COMMENT;
		else if (checked)
			result = FORWARD_COMMAND;
		else
			result = ONLY_FORWARD;
		return Integer.toString(result);
	}

	private String getUrl(Context context, String guid, int type) {
		String baseUrl = null, url = null;
		long lastId = -1;
		switch (type) {
		case LOAD_NEXT_COMMENT:
			if (!comments.isEmpty())
				lastId = comments.get(comments.size() - 1).getId();
		case LOAD_COMMENT:
			baseUrl = URLText.commentListUrl;
			break;
		case LOAD_NEXT_FORWARD:
			if (!forwardComments.isEmpty())
				lastId = forwardComments.get(forwardComments.size() - 1)
						.getId();
		case LOAD_FORWARD:
			baseUrl = URLText.forwardListUrl + guid + ".json";
			break;
		case LOAD_NEXT_RECOMMEND:
			if (!recommends.isEmpty())
				lastId = recommends.get(recommends.size() - 1).getId();
		case LOAD_RECOMMEND:
			baseUrl = URLText.recommandListUrl;
			break;
		}
		if (baseUrl != null) {
			url = baseUrl;
			Map<String, String> paramMap = new HashMap<String, String>();
			paramMap.put("jd_user_name", LoginUser.getpin());
			paramMap.put(TweetModel.ENTITY_ID, guid);
			if (lastId != -1) {
				paramMap.put(BEFORE_ID, Long.toString(lastId));
			}
			url = URLBuilder.addParameter(baseUrl, paramMap);
		}
		return url;
	}

	private String getJsonKey(int type) {
		String jsonKey = null;
		switch (type) {
		case LOAD_NEXT_COMMENT:
		case LOAD_COMMENT:
			jsonKey = ENTITY_COMMENTS;
			break;
		case LOAD_NEXT_FORWARD:
		case LOAD_FORWARD:
			jsonKey = ENTITY_FORWARDS;
			break;
		case LOAD_NEXT_RECOMMEND:
		case LOAD_RECOMMEND:
			jsonKey = ENTITY_RECOMMENDS;
			break;
		}
		return jsonKey;
	}

	private void addCommentsToTempField(List<Comment> comments, int type) {
		switch (type) {
		case LOAD_NEXT_COMMENT:
		case LOAD_COMMENT:
			tempComments.clear();
			tempComments.addAll(comments);
			break;
		case LOAD_NEXT_FORWARD:
		case LOAD_FORWARD:
			tempForwards.clear();
			tempForwards.addAll(comments);
			break;
		case LOAD_NEXT_RECOMMEND:
		case LOAD_RECOMMEND:
			tempRecommends.clear();
			tempRecommends.addAll(comments);
			break;
		}
	}

	/**
	 * 从JSONObject中取得数据，并将结果保存到TweetEntity中
	 * 
	 * @param guid
	 *            待请求对象的guid
	 */
	private void parseJson(String guid, Context context, int type) {
		requestTweet(context, guid, type);
	}

	private void parseJson(long noteId, Context context, int type) {
		requestTweet(context, noteId, type);
	}

	/**
	 * @param jsonString
	 * @return
	 */
	private int paresJson(String jsonString) {
		int result;
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			if (resourceNotFound(jsonObject, ERROR_NOT_FOUND)) {
				result = ERROR_NOT_FOUND;
			} else {
				super.parseJson(jsonObject, true);
				guid = jsonObject.optString(GUID);
				recommand = jsonObject.optBoolean(RECOMMAND);
				favourite = jsonObject.optBoolean(FAVOURITE);
				totalRecommends = jsonObject.optInt(RECOMMAND_COUNT);
				result = ObservableModel.SUCCESS_INT;
			}

		} catch (JSONException e) {
			MZLog.e("timeline_tweet", Log.getStackTraceString(e));
			result = ObservableModel.FAIL_INT;
		}
		return result;
	}

//	private void formatContent(JSONObject jsonObject) {
//		JSONObject renderBodyJson = jsonObject
//				.optJSONObject(Entity.RENDER_BODY);
//		String content = renderBodyJson.optString(RenderBody.RENDER_CONTENT);
//		String quote = renderBodyJson.optString(RenderBody.RENDER_QUOTE);
//		RenderBody renderBody = getRenderBody();
//		renderBody.setContent(content);
//		renderBody.setQuote(quote);
//	}

	/**
	 * 根据JsonArray,初始化评论列表
	 * 
	 * @param jsonArray
	 *            数据源
	 * @param comments
	 *            待初始化的评论列别
	 * @throws JSONException
	 */
	private void fillComments(JSONArray jsonArray, List<Comment> comments)
			throws JSONException {
		JSONObject jsonObject;
		for (int i = 0; i < jsonArray.length(); i++) {
			jsonObject = jsonArray.getJSONObject(i);
			Comment comment = new Comment();
			comment.parseJson(jsonObject);
			comments.add(comment);
		}
	}

	/**
	 * 向服务器提交点赞，喜欢操作
	 * @param context
	 * @param nextState
	 * @param baseUrl
	 * @param entityId
	 * @param type
	 */
	private void postRecommandFavourite(final Context context,
			Boolean nextState, final String baseUrl, String entityId, int type) {

		final boolean tempNextState = nextState;
		final int temptype = type;

		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {

				WebRequestHelper.post(baseUrl,
						RequestParamsPool.getTimelineFavoriteParams(getGuid()),
						true, new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {

								MZLog.d("wangguodong", new String(responseBody));

								boolean success;
								success = parsePostResult(new String(
										responseBody));

								if (success) {
									if (temptype == 1)
										favourite = tempNextState;
									else
										recommand = tempNextState;
								} else {
									try {

										JSONObject object = new JSONObject(
												new String(responseBody));

										String msg = object
												.optString("message");

										if (!TextUtils.isEmpty(msg))
											ToastUtil.showToastInThread(msg,
													Toast.LENGTH_SHORT);

									} catch (Exception e) {
										e.printStackTrace();
									}

								}

								if (temptype == 1)
									notifyDataChanged(CLICK_FAVOURITE, success);
								else
									notifyDataChanged(CLICK_RECOMMAND, success);

							}

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {

								ToastUtil.showToastInThread("请求出错了，请检查网络！",
										Toast.LENGTH_SHORT);
							}
						});

			}
		});

	}

	int result;

	private void requestTweet(final Context context, final String guid,
			final int type) {
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				WebRequestHelper.get(URLText.TimeLine_detail_URL + guid,
						RequestParamsPool.getTimelineDetailParams(), true,
						new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {
								String jsonString = new String(responseBody);
								result = paresJson(jsonString);
								notifyDataSetChanged(result, type);
							}
						});
			}
		});
	}

	private int results;

	private void requestTweet(final Context context, final long noteId,
			final int type) {
		// Map<String, String> paramMap = new HashMap<String, String>();
		// paramMap.put(WebRequest.AUTH_TOKEN,
		// LocalUserSetting.getToken(context));
		// paramMap.put(WITHOUT_COMMENTS, Boolean.toString(true));
		// String urlText = URLBuilder.addParameter(URLText.showAsEntityUrl
		// + String.valueOf(noteId) + ".json", paramMap);
		// String result = WebRequest.getWebDataWithContext(context, urlText);
		((Activity) context).runOnUiThread(new Runnable() {

			@Override
			public void run() {
				WebRequestHelper.post(URLText.BookNote_Time, RequestParamsPool
						.getBookNoteTimelineParams(noteId + ""), true,
						new MyAsyncHttpResponseHandler(context) {

							@Override
							public void onFailure(int arg0, Header[] arg1,
									byte[] arg2, Throwable arg3) {
							}

							@Override
							public void onResponse(int statusCode,
									Header[] headers, byte[] responseBody) {
								String jsonString = new String(responseBody);
								results = paresJson(jsonString);
								notifyDataSetChanged(results, type);
							}
						});
			}
		});
		// return result;
	}
}
