package com.jingdong.app.reader.epub.paging;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;
import org.xmlpull.v1.XmlPullParserException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Base64;

import com.jingdong.app.reader.activity.BookPageViewActivity;
import com.jingdong.app.reader.application.MZBookApplication;
import com.jingdong.app.reader.book.BookEntity;
import com.jingdong.app.reader.data.db.MZBookDatabase;
import com.jingdong.app.reader.entity.ReadPageContent;
import com.jingdong.app.reader.epub.JDDecryptUtil;
import com.jingdong.app.reader.epub.epub.Spine;
import com.jingdong.app.reader.epub.epub.TOCItem;
import com.jingdong.app.reader.epub.parser.Kit42;
import com.jingdong.app.reader.epub.parser.Kit42Node;
import com.jingdong.app.reader.io.IOUtil;
import com.jingdong.app.reader.reading.BookMark;
import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.reading.ReadSearchData;
import com.jingdong.app.reader.user.LoginUser;

public class Chapter extends Observable{
	/**
	 * 页面列表
	 */
    List<Page> pageList = new ArrayList<Page>();
    
    /**
     * 当前用户笔记列表
     */
    List<ReadNote> noteList = new ArrayList<ReadNote>();
    
    /**
     * 全部人笔记
     */
    List<ReadNote> allPeopleNoteList = new ArrayList<ReadNote>();
    /**
     * 书签
     */
    private List<BookMark> bookMarkList = new ArrayList<BookMark>();
    
    /**
     * 目录列表
     */
    private List<TOCItem> tocList;
    
    /**
     * 排版计算器
     */
    private PageCalculator pageCalculator;
    private PagePool pagePool;
    /**
     * 章节信息（OPF文件的描述）
     */
    private Spine spine;
    /**
     * 前一个章节
     */
    private Chapter prevChapter;
    /**
     * 章节顺序
     */
    private int chapterIndex;
    /**
     * 当前章节排版分页数
     */
    private int pageCount;
    private int chapterPageOffset = -1;
    private int bookPageCount = -1;
    private int textSizeLevel = 0;
    private long eBookId = 0;
    private int blockCount = 0;
    private int docId = 0;
    private String key;
    private String position;
    private String fontPath;
    private String chapterTitle;
    private boolean isNextPage = false;
    private boolean isObservable = false;
    private boolean isFirstChapter = false;
    private boolean isLastChapter = false;
    private boolean isCancelBuildPage = false;
    private boolean isCancelSearchPage = false;
    private boolean isShowAllNotes = false;
    private boolean isLoadAllNotesDone = false;
    private HashMap<String, Integer> idLocation = new HashMap<String, Integer>();
    private HashMap<String, Block> blockMap = new HashMap<String, Block>();
    private HashMap<String, TOCItem> tocMap = new HashMap<String, TOCItem>();
    private HashMap<String, String> tocIdMap = new HashMap<String, String>();
    private HashMap<String, String> anchorMap = new HashMap<String, String>();
    private HashMap<String, List<ReadSearchData>> searchMap = new HashMap<String, List<ReadSearchData>>();
    
    /**
     * 章节初始化
     * @param pageCalculator 排版计算器
     * @param spine 章节描述信息（OPF文件描述信息）
     * @param key 证书信息（Rights）
     * @param pagePool
     * @param tocList
     */
    public Chapter(PageCalculator pageCalculator, Spine spine, String key, PagePool pagePool, List<TOCItem> tocList) {
        this.pageCalculator = pageCalculator;
        this.spine = spine;//章节（OPF文件信息）
        this.key = key;//证书信息（Rights）
        this.pagePool = pagePool;
        this.tocList = tocList;//目录信息
        File item = new File(spine.spinePath);
        //ePub基本路径
        String basePath = item.getParent() + File.separator;
        this.pageCalculator.setBasePath(basePath);
    }
    
    /**
     * 创建页面
     * @param paraIndex
     * @param offset
     * @return
     */
    public Page createPage(int paraIndex, int offset) {
        Page page = pagePool.acquirePage();
        page.initialize(pageCalculator.getPageContext(), this);
        page.setStart(paraIndex, offset);
        return page;
    }
    
    /**
     * 创建页面
     * @param paraIndex
     * @param offset
     * @return
     */
    public Page createPage(String paraIndex, String offset) {
        if (TextUtils.isEmpty(paraIndex) || TextUtils.isEmpty(offset)) {
            return createPage(0, 0);
        }
        String[] startParaStrArray = paraIndex.split(PageCalculator.pageParamDivider);
        String[] startOffsetStrArray = offset.split(PageCalculator.pageParamDivider);
        Page page = createPage(Integer.parseInt(startParaStrArray[0]),Integer.parseInt(startOffsetStrArray[0]));
        page.setMultipleStartPara(true);
        page.setStartParaStr(paraIndex);
        page.setStartOffsetStr(offset);
        return page;
    }
    
    /**
     * 排版分页
     */
    public void doPage() {
    	//是否已经排版完毕 或者取消了排版
        if (isChapterPageReady() || isCancelBuildPage) {
            return;
        }
        if (!isCancelBuildPage) {
            synchronized (this) {
                if (pageList.isEmpty()) {
                    tryToLoadPageCache();
                }
            }
        }
        
        if (pageList.isEmpty()/*是否已经保存了排版数据*/) {
        	//排版分页
            pageList = pageChapter(true, false);
        } else {
        	//不需要进行分页，因为本身已经分好页
            pageChapter(false, false);
        }
        if (isCancelBuildPage) {
            return;
        }
        //得到总页数
        pageCount = pageList.size();
        notifyPageView();
    }

