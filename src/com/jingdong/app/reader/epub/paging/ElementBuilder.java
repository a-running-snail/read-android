package com.jingdong.app.reader.epub.paging;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.jingdong.app.reader.R;
import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.epub.css.CSS;
import com.jingdong.app.reader.epub.css.CSSCollection;
import com.jingdong.app.reader.epub.css.CSSFont;
import com.jingdong.app.reader.epub.parser.Kit42Node;
import com.jingdong.app.reader.user.LocalUserSetting;
import com.jingdong.app.reader.util.Log;
import com.spreada.utils.chinese.ZHConverter;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;

class ElementBuilder {

    static final String TAG = "ElementBuilder";
    private BuildingReceiver receiver = new BuildingReceiver();
    private CSSCollection cssCollection;
    private CSSCollection localCss;
    private Paint globalPaint;
    private float pxPerEm;
    private float tableWidth;
    private float tableCellSpacing;
    private Size pageSize;
    private PagePool pagePool;
    private String chapterTitle = null;
    private boolean isCancelBuild = false;
    private int tableRowNumber = 0;
    private int tableCellNumber = 0;
    private List<Float> tableCellWidth = new ArrayList<Float>();

    public ElementBuilder(CSSCollection cssCollection, Paint paint, float pxPerEm, Size pageSize, PagePool pagePool) {
        this.cssCollection = cssCollection;//样式
        globalPaint = paint;
        this.pxPerEm = pxPerEm;
        this.pageSize = pageSize;
        this.pagePool = pagePool;
        registerReceiver();
    }
    
    /**
     * 核心方法：组织段落信息列表
     * @param blockList 段落列表，为递归准备
     * @param node Dom树
     */
    void buildBlockList(List<Block> blockList/*段落列表，为递归准备*/, Kit42Node node/*Dom树*/) {
        if (isCancelBuild) {
            return;
        }
        isBRLabelChangeLine = false;
        if/*文本*/ (isTextTag(node)) {
        	processTextTag(blockList, node);//核心：此处很关键（里侧对每个节点进行处理，会对每个文字节点的文字进行计算宽度）
            return;
        } else if/*图片*/ (node.name.equalsIgnoreCase("img") || node.name.equalsIgnoreCase("image")) {
        	processImgTag(blockList, node);
            return;
        } else if /*DIV*/(node.name.equalsIgnoreCase("div") || node.name.equalsIgnoreCase("blockquote")) {
            processDivTagOrBlockquoteTag(blockList, node);
            return;
        } else if/*列表*/ (node.name.equalsIgnoreCase("ol") || node.name.equalsIgnoreCase("ul")) {
            processOlTagOrUlTag(blockList, node);
			return;
        } else if /*表格，解析难点*/(node.name.equalsIgnoreCase("table")) {
            buildTableBlock(blockList, node);
        } else if /*页面内置样式*/(node.name.equalsIgnoreCase("style")) {
            processStyleTag(node);
        } else if/*网页标题*/ (node.name.equalsIgnoreCase("title")) {
            processTitleTag(node);//此部分为第一个运行，除非没有title
        } else if /*Body节点*/(node.name.equalsIgnoreCase("body")) {
            processBodyTag();//由于body总是段落开始，此处会第二个运行
        }

        //循环子节点
        processChildNodes(blockList, node);
    }

    /**
     * 是否问文本Tag
     * @param node DOM节点
     * @return 是返回true，否返回false
     */
	private boolean isTextTag(Kit42Node node) {
		return node.name.equalsIgnoreCase("p") || node.name.equalsIgnoreCase("pre")
                || node.name.equalsIgnoreCase("h1") || node.name.equalsIgnoreCase("h2")
                || node.name.equalsIgnoreCase("h3") || node.name.equalsIgnoreCase("h4")
                || node.name.equalsIgnoreCase("h5") || node.name.equalsIgnoreCase("h6")
                || node.name.equalsIgnoreCase("li");
	}

    /**
     * 循环子节点
     * @param blockList 段落列表
     * @param node DOM节点
     */
	private void processChildNodes(List<Block> blockList, Kit42Node node) {
		//递归解析子节点
        for (Kit42Node child : node.children) {
            if (isCancelBuild) {
                return;
            }
            buildBlockList(blockList, child);
        }
	}

