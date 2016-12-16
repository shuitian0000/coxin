package com.codeim.coxin;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapDoubleClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapLoadedCallback;
import com.baidu.mapapi.map.BaiduMap.OnMapLongClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMapStatusChangeListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMyLocationClickListener;
import com.baidu.mapapi.map.BaiduMap.SnapshotReadyCallback;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.ui.base.BaseActivity;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.NavBar;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
import com.codeim.coxin.util.ScaleUtils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;


public class InfoMapActivity extends BaseActivity {
	static final String TAG = "InfoMapActivity";
	
	protected NavBar mNavBar;
	protected Feedback mFeedback;
	
	protected static final int STATE_ALL = 0;
	
	protected MapView mMapView = null;
	protected BaiduMap mBaiduMap;
	protected MapStatus mMapStatus;
	protected TextView map_head_info;
	protected ImageView request_myLocation;
	protected TextView scaleText;
	protected Button btnScale;
	protected ImageButton zoominBtn;
	protected ImageButton zoomoutBtn;
	protected LinearLayout map_footerView;
	protected TextView txt_map_pop;
	protected Button okBtn;
	
    private double latitude;
	private double longitude;
	private String name_txt;
	private String  info_txt="";
	
	private double new_latitude;
	private double new_longitude;
	protected int last_level = -1;
	
	private boolean isRequest = false;//是否手动触发请求定位  
	private boolean selected;
	private boolean isMyLoc=true;
	
	Marker marker=null;
	private PopupWindow mark_popupWindow;
	
	private LocationClient mLocClient;
    /** 
     * 用户位置信息  
     */  
    private MyLocationData mLocData;
    
    private static final int default_zoom = 18;
    private static int min_zoom=3;
    private static int max_zoom=20;

	
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (super._onCreate(savedInstanceState)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(getLayoutId());
			
	        Bundle bundle = getIntent().getExtras();
	        latitude = bundle.getDouble("latitude");
	        longitude = bundle.getDouble("longitude");
	        name_txt = bundle.getString("name");
			
			mNavBar = new NavBar(NavBar.HEADER_STYLE_BACK, this);
			mNavBar.setHeaderTitle(getHeaderTxt());
			mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);
			mPreferences.getInt(Preferences.TWITTER_ACTIVITY_STATE_KEY, STATE_ALL);
			selected = false;
			isMyLoc = false;
			
			setupState();
			setupLocClient();
			
			return true;
		} else {
			return false;
		}
	}
	
    protected int getLayoutId() {
        return R.layout.map_info;
    }
    protected String getHeaderTxt() {
    	return "选择地点";
    }
    protected void setupState() {
    	scaleText=(TextView)findViewById(R.id.btnScaleText);
    	btnScale=(Button)findViewById(R.id.btnScale);
    	zoominBtn = (ImageButton) findViewById(R.id.zoominBtn);
    	zoomoutBtn = (ImageButton) findViewById(R.id.zoomoutBtn);
    	map_footerView = (LinearLayout) findViewById(R.id.map_footerView);
    	txt_map_pop = (TextView) findViewById(R.id.txt_map_pop);
    	okBtn = (Button) findViewById(R.id.okBtn);
    	map_head_info = (TextView) findViewById(R.id.map_head_info); 
    	request_myLocation = (ImageView) findViewById(R.id.request);
    	mMapView = (MapView) findViewById(R.id.bmapView); 
    	mBaiduMap = mMapView.getMap();
    	//普通地图  
    	mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);  
    	//卫星地图  
    	//mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
    	mBaiduMap.setMyLocationEnabled(true);
    	
    	mMapView.showZoomControls(false);
    	mMapView.showScaleControl(false);
    	
    	//register Listener
    	mBaiduMap.setOnMapClickListener(mOnMapClickListener);
    	mBaiduMap.setOnMarkerClickListener(mOnMarkerClickListener);
    	mBaiduMap.setOnMapLoadedCallback(callback);
    	mBaiduMap.setOnMapStatusChangeListener(mOnMapStatusListener);
    	
