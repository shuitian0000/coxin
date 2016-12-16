package com.codeim.floorview.adapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
//import java.util.HashMap;
import java.util.List;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
//import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;
//import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
//import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
// import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
//import android.widget.Toast;

import android.widget.AdapterView.OnItemClickListener;

import com.codeim.coxin.R;
import com.codeim.coxin.R.color;
import com.codeim.coxin.R.drawable;
import com.codeim.coxin.ProfileActivity;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.app.LazyImageLoader.ImageLoaderCallback;
import com.codeim.coxin.data.Info;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.http.HttpClient;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.http.Response;
import com.codeim.coxin.task.CommonTask;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.task.CommonTask.TogglePraiseTask;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter;
import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter.OnClickDownArrow;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter.OnTimeSet;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.util.DistanceHelper;
import com.codeim.floorview.CommentWriteActivity;
import com.codeim.floorview.MatrixImageActivity;
//import com.codeim.coxin.data.MsgType;
import com.codeim.floorview.bean.Comment;
import com.codeim.floorview.utils.DateFormatUtils;
import com.codeim.floorview.view.FloorView;
import com.codeim.floorview.view.SelectForFullImagePopupWindow;
import com.codeim.floorview.view.SubComments;
import com.codeim.floorview.view.SubFloorFactory;
import com.codeim.floorview.widget.PinnedSectionListView.PinnedSectionListAdapter;


/*
 * 用于用户的Adapter
 */
//public class MsgTypeArrayAdapter extends BaseAdapter implements TweetAdapter, OnCompletionListener, OnErrorListener {
public class CommentArrayAdapter extends BaseAdapter implements TweetAdapter, PinnedSectionListAdapter, OnClickListener {
	private static final String TAG = "CommentArrayAdapter";
	public static final String DOWNLOAD_DEFAULT_DIR = "/coxin/download";
	protected static final int REQUEST_CODE_LAUNCH_ACTIVITY = 0;
	
	public static final int IDLE_STATE = 0;
	public static final int PLAYING_STATE = 1;
	public static final int DOWNLOADING_STATE = 2;
	
	public static final int  OTHERS_TYPE = 0;
	public static final int  FIRST_TYPE = 1;
	public static final int  SECTION_TYPE = 2;
	public static final int  PROGRESS_BAR = 3;
	
	private static View pbItemView=null;
	static PbItemViewHolder pbItemViewHolder=null;
	public boolean pb_visiable=true;
//	RelativeLayout comment_refresh;
//    private ProgressBar pb_refresh;
//    private TextView tv_title;
//    private TextView tv_time;
	
	private int mState = IDLE_STATE;

	protected ArrayList<Comment> mComments;
	private Context mContext;
	protected LayoutInflater mInflater;
	
	//protected List<HashMap<String, Boolean>> mButtonStatuses;
	protected List<Integer> mButtonSelections;
	protected List<Integer> mProgressbarPercent;
	
	//private MediaPlayer mItemPlayer = null;
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
	
//	private ImageLoaderCallback callback = new ImageLoaderCallback() {
//		@Override
//		public void refresh(String url, Bitmap bitmap) {
//			NearbyInfoArrayAdapter.this.refresh();
//		} 
//	};

	public CommentArrayAdapter(Context context) {
		mComments = new ArrayList<Comment>();
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		//mButtonStatuses = new ArrayList<HashMap<String, Boolean>>();
		mButtonSelections = new ArrayList<Integer>();
		mProgressbarPercent = new ArrayList<Integer>();
		
//		File downloadDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + DOWNLOAD_DEFAULT_DIR);
//        if (!downloadDir.exists()) {
//            downloadDir.mkdirs();
//        }
//        mDownloadDir = downloadDir;
//		mClient = getApi().getHttpClient();
	}

	@Override
	public int getCount() {
		Log.v("comment_count", String.valueOf(mComments.size()));
		return mComments.size();
	}

