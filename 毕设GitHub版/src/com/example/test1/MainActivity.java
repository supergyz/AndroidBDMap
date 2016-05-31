package com.example.test1;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.test1.Info;
import com.lidroid.xutils.BitmapUtils;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.thinkland.sdk.android.DataCallBack;
import com.thinkland.sdk.android.JuheData;
import com.thinkland.sdk.android.Parameters;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//bGcvrBWY8TVLHEIuDpdasY8ZTHLepIPp 公司电脑
//aKLN6Kadxyuqb0tbx1WLrwLb 笔记本
public class MainActivity extends Activity {
	
	private MapView mMapView = null;
	private RelativeLayout mMarkerInfoLy;//定义相信信息框的布局
	private BaiduMap mBaiduMap;
	private BitmapDescriptor mIconMaker;
	private MainActivity mActivity = this;
	private EditText mEditText = null;
	private Button back1 = null;
	private Context mContext = null;
	private JSONArray jsonarray = null;//很重要，服务器端返回的获取结果
	private Bitmap bm = null;//存储Marker点的图标
	private MyLocation mylocation = null;
	
	private class ViewHolder
	{
		ImageView infoImg;
		TextView infoName;
		TextView infoAddress;
		TextView infoKcw;
		TextView infoZcw;
	}
	
	protected void popupInfo(RelativeLayout mMarkerLy, Info info)//点击Marker点，先存入ViewHolder，然后显示在控件上
	{															 //加上IF是为了判断是否是 ！第一次！点击MARKER,从而知道布局文件中是否是有数据
																//当点击initMapClickEvent后，布局消失，tag存在。
		ViewHolder viewHolder = null;
		
		if (mMarkerLy.getTag() == null)
		{
			viewHolder = new ViewHolder();
			viewHolder.infoImg = (ImageView) mMarkerLy
					.findViewById(R.id.info_img);
			viewHolder.infoName = (TextView) mMarkerLy
					.findViewById(R.id.info_name);
			viewHolder.infoAddress = (TextView) mMarkerLy
					.findViewById(R.id.info_distance);
			viewHolder.infoKcw = (TextView) mMarkerLy
					.findViewById(R.id.info_kcw);
			viewHolder.infoZcw = (TextView) mMarkerLy
					.findViewById(R.id.info_zcw);

			mMarkerLy.setTag(viewHolder);
		}
		
		viewHolder = (ViewHolder) mMarkerLy.getTag();//info.getPicture()
		
		BitmapUtils bitmapUtils= new BitmapUtils(MainActivity.this);
		bitmapUtils.display(viewHolder.infoImg, info.getPicture());//网络图片
		viewHolder.infoAddress.setText(info.getAddress());
		viewHolder.infoName.setText(info.getName());
		viewHolder.infoKcw.setText(info.getKcw());
		viewHolder.infoZcw.setText(info.getZcw());
		
		NaviSkipClickEvent(info);
	}
		
	
	public void setStaticDataToInfo(){
		Info.infos.clear();
		
		//增加学校内的停车点
		Info.infos.add(new Info( "116.425130", "39.977404", "http://images.juheapi.com/park/6202.jpg" , "http://images.juheapi.com/park/P1004.png", "北京化工大学招待所", "北京市朝阳区北京化工大学西门", "123", "88"));
		Info.infos.add(new Info( "116.427915", "39.978952", "http://images.juheapi.com/park/6202.jpg" , "http://images.juheapi.com/park/P1001.png", "北京化工大学科技大厦", "北京市朝阳区北京化工大学", "123", "77"));
		Info.infos.add(new Info( "116.430466", "39.976727", "http://images.juheapi.com/park/6202.jpg" , "http://images.juheapi.com/park/P1003.png", "北京化工大学电教楼", "北京市朝阳区北京化工大学", "123", "66"));
		
		addInfosOverlay(Info.infos);//把存储在Info中的信息填充到Marker点中，显示在地图上。
	}
	
	//北京化工大学 百度地图：39.9776840000,116.4280790000
	//		       高德地图：39.9715961669,116.4215747657
	//			差值  ：00.00608        00.00650   
	
