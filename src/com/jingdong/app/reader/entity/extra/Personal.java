package com.jingdong.app.reader.entity.extra;

import com.jingdong.app.reader.user.UserInfo;

public class Personal {

	private UserInfo user;
	private boolean current_user_is_followed_by;
	private boolean current_user_is_following;
	private int following_users_count;
	private int books_count_in_book_shelf;
	private int follower_users_count;
	private int notes_entities_count;
	private int read_books_count;
	private int wish_books_count;
	private int entity_count;
	private int user_tweet_count;
	private int book_comment_count;
	private int book_count;
	private int document_count;
	private int favourite_count;

	public boolean isCurrent_user_is_followed_by() {
		return current_user_is_followed_by;
	}

	public void setCurrent_user_is_followed_by(
			boolean current_user_is_followed_by) {
		this.current_user_is_followed_by = current_user_is_followed_by;
	}

	public boolean isCurrent_user_is_following() {
		return current_user_is_following;
	}

	public void setCurrent_user_is_following(boolean current_user_is_following) {
		this.current_user_is_following = current_user_is_following;
	}

	public int getFollowing_users_count() {
		return following_users_count;
	}

	public void setFollowing_users_count(int following_users_count) {
		this.following_users_count = following_users_count;
	}

	public int getBooks_count_in_book_shelf() {
		return books_count_in_book_shelf;
	}

	public void setBooks_count_in_book_shelf(int books_count_in_book_shelf) {
		this.books_count_in_book_shelf = books_count_in_book_shelf;
	}

	public int getFollower_users_count() {
		return follower_users_count;
	}

	public void setFollower_users_count(int follower_users_count) {
		this.follower_users_count = follower_users_count;
	}

	public int getNotes_entities_count() {
		return notes_entities_count;
	}

	public void setNotes_entities_count(int notes_entities_count) {
		this.notes_entities_count = notes_entities_count;
	}

	public int getRead_books_count() {
		return read_books_count;
	}

	public void setRead_books_count(int read_books_count) {
		this.read_books_count = read_books_count;
	}

	public int getWish_books_count() {
		return wish_books_count;
	}

	public void setWish_books_count(int wish_books_count) {
		this.wish_books_count = wish_books_count;
	}

	public int getEntity_count() {
		return entity_count;
	}

	public void setEntity_count(int entity_count) {
		this.entity_count = entity_count;
	}

	public int getUser_tweet_count() {
		return user_tweet_count;
	}

	public void setUser_tweet_count(int user_tweet_count) {
		this.user_tweet_count = user_tweet_count;
	}

	public int getBook_comment_count() {
		return book_comment_count;
	}

	public void setBook_comment_count(int book_comment_count) {
		this.book_comment_count = book_comment_count;
	}

	public int getBook_count() {
		return book_count;
	}

	public void setBook_count(int book_count) {
		this.book_count = book_count;
	}

	public int getDocument_count() {
		return document_count;
	}

	public void setDocument_count(int document_count) {
		this.document_count = document_count;
	}

	public int getFavourite_count() {
		return favourite_count;
	}

	public void setFavourite_count(int favourite_count) {
		this.favourite_count = favourite_count;
	}

	public UserInfo getUser() {
		return user;
	}

	public void setUser(UserInfo user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Personal [user=" + user + ", current_user_is_followed_by="
				+ current_user_is_followed_by + ", current_user_is_following="
				+ current_user_is_following + ", following_users_count="
				+ following_users_count + ", books_count_in_book_shelf="
				+ books_count_in_book_shelf + ", follower_users_count="
				+ follower_users_count + ", notes_entities_count="
				+ notes_entities_count + ", read_books_count="
				+ read_books_count + ", wish_books_count=" + wish_books_count
				+ ", entity_count=" + entity_count + ", user_tweet_count="
				+ user_tweet_count + ", book_comment_count="
				+ book_comment_count + ", book_count=" + book_count
				+ ", document_count=" + document_count + ", favourite_count="
				+ favourite_count + "]";
	}
	
	
}
