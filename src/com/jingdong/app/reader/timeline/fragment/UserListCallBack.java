package com.jingdong.app.reader.timeline.fragment;

import com.jingdong.app.reader.book.Document;
import com.jingdong.app.reader.net.url.QueryUrlGetter;
import com.jingdong.app.reader.user.UserInfo;

public interface UserListCallBack {
	/**
	 * 该方法在UserListFragment中某一行发生点击事件的时候被回调。
	 * 
	 * @param userInfo
	 *            被点击行所对应的userInfo对象
	 */
	public void onItemClick(UserInfo userInfo);

	public void onItemClick(Document document);

	/**
	 * 该方法会在设置当前fragment的Model时被回调，且仅回调一次。
	 * 
	 * @return 一个QueryUrlGetter对象。该UrlGetter对象负责设置一组URL。如果不需要实现搜索功能中，
	 *         QueryUrlGetter中的getQueryInitPageUrl和getQueryNextPageUrl可以不实现
	 *         。这个方法可以不实现，并添加fragment.setArugment(SHOW_SEARCH_VIEW,false)
	 */
	public QueryUrlGetter getUsersUrlGetter();
}

