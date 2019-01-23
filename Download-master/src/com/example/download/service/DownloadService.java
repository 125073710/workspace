package com.example.download.service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.download.entitis.FileInfo;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

/**
 * 下载服务，用于执行下载任务，并且将下载进度传到Activity中
 */
public class DownloadService extends Service {

	public static final String ACTION_START = "ACTION_START";
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
	InitThread thread;
	/**
	 * 文件的保存路徑
	 */
	//public static final String DownloadPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/download/";
	public static final String DownloadPath =  "/storage/usbotg/download/";
	public static final int MSG_INIT = 0;
	private DownloadTask mTask = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// 获得Activity传来的参数
		if (ACTION_START.equals(intent.getAction())) {
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			Log.e("ybf", "START[文件信息]" + fileInfo.toString());
			 thread = new InitThread(fileInfo);//初始化下载
			thread.start();
		} else if (ACTION_STOP.equals(intent.getAction())) {
			FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
			Log.e("ybf", "STOP[文件信息]" + fileInfo.toString());
			if (mTask != null) {
				mTask.mIsPause = true;
				thread.interrupt();
			}
		}
		return super.onStartCommand(intent, flags, startId);
	}

	// 從InitThread綫程中獲取FileInfo信息，然後開始下載任務
	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_INIT:
				FileInfo fileInfo = (FileInfo) msg.obj;
				Log.e("ybf", "INIT:" + fileInfo.toString());
				// 获取到FileInfo对象，開始下載任務  开始下载逻辑
				mTask = new DownloadTask(DownloadService.this, fileInfo);
				mTask.download();
				break;
			}
		};
	};

	//初始化下载线程，获取下载文件的信息
	class InitThread extends Thread {
		private FileInfo mFileInfo = null;

		public InitThread(FileInfo mFileInfo) {
			super();
			this.mFileInfo = mFileInfo;
		}

		@Override
		public void run() {
			HttpURLConnection conn = null;
			RandomAccessFile raf = null;
			try {
				URL url = new URL(mFileInfo.getUrl());
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5 * 1000);
				conn.setRequestMethod("GET");
				int code = conn.getResponseCode();
				Log.e("ybf", "code-->"+code);
				int length = -1;
				if (code == HttpURLConnection.HTTP_OK) {
					length = conn.getContentLength();
					Log.e("ybf", "[文件大小]="+length/1024/1024+"M");
				}
				//如果文件长度为小于0，表示获取文件失败，直接返回
				if (length <= 0) {
					return;
				}
				// 判斷文件路徑是否存在，不存在這創建
				//判断文件路径是否存在，不存在创建
				File dir = new File(DownloadPath);
				if (!dir.exists()) {
					dir.mkdir();
				}
				// 创建本地文件
				File file = new File(dir, mFileInfo.getFileName());
				raf = new RandomAccessFile(file, "rwd");
				raf.setLength(length);
				Log.e("ybf", "[raf.length]="+length);
				//设置文件长度
				mFileInfo.setLength(length);
				Log.e("ybf", "[mFileInfo.length]="+length);
				//将FileInfo对象传递给Handler
				Message msg = Message.obtain();
				msg.obj = mFileInfo;
				msg.what = MSG_INIT;
				mHandler.sendMessage(msg);
//				msg.setTarget(mHandler);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
				try {
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