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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import com.baidu.location.BDLocation;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.data.Info;
import com.codeim.coxin.data.Tweet;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.task.CommonTask;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.task.CommonTask.ChangeTimeInterface;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.coxin.ui.base.NearbyArrayBaseActivity;
import com.codeim.coxin.ui.module.SimpleFeedback;
import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter.OnTimeSet;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.view.InfoMenuDialog;
import com.codeim.coxin.view.InfoMenuPopupwindow;
import com.codeim.coxin.view.InfoWheelAddTimeDialog;
import com.codeim.coxin.view.InfoWheelSetTimeDialog;
import com.codeim.coxin.view.MultiChoiceDialog;
import com.codeim.coxin.view.MultiChoiceDialog.OnNegativeButton;
import com.codeim.coxin.view.MultiChoiceDialog.OnPositiveButton;
import com.codeim.floorview.CommentActivity;
import com.codeim.floorview.CommentPinnedSectionActivity;
import com.codeim.floorview.CommentWriteActivity;
// import com.codeim.coxin.data.Tweet;
// import com.codeim.coxin.db.StatusTable;
// import com.codeim.coxin.fanfou.Paging;
// import com.codeim.coxin.fanfou.Status;
// import com.codeim.coxin.task.GenericTask;
// import com.codeim.coxin.task.TaskAdapter;
// import com.codeim.coxin.task.TaskListener;
// import com.codeim.coxin.task.TaskParams;
// import com.codeim.coxin.task.TaskResult;
// import com.codeim.coxin.task.TweetCommonTask;
//import com.codeim.coxin.R;
import com.codeim.coxin.R;

public class NearbyActivity extends NearbyArrayBaseActivity {
	protected static final String TAG = "NearbyActivity";

	private static final String LAUNCH_ACTION = "com.codeim.coxin.TWEETS";
	
	private static final int INFO_POP_DIALOG_SELF = 1;
	private static final int INFO_POP_DIALOG_OTHER = 2;

	protected static final ContextThemeWrapper mContext = null;
	
	private LayoutInflater inflater ;

	ArrayList<String> infoMenuOptionList = new ArrayList<String>();
	
//	private InfoMenuPopupwindow info_menu_pop_view;
	private InfoMenuDialog info_menu_alertDialog;
//	private InfoMenuAdapterForDialog mMenuOptionAdapter;
	
	
	protected GenericTask mDeleteTask;
	protected GenericTask mChangeTimeTask;
	protected GenericTask mReportTask;
	protected GenericTask mGetInfoByIdTask;
	
	private String toDeleteId;
	private String toChangeTimeId;
	private Date afterChangeTime;

	
	private TaskListener mDeleteTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "DeleteTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				onDeleteSuccess(toDeleteId);
			} else if (result == TaskResult.IO_ERROR) {
				onDeleteFailure();
			}
		}
	};
	private TaskListener mChangeTimeTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "ChangeTimeTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				if(task == mChangeTimeTask)
					afterChangeTime = ((CommonTask.ChangeTimeTask)mChangeTimeTask).afterChangeTime;
				onChangeTimeSuccess(toChangeTimeId, afterChangeTime);
			} else if (result == TaskResult.IO_ERROR) {
				onChangeTimeFailure();
			}
		}
	};
	

