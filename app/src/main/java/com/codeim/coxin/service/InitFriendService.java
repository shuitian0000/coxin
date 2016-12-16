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

package com.codeim.coxin.service;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.data.Friend;
import com.codeim.coxin.db.TwitterDatabase;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.SimpleFeedback;
import com.codeim.coxin.util.DebugTimer;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;

public class InitFriendService extends Service {
	private static final String TAG = "InitFriendService";

	private NotificationManager mNotificationManager;
	
//	private ArrayList<Tweet> mNewTweets;
//	private ArrayList<Tweet> mNewMentions;
//	private ArrayList<Dm> mNewDms;

	volatile protected ArrayList<com.codeim.coxin.data.Friend> allContactList;
	private FriendsRetrieveTask mRetrieveTask;
	private int LocalLastFriendId=-1;
	private int page_size=1;
	private int page_index=0;
	private boolean data_finish = false;
	private boolean no_data = false;
	private Feedback mFeedback;
	
    class FriendsRetrieveTask extends GenericTask {
        protected String _errorMsg;

        public String getErrorMsg() {
            return _errorMsg;
        }

        @Override
        protected TaskResult _doInBackground(TaskParams... params) {
			//TwitterApplication twitterApplication = (TwitterApplication) contextActivity.getApplication();
			
			List<com.codeim.coxin.fanfou.Friend> friendsList = null;
			
            try {
            	//friend relationship, pull from web
                friendsList = TwitterApplication.mApi.getFriendsFromWeb(page_size, page_index, LocalLastFriendId, 
    			        TwitterApplication.getMyselfId(false));
            } catch (HttpException e) {
                Log.e(TAG, e.getMessage(), e);
                _errorMsg = e.getMessage();
                return TaskResult.IO_ERROR;
            }

            publishProgress(SimpleFeedback.calProgressBySize(40, 20, friendsList));
            allContactList.clear();
			for (com.codeim.coxin.fanfou.Friend friend : friendsList) {
				if (isCancelled()) {
					return TaskResult.CANCELLED;
				}
				// Log.d(TAG, "User: " + user.toString());
				Friend u = Friend.createWithoutURL(friend);
				allContactList.add(u);
			}
			if (isCancelled()) {
				return TaskResult.CANCELLED;
			}
			
			if(allContactList ==null || allContactList.size()<1) {
				data_finish = true;
				no_data = true;
				
				return TaskResult.OK;
			}
			
			com.codeim.coxin.data.Friend friend = allContactList.get(allContactList.size()-1);
			if(friend.id.equals("-1")) {
				allContactList.remove(allContactList.size()-1);
				data_finish = true;
			}
			if(allContactList.size()>0) {
				LocalLastFriendId = Integer.valueOf(allContactList.get(allContactList.size()-1).id);
			} else {
				no_data = true;
			}

            return TaskResult.OK;
        }
    }

	public String getUserId() {
		return TwitterApplication.getMyselfId(false);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// fetchMessages();
		// handler.postDelayed(mTask, 10000);
		Log.d(TAG, "Start Once");

		return super.onStartCommand(intent, flags, startId);
	}
	
	
    private TaskListener mRetrieveTaskListener = new TaskAdapter() {
        @Override
        public String getName() {
            return "mRetrieveTaskListener";
        }

        @Override
        public void onPostExecute(GenericTask task, TaskResult result) {
            if (result == TaskResult.AUTH_ERROR) {
                mFeedback.failed("登录信息出错");
                onGetFriendsEnd();//logout();
            } else if (result == TaskResult.OK) {
            	onGetFriendsEnd();//draw();
//                if (task == mRetrieveTask) {
//                    goTop();
//                }
            } else if (result == TaskResult.IO_ERROR) {
                // FIXME: bad smell
            	onGetFriendsEnd();//
//                if (task == mRetrieveTask) {
//                    mFeedback.failed(((RetrieveTask) task).getErrorMsg());
//                } else if (task == mGetMoreTask) {
//                    mFeedback.failed(((GetMoreTask) task).getErrorMsg());
//                }
            } else {
            	onGetFriendsEnd();// do nothing
            }
            
            // DEBUG
            if (TwitterApplication.DEBUG) {
                DebugTimer.stop();
                Log.v("DEBUG", DebugTimer.getProfileAsString());
            }
            
            Log.v("mRetrieveTaskListener", "postexecute!");
        }

        @Override
        public void onPreExecute(GenericTask task) {
            //mTweetList.prepareForRefresh();
            if (TwitterApplication.DEBUG) {
                DebugTimer.start();
            }
        }

        @Override
        public void onProgressUpdate(GenericTask task, Object param) {
            // Log.d(TAG, "onProgressUpdate");
        	onGetFriendsEnd();//draw();
        }
    };

	public void onGetFriendsEnd() {
		TwitterApplication.mDb.syncContacts(allContactList);
		
		if(data_finish) {
			stopSelf();
		} else {
			getOnePageFriendsFromWeb();
		}
	}

	private WakeLock mWakeLock;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private TwitterDatabase getDb() {
		return TwitterApplication.mDb;
	}

	private Weibo getApi() {
		return TwitterApplication.mApi;
	}

