package com.jingdong.app.reader.epub.paging;

import net.davidashen.text.Hyphenator;
import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;

import com.jingdong.app.reader.util.StringUtil;

public class PageContext {
	/**
	 * 上下文
	 */
    Context context;
    Hyphenator hyphenator;
    String hyphenCode;
    float pxPerEm;
    float lineSpace;
    float blockSpace;
    boolean isCancel = false;

    public PageContext(Context context, float pxPerEm, float lineSpace, float blockSpace, Hyphenator hyphenator) {
        this.context = context;//上下文
        this.pxPerEm = pxPerEm;
        this.lineSpace = lineSpace;
        this.blockSpace = blockSpace;
        this.hyphenator = hyphenator;
        byte[] byteArray = StringUtil.hexStringToBytes("c2ad");
        hyphenCode = new String(byteArray);
    }
    
    public void setCancel(boolean isCancel) {
        this.isCancel = isCancel;
    }

    protected void setPxPerEm(float pxPerEm) {
        this.pxPerEm = pxPerEm;
    }
    
    protected float getPxPerEm() {
        return this.pxPerEm;
    }
    
    public String getHyphenCode() {
        return this.hyphenCode;
    }
    
    public String doHyphenation(Paint paint, String word, float limitedWidth, ElementText et, boolean isForceHyphen) {
        if (hyphenator == null || word == null || paint == null || et == null) {
            return et.getContent();
        }
        if (isCancel) {
            return word;
        }
        
        String hyphenatedWord = hyphenator.hyphenate(word);
        if (!hyphenatedWord.contains(hyphenCode)) {
            return et.getText();
        }
        
        String[] subWord = hyphenatedWord.split(hyphenCode);
        String text = "";
        float width = 0f;
        float stringWidth = 0f;
        for (String string : subWord) {
            stringWidth = paint.measureText(string);
            if (width + stringWidth > limitedWidth) {
                break;
            }
            text += string;
            width += stringWidth;
        }
        
        if (width <= 0 || TextUtils.isEmpty(text)) {
            if (isForceHyphen) {
                hyphenatedWord = getMatchWidthText(paint, word, limitedWidth);
                if (!TextUtils.isEmpty(hyphenatedWord)) {
                    et.setHyphenWidth(paint.measureText(hyphenatedWord));
                    hyphenatedWord += hyphenCode;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            et.setHyphenWidth(width);
            hyphenatedWord = text + hyphenCode;
        }
        
        return hyphenatedWord;
    }
    
    public String getMatchWidthText(Paint paint, String word, float limitedWidth) {
        if (TextUtils.isEmpty(word) || limitedWidth <= 0) {
            return null;
        }
        String text = "";
        float stringWidth = 0f;
        float dividerWidth = paint.measureText(Element.textDivider);
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, n = word.length(); i < n; i++) {
            text = word.substring(i, i+1);
            stringWidth += paint.measureText(text);
            if (stringWidth + dividerWidth > limitedWidth) {
                break;
            } else {
                buffer.append(text);
            }
        }
        return buffer.toString();
    }
}
