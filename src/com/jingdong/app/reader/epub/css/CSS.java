package com.jingdong.app.reader.epub.css;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;

import android.graphics.Typeface;
import android.text.TextUtils;

import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.epub.FilePath;

public class CSS {
    public enum Align {
        Left, Right, Center, Justify, Unknown
    }

    private Align align = Align.Unknown;
    private Map<String, String> propertyMap;
    private float pxPerEm;
    private Typeface typeface;

    public CSS() {
    }

    public void initialize(Map<String, String> propertyMap, Map<String, CSSFont> fontMap, float pxPerEm) {
        align = Align.Unknown;
        this.propertyMap = propertyMap;
        this.pxPerEm = pxPerEm;
        typeface = getTypeface(propertyMap, fontMap);
    }

    public void mergePorperties(Map<String, String> map) {
        for (String key : map.keySet()) {
            if (propertyMap.containsKey(key)) {
                String value = propertyMap.get(key);
                if ("inherit".equalsIgnoreCase(value)) {
                    propertyMap.put(key, map.get(key));
                }
                continue;
            } else {
                propertyMap.put(key, map.get(key));
            }
        }
    }

    public Map<String, String> getProperties() {
        return propertyMap;
    }

    public float getPxPerEm() {
        return pxPerEm;
    }

    public Typeface getTypeface() {
        return typeface;
    }

    public float getFontSize() {
        String sizeText = propertyMap.get("font-size");
        if (TextUtils.isEmpty(sizeText)) {
            return pxPerEm;
        }

        try {
            if (sizeText.endsWith("em") || sizeText.endsWith("EM")
                    || sizeText.endsWith("Em") || sizeText.endsWith("eM")) {
                String sub = sizeText.substring(0, sizeText.length() - 2);
                float size = Float.parseFloat(sub.trim());
                return size * pxPerEm;
            }

            if (sizeText.endsWith("px") || sizeText.endsWith("PX")
                    || sizeText.endsWith("Px") || sizeText.endsWith("pX")) {
                String sub = sizeText.substring(0, sizeText.length() - 2);
                float size = Float.parseFloat(sub.trim());
                return size * pxPerEm / 20;
            }

            if (sizeText.endsWith("pt") || sizeText.endsWith("PT")
                    || sizeText.endsWith("Pt") || sizeText.endsWith("pT")) {
                String sub = sizeText.substring(0, sizeText.length() - 2);
                float size = Float.parseFloat(sub.trim());
                return size * pxPerEm / 20;
            }

            if (sizeText.endsWith("%")) {
                String sub = sizeText.substring(0, sizeText.length() - 1);
                float size = Float.parseFloat(sub.trim());
                return size * pxPerEm / 100;
            }

            if (sizeText.equals("xx-small")) {
                return pxPerEm * 0.6f;
            }
            if (sizeText.equals("x-small")) {
                return pxPerEm * 0.7f;
            }
            if (sizeText.equals("small")) {
                return pxPerEm * 0.8f;
            }
            if (sizeText.equals("medium")) {
                return pxPerEm;
            }
            if (sizeText.equals("large")) {
                return pxPerEm * 1.2f;
            }
            if (sizeText.equals("x-large")) {
                return pxPerEm * 1.3f;
            }
            if (sizeText.equals("xx-large")) {
                return pxPerEm * 1.4f;
            }
        } catch (NumberFormatException e) {
            return pxPerEm;
        }

        return pxPerEm;
    }

    public Align getTextAlign() {

        if (align != Align.Unknown) {
            return align;
        }

        String alignText = propertyMap.get("text-align");
        if (TextUtils.isEmpty(alignText) || alignText.equalsIgnoreCase("left")) {
            align = Align.Left;
        } else if (alignText.equalsIgnoreCase("right")) {
            align = Align.Right;
        } else if (alignText.equalsIgnoreCase("center")) {
            align = Align.Center;
        } else if (alignText.equalsIgnoreCase("justify")) {
            align = Align.Justify;
        } else {
            align = Align.Left;
        }

        return align;
    }

    public boolean textIsBold() {
        return textIsBold(propertyMap);
    }
    
    public boolean textIsItalic() {
        return textIsItalic(propertyMap);
    }
    
