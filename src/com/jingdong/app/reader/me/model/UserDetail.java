package com.jingdong.app.reader.me.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Parcel;
import android.util.Log;

import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.me.fragment.UserFragment;
import com.jingdong.app.reader.net.URLBuilder;
import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.net.WebRequest;
import com.jingdong.app.reader.timeline.model.TweetModel;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.user.UserInfo;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.UiStaticMethod;

public class UserDetail extends UserInfo {
	public static final Creator<UserDetail> CREATOR = new Creator<UserDetail>() {

		@Override
		public UserDetail createFromParcel(Parcel source) {
			return new UserDetail(source);
		}

		@Override
		public UserDetail[] newArray(int size) {
			return new UserDetail[size];
		}
	};
	public static final String CURRENT_USER_FOLLOWED_BY = "current_user_is_followed_by";
	public static final String CURRENT_USER_FOLLOWING = "current_user_is_following";
	public static final String WITHOUT_ENTITY = "without_entities";
	private static final String USER = "user";
	private static final String SOCIAL_ACCOUNT="social_accounts";
	private static final String PUBLIC_SOCIAL_ACCOUNT = "public_social_accounts";
	private static final String FOLLOWING_USERS_COUNT = "following_users_count";
	private static final String FOLLOWER_USERS_COUNT = "follower_users_count";
	private static final String NOTES_COUNT = "notes_entities_count";
	private static final String READ_BOOKS_COUNT = "read_books_count";
	private static final String WISH_BOOKS_COUNT = "wish_books_count";
	private static final String ENTITYES_COUNT = "entity_count";
	private static final String BOOK_COMMENT_COUNT = "book_comment_count";
	private static final String EBOOK_COUNT = "ebook_count";
	private static final String DOCUMENT_COUNT = "document_count";
	private static final String FAVOURITE_COUNT = "favourite_count";
	
	private static final String USER_TWEET_COUNT = "user_tweet_count";
	private static final String BOOK_COUNT_IN_BOOKSHELF = "books_count_in_book_shelf";
	private static final String AUTHOR_BOOK_COUNT = "total";
	private static final String AS_AUTHOR = "as_author";
	private static final String BOOKS = "books";
	
	
	private SocialModel[] socialModels;
	private boolean followedByCurrentUser;
	private boolean followingCurrentUser;
	private int followingCount;
	private int followerCount;
	private int entitiesCount;
	private int bookCommentsCount;
	private int notesCount;
	private int readBookCount;
	private int wishBookCount;
	private int boughtBookCount;
	private int importBookCount;
	private int favouriteCount;
	
	private int userTweetCount;
	private int bookCountInBookshelf;
	private int total;
	private Book[] books;
	

	public UserDetail() {

	}

	protected UserDetail(Parcel source) {
		super(source);
		int length=source.readInt();
		if(length!=0){
			socialModels = new SocialModel[length];
			source.readTypedArray(socialModels, SocialModel.CREATOR);
		}
		followedByCurrentUser = (source.readByte() == 0) ? false : true;
		followingCurrentUser = (source.readByte() == 0) ? false : true;
		followingCount = source.readInt();
		followerCount = source.readInt();
		entitiesCount = source.readInt();
		bookCommentsCount = source.readInt();
		notesCount = source.readInt();
		readBookCount = source.readInt();
		wishBookCount = source.readInt();
		boughtBookCount = source.readInt();
		importBookCount = source.readInt();
		favouriteCount = source.readInt();
		
		userTweetCount= source.readInt();
		bookCountInBookshelf= source.readInt();
		total= source.readInt();
		
		int len=source.readInt();
		if(len!=0){
			books = new Book[len];
			source.readTypedArray(books, Book.CREATOR);
		}
	}

