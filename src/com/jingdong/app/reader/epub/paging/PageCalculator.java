package com.jingdong.app.reader.epub.paging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;

import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.epub.css.CSS;
import com.jingdong.app.reader.epub.css.CSSCollection;
import com.jingdong.app.reader.epub.epub.TOCItem;
import com.jingdong.app.reader.epub.parser.Kit42Node;

/**
 * ePub排版分页计算
 */
public class PageCalculator {

    public static final String pageParamDivider = "-";
    private final PageContext pageContext;
    private final CSSCollection cssCollection;
    private float pageWidth;
    private float pageHeight;
    private Paint globalPaint;
    private String basePath;

    public PageCalculator(float pageWidth, float pageHeight, PageContext pageContext, CSSCollection cssCollection,Paint globalPaint) {
        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;
        this.pageContext = pageContext;
        this.globalPaint = globalPaint;
        this.cssCollection = new CSSCollection(cssCollection.getCssPath(), "");
        this.cssCollection.setCssColorJson(cssCollection.getCssColorJson());
        this.cssCollection.merge(cssCollection);
    }
    
    /**
     * 核心方法：排版分页
     * @param node dom树
     * @param chapter 章节内容
     * @param tocMap 
     * @return
     */
    protected List<Page> buildPageList(Kit42Node node, Chapter chapter, HashMap<String, TOCItem> tocMap) {
        float lineSpace = pageContext.lineSpace;
        float blockExtraSpace = pageContext.blockSpace;
        ArrayList<Page> pageList = new ArrayList<Page>();
        pageList.add(chapter.createPage(0, 0));
        String divHashcode = null;
        String blockId = null;
        String tocId = null;
        int paraIndex = 0;
        int tableRowSpan = 0;
        int tableRowNumber = 0;//行序号
        int tableCellNumber = 0;//列序号
        int tableRowSpanCell = 0;//表格rowspan单元格列序号
        int tableCellPageMax = 0;//某列分页最多页数
        int divFloatPageMax = 0;//某div-float分页最多页数
        float tableRowSpanWidth = 0;
        float tableRowYOffset = 0;//行的Y轴偏移
        float tableCellWidth = 0;//单元格宽度
        float tableCellSpacing = 0;//单元格间隔
        float tableCellYOffset = 0;//单元格Y轴偏移
        float tableCellMaxYOffset = 0;//单元格最大Y轴偏移
        float maxFloatHeight = 0;//div-float最大高度
        float divFloatYOffset = 0;//div-floatY轴偏移
        float divFloatXOffset = 0;//div-floatX轴偏移
        float lineMarginLeft = 0;
        float lineMarginRight = 0;
        float blockFloatHeight = 0;//float block整个高度，可能超过几页高度
        float currentPageXOffset = 0;
        float currentPageYOffset = 0;
        boolean isTocPageIndex = false;
        boolean isFloatBlock = false;
        boolean isDivFloatBlock = false;
        boolean isTableRowPageDone = false;//表格某行全部单元格分页结束
        boolean isTableCellPageDone = false;//表格某单元格分页结束
        boolean isTablePageMaxYOffset = false;//是否存在最大Y轴坐标
        List<String> tableCellPageList = new ArrayList<String>();//记录表格cell分页的startPara和offset
        HashMap<Integer, List<String>> tablePageHash = new HashMap<Integer, List<String>>();//记录表格分页所有cell的startPara和offset
        HashMap<Integer, Float> tableYOffsetHash = new HashMap<Integer, Float>();//记录cell的Y轴offset
        List<String> divFloatPageList = new ArrayList<String>();//记录div-float分页的startPara和offset
        HashMap<Integer, List<String>> divFloatPageHash = new HashMap<Integer, List<String>>();//记录所有div-float分页的startPara和offset

        ElementBuilder eb = new ElementBuilder(cssCollection, globalPaint, pageContext.pxPerEm, new Size(pageWidth,pageHeight), chapter.getPagePool());
        List<Block> blockList = new ArrayList<Block>();
        //核心：组织段落列表
        eb.buildBlockList(blockList, node);
        chapter.setChapterTitle(eb.getTitle());//章节标题
        eb.release();//1、清理释放局部的css不影响全局的css 2、取消注册本地广播
        //对段落分页排版
        for (Block block : blockList) {
            if (chapter.isCancelBuildPage()) {
                return null;
            }
            if (block.isDisplayHidden()) {
                continue;
            }
            if (!TextUtils.isEmpty(block.id)) {
                blockId = block.id;
                chapter.addIdLocation(blockId, paraIndex);
                if (chapter.isTocId(blockId)) {//是否是章节目录对应的段落
                    tocId = blockId;
                }
                isTocPageIndex = true;
            }
            if (blockFloatHeight <= 0) {
                blockFloatHeight = 0;
                lineMarginLeft = 0;
                lineMarginRight = 0;
            }
            if (block.isTableRowSpan()) {
                tableRowSpan = block.getTableRowSpan();
                tableRowSpanCell = block.getTableCellNumber();
                tableRowSpanWidth = block.getTableCellWidth();
            }
            if (tableRowNumber != block.getTableRowNumber()) {
                currentPageXOffset = 0;
                tableCellNumber = 1;
            }
            
            //表格相关信息初始化
            if (block.isTableCell()) {
                tableCellSpacing = block.getTableCellSpacing();
                if (block.getTableCellNumber() == 1) {
                    if (tableRowNumber == block.getTableRowNumber()) {
                        tableCellYOffset = Math.min(currentPageYOffset, tableCellYOffset);
                    } else {
                        if (tableYOffsetHash.size() > 0) {
                            tableCellYOffset = 0;
                            for (Integer key : tableYOffsetHash.keySet()) {
                                Float ft = tableYOffsetHash.get(key);
                                if (tableRowSpan <= 0) {
                                    tableCellYOffset = Math.max(tableCellYOffset, ft.floatValue());
                                } else {
                                    tableCellYOffset = Math.min(tableCellYOffset, ft.floatValue());
                                }
                            }
                            tableYOffsetHash.clear();
                        } else {
                            if (tableRowSpan <= 0) {
                                tableCellYOffset = Math.max(currentPageYOffset, tableCellMaxYOffset);
                            } else {
                                if (tablePageHash.size() > 0) {
                                    tableCellYOffset = Math.max(currentPageYOffset, tableCellMaxYOffset);
                                } else {
                                    tableCellYOffset = Math.min(currentPageYOffset, tableCellMaxYOffset);
                                }
                            }
                        }
                        currentPageYOffset = tableCellYOffset;
                        tableRowYOffset = tableCellYOffset;
                    }
                } else {
                    if (isTableCellPageDone) {
                        isTableCellPageDone = false;
                        if (tableCellNumber != block.getTableCellNumber()) {
                            tableCellYOffset = tableRowYOffset;
                            currentPageYOffset = tableRowYOffset;
                        }
                    } else {
                        if (tableCellNumber != block.getTableCellNumber()) {
                            tableCellMaxYOffset = Math.max(currentPageYOffset, tableCellMaxYOffset);
                            currentPageYOffset = tableCellYOffset;
                        }
                    }
                }
            } else {//非表格排版初始化，绝大部分逻辑都走这个else分支
                if (tableYOffsetHash.size() > 0) {
                    currentPageYOffset = 0;
                    for (Integer key : tableYOffsetHash.keySet()) {
                        Float ft = tableYOffsetHash.get(key);
                        currentPageYOffset = Math.max(currentPageYOffset, ft.floatValue());
                    }
                    tableYOffsetHash.clear();
                } else {
                    currentPageYOffset = Math.max(currentPageYOffset, tableCellMaxYOffset);
                }
                currentPageXOffset = divFloatXOffset;
                tableRowSpanWidth = 0;
                tableRowSpanCell = 0;
                tableCellMaxYOffset = 0;
                tableCellYOffset = 0;
                tableCellSpacing = 0;
                tableCellNumber = 1;
                tableCellWidth = 0;
                tableRowYOffset = 0;
                tableRowNumber = 0;
                tableRowSpan = 0;
            }
            
            if (tableCellNumber != block.getTableCellNumber()) {
                currentPageXOffset += tableCellWidth + tableCellSpacing;
                tableCellPageList = new ArrayList<String>();
                isTablePageMaxYOffset = false;
            }
            block.setImageFloatMarginLeft(lineMarginLeft);
            block.setImageFloatMarginRight(lineMarginRight);
            block.addParaIndex(paraIndex);
            isTableRowPageDone = false;
            int offsetInPara = 0;
            
            //是否需要在此处进行强制分页
            if (block.isForcePageBreakBefore()) {
                offsetInPara = 0;
                currentPageYOffset = 0;
                tableCellYOffset = 0;
                tableCellMaxYOffset = 0;
                //强制分页
                pageList.add(chapter.createPage(paraIndex, offsetInPara));
            }
            
            //处理浮动
            if (block.isDivFloatBlock()) {
                if (!isDivFloatBlock) {
                    divFloatYOffset = currentPageYOffset;
                    isDivFloatBlock = true;
                    maxFloatHeight = 0;
                }
                if (!block.getDivHashcodeString().equals(divHashcode)) {
                    float lineMaxWidth = CSS.getWidth(block.getCSS().getProperties(), pageContext.pxPerEm, pageWidth);
                    lineMaxWidth = lineMaxWidth + currentPageXOffset > pageWidth ? pageWidth - currentPageXOffset : lineMaxWidth;
                    divFloatXOffset += lineMaxWidth;
                    divHashcode = block.getDivHashcodeString();
                    divFloatPageList = new ArrayList<String>();
                    blockFloatHeight = Math.max(blockFloatHeight, maxFloatHeight);
                    block.setBlockFloatHeight(blockFloatHeight);
                    currentPageYOffset = divFloatYOffset;
                }
            } else if (!block.isTableCell()) {
                if (isDivFloatBlock) {
                    isDivFloatBlock = false;
                    divHashcode = block.getDivHashcodeString();
                    currentPageXOffset = divFloatXOffset;
                    currentPageYOffset = divFloatYOffset;
                    divFloatPageList = new ArrayList<String>();
                    divFloatYOffset = 0;
                }
                blockFloatHeight = maxFloatHeight;
                block.setBlockFloatHeight(blockFloatHeight);
                if (blockFloatHeight <= 0) {
                    divFloatXOffset = 0;
                    currentPageXOffset = 0;
                }
            }
            
            //处理图片段落，图片段落可直接进行排版分页
            if (block.isImageBlock()) {
                ElementImage imageElement = block.getImageElement();
                imageElement.setEnlargeEnable(true);
                if (imageElement.isDisplayHidden()) {
                    continue;
                }
                if (!block.isForcePageBreakAfter()) {
                    block.setForcePageBreakAfter(imageElement.isForcePageBreakAfter());
                }
                if (!block.isForcePageBreakBefore()) {
                    block.setForcePageBreakBefore(imageElement.isForcePageBreakBefore());
                    //是否强制分页
                    if (block.isForcePageBreakBefore()) {
                        offsetInPara = 0;
                        currentPageYOffset = 0;
                        tableCellYOffset = 0;
                        tableCellMaxYOffset = 0;
                        pageList.add(chapter.createPage(paraIndex, offsetInPara));//强制分页
                    }
                }
                
                //图片大小
                Size size = imageElement.measureSize(pageContext, basePath);//计算图片大小
                float elementHeight = Math.min(size.height, pageHeight);
                //是否达到新增页面的条件，即当前页内容加上图片的高度是否超过屏幕展示高度
                if (elementHeight + currentPageYOffset > pageHeight) /**/{
                    divFloatYOffset = 0;
                    currentPageYOffset = 0;
                    tableCellYOffset = 0;
                    tableCellMaxYOffset = 0;
                    //新增页面
                    pageList.add(chapter.createPage(paraIndex, offsetInPara));
                }
                
                //是否目录所指的段落
                if (isTocPageIndex) {
                    isTocPageIndex = false;
                    chapter.setTocPageIndex(blockId, pageList.size());
                }
                
                //顶部Margin
                currentPageYOffset += block.getMarginTop();
                /***************** Image Float Code Start *******************/
                if (imageElement.isFloatLeft() || block.isFloatLeft()) /*左浮动*/ {
                    imageElement.setFloatLeft(true);
                    lineMarginRight = 0;
                    lineMarginLeft = size.width + imageElement.getMarginLeft()+ imageElement.getMarginRight();
                    maxFloatHeight = size.height + lineSpace + imageElement.getMarginTop() + imageElement.getMarginBottom();
                    chapter.addBlock(paraIndex, block);
                    chapter.addTocId(paraIndex, tocId);
                    blockFloatHeight = maxFloatHeight;
                    divFloatXOffset = lineMarginLeft;
                    isFloatBlock = true;
                    paraIndex++;
                    continue;
                }
                
                //右浮动
                if (imageElement.isFloatRight() || block.isFloatRight()) {
                    imageElement.setFloatRight(true);
                    lineMarginLeft = 0;
                    lineMarginRight = size.width + imageElement.getMarginLeft() + imageElement.getMarginRight();
                    maxFloatHeight = size.height + lineSpace + imageElement.getMarginTop() + imageElement.getMarginBottom();
                    chapter.addBlock(paraIndex, block);
                    chapter.addTocId(paraIndex, tocId);
                    blockFloatHeight = maxFloatHeight;
                    divFloatXOffset = lineMarginRight;
                    isFloatBlock = true;
                    paraIndex++;
                    continue;
                }
                /***************** Image Float Code End *******************/
                currentPageYOffset += size.height + lineSpace;
                isTableRowPageDone = true;
            } else {
            	//非图片段落，需要对文本内进行分词断行，计算大小后，才能进行分页
                //核心：文本内容排版断行分页
            	offsetInPara = 0;
                if (!isFloatBlock) {
                    currentPageYOffset += block.getMarginTop();
                    if (isDivFloatBlock) {
                        maxFloatHeight += block.getMarginTop();
                    } else {
                        maxFloatHeight -= block.getMarginTop();
                        if (maxFloatHeight < 0) {
                            maxFloatHeight = 0;
                        }
                    }
                } else {
                    isFloatBlock = false;
                }
                
                if (currentPageXOffset > pageWidth) {
                    currentPageXOffset = 0;
                }
                // FIXME table rowspan逻辑没有涵盖所有情况，某些情况下可能有bug，后期有待优化 --liqiang
                if (tableRowSpan > 0 && tableRowSpanCell == block.getTableCellNumber()) {
                    tableRowSpan--;
                    if (!block.isTableRowSpan()) {
                        currentPageXOffset += tableRowSpanWidth + tableCellSpacing;
                    }
                }
                if (block.isTableCell()) {
                    isTableRowPageDone = false;
                    if (currentPageXOffset == 0
                            && (tableCellNumber > 1 || tableRowNumber != block
                                    .getTableRowNumber())) {
                        if (tableRowSpan <= 0) {
                            isTableRowPageDone = true;
                        }
                    }
                } else {
                    isTableRowPageDone = true;
                }
                if (isTableRowPageDone && tablePageHash.size() > 0) {
                    isTableRowPageDone = false;
                    String paraStr = new String("");
                    String offsetStr = new String("");
                    
                    for (int n = 0; n < tableCellPageMax; n++) {
                        for (int i = 0, count = tablePageHash.size(); i < count; i++) {
                            if (tablePageHash.containsKey(Integer.valueOf(i))) {
                                List<String> cellParaList = tablePageHash.get(Integer.valueOf(i));
                                if (cellParaList.size() > n) {
                                    String para = cellParaList.get(n);
                                    String[] paraArray = para.split(pageParamDivider);
                                    paraStr += paraArray[0] + pageParamDivider;
                                    offsetStr += paraArray[1] + pageParamDivider;
                                }
                            }
                        }
                        if (!TextUtils.isEmpty(paraStr)  && !TextUtils.isEmpty(offsetStr)) {
                            pageList.add(chapter.createPage(paraStr, offsetStr));
                            paraStr = new String("");
                            offsetStr = new String("");
                        }
                    }
                    
                    tablePageHash.clear();
                    tableCellPageList.clear();
                    tableCellPageMax = 0;
                }
                
                //核心：此处要组织行信息列表,会针对段落进行断行
                List<PageLine> lineList = buildBlockLineList(block, 0, chapter, currentPageXOffset, currentPageYOffset);
                currentPageXOffset = currentPageXOffset > 0 ? currentPageXOffset : 0;
                tableRowNumber = block.getTableRowNumber();
                tableCellNumber = block.getTableCellNumber();
                tableCellWidth = block.getTableCellWidth();
                for (int i = 0, n = lineList.size(); i < n; i++) {
                    PageLine line = lineList.get(i);
                    if (chapter.isCancelBuildPage()) {
                        return null;
                    }
                    line.setDivHashcode(block.getDivHashcode() == 0?block.hashCode():block.getDivHashcode());
                    float lineHeight = line.isImageFloat()? line.getFloatHeight() : line.getLineHeight();
                    float elementHeight = Math.min(lineHeight, pageHeight);
                    //是否需要分页
                    if (elementHeight + currentPageYOffset > pageHeight) {
                        if (block.isTableCell()) {
                            blockFloatHeight = 0;
                            currentPageYOffset = 0;
                            tableCellMaxYOffset = 0;
                            isTableCellPageDone = true;
                            tableCellPageList.add(paraIndex+pageParamDivider+offsetInPara);
                            if (!tablePageHash.containsValue(tableCellPageList)) {
                                tablePageHash.put(Integer.valueOf(tablePageHash.size()), tableCellPageList);
                            }
                            if (tableCellPageMax <= tableCellPageList.size()) {
                                tableCellPageMax = tableCellPageList.size();
                                isTablePageMaxYOffset = true;
                            } else {
                                isTablePageMaxYOffset = false;
                            }
                        } else if (maxFloatHeight > 0) {
                            if (i == 0 && block.isDivBlock()) {
                                maxFloatHeight += pageHeight - currentPageYOffset;
                            }
                            currentPageYOffset = 0;
                            tableCellMaxYOffset = 0;
                            divFloatPageList.add(paraIndex+pageParamDivider+offsetInPara);
                            if (!divFloatPageHash.containsValue(divFloatPageList)) {
                                divFloatPageHash.put(Integer.valueOf(divFloatPageHash.size()), divFloatPageList);
                            }
                            if (divFloatPageMax <= divFloatPageList.size()) {
                                divFloatPageMax = divFloatPageList.size();
                            }
                        } else {
                            divFloatYOffset = 0;
                            blockFloatHeight = 0;
                            currentPageYOffset = 0;
                            tableCellYOffset = 0;
                            tableCellMaxYOffset = 0;
                            if (i == 0) {
                                currentPageYOffset += block.getMarginTop();
                                maxFloatHeight -= block.getMarginTop();
                                if (maxFloatHeight < 0) {
                                    maxFloatHeight = 0;
                                }
                            }
                            boolean isNewPage = true;
                            if (divFloatPageHash.size() > 0) {
                                String paraStr = new String("");
                                String offsetStr = new String("");
                                if (divFloatPageList.size() < divFloatPageMax) {
                                    isNewPage = false;
                                    divFloatPageList.add(paraIndex+pageParamDivider+offsetInPara);
                                }
                                
                                for (int x = 0; x < divFloatPageMax; x++) {
                                    for (int j = 0, count = divFloatPageHash.size(); j < count; j++) {
                                        if (divFloatPageHash.containsKey(Integer.valueOf(j))) {
                                            List<String> divFloatList = divFloatPageHash
                                                    .get(Integer.valueOf(j));
                                            if (divFloatList.size() > x) {
                                                String para = divFloatList.get(x);
                                                String[] paraArray = para.split(pageParamDivider);
                                                paraStr += paraArray[0] + pageParamDivider;
                                                offsetStr += paraArray[1] + pageParamDivider;
                                            }
                                        }
                                    }
                                    if (!TextUtils.isEmpty(paraStr)
                                            && !TextUtils.isEmpty(offsetStr)) {
                                        pageList.add(chapter.createPage(paraStr, offsetStr));
                                        paraStr = new String("");
                                        offsetStr = new String("");
                                    }
                                }
                                
                                divFloatPageHash.clear();
                                divFloatPageList.clear();
                                divFloatPageMax = 0;
                            }
                            
                            if (isNewPage) {
                                pageList.add(chapter.createPage(paraIndex,offsetInPara));
                            }
                        }
                    }
                    
                    
                    if (isTocPageIndex) {
                        isTocPageIndex = false;
                        chapter.setTocPageIndex(blockId, pageList.size());
                    }
                    offsetInPara += line.getCount();
                    if (!line.isImageFloat()) {
                        currentPageYOffset += lineHeight + lineSpace;
                        if (isDivFloatBlock) {
                            maxFloatHeight += lineHeight + lineSpace;
                        } else {
                            maxFloatHeight -= lineHeight + lineSpace;
                            if (maxFloatHeight < 0) {
                                maxFloatHeight = 0;
                            }
                        }
                    }
                    if (blockFloatHeight > 0) {
                        blockFloatHeight = blockFloatHeight - lineHeight - lineSpace;
                    }
                }
                for (PageLine line : lineList) {
                    chapter.getPagePool().releasePageLine(line);
                }
            }
            if (blockFloatHeight > 0) {
                blockFloatHeight -= blockExtraSpace;
            }
            if (!block.isTableCell() && !block.isDivFloatBlock()) {
                currentPageYOffset += blockExtraSpace + block.getMarginBottom();
                maxFloatHeight -= blockExtraSpace + block.getMarginBottom();
                if (maxFloatHeight < 0) {
                    maxFloatHeight = 0;
                }
            }
            chapter.addBlock(paraIndex, block);
            chapter.addTocId(paraIndex, tocId);
            ++paraIndex;
            if (block.isForcePageBreakAfter()) {
                offsetInPara = 0;
                currentPageYOffset = 0;
                tableCellYOffset = 0;
                tableCellMaxYOffset = 0;
                pageList.add(chapter.createPage(paraIndex, offsetInPara));
            }
            if (isTablePageMaxYOffset && block.isTableCell()) {
                isTablePageMaxYOffset = false;
                tableYOffsetHash.put(Integer.valueOf(tableCellNumber), Float.valueOf(currentPageYOffset));
            } else if (tableRowSpan > 0 && block.isTableCell()) {
                tableYOffsetHash.put(Integer.valueOf(tableCellNumber), Float.valueOf(currentPageYOffset));
            }
            if (paraIndex == blockList.size() && tablePageHash.size() > 0) {
                isTableRowPageDone = false;
                String paraStr = new String("");
                String offsetStr = new String("");
                
                for (int n = 0; n < tableCellPageMax; n++) {
                    for (int i = 0, count = tablePageHash.size(); i < count; i++) {
                        if (tablePageHash.containsKey(Integer.valueOf(i))) {
                            List<String> cellParaList = tablePageHash
                                    .get(Integer.valueOf(i));
                            if (cellParaList.size() > n) {
                                String para = cellParaList.get(n);
                                String[] paraArray = para.split(pageParamDivider);
                                paraStr += paraArray[0] + pageParamDivider;
                                offsetStr += paraArray[1] + pageParamDivider;
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(paraStr)
                            && !TextUtils.isEmpty(offsetStr)) {
                        pageList.add(chapter.createPage(paraStr, offsetStr));
                        paraStr = new String("");
                        offsetStr = new String("");
                    }
                }
                
                tablePageHash.clear();
                tableCellPageList.clear();
                tableCellPageMax = 0;
            }
            if ((paraIndex == blockList.size() || maxFloatHeight <= 0) && divFloatPageHash.size() > 0) {
                String paraStr = new String("");
                String offsetStr = new String("");
                
                for (int n = 0; n < divFloatPageMax; n++) {
                    for (int i = 0, count = divFloatPageHash.size(); i < count; i++) {
                        if (divFloatPageHash.containsKey(Integer.valueOf(i))) {
                            List<String> divFloatList = divFloatPageHash
                                    .get(Integer.valueOf(i));
                            if (divFloatList.size() > n) {
                                String para = divFloatList.get(n);
                                String[] paraArray = para.split(pageParamDivider);
                                paraStr += paraArray[0] + pageParamDivider;
                                offsetStr += paraArray[1] + pageParamDivider;
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(paraStr)
                            && !TextUtils.isEmpty(offsetStr)) {
                        pageList.add(chapter.createPage(paraStr, offsetStr));
                        paraStr = new String("");
                        offsetStr = new String("");
                    }
                }
                
                divFloatPageHash.clear();
                divFloatPageList.clear();
                divFloatPageMax = 0;
            }
        }
        return pageList;
    }
    
    /**
     * 构建段落航信息列表（即进行排版断行）
     * @param block
     * @param elementOffset
     * @param chapter
     * @param offsetX
     * @param offsetY
     * @return
     */
    private List<PageLine> buildBlockLineList(Block block, int elementOffset, Chapter chapter, float offsetX, float offsetY) {
        List<PageLine> lineList = new ArrayList<PageLine>();
        if (chapter.isCancelBuildPage()) {
            return lineList;
        }
        offsetX = Math.abs(offsetX);
        Paint blockPaint = block.paint;
        float currentLineWidth = 0;
        //段落行高
        float blockLineHeight = CSS.getLineHeight(block.getCSS().getProperties(), pageContext.pxPerEm);
        //计算得到的最终段落行高
        blockLineHeight = blockLineHeight <= 0 ? blockPaint.descent() - blockPaint.ascent() : blockLineHeight;
        float currentLineHeight = blockLineHeight;
        float lineSpaceHeight = pageContext.lineSpace;
        float blockMarginLeft = block.getMarginLeft();
        float blockMarginRight = block.getMarginRight();
        float marginLeft = blockMarginLeft;
        float marginRight = blockMarginRight;
        float blockFloatHeight = block.getBlockFloatHeight();
        float blockIndent = getBlockIndent(block);
        float[] padding = CSS.getPadding(block.getCSS().getProperties(), pageContext.pxPerEm);
        float lineMaxWidth = CSS.getWidth(block.getCSS().getProperties(), pageContext.pxPerEm, pageWidth);
        lineMaxWidth = lineMaxWidth > pageWidth ? pageWidth: lineMaxWidth;
        if (block.isTableCell()) {
            lineMaxWidth = block.getTableCellWidth();
        }
        lineMaxWidth = lineMaxWidth + offsetX > pageWidth ? pageWidth - offsetX : lineMaxWidth;
        if (lineMaxWidth == 0) {
            lineMaxWidth = pageWidth - offsetX;
        }
        float paddingTop = padding!=null?padding[0]:CSS.getPaddingTop(block.getCSS().getProperties(), pageContext.pxPerEm);
        float paddingRight = padding!=null?padding[1]:CSS.getPaddingRight(block.getCSS().getProperties(), pageContext.pxPerEm);
        float paddingBottom = padding!=null?padding[2]:CSS.getPaddingBottom(block.getCSS().getProperties(), pageContext.pxPerEm);
        float paddingLeft = padding!=null?padding[3]:CSS.getPaddingLeft(block.getCSS().getProperties(), pageContext.pxPerEm);
        block.setPaddingTop(paddingTop);
        block.setPaddingBottom(paddingBottom);
        Border border = Border.parseBorder(block.getCSS());
        if (border.isFullBorder()) {
            paddingLeft += border.getLeftWidth() + border.getPaddingLeft();
            paddingRight += border.getRightWidth() + border.getPaddingRight();
        } else if (border.isLeftBorder()) {
            paddingLeft += border.getLeftWidth() + border.getPaddingLeft();
        } else if (border.isRightBorder()) {
            paddingRight += border.getRightWidth() + border.getPaddingRight();
        }
        String backgroundImageSrc = null; 
        boolean isOrderedLine = block.isOrderedLine();
        boolean isUnorderedLine = block.isUnorderedLine();
        float lineIndent = 0;
        float floatHeight = 0;
        if (isOrderedLine || isUnorderedLine) {
            lineIndent = pageContext.pxPerEm * 2;
        }
        PagePool pagePool = chapter.getPagePool();
        PageLine floatStartLine = null;
        PageLine currentLine = pagePool.acquirePageLine();
        currentLine.initialize(blockPaint, cssCollection);
        currentLine.prepareDrawBackgroundParam(lineMaxWidth, lineSpaceHeight);
        currentLine.style = block.getCSS();
        currentLine.setLineWidth(lineMaxWidth);
        currentLine.setLineIndent(lineIndent);
        currentLine.setPaddingLeft(paddingLeft);
        currentLine.setPaddingRight(paddingRight);
        currentLine.setMarginLeft(marginLeft);
        currentLine.setMarginRight(marginRight);
        lineList.add(currentLine);
        boolean isImageBlock = false;
        boolean isImageFloat = false;
        List<Element> elementList = new ArrayList<Element>();
        if (elementOffset > 0) {
            for (int i = 0, offset = 0, n = block.elementList.size(); i < n; i++) {
                Element e = block.elementList.get(i);
                if (offset < elementOffset && offset + e.getCount() > elementOffset) {
                    if (e instanceof ElementText) {//文本节点
                        ElementText et = ((ElementText) e).getSubElementText(elementOffset - offset);
                        offset += e.getCount();//字符的个数
                        elementList.add(et);
                        continue;
                    }
                }
                if (e instanceof ElementText) {
                    ElementText text = ((ElementText) e);
                    if (!"\n".equals(text.getContent())) {
                        offset += e.getCount();//字符的个数
                    }
                } else {
                    offset += e.getCount();
                }
                if (offset > elementOffset) {
                    elementList.add(e);
                }
            }
        } else {
            elementList.addAll(block.elementList);
        }
        
        for (Element e : elementList) {
            if (chapter.isCancelBuildPage()) {
                return lineList;
            }
            
            //图片
            if (e instanceof ElementImage) {
                ElementImage eImg = (ElementImage) e;
                if (eImg.isDisplayHidden()) /*元素是否隐藏*/{
                    continue;
                }
            }
            
            chapter.addAnchor(e.getAnchorId(), e.paraIndex, e.offsetInPara);
            if (e instanceof ElementText) {//文本元素
                String content = ((ElementText) e).getContent();
                if ("\n".equals(content)) {//强行换行
                	offsetY = currentLineHeight + lineSpaceHeight;
                    currentLineWidth = 0;
                    currentLineHeight = blockLineHeight;
                    currentLine = forceNewLine(block, lineList, blockPaint, currentLineWidth, currentLineHeight,
							lineSpaceHeight, marginLeft, marginRight, lineMaxWidth, paddingRight, paddingLeft,
							lineIndent, pagePool, currentLine);
                    continue;
                }
            }
            
            if (e instanceof ElementImage) {//图片元素
            	//元素大小
                Size size = e.measureSize(pageContext, basePath);
                ElementImage imageElement = (ElementImage) e;
                isImageBlock = imageElement.isInBlock();//是否CSS的块级样式，即非行内元素
                if (size.height > currentLineHeight + lineSpaceHeight) {
                    imageElement.setEnlargeEnable(true);
                }

                isImageFloat = false;
                if (size.height <= currentLineHeight) {
                    imageElement.setInBlock(isImageBlock);
                }
                
                if (imageElement.isFloatLeft()) {
                    isImageFloat = true;
                    isImageBlock = true;
                    marginRight = 0;
                    marginLeft = size.width + imageElement.getMarginLeft() + imageElement.getMarginRight();
                    blockFloatHeight = size.height + lineSpaceHeight + imageElement.getMarginTop() + imageElement.getMarginBottom();
                } else if (imageElement.isFloatRight()) {
                    isImageFloat = true;
                    isImageBlock = true;
                    marginLeft = 0;
                    marginRight = size.width + imageElement.getMarginLeft() + imageElement.getMarginRight();
                    blockFloatHeight = size.height + lineSpaceHeight + imageElement.getMarginTop() + imageElement.getMarginBottom();
                }
                
                if (isImageBlock) {
                    if (!currentLine.isEmpty()) {
                        currentLine.setRect(new RectF(lineIndent, 0,currentLineWidth - lineIndent,currentLineHeight));
                        currentLine.prepareDrawBackgroundParam(lineMaxWidth,lineSpaceHeight);
                        currentLine.setLineWidth(lineMaxWidth);
                        currentLine.setLastLine(true);
                        offsetY = currentLineHeight + lineSpaceHeight;
                        currentLineWidth = 0;
                        currentLineHeight = blockLineHeight;
                        currentLine = pagePool.acquirePageLine();
                        currentLine.initialize(blockPaint, cssCollection);
                        currentLine.style = block.getCSS();
                        currentLine.setLineIndent(lineIndent);
                        lineList.add(currentLine);
                    }
                    currentLineHeight = Math.max(currentLineHeight, size.height);
                    currentLine.addElement(e);
                    currentLine.setRect(new RectF(lineIndent, 0, lineMaxWidth-lineIndent, currentLineHeight));
                    currentLine.prepareDrawBackgroundParam(lineMaxWidth, lineSpaceHeight);
                    currentLine.setLineWidth(lineMaxWidth);
                    currentLine.setImageFloat(isImageFloat);
                    if (isImageFloat) {
                        floatStartLine = currentLine;
                        floatHeight = 0;
                    } else {
                        currentLine.setMarginLeft(marginLeft);
                        currentLine.setMarginRight(marginRight);
                    }
                    
                    if (isImageFloat) {
                        currentLineHeight = blockLineHeight;
                    }
                    offsetY = currentLineHeight + lineSpaceHeight;
                    currentLineWidth = 0;
                    currentLineHeight = blockLineHeight;
                    currentLine = pagePool.acquirePageLine();
                    currentLine.initialize(blockPaint, cssCollection);
                    currentLine.style = block.getCSS();
                    currentLine.prepareDrawBackgroundParam(lineMaxWidth, lineSpaceHeight);
                    currentLine.setLineWidth(lineMaxWidth);
                    currentLine.setLineIndent(lineIndent);
                    currentLine.setPaddingLeft(paddingLeft);
                    currentLine.setPaddingRight(paddingRight);
                    currentLine.setMarginLeft(marginLeft);
                    currentLine.setMarginRight(marginRight);
                    lineList.add(currentLine);
                    continue;
                }
            } else {//非图片元素
                if (isImageBlock) {
                    ((ElementText) e).setTextIndent(blockIndent);
                    isImageBlock = false;
                }
            }
            
            Size size = e.measureSize(pageContext, basePath);//元素大小
            if (blockFloatHeight > 0) {
                currentLineHeight = Math.max(currentLineHeight, size.height);
                if (currentLineHeight + offsetY > pageHeight) {
                    marginLeft = 0;
                    marginRight = 0;
                }
            } else {
                offsetX = 0;
                lineMaxWidth = CSS.getWidth(block.getCSS().getProperties(), pageContext.pxPerEm, pageWidth);//todo 优化
                if (block.isTableCell()) {
                    lineMaxWidth = block.getTableCellWidth();
                }
                lineMaxWidth = lineMaxWidth + offsetX > pageWidth ? pageWidth - offsetX : lineMaxWidth;
                if (lineMaxWidth == 0) {
                    lineMaxWidth = pageWidth - offsetX;
                }
            }
            
            ElementText eHyphen = null;
            if (e instanceof ElementText) {
                ElementText et = (ElementText) e;
                eHyphen = et.getHyphenText();
            }
            
            float limitedWidth = lineMaxWidth - marginLeft - marginRight - paddingLeft - paddingRight - currentLineWidth - lineIndent;
            String textWords = null;
            
            if (size.width > limitedWidth || eHyphen != null) {
                float lineWidthTemp = Float.MIN_VALUE;
                do {
                    if (chapter.isCancelBuildPage()) {
                        break;
                    }
                    if (blockFloatHeight > 0 && lineWidthTemp != currentLineWidth) {
                        blockFloatHeight = blockFloatHeight - currentLineHeight - lineSpaceHeight;
                        floatHeight += currentLineHeight + lineSpaceHeight;
                    }
                    if (blockFloatHeight <= 0) {
                        blockFloatHeight = 0;
                        marginLeft = blockMarginLeft;
                        marginRight = blockMarginRight;
                        if (floatStartLine != null) {
                            floatStartLine.setFloatHeight(floatHeight);
                        }
                    }
                    if (e instanceof ElementText) {
                        ElementText et = (ElementText) e;
                        if (eHyphen == null && et.getCount() > 6) {
                            et = ((ElementText) e).clone();//遇到分词处理要clone避免修改了原数据
                            String word = et.getText();
                            String hyphenWord = pageContext.doHyphenation(et.paint, word, limitedWidth, et, block.isTableCell());
                            String hyphenCode = pageContext.getHyphenCode();
                            eHyphen = et.handleHyphenation(hyphenWord, hyphenCode, block.isTableCell());
                        }
                        if (eHyphen != null) {
                            et = et.getSubElementText(0);
                            currentLine.addElement(eHyphen);
                            size = eHyphen.measureSize(pageContext, basePath);
                            currentLineHeight = Math.max(currentLineHeight, size.height);
                            currentLineWidth += size.width;
                            size = et.measureSize(pageContext, basePath);
                            e = et;
                        }
                    }
                    if (currentLine.elementList.size() > 0) {
                        currentLine.setBackgroundImageSrc(backgroundImageSrc);
                        currentLine.setRect(new RectF(lineIndent, 0, currentLineWidth-lineIndent, currentLineHeight));
                        currentLine.prepareDrawBackgroundParam(lineMaxWidth, lineSpaceHeight);
                        currentLine.setLineWidth(lineMaxWidth);
                        offsetY = currentLineHeight + lineSpaceHeight;
                        currentLineWidth = 0;
                        currentLineHeight = blockLineHeight;
                        currentLine = pagePool.acquirePageLine();
                        currentLine.initialize(blockPaint, cssCollection);
                        currentLine.style = block.getCSS();
                        currentLine.prepareDrawBackgroundParam(lineMaxWidth, lineSpaceHeight);
                        currentLine.setLineWidth(lineMaxWidth);
                        currentLine.setLineIndent(lineIndent);
                        currentLine.setPaddingLeft(paddingLeft);
                        currentLine.setPaddingRight(paddingRight);
                        currentLine.setMarginLeft(marginLeft);
                        currentLine.setMarginRight(marginRight);
                        lineList.add(currentLine);
                    }
                    limitedWidth = lineMaxWidth - marginLeft - marginRight - paddingLeft - paddingRight - currentLineWidth - lineIndent;
                    // FIXME 分词循环和退出逻辑需要再优化 --liqiang
                    if (textWords != null && textWords.equals(e.getContent())) {
                        break;
                    } else {
                        textWords = e.getContent();
                    }
                    eHyphen = null;
                    lineWidthTemp = currentLineWidth;
                } while (size.width > limitedWidth);
            }
            currentLineHeight = Math.max(currentLineHeight, size.height);
            currentLine.addElement(e);
            currentLineWidth += size.width;
        }
        if (blockFloatHeight > 0) {
            blockFloatHeight = blockFloatHeight - currentLineHeight - lineSpaceHeight;
        }
        currentLine.setBackgroundImageSrc(backgroundImageSrc);
        currentLine.setRect(new RectF(lineIndent, 0, currentLineWidth-lineIndent, currentLineHeight));
        currentLine.prepareDrawBackgroundParam(lineMaxWidth, lineSpaceHeight);

        if (lineList.size() > 0) {
            PageLine firstLine = lineList.get(0);
            firstLine.setFirstLine(true);
            if (elementOffset == 0) {
                firstLine.setPaddingTop(paddingTop);
            }
            if (isOrderedLine || isUnorderedLine) {
                firstLine.setOrderedLine(isOrderedLine);
                firstLine.setUnorderedLine(isUnorderedLine);
                firstLine.setOrderedLineIndex(block.getOrderedIndex());
            }
            PageLine lastLine = lineList.get(lineList.size() - 1);
            lastLine.setPaddingBottom(paddingBottom);
            lastLine.setLastLine(true);
            lastLine.setLineSpaceDrawBg(false);
        }

        return lineList;
    }

    /**
     * 遇到\n则强行换行
     * @param block
     * @param lineList
     * @param blockPaint
     * @param currentLineWidth
     * @param currentLineHeight
     * @param lineSpaceHeight
     * @param marginLeft
     * @param marginRight
     * @param lineMaxWidth
     * @param paddingRight
     * @param paddingLeft
     * @param lineIndent
     * @param pagePool
     * @param currentLine
     * @return
     */
	private PageLine forceNewLine(Block block, List<PageLine> lineList, Paint blockPaint, float currentLineWidth,
			float currentLineHeight, float lineSpaceHeight, float marginLeft, float marginRight, float lineMaxWidth,
			float paddingRight, float paddingLeft, float lineIndent, PagePool pagePool, PageLine currentLine) {
		currentLine.setRect(new RectF(lineIndent, 0, currentLineWidth-lineIndent, currentLineHeight));
		currentLine.prepareDrawBackgroundParam(lineMaxWidth, lineSpaceHeight);
		currentLine.setLastLine(true);
		currentLine = pagePool.acquirePageLine();
		currentLine.initialize(blockPaint, cssCollection);
		currentLine.style = block.getCSS();
		currentLine.setLineIndent(lineIndent);
		currentLine.setPaddingLeft(paddingLeft);
		currentLine.setPaddingRight(paddingRight);
		currentLine.setMarginLeft(marginLeft);
		currentLine.setMarginRight(marginRight);
		lineList.add(currentLine);
		return currentLine;
	}

    private void layoutLine(Block block, PageLine line, Page page) {
        if (line.style.getTextAlign() == CSS.Align.Justify) {
            layoutJustifyLine(block, line, page);
        } else if (line.style.getTextAlign() == CSS.Align.Right) {
            layoutRightLine(block, line, page);
        } else if (line.style.getTextAlign() == CSS.Align.Center) {
            layoutCenterLine(block, line, page);
        } else {
            layoutLeftLine(block, line, page);
        }
    }
  
    protected void buildPageContent(Chapter chapter, Page page) {
        if (page != null && chapter != null && pageContext != null) {
            String divHashcode = null;
            int startIndex = 0;
            int tableRowSpan = 0;
            int tableRowNumber = 0;//表格行序号
            int tableCellNumber = 0;//表格列序号
            int tableRowSpanCell = 0;//表格rowspan单元格列序号
            int startTableRowNumber = 0;//页面起始表格行数
            int startTableCellNumber = 0;//页面起始表格列数
            float tableRowSpanWidth = 0;
            float tableCellWidth = 0;//单元格宽度
            float tableCellSpacing = 0;//单元格间隔
            float tableCellYOffset = 0;//单元格Y轴偏移
            float tableCellBgYOffset = 0;//单元格背景Y轴偏移
            float tableCellMaxYOffset = 0;//单元格最大Y轴偏移
            float divBgYOffset = 0;//div背景Y轴偏移
            float maxFloatHeight = 0;//div-float最大高度
            float divFloatYOffset = 0;//div-floatY轴偏移
            float divFloatXOffset = 0;//div-floatX轴偏移
            float lineSpace = pageContext.lineSpace;
            float blockExtraSpace = pageContext.blockSpace;
            float currentPageXOffset = 0;
            float currentPageYOffset = 0;
            float lineMarginLeft = 0;
            float lineMarginRight = 0;
            float blockFloatHeight = 0;//float block高度，最多一页高度
            float paddingTop = 0;
            float paddingBottom = 0;
            float divRightFloatWidth = 0;
            boolean isPageHead = true;
            boolean isFloatBlock = false;
            boolean isDivFloatBlock = false;
            boolean isDivFloatPageDone = false;
            boolean isTableRowSpanStart = false;
            boolean isTableCellPageDone = false;//表格某单元格分页结束
            int paraIndex = page.startParaIndex;
            int offsetInParaOfPage = page.startOffset;
            int endParaIndex = -1;
            int endOffsetInPara = -1;
            int[] paraIndexArray = null;//多个开始位置
            int[] offsetInParaArray = null;//多个开始位置偏移
            if (page.isMultipleStartPara()) {
                String[] startParaStrArray = null;
                if(!TextUtils.isEmpty(page.startParaStr)) {
                    startParaStrArray = page.startParaStr.split(pageParamDivider);
                }
                String[] startOffsetStrArray = null;
                if(!TextUtils.isEmpty(page.startOffsetStr)) {
                    startOffsetStrArray = page.startOffsetStr.split(pageParamDivider);
                }
                if(null != startParaStrArray) {
                    paraIndexArray = new int[startParaStrArray.length];
                }
                if(null != startOffsetStrArray) {
                    offsetInParaArray = new int[startOffsetStrArray.length];    
                }
                if(null != startParaStrArray) {
                    for (int i = 0; i < startParaStrArray.length; i++) {
                        paraIndexArray[i] = Integer.valueOf(startParaStrArray[i]);
                        offsetInParaArray[i] = Integer.valueOf(startOffsetStrArray[i]);
                    }
                }
                Block block = chapter.getBlock(paraIndex);
                if (block != null && block.isTableCell()) {
                    tableRowNumber = block.getTableRowNumber();
                    tableCellNumber = block.getTableCellNumber();
                    startTableRowNumber = tableRowNumber;
                    startTableCellNumber = tableCellNumber;
                    isTableRowSpanStart = block.isTableRowSpan();
                }
            }
            Page nextPage = chapter.getNextPage(page);
            if (nextPage != null) {
                if (nextPage.isMultipleStartPara()) {
                    String[] startParaStrArray = null;
                    if(!TextUtils.isEmpty(page.startParaStr)) {
                        startParaStrArray = nextPage.startParaStr.split(pageParamDivider);
                    }
                    String[] startOffsetStrArray = null;
                    if(!TextUtils.isEmpty(page.startOffsetStr)) {
                        startOffsetStrArray = nextPage.startOffsetStr.split(pageParamDivider);
                    }
                    if(null != startParaStrArray) {
                        for (int i = 0; i < startParaStrArray.length; i++) {
                            int nextParaIndex = Integer.valueOf(startParaStrArray[i]);
                            Block block = chapter.getBlock(nextParaIndex);
                            if (!block.isTableCell() && !block.isFloatLeft() && !block.isFloatRight()) {
                                endParaIndex = nextParaIndex;
                                endOffsetInPara = Integer.valueOf(startOffsetStrArray[i]);
                                break;
                            }
                        }
                    }
                } else {
                    endParaIndex = nextPage.startParaIndex;
                    endOffsetInPara = nextPage.startOffset;
                }
            }
            HashMap<String, PageLine> divLineMap = new HashMap<String, PageLine>();
            HashMap<Integer, PageLine> tableCellLineMap = new HashMap<Integer, PageLine>();
            if (page.isReadNoteShare()) {
                // 分享笔记时会清空block的offset之前的数据，这里offsetInParaOfPage应该置成0
                offsetInParaOfPage = 0;
            }
            
            PagePool pagePool = chapter.getPagePool();
            while (true) {
                if (page.isReadNoteShare()) {
                    if (paraIndex > page.endParaIndex) {
                        page.setContentReady(true);
                        break;
                    }
                }
                Block block = chapter.getBlock(paraIndex);
                if (block == null) {
                    page.setContentReady(true);
                    break;
                }
                if (chapter.isCancelBuildPage()) {
                    return;
                }
                if (block.isDisplayHidden()) {
                    paraIndex++;
                    continue;
                }
                if (blockFloatHeight <= 0) {
                    blockFloatHeight = 0;
                    lineMarginLeft = 0;
                    lineMarginRight = 0;
                }
                if (block.isTableCell()) {
                    tableCellSpacing = block.getTableCellSpacing();
                    if (startTableRowNumber == block.getTableRowNumber()) {
                        if (startTableCellNumber == block.getTableCellNumber()) {
                            if (isTableCellPageDone) {
                                paraIndex++;
                                continue;
                            }
                        } else {
                            isTableCellPageDone = false;
                            startIndex ++;
                            if (paraIndexArray != null && startIndex < paraIndexArray.length) {
                                paraIndex = paraIndexArray[startIndex];
                                offsetInParaOfPage = offsetInParaArray[startIndex];
                                Block cell = chapter.getBlock(paraIndex);
                                startTableRowNumber = cell.getTableRowNumber();
                                startTableCellNumber = cell.getTableCellNumber();
                                isTableRowSpanStart = cell.isTableRowSpan();
                                continue;
                            } else {
                                paraIndex++;
                                continue;
                            }
                        }
                    } else {
                        isTableCellPageDone = false;
                        if (isTableRowSpanStart) {
                            paraIndex++;
                            continue;
                        }
                    }
                    if (block.getTableCellNumber() == 1) {
                        if (tableRowNumber == block.getTableRowNumber()) {
                            tableCellYOffset = Math.min(currentPageYOffset, tableCellYOffset);
                        } else {
                            if (tableRowSpan <= 0) {
                                tableCellYOffset = Math.max(currentPageYOffset, tableCellMaxYOffset);
                            } else {
                                tableCellYOffset = Math.min(currentPageYOffset, tableCellMaxYOffset);
                            }
                            currentPageYOffset = tableCellYOffset;
                        }
                    } else {
                        if (tableCellNumber != block.getTableCellNumber()) {
                            tableCellMaxYOffset = Math.max(currentPageYOffset, tableCellMaxYOffset);
                            currentPageYOffset = tableCellYOffset;
                        } else {
                            if (startTableRowNumber == block.getTableRowNumber() && startTableCellNumber == block.getTableCellNumber()) {
                                tableCellYOffset = Math.max(currentPageYOffset, tableCellMaxYOffset);
                                currentPageYOffset = tableCellYOffset;
                            }
                        }
                    }
                    if (block.isTableRowSpan()) {
                        tableRowSpan = block.getTableRowSpan();
                        tableRowSpanCell = block.getTableCellNumber();
                        tableRowSpanWidth = block.getTableCellWidth();
                    }
                } else {
                    currentPageYOffset = Math.max(currentPageYOffset, tableCellMaxYOffset);
                    if (!block.isDivFloatBlock()) {
                        currentPageXOffset = divFloatXOffset;
                    }
                    tableRowSpanWidth = 0;
                    tableRowSpanCell = 0;
                    tableCellMaxYOffset = 0;
                    tableCellYOffset = 0;
                    tableCellSpacing = 0;
                    tableCellWidth = 0;
                    tableRowNumber = 0;
                    tableRowSpan = 0;
                    if (isTableCellPageDone) {
                        isTableCellPageDone = false;
                        page.setContentReady(true);
                        break;
                    }
                }
                if (tableRowNumber != block.getTableRowNumber()) {
                    currentPageXOffset = divFloatXOffset;
                    tableCellNumber = 1;
                }
                block.setImageFloatMarginLeft(lineMarginLeft);
                block.setImageFloatMarginRight(lineMarginRight);
                if (isPageHead) {
                    isPageHead = false;
                    page.setTocId(chapter.getTocId(paraIndex));
                }
                float lineBgWidth = 0;
                if (block.isForcePageBreakBefore()
                        && page.startParaIndex != block.getParaIndex()) {
                    page.setContentReady(true);
                    break;
                }
                if (block.isDivFloatBlock()) {
                    if (!isDivFloatBlock) {
                        divFloatYOffset = currentPageYOffset;
                        isDivFloatBlock = true;
                    }
                    if (!block.getDivHashcodeString().equals(divHashcode)) {
                        float lineMaxWidth = CSS.getWidth(block.getCSS().getProperties(), pageContext.pxPerEm, pageWidth);
                        lineMaxWidth = lineMaxWidth + currentPageXOffset > pageWidth ? pageWidth - currentPageXOffset : lineMaxWidth;
                        if (block.isFloatRight()) {
                            currentPageXOffset = pageWidth - lineMaxWidth;
                            divRightFloatWidth = lineMaxWidth;
                            divFloatXOffset = 0;
                        } else {
                            divFloatXOffset += lineMaxWidth;
                        }
                        divHashcode = block.getDivHashcodeString();
                        blockFloatHeight = Math.max(blockFloatHeight, maxFloatHeight);
                        block.setBlockFloatHeight(blockFloatHeight);
                    } else {
                        if (isDivFloatPageDone) {
                            startIndex ++;
                            if (paraIndexArray != null && startIndex < paraIndexArray.length) {
                                paraIndex = paraIndexArray[startIndex];
                                offsetInParaOfPage = offsetInParaArray[startIndex];
                                isDivFloatPageDone = false;
                                continue;
                            }
                            paraIndex++;
                            continue;
                        }
                    }
                } else if (!block.isTableCell()) {
                    if (isDivFloatBlock) {
                        isDivFloatBlock = false;
                        divHashcode = block.getDivHashcodeString();
                        currentPageXOffset = divFloatXOffset;
                        currentPageYOffset = divFloatYOffset;
                    }
                    startIndex ++;
                    blockFloatHeight = maxFloatHeight;
                    block.setBlockFloatHeight(blockFloatHeight);
                    if (blockFloatHeight <= 0) {
                        divFloatXOffset = 0;
                        currentPageXOffset = 0;
                        divRightFloatWidth = 0;
                    }
                    if (paraIndexArray != null && startIndex < paraIndexArray.length) {
                        paraIndex = paraIndexArray[startIndex];
                        offsetInParaOfPage = offsetInParaArray[startIndex];
                        continue;
                    }
                }
                paraIndex++;
                if (block.isImageBlock()) {
                    ElementImage imageElement = block.getImageElement();
                    imageElement.setEnlargeEnable(true);
                    if (imageElement.isDisplayHidden()) {
                        continue;
                    }
                    if (!block.isForcePageBreakAfter()) {
                        block.setForcePageBreakAfter(imageElement.isForcePageBreakAfter());
                    }
                    if (!block.isForcePageBreakBefore()) {
                        block.setForcePageBreakBefore(imageElement.isForcePageBreakBefore());
                        if (block.isForcePageBreakBefore()
                                && page.startParaIndex != block.getParaIndex()) {
                            page.setContentReady(true);
                            break;
                        }
                    }
                    chapter.addAnchor(imageElement.getAnchorId(), imageElement.paraIndex, imageElement.offsetInPara);
                    lineBgWidth = imageElement.getParentSize().width;
                    lineBgWidth = lineBgWidth <= 0 ? pageWidth : lineBgWidth;
                    Size size = imageElement.measureSize(pageContext, basePath);

                    float elementHeight = Math.min(size.height, pageHeight);
                    if (!page.isReadNoteShare() && elementHeight + currentPageYOffset > pageHeight) {
                        currentPageYOffset = 0;
                        page.setContentReady(true);
                        break;
                    }

                    float x = (lineBgWidth - size.width) / 2;
                    if (x < 0) {
                        x = 0;
                    }
                    currentPageYOffset += block.getMarginTop();
                    RectF rect = new RectF(x, currentPageYOffset, x + size.width, currentPageYOffset + size.height);
                    imageElement.setRect(rect);
                    PageLine imageLine = pagePool.acquirePageLine();
                    imageLine.initialize(block.paint, cssCollection);
                    imageLine.prepareDrawBackgroundParam(lineBgWidth, lineSpace);
                    imageLine.setLineWidth(lineBgWidth);
                    imageLine.style = block.getCSS();
                    imageLine.addElement(imageElement);
                    imageLine.setRect(new RectF(0, currentPageYOffset, lineBgWidth, currentPageYOffset + size.height));
                    page.addLine(imageLine);
                    page.addPicture(imageElement);
                    page.setFullScreenImage(imageElement.isMZFullScreen());
                    /***************** Image Float Code Start *******************/
                    if (imageElement.isFloatLeft() || block.isFloatLeft()) {
                        imageElement.setFloatLeft(true);
                        imageLine.setImageFloat(true);
                        lineMarginRight = 0;
                        lineMarginLeft = size.width + imageElement.getMarginLeft()
                                + imageElement.getMarginRight();
                        maxFloatHeight = size.height + lineSpace
                                + imageElement.getMarginTop()
                                + imageElement.getMarginBottom();
                        blockFloatHeight = maxFloatHeight;
                        divFloatXOffset = lineMarginLeft;
                        isFloatBlock = true;
                        continue;
                    }
                    if (imageElement.isFloatRight() || block.isFloatRight()) {
                        imageElement.setFloatRight(true);
                        imageLine.setImageFloat(true);
                        lineMarginLeft = 0;
                        lineMarginRight = size.width + imageElement.getMarginLeft()
                                + imageElement.getMarginRight();
                        maxFloatHeight = size.height + lineSpace
                                + imageElement.getMarginTop()
                                + imageElement.getMarginBottom();
                        blockFloatHeight = maxFloatHeight;
                        divFloatXOffset = -lineMarginRight;
                        isFloatBlock = true;
                        imageLine.setRect(new RectF(lineMarginRight, currentPageYOffset, lineMarginRight + lineBgWidth, currentPageYOffset + size.height));
                        continue;
                    }
                    /***************** Image Float Code End *******************/
                    currentPageYOffset += size.height + lineSpace;
                    page.readNoteHeight += (int) (size.height + lineSpace + block.getMarginBottom());
                } else {
                    int offsetInPara = 0;
                    if (!isFloatBlock && offsetInParaOfPage == 0) {
                        currentPageYOffset += block.getMarginTop();
                        if (isDivFloatBlock) {
                            maxFloatHeight += block.getMarginTop();
                        } else {
                            maxFloatHeight -= block.getMarginTop();
                            if (maxFloatHeight < 0) {
                                maxFloatHeight = 0;
                            }
                        }
                    } else {
                        isFloatBlock = false;
                    }
                    if (currentPageXOffset > pageWidth) {
                        currentPageXOffset = 0;
                    }
                    if (tableCellNumber != block.getTableCellNumber()) {
                        currentPageXOffset += tableCellWidth + tableCellSpacing;
                    }
                    if (currentPageXOffset == 0 && block.isTableCell()) {
                        currentPageXOffset = getCellOffsetX(chapter, block, pageWidth);
                        prepareTableCellBackground(chapter, block, page, paraIndexArray, pagePool, currentPageXOffset, currentPageYOffset, tableCellLineMap);
                        if (currentPageXOffset > 0) {
                            currentPageXOffset += tableCellSpacing;
                        }
                    }
                    if (tableRowSpan > 0 && tableRowSpanCell == block.getTableCellNumber()) {
                        tableRowSpan--;
                        if (!block.isTableRowSpan()) {
                            currentPageXOffset += tableRowSpanWidth + tableCellSpacing;
                        }
                    }
                    if (!block.isDivFloatBlock() && currentPageXOffset == 0 && divRightFloatWidth > 0) {
                        currentPageXOffset = divRightFloatWidth;
                    }
                    List<PageLine> lineList = buildBlockLineList(block, offsetInParaOfPage, chapter, currentPageXOffset, currentPageYOffset);
                    currentPageXOffset = currentPageXOffset > 0 ? currentPageXOffset : 0;
                    if (!block.isDivFloatBlock() && divRightFloatWidth > 0) {
                        currentPageXOffset = 0;
                    }
                    tableCellNumber = block.getTableCellNumber();
                    if (!block.isTableCell() || tableRowNumber != block.getTableRowNumber() || (tableCellNumber > 1 && currentPageXOffset <= 0)) {
                        PageLine tableRowSpanLine = null;
                        int rowSpanCellNumber = 0;
                        for (Integer key : tableCellLineMap.keySet()) {
                            PageLine line = tableCellLineMap.get(key);
                            line.setTableBorder(true);
                            if (line.isTableRowSpanLine()) {
                                tableRowSpanLine = line;
                                rowSpanCellNumber = key.intValue();
                            }
                        }
                        tableCellLineMap.clear();
                        if (tableRowSpanLine != null) {
                            tableCellLineMap.put(Integer.valueOf(rowSpanCellNumber), tableRowSpanLine);
                        }
                    }
                    if (block.isTableCell() || !divLineMap.containsKey(block.getDivHashcodeString())) {
                        for (String key : divLineMap.keySet()) {
                            PageLine line = divLineMap.get(key);
                            float width = line.getBgWidth();
                            if (line.rect.left + width > pageWidth) {
                                width = pageWidth - line.rect.left;
                            }
                            line.prepareTableCellBackgroundParam(width, divBgYOffset);
                        }
                        divLineMap.clear();
                        //不是同一个div的没有背景
                    }
                    tableRowNumber = block.getTableRowNumber();
                    tableCellWidth = block.getTableCellWidth();
                    paddingTop = offsetInParaOfPage == 0 ? block.getPaddingTop() : 0;
                    paddingBottom = block.getPaddingBottom();
                    PageLine lastLine = null;
                    for (int i = 0, n = lineList.size(); i < n; i++) {
                        PageLine line = lineList.get(i);
                        if (chapter.isCancelBuildPage()) {
                            return;
                        }
                        line.setTableCellLine(block.isTableCell());
                        line.setDivHashcode(block.getDivHashcode() == 0?block.hashCode():block.getDivHashcode());
                        if (paddingTop > 0) {
                            line.setPaddingTop(paddingTop);
                            paddingTop = 0;
                        }
                        float lineHeight = line.isImageFloat() ? line.getFloatHeight() : line.getLineHeight();
                        float elementHeight = Math.min(lineHeight, pageHeight);

                        if (!page.isReadNoteShare()
                                && elementHeight + currentPageYOffset > pageHeight) {
                            if (block.isTableCell()) {
                                blockFloatHeight = 0;
                                isTableCellPageDone = true;
                            } else if (block.isDivFloatBlock()) {
                                isDivFloatPageDone = true;
                                if (line.isFirstLine()) {
                                    maxFloatHeight += pageHeight - currentPageYOffset;
                                }
                            } else {
                                if (lastLine != null) {
                                    lastLine.setPaddingBottom(paddingBottom);
                                    lastLine.setLineSpaceDrawBg(false);
                                }
                                blockFloatHeight = 0;
                                currentPageYOffset = 0;
                                page.setContentReady(true);
                            }
                            break;
                        }
                        if (endParaIndex >= 0 && endOffsetInPara >= 0
                                && block.getParaIndex() == endParaIndex
                                && offsetInPara + offsetInParaOfPage + line.getCount() > endOffsetInPara) {
                            int len = offsetInPara + offsetInParaOfPage + line.getCount() - endOffsetInPara;
                            line.removeElements(len);
                            if (line.getCount() <= 0) {
                                page.setContentReady(true);
                                break;
                            }
                        }
                        lastLine = line;
                        page.addLine(line);
                        if (!block.isTableCell() && !block.isDivFloatBlock() && line.getLineWidth() >= pageWidth) {
                            currentPageXOffset = 0;
                        }
                        line.setOrigin(currentPageXOffset, currentPageYOffset + line.getPaddingTop());
                        layoutLine(block, line, page);
                        if (!line.isImageFloat()) {
                            currentPageYOffset += lineHeight + lineSpace;
                            page.readNoteHeight += (int) (lineHeight + lineSpace);
                            if (isDivFloatBlock) {
                                maxFloatHeight += lineHeight + lineSpace;
                            } else {
                                maxFloatHeight -= lineHeight + lineSpace;
                                if (maxFloatHeight < 0) {
                                    maxFloatHeight = 0;
                                }
                            }
                        }
                        if (blockFloatHeight > 0) {
                            blockFloatHeight = blockFloatHeight - lineHeight
                                    - lineSpace;
                        }
                        offsetInPara += line.getCount();
                        lineBgWidth = line.getBgWidth();
                        if (page.isReadNoteShare()) {
                            if (page.startParaIndex == page.endParaIndex) {
                                if (offsetInPara > page.endOffset - page.startOffset) {
                                    page.setContentReady(true);
                                    break;
                                }
                            } else {
                                if (paraIndex >= page.endParaIndex && offsetInPara > page.endOffset) {
                                    page.setContentReady(true);
                                    break;
                                }
                            }
                        }
                        if (block.isTableCell() && !tableCellLineMap.containsKey(Integer.valueOf(tableCellNumber))) {
                            line.setTableRowSpanLine(block.isTableRowSpan());
                            int lineCellNumber = tableCellNumber;
                            if (block.isTableRowSpan()) {
                                lineCellNumber = 0;
                            }
                            line.setTableBorder(true);
                            tableCellLineMap.put(Integer.valueOf(lineCellNumber), line);
                        }
                        String divHashString = block.getDivHashcodeString();
                        if (!block.isTableCell() && !TextUtils.isEmpty(divHashString) && !divLineMap.containsKey(divHashString)) {
                            divLineMap.put(divHashString, line);
                        }
                        divBgYOffset = line.rect.bottom + line.getPaddingBottom();
                    }
                    offsetInParaOfPage = 0;
                }
                if (page.isContentReady()) {
                    break;
                }
                if (block.isForcePageBreakAfter()) {
                    page.setContentReady(true);
                    break;
                }
                if (blockFloatHeight > 0) {
                    blockFloatHeight -= blockExtraSpace;
                }
                if (!block.isTableCell() && !block.isDivFloatBlock()) {
                    currentPageYOffset += blockExtraSpace + block.getMarginBottom();
                    maxFloatHeight -= blockExtraSpace + block.getMarginBottom();
                    if (maxFloatHeight < 0) {
                        maxFloatHeight = 0;
                    }
                }
                page.readNoteHeight += (int) blockExtraSpace + block.getMarginBottom();
                tableCellBgYOffset = Math.max(tableCellBgYOffset, currentPageYOffset);
                if (tableRowSpan > 0 && block.isTableCell() && !block.isTableRowSpan()) {
                    tableCellMaxYOffset = Math.max(currentPageYOffset, tableCellMaxYOffset);
                }
                if ((currentPageXOffset <= 0 || tableRowSpan >= 0) && tableCellLineMap.size() > 0) {
                    for (Integer key : tableCellLineMap.keySet()) {
                        PageLine line = tableCellLineMap.get(key);
                        float width = line.getBgWidth();
                        if (line.rect.left + width > pageWidth) {
                            width = pageWidth - line.rect.left;
                        }
                        line.prepareTableCellBackgroundParam(width, tableCellBgYOffset);
                    }
                }
                if (!block.isTableCell() && divLineMap.size() > 0) {
                    for (String key : divLineMap.keySet()) {
                        PageLine line = divLineMap.get(key);
                        float width = line.getBgWidth();
                        if (line.rect.left + width > pageWidth) {
                            width = pageWidth - line.rect.left;
                        }
                        line.prepareTableCellBackgroundParam(width, currentPageYOffset);
                    }
                }
            }
            tableCellLineMap.clear();
            divLineMap.clear();
        }
    }
    
    private float getCellOffsetX(Chapter chapter, Block block, float pageWidth) {
        if (block.isTableCell() && block.getTableCellNumber() > 1) {
            int paraIndex = block.getParaIndex();
            int index = paraIndex - 1;
            while (true) {
                Block cell = chapter.getBlock(index);
                if (!cell.isTableCell()) {
                    break;
                }
                if (block.getTableRowNumber() != cell.getTableRowNumber()) {
                    break;
                }
                index--;
            }
            int cellOffsetX = 0;
            int cellNumber = 0;
            while (true) {
                index++;
                if (index == paraIndex) {
                    return cellOffsetX;
                }
                Block cell = chapter.getBlock(index);
                if (cellNumber != cell.getTableCellNumber()) {
                    cellNumber = cell.getTableCellNumber();
                    if (block.isTableRowSpan() && cellNumber == block.getTableCellNumber()) {
                        return cellOffsetX;
                    }
                    cellOffsetX += cell.getTableCellWidth();
                    if (cellOffsetX >= pageWidth) {
                        cellOffsetX = 0;
                    }
                }
            }
        }
        return 0;
    }
    
    private void prepareTableCellBackground(Chapter chapter, Block block, Page page, int[] paraIndexArray, PagePool pagePool, float currentPageXOffset, float currentPageYOffset, HashMap<Integer, PageLine> tableCellLineMap) {
        if (paraIndexArray == null) {
            return;
        }
        if (block.isTableCell() && block.getTableCellNumber() > 1) {
            float cellSpace = block.getTableCellSpacing();
            float cellXOffset = currentPageXOffset;
            int cellNumber = block.getTableCellNumber();
            int paraIndex = block.getParaIndex();
            int index = paraIndex - 1;
            while (true) {
                Block cell = chapter.getBlock(index);
                if (!cell.isTableCell()) {
                    break;
                }
                if (block.getTableRowNumber() != cell.getTableRowNumber()) {
                    break;
                }
                if (cellNumber != cell.getTableCellNumber()) {
                    cellNumber = cell.getTableCellNumber();
                    if (!tableCellLineMap.containsKey(cellNumber)) {
                        PageLine space = pagePool.acquirePageLine();
                        space.initialize(cell.paint, cssCollection);
                        space.prepareDrawBackgroundParam(cell.getTableCellWidth(), 0);
                        space.style = cell.getCSS();
                        cellXOffset -= cell.getTableCellWidth();
                        float marginLeft = cell.getMarginLeft() + cellXOffset + cellSpace*(cellNumber-1);
                        space.setRect(new RectF(marginLeft, currentPageYOffset, marginLeft, currentPageYOffset));
                        page.addLine(space);
                        tableCellLineMap.put(Integer.valueOf(cellNumber), space);
                    }
                }
                index--;
            }
            cellXOffset = currentPageXOffset;
            index = paraIndex + 1;
            cellNumber = block.getTableCellNumber();
            List<Integer> cellList = new ArrayList<Integer>();
            for (int i = 0; i < paraIndexArray.length; i++) {
                int number = chapter.getBlock(paraIndexArray[i]).getTableCellNumber();
                cellList.add(Integer.valueOf(number));
            }
            while (true) {
                Block cell = chapter.getBlock(index);
                if (!cell.isTableCell()) {
                    break;
                }
                if (block.getTableRowNumber() != cell.getTableRowNumber()) {
                    break;
                }
                if (cellNumber != cell.getTableCellNumber()) {
                    cellNumber = cell.getTableCellNumber();
                    if (cellList.contains(Integer.valueOf(cellNumber))) {
                        continue;
                    }
                    if (!tableCellLineMap.containsKey(cellNumber)) {
                        PageLine space = pagePool.acquirePageLine();
                        space.initialize(cell.paint, cssCollection);
                        space.prepareDrawBackgroundParam(cell.getTableCellWidth(), 0);
                        space.style = cell.getCSS();
                        float marginLeft = cell.getMarginLeft() + cellXOffset + block.getTableCellWidth() + cellSpace*(cellNumber-1);
                        cellXOffset += cell.getTableCellWidth();
                        space.setRect(new RectF(marginLeft, currentPageYOffset, marginLeft, currentPageYOffset));
                        page.addLine(space);
                        tableCellLineMap.put(Integer.valueOf(cellNumber), space);
                    }
                }
                index++;
            }
        }
    }
    
    protected void buildBlockList(Kit42Node node, Chapter chapter, HashMap<String, TOCItem> pageHeadMap) {
        List<Block> blockList = new ArrayList<Block>();
        String tocId = null;
        int paraIndex = 0;

        ElementBuilder eb = new ElementBuilder(cssCollection, globalPaint, pageContext.pxPerEm, new Size(pageWidth,pageHeight), chapter.getPagePool());
        eb.buildBlockList(blockList, node);
        eb.release();
        for (Block block : blockList) {
            if (chapter.isCancelBuildPage()) {
                return;
            }
            if (!TextUtils.isEmpty(block.id)) {
                chapter.addIdLocation(block.id, paraIndex);
                if (chapter.isTocId(block.id)) {
                    tocId = block.id;
                }
            }
            block.addParaIndex(paraIndex);
            chapter.addBlock(paraIndex, block);
            chapter.addTocId(paraIndex, tocId);
            ++paraIndex;
        }
    }
    
    

    private void layoutJustifyLine(Block block, PageLine line, Page page) {
        RectF rect = line.rect;
        float letterSpace = 0.0f;
        float lineWidth = line.getLineWidth() <= 0 ? pageWidth :line.getLineWidth();
        // elementlist could be only 1, in this case, letterSpace is 0
        if (!block.isOrderedLine() && !block.isUnorderedLine()) {
            if (!line.isLastLine() && line.elementList.size() > 1) {
                float extraSpace = lineWidth - rect.width() - line.getMarginLeft()
                        - line.getMarginRight() - line.getPaddingLeft()
                        - line.getPaddingRight() - line.getLineIndent();
                letterSpace = extraSpace / (float) (line.elementList.size() - 1);
            }
        }
        float x = rect.left + line.getPaddingLeft() + line.getLineIndent();
        float y = rect.top;
        int index = 0;
        PageLink currentPageLink = null;
        PagePool pagePool = page.getChapter().getPagePool();
        for (Element e : line.elementList) {
            if (page.getChapter().isCancelBuildPage()) {
                return;
            }
            if (e instanceof ElementImage) {
                ElementImage picture = (ElementImage) e;
                page.addPicture(picture);
                if (picture.isInBlock()) {
                    Size size = e.measureSize(pageContext, basePath);
                    float ex = (lineWidth - size.width) / 2;
                    if (ex < 0) {
                        ex = 0;
                    }
                    float height = Math.max(size.height, rect.height());
                    RectF rectf = new RectF(ex, y, ex + size.width, y + height);
                    e.setRect(rectf);
                    x += lineWidth;
                    ++index;
                    continue;
                }
            } else if (e instanceof ElementText) {
                if (e.isLink()) {
                    if (currentPageLink == null || !currentPageLink.isSameLink(e.getlinkUUID())) {
                        currentPageLink = pagePool.acquirePageLink();
                        currentPageLink.initialize(e.getlinkUUID(), basePath);
                        page.addLink(currentPageLink);
                    }
                    currentPageLink.addElement((ElementText)e);
                }
            }
            Size size = e.measureSize(pageContext, basePath);
            RectF elementRect;
            if (index != line.elementList.size() - 1) {
                elementRect = new RectF(x, y, x + size.width + letterSpace, y + rect.height());
            } else {
                elementRect = new RectF(x, y, x + size.width, y + rect.height());
            }
            e.setRect(elementRect);
            x += (size.width + letterSpace);
            ++index;
        }
    }

    private void layoutLeftLine(Block block, PageLine line, Page page) {
        RectF rect = line.rect;
        float letterSpace = 0.0f;
        float lineWidth = line.getLineWidth() <= 0 ? pageWidth :line.getLineWidth();
        // elementlist could be only 1, in this case, letterSpace is 0
        // 有序和无序列表计算字间距逻辑与其他的情况不相同，所以暂不计算字间距，案例:《钓到一条幸福鱼》目录
        if (!block.isOrderedLine() && !block.isUnorderedLine()) {
            if (!line.isLastLine() && line.elementList.size() > 1) {
                float extraSpace = lineWidth - rect.width()
                        - line.getMarginLeft() - line.getMarginRight()
                        - line.getPaddingLeft() - line.getPaddingRight()
                        - line.getLineIndent();
                letterSpace = extraSpace / (float) (line.elementList.size() - 1);
            }
        }
        float x = rect.left + line.getPaddingLeft() + line.getLineIndent();
        float y = rect.top;
        int index = 0;
        PageLink currentPageLink = null;
        PagePool pagePool = page.getChapter().getPagePool();
        for (Element e : line.elementList) {
            if (page.getChapter().isCancelBuildPage()) {
                return;
            }
            if (e instanceof ElementImage) {
                ElementImage picture = (ElementImage) e;
                page.addPicture(picture);
            } else if (e instanceof ElementText){
                if (e.isLink()) {
                    if (currentPageLink == null || !currentPageLink.isSameLink(e.getlinkUUID())) {
                        currentPageLink = pagePool.acquirePageLink();
                        currentPageLink.initialize(e.getlinkUUID(), basePath);
                        page.addLink(currentPageLink);
                    }
                    currentPageLink.addElement((ElementText)e);
                }
            }
            Size size = e.measureSize(pageContext, basePath);
            RectF elementRect = null;
            if (index != line.elementList.size() - 1) {
                elementRect = new RectF(x, y, x + size.width + letterSpace, y + rect.height());
            } else {
                elementRect = new RectF(x, y, x + size.width, y + rect.height());
            }
            e.setRect(elementRect);
            x += (size.width + letterSpace);
            ++index;
        }
    }

    private void layoutCenterLine(Block block, PageLine line, Page page) {
        RectF rect = line.rect;
        float lineWidth = line.getLineWidth() <= 0 ? pageWidth :line.getLineWidth();
        float x = (lineWidth - rect.width() + line.getMarginLeft()
                - line.getMarginRight() + line.getPaddingLeft() - 
                line.getPaddingRight()) / 2f;
        if (block.isFloatRight()) {
            x += rect.left;
        }
        float y = rect.top;
        PageLink currentPageLink = null;
        PagePool pagePool = page.getChapter().getPagePool();
        for (Element e : line.elementList) {
            if (page.getChapter().isCancelBuildPage()) {
                return;
            }
            if (e instanceof ElementImage) {
                ElementImage picture = (ElementImage) e;
                page.addPicture(picture);
            } else if (e instanceof ElementText) {
                if (e.isLink()) {
                    if (currentPageLink == null || !currentPageLink.isSameLink(e.getlinkUUID())) {
                        currentPageLink = pagePool.acquirePageLink();
                        currentPageLink.initialize(e.getlinkUUID(), basePath);
                        page.addLink(currentPageLink);
                    }
                    currentPageLink.addElement((ElementText)e);
                }
            }
            Size size = e.measureSize(pageContext, basePath);
            RectF elementRect = new RectF(x, y, x + size.width, y + rect.height());
            e.setRect(elementRect);
            x += (size.width);
        }
    }

    private void layoutRightLine(Block block, PageLine line, Page page) {
        RectF rect = line.rect;
        float lineWidth = line.getLineWidth() <= 0 ? pageWidth :line.getLineWidth();
        float x = lineWidth - rect.width() + line.getMarginLeft()
                - line.getMarginRight() + line.getPaddingLeft()
                - line.getPaddingRight();
        if (block.isFloatRight()) {
            x += rect.left;
        }
        float y = rect.top;
        PageLink currentPageLink = null;
        PagePool pagePool = page.getChapter().getPagePool();
        for (Element e : line.elementList) {
            if (page.getChapter().isCancelBuildPage()) {
                return;
            }
            if (e instanceof ElementImage) {
                ElementImage picture = (ElementImage) e;
                page.addPicture(picture);
            } else if (e instanceof ElementText){
                if (e.isLink()) {
                    if (currentPageLink == null || !currentPageLink.isSameLink(e.getlinkUUID())) {
                        currentPageLink = pagePool.acquirePageLink();
                        currentPageLink.initialize(e.getlinkUUID(), basePath);
                        page.addLink(currentPageLink);
                    }
                    currentPageLink.addElement((ElementText)e);
                }
            }
            Size size = e.measureSize(pageContext, basePath);
            RectF elementRect = new RectF(x, y, x + size.width, y + rect.height());
            e.setRect(elementRect);
            x += (size.width);
        }
    }

    
    
    
    private float getBlockIndent(Block block) {
        if (block.elementList.size() > 0) {
            Element e = block.elementList.get(0);
            if (e instanceof ElementText) {
                return ((ElementText) e).getTextIndent();
            }
        }
        return 0;
    }
    
    protected void release() {
        cssCollection.release();
    }
    
    protected void setBasePath(String basePath) {
        this.basePath = basePath;
    }
    
    protected PageContext getPageContext() {
        return pageContext;
    }
    
    protected Paint getGlobalPaint() {
        return globalPaint;
    }
    
    public void setGlobalPaint(Paint globalPaint) {
        this.globalPaint = globalPaint;
    }

    protected float getPageWidth() {
        return pageWidth;
    }

    protected void setPageWidth(float pageWidth) {
        this.pageWidth = pageWidth;
    }

    protected void setPageHeight(float pageHeight) {
        this.pageHeight = pageHeight;
    }

    /**
     *  注意修改了字体大小，也会改变行间距
     * @param pxPerEm   像素
     */
    public void setBaseTextSize(float pxPerEm) {
        pageContext.setPxPerEm(pxPerEm);
        pageContext.lineSpace = pxPerEm * BookPageViewActivity.getPageLineSpace();
        pageContext.blockSpace = pxPerEm * BookPageViewActivity.getPageBlockSpace();
    }
    
    protected float getBaseTextSize() {
        return pageContext.getPxPerEm();
    }
    
    protected int getLineSpace() {
        return (int) pageContext.lineSpace;
    }
    
    protected int getBlockSpace() {
        return (int) pageContext.blockSpace;
    }
}
