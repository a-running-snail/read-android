package com.jingdong.app.reader.epub.paging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.reading.BookMark;
import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.reading.ReadSearchData;
import com.jingdong.app.reader.util.MZLog;
import com.jingdong.app.reader.util.ScreenUtils;

public class Page {
    private List<PageLine> lineList = new CopyOnWriteArrayList<PageLine>();
    private List<PageLink> linkList = new CopyOnWriteArrayList<PageLink>();
    private Map<ReadNote, List<Element>> noteMap;
    private List<ElementImage> pictureList = new CopyOnWriteArrayList<ElementImage>();
    private PageContext pageContext;
    private PagePool pagePool;
    private Chapter chapter;
    private String tocId;
    private boolean isContentReady = false;
    private boolean isReadNoteShare = false;
    private boolean isFullScreenImage = false;
    private boolean isMultipleStartPara = false;
    private List<Element> elementList = new CopyOnWriteArrayList<Element>();
    private BookMark bookMark;
    private ReadSearchData firstSearchData;
    private ReadSearchData lastSearchData;
    
    int startParaIndex = 0;
    int startOffset = 0;
    int endParaIndex = 0; //分享专用，笔记结尾的段落
    int endOffset = 0; //分享专用，笔记结尾的偏移位置
    int readNoteHeight = 0; //分享专用，笔记的高度
    String startParaStr = null;
    String startOffsetStr = null;
    
    protected Page() {
    }
    
    void initialize(PageContext pageContext, Chapter chapter) {
        this.pageContext = pageContext;
        this.chapter = chapter;
        if (chapter != null) {
            this.pagePool = chapter.getPagePool();
        }
        releaseContent();
    }
    
    synchronized void releaseContent() {
        isContentReady = false;
        isReadNoteShare = false;
        isFullScreenImage = false;
        isMultipleStartPara = false;
        if (pagePool != null) {
            for (PageLine pageLine : lineList) {
                pagePool.releasePageLine(pageLine);
            }
            for (PageLink pageLink : linkList) {
                pagePool.releasePageLink(pageLink);
            }
        }
        if (noteMap != null) {
            noteMap.clear();
        }
        tocId = null;
        bookMark = null;
        lineList.clear();
        linkList.clear();
        pictureList.clear();
        elementList.clear();
        readNoteHeight = 0;
        startParaIndex = 0;
        startOffset = 0;
        endParaIndex = 0;
        endOffset = 0;
        startParaStr = null;
        startOffsetStr = null;
        firstSearchData = null;
        lastSearchData = null;
    }

    void setStart(int paraIndex, int offset) {
        startParaIndex = paraIndex;
        startOffset = offset;
    }
    
    void setEnd(int paraIndex, int offset) {
        endParaIndex = paraIndex;
        endOffset = offset;
    }
    
    public boolean isReadNoteShare() {
        return isReadNoteShare;
    }

    public void setReadNoteShare(boolean isReadNoteShare) {
        this.isReadNoteShare = isReadNoteShare;
    }

    public boolean isFullScreenImage() {
        return isFullScreenImage;
    }

    public void setFullScreenImage(boolean isFullScreenImage) {
        this.isFullScreenImage = isFullScreenImage;
    }

    /**
     * 页面数据还没有准备好的时候，显示“正在加载中”字符串
     */
    void drawLoadingText(Canvas canvas, float width, float height, int color) {
        String loadingText = "正在加载中";
        Paint paint = new Paint(chapter.getGlobalPaint());
        paint.setColor(color);
        paint.setTextSize(ScreenUtils.dip2px(20f));
        
        Rect rect = new Rect();
        // 返回包围整个字符串的最小的一个Rect区域
        paint.getTextBounds(loadingText, 0, loadingText.length(), rect);
        float x = (width - rect.width()) / 2;
        float y = (height - rect.height()) / 2;

        canvas.drawText(loadingText, x, y, paint);
    }
    
    void setContentReady(boolean isReady) {
        isContentReady = isReady;
    }
    
    boolean isContentReady() {
        return isContentReady;
    }
    
    public void buildPageContent() {
        if (isReadNoteShare && chapter != null) {
            chapter.prepareBlockForReadNoteShare(this);
        }
        if (!isContentReady && chapter != null) {
            chapter.buildPage(this);
        }
    }
    
