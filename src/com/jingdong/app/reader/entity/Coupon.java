package com.jingdong.app.reader.entity;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONException;

import com.jingdong.app.reader.util.JSONArrayPoxy;
import com.jingdong.app.reader.util.JSONObjectProxy;
import com.jingdong.app.reader.util.MZLog;



/**
 * Copyright 2011 Jingdong Android Mobile Application
 * 
 * @author lijingzuo
 * 
 *         Time: 2011-1-14 上午10:16:54
 * 
 *         Name:
 * 
 *         Description: 赠券
 */
public class Coupon implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1173004860606670344L;

	public static final int PROMOTION = 0;

	public Coupon(JSONObjectProxy jsonObject, int functionId) {

		switch (functionId) {

		case PROMOTION:
			setBalance(jsonObject.getIntOrNull("balance"));
			setType(jsonObject.getIntOrNull("bankType"));
			setMessage(jsonObject.getStringOrNull("message"));
			break;

		default:
			break;

		}

	}

	public static ArrayList<Coupon> toList(JSONArrayPoxy jsonArray,
			int functionId) {

		if (null == jsonArray) {
			return null;
		}

		ArrayList<Coupon> list = null;

		try {

			list = new ArrayList<Coupon>();
			for (int i = 0; i < jsonArray.length(); i++) {
				Coupon ware = new Coupon(jsonArray.getJSONObject(i), functionId);
				list.add(ware);
			}

		} catch (JSONException e) {
				MZLog.d("Ware", e.getMessage());
		}

		return list;

	}

	private Integer balance;// 抵价
	private Integer type;// 类型
	private String message;// 消息

	public Integer getBalance() {
		return balance;
	}

	public void setBalance(Integer balance) {
		this.balance = balance;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
