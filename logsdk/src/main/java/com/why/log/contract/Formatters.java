package com.why.log.contract;


import com.why.log.cache.LogCache;

import java.text.SimpleDateFormat;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/19.
 */

public class Formatters {

	public static String dateStr = "yyyy年MM月dd日";
	public static String timeStr = "HH时mm分ss秒-SSS ";


	public static SimpleDateFormat getDateFormat() {
		SimpleDateFormat format = LogCache.getDateFormat();
		format.applyPattern(dateStr);
		return format;
	}

	public static SimpleDateFormat getTimeFormat() {
		SimpleDateFormat format = LogCache.getDateFormat();
		format.applyPattern(timeStr);
		return format;
	}

}
