package com.codeim.coxin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.codeim.coxin.NearbyActivity;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.TwitterApplication.MyOnGetGeoCoderResultListener;
import com.codeim.coxin.app.LazyImageUp;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.app.LazyImageUp.ImageUpCallback;
import com.codeim.coxin.fanfou.Photo;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.location.LocationUtils;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskFeedback;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.NavBar;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
import com.codeim.coxin.view.InfoWheelAddTimeDialog;
import com.codeim.coxin.view.InfoWheelExpireTimeDialog;
import com.codeim.floorview.AlbumActivity;
import com.codeim.floorview.CommentPinnedSectionActivity;
import com.codeim.floorview.MatrixImageActivity;
import com.codeim.floorview.adapter.EmojiAdapter;
import com.codeim.floorview.adapter.EmojiViewPagerAdapter;
import com.codeim.floorview.adapter.AlbumPreviewGridImageAdapter;
import com.codeim.floorview.adapter.CommentWriteMoreAddAdapter;
import com.codeim.floorview.bean.Comment;
import com.codeim.floorview.bean.Emoji;
import com.codeim.floorview.utils.EmojiConversionUtil;
import com.codeim.floorview.utils.TextNumLimitWatcher;
import com.codeim.floorview.view.AlbumViewPager;
import com.codeim.floorview.view.AlbumViewPagerLayoutView;
import com.codeim.floorview.view.DeleteForFullImagePopupWindow;
import com.codeim.floorview.view.EmojiRelativeLayoutView;
import com.codeim.floorview.view.SelectForFullImagePopupWindow;
import com.codeim.floorview.view.SelectForFullImagePopupWindow.OnCheckerClickListener;
import com.codeim.coxin.R;

public class WriteInfoActivity extends BaseWriteActivity{

	static final String TAG = "WriteCommentActivity";
	
	private GenericTask mSendInfoTask;
	
	private LinearLayout write_info_expire;
	private TextView txt_expire_time;
	private LinearLayout view_location;
	private TextView txt_location;
	
	private LocationClient mLocClient;
	private String loc_city;
    private double latitude;
	private double longitude;
	private double gps_latitude;
	private double gps_longitude;
	private String sel_location; //when return, not into first
	private boolean return_or_first_into;
	private List<PoiInfo> location_poiinfo_list = new ArrayList<PoiInfo> ();
	
	private int days;
	private int hours;
	private int mins;

	
//	@Override
//	protected boolean _onCreate(Bundle savedInstanceState) {
//		if (super._onCreate(savedInstanceState)) {
//			setContentView(R.layout.comment_write);
//			mNavBar = new NavBar(NavBar.HEADER_STYLE_BACK, this);
//			mNavBar.setHeaderTitle(Header_txt);
//			mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);
//			mPreferences.getInt(Preferences.TWITTER_ACTIVITY_STATE_KEY, STATE_ALL);
//
//			//the footer menu, from weibo
//			comment_menu_send = (Button) findViewById(R.id.comment_menu_send);
//			comment_content = (EditText) findViewById(R.id.status_new_content);
////			addLocation = (EditText) findViewById(R.id.addInfoPlace);
//			
//			
//			comment_content.addTextChangedListener(
//	                new TextNumLimitWatcher((TextView) findViewById(R.id.comment_menu_send), comment_content, this));
//			comment_content.setDrawingCacheEnabled(true);
//			//正文中的图片
//			grid_addPics=(GridView)this.findViewById(R.id.addPic);
//			dataList.add("camera_default");
//			gridImageAdapter=new AlbumPreviewGridImageAdapter(this, dataList);
//			grid_addPics.setAdapter(gridImageAdapter);
//			//表情
//			emojiRelativeLayoutView = (EmojiRelativeLayoutView) findViewById(R.id.ll_facechoose);
////			mAlbumViewPagerLayoutView = (AlbumViewPagerLayoutView) findViewById(R.id.image_viewpager);
//			//更多
//			mMoreAddAdapter = new CommentWriteMoreAddAdapter(this);
//			moreAddGridView=(GridView)this.findViewById(R.id.moreAdd_gridView);
//			moreAddGridView.setAdapter(mMoreAddAdapter);
//			
//			//for autoCompleteAdapter, not use for temp
////	        AutoCompleteAdapter adapter = new AutoCompleteAdapter(this, comment_content,
////	                (ProgressBar) title.findViewById(R.id.have_suggest_progressbar));
////	        comment_content.setAdapter(adapter);
//			
//			
//			//from fanfou
//			setupState();
////			showInputMethod();
////			registerForContextMenu(getTweetList());
////			registerOnClickListener(getTweetList());
//			
////			InputMethodManager mInputMethodManager = (InputMethodManager) 
//			mInputMethodManager = (InputMethodManager) 
//		    getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
//			mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//			
//			actionBar=getActionBar();
//			actionBar.setDisplayHomeAsUpEnabled(true);
//			//actionBar.setHomeButtonEnabled(true);
			
//	        beforeReturn();
//			return true;
//		} else {
//			return false;
//		}
//	}
	
