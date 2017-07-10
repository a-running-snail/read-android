package com.jingdong.app.reader.entity;

/**
 * 
 * 书籍页面信息，根据数据库pagecontent表数据构造对象
 * 
 * @author liqiang
 * 
 */
public class ReadPageContent {

	private String anchorLocation;
	private String chapterItemRef;
	private String pageStartPara;
	private String pageStartOffset;
	private String fontFace;
	private int textSize = 2;// R.integer.default_textsize_level
	private int lineSpace = 0;
	private int blockSpace = 0;
	private int pageEdgeSpace = 0;
	private int screenMode = 0;// Portrait:0 landscape:1

	public String getAnchorLocation() {
		return anchorLocation;
	}

	public void setAnchorLocation(String anchorLocation) {
		this.anchorLocation = anchorLocation;
	}

	public String getChapterItemRef() {
		return chapterItemRef;
	}

	public void setChapterItemRef(String chapterItemRef) {
		this.chapterItemRef = chapterItemRef;
	}

	public String getPageStartPara() {
		return pageStartPara;
	}

	public void setPageStartPara(String pageStartPara) {
		this.pageStartPara = pageStartPara;
	}

	public String getPageStartOffset() {
		return pageStartOffset;
	}

	public void setPageStartOffset(String pageStartOffset) {
		this.pageStartOffset = pageStartOffset;
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
