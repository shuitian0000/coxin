/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codeim.coxin;

import android.app.Activity;
import android.app.AlertDialog;
// import android.app.AlertDialog.Builder;
// import android.app.PendingIntent;
// import android.app.PendingIntent.CanceledException;
// import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
// import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
// import android.preference.PreferenceManager;
import android.text.TextUtils;
// import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
// import android.widget.TextView;



import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import com.baidu.location.LocationClientOption.LocationMode;
// import com.codeim.coxin.app.Preferences;
// import com.codeim.coxin.fanfou.Configuration;
// import com.codeim.coxin.fanfou.User;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.location.LocationUtils;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskFeedback;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.module.GenderChoose;
import com.codeim.coxin.ui.module.NavBar;
// import com.codeim.coxin.http.HttpAuthException;
//import com.codeim.coxin.R;

import com.codeim.coxin.R;

//登录页面需要个性化的菜单绑定, 不直接继承 BaseNoDoubleClickActivity
public class RegisterActivity extends Activity implements GenderChoose.IOnCheckedChange {
	private static final String TAG = "RegisterActivity";
	private static final String SIS_RUNNING_KEY = "running";
	
	private NavBar mNavBar;

	private String mUsername;
	private String mPassword;
	private String mConfirmPassword;
	private int mGender = 1;  // 性别: 0 女；1 男
  
    // private double mLat;
    // private double mLon;

    private LocationClient mLocClient;	

	// Views.
	private EditText mUsernameEdit;
	private EditText mPasswordEdit;
	private EditText mConfirmPasswordEdit;
	private GenderChoose mGenderChoose;
	private Button mActualRegisterButton;
	// private ProgressDialog dialog;
	// private TextView mLocationDisplay;

	// Preferences.
	// private SharedPreferences mPreferences;

	// Tasks.
	private GenericTask mLoginTask;
	private GenericTask mRegisterTask;
	
	private String mRegisterFeedback;
	
