package com.example.test1;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;

public class WelcomeActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		
		final Intent intent = new Intent(this,HomePageActivity.class);
		
		Timer timer = new Timer();
		
		TimerTask timertask = new TimerTask(){
			@Override
			public void run() {
				startActivity(intent);
				WelcomeActivity.this.finish();
			}
		};
		
		timer.schedule(timertask, 3000);//任务（开启新的Activity）+延时		
	}
}
