package com.jingdong.app.reader.entity;

import java.io.Serializable;
import java.util.LinkedList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jingdong.app.reader.util.MZLog;


public class Image implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8044539732929710369L;

	public static final int PRODUCTDETAIL = 0;

	public Image(String small, String big) {
		this.small = small;
		this.big = big;
	}

	public Image(JSONObject jsonObject, int functionId) {

		try {

			switch (functionId) {

			case PRODUCTDETAIL:
				setSmall(jsonObject.getString("newpath"));
				setBig(jsonObject.getString("bigpath"));
				break;

			default:
				break;

			}

		} catch (JSONException e) {
				MZLog.d(Image.class.getName(), e.getMessage());
		}

	}

	public static LinkedList<Image> toList(JSONArray jsonArray, int functionId) {

		if (null == jsonArray) {
			return null;
		}

		LinkedList<Image> list = null;

		try {

			list = new LinkedList<Image>();
			for (int i = 0; i < jsonArray.length(); i++) {
				Image image = new Image(jsonArray.getJSONObject(i), functionId);
				list.add(image);
			}

		} catch (JSONException e) {
			MZLog.d(Image.class.getName(), e.getMessage());
		}

		return list;
	}

	private String small;
	private String big;

	public String getSmall() {
		return small;
	}

	public void setSmall(String small) {
		this.small = small;
	}

	public String getBig() {
		return big;
	}

	public void setBig(String big) {
		this.big = big;
	}

}
