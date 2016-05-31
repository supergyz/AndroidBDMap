package com.example.test1;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.widget.Switch;

public class TheFirst extends Activity {//不实现GuideAcitivity的功能，全都跳转到主界面
	
	private boolean isFirst ;
	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what){
			case 1:
				startActivity(new Intent(TheFirst.this,WelcomeActivity.class));
				TheFirst.this.finish();
				break;
			case 2:
				startActivity(new Intent(TheFirst.this,WelcomeActivity.class));
				TheFirst.this.finish();
				break;
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.thefirst);
		
		SharedPreferences mshare = getSharedPreferences("setting", MODE_PRIVATE);
		isFirst = mshare.getBoolean("isFirst", true);  
		
		if(isFirst){
			handler.sendEmptyMessageDelayed(1, 200);
		}else{
			handler.sendEmptyMessageDelayed(2, 200);
		}
	}
	
}





