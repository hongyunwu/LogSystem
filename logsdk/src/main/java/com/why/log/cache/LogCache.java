package com.why.log.cache;

import android.content.Context;

import com.why.log.config.LogConfiguration;
import com.why.log.io.FileManager;
import com.why.log.io.LogWriter;
import com.why.log.xml.LogXmlSerializer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/16.
 * 对诸如Context等提供缓存能力。
 */

public class LogCache {

	private static String context = "Context";

	private LogCache(){

	}
	private static LogCache mLogCache;
	public static LogCache getInstance() {
		if (mLogCache==null){
			synchronized (LogCache.class){
				if (mLogCache==null){
					mLogCache = new LogCache();
				}
			}
		}
		return mLogCache;
	}

	HashMap<String,Object> mCache = new HashMap<>();
	public static Context getContext() {
		return getInstance().get(LogCache.context);
	}

	private Context get(String key) {
		Object value = mCache.get(key);
		return (Context) value;
	}

	public static Date getDate() {
		return new Date();
	}

	public static SimpleDateFormat getDateFormat() {
		return new SimpleDateFormat();
	}

	public static LogWriter getLogWriter() {
		Object o = getInstance().mCache.get("LogWriter");
		LogWriter writer;
		if (o!=null&&o instanceof LogWriter){
			writer = (LogWriter) o;
		}else {
			writer = LogWriter.getInstance(getContext());
			getInstance().mCache.put("LogWriter",writer);
		}

		return writer;
	}

	public static BufferedWriter getStream(String fileName) {
		 Object o = getInstance().mCache.get(fileName);
		 BufferedWriter writer = null;
		 if (o!=null&& o instanceof BufferedWriter){
		 	writer = (BufferedWriter)o;
		}
		return writer;
	}

	public static void putStream(String fileName, BufferedWriter writer) {
		getInstance().put(fileName,writer);
	}

	public static void onDestroy() {
		getInstance().mCache.clear();
	}

	public static void setContext(Context context) {

		getInstance().put(LogCache.context,context);
	}

	private void put(String key, Object value) {

		mCache.put(key,value);
	}

	public static FileManager getFileManager() {
		return FileManager.getInstance(getContext());
	}

	public static LogConfiguration getLogConfiguration() {
		return LogConfiguration.getInstance();
	}

	public static File getFile(String fileName) {
		Object o = getInstance().mCache.get(fileName);
		File file = null;
		if (o!=null&&o instanceof File){
			file = (File) o;
		}
		return file;
	}

	public static void putFile(String fileName, File file) {
		getInstance().put(fileName,file);
	}

	public static BufferedReader getBufferedReader(String name) {
		Object o = getInstance().mCache.get("buffer-" + name);
		BufferedReader reader = null;
		if (o!=null && o instanceof BufferedReader){
			reader = (BufferedReader) o;

		}
		return reader;
	}

	public static void putBufferedReader(String key, BufferedReader value) {
		getInstance().put("buffer-"+key,value);
	}

	public static LogXmlSerializer getLogSerializer() {
		Object o = getInstance().mCache.get("log-serializer");
		LogXmlSerializer serializer = null;
		if (o!=null&& o instanceof LogXmlSerializer){
			serializer = (LogXmlSerializer) o;
		}
		return serializer;
	}
}
