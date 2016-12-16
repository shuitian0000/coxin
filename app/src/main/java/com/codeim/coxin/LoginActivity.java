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

import java.io.IOException;
// import java.text.SimpleDateFormat;



import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.fanfou.User;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.http.HttpAuthException;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.service.InitFriendService;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskFeedback;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
//import com.codeim.coxin.weibo.ConstantS;
//import com.codeim.coxin.R;
//import com.weibo.sdk.android.Oauth2AccessToken;
//import com.weibo.sdk.android.Weibo;
//import com.weibo.sdk.android.WeiboAuthListener;
//import com.weibo.sdk.android.WeiboDialogError;
//import com.weibo.sdk.android.WeiboException;
//import com.weibo.sdk.android.sso.SsoHandler;
//import com.weibo.sdk.android.util.AccessTokenKeeper;

import com.codeim.coxin.R;

//登录页面需要个性化的菜单绑定, 不直接继承 BaseNoDoubleClickActivity
public class LoginActivity extends Activity {
	private static final String TAG = "LoginActivity";
	private static final String SIS_RUNNING_KEY = "running";

	private String mUsername;
	private String mPassword;

	// Views.
	private EditText mUsernameEdit;
	private EditText mPasswordEdit;
	private Button mSigninButton;
	private Button mRegisterButton;
	private Button mBrowseButton;
	private Button mWeiboSigninButton;

	// Preferences.
	private SharedPreferences mPreferences;

	// Tasks.
	private GenericTask mLoginTask;
	private GenericTask mLoginWeiboTask;
	private GenericTask mGetWeiboUserInfoTask;

	private User user;
	
	/** 注意：SsoHandler 仅当sdk支持sso时有效 */
    //private SsoHandler mSsoHandler;
	
	/** 微博API接口类，提供登陆等功能  */
    private Weibo mWeibo;
    
    /** 封装了 "access_token"，"expires_in"，"refresh_token"，并提供了他们的管理功能  */
    //private Oauth2AccessToken mAccessToken;

	private TaskListener mLoginTaskListener = new TaskAdapter() {

		@Override
		public void onPreExecute(GenericTask task) {
			onLoginBegin();
		}

		@Override
		public void onProgressUpdate(GenericTask task, Object param) {
			updateProgress((String) param);
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				onLoginSuccess();
			} else {
			    if (task == mLoginTask) {
				    onLoginFailure(((LoginTask) task).getMsg());
//				} else if (task == mLoginWeiboTask) {
//				    onLoginFailure(((LoginWeiboTask) task).getMsg());
				}
			}
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return "Login";
		}
	};
	
//	private TaskListener mGetWeiboUserInfoTaskListener = new TaskAdapter() {
//		@Override
//		public void onPreExecute(GenericTask task) {
//			
//		}
//
//		@Override
//		public void onProgressUpdate(GenericTask task, Object param) {
//			updateProgress((String) param);
//		}
//
//		@Override
//		public void onPostExecute(GenericTask task, TaskResult result) {
//			if (result == TaskResult.OK) {
//				String token = mPreferences.getString(Preferences.WEIBO_ACCESS_TOKEN_KEY, "");
//				String expires_in = mPreferences.getString(Preferences.WEIBO_EXPIRES_IN_KEY, "");
//				String uid = mPreferences.getString(Preferences.WEIBO_UID_KEY, "");
//				String userName = mPreferences.getString(Preferences.WEIBO_USERNAME_KEY, "");
//				String gender = mPreferences.getString(Preferences.WEIBO_GENDER_KEY, "n");
//				doLoginByWeibo(token, expires_in, uid, userName, gender);
//			} else {
//			    
//			}
//		}
//
//		@Override
//		public String getName() {
//			// TODO Auto-generated method stub
//			return "GetWeiboUserInfo";
//		}
//	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		// No Title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		setContentView(R.layout.newlogin);

		mUsernameEdit = (EditText) findViewById(R.id.username_edit);
		mPasswordEdit = (EditText) findViewById(R.id.password_edit);
		mUsernameEdit.setText("");
		mPasswordEdit.setText("");
		
		//mWeibo = Weibo.getInstance(ConstantS.APP_KEY, ConstantS.REDIRECT_URL, ConstantS.SCOPE);
		
		// mUsernameEdit.setOnKeyListener(enterKeyHandler);
		mPasswordEdit.setOnKeyListener(enterKeyHandler);
		mSigninButton = (Button) findViewById(R.id.signin_button);
		mRegisterButton = (Button) findViewById(R.id.register_button);
		mBrowseButton = (Button) findViewById(R.id.browse_button);
		mWeiboSigninButton = (Button) findViewById(R.id.weibo_signin_button);

		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(SIS_RUNNING_KEY)) {
				if (savedInstanceState.getBoolean(SIS_RUNNING_KEY)) {
					Log.d(TAG, "Was previously logging in. Restart action.");
					doLogin();
				}
			}
		}

		mSigninButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doLogin();
			}
		});
		
		mRegisterButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				startActivity(intent);
				// finish();
			}
		});
		
