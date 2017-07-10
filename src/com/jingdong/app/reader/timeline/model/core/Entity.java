package com.jingdong.app.reader.timeline.model.core;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.timeline.model.ObservableModel;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.MZLog;
import com.google.zxing.common.StringUtils;
import com.jingdong.app.reader.R;

public class Entity extends ObservableModel implements Parcelable {
	public final static Creator<Entity> CREATOR = new Creator<Entity>() {

		@Override
		public Entity createFromParcel(Parcel source) {
			return new Entity(source);
		}

		@Override
		public Entity[] newArray(int size) {
			return new Entity[size];
		}
	};

	public enum RenderType {
		NOT_EXIST, UserTweet, BookComment, Note, EntityComment
	};

	private final static int NO_RENDER_TYPE = -1;
	private final static int USER_TWEET = 0;
	private final static int BOOK_COMMENT = 1;
	private final static int NOTE = 4;
	private final static int ENTITY_COMMENT = 6;
	private final static String TIME_STAMP = "created_at_timestamp";
	private final static String BOOK = "book";
	private final static String DOCUMENT = "document";
	private final static String USER = "user";
	public final static String RENDER_BODY = "render_body";
	public final static String GUID = "guid";
	public final static String ID = "id";
	public final static String COMMENT_NUMBER = "comments_count";
	public final static String FORWARD_NUMBER = "forwards_count";
	public final static String RECOMMENTS_COUNT = "recommends_count";
	public final static String VIEWERRECOMMENDED = "viewer_recommended";
	public final static String IS_DELETE = "delete";
	public final static String RENDER_TYPE = "render_type";
	private boolean viewerRecommended;
	private boolean topEntity;
	private int renderType = NO_RENDER_TYPE;
	private int commentNumber;
	private int recommendsCount;
	private int forwardNumber;
	private long id;
	private long timeStamp;
	private String guid;
	private RenderBody renderBody;
	private String render_body;
	private Book book;
	private UserInfo user;
	private String message;
	private String code;
	private ArrayList<String> images;

	protected Entity(Parcel source) {
		guid = source.readString();
		id = source.readLong();
		render_body = source.readString();
		timeStamp = source.readLong();
		renderType = source.readInt();
		commentNumber = source.readInt();
		recommendsCount = source.readInt();
		forwardNumber = source.readInt();
		renderBody = source.readParcelable(RenderBody.class.getClassLoader());
		book = source.readParcelable(Book.class.getClassLoader());
		user = source.readParcelable(UserInfo.class.getClassLoader());
		topEntity = (source.readByte() == 0) ? false : true;
		viewerRecommended = (source.readByte() == 0) ? false : true;
	}

	public Entity() {
		renderBody = new RenderBody();
		book = new Book();
		user = new UserInfo();
		images=new ArrayList<String>();
	}

