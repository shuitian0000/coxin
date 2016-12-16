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

/**
 * AbstractTwitterListBaseLine用于抽象tweets List的展现
 * UI基本元素要求：一个ListView用于tweet列表，一个ProgressText用于提示信息
 */
package com.codeim.coxin.ui.base;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//import com.codeim.youliao.DmActivity;
//import com.codeim.youliao.MentionActivity;
//import com.codeim.youliao.ProfileActivity;
import com.codeim.coxin.R;
import com.codeim.coxin.AboutActivity;
import com.codeim.coxin.LoginActivity;
import com.codeim.coxin.SettingsActivity;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.data.Info;
import com.codeim.coxin.data.Tweet;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.NavBar;
import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
import com.codeim.floorview.CommentActivity;
//import com.codeim.coxin.StatusActivity;
//import com.codeim.coxin.StatusWithCommentActivity;
//import com.codeim.coxin.TwitterActivity;
//import com.codeim.coxin.WriteActivity;
//import com.codeim.coxin.WriteDmActivity;
//import com.codeim.coxin.data.User;
//import com.codeim.coxin.task.TweetCommonTask;

public abstract class NearbyListBaseActivity extends BaseNoDoubleClickActivity implements Refreshable {
	static final String TAG = "NearbyListBaseActivity";

	protected Feedback mFeedback;
	protected NavBar mNavBar;

	protected static final int STATE_ALL = 0;
	protected static final String SIS_RUNNING_KEY = "running";

