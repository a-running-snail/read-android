package com.jingdong.app.reader.entity.extra;

public class UsersList {

	public String id;
	public String name;
	public int role;
	public String avatar;
	public String jd_user_name;
	public Relation_with_current_user relation_with_current_user;
	public String summary;
	public String sex;
	public String cover;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getRole() {
		return role;
	}

	public void setRole(int role) {
		this.role = role;
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

	public Relation_with_current_user getRelation_with_current_user() {
		return relation_with_current_user;
	}

	public void setRelation_with_current_user(
			Relation_with_current_user relation_with_current_user) {
		this.relation_with_current_user = relation_with_current_user;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}
}
