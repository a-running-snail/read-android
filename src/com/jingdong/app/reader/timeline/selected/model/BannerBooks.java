package com.jingdong.app.reader.timeline.selected.model;

import org.json.JSONException;
import org.json.JSONObject;


public class BannerBooks {

	private int id;
	private int book_id;
	private String book_name;
	private String book_author_name;
	private String book_cover;
	private String download_link;
	private String label;

	public void loadBooks(String bookJson) {
		try {
			JSONObject object = new JSONObject(bookJson);
			setId(object.optInt("id"));
			setDownload_link(object.optString("download_link"));
			setLabel(object.optString("label"));
			JSONObject bkJsonObject = new JSONObject(object.optString("book"));
			setBook_id(bkJsonObject.optInt("id"));
			setBook_author_name(bkJsonObject.optString("author_name"));
			setBook_cover(bkJsonObject.optString("cover"));
			setBook_name(bkJsonObject.optString("name"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getBook_id() {
		return book_id;
	}

	public void setBook_id(int book_id) {
		this.book_id = book_id;
	}

	public String getBook_name() {
		return book_name;
	}

	public void setBook_name(String book_name) {
		this.book_name = book_name;
	}

	public String getBook_author_name() {
		return book_author_name;
	}

	public void setBook_author_name(String book_author_name) {
		this.book_author_name = book_author_name;
	}

	public String getBook_cover() {
		return book_cover;
	}

	public void setBook_cover(String book_cover) {
		this.book_cover = book_cover;
	}

	public String getDownload_link() {
		return download_link;
	}

	public void setDownload_link(String download_link) {
		this.download_link = download_link;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

}