package com.codeim.coxin.ui.module;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
//import java.util.HashMap;
import java.util.List;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;
//import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
// import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
//import android.widget.Toast;



import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;

import com.codeim.coxin.ProfileActivity;
import com.codeim.coxin.Time2Activity;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.app.LazyImageLoader.ImageLoaderCallback;
import com.codeim.coxin.data.Info;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.http.HttpClient;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.http.Response;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.task.CommonTask.TogglePraiseTask;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.util.DistanceHelper;
import com.codeim.coxin.view.InfoMenuPopupwindow;
import com.codeim.coxin.view.InfoMenuPopupwindow.InfoMenuAdapter;
import com.codeim.floorview.CommentActivity;
import com.codeim.floorview.CommentWriteActivity;
import com.codeim.floorview.MatrixImageActivity;
import com.codeim.floorview.adapter.CommentPicViewAdapter;
import com.codeim.floorview.bean.ImageFolder;
import com.codeim.floorview.view.ListImageDirPopupWindow;
import com.codeim.weixin.FriendViewActivity;
//import com.codeim.coxin.R;
import com.codeim.coxin.R;
import com.codeim.coxin.R.color;
//import com.codeim.coxin.data.User;

//import com.codeim.coxin.NearbyActivity;

/*
 * 用于用户的Adapter
 */
public class NearbyInfoArrayAdapter extends BaseAdapter implements TweetAdapter, OnCompletionListener, OnErrorListener, OnClickListener {
	private static final String TAG = "NearbyUserArrayAdapter";
	public static final String DOWNLOAD_DEFAULT_DIR = "/coxin/download";
	protected static final int REQUEST_CODE_LAUNCH_ACTIVITY = 0;
	
	public static final int IDLE_STATE = 0;
	public static final int PLAYING_STATE = 1;
	public static final int DOWNLOADING_STATE = 2;
	
	private int mState = IDLE_STATE;

	protected ArrayList<Info> mInfos;
	private Context mContext;
	protected LayoutInflater mInflater;
	
	//protected List<HashMap<String, Boolean>> mButtonStatuses;
	protected List<Integer> mButtonSelections;
	protected List<Integer> mProgressbarPercent;
	
	private MediaPlayer mItemPlayer = null;
	private String mPlayLink = null;
	
	//private int lastPosition;
	private int curPosition;
	//private View lastView = null;
	//private View curView = null;
	
	//private File mDownloadFile = null;
	private File mDownloadDir = null;
	
	//private View mPlayingView;
	private Integer mPlayingPosition;
	
	private ViewHolder mPlayingHolder;
	
	// ArrayList<HashMap<Integer, View>> mlistPositionView = new ArrayList<HashMap<Integer, View>>();
	// HashMap<Integer, ViewHolder> mPositionViewHolder = new HashMap<Integer, ViewHolder>();
	List<Integer> mPositionDownloadingList = new ArrayList<Integer>();
	ArrayList<String> infoMenuOptionList = new ArrayList<String>();
	
	//private InfoMenuPopupwindow info_menu_pop_view;
	
	private HttpClient mClient;
	
	private TogglePraiseTask mTogglePraiseTask;
	//private DownloadAudioTask mDownloadAudioTask;
	
	private final Handler mHandler = new Handler();
	
	private Runnable mUpdateSeekBar = new Runnable() {
        @Override
        public void run() {
		    if (mPlayingPosition != null) {
                //updateProgressBar(mPlayingPosition);
		    }
        }
    };
	
	private ImageLoaderCallback callback = new ImageLoaderCallback() {
		@Override
		public void refresh(String url, Bitmap bitmap) {
			NearbyInfoArrayAdapter.this.refresh();
		} 
	};

	public NearbyInfoArrayAdapter(Context context) {
		mInfos = new ArrayList<Info>();
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		//mButtonStatuses = new ArrayList<HashMap<String, Boolean>>();
		mButtonSelections = new ArrayList<Integer>();
		mProgressbarPercent = new ArrayList<Integer>();
		
		File downloadDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + DOWNLOAD_DEFAULT_DIR);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        mDownloadDir = downloadDir;
		mClient = getApi().getHttpClient();
		
