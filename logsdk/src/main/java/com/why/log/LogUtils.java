package com.why.log;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.why.log.cache.LogCache;
import com.why.log.config.LogConfiguration;
import com.why.log.contract.OnCopyCompletedListener;
import com.why.log.contract.OnParseCompletedListener;
import com.why.log.io.CrashHandler;
import com.why.log.io.LogCopyPerformer;
import com.why.log.io.LogWriter;
import com.why.log.xml.LogXmlParser;

import java.io.FileNotFoundException;

/**
 * Created by android_wuhongyun@163.com
 * on 17-7-18.
 * 可以不使用TAG的日志打印类，默认TAG为当前类名,
 */
public class LogUtils {

	public static final boolean SHOW_STACK_TRACE = false;
	public static final int VERBOSE = 1;
	public static final int DEBUG = 2;
	public static final int INFO = 3;
	public static final int WARN = 4;
	public static final int ERROR = 5;
	public static final int NOTHING = 6;
	public static boolean PARSE_COMPLETED = false;
	public static String VERSION = "1.0";
	public static int LEVEL = VERBOSE;
	public static String LEVEL_TO_STRING = "INFO";
	public static final String SEPARATOR = ",";

	/**
	 * 初始化调用
	 *
	 * @param context
	 */
	public static void init(@NonNull Context context) {
		LogCache.setContext(context);

		if (context != null) {
			LogCache.getFileManager().setFileName(context.getPackageName() + ".log");
		}

		//执行复制
		LogCopyPerformer logCopyPerformer = new LogCopyPerformer();
		logCopyPerformer.performCopyConfig(new OnCopyCompletedListener() {
			@Override
			public void onCopyCompleted() {
				try {
					LogXmlParser.getInstance().loadStream(LogCache.getContext().openFileInput(LogCopyPerformer.mFileName), new OnParseCompletedListener() {
						@Override
						public void onParseCompleted() {
							//解析完成，然后才开始使用LogUtils
							PARSE_COMPLETED = true;
							Log.v("XmlParser", "onParseCompleted");
						}
					});
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}
		});
		//设置进程crash报告
		new Handler(Looper.getMainLooper()).post(new Runnable() {
			@Override
			public void run() {
				Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
			}
		});

	}

	public static void v(String message) {
		if (LEVEL <= VERBOSE) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (!filter(stackTraceElement, null)) {
				String tag = getDefaultTag(stackTraceElement);
				String prefixTag = addPrefix(tag);
				String msg = getLogInfo(stackTraceElement) + message;
				Log.v(prefixTag, msg);
				if (isWriteLog()) {
					getLogWriter().write(prefixTag + ":V:" + msg);
				}
			}
		}
	}

	public static void v(String tag, String message) {
		if (LEVEL <= VERBOSE) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];

