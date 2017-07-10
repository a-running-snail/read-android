package com.jingdong.app.reader.epub.paging;

import java.util.Map;

import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.epub.css.CSS;
import com.jingdong.app.reader.epub.css.CSSCollection;

import android.graphics.Paint;
import android.graphics.RectF;
import android.text.TextUtils;

public class Border {
    
    public enum BorderStyle {
        None, Hidden, Dotted, Dashed, Solid, Double
    }
    
    private final static String TOP = "top";
    private final static String LEFT = "left";
    private final static String RIGHT = "right";
    private final static String BOTTOM = "bottom";

    private RectF rect;
    private Paint paint;
    private float width = 0;
    private float leftWidth = 0;
    private float topWidth = 0;
    private float rightWidth = 0;
    private float bottomWidth = 0;
    private float pxPerEm = 0;
    private float paddingLeft = 0;
    private float paddingRight = 0;
    private boolean fullBorder = false;
    private boolean leftBorder = false;
    private boolean topBorder = false;
    private boolean rightBorder = false;
    private boolean bottomBorder = false;
    private BorderStyle style = BorderStyle.None;
    private CSSCollection cssCollection;
    private String color = null;
    private String property = null;

    public static Border parseBorder(CSS css) {
        Border border = new Border();
        if (css != null) {
            border.pxPerEm = css.getPxPerEm();
            Map<String, String> propertyMap = css.getProperties();
            if (propertyMap.containsKey("border")) {
                border.fullBorder = true;
                border.property = "border";
            }
            if (border.fullBorder) {
                border.parseFull(propertyMap, border.property);
            } else {
                while (border.parse(propertyMap)) {
                    border.parseFull(propertyMap, "border-" + border.property);
                    if (TextUtils.isEmpty(border.color)) {
                        border.color = border.getBorderColor(propertyMap);
                    }
                    if (border.style == BorderStyle.None) {
                        border.style = border.getBorderStyle(propertyMap);
                    }
                    if (border.width <= 0) {
                        border.width = border.getBorderWidth(propertyMap);
                    }
                    if (LEFT.equals(border.property)) {
                        border.leftWidth = border.width;
                    } else if (RIGHT.equals(border.property)) {
                        border.rightWidth = border.width;
                    } else if (TOP.equals(border.property)) {
                        border.topWidth = border.width;
                    } else if (BOTTOM.equals(border.property)) {
                        border.bottomWidth = border.width;
                    }
                }
            }
            if (TextUtils.isEmpty(border.color)) {
                border.color = CSS.getTextColor(propertyMap, null);
            }
            border.parsePadding(propertyMap);
        }
        return border;
    }
    
    private boolean parse(Map<String, String> propertyMap) {
        if (!leftBorder
                && (propertyMap.containsKey("border-left-color") || propertyMap
                        .containsKey("border-left"))) {
            leftBorder = true;
            property = LEFT;
            return true;
        }
        if (!topBorder
                && (propertyMap.containsKey("border-top-color") || propertyMap
                        .containsKey("border-top"))) {
            topBorder = true;
            property = TOP;
            return true;
        }
        if (!rightBorder
                && (propertyMap.containsKey("border-right-color") || propertyMap
                        .containsKey("border-right"))) {
            rightBorder = true;
            property = RIGHT;
            return true;
        }
        if (!bottomBorder
                && (propertyMap.containsKey("border-bottom-color") || propertyMap
                        .containsKey("border-bottom"))) {
            bottomBorder = true;
            property = BOTTOM;
            return true;
        }
        return false;
    }
    
    private void parseFull(Map<String, String> propertyMap, String name) {
        if (TextUtils.isEmpty(name)) {
            return;
        }
        String value = propertyMap.get(name);
        if (TextUtils.isEmpty(value)) {
            return;
        }
        while (value.indexOf(", ") > 0) {
            value = value.replaceAll(", ", ",");
        }
        String[] array = value.split(" ");
        for (String str : array) {
            str = str.trim();
            if (TextUtils.isEmpty(str)) {
                continue;
            }
            BorderStyle bs = parseStyle(str);
            if (bs != BorderStyle.None) {
                style = bs;
                continue;
            } else {
                float w = parseWidth(str);
                if (w == 0) {
                    color = str;
                } else {
                    width = w;
                }
            }
        }
    }
    
    private String getBorderColor(Map<String, String> propertyMap) {
        return propertyMap.get("border-"+property+"-color");
    }
    
