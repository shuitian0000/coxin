package com.codeim.coxin;

import java.util.HashSet;
import java.util.Observer;

//import org.acra.ReportingInteractionMode;
//import org.acra.annotation.ReportsCrashes;


import android.app.Application;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
// import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.codeim.coxin.app.ImageManager;
import com.codeim.coxin.app.LazyImageLoader;
import com.codeim.coxin.app.LazyImageUp;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.db.StatusTable;
import com.codeim.coxin.db.TwitterDatabase;
import com.codeim.coxin.fanfou.Configuration;
import com.codeim.coxin.fanfou.User;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.location.BestLocationListener;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
// import com.codeim.coxin.location.LocationUtils;
import com.codeim.coxin.service.InitFriendService;

//@ReportsCrashes(formKey="dHowMk5LMXQweVJkWGthb1E1T1NUUHc6MQ",
//    mode = ReportingInteractionMode.NOTIFICATION,
//    resNotifTickerText = R.string.crash_notif_ticker_text,
//    resNotifTitle = R.string.crash_notif_title,
//    resNotifText = R.string.crash_notif_text,
//    resNotifIcon = android.R.drawable.stat_notify_error, // optional. default is a warning sign
//    resDialogText = R.string.crash_dialog_text,
//    resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
//    resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
//    resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
//    resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
//)
public class TwitterApplication extends Application {

	public static final String TAG = "TwitterApplication";
	public final static boolean DEBUG = Configuration.getDebug();

	public static ImageManager mImageManager;
	public static LazyImageLoader mImageLoader;
	public static LazyImageUp mImageUp;
	public static TwitterDatabase mDb;
	public static Weibo mApi; // new API
	public static Context mContext;
	public static SharedPreferences mPref;
	
	public LocationClient mLocationClient = null;
	public BDLocation mBDLocation = null;
	public MyLocationListenner myListener = new MyLocationListenner();
	
	public static int networkType = 0;
	
	private BestLocationListener mBestLocationListener = new BestLocationListener();
	
	public GenericTask mUserInfoTask = new GetUserInfoTask();
	
	// FIXME:获取登录用户id, 据肉眼观察，刚注册的用户系统分配id都是~开头的，因为不知道
	// 用户何时去修改这个ID，目前只有把所有以~开头的ID在每次需要UserId时都去服务器
	// 取一次数据，看看新的ID是否已经设定，判断依据是是否以~开头。这么判断会把有些用户
	// 就是把自己ID设置的以~开头的造成,每次都需要去服务器取数。
	// 只是简单处理了mPref没有CURRENT_USER_ID的情况，因为用户在登陆时，肯定会记一个CURRENT_USER_ID
	// 到mPref.
	private static void fetchMyselfInfo() {
		User myself;
		try {
			myself = TwitterApplication.mApi.showUser(TwitterApplication.mApi.getUserId());
			TwitterApplication.mPref.edit().putString(Preferences.CURRENT_USER_ID, myself.getId()).commit();
			TwitterApplication.mPref.edit().putString(Preferences.CURRENT_USER_SCREEN_NAME, myself.getScreenName()).commit();
			TwitterApplication.mPref.edit().putString(Preferences.CURRENT_USER_IMAGE_URL, myself.getAttachmentUrl().toString()).commit();
		} catch (HttpException e) {
			e.printStackTrace();
		}
	}

	public static String getMyselfId(boolean forceGet) {
		if (!mPref.contains(Preferences.CURRENT_USER_ID)){
			if (forceGet && mPref.getString(Preferences.CURRENT_USER_ID, "~").startsWith("~")){
				fetchMyselfInfo();
			}
		}
		return mPref.getString(Preferences.CURRENT_USER_ID, "~");
	}