    public void buildNoteList() {
        
        if (noteMap != null) {
            noteMap.clear();
        }
        
        if (elementList.size() <= 0) {
            return;
        }
        
        Map<ReadNote, List<Element>> notes = new HashMap<ReadNote, List<Element>>();
        for (ReadNote note : chapter.getNoteList()) {
            if (noteBeforeStart(note) || noteAfterEnd(note) || note.deleted) {
                continue;
            } else {
                if (chapter.isShowAllNotes()
                        && !note.userId.equals(BookPageViewActivity.getUserId())) {
                    continue;
                }
                notes.put(note, new ArrayList<Element>());
            }
        }
        if (chapter.isShowAllNotes()) {
            for (ReadNote note : chapter.getAllPeopleNoteList()) {
                if (noteBeforeStart(note) || noteAfterEnd(note)) {
                    continue;
                } else {
                    notes.put(note, new ArrayList<Element>());
                }
            }
        }
        if (notes.size() > 0) {
            noteMap = notes;
            MZLog.d("NOTE", "note size is " + notes.size());
            updateElementNoteStatus();
        } else {
            for (Element element : elementList) {
                element.setNoteStatus(Element.NoteStatus.UNNOTE);
            }
        }

    }

    private boolean noteBeforeStart(ReadNote note) {
        Element start = elementList.get(0);
        if (note.toParaIndex < start.paraIndex) {
            return true;
        }
        if (note.toParaIndex == start.paraIndex
                && note.toOffsetInPara < start.offsetInPara) {
            return true;
        }
        return false;
    }

    private boolean noteAfterEnd(ReadNote note) {
        Element end = elementList.get(elementList.size() - 1);
        if (note.fromParaIndex > end.paraIndex) {
            return true;
        }
        if (note.fromParaIndex == end.paraIndex
                && note.fromOffsetInPara > end.offsetInPara + end.getCount()
                        - 1) {
            return true;
        }
        return false;
    }
    
    
    private void updateElementNoteStatus() {
        if (noteMap == null || noteMap.size() == 0) {
            return;
        } 
        
        for (Element element : elementList) {
            setElementNoteStatus(element);
        }
    }
    
    void setElementNoteStatus(Element element) {
        element.setNoteStatus(Element.NoteStatus.UNNOTE);
        for (ReadNote note : noteMap.keySet()) {
            if (elementBeforeNote(element, note) || elementAfterNote(element, note)) {
                continue;
            } else {
                if (note.userId.equals(BookPageViewActivity.getUserId())) {
                    element.setNoteStatus(Element.NoteStatus.NOTE);
                } else {
                    if (element.noteStatus != Element.NoteStatus.NOTE) {
                        element.setNoteStatus(Element.NoteStatus.PEOPLENOTE);
                    }
                }
                noteMap.get(note).add(element);
            }
        }
    }

    boolean elementBeforeNote(Element element, ReadNote note) {
        if (element.paraIndex < note.fromParaIndex) {
            return true;
        }
        if (element.paraIndex == note.fromParaIndex
                && element.offsetInPara + element.getCount() - 1 < note.fromOffsetInPara) {
            return true;
        }
        return false;
    }
    
    boolean elementAfterNote(Element element, ReadNote note) {
        if (element.paraIndex > note.toParaIndex) {
            return true;
        }
        if (element.paraIndex == note.toParaIndex && element.offsetInPara >= note.toOffsetInPara) {
            return true;
        }
        return false;
    }
    
    public Chapter getChapter() {
        return chapter;
    }
    
    public int getParaIndex() {
        return startParaIndex;
    }
    
    public int getOffsetInPara() {
        return startOffset;
    }
    
    public String getStartParaStr() {
        return startParaStr;
    }

    public void setStartParaStr(String startParaStr) {
        this.startParaStr = startParaStr;
    }

    public String getStartOffsetStr() {
        return startOffsetStr;
    }

    public void setStartOffsetStr(String startOffsetStr) {
        this.startOffsetStr = startOffsetStr;
    }

    public boolean isMultipleStartPara() {
        return isMultipleStartPara;
    }

    public void setMultipleStartPara(boolean isMultipleStartPara) {
        this.isMultipleStartPara = isMultipleStartPara;
    }

    public boolean pageAfterTheIndex(int paraIndex, int offset) {
        if (startParaIndex > paraIndex) {
            return true;
        }
        
        if (startParaIndex == paraIndex && offset < startOffset) {
            return true;
        }
        
        return false;
    }
    
    public boolean pageMatchTheIndex(int paraIndex, int offset) {
        if (startParaIndex == paraIndex && offset == startOffset) {
            return true;
        }
        
        return false;
    }
    
