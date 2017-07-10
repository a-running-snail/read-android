package com.android.mzbook.sortview.model;
/**
 * 书架拖动布局
 * @author WANGGUODONG 
 * time:2014 -7 17
 */
public class Folder {
	int folderId;
	int userid;
	String folderName;
	double changetime;
	
	
	public String getFolderName() {
		return folderName;
	}
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	public double getChangetime() {
		return changetime;
	}
	public void setChangetime(double changetime) {
		this.changetime = changetime;
	}

	public int getFolderId() {
		return folderId;
	}
	public void setFolderId(int folderId) {
		this.folderId = folderId;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}

}
