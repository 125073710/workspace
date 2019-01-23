package com.example.download;

import com.example.download.entitis.FileInfo;
import com.example.download.service.DownloadService;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private TextView fileName;
	private Button startButton;
	private Button stopButton;
	private ProgressBar downloadProgress;
	private String urlstr = "http://sw.bos.baidu.com/sw-search-sp/software/b2dd2f4e9506d/Baidu_Setup_6676_3.3.0.3153_10000001.exe";
	private FileInfo fileInfo;
	private Intent intent;
	private UIRecive mRecive;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 初始化控件
		fileName = (TextView) findViewById(R.id.file_textview);
		downloadProgress = (ProgressBar) findViewById(R.id.progressBar2);
		startButton = (Button) findViewById(R.id.start_button);
		stopButton = (Button) findViewById(R.id.stop_button);
		downloadProgress.setMax(100);
		// 初始化文件对象
		fileInfo = new FileInfo(0, urlstr, getfileName(urlstr), 0, 0);

		startButton.setOnClickListener(this);
		stopButton.setOnClickListener(this);

		intent = new Intent(MainActivity.this, DownloadService.class);
		mRecive = new UIRecive();

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(DownloadService.ACTION_UPDATE);
		registerReceiver(mRecive, intentFilter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.start_button:
			// 開啓服務
			fileName.setText(getfileName(urlstr));
			intent.setAction(DownloadService.ACTION_START);
			intent.putExtra("fileInfo", fileInfo);
			startService(intent);

			break;
		case R.id.stop_button:
			intent.setAction(DownloadService.ACTION_STOP);
			intent.putExtra("fileInfo", fileInfo);
			startService(intent);
			break;
		}

	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mRecive);
		super.onDestroy();
	}

	// 從URL地址中獲取文件名，即/後面的字符
	private String getfileName(String url) {

		return urlstr.substring(urlstr.lastIndexOf("/") + 1);
	}

	// 從DownloadTadk中獲取廣播信息，更新進度條
	class UIRecive extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (DownloadService.ACTION_UPDATE.equals(intent.getAction())) {
				int finished = intent.getIntExtra("finished", 0);
				downloadProgress.setProgress(finished);

			}
		}

	}

}
