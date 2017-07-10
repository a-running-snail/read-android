package com.jingdong.app.reader.entity.extra;

import java.util.List;

public class Monthly {

	private String code;
	private List<MonthlyList> serverAndCardDetailList;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public List<MonthlyList> getServerAndCardDetailList() {
		return serverAndCardDetailList;
	}
	public void setServerAndCardDetailList(List<MonthlyList> serverAndCardDetailList) {
		this.serverAndCardDetailList = serverAndCardDetailList;
	}

	
}
