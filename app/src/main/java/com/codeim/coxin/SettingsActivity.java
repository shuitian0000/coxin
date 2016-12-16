package com.codeim.coxin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

import com.codeim.coxin.R;

public class SettingsActivity extends BaseNoDoubleClickActivity {
    private static final String TAG = "SettingActivity";
	
	/* 请求码 */
	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESULT_REQUEST_CODE = 2;
	
	private static final String IMAGE_FILE_NAME = "coxin/picture/faceImage.jpg";
	private static final String IMAGE_FILE_Dir = "/coxin/picture";
	private static final String FILE_EXTENSION_JPEG = ".jpg";
	
	private String[] items = new String[] { "相册", /* "拍照", */ "取消" };
	
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
	private RelativeLayout uploadAvatar;
    private RelativeLayout aboutContent;	
	private Button logoutBtn;
	private ProgressDialog dialog;  
	
	protected TaskManager taskManager = new TaskManager();
	private GenericTask mProfileInfoTask;  
	private GenericTask mSendAvatarTask;
	
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
	
	private TaskListener mSendAvatarTaskListener = new TaskAdapter() {
		@Override
		public void onPreExecute(GenericTask task) {
			onSendAvatarBegin();
		}
		
		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			//endTime = System.currentTimeMillis();
			//Log.d("LDS", "Sended a status in " + (endTime - startTime));
			Log.d("LDS","post sendAvatar");

			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				onSendAvatarSuccess();
			} else if (result == TaskResult.IO_ERROR) {
				onSendAvatarFailure();
			}
		}
		
		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return "SendAvatarTask";
		}
	};
	
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "OnCreate start");

		if (super._onCreate(savedInstanceState)) {
			setContentView(R.layout.newsettings);
			mNavBar = new NavBar(NavBar.HEADER_STYLE_TITLE, this);
			mNavBar.setHeaderTitle("设置");
			
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
		uploadAvatar = (RelativeLayout) findViewById(R.id.upload_avatar_layout);
		aboutContent = (RelativeLayout) findViewById(R.id.about_layout);
		logoutBtn = (Button) findViewById(R.id.logout_btn);
		
		uploadAvatar.setOnClickListener(uploadAvatarListener);
		aboutContent.setOnClickListener(aboutContentListener);
		logoutBtn.setOnClickListener(logoutListener);
	}
	
	private void draw() {
		Log.d(TAG, "draw");
		// bindControl();
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
	
	View.OnClickListener uploadAvatarListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
	        showChangeAvatarDialog();
		}
	};
	
	View.OnClickListener aboutContentListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
			startActivity(intent);
			// finish();
		}
	};
	
	View.OnClickListener logoutListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putString(Preferences.USERNAME_KEY, "");
			editor.putString(Preferences.PASSWORD_KEY, "");
			editor.putString(Preferences.CURRENT_USER_ID, "");
			editor.commit();
			Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
			startActivity(intent);
			finish();
		}
	};
	
	private ImageLoaderCallback callback = new ImageLoaderCallback() {
		@Override
		public void refresh(String url, Bitmap bitmap) {
			profileImageView.setImageBitmap(bitmap);
		}
	};
	
	private void showChangeAvatarDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this).setItems(items, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch (which) {
				case 0:
					Intent intentFromGallery = new Intent();
					intentFromGallery.setType("image/*"); // 设置文件类型
					intentFromGallery.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(intentFromGallery, IMAGE_REQUEST_CODE);
					break;
				/*
				case 1:
					Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					// 判断存储卡是否可以用，可用进行存储
					if (hasSdcard()) {
						intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(Environment
								.getExternalStorageDirectory(), IMAGE_FILE_NAME)));
					}
					startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);
					break;
				*/
				case 1:
					dialog.dismiss();
					break;
				}
			}
		}).create();
		Window dialogWindow = dialog.getWindow();
		dialogWindow.setGravity(Gravity.BOTTOM);
		dialogWindow.setWindowAnimations(R.style.dialog_style);  // 添加动画
		dialog.show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//结果码不等于取消时候
		if (resultCode != RESULT_CANCELED) {

			switch (requestCode) {
			case IMAGE_REQUEST_CODE:
				startPhotoZoom(data.getData());
				break;
			case CAMERA_REQUEST_CODE:
				if (hasSdcard()) {
					File tempFile = new File(Environment.getExternalStorageDirectory() + IMAGE_FILE_NAME);
					startPhotoZoom(Uri.fromFile(tempFile));
				} else {
					Toast.makeText(SettingsActivity.this, "未找到存储卡，无法存储照片！", Toast.LENGTH_LONG).show();
				}
				break;
			case RESULT_REQUEST_CODE:
				if (data != null) {
					getImageToView(data);
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// 设置裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 320);
		intent.putExtra("outputY", 320);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 2);
	}
	
	/**
	 * 保存裁剪之后的图片数据
	 * 
	 * @param picdata
	 */
	private void getImageToView(Intent data) {
		Bundle extras = data.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			File imageDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + IMAGE_FILE_Dir);
			if (!imageDir.exists()) {
			    imageDir.mkdirs();
			}
			//String imageName = profileInfo.screenName + "_" + System.currentTimeMillis() + FILE_EXTENSION_JPEG;
			String imageName = "_" + System.currentTimeMillis() + FILE_EXTENSION_JPEG;
			File imageFile = new File(imageDir, imageName);
			if (!imageFile.exists()) {
				try {
					imageFile.createNewFile();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			saveImageToFile(photo, imageFile);
			Drawable drawable = new BitmapDrawable(photo);
			profileImageView.setImageDrawable(drawable);
			mAvatarFile = imageFile;
			doSendAvatar();
		}
	}
	
	private void saveImageToFile(Bitmap bitmap, File imageFile) {
		FileOutputStream fileOutputStream = null;
		try {
		    fileOutputStream = new FileOutputStream(imageFile);
		} catch (IOException e) {
		    e.printStackTrace();
		}
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
		try {
		    fileOutputStream.flush();
		} catch (IOException e) {
		    e.printStackTrace();
		}
		try {
		    fileOutputStream.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public boolean hasSdcard(){
		String state = Environment.getExternalStorageState();
		if(state.equals(Environment.MEDIA_MOUNTED)){
			return true;
		}else{
			return false;
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
	
	public void doSendAvatar() {
		if (mSendAvatarTask != null && mSendAvatarTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			if (mAvatarFile.exists()) {
				mSendAvatarTask = new SendAvatarTask();
				mSendAvatarTask.setListener(mSendAvatarTaskListener);
				mSendAvatarTask.execute();
			} else {
				return;
			}
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
			    com.codeim.coxin.fanfou.User userInfo = getApi().showUser(myself);
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
	
	private class SendAvatarTask extends GenericTask {
		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			try {
				if (mAvatarFile != null) {
					getApi().updateAvatar(mAvatarFile);
				} else {
					Log.e(TAG, "Cann't update avatar without PHOTO.");
				}
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);

				if (e.getStatusCode() == HttpClient.NOT_AUTHORIZED) {
					return TaskResult.AUTH_ERROR;
				}
				return TaskResult.IO_ERROR;
			}
			return TaskResult.OK;
		}
	}
	
	private void onSendAvatarBegin() {
		dialog = ProgressDialog.show(SettingsActivity.this, "", getString(R.string.page_status_updating), true);
		if (dialog != null) {
			dialog.setCancelable(false);
		}
	}
	
	private void onSendAvatarSuccess() {
		Log.d("LDS","onSendAvatarSuccess");
		if (dialog != null) {
			dialog.setMessage(getString(R.string.page_status_update_success));
			dialog.dismiss();
		}
	}
	
	private void onSendAvatarFailure() {
		Log.d("LDS","onSendAvatarFailure");
		if (dialog != null){
			dialog.setMessage(getString(R.string.page_status_unable_to_update));
			dialog.dismiss();
		}
	}
}