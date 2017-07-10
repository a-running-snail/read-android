package com.jingdong.app.reader.entity1.bookinfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import com.jingdong.app.reader.net.URLText;
import com.jingdong.app.reader.user.UserInfo;

public class Book {
	String id;
	String name;
	String author_name;
	String translator_name;
	String pages;
	String price;
	String publisher_name;
	String publish_time;
	String cover;
	String rating;
	String isbn;
	String media;
	String summary;
	String authorSummary;
	String subName;
	boolean borrow;
	Ebook ebook;
	List<UserInfo> withAuthor = new ArrayList<UserInfo>();
	List<Publisher> withPublishers = new ArrayList<Publisher>();
	

	public void fromJson(JSONObject object) throws JSONException {

		setId(object.optString("id"));
		setName(object.optString("name"));
		setAuthor_name(object.optString("author_name"));
		setTranslator_name(object.optString("translator_name"));
		setPages(object.optString("pages"));
		setPrice(object.optString("price"));
		setPublisher_name(object.optString("publisher_name"));
		setPublish_time(object.optString("publish_time"));
		setCover(object.optString("cover"));
		setRating(object.optString("rating"));
		setIsbn(object.optString("isbn"));
		setMedia(object.optString("media"));
		setSummary(object.optString("summary"));
		setAuthorSummary(object.optString("author_summary"));
		setBorrow(object.optBoolean("borrow"));
		setSubName(object.optString("sub_name"));
		Ebook tempEbook=new Ebook();
		tempEbook.fromJson(object.optJSONObject("ebook"));
		setEbook(tempEbook);

		JSONArray withauthorarr = object.optJSONArray("with_author");
		List<UserInfo> tempWithAuthor = new ArrayList<UserInfo>();
		if (withauthorarr != null) {
			for (int i = 0; i < withauthorarr.length(); i++) {
				tempWithAuthor.add(UserInfo.fromJSON(withauthorarr
						.getJSONObject(i).getJSONObject("user")));
			}
		}
		setWithAuthor(tempWithAuthor);
		JSONArray withPublishersArr = object.optJSONArray("with_ebook_tags");
		List<Publisher> tempWithPublishers = new ArrayList<Publisher>();
		if (withPublishersArr != null) {

			for (int i = 0; i < withPublishersArr.length(); i++) {

				Publisher publisher = new Publisher();
				publisher.fromJson(withPublishersArr.getJSONObject(i));
				tempWithPublishers.add(publisher);

			}

		}
		setWithPublishers(tempWithPublishers);

	}
	public String getShareURL() {
		StringBuilder builder = new StringBuilder();
		builder.append(URLText.shareBookUrl);
		builder.append(getId());
		return builder.toString();
	}

	public Ebook getEbook() {
		return ebook;
	}


	public String getSubName() {
		return subName;
	}
	public void setSubName(String subName) {
		this.subName = subName;
	}
	public String getAuthorSummary() {
		return authorSummary;
	}
	public void setAuthorSummary(String authorSummary) {
		this.authorSummary = authorSummary;
	}
	public void setEbook(Ebook ebook) {
		this.ebook = ebook;
	}


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

	public String getAuthor_name() {
		return author_name;
	}

	public void setAuthor_name(String author_name) {
		this.author_name = author_name;
	}

	public String getTranslator_name() {
		return translator_name;
	}

	public void setTranslator_name(String translator_name) {
		this.translator_name = translator_name;
	}

	public String getPages() {
		return pages;
	}

	public void setPages(String pages) {
		this.pages = pages;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getPublisher_name() {
		return publisher_name;
	}

	public void setPublisher_name(String publisher_name) {
		this.publisher_name = publisher_name;
	}

	public String getPublish_time() {
		return publish_time;
	}

	public void setPublish_time(String publish_time) {
		this.publish_time = publish_time;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public String getMedia() {
		return media;
	}

	public void setMedia(String media) {
		this.media = media;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public boolean isBorrow() {
		return borrow;
	}

	public void setBorrow(boolean borrow) {
		this.borrow = borrow;
	}

	public List<UserInfo> getWithAuthor() {
		return withAuthor;
	}

	public void setWithAuthor(List<UserInfo> withAuthor) {
		this.withAuthor = withAuthor;
	}

	public List<Publisher> getWithPublishers() {
		return withPublishers;
	}

	public void setWithPublishers(List<Publisher> withPublishers) {
		this.withPublishers = withPublishers;
	}
	
	

}

