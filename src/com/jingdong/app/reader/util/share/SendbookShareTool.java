package com.jingdong.app.reader.util.share;

/**
 * 购书送书分享类
 * @author tanmojie
 *
 */
public class SendbookShareTool {
	
	private static SendbookShareTool mInstance;
	public static SendbookShareTool getInstance() {
		if (mInstance == null) {
			mInstance = new SendbookShareTool();
		}
		return mInstance;
	}

	public void showBuyerSharePopwindow(){
		
	}
	
}
