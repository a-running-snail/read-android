package com.jingdong.app.reader.entity.extra;

public class UpdateInfo {

	private String code;
	private boolean isLatest;
	private Update result;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public boolean isLatest() {
		return isLatest;
	}

	public void setLatest(boolean isLatest) {
		this.isLatest = isLatest;
	}

	public Update getResult() {
		return result;
	}

	public void setResult(Update result) {
		this.result = result;
	}
}
