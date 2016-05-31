package com.example.test1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SearchActivity extends ListActivity {

	private Button mButton = null;
	private EditText mEditText = null;
	private ImageView delete = null;
	private ArrayList<String> NameInfo = new ArrayList<String>();//把数据库中所有地点的名称提出，放入NameInfo，为了进行匹配
	private Handler mhandler = null;
	private ArrayList<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();
	//定义匹配字符串的数据集合，为了搭建适配器，显示在列表上
	//想要实现显示所有信息列表的话，就把初始化时把NameInfo中的信息写入mData
	private MyAdapter adapter = null;
	
	
	private Runnable eChanged = new Runnable() {
		
	    @Override
	    public void run() {
	          String data = mEditText.getText().toString();
				
	          mData.clear();
				
	          getmDataSub(data);//匹配输入的字符串（与数据集NameInfo比较），把结果写入数据集mData
			  
	          adapter.notifyDataSetChanged();	
	    }
	};
	
	
	private void getmDataSub(String data)//匹配算法，可以直接并如Runnable的Run方法中
	{
	     for(int i = 0; i < NameInfo.size(); ++i){
	           if(NameInfo.get(i).contains(data)){
	        	    Map<String, Object> map = new HashMap<String, Object>();
	        	    map.put("textview", NameInfo.get(i));
	        	    mData.add(map);
	        	    //System.out.println("This is the mData:" + NameInfo.get(i).toString());
	            }
	     }
	}   

	
	private void Init(){//初始化信息，把数据库中的Name栏信息，填充到NameInfo中，方便之后的操作	
		
		for(Info info : Info.infos){
			NameInfo.add(info.getName());
		}
		Log.d("TAG", NameInfo.size()+"");
		mData.clear();
	    
	    /** 自定义一个Adapter
		 * 第一个参数为当前Activity
		 * 第二个array为指定需要显示的数据集合
		 * 第三个参数是将要显示每行数据的View布局，
		 * 第四个主要是将Map对象中的名称映射到列名，一一对应
		 * 第五个参数是int数组，对应布局文件中的id。
		 */
		adapter = new MyAdapter(this, mData, R.layout.listview,
				new String[]{"textview"},
				new int[]{R.id.textview});//数据类型为键值对，第4个参数是键值对的第一个参数
		
		setListAdapter(adapter);
	}
	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// 响应list点击事件
		super.onListItemClick(l, v, position, id);
		TextView user = (TextView) v.findViewById(R.id.textview);
		Toast.makeText(this, "你选择了：" + user.getText(), 1000).show();
		
		Intent intent = new Intent(this,MainActivity.class);

		Bundle bundle = new Bundle();
		bundle.putString("click_item", user.getText().toString());
		
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_search);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.custom_title_search);
		Init();//初始化
		//ArrayList<Map<String, Object>> array = new ArrayList<Map<String, Object>>();
		//Map<String, Object> map1 = new HashMap<String, Object>();	
			
		mhandler = new Handler();
		
		mButton = (Button) findViewById(R.id.btnSearch2);
		mButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(SearchActivity.this,MainActivity.class);
				startActivity(intent);
			}
		});
		
		delete = (ImageView) findViewById(R.id.ivDeleteText);
		delete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mEditText.setText("");
			}
		});	
		
		mEditText = (EditText) findViewById(R.id.etSearch2);
		mEditText.addTextChangedListener(new TextWatcher() {	
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if(mEditText.length() == 0){
					delete.setVisibility(View.GONE);
				}else{
					delete.setVisibility(View.VISIBLE);
					mhandler.post(eChanged);//handler加runable不太理解！
				}
			}
		});	
	}
	
	//通用的适配器
	class MyAdapter extends SimpleAdapter
	{
		public MyAdapter(Context context, ArrayList<? extends Map<String, ?>> data,
				int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View result = super.getView(position, convertView, parent);
			
			if (result == null) {
				convertView = getLayoutInflater().inflate(R.layout.listview, null);	
			}
			return result;
		}
	
	}
	
}
