package com.jingdong.app.reader.reading;

public class ReadingData {

	int _id;
	long ebook_id = 0;
	int document_id = 0;//本地documentId

	long start_time;
	String start_chapter;
	int start_para_idx;
	int start_pdf_page;

	long end_time;
	String end_chapter;
	int end_para_idx;
	int end_pdf_page;
	long length;
	
	String userId = "";
	long docBindId = 0;//document serverId

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	public long getEbook_id() {
		return ebook_id;
	}

	public void setEbook_id(long ebook_id) {
		this.ebook_id = ebook_id;
	}

	public int getDocument_id() {
		return document_id;
	}

	public void setDocument_id(int document_id) {
		this.document_id = document_id;
	}

	public long getStart_time() {
		return start_time;
	}

	public void setStart_time(long start_time) {
		this.start_time = start_time;
	}

	public String getStart_chapter() {
		return start_chapter;
	}

	public void setStart_chapter(String start_chapter) {
		this.start_chapter = start_chapter;
	}

	public int getStart_para_idx() {
		return start_para_idx;
	}

	public void setStart_para_idx(int start_para_idx) {
		this.start_para_idx = start_para_idx;
	}

	public long getEnd_time() {
		return end_time;
	}

	public void setEnd_time(long end_time) {
		this.end_time = end_time;
	}

	public String getEnd_chapter() {
		return end_chapter;
	}

	public void setEnd_chapter(String end_chapter) {
		this.end_chapter = end_chapter;
	}

	public int getEnd_para_idx() {
		return end_para_idx;
	}

	public void setEnd_para_idx(int end_para_idx) {
		this.end_para_idx = end_para_idx;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getStart_pdf_page() {
		return start_pdf_page;
	}

	public void setStart_pdf_page(int start_pdf_page) {
		this.start_pdf_page = start_pdf_page;
	}

	public int getEnd_pdf_page() {
		return end_pdf_page;
	}

	public void setEnd_pdf_page(int end_pdf_page) {
		this.end_pdf_page = end_pdf_page;
	}

	public long getDocBindId() {
		return docBindId;
	}

	public void setDocBindId(long docBindId) {
		this.docBindId = docBindId;
	}

}