	public static String getMyselfName(boolean forceGet) {
		if (!mPref.contains(Preferences.CURRENT_USER_ID) || !mPref.contains(Preferences.CURRENT_USER_SCREEN_NAME)) {
			if (forceGet && mPref.getString(Preferences.CURRENT_USER_ID, "~").startsWith("~")){
				fetchMyselfInfo();
			}
		}
		return mPref.getString(Preferences.CURRENT_USER_SCREEN_NAME, "");
	}
	public static String getMyselfImgURL(boolean forceGet) {
		if (!mPref.contains(Preferences.CURRENT_USER_IMAGE_URL) || !mPref.contains(Preferences.CURRENT_USER_IMAGE_URL)) {
			if (forceGet && mPref.getString(Preferences.CURRENT_USER_ID, "~").startsWith("~")){
				fetchMyselfInfo();
			}
		}
		return mPref.getString(Preferences.CURRENT_USER_IMAGE_URL, "");
	}

	@Override
	public void onCreate() {
		// FIXME: StrictMode类在1.6以下的版本中没有，会导致类加载失败。
		// 因此将这些代码设成关闭状态，仅在做性能调试时才打开。
		// //NOTE: StrictMode模式需要2.3+ API支持。
		// if (DEBUG){
		// StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
		// .detectAll()
		// .penaltyLog()
		// .build());
		// StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
		// .detectAll()
		// .penaltyLog()
		// .build());
		// }

		super.onCreate();
		Log.d(TAG, "onCreate");
		
		// 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
		SDKInitializer.initialize(this);

		mContext = this.getApplicationContext();
		mImageManager = new ImageManager(this);
		mImageLoader = new LazyImageLoader();
		mImageUp = new LazyImageUp();
		mApi = new Weibo();
		Log.d(TAG, "TwitterDatabase Instance");
		mDb = TwitterDatabase.getInstance(this);

		mPref = PreferenceManager.getDefaultSharedPreferences(this);
		String username = mPref.getString(Preferences.USERNAME_KEY, "");
		String password = mPref.getString(Preferences.PASSWORD_KEY, "");
		password = LoginActivity.decryptPassword(password);
		
		mLocationClient = new LocationClient(this);
		mLocationClient.registerLocationListener(myListener);

		if (Weibo.isValidCredentials(username, password)) {
			mApi.setCredentials(username, password); // Setup API and HttpClient
			doGetUserInfo();
			
			
            Intent intent = new Intent(this, InitFriendService.class);
            //intent.putExtra(RecorderService.ACTION_NAME, RecorderService.ACTION_DISABLE_MONITOR_REMAIN_TIME);
            startService(intent);
		}
		
		// 为cmwap用户设置代理上网
		String type = getNetworkType();
		if (null != type && type.equalsIgnoreCase("cmwap")) {
			// Toast.makeText(this, "您当前正在使用cmwap网络上网.", Toast.LENGTH_SHORT);
			mApi.getHttpClient().setProxy("10.0.0.172", 80, "http");
		}
	}

	public String getNetworkType() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		// NetworkInfo mobNetInfo = connectivityManager
		// .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (activeNetInfo != null) {
			return activeNetInfo.getExtraInfo(); // 接入点名称: 此名称可被用户任意更改 如: cmwap, cmnet, internet ...
		} else {
			return null;
		}
	}
	
	public BDLocation getBDLocation() {
	    return mBDLocation;
	}

	@Override
	public void onTerminate() {
		// FIXME: 根据android文档，onTerminate不会在真实机器上被执行到
		// 因此这些清理动作需要再找合适的地方放置，以确保执行。
//		cleanupImages();
		mDb.close();
		// Toast.makeText(this, "exit app", Toast.LENGTH_LONG);

		super.onTerminate();
	}