	    initInfoPopView();
	}
	
	public void initInfoPopView() {
		
//		DisplayMetrics outMetrics = new DisplayMetrics(); 
////		getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
//		int mScreenHeight = outMetrics.heightPixels;
//		
//		infoMenuOptionList.add("回复");
//		
//		if(Integer.valueOf(TwitterApplication.getMyselfId(true))
//				== Integer.valueOf(info.owerId)) {
//		    infoMenuOptionList.add("删除");
//		} else {
//			infoMenuOptionList.add("举报");
//		}
//		
////		LayoutInflater inflater = getLayoutInflater () ;
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View mView = inflater.inflate(R.layout.info_pop_menu_view, null);
//        final InfoMenuPopupwindow info_menu_pop_view = new InfoMenuPopupwindow(LayoutParams.MATCH_PARENT, (int) (mScreenHeight * 0.7),
//				                   infoMenuOptionList, mView);
//        info_menu_pop_view.setOutsideTouchable(true);
//        info_menu_pop_view.setFocusable(true);
	}

	@Override
	public int getCount() {
		return mInfos.size();
	}

	@Override
	public Object getItem(int position) {
		return mInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder {
		public LinearLayout info_item_header;
		public ImageView info_item_divider;
		public LinearLayout info_item_footer;
		
		public ImageView profileImage;  //头像
		public TextView screenName;  //第一行：用户名
		public TextView distanceAndCreatedAt;  //第一行：距离与时间
		public TextView lastStatus;  //第二行：标题
		public ImageView down_arrow;
		
		public TextView public_time;//消息发布时间
		public TextView info_stauts;//消息状态
		
		public TextView context;
		
		public FrameLayout playBtn;  //第三行：播放按钮
		public FrameLayout downloadingBtn;  //第三行：正在下载按钮
		public FrameLayout stopBtn;  //第三行：停止按钮
		public ProgressBar playProgressBar;  //第三行：播放进度
		public TextView totalTimeText;  //第三行：语音时间长度
		public TextView commentCountText;  //第三行：评论个数
		
        public GridView infoPicShow;
        public CommentPicViewAdapter infoPicViewAdapter;
        
        public View info_item_footer_status;
        public TextView expire_time;
        public View info_item_footer_reply;
        public View info_item_footer_praise;
        public ImageView btn_praise;
        public TextView comment_count;
        public TextView praise_count;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;

		SharedPreferences pref = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(mContext);
		boolean useProfileImage = pref.getBoolean(Preferences.USE_PROFILE_IMAGE, true);
		
		view = convertView;
		final ViewHolder holder;

		if (view == null) {
//			view = mInflater.inflate(R.layout.user_item, parent, false);
			view = mInflater.inflate(R.layout.info_item, parent, false);
			holder = new ViewHolder();
			holder.info_item_header = (LinearLayout) view.findViewById(R.id.info_item_header);
			holder.info_item_divider = (ImageView) view.findViewById(R.id.info_item_divider);
			holder.info_item_footer = (LinearLayout) view.findViewById(R.id.info_item_footer);
			
			holder.profileImage = (ImageView) view.findViewById(R.id.profile_image);
			
			holder.screenName = (TextView) view.findViewById(R.id.screen_name);
			holder.distanceAndCreatedAt = (TextView) view.findViewById(R.id.tweet_meta_text);
			holder.down_arrow = (ImageView) view.findViewById(R.id.down_arrow);
			
			holder.public_time = (TextView) view.findViewById(R.id.public_time);
			
			holder.context = (TextView) view.findViewById(R.id.tweet_text);
			
			//the follow is gone temporally
			holder.playBtn = (FrameLayout) view.findViewById(R.id.play_btn_layout);
			holder.downloadingBtn = (FrameLayout) view.findViewById(R.id.downloading_btn_layout);
			holder.stopBtn = (FrameLayout) view.findViewById(R.id.stop_btn_layout);
			holder.playProgressBar = (ProgressBar) view.findViewById(R.id.play_progressbar);
			holder.totalTimeText = (TextView) view.findViewById(R.id.play_total_time_text);
			holder.commentCountText = (TextView) view.findViewById(R.id.comment_count_text);
			
	        holder.infoPicShow = ( GridView ) view.findViewById ( R.id.infoPicShow ) ;
	        holder.infoPicViewAdapter = new CommentPicViewAdapter(mContext);
	        
	        holder.info_item_footer_status = (View) view.findViewById ( R.id.info_item_footer_status ) ;
	        holder.expire_time = (TextView) view.findViewById ( R.id.txt_expire_time ) ;
	        holder.info_item_footer_reply = (View) view.findViewById ( R.id.info_item_footer_reply ) ;
	        holder.info_item_footer_praise = (View) view.findViewById ( R.id.info_item_footer_praise ) ;
	        holder.comment_count = (TextView) view.findViewById ( R.id.cnt_comment) ;
	        holder.praise_count = (TextView) view.findViewById ( R.id.cnt_praise ) ;
	        holder.btn_praise = (ImageView) view.findViewById ( R.id.btn_praise ) ;
	        
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		holder.down_arrow.setTag(position);
		holder.info_item_footer_status.setTag(position);
		final Info info = mInfos.get(position);
		
		/**** code for the local expire info ****/
		if(info.expire>0) {
			holder.info_item_header.setVisibility(View.GONE);
			holder.context.setText("本消息已经到期消除");
			holder.infoPicShow.setVisibility(View.GONE);
			holder.info_item_divider.setVisibility(View.GONE);
			holder.info_item_footer.setVisibility(View.GONE);
			
			return view;
		}
		holder.info_item_header.setVisibility(View.VISIBLE);
		holder.infoPicShow.setVisibility(View.VISIBLE);
		holder.info_item_divider.setVisibility(View.VISIBLE);
		holder.info_item_footer.setVisibility(View.VISIBLE);
		
		String owerImageUrl = info.owerImageUrl;
		mPlayLink = info.attachmentUrl;
		//Log.d(TAG, "mPlayLink = " + mPlayLink);
		if (useProfileImage) {
			if (!TextUtils.isEmpty(owerImageUrl)) {
				holder.profileImage.setImageBitmap(TwitterApplication.mImageLoader.get(owerImageUrl.toString(), callback));
			}
		} else {
			holder.profileImage.setVisibility(View.GONE);
		}
		// holder.profileImage.setImageBitmap(ImageManager.mDefaultBitmap);
		
		holder.screenName.setText(info.owerName);
		
		if (info.distance == -1) {
		    holder.distanceAndCreatedAt.setText(DateTimeHelper.getRelativeDate(info.createdAt));
		    holder.public_time.setText(DateTimeHelper.getRelativeDate(info.createdAt));
		} else if (info.createdAt != null) {
		    holder.distanceAndCreatedAt.setText(DistanceHelper.distanceConvert(info.distance) 
			        + " | " + DateTimeHelper.getRelativeDate(info.createdAt));
		    holder.public_time.setText(DistanceHelper.distanceConvert(info.distance) 
			        + " | " + DateTimeHelper.getRelativeDate(info.createdAt));
		} else {
		    holder.distanceAndCreatedAt.setText(DistanceHelper.distanceConvert(info.distance));
		    holder.public_time.setText(DistanceHelper.distanceConvert(info.distance));
		}
		holder.context.setText(info.context);
//		holder.totalTimeText.setText(String.valueOf(user.statusAudioDuration) + "''");
		holder.totalTimeText.setText("''");
		
		// Log.d(TAG, "statusInReplyToStatusId = " + user.statusInReplyToStatusId);
		/*
		if (user.statusInReplyToStatusId.equals("") || user.statusInReplyToStatusId.equals("null")) {
			holder.commentCountText.setText("评:" + String.valueOf(user.statusConversationCount-1));
		} else {
			holder.commentCountText.setText("评:" + String.valueOf(user.statusReplyCount));
		}
		*/
		holder.commentCountText.setText("评:" + String.valueOf(info.conversationCount-1));
		
		holder.expire_time.setText(""+ DateTimeHelper.dateToString(info.expireTime, ""));
		holder.comment_count.setText(String.valueOf(info.conversationCount));
		holder.praise_count.setText(String.valueOf(info.praiseCount));
		if(info.user_praise==1) {
			holder.btn_praise.setBackgroundResource(R.drawable.fav_en_dark);
		} else {
			holder.btn_praise.setBackgroundResource(R.drawable.fav_un_dark);
		}

		
		if (mButtonSelections.get(position) == 1) {
			holder.playBtn.setVisibility(View.VISIBLE);
			holder.downloadingBtn.setVisibility(View.GONE);
			holder.stopBtn.setVisibility(View.GONE);
		} else if (mButtonSelections.get(position) == 2) {
			holder.downloadingBtn.setVisibility(View.VISIBLE);
			holder.playBtn.setVisibility(View.GONE);
			holder.stopBtn.setVisibility(View.GONE);
		} else if (mButtonSelections.get(position) == 3) {
			holder.stopBtn.setVisibility(View.VISIBLE);
			holder.playBtn.setVisibility(View.GONE);
			holder.downloadingBtn.setVisibility(View.GONE);
		}
		holder.playProgressBar.setMax(10000);
		//holder.playProgressBar.setProgress(mProgressbarPercent.get(position));
		
		//点击头像进入用户信息页面
		holder.profileImage.setOnClickListener(new OnClickListener() {
		    @Override
			public void onClick(View v) {
		    	Log.e(TAG, "holder.profileImage position: "+String.valueOf(position));
		    	// launchActivity(ProfileActivity.createIntent(user.id));
//				launchActivity(ProfileActivity.createIntent(user));
//				launchActivity(ProfileActivity.createIntent(info));
		    	
		    	Intent intent = new Intent((Activity) mContext, FriendViewActivity.class);
		    	intent.putExtra("slaveId", info.owerId);
		    	intent.putExtra("serverId", "0");
		    	((Activity) mContext).startActivityForResult(intent, 600);
			}
		});
		
		holder.down_arrow.setOnClickListener(this);
		holder.info_item_footer_status.setOnClickListener(this);
		//播放按钮
		holder.playBtn.setOnClickListener(new OnClickListener() {
		    @Override
			public void onClick(View v) {
			    mPlayLink = info.attachmentUrl;
				String fileName = mPlayLink.substring(mPlayLink.lastIndexOf("/") + 1);

			    //Log.d(TAG, "position " + position);
				//lastPosition = curPosition;
				curPosition = position;
				
				//lastView = curView;
				//curView = v;
				
				ViewHolder itemHolder = holder;
				
				if (mState == PLAYING_STATE) {  //其中一个item正在播放，需要先停止这个item
				    stopPlay();
				}
				
				//if (mPositionViewHolder.containsKey(position)) {  //正在下载相同position的语音
			    if (mPositionDownloadingList.contains(position)) {
					//因为已经在下载所以仅仅切换按钮就可以
					mButtonSelections.set(position, 2);
			        notifyDataSetChanged();	
				} else {
					File downloadFile = new File(mDownloadDir.getAbsolutePath() + "/" + fileName);
					if (downloadFile.exists()) {  //语音已经存在，直接播放
					    mPlayingHolder = itemHolder;
						startPlay(position, downloadFile.getAbsolutePath());
					} else {  //语音不存在，需要下载
						downloadAudio(position, fileName, mPlayLink);
					}
				}
			}
		});
		
		//正在下载
		holder.downloadingBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    //Log.d(TAG, "position " + position);
				curPosition = position;
				mButtonSelections.set(position, 1);
			    notifyDataSetChanged();	
			}
		});
		
		//停止按钮
		holder.stopBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    //Log.d(TAG, "position " + position);
				curPosition = position;
				//curView = v;
				stopPlay();
			}
		});
		
		//picShow in info
        final ArrayList<String> picDataList = info.getPicPathList();
