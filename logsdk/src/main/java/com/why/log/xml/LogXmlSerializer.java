package com.why.log.xml;

import android.util.Xml;
import android.widget.Toast;

import com.why.log.LogUtils;
import com.why.log.cache.LogCache;
import com.why.log.config.LogConfiguration;
import com.why.log.contract.Formatters;
import com.why.log.contract.ISerializer;
import com.why.log.io.BaseWriter;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/16.
 */

public class LogXmlSerializer extends BaseWriter implements ISerializer {

	public LogXmlSerializer() {
		super("log-serializer");
	}

	@Override
	public void saveFile(final File file) {
		mLogWriteThread.getHandler().post(new Runnable() {
			@Override
			public void run() {
				FileOutputStream fos = null;
				try {
					if (file == null) return;
					fos = new FileOutputStream(file);

					// 获得一个序列化工具
					XmlSerializer serializer = Xml.newSerializer();
					serializer.setOutput(fos, "utf-8");
					serialize(serializer);
					fos.flush();
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(LogCache.getContext(), "写入失败", Toast.LENGTH_SHORT).show();
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}
			}
		});
	}

	@Override
	public void saveStream(final FileOutputStream fos) {
		mLogWriteThread.getHandler().post(new Runnable() {
			@Override
			public void run() {
				try {
					if (fos == null) {
						return;
					}
					// 获得一个序列化工具
					XmlSerializer serializer = Xml.newSerializer();
					serializer.setOutput(fos, "utf-8");
					serialize(serializer);
					fos.flush();
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(LogCache.getContext(), "写入失败", Toast.LENGTH_SHORT).show();
				} finally {
					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				}
			}
		});

	}

	private static void serialize(XmlSerializer serializer) throws Exception {
		// 设置文件头
		serializer.startDocument("utf-8", true);
		serializer.startTag(null, "log-configuration");

		serializer.startTag(null, "log-version");
		serializer.attribute(null, "version", LogUtils.VERSION);
		serializer.endTag(null, "log-version");

		serializer.startTag(null, "log-level");
		serializer.attribute(null, "level", LogUtils.getLevelToSting());
		serializer.endTag(null, "log-level");

		serializer.startTag(null, "log-prefix");
		serializer.attribute(null, "prefix", LogCache.getLogConfiguration().getPrefix());
		serializer.endTag(null, "log-prefix");

		serializer.startTag(null, "log-writer");
		serializer.attribute(null, "writable", "" + LogCache.getLogWriter().WRITE_LOG);
		writerSerialize(serializer);
		serializer.endTag(null, "log-writer");

		serializer.startTag(null, "log-filter");

		serializer.attribute(null, "range", LogCache.getLogConfiguration().getAllSize() == 0 ? "all" : "list");
		filterListSerialize(serializer);
		serializer.endTag(null, "log-filter");
		serializer.endTag(null, "log-configuration");
		serializer.endDocument();
	}

	private static void writerSerialize(XmlSerializer serializer) throws Exception {
		serializer.startTag(null, "time-format");
		serializer.text(Formatters.timeStr);
		serializer.endTag(null, "time-format");

		serializer.startTag(null, "file");
		fileSerialize(serializer);
		serializer.endTag(null, "file");

	}

	private static void fileSerialize(XmlSerializer serializer) throws Exception {
		serializer.startTag(null, "path");
		serializer.text(LogCache.getFileManager().DIRECTORY);
		serializer.endTag(null, "path");

		serializer.startTag(null, "name");
		serializer.text(LogCache.getFileManager().getFileName());
		serializer.endTag(null, "name");

		serializer.startTag(null, "maxsize");
		serializer.text("" + (int) (LogCache.getFileManager().FILE_SIZE * 1f / 1024 / 1024));
		serializer.endTag(null, "maxsize");

		serializer.startTag(null, "date-format");
		serializer.text(Formatters.dateStr);
		serializer.endTag(null, "date-format");

		serializer.startTag(null, "expire-time");
		serializer.text("" + (int) (LogCache.getFileManager().EXPIRE_TIME * 1f / 1000 / 60 / 60 / 24));
		serializer.endTag(null, "expire-time");
	}

	private static void filterListSerialize(XmlSerializer serializer) throws Exception {
		serializer.startTag(null, "filter-list");
		LogConfiguration logConfiguration = LogCache.getLogConfiguration();
		List<String> packagesList = logConfiguration.getPackagesList();
		List<String> clazzList = logConfiguration.getClazzList();
		List<String> tagsList = logConfiguration.getTagsList();
		List<String> containsList = logConfiguration.getContainsList();
		filterSerialize(serializer, "package", packagesList);
		filterSerialize(serializer, "class", clazzList);
		filterSerialize(serializer, "tag", tagsList);
		filterSerialize(serializer, "contain", containsList);
		serializer.endTag(null, "filter-list");
	}

	private static void filterSerialize(XmlSerializer serializer, String type, List<String> list) throws IOException {
		for (String filter : list) {
			serializer.startTag(null, "filter");
			serializer.attribute(null, "type", type);
			serializer.text(filter);
			serializer.endTag(null, "filter");
		}
	}

}
