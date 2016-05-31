package com.example.test1;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Info implements Serializable
{
	private static final long serialVersionUID = -758459502806858414L;
	
	//8项内容
	private String latitude;//经度
	private String longitude;//纬度
	private String picture;//停车场图片
	private String state;//车位状态图
	private String name;//停车场名称
	private String address;//停车场地址
	private String zcw;//总车位
	private String kcw;//空车位

	public static List<Info> infos = new ArrayList<Info>();//成员变量，存储所有marker点的信息，不同Activity间也可以调用，
														  ///不一样的是，把infos的填充方法放在了MainActivity的setDatatoInfo方法中，而不是static中


	//生成时执行的初始化方法
	public Info(){}

	//创建Info的带参方法
	public Info(String latitude, String longitude, String picture, String state, String name,
			String address, String zcw, String kcw)
	{
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.picture = picture;
		this.state = state;
		this.name = name;
		this.address = address;
		this.zcw = zcw;
		this.kcw = kcw;
	}
	
	//经度
	public String getLatitude()
	{
		return latitude;
	}
	public void setLatitude(String latitude)
	{
		this.latitude = latitude;
	}

	//维度
	public String getLongitude()
	{
		return longitude;
	}
	public void setLongitude(String longitude)
	{
		this.longitude = longitude;
	}

	//停车场名称
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}

	//停车场图片
	public String getPicture()
	{
		return picture;
	}
	public void setPicture(String picture)
	{
		this.picture = picture;
	}

	//车位状态图
	public String getState()
	{
		return state;
	}
	public void setState(String state)
	{
		this.state = state;
	}

	//停车场的位置
	public String getAddress()
	{
		return address;
	}
	public void setAddress(String address)
	{
		this.address = address;
	}

	//总车位
	public String getZcw()
	{
		return zcw;
	}
	public void setZcw(String zcw)
	{
		this.zcw = zcw;
	}
	
	//空车位
	public String getKcw()
	{
		return kcw;
	}
	public void setKcw(String kcw)
	{
		this.kcw = kcw;
	}

}