	@Override
	public Comment getItem(int position) {
		return mComments.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
    @Override 
    public int getViewTypeCount() {  
        return 4;  
    }  
       
    @Override 
    public int getItemViewType(int position) {
        if (position==0) {
            return FIRST_TYPE;
//        } else if(getItem(position).section) {
        } else if(position == 1) {
        	return SECTION_TYPE;
        } else if(position==2) {
        	return PROGRESS_BAR;
        } else {
            return OTHERS_TYPE;  
        }  
    } 
    
    @Override
    public boolean isItemViewTypePinned(int viewType) {
    	return viewType == SECTION_TYPE;
    }

  //第一个Item的ViewHolder  
    private class FirstItemViewHolder{  
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
    
	private static class ViewHolder {
        public TextView floor_date; 
        public TextView floor_username;
        public TextView floor_content;
        
        public GridView commentPicShow;
        public CommentPicViewAdapter commentPicViewAdapter;
        
        public FloorView subFloors; 
	}
	private static class SectionItemViewHolder {
        public TextView section_txt;
	}
	private static class PbItemViewHolder {
		RelativeLayout comment_refresh;
        ProgressBar pb_refresh;
        TextView tv_title;
        TextView tv_time;
	}
	
	private ImageLoaderCallback callback = new ImageLoaderCallback() {
		@Override
		public void refresh(String url, Bitmap bitmap) {
			CommentArrayAdapter.this.refresh();
		} 
	};
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;
		ViewGroup floor;
		
		SharedPreferences pref = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(mContext);
		//boolean useProfileImage = pref.getBoolean(Preferences.USE_PROFILE_IMAGE, true);
		
		view = convertView;
		ViewHolder holder;
		
		Log.v("getView", "into getView");
		
		int currentType= getItemViewType(position);
		View firstItemView = null;
		View sectionItemView = null;
//		View pbItemView = null;
//		pbItemView = null;
		boolean useProfileImage = pref.getBoolean(Preferences.USE_PROFILE_IMAGE, true);
		
		if(currentType== PROGRESS_BAR) {
			pbItemView = convertView;
//			PbItemViewHolder pbItemViewHolder=null;
			if(pbItemView == null) {
				pbItemView = mInflater.inflate(R.layout.comment_refresh_item, parent, false);
				pbItemViewHolder = new PbItemViewHolder();
				
				pbItemViewHolder.comment_refresh = (RelativeLayout) pbItemView.findViewById(R.id.comment_refresh);
		        pbItemViewHolder.pb_refresh = (ProgressBar) pbItemView.findViewById(R.id.pb_refresh);
		        pbItemViewHolder.tv_title = (TextView) pbItemView.findViewById(R.id.tv_title);
		        pbItemViewHolder.tv_time = (TextView) pbItemView.findViewById(R.id.tv_time);
		        
//		        comment_refresh = (RelativeLayout) pbItemView.findViewById(R.id.comment_refresh);
//		        pb_refresh = (ProgressBar) pbItemView.findViewById(R.id.pb_refresh);
//		        tv_title = (TextView) pbItemView.findViewById(R.id.tv_title);
//		        tv_time = (TextView) pbItemView.findViewById(R.id.tv_time);
		        
		        pbItemView.setTag(pbItemViewHolder);
			}
			else {
				pbItemViewHolder = (PbItemViewHolder) pbItemView.getTag();
			}
			
			if(!pb_visiable) pbItemView.setVisibility(View.GONE);
			return pbItemView;
		}
//		if(currentType== PROGRESS_BAR) {
//			pbItemView = convertView;
//			SectionItemViewHolder pbItemViewHolder=null;
//			if(pbItemView == null) {
//				pbItemView = mInflater.inflate(R.layout.comment_section_item, parent, false);
//				pbItemViewHolder = new SectionItemViewHolder();
//				pbItemViewHolder.section_txt = (TextView) pbItemView.findViewById(R.id.section_txt);
//				
//				pbItemViewHolder.section_txt.setText("评论");
//				
//				pbItemView.setTag(pbItemViewHolder);
//			} else {
//				pbItemViewHolder = (SectionItemViewHolder) pbItemView.getTag();
//			}
//			
//			return pbItemView;
//		}
		if(currentType== SECTION_TYPE) {
			sectionItemView = convertView;
			SectionItemViewHolder sectionItemViewHolder=null;
			if(sectionItemView == null) {
				sectionItemView = mInflater.inflate(R.layout.comment_section_item, parent, false);
				sectionItemViewHolder = new SectionItemViewHolder();
				sectionItemViewHolder.section_txt = (TextView) sectionItemView.findViewById(R.id.section_txt);
				
				sectionItemViewHolder.section_txt.setText("评论");
				
				sectionItemView.setTag(sectionItemViewHolder);
			} else {
				sectionItemViewHolder = (SectionItemViewHolder) sectionItemView.getTag();
			}
			
			return sectionItemView;
		}
		if (currentType== FIRST_TYPE) { 
			firstItemView = convertView;
			final FirstItemViewHolder firstItemViewHolder;
			
			if (firstItemView == null) {
//				view = mInflater.inflate(R.layout.user_item, parent, false);
				firstItemView = mInflater.inflate(R.layout.comment_from_info_item, parent, false);
				firstItemViewHolder = new FirstItemViewHolder();
				firstItemViewHolder.info_item_header = (LinearLayout) firstItemView.findViewById(R.id.info_item_header);
				firstItemViewHolder.info_item_divider = (ImageView) firstItemView.findViewById(R.id.info_item_divider);
				firstItemViewHolder.info_item_footer = (LinearLayout) firstItemView.findViewById(R.id.info_item_footer);
				
				firstItemViewHolder.profileImage = (ImageView) firstItemView.findViewById(R.id.profile_image);
				
				firstItemViewHolder.screenName = (TextView) firstItemView.findViewById(R.id.screen_name);
				firstItemViewHolder.distanceAndCreatedAt = (TextView) firstItemView.findViewById(R.id.tweet_meta_text);
				firstItemViewHolder.down_arrow = (ImageView) firstItemView.findViewById(R.id.down_arrow);
				
				firstItemViewHolder.public_time = (TextView) firstItemView.findViewById(R.id.public_time);
				
				firstItemViewHolder.context = (TextView) firstItemView.findViewById(R.id.tweet_text);
				
				//the follow is gone temporally
				firstItemViewHolder.playBtn = (FrameLayout) firstItemView.findViewById(R.id.play_btn_layout);
				firstItemViewHolder.downloadingBtn = (FrameLayout) firstItemView.findViewById(R.id.downloading_btn_layout);
				firstItemViewHolder.stopBtn = (FrameLayout) firstItemView.findViewById(R.id.stop_btn_layout);
				firstItemViewHolder.playProgressBar = (ProgressBar) firstItemView.findViewById(R.id.play_progressbar);
				firstItemViewHolder.totalTimeText = (TextView) firstItemView.findViewById(R.id.play_total_time_text);
				firstItemViewHolder.commentCountText = (TextView) firstItemView.findViewById(R.id.comment_count_text);
				
				firstItemViewHolder.infoPicShow = ( GridView ) firstItemView.findViewById ( R.id.infoPicShow ) ;
				firstItemViewHolder.infoPicViewAdapter = new CommentPicViewAdapter(mContext);
		        
				firstItemViewHolder.info_item_footer_status = (View) firstItemView.findViewById ( R.id.info_item_footer_status ) ;
				firstItemViewHolder.expire_time = (TextView) firstItemView.findViewById ( R.id.txt_expire_time ) ;
				firstItemViewHolder.info_item_footer_reply = (View) firstItemView.findViewById ( R.id.info_item_footer_reply ) ;
				firstItemViewHolder.info_item_footer_praise = (View) firstItemView.findViewById ( R.id.info_item_footer_praise ) ;
				firstItemViewHolder.comment_count = (TextView) firstItemView.findViewById ( R.id.cnt_comment) ;
				firstItemViewHolder.praise_count = (TextView) firstItemView.findViewById ( R.id.cnt_praise ) ;
				firstItemViewHolder.btn_praise = (ImageView) firstItemView.findViewById ( R.id.btn_praise ) ;
		        
				firstItemView.setTag(firstItemViewHolder);
			} else {
				firstItemViewHolder = (FirstItemViewHolder) firstItemView.getTag();
			}
			
			firstItemViewHolder.down_arrow.setTag(position);
			firstItemViewHolder.info_item_footer_status.setTag(position);
			//final Info info = mInfos.get(position);
			final Info info = mComments.get(position).getInfoFromComment();
			
			/**** code for the local expire info ****/
//			if(info.expire>0) {
//				holder.info_item_header.setVisibility(View.GONE);
//				holder.context.setText("本消息已经到期消除");
//				holder.infoPicShow.setVisibility(View.GONE);
//				holder.info_item_divider.setVisibility(View.GONE);
//				holder.info_item_footer.setVisibility(View.GONE);
//				
//				return view;
//			}
			firstItemViewHolder.info_item_header.setVisibility(View.VISIBLE);
			firstItemViewHolder.infoPicShow.setVisibility(View.VISIBLE);
			firstItemViewHolder.info_item_divider.setVisibility(View.VISIBLE);
			firstItemViewHolder.info_item_footer.setVisibility(View.VISIBLE);
			
			String owerImageUrl = info.owerImageUrl;
			mPlayLink = info.attachmentUrl;
			//Log.d(TAG, "mPlayLink = " + mPlayLink);
			if (useProfileImage) {
				if (!TextUtils.isEmpty(owerImageUrl)) {
					firstItemViewHolder.profileImage.setImageBitmap(TwitterApplication.mImageLoader.get(owerImageUrl.toString(), callback));
				}
			} else {
				firstItemViewHolder.profileImage.setVisibility(View.GONE);
			}
			// holder.profileImage.setImageBitmap(ImageManager.mDefaultBitmap);
			
			firstItemViewHolder.screenName.setText(info.owerName);
			
			if (info.distance == -1) {
				firstItemViewHolder.distanceAndCreatedAt.setText(DateTimeHelper.getRelativeDate(info.createdAt));
				firstItemViewHolder.public_time.setText(DateTimeHelper.getRelativeDate(info.createdAt));
			} else if (info.createdAt != null) {
				firstItemViewHolder.distanceAndCreatedAt.setText(DistanceHelper.distanceConvert(info.distance) 
				        + " | " + DateTimeHelper.getRelativeDate(info.createdAt));
				firstItemViewHolder.public_time.setText(DistanceHelper.distanceConvert(info.distance) 
				        + " | " + DateTimeHelper.getRelativeDate(info.createdAt));
			} else {
				firstItemViewHolder.distanceAndCreatedAt.setText(DistanceHelper.distanceConvert(info.distance));
				firstItemViewHolder.public_time.setText(DistanceHelper.distanceConvert(info.distance));
			}
			firstItemViewHolder.context.setText(info.context);
//			holder.totalTimeText.setText(String.valueOf(user.statusAudioDuration) + "''");
			firstItemViewHolder.totalTimeText.setText("''");
			
			// Log.d(TAG, "statusInReplyToStatusId = " + user.statusInReplyToStatusId);
			/*
			if (user.statusInReplyToStatusId.equals("") || user.statusInReplyToStatusId.equals("null")) {
				holder.commentCountText.setText("评:" + String.valueOf(user.statusConversationCount-1));
			} else {
				holder.commentCountText.setText("评:" + String.valueOf(user.statusReplyCount));
			}
			*/
			firstItemViewHolder.commentCountText.setText("评:" + String.valueOf(info.conversationCount-1));
			
			firstItemViewHolder.expire_time.setText(""+ DateTimeHelper.dateToString(info.expireTime, ""));
			firstItemViewHolder.comment_count.setText(String.valueOf(info.conversationCount));
			firstItemViewHolder.praise_count.setText(String.valueOf(info.praiseCount));
			if(info.user_praise==1) {
				firstItemViewHolder.btn_praise.setBackgroundResource(R.drawable.fav_en_dark);
			} else {
				firstItemViewHolder.btn_praise.setBackgroundResource(R.drawable.fav_un_dark);
			}

			
			if (mButtonSelections.get(position) == 1) {
				firstItemViewHolder.playBtn.setVisibility(View.VISIBLE);
				firstItemViewHolder.downloadingBtn.setVisibility(View.GONE);
				firstItemViewHolder.stopBtn.setVisibility(View.GONE);
			} else if (mButtonSelections.get(position) == 2) {
				firstItemViewHolder.downloadingBtn.setVisibility(View.VISIBLE);
				firstItemViewHolder.playBtn.setVisibility(View.GONE);
				firstItemViewHolder.stopBtn.setVisibility(View.GONE);
			} else if (mButtonSelections.get(position) == 3) {
				firstItemViewHolder.stopBtn.setVisibility(View.VISIBLE);
				firstItemViewHolder.playBtn.setVisibility(View.GONE);
				firstItemViewHolder.downloadingBtn.setVisibility(View.GONE);
			}
			firstItemViewHolder.playProgressBar.setMax(10000);
			//holder.playProgressBar.setProgress(mProgressbarPercent.get(position));
			
			//点击头像进入用户信息页面
			firstItemViewHolder.profileImage.setOnClickListener(new OnClickListener() {
			    @Override
				public void onClick(View v) {
			    	Log.e(TAG, "holder.profileImage position: "+String.valueOf(position));
			    	// launchActivity(ProfileActivity.createIntent(user.id));
//					launchActivity(ProfileActivity.createIntent(user));
//					launchActivity(ProfileActivity.createIntent(info));
				}
			});
			
			firstItemViewHolder.down_arrow.setOnClickListener(this);
			firstItemViewHolder.info_item_footer_status.setOnClickListener(this);
			//播放按钮
			firstItemViewHolder.playBtn.setOnClickListener(new OnClickListener() {
			    @Override
				public void onClick(View v) {
				    mPlayLink = info.attachmentUrl;
					String fileName = mPlayLink.substring(mPlayLink.lastIndexOf("/") + 1);

				    //Log.d(TAG, "position " + position);
					//lastPosition = curPosition;
					curPosition = position;
					
					//lastView = curView;
					//curView = v;
					
//					FirstItemViewHolder itemHolder = firstItemViewHolder;
					
					if (mState == PLAYING_STATE) {  //其中一个item正在播放，需要先停止这个item
//					    stopPlay();
					}
					
					//if (mPositionViewHolder.containsKey(position)) {  //正在下载相同position的语音
				    if (mPositionDownloadingList.contains(position)) {
						//因为已经在下载所以仅仅切换按钮就可以
						mButtonSelections.set(position, 2);
				        notifyDataSetChanged();	
					} else {
						File downloadFile = new File(mDownloadDir.getAbsolutePath() + "/" + fileName);
						if (downloadFile.exists()) {  //语音已经存在，直接播放
//						    mPlayingHolder = itemHolder;
//							startPlay(position, downloadFile.getAbsolutePath());
						} else {  //语音不存在，需要下载
//							downloadAudio(position, fileName, mPlayLink);
						}
					}
				}
			});
			
			//正在下载
			firstItemViewHolder.downloadingBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
				    //Log.d(TAG, "position " + position);
					curPosition = position;
					mButtonSelections.set(position, 1);
				    notifyDataSetChanged();	
				}
			});
			