    private BorderStyle getBorderStyle(Map<String, String> propertyMap) {
        String borderStyle = propertyMap.get("border-"+property+"-style");
        if (TextUtils.isEmpty(borderStyle)) {
            return BorderStyle.None;
        }
        return parseStyle(borderStyle);
    }
    
    private BorderStyle parseStyle(String borderStyle) {
        if (borderStyle.equalsIgnoreCase("hidden")) {
            return BorderStyle.Hidden;
        }
        if (borderStyle.equalsIgnoreCase("dotted")) {
            return BorderStyle.Dotted;
        }
        if (borderStyle.equalsIgnoreCase("dashed")) {
            return BorderStyle.Dashed;
        }
        if (borderStyle.equalsIgnoreCase("solid")) {
            return BorderStyle.Solid;
        }
        if (borderStyle.equalsIgnoreCase("double")) {
            return BorderStyle.Double;
        }
        return BorderStyle.None;
    }
    
    private float getBorderWidth(Map<String, String> propertyMap) {
        String width = propertyMap.get("border-"+property+"-width");
        if (TextUtils.isEmpty(width)) {
            return 0;
        }
        return parseWidth(width);
    }
    
    private float parseWidth(String width) {
        try {
            if (width.endsWith("px") || width.endsWith("PX")
                    || width.endsWith("Px") || width.endsWith("pX")) {
                String sub = width.substring(0, width.length() - 2);
                float size = Float.parseFloat(sub.trim());
                return size;
            }

            if (width.endsWith("em") || width.endsWith("EM")
                    || width.endsWith("Em") || width.endsWith("eM")) {
                String sub = width.substring(0, width.length() - 2);
                float size = Float.parseFloat(sub.trim());
                return size * pxPerEm;
            }

            if (width.endsWith("%")) {
                String sub = width.substring(0, width.length() - 1);
                float size = Float.parseFloat(sub.trim());
                return size * pxPerEm / 100;
            }
            
            float size = Float.parseFloat(width);
            return size;
        } catch (NumberFormatException e) {
            
        }
        
        return 0;
    }
    
    private void parsePadding(Map<String, String> propertyMap) {
        float[] padding = CSS.getPadding(propertyMap, pxPerEm);
        float paddingTop = padding!=null?padding[0]:CSS.getPaddingTop(propertyMap, pxPerEm);
        paddingRight = padding!=null?padding[1]:CSS.getPaddingRight(propertyMap, pxPerEm);
        float paddingBottom = padding!=null?padding[2]:CSS.getPaddingBottom(propertyMap, pxPerEm);
        paddingLeft = padding!=null?padding[3]:CSS.getPaddingLeft(propertyMap, pxPerEm);
    }

    public RectF getRect() {
        return rect;
    }

    public void setRect(RectF rect) {
        this.rect = rect;
    }
    
    public void resetBottom(float bottom) {
        rect.bottom = bottom;
    }

    public float getWidth() {
        return width;
    }

    public float getLeftWidth() {
        if (fullBorder) {
            return width;
        }
        return leftWidth;
    }

    public float getTopWidth() {
        if (fullBorder) {
            return width;
        }
        return topWidth;
    }

    public float getRightWidth() {
        if (fullBorder) {
            return width;
        }
        return rightWidth;
    }

    public float getBottomWidth() {
        if (fullBorder) {
            return width;
        }
        return bottomWidth;
    }

    public boolean isFullBorder() {
        return fullBorder;
    }

    public boolean isLeftBorder() {
        return leftBorder;
    }

    public boolean isTopBorder() {
        return topBorder;
    }

    public boolean isBottomBorder() {
        return bottomBorder;
    }

    public boolean isRightBorder() {
        return rightBorder;
    }

    public BorderStyle getStyle() {
        if (fullBorder) {
            if (style == BorderStyle.None) {
                return BorderStyle.Solid;
            }
        }
        return style;
    }

    public int getColor() {
        int col = this.cssCollection.getColor(color);
        if (fullBorder) {
            if (col == 0) {
                return BookPageViewActivity.getFontColor();
            }
        }
        return col;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public CSSCollection getCssCollection() {
        return cssCollection;
    }

    public void setCssCollection(CSSCollection cssCollection) {
        this.cssCollection = cssCollection;
    }

    public float getPaddingLeft() {
        return paddingLeft;
    }

    public float getPaddingRight() {
        return paddingRight;
    }

}
