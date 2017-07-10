package com.jingdong.app.reader.entity.extra;

import java.util.List;

import com.jingdong.app.reader.entity.ShowOrderEntity.bookList;

public class JDBook {

	public String code;
	public int currentPage;
	public boolean isSuccess;
	public int pageSize;
	public int resultCount;
	public int totalPage;
	public List<JDBookDetail> bookList;

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

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getResultCount() {
		return resultCount;
	}

	public void setResultCount(int resultCount) {
		this.resultCount = resultCount;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public List<JDBookDetail> getBookList() {
		return bookList;
	}

	public void setBookList(List<JDBookDetail> bookList) {
		this.bookList = bookList;
	}
}