	@Override
	public void parseJson(JSONObject jObject) throws JSONException {
		JSONObject userJsonObject = jObject.optJSONObject(USER);
		if (userJsonObject == null)
			userJsonObject = jObject;
		super.parseJson(userJsonObject);
		JSONArray socialArray=userJsonObject.optJSONArray(PUBLIC_SOCIAL_ACCOUNT);
		if(socialArray==null)
			socialArray=userJsonObject.optJSONArray(SOCIAL_ACCOUNT);
		parseSocialJson(socialArray);
		setFollowedByCurrentUser(jObject.optBoolean(CURRENT_USER_FOLLOWING));
		setFollowingCurrentUser(jObject.optBoolean(CURRENT_USER_FOLLOWED_BY));
		setFollowingCount(jObject.optInt(FOLLOWING_USERS_COUNT));
		setFollowerCount(jObject.optInt(FOLLOWER_USERS_COUNT));
		setEntitiesCount(jObject.optInt(ENTITYES_COUNT));
		setBookCommentsCount(jObject.optInt(BOOK_COMMENT_COUNT));
		setNotesCount(jObject.optInt(NOTES_COUNT));
		setReadBookCount(jObject.optInt(READ_BOOKS_COUNT));
		setWishBookCount(jObject.optInt(WISH_BOOKS_COUNT));
		setBoughtBookCount(jObject.optInt(EBOOK_COUNT));
		setImportBookCount(jObject.optInt(DOCUMENT_COUNT));
		setFavouriteCount(jObject.optInt(FAVOURITE_COUNT));
		
		setUserTweetCount(jObject.optInt(USER_TWEET_COUNT));
		setBookCountInBookshelf(jObject.optInt(BOOK_COUNT_IN_BOOKSHELF));
		

		JSONObject bookJsonObject = userJsonObject.optJSONObject(AS_AUTHOR);
		if(null!=bookJsonObject)
		{	setTotal(bookJsonObject.optInt(AUTHOR_BOOK_COUNT));
			JSONArray bookArray=bookJsonObject.optJSONArray(BOOKS);
			parseBookJson(bookArray);
		}
		else{
			setTotal(0);
		}
		
	}

