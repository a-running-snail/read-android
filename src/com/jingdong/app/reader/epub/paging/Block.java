package com.jingdong.app.reader.epub.paging;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.epub.css.CSS;
import com.jingdong.app.reader.reading.ReadSearchData;
import com.jingdong.app.reader.util.StringUtil;

import android.graphics.Paint;
import android.text.TextUtils;

public class Block {
    private static final int MAX_PREFIX_LENGTH = 10;
    private static final int MAX_QUOTE_LENGTH = 50;
    List<Element> elementList = new CopyOnWriteArrayList<Element>();
    List<Integer> divHashcode = new CopyOnWriteArrayList<Integer>();//div hashcode list
    private CSS css;
    Paint paint;
    String id;
    PagePool pagePool;
    private float imageFloatMarginLeft = 0;
    private float imageFloatMarginRight = 0;
    private float blockFloatHeight = 0;
    private float marginLeft = 0;
    private float marginRight = 0;
    private float marginTop = 0;
    private float marginBottom = 0;
    private float paddingTop = 0;
    private float paddingBottom = 0;
    private float tableCellWidth = 0;
    private float tableCellSpacing = 0;
    private int floatHashcode = 0;
    private int orderedIndex = 0;
    private int paraIndex = 0;
    private int tableRowSpan = 0;
    private int tableRowNumber = 0;//从1开始
    private int tableCellNumber = 0;//从1开始
    private boolean isFloatLeft = false;
    private boolean isFloatRight = false;
    private boolean isOrderedLine = false;
    private boolean isUnorderedLine = false;
    private boolean isDisplayHidden = false;
    private boolean isForcePageBreakAfter = false;
    private boolean isForcePageBreakBefore = false;
    private boolean isKeywordsHighlight = false;
    
    public Block() {
    }
    
    /**
     * 初始化
     * @param css
     * @param globalPaint
     * @param id
     * @param pagePool
     */
    void initialize(CSS css, Paint globalPaint, String id, PagePool pagePool) {
        this.css = css;//样式
        paint = pagePool.acquirePaint();
        paint.set(globalPaint);
        this.id = id;
        this.pagePool = pagePool;
        divHashcode.clear();
        paddingTop = 0;
        paddingBottom = 0;
        tableCellWidth = 0;
        tableCellSpacing = 0;
        tableRowNumber = 0;
        tableCellNumber = 0;
        imageFloatMarginLeft = 0;
        imageFloatMarginRight = 0;
        blockFloatHeight = 0;
        floatHashcode = 0;
        orderedIndex = 0;
        isFloatLeft = false;
        isFloatRight = false;
        isOrderedLine = false;
        isUnorderedLine = false;
        isKeywordsHighlight = false;
        prepareBlockProperties();
    }

    CSS getCSS() {
        return css;
    }
    
    void mergeCSS(CSS css) {
        this.css.mergePorperties(css.getProperties());
    }
    
    /**
     * 初始化块的属性信息
     */
    void prepareBlockProperties() {
    	//文字大小
        paint.setTextSize(css.getFontSize());
        //是否粗体
        paint.setFakeBoldText(css.textIsBold());
        //字体
        if (css.getTypeface() != null) {
            paint.setTypeface(css.getTypeface());
        } else {
            paint.setTypeface(BookPageViewActivity.fontDefault);
        }
        paint.setStrikeThruText(css.textHasStrikeThru());
        if (css.textIsItalic()) {
            paint.setTextSkewX(-0.2f);
        }
        float[] margin = css.getMargin();
        if (margin != null) {
            marginTop = margin[0];
            marginRight = margin[1];
            marginBottom = margin[2];
            marginLeft = margin[3];
        } else {
            marginTop = css.getMarginTop();
            marginRight = css.getMarginRight();
            marginBottom = css.getMarginBottom();
            marginLeft = css.getMarginLeft();
        }
        isDisplayHidden = css.isDisplayHidden();
        isForcePageBreakBefore = css.isForcePageBreakBefore();
        isForcePageBreakAfter = css.isForcePageBreakAfter();
        tableRowSpan = css.getTableRowSpan();
        isFloatLeft = css.isFloatLeft();
        isFloatRight = css.isFloatRight();
    }
    
    void addParaIndex(int paraIndex) {
        this.paraIndex = paraIndex;
        int offsetInPara = 0;
        for (Element element : elementList) {
            element.setChapterLocation(paraIndex, offsetInPara);
            offsetInPara += element.getCount();
        }
    }

    boolean isImageBlock() {
        if (elementList.size() == 1 && elementList.get(0) instanceof ElementImage) {
            return true;
        } else {
            return false;
        }
    }

    ElementImage getImageElement() {
        ElementImage image = (ElementImage) elementList.get(0);
        if (this.isTableCell()) {
            //FIXME table cell not support image
            image.hideImage();
        }
        return image;
    }
    
