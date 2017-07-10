package com.jingdong.app.reader.me.model;

public class SignTypeData {
	/** 1 书城 2 社区 3 我 4 url */
	private int targetVCType;
	/** 1积分 2阅历 3我 4书城 5书城子模块 */
	private int targetSubType;
	/** 弹窗显示信息 */
	private String msg;
	/** 跳转按钮显示信息 */
	private String buttonText;
	/** url地址 */
	private String url;
	private TargetSubParams targetSubParams;
	
	public void setTargetVCType(int targetVCType) {
		this.targetVCType = targetVCType;
	}
	public int getTargetVCType() {
		return this.targetVCType;
	}
	public void setTargetSubType(int targetSubType) {
		this.targetSubType = targetSubType;
	}
	public int getTargetSubType() {
		return this.targetSubType;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getMsg() {
		return this.msg;
	}
	public void setButtonText(String buttonText) {
		this.buttonText = buttonText;
	}
	public String getButtonText() {
		return this.buttonText;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUrl() {
		return this.url;
	}
	public void setTargetSubParams(TargetSubParams targetSubParams) {
		this.targetSubParams = targetSubParams;
	}
	public TargetSubParams getTargetSubParams() {
		return this.targetSubParams;
	}

	public class TargetSubParams {
		private String showInfo;
		private int fid;
		private int id;
		private String relateLink;
		private String showName;
		private int showType;
		private String picAddress2All;
		private String picAddress3All;
		private String picAddressAll;
		private int isShow;
		
		public void setShowInfo(String showInfo) {
			this.showInfo = showInfo;
		}
		public String getShowInfo() {
			return this.showInfo;
		}
		public void setFid(int fid) {
			this.fid = fid;
		}
		public int getFid() {
			return this.fid;
		}
		public void setId(int id) {
			this.id = id;
		}
		public int getId() {
			return this.id;
		}
		public void setRelateLink(String relateLink) {
			this.relateLink = relateLink;
		}
		public String getRelateLink() {
			return this.relateLink;
		}
		public void setShowName(String showName) {
			this.showName = showName;
		}
		public String getShowName() {
			return this.showName;
		}
		public void setShowType(int showType) {
			this.showType = showType;
		}
		public int getShowType() {
			return this.showType;
		}
		public void setPicAddress2All(String picAddress2All) {
			this.picAddress2All = picAddress2All;
		}
		public String getPicAddress2All() {
			return this.picAddress2All;
		}
		public void setPicAddress3All(String picAddress3All) {
			this.picAddress3All = picAddress3All;
		}
		public String getPicAddress3All() {
			return this.picAddress3All;
		}
		public void setPicAddressAll(String picAddressAll) {
			this.picAddressAll = picAddressAll;
		}
		public String getPicAddressAll() {
			return this.picAddressAll;
		}
		public void setIsShow(int isShow) {
			this.isShow = isShow;
		}
		public int getIsShow() {
			return this.isShow;
		}
		
	}

}
