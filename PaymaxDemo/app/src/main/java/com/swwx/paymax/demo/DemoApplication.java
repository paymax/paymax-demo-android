package com.swwx.paymax.demo;

import android.app.Application;

import com.lkl.pay.app.application.ApplicationController;


public class DemoApplication extends Application {
	
	@Override
	public void onCreate() {
		//初始化 这个必须要有
		ApplicationController.initData(this);
	}
}