    /**
     * 处理文本相关段落，可能段落中含有图片、文字、超链接等
     * @param blockList
     * @param node
     */
	private void processTextTag(List<Block> blockList, Kit42Node node) {
		//组织文本段落
		Block blockListResult = buildTextBlock(node);//此处可能会递归处理
		if (!blockListResult.isEmpty() || node.name.equalsIgnoreCase("p")) {
			//添加进段落列表
		    blockList.add(blockListResult);
		} else {
		    pagePool.releaseBlock(blockListResult);
		}
	}

	/**
	 * 处理图片相关
	 * @param blockList
	 * @param node
	 */
	private void processImgTag(List<Block> blockList, Kit42Node node) {
		buildImageBlock(blockList, node);
	}

	/**
	 * 处理Div
	 * @param blockList
	 * @param node
	 */
	private void processDivTagOrBlockquoteTag(List<Block> blockList, Kit42Node node) {
		List<Block> divList = buildDivElementList(node);
		if (node.name.equalsIgnoreCase("div")) {
		    int hashcode = node.hashCode();
		    for (Block block : divList) {
		        block.addDivHashcode(hashcode);
		    }
		}
		blockList.addAll(divList);
	}

	/**
	 * 处理OL或者UL
	 * @param blockList
	 * @param node
	 */
	private void processOlTagOrUlTag(List<Block> blockList, Kit42Node node) {
		boolean isOrderedLine = node.name.equalsIgnoreCase("ol");
		boolean isUnorderedLine = node.name.equalsIgnoreCase("ul");
		String className = node.attributeMap.get("class");
		if (!"mz-footnote".equals(className)) {
		    int index = 1;
		    List<Block> divList = buildDivElementList(node);
		    for (Block block : divList) {
		        block.setOrderedLine(isOrderedLine);
		        block.setUnorderedLine(isUnorderedLine);
		        block.setOrderedIndex(index++);
		    }
		    blockList.addAll(divList);
		}
	}

	/**
	 * 处理Body
	 */
	private void processBodyTag() {
		//查找Body相关的样式信息
		cssCollection.findBodyPropertyMap();
		Map<String, String> bodyMap = cssCollection.getBodyPropertyMap();
		if (bodyMap != null && bodyMap.size() > 0) {
		    String fontFamily = bodyMap.get("font-family");
		    String fontStyle = bodyMap.get("font-style");
		    String fontWeight = bodyMap.get("font-weight");
		    CSSFont cssFont = cssCollection.getCSSFont(fontFamily);
		    if (cssFont != null) {
		        cssFont.generateTypeface(fontStyle, fontWeight);
		        cssFont.setTypeface(fontStyle, fontWeight, globalPaint);
		    } else {
		        Typeface bodyFontFace = CSS.getTypeface(bodyMap,
		                cssCollection.getFontMap());
		        if (bodyFontFace != null) {
		            globalPaint.setTypeface(bodyFontFace);
		        }
		    }
		}
	}

	/**
	 * 处理title标签
	 * @param node
	 */
	private void processTitleTag(Kit42Node node) {
		if (node.children.size() > 0) {
		    Kit42Node childNode = node.children.get(0);
		    chapterTitle = childNode.text;
		}
	}

	/**
	 * 处理Style
	 * @param node
	 */
	private void processStyleTag(Kit42Node node) {
		String text = node.attributeMap.get("type");
		if ("text/css".equalsIgnoreCase(text) && node.children.size() > 0) {
		    Kit42Node childNode = node.children.get(0);
		    localCss = new CSSCollection(null, childNode.text);//本地样式
		}
	}

	private void buildTableBlock(List<Block> blockList, Kit42Node node) {
		tableWidth = CSS.getWidth(node.attributeMap, pxPerEm, pageSize.width);
		tableCellSpacing = CSS.getTableCellSpacing(node.attributeMap, pxPerEm);
		List<Block> childDivList = buildDivElementList(node);
		if (tableCellSpacing > 0) {
		    for (Block block : childDivList) {
		        block.setTableCellSpacing(tableCellSpacing);
		    }
		} else {
		    for (Block block : childDivList) {
		        block.setTableCellSpacing(3);
		    }
		}
		blockList.addAll(childDivList);
	}

