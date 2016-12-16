package com.codeim.weixin.task;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.data.Friend;
import com.codeim.coxin.data.Info;
import com.codeim.coxin.data.Tweet;
import com.codeim.coxin.data.User;
import com.codeim.coxin.db.StatusTable;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.view.InfoWheelAddTimeDialog;
import com.codeim.coxin.view.InfoWheelSetTimeDialog;
import com.codeim.floorview.CommentPinnedSectionActivity;
import com.codeim.coxin.NearbyActivity;
import com.codeim.coxin.R;
import com.codeim.coxin.R.color;

public class WeixinCommonTask {

	public static class DeleteFriendTask extends GenericTask {
		public static final String TAG = "DeleteTask";

//		private BaseNoDoubleClickActivity activity;
//
//		public DeleteTask(BaseNoDoubleClickActivity activity) {
//			this.activity = activity;
//		}

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			try {
				String deleteId = param.getString("serverId");
				String userId = param.getString("userId");
//				com.codeim.coxin.fanfou.Status status = null;

//				status = activity.getApi().destroyStatus(id);
//				status = activity.getApi().deleteOneInfo(id);
				JSONObject jsonData = TwitterApplication.mApi.deleteOneFriend(deleteId,userId);
				
				int afterDeleteId;
				try {
					afterDeleteId = jsonData.getInt("deleteId");
				} catch (JSONException e) {
	                Log.e(TAG, e.getMessage(), e);
//	                _errorMsg = e.getMessage();
	                return TaskResult.IO_ERROR;
	            }

                if(afterDeleteId<0) {
                	return TaskResult.IO_ERROR;
                }

				// 对所有相关表的对应消息都进行删除（如果存在的话）
				TwitterApplication.mDb.deleteFriend(deleteId);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			return TaskResult.OK;
		}

	}
	
	/**
	 * 获取是否为朋友的关系，决定显示的按钮的不同
	 * 
	 * @author Dino
	 * 
	 */
	public static class GetRelationshipTask extends GenericTask {
		public static final String TAG = "GetRelationshipTask";

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			Log.v(TAG, "get relationship task");
			
			TaskParams param = params[0];
			try {
				String ownerId = param.getString("ownerId");
				String otherId = param.getString("otherId");
				
				if(TwitterApplication.mDb.isFriend(ownerId, otherId)) {//local database exist the friend relationship
					return TaskResult.YES;
				}
				
			    com.codeim.coxin.fanfou.Friend userFriend = TwitterApplication.mApi.isFriend(ownerId, otherId);
			    Log.v(TAG, "get user relationship");
				if (userFriend != null) {
					//Log.v(TAG, "before create user");
					com.codeim.coxin.data.Friend friend = Friend.create(userFriend);
				    //Log.v(TAG, "create user already");
					if(friend.id.equals("-1")) {
						return TaskResult.NO;
					} else {
						return TaskResult.YES;
					}
				} else {
					Log.e(TAG, "userInfo is null");
					return TaskResult.NO;
				}
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage());
				return TaskResult.FAILED;
			}
			
			//return TaskResult.NO;
		}
	}	
	
	
	/**
	 * 创建朋友关系，首先去服务器，然后更新本地
	 * 
	 * @author Dino
	 * 
	 */
	public static class CreateRelationshipTask extends GenericTask {
		public static final String TAG = "CreateRelationshipTask";

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			Log.v(TAG, "Create relationship task");
			
			TaskParams param = params[0];
			try {
				String ownerId = param.getString("ownerId");
				String otherId = param.getString("otherId");
				
//				if(TwitterApplication.mDb.isFriend(ownerId, otherId)) {//local database exist the friend relationship
//					return TaskResult.YES;
//				}
				
			    com.codeim.coxin.fanfou.Friend userFriend = TwitterApplication.mApi.createFriend(ownerId, otherId);
			    Log.v(TAG, "create user relationship");
				if (userFriend != null) {
					//Log.v(TAG, "before create user");
					com.codeim.coxin.data.Friend friend = Friend.createWithoutURL(userFriend);
				    //Log.v(TAG, "create user already");
					if(friend.id.equals("-1")) {
						return TaskResult.NO;
					} else {
						TwitterApplication.mDb.createFriend(friend);
						return TaskResult.YES;
					}
				} else {
					Log.e(TAG, "userInfo is null");
					return TaskResult.NO;
				}
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage());
				return TaskResult.FAILED;
			}
			
			//return TaskResult.NO;
		}
	}	
}
