package com.jingdong.app.reader.entity;

import java.util.ArrayList;

import org.json.JSONException;

import com.jingdong.app.reader.util.JSONArrayPoxy;
import com.jingdong.app.reader.util.JSONObjectProxy;


public class CityMode1 {

	public static final int DIRECT_WARE = 0;

	private String name;
	private int id;
	private long productId;
	private ProvinceMode1 parent;

	private CityMode1(JSONObjectProxy jsonObject, int functionId, Object[] varargs) {

		switch (functionId) {

		case DIRECT_WARE:

			ProvinceMode1 parent = null;
			if (null != varargs && varargs.length > 0 && null != varargs[0] && varargs[0] instanceof ProvinceMode1) {
				parent = (ProvinceMode1) varargs[0];
			}

			// 辅助以sku id确定city id和province id
			Product product = null;
			if (null != varargs && varargs.length > 1 && null != varargs[1] && varargs[1] instanceof Product) {
				product = (Product) varargs[1];
			}

			setName(jsonObject.getStringOrNull("name"));
			setId(jsonObject.getIntOrNull("idCity"));
			setProductId(jsonObject.getLongOrNull("skuid"));
			setParent(parent);

			// 辅助以sku id确定city id和province id
			if (null != product) {
				product.putInCityMode1Map(getProductId(), this);
			}

			break;
		default:
			break;

		}

	}

	public static ArrayList<CityMode1> toList(JSONArrayPoxy jsonArray, int functionId) {
		return toList(jsonArray, functionId, null);
	}

	public static ArrayList<CityMode1> toList(JSONArrayPoxy jsonArray, int functionId, Object[] varargs) {

		if (null == jsonArray) {
			return null;
		}

		ArrayList<CityMode1> list = null;

		try {

			list = new ArrayList<CityMode1>();
			for (int i = 0; i < jsonArray.length(); i++) {
				CityMode1 city = new CityMode1(jsonArray.getJSONObject(i), functionId, varargs);
				list.add(city);
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

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public ProvinceMode1 getParent() {
		return parent;
	}

	public void setParent(ProvinceMode1 parent) {
		this.parent = parent;
	}

}