	private void buildImageBlock(List<Block> blockList, Kit42Node node) {
		//图片块处理
		Block imageBlock = pagePool.acquireBlock();
		imageBlock.initialize(buildCss(node), globalPaint, node.attributeMap.get("id"), pagePool);
		addElementWithImage(imageBlock.elementList, node, null, imageBlock.paint);
		blockList.add(imageBlock);
	}
    
    /**
     * 文本段落
     * @param pNode
     * @return
     */
    private Block buildTextBlock(Kit42Node pNode) {
        Block block = pagePool.acquireBlock();
        if (isCancelBuild) {
            return block;
        } 
        CSS css = buildCss(pNode);//样式
        //初始化
        block.initialize(css, globalPaint, pNode.attributeMap.get("id"), pagePool);
        //核心：此处会循环DOM子节点
        addElement(block.elementList, pNode, block.paint, block);
        return block;
    }

    /**
     * DIV的段落
     * @param divNode DOM节点
     * @return 段落列表
     */
    private List<Block> buildDivElementList(Kit42Node divNode) {
        List<Block> blockList = new ArrayList<Block>();
        Block currentInlineList = null;
        for (Kit42Node node : divNode.children) {
            if (isCancelBuild) {
                return blockList;
            }
            if (node.name.equalsIgnoreCase("p") || node.name.equalsIgnoreCase("h1") || node.name.equalsIgnoreCase("h2")
                    || node.name.equalsIgnoreCase("h3") || node.name.equalsIgnoreCase("h4")
                    || node.name.equalsIgnoreCase("h5") || node.name.equalsIgnoreCase("h6")
                    || node.name.equalsIgnoreCase("li") || node.name.equalsIgnoreCase("pre")) {
                currentInlineList = null;
                node.mergeAttribute(divNode.attributeMap);
                processTextTag(blockList, node);//处理文本节点
            } else if (node.name.equalsIgnoreCase("div") || node.name.equalsIgnoreCase("blockquote"))/*子div*/ {
                currentInlineList = null;
                buildSubDivElements(divNode, blockList, node);
            } else if (node.name.equalsIgnoreCase("ol") || node.name.equalsIgnoreCase("ul")) /*列表*/ {
                currentInlineList = buildOlOrUlTagElements(divNode, blockList, currentInlineList, node);
            } else if (node.name.equalsIgnoreCase("table")) {
                currentInlineList = null;
                tableCellWidth.clear();
                buildTableBlock(blockList, node);
            } else if (node.name.equalsIgnoreCase("thead")
                    || node.name.equalsIgnoreCase("tbody")
                    || node.name.equalsIgnoreCase("tfoot")) {
                List<Block> childDivList = buildDivElementList(node);//递归处理
                blockList.addAll(childDivList);
            } else if (node.name.equalsIgnoreCase("tr")) {
                currentInlineList = null;
                tableRowNumber++;
                tableCellNumber = 0;
                List<Block> childDivList = buildTrTagElements(node);
                blockList.addAll(childDivList);
            } else if (node.name.equalsIgnoreCase("th") || node.name.equalsIgnoreCase("td")) {
                currentInlineList = null;
                tableCellNumber++;
                List<Block> childDivList = buildThOrTdTagElements(node);
                blockList.addAll(childDivList);
            } else {
                if (currentInlineList == null) {
                    currentInlineList = pagePool.acquireBlock();
                    currentInlineList.initialize(buildCss(divNode), globalPaint, divNode.attributeMap.get("id"), pagePool);
                    blockList.add(currentInlineList);
                }

                if (node.name.equalsIgnoreCase("TEXT")) {//纯文本
                    addElementWithText(currentInlineList.elementList, node.text.trim(), divNode.attributeMap,currentInlineList.paint);
                } else if (node.name.equalsIgnoreCase("img") || node.name.equalsIgnoreCase("image")) {//图片
                    addElementWithImage(currentInlineList.elementList, node, divNode, currentInlineList.paint);
                } else if (node.name.equalsIgnoreCase("a")) {//超链接
                    isLink = true;
                    linkUUID = UUID.randomUUID().toString();
                    String id = node.attributeMap.get("id");
                    if (!TextUtils.isEmpty(id)) {
                        currentInlineList.id = id;
                    }
                    linkAttributeMap = node.attributeMap;
                    addElement(currentInlineList.elementList, node, currentInlineList.paint, currentInlineList);
                    isLink = false;
                    linkUUID = null;
                } else {//其他，例如li或者dd
                    addElement(currentInlineList.elementList, node, currentInlineList.paint, currentInlineList);
                }
                if (currentInlineList.isEmpty()) {
                    blockList.remove(currentInlineList);
                    pagePool.releaseBlock(currentInlineList);
                    currentInlineList = null;
                }
            }
        }
        return blockList;
    }