    public boolean textHasStrikeThru() {
//        String fontStyle = propertyMap.get("text-decoration");
//        if (!TextUtils.isEmpty(fontStyle)) {
//            if ("line-through".equalsIgnoreCase(fontStyle)) {
//                return true;
//            }
//        }
        return false;
    }

    public float getTextPaddingLeft() {
        String paddingLeft = propertyMap.get("padding-left");
        if (TextUtils.isEmpty(paddingLeft)) {
            return 0;
        }

        try {
            if (paddingLeft.endsWith("em") || paddingLeft.endsWith("EM")
                    || paddingLeft.endsWith("Em") || paddingLeft.endsWith("eM")) {
                String sub = paddingLeft.substring(0, paddingLeft.length() - 2);
                float size = Float.parseFloat(sub.trim());
                return size * pxPerEm;
            }

            if (paddingLeft.endsWith("%")) {
                String sub = paddingLeft.substring(0, paddingLeft.length() - 1);
                float size = Float.parseFloat(sub.trim());
                return size * pxPerEm / 100;
            }
        } catch (NumberFormatException e) {
            
        }

        return 0;
    }

    public String getBackgroundColor() {
        return getBackgroundColor(propertyMap);
    }

    public String getBackgroundImage(String absPath) {
        return getBackgroundImage(propertyMap, absPath);
    }

    public String getBackgroundRepeat() {
        return getBackgroundRepeat(propertyMap);
    }

    public boolean isFloatLeft() {
        return isFloatLeft(propertyMap);
    }

    public boolean isFloatRight() {
        return isFloatRight(propertyMap);
    }

    public boolean isDrawBorder() {
        String str = propertyMap.get("border-left-color");
        if (TextUtils.isEmpty(str)) {
            str = propertyMap.get("border-top-color");
        }
        if (TextUtils.isEmpty(str)) {
            str = propertyMap.get("border-right-color");
        }
        if (TextUtils.isEmpty(str)) {
            str = propertyMap.get("border-bottom-color");
        }
        if (TextUtils.isEmpty(str)) {
            str = propertyMap.get("border");
        }
        if (TextUtils.isEmpty(str)) {
            str = propertyMap.get("border-left");
        }
        if (TextUtils.isEmpty(str)) {
            str = propertyMap.get("border-top");
        }
        if (TextUtils.isEmpty(str)) {
            str = propertyMap.get("border-right");
        }
        if (TextUtils.isEmpty(str)) {
            str = propertyMap.get("border-bottom");
        }
        if (TextUtils.isEmpty(str)) {
            str = propertyMap.get("border");
        }
        if (TextUtils.isEmpty(str)) {
            return false;
        } else {
            return true;
        }
    }
    
    public float[] getMargin() {
        return getMargin(propertyMap, pxPerEm);
    }
    
    public float getMarginLeft() {
        return getMarginLeft(propertyMap, pxPerEm);
    }
    
    public float getMarginTop() {
        return getMarginTop(propertyMap, pxPerEm);
    }
    
    public float getMarginRight() {
        return getMarginRight(propertyMap, pxPerEm);
    }
    
    public float getMarginBottom() {
        return getMarginBottom(propertyMap, pxPerEm);
    }
    
    public static Typeface getTypeface(Map<String, String> propertyMap, Map<String, CSSFont> fontMap) {
        Typeface typeface = null;
        String fontFamily = propertyMap.get("font-family");
        if (!TextUtils.isEmpty(fontFamily)) {
            if (fontFamily.contains(",")) {
                String[] fontArray = fontFamily.split(",");
                for (int i = 0; i < fontArray.length; i++) {
                    typeface = generateTypeface(fontFamily, propertyMap, fontMap);
                    if (typeface != null) {
                        break;
                    }
                }
            } else {
                typeface = generateTypeface(fontFamily, propertyMap, fontMap);
            }
        }
        return typeface;
    }
    