	private TaskListener mRegisterTaskListener = new TaskAdapter() {
		
		@Override
		public void onPreExecute(GenericTask task) {
			onRegisterBegin();
		}
		
		@Override
		public void onProgressUpdate(GenericTask task, Object param) {
			
		}
		
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK && mRegisterFeedback.equals("ok")) {
				onRegisterSuccess();
				// mLocationDisplay.setVisibility(View.VISIBLE);
			    // mLocationDisplay.setText(mLat + " " + mLon);
			} else if (result == TaskResult.OK && !mRegisterFeedback.equals("ok")) {
				TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, RegisterActivity.this).success("");
				// mLocationDisplay.setText(mRegisterFeedback);
				warnDialog(mRegisterFeedback);
			} else {
				 onRegisterFailure("注册失败");
			}
		}
		
		@Override
		public String getName() {
			return "Register";
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		// No Title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		// mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		setContentView(R.layout.newregister);
		mNavBar = new NavBar(NavBar.HEADER_STYLE_BACK, this);
		mNavBar.setHeaderTitle("注册");

		// TextView中嵌入HTML链接
		// TextView registerLink = (TextView) findViewById(R.id.register_link);
		// registerLink.setMovementMethod(LinkMovementMethod.getInstance());

		mUsernameEdit = (EditText) findViewById(R.id.username_edit);
		mPasswordEdit = (EditText) findViewById(R.id.password_edit);
		mConfirmPasswordEdit = (EditText) findViewById(R.id.confirm_password_edit);
		mGenderChoose = (GenderChoose) findViewById(R.id.genderChoose);
		mActualRegisterButton = (Button) findViewById(R.id.register_button);
		
		// mLocationDisplay = (TextView) findViewById(R.id.location_display);
		
		// mUsernameEdit.setOnKeyListener(enterKeyHandler);
		// mPasswordEdit.setOnKeyListener(enterKeyHandler);
		mConfirmPasswordEdit.setOnKeyListener(enterKeyHandler);
		
		mGenderChoose.setTexts("男", "女");
		mGenderChoose.setOnClick(this, 0);
		
		mLocClient = ((TwitterApplication)getApplication()).mLocationClient;

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(SIS_RUNNING_KEY)) {
				if (savedInstanceState.getBoolean(SIS_RUNNING_KEY)) {
					Log.d(TAG, "Was previously logging in. Restart action.");
					// doLogin();
					doRegieter();
				}
			}
		}

		mActualRegisterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// doLogin();
				doRegieter();
			}
		});
	}
	
	@Override
	public void onResume() {
	    super.onResume();
		((TwitterApplication) getApplication()).requestLocationUpdates(false);
		setLocationOption();
		mLocClient.start();
	}
	
	@Override
	public void onPause() {
	    super.onPause();
		((TwitterApplication) getApplication()).removeLocationUpdates();
		mLocClient.stop();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestory");
		if (mLoginTask != null && mLoginTask.getStatus() == GenericTask.Status.RUNNING) {
			mLoginTask.cancel(true);
		}

		// dismiss dialog before destroy
		// to avoid android.view.WindowLeaked Exception
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, RegisterActivity.this).cancel();
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mLoginTask != null && mLoginTask.getStatus() == GenericTask.Status.RUNNING) {
			// If the task was running, want to start it anew when the
			// Activity restarts.
			// This addresses the case where you user changes orientation
			// in the middle of execution.
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
	    if (ev.getAction() == MotionEvent.ACTION_DOWN) {
		    View v = getCurrentFocus();
			if (isShouldHideInput(v, ev)) {
			    hideSoftInput(v.getWindowToken());
			}
		}
		return super.dispatchTouchEvent(ev);
	}
	
	/*
	private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = { 0, 0 };
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }
	*/
	
	private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = { 0, 0 };
            mUsernameEdit.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + mUsernameEdit.getHeight()*3, right = left + mUsernameEdit.getWidth();
            if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }
	
	private void hideSoftInput(IBinder token) {
	    if (token != null) {
	        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		    imm.hideSoftInputFromWindow(mUsernameEdit.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
	
//	//设置相关参数
//	private void setLocationOption() {
//		LocationClientOption option = new LocationClientOption();
//		option.setOpenGps(true);  //打开gps
//		option.setCoorType("bd0911");  //设置坐标类型
//		option.setServiceName("com.baidu.location.service_v2.9");
//		option.setPoiExtraInfo(true);
//        option.setAddrType("all");
//        option.setScanSpan(1000);
//        option.setPriority(LocationClientOption.NetWorkFirst);  // LocationClientOption.GpsFirst 
//		option.setPoiNumber(10);
//		option.disableCache(true);		
//		mLocClient.setLocOption(option);
//	}
	//设置相关参数
	private void setLocationOption() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy); ////高精度，设置定位模式，高精度，低功耗，仅设备
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

	// UI helpers.
	private void updateProgress(String progress) {
		// mProgressText.setText(progress);
	}
	
	// Register task
	private void doRegieter() {
		mUsername = mUsernameEdit.getText().toString();
		mPassword = mPasswordEdit.getText().toString();
		mConfirmPassword = mConfirmPasswordEdit.getText().toString();
		
		if (mRegisterTask != null && mRegisterTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			if (!TextUtils.isEmpty(mUsername) && !TextUtils.isEmpty(mPassword) && !TextUtils.isEmpty(mConfirmPassword)) {
				mRegisterTask = new RegisterTask();
				mRegisterTask.setListener(mRegisterTaskListener);
				TaskParams params = new TaskParams();
				params.put("username", mUsername);
				params.put("password", mPassword);
				params.put("confirmPassword", mConfirmPassword);
				params.put("gender", mGender);
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
				mRegisterTask.execute(params);
			} else if (TextUtils.isEmpty(mUsername)) {
				warnDialog("请输入用户名");
			} else if (TextUtils.isEmpty(mPassword)) {
				warnDialog("请输入密码");
			} else if (TextUtils.isEmpty(mConfirmPassword)) {
				warnDialog("请输入确认密码");
			}
		}
	}
	
	protected void warnDialog(String warn) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(warn);
		builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int which) {
			    dialog.dismiss();
			}
		});
		builder.show();
	}
	
	private void onRegisterBegin() {
		// disableLogin();
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, RegisterActivity.this).start("正在注册");
	}
	
	private void onRegisterSuccess() {
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, RegisterActivity.this).success("");
		updateProgress("");
		//mUsernameEdit.setText("");
		//mPasswordEdit.setText("");

		Log.d(TAG, "Storing credentials.");
		TwitterApplication.mApi.setCredentials(mUsername, mPassword);

		/*
		Intent intent = getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
		String action = intent.getAction();

		if (intent.getAction() == null || !Intent.ACTION_SEND.equals(action)) {
			// We only want to reuse the intent if it was photo send.
			// Or else default to the main activity.
			intent = new Intent(this, MainActivity.class);
		}
		*/
		Intent intent = new Intent(this, UploadAvatarActivity.class);
		
		/*
		// 发送消息给widget
		Intent reflogin = new Intent(this.getBaseContext(), FanfouWidget.class);
		reflogin.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		PendingIntent l = PendingIntent.getBroadcast(this.getBaseContext(), 0, reflogin, PendingIntent.FLAG_UPDATE_CURRENT);
		try {
			l.send();
		} catch (CanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 发送消息给widget_small
		Intent reflogin2 = new Intent(this.getBaseContext(), FanfouWidgetSmall.class);
		reflogin2.setAction("android.appwidget.action.APPWIDGET_UPDATE");
		PendingIntent l2 = PendingIntent.getBroadcast(this.getBaseContext(), 0, reflogin2, PendingIntent.FLAG_UPDATE_CURRENT);
		try {
			l2.send();
		} catch (CanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/

		startActivity(intent);
		finish();
	}
	
	private void onRegisterFailure(String reason) {
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, RegisterActivity.this).failed(reason);
	}
	
	private class RegisterTask extends GenericTask {
		
		// private String msg = getString(R.string.register_status_failure);

		/*
		public String getMsg() {
			return msg;
		}
		*/
		
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			TwitterApplication twitterApplication = (TwitterApplication) getApplication();
			
			try {
				String username = param.getString("username");
				String password = param.getString("password");
				String confirmPassword = param.getString("confirmPassword");
				double latitude;
				double longitude;
				int gender = param.getInt("gender");
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
				
				// mLat = latitude;
				// mLon = longitude;
			    mRegisterFeedback = TwitterApplication.mApi.register(true, username, password, confirmPassword, gender, 
						latitude, longitude).asString();
				Log.d(TAG, "mRegisterFeedback = " + mRegisterFeedback);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.FAILED;
			}
			
			return TaskResult.OK;
		}
	}

	private View.OnKeyListener enterKeyHandler = new View.OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
				if (event.getAction() == KeyEvent.ACTION_UP) {
					// doLogin();
					doRegieter();
				}
				return true;
			}
			return false;
		}
	};

	public static String encryptPassword(String password) {
		// return Base64.encodeToString(password.getBytes(), Base64.DEFAULT);
		return password;
	}

	public static String decryptPassword(String password) {
		// return new String(Base64.decode(password, Base64.DEFAULT));
		return password;
	}
	
	@Override 
	public void leftOnClick(int id) {  // 男
	    mGender = 1;
	}
	
	@Override
	public void rightOnClick(int id) {  // 女
	    mGender = 0;
	}
}