//	static final int DIALOG_WRITE_ID = 0;

	public static Intent createIntent(Context context) {
		Intent intent = new Intent(LAUNCH_ACTION);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		return intent;
	}

	public static Intent createNewTaskIntent(Context context) {
		Intent intent = createIntent(context);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		return intent;
	}

	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
	
	    mNameType = "nearby_";
		
		if (super._onCreate(savedInstanceState)) {
			mNavBar.setHeaderTitle("附近");
			// 仅在这个页面进行schedule的处理
			manageUpdateChecks();
			
			inflater = this.getLayoutInflater () ;
			return true;
		} else {
			return false;
		}
	}
	
	/****
	 * override onBackPressed(), not exit problem, but only hide it like homePressed
	 */
	@Override
	public void onBackPressed() {
		//super.onBackPressed();
	    Intent intent= new Intent(Intent.ACTION_MAIN); 
	    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	    intent.addCategory(Intent.CATEGORY_HOME); 
	    startActivity(intent);
	}
	
	/****
	 * real exitProprames(), can be called when want to exit problem.
	 */
	public void exitProgrames(){ 
	    Intent startMain = new Intent(Intent.ACTION_MAIN);
	    startMain.addCategory(Intent.CATEGORY_HOME);
	    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    startActivity(startMain); 
	    android.os.Process.killProcess(android.os.Process.myPid());
	    System.exit(0);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		/*
		if (mDeleteTask != null && mDeleteTask.getStatus() == GenericTask.Status.RUNNING) {
			mDeleteTask.cancel(true);
		}
		*/
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		/*
		if (mDeleteTask != null && mDeleteTask.getStatus() == GenericTask.Status.RUNNING) {
			outState.putBoolean(SIS_RUNNING_KEY, true);
		}
		*/
	}


	// private int CONTEXT_DELETE_ID = getLastContextMenuId() + 1;

	/*
	@Override
	protected int getLastContextMenuId() {
		return CONTEXT_DELETE_ID;
	}
	*/

	/*
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		Tweet tweet = getContextItemTweet(info.position);
		if (null != tweet) {// 当按钮为 刷新/更多的时候为空

			if (tweet.userId.equals(TwitterApplication.getMyselfId(false))) {
				menu.add(0, CONTEXT_DELETE_ID, 0, R.string.cmenu_delete);
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

		if (item.getItemId() == CONTEXT_DELETE_ID) {
			doDelete(tweet.id);
			return true;
		} else {
			return super.onContextItemSelected(item);
		}
	}
	*/

	/*
	@SuppressWarnings("deprecation")
	@Override
	protected Cursor fetchMessages() {
		return getDb().fetchAllTweets(getUserId(), StatusTable.TYPE_HOME);
	}
	*/

	@Override
	protected String getActivityTitle() {
		return getResources().getString(R.string.page_title_home);
	}

	/*
	@Override
	protected void markAllRead() {
		getDb().markAllTweetsRead(getUserId(), StatusTable.TYPE_HOME);
	}
	*/

	/*
	// hasRetrieveListTask interface
	@Override
	public int addMessages(ArrayList<Tweet> tweets, boolean isUnread) {
		// 获取消息的时候，将status里获取的user也存储到数据库

		// ::MARK::
		for (Tweet t : tweets) {
			getDb().createWeiboUserInfo(t.user);
		}
		return getDb().putTweets(tweets, getUserId(), StatusTable.TYPE_HOME, isUnread);
	}
	*/
	
	@Override
    public List<com.codeim.coxin.fanfou.Info> getNearbyInfo(int page_size, int page_index, int last_id, String infoType, 
	        double lat, double lng) throws HttpException {
	    return getApi().getFlyInfomsgRefreshLocation(page_size, page_index, last_id, infoType, lat, lng);
	}

	public void onDeleteFailure() {
		Log.e(TAG, "Delete failed");
	}
	public void onChangeTimeFailure() {
		Log.e(TAG, "ChangeTime failed");
	}

	public void onDeleteSuccess(String toDeleteId) {
		Toast.makeText(NearbyActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
		
		Log.e(TAG, "onDeleteSuccess"+toDeleteId);
		removeAsInfoId(toDeleteId);
		//mTweetAdapter.refresh();
		mInfoListAdapter.refresh(allInfoList);
	}
	public void onChangeTimeSuccess(String toChangeTimeId, Date afterChangeTime) {
		Toast.makeText(NearbyActivity.this, "更新有效期成功", Toast.LENGTH_SHORT).show();
		
		Log.e(TAG, "onChangeTimeSuccess"+toChangeTimeId);
		changeExpireAsInfoId(toChangeTimeId, afterChangeTime);
		//mTweetAdapter.refresh();
		mInfoListAdapter.refresh();
	}
	
	private void doDelete(String id) {

		if (mDeleteTask != null && mDeleteTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
//			mDeleteTask = new TweetCommonTask.DeleteTask(this);
			mDeleteTask = new CommonTask.DeleteTask();
			mDeleteTask.setListener(mDeleteTaskListener);

			TaskParams params = new TaskParams();
			params.put("id", id);
			mDeleteTask.execute(params);
			
			Log.e(TAG, "doDelete"+id);
			toDeleteId = id;
			
			taskManager.addTask(mDeleteTask);
		}
	}
	
	/***doChangeTime related method start***/
	ChangeTimeInterface mChangeTimeInterface = new ChangeTimeInterface() {
		@Override
	    public void doChangeTime(String id, String TAG, Date expireTime, Date newTime, long addTime) {

		    if(DateTimeHelper.getNowTime()>expireTime.getTime()) {
			    return;
		    }
		    if (mChangeTimeTask != null && mChangeTimeTask.getStatus() == GenericTask.Status.RUNNING) {
		    	return;
		    } else {
//			    mChangeTimeTask = new TweetCommonTask.DeleteTask(this);
			    mChangeTimeTask = new CommonTask.ChangeTimeTask();
			    mChangeTimeTask.setListener(mChangeTimeTaskListener);

			    TaskParams params = new TaskParams();
			    params.put("id", id);
			    params.put("TAG", TAG);
			    params.put("newTime", DateTimeHelper.dateToString(newTime, ""));
			    params.put("addTime", String.valueOf(addTime));
			    mChangeTimeTask.execute(params);
			
			    Log.e(TAG, "doChangeTime"+id);
			    toChangeTimeId = id;
			    afterChangeTime = newTime;
			
			    taskManager.addTask(mChangeTimeTask);
		    }
	    }
	};

	/*
	@Override
	public String fetchMinId() {
		return getDb().fetchMinTweetId(getUserId(), StatusTable.TYPE_HOME);
	}
	*/

	/*
	@Override
	public List<Status> getMoreMessageFromId(String minId) throws HttpException {
		Paging paging = new Paging(1, 20);
		paging.setMaxId(minId);
		return getApi().getFriendsTimeline(paging);
	}
	*/

	/*
	@Override
	public int getDatabaseType() {
		return StatusTable.TYPE_HOME;
	}
	*/

	@Override
	public String getUserId() {
		return TwitterApplication.getMyselfId(false);
	}
	
	@Override
	protected void registerOnClickListener(ListView listView) {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.e(TAG, "setOnItemClickListener position: "+String.valueOf(position));
				final Info info = getContextItemTweet(position);
//				Info info = (Info) parent.getAdapter().getItem(position);

				if (info == null) {
					Log.w(TAG, "Selected item not available.");
					specialItemClicked(position);
				} else if(info.expire==0){
					Log.d(TAG,String.valueOf(position));
					
					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_ID, info.owerId.toString() ).commit();
					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_USERNAME, info.owerName.toString() ).commit();
					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_ID, info.id.toString() ).commit();
					//Intent intent = new Intent(NearbyListBaseActivity.this, CommentActivity.class);