//        if(available && picDataList != null && (!picDataList.isEmpty())) {
        if(picDataList != null && (!picDataList.isEmpty())) {
        	holder.infoPicShow.setVisibility(View.VISIBLE);
//            holder.commentPicViewAdapter = new CommentPicViewAdapter(mContext, picDataList);
        	holder.infoPicViewAdapter.setData(picDataList);
            holder.infoPicShow.setAdapter(holder.infoPicViewAdapter);
            holder.infoPicViewAdapter.refresh();
        } else {
        	holder.infoPicShow.setVisibility(View.GONE);
        }
        //for comment picture full Image PopupWindow
        final GridView mGrideView = holder.infoPicShow;
        mGrideView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				/**直接弹出popupwindow**/
//		        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		        final View mView = inflater.inflate(R.layout.image_select_full_shower, null);
//		        
//				final SelectForFullImagePopupWindow mPopupWindow;
//				mPopupWindow = new SelectForFullImagePopupWindow(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
//						picDataList.get(position), mView, false, true);
//				mPopupWindow.setOutsideTouchable(true);
//				mPopupWindow.setFocusable(true);
//				mPopupWindow.findViewById(R.id.image_checker).setVisibility(View.GONE);
//				final View headView = ((Activity) mContext).findViewById(R.id.title);
//				mPopupWindow.showAsDropDown(headView);
				
				Intent intent=new Intent((Activity) mContext, MatrixImageActivity.class);
				Bundle bundle=new Bundle();
				intent.putExtra("position", position);
				intent.putExtra("style", MatrixImageActivity.HEADER_ONLY_HEADER_BACK);
				intent.putExtra("local_image", false);
				intent.putStringArrayListExtra("datalist", getIntentArrayList(picDataList));
				intent.putExtras(bundle);
				launchActivity(intent);
