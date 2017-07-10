package com.jingdong.app.reader.util.share;

public interface ShareResultListener {

	public static final int SHARE_SUCCESS = 1;
	public static final int SHARE_CANCEL = 2;
	public static final int SHARE_FAILURE = 3;
	
	public void onShareRusult(int resultType);
}