//					Intent intent = new Intent(NearbyActivity.this, CommentActivity.class);
					Intent intent = new Intent(NearbyActivity.this, CommentPinnedSectionActivity.class);
//				    Bundle bundle=new Bundle();
//				    bundle.putInt("userId", Integer.valueOf(info.owerId));
//				    bundle.putInt("infoId", Integer.valueOf(info.id));
//				    bundle.putParcelable("INFO", info);
//				    intent.putExtras(bundle);
////					intent.putExtra("userId", Integer.valueOf(info.owerId));
////					intent.putExtra(Intent.EXTRA_INTENT, getIntent());
					intent.putExtra("INFO", info);
					//startActivity(intent);
					startActivityForResult(intent, 200);
					
					// Log.d(TAG, "User: " + user.statusInReplyToStatusId + " " + user.statusInReplyToUserId + " " + user.statusInReplyToScreenName);
					// Log.d(TAG, "user attachmentUrl " + user.attachmentUrl);
//				    tweet = User.userSwitchToTweet(user);
					// Log.d(TAG, "tweet: " + tweet.toString());
//				    launchActivity(StatusWithCommentActivity.createIntent(tweet));
					// launchActivity(StatusActivity.createIntent(user));
				}
			}
		});
		
		//长按动作 ----  黑色长条弹出框,改用另外一种
