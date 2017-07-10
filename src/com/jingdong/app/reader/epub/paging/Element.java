package com.jingdong.app.reader.epub.paging;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

public abstract class Element {
    
    public static final String textDivider = "-";
    enum SelectionStatus {
        UNSELECTION,
        SELECTION
    }
    SelectionStatus selectionStatus = SelectionStatus.UNSELECTION;
    enum NoteStatus {
        UNNOTE,
        NOTE,
        PEOPLENOTE
    }
    NoteStatus noteStatus = NoteStatus.UNNOTE;
    
    Paint paint;
    RectF rect;
    
    int paraIndex;
    int offsetInPara;
    private boolean isLink = false;
    private String linkUUID;
    private String anchorId;
    
    Element(Paint paint) {
        this.paint = paint;
    }
    abstract Size measureSize(PageContext measurer, String basePath);

    abstract int getCount();
    
    abstract void draw(Canvas canvas, PageContext measurer, Context context, float density, boolean isReadNoteShare);
    
    public abstract String getContent();
    
    void setChapterLocation(int paraIndex, int offsetInPara) {
        this.paraIndex = paraIndex;
        this.offsetInPara = offsetInPara;
    }
    
    void setSelectionStatus(SelectionStatus status) {
        selectionStatus = status;
    }
    
    void setNoteStatus(NoteStatus status) {
        noteStatus = status;
    }
    
    void setRect(RectF rect) {
        this.rect = rect;
    }
    
    RectF getRect() {
        return rect;
    }
    
    boolean isLink() {
        return isLink;
    }
    
    void setlinkUUID(String UUIDText) {
        linkUUID = UUIDText;
    }
    
    String getlinkUUID() {
        return linkUUID;
    }
    
    void setIsLink(boolean isLink) {
        this.isLink = isLink;
    }
    
    public String getAnchorId() {
        return anchorId;
    }
    
    public void setAnchorId(String anchorId) {
        this.anchorId = anchorId;
    }
    
}
