package com.jingdong.app.reader.entity.extra;

import java.util.List;

public class OrderDetail {

	private String code;
	private List<OrderDetailList> resultList;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public List<OrderDetailList> getResultList() {
		return resultList;
	}
	public void setResultList(List<OrderDetailList> resultList) {
		this.resultList = resultList;
	}
}