//		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
//			@Override
//			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
//				Info info = getContextItemTweet(position);
//
//				if (info == null) {
//					Log.w(TAG, "Selected item not available.");
//					specialItemClicked(position);
//				} else {
//					Tweet tweet;
//					Log.d(TAG,String.valueOf(position));
//					
//					RelativeLayout pop_view;
//					RadioButton reply;
//					RadioButton report;
//					RadioButton dele;
// 
//					pop_view = ( RelativeLayout ) inflater.inflate(R.layout.info_pop_menu, parent, false);
//					
//					final PopupWindow pop = new PopupWindow(pop_view,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, false); 
////					pop.setBackgroundDrawable(new BitmapDrawable());
//					pop.setBackgroundDrawable(getResources().getDrawable(R.drawable.title_bar));
//					pop.setOutsideTouchable(true);
//					pop.setFocusable(true);
//					
//					reply         = (RadioButton) pop_view.findViewById(R.id.info_btn_reply);
//					report        = (RadioButton) pop_view.findViewById(R.id.info_btn_report);
//					dele         = (RadioButton) pop_view.findViewById(R.id.info_btn_delete);
//					
//					if (info == null) {
//						Log.d(TAG,String.valueOf(position));
//						Log.d(TAG,String.valueOf(id));
//						Log.w(TAG, "Selected item not available.");
//						specialItemClicked(position);
//					} else {
//						//Tweet tweet;
//						Log.d(TAG,String.valueOf(position));
//						Log.d(TAG,String.valueOf(id));
//						
//						if(pop.isShowing()) {
////						    pop.dismiss();
//						} else {
//							//only the info belongs to the user, the user can be delete the info
//							if(Integer.valueOf(TwitterApplication.getMyselfId(true))==Integer.valueOf(info.owerId)) {
//								reply.setVisibility(View.VISIBLE);
//								report.setVisibility(View.VISIBLE);
//								dele.setVisibility(View.VISIBLE);
//							} else {
//								reply.setVisibility(View.VISIBLE);
//								report.setVisibility(View.VISIBLE);
//								dele.setVisibility(View.GONE);
//							}
//							
//				            int[] arrayOfInt = new int[2];
//				            //获取点击按钮的坐标
////				            view.getLocationInWindow(arrayOfInt);
//				            view.getLocationOnScreen(arrayOfInt);
//				            int x = arrayOfInt[0];
//				            int y = arrayOfInt[1];
//				            
//				            DisplayMetrics  dm = new DisplayMetrics();
//				            getWindowManager().getDefaultDisplay().getMetrics(dm);
//				            int screenWidth = dm.widthPixels;
//				            int screenHeight = dm.heightPixels;
//
//				            pop_view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
////							pop.showAtLocation(parent, Gravity.CENTER_VERTICAL, 100, 100);
////							pop.showAtLocation(view, 0 , screenWidth/2-pop.getWidth()/2, y+pop.getHeight());	
//				            pop.showAtLocation(view, 0 , screenWidth/2-150, y+50);
////							pop.showAsDropDown(view, 0, 0);
//							pop.update(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//						}
//					}
//				}
//				
//				return true; //no call onItemClick
//			}
//		});
		
		//长按动作 ----  dialog 或者  popupwindow
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				final Info info = getContextItemTweet(position);

				if (info == null) {
					Log.w(TAG, "Selected item not available.");
					specialItemClicked(position);
				} else {
					Log.d(TAG,String.valueOf(position));
					
					makePopDialog(info);
				}
				
				return true; //no call onItemClick
			}
		});
		
		mInfoListAdapter.setOnClickDownArrow(new com.codeim.coxin.ui.module.NearbyInfoArrayAdapter.OnClickDownArrow() {
			@Override
			public void downArrowPop(int position) {
//				final Info info = getContextItemTweet(position);
				final Info info = getListItem(position);

				if (info == null) {
					Log.w(TAG, "Selected item not available.");
					specialItemClicked(position);
				} else {
					Log.d(TAG,String.valueOf(position));
					
					makePopDialog(info);
				}
				
			}
		});
		
		mInfoListAdapter.setOnTimeSet(new OnTimeSet() {
			@Override
			public void clickTimeSet(int position) {
				// TODO Auto-generated method stub
//				final Info info = getContextItemTweet(position);
				final Info info = getListItem(position);
				
				//showAddTimeDialog(info);
				CommonTask.showAddTimeDialog(NearbyActivity.this, info, mChangeTimeInterface);
            
			}
		});
		