    /**
     * 处理子DIV段落列表
     * @param divNode DOM节点
     * @param blockList 段落列表
     * @param node DOM节点
     */
	private void buildSubDivElements(Kit42Node divNode, List<Block> blockList, Kit42Node node) {
		CSS css = buildCss(divNode);//样式
		node.mergeAttribute(css.getProperties());//合并样式
		List<Block> childDivList = buildDivElementList(node);//递归处理
		if (node.name.equalsIgnoreCase("div")) {
		    int hashcode = node.hashCode();
		    for (Block block : childDivList) {
		        block.addDivHashcode(hashcode);
		        if (css.isFloatLeft() || css.isFloatRight()) {
		            block.setFloatHashcode(hashcode);
		        }
		    }
		}
		blockList.addAll(childDivList);//段落列表
	}

	private Block buildOlOrUlTagElements(Kit42Node divNode, List<Block> blockList, Block currentInlineList,
			Kit42Node node) {
		boolean isOrderedLine = node.name.equalsIgnoreCase("ol");
		boolean isUnorderedLine = node.name.equalsIgnoreCase("ul");
		String className = node.attributeMap.get("class");
		if (!"mz-footnote".equals(className)) {
		    currentInlineList = null;
		    node.mergeAttribute(divNode.attributeMap);
		    List<Block> childDivList = buildDivElementList(node);
		    for (Block block : childDivList) {
		        block.setOrderedLine(isOrderedLine);
		        block.setUnorderedLine(isUnorderedLine);
		    }
		    blockList.addAll(childDivList);
		}
		return currentInlineList;
	}

	private List<Block> buildThOrTdTagElements(Kit42Node node) {
		float width = CSS.getWidth(node.attributeMap, pxPerEm, 0);
		if (width > 0) {
		    if (tableCellWidth.size() >= tableCellNumber) {
		        tableCellWidth.set(tableCellNumber-1, width);
		    } else {
		        tableCellWidth.add(width);
		    }
		} else {
		    if (tableCellWidth.size() >= tableCellNumber) {
		        width = tableCellWidth.get(tableCellNumber-1);
		    }
		}
		CSS css = buildCss(node.parent);
		node.mergeAttribute(css.getProperties());
		int rowSpan = CSS.getTableRowSpan(node.attributeMap);
		List<Block> childDivList = buildDivElementList(node);
		if (childDivList.isEmpty()) {
		    Block block = pagePool.acquireBlock();
		    block.initialize(buildCss(node), globalPaint, node.attributeMap.get("id"), pagePool);
		    childDivList.add(block);
		}
		for (Block block : childDivList) {
		    block.setTableRowNumber(tableRowNumber);
		    block.setTableCellNumber(tableCellNumber);
		    block.setTableCellWidth(width);
		    if (rowSpan > 0) {
		        block.setTableRowSpan(rowSpan);
		    }
		}
		return childDivList;
	}

	private List<Block> buildTrTagElements(Kit42Node node) {
		CSS css = buildCss(node.parent);
		node.mergeAttribute(css.getProperties());
		float width = CSS.getWidth(node.attributeMap, pxPerEm, tableWidth);
		List<Block> childDivList = buildDivElementList(node);
		if (tableCellWidth.size() <= 0 && width > 0) {
		    width = width / tableCellNumber;
		    for (Block block : childDivList) {
		        block.setTableCellWidth(width);
		    }
		}
		return childDivList;
	}

    boolean isLink = false;
    boolean isSpan = false;
    boolean isBold = false;
    boolean isBRLabelChangeLine = false;
    Map<String, String> linkAttributeMap = new HashMap<String, String>();
    Map<String, String> spanAttributeMap = new HashMap<String, String>();
    String linkUUID = null;