    private static Typeface generateTypeface(String fontFamily, Map<String, String> propertyMap, Map<String, CSSFont> fontMap) {
        Typeface typeface = null;
        CSSFont cssFont = fontMap.get(fontFamily);
        if (cssFont != null) {
            String fontStyle = propertyMap.get("font-style");
            String fontWeight = propertyMap.get("font-weight");
            cssFont.generateTypeface(fontStyle, fontWeight);
            if ("italic".equalsIgnoreCase(fontStyle)) {
                if ("bold".equalsIgnoreCase(fontWeight)) {
                    typeface = cssFont.getTypeFaceIB();
                } else {
                    typeface = cssFont.getTypeFaceIN();
                }
            } else {
                if ("bold".equalsIgnoreCase(fontWeight)) {
                    typeface = cssFont.getTypeFaceNB();
                } else {
                    typeface = cssFont.getTypeFaceNN();
                }
            }
        } else {
            if (fontFamily.startsWith("\"")) {
                fontFamily = fontFamily.substring(1);
            }
            if (fontFamily.endsWith("\"")) {
                fontFamily = fontFamily.substring(0, fontFamily.length()-1);
            }
            if ("monospace".equalsIgnoreCase(fontFamily)) {
                typeface = Typeface.MONOSPACE;
            } else if (CSSFont.isFZSSFont(fontFamily)) {
                typeface = BookPageViewActivity.getFZSSFont();
            } else if (CSSFont.isFZLTHFont(fontFamily)) {
                typeface = BookPageViewActivity.getFZLTHFont();
            } else if (CSSFont.isFZKTFont(fontFamily)) {
                typeface = BookPageViewActivity.getFZKTFont();
            }
        }
        return typeface;
    }
    
    public static boolean hasTypeface(Map<String, String> propertyMap) {
        return propertyMap.containsKey("font-family");
    }
    
    public static boolean textHasUnderline(Map<String, String> propertyMap) {
        String fontStyle = propertyMap.get("text-decoration");
        if (!TextUtils.isEmpty(fontStyle)) {
            if ("underline".equalsIgnoreCase(fontStyle)) {
                return true;
            }
        }
        return false;
    }
    
    public static String getTextColor(Map<String, String> propertyMap, String defaultColor) {
        String color = propertyMap.get("color");
        if (!TextUtils.isEmpty(color)) {
            return color;
        }
        return defaultColor;
    }
    
    public static float getTextIndent(Map<String, String> propertyMap, float pxPerEm) {
        String textIndent = propertyMap.get("text-indent");
        if (TextUtils.isEmpty(textIndent)) {
            return 0;
        }
        
        try {
            if (textIndent.endsWith("em") || textIndent.endsWith("EM")
                    || textIndent.endsWith("Em") || textIndent.endsWith("eM")) {
                String sub = textIndent.substring(0, textIndent.length() - 2);
                float size = Math.abs(Float.parseFloat(sub.trim()));
                return size * pxPerEm;
            }

            if (textIndent.endsWith("%")) {
                String sub = textIndent.substring(0, textIndent.length() - 1);
                float size = Math.abs(Float.parseFloat(sub.trim()));
                return size * pxPerEm / 100;
            }
        } catch (NumberFormatException e) {
            
        }
        
        return 0;
    }
    
    public static boolean textIsItalic(Map<String, String> propertyMap) {
        String fontStyle = propertyMap.get("font-style");
        if (!TextUtils.isEmpty(fontStyle)) {
            if ("italic".equalsIgnoreCase(fontStyle)) {
                return true;
            } else if ("oblique".equalsIgnoreCase(fontStyle)) {
                return true;
            } else if ("normal".equalsIgnoreCase(fontStyle)) {
                return false;
            }
        }
        return false;
    }
    
    public static String getBackgroundColor(Map<String, String> propertyMap) {
        return propertyMap.get("background-color");
    }
    