	// Tasks.
	protected GenericTask mFavTask;
	private TaskListener mFavTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "FavoriteTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				onFavSuccess();
			} else if (result == TaskResult.IO_ERROR) {
				onFavFailure();
			}
		}
	};

	static final int DIALOG_WRITE_ID = 0;

	abstract protected int getLayoutId();

	abstract protected ListView getTweetList();

	abstract protected TweetAdapter getTweetAdapter();  // NearbyUserArrayAdapter

	abstract protected void setupState();

	abstract protected String getActivityTitle();

	// abstract protected boolean useBasicMenu();

	abstract protected Info getContextItemTweet(int position);

	abstract protected void updateTweet(Tweet tweet);

	// public static final int CONTEXT_REPLY_ID = Menu.FIRST + 1;
	// public static final int CONTEXT_AT_ID = Menu.FIRST + 2;
	// public static final int CONTEXT_RETWEET_ID = Menu.FIRST + 3;
	// public static final int CONTEXT_DM_ID = Menu.FIRST + 4;
	// public static final int CONTEXT_MORE_ID = Menu.FIRST + 5;
	// public static final int CONTEXT_ADD_FAV_ID = Menu.FIRST + 6;
	// public static final int CONTEXT_DEL_FAV_ID = Menu.FIRST + 7;

	/**
	 * 如果增加了Context Menu常量的数量，则必须重载此方法， 以保证其他人使用常量时不产生重复
	 * 
	 * @return 最大的Context Menu常量
	 */
	/*
	protected int getLastContextMenuId() {
		return CONTEXT_DEL_FAV_ID;
	}
	*/

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (super._onCreate(savedInstanceState)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			
//			getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);  
//			   setContentView(R.layout.main); 
			
			setContentView(getLayoutId());
			mNavBar = new NavBar(NavBar.HEADER_STYLE_HOME, this);
			mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);
			mPreferences.getInt(Preferences.TWITTER_ACTIVITY_STATE_KEY, STATE_ALL);
			setupState();
			registerForContextMenu(getTweetList());
			registerOnClickListener(getTweetList());
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		checkIsLogedIn();

		if(Build.VERSION.SDK_INT >= 23) {//above 6.0, need dynamic permissions
			String[] permissions = //{Manifest.permission.WRITE_EXTERNAL_STORAGE,
					                //{Manifest.permission.READ_PHONE_STATE};
					                {Manifest.permission.ACCESS_FINE_LOCATION};
			                        //Manifest.permission.CAMERA};
			// Should we show an explanation?
			if(checkSelfPermission(permissions[0])!= PackageManager.PERMISSION_GRANTED) {
				if(shouldShowRequestPermissionRationale(permissions[0])) {
					// Show an explanation to the user *asynchronously* -- don't block
					// this thread waiting for the user's response! After the user
					// sees the explanation, try again to request the permission.
					Toast.makeText(this, "Please grant the permission this time", Toast.LENGTH_LONG).show();
				}
				requestPermissions(permissions, 130);
			}


			//for WRITE_SETTING
			//requestWriteSettings();
			//requestAlertWindowPermission();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch(requestCode) {
			case 130: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 //the length is equal to number of permission
						&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					Log.i("Granted", "onRequestPermissionsResult granted");
				} else {

					// permission denied, boo! Disable the
					// functionality that depends on this permission.
				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	//for WRITE_SETTING
	protected static final int REQUEST_CODE_WRITE_SETTINGS = 128;
	protected static final int REQUEST_CODE_OVERLAY = 129;
	protected void requestWriteSettings() {
		Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
		intent.setData(Uri.parse("package:" + getPackageName()));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivityForResult(intent, REQUEST_CODE_WRITE_SETTINGS );
	}
	protected void requestAlertWindowPermission() {
		Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
		intent.setData(Uri.parse("package:" + getPackageName()));
		startActivityForResult(intent, REQUEST_CODE_OVERLAY);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_WRITE_SETTINGS) {
			if(Build.VERSION.SDK_INT >= 23) {//above 6.0, need dynamic permissions
				if (Settings.System.canWrite(this)) {
					Log.i("Granted", "onActivityResult write settings granted");
				}
			}
		}
		else if (requestCode == REQUEST_CODE_OVERLAY) {
			if(Build.VERSION.SDK_INT >= 23) {//above 6.0, need dynamic permissions
				if (Settings.canDrawOverlays(this)) {
					Log.i("Granted", "onActivityResult granted");
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mFavTask != null && mFavTask.getStatus() == GenericTask.Status.RUNNING) {
			mFavTask.cancel(true);
		}
	}

	/*
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (useBasicMenu()) {
			AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			Tweet tweet = getContextItemTweet(info.position);

			if (tweet == null) {
				Log.w(TAG, "Selected item not available.");
				return;
			}

			menu.add(0, CONTEXT_MORE_ID, 0, tweet.screenName + getResources().getString(R.string.cmenu_user_profile_prefix));
			menu.add(0, CONTEXT_REPLY_ID, 0, R.string.cmenu_reply);
			menu.add(0, CONTEXT_RETWEET_ID, 0, R.string.cmenu_retweet);
			menu.add(0, CONTEXT_DM_ID, 0, R.string.cmenu_direct_message);

			if (tweet.favorited.equals("true")) {
				menu.add(0, CONTEXT_DEL_FAV_ID, 0, R.string.cmenu_del_fav);
			} else {
				menu.add(0, CONTEXT_ADD_FAV_ID, 0, R.string.cmenu_add_fav);
			}
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Tweet tweet = getContextItemTweet(info.position);

		if (tweet == null) {
			Log.w(TAG, "Selected item not available.");
			return super.onContextItemSelected(item);
		}

		switch (item.getItemId()) {
		case CONTEXT_MORE_ID:
			launchActivity(ProfileActivity.createIntent(tweet.userId));
			return true;
		case CONTEXT_REPLY_ID: {
			// TODO: this isn't quite perfect. It leaves extra empty spaces if
			// you perform the reply action again.
			Intent intent = WriteActivity.createNewReplyIntent(tweet.text, tweet.screenName, tweet.id);
			startActivity(intent);
			return true;
		}
		case CONTEXT_RETWEET_ID:
			Intent intent = WriteActivity.createNewRepostIntent(this, tweet.text, tweet.screenName, tweet.id);
			startActivity(intent);
			return true;
		case CONTEXT_DM_ID:
			launchActivity(WriteDmActivity.createIntent(tweet.userId));
			return true;
		case CONTEXT_ADD_FAV_ID:
			doFavorite("add", tweet.id);
			return true;
		case CONTEXT_DEL_FAV_ID:
			doFavorite("del", tweet.id);
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}
	*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case OPTIONS_MENU_ID_TWEETS:
//			launchActivity(TwitterActivity.createIntent(this));
			return true;
		case OPTIONS_MENU_ID_REPLIES:
//			launchActivity(MentionActivity.createIntent(this));
			return true;
		case OPTIONS_MENU_ID_DM:
//			launchActivity(DmActivity.createIntent());
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/*
	protected void draw() {
		getTweetAdapter().refresh();
	}

	protected void goTop() {
		getTweetList().setSelection(1);
	}
	*/

	protected void adapterRefresh() {
		getTweetAdapter().refresh();
	}

	// for HasFavorite interface

//	public void doFavorite(String action, String id) {
//		if (!TextUtils.isEmpty(id)) {
//			if (mFavTask != null && mFavTask.getStatus() == GenericTask.Status.RUNNING) {
//				return;
//			} else {
//				mFavTask = new TweetCommonTask.FavoriteTask(this);
//				mFavTask.setListener(mFavTaskListener);
//
//				TaskParams params = new TaskParams();
//				params.put("action", action);
//				params.put("id", id);
//				mFavTask.execute(params);
//			}
//		}
//	}

	public void onFavSuccess() {
		// updateProgress(getString(R.string.refreshing));
		adapterRefresh();
	}

	public void onFavFailure() {
		// updateProgress(getString(R.string.refreshing));
	}

	protected void specialItemClicked(int position) {
	}

	protected void registerOnClickListener(ListView listView) {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Info info = getContextItemTweet(position);

				if (info == null) {
					Log.w(TAG, "Selected item not available.");
					specialItemClicked(position);
				} else {
					Tweet tweet;
					Log.d(TAG,String.valueOf(position));

					
					// Log.d(TAG, "User: " + user.statusInReplyToStatusId + " " + user.statusInReplyToUserId + " " + user.statusInReplyToScreenName);
					// Log.d(TAG, "user attachmentUrl " + user.attachmentUrl);
//				    tweet = User.userSwitchToTweet(user);
					// Log.d(TAG, "tweet: " + tweet.toString());
//				    launchActivity(StatusWithCommentActivity.createIntent(tweet));
					// launchActivity(StatusActivity.createIntent(user));
				}
			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mFavTask != null && mFavTask.getStatus() == GenericTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
		if(getTweetList() != null) {
			int lastPosition = getTweetList().getFirstVisiblePosition();
			outState.putInt("LAST_POSITION", lastPosition);
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		if(getTweetList() != null) {
			int lastPosition = savedInstanceState.getInt("LAST_POSITION");
			getTweetList().setSelection(lastPosition);
		}
	}

	@Override
	public void doRetrieve() {
		// TODO Auto-generated method stub
	}
}