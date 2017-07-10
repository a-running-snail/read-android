package com.jingdong.app.reader.epub.paging;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.text.TextUtils;
import com.jingdong.app.reader.activity.BookPageViewActivity;

/**
 * 文本元素
 *
 */
public class ElementText extends Element {
    Size size;
    private String text;
    private String fullText;
    private String footnoteId;
    private String multiMatchText;
    private String filterSpecialText;//过滤掉特殊字符的搜索关键词
    private int fontColor;
    private int startOffset;
    private int endOffset;
    private int highlightColor;
    private int backgroundColor;
    private int hlStartOffset;
    private int hlEndOffset;
    private float width;
    private float textIndent;
    private float hyphenWidth;
    private float extraWidth;
    private float paddingLeft;
    private float paddingRight;
    private float borderRadiusLeft;
    private float borderRadiusRight;
    private boolean isBold;
    private boolean isItalic;
    private boolean isUnderline;
    private boolean isHyphenated;
    private boolean isHyphenateText;
    private boolean isJumpAction;
    private boolean isCloned;
    private ElementText hyphenText;
    private Map<String, String> attributeMap;
    
    public ElementText() {
        super(null);
    }
    
    public ElementText(String text, int startOffset, int endOffset, Map<String, String> attributeMap, Paint paint) {
        super(paint);
        this.text = text;//文本内容
        this.startOffset = startOffset;//开始位置
        this.endOffset = endOffset;//结束位置
        this.attributeMap = attributeMap;//样式属性
    }

    void initialize(String text, int startOffset, int endOffset, Map<String, String> attributeMap, Paint paint) {
        this.paint = paint;
        this.text = text;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        if (this.attributeMap != null) {
            this.attributeMap.clear();
        }
        if (attributeMap != null) {
            this.attributeMap = attributeMap;
        } else {
            this.attributeMap = new HashMap<String, String>();
        }
        this.size = null;
        this.rect = null;
        this.fullText = null;
        this.hlEndOffset = -1;
        this.hlStartOffset = -1;
        this.highlightColor = 0;
        this.backgroundColor = 0;
        this.fontColor = 0;
        this.width = 0;
        this.extraWidth = 0;
        this.hyphenWidth = 0;
        this.textIndent = 0;
        this.paraIndex = 0;
        this.offsetInPara = 0;
        this.paddingLeft = 0;
        this.paddingRight = 0;
        this.borderRadiusLeft = 0;
        this.borderRadiusRight = 0;
        this.isCloned = false;
        this.isBold = false;
        this.isItalic = false;
        this.isUnderline = false;
        this.isHyphenated = false;
        this.isHyphenateText = false;
        this.isJumpAction = false;
        this.multiMatchText = null;
        this.filterSpecialText = null;
        this.hyphenText = null;
        this.footnoteId = null;
        this.setIsLink(false);
        this.setlinkUUID(null);
        this.setAnchorId(null);
        this.noteStatus = NoteStatus.UNNOTE;
        this.selectionStatus = SelectionStatus.UNSELECTION;
        
        String classText = this.attributeMap.get("class");
        if (!TextUtils.isEmpty(classText)) {
            String href = this.attributeMap.get("href");
            if (!TextUtils.isEmpty(href)) {
                if (classText.equals("mz-footnote-link")) {
                    footnoteId = href;
                }
            }
        } else {
            String href = this.attributeMap.get("href");
            if (!TextUtils.isEmpty(href) && href.startsWith("#")) {
                footnoteId = href;
                isJumpAction = true;
            }
        }
        String id = this.attributeMap.get("id");
        if (!TextUtils.isEmpty(id)) {
            setAnchorId(id);
        }
    }

    @Override
    int getCount() {
        return endOffset - startOffset;
    }
    
