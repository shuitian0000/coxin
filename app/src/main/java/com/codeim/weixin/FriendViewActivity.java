package com.codeim.weixin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.app.LazyImageLoader.ImageLoaderCallback;
import com.codeim.coxin.data.User;
import com.codeim.coxin.http.HttpClient;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskManager;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.NavBar;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
//import com.codeim.coxin.R;

import com.codeim.coxin.NearbyActivity;
import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.weixin.task.WeixinCommonTask;

public class FriendViewActivity extends BaseNoDoubleClickActivity {
    private static final String TAG = "FriendViewActivity";
	
	/* 请求码 */
	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESULT_REQUEST_CODE = 2;
	
	private static final String IMAGE_FILE_NAME = "coxin/picture/faceImage.jpg";
	private static final String IMAGE_FILE_Dir = "/coxin/picture";
	private static final String FILE_EXTENSION_JPEG = ".jpg";
	
	private String[] items = new String[] { "相册", /* "拍照", */ "取消" };
	
	private String serverId;
	private String slaveId;
	private String myself;
	private User profileInfo;
	
	private Feedback mFeedback;
	private NavBar mNavBar;
	
	private File mAvatarFile;
	
	private ImageView profileImageView;  
	private TextView profileScreenName;  
	private TextView friendsCount;  
	private TextView followersCount;  
	// private TextView statusCount;
	private TextView topicCount;
	private TextView replyCount;
//	private RelativeLayout uploadAvatar;
//    private RelativeLayout aboutContent;	
//	private Button logoutBtn;
	private Button toSendBtn;
	private Button toDeleteBtn;
	private ProgressDialog dialog;  
	
	protected TaskManager taskManager = new TaskManager();
	private GenericTask mProfileInfoTask;
	private GenericTask mDeleteFriendTask;
	private GenericTask mGetRelationshipTask;
	private GenericTask mCreateFriendTask;
	
	
	private TaskListener getRelationshipTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.YES) {
				mFeedback.success("");
				onGetRelationship(true);
				if (dialog != null) {
					dialog.dismiss();
				}
			} else if (result == TaskResult.NO) {
				mFeedback.success("");
				onGetRelationship(false);
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		}

		@Override
		public String getName() {
			return "GetRelationship";
		}
	};
	private TaskListener profileInfoTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				mFeedback.success("");
				bindControl();
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		}

		@Override
		public String getName() {
			return "GetProfileInfo";
		}
	};
	private TaskListener deleteFriendTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.OK) {
				mFeedback.success("");
				onDeleteFriend();
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		}
		@Override
		public String getName() {
			return "GetProfileInfo";
		}
	};
	private TaskListener createFriendTaskListener = new TaskAdapter() {
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.YES) {
				mFeedback.success("");
				onCreateFriend(true);
				if (dialog != null) {
					dialog.dismiss();
				}
			} else if (result == TaskResult.NO) {
				mFeedback.success("");
				onCreateFriend(false);
				if (dialog != null) {
					dialog.dismiss();
				}
			}
		}

		@Override
		public String getName() {
			return "CreateFriend";
		}
	};
	
	
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "OnCreate start");

		if (super._onCreate(savedInstanceState)) {
			setContentView(R.layout.friend_view);
			mNavBar = new NavBar(NavBar.HEADER_STYLE_TITLE, this);
			mNavBar.setHeaderTitle("朋友");
			
			Intent intent = getIntent();
			Bundle bundle = intent.getExtras();
			serverId = bundle.getString("serverId");
			slaveId = bundle.getString("slaveId");
			
			myself = TwitterApplication.getMyselfId(false);

			// 初始化控件
			initControls();
			// db = this.getDb();
			draw();

			return true;
		} else {
			return false;
		}
	}
	
	private void initControls() {
		mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);
		profileImageView = (ImageView) findViewById(R.id.profile_image);
		profileScreenName = (TextView) findViewById(R.id.screen_name);
		friendsCount = (TextView) findViewById(R.id.following_count);   
		followersCount = (TextView) findViewById(R.id.follower_count);  
		// statusCount = (TextView) findViewById(R.id.status_count);
		topicCount = (TextView) findViewById(R.id.topic_count);
		replyCount = (TextView) findViewById(R.id.reply_count);
		