			//停止按钮
			firstItemViewHolder.stopBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
				    //Log.d(TAG, "position " + position);
					curPosition = position;
					//curView = v;
//					stopPlay();
				}
			});
			
			//picShow in info
	        final ArrayList<String> picDataList = info.getPicPathList();
//	        if(available && picDataList != null && (!picDataList.isEmpty())) {
	        if(picDataList != null && (!picDataList.isEmpty())) {
	        	firstItemViewHolder.infoPicShow.setVisibility(View.VISIBLE);
//	            holder.commentPicViewAdapter = new CommentPicViewAdapter(mContext, picDataList);
	        	firstItemViewHolder.infoPicViewAdapter.setData(picDataList);
	        	firstItemViewHolder.infoPicShow.setAdapter(firstItemViewHolder.infoPicViewAdapter);
	        	firstItemViewHolder.infoPicViewAdapter.refresh();
	        } else {
	        	firstItemViewHolder.infoPicShow.setVisibility(View.GONE);
	        }
	        //for comment picture full Image PopupWindow
	        final GridView mGrideView = firstItemViewHolder.infoPicShow;
	        mGrideView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					/**直接弹出popupwindow**/
//			        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//			        final View mView = inflater.inflate(R.layout.image_select_full_shower, null);
//			        
//					final SelectForFullImagePopupWindow mPopupWindow;
//					mPopupWindow = new SelectForFullImagePopupWindow(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
//							picDataList.get(position), mView, false, true);
//					mPopupWindow.setOutsideTouchable(true);
//					mPopupWindow.setFocusable(true);
//					mPopupWindow.findViewById(R.id.image_checker).setVisibility(View.GONE);
//					final View headView = ((Activity) mContext).findViewById(R.id.title);
//					mPopupWindow.showAsDropDown(headView);
					
					Intent intent=new Intent((Activity) mContext, MatrixImageActivity.class);
					Bundle bundle=new Bundle();
					intent.putExtra("position", position);
					intent.putExtra("style", MatrixImageActivity.HEADER_ONLY_HEADER_BACK);
					intent.putExtra("local_image", false);
					intent.putStringArrayListExtra("datalist", getIntentArrayList(picDataList));
					intent.putExtras(bundle);
					launchActivity(intent);