    /**
     * 处理段落元素
     * @param elementList
     * @param parent
     * @param paint
     * @param block
     */
    private void addElement(List<Element> elementList, Kit42Node parent, Paint paint, Block block) {
        for (Kit42Node node : parent.children) {
            if (isCancelBuild) {
                return;
            }
            if (node.name.equalsIgnoreCase("TEXT")) {//纯文本直接处理
                plainTextElement(elementList, parent, paint, block, node);
            } else if (node.name.equalsIgnoreCase("img") || node.name.equalsIgnoreCase("image")) {
                addElementWithImage(elementList, node, parent, paint);//图片节点
            } else if (node.name.equalsIgnoreCase("a")) {
                linkElement(elementList, paint, block, node);//超链接
            } else if (node.name.equalsIgnoreCase("b") || node.name.equalsIgnoreCase("strong")) {
                boldElement(elementList, paint, block, node);//加粗文本
            } else if (node.name.equalsIgnoreCase("br")) {
                isBRLabelChangeLine = true;//强制换行
            } else {
                addElement(elementList, node, paint, block);
            }
        }
    }

    /**
     * 加粗
     * @param elementList
     * @param paint
     * @param block
     * @param node
     */
	private void boldElement(List<Element> elementList, Paint paint, Block block, Kit42Node node) {
		isBold = true;
		addElement(elementList, node, paint, block);
		isBold = false;
	}

    /**
     * 处理超链接
     * @param elementList
     * @param paint
     * @param block
     * @param node
     */
	private void linkElement(List<Element> elementList, Paint paint, Block block, Kit42Node node) {
		isLink = true;//链接
		linkUUID = UUID.randomUUID().toString();
		String id = node.attributeMap.get("id");
		if (!TextUtils.isEmpty(id)) {
		    block.id = id;
		}
		linkAttributeMap = buildCssAttributeMap(node);
		linkAttributeMap.putAll(node.attributeMap);
		if (TextUtils.isEmpty(node.text) && (node.children == null || node.children.size() == 0)) {//超链接没有任何内容
		    addElementWithText(elementList, "", block.getCSS().getProperties(), paint);
		} else {
			//超链接需要递归下去
		    addElement(elementList, node, paint, block);
		}
		isLink = false;
		linkUUID = null;
	}

	/**
	 * 处理纯文本
	 * @param elementList
	 * @param parentNode
	 * @param paint
	 * @param block
	 * @param node
	 */
	private void plainTextElement(List<Element> elementList, Kit42Node parentNode, Paint paint, Block block,Kit42Node node) {
		if (isBRLabelChangeLine) {
		    node.text = "\n" + node.text.trim();//换行
		    isBRLabelChangeLine = false;
		}
		if (parentNode.name.equalsIgnoreCase("span")) {//span标签
		    isSpan = true;
		    spanAttributeMap = buildCssAttributeMap(parentNode);//处理样式
		    addElementWithText(elementList, node.text/*节点文本内容*/, block.getCSS().getProperties(), paint);
		    isSpan = false;
		} else {
		    addElementWithText(elementList, node.text/*节点文本内容*/, block.getCSS().getProperties(), paint);
		}
	}
    
