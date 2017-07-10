package com.jingdong.app.reader.eventbus.event;

public class MessageEvent {

	
	private int refreshItemIndex =-1;
	
	public MessageEvent(int index){
		refreshItemIndex=index;
	}
	
	public  int getRefreshItemIndex(){
		return refreshItemIndex;
	}
	
}
