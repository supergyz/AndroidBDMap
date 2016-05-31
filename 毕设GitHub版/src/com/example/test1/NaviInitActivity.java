package com.example.test1;

import android.os.Bundle;
import android.app.Activity;
import android.os.Bundle;
import android.app.Activity;import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRouteGuideManager;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.NaviInitListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;
import com.baidu.navisdk.adapter.PackageUtil;
import com.baidu.navisdk.adapter.base.BaiduNaviSDKProxy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class NaviInitActivity extends Activity {

	public static List<Activity> activityList = new LinkedList<Activity>();

	private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";

	private Button search = null;
	private EditText et1 = null;
	private EditText et2 = null;
	private EditText et3 = null;
	private EditText et4 = null;
	private String mSDCardPath = null;
	
	private Bundle bundle = null;
	
	private String authinfo = null;

	public static final String ROUTE_PLAN_NODE = "routePlanNode";
	public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
	public static final String RESET_END_NODE = "resetEndNode";
	public static final String VOID_MODE = "voidMode";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activityList.add(this);
		
		setContentView(R.layout.activity_navi_init);
		search = (Button) findViewById(R.id.search);
		BNOuterLogUtil.setLogSwitcher(true);
		
		et1 = (EditText) findViewById(R.id.et1);
		et2 = (EditText) findViewById(R.id.et2);
		et3 = (EditText) findViewById(R.id.et3);
		et4 = (EditText) findViewById(R.id.et4);
		
		bundle = getIntent().getExtras();
		
		if(bundle == null){
			Log.d("TAG", "bundle is null !");
		}else{
			et1.setText(bundle.getString("start_jd"));
			et2.setText(bundle.getString("start_wd"));
			et3.setText(bundle.getString("end_jd"));
			et4.setText(bundle.getString("end_wd"));
		}
		
		if (search != null) {
			search.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (BaiduNaviManager.isNaviInited()) {
						routeplanToNavi(CoordinateType.BD09LL);
					}
				}
			});
		}
		
		if (initDirs()) {
			initNavi();		
		}
	}


	private boolean initDirs() {
		mSDCardPath = getSdcardDir();
		if (mSDCardPath == null) {
			return false;
		}
		File f = new File(mSDCardPath, APP_FOLDER_NAME);
		if (!f.exists()) {
			try {
				f.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}


	/**
	 * 内部TTS播报状态回传handler
	 */
	private Handler ttsHandler = new Handler() {
	    public void handleMessage(Message msg) {
	        int type = msg.what;
	        switch (type) {
	            case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
	                 showToastMsg("Handler : TTS play start");
	                break;
	            }
	            case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
	                 showToastMsg("Handler : TTS play end");
	                break;
	            }
	            default :
	                break;
	        }
	    }
	};
	
	/**
	 * 内部TTS播报状态回调接口(暂时用不到)
	 */
	private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {
        
        @Override
        public void playEnd() {
//            showToastMsg("TTSPlayStateListener : TTS play end");
        }
        
        @Override
        public void playStart() {
//            showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };
	
    //Toast显示框，需要在主线程中显示Toast
	public void showToastMsg(final String msg) {
		NaviInitActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(NaviInitActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
	}
	
	//初始化，验证KEY，启动导航引擎
	private void initNavi() {	
	
		BNOuterTTSPlayerCallback ttsCallback = null;

		BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new NaviInitListener() {
			@Override
			public void onAuthResult(int status, String msg) {
				if (0 == status) {
					authinfo = "key校验成功!";
				} else {
					authinfo = "key校验失败, " + msg;
				}
				NaviInitActivity.this.runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Toast.makeText(NaviInitActivity.this, authinfo, Toast.LENGTH_LONG).show();
					}
				});
			}

			public void initSuccess() {
				Toast.makeText(NaviInitActivity.this, "Zhi导航引擎初始化成功", Toast.LENGTH_SHORT).show();
				initSetting();
			}

			public void initStart() {
				Toast.makeText(NaviInitActivity.this, "Zhi导航引擎初始化开始", Toast.LENGTH_SHORT).show();
			}

			public void initFailed() {
				Toast.makeText(NaviInitActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
			}


		},  null, ttsHandler, null);

	}

	//获取本地SD卡目录
	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	//生成路径，需要目的地和出发地，生成导航器
	private void routeplanToNavi(CoordinateType coType) {
		BNRoutePlanNode sNode = null;
		BNRoutePlanNode eNode = null;
		
		double start_jd = Double.parseDouble(bundle.getString("start_jd"));
		double start_wd = Double.parseDouble(bundle.getString("start_wd"));
		double end_jd = Double.parseDouble(bundle.getString("end_jd"));
		double end_wd = Double.parseDouble(bundle.getString("end_wd"));

		//sNode = new BNRoutePlanNode(116.30784537597782, 40.057009624099436, "百度大厦", null, coType);
		//eNode = new BNRoutePlanNode(116.40386525193937, 39.915160800132085, "北京天安门", null, coType);
		sNode = new BNRoutePlanNode(start_jd, start_wd, "我的位置", null, coType);
		eNode = new BNRoutePlanNode(end_jd, end_wd, "目的地", null, coType);

		
		if (sNode != null && eNode != null) {
			List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
			list.add(sNode);
			list.add(eNode);
			BaiduNaviManager.getInstance().launchNavigator(this, list, 1, false, new DemoRoutePlanListener(sNode));
		}
	}

	//路径导航监听器
	public class DemoRoutePlanListener implements RoutePlanListener {

		private BNRoutePlanNode mBNRoutePlanNode = null;

		public DemoRoutePlanListener(BNRoutePlanNode node) {
			mBNRoutePlanNode = node;
		}

		@Override
		public void onJumpToNavigator() {
			/*
			 * 设置途径点以及resetEndNode会回调该接口
			 */
			for (Activity ac : activityList) {
			   
				if (ac.getClass().getName().endsWith("NaviGuideActivity")) {
					return;
				}
			}
			Intent intent = new Intent(NaviInitActivity.this, NaviGuideActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
			intent.putExtras(bundle);
			startActivity(intent);
			
		}

		@Override
		public void onRoutePlanFailed() {
			// TODO Auto-generated method stub
			Toast.makeText(NaviInitActivity.this, "算路失败", Toast.LENGTH_SHORT).show();
		}
	}
	
	//设置导航里的模式
	private void initSetting(){
	    BNaviSettingManager.setDayNightMode(BNaviSettingManager.DayNightMode.DAY_NIGHT_MODE_DAY);
	    BNaviSettingManager.setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
	    BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);	    
        BNaviSettingManager.setPowerSaveMode(BNaviSettingManager.PowerSaveMode.DISABLE_MODE);
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
	}

	//暂时没用到
	private BNOuterTTSPlayerCallback mTTSCallback = new BNOuterTTSPlayerCallback() {

		@Override
		public void stopTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "stopTTS");
		}

		@Override
		public void resumeTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "resumeTTS");
		}

		@Override
		public void releaseTTSPlayer() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "releaseTTSPlayer");
		}

		@Override
		public int playTTSText(String speech, int bPreempt) {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "playTTSText" + "_" + speech + "_" + bPreempt);

			return 1;
		}

		@Override
		public void phoneHangUp() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "phoneHangUp");
		}

		@Override
		public void phoneCalling() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "phoneCalling");
		}

		@Override
		public void pauseTTS() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "pauseTTS");
		}

		@Override
		public void initTTSPlayer() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "initTTSPlayer");
		}

		@Override
		public int getTTSState() {
			// TODO Auto-generated method stub
			Log.e("test_TTS", "getTTSState");
			return 1;
		}
	};

}
