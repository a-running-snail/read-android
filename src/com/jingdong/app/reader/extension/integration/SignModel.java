package com.jingdong.app.reader.extension.integration;


public class SignModel{
	String date;
	boolean isSigned = false;
	boolean isShown3min = false;
	String userpin;
	
	
	public SignModel(String date, boolean isSigned, boolean isShown3min,String userpin) {
		super();
		this.date = date;
		this.isSigned = isSigned;
		this.isShown3min = isShown3min;
		this.userpin = userpin;
	}
	
	public String getUserpin() {
		return userpin;
	}
	public void setUserpin(String userpin) {
		this.userpin = userpin;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public boolean isSigned() {
		return isSigned;
	}
	public void setSigned(boolean isSigned) {
		this.isSigned = isSigned;
	}
	public boolean isShown3min() {
		return isShown3min;
	}
	public void setShown3min(boolean isShown3min) {
		this.isShown3min = isShown3min;
	}
	
	
}