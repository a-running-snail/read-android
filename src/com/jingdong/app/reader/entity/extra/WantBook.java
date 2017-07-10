package com.jingdong.app.reader.entity.extra;

import java.util.List;

public class WantBook {

	private int count;
	private List<WantBookList> books;
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public List<WantBookList> getBooks() {
		return books;
	}
	public void setBooks(List<WantBookList> books) {
		this.books = books;
	}
}
