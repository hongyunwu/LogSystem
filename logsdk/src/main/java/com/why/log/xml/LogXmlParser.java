package com.why.log.xml;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.support.annotation.XmlRes;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.why.log.LogUtils;
import com.why.log.cache.LogCache;
import com.why.log.config.LogConfiguration;
import com.why.log.contract.Formatters;
import com.why.log.contract.IParser;
import com.why.log.contract.OnParseCompletedListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/16.
 * <p>
 * 用于解析xml文件中的配置
 */

public class LogXmlParser implements IParser {


	private static LogXmlParser mLogXmlParser;

	protected LogXmlParser() {
	}


	public void loadXml(@XmlRes int id, OnParseCompletedListener listener) {
		Context context = LogCache.getContext();
		XmlResourceParser parser = null;
		try {
			parser = context.getResources().getXml(id
			);
		} catch (Resources.NotFoundException e) {
			e.printStackTrace();
		}
		if (parser != null) {
			parse(context, parser);
		}
		if (listener != null) {
			listener.onParseCompleted();
		}
	}

	@Override
	public void loadFile(File file, OnParseCompletedListener listener) {
		Context context = LogCache.getContext();
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(new FileInputStream(file), "utf-8");
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if (parser != null) {
			parse(context, parser);
		}
		if (listener != null) {
			listener.onParseCompleted();
		}
	}

	@Override
	public void loadStream(InputStream stream, OnParseCompletedListener listener) {
		Context context = LogCache.getContext();
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(stream, "utf-8");
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		if (parser != null) {
			parse(context, parser);
		}
		if (listener != null) {
			listener.onParseCompleted();
		}
	}

