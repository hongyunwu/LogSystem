package com.why.log.contract;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/19.
 */

public interface IWriter {
	void write(String file, String log, boolean append);
}
