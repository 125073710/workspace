package com.example.download.db;

import java.util.ArrayList;
import java.util.List;

import com.example.download.entitis.ThreadInfo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 *数据库增删改查的实现类
 */
public class ThreadDAOImple implements ThreadDAO {
	private DBHelper dbHelper = null;

	public ThreadDAOImple(Context context) {
		super();
		this.dbHelper = new DBHelper(context);
	}

	// 插入线程
	@Override
	public void insertThread(ThreadInfo info) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		ContentValues values = new ContentValues();
		values.put("thread_id", info.getId());
		values.put("url", info.getUrl());
		values.put("start", info.getStart());
		values.put("end", info.getEnd());
		values.put("finished", info.getFinished());
		db.insert("thread_info", null, values);
		
		db.close();
	}

	// 删除线程
	@Override
	public void deleteThread(String url, int thread_id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		db.delete("thread_info", "url = ? and thread_id = ?", new String[] { url, thread_id + "" });
		
		db.close();

	}

	// 更新线程
	@Override
	public void updateThread(String url, int thread_id, int finished) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		db.execSQL("update thread_info set finished = ? where url = ? and thread_id = ?",
				new Object[]{finished, url, thread_id});
		db.close();
	}

	// 查询线程
	@Override
	public List<ThreadInfo> queryThreads(String url) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		
		List<ThreadInfo> list = new ArrayList<ThreadInfo>();
		
		Cursor cursor = db.query("thread_info", null, "url = ?", new String[] { url }, null, null, null);
		while (cursor.moveToNext()) {
			ThreadInfo thread = new ThreadInfo();
			thread.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
			thread.setUrl(cursor.getString(cursor.getColumnIndex("url")));
			thread.setStart(cursor.getInt(cursor.getColumnIndex("start")));
			thread.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
			thread.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
			list.add(thread);
		}
		
	
		cursor.close();
		db.close();
		return list;
	}

	//  判断线程是否为空
	@Override
	public boolean isExists(String url, int thread_id) {
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		Cursor cursor = db.query("thread_info", null, "url = ? and thread_id = ?", new String[] { url, thread_id + "" },
				null, null, null);
		boolean exists = cursor.moveToNext();
		
		db.close();
		db.close();
		return exists;
	}

}