//		uploadAvatar = (RelativeLayout) findViewById(R.id.upload_avatar_layout);
//		aboutContent = (RelativeLayout) findViewById(R.id.about_layout);
//		logoutBtn = (Button) findViewById(R.id.logout_btn);
		toSendBtn = (Button) findViewById(R.id.to_send_btn);
		toDeleteBtn = (Button) findViewById(R.id.to_delete_btn);
		
//		uploadAvatar.setOnClickListener(uploadAvatarListener);
//		aboutContent.setOnClickListener(aboutContentListener);
//		logoutBtn.setOnClickListener(logoutListener);
//		toSendBtn.setOnClickListener(toSendListener);
//		toDeleteBtn.setOnClickListener(toDeleteListener);
	    toSendBtn.setVisibility(View.GONE);
	    toDeleteBtn.setVisibility(View.GONE);
	}
	
	private void draw() {
		Log.d(TAG, "draw");
		// bindControl();
		if(slaveId.equals(myself)) {
			doGetRelationship();
		} else {
			doGetRelationship();
		}
		doGetProfileInfo();
	}
	
	private void bindControl() {
		Log.d(TAG, "bindControl");
		
		if(profileInfo.profileImageUrl==null) {
			return;
		}

		profileImageView.setImageBitmap(TwitterApplication.mImageLoader.get(profileInfo.profileImageUrl.toString(), callback));
//		profileScreenName.setText(profileInfo.screenName);

//		friendsCount.setText(String.valueOf(profileInfo.friendsCount));
//		followersCount.setText(String.valueOf(profileInfo.followersCount));
//		// statusCount.setText(String.valueOf(profileInfo.statusesCount));
//		Log.d(TAG, "friends count is: " + profileInfo.friendsCount);
//		Log.d(TAG, "followers count is: " + profileInfo.followersCount);
//		Log.d(TAG, "topic count is: " + profileInfo.topicCount);
//		Log.d(TAG, "reply count is: " + profileInfo.replyCount);
//		topicCount.setText(String.valueOf(profileInfo.topicCount));
//		replyCount.setText(String.valueOf(profileInfo.replyCount));
	}
	
	private void onGetRelationship(boolean isFriend) {
		if(isFriend) {
			Log.d(TAG, "onGetRelationship: " + "exist friend relationship");
			
		    toSendBtn.setVisibility(View.VISIBLE);
		    toDeleteBtn.setVisibility(View.VISIBLE);
		
		    toSendBtn.setOnClickListener(toSendListener);
		    toDeleteBtn.setOnClickListener(toDeleteListener);
		} else {
			Log.d(TAG, "onGetRelationship: " + "doesn't exist friend relationship!");
			
		    toSendBtn.setVisibility(View.VISIBLE);
		    toDeleteBtn.setVisibility(View.VISIBLE);
		    toDeleteBtn.setText("加好友");
		
		    toSendBtn.setOnClickListener(toSendListener);
		    toDeleteBtn.setOnClickListener(toAddFriendListener);
		}
	}
	private void onDeleteFriend() {
		Toast.makeText(FriendViewActivity.this, "删除好友成功", Toast.LENGTH_SHORT).show();
		setResult(100);
	    finish();  
	}
	private void onCreateFriend(boolean finish) {
		Toast.makeText(FriendViewActivity.this, "添加好友成功", Toast.LENGTH_SHORT).show();
		setResult(100);
	    finish();
//		onGetRelationship(true);
	}
	

	View.OnClickListener toSendListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