	@Override
	public void onCreate() {
		Log.v(TAG, "InitFriendService Created");
		super.onCreate();

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();

		allContactList = new ArrayList<com.codeim.coxin.data.Friend>();
//		boolean needCheck = TwitterApplication.mPref.getBoolean(
//				Preferences.CHECK_UPDATES_KEY, false);
//		boolean widgetIsEnabled = InitFriendService.widgetIsEnabled;
//		Log.v(TAG, "Check Updates is " + needCheck + "/wg:" + widgetIsEnabled);
//		if (!needCheck && !widgetIsEnabled) {
//			Log.d(TAG, "Check update preference is false.");
//			stopSelf();
//			return;
//		}
		
		LocalLastFriendId = TwitterApplication.mDb.getLocalLastFriendId(getUserId());
		getOnePageFriendsFromWeb();
		

		if (!getApi().isLoggedIn()) {
			Log.d(TAG, "Not logged in.");
			stopSelf();
			return;
		}

		schedule(InitFriendService.this);

		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

//		mNewTweets = new ArrayList<Tweet>();
//		mNewMentions = new ArrayList<Tweet>();
//		mNewDms = new ArrayList<Dm>();

	}
	public void getOnePageFriendsFromWeb() {
		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mRetrieveTask = new FriendsRetrieveTask();
			mRetrieveTask.setListener(mRetrieveTaskListener);
			mRetrieveTask.execute((TaskParams[]) null);
		}
	}
	
	
//	private static int TWEET_NOTIFICATION_ID = 0;
//	private static int DM_NOTIFICATION_ID = 1;
//	private static int MENTION_NOTIFICATION_ID = 2;

//	private void notify(PendingIntent intent, int notificationId,
//			int notifyIconId, String tickerText, String title, String text) {
//		Notification notification = new Notification(notifyIconId, tickerText,
//				System.currentTimeMillis());
//
//		notification.setLatestEventInfo(this, title, text, intent);
//
//		notification.flags = Notification.FLAG_AUTO_CANCEL
//				| Notification.FLAG_ONLY_ALERT_ONCE
//				| Notification.FLAG_SHOW_LIGHTS;
//
//		notification.ledARGB = 0xFF84E4FA;
//		notification.ledOnMS = 5000;
//		notification.ledOffMS = 5000;
//
//		String ringtoneUri = TwitterApplication.mPref.getString(
//				Preferences.RINGTONE_KEY, null);
//
//		if (ringtoneUri == null) {
//			notification.defaults |= Notification.DEFAULT_SOUND;
//		} else {
//			notification.sound = Uri.parse(ringtoneUri);
//		}
//
//		if (TwitterApplication.mPref.getBoolean(Preferences.VIBRATE_KEY, false)) {
//			notification.defaults |= Notification.DEFAULT_VIBRATE;
//		}
//
//		mNotificationManager.notify(notificationId, notification);
//	}

//	private void processNewDms() {
//		int count = mNewDms.size();
//		if (count <= 0) {
//			return;
//		}
//
//		Dm latest = mNewDms.get(0);
//
//		String title;
//		String text;
//
//		if (count == 1) {
//			title = latest.screenName;
//			text = TextHelper.getSimpleTweetText(latest.text);
//		} else {
//			title = getString(R.string.service_new_direct_message_updates);
//			text = getString(R.string.service_x_new_direct_messages);
//			text = MessageFormat.format(text, count);
//		}
//
//		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//				DmActivity.createIntent(), 0);
//
//		notify(pendingIntent, DM_NOTIFICATION_ID, R.drawable.notify_dm,
//				TextHelper.getSimpleTweetText(latest.text), title, text);
//	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "Service Destroy.");

		if (mRetrieveTask != null
				&& mRetrieveTask.getStatus() == GenericTask.Status.RUNNING) {
			mRetrieveTask.cancel(true);
		}

		mWakeLock.release();
		super.onDestroy();
	}

	public static void schedule(Context context) {
//		SharedPreferences preferences = TwitterApplication.mPref;
//
//		boolean needCheck = preferences.getBoolean(
//				Preferences.CHECK_UPDATES_KEY, false);
//		boolean widgetIsEnabled = InitFriendService.widgetIsEnabled;
//		if (!needCheck && !widgetIsEnabled) {
//			Log.d(TAG, "Check update preference is false.");
//			return;
//		}
//
//		String intervalPref = preferences
//				.getString(
//						Preferences.CHECK_UPDATE_INTERVAL_KEY,
//						context.getString(R.string.pref_check_updates_interval_default));
//		int interval = Integer.parseInt(intervalPref);
//		// interval = 1; //for debug
//
//		Intent intent = new Intent(context, InitFriendService.class);
//		PendingIntent pending = PendingIntent.getService(context, 0, intent,
//				PendingIntent.FLAG_CANCEL_CURRENT);
//		Calendar c = new GregorianCalendar();
//		c.add(Calendar.MINUTE, interval);
//
//		DateFormat df = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
//		Log.d(TAG, "Schedule, next run at " + df.format(c.getTime()));
//
//		AlarmManager alarm = (AlarmManager) context
//				.getSystemService(Context.ALARM_SERVICE);
//		alarm.cancel(pending);
//		if (needCheck) {
//			alarm.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pending);
//		} else {
//			// only for widget
//			alarm.set(AlarmManager.RTC, c.getTimeInMillis(), pending);
//		}
	}

	public static void unschedule(Context context) {
//		Intent intent = new Intent(context, InitFriendService.class);
//		PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
//		AlarmManager alarm = (AlarmManager) context
//				.getSystemService(Context.ALARM_SERVICE);
//		Log.d(TAG, "Cancelling alarms.");
//		alarm.cancel(pending);
	}

	private static boolean widgetIsEnabled = false;

	public static void setWidgetStatus(boolean isEnabled) {
		widgetIsEnabled = isEnabled;
	}

	public static boolean isWidgetEnabled() {
		return widgetIsEnabled;
	}

}