    void addLine(PageLine line) {
        lineList.add(line);
        elementList.addAll(line.elementList);
    }
    
    void addLink(PageLink link) {
        linkList.add(link);
    }

    void addPicture(ElementImage picture) {
        pictureList.add(picture);
        elementList.add(picture);
    }

    public List<PageLine> getLineList() {
        return lineList;
    }

    public List<ElementImage> getPictureList() {
        return pictureList;
    }
    
    public List<PageLink> getLinkList() {
        return linkList;
    }

    public PageContext getPageContext() {
        return pageContext;
    }
    
    Map<ReadNote, List<Element>> getNoteMap() {
        return noteMap;
    }
    
    public void setTocId(String tocId) {
        this.tocId = tocId;
    }
    
    public String getTocId() {
        return this.tocId;
    }

    public String getPageHead() {
        return chapter.getPageHead(tocId);
    }

    @Deprecated
    public int getPlayOrder() {
        return chapter.getPlayOrder();
    }

    public String getDigest() {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0, n = elementList.size(); i < n; i++) {
            Element e = elementList.get(i);
            if (e instanceof ElementText) {
                buffer.append(((ElementText) e).getContent());
                if (buffer.length() >= 80) {
                    break;
                }
            }
        }
        return buffer.toString();
    }

    public BookMark getBookMark() {
        return bookMark;
    }

    public void setBookMark(BookMark bookMark) {
        this.bookMark = bookMark;
    }
    
    public void addBookMark(BookMark bookMark) {
        this.bookMark = bookMark;
        this.chapter.addBookMark(bookMark);
    }
    
    public void removeBookMark() {
        this.chapter.removeBookMark(bookMark);
        this.bookMark = null;
    }
    
    public void prepareSearchHighlight(String keywords, String filterSpecialWords) {
        if (chapter == null || elementList.size() == 0) {
            return;
        }
        List<ReadSearchData> result = chapter.findSearchResultInPage(this);
        if (result.size() > 0) {
            firstSearchData = result.get(0);
            lastSearchData = result.get(result.size()-1);
        }
        
        for (ReadSearchData data : result) {
            Block block = chapter.getBlock(data.getParaIndex());
            block.searchHighlightElement(keywords, filterSpecialWords, data);
        }
        
        prepareCloneHighlight();
    }

    public ReadSearchData getFirstSearchData() {
        return firstSearchData;
    }

    public ReadSearchData getLastSearchData() {
        return lastSearchData;
    }
    
    private void prepareCloneHighlight() {
        if (chapter == null || elementList.size() == 0) {
            return;
        }
        for (Element e : elementList) {
            if (e instanceof ElementText) {
                ElementText et = (ElementText) e;
                if (et.isCloned()) {
                    setupCloneHighlight(et);
                }
            }
        }
    }
    
    private void setupCloneHighlight(ElementText et) {
        Block block = chapter.getBlock(et.paraIndex);
        for (Element e : block.elementList) {
            if (e instanceof ElementText) {
                ElementText eText = (ElementText) e;
                if (eText.getStartOffset() == et.getStartOffset()
                        || eText.getEndOffset() == et.getEndOffset()) {
                    if (eText.getHlStartOffset() >= et.getStartOffset()
                            && eText.getHlEndOffset() <= et.getEndOffset()) {
                        et.setHlStartOffset(eText.getHlStartOffset());
                        et.setHlEndOffset(eText.getHlEndOffset());
                    } else if (et.isHyphenateText()
                            && (eText.getHlStartOffset() >= et.getStartOffset() || eText
                                    .getHlEndOffset() <= et.getEndOffset())) {
                        et.setHlStartOffset(eText.getHlStartOffset());
                        et.setHlEndOffset(eText.getHlEndOffset());
                    } else {
                        et.setMultiMatchText(eText.getMultiMatchText());
                        et.setFilterSpecialText(eText.getFilterSpecialText());
                    }
                    return;
                }
            }
        }
    }
    
    public void clearCloneHightlight() {
        if (elementList.size() == 0) {
            return;
        }
        for (Element e : elementList) {
            if (e instanceof ElementText) {
                ElementText text = (ElementText) e;
                if (text.isCloned()) {
                    text.setFilterSpecialText(null);
                    text.setMultiMatchText(null);
                    text.setHlStartOffset(-1);
                    text.setHlEndOffset(-1);
                }
            }
        }
    }
}
