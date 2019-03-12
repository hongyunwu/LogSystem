package com.why.log.io;

import android.util.Log;

import com.why.log.LogUtils;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/19.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

		private Thread.UncaughtExceptionHandler mDefaultHandler;

		/**
		 * 数据库线程发生崩溃时调用
		 */
		public CrashHandler() {
			mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		}

		@Override
		public void uncaughtException(Thread t, Throwable e) {
			if (!handleException(e) && mDefaultHandler != null) {
				mDefaultHandler.uncaughtException(t, e);
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				android.os.Process.killProcess(android.os.Process.myPid());
				//非0标识异常终止
				System.exit(1);
			}

		}

		/**
		 * 保留方法，记录数据库crash原因
		 *
		 * @param throwable 异常
		 * @return true表示由CrashHandler接手异常处理
		 */
		private boolean handleException(Throwable throwable) {
			throwable.printStackTrace();
			//打印日志
			LogUtils.e(Log.getStackTraceString(throwable));
			return true;
		}
	}