package com.jingdong.app.reader.epub.paging;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;

import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.epub.css.CSS;
import com.jingdong.app.reader.epub.css.CSSCollection;
import com.jingdong.app.reader.util.ImageUtils;


public class PageLine {
    boolean lastLine = false;
    boolean firstLine = false;
    CSS style;
    Paint paint;
    List<Element> elementList = new CopyOnWriteArrayList<Element>();
    RectF rect;
    boolean isLineSpaceDrawBg = true;
    boolean isImageFloat = false;
    float bgWidth = 0;
    float lineWidth = 0;
    float lineSpace = 0;
    float tableCellWidth = 0;
    float tableCellYOffset = 0;
    private int count;
    private int divHashcode;
    private float lineIndent = 0;
    private float paddingTop = 0;
    private float paddingBottom = 0;
    private float paddingLeft = 0;
    private float paddingRight = 0;
    private float marginLeft = 0;
    private float marginRight = 0;
    private float floatHeight = 0;
    private float orderedIndexWidth = 0;
    private boolean isOrderedLine = false;
    private boolean isUnorderedLine = false;
    private boolean isTableRowSpanLine = false;
    private boolean isTableBorder = false;
    private boolean isTableCellLine = false;
    private String orderedLineIndex = null;
    private String backgroundImageSrc = null;
    private Bitmap backgroundImage = null;
    private CSSCollection cssCollection;

    PageLine(){
    }
    
    PageLine(Paint paint){
        this.paint = paint;
    }
    
    void initialize(Paint paint, CSSCollection cssCollection) {
        this.paint = paint;
        this.cssCollection = cssCollection;
        release();
    }
    
    void release() {
        this.elementList.clear();
        if (backgroundImage != null && !backgroundImage.isRecycled()) {
            backgroundImage.recycle();
        }
        isTableRowSpanLine = false;
        isLineSpaceDrawBg = true;
        isUnorderedLine = false;
        isTableCellLine = false;
        isTableBorder = false;
        isOrderedLine = false;
        isImageFloat = false;
        lastLine = false;
        firstLine = false;
        count = 0;
        bgWidth = 0;
        lineWidth = 0;
        lineSpace = 0;
        lineIndent = 0;
        tableCellWidth = 0;
        tableCellYOffset = 0;
        paddingTop = 0;
        paddingBottom = 0;
        paddingLeft = 0;
        paddingRight = 0;
        marginLeft = 0;
        marginRight = 0;
        floatHeight = 0;
        orderedIndexWidth = 0;
        orderedLineIndex = null;
        backgroundImageSrc = null;
        backgroundImage = null;
        style = null;
        rect = null;
    }

    void setRect(RectF rect) {
        this.rect = rect;
        if (backgroundImage != null && !backgroundImage.isRecycled()) {
            backgroundImage.recycle();
        }
        if (!TextUtils.isEmpty(backgroundImageSrc)) {
            backgroundImage = ImageUtils.getBitmapFromNamePath(backgroundImageSrc, (int)rect.width(), (int)rect.height());
        }
    }
    
    CSSCollection getCssCollection() {
        return cssCollection;
    }
    
    boolean isEmpty() {
        return elementList.size() == 0;
    }

    /**
     * 准备画背景的参数
     * @param width     背景宽度
     * @param lineSpace 行间距
     */
    void prepareDrawBackgroundParam(float width, float lineSpace) {
        this.bgWidth = width - marginLeft - marginRight;
        this.lineSpace = lineSpace;
    }
    
    void prepareTableCellBackgroundParam(float width, float yOffset) {
        this.tableCellWidth = width;
        this.tableCellYOffset = yOffset;
    }
    
    void setLineSpaceDrawBg(boolean isLineSpaceDrawBg) {
        this.isLineSpaceDrawBg = isLineSpaceDrawBg;
    }

    void setOrigin(float x, float y) {
        x += marginLeft;
        this.rect.offsetTo(x, y);
    }
    
    float getPosY() {
        return rect.top + rect.height() / 2;
    }
    
    float distance(float y) {
        return Math.abs(y - (rect.top + rect.height() / 2));
    }
    
    boolean isImageFloat() {
        return isImageFloat;
    }
    
    void setImageFloat(boolean isImageFloat) {
        this.isImageFloat = isImageFloat;
    }

    void addElement(Element e) {
        elementList.add(e);
        count += e.getCount();
    }
    
    int getCount() {
        return count;
    }
    
