package com.jingdong.app.reader.download.db;

import java.util.List;

import com.jingdong.app.reader.download.entity.ThreadInfo;


/**
 * 数据访问接口
 * @author Beyond
 *
 */
public interface ThreadDAO {

	/**
	 * 插入线程信息
	 * @param threadInfo
	 */
	public void insertThreadInfo(ThreadInfo threadInfo);
	
	/**
	 * 删除线程
	 * @param url
	 * @param thread_id
	 */
	public void deleteThread(String url);
	
	/**
	 * 更新线程下载进度
	 * @param url
	 * @param thread_id
	 * @param finished
	 */
	public void updateThread(String url, int thread_id,long finished,int state);
	
	/**
	 * 查询文件的线程信息
	 * @param url
	 * @return
	 */
	public List<ThreadInfo> getThreads(String url);
	
	/**
	 * 
	 * @Title: getThread
	 * @Description: 获取单个线程信息
	 * @param @param thread_id
	 * @param @return
	 * @return ThreadInfo
	 * @throws
	 */
	public ThreadInfo getThread(int thread_id);
	
	/**
	 * 线程信息是否存在
	 * @param url
	 * @param thread_id
	 * @return
	 */
	public boolean isExists(String url,int thread_id);
	
}
