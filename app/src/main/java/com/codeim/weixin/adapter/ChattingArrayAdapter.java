package com.codeim.weixin.adapter;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.LazyImageLoader.ImageLoaderCallback;
import com.codeim.coxin.data.ChatMsg;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.http.HttpClient;
import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.coxin.R;

/*
 * 用于用户的Adapter
 */
public class ChattingArrayAdapter extends BaseAdapter implements TweetAdapter, OnCompletionListener, OnErrorListener, OnClickListener {
	private static final String TAG = "ChattingArrayAdapter";
	public static final String DOWNLOAD_DEFAULT_DIR = "/coxin/download";
	protected static final int REQUEST_CODE_LAUNCH_ACTIVITY = 0;
	
	public static final int IDLE_STATE = 0;
	public static final int PLAYING_STATE = 1;
	public static final int DOWNLOADING_STATE = 2;
	
	private int mState = IDLE_STATE;

	protected ArrayList<ChatMsg> mChatMsgs;
	private Context mContext;
	protected LayoutInflater mInflater;
	private String myself;
	
	private String myselfImageUrl;
	private String otherImageUrl;
	
	private MediaPlayer mMediaPlayer = new MediaPlayer();
	private File mDownloadDir = null;
	private HttpClient mClient;
	//private final Handler mHandler = new Handler();
	
    public static interface IMsgViewType {  
        int IMVT_COM_MSG = 0;  
        int IMVT_TO_MSG = 1;  
    }
	
//	private Runnable mUpdateSeekBar = new Runnable() {
//        @Override
//        public void run() {
//		    if (mPlayingPosition != null) {
//                //updateProgressBar(mPlayingPosition);
//		    }
//        }
//    };
	
	private ImageLoaderCallback callback = new ImageLoaderCallback() {
		@Override
		public void refresh(String url, Bitmap bitmap) {
			ChattingArrayAdapter.this.refresh();
		}
	};

	public ChattingArrayAdapter(Context context, String other_ImageUrl) {
		mChatMsgs = new ArrayList<ChatMsg>();
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		
		myself = TwitterApplication.getMyselfId(false);
		otherImageUrl = other_ImageUrl;
		myselfImageUrl = TwitterApplication.getMyselfImgURL(false);

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
		return mChatMsgs.size();
	}