    private void addElementWithText(List<Element> elementList, String textContent, Map<String, String> parentAttribute, Paint paint) {
        if (isCancelBuild) {
            return;
        }
        Map<String, String> attributeMap = new HashMap<String, String>();
        attributeMap.putAll(parentAttribute);
        int fontColor = 0;
        int highlightColor = 0;
        int backgroundColor = 0;
        float paddingLeft = 0;
        float paddingRight = 0;
        float borderRadius = 0;
        //颜色
        String color = attributeMap.get("color");
        //首行缩进
        float textIndent = CSS.getTextIndent(attributeMap, pxPerEm);
        //粗体
        boolean isBold = CSS.textIsBold(attributeMap);
        //斜体
        boolean isItalic = CSS.textIsItalic(attributeMap);
        //下划线
        boolean isUnderline = CSS.textHasUnderline(attributeMap);
        Paint textPaint = new Paint(paint);
        if (this.isLink) /*父节点为超链接*/ {
            isUnderline = true;
            attributeMap.putAll(linkAttributeMap);
            color = CSS.getTextColor(linkAttributeMap, color);
        }
        
        if (this.isSpan) /*父节点为Span*/ {
            isBold = CSS.textIsBold(spanAttributeMap);
            isItalic = CSS.textIsItalic(spanAttributeMap);
            if (CSS.hasTypeface(spanAttributeMap)) {
                Typeface typeface = CSS.getTypeface(spanAttributeMap, cssCollection.getFontMap());
                if (typeface != null) {
                    textPaint.setTypeface(typeface);
                }
            }
            if (CSS.textHasUnderline(spanAttributeMap)) {
                isUnderline = true;
            }
            color = CSS.getTextColor(spanAttributeMap, color);
            String bgColor = CSS.getBackgroundColor(spanAttributeMap);
            if (!TextUtils.isEmpty(bgColor)) {
                backgroundColor = cssCollection.getColor(bgColor);
            } else {
                backgroundColor = 0;
            }
            borderRadius = CSS.getBorderRadius(spanAttributeMap, pxPerEm);
            float[] padding = CSS.getPadding(spanAttributeMap, pxPerEm);
            if (padding != null) {
                paddingRight = padding[1] * BookPageViewActivity.getDensity();
                paddingLeft = padding[3] * BookPageViewActivity.getDensity();
            }
        }
        if (this.isBold) {
            isBold = true;
        }
        if (elementList.size() > 0) {
            textIndent = 0;
        }
        fontColor = cssCollection.getColor(color);
        
        highlightColor = MZBookApplication.getContext().getResources().getColor(R.color.r_theme);
        boolean isTraditional = LocalUserSetting.isTraditional(MZBookApplication.getContext());
        if (isTraditional) {
            textContent = ZHConverter.convert(textContent, ZHConverter.TRADITIONAL);
        }
        
        LineBreaker breaker = new LineBreaker("zh");//计算每个字的宽度
        String[] textArray = textContent.split("\n");//按照换行符切分
		float textSize = Math.round(textPaint.getTextSize());
        for (int x = 0; x < textArray.length ; x++) {
            if (isCancelBuild) {
                return;
            }
            String text = textArray[x];
            final byte[] breaks = new byte[text.length()];
            breaker.setLineBreaks(text, breaks);//进行断词，并计算每个文字的宽度
    
            float[] widths = new float[text.length()];
//            textPaint.getTextWidths(text, widths);
	        for( int i=0; i < text.length();++i){
	        	char c = text.charAt(i);
	        	 if((c >= 0x4e00)&&(c <= 0x9fbb)) {
	        	 	widths[i] = textSize;
	        	 }else {
	        		 float[] v = new float[1];
	        		 textPaint.getTextWidths(String.valueOf(c), v);
	        		 widths[i] = v[0];  
	        	 }	 	
	        }
            
            int startOffset = 0;
            int endOffset = 1;
            float width = 0;
            
            for (int i = 0; i < breaks.length; ++i) {
                if (isCancelBuild) {
                    return;
                }
                if (breaks[i] == LineBreaker.AllowBreak || breaks[i] == LineBreaker.MustBreak) {
                    width = 0;
                    endOffset = i + 1;
                    for (int n = startOffset; n < endOffset; n++) {
                        width += widths[n];
                    }
                    
                    ElementText element = new ElementText();
                    element.initialize(text, startOffset, endOffset, attributeMap, textPaint);
                    if (startOffset == 0) {
                        element.setTextIndent(textIndent);
                        element.setPaddingLeft(paddingLeft);
                        element.setBorderRadiusLeft(borderRadius);
                    }
                    if (endOffset == text.length()) {
                        element.setPaddingRight(paddingRight);
                        element.setBorderRadiusRight(borderRadius);
                    }
                    element.setWidth(width);
                    element.setBold(isBold);
                    element.setItalic(isItalic);
                    element.setIsLink(isLink);
                    element.setlinkUUID(linkUUID);
                    element.setUnderline(isUnderline);
                    element.setFontColor(fontColor);
                    element.setHighlightColor(highlightColor);
                    element.setBackgroundColor(backgroundColor);
                    startOffset = endOffset;
                    elementList.add(element);
                }
            }
            
            if (textArray.length > 1 && x < textArray.length - 1) {
                ElementText endText = new ElementText();
                endText.initialize("\n", 0, 1, null, paint);
                elementList.add(endText);
            }
        }
    }
    