//        addMark(new LatLng(latitude,longitude), 0);
//        showInfoBoard(String.valueOf(latitude)+";"+String.valueOf(longitude)+":"
//        		+name_txt);
    	setCenter(latitude, longitude, default_zoom);
    }
    protected void openMyLocation() {
//    	// 开启定位图层  
//    	mBaiduMap.setMyLocationEnabled(true);  
//    	// 构造定位数据  
//    	MyLocationData locData = new MyLocationData.Builder()  
//    	    .accuracy(location.getRadius())  
//    	    // 此处设置开发者获取到的方向信息，顺时针0-360  
//    	    .direction(100).latitude(location.getLatitude())  
//    	    .longitude(location.getLongitude()).build(); 
//    	// 设置定位数据  
//    	mBaiduMap.setMyLocationData(locData);  
//    	// 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）  
//    	mCurrentMarker = BitmapDescriptorFactory  
//    	    .fromResource(R.drawable.icon_geo);  
//    	MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);  
//    	mBaiduMap.setMyLocationConfiguration();  
    }
    
    public void setupLocClient() {
    	mLocClient = ((TwitterApplication)getApplication()).mLocationClient;  // 百度位置获取客户端
    	mLocClient.registerLocationListener(new BDLocationListenerImpl());//注册定位监听接口  
    	
    	 //点击按钮手动请求定位  
    	request_myLocation.setOnClickListener(new OnClickListener() {  
            @Override  
            public void onClick(View v) {
                requestLocation();  
            }  
        });  
    	
    	zoomoutBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int mZoom = (int) mBaiduMap.getMapStatus().zoom;
				mZoom = mZoom>min_zoom?mZoom-1:min_zoom;
				refreshZoomButtons(mZoom);
//				setCenter(new_latitude, new_longitude, mZoom);
				mMapStatus = new MapStatus.Builder().zoom(mZoom).build();
		   	    MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		   	    mBaiduMap.animateMapStatus(mMapStatusUpdate);
			}
    	});
    	zoominBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int mZoom = (int) mBaiduMap.getMapStatus().zoom;
				mZoom = mZoom<max_zoom?mZoom+1:max_zoom;
				refreshZoomButtons(mZoom);
