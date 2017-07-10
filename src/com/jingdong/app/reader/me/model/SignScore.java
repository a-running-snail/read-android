package com.jingdong.app.reader.me.model;

public class SignScore {

	private String code;
	private int getScore;
	private SignSuccessionResult signSuccessionResult;
	private SignTypeData data;
	
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getGetScore() {
		return getScore;
	}
	public void setGetScore(int getScore) {
		this.getScore = getScore;
	}
	public SignSuccessionResult getSignSuccessionResult() {
		return signSuccessionResult;
	}
	public void setSignSuccessionResult(SignSuccessionResult signSuccessionResult) {
		this.signSuccessionResult = signSuccessionResult;
	}
	public void setSignTypeData(SignTypeData signTypeData) {
		this.data = signTypeData;
	}
	public SignTypeData getSignTypeData() {
		return this.data;
	}
	
}