    /**
     * 尝试加载分页缓存信息
     */
	private void tryToLoadPageCache() {
		int localTextSize;
		ReadPageContent pageContent = MZBookDatabase.instance.getPageContent(eBookId, docId, spine.spineIdRef);
		if (pageContent != null) {
		    localTextSize = pageContent.getTextSize();
		    String pageStartPara = null;
		    String pageStartOffset = null;
		    String anchorLocation = null;
		    boolean isLineSpaceChanged = false;
		    boolean isBlockSpaceChanged = false;
		    boolean isPageEdgeWidthChanged = false;
		    // TODO 需要完善横竖屏判断逻辑 --liqiang
		    if (fontPath != null  && fontPath.equals(pageContent.getFontFace())) {
		        pageStartPara = pageContent.getPageStartPara();
		        pageStartOffset = pageContent.getPageStartOffset();
		        anchorLocation = pageContent.getAnchorLocation();
		        isLineSpaceChanged = pageContent.getLineSpace() != pageCalculator.getLineSpace();
		        isBlockSpaceChanged = pageContent.getBlockSpace() != pageCalculator.getBlockSpace();
		        isPageEdgeWidthChanged = pageContent.getPageEdgeSpace() != BookPageViewActivity.getPageMarginLeft();
		    }
		    if (textSizeLevel == localTextSize
		            && !isLineSpaceChanged
		            && !isBlockSpaceChanged
		            && !isPageEdgeWidthChanged
		            && !TextUtils.isEmpty(pageStartPara)
		            && !TextUtils.isEmpty(pageStartOffset)
		            && pageList.isEmpty()) {
		        String[] startPara = pageStartPara.split(";");
		        String[] startOffset = pageStartOffset.split(";");
		        for (int i = 0; i < startPara.length; i++) {
		            if (startPara[i].contains(PageCalculator.pageParamDivider)) {
		                pageList.add(createPage(startPara[i],startOffset[i]));
		            } else {
		                pageList.add(createPage(Integer.valueOf(startPara[i]),Integer.valueOf(startOffset[i])));
		            }
		        }
		    }
		    
		    if (!TextUtils.isEmpty(anchorLocation)) {
		        String[] anchorArray = anchorLocation.split(";");
		        for (int i = 0; i < anchorArray.length; i++) {
		            if (!TextUtils.isEmpty(anchorArray[i])) {
		                String[] anchor = anchorArray[i].split(",");
		                addAnchor(anchor[0], anchor[1], anchor[2]);
		            }
		        }
		    }
		}
	}
    
    /**
     * 针对章节进行分页
     * @param isBuildPage
     * @param isCalculatePageCount
     * @return
     */
    private synchronized List<Page> pageChapter(boolean isBuildPage, boolean isCalculatePageCount) {
        
        if (isChapterPageReady())/*已经排版就绪，直接返回*/ {
            return pageList;
        }
        
        //将目录信息列表整理成Map
        fillTocMap(isBuildPage);
        
        boolean isOpenError = false;
        List<Page> localPageList = null;
        InputStream is = null;
        try {
            if (eBookId != 0) {
                is = JDDecryptUtil.decryptFile(URLDecoder.decode(spine.spinePath, "UTF-8"));
            } else if (docId != 0) {
                is = new FileInputStream(URLDecoder.decode(spine.spinePath, "UTF-8"));
            }
            
            Kit42Node node = null;
            if (!isCancelBuildPage) {
                if (is != null) {
                    node = Kit42.parse(is);//DOM树解析
                }
                if (node == null) {
                    isOpenError = true;
                } else {
                    if (isBuildPage) {
                    	//核心：调用，排版分页
                        localPageList = pageCalculator.buildPageList(node/*DOM树*/, this, tocMap);
                    } else {
                        pageCalculator.buildBlockList(node, this, tocMap);
                    }
                }
            }
        } catch (IOException e) {
            isOpenError = true;
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            isOpenError = true;
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(is);
        }
        
        if (isOpenError && !isCalculatePageCount) {
            this.setChanged();
            this.notifyObservers();
        }
        
        if (!isCancelBuildPage && localPageList != null) {
            cachePagedData(localPageList);//对已经分页好的信息进行缓存
            releasePageInfo(isCalculatePageCount, localPageList);
        }
        if (localPageList == null) {
            return new ArrayList<Page>();
        } else {
            return localPageList;
        }
    }

	private void fillTocMap(boolean isBuildPage) {
		if (tocMap.isEmpty()) {
        	//目录信息列表转化成Map
            for (TOCItem tocItem : tocList) {
                if(null == tocItem || (null != tocItem && null == tocItem.contentSrc) || null == spine) {
                    continue;
                }
                //章节信息
                if (tocItem.contentSrc.contains(spine.spinePath)) {
                    Uri uri = Uri.parse(tocItem.contentSrc);
                    String id = uri.getFragment();
                    if (isBuildPage) {
                        if (TextUtils.isEmpty(id)) {
                            id = tocItem.navLabel;
                            tocItem.pageNumber = 1;
                        } else {
                            tocItem.pageNumber = 0;
                        }
                    }
                    tocMap.put(id, tocItem);
                }
            }
        }
	}

