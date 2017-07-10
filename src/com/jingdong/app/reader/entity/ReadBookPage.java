package com.jingdong.app.reader.entity;

/**
 * 
 * 书籍分页信息，根据数据库bookpage表数据构造对象
 * 
 * @author liqiang
 * 
 */
public class ReadBookPage {

	private String chapterPage;
	private String chapterBlockCount;
	private String fontFace;
	private int textSize = 2;// R.integer.default_textsize_level
	private int lineSpace = 0;
	private int blockSpace = 0;
	private int pageEdgeSpace = 0;
	private int screenMode = 0;// Portrait:0 landscape:1

	public String getChapterPage() {
		return chapterPage;
	}

	public void setChapterPage(String chapterPage) {
		this.chapterPage = chapterPage;
	}

	public String getChapterBlockCount() {
		return chapterBlockCount;
	}

	public void setChapterBlockCount(String chapterBlockCount) {
		this.chapterBlockCount = chapterBlockCount;
	}

	public String getFontFace() {
		return fontFace;
	}

	public void setFontFace(String fontFace) {
		this.fontFace = fontFace;
	}

	public int getTextSize() {
		return textSize;
	}

	public void setTextSize(int textSize) {
		this.textSize = textSize;
	}

	public int getLineSpace() {
		return lineSpace;
	}

	public void setLineSpace(int lineSpace) {
		this.lineSpace = lineSpace;
	}

	public int getBlockSpace() {
		return blockSpace;
	}

	public void setBlockSpace(int blockSpace) {
		this.blockSpace = blockSpace;
	}

	public int getPageEdgeSpace() {
		return pageEdgeSpace;
	}

	public void setPageEdgeSpace(int pageEdgeSpace) {
		this.pageEdgeSpace = pageEdgeSpace;
	}

	public int getScreenMode() {
		return screenMode;
	}

	public void setScreenMode(int screenMode) {
		this.screenMode = screenMode;
	}

}
