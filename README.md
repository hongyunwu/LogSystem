# LogSystem
本地化日志系统

优点

1.可缓存，并根据max size即expire time删除重建

2.可level化，低于某level则不打印

3.使用简单，默认加tag，并可添加同一前缀

4.可配置过滤列表，过滤包含某tag的log

# 使用步骤

1.在Application中初始化
```
public class DemoApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		LogUtils.init(getApplicationContext());

	}
}
```
2.调用
```
public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		LogUtils.i("onCreate");
		findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				LogUtils.d("onClick");
			}
		});

	}
}
```
