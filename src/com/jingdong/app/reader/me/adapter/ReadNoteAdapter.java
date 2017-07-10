package com.jingdong.app.reader.me.adapter;

import android.text.TextUtils;

import com.jingdong.app.reader.reading.ReadNote;
import com.jingdong.app.reader.timeline.model.core.BookNoteInterface;

public class ReadNoteAdapter implements BookNoteInterface {
	private ReadNote readNote;
	private String content;
	private String quote;
	public ReadNoteAdapter(ReadNote readNote) {
		this.readNote = readNote;
	}

	/**
	 * 这个要返回书名，不过对于本地笔记，由于list的点击事件不会跳转到详情界面，所以这个字段不会被读取。
	 */
	@Override
	public String getTitle() {
		return "";
	}

	@Override
	public String getQuote() {
		if(TextUtils.isEmpty(quote))
			quote=readNote.quoteText;
		return quote;
	}

	@Override
	public void setQuote(String quote) {
		this.quote=quote;
	}
	
	@Override
	public String getContent() {
		if(TextUtils.isEmpty(content))
			content=readNote.contentText;
		return content;
	}

	@Override
	public void setContent(String content) {
		this.content=content;
	}
	
	@Override
	public String getChapterName() {
		return readNote.chapterName;
	}

	@Override
	public boolean isPrivate() {
		return readNote.isPrivate;
	}

	@Override
	public long getWrittenTime() {
		return readNote.updateTime;
	}

	@Override
	public void setPrivate(boolean isPrivate) {
		readNote.isPrivate = isPrivate;
	}

	public String getChapterItemRef() {
		return readNote.spineIdRef;
	}

	public int getParaIndex() {
		return readNote.fromParaIndex;
	}

	public int getOffsetInPara() {
		return readNote.fromOffsetInPara;
	}

}
