package com.android.mzbook.sortview.model;

import java.io.Serializable;

import com.jingdong.app.reader.client.DownloadedAble;
import com.jingdong.app.reader.entity.LocalBook;
/**
 * 书架拖动布局
 * @author WANGGUODONG 
 * time:2014 -7 17
 */
public class BookShelfModel implements Serializable {
	
	public static final String FREE_GIFTS="free_gifts";
	public static final String MORE="more";
	public static final String FOLDER="folder";
	public static final String DOCUMENT="document";
	public static final String EBOOK="ebook";
	
	public static final int LABEL_TRYREAD=101;
	public static final int LABEL_BORROWED=102;
	public static final int LABEL_CHANGDU=103;
	public static final int LABEL_USER_BORROWED=104;
	
	int id;
	int bookid;// 书籍或文件夹id 本地id
	long serverid; //server_id, Ebook为bookid，document为docbind表的server_id
	String bookName;
	String bookType;// ebook document folder
	String author;
	double modifiedTime; //加入书架时间
	double percentTime; //进度时间
	

	/**
	 * 图书封面
	 */
	String bookCover;
	int belongDirId;//
	// 2014/7/29 添加阅读进度 和文件夹书籍数目统计
	float bookPercent;// 当前书籍的阅读进度
	int dirBookCount;// booktype==folder 统计书籍数目

	
	//2015/1/7 添加封面下载状态
	long book_size;//书籍大小
	long download_progress;//书籍下载大小
	int download_state;//下载状态
	int note_num;//笔记数
    private boolean isDownloaded = true;//表示书籍是否下载完成 
    /** 图书文件下载类型 */
	private DownLoadType downloadType = DownLoadType.Normal;
	/** 开始下载标识，处理点击下载后初始状态显示暂停状态问题 */
	public boolean startDownload=false;
	
	/**
	 * 图书状态
	 *
	 */
	public static enum DownLoadType {
		Normal,			
		DownLoad, 		//下载 
		Update, 		//更新
		Buyed,			//已购
		DownLoading,	//下载中
		Finish			//完成
	}
    
    //2015-1-31 添加角标
    int bookCoverLabel;//角标
    
    long document_bookId=-1;//document 在docbind中的bookid
	

	public long getDocument_bookId() {
		return document_bookId;
	}

	public void setDocument_bookId(long document_bookId) {
		this.document_bookId = document_bookId;
	}

	public int getBookCoverLabel() {
		return bookCoverLabel;
	}

	public void setBookCoverLabel(int bookCoverLabel) {
		this.bookCoverLabel = bookCoverLabel;
	}

	public int getBelongDirId() {
		return belongDirId;
	}

	public void setBelongDirId(int belongDirId) {
		this.belongDirId = belongDirId;
	}

	public int getBookid() {
		return bookid;
	}

	public void setBookid(int bookid) {
		this.bookid = bookid;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getBookName() {
		return bookName;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public String getBookType() {
		return bookType;
	}

	public void setBookType(String bookType) {
		this.bookType = bookType;
	}

	public double getModifiedTime() {
		return modifiedTime;
	}

	public void setModifiedTime(double modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	public String getBookCover() {
		return bookCover;
	}

	public void setBookCover(String bookCover) {
		this.bookCover = bookCover;
	}

	public float getBookPercent() {
		return bookPercent;
	}

	public void setBookPercent(float bookPercent) {
		this.bookPercent = bookPercent;
	}

	public int getDirBookCount() {
		return dirBookCount;
	}

	public void setDirBookCount(int dirBookCount) {
		this.dirBookCount = dirBookCount;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}


	
	public long getBook_size() {
		return book_size;
	}

	public void setBook_size(long book_size) {
		this.book_size = book_size;
	}

	public long getDownload_progress() {
		return download_progress;
	}

	public void setDownload_progress(long download_progress) {
		this.download_progress = download_progress;
	}

	public int getDownload_state() {
		return download_state;
	}

	public void setDownload_state(int download_state) {
		this.download_state = download_state;
	}

	public int getNote_num() {
		return note_num;
	}

	public void setNote_num(int note_num) {
		this.note_num = note_num;
	}

	public boolean isDownloaded() {
		return this.download_state==DownloadedAble.STATE_LOADED;
	}

	public void setDownloaded(boolean isDownloaded) {
		this.isDownloaded = isDownloaded;
	}
	
	public double getPercentTime() {
		return percentTime;
	}

	public void setPercentTime(double percentTime) {
		this.percentTime = percentTime;
	}

	public long getServerid() {
		return serverid;
	}

	public void setServerid(long serverid) {
		this.serverid = serverid;
	}
	
	public DownLoadType getDownloadType() {
		return this.downloadType;
	}
	
	public void setDownloadType(DownLoadType type) {
		this.downloadType = type;
	}
	
}