    public float getLineHeight() {
        return rect.height() + paddingTop + paddingBottom;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public void setLineWidth(float lineWidth) {
        this.lineWidth = lineWidth;
    }

    public float getBgWidth() {
        return bgWidth;
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

    public float getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(float paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    public float getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(float paddingRight) {
        this.paddingRight = paddingRight;
    }

    public float getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(float marginLeft) {
        this.marginLeft = marginLeft;
    }

    public float getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(float marginRight) {
        this.marginRight = marginRight;
    }

    public float getFloatHeight() {
        return floatHeight;
    }

    public void setFloatHeight(float floatHeight) {
        this.floatHeight = floatHeight;
    }

    public boolean isFirstLine() {
        return firstLine;
    }

    public boolean isLastLine() {
        return lastLine;
    }

    void setFirstLine(boolean firstLine) {
        this.firstLine = firstLine;
    }

    void setLastLine(boolean lastLine) {
        this.lastLine = lastLine;
    }

    public float getLineIndent() {
        return lineIndent;
    }

    public void setLineIndent(float lineIndent) {
        this.lineIndent = lineIndent;
    }

    public void setOrderedLine(boolean isOrderedLine) {
        this.isOrderedLine = isOrderedLine;
    }

    public void setUnorderedLine(boolean isUnorderedLine) {
        this.isUnorderedLine = isUnorderedLine;
    }

    public boolean isTableRowSpanLine() {
        return isTableRowSpanLine;
    }

    public void setTableRowSpanLine(boolean isTableRowSpanLine) {
        this.isTableRowSpanLine = isTableRowSpanLine;
    }

    public void setOrderedLineIndex(int orderedLineIndex) {
        this.orderedLineIndex = String.valueOf(orderedLineIndex)+".";
        orderedIndexWidth = paint.measureText(this.orderedLineIndex);
    }

    public int getDivHashcode() {
        return divHashcode;
    }

    public void setDivHashcode(int divHashcode) {
        this.divHashcode = divHashcode;
    }

    public float getTableCellYOffset() {
        return tableCellYOffset;
    }

    public boolean isTableBorder() {
        return isTableBorder;
    }

    public void setTableBorder(boolean isTableBorder) {
        this.isTableBorder = isTableBorder;
    }

    public boolean isTableCellLine() {
        return isTableCellLine;
    }

    public void setTableCellLine(boolean isTableCellLine) {
        this.isTableCellLine = isTableCellLine;
    }

    public void setBackgroundImageSrc(String backgroundImageSrc) {
        this.backgroundImageSrc = backgroundImageSrc;
    }

    void draw(Canvas canvas, PageContext pageContext, Context context, float density, boolean isReadNoteShare) {
        drawBackground(canvas);
        drawOrderedLine(canvas);
        drawUnorderedLine(canvas);
        for (Element e : elementList) {
            e.draw(canvas, pageContext, context, density, isReadNoteShare);
        }
    }
    
    void drawOrderedLine(Canvas canvas) {
        if (isOrderedLine) {
            canvas.drawText(orderedLineIndex, rect.left+lineIndent*3/4-orderedIndexWidth, rect.top - paint.getFontMetrics().top, paint);
        }
    }
    
    void drawUnorderedLine(Canvas canvas) {
        if (isUnorderedLine) {
            canvas.drawText("•", rect.left+lineIndent/2, rect.top - paint.getFontMetrics().top, paint);
        }
    }
    
    void drawBackground(Canvas canvas) {
        if (rect == null) {
            return;
        }
        if (style != null) {
            String backgroundColor = style.getBackgroundColor();
            if (!TextUtils.isEmpty(backgroundColor)) {
                int lineBgColor = cssCollection.getColor(backgroundColor);
                lineBgColor = BookPageViewActivity.getMergedBgColor(lineBgColor);
                paint.setColor(lineBgColor);
                if (isTableCellLine) {
                    canvas.drawRect(rect.left, rect.top - paddingTop, rect.left + bgWidth, rect.bottom
                            + paddingBottom, paint);
                    if (isLineSpaceDrawBg) {
                        canvas.drawRect(rect.left, rect.bottom + paddingBottom - 1,
                                rect.left + bgWidth, rect.bottom + paddingBottom + 1
                                        + lineSpace, paint);
                    }
                    if (tableCellWidth > 0 && tableCellYOffset > 0) {
                        canvas.drawRect(rect.left, rect.top - paddingTop, rect.left + tableCellWidth, 
                                tableCellYOffset, paint);
                    }
                } else {
                    canvas.drawRect(rect.left, rect.top - paddingTop, rect.left + bgWidth, rect.bottom
                            + paddingBottom, paint);
                    if (isLineSpaceDrawBg) {
                        canvas.drawRect(rect.left, rect.bottom + paddingBottom - 1,
                                rect.left + bgWidth, rect.bottom + paddingBottom + 1
                                        + lineSpace, paint);
                    }
                    if (tableCellWidth > 0 && tableCellYOffset > 0) {
                        canvas.drawRect(rect.left, rect.top - paddingTop, rect.left + tableCellWidth, 
                                tableCellYOffset, paint);
                    }
                }
            }
        }
        if (backgroundImage != null && !backgroundImage.isRecycled()) {
            canvas.drawBitmap(backgroundImage, rect.left, rect.top, paint);
        }
    }
    

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Element e : elementList) {
            sb.append(e.toString());
        }
        return sb.toString();
    }

    public List<Element> getElementList() {
        return elementList;
    }

    // 删除偏移位置之后的所有Element
    public void removeElements(int offset) {
        if (getCount() >= offset) {
            int removeCount = 0;
            for (int i = elementList.size() - 1; i >= 0; i--) {
                Element e = elementList.remove(i);
                removeCount += e.getCount();
                if (removeCount == offset) {
                    break;
                }
            }
        }
        count -= offset;
    }
}
