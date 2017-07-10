package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;

public class ReadedUserInfo implements Serializable{

	private String email;
	private String nickName;
	private String pin;
	private String regTime;
	private String usex;
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
