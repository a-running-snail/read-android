package com.jingdong.app.reader.entity.extra;

import java.util.List;

import android.R.integer;

public class OrderEntity {

	private int amount;
	private String code;
	private int currentPage;
	private int pageSize;
	private int totalPage;
	private List<OrderList> resultList;
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
	public List<OrderList> getResultList() {
		return resultList;
	}
	public void setResultList(List<OrderList> resultList) {
		this.resultList = resultList;
	}
}
