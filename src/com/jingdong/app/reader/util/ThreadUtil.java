package com.jingdong.app.reader.util;


public class ThreadUtil {
	/*
	 * @author xiawei
	 * 
	 * @
	 */
	/***********
	 * @author ThinkinBunny
	 * @since 2012-8-27
	 * @param runInThread
	 * 
	 * ******/
	public static void runInThread(final Runnable runnable) {
		new Thread() {
			@Override
			public void run() {
				// TODO 自动生成的方法存根

				runnable.run();
				// super.run();
			}
		}.start();

	}

}
