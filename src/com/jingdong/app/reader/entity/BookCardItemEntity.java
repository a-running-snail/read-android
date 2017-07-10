package com.jingdong.app.reader.entity;

import java.util.List;

public class BookCardItemEntity {

	private double cashback;
	private String imageDomain;
	private String newImageDomain;
	private double originalPrice;
	private boolean success;
	private List<SuitEntity> suitEntityList;
	private List<ProductEntity> signalProductList;
	private double totalCostcontent;
	private double totalCostcontent2;
	private int totalNum;
	
	
	
	
	public List<ProductEntity> getSignalProductList() {
		return signalProductList;
	}
	public void setSignalProductList(List<ProductEntity> signalProductList) {
		this.signalProductList = signalProductList;
	}
	public double getCashback() {
		return cashback;
	}
	public void setCashback(int cashback) {
		this.cashback = cashback;
	}
	public String getImageDomain() {
		return imageDomain;
	}
	public void setImageDomain(String imageDomain) {
		this.imageDomain = imageDomain;
	}
	public String getNewImageDomain() {
		return newImageDomain;
	}
	public void setNewImageDomain(String newImageDomain) {
		this.newImageDomain = newImageDomain;
	}
	public double getOriginalPrice() {
		return originalPrice;
	}
	public void setOriginalPrice(double originalPrice) {
		this.originalPrice = originalPrice;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public List<SuitEntity> getSuitEntityList() {
		return suitEntityList;
	}
	public void setSuitEntityList(List<SuitEntity> suitEntityList) {
		this.suitEntityList = suitEntityList;
	}
	public double getTotalCostcontent() {
		return totalCostcontent;
	}
	public void setTotalCostcontent(double totalCostcontent) {
		this.totalCostcontent = totalCostcontent;
	}
	public double getTotalCostcontent2() {
		return totalCostcontent2;
	}
	public void setTotalCostcontent2(double totalCostcontent2) {
		this.totalCostcontent2 = totalCostcontent2;
	}
	public int getTotalNum() {
		return totalNum;
	}
	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}
	
	
	
}