//		mInfoListAdapter.setOnPopupWindowStateChange(new com.codeim.coxin.ui.module.NearbyInfoArrayAdapter.OnPopupWindowStateChange() {
//			
//			@SuppressWarnings("deprecation")
//			@Override
//			public void popupWindowStateChange(boolean state) {
//				// TODO Auto-generated method stub
//				
//				WindowManager.LayoutParams lp = getWindow().getAttributes();
//				if(state) {
//					lp.alpha = 0.3f;
//					lp.dimAmount=0.5f;
//				} else {
//					lp.alpha = 1.0f;
//					lp.dimAmount=0.0f;
//				}
//				getWindow().setAttributes(lp);
//				getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
////				getWindow().addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
//			}
//		});
	}

	protected void makePopDialog(final Info info) {
		DisplayMetrics outMetrics = new DisplayMetrics(); 
		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
		int mScreenHeight = outMetrics.heightPixels;
		int mScreenWidth = outMetrics.widthPixels;
		
		infoMenuOptionList.clear();
		infoMenuOptionList.add("回复");
		
		if(Integer.valueOf(TwitterApplication.getMyselfId(true))
				== Integer.valueOf(info.owerId)) {
		    infoMenuOptionList.add("删除");
		} else {
			infoMenuOptionList.add("举报");
		}
		
//        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View mView = inflater.inflate(R.layout.info_pop_menu_view, null);
		
        info_menu_alertDialog = new InfoMenuDialog((int) (mScreenWidth * 0.7), android.view.WindowManager.LayoutParams.WRAP_CONTENT,
        		infoMenuOptionList, NearbyActivity.this, R.layout.info_pop_menu_view);
		info_menu_alertDialog.show();
		info_menu_alertDialog.refresh();
		WindowManager.LayoutParams lp = info_menu_alertDialog.getWindow().getAttributes();
		lp.width = (int) (mScreenWidth * 0.7);
		lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
		lp.gravity = Gravity.CENTER;
		info_menu_alertDialog.getWindow().setAttributes(lp);
		
		info_menu_alertDialog.setOnOptionSelected(new com.codeim.coxin.view.InfoMenuDialog.OnOptionSelected() {
			@Override
			public void selected(int position, String option) { //写评论页面，另外一个Activity
				if(option.contains("回复")) {
					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_ID, info.owerId.toString() ).commit();
					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_USERNAME, info.owerName.toString() ).commit();
					TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_ID, info.id.toString() ).commit();
					
					Intent intent = new Intent(NearbyActivity.this, CommentWriteActivity.class);
					Bundle bundle=new Bundle();
					intent.putExtra("parentid", 0);
					intent.putExtra("floornum", 0);
					intent.putExtras(bundle);
					startActivityForResult(intent, 100);
					
					info_menu_alertDialog.dismiss();
				} else if(option.contains("删除")) { //删除页面，回弹出一个是否删除的dialog,采用的是setContentView自定义方式
					LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(R.layout.base_dialog, null);
//					final WindowManager.LayoutParams lp = getWindow().getAttributes();
					
					AlertDialog.Builder builder = new AlertDialog.Builder(NearbyActivity.this, R.style.DialogStyle);
					final AlertDialog myDialog = builder.create();
					myDialog.show();
					myDialog.getWindow().setContentView(R.layout.base_dialog);
//					lp.alpha = 0.3f;
//					lp.dimAmount=0.5f;
//					getWindow().setAttributes(lp);
//					getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
					
					final TextView v= (TextView) myDialog.getWindow().findViewById(R.id.title);
					final Button positiveButton = (Button) myDialog.getWindow().findViewById(R.id.positiveButton);
					final Button negativeButton = (Button) myDialog.getWindow().findViewById(R.id.negativeButton);
					v.setText("你确定删除吗");
					positiveButton.setText("确定");
					negativeButton.setText("取消");
					positiveButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							myDialog.dismiss();
							doDelete(info.id);
						}
					});
					negativeButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
