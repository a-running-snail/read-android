package com.jingdong.app.reader.epub.paging;

import java.util.ArrayList;
import java.util.List;

import android.graphics.RectF;


public class PageLink {
    private List<Element> elementList = new ArrayList<Element>();
    private String uuid;
    private String urlText;
    private String basePath;
    private String footnoteId;
    private boolean isJumpAction;
    RectF rect;
    
    PageLink() {
    }
    
    PageLink(String uuid) {
        this.uuid = uuid;
    }
    
    void initialize(String uuid, String basePath) {
        this.uuid = uuid;
        this.basePath = basePath;
        release();
    }
    
    void release() {
        this.elementList.clear();
        this.isJumpAction = false;
        this.footnoteId = null;
        this.urlText = null;
        this.rect = null;
    }
    
    void addElement(ElementText element) {
        if (urlText == null) {
            urlText = element.getUrlText();
        }
        if (footnoteId == null) {
            footnoteId = element.getFootnoteId();
        }
        if (!isJumpAction) {
            isJumpAction = element.isJumpAction();
        }
        elementList.add(element);
    }
    
    String getFootnoteId() {
        return footnoteId;
    }

    public boolean isJumpAction() {
        return isJumpAction;
    }

    boolean isSameLink(String uuid) {
        return this.uuid.equals(uuid);
    }
    
    String getUrlText() {
        return urlText;
    }
    
    String getBasePath() {
        return basePath;
    }
    
    public RectF getRect() {
        if (rect != null) {
            return rect;
        }
        float x = elementList.get(0).rect.left;
        float y = elementList.get(0).rect.top;
        float width = 0;
        float height = elementList.get(0).rect.height();
        for (Element e : elementList) {
            width += e.rect.width();
        }
        rect = new RectF(x, y, x + width, y + height);
        return rect;
    }

}
