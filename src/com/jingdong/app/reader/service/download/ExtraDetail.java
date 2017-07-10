package com.jingdong.app.reader.service.download;

import java.io.Serializable;

import com.jingdong.app.reader.book.Book;
import com.jingdong.app.reader.book.DocBind;
import com.jingdong.app.reader.book.SerializableBook;

/**
 * 
 * 请根据下载的类型 合理设置参数 如果是其他文件类型 不需要参数 可以不用处理
 * @author WANGGUODONG
 *
 */
public class ExtraDetail implements Serializable{
	//document 需要的数据开始
	public String bookName;
	public int serverId;
	public DocBind docBind;//可选操作 如果需要下载并使用先前的绑定信息 则设置这个值
	//document 需要的数据结束
	
	//ebook 需要的数据开始
	public SerializableBook book;	
	long purchaseTime;
	int  edition;
	//ebook 需要的数据结束

	public String getBookName() {
		return bookName;
	}

	public DocBind getDocBind() {
		return docBind;
	}

	public void setDocBind(DocBind docBind) {
		this.docBind = docBind;
	}

	public void setBookName(String bookName) {
		this.bookName = bookName;
	}

	public int getServerId() {
		return serverId;
	}

	public void setServerId(int serverId) {
		this.serverId = serverId;
	}

	public SerializableBook getBook() {
		return book;
	}

	public void setBook(SerializableBook book) {
		this.book = book;
	}

	public long getPurchaseTime() {
		return purchaseTime;
	}

	public void setPurchaseTime(long purchaseTime) {
		this.purchaseTime = purchaseTime;
	}

	public int getEdition() {
		return edition;
	}

	public void setEdition(int edition) {
		this.edition = edition;
	}
	
}