	private void releasePageInfo(boolean isCalculatePageCount, List<Page> localPageList) {
		if (isCalculatePageCount) {
			Collection<Block> blocks = blockMap.values();
		    pagePool.asyncReleaseBlocks(blocks);
		    blockMap.clear();
		    tocIdMap.clear();
			
		    pagePool.asyncReleasePages(localPageList);
		    localPageList.clear();
		    clearPage();
		}
	}

	private void cachePagedData(List<Page> localPageList) {
		blockCount = blockMap.size();//段落列表
		pageCount = localPageList.size();//当前章节页面总数
		StringBuffer startPara = new StringBuffer();
		StringBuffer startOffset = new StringBuffer();
		for (Page page : localPageList) {
		    if (page.isMultipleStartPara()) {
		        startPara.append(page.getStartParaStr());
		        startPara.append(";");
		        startOffset.append(page.getStartOffsetStr());
		        startOffset.append(";");
		    } else {
		        startPara.append(page.getParaIndex());
		        startPara.append(";");
		        startOffset.append(page.getOffsetInPara());
		        startOffset.append(";");
		    }
		}
		StringBuffer anchorPara = new StringBuffer();
		List<String> anchorList = new ArrayList<String>(anchorMap.keySet());
		for (String key : anchorList) {
		    anchorPara.append(key);
		    anchorPara.append(",");
		    anchorPara.append(anchorMap.get(key));
		    anchorPara.append(";");
		}
		if (startPara.length() > 0 && startOffset.length() > 0) {
		    ReadPageContent pageContent = new ReadPageContent();
		    pageContent.setTextSize(textSizeLevel);
		    pageContent.setChapterItemRef(spine.spineIdRef);
		    pageContent.setPageStartPara(startPara.toString());
		    pageContent.setPageStartOffset(startOffset.toString());
		    pageContent.setLineSpace(pageCalculator.getLineSpace());
		    pageContent.setBlockSpace(pageCalculator.getBlockSpace());
		    pageContent.setPageEdgeSpace(BookPageViewActivity.getPageMarginLeft());
		    pageContent.setAnchorLocation(anchorPara.toString());
		    pageContent.setFontFace(fontPath);
		    // TODO 需要完善横竖屏保存逻辑 --liqiang
		    MZBookDatabase.instance.insertOrUpdatePageContent(eBookId,docId, pageContent);
		}
	}
    
    private String decrypt() {
        String text = "";
        if (!TextUtils.isEmpty(key)) {
            InputStream is = null; 
            String k = BookEntity.getK() + key;
            Cipher cipher;
            try {
                is = new FileInputStream(URLDecoder.decode(spine.spinePath, "UTF-8"));
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.reset();
                byte[] byteData = digest.digest(k.getBytes("UTF-8"));
                SecretKeySpec skeySpec = new SecretKeySpec(byteData, "AES");
                byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
                AlgorithmParameterSpec algorithmSpec = new IvParameterSpec(iv);
                cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
                cipher.init(Cipher.DECRYPT_MODE, skeySpec, algorithmSpec);
                String content = IOUtil.readIt(is);
                byte[] contentByte = Base64.decode(content, Base64.DEFAULT);
                byte[] decrypted = cipher.doFinal(contentByte);
                text = new String(decrypted);
            } catch (Exception e2) {
                e2.printStackTrace();
            } finally {
                IOUtil.closeStream(is);
            }
        }
        return text;
    }

    public String queryFootnote(String id) {
        String path = null;
        try {
            path = URLDecoder.decode(spine.spinePath, "UTF-8");
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
            return null;
        }
        InputStream is = null;
        String text = null;
        try {
            if (eBookId > 0) {
                is = JDDecryptUtil.decryptFile(path);
            } else {
                is = new FileInputStream(path);
            }
            InputSource source = new InputSource(is);
            XPathFactory factory = XPathFactory.newInstance(); 
            XPath xpath = factory.newXPath();
            String expression = "//*[@id='" + id + "']";
            text = xpath.compile(expression).evaluate(source);
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            String content = decrypt();
            if (!TextUtils.isEmpty(content)) {
                InputSource source = new InputSource(new StringReader(content));
                XPathFactory factory = XPathFactory.newInstance(); 
                XPath xpath = factory.newXPath();
                String expression = "//*[@id='" + id + "']";
                try {
                    text = xpath.compile(expression).evaluate(source);
                } catch (XPathExpressionException e1) {
                    e1.printStackTrace();
                }
            }
        } finally {
            IOUtil.closeStream(is);
        }
        return text;
    }
    
    private synchronized void notifyPageView() {
        if (pageList.size() == 0) {
            this.setChanged();
            this.notifyObservers();
            return;
        }
        if (isObservable) {
            isObservable = false;
            Page page = null;
            if (isNextPage) {
                page = pageList.get(0);
            } else {
                page = pageList.get(pageCount - 1);
            }
            this.setChanged();
            this.notifyObservers(new Object[] { position, page });
        }
    }
    
