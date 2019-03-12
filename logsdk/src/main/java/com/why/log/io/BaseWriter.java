package com.why.log.io;

import android.os.HandlerThread;
import android.util.Log;


/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/16.
 * 应具有提供stream操作的能力
 */

public abstract class BaseWriter{
	protected LogWriteThread mLogWriteThread;

	protected BaseWriter(String name){
		mLogWriteThread = new LogWriteThread(name);
		mLogWriteThread.setUncaughtExceptionHandler(new CrashHandler(mLogWriteThread));
		mLogWriteThread.start();
	}

	//子线程处理所有逻辑
	public class CrashHandler implements Thread.UncaughtExceptionHandler {

		private Thread.UncaughtExceptionHandler mDefaultHandler;
		private HandlerThread mLastThread;

		/**
		 * 数据库线程发生崩溃时调用
		 */
		public CrashHandler(HandlerThread thread) {
			mLastThread = thread;
			mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		}

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			if (!handleException(e) && mDefaultHandler != null) {
				mDefaultHandler.uncaughtException(t, e);
			} else {
				Log.e("LogWriter", "日志读写发生错误！！！");
				//此处是否需要重新创建线程。
				synchronized (LogWriter.class) {
					if (mLogWriteThread != null /*&& (!mHandlerThread.isAlive()||mHandlerThread.isInterrupted())*/ && mLogWriteThread.getId() == mLastThread.getId()) {
						mLogWriteThread.quitSafely();
						mLogWriteThread = null;
					}
					if (mLogWriteThread == null) {
						mLastThread = mLogWriteThread = new LogWriteThread("server-db-operation");
						mLogWriteThread.start();
						mLogWriteThread.setUncaughtExceptionHandler(this);
					}
				}
			}
		}

		private boolean handleException(Throwable throwable) {
			throwable.printStackTrace();
			return true;
		}
	}


}
