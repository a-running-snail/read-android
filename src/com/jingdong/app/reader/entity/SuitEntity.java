package com.jingdong.app.reader.entity;

import java.util.List;

/**
 * 促销满减实体bean
 * @author Beyond
 *
 */
public class SuitEntity {
	private int promoteId;//标示满减活动Id
	private List<ProductEntity> productEntityList; 
	private PromotionalEntity promotionalEntity;
	
	
	public int getPromoteId() {
		return promoteId;
	}

	public void setPromoteId(int promoteId) {
		this.promoteId = promoteId;
	}

	public List<ProductEntity> getProductEntityList() {
		return productEntityList;
	}

	public void setProductEntityList(List<ProductEntity> productEntityList) {
		this.productEntityList = productEntityList;
	}

	public PromotionalEntity getPromotionalEntity() {
		return promotionalEntity;
	}

	public void setPromotionalEntity(PromotionalEntity promotionalEntity) {
		this.promotionalEntity = promotionalEntity;
	}

	
	
	public class PromotionalEntity{
		private String name;
		private double needPrice;
		private double rePrice;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public double getNeedPrice() {
			return needPrice;
		}
		public void setNeedPrice(double needPrice) {
			this.needPrice = needPrice;
		}
		public double getRePrice() {
			return rePrice;
		}
		public void setRePrice(double rePrice) {
			this.rePrice = rePrice;
		}
		
		
	}
}
