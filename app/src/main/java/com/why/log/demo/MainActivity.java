package com.why.log.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.why.log.LogUtils;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		LogUtils.i("onCreate");
	}
}
