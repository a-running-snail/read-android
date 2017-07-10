package com.jingdong.app.reader.epub.epub;

/**
 * ePub目录条目
 *
 */
public class TOCItem {
	/**
	 * 目录层级
	 */
    public int level;
    /**
     * 目录名字
     */
    public String navLabel;
    /**
     * 目录对应的章节内容位置
     */
    public String contentSrc;
    /**
     * 显示给用户看的页数
     */
    public int pageNumber;

}