	@Override
	protected void getParamFromActivity() {
		write_info_expire = (LinearLayout) findViewById(R.id.write_info_expire);
		write_info_expire.setVisibility(View.VISIBLE);
		txt_expire_time = (TextView) findViewById(R.id.txt_expire_time);
		view_location = (LinearLayout) findViewById(R.id.view_location);
		view_location.setVisibility(View.VISIBLE);
		txt_location = (TextView) findViewById(R.id.txt_location);
		
    	days = 2;
    	hours = 0;
    	mins = 0;
    	return_or_first_into = false;
    	
    	write_info_expire.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showExpireTimeDialog();
			}
		});
    	txt_location.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(WriteInfoActivity.this, PoiSearchActivity.class);
			    Bundle bundle=new Bundle();
			    bundle.putDouble("latitude", latitude);
			    bundle.putDouble("longitude", longitude);
			    bundle.putString("city", loc_city);
			    intent.putExtras(bundle);
//				startActivity(intent);
				startActivityForResult(intent, 200);
				
			}
    	});
	}
	@Override
	protected void beforeReturn() {
		mLocClient = ((TwitterApplication)getApplication()).mLocationClient;  // 百度位置获取客户端
	}
	
	public void showExpireTimeDialog() {
	    DisplayMetrics outMetrics = new DisplayMetrics(); 
	    getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
	    int mScreenHeight = outMetrics.heightPixels;
	    int mScreenWidth = outMetrics.widthPixels;

	    final InfoWheelExpireTimeDialog myDialog = new InfoWheelExpireTimeDialog((int) (mScreenWidth * 0.7), android.view.WindowManager.LayoutParams.WRAP_CONTENT,
			WriteInfoActivity.this, R.layout.base_wheel_add_time_dialog, 6, 23, 59);
	    myDialog.setTitleText("设置有效时长");
	    myDialog.show();
	    myDialog.refresh();
	    WindowManager.LayoutParams lp = myDialog.getWindow().getAttributes();
	    lp.width = (int) (mScreenWidth * 0.7);
	    lp.gravity = Gravity.CENTER;
	    myDialog.getWindow().setAttributes(lp);
	
	    final TextView v= (TextView) myDialog.getWindow().findViewById(R.id.title);
	    final Button positiveButton = (Button) myDialog.getWindow().findViewById(R.id.positiveButton);
	    final Button negativeButton = (Button) myDialog.getWindow().findViewById(R.id.negativeButton);
	    positiveButton.setText("确定");
	    negativeButton.setText("取消");
	    positiveButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    // TODO Auto-generated method stub
		    	days = myDialog.getDaysNum();
		    	hours = myDialog.getHoursNum();
		    	mins = myDialog.getMinsNum();
			    myDialog.dismiss();
		        //mChangeTimeInterface.doChangeTime(info.id, "3", info.expireTime, myDialog.getNewTime(), myDialog.getAddTime());
			    String txt="有效时间: " + String.valueOf(days) +"天" + String.valueOf(hours) +"小时" + String.valueOf(mins) +"分钟";
			    txt_expire_time.setText(txt);
//			    txt_expire_time
		    }
	    });
	    negativeButton.setOnClickListener(new View.OnClickListener() {
		    @Override
		    public void onClick(View v) {
			    // TODO Auto-generated method stub
//			    doDelete(info.id);
			    myDialog.dismiss();
		    }
	    });
        myDialog.setOnDismissListener(new OnDismissListener() {

		    @Override
		    public void onDismiss(DialogInterface dialog) {
		    	// TODO Auto-generated method stub
//		    	lp.alpha = 1.0f;
//		    	lp.dimAmount=0.0f;
//		    	getWindow().setAttributes(lp);
//		    	getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		    }
    	
        });
        
        myDialog.btn_toggle.setVisibility(View.GONE);
        myDialog.before_time.setVisibility(View.GONE);
	}
	
	@Override
	protected String getHeaderTxt() {
		return "发布消息";
	}
	
	// send info task
	@Override
	protected void doSendInfo() {
		commentContext = comment_content.getText().toString();
//		commentLocation = addLocation.getText().toString();
		commentLocation = " ";
		
		if (mSendInfoTask != null && mSendInfoTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			if (!TextUtils.isEmpty(commentContext)) {
				mSendInfoTask = new SendInfoTask();
				mSendInfoTask.setFeedback(mFeedback);
				mSendInfoTask.setListener(mSendInfoTaskListener);
				TaskParams params = new TaskParams();
				params.put("commentContext", commentContext);
				params.put("commentPlace", commentLocation);
				/*
				if (mLocation != null) {
				    params.put("latitude", mLocation.getLatitude());
				    params.put("longitude", mLocation.getLongitude());
				    Log.d(TAG, "latitude = " + mLocation.getLatitude());
				    Log.d(TAG, "longitude = " + mLocation.getLongitude());
					mLocationDisplay.setVisibility(View.VISIBLE);
					mLocationDisplay.setText(mLocation.getLatitude() + " " + mLocation.getLongitude());
				} else {
				    params.put("latitude", 22.33);
					params.put("longitude", 114.07);
				}
				*/
				mSendInfoTask.execute(params);
			} else if (TextUtils.isEmpty(commentContext)) {
				warnDialog("请说些什么吧");
			}
		}
	}
	
	private class SendInfoTask extends GenericTask {
		
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			TwitterApplication twitterApplication = (TwitterApplication) getApplication();
			
			try {
				String commentContext = param.getString("commentContext");
				String commentPlace = param.getString("commentPlace");
				double latitude;
				double longitude;
				// double latitude = param.getDouble("latitude");
				// double longitude = param.getDouble("longitude");
				Weibo.Location location = null;
				location = LocationUtils.createFoursquareLocation(twitterApplication.getLastKnownLocation());
				if (twitterApplication.getLastKnownLocation() != null) {
				    latitude = location.getLat();
				    longitude = location.getLon();
				} else {
				    BDLocation BDLoc = twitterApplication.getBDLocation();
					while (BDLoc == null) {
					    BDLoc = twitterApplication.getBDLocation();
					}
					latitude = BDLoc.getLatitude();
					longitude = BDLoc.getLongitude();
				}
				
				//for only simulator debug
				latitude = 31.205174;
				longitude= 121.596926;
				
				// mLat = latitude;
				// mLon = longitude;
//			    mSendInfoFeedback = TwitterApplication.mApi.sendComment(true, commentContext, info_id, parent_id, floornum,
//			    		commentPlace,latitude, longitude).asString();
				
//				mSendInfoFeedback = TwitterApplication.mApi.sendInfo(true, commentContext, commentPlace, 
//						latitude, longitude).asString();
				final long addTime = days*24*60*60 + hours*60*60 + mins*60;
				com.codeim.coxin.fanfou.Info my_info = TwitterApplication.mApi.sendInfo(true, commentContext, commentPlace, 
						String.valueOf(addTime),latitude, longitude);
				
			    final List <String> images = new ArrayList <String>();
			    for(String image: dataList) {
			    	images.add(image);
			    }
			    if(dataList.size()>0) {
			    	String new_info_id = my_info.getId();

//			        TwitterApplication.mImageUp.upMultiImage(images,  new_info_id, new LazyImageUp.ImageUpCallback() {
//			    	    @Override
//			    	    public void refresh(String url, int info_id) {
//			    	    }
//			        });
			    	
			    	//try to use the serial way: one by one
			    	for(String file:dataList) {
			    		if(file.contains("default")) {
			    			
			    		} else {
			    		File photo = new File(file);
			    		    Photo mPhoto=TwitterApplication.mApi.uploadPhoto(1, new_info_id, photo); //1--info
			    		    int photo_id = mPhoto.getId();
			    		}
			    	}
			    }
			    
//				if(!mSendInfoFeedback.equals("ok")) {
//					return TaskResult.FAILED;
//				}
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.FAILED;
			}
			
			return TaskResult.OK;
		}
	}

	@Override
	protected void onSendInfoSuccess() {
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, WriteInfoActivity.this).success("");
		updateProgress("发布成功");
		//mUsernameEdit.setText("");
		//mPasswordEdit.setText("");
		
		//成功发布消息之后
		Toast.makeText(WriteInfoActivity.this, "成功", Toast.LENGTH_SHORT).show();
