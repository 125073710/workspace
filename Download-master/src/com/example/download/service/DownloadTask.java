package com.example.download.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.example.download.db.ThreadDAO;
import com.example.download.db.ThreadDAOImple;
import com.example.download.entitis.FileInfo;
import com.example.download.entitis.ThreadInfo;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class DownloadTask {
	private Context mComtext = null;
	private FileInfo mFileInfo = null;
	private ThreadDAO mDao = null;
	private int mFinished = 0;
	public boolean mIsPause = false;

	public DownloadTask(Context comtext, FileInfo fileInfo) {
		super();
		this.mComtext = comtext;
		this.mFileInfo = fileInfo;
		this.mDao = new ThreadDAOImple(mComtext);
	}
	
	public void download(){
		//从数据库获取到下载信息
		List<ThreadInfo> list = mDao.queryThreads(mFileInfo.getUrl());
		ThreadInfo info = null;
		if (list.size() == 0) {
			info = new ThreadInfo(0, mFileInfo.getUrl(), 0, mFileInfo.getLength(), 0);
		}else{
			info= list.get(0);
			 
		}
		new DownloadThread(info).start();
	}

	class DownloadThread extends Thread {
		private ThreadInfo threadInfo = null;

		public DownloadThread(ThreadInfo threadInfo) {
			this.threadInfo = threadInfo;
		}

		@Override
		public void run() {
			//如果数据库不存在下载信息，添加下载信息
			if (!mDao.isExists(threadInfo.getUrl(), threadInfo.getId())) {
				mDao.insertThread(threadInfo);
			}
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			InputStream is = null;
			try {
				URL url = new URL(mFileInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5 * 1000);
				conn.setRequestMethod("GET");

				int start = threadInfo.getStart() + threadInfo.getFinished();
				// 设置下载文件开始到结束的位置
				conn.setRequestProperty("Range", "bytes=" + start + "-" + threadInfo.getEnd());
				File file = new File(DownloadService.DownloadPath, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				raf.seek(start);
				mFinished += threadInfo.getFinished();
				
				
				int code = conn.getResponseCode();
				if (code == HttpURLConnection.HTTP_PARTIAL) {
					is = conn.getInputStream();
					byte[] bt = new byte[1024];
					int len = -1;
					// 定义UI刷新时间
					long time = System.currentTimeMillis();
					while ((len = is.read(bt)) != -1) {
						raf.write(bt, 0, len);
						mFinished += len;
						// 设置500毫秒更新一次
						if (System.currentTimeMillis() - time > 00) {
							time = System.currentTimeMillis();
							
							Intent intent = new Intent(DownloadService.ACTION_UPDATE);
							intent.putExtra("finished", mFinished * 100 / mFileInfo.getLength());
							Log.e("ybf", "[已下载]"+mFinished + "[文件大小]"+ mFileInfo.getLength() + "");
							// 发送广播給Activity
							mComtext.sendBroadcast(intent);
						}
						if (mIsPause) {
							mDao.updateThread(threadInfo.getUrl(), threadInfo.getId(), mFinished);
							return;
						}
					}
				}
				// 下載完成后，刪除數據庫信息
				mDao.deleteThread(threadInfo.getUrl(), threadInfo.getId());
				Log.i("DownloadTask", "下载完毕");

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
				try {
					if (is != null) {
						is.close();
					}
					if (raf != null) {
						raf.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			super.run();
		}
	}
}
