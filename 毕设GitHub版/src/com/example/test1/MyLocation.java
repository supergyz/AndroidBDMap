package com.example.test1;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

/**
 * 
 * 功能：实现我的定位
 * 传递的参数：1.mMapView
 * 			2.mBaiduMap
 * 			3.activity（也就是This）
 * 调用此类需要：new MyLocation(mMapView,mBaiduMap,activity);
 * 
 * @author YzGuo
 *
 */

public class MyLocation{

	public MyLocationListenner myListener = new MyLocationListenner();
	
	private LocationClient mLocClient;
	private MapView mMapView ;
	private BaiduMap mBaiduMap ;
	private MainActivity activity ;
	public double mJD = 0;
	public double mWD = 0;
	

	public MyLocation(){

	}
	
	public MyLocation(MapView index , BaiduMap index1 ,MainActivity index2){
		
		this.mMapView = index;
		this.mBaiduMap = index1;
		this.activity = index2;
		this.getLocation();
	}
	
	public double getMyJD(){
		return mJD;
	}
	
	public double getMyWD(){
		return mWD;
	}
	
	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			
			if (location == null || mMapView == null){
				System.out.print("location is null");
				return;
			}
			
			mWD = location.getLatitude();
			mJD = location.getLongitude();
			
			//Log.d("TAG", msg);
			//从百度地图API中获取定位信息，绑定在mBaiduMap上
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					//此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			
			//设置当前窗口界面的经纬度为所在的位置
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(new LatLng(location.getLatitude(),location.getLongitude()));
			mBaiduMap.animateMapStatus(u);
			
			//设置地图的缩放级为15
			MapStatusUpdate n = MapStatusUpdateFactory.newMapStatus(new MapStatus.Builder().zoom(15).build());
			mBaiduMap.setMapStatus(n);//无法实现每次点击按钮，进行缩放级的一致
			
			//停止定位
			mLocClient.stop();
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
	
	public void getLocation() {
		// 定位初始化
		mLocClient = new LocationClient(activity);
		mLocClient.registerLocationListener(myListener);

		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);//打开gps
		option.setCoorType("bd09ll"); //设置坐标类型
		option.setScanSpan(5000); //定位时间间隔
		mLocClient.setLocOption(option);

		mLocClient.start();
	}
}