//				startActivityForResult(intent, MATRIX_IMAGE_PREVIEW);
			}
		});
        
        holder.info_item_footer_reply.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_ID, info.owerId.toString() ).commit();
				TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_OWNER_USERNAME, info.owerName.toString() ).commit();
				TwitterApplication.mPref.edit().putString(Preferences.CURRENT_INFO_ID, info.id.toString() ).commit();
				
				Intent intent = new Intent((Activity) mContext, CommentWriteActivity.class);
				Bundle bundle=new Bundle();
				intent.putExtra("parentid", 0);
				intent.putExtra("floornum", 0);
				intent.putExtras(bundle);
				((Activity) mContext).startActivityForResult(intent, 100);
			}
        });
        
        holder.info_item_footer_praise.setOnClickListener(new OnClickListener() {
        	@Override
        	public void onClick(View v) {
        		toggle_praise(info.id, info.user_praise, holder.praise_count, holder.btn_praise); //temp false
        	}
        });
        
//      holder.info_item_footer_status.setOnClickListener(new OnClickListener() {
//    	@Override
//    	public void onClick(View v) {
//    		mOnTimeSet.clickTimeSet(position,info.expireTime);
//    	}
//      });

//        holder.info_item_footer_status.setOnClickListener(new OnClickListener() {
//        	@Override
//        	public void onClick(View v) {
//        		DisplayMetrics outMetrics = new DisplayMetrics(); 
//        		((Activity)mContext).getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
//        		int mScreenHeight = outMetrics.heightPixels;
//        		int mScreenWidth = outMetrics.widthPixels;
//        		
//        		infoMenuOptionList.clear();
//        		infoMenuOptionList.add("回复");
//        		
//				if(Integer.valueOf(TwitterApplication.getMyselfId(true))
//						== Integer.valueOf(info.owerId)) {
//        		    infoMenuOptionList.add("删除");
//				} else {
//					infoMenuOptionList.add("举报");
//				}
//        		
////        		LayoutInflater inflater = getLayoutInflater () ;
//                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                final View mView = inflater.inflate(R.layout.info_pop_menu_view, null);
//                if(info_menu_pop_view==null) {
//                info_menu_pop_view = new InfoMenuPopupwindow((int) (mScreenWidth * 0.7), android.view.WindowManager.LayoutParams.WRAP_CONTENT,
//        				                   infoMenuOptionList, mView);
//                }
//                info_menu_pop_view.setOutsideTouchable(true);
//                info_menu_pop_view.setFocusable(true);
////                info_menu_pop_view.setBackgroundDrawable(new BitmapDrawable()); 
//                
//                info_menu_pop_view.setOnDismissListener(new OnDismissListener() {
//                	public void onDismiss() {
//    					// 设置背景颜色变亮
////                		((Activity)mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//    					WindowManager.LayoutParams lp = ((Activity)mContext).getWindow().getAttributes();
//    					lp.alpha = 1.0f;
////    					lp.dimAmount=0.0f;
//    					((Activity)mContext).getWindow().setAttributes(lp);
//    					
//    					mOnPopupWindowStateChange.popupWindowStateChange(false);
//                	}
//                });
//                
//                if(!info_menu_pop_view.isShowing()) {
////					info_menu_pop_view.showAtLocation((View)v.getParent(), Gravity.CENTER|Gravity.CENTER_HORIZONTAL, 0, 0);
//////					info_menu_pop_view.setAnimationStyle(R.style.PopupAnimation);
////					info_menu_pop_view.refresh();
////					info_menu_pop_view.update();
//					
//					
////					ColorDrawable cd = new ColorDrawable(0x000000);
////					info_menu_pop_view.setBackgroundDrawable(cd); 
//					
//					//设置背景颜色变暗
//					WindowManager.LayoutParams lp = ((Activity)mContext).getWindow().getAttributes();
//					lp.alpha = 0.3f;
////					lp.dimAmount=0.7f;
//					((Activity)mContext).getWindow().setAttributes(lp);
////					((Activity)mContext).getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
//					
////					mView.setBackgroundColor(((Activity)mContext).getResources().getColor(android.R.color.black));
////					//设置背景布局的透明度
////					mView.getBackground().setAlpha(105);
//					
//					info_menu_pop_view.showAtLocation((View)v.getParent(), Gravity.CENTER|Gravity.CENTER_HORIZONTAL, 0, 0);
////					info_menu_pop_view.setAnimationStyle(R.style.PopupAnimation);
//					info_menu_pop_view.refresh();
//					info_menu_pop_view.update();
//					
////					mOnPopupWindowStateChange.popupWindowStateChange(true);
//                } else {
//                	info_menu_pop_view.dismiss();
//                }
//                
//				
////				mView.setFocusableInTouchMode(true);//能够获得焦点
////				mView.setOnKeyListener(new OnKeyListener() {
////		             
////		            @Override
////		            public boolean onKey(View v, int keyCode, KeyEvent event) {
////		                if (event.getAction() == KeyEvent.ACTION_DOWN) {
////		                    switch(keyCode) {
////		                    case KeyEvent.KEYCODE_BACK:
////		                        closePopupWindow();
////		                        break;
////		                    case KeyEvent.KEYCODE_MENU:
////		                        closePopupWindow();
////		                        break;
////		                    }
////		                }
////		                return true;
////		            }
////		        });
//				
//        	}
//        });

		return view;
	}
	
	protected void launchActivity(Intent intent) {
		mContext.startActivity(intent);
	}
	
	public void startPlay(int position, String downloadFilePath) {
		//Log.d(TAG, "downloadFile: " + downloadFilePath);
        //stopPlay();
        mItemPlayer = new MediaPlayer();
        try {
            mItemPlayer.setDataSource(downloadFilePath);
            mItemPlayer.setOnCompletionListener(this);
            mItemPlayer.setOnErrorListener(this);
            mItemPlayer.prepare();
            mItemPlayer.seekTo(0);
            mItemPlayer.start();
        } catch (IllegalArgumentException e) {
            mItemPlayer = null;
            return;
        } catch (IOException e) {
            mItemPlayer = null;
            return;
        }
        
		mPlayingPosition = position;
        mButtonSelections.set(position, 3);
        mProgressbarPercent.set(position, 0);
        notifyDataSetChanged();
        
		mState = PLAYING_STATE;
		//updateProgressBar(position);
    }
	
	/*
	private void updateProgressBar(int position) {
        if (mState == PLAYING_STATE) {
        	//playProgressBar.setProgress(mItemPlayer.getCurrentPosition());
        	float percent = (float) mItemPlayer.getCurrentPosition()/mItemPlayer.getDuration();
        	//Log.d(TAG, "Play percent is " + percent + "  " + (int)(10000*percent) + "  " + mItemPlayer.getCurrentPosition() + "  " + mItemPlayer.getDuration());
        	mProgressbarPercent.set(position, (int)(10000*percent));
        	//notifyDataSetChanged();
        	updateProgressBarView(position, (int)(10000*percent));
            mHandler.postDelayed(mUpdateSeekBar, 500);
        }
    }
	
	private void updateProgressBarView(int position, int progress) {
		//int visiblePosition = listView.getFirstVisiblePosition();
		//Log.d(TAG, "visiblePosition = " + visiblePosition + "   positon = " + position);
		//View itemView = listView.getChildAt(position);
		//ProgressBar itemProgressBar = (ProgressBar) itemView.findViewById(R.id.play_progressbar);
		ProgressBar itemProgressBar = mPlayingHolder.playProgressBar;
		itemProgressBar.setProgress(progress);
	}
	*/
	
	public void stopPlay() {
        if (mItemPlayer == null) // we were not in playback
            return;
        //Log.d(TAG, "stopPlay");
        mItemPlayer.stop();
        mItemPlayer.release();
        mItemPlayer = null;
        
        mButtonSelections.set(mPlayingPosition, 1);
        notifyDataSetChanged();

		mPlayingPosition = null;
		mPlayingHolder = null;

        mState = IDLE_STATE;
    }

	public void refresh(ArrayList<Info> infos) {
		mInfos = (ArrayList<Info>) infos.clone();
		
		for (int i=0; i<mInfos.size(); i++) {
			//Log.d(TAG, "i = " + i);
			//mButtonStatuses.get(i).put(key, value);
			mButtonSelections.add(1);
			mProgressbarPercent.add(0);
		}
		
		notifyDataSetChanged();
	}

	@Override
	public void refresh() {
		notifyDataSetChanged();
	}
	
	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
        stopPlay();
        return true;
    }

	@Override
    public void onCompletion(MediaPlayer mp) {
        stopPlay();
    }
	
	public Weibo getApi() {
		return TwitterApplication.mApi;
	}
	
	private InputStream getInputStream(String url) throws HttpException, IOException {
	    Response res = mClient.get(url);
		InputStream is = res.asStream();
		
		return is;
	}
	
	private void downloadAudio(Integer position, String fileName, String audioURL) {

		//if (mDownloadAudioTask != null && mDownloadAudioTask.getStatus() == AsyncTask.Status.RUNNING) {
		//	return;
		//} else {
		
			DownloadAudioTask downloadAudioTask = new DownloadAudioTask(position, fileName);
			TaskParams params = new TaskParams();
			params.put("filename_str", fileName);
			params.put("audio_url", audioURL);
			downloadAudioTask.execute(params);
		//}
	}
	
	private class DownloadAudioTask extends AsyncTask<TaskParams, Object, TaskResult> {
		
		private Integer position;
		private String downloadFilePath;
		
		public DownloadAudioTask(Integer position, String fileName) {
			super();
			this.position = position;
			this.downloadFilePath = mDownloadDir.getAbsolutePath() + "/" + fileName;
		}
	    
		@Override
		protected TaskResult doInBackground(TaskParams... params) {
		    TaskParams param = params[0];
			try {
				String fileName = param.getString("filename_str");
			    String audioURL = param.getString("audio_url");
				if (!TextUtils.isEmpty(audioURL)) {
				    InputStream is = getInputStream(audioURL);
				    File downloadFile = new File(mDownloadDir.getAbsolutePath() + "/" + fileName);
				    downloadFile.createNewFile();
					OutputStream out = null;
					out = new FileOutputStream(downloadFile);
					int temp = 0;
			        byte[] data = new byte[1024];
			        while((temp = is.read(data))!=-1){
				        out.write(data, 0, temp);
			        }
			        out.flush();
			        is.close();
			        out.close();
				}
			} catch (HttpException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			} catch (IOException e) {
				Log.e(TAG, e.getMessage(), e);
				return TaskResult.IO_ERROR;
			} 
			return TaskResult.OK;
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//mPositionViewHolder.put(position, holder);
			//holder.playBtn.setVisibility(View.GONE);
			//holder.downloadingBtn.setVisibility(View.VISIBLE);
			mPositionDownloadingList.add(position);
			mButtonSelections.set(position, 2);
			notifyDataSetChanged();
		}
		
		@Override
		protected void onPostExecute(TaskResult result) {
			super.onPostExecute(result);
			
			//mPositionViewHolder.remove(position);
			mPositionDownloadingList.remove(position);
			
			if (result == TaskResult.AUTH_ERROR) {
				
            } else if (result == TaskResult.OK) {
            	if ((position == curPosition) && (mButtonSelections.get(position) == 2)) { 
            		//holder.downloadingBtn.setVisibility(View.GONE);
            		//holder.playBtn.setVisibility(View.VISIBLE);
            		mButtonSelections.set(position, 3);
            		notifyDataSetChanged();
            		startPlay(position, downloadFilePath);
            	} else {
            		//holder.downloadingBtn.setVisibility(View.GONE);
            		//holder.playBtn.setVisibility(View.VISIBLE);
            		mButtonSelections.set(position, 1);
            		notifyDataSetChanged();
            	}
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

	private void toggle_praise(String info_id, int nowAvailable, final TextView praise_count, final ImageView btn_praise) {

		if (mTogglePraiseTask != null && mTogglePraiseTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		} else {
		
		    mTogglePraiseTask = new TogglePraiseTask(info_id, nowAvailable, praise_count, btn_praise);
			TaskParams params = new TaskParams();
//			params.put("info_id", info_id);
//			params.put("is_available", nowAvailable);
			mTogglePraiseTask.execute(params);
		}
	}
	
	private ArrayList<String> getIntentArrayList(ArrayList<String> dataList) {
		ArrayList<String> tDataList = new ArrayList<String>();
		for (String s : dataList) {
			if (!s.contains("default")) {
				tDataList.add(s);
			}
		}
		return tDataList;
	}
	
	
	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
		switch (v.getId()) {	    
		    case R.id.down_arrow:
		    	Log.e(TAG, "R.id.down_arrow position: "+String.valueOf(position));
			    mOnClickDownArrow.downArrowPop(position);
			    break;
//		    case R.id.txt_expire_time:
		    case R.id.info_item_footer_status:
		    	Log.e(TAG, "R.id.info_item_footer_status: "+String.valueOf(position));
		    	mOnTimeSet.clickTimeSet(position);
		    	break;
		}
	}
	
	private OnPopupWindowStateChange mOnPopupWindowStateChange;
	public void setOnPopupWindowStateChange(OnPopupWindowStateChange l) {
		mOnPopupWindowStateChange = l;
	}
	public interface OnPopupWindowStateChange {
		public void popupWindowStateChange(boolean state);
	}
	
	
	private OnClickDownArrow mOnClickDownArrow;
	public void setOnClickDownArrow(OnClickDownArrow l) {
		mOnClickDownArrow = l;
	}
	public interface OnClickDownArrow {
		public void downArrowPop(int position);
	}
	
	private OnTimeSet mOnTimeSet;
	public void setOnTimeSet(OnTimeSet l) {
		mOnTimeSet = l;
	}
	public interface OnTimeSet {
		public void clickTimeSet(int position);
	}
	
}
