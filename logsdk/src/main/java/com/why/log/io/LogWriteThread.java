package com.why.log.io;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/4.
 */

public class LogWriteThread extends HandlerThread {

	private Handler mHandler;
	public LogWriteThread(String name) {
		super(name);
	}

	@Override
	protected void onLooperPrepared() {
		getHandler();
	}

	public Handler getHandler(){
		if (mHandler==null){
			synchronized (LogWriteThread.class){
				if (mHandler==null){
					mHandler = new Handler(getLooper());
				}
			}
		}
		return mHandler;
	}

	@Override
	public boolean quit() {
		getHandler().removeCallbacksAndMessages(null);
		return super.quit();
	}

	@Override
	public boolean quitSafely() {
		getHandler().removeCallbacksAndMessages(null);
		return super.quitSafely();
	}
}