    @Override
    Size measureSize(PageContext measurer, String basePath) {
        if (size == null) {
            if (paint == null) {
                size = new Size(0, 0);
                return size;
            }
            //float width = paint.measureText(text, startOffset, endOffset);
            float widthSize = isHyphenated ? hyphenWidth+textIndent+paddingLeft+paddingRight : width-hyphenWidth+textIndent+paddingLeft+paddingRight;
            float heightSize = paint.getFontMetrics().bottom - paint.getFontMetrics().top;
            size = new Size(widthSize, heightSize);
        }
        return size;
    }

    @Override
    void draw(Canvas canvas, PageContext measurer, Context context, float density, boolean isReadNoteShare) {
        if (rect == null || paint == null) {
            return;
        }
        if (text.length() <= startOffset || text.length() < endOffset) {
            return;
        }
        if (backgroundColor != 0) {
            paint.setColor(backgroundColor);
            if (borderRadiusLeft > 0 || borderRadiusRight > 0) {
                if (borderRadiusLeft > 0 && borderRadiusRight > 0) {
                    float[] radii={borderRadiusLeft,borderRadiusLeft,borderRadiusLeft,borderRadiusLeft,borderRadiusLeft,borderRadiusLeft,borderRadiusLeft,borderRadiusLeft};
                    Path path = new Path();
                    path.addRoundRect(rect, radii, Path.Direction.CW);
                    canvas.drawPath(path, paint);
                } else if (borderRadiusLeft > 0) {
                    float[] radii={borderRadiusLeft,borderRadiusLeft,0f,0f,0f,0f,borderRadiusLeft,borderRadiusLeft};
                    Path path = new Path();
                    path.addRoundRect(rect, radii, Path.Direction.CW);
                    canvas.drawPath(path, paint);
                } else if (borderRadiusRight > 0) {
                    float[] radii={0f,0f,borderRadiusRight,borderRadiusRight,borderRadiusRight,borderRadiusRight,0f,0f};
                    Path path = new Path();
                    path.addRoundRect(rect, radii, Path.Direction.CW);
                    canvas.drawPath(path, paint);
                }
            } else {
                canvas.drawRect(rect.left, rect.top, rect.right, rect.bottom, paint);
            }
        }
        paint.setColor(BookPageViewActivity.getFontColor());
        if (fontColor != 0) {
            paint.setColor(BookPageViewActivity.getMatchFontColor(fontColor));
        }
        if (this.isLink()) {
            paint.setColor(BookPageViewActivity.getLinkColor());
        }
        if (isItalic) {
            paint.setTextSkewX(-0.2f);
        }
        if (isBold) {
            paint.setFakeBoldText(true);
        }
        if (isUnderline) {
            paint.setStyle(Paint.Style.FILL);
            float bottom = rect.bottom - 2;
            if (size.height > 0) {
                bottom = rect.top + size.height;
            }
            canvas.drawRect(rect.left, bottom, rect.right, bottom + 2, paint);
        }
        canvas.drawText(text, startOffset, endOffset, rect.left+paddingLeft, rect.top - paint.getFontMetrics().top, paint);
        if (isHyphenated) {
            canvas.drawText(textDivider, 0, 1, rect.right+1-extraWidth, rect.top - paint.getFontMetrics().top, paint);
        }
        if (!TextUtils.isEmpty(multiMatchText)) {
            paint.setColor(highlightColor);
            String content = getContent();
            hlStartOffset = startOffset;
            String[] array = content.split(filterSpecialText);
            for (int i = 0; i < array.length-1; i++) {
                hlStartOffset = hlStartOffset + array[i].length();
                hlEndOffset = hlStartOffset + multiMatchText.length();
                if (hlStartOffset >= endOffset) {
                    break;
                }
                float startHL = 0;
                if (hlStartOffset > startOffset) {
                    startHL = paint.measureText(text.substring(startOffset, hlStartOffset));
                }
                canvas.drawText(text, hlStartOffset, hlEndOffset, rect.left+paddingLeft+startHL, rect.top - paint.getFontMetrics().top, paint);
                hlStartOffset += multiMatchText.length();
            }
        } else if (hlStartOffset >= 0 && hlEndOffset >= 0) {
            paint.setColor(highlightColor);
            float startHL = 0;
            if (hlStartOffset > startOffset) {
                startHL = paint.measureText(text.substring(startOffset, hlStartOffset));
            }
            if (hlStartOffset < startOffset) {
                hlStartOffset = startOffset;
            }
            if (hlEndOffset > endOffset) {
                hlEndOffset = endOffset;
            }
            canvas.drawText(text, hlStartOffset, hlEndOffset, rect.left+paddingLeft+startHL, rect.top - paint.getFontMetrics().top, paint);
        }
        if (this.noteStatus == NoteStatus.NOTE) {
            Paint paintNote = new Paint(paint);
            //paintNote.setStrokeWidth(2.0f);
            paintNote.setColor(0xffed7057);
            paintNote.setStyle(Paint.Style.FILL);
            float bottom = rect.bottom - 2;
            if (size.height > 0) {
                bottom = rect.top + size.height;
            }
            canvas.drawRect(rect.left, bottom, rect.right, bottom + 2, paintNote);
        } else if (this.noteStatus == NoteStatus.PEOPLENOTE) {
            Paint paintNote = new Paint(paint);
            paintNote.setStrokeWidth(3.0f);
            paintNote.setColor(0xffed7057);
            paintNote.setStyle(Paint.Style.STROKE);
            float bottom = rect.bottom - 2;
            if (size.height > 0) {
                bottom = rect.top + size.height;
            }
            Path path = new Path();
            path.moveTo(rect.left, bottom);
            path.lineTo(rect.right, bottom);
            PathEffect effects = new DashPathEffect(new float[]{5,5,5,5}, 1);
            paintNote.setPathEffect(effects);
            canvas.drawPath(path, paintNote);
        }
        if (this.selectionStatus == SelectionStatus.SELECTION) {
            Paint paintSelection = new Paint(paint);
            paintSelection.setColor(0x32ed7057);
            canvas.drawRect(rect, paintSelection);
        }
    }
    
