package com.why.log.contract;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/20.
 */

public interface ISerializer {
	void saveFile(File file);
	void saveStream(FileOutputStream fos);
}
