package com.codeim.coxin.task;

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
import com.codeim.coxin.data.Info;
import com.codeim.coxin.data.Tweet;
import com.codeim.coxin.db.StatusTable;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.view.InfoWheelAddTimeDialog;
import com.codeim.coxin.view.InfoWheelSetTimeDialog;
import com.codeim.floorview.CommentPinnedSectionActivity;
import com.codeim.coxin.NearbyActivity;
import com.codeim.coxin.R;
import com.codeim.coxin.R.color;

public class CommonTask {
//	public static class DeleteTask extends GenericTask {
//		public static final String TAG = "DeleteTask";
//
//		private BaseNoDoubleClickActivity activity;
//
//		public DeleteTask(BaseNoDoubleClickActivity activity) {
//			this.activity = activity;
//		}
//
//		@Override
//		protected TaskResult _doInBackground(TaskParams... params) {
//			TaskParams param = params[0];
//			try {
//				String id = param.getString("id");
//				com.codeim.coxin.fanfou.Status status = null;
//
//				status = activity.getApi().destroyStatus(id);
//
//				// 对所有相关表的对应消息都进行删除（如果存在的话）
//				activity.getDb().deleteTweet(status.getId(), "", -1);
//			} catch (HttpException e) {
//				Log.e(TAG, e.getMessage(), e);
//				return TaskResult.IO_ERROR;
//			}
//
//			return TaskResult.OK;
//
//		}
//
//	}
	public static class DeleteTask extends GenericTask {
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
				String deleteId = param.getString("id");
//				com.codeim.coxin.fanfou.Status status = null;

//				status = activity.getApi().destroyStatus(id);
//				status = activity.getApi().deleteOneInfo(id);
				JSONObject jsonData = TwitterApplication.mApi.deleteOneInfo(deleteId);
				
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
//				activity.getDb().deleteTweet(status.getId(), "", -1);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			return TaskResult.OK;
		}

	}
	
	public static class ReportTask extends GenericTask {
		public static final String TAG = "ReportTask";
		
//		private BaseNoDoubleClickActivity activity;
//
//		public DeleteTask(BaseNoDoubleClickActivity activity) {
//			this.activity = activity;
//		}

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			try {
				String infoId = param.getString("infoId");
				String reportContent = param.getString("report");
//				com.codeim.coxin.fanfou.Status status = null;

//				status = activity.getApi().destroyStatus(id);
//				status = activity.getApi().deleteOneInfo(id);
				JSONObject jsonData = TwitterApplication.mApi.reportInfo(infoId, reportContent);
				
				int aftetReportId;
				try {
					aftetReportId = jsonData.getInt("reportId");
				} catch (JSONException e) {
	                Log.e(TAG, e.getMessage(), e);
//	                _errorMsg = e.getMessage();
	                return TaskResult.IO_ERROR;
	            }

                if(aftetReportId<0) {
                	return TaskResult.IO_ERROR;
                }

				// 对所有相关表的对应消息都进行删除（如果存在的话）
//				activity.getDb().deleteTweet(status.getId(), "", -1);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			return TaskResult.OK;
		}

	}
	
	
	public static class GetExpireTask extends GenericTask {
		public static final String TAG = "GetExpireTask";
		public Date ExpireTime;
		
		public Date getExpireTime() {
			return ExpireTime;
		}

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			try {
				String infoId = param.getString("infoId");

				JSONObject jsonData = TwitterApplication.mApi.getInfoExpire(infoId);
				
				//int afterChangeTimeId;
				try {
					ExpireTime = DateTimeHelper.parseDateFromStr(jsonData.getString("expireTime"), "yyyy-MM-dd HH:mm:ss");
				} catch (JSONException e) {
	                Log.e(TAG, e.getMessage(), e);
	                return TaskResult.IO_ERROR;
	            }

				// 对所有相关表的对应消息都进行删除（如果存在的话）
//				activity.getDb().deleteTweet(status.getId(), "", -1);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			return TaskResult.OK;
		}

	}