	@Override
	public Object getItem(int position) {
		return mChatMsgs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
    public int getItemViewType(int position) {  
        // TODO Auto-generated method stub  
        ChatMsg entity = mChatMsgs.get(position);  
  
        if (entity.masterId.equals(myself)) {
            return IMsgViewType.IMVT_COM_MSG;  
        } else {  
            return IMsgViewType.IMVT_TO_MSG;  
        }  
  
    }
    public int getViewTypeCount() {
        // TODO Auto-generated method stub  
        return 2;  
    }  

	private static class ViewHolder {
        public TextView tvSendTime;
        public ImageView iv_userhead;
        public TextView tvUserName;
        public TextView tvContent;
        public ImageView tv_chatimage;
        public TextView tvTime;
        public boolean isComMsg = true;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;

		SharedPreferences pref = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(mContext);
//		boolean useProfileImage = pref.getBoolean(Preferences.USE_PROFILE_IMAGE, true);
		
		final ChatMsg entity = mChatMsgs.get(position); 
		final ViewHolder viewHolder;
		final boolean isComMsg = entity.masterId.equals(myself);
		
        if (convertView == null) {  
            if (isComMsg) {  
                convertView = mInflater.inflate(R.layout.chatting_item_msg_left, null);
            } else {  
                convertView = mInflater.inflate(R.layout.chatting_item_msg_right, null);
            }  
  
            viewHolder = new ViewHolder();
            viewHolder.tvSendTime = (TextView) convertView.findViewById(R.id.tv_sendtime);
            viewHolder.iv_userhead = (ImageView) convertView.findViewById(R.id.iv_userhead);
            viewHolder.tvUserName = (TextView) convertView.findViewById(R.id.tv_username);  
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.tv_chatcontent); 
            viewHolder.tv_chatimage = (ImageView) convertView.findViewById(R.id.tv_chatimage);
//            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
            viewHolder.isComMsg = isComMsg;
  
            convertView.setTag(viewHolder);  
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }  
  
        viewHolder.tvSendTime.setText(entity.chatMsgTime.toString());
        if(isComMsg) {
        	viewHolder.iv_userhead.setImageBitmap(TwitterApplication.mImageLoader.get(this.otherImageUrl, callback));
        } else {
        	viewHolder.iv_userhead.setImageBitmap(TwitterApplication.mImageLoader.get(this.myselfImageUrl, callback));
        }
//        viewHolder.tvUserName.setText(entity);
        //if (entity.content.contains(".amr")) {
        if (entity.msgType==2) { //audio
        	viewHolder.tvContent.setVisibility(View.VISIBLE);
        	viewHolder.tv_chatimage.setVisibility(View.GONE);
        	
            viewHolder.tvContent.setText("");  
            viewHolder.tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.chatto_voice_playing, 0);  
            viewHolder.tvTime.setText(entity.chatMsgTime.toString());  
        } else if(entity.msgType==1) { //picture
        	viewHolder.tvContent.setVisibility(View.GONE);
        	viewHolder.tv_chatimage.setVisibility(View.VISIBLE);
        	
        	if(isComMsg) {
        		String downloadUrl=entity.content;
        		if (downloadUrl.contains("default")) {
        			viewHolder.tv_chatimage.setImageResource(R.drawable.bg_userheader_cover);
        		} else {
        			//TwitterApplication.mImageManager.displayImage(viewHolder.imageView,
        			//		path, R.drawable.bg_userheader_cover, 100, 100);
        			Bitmap bm = TwitterApplication.mImageLoader.get(downloadUrl, callback);
        			viewHolder.tv_chatimage.setImageBitmap(bm);
        		}
        	} else {
        		String path=android.os.Environment.getExternalStorageDirectory() + "/" + entity.content;
        		File localFile = new File(path);
        		if (localFile.exists()) {
        			TwitterApplication.mImageManager.displayImage(viewHolder.tv_chatimage,
        					path, R.drawable.bg_userheader_cover, false);
        		} else {
        		}
        	}
        	
        } else if(entity.msgType==3) { //video
        	viewHolder.tvContent.setVisibility(View.VISIBLE);
        	viewHolder.tv_chatimage.setVisibility(View.GONE);
        	
        } else if(entity.msgType==0) { //text
        	viewHolder.tvContent.setVisibility(View.VISIBLE);
        	viewHolder.tv_chatimage.setVisibility(View.GONE);
        	
            viewHolder.tvContent.setText(entity.content);           
            viewHolder.tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);  
            viewHolder.tvTime.setText("");  
        } else {
        }
        
        viewHolder.tvContent.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (entity.msgType==2) {  //audio
                	if(isComMsg) {
                		String downloadAudio = mDownloadDir.getAbsolutePath() + "/" + entity.content;
                    	//String localAudio = android.os.Environment.getExternalStorageDirectory() + "/" + entity.content;
                    	File downloadFile = new File(downloadAudio);
    					if (downloadFile.exists()) {  //语音已经存在，直接播放
    						playMusic(downloadAudio);
    					} else {  //语音不存在，需要下载
    						//downloadAudio(position, fileName, mPlayLink);
    					}
    					//playMusic(android.os.Environment.getExternalStorageDirectory()+"/"+entity.getText()) ;
                	} else {
                    	//File downloadFile = new File(mDownloadDir.getAbsolutePath() + "/" + entity.content);
                    	String localAudio = android.os.Environment.getExternalStorageDirectory() + "/" + entity.content;
                    	File downloadFile = new File(localAudio);
    					if (downloadFile.exists()) {  //语音已经存在，直接播放
    						playMusic(localAudio);
    					} else {  //语音不存在，需要下载
    						//downloadAudio(position, fileName, mPlayLink);
    					}
    					//playMusic(android.os.Environment.getExternalStorageDirectory()+"/"+entity.getText()) ;
                	}

                }
            }  
        });
          
        return convertView;
	}
	
	protected void launchActivity(Intent intent) {
		mContext.startActivity(intent);
	}
	
	public void refresh(ArrayList<ChatMsg> chatMsgs) {
		mChatMsgs = (ArrayList<ChatMsg>) chatMsgs.clone();
		
//		for (int i=0; i<mChats.size(); i++) {
//			//Log.d(TAG, "i = " + i);
//			//mButtonStatuses.get(i).put(key, value);
//			mButtonSelections.add(1);
//			mProgressbarPercent.add(0);
//		}
		
		notifyDataSetChanged();
	}

	@Override
	public void refresh() {
		notifyDataSetChanged();
	}
	
	public Weibo getApi() {
		return TwitterApplication.mApi;
	}

//	private OnTimeSet mOnTimeSet;
//	public void setOnTimeSet(OnTimeSet l) {
//		mOnTimeSet = l;
//	}
//	public interface OnTimeSet {
//		public void clickTimeSet(int position);
//	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub
		
	}

//	private OnClickChatItem mOnClickChatItem;
//	public void setOnClickItem(OnClickChatItem l) {
//		mOnClickChatItem = l;
//	}
//	public interface OnClickChatItem {
//		public void doChatItemClick(int position, int mType);
//	}
	
	@Override
	public void onClick(View v) {
//		// TODO Auto-generated method stub
//		int position = (Integer) v.getTag();
//		mOnClickChatItem.doChatItemClick(position, mType);
	}
	
	/**
	 * @Description
	 * @param name
	 */
	private void playMusic(String name) {
		try {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.reset();
			mMediaPlayer.setDataSource(name);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {

				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