//				setCenter(new_latitude, new_longitude, mZoom);
				mMapStatus = new MapStatus.Builder().zoom(mZoom).build();
		   	    MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
		   	    mBaiduMap.animateMapStatus(mMapStatusUpdate);
			}
    		
    	});
    	okBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				// intent.putArrayListExtra("dataList", dataList);
				bundle.putDouble("latitude", new_latitude);
				bundle.putDouble("longitude", new_longitude);
				bundle.putBoolean("change", true);
				bundle.putString("info", info_txt);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				finish();
			}
    	});
    }
    /** 
     * 手动请求定位的方法 
     */  
    public void requestLocation() {  
        isRequest = true;  
          
        if(mLocClient != null && mLocClient.isStarted()){
            //showToast("正在定位......");  
            mLocClient.requestLocation();
            
        }else{  
            Log.d("log", "locClient is null or not started");  
        }  
    }  
    
	private void refreshZoomButtons(int level){
		if(level<max_zoom&&level>min_zoom){
			if(!zoominBtn.isEnabled()){
				zoominBtn.setEnabled(true);
			}
			if(!zoomoutBtn.isEnabled()){
				zoomoutBtn.setEnabled(true);
			}
		}else if(level==max_zoom){
			zoominBtn.setEnabled(false);
		}else if(level==min_zoom){
			zoomoutBtn.setEnabled(false);
		}
	}
	
	private void refreshScale(int zoomLevel){
		String scaleDesc=ScaleUtils.getScaleDesc(zoomLevel);
		//得到比例尺描述
		scaleText.setText(scaleDesc);
		//得到比例尺比例值
		int scale=ScaleUtils.getScale(zoomLevel);
		//得到此比比例例尺下，一个比例单位在地图界面上对应的象素值
		int pixels=ScaleUtils.meterToPixels(mMapView,scale);
		
		LinearLayout.LayoutParams linearParams = (LinearLayout.LayoutParams) btnScale.getLayoutParams();    // 取控件aaa当前的布局参数
		linearParams.width = pixels; 
		btnScale.setLayoutParams(linearParams); 
	}
    
    OnMapStatusChangeListener mOnMapStatusListener = new OnMapStatusChangeListener() {  
        /** 
        * 手势操作地图，设置地图状态等操作导致地图状态开始改变。 
        * @param status 地图状态改变开始时的地图状态 
        */  
        public void onMapStatusChangeStart(MapStatus status){  
        }  
        /** 
        * 地图状态变化中 
        * @param status 当前地图状态 
        */  
        public void onMapStatusChange(MapStatus status){  
        }  
        /** 
        * 地图状态改变结束 
        * @param status 地图状态改变结束后的地图状态 
        */  
        public void onMapStatusChangeFinish(MapStatus status){
	   	    last_level = (int) mBaiduMap.getMapStatus().zoom;
	   	    refreshScale(last_level);
        }  
    };
    
    OnMapClickListener mOnMapClickListener = new OnMapClickListener() {  
        /** 
        * 地图单击事件回调函数 
        * @param point 点击的地理坐标 
        */  
        public void onMapClick(LatLng point){     
//            //先清除图层  
            mBaiduMap.clear();
        	if(mark_popupWindow!=null)
    	        mark_popupWindow.dismiss();
        	map_footerView.setVisibility(View.GONE);
            selected = false;
            isMyLoc = false;
            
        }  
        /** 
        * 地图内 Poi 单击事件回调函数 
        * @param poi 点击的 poi 信息 
        */  
        public boolean onMapPoiClick(MapPoi poi){
        	mBaiduMap.clear();
        	if(mark_popupWindow!=null)
    	        mark_popupWindow.dismiss();
        	map_footerView.setVisibility(View.GONE);
        	
        	LatLng position = poi.getPosition();
        	selected = true;
            new_latitude = position.latitude;  
            new_longitude = position.longitude;
        	
            addMark(position, 0);
            showInfoBoard(String.valueOf(new_latitude)+";"+String.valueOf(new_longitude)+":"
            		+poi.getName());
//                    +arg0.getAddress());
            setCenter(new_latitude, new_longitude, 0);
            info_txt = poi.getName();
        	return false;
        }  
    };
    
    OnMapLoadedCallback callback = new OnMapLoadedCallback() {  
        /** 
        * 地图加载完成回调函数 
        */  
        public void onMapLoaded(){
        	//min_zoom = (int) mBaiduMap.getMinZoomLevel();
        	//max_zoom = (int) mBaiduMap.getMaxZoomLevel();
        	//setCenter(latitude, longitude, default_zoom);
        	refreshScale(default_zoom);
        }  
    };
    
    OnMapDoubleClickListener mOnMapDoubleClickListener = new OnMapDoubleClickListener() {  
        /** 
        * 地图双击事件监听回调函数 
        * @param point 双击的地理坐标 
        */  
        public void onMapDoubleClick(LatLng point){
        }  
    };
    
    OnMapLongClickListener mOnMapLongClickListener = new OnMapLongClickListener() {  
        /** 
        * 地图长按事件监听回调函数 
        * @param point 长按的地理坐标 
        */  
        public void onMapLongClick(LatLng point){  
        }  
    };
    
    OnMarkerClickListener mOnMarkerClickListener = new OnMarkerClickListener() {  
        /** 
        * 地图 Marker 覆盖物点击事件监听函数 
        * @param marker 被点击的 marker 
        */
    	@Override
        public boolean onMarkerClick(Marker marker){
        	final LatLng  position = marker.getPosition();
        	showMarkPop(position);
        	return false;
        }  
    };
    
    OnMyLocationClickListener mOnMyLocationClickListener = new OnMyLocationClickListener() {  
        /** 
        * 地图定位图标点击事件监听函数 
        */  
        public boolean onMyLocationClick(){
        	return false;
        }  
    };
    
    SnapshotReadyCallback mSnapshotReadyCallback = new SnapshotReadyCallback() {  
        /** 
        * 地图截屏回调接口 
        * @param snapshot 截屏返回的 bitmap 数据 
        */  
        public void onSnapshotReady(Bitmap snapshot){  
        }  
    };
    
