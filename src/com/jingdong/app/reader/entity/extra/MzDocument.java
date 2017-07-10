package com.jingdong.app.reader.entity.extra;
class Book{
	long bookId = -1;
	String title = "";
	String author = "";
	String imgUrl = "";
}

public class MzDocument {
	public long id;// server_id
	public String name;
	public String user_pin;
	public int need_bind;// bind 1:已经绑定 -1 不绑定 0 初始状态
	public String type;
	public String sign;
	public long size;
	// 书籍如果绑定后会有以下数据
	public Book book;

	public static LocalDocument MzDocumentToLocalDocument(MzDocument document){
		
		LocalDocument doc=new LocalDocument();
		
		doc.server_id=document.id;
		doc.title=document.name;
		doc.user_id=document.user_pin;
		doc.bind=document.need_bind;// bind 1:已经绑定 -1 不绑定 0 初始状态
		doc.format=document.type.equals("pdf")?1:2;
		doc.opf_md5=document.sign;
		doc.size=document.size;
		// 书籍如果绑定后会有以下数据
		if(null!=document.book){
		doc.bookid =document.book.bookId;
		doc.serverTitle =document.book.title;
		doc.serverAuthor =document.book.author;
		doc.serverImageUrl=document.book.imgUrl;}
		return doc;
	}
	
}
