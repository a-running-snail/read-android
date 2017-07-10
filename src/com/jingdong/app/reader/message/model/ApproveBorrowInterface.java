package com.jingdong.app.reader.message.model;

import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.privateMsg.DocumentRequest;
import com.jingdong.app.reader.util.TaskStatus;

public interface ApproveBorrowInterface {
	public TaskStatus approveBorrow(Document document, DocumentRequest docRequest);
	public TaskStatus getDownloadLink(long messageId,String bookName);
}