	private static void parse(Context context, XmlPullParser parser) {
		try {
			if (context == null) {
				throw new NullPointerException("Context can't be null");
			}
			int eventType = parser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
					case XmlPullParser.START_TAG:
						switch (parser.getName()) {
							case "log-version":
								String version = parser.getAttributeValue(null, "version");
								LogUtils.VERSION = version;
								break;
							case "log-level":
								String level = parser.getAttributeValue(null, "level");

								LogUtils.setLevel(level);
								break;
							case "log-prefix":
								String logPrefix = parser.getAttributeValue(null, "prefix");
								if (!TextUtils.isEmpty(logPrefix)) {
									LogCache.getLogConfiguration().setPrefix(logPrefix);
								}
								break;
							case "log-writer":
								String writableStr = parser.getAttributeValue(null, "writable");
								if (TextUtils.isEmpty(writableStr)) {
									writableStr = "false";
								}
								boolean writable = Boolean.parseBoolean(writableStr);
								LogCache.getLogWriter().WRITE_LOG = writable;
								if (writable) {
									//解析文件属性
									LogWriterParser.parse(parser);
								}
								break;
							case "log-filter":
								String range = parser.getAttributeValue(null, "range");
								if ("all".equals(range)) {
									//代表所有的tag都显示
									LogCache.getLogConfiguration().clear();

								} else if ("list".equals(range)) {
									//代表需继续往下解析
									ArrayList<String> packages = new ArrayList<>();
									ArrayList<String> clazz = new ArrayList<>();
									ArrayList<String> tags = new ArrayList<>();
									ArrayList<String> contains = new ArrayList<>();
									LogFilterParser.parse(parser, packages, clazz, tags, contains);
									LogConfiguration logConfiguration = LogCache.getLogConfiguration();
									for (String p : packages) {
										logConfiguration.addFilter("package", p);
									}
									for (String c : clazz) {
										logConfiguration.addFilter("class", c);
									}
									for (String t : tags) {
										logConfiguration.addFilter("tag", t);
									}
									for (String c : contains) {
										logConfiguration.addFilter("contain", c);
									}
								}
								break;

						}
						break;
					case XmlPullParser.END_TAG:

						break;
				}

				eventType = parser.next();
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static LogXmlParser getInstance() {

		if (mLogXmlParser == null) {
			synchronized (LogXmlParser.class) {
				if (mLogXmlParser == null) {
					mLogXmlParser = new LogXmlParser();
				}
			}
		}
		return mLogXmlParser;
	}

	public static class LogWriterParser {

		public static void parse(XmlPullParser parser) throws XmlPullParserException, IOException {
			int eventType = parser.getEventType();
			while (!(eventType == XmlPullParser.END_TAG && "log-writer".equals(parser.getName()))) {
				switch (eventType) {
					case XmlPullParser.START_TAG:
						switch (parser.getName()) {
							case "time-format":
								String timeFormat = parser.nextText();
								Formatters.timeStr = timeFormat;
								break;
							case "file":
								FileParser.FileBean file = FileParser.parse(parser);
								if (!TextUtils.isEmpty(file.expireTime) && Pattern.matches("[0-9]*", file.expireTime)) {
									Log.i("LogXMlParser", "parse: " + file.expireTime);

									LogCache.getFileManager().EXPIRE_TIME = (Long.parseLong(file.expireTime)) * 1000 * 60 * 60 * 24;
								}

								if (!TextUtils.isEmpty(file.maxsize) && Pattern.matches("[0-9]*", file.maxsize)) {
									LogCache.getFileManager().FILE_SIZE = Long.parseLong(file.maxsize) * 1024 * 1024;
								}

								if (!TextUtils.isEmpty(file.name)) {
									LogCache.getFileManager().setFileName(file.name);
								}

								if (!TextUtils.isEmpty(file.path)) {
									if (!file.path.startsWith("/"))
										file.path = "/" + file.path;
									if (!file.path.endsWith("/"))
										file.path = file.path + "/";
									LogCache.getFileManager().DIRECTORY = file.path;
								}
								if (!TextUtils.isEmpty(file.dateFormat)) {
									Formatters.dateStr = file.dateFormat;
								}
								break;

						}

						break;
					case XmlPullParser.END_TAG:

						break;

				}

				eventType = parser.next();
			}

		}

		public static class FileParser {
			public static FileBean parse(XmlPullParser parser) throws XmlPullParserException, IOException {
				int eventType = parser.getEventType();
				FileBean fileBean = new FileBean();
				while (!(eventType == XmlPullParser.END_TAG && "file".equals(parser.getName()))) {
					switch (eventType) {
						case XmlPullParser.START_TAG:
							switch (parser.getName()) {
								case "path":
									fileBean.path = parser.nextText();
									break;
								case "name":
									fileBean.name = parser.nextText();
									break;
								case "maxsize":
									fileBean.maxsize = parser.nextText();
									break;
								case "date-format":
									fileBean.dateFormat = parser.nextText();
									break;
								case "expire-time":
									fileBean.expireTime = parser.nextText();
									break;
							}
							break;

						case XmlPullParser.END_TAG:

							break;
					}
					eventType = parser.next();
				}


				return fileBean;
			}

			public static class FileBean {

				public String path;
				public String name;
				public String maxsize;
				public String dateFormat;
				public String expireTime;


				@Override
				public String toString() {
					return "FileBean{" +
							"path='" + path + '\'' +
							", name='" + name + '\'' +
							", maxsize='" + maxsize + '\'' +
							", dateFormat='" + dateFormat + '\'' +
							", expireTime='" + expireTime + '\'' +
							'}';
				}
			}

		}
	}

	public static class LogFilterParser {

		public static void parse(XmlPullParser parser, ArrayList<String> packages, ArrayList<String> clazz, ArrayList<String> tags, ArrayList<String> contains) throws XmlPullParserException, IOException {
			int eventType = parser.getEventType();
			while (!(eventType == XmlPullParser.END_TAG && "log-filter".equals(parser.getName()))) {
				switch (eventType) {
					case XmlPullParser.START_TAG:
						if ("filter-list".equals(parser.getName())) {
							parseFilter(parser, packages, clazz, tags, contains);
						}

						break;

					case XmlPullParser.END_TAG:
						break;
				}

				eventType = parser.next();
			}
		}

		private static void parseFilter(XmlPullParser parser, ArrayList<String> packages, ArrayList<String> clazz, ArrayList<String> tags, ArrayList<String> contains) throws XmlPullParserException, IOException {
			int eventType = parser.getEventType();
			while (!(eventType == XmlPullParser.END_TAG && "filter-list".equals(parser.getName()))) {
				if (eventType == XmlPullParser.START_TAG) {
					switch (parser.getName()) {
						case "filter":
							String type = parser.getAttributeValue(null, "type");
							if ("package".equals(type)) {
								String p = parser.nextText();
								if (!TextUtils.isEmpty(p) && !packages.contains(p)) {
									packages.add(p);
								}
							} else if ("tag".equals(type)) {
								String t = parser.nextText();
								if (!TextUtils.isEmpty(t) && !tags.contains(t)) {
									tags.add(t);
								}
							} else if ("class".equals(type)) {
								String c = parser.nextText();
								if (!TextUtils.isEmpty(c) && !clazz.contains(c)) {
									clazz.add(c);
								}
							} else if ("contain".equals(type)) {
								String c = parser.nextText();
								if (!TextUtils.isEmpty(c) && !contains.contains(c)) {
									contains.add(c);
								}
							}
							break;

					}
				}

				eventType = parser.next();
			}
		}
	}
}
