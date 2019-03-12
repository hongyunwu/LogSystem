package com.why.log.io;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.why.log.cache.LogCache;
import com.why.log.contract.IWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/4.
 */

public class LogWriter extends BaseWriter implements IWriter {

	private static final boolean DEFAULT_WRITE = false;

	public static boolean WRITE_LOG = DEFAULT_WRITE;
	private Context mContext;

	private FileManager mFileManager;

	private LogWriter(Context context) {
		super("log-writer");
		mContext = context;
		//初始化file manager
		mFileManager = LogCache.getFileManager();

	}

	private static LogWriter mLogWriter;

	public static LogWriter getInstance(Context context) {
		if (mLogWriter == null) {
			synchronized (LogWriter.class) {
				if (mLogWriter == null) {
					mLogWriter = new LogWriter(context);
				}
			}
		}
		return mLogWriter;
	}

	public void write(final String fileName, final String log, final boolean append) {
		final String time = mFileManager.getTime();
		mLogWriteThread.getHandler().post(new Runnable() {
			@Override
			public void run() {

				try {
					File file = LogCache.getFile(mFileManager.getFilePath() + "-" + fileName);
					boolean reCreate = false;
					if (file != null && file.exists()) {
						mFileManager.checkCreateTime(file);
					} else {
						Log.w("LogWriter", "file is null or not exists");
						reCreate = true;
					}

					BufferedWriter writer = getStream(fileName, append, reCreate);
					if (writer == null) {
						Log.w("LogWriter", "writer stream is null");
						return;
					}
					writer.write(time);
					writer.write(log);
					writer.write("\n");//换行
					writer.flush();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					Log.e("LogWriter", "IO流创建失败:" + e.getMessage());
					return;

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
					Log.e("LogWriter", "编码出错:" + e.getMessage());
					return;
				} catch (IOException e) {
					e.printStackTrace();
					Log.e("LogWriter", "write failed:" + e.getMessage());
					return;
				}
			}
		});
	}

	BufferedWriter mStreamWriter = null;

	@Nullable
	private BufferedWriter getStream(String fileName, boolean append, boolean reCreate) throws FileNotFoundException, UnsupportedEncodingException {


		if (mStreamWriter == null || reCreate) {
			Log.i("LogWriter", "getStream-> stream is null");
			mStreamWriter = LogCache.getStream(fileName);
			if (mStreamWriter == null || reCreate) {
				//TODO进行日志读写操作
				String filePath = mFileManager.getFilePath();
				Log.i("LogWriter", "filePath:" + filePath);
				File file = LogCache.getFile(filePath + "-" + fileName);
				if (file == null) {
					file = new File(filePath, fileName);
					LogCache.putFile(filePath + "-" + fileName, file);
				}

				if (!mFileManager.createFileIfNeed(file, append)) {
					Log.i("LogWriter", "getStream-> create file failed");
					return null;
				}

				FileOutputStream fos = new FileOutputStream(file, append);
				mStreamWriter = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
				LogCache.putStream(fileName, mStreamWriter);
			}
		} else {
			mStreamWriter = LogCache.getStream(fileName);
			if (mStreamWriter == null || reCreate) {
				//TODO进行日志读写操作
				String filePath = mFileManager.getFilePath();
				Log.i("LogWriter", "filePath:" + filePath);
				File file = LogCache.getFile(filePath + "-" + fileName);
				if (file == null) {
					file = new File(filePath, fileName);
					LogCache.putFile(filePath + "-" + fileName, file);
				}

				if (!mFileManager.createFileIfNeed(file, append)) {
					Log.i("LogWriter", "getStream-> create file failed");
					return null;
				}

				FileOutputStream fos = new FileOutputStream(file, append);
				mStreamWriter = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
				LogCache.putStream(fileName, mStreamWriter);
			}
		}

		return mStreamWriter;
	}

	public void onDestroy() {
		if (mStreamWriter != null) {
			try {
				mStreamWriter.flush();
				mStreamWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		LogCache.onDestroy();
	}


	public void write(String log, boolean append) {
		write(mFileManager.mFileName, log, append);
	}

	public void write(String log) {
		write(log, true);
	}

}