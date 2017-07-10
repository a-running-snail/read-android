package com.jingdong.app.reader.bookstore.sendbook;

import java.io.Serializable;
import java.util.List;

import com.jingdong.app.reader.activity.DraftsActivity.Draft;

public class SendBookReceiveInfo implements Serializable {
	
	private static final long serialVersionUID = 1L;
	public String orderId;
	public String ebookId;
	public String sendMsg;
	public String sendNickName;
	public String userPin;
	
	public static class SendBookReceiveInfos {
		public List<SendBookReceiveInfo> infos = null;
	}
	
	public String getEbookId() {
		return ebookId;
	}
	public void setEbookId(String ebookId) {
		this.ebookId = ebookId;
	}
	public String getSendMsg() {
		return sendMsg;
	}
	public void setSendMsg(String sendMsg) {
		this.sendMsg = sendMsg;
	}
	public String getSendNickName() {
		return sendNickName;
	}
	public void setSendNickName(String sendNickName) {
		this.sendNickName = sendNickName;
	}
	
	
}