    /**
     * 添加图片段落
     * @param elementList 元素列表
     * @param node DOM节点
     * @param parent DOM节点的父级元素
     * @param paint
     */
    private void addElementWithImage(List<Element> elementList, Kit42Node node, Kit42Node parent, Paint paint) {
        if (isCancelBuild) {
            return;
        }
        Map<String, String> attributeMap = buildCssAttributeMap(node);
        attributeMap.putAll(node.attributeMap);
        if (isLink) {
            attributeMap.putAll(linkAttributeMap);
        }
        String id = parent != null ? parent.attributeMap.get("id") : null;
        Size parentSize = getParentSize(parent);
        int parentFloat = getParentFloat(parent);
        ElementImage element = pagePool.acquireElementImage();
        element.initialize(attributeMap, paint, pageSize, parentSize, pxPerEm);
        element.setParentFloat(parentFloat);
        element.setIsLink(isLink);
        element.setlinkUUID(linkUUID);
        if (!TextUtils.isEmpty(id)) {
            element.setAnchorId(id);
        }
        elementList.add(element);
    }
    
    private Size getParentSize(Kit42Node parent) {
        float parentWidth = 0;
        float parentHeight = 0;
        if (parent == null) {
            return new Size(parentWidth, parentHeight);
        }
        Map<String, String> parentAttributeMap = buildCssAttributeMap(parent);
        String width = null;
        String height = null;
        if (!parentAttributeMap.isEmpty()) {
            width = parentAttributeMap.get("width");
            height = parentAttributeMap.get("height");
        }
        if (TextUtils.isEmpty(width) && TextUtils.isEmpty(height)) {
            return getParentSize(parent.parent);
        } else {
            parentWidth = CSS.getWidth(parentAttributeMap, pxPerEm, pageSize.width);
            parentHeight = CSS.getHeight(parentAttributeMap, pxPerEm, pageSize.height);
        }
        
        return new Size(parentWidth, parentHeight);
    }
    
    /**
     * 解析父亲节点的float
     * @param parent
     * @return  0  no float
     *          1  left float
     *          2  right float
     */
    private int getParentFloat(Kit42Node parent) {
        int parentFloat = 0;
        if (parent == null) {
            return parentFloat;
        }
        Map<String, String> parentAttributeMap = buildCssAttributeMap(parent);

        if (CSS.isFloatLeft(parentAttributeMap)) {
            parentFloat = 1;
        } else if (CSS.isFloatRight(parentAttributeMap)) {
            parentFloat = 2;
        } else {
            return getParentFloat(parent.parent);
        }

        return parentFloat;
    }

    private CSS buildCss(Kit42Node node) {
        CSS css = pagePool.acquireCSS();
        Map<String, String> attributeMap = new HashMap<String, String>();
        Map<String, String> bodyMap = cssCollection.getBodyPropertyMap();
        if (bodyMap != null && bodyMap.size() > 0) {
            attributeMap.putAll(bodyMap);//页面级样式
        }
        if (node.parent != null && "div".equalsIgnoreCase(node.parent.name)) {
        	//添加父节点样式
            attributeMap.putAll(buildCssAttributeMap(node.parent));
            //添加当前节点样式
            attributeMap.putAll(buildCssAttributeMap(node));
            css.initialize(attributeMap, cssCollection.getFontMap(), pxPerEm);
        } else {
            attributeMap.putAll(buildCssAttributeMap(node));
            css.initialize(attributeMap, cssCollection.getFontMap(), pxPerEm);
        }
        return css;
    }

