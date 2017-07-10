package com.jingdong.app.reader.timeline.model.core;

public interface BookNoteInterface {

	public String getTitle();
	
	public String getContent();

	public void setContent(String content);
	
	public String getQuote();

	public void setQuote(String quote);

	public String getChapterName();

	public boolean isPrivate();

	public void setPrivate(boolean isPrivate);

	public long getWrittenTime();
}