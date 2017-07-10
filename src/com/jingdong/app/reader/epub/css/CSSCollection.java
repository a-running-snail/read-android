package com.jingdong.app.reader.epub.css;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSCharsetRule;
import org.w3c.dom.css.CSSFontFaceRule;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSMediaRule;
import org.w3c.dom.css.CSSPageRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.CSSUnknownRule;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.util.UiStaticMethod;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;

/**
 * ePub CSS相关内容集合
 *
 */
public class CSSCollection {
    private Map<String, CSSFont> fontMap = new HashMap<String, CSSFont>();
    private Map<String, Map<String, String>> ruleMap = new HashMap<String, Map<String, String>>();
    private Map<String, String> bodyPropertyMap = null;
    
    private JSONObject cssColorJson;
    private String cssPath;

    public CSSCollection(String cssPath, InputStream is) {
        try {
            String cssText = IOUtil.readIt(is);
            //解析CSS
            parseCssRule(cssPath, cssText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public CSSCollection(String cssPath, String cssText) {
        parseCssRule(cssPath, cssText);
    }
    
    /**
     * 释放集合
     */
    public void release() {
        for (Map.Entry<String, Map<String, String>> entry : ruleMap.entrySet()) {
            ruleMap.get(entry.getKey()).clear();
        }
        ruleMap.clear();
        fontMap.clear();
    }
    
    /**
     * 过滤部分格式内容 以及与屏幕大小相关的内容
     * @param text css内容
     * @return 过滤后的内容
     */
    private String filterIllegal(String text) {
        String regular = "<!--.*?-->";
        text = text.replaceAll(regular, "");
        regular = "@media.*?\\{";
        text = text.replaceAll(regular, "");
        String[] array = text.split("\\}");  
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            if (array[i].indexOf("{") >= 0) {
                buffer.append(array[i]);
                buffer.append("}");
            }
        }
        if (buffer.length() > 0) {
            return buffer.toString();
        } else {
            return text;
        }
    }
    
    /**
     * 解析CSS内容的CSS<br />
     * 将每一项样式解析成一项一项<br />
     * @param cssPath css样式文件路径
     * @param cssText css样式内容字符串
     */
    private void parseCssRule(String cssPath, String cssText) {
        this.cssPath = cssPath;
        if (TextUtils.isEmpty(cssText)) {
            return;
        }
        //过滤不合法的内容
        cssText = filterIllegal(cssText);
        InputSource source = new InputSource(new StringReader(cssText));
        CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
        try {
        	//解析样式
            CSSStyleSheet sheet = parser.parseStyleSheet(source, null, null);
            CSSRuleList list = sheet.getCssRules();
            for (int i = 0, l = list.getLength(); i < l; i++) {
                CSSRule rule = list.item(i);
                if (rule instanceof CSSCharsetRule) {
                } else if (rule instanceof CSSFontFaceRule) {
                    CSSFontFaceRule styleRule = (CSSFontFaceRule) rule;
                    CSSStyleDeclaration declaration = styleRule.getStyle();
                    String fontFamily = declaration.getPropertyValue("font-family");
                    CSSFont cssFont = null;
                    if (fontMap.containsKey(fontFamily)) {
                        cssFont = fontMap.get(fontFamily);
                    } else {
                        cssFont = new CSSFont();
                        cssFont.setCssPath(cssPath);
                        cssFont.setFontFamily(fontFamily);
                    }
                    setFontFaceSrc(cssFont, declaration);
                    fontMap.put(fontFamily, cssFont);
                } else if (rule instanceof CSSImportRule) {
                    // CSSImportRule importRule = (CSSImportRule) rule;
                    // System.out.println("!===qli=== css import: "+importRule.getHref());
                } else if (rule instanceof CSSMediaRule) {
                    // do nothing
                } else if (rule instanceof CSSPageRule) {
                    // do nothing
                } else if (rule instanceof CSSStyleRule) {
                    CSSStyleRule styleRule = (CSSStyleRule) rule;
                    CSSStyleDeclaration declaration = styleRule.getStyle();
                    HashMap<String, String> propertyMap = new HashMap<String, String>();
                    for (int n = 0; n < declaration.getLength(); n++) {
                        String property = declaration.item(n);
                        propertyMap.put(property.toLowerCase(),declaration.getPropertyValue(property));
                        // declaration.getPropertyPriority(property)
                    }

                    //选择器
                    String selectorText = styleRule.getSelectorText();
                    if (!TextUtils.isEmpty(selectorText)) {
                        if (selectorText.startsWith("*")) {
                            // CSS解析器会默认加一个*在selector名称前面，因此这里去掉
                            selectorText = selectorText.substring(1);
                        }
                        if (selectorText.contains(",")) {
                            String[] selectorArray = selectorText.split(",");
                            for (int n = 0; n < selectorArray.length; n++) {
                                String keyText = selectorArray[n].trim();
                                if (ruleMap.containsKey(keyText)) {
                                    ruleMap.get(keyText).putAll(propertyMap);
                                } else {
                                    ruleMap.put(keyText, propertyMap);
                                }
                            }
                        } else {
                            if (ruleMap.containsKey(selectorText)) {
                                ruleMap.get(selectorText).putAll(propertyMap);
                            } else {
                                ruleMap.put(selectorText, propertyMap);
                            }
                        }
                    }
                } else if (rule instanceof CSSUnknownRule) {
                    // do nothing
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
    
    private void setFontFaceSrc(CSSFont cssFont, CSSStyleDeclaration declaration) {
        String fontStyle = declaration.getPropertyValue("font-style");
        String fontWeight = declaration.getPropertyValue("font-weight");
        String src = declaration.getPropertyValue("src");
        if ("italic".equalsIgnoreCase(fontStyle)) {
            if ("bold".equalsIgnoreCase(fontWeight)) {
                cssFont.setSrcItalicBold(src);
            } else {
                cssFont.setSrcItalicNormal(src);
            }
        } else {
            if ("bold".equalsIgnoreCase(fontWeight)) {
                cssFont.setSrcNormalBold(src);
            } else {
                cssFont.setSrcNormalNormal(src);
            }
        }
    }

    public void merge(CSSCollection css) {
        for (String key : css.fontMap.keySet()) {
            if (fontMap.containsKey(key)) {
                CSSFont cssFont = fontMap.get(key);
                CSSFont font = css.fontMap.get(key);
                String src = font.getSrcItalicBold();
                if (!TextUtils.isEmpty(src)) {
                    cssFont.setSrcItalicBold(src);
                }
                src = font.getSrcItalicNormal();
                if (!TextUtils.isEmpty(src)) {
                    cssFont.setSrcItalicNormal(src);
                }
                src = font.getSrcNormalBold();
                if (!TextUtils.isEmpty(src)) {
                    cssFont.setSrcNormalBold(src);
                }
                src = font.getSrcNormalNormal();
                if (!TextUtils.isEmpty(src)) {
                    cssFont.setSrcNormalNormal(src);
                }
                fontMap.put(key, cssFont);
            } else {
                fontMap.put(key, css.fontMap.get(key));
            }
        }
        for (Map.Entry<String, Map<String, String>> entry : css.ruleMap.entrySet()) {
            if (ruleMap.containsKey(entry.getKey())) {
                ruleMap.get(entry.getKey()).putAll(entry.getValue());
            } else {
                ruleMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public CSSFont getCSSFont(String font) {
        return fontMap.get(font);
    }
    
    public Map<String, CSSFont> getFontMap() {
        return fontMap;
    }

    public Map<String, String> getPropertyMap(String selector) {
        return ruleMap.get(selector);
    }
    
	public void findBodyPropertyMap() {
        for(String key:ruleMap.keySet()) {
            if (key.endsWith("body")) {
                bodyPropertyMap = ruleMap.get(key);
            }
        }
    }
    
    public Map<String, String> getBodyPropertyMap() {
        return bodyPropertyMap;
    }
    
    public void setColorJson(InputStream is) {
        try {
            String colorText = IOUtil.readIt(is);
            cssColorJson = new JSONObject(colorText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public JSONObject getCssColorJson() {
        return cssColorJson;
    }

    public void setCssColorJson(JSONObject cssColorJson) {
        this.cssColorJson = cssColorJson;
    }

    public String getCssPath() {
        return cssPath;
    }

    public int getColor(String name) {
        if (TextUtils.isEmpty(name)) {
            return 0;
        }
        String color = null;
        if (cssColorJson != null) {
            color = cssColorJson.optString(name, null);
        }
        if (TextUtils.isEmpty(color)) {
            color = prepareColor(name);
        } else {
            color = prepareColor(color);
        }
        try {
            return UiStaticMethod.getColorFromString(color);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    private String prepareColor(String color) {
        int index = color.indexOf("#");
        if (index >= 0) {
            color = color.substring(index+1);
        }
        index = color.indexOf("(");
        if (index > 0) {
            color = color.substring(index+1, color.indexOf(")"));
        } else {
            if (color.length() == 6) {
                color = "ff" + color;
            }
            if (color.length() == 3) {
                color = "f" + color;
            }
        }
        return color;
    }
    
    public void findAllFont() {
        boolean isSongti = false;
        boolean isHeiti = false;
        boolean isKaiti = false;
        for (String key : fontMap.keySet()) {
            CSSFont cssFont = fontMap.get(key);
            String[] array = cssFont.getFontFamily().split(",");
            for (int i = 0; i < array.length; i++) {
                //宋体
            	if (!isSongti) {
                    if (CSSFont.isFZSSFont(array[i])) {
                        isSongti = true;
                        BookPageViewActivity.initFZSSFont();
                    }
                }
                //黑体
                if (!isHeiti) {
                    if (CSSFont.isFZLTHFont(array[i])) {
                        isHeiti = true;
                        BookPageViewActivity.initFZLTHFont();
                    }
                }
                //楷体
                if (!isKaiti) {
                    if (CSSFont.isFZKTFont(array[i])) {
                        isKaiti = true;
                        BookPageViewActivity.initFZKTFont();
                    }
                }
                if (isSongti && isHeiti && isKaiti) {
                    break;
                }
            }
        }
        
        //循环解析得到的CSS规则
        for (String key : ruleMap.keySet()) {
            Map<String, String> map = ruleMap.get(key);
            if (map.containsKey("font-family")) {
                String fontFamily = map.get("font-family");
                if (fontFamily.startsWith("\"")) {
                    fontFamily = fontFamily.substring(1);
                }
                if (fontFamily.endsWith("\"")) {
                    fontFamily = fontFamily.substring(0, fontFamily.length()-1);
                }
                //宋体
                if (!isSongti) {
                    if (CSSFont.isFZSSFont(fontFamily)) {
                        isSongti = true;
                        BookPageViewActivity.initFZSSFont();
                    }
                }
                //黑体
                if (!isHeiti) {
                    if (CSSFont.isFZLTHFont(fontFamily)) {
                        isHeiti = true;
                        BookPageViewActivity.initFZLTHFont();
                    }
                }
                //楷体
                if (!isKaiti) {
                    if (CSSFont.isFZKTFont(fontFamily)) {
                        isKaiti = true;
                        BookPageViewActivity.initFZKTFont();
                    }
                }
            }
            if (isSongti && isHeiti && isKaiti) {
                break;
            }
        }
        if (isSongti || isHeiti || isKaiti) {
        	//需要展示字体下载对话框
            Intent intent = new Intent(BookPageViewActivity.ACTION_SHOW_FONT_DIALOG);
            LocalBroadcastManager.getInstance(MZBookApplication.getContext()).sendBroadcast(intent);
        }
    }
}
