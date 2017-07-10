package com.jingdong.app.reader.timeline.selected.model;



import org.json.JSONException;
import org.json.JSONObject;

class User {
	private int id;
	private String name;
	private String avatar;
	private int sex;
	private int role;
	private boolean locked;
	private String blog_address;
	private String contact_email;
	private String vname;
	private boolean register_from_third_party;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public String getBlog_address() {
		return blog_address;
	}

	public void setBlog_address(String blog_address) {
		this.blog_address = blog_address;
	}

	public String getContact_email() {
		return contact_email;
	}

	public void setContact_email(String contact_email) {
		this.contact_email = contact_email;
	}

	public String getVname() {
		return vname;
	}

	public void setVname(String vname) {
		this.vname = vname;
	}

	public boolean isRegister_from_third_party() {
		return register_from_third_party;
	}

	public void setRegister_from_third_party(boolean register_from_third_party) {
		this.register_from_third_party = register_from_third_party;
	}

}

public class AttachedEntityModel {

	private String title;
	private String guid;
	private String book_list_id;
	private User user;

	public void loadAttachedEntity(String attachedEntityJson) {

		try {
			JSONObject object = new JSONObject(attachedEntityJson);
			setTitle(object.optString("title"));
			setGuid(object.optString("guid"));
			setBook_list_id(object.optString("book_list_id"));

			User user = new User();
			JSONObject userObject = new JSONObject(object.optString("user"));
			user.setId(userObject.optInt("id"));
			user.setName(userObject.optString("name"));
			user.setAvatar(userObject.optString("avatar"));
			user.setSex(userObject.optInt("sex"));
			user.setRole(userObject.optInt("role"));
			user.setLocked(userObject.optBoolean("locked"));
			user.setBlog_address(userObject.optString("blog_address"));
			user.setContact_email(userObject.optString("contact_email"));
			user.setVname(userObject.optString("vname"));
			user.setRegister_from_third_party(userObject
					.optBoolean("register_from_third_party"));
			setUser(user);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public String getBook_list_id() {
		return book_list_id;
	}

	public void setBook_list_id(String book_list_id) {
		this.book_list_id = book_list_id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