    @Override
    public
    String getContent() {
        try{
            if (fullText != null) {
                return fullText;
            }
            String content = text.substring(startOffset, endOffset);
            return content;
        
        }catch(Exception e){
            return "";
        }
    }
    
    String getText() {
        try{

            String content = text.substring(startOffset, endOffset);
            return content;
        
        }catch(Exception e){
            return "";
        }
    }
    
    String getUrlText() {
        return attributeMap.get("href");
    }

    public void setWidth(float width) {
        this.width = width;
    }
    
    public void setHyphenWidth(float hyphenWidth) {
        this.extraWidth = paint.measureText(textDivider);
        this.hyphenWidth = hyphenWidth;
    }

    void setTextIndent(float textIndent) {
        this.textIndent = textIndent;
    }
    
    float getTextIndent() {
        return this.textIndent;
    }
    
    @Override
    void setRect(RectF rect) {
        super.setRect(rect);
        //缩进不算字符区域
        this.rect.left = this.rect.left + this.textIndent;
    }
    
    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
    }
    
    public void setItalic(boolean isItalic) {
        this.isItalic = isItalic;
    }
    
    public void setBold(boolean isBold) {
        this.isBold = isBold;
    }
    
    public void setUnderline(boolean isUnderline) {
        this.isUnderline = isUnderline;
    }
    
    public void setHighlightColor(int highlightColor) {
        this.highlightColor = highlightColor;
    }
    
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
    
    public ElementText getHyphenText() {
        return hyphenText;
    }

    public String getFootnoteId() {
        return footnoteId;
    }

    public boolean isJumpAction() {
        return isJumpAction;
    }

    public void setPaddingLeft(float paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public void setPaddingRight(float paddingRight) {
        this.paddingRight = paddingRight;
    }

    public void setBorderRadiusLeft(float borderRadiusLeft) {
        this.borderRadiusLeft = borderRadiusLeft;
    }

    public void setBorderRadiusRight(float borderRadiusRight) {
        this.borderRadiusRight = borderRadiusRight;
    }

    public boolean isHyphenated() {
        return isHyphenated;
    }

    public boolean isHyphenateText() {
        return isHyphenateText;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    public int getHlStartOffset() {
        return hlStartOffset;
    }

    public int getHlEndOffset() {
        return hlEndOffset;
    }

    public String getMultiMatchText() {
        return multiMatchText;
    }

    public String getFilterSpecialText() {
        return filterSpecialText;
    }

    public void setFilterSpecialText(String filterSpecialText) {
        this.filterSpecialText = filterSpecialText;
        if (this.hyphenText != null) {
            this.hyphenText.filterSpecialText = filterSpecialText;
        }
    }

    public void setHlStartOffset(int hlStartOffset) {
        this.hlStartOffset = hlStartOffset;
        if (this.hyphenText != null) {
            this.hyphenText.hlStartOffset = hlStartOffset;
        }
    }

    public void setHlEndOffset(int hlEndOffset) {
        this.hlEndOffset = hlEndOffset;
        if (this.hyphenText != null) {
            this.hyphenText.hlEndOffset = hlEndOffset;
        }
    }
    
    public void setMultiMatchText(String multiMatchText) {
        this.multiMatchText = multiMatchText;
        if (this.hyphenText != null) {
            this.hyphenText.multiMatchText = multiMatchText;
        }
    }

    public void setMatchSearchText(String blockText, String searchText, String filterSpecialText) {
        String matchText = blockText.substring(hlStartOffset, hlEndOffset);
        String content = getContent();
        if (this.hyphenText != null) {
            content = content.substring(startOffset, endOffset);
        }
        if (content.length() > 1) {
            String[] array = content.split(filterSpecialText);
            if (array.length > 2) {
                multiMatchText = searchText;
                this.filterSpecialText = filterSpecialText;
            }
        }
        if (!content.contains(searchText)) {
            hlStartOffset = startOffset + content.indexOf(matchText);
        } else {
            hlStartOffset = startOffset + content.lastIndexOf(matchText);
        }
        hlEndOffset = hlStartOffset + matchText.length();
        if (this.hyphenText != null) {
            this.hyphenText.hlStartOffset = hlStartOffset;
            this.hyphenText.hlEndOffset = hlEndOffset;
            this.hyphenText.multiMatchText = multiMatchText;
            this.hyphenText.filterSpecialText = this.filterSpecialText;
        }
    }

    public ElementText handleHyphenation(String word, String code, boolean isTableBlock) {
        this.fullText = null;
        if (word == null || word.length() == 0) {
            return null;
        }
        if (!word.contains(code)) {
            return null;
        }
        if (!isTableBlock && word.indexOf(code) <= 5) {
            return null;
        }
        this.fullText = this.getContent();
        hyphenText = new ElementText();
        hyphenText.text = this.text;
        hyphenText.fullText = this.fullText;
        hyphenText.fontColor = this.fontColor;
        hyphenText.startOffset = this.startOffset;
        hyphenText.endOffset = this.startOffset + word.indexOf(code);
        this.startOffset = hyphenText.endOffset;
        hyphenText.highlightColor = this.highlightColor;
        hyphenText.backgroundColor = this.backgroundColor;
        hyphenText.width = this.width;
        hyphenText.extraWidth = this.extraWidth;
        hyphenText.hyphenWidth = this.hyphenWidth + this.extraWidth;
        hyphenText.textIndent = this.textIndent;
        hyphenText.isBold = this.isBold;
        hyphenText.isItalic = this.isItalic;
        hyphenText.isUnderline = this.isUnderline;
        hyphenText.attributeMap = this.attributeMap;
        hyphenText.noteStatus = this.noteStatus;
        hyphenText.paint = this.paint;
        hyphenText.rect = this.rect;
        hyphenText.paraIndex = this.paraIndex;
        hyphenText.offsetInPara = this.offsetInPara;
        hyphenText.multiMatchText = this.multiMatchText;
        hyphenText.filterSpecialText = this.filterSpecialText;
        hyphenText.hlStartOffset = this.hlStartOffset;
        hyphenText.hlEndOffset = this.hlEndOffset;
        hyphenText.footnoteId = this.footnoteId;
        hyphenText.isJumpAction = this.isJumpAction;
        hyphenText.setIsLink(this.isLink());
        hyphenText.setlinkUUID(this.getlinkUUID());
        hyphenText.setAnchorId(this.getAnchorId());
        hyphenText.isHyphenated = true;
        hyphenText.isHyphenateText = true;
        this.isHyphenateText = true;
        hyphenText.isCloned = this.isCloned;
        this.startOffset = hyphenText.endOffset;
        this.size = null;
        return hyphenText;
    }
    
    public ElementText getSubElementText(int offset) {
        ElementText sub = new ElementText();
        sub.initialize(text, startOffset+offset, endOffset, attributeMap, paint);
        sub.width = paint.measureText(sub.getContent());
        sub.fullText = this.fullText;
        sub.isBold = this.isBold;
        sub.isItalic = this.isItalic;
        sub.setIsLink(this.isLink());
        sub.setlinkUUID(this.getlinkUUID());
        sub.setAnchorId(this.getAnchorId());
        sub.isUnderline = this.isUnderline;
        sub.fontColor = this.fontColor;
        sub.highlightColor = this.highlightColor;
        sub.backgroundColor = this.backgroundColor;
        sub.textIndent = this.textIndent;
        sub.paddingLeft = this.paddingLeft;
        sub.paddingRight = this.paddingRight;
        sub.borderRadiusLeft = this.borderRadiusLeft;
        sub.borderRadiusRight = this.borderRadiusRight;
        sub.multiMatchText = this.multiMatchText;
        sub.filterSpecialText = this.filterSpecialText;
        sub.hlStartOffset = this.hlStartOffset;
        sub.hlEndOffset = this.hlEndOffset;
        sub.paraIndex = this.paraIndex;
        sub.offsetInPara = this.offsetInPara;
        sub.footnoteId = this.footnoteId;
        sub.isJumpAction = this.isJumpAction;
        sub.noteStatus = this.noteStatus;
        sub.isCloned = this.isCloned;
        sub.isHyphenated = this.isHyphenated;
        sub.isHyphenateText = this.isHyphenateText;
        return sub;
    }
    
    public boolean isCloned() {
        return isCloned;
    }

    public ElementText clone() {
        ElementText et = new ElementText();
        et.initialize(text, startOffset, endOffset, attributeMap, paint);
        et.fullText = this.fullText;
        et.width = this.width;
        et.isBold = this.isBold;
        et.isItalic = this.isItalic;
        et.setIsLink(this.isLink());
        et.setlinkUUID(this.getlinkUUID());
        et.setAnchorId(this.getAnchorId());
        et.isUnderline = this.isUnderline;
        et.fontColor = this.fontColor;
        et.highlightColor = this.highlightColor;
        et.backgroundColor = this.backgroundColor;
        et.textIndent = this.textIndent;
        et.paddingLeft = this.paddingLeft;
        et.paddingRight = this.paddingRight;
        et.borderRadiusLeft = this.borderRadiusLeft;
        et.borderRadiusRight = this.borderRadiusRight;
        et.multiMatchText = this.multiMatchText;
        et.filterSpecialText = this.filterSpecialText;
        et.paraIndex = this.paraIndex;
        et.offsetInPara = this.offsetInPara;
        et.footnoteId = this.footnoteId;
        et.isJumpAction = this.isJumpAction;
        et.hlStartOffset = this.hlStartOffset;
        et.hlEndOffset = this.hlEndOffset;
        et.noteStatus = this.noteStatus;
        et.isCloned = true;
        et.isHyphenated = this.isHyphenated;
        et.isHyphenateText = this.isHyphenateText;
        return et;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('(');
        sb.append(text.substring(startOffset, endOffset));
        sb.append(')');
        return sb.toString();
    }
    
}