    public static String getBackgroundImage(Map<String, String> propertyMap, String absPath) {
        String src = propertyMap.get("background-image");
        if (!TextUtils.isEmpty(src)) {
            if (src.startsWith("url(")) {
                src = src.substring(4);
            }
            if (src.endsWith(")")) {
                src = src.substring(0, src.length() - 1);
            }
            try {
                src = URLDecoder.decode(FilePath.resolveRelativePath(absPath, src), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return src;
    }
    
    public static String getBackgroundRepeat(Map<String, String> propertyMap) {
        return propertyMap.get("background-repeat");
    }
    
    public static boolean isFloatLeft(Map<String, String> propertyMap) {
        String imageFloat = propertyMap.get("float");
        return "left".equalsIgnoreCase(imageFloat);
    }
    
    public static boolean isFloatRight(Map<String, String> propertyMap) {
        String imageFloat = propertyMap.get("float");
        return "right".equalsIgnoreCase(imageFloat);
    }
    
    public static float[] getMargin(Map<String, String> propertyMap, float pxPerEm) {
        float[] margin = null;
        String marginStr = propertyMap.get("margin");
        if (!TextUtils.isEmpty(marginStr)) {
            margin = new float[4];
            String[] strArray = marginStr.trim().split(" ");
            String marginTop = null;
            String marginRight = null;
            String marginBottom = null;
            String marginLeft = null;
            for (int i=0; i<strArray.length ;i++) {
                String text = strArray[i].trim();
                if (!TextUtils.isEmpty(text)) {
                    if (marginTop == null) {
                        marginTop = text;
                    } else if (marginRight == null) {
                        marginRight = text;
                    } else if (marginBottom == null) {
                        marginBottom = text;
                    } else if (marginLeft == null) {
                        marginLeft = text;
                    }
                }
            }
            if (marginRight == null) {
                marginRight = marginTop;
            }
            if (marginBottom == null) {
                marginBottom = marginTop;
            }
            if (marginLeft == null) {
                marginLeft = marginRight;
            }
            margin[0] = parseSize(marginTop, pxPerEm);
            margin[1] = parseSize(marginRight, pxPerEm);
            margin[2] = parseSize(marginBottom, pxPerEm);
            margin[3] = parseSize(marginLeft, pxPerEm);
        }
        return margin;
    }
    
    public static float getMarginLeft(Map<String, String> propertyMap, float pxPerEm) {
        String marginLeft = propertyMap.get("margin-left");
        return parseSize(marginLeft, pxPerEm);
    }
    
    public static float getMarginRight(Map<String, String> propertyMap, float pxPerEm) {
        String marginRight = propertyMap.get("margin-right");
        return parseSize(marginRight, pxPerEm);
    }
    
    public static float getMarginTop(Map<String, String> propertyMap, float pxPerEm) {
        String marginTop = propertyMap.get("margin-top");
        return parseSize(marginTop, pxPerEm);
    }
    
    public static float getMarginBottom(Map<String, String> propertyMap, float pxPerEm) {
        String marginBottom = propertyMap.get("margin-bottom");
        return parseSize(marginBottom, pxPerEm);
    }
    
    public static float[] getPadding(Map<String, String> propertyMap, float pxPerEm) {
        float[] padding = null;
        String paddingStr = propertyMap.get("padding");
        if (!TextUtils.isEmpty(paddingStr)) {
            padding = new float[4];
            String[] strArray = paddingStr.trim().split(" ");
            String paddingTop = null;
            String paddingRight = null;
            String paddingBottom = null;
            String paddingLeft = null;
            for (int i=0; i<strArray.length ;i++) {
                String text = strArray[i].trim();
                if (!TextUtils.isEmpty(text)) {
                    if (paddingTop == null) {
                        paddingTop = text;
                    } else if (paddingRight == null) {
                        paddingRight = text;
                    } else if (paddingBottom == null) {
                        paddingBottom = text;
                    } else if (paddingLeft == null) {
                        paddingLeft = text;
                    }
                }
            }
            if (paddingRight == null) {
                paddingRight = paddingTop;
            }
            if (paddingBottom == null) {
                paddingBottom = paddingTop;
            }
            if (paddingLeft == null) {
                paddingLeft = paddingRight;
            }
            padding[0] = parseSize(paddingTop, pxPerEm);
            padding[1] = parseSize(paddingRight, pxPerEm);
            padding[2] = parseSize(paddingBottom, pxPerEm);
            padding[3] = parseSize(paddingLeft, pxPerEm);
        }
        return padding;
    }
    
    public static float getPaddingLeft(Map<String, String> propertyMap, float pxPerEm) {
        String paddingLeft = propertyMap.get("padding-left");
        return parseSize(paddingLeft, pxPerEm);
    }
    
    public static float getPaddingRight(Map<String, String> propertyMap, float pxPerEm) {
        String paddingRight = propertyMap.get("padding-right");
        return parseSize(paddingRight, pxPerEm);
    }
    
    public static float getPaddingTop(Map<String, String> propertyMap, float pxPerEm) {
        String paddingTop = propertyMap.get("padding-top");
        return parseSize(paddingTop, pxPerEm);
    }
    
    public static float getPaddingBottom(Map<String, String> propertyMap, float pxPerEm) {
        String paddingBottom = propertyMap.get("padding-bottom");
        return parseSize(paddingBottom, pxPerEm);
    }
    
    private static float parseSize(String sizeString, float pxPerEm) {
        if (!TextUtils.isEmpty(sizeString)) {
            try {
                if (sizeString.endsWith("em") || sizeString.endsWith("EM")
                        || sizeString.endsWith("Em") || sizeString.endsWith("eM")) {
                    String sub = sizeString.substring(0, sizeString.length() - 2);
                    float size = Float.parseFloat(sub.trim());
                    return size * pxPerEm;
                } else if (sizeString.endsWith("px") || sizeString.endsWith("PX")
                        || sizeString.endsWith("Px") || sizeString.endsWith("pX")) {
                    String sub = sizeString.substring(0, sizeString.length() - 2);
                    float size = Float.parseFloat(sub.trim());
                    return size;
                }
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }
    
    public static float getHeight(Map<String, String> propertyMap, float pxPerEm, float parentHeight) {
        String height = propertyMap.get("height");
        if (!TextUtils.isEmpty(height)) {
            try {
                if (height.endsWith("px") || height.endsWith("PX") || height.endsWith("pX") || height.endsWith("Px")) {
                    height = height.substring(0, height.length() - 2);
                    float size = Float.parseFloat(height.trim());
                    return size;
                } else if (height.endsWith("em") || height.endsWith("EM") || height.endsWith("eM") || height.endsWith("Em")) {
                    height = height.substring(0, height.length() - 2);
                    float size = Float.parseFloat(height.trim());
                    return pxPerEm * size;
                } else if (height.endsWith("%")) {
                    height = height.substring(0, height.length() - 1);
                    float size = Float.parseFloat(height.trim());
                    return parentHeight * size / 100;
                } else {
                    float size = Float.parseFloat(height.trim());
                    return size;
                }
            } catch (NumberFormatException e) {
            }
        }
        return parentHeight;
    }
    
    public static float getWidth(Map<String, String> propertyMap, float pxPerEm, float parentWidth) {
        String width = propertyMap.get("width");
        if (!TextUtils.isEmpty(width)) {
            try {
                if (width.endsWith("px") || width.endsWith("PX")
                        || width.endsWith("pX") || width.endsWith("Px")) {
                    width = width.substring(0, width.length() - 2);
                    float size = Float.parseFloat(width.trim());
                    return size;
                } else if (width.endsWith("em") || width.endsWith("EM")
                        || width.endsWith("eM") || width.endsWith("Em")) {
                    width = width.substring(0, width.length() - 2);
                    float size = Float.parseFloat(width.trim());
                    return pxPerEm * size;
                } else if (width.endsWith("%")) {
                    width = width.substring(0, width.length() - 1);
                    float size = Float.parseFloat(width.trim());
                    return parentWidth * size / 100;
                } else {
                    float size = Float.parseFloat(width.trim());
                    return size;
                }
            } catch (NumberFormatException e) {
            }
        }
        return parentWidth;
    }
    
    /**
     * 从CSS样式中获取行高信息
     * @param propertyMap CSS属性信息列表
     * @param pxPerEm 行高基准，即父节点的行高设置
     * @return
     */
    public static float getLineHeight(Map<String, String> propertyMap, float pxPerEm /*行高基准*/) {
        String height = propertyMap.get("line-height");
        if (!TextUtils.isEmpty(height)) {
            try {
                if (height.endsWith("px") || height.endsWith("PX")  || height.endsWith("pX") || height.endsWith("Px")) {
                    height = height.substring(0, height.length() - 2);
                    float size = Float.parseFloat(height.trim());
                    return size;
                } else if (height.endsWith("em") || height.endsWith("EM") || height.endsWith("eM") || height.endsWith("Em")) {
                    height = height.substring(0, height.length() - 2);
                    float size = Float.parseFloat(height.trim());
                    return pxPerEm * size;
                } else if (height.endsWith("%")) {
                    height = height.substring(0, height.length() - 1);
                    float size = Float.parseFloat(height.trim());
                    return pxPerEm * size / 100;
                } else {
                    float size = Float.parseFloat(height.trim());
                    return size;
                }
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }
    
    public static boolean textIsBold(Map<String, String> propertyMap) {
        String weightText = propertyMap.get("font-weight");
        if (TextUtils.isEmpty(weightText)) {
            return false;
        }

        if (weightText.equalsIgnoreCase("bold")) {
            return true;
        }

        return false;
    }
    
    public static float getBorderRadius(Map<String, String> propertyMap, float pxPerEm) {
        String width = propertyMap.get("border-radius");
        if (!TextUtils.isEmpty(width)) {
            try {
                if (width.endsWith("px") || width.endsWith("PX")
                        || width.endsWith("pX") || width.endsWith("Px")) {
                    width = width.substring(0, width.length() - 2);
                    float size = Float.parseFloat(width.trim());
                    return size * BookPageViewActivity.getDensity();
                } else if (width.endsWith("em") || width.endsWith("EM")
                        || width.endsWith("eM") || width.endsWith("Em")) {
                    width = width.substring(0, width.length() - 2);
                    float size = Float.parseFloat(width.trim());
                    return size * pxPerEm;
                } else {
                    float size = Float.parseFloat(width.trim());
                    return size;
                }
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }
    
    public boolean isForcePageBreakAfter() {
        return CSS.isForcePageBreakAfter(propertyMap);
    }
    
    public boolean isForcePageBreakBefore() {
        return CSS.isForcePageBreakBefore(propertyMap);
    }
    
    public static boolean isForcePageBreakAfter(Map<String, String> propertyMap) {
        String after = propertyMap.get("page-break-after");
        if (!TextUtils.isEmpty(after)) {
            if (after.equals("always")) {
                return true;
            } else if (after.equals("auto")) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isForcePageBreakBefore(Map<String, String> propertyMap) {
        String before = propertyMap.get("page-break-before");
        if (!TextUtils.isEmpty(before)) {
            if (before.equals("always")) {
                return true;
            } else if (before.equals("auto")) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isDisplayHidden() {
        return CSS.isDisplayHidden(propertyMap);
    }
    
    public static boolean isDisplayHidden(Map<String, String> propertyMap) {
        String value = propertyMap.get("display");
        if (!TextUtils.isEmpty(value)) {
            if (value.equals("hidden")) {
                return true;
            }
        }
        return false;
    }
    
    public int getTableRowSpan() {
        return CSS.getTableRowSpan(propertyMap);
    }
    
    public static int getTableRowSpan(Map<String, String> propertyMap) {
        String value = propertyMap.get("rowspan");
        if (!TextUtils.isEmpty(value)) {
            Integer rowspan = new Integer(0);
            try {
                rowspan = Integer.valueOf(value);
            } catch(Exception e) {
            }
            return rowspan.intValue();
        }
        return 0;
    }
    
    public float getTableCellSpacing() {
        return CSS.getTableCellSpacing(propertyMap, pxPerEm);
    }
    
    public static float getTableCellSpacing(Map<String, String> propertyMap, float pxPerEm) {
        String height = propertyMap.get("cellspacing");
        if (!TextUtils.isEmpty(height)) {
            try {
                if (height.endsWith("px") || height.endsWith("PX")
                        || height.endsWith("pX") || height.endsWith("Px")) {
                    height = height.substring(0, height.length() - 2);
                    float size = Float.parseFloat(height.trim());
                    return size;
                } else if (height.endsWith("em") || height.endsWith("EM")
                        || height.endsWith("eM") || height.endsWith("Em")) {
                    height = height.substring(0, height.length() - 2);
                    float size = Float.parseFloat(height.trim());
                    return pxPerEm * size;
                } else {
                    float size = Float.parseFloat(height.trim());
                    return size;
                }
            } catch (NumberFormatException e) {
            }
        }
        return 0;
    }
}
