package com.why.log.demo;

import android.app.Application;

import com.why.log.LogUtils;

/**
 * Created by android_wuhongyun@163.com
 * on 2019/3/12.
 */
public class DemoApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		LogUtils.init(getApplicationContext());

	}
}