	private void parseBookJson(JSONArray jsonArray) {
		if (jsonArray != null && jsonArray.length() != 0) {
			books = new Book[jsonArray.length()];
			for (int i = 0; i < books.length; i++) {
				books[i] = new Book();
				try {
					books[i].parseJson(jsonArray.optJSONObject(i));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 根据用户名向服务器请求用户数据，并解析返回的Json字符串，之后通知观察者。
	 *
	 * @param context
	 *            数据上下文
	 * @param userName
	 *            用户名
	 */
	@Deprecated
	public void parseJson(Context context, String userName) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		try {
			String url = URLBuilder.addParameter(URLText.userNameUrl + URLEncoder.encode(userName, WebRequest.CHAR_SET)
					+ ".json", paramMap);
			String jsonString = WebRequest.getWebDataWithContext(context, url);
			parseJson(jsonString);
		} catch (UnsupportedEncodingException e) {
			MZLog.e("UserInfo", Log.getStackTraceString(e));
		}
	}

	/**
	 * 根据用户Id向服务器请求用户数据，并解析返回的Json字符串，之后通知观察者。
	 *
	 * @param context
	 *            数据上下文
	 * @param userId
	 *            用户Id
	 */
	@Deprecated
	public void parseJson(Context context, long userId) {
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WITHOUT_ENTITY, Boolean.toString(true));
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		String url = URLBuilder.addParameter(URLText.usersPublicUrl + userId + ".json", paramMap);
		String jsonString = WebRequest.getWebDataWithContext(context, url);
		parseJson(jsonString);
	}

	/**
	 * 根据用户Id向服务器请求数据，这个方法访问服务器的/setting.json页面，主要用来取得第三方绑定信息，不可作为取得用户详细资料的入口
	 * @param context
	 */
	@Deprecated
	public void parseJson(Context context){
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put(WebRequest.AUTH_TOKEN, LocalUserSetting.getToken(context));
		String url=URLBuilder.addParameter(URLText.settingUrl, paramMap);
		String jsonString=WebRequest.getWebDataWithContext(context, url);
		parseJson(jsonString);
	}
	
	/**
	 * 判断当前登录的用户是否和目前这个对象所代表的用户是一个用户
	 *
	 * @param context
	 *            数据上下文
	 * @return true表示两个用户是同一个用户，false表示两个用户不是同一个用户。
	 */
	public boolean isCurrentUser(Context context) {
		return LocalUserSetting.getUserId(context) == getId();
	}

	public boolean isFollowedByCurrentUser() {
		return followedByCurrentUser;
	}

	public boolean isFollowingCurrentUser() {
		return followingCurrentUser;
	}

	public int getFollowingCount() {
		return followingCount;
	}

	public int getFollowerCount() {
		return followerCount;
	}

	public int getEntitiesCount() {
		return entitiesCount;
	}

	public int getBookCommentsCount() {
		return bookCommentsCount;
	}

	public int getNotesCount() {
		return notesCount;
	}

	public int getReadBookCount() {
		return readBookCount;
	}

	public int getWishBookCount() {
		return wishBookCount;
	}

	public int getBoughtBookCount() {
		return boughtBookCount;
	}

	public int getImportBookCount() {
		return importBookCount;
	}

	public int getFavouriteCount() {
		return favouriteCount;
	}

	public void setFollowedByCurrentUser(boolean followedByCurrentUser) {
		this.followedByCurrentUser = followedByCurrentUser;
	}

	public SocialModel getSinalModel() {
		String source;
		if (socialModels != null) {
			for (SocialModel socialModel : socialModels) {
				source = socialModel.getSource();
				if (source != null && source.equals(SocialModel.SINA)) {
					return socialModel;
				}
			}
		}
		return null;
	}
	
	public SocialModel getTencentModel() {
		String source;
		if (socialModels != null) {
			for (SocialModel socialModel : socialModels) {
				source = socialModel.getSource();
				if (source != null && source.equals("qq")) {
					return socialModel;
				}
			}
		}
		return null;
	}
	
	public SocialModel getDoubanModel() {
		String source;
		if (socialModels != null) {
			for (SocialModel socialModel : socialModels) {
				source = socialModel.getSource();
				if (source != null && source.equals("douban")) {
					return socialModel;
				}
			}
		}
		return null;
	}

	void setFollowingCurrentUser(boolean followingCurrentUser) {
		this.followingCurrentUser = followingCurrentUser;
	}

	void setFollowingCount(int followingCount) {
		this.followingCount = followingCount;
	}

	void setFollowerCount(int followerCount) {
		this.followerCount = followerCount;
	}

	void setEntitiesCount(int entitiesCount) {
		this.entitiesCount = entitiesCount;
	}

	void setBookCommentsCount(int bookCommentsCount) {
		this.bookCommentsCount = bookCommentsCount;
	}

	void setNotesCount(int notesCount) {
		this.notesCount = notesCount;
	}

	void setReadBookCount(int readBookCount) {
		this.readBookCount = readBookCount;
	}

	void setWishBookCount(int wishBookCount) {
		this.wishBookCount = wishBookCount;
	}

	void setBoughtBookCount(int boughtBookCount) {
		this.boughtBookCount = boughtBookCount;
	}

	void setImportBookCount(int importBookCount) {
		this.importBookCount = importBookCount;
	}

	void setFavouriteCount(int favouriteCount) {
		this.favouriteCount = favouriteCount;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		if (socialModels == null)
			dest.writeInt(0);
		else {
			dest.writeInt(socialModels.length);
			dest.writeTypedArray(socialModels, flags);
		}
		dest.writeByte((byte) (followedByCurrentUser ? 1 : 0));
		dest.writeByte((byte) (followingCurrentUser ? 1 : 0));
		dest.writeInt(followingCount);
		dest.writeInt(followerCount);
		dest.writeInt(entitiesCount);
		dest.writeInt(bookCommentsCount);
		dest.writeInt(notesCount);
		dest.writeInt(readBookCount);
		dest.writeInt(wishBookCount);
		dest.writeInt(boughtBookCount);
		dest.writeInt(importBookCount);
		dest.writeInt(favouriteCount);
		dest.writeInt(userTweetCount);
		dest.writeInt(bookCountInBookshelf);
		dest.writeInt(total);
		if (books == null)
			dest.writeInt(0);
		else {
			dest.writeInt(books.length);
			dest.writeTypedArray(books, flags);
		}

	}

	private void parseSocialJson(JSONArray jsonArray) {
		if (jsonArray != null && jsonArray.length() != 0) {
			socialModels = new SocialModel[jsonArray.length()];
			for (int i = 0; i < socialModels.length; i++) {
				socialModels[i] = new SocialModel();
				socialModels[i].parseJson(jsonArray.optJSONObject(i));
			}
		}
	}

	public SocialModel[] getSocialModels() {
		return socialModels;
	}

	public void setSocialModels(SocialModel[] socialModels) {
		this.socialModels = socialModels;
	}

	public int getUserTweetCount() {
		return userTweetCount;
	}

	public void setUserTweetCount(int userTweetCount) {
		this.userTweetCount = userTweetCount;
	}

	public int getBookCountInBookshelf() {
		return bookCountInBookshelf;
	}

	public void setBookCountInBookshelf(int bookCountInBookshelf) {
		this.bookCountInBookshelf = bookCountInBookshelf;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}



	public Book[] getBooks() {
		return books;
	}

	public void setBooks(Book[] books) {
		this.books = books;
	}

	/**
	 * 解析Jsont字符串，并通知观察者
	 *
	 * @param jsonString
	 *            待解析的Json字符串
	 */
	private void parseJson(String jsonString) {
		boolean connected = UiStaticMethod.isNetWorkConnected(jsonString);
		boolean hasContent = false;
		if (connected) {
			try {
				JSONObject jsonObject = new JSONObject(jsonString);
				if (TweetModel.resourceNotFound(jsonObject, TweetModel.ERROR_NOT_FOUND)) {
					hasContent = false;
				} else {
					parseJson(jsonObject);
					hasContent = true;
				}
			} catch (JSONException e) {
				MZLog.e("UserInfo", Log.getStackTraceString(e));
			}
		}
		notifyDataChanged(UserFragment.INIT_LOAD, connected, hasContent);
	}

}