//			Intent intent = new Intent(FriendViewActivity.this, ChatActivity.class);
//		    Bundle bundle=new Bundle();
//		    bundle.putInt("masterId", Integer.valueOf(myself));
//		    bundle.putInt("slaveId", Integer.valueOf(slaveId));
//		    intent.putExtras(bundle);
//			startActivity(intent);
//			finish();
		}
	};
	
	View.OnClickListener toDeleteListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "toDeleteListener");
//			Intent intent = new Intent(FriendViewActivity.this, ChatActivity.class);
//		    Bundle bundle=new Bundle();
//		    bundle.putInt("masterId", Integer.valueOf(myself));
//		    bundle.putInt("slaveId", Integer.valueOf(slaveId));
//		    intent.putExtras(bundle);
//			startActivity(intent);
//			finish();
			doDeleteFriend();
		}
	};
	View.OnClickListener toAddFriendListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.d(TAG, "toAddFriendListener");
//			Intent intent = new Intent(FriendViewActivity.this, ChatActivity.class);
//		    Bundle bundle=new Bundle();
//		    bundle.putInt("masterId", Integer.valueOf(myself));
//		    bundle.putInt("slaveId", Integer.valueOf(slaveId));
//		    intent.putExtras(bundle);
//			startActivity(intent);
//			finish();
			doCreateFriend();
		}
	};
	
	
	private ImageLoaderCallback callback = new ImageLoaderCallback() {
		@Override
		public void refresh(String url, Bitmap bitmap) {
			profileImageView.setImageBitmap(bitmap);
		}
	};
	
	private void doGetRelationship() {
		//mFeedback.start("");
		if (mGetRelationshipTask != null && mGetRelationshipTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mGetRelationshipTask = new WeixinCommonTask.GetRelationshipTask();
			mGetRelationshipTask.setListener(getRelationshipTaskListener);
			TaskParams params = new TaskParams();
			params.put("ownerId", myself);
			params.put("otherId", slaveId);
			mGetRelationshipTask.execute(params);
		}
	}
	private void doGetProfileInfo() {
		mFeedback.start("");
		if (mProfileInfoTask != null && mProfileInfoTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mProfileInfoTask = new GetProfileTask();
			mProfileInfoTask.setListener(profileInfoTaskListener);
			TaskParams params = new TaskParams();
			mProfileInfoTask.execute(params);
		}
	}
	private void doDeleteFriend() {
		mFeedback.start("");
		if (mDeleteFriendTask != null && mDeleteFriendTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mDeleteFriendTask = new WeixinCommonTask.DeleteFriendTask();
			mDeleteFriendTask.setListener(deleteFriendTaskListener);
			TaskParams params = new TaskParams();
			params.put("serverId", serverId);
			params.put("userId", myself);
			mDeleteFriendTask.execute(params);
		}
	}
	private void doCreateFriend() {
		mFeedback.start("");
		if (mCreateFriendTask != null && mCreateFriendTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mCreateFriendTask = new WeixinCommonTask.CreateRelationshipTask();
			mCreateFriendTask.setListener(createFriendTaskListener);
			TaskParams params = new TaskParams();
			params.put("ownerId", myself);
			params.put("otherId", slaveId);
			mCreateFriendTask.execute(params);
		}
	}
	
	/**
	 * 获取用户信息task
	 * 
	 * @author Dino
	 * 
	 */
	private class GetProfileTask extends GenericTask {

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			Log.v(TAG, "get profile task");
			try {
			    com.codeim.coxin.fanfou.User userInfo = getApi().showUser(slaveId);
			    Log.v(TAG, "get user profile");
				if (userInfo != null) {
					Log.v(TAG, "before create user");
				    profileInfo = User.create(userInfo);
				    Log.v(TAG, "create user already");
				} else {
					Log.e(TAG, "userInfo is null");
				}
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage());
				return TaskResult.FAILED;
			}
			mFeedback.update(99);
			return TaskResult.OK;
		}
	}
	
	
}