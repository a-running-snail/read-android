package com.jingdong.app.reader.entity;

import com.jingdong.app.reader.util.JSONObjectProxy;



public class WeiXinEntity {

	/**
	 * 唯一标识，应用的id，在微信开放平台注册时获得
	 */
	private String appId = "wx79f9198071040f23";

	/**
	 * 商家向财付通申请的商家id
	 */
	private String partnerId;

	/**
	 * 预支付订单
	 */
	private String prepayId;

	/**
	 * 随机串，防重发
	 */
	private String nonceStr;

	/**
	 * 时间戳，防重发
	 */
	private String timeStamp;

	/**
	 * 商家根据财付通文档填写的数据和签名
	 */
	private String packageValue;

	/**
	 * 商家根据微信开放平台文档对数据做的签名
	 */
	private String sign;

	public WeiXinEntity() {

	}

	public WeiXinEntity(JSONObjectProxy jsonObject) {

		if (jsonObject == null) {
			return;
		}

		setPartnerId(jsonObject.getStringOrNull("partnerId"));
		setPrepayId(jsonObject.getStringOrNull("prepayId"));
		setNonceStr(jsonObject.getStringOrNull("nonceStr"));
		setTimeStamp(jsonObject.getStringOrNull("timeStamp"));
		setPackageValue(jsonObject.getStringOrNull("package"));
		setSign(jsonObject.getStringOrNull("sign"));
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(String partnerId) {
		this.partnerId = partnerId;
	}

	public String getPrepayId() {
		return prepayId;
	}

	public void setPrepayId(String prepayId) {
		this.prepayId = prepayId;
	}

	public String getNonceStr() {
		return nonceStr;
	}

	public void setNonceStr(String nonceStr) {
		this.nonceStr = nonceStr;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getPackageValue() {
		return packageValue;
	}

	public void setPackageValue(String packageValue) {
		this.packageValue = packageValue;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

}
