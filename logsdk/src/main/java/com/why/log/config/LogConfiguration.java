package com.why.log.config;

import android.content.Context;
import android.text.TextUtils;

import com.why.log.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/6/20.
 * 日志配置类
 */

public class LogConfiguration {
	private ArrayList<String> mFilterPackages = new ArrayList<>();
	private ArrayList<String> mFilterClasses = new ArrayList<>();
	private ArrayList<String> mFilterTags = new ArrayList<>();
	private ArrayList<String> mFilterContains = new ArrayList<>();
	private static LogConfiguration mLogConfiguration = null;
	private String mPrefix = "subdisplay-server:";
	private Context mContext;

	private LogConfiguration(){
		//TODO加载xml文件

	}
	public static LogConfiguration getInstance() {
		if (mLogConfiguration == null){
			synchronized (LogConfiguration.class){
				if (mLogConfiguration==null){
					mLogConfiguration = new LogConfiguration();
				}
			}
		}
		return mLogConfiguration;
	}

	public static boolean filter(StackTraceElement stackTraceElement, String tag) {
		boolean filter = false;
		for (String packageName : getInstance().mFilterPackages){
			if (stackTraceElement.getClassName().startsWith(packageName)){
				filter = true;
				break;
			}
		}
		if (getInstance().mFilterClasses.contains(stackTraceElement.getClassName())){
			filter = true;
		}

		if (getInstance().mFilterTags.contains(LogUtils.getDefaultTag(stackTraceElement))){
			filter = true;
		}
		if (!TextUtils.isEmpty(tag)){
			if (getInstance().mFilterTags.contains(tag)){
				filter = true;
			}
		}
		for (String contain : getInstance().mFilterContains){
			if (!TextUtils.isEmpty(stackTraceElement.getClassName()) && stackTraceElement.getClassName().contains(contain)) {
				filter = true;
				break;
			}
		}
		return filter;
	}

	/**
	 * 广播调用，添加过滤的tag
	 *
	 * @param filterKey 过滤tag的类型，package、class、tag、contain
	 * @param filterValue 需要过滤的具体的value
	 */
	public void addFilter(String filterKey, String filterValue) {
		switch (filterKey){
			case "package":
				addPackage(filterValue);
				break;
			case "class":
				addClass(filterValue);
				break;
			case "tag":
				addTag(filterValue);
				break;
			case "contain":
				addContain(filterValue);
				break;
		}
	}

	/**
	 * 广播调用，删除过滤tag
	 *
	 * @param filterKey
	 * @param filterValue
	 */
	public void deleteFilter(String filterKey, String filterValue) {
		switch (filterKey){
			case "package":
				deletePackage(filterValue);
				break;
			case "class":
				deleteClass(filterValue);
				break;
			case "tag":
				deleteTag(filterValue);
				break;
			case "contain":
				deleteContain(filterValue);
				break;
		}
	}
	private void addContain(String filterValue) {
		if (!mFilterContains.contains(filterValue))
			mFilterContains.add(filterValue);
	}

	private void addTag(String filterValue) {
		if (!mFilterTags.contains(filterValue))
			mFilterTags.add(filterValue);
	}

	private void addClass(String filterValue) {
		if (!mFilterClasses.contains(filterValue))
			mFilterClasses.add(filterValue);
	}

	private void addPackage(String filterValue) {
		if (!mFilterPackages.contains(filterValue))
			mFilterPackages.add(filterValue);
	}

	private void deleteContain(String filterValue) {
		if (mFilterContains.contains(filterValue))
			mFilterContains.remove(filterValue);
	}

	private void deleteTag(String filterValue) {
		if (mFilterTags.contains(filterValue))
			mFilterTags.remove(filterValue);
	}

	private void deleteClass(String filterValue) {
		if (mFilterClasses.contains(filterValue))
			mFilterClasses.remove(filterValue);
	}

	private void deletePackage(String filterValue) {
		if (mFilterPackages.contains(filterValue))
			mFilterPackages.remove(filterValue);
	}

	/**
	 * 删除所有过滤tag
	 */
	public void deleteAllFilter() {
		mFilterClasses.clear();
		mFilterContains.clear();
		mFilterPackages.clear();
		mFilterTags.clear();
	}

	/**
	 * 获取为tag添加的统一前缀
	 *
	 * @return tag前缀
	 */
	public String getPrefix() {
		return mPrefix;
	}

	/**
	 * 设置tag前缀
	 *
	 * @param prefix
	 */
	public void setPrefix(String prefix) {
		if (prefix==null)prefix = "";
		mPrefix = prefix;
	}

	public List<String> getPackagesList() {
		return mFilterPackages;
	}
	public List<String> getClazzList() {
		return mFilterClasses;
	}
	public List<String> getTagsList() {
		return mFilterTags;
	}
	public List<String> getContainsList() {
		return mFilterContains;
	}

	/**
	 * 用于构建Log配置信息
	 */
	public static class Builder{

		private final LogConfiguration mLogConfiguration;
		private ArrayList<String> filterPackages = new ArrayList<>();
		private ArrayList<String> filterClasses = new ArrayList<>();
		private ArrayList<String> filterTags = new ArrayList<>();
		private ArrayList<String> filterContains = new ArrayList<>();
		public Builder(){
			mLogConfiguration = LogConfiguration.getInstance();
			//mLogConfiguration.clear();
			filterPackages.clear();
			filterClasses.clear();
			filterTags.clear();
			filterContains.clear();
		}

		/**
		 * 设置日志tag的统一前缀
		 *
		 * @param prefix
		 * @return
		 */
		public Builder setTagPrefix(String prefix){
			mLogConfiguration.setPrefix(prefix);
			return this;
		}

		/**
		 * 设置过滤报名下的所有日志
		 *
		 * @param packageName
		 * @return
		 */
		public Builder filterPackage( String packageName){
			if (!filterPackages.contains(packageName))filterPackages.add(packageName);
			return this;
		}

		/**
		 * 设置过滤在类中的所有日志
		 *
		 * @param className
		 * @return
		 */
		public Builder filterClass( String className){
			if (!filterClasses.contains(className))filterClasses.add(className);
			return this;
		}

		/**
		 * 设置过滤指定tag的日志
		 *
		 * @param tagName
		 * @return
		 */
		public Builder filterTag( String tagName){
			if (!filterTags.contains(tagName))filterTags.add(tagName);
			return this;
		}


		/**
		 * 设置过滤掉包含某字符的日志
		 *
		 * @param containName
		 * @return
		 */
		public Builder filterContains( String containName){
			if (!filterContains.contains(containName))filterContains.add(containName);
			return this;
		}

		public void build(){
			for (String packageName :filterPackages){
				mLogConfiguration.addPackage(packageName);
			}
			for (String className :filterClasses){
				mLogConfiguration.addClass(className);
			}
			for (String tagName :filterTags){
				mLogConfiguration.addTag(tagName);
			}
			for (String containName :filterContains){
				mLogConfiguration.addContain(containName);
			}
		}
	}

	/**
	 * 设置上下文
	 *
	 * @param context
	 */
	private void setContext(Context context) {
		this.mContext = context;
	}

	/**
	 * 获取上下文
	 *
	 * @return
	 */
	public Context getAppContext() {
		return mContext;
	}

	/**
	 * 清除所有日志过滤
	 */
	public void clear() {
		mFilterPackages.clear();
		mFilterClasses.clear();
		mFilterTags.clear();
		mFilterContains.clear();
	}

	public int getAllSize(){
	    return mFilterPackages.size()+mFilterClasses.size()+mFilterTags.size()+mFilterContains.size();
    }

}
