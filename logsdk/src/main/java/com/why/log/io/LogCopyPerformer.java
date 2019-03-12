package com.why.log.io;

import android.content.Context;
import android.content.SharedPreferences;

import com.why.log.LogUtils;
import com.why.log.cache.LogCache;
import com.why.log.contract.OnCopyCompletedListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by android_wuhongyun@163.com
 * on 2018/7/20.
 */

public class LogCopyPerformer extends BaseWriter{
	public static String mFileName = "log-config.xml";

	public LogCopyPerformer() {
		super("config-copy");
	}

	public void performCopyConfig(final OnCopyCompletedListener listener){
		final Context context = LogCache.getContext();
		if (context==null){
			return;
		}
		mLogWriteThread.getHandler().post(new Runnable() {
			@Override
			public void run() {
				SharedPreferences sp = context.getSharedPreferences("log-config", Context.MODE_PRIVATE);
				boolean exist = checkFileExist(context, mFileName);
				if (!exist){
					sp.edit().putBoolean("config-copy",false).commit();
				}
				boolean configCopy = sp.getBoolean("config-copy", false);
				if (!configCopy){
					try {
						copyConfig();
						sp.edit().putBoolean("config-copy",true).commit();
						listener.onCopyCompleted();
						LogUtils.i("copyConfig completed");
					} catch (IOException e) {
						e.printStackTrace();
						//delete file
						deleteFile(context, mFileName);
						sp.edit().putBoolean("config-copy",false).commit();
					}

				}else {
					listener.onCopyCompleted();
				}

				mLogWriteThread.quitSafely();
			}
		})
		;
	}

	private void deleteFile(Context context, String fileName) {
		File file = new File(context.getFilesDir(), fileName);
		if (file.exists()){
			file.delete();
		}
	}

	private boolean checkFileExist(Context context, String fileName) {
		File file = new File(context.getFilesDir(), fileName);
		if (file.exists()){
			return true;
		}
		return false;
	}

	private void copyConfig() throws IOException {
			InputStream inputStream = LogCache.getContext().getAssets().open(mFileName);
			FileOutputStream outputStream = LogCache.getContext().openFileOutput(mFileName, Context.MODE_PRIVATE);
			byte[] buffer = new byte[1024];
			int length;
			while ((length=inputStream.read(buffer))>0){
				outputStream.write(buffer,0,length);
			}
			outputStream.flush();
			inputStream.close();
			outputStream.close();


	}
}