			if (!filter(stackTraceElement, tag)) {
				if (TextUtils.isEmpty(tag)) {
					tag = getDefaultTag(stackTraceElement);
				}
				String prefixTag = addPrefix(tag);
				String msg = getLogInfo(stackTraceElement) + message;
				Log.v(prefixTag, msg);
				if (isWriteLog()) {
					getLogWriter().write(prefixTag + ":V:" + msg);
				}
			}
		}
	}

	public static void d(String message) {
		if (LEVEL <= DEBUG) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (!filter(stackTraceElement, null)) {
				String tag = getDefaultTag(stackTraceElement);
				String msg = getLogInfo(stackTraceElement) + message;
				String prefixTag = addPrefix(tag);
				Log.d(prefixTag, msg);
				if (isWriteLog()) {
					getLogWriter().write(prefixTag + ":D:" + msg);
				}
			}
		}
	}

	private static boolean isWriteLog() {
		return LogWriter.WRITE_LOG && PARSE_COMPLETED;
	}

	public static void d(String tag, String message) {
		if (LEVEL <= DEBUG) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (!filter(stackTraceElement, tag)) {
				if (TextUtils.isEmpty(tag)) {
					tag = getDefaultTag(stackTraceElement);
				}
				String prefixTag = addPrefix(tag);
				String msg = getLogInfo(stackTraceElement) + message;
				Log.d(prefixTag, msg);
				if (isWriteLog()) {
					getLogWriter().write(prefixTag + ":D:" + msg);
				}
			}
		}
	}


	public static void i(String message) {
		if (LEVEL <= INFO) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (!filter(stackTraceElement, null)) {
				String tag = getDefaultTag(stackTraceElement);
				String prefixTag = addPrefix(tag);
				String msg = getLogInfo(stackTraceElement) + message;
				Log.i(prefixTag, msg);
				if (isWriteLog()) {
					getLogWriter().write(prefixTag + ":I:" + msg);
				}
			}
		}
	}

	public static void i(String tag, String message) {
		if (LEVEL <= INFO) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (!filter(stackTraceElement, tag)) {
				if (TextUtils.isEmpty(tag)) {
					tag = getDefaultTag(stackTraceElement);
				}
				String prefixTag = addPrefix(tag);
				String msg = getLogInfo(stackTraceElement) + message;
				Log.i(prefixTag, msg);
				if (isWriteLog()) {
					getLogWriter().write(prefixTag + ":I:" + msg);
				}
			}
		}
	}

	public static void w(String message) {
		if (LEVEL <= WARN) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (!filter(stackTraceElement, null)) {
				String tag = getDefaultTag(stackTraceElement);
				String prefixTag = addPrefix(tag);
				String msg = getLogInfo(stackTraceElement) + message;
				Log.w(prefixTag, msg);
				if (isWriteLog()) {
					getLogWriter().write(prefixTag + ":W:" + msg);
				}
			}
		}
	}

	public static void w(String tag, String message) {
		if (LEVEL <= WARN) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (!filter(stackTraceElement, tag)) {
				if (TextUtils.isEmpty(tag)) {
					tag = getDefaultTag(stackTraceElement);
				}
				String prefixTag = addPrefix(tag);
				String msg = getLogInfo(stackTraceElement) + message;
				Log.w(prefixTag, msg);
				if (isWriteLog()) {
					getLogWriter().write(prefixTag + ":W:" + msg);
				}
			}
		}
	}

	public static void e(String tag, String message) {
		if (LEVEL <= ERROR) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (!filter(stackTraceElement, tag)) {
				if (TextUtils.isEmpty(tag)) {
					tag = getDefaultTag(stackTraceElement);
				}
				String prefixTag = addPrefix(tag);
				String msg = getLogInfo(stackTraceElement) + message;
				Log.e(prefixTag, msg);
				if (isWriteLog()) {
					getLogWriter().write(prefixTag + ":E:" + msg);
				}
			}
		}
	}

	public static void e(String message) {
		String tag = null;
		if (LEVEL <= ERROR) {
			StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
			if (!filter(stackTraceElement, tag)) {
				if (TextUtils.isEmpty(tag)) {
					tag = getDefaultTag(stackTraceElement);
				}
				String prefixTag = addPrefix(tag);
				String msg = getLogInfo(stackTraceElement) + message;
				Log.e(prefixTag, msg);
				if (isWriteLog()) {
					getLogWriter().write(prefixTag + ":E:" + msg);
				}
			}
		}
	}

	/**
	 * 获取默认的TAG名称.
	 * 比如在MainActivity.java中调用了日志输出.
	 * 则TAG为MainActivity
	 */
	public static String getDefaultTag(StackTraceElement stackTraceElement) {
		String fileName = stackTraceElement.getFileName();
		String stringArray[] = fileName.split("\\.");
		String tag = stringArray[0];
		return tag;
	}

	public static String addPrefix(String tag) {
		return LogCache.getLogConfiguration().getPrefix()+":" + tag;
	}

	/**
	 * 输出日志所包含的信息
	 */
	public static String getLogInfo(StackTraceElement stackTraceElement) {
		if (SHOW_STACK_TRACE) {
			StringBuilder logInfoStringBuilder = new StringBuilder();
			// 获取线程名
			String threadName = Thread.currentThread().getName();
			// 获取线程ID
			long threadID = Thread.currentThread().getId();
			// 获取文件名.即xxx.java
			String fileName = stackTraceElement.getFileName();
			// 获取类名.即包名+类名
			String className = stackTraceElement.getClassName();
			// 获取方法名称
			String methodName = stackTraceElement.getMethodName();
			// 获取生日输出行数
			int lineNumber = stackTraceElement.getLineNumber();
			logInfoStringBuilder.append("[ ");
			logInfoStringBuilder.append("threadID=" + threadID).append(SEPARATOR);
			logInfoStringBuilder.append("threadName=" + threadName).append(SEPARATOR);
			logInfoStringBuilder.append("fileName=" + fileName).append(SEPARATOR);
			logInfoStringBuilder.append("className=" + className).append(SEPARATOR);
			logInfoStringBuilder.append("methodName=" + methodName).append(SEPARATOR);
			logInfoStringBuilder.append("lineNumber=" + lineNumber);
			logInfoStringBuilder.append(" ] ");
			return logInfoStringBuilder.toString();
		}
		return "";

	}

	private static LogWriter getLogWriter() {

		return LogCache.getLogWriter();
	}

	private static boolean filter(StackTraceElement stackTraceElement, String tag) {

		return LogConfiguration.filter(stackTraceElement, tag);
	}


	public static void setLevel(String level) {
		if (!TextUtils.isEmpty(level)) {
			String level_backup = LEVEL_TO_STRING;
			LEVEL_TO_STRING = level;
			switch (level) {
				case "VERBOSE":
					LEVEL = VERBOSE;
					break;
				case "DEBUG":
					LEVEL = DEBUG;
					break;
				case "INFO":
					LEVEL = INFO;
					break;
				case "WARNING":
					LEVEL = WARN;
					break;
				case "ERROR":
					LEVEL = ERROR;
					break;
				case "NOTHING":
					LEVEL = NOTHING;
					break;
				default:
					LEVEL_TO_STRING = level_backup;
					break;

			}
		}


	}

	public static String getLevelToSting() {
		return LEVEL_TO_STRING;
	}

	/**
	 * 与init配套
	 */
	public static void destroy() {
		try {
			LogCache.getLogWriter().onDestroy();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
