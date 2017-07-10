package com.jingdong.app.reader.entity.extra;

public class Relation_with_current_user {

	private boolean following;
	private boolean followed;

	public boolean isFollowing() {
		return following;
	}

	public void setFollowing(boolean following) {
		this.following = following;
	}

	public boolean isFollowed() {
		return followed;
	}

	public void setFollowed(boolean followed) {
		this.followed = followed;
	}
}
