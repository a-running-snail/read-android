package com.jingdong.app.reader.timeline.selected.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BannersModel {

	private int id;
	private String name;
	private String target_type;

	private String target_id;
	private String image;
	private String banner_title;
	private String banner_desc;
	private String download_link;
	private List<BannerBooks> books;
	private List<AttachedEntityModel> attached_entity;

	/*
	 * @author:wangguodong
	 * @info 精选界面的json数据解析model
	 */

	public void loadBannersModel(String json) {// just one banner

		try {
			JSONObject object = new JSONObject(json);
			setId(object.optInt("id"));
			setName(object.optString("name"));
			setTarget_type(object.optString("target_type"));
			setTarget_id(object.optString("target_id"));
			setImage(object.optString("image"));
			setBanner_title(object.optString("banner_title"));
			setBanner_desc(object.optString("banner_desc"));
			setDownload_link(object.optString("download_link"));

			JSONArray booksArray = object.getJSONArray("books");

			List<BannerBooks> list = new ArrayList<BannerBooks>();
			for (int i = 0; i < booksArray.length(); i++) {
				BannerBooks book = new BannerBooks();
				book.loadBooks(booksArray.getString(i));
				list.add(book);
			}
			setBooks(list);

			JSONArray attachedArray = object.getJSONArray("attached_entity");

			List<AttachedEntityModel> aList = new ArrayList<AttachedEntityModel>();

			for (int i = 0; i < attachedArray.length(); i++) {
				AttachedEntityModel model = new AttachedEntityModel();
				model.loadAttachedEntity(attachedArray.getString(i));
				aList.add(model);
			}
			setAttached_entity(aList);

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

	public String getTarget_type() {
		return target_type;
	}

	public void setTarget_type(String target_type) {
		this.target_type = target_type;
	}

	public String getTarget_id() {
		return target_id;
	}

	public void setTarget_id(String target_id) {
		this.target_id = target_id;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getBanner_title() {
		return banner_title;
	}

	public void setBanner_title(String banner_title) {
		this.banner_title = banner_title;
	}

	public String getBanner_desc() {
		return banner_desc;
	}

	public void setBanner_desc(String banner_desc) {
		this.banner_desc = banner_desc;
	}

	public String getDownload_link() {
		return download_link;
	}

	public void setDownload_link(String download_link) {
		this.download_link = download_link;
	}

	public List<BannerBooks> getBooks() {
		return books;
	}

	public void setBooks(List<BannerBooks> books) {
		this.books = books;
	}

	public List<AttachedEntityModel> getAttached_entity() {
		return attached_entity;
	}

	public void setAttached_entity(List<AttachedEntityModel> attached_entity) {
		this.attached_entity = attached_entity;
	}

}
