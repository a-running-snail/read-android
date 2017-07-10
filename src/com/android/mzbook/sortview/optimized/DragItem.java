package com.android.mzbook.sortview.optimized;

import com.android.mzbook.sortview.model.BookShelfModel;

public class DragItem {
	
	private boolean isFolder;
	private BookShelfModel mo;
	// 表示是否选中 选中的书籍个数
	private boolean isSelected;
	private int selectedBooksNum;// -1 表示单本书籍选中 >0 表示文件夹上的数字

	private boolean isDownloaded = true;// 表示书籍是否下载完成 下载完成不显示进度或者下载状态 ，否则封面显示下载
	
	/**
	 * 可拖拽条目
	 * @param model 书架图书信息实体
	 * @param folder 是否为文件夹
	 * @param isSelected 是否已被选中
	 * @param selectedBooksNum 选中的图书数量
	 * @param isDownloaded 是否已经下载完成
	 */
	public DragItem(BookShelfModel model, boolean folder, boolean isSelected,int selectedBooksNum, boolean isDownloaded) {

		this.isFolder = folder;
		this.mo = model;
		this.isSelected = isSelected;
		this.selectedBooksNum = selectedBooksNum;
		this.isDownloaded = isDownloaded;

	}

	public boolean isDownloaded() {
		return isDownloaded;
	}

	public void setDownloaded(boolean isDownloaded) {
		this.isDownloaded = isDownloaded;
	}

	public void setMo(BookShelfModel mo) {
		this.mo = mo;
	}

	public BookShelfModel getMo() {
		return mo;
	}

	public boolean isFolder() {
		return isFolder;
	}

	public void setFolder(boolean isFolder) {
		this.isFolder = isFolder;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public int getSelectedBooksNum() {
		return selectedBooksNum;
	}

	public void setSelectedBooksNum(int selectedBooksNum) {
		this.selectedBooksNum = selectedBooksNum;
	}

}