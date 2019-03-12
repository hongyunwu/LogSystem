package com.why.log.contract;

import java.io.File;
import java.io.InputStream;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/20.
 */

public interface IParser {
	void loadXml(int resId, OnParseCompletedListener listener);

	void loadFile(File file, OnParseCompletedListener listener);

	void loadStream(InputStream stream, OnParseCompletedListener listener);
}