//	public static class FavoriteTask extends GenericTask {
//		private static final String TAG = "FavoriteTask";
//
//		private BaseNoDoubleClickActivity activity;
//
//		public static final String TYPE_ADD = "add";
//		public static final String TYPE_DEL = "del";
//
//		private String type;
//
//		public String getType() {
//			return type;
//		}
//
//		public FavoriteTask(BaseNoDoubleClickActivity activity) {
//			this.activity = activity;
//		}
//
//		@Override
//		protected TaskResult _doInBackground(TaskParams... params) {
//			TaskParams param = params[0];
//			try {
//				String action = param.getString("action");
//				String id = param.getString("id");
//
//				com.codeim.coxin.fanfou.Status status = null;
//				if (action.equals(TYPE_ADD)) {
//					status = activity.getApi().createFavorite(id);
//					activity.getDb().setFavorited(id, "true");
//					type = TYPE_ADD;
//				} else {
//					status = activity.getApi().destroyFavorite(id);
//					activity.getDb().setFavorited(id, "false");
//					type = TYPE_DEL;
//				}
//
//				Tweet tweet = Tweet.create(status);
//
//				// if (!Utils.isEmpty(tweet.profileImageUrl)) {
//				// // Fetch image to cache.
//				// try {
//				// activity.getImageManager().put(tweet.profileImageUrl);
//				// } catch (IOException e) {
//				// Log.e(TAG, e.getMessage(), e);
//				// }
//				// }
//
//				if (action.equals(TYPE_DEL)) {
//					activity.getDb().deleteTweet(tweet.id,
//							TwitterApplication.getMyselfId(false),
//							StatusTable.TYPE_FAVORITE);
//				}
//			} catch (HttpException e) {
//				Log.e(TAG, e.getMessage(), e);
//				return TaskResult.IO_ERROR;
//			}
//
//			return TaskResult.OK;
//		}
//	}

	// public static class UserTask extends GenericTask{
	//
	// @Override
	// protected TaskResult _doInBackground(TaskParams... params) {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// }
	
	public interface ChangeTimeInterface {
		/****
		 * doChangeTime, several different from the NearbyActivity's
		 * @param id
		 * @param expireTime
		 * @param newTime
		 */
		public void doChangeTime(String id, String TAG, Date expireTime, Date newTime, long addTime);
	}
	public static void showAddTimeDialog(final Activity activity, final Info info, final ChangeTimeInterface mChangeTimeInterface) {
		final Activity mActivity;
		Date expireTime = info.expireTime;
		
		DisplayMetrics outMetrics = new DisplayMetrics(); 
		activity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		int mScreenHeight = outMetrics.heightPixels;
		int mScreenWidth = outMetrics.widthPixels;
		
		final InfoWheelAddTimeDialog myDialog = new InfoWheelAddTimeDialog((int) (mScreenWidth * 0.8), android.view.WindowManager.LayoutParams.WRAP_CONTENT,
				activity, R.layout.base_wheel_add_time_dialog, info.id, 0, 1, 60,expireTime);
		myDialog.setTitleText("设置延长的时间值");
		myDialog.show();
		myDialog.refresh();
		WindowManager.LayoutParams lp = myDialog.getWindow().getAttributes();
//		lp.width = (int) (mScreenWidth * 0.8);
		lp.width = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
		lp.gravity = Gravity.CENTER;
		myDialog.getWindow().setAttributes(lp);
		
		final TextView v= (TextView) myDialog.getWindow().findViewById(R.id.title);
		final Button positiveButton = (Button) myDialog.getWindow().findViewById(R.id.positiveButton);
		final Button negativeButton = (Button) myDialog.getWindow().findViewById(R.id.negativeButton);
		v.setText("设置延长的时间值");
		positiveButton.setText("确定");
		negativeButton.setText("取消");
		positiveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				myDialog.dismiss();
			    mChangeTimeInterface.doChangeTime(info.id, "3", info.expireTime, myDialog.getNewTime(), myDialog.getAddTime());
			}
		});
		negativeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				doDelete(info.id);
				myDialog.dismiss();
			}
		});
        myDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
