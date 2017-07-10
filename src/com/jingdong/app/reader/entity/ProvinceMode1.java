package com.jingdong.app.reader.entity;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;

import com.jingdong.app.reader.util.JSONArrayPoxy;
import com.jingdong.app.reader.util.JSONObjectProxy;


public class ProvinceMode1 {

	public static final int DIRECT_WARE = 0;

	private String name;
	private int id;
	private ArrayList<CityMode1> children;
	private HashMap<Integer, Integer> childrenMap;// 辅助以ID确定index

	private ProvinceMode1(JSONObjectProxy jsonObject, int functionId, Object[] varargs) {

		switch (functionId) {

		case DIRECT_WARE:

			Product product = null;
			if (null != varargs && varargs.length > 0 && null != varargs[0] && varargs[0] instanceof Product) {
				product = (Product) varargs[0];
			}

			setName(jsonObject.getStringOrNull("name"));
			setId(jsonObject.getIntOrNull("idProvince"));
			setChildren(CityMode1.toList(jsonObject.getJSONArrayOrNull("idCityes"), functionId, new Object[] { this, product }));

			break;
		default:
			break;

		}

	}

	public static ArrayList<ProvinceMode1> toList(JSONArrayPoxy jsonArray, int functionId) {
		return toList(jsonArray, functionId, null);
	}

	public static ArrayList<ProvinceMode1> toList(JSONArrayPoxy jsonArray, int functionId, Object[] varargs) {

		if (null == jsonArray) {
			return null;
		}

		ArrayList<ProvinceMode1> list = null;

		try {

			list = new ArrayList<ProvinceMode1>();
			for (int i = 0; i < jsonArray.length(); i++) {
				ProvinceMode1 province = new ProvinceMode1(jsonArray.getJSONObject(i), functionId, varargs);
				list.add(province);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return list;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public ArrayList<CityMode1> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<CityMode1> children) {
		this.children = children;
		// 使用HashMap，方便以ID确定index
		childrenMap = new HashMap<Integer, Integer>();
		for (int i = 0; i < children.size(); i++) {
			CityMode1 city = children.get(i);
			childrenMap.put(city.getId(), i);
		}
	}

	/**
	 * 辅助以ID确定index
	 */
	public Integer getCityMode1IndexById(int id) {
		return childrenMap.get(id);
	}

}
