package com.jingdong.app.reader.bookstore;

import org.json.JSONException;
import org.json.JSONObject;

public class StoreBook {
	int id;
	String name;
	String author_name;
	String translator_name;
	String cover;
	String sales_tag;
	String price;
	String web_price;
	String origin_price;
	String description;

	public void fromJson(String json) {
		try {
			JSONObject object = new JSONObject(json);

			setId(object.getInt("id"));
			setName(object.getString("name"));
			setAuthor_name(object.getString("author_name"));
			setTranslator_name(object.optString("translator_name"));
			setCover(object.getString("cover"));
			setSales_tag(object.optString("sales_tag"));
			setPrice(object.optString("price"));
			setWeb_price(object.optString("web_price"));
			setOrigin_price(object.optString("origin_price"));
			setDescription(object.optString("description"));

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

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public String getSales_tag() {
		return sales_tag;
	}

	public void setSales_tag(String sales_tag) {
		this.sales_tag = sales_tag;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getWeb_price() {
		return web_price;
	}

	public void setWeb_price(String web_price) {
		this.web_price = web_price;
	}

	public String getTranslator_name() {
		return translator_name;
	}

	public void setTranslator_name(String translator_name) {
		this.translator_name = translator_name;
	}

	public String getOrigin_price() {
		return origin_price;
	}

	public void setOrigin_price(String origin_price) {
		this.origin_price = origin_price;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