    void release() {
        isKeywordsHighlight = false;
        pagePool.releaseCSS(css);
        pagePool.releasePaint(paint);
        for (Element element : elementList) {
            if (element instanceof ElementImage) {
                pagePool.releaseElementImage((ElementImage)element);
            } else if (element instanceof ElementText) {
                ((ElementText)element).initialize(null, 0, 0, null, null);
            }
        }
        elementList.clear();
        paint = null;
        css = null;
    }

    public float getImageFloatMarginLeft() {
        return imageFloatMarginLeft;
    }

    public void setImageFloatMarginLeft(float imageFloatMarginLeft) {
        this.imageFloatMarginLeft = imageFloatMarginLeft;
    }

    public float getImageFloatMarginRight() {
        return imageFloatMarginRight;
    }

    public void setImageFloatMarginRight(float imageFloatMarginRight) {
        this.imageFloatMarginRight = imageFloatMarginRight;
    }

    public float getBlockFloatHeight() {
        return blockFloatHeight;
    }

    public void setBlockFloatHeight(float blockFloatHeight) {
        this.blockFloatHeight = blockFloatHeight;
    }

    public int getFloatHashcode() {
        return floatHashcode;
    }

    public void setFloatHashcode(int hashcode) {
        if (this.floatHashcode == 0) {
            this.floatHashcode = hashcode;
        }
    }

    public boolean isFloatLeft() {
        return isFloatLeft;
    }

    public boolean isFloatRight() {
        return isFloatRight;
    }

    public boolean isOrderedLine() {
        return isOrderedLine;
    }

    public void setOrderedLine(boolean isOrderedLine) {
        this.isOrderedLine = isOrderedLine;
    }

    public boolean isUnorderedLine() {
        return isUnorderedLine;
    }

    public void setUnorderedLine(boolean isUnorderedLine) {
        this.isUnorderedLine = isUnorderedLine;
    }

    public int getOrderedIndex() {
        return orderedIndex;
    }

    public void setOrderedIndex(int orderedIndex) {
        this.orderedIndex = orderedIndex;
    }

    public int getParaIndex() {
        return paraIndex;
    }

    public float getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(float paddingTop) {
        this.paddingTop = paddingTop;
    }