//					startActivityForResult(intent, MATRIX_IMAGE_PREVIEW);
				}
			});
	        
	        firstItemViewHolder.info_item_footer_reply.setOnClickListener(new OnClickListener() {
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
	        
	        firstItemViewHolder.info_item_footer_praise.setOnClickListener(new OnClickListener() {
	        	@Override
	        	public void onClick(View v) {
	        		toggle_praise(info.id, info.user_praise, firstItemViewHolder.praise_count, firstItemViewHolder.btn_praise); //temp false
	        	}
	        });
	        
			return firstItemView;
		}
		
		if (view == null) {
			view = mInflater.inflate(R.layout.comment_list_item, parent, false);
			
			holder = new ViewHolder();
			
	        holder.floor_date = ( TextView ) view.findViewById ( R.id.floor_date ) ; 
	        holder.floor_username = ( TextView ) view.findViewById ( R.id.floor_username ) ;
	        holder.floor_content = ( TextView ) view.findViewById ( R.id.floor_content ) ;
	        
	        holder.commentPicShow = ( GridView ) view.findViewById ( R.id.commentPicShow ) ;
	        holder.commentPicViewAdapter = new CommentPicViewAdapter(mContext);
	        
	        holder.subFloors = ( FloorView ) view.findViewById ( R.id.sub_floors ) ;
	        
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}

//		final Comment cmt = mComments.get(position);
		Comment cmt = mComments.get(position);
		
		boolean available = cmt.isAvailable();
		if(!available) {
			holder.floor_date.setText ( "0000-00-00 08:00:00" ) ;
			holder.floor_username.setText ( "移民火星" ) ;
//			SpannableString spannableString = EmojiConversionUtil.getInstace().getExpressionString(context, entity.getText());
			holder.floor_content.setText ( "此评论已经被黑洞吸收" ) ;
			holder.floor_content.setAlpha((float) 0.3);
		} else {
			holder.floor_date.setText ( DateFormatUtils.format ( cmt.getDate () ) ) ;
			holder.floor_username.setText ( cmt.getUserName () ) ;
//			SpannableString spannableString = EmojiConversionUtil.getInstace().getExpressionString(context, entity.getText());
			holder.floor_content.setText ( cmt.getContent () ) ;
			holder.floor_content.setAlpha((float) 1.0);
		}
		
        if (cmt.getParentId()!=Comment.NULL_PARENT && cmt.getParentId()>0) {
        	SubComments cmts = new SubComments ( addSubFloors_1 ( cmt.getParentId ()) ) ;
        	
        	Log.v("cmt.id", String.valueOf(cmt.getId()));
        	Log.v("debug list size", String.valueOf(cmts.size()));
        	
        	holder.subFloors.setComments ( cmts ) ;
        	holder.subFloors.setFactory ( new SubFloorFactory() ) ;
        	holder.subFloors.setBoundDrawer ( mContext.getResources ().getDrawable ( R.drawable.bound ) ) ;
        	holder.subFloors.init () ;
        	holder.subFloors.setVisibility ( View.VISIBLE ) ;
        } else {
        	holder.subFloors.setVisibility ( View.GONE ) ;
        }
		
        final ArrayList<String> picDataList = mComments.get(position).getPicPath();
        if(available && picDataList != null && (!picDataList.isEmpty())) {
        	holder.commentPicShow.setVisibility(View.VISIBLE);
//            holder.commentPicViewAdapter = new CommentPicViewAdapter(mContext, picDataList);
        	holder.commentPicViewAdapter.setData(picDataList);
            holder.commentPicShow.setAdapter(holder.commentPicViewAdapter);
            holder.commentPicViewAdapter.refresh();
        } else {
        	holder.commentPicShow.setVisibility(View.GONE);
        }
		
        //for comment picture full Image PopupWindow
        final GridView mGrideView = holder.commentPicShow;
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

		return view;
	}
	