//		Intent intent = new Intent();
////		Bundle bundle = new Bundle();
//		intent.putExtra("infoId", info_id);
//		intent.putExtra("commentCnt", new_comment_cnt);
////		bundle.putStringArrayList("datalist",selectedDataList);
////		intent.putExtras(bundle);
//		setResult(RESULT_OK, intent);

//		setResult(100);
//		startActivity(intent);
		finish();
	}
	
//	public boolean hasSdcard(){
//		String state = Environment.getExternalStorageState();
//		if(state.equals(Environment.MEDIA_MOUNTED)){
//			return true;
//		}else{
//			return false;
//		}
//	}
	
	
//	//for EmojiAdapter
//	protected Emoji getContextItemTweet(int position) {
////        position = position - 1;
//        // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
//        if (position >= 0 && position < mEmojiAdapter.getCount()) {
//        	Emoji emoji = (Emoji) mEmojiAdapter.getItem(position);
//            if (emoji == null) {
//                return null;
//            } else {
//                return emoji;
//            }
//        } else {
//            return null;
//        }
//	}
	
//	@Override
//	public void onBackPressed() {
//		if(mAlbumViewPagerLayoutView.getVisibility()==View.VISIBLE) {
//			mAlbumViewPagerLayoutView.setVisibility(View.GONE);
//		}
//		else {
//			super.onBackPressed();
//		}
//	}
	
	//设置相关参数
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy); ////高精度，设置定位模式，高精度，低功耗，仅设备
		option.setOpenGps(true);  //打开gps
