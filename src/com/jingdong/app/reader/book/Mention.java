package com.jingdong.app.reader.book;



public class Mention {
	
	long  _id;
	long  mentionBookId;
	String 	 mentionBookName;
	long mentionAt;
	String author;
	String bookCover;
	
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public long getMentionBookId() {
		return mentionBookId;
	}
	public void setMentionBookId(long mentionBookId) {
		this.mentionBookId = mentionBookId;
	}
	public String getMentionBookName() {
		return mentionBookName;
	}
	public void setMentionBookName(String mentionBookName) {
		this.mentionBookName = mentionBookName;
	}
	public long getMentionAt() {
		return mentionAt;
	}
	public void setMentionAt(long mentionAt) {
		this.mentionAt = mentionAt;
	}
	
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getBookCover() {
		return bookCover;
	}
	public void setBookCover(String bookCover) {
		this.bookCover = bookCover;
	}
	

}
