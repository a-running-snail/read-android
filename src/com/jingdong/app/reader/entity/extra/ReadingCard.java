package com.jingdong.app.reader.entity.extra;

import java.util.List;

public class ReadingCard {

	private String code;
	private String sysDate;
	private List<ReadCardInfo> readCardList;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getSysDate() {
		return sysDate;
	}

	public void setSysDate(String sysDate) {
		this.sysDate = sysDate;
	}

	public List<ReadCardInfo> getReadCardList() {
		return readCardList;
	}

	public void setReadCardList(List<ReadCardInfo> readCardList) {
		this.readCardList = readCardList;
	}

	
}
