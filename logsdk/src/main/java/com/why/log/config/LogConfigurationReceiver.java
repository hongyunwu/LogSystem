package com.why.log.config;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.why.log.LogUtils;
import com.why.log.cache.LogCache;
import com.why.log.io.LogWriter;
import com.why.log.xml.LogXmlSerializer;

import java.io.FileNotFoundException;
import java.util.regex.Pattern;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/6/20.
 * 用于动态通过广播来控制日志显示
 */

public class LogConfigurationReceiver extends BroadcastReceiver {

	@CallSuper
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		String packageName = context.getPackageName();

		if (!TextUtils.isEmpty(action)) {
			if (action.equals(packageName + ".filter")) {
				handleFilter(intent);
				handleUnFilter(intent);
			} else if (action.equals(packageName + ".config")) {
				handleLogConfig(intent);
			} else if (action.equals(packageName + ".config.print")) {
				handleConfigPrint(intent);
			}
		}
	}

	private void handleConfigPrint(Intent intent) {
		String print = intent.getStringExtra("print");
		if (!TextUtils.isEmpty(print)) {
			Log.i("server-print", "============= print start ==============");
			switch (print) {
				case "log-config":
					Log.i("server-print", "log-level   :" + LogUtils.getLevelToSting());
					Log.i("server-print", "log-writable:" + LogWriter.WRITE_LOG);
					Log.i("server-print", "log-filepath:" + LogCache.getFileManager().getFilePath());
					Log.i("server-print", "log-filename:" + LogCache.getFileManager().getFileName());
					Log.i("server-print", "log-maxsize :" + LogCache.getFileManager().getMaxSize());
					Log.i("server-print", "log-expireT :" + LogCache.getFileManager().getExpireTime());
					Log.i("server-print", "log-filters :"
							+ "\npackages:" + LogCache.getLogConfiguration().getPackagesList()
							+ "\nclazz   :" + LogCache.getLogConfiguration().getClazzList()
							+ "\ntags    :" + LogCache.getLogConfiguration().getTagsList()
							+ "\ncontains:" + LogCache.getLogConfiguration().getContainsList());
					Log.i("server-print", "   ");
					break;
				default:
					break;

			}
			Log.i("server-print", "=============  print end  ==============");
		}


	}

	private void handleLogConfig(Intent intent) {
		String level = intent.getStringExtra("logLevel");
		if (!TextUtils.isEmpty(level)) {
			LogUtils.setLevel(level);
			xmlSerialize();
		}
		String prefix = intent.getStringExtra("logPrefix");
		if (!TextUtils.isEmpty(prefix)) {
			LogCache.getLogConfiguration().setPrefix(prefix);
			xmlSerialize();
		}
		String maxsize = intent.getStringExtra("maxSize");
		if (!TextUtils.isEmpty(maxsize) && Pattern.matches("[0-9]*", maxsize)) {
			LogCache.getFileManager().FILE_SIZE = Long.parseLong(maxsize) * 1024 * 1024;
			xmlSerialize();
		}
		String expireTime = intent.getStringExtra("expireTime");
		if (!TextUtils.isEmpty(expireTime) && Pattern.matches("[0-9]*", expireTime)) {
			LogCache.getFileManager().EXPIRE_TIME = Long.parseLong(expireTime) * 1000 * 60 * 60 * 24;
			xmlSerialize();
		}
		String writable = intent.getStringExtra("writable");
		if (!TextUtils.isEmpty(writable) && ("true".equals(writable) || "false".equals(writable))) {
			if (!LogWriter.WRITE_LOG){
				//清空stream
				LogCache.putStream(LogCache.getFileManager().mFileName,null);
			}
			LogWriter.WRITE_LOG = Boolean.parseBoolean(writable);
			xmlSerialize();
		}

		String filename = intent.getStringExtra("fileName");
		if (!TextUtils.isEmpty(filename)) {
			LogCache.getFileManager().setFileName(filename);
			xmlSerialize();
		}
		xmlSerialize();
	}

	private void xmlSerialize() {
		LogXmlSerializer serializer = LogCache.getLogSerializer();
		if (serializer == null) {
			serializer = new LogXmlSerializer();
		}
		Context context = LogCache.getContext();
		if (context == null) {
			LogUtils.i("Context is null");
			return;
		}
		try {
			serializer.saveStream(context.openFileOutput("log-config.xml", Context.MODE_PRIVATE));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void handleUnFilter(Intent intent) {
		String unFilter = intent.getStringExtra("unFilter");
		if (!TextUtils.isEmpty(unFilter) && unFilter.contains(":") && unFilter.split(":").length >= 2) {

			String[] split = unFilter.split(":");
			String filterKey = split[0];
			String filterValue = split[1];
			LogCache.getLogConfiguration().deleteFilter(filterKey, filterValue);
			xmlSerialize();
		}
		if (!TextUtils.isEmpty(unFilter) && unFilter.equals("all")) {
			LogCache.getLogConfiguration().deleteAllFilter();
			xmlSerialize();
		}
	}

	@Nullable
	private void handleFilter(Intent intent) {
		String filter = intent.getStringExtra("filter");
		if (!TextUtils.isEmpty(filter) && filter.contains(":") && filter.split(":").length >= 2) {

			String[] split = filter.split(":");
			String filterKey = split[0];
			String filterValue = split[1];
			LogCache.getLogConfiguration().addFilter(filterKey, filterValue);
			xmlSerialize();
		}
	}
}