//		mBrowseButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				Intent intent = new Intent(LoginActivity.this, BrowseActivity.class);
//				startActivity(intent);
//				// finish();
//			}
//		});
		
//		mWeiboSigninButton.setOnClickListener(new View.OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				mSsoHandler = new SsoHandler(LoginActivity.this, mWeibo);
//                mSsoHandler.authorize(new AuthDialogListener(), null);
//			}
//		}); 
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "onDestory");
		if (mLoginTask != null && mLoginTask.getStatus() == GenericTask.Status.RUNNING) {
			mLoginTask.cancel(true);
		}

		// dismiss dialog before destroy to avoid android.view.WindowLeaked Exception
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, LoginActivity.this).cancel();
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
	
	private boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l1 = { 0, 0 };
			int[] l2 = { 0, 0 };
            mUsernameEdit.getLocationInWindow(l1);
			mPasswordEdit.getLocationInWindow(l2);
            // int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left + v.getWidth();
			int left = l1[0], top = l1[1], bottom = l2[1] + mPasswordEdit.getHeight(), right = l2[0] + mPasswordEdit.getWidth();
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
	
	/**
     * 微博认证授权回调类。
     * 1. SSO登陆时，需要在{@link #onActivityResult}中调用mSsoHandler.authorizeCallBack后，
     *    该回调才会被执行。
     * 2. 非SSO登陆时，当授权后，就会被执行。
     * 当授权成功后，请保存该access_token、expires_in等信息到SharedPreferences中。
     */
//    class AuthDialogListener implements WeiboAuthListener {
//        
//        @Override
//        public void onComplete(Bundle values) {
//            
//			String gender = "n";
//            String token = values.getString("access_token");
//            String expires_in = values.getString("expires_in");
//			String uid = values.getString("uid");
//			String userName = values.getString("userName");
//            mAccessToken = new Oauth2AccessToken(token, expires_in);
//            if (mAccessToken.isSessionValid()) {
//			    SharedPreferences.Editor editor = mPreferences.edit();
//			    editor.putString(Preferences.WEIBO_ACCESS_TOKEN_KEY, token);
//			    editor.putString(Preferences.WEIBO_EXPIRES_IN_KEY, expires_in);
//				editor.putString(Preferences.WEIBO_UID_KEY, uid);
//				editor.putString(Preferences.WEIBO_USERNAME_KEY, userName);
//				editor.commit();
//                AccessTokenKeeper.keepAccessToken(LoginActivity.this, mAccessToken);
//				doLoginByWeibo(token, expires_in, uid, userName, gender);
//                // doGetWeiboUserInfo(token, uid);
//            }
//        }
//
//        @Override
//        public void onError(WeiboDialogError e) {
//            Toast.makeText(getApplicationContext(), "Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//
//        @Override
//        public void onCancel() {
//            Toast.makeText(getApplicationContext(), "Auth cancel", Toast.LENGTH_LONG).show();
//        }
//
//        @Override
//        public void onWeiboException(WeiboException e) {
//            Toast.makeText(getApplicationContext(), "Auth exception : " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//    }
	
//	@Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        
//        // SSO 授权回调
//        // 重要：发起 SSO 登陆的Activity必须重写onActivityResult
//        if (mSsoHandler != null) {
//            mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
//        }
//    }

	// UI helpers.
	private void updateProgress(String progress) {
		
	}

	private void enableLogin() {
		mUsernameEdit.setEnabled(true);
		mPasswordEdit.setEnabled(true);
		mSigninButton.setEnabled(true);
		mRegisterButton.setEnabled(true);
		mBrowseButton.setEnabled(true);
		mWeiboSigninButton.setEnabled(true);
	}

	private void disableLogin() {
		mUsernameEdit.setEnabled(false);
		mPasswordEdit.setEnabled(false);
		mSigninButton.setEnabled(false);
		mRegisterButton.setEnabled(false);
		mBrowseButton.setEnabled(false);
		mWeiboSigninButton.setEnabled(false);
	}

	// Login task.
	private void doLogin() {
		mUsername = mUsernameEdit.getText().toString();
		mPassword = mPasswordEdit.getText().toString();

		if (mLoginTask != null && mLoginTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			if (!TextUtils.isEmpty(mUsername) && !TextUtils.isEmpty(mPassword)) {
				mLoginTask = new LoginTask();
				mLoginTask.setListener(mLoginTaskListener);

				TaskParams params = new TaskParams();
				params.put("username", mUsername);
				params.put("password", mPassword);
				mLoginTask.execute(params);
			} else {
				updateProgress(getString(R.string.login_status_null_username_or_password));
			}
		}
	}

	private void onLoginBegin() {
		disableLogin();
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, LoginActivity.this).start(getString(R.string.login_status_logging_in));
	}

	private void onLoginSuccess() {
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, LoginActivity.this).success("");
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
//		Intent intent = new Intent(this, MainActivity.class);
		Intent intent = new Intent(this, NewActivity.class);
		
		// 发送消息给widget