	/**
	 * 从JSONObject中取得数据，并将结果保存到Entity中
	 * 
	 * @param topEntity
	 *            true表示该entity为顶层实体，false表示该entit为第二层实体。
	 * @param jObject
	 *            数据源
	 * @throws JSONException
	 *             解析JSON时发生异常
	 */
	public void parseJson(JSONObject jObject, boolean topEnddtity)
			throws JSONException {
		if (jObject != null) {

				setTopEntity(topEnddtity);
				setGuid(jObject.optString(GUID));
				setId(jObject.optLong(ID));
				
				MZLog.d("wangguodong", "######parseJsonparseJsonparseJsonparseJson");

				String renderType = jObject.optString(RENDER_TYPE);
				int type = NO_RENDER_TYPE;// 动态 render_type: UserTweet, BookComment,// Note, EntityComment
				if (TextUtils.isEmpty(renderType)) {
					type = NO_RENDER_TYPE;
				} else if (renderType.equals("UserTweet")) {
					type = USER_TWEET;
				} else if (renderType.equals("BookComment")) {
					type = BOOK_COMMENT;
				} else if (renderType.equals("Note")) {
					type = NOTE;
				} else if (renderType.equals("EntityComment")) {
					type = ENTITY_COMMENT;
				}
				setRenderType(type);
				MZLog.d("wangguodong", "1111111--%%%"+type);
				//setRenderType(jObject.optInt(RENDER_TYPE, NO_RENDER_TYPE));
				setTimeStamp(jObject.getInt(TIME_STAMP));
				setCommentNumber(jObject.optInt(COMMENT_NUMBER));
				setRecommendsCount(jObject.optInt(RECOMMENTS_COUNT));
				setViewerRecommended(jObject.optBoolean(VIEWERRECOMMENDED));
				setForwardNumber(jObject.optInt(FORWARD_NUMBER));
				getBook().parseJson(jObject.optJSONObject(BOOK));
				getUser().parseJson(jObject.optJSONObject(USER));
				getRenderBody().parseJson(jObject.optJSONObject(RENDER_BODY),
						topEntity);
				//随便说说新增图片
				try {
					setImages(jObject.optJSONArray("images"));
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	private void setImages(JSONArray jsonArray) {
		if(jsonArray!=null){
			if(images==null)
				images=new ArrayList<String>();
			for(int i=0;i<jsonArray.length();i++){
				try {
					images.add(jsonArray.getString(i));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public ArrayList<String> getImages(){
		return images;
	}

	public boolean isViewerRecommended() {
		return viewerRecommended;
	}

	public void setViewerRecommended(boolean viewerRecommended) {
		this.viewerRecommended = viewerRecommended;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public Book getBook() {
		return book;
	}

	public UserInfo getUser() {
		return user;
	}

	public int getCommentNumber() {
		return commentNumber;
	}

	public int getForwardNumber() {
		return forwardNumber;
	}

	public String getGuid() {
		return guid;
	}

	public long getId() {
		return id;
	}

	public RenderBody getRenderBody() {
		return renderBody;
	}

	public boolean isTopEntity() {
		return topEntity;
	}

	public void setRenderType(String type){
		if(type.equals("BookComment")){
			this.renderType = BOOK_COMMENT;
		}
	}
	
	public RenderType getRenderType() {
		RenderType renderType;
		switch (this.renderType) {
		case USER_TWEET:
			renderType = RenderType.UserTweet;
			break;
		case BOOK_COMMENT:
			renderType = RenderType.BookComment;
			break;
		case NOTE:
			renderType = RenderType.Note;
			break;
		case ENTITY_COMMENT:
			renderType = RenderType.EntityComment;
			break;
		default:
			renderType = RenderType.NOT_EXIST;
			break;
		}
		return renderType;
	}

	public void setCommentNumber(int commentNumber) {
		this.commentNumber = commentNumber;
	}

	public void setForwardNumber(int forwardCount) {
		this.forwardNumber = forwardCount;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	protected void setId(long id) {
		this.id = id;
	}

	public void setRenderBody(RenderBody renderBody) {
		this.renderBody = renderBody;
	}

	public void setBook(Book book) {
		this.book = book;
	}
	
	public void setUser(UserInfo user) {
		this.user = user;
	}

	public String getEntityURL() {
		StringBuilder builder = new StringBuilder();
		builder.append(URLText.shareTweetURL);
		builder.append(guid);
		return builder.toString();
	}

	public String getWeiXinTitle(Resources resources) {
		RenderBody renderBody;
		String weixinTitle;
		Book book;
		if ((renderBody = getRenderBody()) != null) {
			weixinTitle = renderBody.getTitle();
			if (TextUtils.isEmpty(weixinTitle)) {
				if ((book = getBook()) == null) {
					weixinTitle = getDefaulWeiXinTitle(resources);
				} else {
					weixinTitle = book.getTitle();
				}
			}
		} else
			weixinTitle = "";
		return weixinTitle;
	}

	public String getShareString(Resources resources) {
		StringBuilder stringBuilder = new StringBuilder();
		String userName, type;
		if (getUser() != null && !TextUtils.isEmpty(getUser().getName())) {
			userName = getUser().getName();
			stringBuilder.append(userName);
			stringBuilder.append(resources.getString(R.string.atJdRead));
			stringBuilder.append(' ');
		}
		if (!TextUtils.isEmpty(type = getSharePrefix(resources))) {
			stringBuilder.append(type);
		}
		if (getRenderBody() != null) {
			getRenderBodyString(stringBuilder, getRenderBody());
		}
		return stringBuilder.toString();
	}
	
	public String getShareToWeixinTitle(Resources resources) {
		StringBuilder stringBuilder = new StringBuilder();
		RenderType renderType = getRenderType();
		
		String userName;
		if (getUser() != null && !TextUtils.isEmpty(getUser().getName())) {
			userName = getUser().getName();
			stringBuilder.append(userName);
			stringBuilder.append(' ');
		}
		
		switch (renderType) {
		case UserTweet:
			stringBuilder.append(resources.getString(R.string.postTweet));
			break;
		case BookComment:
			stringBuilder.append(resources.getString(R.string.postBookCommentNew));
			break;
		case Note:
			stringBuilder.append(resources.getString(R.string.postNote));
			break;
		case EntityComment:
			stringBuilder.append(resources.getString(R.string.postComment));
			break;
		default:
			break;
		}

		return stringBuilder.toString();
	}
	
	
	

	protected void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	void setTopEntity(boolean topEntity) {
		this.topEntity = topEntity;
	}

	void setRenderType(int renderType) {
		this.renderType = renderType;
	}

	public int getRecommendsCount() {
		return recommendsCount;
	}

	public void setRecommendsCount(int recommendsCount) {
		this.recommendsCount = recommendsCount;
	}

	@Override
	public int hashCode() {
		return guid.hashCode();
	}

	@Override
	public String toString() {
		return guid;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Entity && ((Entity) o).getGuid().equals(guid);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(guid);
		dest.writeLong(id);
		dest.writeString(render_body);
		dest.writeLong(timeStamp);
		dest.writeInt(renderType);
		dest.writeInt(commentNumber);
		dest.writeInt(recommendsCount);
		dest.writeInt(forwardNumber);
		dest.writeParcelable(renderBody, flags);
		dest.writeParcelable(book, flags);
		dest.writeParcelable(user, flags);
		dest.writeByte((byte) (topEntity ? 1 : 0));
		dest.writeByte((byte) (viewerRecommended ? 1 : 0));
	}

	private String getDefaulWeiXinTitle(Resources resources) {
		String title;
		UserInfo uInfo = getUser();
		if (uInfo != null)
			title = resources.getString(R.string.sayAtMzread, user.getName());
		else
			title = "";
		return title;
	}

	private String getSharePrefix(Resources resources) {
		StringBuilder stringBuilder = new StringBuilder();
		RenderType renderType = getRenderType();
		switch (renderType) {
		case UserTweet:
			stringBuilder.append(resources.getString(R.string.postTweet));
			stringBuilder.append(": ");
			break;
		case BookComment:
			getBookCommentPrefix(resources, stringBuilder);
			break;
		case Note:
			stringBuilder.append(resources.getString(R.string.postNote));
			stringBuilder.append(":");
			stringBuilder.append(' ');
			if (getBook().bookId > 0) {
				stringBuilder.append("《");
				stringBuilder.append(getBook().getTitle());
				stringBuilder.append("》");
				stringBuilder.append(' ');
			}else{
				stringBuilder.append("《");
				stringBuilder.append(getRenderBody().getDocument().getTitle());
				stringBuilder.append("》");
				stringBuilder.append(' ');			
			}
			break;
		case EntityComment:
			stringBuilder.append(resources.getString(R.string.postComment));
			stringBuilder.append(":");
			stringBuilder.append(' ');
			break;
		default:
			break;
		}

		MZLog.d("wangguodong", "格式化后的数据:"+ stringBuilder.toString());
		return stringBuilder.toString();
	}

	
	public String getBookNameString (){
		if (null!=getBook()) {
			return getBook().getTitle();
		}
		return  "";
	}
	
	public float getBookRating (){
		if ( null!= getRenderBody()) {
			return (float) getRenderBody().getRating();
		}
		return  -1;
	}
	
	private void getBookCommentPrefix(Resources resources,
			StringBuilder stringBuilder) {
		stringBuilder.append(resources.getString(R.string.postBookComment));
		stringBuilder.append(' ');
		if (getBook() != null) {
			stringBuilder.append(getBook().getQuotedTitle());
			stringBuilder.append(' ');
			final int total = 5;
			int times;
			if (getRenderBody() != null
					&& (times = (int) getRenderBody().getRating()) > 0) {
				final char star = '\u2605', unstar = '\u2606';
				for (int i = 0; i < times; i++) {
					stringBuilder.append(star);
				}
				for (int i = 0; i < (total - times); i++) {
					stringBuilder.append(unstar);
				}
				stringBuilder.append(' ');
			}
		}
	}

	private void getRenderBodyString(StringBuilder stringBuilder, RenderBody render) {
		boolean quote;
		if(null == stringBuilder || null == render) {
			return;
		}
		if(null == render.getContent()) {
			return;
		}
		stringBuilder.append(render.getContent().toString());
		if (getRenderType() == RenderType.Note)
			quote = true;
		else
			quote = false;
		if (quote)
			stringBuilder.append('\u300C');
		if(render.getQuote()!=null)
			stringBuilder.append(render.getQuote());
		if (quote)
			stringBuilder.append('\u300D');
	}

	public String getRender_body() {
		return render_body;
	}

	public void setRender_body(String render_body) {
		this.render_body = render_body;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}