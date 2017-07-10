package com.jingdong.app.reader.epub.epub;

/**
 * ePub文件中的OPF文件中的章节信息
 */
public class Spine {
	
	/**
	 * 初始化章节文件信息
	 * @param idref 所引用文件清单的条目的ID
	 * @param path 章节对应的文件的路径信息
	 * @param index 顺序
	 */
    Spine(String idref, String path, int index) {
        spineIdRef = idref;
        spinePath = path;
        playOrder = index;
    }
    /**
     * 当前章节所引用的文件的ID
     */
    public String spineIdRef;
    /**
     * 章节文件路径
     */
    public String spinePath;
    /**
     * 有的书spine缺失章节名，因此获取章节名逻辑，请用Chapter.getPageHead()或Page.getPageHead()
     */
    public String chapterName;
    
    @Deprecated
    public int playOrder;
}