    /**
     * 处理CSS样式属性信息
     * @param node
     * @return
     */
    private Map<String, String> buildCssAttributeMap(Kit42Node node) {
        Map<String, String> attributeMap = new HashMap<String, String>();
        if (node == null) {
            return attributeMap;
        }

        //查找标签的统一样式（CSS中，直接标签名的样式）
        Map<String, String> selectorMap = cssCollection.getPropertyMap(node.name);
        if (selectorMap != null) {
            attributeMap.putAll(selectorMap);
        }
        
        //本地样式
        if (localCss != null) {
            Map<String, String> localMap = localCss.getPropertyMap(node.name);
            if (localMap != null) {
                attributeMap.putAll(localMap);
            }
        }

        String className = node.attributeMap.get("class");
        //查找class的对应的样式
        prepareClassProperties(className, node, attributeMap, cssCollection);
        prepareClassProperties(className, node, attributeMap, localCss);

        //行内样式（直接写在标签属性style上）
        String style = node.attributeMap.get("style");
        if (style != null) {
            style = style.trim();
            if (!style.endsWith(";")) {//对样式进行切分
                style += ";";
            }
            if (style.indexOf("{") <= 0) {
                if (!style.startsWith("{")) {
                    style = "name{" + style;
                } else {
                    style = "name" + style;
                }
            }
            if (!style.endsWith("}")) {
                style += "}";
            }
            try {
            	//解析样式
                InputSource source = new InputSource(new StringReader(style));
                CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
                CSSStyleSheet sheet = parser.parseStyleSheet(source, null, null);
                CSSRuleList list = sheet.getCssRules();
                for (int i = 0, l = list.getLength(); i < l; i++) {
                    CSSRule rule = list.item(i);
                    if (rule instanceof CSSStyleRule) {
                        CSSStyleRule styleRule = (CSSStyleRule) rule;
                        CSSStyleDeclaration declaration = styleRule.getStyle();
                        for (int n = 0; n < declaration.getLength(); n++) {
                            if (isCancelBuild) {
                                return attributeMap;
                            }
                            String property = declaration.item(n);
                            attributeMap.put(property.toLowerCase(),declaration.getPropertyValue(property));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        for (String key : node.attributeMap.keySet()) {
            if (attributeMap.containsKey(key)) {
                String value = attributeMap.get(key);
                if ("inherit".equalsIgnoreCase(value)) {
                    attributeMap.put(key, node.attributeMap.get(key));
                }
                continue;
            } else {
                attributeMap.put(key, node.attributeMap.get(key));
            }
        }
        return attributeMap;
    }
    
    /**
     * 获取Dom节点的样式
     * @param className 样式类名
     * @param node 节点
     * @param attributeMap 属性哈希表
     * @param cssCollection 样式信息集合
     */
    private void prepareClassProperties(String className, Kit42Node node, Map<String, String> attributeMap, CSSCollection cssCollection) {
        try{
            if (!TextUtils.isEmpty(className) && cssCollection != null) {
            	//可能存在多个class，需要进行切分
                TextUtils.StringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
                splitter.setString(className);
                for (String s : splitter) {//查找每一个单独的class样式
                    if (isCancelBuild) {
                        return;
                    }
                    String str = s.trim();//去掉class两边空白，避免取不到样式信息
                    if (!TextUtils.isEmpty(str)) {
                    	//取独立class样式
                        Map<String, String> globalClassMap = cssCollection.getPropertyMap("." + str);
                        if (globalClassMap != null) {
                            attributeMap.putAll(globalClassMap);
                        }

                        //取标签名+class名的样式
                        Map<String, String> classMap = cssCollection.getPropertyMap(node.name + "." + str);
                        if (classMap != null) {
                            attributeMap.putAll(classMap);
                        }
                        
                        //取与父节点class组合的样式
                        if (node.parent != null) {
                            String parentClass = node.parent.attributeMap.get("class");
                            if (!TextUtils.isEmpty(parentClass)) {
                                Map<String, String> map = cssCollection.getPropertyMap("." + parentClass + " *." + str);//CSS parser解析后会加一个*，所以这里有*
                                if (map != null) {
                                    attributeMap.putAll(map);
                                }
                            }
                        }
                        
                    }
                }
            }
        }catch(ConcurrentModificationException e){
            e.printStackTrace();
        }
    }
    
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BookPageViewActivity.ACTION_CANCEL_BUILDING);
        LocalBroadcastManager.getInstance(MZBookApplication.getContext()).registerReceiver(receiver,
                filter);
    }
    
    private void unregisterReceiver() {
        LocalBroadcastManager.getInstance(MZBookApplication.getContext()).unregisterReceiver(receiver);
    }
    
    void release() {
        if (localCss != null) {
            //清理释放局部的css不影响全局的css
            localCss.release();
        }
        unregisterReceiver();
    }
    
    public String getTitle() {
        return chapterTitle;
    }
    
    class BuildingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BookPageViewActivity.ACTION_CANCEL_BUILDING)) {
                isCancelBuild = true;
            }
        }
    }
}
