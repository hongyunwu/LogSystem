package com.why.log.io;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;

import com.why.log.cache.LogCache;
import com.why.log.contract.Formatters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/19.
 */
public class FileManager {
	private static FileManager mFileManager;
	private Context mContext;


	private FileManager(Context context) {
		this.mContext = context;
	}

	public static FileManager getInstance(Context context) {
		if (mFileManager == null) {
			synchronized (FileManager.class) {
				mFileManager = new FileManager(context);
			}
		}
		return mFileManager;
	}

	//默认文件名
	private final String DEFAULT_FILE = "wm.log";
	//文件名
	public String mFileName = DEFAULT_FILE;
	//默认过期时间 3天
	public long EXPIRE_TIME = 1000 * 60 * 60 * 24 * 3;

	//默认文件大小
	public long FILE_SIZE = 1024 * 1024 * 100;

	//默认保存目录
	public String DIRECTORY = "/Weltmeister/";

	public void createFile(File file, boolean append) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		file.createNewFile();
		FileOutputStream fos = new FileOutputStream(file, append);
		OutputStreamWriter writer = new OutputStreamWriter(fos, "utf-8");
		writer.write("创建时间:" + Formatters.getDateFormat().format(getNow(LogCache.getDate())));
		writer.write("\n");//换行
		writer.flush();
		fos.flush();
		fos.close();
		writer.close();
	}


	public void setFileName(String fileName) {
		this.mFileName = fileName;
	}

	public String getFileName() {
		return this.mFileName;
	}

	/**
	 * 获取文件目录
	 *
	 * @return
	 */
	public String getFilePath() {
		File directory = null;
		boolean mkdir = false;
		///data/Weltmeister/
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) && hasWritePermission(mContext)) {
			directory = new File(Environment.getDataDirectory().getAbsolutePath()
					, DIRECTORY);
			mkdir = directory.mkdirs();
		}
		//data/data/包名

		else {
			if (mContext != null) {
				directory = new File(mContext.getFilesDir()
						, DIRECTORY);
				mkdir = directory.mkdirs();
			} else {
			}

		}

		return directory.getPath();
	}


	private boolean hasWritePermission(Context context) {
		if (context == null) {
			return false;
		}
		boolean granted = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				== PackageManager.PERMISSION_GRANTED
				&& ContextCompat.checkSelfPermission(context, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS)
				== PackageManager.PERMISSION_GRANTED;

		return granted;
	}

	public String getTime() {
		Date date = getNow(LogCache.getDate());
		return Formatters.getTimeFormat().format(date);
	}

	private Date getNow(Date date) {
		date.setTime(System.currentTimeMillis());
		return date;
	}

	/**
	 * 检查文件是否存在，如果不存在则创建，如果存在则检查文件是否过期，如果过期则删除重建。
	 *
	 * @param file   文件名
	 * @param append 是否append
	 * @return 返回是否文件真实存在，true代表文件存在或者创建成功
	 */
	public boolean createFileIfNeed(File file, boolean append) {

		if (!file.exists()) {
			try {
				createFile(file, append);
				Log.i("LogWriter", "createFileIfNeed-> file is not exists ,so create");
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("LogWriter", "file创建失败:" + e.getMessage());

			}
		} else {
			return checkCreateTime(file);
		}
		return false;
	}

	/**
	 * 查看file创建时间。
	 *
	 * @param file
	 */
	public boolean checkCreateTime(File file) {
		boolean flag = false;
		//根据时间和大小双重判断
		try {
			BufferedReader reader = LogCache.getBufferedReader(file.getAbsolutePath() + "-" + file.getName());
			if (reader == null) {
				reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				LogCache.putBufferedReader(file.getAbsolutePath() + "-" + file.getName(), reader);
			}
			//调到第0行
			try {
				reader.reset();
			} catch (IOException e) {
				e.printStackTrace();
			}
			reader.mark(50);
			String firstLine = reader.readLine();
			if (!TextUtils.isEmpty(firstLine) && firstLine.contains(":")) {
				String createTime = firstLine.split(":")[1];
				if (!TextUtils.isEmpty(createTime)) {
					Date createDate = Formatters.getDateFormat().parse(createTime);
					Date nowDate = getNow(LogCache.getDate());
					//三天
					if (nowDate.getTime() - createDate.getTime() >= EXPIRE_TIME) {
						Log.d("LogWriter", "log超过保存时间...删除中");
						file.delete();
						createFile(file, true);
					} else if (file.length() > FILE_SIZE) {
						if (mContext != null) {
							Log.d("LogWriter", "log超过最大size...删除中" + Formatter.formatFileSize(mContext, file.length()));
						}
						//判断文件大小
						file.delete();
						createFile(file, true);
					}

				} else if (file.length() > FILE_SIZE) {
					if (mContext != null) {
						Log.d("LogWriter", "log超过最大size...删除中:" + Formatter.formatFileSize(mContext, file.length()));
					}
					//判断文件大小
					file.delete();
					createFile(file, true);
				}

			} else if (file.length() > FILE_SIZE) {
				if (mContext != null) {
					Log.d("LogWriter", "log超过最大size...删除中:" + Formatter.formatFileSize(mContext, file.length()));
				}
				//判断文件大小
				file.delete();
				createFile(file, true);
			} else if (!file.exists()) {
				Log.d("LogWriter", "checkCreateTime->file is not exists,so create");

				createFile(file, true);
			}
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	public int getMaxSize() {
		return (int) (FILE_SIZE * 1f / 1024 / 1024);
	}

	public int getExpireTime() {
		return (int) (EXPIRE_TIME * 1f / 1000 / 60 / 60 / 24);
	}
}