//	private void cleanupImages() {
//		HashSet<String> keepers = new HashSet<String>();
//
//		Cursor cursor = mDb.fetchAllTweets(StatusTable.TYPE_HOME);
//
//		if (cursor.moveToFirst()) {
//			int imageIndex = cursor.getColumnIndexOrThrow(StatusTable.PROFILE_IMAGE_URL);
//			do {
//				keepers.add(cursor.getString(imageIndex));
//			} while (cursor.moveToNext());
//		}
//
//		cursor.close();
//
//		cursor = mDb.fetchAllDms(-1);
//
//		if (cursor.moveToFirst()) {
//			int imageIndex = cursor.getColumnIndexOrThrow(StatusTable.PROFILE_IMAGE_URL);
//			do {
//				keepers.add(cursor.getString(imageIndex));
//			} while (cursor.moveToNext());
//		}
//
//		cursor.close();
//
//		mImageLoader.getImageManager().cleanup(keepers);
//	}
	
	public void doGetUserInfo() {
        if (mUserInfoTask != null && mUserInfoTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
        	mUserInfoTask = new GetUserInfoTask();
        	mUserInfoTask.setListener(new TaskAdapter(){

				@Override
				public String getName() {
					return "GetUserInfo";
				}
        		
        	});
        	mUserInfoTask.execute();
        }
	}
	
	public BestLocationListener requestLocationUpdates(boolean gps) {
        mBestLocationListener.register((LocationManager) getSystemService(Context.LOCATION_SERVICE), gps);
        return mBestLocationListener;
    }

    public BestLocationListener requestLocationUpdates(Observer observer) {
        mBestLocationListener.addObserver(observer);
        mBestLocationListener.register((LocationManager) getSystemService(Context.LOCATION_SERVICE), true);
        return mBestLocationListener;
    }
	
	public void removeLocationUpdates() {
        mBestLocationListener.unregister((LocationManager) getSystemService(Context.LOCATION_SERVICE));
    }

    public void removeLocationUpdates(Observer observer) {
        mBestLocationListener.deleteObserver(observer);
        this.removeLocationUpdates();
    }

    public Location getLastKnownLocation() {
        return mBestLocationListener.getLastKnownLocation();
    }

    public Location getLastKnownLocationOrThrow() throws LocationException {
        Location location = mBestLocationListener.getLastKnownLocation();
        if (location == null) {
            throw new LocationException();
        }
        return location;
    }
    
    public void clearLastKnownLocation() {
        mBestLocationListener.clearLastKnownLocation();
    }
	
	public LocationListener getLocationListener() {
		// TODO Auto-generated method stub
		return mBestLocationListener;
	}
	
	public class GetUserInfoTask extends GenericTask {
		public static final String TAG = "DeleteTask";

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			getMyselfId(true);
			getMyselfName(true);
			
			return TaskResult.OK;
		}
	}
	
	public class MyLocationListenner implements BDLocationListener {
	    @Override
		public void onReceiveLocation(BDLocation location) {
		    if (location == null) {
			    return ;
			} else {
			    mBDLocation = location;
			}
		}
		
		public void onReceivePoi(BDLocation poiLocation) {
		    if (poiLocation == null) {
			    return ;
			}
		}
	}
	
	public class LocationException extends Exception {
	    public LocationException() {
	        super("Unable to determine your location.");
	    }

	    public LocationException(String message) {
	        super(message);
	    }

	    private static final long serialVersionUID = 1L;
	}
	
	public static String GetGeoCodePlace="";
	public class MyOnGetGeoCoderResultListener implements OnGetGeoCoderResultListener {  
        // 反地理编码查询结果回调函数  
        @Override  
        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {  
            if (arg0 == null  
                    || arg0.error != SearchResult.ERRORNO.NO_ERROR) {  
                // 没有检测到结果  
//                Toast.makeText(MainActivity.this, "抱歉，未能找到结果",  
//                        Toast.LENGTH_LONG).show();  
            }  
//            Toast.makeText(MainActivity.this,  
//                    "位置：" + arg0.getAddress(), Toast.LENGTH_LONG)  
//                    .show();  
            GetGeoCodePlace = arg0.getAddress();
        }  

        // 地理编码查询结果回调函数  
        @Override  
        public void onGetGeoCodeResult(GeoCodeResult arg0) {  
            if (arg0 == null  
                    || arg0.error != SearchResult.ERRORNO.NO_ERROR) {  
                // 没有检测到结果  
            }  
        }
    }

	/**
	 * 反地理编码得到地址信息
	 */
	public String reverseGeoCode(LatLng latLng) {
		// 创建地理编码检索实例
		GeoCoder geoCoder = GeoCoder.newInstance();
		//
		OnGetGeoCoderResultListener listener = new MyOnGetGeoCoderResultListener();
		// 设置地理编码检索监听者
		geoCoder.setOnGetGeoCodeResultListener(listener);
		//
		geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
		// 释放地理编码检索实例
		// geoCoder.destroy();
		return GetGeoCodePlace;
	}
}
