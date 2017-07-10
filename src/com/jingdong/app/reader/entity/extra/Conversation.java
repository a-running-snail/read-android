package com.jingdong.app.reader.entity.extra;

import java.io.Serializable;
import java.util.List;

public class Conversation implements Serializable{

	private List<ConversationItem> conversations;

	public List<ConversationItem> getConversationItem() {
		return conversations;
	}

	public void setConversationItem(List<ConversationItem> conversations) {
		this.conversations = conversations;
	}
}