//		option.setPriority(LocationClientOption.GpsFirst); // 设置GPS优先
		option.setPriority(LocationClientOption.NetWorkFirst);
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
	
    /** 
     * This is really just a dummy observer to get the GPS running
     * since this is the new splash page. After getting a fix, we
     * might want to stop registering this observer thereafter so
     * it doesn't annoy the user too much.
     */
    private class SearchLocationObserver implements Observer {
        @Override
        public void update(Observable observable, Object data) {
        }
    }
    private SearchLocationObserver mSearchLocationObserver = new SearchLocationObserver();
	
	@Override
    protected void onResume() {
        Log.d(TAG, "onResume.");

        super.onResume();
        //checkIsLogedIn();
		((TwitterApplication) getApplication()).requestLocationUpdates(mSearchLocationObserver);
		setLocationOption();  // 百度位置
		mLocClient.start();  // 百度位置
		mLocClient.requestLocation();
		
		if(return_or_first_into) {
			
		} else { //first into the activity, or the gps_place not been changed
			getAndSetLocation();
		}
    }
	
	@Override
    protected void onPause() {
        Log.d(TAG, "onPause.");

        super.onPause();
        
		((TwitterApplication) getApplication()).removeLocationUpdates(mSearchLocationObserver);
		mLocClient.stop();  // 百度位置
    }
	
	public void getAndSetLocation() {
		TwitterApplication twitterApplication = (TwitterApplication) getApplication();
		BDLocation BDLoc = twitterApplication.getBDLocation();
		while (BDLoc == null || (BDLoc.getLatitude() == 0 && BDLoc.getLongitude() == 0)) {
			BDLoc = twitterApplication.getBDLocation();
		}
		gps_latitude = BDLoc.getLatitude();
		gps_longitude = BDLoc.getLongitude();
		latitude = gps_latitude;
		longitude = gps_longitude;
		loc_city = BDLoc.getCity();
		
		reverseGeoCode(new LatLng(latitude, longitude));
	}
	
	/**
	 * 反地理编码得到地址信息
	 */
	private void reverseGeoCode(LatLng latLng) {
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
	            txt_location.setText(arg0.getAddress());
	            sel_location = arg0.getAddress();
	            location_poiinfo_list = arg0.getPoiList();
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
	    if(requestCode == 200){ //从PoiSearchActivity返回
	    //比较requestCode和REQUESTCODE，证明活动是否为REQUESTCODE相关的操作发起。
	        if(resultCode == RESULT_OK){
	        //比较resultCode和SecondActivity中的RESULTCODE，证明SecondActivity活动是否返回成功。
	            Bundle bundle = data.getExtras();
	            String place = bundle.getString("place");
	            
	            if(place!="") {
	                return_or_first_into = true;
	                sel_location = place;
	                txt_location.setText(place);
	            }
//	            Toast.makeText(WriteInfoActivity.this, place, Toast.LENGTH_SHORT).show();
	        }
	    }
	}

}