//							doDelete(info.id);
							myDialog.dismiss();
						}
					});
                    myDialog.setOnDismissListener(new OnDismissListener() {

						@Override
						public void onDismiss(DialogInterface dialog) {
							// TODO Auto-generated method stub
//							lp.alpha = 1.0f;
//							lp.dimAmount=0.0f;
//							getWindow().setAttributes(lp);
//							getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
						}
                    	
                    });
					
					info_menu_alertDialog.dismiss();
				} else if(option.contains("举报")) { //举报页面，回弹出举报类型以及举报确认的dialog,采用的是自定义view的方式，传入的是layout

					DisplayMetrics outMetrics = new DisplayMetrics(); 
					getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
					int mScreenHeight = outMetrics.heightPixels;
					int mScreenWidth = outMetrics.widthPixels;
					
					ArrayList<String> arrayReportList = new ArrayList<String>();
					arrayReportList.clear();
					arrayReportList.add("垃圾营销");
					arrayReportList.add("违反法律");
					arrayReportList.add("三俗暴力");
					arrayReportList.add("不实信息");
					arrayReportList.add("敏感信息");
					arrayReportList.add("不实信息");
					arrayReportList.add("不实信息");
					arrayReportList.add("不实信息");
					arrayReportList.add("不实信息");
					arrayReportList.add("不实信息");
					arrayReportList.add("其他");
					
					//1.aleartDialog method
//					final String ReportList[]={"垃圾营销","违反法律","三俗暴力","不实信息","敏感信息","不实信息",
//							"不实信息","不实信息","不实信息","不实信息","其他"};  
//			        final boolean selected[]={false,false,false,false,false,false,false,false,false,false,false};  
//					final AlertDialog.Builder myDialogBuilder = new AlertDialog.Builder(NearbyActivity.this);
//					myDialogBuilder.setTitle("选择举报类型");
//			        myDialogBuilder.setMultiChoiceItems(ReportList,selected,new DialogInterface.OnMultiChoiceClickListener() {  
//			            @Override  
//			            public void onClick(DialogInterface dialog, int which, boolean isChecked) {  
//			               // dialog.dismiss(); 
//			                String select_item = ReportList[which].toString();
//	                        Toast.makeText(NearbyActivity.this,
//	                                 "选择了--->>" + select_item, Toast.LENGTH_SHORT)
//	                                 .show();
//			            }  
//			        }); 
//			        myDialogBuilder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							// TODO Auto-generated method stub
//							String re="";
//							for(int i=0; i<6;i++) {
//								if(selected[i]) {
//									re=re+ReportList[i];
//								}
//							}
//							doReport(info.id, re);
//							dialog.dismiss();
//						}
//					});
//			        myDialogBuilder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int which) {
//							// TODO Auto-generated method stub
//							dialog.dismiss();
//						}
//					});
//			        myDialogBuilder.create().show();
					
					//2.custom Dialog method
					final MultiChoiceDialog myDialog = new MultiChoiceDialog((int) (mScreenWidth * 0.7), android.view.WindowManager.LayoutParams.WRAP_CONTENT,
							arrayReportList, NearbyActivity.this, R.layout.base_dialog);
					myDialog.setTitleText("选择举报类型");
					myDialog.show();
					myDialog.refresh();
					WindowManager.LayoutParams lp = myDialog.getWindow().getAttributes();
					lp.width = (int) (mScreenWidth * 0.7);
//					lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
					lp.height = (int) ((mScreenHeight*0.8)<650?(mScreenHeight*0.8):650);
					lp.gravity = Gravity.CENTER;
					myDialog.getWindow().setAttributes(lp);
					
					myDialog.setOnPositiveButton(new OnPositiveButton() {
						@Override
						public void positiveButtonselected() {
							// TODO Auto-generated method stub
							doReport(info.id, myDialog.getAllSelected());
							myDialog.dismiss();
						}
					});
					myDialog.setOnNegativeButton(new OnNegativeButton() {
						@Override
						public void negativeButtonselected() {
							// TODO Auto-generated method stub
							myDialog.dismiss();
						}
					});

					
					info_menu_alertDialog.dismiss();
				}
			}
		});
	}
	
	
	public void doReport(String info_id, String reportContent) {
		if (mReportTask != null && mReportTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
//			mDeleteTask = new TweetCommonTask.DeleteTask(this);
			mReportTask = new CommonTask.ReportTask();
			mReportTask.setListener(mReportTaskListener);

			TaskParams params = new TaskParams();
			params.put("infoId", info_id);
			params.put("report", reportContent);
			mReportTask.execute(params);
			
			taskManager.addTask(mReportTask);
		}
	}
	
	private TaskListener mReportTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "ReportTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
