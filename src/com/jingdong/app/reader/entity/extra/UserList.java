package com.jingdong.app.reader.entity.extra;

public class UserList {

	private String email;//邮箱
	private String nickName;//昵称
	private String pin;//用户pin
	private String regTime;//注册时间
	private String uremark;//个人说明
	private String usex;//性别（0男，1女，2未知）
	private String yunBigImageUrl;
	private String yunMidImageUrl;
	private String yunSmaImageUrl;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getNickName() {
		return nickName;
	}
	public void setNickName(String nickName) {
		this.nickName = nickName;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
	public String getRegTime() {
		return regTime;
	}
	public void setRegTime(String regTime) {
		this.regTime = regTime;
	}
	public String getUremark() {
		return uremark;
	}
	public void setUremark(String uremark) {
		this.uremark = uremark;
	}
	public String getUsex() {
		return usex;
	}
	public void setUsex(String usex) {
		this.usex = usex;
	}
	public String getYunBigImageUrl() {
		return yunBigImageUrl;
	}
	public void setYunBigImageUrl(String yunBigImageUrl) {
		this.yunBigImageUrl = yunBigImageUrl;
	}
	public String getYunMidImageUrl() {
		return yunMidImageUrl;
	}
	public void setYunMidImageUrl(String yunMidImageUrl) {
		this.yunMidImageUrl = yunMidImageUrl;
	}
	public String getYunSmaImageUrl() {
		return yunSmaImageUrl;
	}
	public void setYunSmaImageUrl(String yunSmaImageUrl) {
		this.yunSmaImageUrl = yunSmaImageUrl;
	}
}