    public void addIdLocation(String id, int paraIndex) {
        idLocation.put(id, paraIndex);
    }
    
    public int getIdLocation(String id) {
        Integer idInt = idLocation.get(id);
        if (idInt != null) {
            return idInt.intValue();
        } else {
            return 0;
        }
    }
    
    protected void addTocId(int blockIndex, String id) {
        if (!TextUtils.isEmpty(id)) {
            tocIdMap.put(String.valueOf(blockIndex), id);
        }
    }
    
    protected String getTocId(int blockIndex) {
        return tocIdMap.get(String.valueOf(blockIndex));
    }
    
    protected boolean isTocId(String id) {
        if (!TextUtils.isEmpty(id)) {
            return tocMap.containsKey(id);
        }
        return false;
    }
    
    public String getPageHead(String id) {
        String head = null;
        TOCItem item = tocMap.get(id);
        if (item != null) {
            head = item.navLabel;
        } else {
            head = spine.chapterName;
        }
        //某些书籍读取不到章节
        if (TextUtils.isEmpty(head)) {
            head = chapterTitle;
        }
        if (TextUtils.isEmpty(head) && prevChapter != null) {
            head = prevChapter.getPageHead("");
            chapterTitle = head;
        }
        if (head == null) {
            head = "";
        }
        return head;
    }
    
    @Deprecated
    public int getPlayOrder() {
        return spine.playOrder;
    }
    
    /**
     * 把章节页面索引赋值给TOCItem，不包括章节的偏移
     * @param id  block.id
     * @param pageIndex  页面偏移
     */
    public void setTocPageIndex(String id, int pageIndex) {
        TOCItem item = tocMap.get(id);
        if (item != null) {
            item.pageNumber = pageIndex;
        }
    }
    
    /**
     * 把章节的偏移加到TOCItem
     */
    public void tocPageNumberDone() {
        Iterator<TOCItem> iterator = tocMap.values().iterator();
        while(iterator.hasNext()){
            TOCItem item = iterator.next();
            if (item != null) {
                item.pageNumber += chapterPageOffset;
            }
        }
    }
    
    /**
     * 如果页数大于6页，那么启动线程做回收Page和Block
     * 否则不用线程，这样做使得整体开销最小且效率最大化
     */
    public synchronized void clearPage() {
        if (pageList.size() > 6) {
            pagePool.asyncReleasePages(pageList);
            Collection<Block> blocks = blockMap.values();
            pagePool.asyncReleaseBlocks(blocks);
        } else {
            for (Page page : pageList) {
                pagePool.releasePage(page);
            }
            if (blockMap.size() > 0) {
                Collection<Block> blocks = blockMap.values();
                Iterator<Block> iterator = blocks.iterator();
                while (iterator.hasNext()) {
                    Block block = iterator.next();
                    pagePool.releaseBlock(block);
                }
            }
        }
        pageList.clear();
        blockMap.clear();
        tocIdMap.clear();
        clearAllPeopleNotes();
    }
    
    public void reset() {
        clearPage();
        pageCount = 0;
        chapterPageOffset = -1;
        bookPageCount = -1;
        tocMap.clear();
    }
    
    public void release() {
        reset();
        prevChapter = null;
        idLocation.clear();
        anchorMap.clear();
        searchMap.clear();
        pageCalculator.release();
    }
    
    public void setChapterIndex(int chapterIndex) {
        this.chapterIndex = chapterIndex;
    }

    private void clearAllPeopleNotes() {
        isLoadAllNotesDone = false;
        allPeopleNoteList.clear();
    }

    public boolean isFirstChapter() {
        return isFirstChapter;
    }

    public void setFirstChapter(boolean isFirstChapter) {
        this.isFirstChapter = isFirstChapter;
    }

    public boolean isLastChapter() {
        return isLastChapter;
    }

    public void setLastChapter(boolean isLastChapter) {
        this.isLastChapter = isLastChapter;
    }
    
    public boolean isChapterPageReady() {
        return !pageList.isEmpty() && !blockMap.isEmpty();
    }
    
    public boolean isFirstPage(Page page) {
        if (page == null) {
            return false;
        }
        if (pageList.size() <= 0) {
            return false;
        }
        int index = pageList.indexOf(page);
        return index == 0;
    }
    
    public boolean isLastPage(Page page) {
        if (page == null) {
            return false;
        }
        if (pageList.size() <= 0) {
            return false;
        }
        int index = pageList.indexOf(page);
        return index == (pageList.size() - 1);
    }
    
    public Page getPage(int index) {
        if (index < 0 || index >= pageList.size()) {
            return null;
        }
        return pageList.get(index);
    }
    
    public Page getNextPage(Page page) {
        if (page == null) {
            return null;
        }
        int index = pageList.indexOf(page);
        if (index >= pageList.size() - 1) {
            return null;
        }
        return pageList.get(index + 1);
    }
    
    public Page getPrevPage(Page page) {
        if (page == null) {
            return null;
        }
        int index = pageList.indexOf(page);
        if (index <= 0) {
            return null;
        }
        return pageList.get(index - 1);
    }
    
    public Page getLastPage() {
        if (isChapterPageReady()) {
            return pageList.get(pageList.size() - 1);
        }
        return null;
    }
    