//    mBaiduMap.setOnMapTouchListener(new OnMapTouchListener() {
//        /**
//        * 当用户触摸地图时回调函数
//        * @param event 触摸事件
//        */
//        public void onTouch(MotionEvent event) {
//        }
//    });
    
    /** 
     * define the my location  button action
     * 定位接口，需要实现两个方法 
     * @author 
     * 
     */  
    public class BDLocationListenerImpl implements BDLocationListener {
        /**
         * 接收异步返回的定位结果，参数是BDLocation类型参数 
         */  
        @Override
        public void onReceiveLocation(BDLocation location) {  
            if (location == null) {  
                return;  
            }

//            StringBuffer sb = new StringBuffer(256);  
//              sb.append("time : ");  
//              sb.append(location.getTime());  
//              sb.append("\nerror code : ");  
//              sb.append(location.getLocType());  
//              sb.append("\nlatitude : ");  
//              sb.append(location.getLatitude());  
//              sb.append("\nlontitude : ");  
//              sb.append(location.getLongitude());  
//              sb.append("\nradius : ");  
//              sb.append(location.getRadius());  
//              if (location.getLocType() == BDLocation.TypeGpsLocation){  
//                   sb.append("\nspeed : ");  
//                   sb.append(location.getSpeed());  
//                   sb.append("\nsatellite : ");  
//                   sb.append(location.getSatelliteNumber());  
//                   } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){  
//                   sb.append("\naddr : ");  
//                   sb.append(location.getAddrStr());  
//                }   
//           
//              Log.e("log", sb.toString());

            new_latitude = location.getLatitude();  
            new_longitude = location.getLongitude();  

//        	mLocData = new MyLocationData.Builder()  
//    	    .accuracy(location.getRadius())  
//    	    // 此处设置开发者获取到的方向信息，顺时针0-360  
//    	    .direction(location.getDerect()).latitude(location.getLatitude())  
//    	    .longitude(location.getLongitude()).build();
//            
//            mBaiduMap.setMyLocationData(mLocData);
//        	// 设置定位图层的配置（定位模式，是否允许方向信息，用户自定义定位图标）  
//            BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory  
//        	    .fromResource(R.drawable.icon_gcoding);  
//        	MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.FOLLOWING, true, mCurrentMarker);  
//        	mBaiduMap.setMyLocationConfigeration(config);
              
            //将定位数据设置到定位图层里  
            //myLocationOverlay.setData(mLocData);
            //更新图层数据执行刷新后生效  
            //mMapView.refresh();
              
              
            //if(isFirstLoc || isRequest){
            if(isRequest){
            	mBaiduMap.clear();
            	isMyLoc = true;
            	
	            addMark(new LatLng(new_latitude,new_longitude), 0);
	            showInfoBoard(String.valueOf(new_latitude)+";"+String.valueOf(new_longitude)+":"
                        +location.getAddrStr());
	            setCenter(new_latitude, new_longitude, 0);
                
	            info_txt = location.getAddrStr();
	            
                isRequest = false;  
            }  
            //isFirstLoc = false;  
              
        }  
  
        /** 
         * 接收异步返回的POI查询结果，参数是BDLocation类型参数 
         */
    }  
    
    @Override  
    protected void onDestroy() {
    	/**
         * MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
         */
        super.onDestroy();  
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理  
        mMapView.onDestroy();
        //退出时销毁定位  
        if (mLocClient != null){
        	((TwitterApplication) getApplication()).removeLocationUpdates();
            mLocClient.stop();
        }  
    }  
    @Override  
    protected void onResume() {  
    	/**
         * MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
         */
        super.onResume();  
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理  
        mMapView.onResume();
        ((TwitterApplication) getApplication()).requestLocationUpdates(true);
        setLocationOption();  // 百度位置
        mLocClient.start();  // 百度位置
        }  
    @Override  
    protected void onPause() {
    	/**
         * MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
         */
        super.onPause();  
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理  
        mMapView.onPause();
        ((TwitterApplication) getApplication()).removeLocationUpdates();
        mLocClient.stop();  // 百度位置
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
 
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
    
    protected void setCenter(double latitude, double longitude, float zoom) {
//    	mBaiduMap.getMapStatus().zoom
    	
   	    LatLng cenpt = new LatLng(latitude,longitude);
   	    //定义地图状态
   	    if(zoom<=0) {
   	   	    mMapStatus = new MapStatus.Builder()
            .target(cenpt)
            .build();
   	    } else {
   	        mMapStatus = new MapStatus.Builder()
            .target(cenpt)
            .zoom(zoom)
            .build();
   	    }
   	    
   	 
   	    //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
   	    MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
   	    mBaiduMap.animateMapStatus(mMapStatusUpdate);
//   	    mBaiduMap.setMapStatus(mMapStatusUpdate);
    }
    
	/**
	 * 反地理编码得到地址信息
	 */
	private void reverseGeoCode(final LatLng latLng) {
		// 创建地理编码检索实例
		GeoCoder geoCoder = GeoCoder.newInstance();
		//
		OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
	        // 反地理编码查询结果回调函数  
	        @Override  
	        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {  
	            if (arg0 == null  
	                    || arg0.error != SearchResult.ERRORNO.NO_ERROR) {
	                // 没有检测到结果  
//	                Toast.makeText(MainActivity.this, "抱歉，未能找到结果",  
//	                        Toast.LENGTH_LONG).show();  
	            }  
//	            Toast.makeText(MainActivity.this,  
//	                    "位置：" + arg0.getAddress(), Toast.LENGTH_LONG) 
//	                    .show();
	            addMark(latLng, 0);
	            showInfoBoard(String.valueOf(new_latitude)+";"+String.valueOf(new_longitude)+":"
                        +arg0.getAddress());
	            setCenter(latLng.latitude, latLng.longitude, 0);
	            //map_head_info.setVisibility(View.VISIBLE);
	            //map_head_info.setText(String.valueOf(new_latitude)+";"+String.valueOf(new_longitude)+":"
                //                      +arg0.getAddress());
//	            sel_location = arg0.getAddress();
//	            location_poiinfo_list = arg0.getPoiList();
	        }  

	        // 地理编码查询结果回调函数  
	        @Override  
	        public void onGetGeoCodeResult(GeoCodeResult arg0) {  
	            if (arg0 == null  
	                    || arg0.error != SearchResult.ERRORNO.NO_ERROR) {  
	                // 没有检测到结果  
	            }  
	        }
		};
		// 设置地理编码检索监听者
		geoCoder.setOnGetGeoCodeResultListener(listener);
		//
		geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
		// 释放地理编码检索实例
		// geoCoder.destroy();
	}
	
	protected void addMark(LatLng point, int arg0) {
		if(0==arg0) arg0 = R.drawable.icon_mark;
		
        // 定义Maker坐标点  
        //LatLng point = new LatLng(latitude, longitude);
        BitmapDescriptor bitmap = BitmapDescriptorFactory  
        	    .fromResource(arg0);
        // 构建MarkerOption，用于在地图上添加Marker  
        MarkerOptions options = new MarkerOptions().position(point)
                .icon(bitmap);
        // 在地图上添加Marker，并显示 
        //mBaiduMap.addOverlay(options);
        marker = (Marker)mBaiduMap.addOverlay(options);
	}
    
	protected void showInfoBoard(String inf) {
		map_footerView.setVisibility(View.VISIBLE);
		txt_map_pop.setText(inf);
		
//		LayoutInflater inflater ;
//		inflater = this.getLayoutInflater () ;
//		View popView = inflater.inflate(R.layout.map_pop_info, null);
//		TextView mText = (TextView)popView.findViewById(R.id.txt_map_pop);
//		mText.setText(inf);
//		mark_popupWindow = new PopupWindow(popView, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
//		mark_popupWindow.setBackgroundDrawable(new ColorDrawable(0));
//	    
//		DisplayMetrics outMetrics = new DisplayMetrics(); 
//		this.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
//		int mScreenHeight = outMetrics.heightPixels;
//		int mScreenWidth = outMetrics.widthPixels;
//		
//		mark_popupWindow.setWidth(mScreenWidth-20);
//		
//	    //设置popwindow显示位置
//		mark_popupWindow.showAsDropDown(request_myLocation, 10, 0);
////	    popupWindow.showAtLocation(mMapView, 0, 2, 10);
//        //获取popwindow焦点
//		mark_popupWindow.setFocusable(true);
//        //设置popwindow如果点击外面区域，便关闭。
//		mark_popupWindow.setOutsideTouchable(false);
//		
//		WindowManager.LayoutParams lp = getWindow().getAttributes();
//		lp.alpha = 0.5f; //0.0-1.0
//		getWindow().setAttributes(lp);
	}
	
	protected void showMarkPop(final LatLng position) {
		LayoutInflater inflater ;
		//InfoWindow popupWindow;
		inflater = this.getLayoutInflater () ;
		View popView = inflater.inflate(R.layout.map_mark_pop, null);
		popView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		
		mark_popupWindow = new PopupWindow(popView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		mark_popupWindow.setBackgroundDrawable(new ColorDrawable(0));
	    
		DisplayMetrics outMetrics = new DisplayMetrics(); 
		this.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		int mScreenHeight = outMetrics.heightPixels;
		int mScreenWidth = outMetrics.widthPixels;
		int popWidth = mark_popupWindow.getContentView().getMeasuredWidth();
		int popHeight = mark_popupWindow.getContentView().getMeasuredHeight();
		
		//mark_popupWindow.setWidth(mScreenWidth-20);
		
	    //设置popwindow显示位置
		//mark_popupWindow.showAsDropDown(, 10, 0);
		mark_popupWindow.showAtLocation(mMapView, 0, mScreenWidth/2-popWidth/2, mScreenHeight/2+popHeight);
        //获取popwindow焦点
		mark_popupWindow.setFocusable(true);
        //设置popwindow如果点击外面区域，便关闭。
		mark_popupWindow.setOutsideTouchable(true);
		
		popView.findViewById(R.id.mark_negativeButton).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mark_popupWindow.dismiss();
			}
		});
		popView.findViewById(R.id.mark_positiveButton).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				// intent.putArrayListExtra("dataList", dataList);
				bundle.putDouble("latitude", position.latitude);
				bundle.putDouble("longitude", position.longitude);
				bundle.putBoolean("change", true);
				bundle.putString("info", info_txt);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}
	
	
	//设置相关参数
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); ////高精度，设置定位模式，高精度，低功耗，仅设备
		option.setOpenGps(true);  //打开gps
		option.setPriority(LocationClientOption.GpsFirst); // 设置GPS优先
		option.setLocationNotify(true);//默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
		option.setCoorType("bd0911");  //设置坐标类型 "gcj02";//国家测绘局标准;"bd09ll";//百度经纬度标准,"bd09";//百度墨卡托标准
//		option.setServiceName("com.baidu.location.service_v2.9");
		option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
//		option.setPoiExtraInfo(true);//是否需要POI的电话和地址等详细信息   
//		option.setPoiDistance(1000); //poi查询距离       
//		option.setPoiNumber(10);//最多返回POI个数   
		option.setIsNeedAddress(true);//设置是否需要地址信息，默认不需要， 只有网络定位才可以
        option.setAddrType("all");
        option.setScanSpan(1000); //默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setPriority(LocationClientOption.NetWorkFirst);  // LocationClientOption.GpsFirst 
		option.disableCache(true);	
		//option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
		//option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
		//option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
		mLocClient.setLocOption(option);
	}
	
}