//		Intent reflogin = new Intent(this.getBaseContext(), FanfouWidget.class);
//		reflogin.setAction("android.appwidget.action.APPWIDGET_UPDATE");
//		PendingIntent l = PendingIntent.getBroadcast(this.getBaseContext(), 0, reflogin, PendingIntent.FLAG_UPDATE_CURRENT);
//		try {
//			l.send();
//		} catch (CanceledException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// 发送消息给widget_small
//		Intent reflogin2 = new Intent(this.getBaseContext(), FanfouWidgetSmall.class);
//		reflogin2.setAction("android.appwidget.action.APPWIDGET_UPDATE");
//		PendingIntent l2 = PendingIntent.getBroadcast(this.getBaseContext(), 0, reflogin2, PendingIntent.FLAG_UPDATE_CURRENT);
//		try {
//			l2.send();
//		} catch (CanceledException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		startActivity(intent);
		
        Intent initFriend = new Intent(this, InitFriendService.class);
//        intent.putExtra(RecorderService.ACTION_NAME, RecorderService.ACTION_ENABLE_MONITOR_REMAIN_TIME);
        startService(initFriend);
        
		finish();
	}

	private void onLoginFailure(String reason) {
		TaskFeedback.getInstance(TaskFeedback.DIALOG_MODE, LoginActivity.this).failed(reason);
		enableLogin();
	}

	private class LoginTask extends GenericTask {

		private String msg = getString(R.string.login_status_failure);

		public String getMsg() {
			return msg;
		}

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			publishProgress(getString(R.string.login_status_logging_in) + "...");

			try {
				String username = param.getString("username");
				String password = param.getString("password");
				user = TwitterApplication.mApi.login(username, password);
				
				TwitterApplication.getMyselfId(true);
				TwitterApplication.getMyselfName(true);
				
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				// TODO:确切的应该从HttpException中返回的消息中获取错误信息
				// Throwable cause = e.getCause(); // Maybe null
				// if (cause instanceof HttpAuthException) {
				if (e instanceof HttpAuthException) {
					// Invalid userName/password
					msg = getString(R.string.login_status_invalid_username_or_password);
				} else {
					msg = getString(R.string.login_status_network_or_connection_error);
				}
				publishProgress(msg);
				return TaskResult.FAILED;
			}

			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString(Preferences.USERNAME_KEY, mUsername);
			editor.putString(Preferences.PASSWORD_KEY, mPassword);
			
			//ywwang on 20160703 for chat avator
			editor.putString(Preferences.CURRENT_USER_IMAGE_URL, user.getProfileImageURL().toString());

			// add 存储当前用户的id
			editor.putString(Preferences.CURRENT_USER_ID, user.getId());
			editor.commit();

			return TaskResult.OK;
		}
	}
	
//	private void doGetWeiboUserInfo(String token, String uid) {
//	    if (mGetWeiboUserInfoTask !=null && mGetWeiboUserInfoTask.getStatus() == GenericTask.Status.RUNNING) {
//		    return;
//		} else {
//		    if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(uid)) {
//			    mGetWeiboUserInfoTask = new GetWeiboUserInfoTask();
//				mGetWeiboUserInfoTask.setListener(mGetWeiboUserInfoTaskListener);
//				TaskParams params = new TaskParams();
//				params.put("token", token);
//				params.put("uid", uid);
//				mGetWeiboUserInfoTask.execute(params);
//			} else {
//			
//			}
//		}
//	}
	