    public Page getFirstPage() {
        if (isChapterPageReady()) {
            return pageList.get(0);
        }
        return null;
    }

    public int getPageCount() {
        return pageCount;
    }
    
    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public Spine getSpine() {
        return spine;
    }
    
    public void setChapterPageOffset(int offset) {
        chapterPageOffset = offset;
    }
    
    public int getChapterPageOffset() {
        return chapterPageOffset;
    }
    
    public int getPageOffset(Page page) {
        if (page == null) {
            return -1;
        }
        int offset = pageList.indexOf(page);
        if (chapterPageOffset == -1) {
            return -1;
        }
        return chapterPageOffset + offset;
    }
    
    public int getBookPageCount() {
        return bookPageCount;
    }

    public void setBookPageCount(int bookPageCount) {
        this.bookPageCount = bookPageCount;
    }
    
    public void addBlock(int key, Block block) {
        blockMap.put(String.valueOf(key), block);
    }
    
    public void addAnchor(String id, String paraIndex, String offsetInPara) {
        if (TextUtils.isEmpty(id)) {
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(paraIndex);
        sb.append(",");
        sb.append(offsetInPara);
        anchorMap.put(id, sb.toString());
    }
    
    public void addAnchor(String id, int paraIndex, int offsetInPara) {
        addAnchor(id, String.valueOf(paraIndex), String.valueOf(offsetInPara));
    }
    
    public String getAnchor(String id) {
        return anchorMap.get(id);
    }

    public Block getBlock(int key) {
        return blockMap.get(String.valueOf(key));
    }

    public void setBlockCount(int count) {
        this.blockCount = count;
    }
    
    public int getBlockCount() {
        return blockCount;
    }
    
    public void setShowAllNotes(boolean isShowAllNotes) {
        this.isShowAllNotes = isShowAllNotes;
    }
    
    public boolean isShowAllNotes() {
        return isShowAllNotes;
    }
    
    public void setLoadAllNotesDone(boolean loadNoteDone) {
        this.isLoadAllNotesDone = loadNoteDone;
    }
    
    public boolean isLoadAllNotesDone() {
        return isLoadAllNotesDone;
    }
    
    public void addAllPeopleNote(List<ReadNote> notes) {
        allPeopleNoteList.addAll(notes);
    }
    
    public List<ReadNote> getAllPeopleNoteList() {
        return allPeopleNoteList;
    }
    
    public void addReadNote(ReadNote note) {
        boolean isNoteMerged = mergeReadNote(note, noteList, true);
        if (!isNoteMerged) {
            noteList.add(note);
        }
    }
    
    public void removeReadNote(int startPara, int startOffset) {
        for (ReadNote note : noteList) {
            if (note.fromParaIndex == startPara && note.fromOffsetInPara == startOffset) {
                note.deleted = true;
                note.modified = true;
                note.updateTime = System.currentTimeMillis();
                break;
            }
        }
    }
    
    public List<ReadNote> getNoteList() {
        return noteList;
    }
    
    public void addReadNote(List<ReadNote> notes) {
        noteList.addAll(notes);
    }
    
    public void refreshReadNote(List<ReadNote> notes) {
        noteList.clear();
        for (ReadNote read : notes) {
            if (read.deleted) {
                continue;
            }
            boolean isNoteMerged = mergeReadNote(read, notes, false);
            if (isNoteMerged) {
                read.deleted = isNoteMerged;
                read.modified = isNoteMerged;
            }
        }
        noteList.addAll(notes);
    }
    
    private boolean isPrevReadNoteScope(ReadNote note, int paraIndex, int offsetInPara) {
        if (note == null || note.deleted) {
            return false;
        }
        if (note.fromParaIndex == note.toParaIndex) {
            if (paraIndex == note.fromParaIndex) {
                if (offsetInPara >= note.fromOffsetInPara
                        && offsetInPara <= note.toOffsetInPara + 1) {
                    return true;
                }
            }
        } else {
            if (paraIndex == note.fromParaIndex) {
                if (offsetInPara >= note.fromOffsetInPara) {
                    return true;
                }
            } else if (paraIndex == note.toParaIndex) {
                if (offsetInPara <= note.toOffsetInPara + 1) {
                    return true;
                }
            } else if (paraIndex > note.fromParaIndex
                    && paraIndex < note.toParaIndex) {
                return true;
            }
        }
        if (offsetInPara == 0 && paraIndex == note.toParaIndex + 1) {
            Block block = getBlock(note.toParaIndex);
            if (block != null) {
                Element lastElement = block.elementList.get(block.elementList.size() - 1);
                int length = lastElement.getCount() + lastElement.offsetInPara;
                if (note.toOffsetInPara == length - 1) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isNextReadNoteScope(ReadNote note, int paraIndex, int offsetInPara) {
        if (note == null || note.deleted) {
            return false;
        }
        if (note.fromParaIndex == note.toParaIndex) {
            if (paraIndex == note.fromParaIndex) {
                if (offsetInPara >= note.fromOffsetInPara - 1
                        && offsetInPara <= note.toOffsetInPara) {
                    return true;
                }
            }
        } else {
            if (paraIndex == note.fromParaIndex) {
                if (offsetInPara >= note.fromOffsetInPara - 1) {
                    return true;
                }
            } else if (paraIndex == note.toParaIndex) {
                if (offsetInPara <= note.toOffsetInPara) {
                    return true;
                }
            } else if (paraIndex > note.fromParaIndex
                    && paraIndex < note.toParaIndex) {
                return true;
            }
        }
        if (note.fromOffsetInPara == 0 && paraIndex == note.fromParaIndex - 1) {
            Block block = getBlock(paraIndex);
            if (block != null) {
                Element lastElement = block.elementList.get(block.elementList.size() - 1);
                int length = lastElement.getCount() + lastElement.offsetInPara;
                if (offsetInPara == length - 1) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    private boolean mergeReadNote(ReadNote note, List<ReadNote> noteList, boolean isAddNote) {
        boolean isNoteMerged = false;
        ReadNote prevNote = null;
        ReadNote nextNote = null;
        String userId = LoginUser.getpin();
        for (ReadNote read : noteList) {
            if (read.deleted) {
                continue;
            }
            if (read.id == note.id) {
                continue;
            }
            if (!userId.equals(read.userId)) {
                continue;
            }
            boolean isPrevScope = isPrevReadNoteScope(read, note.fromParaIndex, note.fromOffsetInPara);
            boolean isNextScope = isNextReadNoteScope(read, note.toParaIndex, note.toOffsetInPara);
            if (isPrevScope && isNextScope) {
                read.contentText += "  "+ note.contentText;
                read.contentText = read.contentText.trim();
                read.modified = true;
                isNoteMerged = true;
                if (isAddNote) {
                    continue;
                } else {
                    break;
                }
            } else if (isPrevScope) {
                prevNote = read;
                isNoteMerged = true;
                if (isAddNote) {
                    continue;
                } else {
                    break;
                }
            } else if (isNextScope) {
                nextNote = read;
                isNoteMerged = true;
                if (isAddNote) {
                    continue;
                } else {
                    break;
                }
            } else {
                isPrevScope = isPrevReadNoteScope(note, read.fromParaIndex, read.fromOffsetInPara);
                isNextScope = isNextReadNoteScope(note, read.toParaIndex, read.toOffsetInPara);
                if (isPrevScope && isNextScope) {
                    read.fromParaIndex = note.fromParaIndex;
                    read.fromOffsetInPara = note.fromOffsetInPara;
                    read.toParaIndex = note.toParaIndex;
                    read.toOffsetInPara = note.toOffsetInPara;
                    read.chapterName = note.chapterName;
                    read.quoteText = note.quoteText;
                    read.contentText += "  "+ note.contentText;
                    read.contentText = read.contentText.trim();
                    read.modified = true;
                    isNoteMerged = true;
                    if (isAddNote) {
                        continue;
                    } else {
                        break;
                    }
                }
            }
        }
        if (prevNote != null && nextNote != null) {
            prevNote.toParaIndex = nextNote.toParaIndex;
            prevNote.toOffsetInPara = nextNote.toOffsetInPara;
            prevNote.contentText += "  " + note.contentText + "  " + nextNote.contentText;
            prevNote.contentText = prevNote.contentText.trim();
            String quote = mergeNoteQuoteText(prevNote.quoteText, note.quoteText);
            quote = mergeNoteQuoteText(quote, nextNote.quoteText);
            prevNote.quoteText = quote;
            prevNote.modified = true;
            nextNote.deleted = true;
            nextNote.modified = true;
        } else if (prevNote != null) {
            prevNote.toParaIndex = note.toParaIndex;
            prevNote.toOffsetInPara = note.toOffsetInPara;
            prevNote.contentText += "  " + note.contentText;
            prevNote.contentText = prevNote.contentText.trim();
            String quote = mergeNoteQuoteText(prevNote.quoteText, note.quoteText);
            prevNote.quoteText = quote;
            prevNote.modified = true;
        } else if (nextNote != null) {
            nextNote.fromParaIndex = note.fromParaIndex;
            nextNote.fromOffsetInPara = note.fromOffsetInPara;
            nextNote.chapterName = note.chapterName;
            nextNote.contentText = note.contentText + "  " + nextNote.contentText;
            nextNote.contentText = nextNote.contentText.trim();
            String quote = mergeNoteQuoteText(note.quoteText, nextNote.quoteText);
            nextNote.quoteText = quote;
            nextNote.modified = true;
        }
        return isNoteMerged;
    }
    
    private String mergeNoteQuoteText(String first, String second) {
        int index = -1;
        for (int i = first.length() - 1; i >= 0; i--) {
            String textEnd = first.substring(i);
            if (second.startsWith(textEnd)) {
                index = i;
                break;
            }
        }
        if (index >= 0) {
            return first.substring(0, index) + second;
        }
        return first + second;
    }
    
    public boolean isBookMarkInPageScope(Page page) {
        if (page == null || bookMarkList == null) {
            return false;
        }
        for (BookMark mark : bookMarkList) {
            if (mark.para_index > page.startParaIndex
                    || (mark.para_index == page.startParaIndex && mark.offset_in_para >= page.startOffset)) {
                Page next = this.getNextPage(page);
                if (next == null) {
                    page.setBookMark(mark);
                    return true;
                } else {
                    if (mark.para_index < next.startParaIndex
                            || (mark.para_index == next.startParaIndex && mark.offset_in_para < next.startOffset)) {
                        page.setBookMark(mark);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public boolean isChapterPage(int pageIndex) {
        if (pageIndex >= chapterPageOffset && pageIndex < chapterPageOffset + pageCount) {
            return true;
        }
        return false;
    }

    public boolean isCancelBuildPage() {
        return isCancelBuildPage;
    }

    public void setCancelBuildPage(boolean isCancelBuildPage) {
        this.isCancelBuildPage = isCancelBuildPage;
        this.pageCalculator.getPageContext().setCancel(isCancelBuildPage);
    }

    public PagePool getPagePool() {
        return pagePool;
    }
    
    public void prepareBlockForReadNoteShare(Page page) {
        int paraIndex = 0;
        while (true) {
            Block block = getBlock(paraIndex);
            if (block == null) {
                break;
            }
            if (paraIndex < page.startParaIndex || paraIndex > page.endParaIndex) {
                block.release();
            }
            if (paraIndex == page.endParaIndex) {
                block.clearElementFromOffset(page.endOffset + 1);// 从笔记结束字符的下一个字符开始清理
            }
            if (paraIndex == page.startParaIndex) {
                block.clearElementToOffset(page.startOffset);
            }
            paraIndex ++;
        }
    }

    public Chapter getClone() {
        Chapter chapter = new Chapter(pageCalculator, spine, key, pagePool, tocList);
        chapter.setBookId(eBookId, docId);
        return chapter;
    }
    
    public void setGlobalPaint(Paint globalPaint) {
        this.pageCalculator.setGlobalPaint(globalPaint);
    }
    
    public void setFontFace(Typeface fontFace) {
        if (fontFace != null) {
            Paint globalPaint = this.pageCalculator.getGlobalPaint();
            globalPaint.setTypeface(fontFace);
        }
    }

    public void setChapterTitle(String title) {
        if (!TextUtils.isEmpty(title)) {
            chapterTitle = title;
        }
    }

    public void setPrevChapter(Chapter chapter) {
        this.prevChapter = chapter;
    }
    
    public ReadSearchData getFirstSearchResult() {
        if (searchMap.size() > 0) {
            for (int index = 0; index < blockCount; index++) {
                List<ReadSearchData> list = searchMap.get(String.valueOf(index));
                if (list != null && list.size() > 0) {
                    return list.get(0);
                }
            }
        }
        return null;
    }
    
    public ReadSearchData getLastSearchResult() {
        if (searchMap.size() > 0) {
            for (int index = blockCount-1; index >= 0; index--) {
                List<ReadSearchData> list = searchMap.get(String.valueOf(index));
                if (list != null && list.size() > 0) {
                    return list.get(list.size()-1);
                }
            }
        }
        return null;
    }
    
    public ReadSearchData findPrevSearchResult(ReadSearchData data) {
        if (searchMap.size() > 0 && data != null) {
            List<ReadSearchData> list = searchMap.get(String.valueOf(data.getParaIndex()));
            if (list != null && list.size() > 1) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    ReadSearchData d = list.get(i);
                    if (d.getStartOffsetInPara() < data.getStartOffsetInPara()) {
                        return d;
                    }
                }
            }
            for (int index = data.getParaIndex() - 1; index >= 0; index--) {
                if (searchMap.containsKey(String.valueOf(index))) {
                    List<ReadSearchData> dataList = searchMap.get(String.valueOf(index));
                    if (dataList != null && dataList.size() > 0) {
                        return dataList.get(dataList.size() - 1);
                    }
                }
            }
        }
        return null;
    }
    
    public ReadSearchData findNextSearchResult(ReadSearchData data) {
        if (searchMap.size() > 0 && data != null) {
            List<ReadSearchData> list = searchMap.get(String.valueOf(data.getParaIndex()));
            if (list != null && list.size() > 1) {
                for (int i = 0; i < list.size(); i++) {
                    ReadSearchData d = list.get(i);
                    if (d.getStartOffsetInPara() > data.getStartOffsetInPara()) {
                        return d;
                    }
                }
            }
            for (int index = data.getParaIndex() + 1; index < blockCount; index++) {
                if (searchMap.containsKey(String.valueOf(index))) {
                    List<ReadSearchData> dataList = searchMap.get(String.valueOf(index));
                    if (dataList != null && dataList.size() > 0) {
                        return dataList.get(0);
                    }
                }
            }
        }
        return null;
    }
    
    public List<ReadSearchData> findSearchResultInPage(Page page) {
        List<ReadSearchData> result = new ArrayList<ReadSearchData>();
        Page nextPage = getNextPage(page);
        if (searchMap.size() > 0) {
            int paraIndex = page.getParaIndex();
            int endParaIndex = paraIndex;
            int endOffsetInPara = 0;
            if (nextPage != null) {
                if (nextPage.getOffsetInPara() == 0) {
                    endParaIndex = nextPage.getParaIndex() - 1;
                    endOffsetInPara = blockMap.get(String.valueOf(endParaIndex)).getContentCount();
                } else {
                    endParaIndex = nextPage.getParaIndex();
                    endOffsetInPara = nextPage.getOffsetInPara();
                }
            } else {
                endParaIndex = blockCount - 1;
                endOffsetInPara = blockMap.get(String.valueOf(endParaIndex)).getContentCount();
            }
            List<ReadSearchData> start = searchMap.get(String.valueOf(paraIndex));
            if (start != null && start.size() > 0) {
                for (ReadSearchData data : start) {
                    if (data.getStartOffsetInPara() > page.getOffsetInPara()
                            || data.getEndOffsetInPara() > page.getOffsetInPara()) {
                        result.add(data);
                    }
                }
            }
            for (int i = paraIndex + 1; i < endParaIndex; i++) {
                List<ReadSearchData> data = searchMap.get(String.valueOf(i));
                if (data != null && data.size() > 0) {
                    result.addAll(data);
                }
            }
            List<ReadSearchData> end = searchMap.get(String.valueOf(endParaIndex));
            if (end != null && end.size() > 0) {
                for (ReadSearchData data : end) {
                    if (data.getStartOffsetInPara() <= endOffsetInPara
                            || data.getEndOffsetInPara() <= endOffsetInPara) {
                        result.add(data);
                    }
                }
            }
        }
        return result;
    }
    
    public void clearKeywordsHighlight() {
        for (String data : searchMap.keySet()) {
            Block block = blockMap.get(data);
            if (block != null) {
                block.clearKeywordsHighlight();
            }
        }
        searchMap.clear();
    }
    
    public ArrayList<ReadSearchData> pageSearch(String keywords) {
        searchMap.clear();
        boolean isRelease = false;
        if (!isChapterPageReady()) {
            isRelease = true;
            InputStream is = null;
            try {
                if (eBookId != 0) {
                    is = JDDecryptUtil.decryptFile(URLDecoder.decode(spine.spinePath, "UTF-8"));
                } else if (docId != 0) {
                    is = new FileInputStream(URLDecoder.decode(spine.spinePath, "UTF-8"));
                }
                Kit42Node node = null;
                if (!isCancelSearchPage) {
                    if (is != null) {
                        node = Kit42.parse(is);
                    }
                    if (!isCancelSearchPage && node != null) {
                        pageCalculator.buildBlockList(node, this, tocMap);
                    }
                }
            } catch (Exception e) {
                
            }
        }
        blockCount = blockMap.size();
        ArrayList<ReadSearchData> resultList = new ArrayList<ReadSearchData>();
        for (int i = 0; i < blockCount; i++) {
            if (isCancelSearchPage) {
                break;
            }
            Block block = blockMap.get(String.valueOf(i));
            if (block != null) {
                String head = getPageHead(getTocId(i));
                List<ReadSearchData> list = block.searchWords(keywords, chapterIndex, i, head);
                if (list != null && list.size() > 0) {
                    resultList.addAll(list);
                    searchMap.put(String.valueOf(i), list);
                }
            }
        }
        if (isRelease) {
            clearPage();
        }
        if (isCancelSearchPage) {
            searchMap.clear();
        }
        
        return resultList;
    }
    
    public List<ReadSearchData> getReadSearchList() {
        List<ReadSearchData> searchDataList = new ArrayList<ReadSearchData>();
        for (int i = 0; i < blockCount; i++) {
            List<ReadSearchData> list = searchMap.get(String.valueOf(i));
            if (list != null) {
                searchDataList.addAll(list);
            }
        }
        return searchDataList;
    }
    
    public void setCancelSearchPage(boolean isCancelSearchPage) {
        this.isCancelSearchPage = isCancelSearchPage;
    }
    
    public void loadBookMark(String userId) {
        bookMarkList = MZBookDatabase.instance.getAllBookMarksOfChapterItemref(userId, eBookId, docId, spine.spineIdRef);
    }
    
    protected void addBookMark(BookMark bookMark) {
        if (bookMarkList != null) {
            bookMarkList.add(bookMark);
        }
    }
    
    protected void removeBookMark(BookMark bookMark) {
        if (bookMarkList != null) {
            bookMarkList.remove(bookMark);
        }
    }
    
    protected Paint getGlobalPaint() {
        return pageCalculator.getGlobalPaint();
    }
    
    protected float getPageWidth() {
        return pageCalculator.getPageWidth();
    }
    
    public void setObservable(int position, boolean isNextPage) {
        isObservable = true;
        this.isNextPage = isNextPage;
        this.position = String.valueOf(position);
    }
    
    public void setBookId(long eBookId, int docId) {
        this.eBookId = eBookId;
        this.docId = docId;
    }
    
    public long geteBookId() {
        return eBookId;
    }

    public int getDocId() {
        return docId;
    }

    public void setTextSizeLevel(int textSizeLevel) {
        this.textSizeLevel = textSizeLevel;
    }
    
    public void setFontPath(String path) {
        this.fontPath = path;
    }
    
    public void setBaseTextSize(float pxPerEm) {
        pageCalculator.setBaseTextSize(pxPerEm);
    }
    
    public void setPageWidth(float width) {
        pageCalculator.setPageWidth(width);
    }
    
    public void setPageHeight(float height) {
        pageCalculator.setPageHeight(height);
    }
    
    protected void buildPage(Page page) {
        pageCalculator.buildPageContent(this, page);
    }
    
    public void buildBlock() {
        pageChapter(false, false);
    }

    public int calculatePageCount() {
        if (pageCount != 0  || isCancelBuildPage) {
            return pageCount;
        }
        pageChapter(true, true);
        return pageCount;
    }
}
