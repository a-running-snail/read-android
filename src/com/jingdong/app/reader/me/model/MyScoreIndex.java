package com.jingdong.app.reader.me.model;

import java.util.List;

public class MyScoreIndex {

	private String code;
	private boolean isSign;
	private int scoreTotal;
	private boolean isLottery;
	private List<ScoreGift> ScoreGifts;
	private SignSuccessionResult signSuccessionResult;
	
	public boolean isLottery() {
		return isLottery;
	}
	public void setLottery(boolean isLottery) {
		this.isLottery = isLottery;
	}
	public int getScoreTotal() {
		return scoreTotal;
	}
	public void setScoreTotal(int scoreTotal) {
		this.scoreTotal = scoreTotal;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public boolean isSign() {
		return isSign;
	}
	public void setSign(boolean isSign) {
		this.isSign = isSign;
	}
	public List<ScoreGift> getScoreGifts() {
		return ScoreGifts;
	}
	public void setScoreGifts(List<ScoreGift> scoreGifts) {
		ScoreGifts = scoreGifts;
	}
	
	public SignSuccessionResult getSignSuccessionResult() {
		return signSuccessionResult;
	}
	public void setSignSuccessionResult(SignSuccessionResult signSuccessionResult) {
		this.signSuccessionResult = signSuccessionResult;
	}
	
	
	
}

