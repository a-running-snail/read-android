package com.jingdong.app.reader.me.model;

import java.util.List;

public class ScoreRecord {

	private int amount;
	private String code;
	private int currentPage;
	private int pageSize;
	private int totalPage;
	private List<ScoreRecordModel> resultList;
	
 	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public int getCurrentPage() {
		return currentPage;
	}
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	public List<ScoreRecordModel> getResultList() {
		return resultList;
	}
	public void setResultList(List<ScoreRecordModel> resultList) {
		this.resultList = resultList;
	}
	
	
}