//	public void doGetCommentPic(int commentId, CommentPicViewAdapter Adapter) {
//		ShowCommentPicTask showCommentPicTask = new ShowCommentPicTask(commentId, Adapter);
//		TaskParams params = new TaskParams();
//		params.put("commentId", commentId);
//		params.put("adapter", Adapter);
//		showCommentPicTask.execute(params);
//	}
//	
//	private class ShowCommentPicTask extends AsyncTask<TaskParams, Object, TaskResult> {
//		
//		private Integer commentId;
//		private CommentPicViewAdapter Adapter;
//		
//		public ShowCommentPicTask(Integer commentId, CommentPicViewAdapter Adapter) {
//			super();
//			this.commentId = commentId;
//			this.Adapter = Adapter;
//		}
//	    
//		@Override
//		protected TaskResult doInBackground(TaskParams... params) {
//		    TaskParams param = params[0];
//			try {
//				int commentId = param.getInt("commentId");
////			    String audioURL = param.getString("audio_url");
//				if (commentId>0) {
//				    InputStream is = getInputStream(audioURL);
//				    File downloadFile = new File(mDownloadDir.getAbsolutePath() + "/" + fileName);
//				    downloadFile.createNewFile();
//					OutputStream out = null;
//					out = new FileOutputStream(downloadFile);
//					int temp = 0;
//			        byte[] data = new byte[1024];
//			        while((temp = is.read(data))!=-1){
//				        out.write(data, 0, temp);
//			        }
//			        out.flush();
//			        is.close();
//			        out.close();
//				}
//			} catch (HttpException e) {
//				Log.e(TAG, e.getMessage(), e);
//				return TaskResult.IO_ERROR;
//			} catch (IOException e) {
//				Log.e(TAG, e.getMessage(), e);
//				return TaskResult.IO_ERROR;
//			} 
//			return TaskResult.OK;
//		}
//		
//		@Override
//		protected void onPreExecute() {
//			super.onPreExecute();
//			//mPositionViewHolder.put(position, holder);
//			//holder.playBtn.setVisibility(View.GONE);
//			//holder.downloadingBtn.setVisibility(View.VISIBLE);
//			mPositionDownloadingList.add(position);
//			mButtonSelections.set(position, 2);
//			notifyDataSetChanged();
//		}
//		
//		@Override
//		protected void onPostExecute(TaskResult result) {
//			super.onPostExecute(result);
//			
//			//mPositionViewHolder.remove(position);
//			mPositionDownloadingList.remove(position);
//			
//			if (result == TaskResult.AUTH_ERROR) {
//				
//            } else if (result == TaskResult.OK) {
//            	if ((position == curPosition) && (mButtonSelections.get(position) == 2)) { 
//            		//holder.downloadingBtn.setVisibility(View.GONE);
//            		//holder.playBtn.setVisibility(View.VISIBLE);
//            		mButtonSelections.set(position, 3);
//            		notifyDataSetChanged();
//            		startPlay(position, downloadFilePath);
//            	} else {
//            		//holder.downloadingBtn.setVisibility(View.GONE);
//            		//holder.playBtn.setVisibility(View.VISIBLE);
//            		mButtonSelections.set(position, 1);
//            		notifyDataSetChanged();
//            	}
//            } else if (result == TaskResult.IO_ERROR) {
//                
//            } else {
//                // do nothing
//            }
//		}
//		
//		@Override
//		protected void onProgressUpdate(Object... values) {
//			super.onProgressUpdate(values);
//		}
//	}
	
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
	
	private void toggle_praise(String info_id, int nowAvailable, final TextView praise_count, final ImageView btn_praise) {

		if (mTogglePraiseTask != null && mTogglePraiseTask.getStatus() == AsyncTask.Status.RUNNING) {
			return;
		} else {
		
			mTogglePraiseTask = new CommonTask.TogglePraiseTask(info_id, nowAvailable, praise_count, btn_praise);
			TaskParams params = new TaskParams();
//			params.put("info_id", info_id);
//			params.put("is_available", nowAvailable);
			mTogglePraiseTask.execute(params);
		}
	}
	
    private List < Comment > addSubFloors_1 ( long parentId) {
    	Comment my_cmt;
    	ArrayList<Comment> my_cmts = new ArrayList<Comment>();
    	long my_id;
    	int flag=0;
    	
    	my_id = parentId;
    	while(my_id!=Comment.NULL_PARENT && my_id>0) {
    		flag=0;
    		for(Comment cmt : mComments) {
    			if(cmt.getId()==my_id) {
    				my_cmt = new Comment(cmt);
    				my_id = my_cmt.getParentId();
    				my_cmts.add(my_cmt);
    				flag=1;
    				break;
    			}
    		}
    		if(flag==0) break;
    	}
    	Collections.reverse(my_cmts);
        return my_cmts ;
    }
	
	public void refresh(ArrayList<Comment> comments) {
		mComments = (ArrayList<Comment>) comments.clone();
		
		Log.v("adapter", String.valueOf(mComments.size()));
		
		for (int i=0; i<mComments.size(); i++) {
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
	
	protected void launchActivity(Intent intent) {
		mContext.startActivity(intent);
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
	
	public void setBpItemView(boolean v) {
		pb_visiable = v;
		if(pbItemView!=null&&pbItemViewHolder!=null) {
			if(v) {
				pbItemViewHolder.comment_refresh.setVisibility(View.VISIBLE);
				pbItemViewHolder.pb_refresh.setVisibility(View.VISIBLE);
				pbItemViewHolder.tv_title.setVisibility(View.VISIBLE);
				pbItemViewHolder.tv_time.setVisibility(View.VISIBLE);
				pbItemView.setVisibility(View.VISIBLE);

				pbItemViewHolder.tv_title.setText(R.string.refreshing);
	            //pbItemView.setPadding(0, 0, 0, 0);
			} else {
				pbItemViewHolder.comment_refresh.setVisibility(View.GONE);
				pbItemViewHolder.pb_refresh.setVisibility(View.GONE);
				pbItemViewHolder.tv_title.setVisibility(View.GONE);
				pbItemViewHolder.tv_time.setVisibility(View.GONE);
				pbItemViewHolder.tv_title.setText(R.string.refreshing);
				pbItemView.setVisibility(View.GONE);

	            pbItemView.setPadding(0, 0, 0, 0);
			}
		}
	}
	
}

