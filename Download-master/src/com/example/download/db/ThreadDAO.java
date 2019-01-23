package com.example.download.db;

import java.util.List;

import com.example.download.entitis.ThreadInfo;

/**
 * 数据库操作接口
 *
 */
public interface ThreadDAO {
	//插入线程
	public void insertThread(ThreadInfo info);
	//删除线程
	public void deleteThread(String url, int thread_id);
	//更新线程
	public void updateThread(String url, int thread_id, int finished);
	
	//查询线程
	public List<ThreadInfo> queryThreads(String url);
	//判断线程是否存在
	public boolean isExists(String url, int threadId);
}