    public float getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(float paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public float getMarginRight() {
        return marginRight;
    }

    public float getMarginTop() {
        return marginTop;
    }

    public float getMarginBottom() {
        return marginBottom;
    }

	/**
	 * NOTE: 笔记分享专用
	 * 从开头一直清理到Offset
	 * @param offset 偏移位置
	 */
	public void clearElementToOffset(int offset) {
		if (elementList == null) {
			return;
		}
		for (int i = 0; i < offset;) {
			Element element = elementList.remove(i);
			if (element instanceof ElementImage) {
				pagePool.releaseElementImage((ElementImage) element);
				offset --;
			} else if (element instanceof ElementText) {
				ElementText text = (ElementText) element;
				offset -= text.getCount();
				text.initialize(null, 0, 0, null, null);
			}
		}
	}
	
	/**
	 * NOTE: 笔记分享专用
	 * 从Offset一直清理到最后
	 * @param offset 偏移位置
	 */
	public void clearElementFromOffset(int offset) {
		if (elementList == null) {
			return;
		}
		
		int index = 0;
		for (int i = 0, n = elementList.size(); i < n; i++) {
			if (index >= offset) {
				index = i;
				break;
			}
			Element element = elementList.get(i);
			if (element instanceof ElementImage) {
				index++;
			} else if (element instanceof ElementText) {
				ElementText text = (ElementText) element;
				index += text.getCount();
			}
		}
		
		for (int n = elementList.size(); index < n; n--) {
			Element element = elementList.remove(index);
			if (element instanceof ElementImage) {
				pagePool.releaseElementImage((ElementImage) element);
			} else if (element instanceof ElementText) {
				ElementText text = (ElementText) element;
				text.initialize(null, 0, 0, null, null);
			}
		}
	}
	
	public List<ReadSearchData> searchWords(String keywords, int chapterIndex, int paraIndex, String pageHead) {
		List<ReadSearchData> resultList = new ArrayList<ReadSearchData>();
		StringBuffer buffer = new StringBuffer();
		for (Element element : elementList) {
			if (element instanceof ElementText) {
				ElementText text = ((ElementText) element);
				if (text.isHyphenated()) {
					continue;
				}
				buffer.append(text.getContent());
			}
		}
		String group = null;
		String result = null;
		String text = buffer.toString();
		String words = StringUtil.escapeExprSpecialWord(keywords);
		String regex = ".*?"+words;
		Pattern pattern = Pattern.compile(regex,Pattern.CASE_INSENSITIVE);   
        Matcher matcher = pattern.matcher(text);
        int keywordLen = keywords.length();
        int prefixLen = MAX_PREFIX_LENGTH;
        int start = -MAX_QUOTE_LENGTH;
        int end = -MAX_QUOTE_LENGTH;
        int offset = 0;
        while (matcher.find()) {
            group = matcher.group();
            if (matcher.start() - start < prefixLen) {
                continue;
            }
            start = matcher.start();
            end = matcher.end();
            
            if (group.length() - keywordLen > prefixLen) {
                if (end + (MAX_QUOTE_LENGTH - keywordLen - prefixLen) < text.length()) {
                    result = "...";
                    result += text.substring(end - keywordLen - prefixLen, end);
                    result += text.substring(end, end+(MAX_QUOTE_LENGTH - keywordLen - prefixLen));
                    result += "...";
                } else {
                    if (text.length() > MAX_QUOTE_LENGTH) {
                        result = "...";
                        result += text.substring(text.length() - MAX_QUOTE_LENGTH, text.length());
                    } else {
                        result = text;
                    }
                }
            } else {
                if (end - keywordLen - prefixLen > 0) {
                    if (end + MAX_QUOTE_LENGTH - keywordLen - prefixLen < text.length()) {
                        result = "...";
                        result += text.substring(end - keywordLen - prefixLen, end);
                        result += text.substring(end, end+(MAX_QUOTE_LENGTH - keywordLen - prefixLen));
                        result += "...";
                    } else {
                        if (end - MAX_QUOTE_LENGTH > 0) {
                            result = "...";
                            result += text.substring(end - MAX_QUOTE_LENGTH, end);
                        } else {
                            result = text;
                        }
                    }
                } else {
                    if (text.length() > start + MAX_QUOTE_LENGTH) {
                        if (start == 0) {
                            result = "";
                        } else {
                            result = "...";
                        }
                        result += text.substring(start, start + MAX_QUOTE_LENGTH);
                        result += "...";
                    } else {
                        result = text;
                    }
                }
            }
            if (resultList.size() > 0) {
                //重复文字的过滤
                ReadSearchData rsd = resultList.get(resultList.size() - 1);
                if (rsd != null && rsd.getSearchText().contains(group)) {
                    int endOffset = rsd.getEndOffsetInPara();
                    if (endOffset < end) {
                        rsd.addOffset(end - keywordLen);
                        rsd.setEndOffsetInPara(endOffset + group.length());
                    }
                    continue;
                }
            }
            offset = end - keywordLen;
            ReadSearchData data = new ReadSearchData();
            data.setTitle(pageHead);
            data.setSearchText(filterRedundantCharacter(result, keywords));
            data.setParaIndex(paraIndex);
            data.setStartOffsetInPara(offset);
            data.setChapterIndex(chapterIndex);
            String[] array = text.substring(offset).split(words);
            data.addOffset(offset);
            int endOffset = keywords.length();
            for (int i = 1; i < array.length - 1; i++) {
                endOffset += array[i].length();
                data.addOffset(offset + endOffset);
                endOffset += keywords.length();
            }
            if (array.length > 0) {
                if (!TextUtils.isEmpty(array[array.length - 1])
                        && result.endsWith(array[array.length - 1] + keywords)) {
                    endOffset += array[array.length - 1].length();
                    data.addOffset(offset + endOffset);
                    endOffset += keywords.length();
                }
            }
            data.setEndOffsetInPara(offset + endOffset);
            resultList.add(data);
        }
        return resultList;
    }
    
    private String filterRedundantCharacter(String text, String keyword) {
        if (TextUtils.isEmpty(text)) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        String[] textArray = text.split("\n");
        if (textArray.length > 1) {
            for (int i = 0, n = 0; i < textArray.length; i++) {
                if (textArray[i].contains(keyword)) {
                    if (n > 0) {
                        buffer.append("\n");
                    }
                    buffer.append(textArray[i]);
                    n++;
                }
            }
            return buffer.toString();
        } else {
            return text;
        }
    }

    public boolean isForcePageBreakAfter() {
        return isForcePageBreakAfter;
    }

    public void setForcePageBreakAfter(boolean isForcePageBreakAfter) {
        this.isForcePageBreakAfter = isForcePageBreakAfter;
    }

    public boolean isForcePageBreakBefore() {
        return isForcePageBreakBefore;
    }

    public void setForcePageBreakBefore(boolean isForcePageBreakBefore) {
        this.isForcePageBreakBefore = isForcePageBreakBefore;
    }

    public boolean isDisplayHidden() {
        return isDisplayHidden;
    }

    public int getTableRowNumber() {
        return tableRowNumber;
    }

    public void setTableRowNumber(int tableRowNumber) {
        this.tableRowNumber = tableRowNumber;
    }

    public int getTableCellNumber() {
        return tableCellNumber;
    }

    public void setTableCellNumber(int tableCellNumber) {
        this.tableCellNumber = tableCellNumber;
    }
    
    public boolean isTableCell() {
        return tableCellNumber > 0;
    }

    public float getTableCellWidth() {
        if (isImageBlock()) {
            //FIXME table cell not support image
            return 0;
        }
        return tableCellWidth;
    }

    public void setTableCellWidth(float tableCellWidth) {
        this.tableCellWidth = tableCellWidth;
    }

    public int getTableRowSpan() {
        return tableRowSpan;
    }

    public void setTableRowSpan(int tableRowSpan) {
        this.tableRowSpan = tableRowSpan;
    }
    
    public boolean isTableRowSpan() {
        return tableRowSpan > 0;
    }

    public float getTableCellSpacing() {
        return tableCellSpacing;
    }

    public void setTableCellSpacing(float tableCellSpacing) {
        this.tableCellSpacing = tableCellSpacing;
    }

    public boolean isDivBlock() {
        return divHashcode != null && divHashcode.size() > 0;
    }

    public void addDivHashcode(int hashcode) {
        this.divHashcode.add(hashcode);
    }
    
    public int getDivHashcode() {
        if (divHashcode != null && divHashcode.size() > 0) {
            return divHashcode.get(divHashcode.size() - 1).intValue();
        }
        return 0;
    }
    
    public String getDivHashcodeString() {
        if (divHashcode != null && divHashcode.size() > 0) {
            StringBuffer buffer = new StringBuffer();
            for (Integer code : divHashcode) {
                buffer.append(code);
                if (divHashcode.indexOf(code) < divHashcode.size() - 1) {
                    buffer.append("-");
                }
            }
            return buffer.toString();
        }
        return null;
    }
    
    public boolean isDivFloatBlock() {
        return isDivBlock() && (isFloatLeft || isFloatRight);
    }

    public List<Element> getElementList() {
        return elementList;
    }
    
    public boolean isEmpty() {
        return elementList == null || elementList.size() <= 0;
    }
    
    public int getContentCount() {
        int count = 0;
        if (elementList != null) {
            for (Element e : elementList) {
                count += e.getCount();
            }
        }
        return count;
    }
    
    public void searchHighlightElement(String keywords, String filterSpecialWords, ReadSearchData data) {
        List<Integer> offsetList = data.getOffsetList();
        StringBuffer buffer = new StringBuffer();
        if (elementList != null) {
            boolean isHighlight = false;
            boolean isHightlightEnd = false;
            int offset = 0;
            int imgCount = 0;
            int textOffset = 0;
            int keywordsIndex = 0;
            int keywordsLength = keywords.length();
            int searchOffset = offsetList.get(keywordsIndex).intValue();
            for (Element e : elementList) {
                if (e instanceof ElementText) {
                    ElementText et = (ElementText) e;
                    buffer.append(e.getContent());
                    if (isHighlight && isHightlightEnd) {
                        isHighlight = false;
                        isHightlightEnd = false;
                        if (keywordsIndex < offsetList.size() - 1) {
                            do {
                                if (keywordsIndex < offsetList.size() - 1) {
                                    keywordsIndex ++;
                                    searchOffset = offsetList.get(keywordsIndex).intValue();
                                } else {
                                    break;
                                }
                            } while(offset > searchOffset);
                        } else {
                            break;
                        }
                    }
                    if ((offset >= searchOffset && offset < searchOffset + keywordsLength)
                            || (offset < searchOffset && offset + et.getCount() > searchOffset)) {
                        isKeywordsHighlight = true;
                        textOffset = et.offsetInPara - imgCount;
                        if (textOffset <= searchOffset) {
                            et.setHlStartOffset(searchOffset);
                        } else {
                            et.setHlStartOffset(textOffset);
                        }
                        if (searchOffset + keywordsLength > textOffset + et.getCount()) {
                            et.setHlEndOffset(textOffset + et.getCount());
                        } else {
                            et.setHlEndOffset(searchOffset + keywordsLength);
                            isHightlightEnd = true;
                        }
                        et.setMatchSearchText(buffer.toString(), keywords, filterSpecialWords);
                        isHighlight = true;
                    }
                    offset += e.getCount();
                } else {
                    imgCount += e.getCount();
                }
            }
        }
    }
    
    public void clearKeywordsHighlight() {
        if (isKeywordsHighlight) {
            isKeywordsHighlight = false;
            for (Element e : elementList) {
                if (e instanceof ElementText) {
                    ElementText text = (ElementText) e;
                    text.setFilterSpecialText(null);
                    text.setMultiMatchText(null);
                    text.setHlStartOffset(-1);
                    text.setHlEndOffset(-1);
                }
            }
        }
    }
}
