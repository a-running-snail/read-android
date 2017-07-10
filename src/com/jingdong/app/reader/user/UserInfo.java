package com.jingdong.app.reader.user;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.jingdong.app.reader.entity.extra.Relation_with_current_user;
import com.jingdong.app.reader.timeline.model.ObservableModel;

public class UserInfo extends ObservableModel implements Parcelable {

	public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {

		@Override
		public UserInfo createFromParcel(Parcel source) {
			return new UserInfo(source);
		}

		@Override
		public UserInfo[] newArray(int size) {
			return new UserInfo[size];
		}
	};
	public final static String RECOMMENT_TEXT = "recommend_text";
	public final static int MALE = 0;
	public final static int FEMALE = 1;
	public static final String ID = "id";
	private static final String USER_NAME = "name";
	private static final String USER_SEX = "sex";
	private static final String USER_ROLE = "role";
	private static final String THUMB_NAIL = "avatar";
	private static final String SUMMARY = "summary";
	private static final String CONTACT_EMAIL = "contact_email";
	private static final String BLOG_ADDRESS = "blog_address";
	private static final String VNAME = "vname";
	private static final String REGISTER_FROM_THIRD_PARTY = "register_from_third_party";
	private String contactEmail;
	private String blog;
	private String summary;
	private String vname;
	private String recommentText;
	private boolean registerFromThirdParty;
	public Relation_with_current_user relation_with_current_user;


	String id = "";
	String name = "";
	String avatar= "";
	String jd_user_name="";
	String thumbNail = "";
	String cover = "";
	boolean sex;
	int role;

	protected UserInfo(Parcel source) {
		contactEmail = source.readString();
		blog = source.readString();
		summary = source.readString();
		vname = source.readString();
		recommentText = source.readString();
		name = source.readString();
		jd_user_name= source.readString();
		thumbNail = source.readString();
		
		id = source.readString();
		role = source.readInt();
		sex = (source.readByte() == 0) ? false : true;
		registerFromThirdParty = (source.readByte() == 0) ? false : true;
	}

	public UserInfo() {

	}

	public static UserInfo fromJSON(JSONObject json) throws JSONException {
		UserInfo user = new UserInfo();
		user.parseJson(json);
		return user;
	}

	protected UserInfo(String jsonText) {
		try {
			JSONObject o = new JSONObject(jsonText);
			parseJson(o);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}
	public String getUserPin() {
		return jd_user_name;
	}

	public String getThumbNail() {
		return thumbNail;
	}

	public boolean isFemale() {
		return sex;
	}

	public boolean isRegisterFromThirdParty() {
		return registerFromThirdParty;
	}

	public int getRole() {
		return role;
	}

	public String getContactEmail() {
		return contactEmail;
	}

	public String getBlog() {
		return blog;
	}

	public String getSummary() {
		return summary;
	}

	public String getVname() {
		return vname;
	}

	public String getRecommentText() {
		return recommentText;
	}

	/**
	 * 从JSONObject中取得用户数据，并将结果保存到User中
	 * 
	 * @param jObject
	 *            数据源
	 * @return 解析的结果，一个User对象
	 */
	public void parseJson(JSONObject jObject) throws JSONException {
		if (jObject != null) {
			id = jObject.optString(ID);
			name = jObject.optString(USER_NAME);
			jd_user_name=jObject.optString("jd_user_name");
			thumbNail = jObject.optString(THUMB_NAIL);
			role = jObject.optInt(USER_ROLE);
			avatar=jObject.optString("avatar");
			if (jObject.optString(USER_SEX).equals("")) {
				sex = true;
			}else {
				sex = Integer.parseInt(jObject.optString(USER_SEX)) == 0 ? true : false;
			}
			
			setSex(sex);
			setRegisterFromThirdParty(jObject.optBoolean(REGISTER_FROM_THIRD_PARTY));
			setSummary(jObject.optString(SUMMARY));
			setBlog(jObject.optString(BLOG_ADDRESS));
			setContactEmail(jObject.optString(CONTACT_EMAIL));
			setVname(jObject.optString(VNAME));
			setRecommentText(jObject.optString(RECOMMENT_TEXT));
			
			try {
				relation_with_current_user=new Relation_with_current_user();
				setRelation_with_current_user(jObject.optJSONObject("relation_with_current_user"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void setRelation_with_current_user(JSONObject jsonObject){
		if(jsonObject!=null){
			relation_with_current_user.setFollowed(jsonObject.optBoolean("followed"));
			relation_with_current_user.setFollowing(jsonObject.optBoolean("following"));
		}
	}
	
	public Relation_with_current_user getRelation_with_current_user() {
		return relation_with_current_user;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setThumbNail(String thumbNail) {
		this.thumbNail = thumbNail;
	}

	public void setSex(boolean female) {
		this.sex = female;
	}

	public void setContactEmail(String email) {
		this.contactEmail = email;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	void setVname(String vname) {
		this.vname = vname;
	}

	void setRecommentText(String recommentText) {
		this.recommentText = recommentText;
	}

	void setBlog(String blog) {
		this.blog = blog;
	}

	void setRole(int role) {
		this.role = role;
	}

	void setRegisterFromThirdParty(boolean registerFromThirdParty) {
		this.registerFromThirdParty = registerFromThirdParty;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof UserInfo && ((UserInfo) o).getId() == id;
	}



	@Override
	public String toString() {
		return "UserInfo [id=" + id + ", name=" + name + ", avatar=" + avatar
				+ ", jd_user_name=" + jd_user_name + ", thumbNail=" + thumbNail
				+ ", cover=" + cover + ", sex=" + sex + ", role=" + role + "]";
	}

	public static boolean convertSex(int sex) {
		if (sex == FEMALE)
			return true;
		else
			return false;
	}

	public static int convertSex(boolean female) {
		if (female)
			return FEMALE;
		else
			return MALE;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(contactEmail);
		dest.writeString(blog);
		dest.writeString(summary);
		dest.writeString(vname);
		dest.writeString(recommentText);
		dest.writeString(name);
		dest.writeString(jd_user_name);
		dest.writeString(thumbNail);
		dest.writeString(id);
		dest.writeInt(role);
		dest.writeByte((byte) (sex ? 1 : 0));
		dest.writeByte((byte) (registerFromThirdParty ? 1 : 0));
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getJd_user_name() {
		return jd_user_name;
	}

	public void setJd_user_name(String jd_user_name) {
		this.jd_user_name = jd_user_name;
	}

	public static Creator<UserInfo> getCreator() {
		return CREATOR;
	}

	public static int getMale() {
		return MALE;
	}

	public static int getFemale() {
		return FEMALE;
	}

	public static String getUserName() {
		return USER_NAME;
	}

	public static String getUserSex() {
		return USER_SEX;
	}

	public static String getUserRole() {
		return USER_ROLE;
	}

	public static String getBlogAddress() {
		return BLOG_ADDRESS;
	}

	public static String getRegisterFromThirdParty() {
		return REGISTER_FROM_THIRD_PARTY;
	}

	public boolean isSex() {
		return sex;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String coverString) {
		this.cover = cover;
	}
}