	public void getDatafromJuhe(){//调用API传值，并接受JSON数据；只查询北化附近500m的停车场，获得结果JSONArray
		jsonarray = new JSONArray();//每次获取数据时需要把array清空
		Parameters params = new Parameters();
		
		params.add("key", "your key");//使用高德地图
		params.add("JD", "116.4215747657");
		params.add("WD", "39.9715961669");
		params.add("JLCX", 700);
		params.add("SDXX", 1);
		/**
		 * 请求的方法 参数: 第一个参数 当前请求的context 第二个参数 接口id 第三个参数 接口请求的url 第四个参数 接口请求的方式
		 * 第五个参数 接口请求的参数,键值对com.thinkland.sdk.android.Parameters类型; 第六个参数
		 * 请求的回调方法,com.thinkland.sdk.android.DataCallBack;
		 * 这是一个异步操作，进程不会等带，所以的到的json数据无法SET到Infos中！！！！！
		 */
		JuheData.executeWithAPI(mContext, 133, "http://japi.juhe.cn/park/nearPark.from",
				JuheData.GET, params, new DataCallBack() {
					/**
					 * 请求成功时调用的方法 statusCode为http状态码,responseString为请求返回数据.
					 */
					@Override
					public void onSuccess(int statusCode, String responseString) {
						// TODO Auto-generated method stub
						
						try {
							JSONObject jsonobject = new JSONObject(responseString);
							JSONArray indexJsonArray = jsonobject.getJSONArray("result");
							Log.d("TAG","This is the total length of data:" + indexJsonArray.length()); 
							jsonarray = indexJsonArray;
							setDatatoInfo();//把得到的数据先存储到Info中
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}						
						
					}

					/**
					 * 请求完成时调用的方法,无论成功或者失败都会调用.
					 */
					@Override
					public void onFinish() {
						// TODO Auto-generated method stub
						Toast.makeText(getApplicationContext(), "finish",
								Toast.LENGTH_SHORT).show();
					}

					/**
					 * 请求失败时调用的方法,statusCode为http状态码,throwable为捕获到的异常
					 * statusCode:30002 没有检测到当前网络. 30003 没有进行初始化. 0
					 * 未明异常,具体查看Throwable信息. 其他异常请参照http状态码.
					 */
					@Override
					public void onFailure(int statusCode, String responseString, Throwable throwable) {
						// TODO Auto-generated method stub
						Log.d("TAG","statusCode:" + statusCode + "/n" + throwable.getMessage());
					}
				});
		
	}
	
	
	public void setDatatoInfo(){//把得到的数据先存储到Info中
		//Info.infos.clear();
		
		String jd = "";
		String wd = "";
		String ccmc = "";
		String ccdz = "";
		String zcw = "";
		String kcw = "";
		String cctp = "";
		String kcwzt = "";
		
		for(int i=0; i<jsonarray.length();i++){
			try {
				//tv.append("CCMC:" + jsonobject2.getString("CCMC") +"KCW:"+ jsonobject2.getString("KCW")+ "\n");
				JSONObject jsonobject = jsonarray.getJSONObject(i);
				
				jd =   jsonobject.getString("JD");//经度
				wd =   jsonobject.getString("WD");//维度
				
				double index1 = Double.parseDouble(jd);
				double index2 = Double.parseDouble(wd);
				
				//高德坐标转化为百度坐标
				index1 = index1 + 0.00650;
				index2 = index2 + 0.00608;
				
				jd = index1 + "";
				wd = index2 + "";
				
				ccmc = jsonobject.getString("CCMC");//车场名称
				ccdz = jsonobject.getString("CCDZ");//车场地址
				zcw =  jsonobject.getString("ZCW");//总车位
				kcw =  jsonobject.getString("KCW");//空车位
				cctp = "http://images.juheapi.com/park/" + jsonobject.getString("CCTP");//停车场图片
				kcwzt = "http://images.juheapi.com/park/" + jsonobject.getString("KCWZT");//车位状态图片
				
				//Log.d("TAG",kcwzt);
				//Log.d("TAG",wd+"!");
				
				Info.infos.add(new Info( jd, wd, cctp, kcwzt, ccmc, ccdz, zcw, kcw));
				
			} catch (JSONException e) {
				e.printStackTrace();
				
			}
		}
		Log.d("TAG","Finish set data to Infos");
		addInfosOverlay(Info.infos);//把存储在Info中的信息填充到Marker点中，显示在地图上。
	}
	
	
	/**Bitmap放大的方法*/ 
	private static Bitmap big(Bitmap bitmap) { 
		Matrix matrix = new Matrix(); 
		matrix.postScale(2.0f,2.0f); //长和宽放大缩小的比例 
		Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true); 
		return resizeBmp; 
	} 
	
	
	public void addInfosOverlay(final List<Info> infos)//初始化，根据INFO增加Marker点,
	{
		new Thread (){//一进来就开启新进程，因为网络读取图片不能再主线程中操作
			
		@SuppressLint("NewApi") public void run() {
		
		//mBaiduMap.clear();
		LatLng latLng = null;
		OverlayOptions overlayOptions = null;
		Marker marker = null;
		
		for (Info info : infos)
		{
			Double latitude = Double.parseDouble(info.getLatitude());//转化为Double经度
			Double longitude = Double.parseDouble(info.getLongitude());//转化为Double纬度
			
			latLng = new LatLng(longitude,latitude);//纬度，经度
			
			//mIconMaker = BitmapDescriptorFactory.fromResource(R.drawable.maker);//把图片转化为BitmapDescriptor格式
			
					try {
						Log.d("TAG","123" + info.getState());
						URL url = new URL(info.getState().toString());
						//BitmapFactory.Options options = new BitmapFactory.Options();   
	        	        bm = BitmapFactory.decodeStream(url.openStream());//把网上的图片（URL地址）转化成Bitmap只需要这一句
	        	        Bitmap bitmap = big(bm);//放大图片,其实是新建了一个bitmap
	        	        mIconMaker = BitmapDescriptorFactory.fromBitmap(bitmap);//把图片转化为BitmapDescriptor格式
	        	        
	        		} catch (MalformedURLException e) {
	        			e.printStackTrace();
	        		} catch (IOException e) {
	        			e.printStackTrace();
	        		}
			
			overlayOptions = new MarkerOptions().position(latLng)
					.icon(mIconMaker).zIndex(10);//zIndex没什么用
			marker = (Marker) (mBaiduMap.addOverlay(overlayOptions));
			//初始化marker的时候，只需要填充两个信息：1.经纬度2.显示图片
			//额外的信息需要用Bundle存储该marker点的Info数据源
			//存入的信息放入键值对中，每次都NEW一个Bundle对象
			//取出时只需要用marker.getExtraInfo().get("info");
			Bundle bundle = new Bundle();//bundel类似于session
			bundle.putSerializable("info", info);
			marker.setExtraInfo(bundle);//把INFO信息存入marker点中
		}
		Log.d("TAG","Finish add Markers");
		
		//把生成的最后一个点的经纬度信息，作为地图显示的中心点。
		//MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
		//mBaiduMap.setMapStatus(u);
		
		skip_from_search();//run方法结束时，一系列操作完成后，判断是不是从search跳转回来
		//把marker点的位置弄正确，加上校园里的点（在setDataToInfo方法中加上info,add），导航
		}
		}.start();
		
	}
	
	
	private void initMarkerClickEvent(){//Marker点击事件
		//对 marker 添加点击相应事件
        mBaiduMap.setOnMarkerClickListener(new OnMarkerClickListener() {
    		@Override
    		public boolean onMarkerClick(Marker marker) {
    			
    			Info info = (Info) marker.getExtraInfo().get("info");
    			
    			mMarkerInfoLy.setVisibility(View.VISIBLE);
    			popupInfo(mMarkerInfoLy, info);//把存在marker点中的信息存入布局框架中
    			
    			//Toast.makeText(getApplicationContext(), "MarkerA被点击了！", Toast.LENGTH_SHORT).show();
    			return false;
    		}
    	});
	}
	
	
	private void initMapClickEvent()//点击地图，使窗口消失
	{
		mBaiduMap.setOnMapClickListener(new OnMapClickListener()
		{

			@Override
			public boolean onMapPoiClick(MapPoi arg0)
			{
				return false;
			}

			@Override
			public void onMapClick(LatLng arg0)
			{
				mMarkerInfoLy.setVisibility(View.GONE);
			}
		});
	}
	
	
	private void IninMapLocation(){//初始化，定位地图中心
		LatLng cenpt = new LatLng(39.9776840000,116.4280790000); 
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder()
        .target(cenpt)
        .zoom(16)//数字越大，地图越放大
        .build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        mBaiduMap.setMyLocationEnabled(true);//使用百度地图定位图层时，要先开启定位
	}
	
	
	private void TheMarker(){//Marker
		//定义Maker坐标点  
        //LatLng point = new LatLng(39.9768880000,116.4270740000);  
        //LatLng point1 = new LatLng(39.9789740000,116.4268280000);  
        
        //BitmapDescriptor bitmap = BitmapDescriptorFactory  
          //  .fromResource(R.drawable.icon_marka);  
        //BitmapDescriptor bitmap1 = BitmapDescriptorFactory  
	      //      .fromResource(R.drawable.icon_markb);
        //生长动画
        //MarkerOptions option = new MarkerOptions().position(point).icon(bitmap)
          //      .zIndex(5).period(10);
        	//option.animateType(MarkerAnimateType.grow);
        	
        //MarkerOptions option1 = new MarkerOptions().position(point1).icon(bitmap1)
	      //      .zIndex(5).period(10);
	        //option1.animateType(MarkerAnimateType.grow);
	        	
        //final Marker markera = (Marker) mBaiduMap.addOverlay(option);
        //final Marker markerb = (Marker) mBaiduMap.addOverlay(option1);
	}
	
	
	private void MyLocationClickEvent(){//点击定位按钮，显示当前定位信息
		//这行代码写在初始化函数中，mBaiduMap.setMyLocationEnabled(true);//使用百度地图定位图层时，要先开启定位
		Button mButton =(Button) findViewById(R.id.btn_location);
		
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mylocation = new MyLocation(mMapView,mBaiduMap,mActivity);
			}
		});
	}
	
	
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			mMarkerInfoLy.setVisibility(View.VISIBLE);
			Info info = (Info) msg.getData().getSerializable("info8");
			if(info == null ){
				Log.d("TAG", "info is null");
			}
			else{	
				popupInfo(mMarkerInfoLy, info);//把存在marker点中的信息存入布局框架中
			}
		}
	};
	
		
	private void skip_from_search(){//从serach里跳转来 的时候，把marker点显示出来
		
		Bundle bundle = new Bundle();
		bundle = getIntent().getExtras();
		
		if(bundle != null){
			Log.d("TAG", "从 搜索页面 跳转回来的");
			String str = bundle.getString("click_item");//得到停车场名字，然后从Infos中循环判断是哪个，然后调用popinfo。
			
			for(Info info : Info.infos){
				if(str.equals(info.getName())){
					Log.d("TAG", "循环里："+info.getName());					
					Bundle index = new Bundle();
					Message msg = new Message();
					index.putSerializable("info8", info);
					msg.setData(index);
					handler.sendMessage(msg);
				}
			}
		}
	}
	
	
	private void NaviSkipClickEvent(final Info info){//点击”到这去“按钮，跳转到导航初始化界面
		Button navi = (Button) findViewById(R.id.navi);
		
		
		navi.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {//要先点击定位按钮！！！！
				Intent intent = new Intent(MainActivity.this,NaviInitActivity.class);
				Bundle bundle = new Bundle();
				
				String start_wd = "" , start_jd = "", end_wd = "", end_jd = "";
				
				end_jd = info.getLatitude();//转化为Double纬度
				end_wd = info.getLongitude();//转化为Double经度
				
				Log.d("TAG","（放入Bundle）目的地经纬度：" + end_wd + "  "+ end_jd);
				
				bundle.putString("end_wd", end_wd);
				bundle.putString("end_jd", end_jd);
				
				if(mylocation != null){
					start_jd = mylocation.getMyJD() + "";
					start_wd = mylocation.getMyWD() + "";
				}
				
				Log.d("TAG","（放入Bundle）定位的经纬度：" + start_wd + "  "+ start_jd);
				
				bundle.putString("start_wd", start_wd);
				bundle.putString("start_jd", start_jd);
				
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
	}
	
	
	private void NormalMapClickEvent(){//点击按钮，显示普通地图
		Button button = (Button) findViewById(R.id.btn_normal_map);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);//普通地图
				mBaiduMap.setTrafficEnabled(false);//交通图
			}
		});		
	}
	
	
	private void SatellitMapClickEvent(){//点击按钮，显示卫星地图
		Button button = (Button) findViewById(R.id.btn_weixing_map);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);//卫星图
			}
		});
	}
	
	
	private void TrafficMapClickEvent(){//点击按钮，显示交通地图
		Button button = (Button) findViewById(R.id.btn_traffic_map);
		button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				mBaiduMap.setTrafficEnabled(true);//交通图
			}
		});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext()); 
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,R.layout.custom_title_main);
		mContext = this;
		
		mEditText = (EditText) findViewById(R.id.etSearch1);
		mEditText.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this,SearchActivity.class);
				startActivity(intent);
			}
		});
		
		back1 = (Button) findViewById(R.id.back1);
		back1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(MainActivity.this,HomePageActivity.class);
				startActivity(intent);
			}
		});
		
		mMarkerInfoLy = (RelativeLayout) findViewById(R.id.id_marker_info);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);//普通地图

		IninMapLocation();
		
		Log.d("TAG","This is the start!");
		
		setStaticDataToInfo();
		getDatafromJuhe();//从服务器端获取数据填充到Info中
		//把两个方法按顺序执行！！！
		//setDatatoInfo();//把得到的数据先存储到Info中
	    //addInfosOverlay(Info.infos);//把存储在Info中的信息填充到Marker点中，显示在地图上。
		Log.d("TAG","This is the end!");
		
		initMarkerClickEvent();//点击Marker点
	    initMapClickEvent();//点击地图
	    
	    
	    MyLocationClickEvent();//当前定位
	    NormalMapClickEvent();//显示正常地图
	    TrafficMapClickEvent();//显示交通图
	    SatellitMapClickEvent();//显示卫星图
	    
	}
	@Override  
    protected void onDestroy() {  
        super.onDestroy();  
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理  
        mMapView.onDestroy();  
        mIconMaker.recycle();
		mMapView = null;
		JuheData.cancelRequests(mContext);
    }  
    @Override  
    protected void onResume() {  
        super.onResume();  
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理  
        mMapView.onResume();  
        }  
    @Override  
    protected void onPause() {  
        super.onPause();  
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理  
        mMapView.onPause();  
    }  
    
	
}