//				lp.alpha = 1.0f;
//				lp.dimAmount=0.0f;
//				getWindow().setAttributes(lp);
//				getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			}
        	
        });
        
		if(Integer.valueOf(TwitterApplication.getMyselfId(true))
				== Integer.valueOf(info.owerId)) {
			myDialog.btn_toggle.setVisibility(View.VISIBLE);
		} else {
			myDialog.btn_toggle.setVisibility(View.GONE);
		}
        myDialog.btn_toggle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//showSetTimeDialog(info);
				showSetTimeDialog(activity, info, mChangeTimeInterface);
				myDialog.dismiss();
			}
        });
	}
	
	public static void showSetTimeDialog(final Activity activity, final Info info, final ChangeTimeInterface mChangeTimeInterface) {
		Date expireTime = info.expireTime;
		
		DisplayMetrics outMetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		int mScreenHeight = outMetrics.heightPixels;
		int mScreenWidth = outMetrics.widthPixels;
		
		final InfoWheelSetTimeDialog myDialog = new InfoWheelSetTimeDialog((int) (mScreenWidth * 0.8), android.view.WindowManager.LayoutParams.WRAP_CONTENT,
				activity, R.layout.base_wheel_set_time_dialog, info.id, 0, 1, 60, expireTime);
		myDialog.setTitleText("设置有效期的时间值");
		myDialog.show();
		myDialog.refresh();
		WindowManager.LayoutParams lp = myDialog.getWindow().getAttributes();
//		lp.width = (int) (mScreenWidth * 0.8);
		lp.width = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
		lp.height = (int) ((mScreenHeight-20)<640?(mScreenHeight-20):640);
//		lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
		lp.gravity = Gravity.CENTER;
		myDialog.getWindow().setAttributes(lp);
		
		final TextView v= (TextView) myDialog.getWindow().findViewById(R.id.title);
		final Button positiveButton = (Button) myDialog.getWindow().findViewById(R.id.positiveButton);
		final Button negativeButton = (Button) myDialog.getWindow().findViewById(R.id.negativeButton);
		v.setText("设置有效期的时间值");
		positiveButton.setText("确定");
		negativeButton.setText("取消");
		positiveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				myDialog.dismiss();
				mChangeTimeInterface.doChangeTime(info.id, "2", info.expireTime, myDialog.getNewTime(),0);
			}
		});
		negativeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				myDialog.dismiss();
			}
		});
        myDialog.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface dialog) {
				// TODO Auto-generated method stub
//				lp.alpha = 1.0f;
//				lp.dimAmount=0.0f;
//				getWindow().setAttributes(lp);
//				getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
			}
        	
        });
        myDialog.btn_toggle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//showSetTimeDialog(info);
				showAddTimeDialog(activity, info, mChangeTimeInterface);
				myDialog.dismiss();
			}
        });
	}
	
	/****
	 * TogglePraiseTask for NearbyInfoArray, CommentPinnedSectionActivity
	 * @author ywwang
	 * @param: id, newTime
	 */
	public static class ChangeTimeTask extends GenericTask {
		public static final String TAG = "ChangeTimeTask";
		public Date afterChangeTime;

		@Override
		protected TaskResult _doInBackground(TaskParams... params) {
			TaskParams param = params[0];
			try {
				String changeId = param.getString("id");
				String changeTag = param.getString("TAG");
				String newTime = param.getString("newTime");
				String addTime = param.getString("addTime");

				JSONObject jsonData = TwitterApplication.mApi.updateInfoExpire(changeId, changeTag, newTime, addTime);
				
				int afterChangeTimeId;
				try {
					afterChangeTimeId = jsonData.getInt("changeTimeId");
					afterChangeTime = DateTimeHelper.parseDateFromStr(jsonData.getString("afterExpire"), "yyyy-MM-dd HH:mm:ss");
				} catch (JSONException e) {
	                Log.e(TAG, e.getMessage(), e);
	                return TaskResult.IO_ERROR;
	            }

                if(afterChangeTimeId<0) {
                	return TaskResult.IO_ERROR;
                }

				// 对所有相关表的对应消息都进行删除（如果存在的话）
//				activity.getDb().deleteTweet(status.getId(), "", -1);
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			}

			return TaskResult.OK;
		}

	}
	
	
	/****
	 * TogglePraiseTask for NearbyInfoArray(NearbyArrayAdapter),  
	 *                      CommentPinnedSectionActivity(CommentArrayAdapter)
	 * @author ywwang
	 *
	 */
	public static class TogglePraiseTask extends AsyncTask<TaskParams, Object, TaskResult> {
		public static final String TAG = "TogglePraiseTask";
		
		private String info_id;
		private int now_available;
		private TextView praise_count;
		private ImageView btn_praise;
		
		private String newCount;
		private int newAvailable;
		
		public TogglePraiseTask(String info_id, int is_available, TextView praise_count, ImageView btn_praise) {
			super();
			this.info_id = info_id;
			this.now_available = is_available;
			this.praise_count = praise_count;
			this.btn_praise = btn_praise;
		}
	    
		@Override
		protected TaskResult doInBackground(TaskParams... params) {
		    TaskParams param = params[0];
			try {
				JSONObject jsonData = TwitterApplication.mApi.toogleInfoPraise(info_id, now_available);
				
				newCount = jsonData.getString("praiseCount");
				newAvailable = jsonData.getInt("returnPraise");
				
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
//			} catch (IOException e) {
//				Log.e(TAG, e.getMessage(), e);
//				return TaskResult.IO_ERROR;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return TaskResult.OK;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
//			mPositionDownloadingList.add(position);
//			mButtonSelections.set(position, 2);
//			notifyDataSetChanged();
		}
		
		@Override
		protected void onPostExecute(TaskResult result) {
			super.onPostExecute(result);
			
			//mPositionViewHolder.remove(position);
//			mPositionDownloadingList.remove(position);
			
			if (result == TaskResult.AUTH_ERROR) {
				
            } else if (result == TaskResult.OK) {
            	if(this.newAvailable==1) {
//            		final android.view.animation.Animation animation;
////            		animation=AnimationUtils.loadAnimation(mContext,R.anim.praise_1);
//            		animation=AnimationUtils.loadAnimation(mContext,R.anim.praise_2);
//            		btn_praise.startAnimation(animation);
//            		new Handler().postDelayed(new Runnable(){
//        	            public void run() {
//        	            	btn_praise.setVisibility(View.VISIBLE);
//        	            } 
//        			}, 1000);
            		
            		btn_praise.setBackgroundResource(R.drawable.fav_en_dark);
//            		btn_praise.setBackgroundColor(color.color_red);
            		this.praise_count.setTextColor(color.color_red);
            	} else {
            		btn_praise.setBackgroundResource(R.drawable.fav_un_dark);
//            		btn_praise.setBackgroundColor(color.color_transparent);
            		this.praise_count.setTextColor(color.none_color);
            	}
            	this.praise_count.setText(newCount);
            } else if (result == TaskResult.IO_ERROR) {
                
            } else {
                // do nothing
            }
		}
		
		@Override
		protected void onProgressUpdate(Object... values) {
			super.onProgressUpdate(values);
		}
	}
	
	/****
	 * get one Info from backstage by infoId
	 * @author ywwang
	 *
	 */
    public static class GetInfoByIdTask extends GenericTask {
    	public static final String TAG = "GetInfoByIdTask";
        private String _errorMsg;
        private Info u;

        public String getErrorMsg() {
            return _errorMsg;
        }
        public Info getInfo() {
            return u;
        }

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
        	TaskParams param = params[0];

            try {
            	String infoId = param.getString("infoId");
            	com.codeim.coxin.fanfou.Info mInfo = TwitterApplication.mApi.getInfoById(infoId);
            	u = Info.create(mInfo);
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            return TaskResult.OK;
        }
    }
}