//	private class GetWeiboUserInfoTask extends GenericTask {
//	    
//		@Override
//		protected TaskResult _doInBackground(TaskParams... params) { 
//			String gender;
//		    TaskParams param = params[0];
//		    
//		    try {
//		    	String token = param.getString("token");
//		    	String uid = param.getString("uid");
//				String source = ConstantS.APP_KEY;
//		    	String url = "https://api.weibo.com/2/users/show.json" + "?uid=" + uid + "&token=" +token;  // "&source=" +source;
//		    	JSONObject weiboUserInfo = TwitterApplication.mApi.getWeiboUserInfo(url);
//		    	gender = weiboUserInfo.getString("gender");
//		    } catch (HttpException e) {
//		    	e.printStackTrace();
//		    	return TaskResult.FAILED;
//		    } catch (JSONException e) {
//		    	e.printStackTrace();
//		    	return TaskResult.FAILED;
//		    }
//		    
//			SharedPreferences.Editor editor = mPreferences.edit();
//	        editor.putString(Preferences.WEIBO_GENDER_KEY, gender);
//	        editor.commit();
//			return TaskResult.OK;
//		
//		    /*
//		    TaskParams param = params[0];
//			String token = null;
//			String uid = null;
//			try {
//				token = param.getString("token");
//				uid = param.getString("uid");
//			} catch (HttpException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			
//			HttpClient client = new DefaultHttpClient();
//			String url = "https://api.weibo.com/2/users/show.json" + "?uid=" + uid +"&access_token=" + token;
//			Log.d(TAG, url);
//			HttpGet get = new HttpGet(url);
//			try {
//			    HttpResponse response = client.execute(get);
//				StatusLine statusLine = response.getStatusLine();
//                int statusCode = statusLine.getStatusCode();
//                if (statusCode == 200) {
//				    HttpEntity entity = response.getEntity();
//                    String string = EntityUtils.toString(entity);
//					JSONObject json=new JSONObject(string);
//					String gender = json.getString("gender");
//					Log.d(TAG, "gender = " + gender);
//					SharedPreferences.Editor editor = mPreferences.edit();
//			        editor.putString(Preferences.WEIBO_GENDER_KEY, gender);
//			        editor.commit();
//					return TaskResult.OK;
//				} else {
//				    return TaskResult.FAILED;
//				}
//				
//			} catch (IOException e) {
//			    e.printStackTrace();
//				return TaskResult.FAILED;
//			} catch (JSONException e) {
//				e.printStackTrace();
//				return TaskResult.FAILED;
//			}
//			*/
//		}
//	}
	
//	private void doLoginByWeibo(String token, String expires_in, String uid, String userName, String gender) {
//	
//		if (mLoginWeiboTask != null && mLoginWeiboTask.getStatus() == GenericTask.Status.RUNNING) {
//			return;
//		} else {
//			if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(expires_in) && 
//			            !TextUtils.isEmpty(uid) && !TextUtils.isEmpty(userName)) {
//				mLoginWeiboTask = new LoginWeiboTask();
//				mLoginWeiboTask.setListener(mLoginTaskListener);  // mLoginWeiboTaskListener
//				TaskParams params = new TaskParams();
//				params.put("token", token);
//				params.put("expires_in", expires_in);
//				params.put("uid", uid);
//				params.put("userName", userName);
//				params.put("gender", gender);
//				mLoginWeiboTask.execute(params);
//			} else {
//				
//			}
//		}
//	}
	
//	private class LoginWeiboTask extends GenericTask {
//		private String msg = getString(R.string.login_status_failure);
//		
//		public String getMsg() {
//			return msg;
//		}
		
//		@Override
//		protected TaskResult _doInBackground(TaskParams... params) {
//			TaskParams param = params[0];
//			JSONObject jsonData;
//			String id = null;
//			String screenName = null;
//			
//			try {
//				String token = param.getString("token");
//				String expires_in = param.getString("expires_in");
//				String uid = param.getString("uid");
//				String userName = param.getString("userName");
//				String gender = param.getString("gender");
//				jsonData = TwitterApplication.mApi.loginWeibo(token, expires_in, uid, userName, gender);
//				id = jsonData.getString("id");
//				screenName = jsonData.getString("screen_name");
//			} catch (HttpException e) {
//				if (e instanceof HttpAuthException) {
//					// Invalid userName/password
//					msg = getString(R.string.login_status_invalid_username_or_password);
//				} else {
//					msg = getString(R.string.login_status_network_or_connection_error);
//				}
//				publishProgress(msg);
//				return TaskResult.FAILED;
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//			SharedPreferences.Editor editor = mPreferences.edit();
//			mUsername = screenName;
//			mPassword = "";
//			editor.putString(Preferences.USERNAME_KEY, mUsername);
//			editor.putString(Preferences.PASSWORD_KEY, mPassword);
//
//			// add 存储当前用户的id
//			editor.putString(Preferences.CURRENT_USER_ID, id);
//			editor.commit();
//
//			return TaskResult.OK;
//		}
//	}

	private View.OnKeyListener enterKeyHandler = new View.OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
				if (event.getAction() == KeyEvent.ACTION_UP) {
					doLogin();
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
}