//				onReportSuccess();
			} else if (result == TaskResult.IO_ERROR) {
				onReportFailure();
			}
		}
	};
	public void onReportFailure() {
		Log.e(TAG, "Report process failed!");
	}
	
	@Override
    protected void onReturnFromCommentList(String info_id) {
        Log.d(TAG, "onReturnFromCommentList.");
        
        if (mGetInfoByIdTask != null && mGetInfoByIdTask.getStatus() == GenericTask.Status.RUNNING) {
            return;
        } else {
        	mGetInfoByIdTask = new CommonTask.GetInfoByIdTask();
        	//mGetInfoByIdTask.setFeedback(mFeedback);
        	mGetInfoByIdTask.setListener(mGetInfoByIdTaskListener);
        	
			TaskParams params = new TaskParams();
			params.put("infoId", info_id);
			mGetInfoByIdTask.execute(params);

            // Add Task to manager
            taskManager.addTask(mGetInfoByIdTask);
        }
    }
	
	private TaskListener mGetInfoByIdTaskListener = new TaskAdapter() {
		@Override
		public String getName() {
			return "GetInfoByIdTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				logout();
			} else if (result == TaskResult.OK) {
				if(task == mGetInfoByIdTask) {
					Info  mInfo = ((CommonTask.GetInfoByIdTask)mGetInfoByIdTask).getInfo();
				    onGetInfoByIdSuccess(mInfo);
				}
			} else if (result == TaskResult.IO_ERROR) {
				//onGetInfoByIdFailure();
			}
		}
	};
	private void onGetInfoByIdSuccess(Info mInfo) {
	    changeInfoAsInfo(mInfo);
	    mInfoListAdapter.refresh();
	}
}