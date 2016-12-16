package com.codeim.coxin.ui.module;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
// import android.text.TextPaint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
// import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.codeim.coxin.InfoMapActivity;
import com.codeim.coxin.NearbyActivity;
import com.codeim.coxin.WriteInfoActivity;
import com.codeim.floorview.CommentPinnedSectionActivity;
//import com.codeim.coxin.R;
//import com.codeim.coxin.SearchActivity;
//import com.codeim.coxin.TwitterActivity;
//import com.codeim.coxin.SoundRecorderActivity;
//import com.codeim.coxin.FriendActivityGroup;
//import com.codeim.coxin.ChannelActivityGroup;
//import com.codeim.coxin.AddInfoActivity;

// import com.codeim.coxin.ui.base.Refreshable;

import com.codeim.floorview.CommentWriteActivity;
import com.codeim.coxin.R;

public class NavBar implements Widget {
	private static final String TAG = "NavBar";

	public static final int HEADER_STYLE_HOME = 1;
	public static final int HEADER_STYLE_WRITE = 2;
	public static final int HEADER_STYLE_TITLE = 3;
	public static final int HEADER_STYLE_BACK = 4;
	public static final int HEADER_STYLE_SEARCH = 5;

	//private ImageView mRefreshButton;
	private ImageButton mSearchButton;
	//private ImageButton mWriteButton;
	//private TextView mTitleButton;
	private TextView mTitleText;
	private ImageButton mNewRecordBtn;
//	private Button mBackButton;
	private ImageButton mBackButton;
	private ImageButton mHomeButton;
	private MenuDialog mDialog;
	private EditText mSearchEdit;
	private ImageButton mAddButton;
	private ImageButton mOverflowButton;
	
	private int mCategory = 0;  // 语音分类
	private Activity mActivity;

	/** @deprecated 已废弃 */
	protected AnimationDrawable mRefreshAnimation;

	private ProgressBar mProgressBar = null; // 进度条(横)
	private ProgressBar mLoadingProgress = null; // 旋转图标

	public NavBar(int style, Context context) {
		mActivity=(Activity) context;
		initHeader(style, (Activity) context);
	}

	private void initHeader(int style, final Activity activity) {
		switch (style) {
		case HEADER_STYLE_HOME:
			addTitleButtonTo(activity);
			addSearchButtonTo(activity);
			addAddButtonTo(activity);
			addOverflowButtonTo(activity);
//			addWriteButtonTo(activity);
			//addSearchButtonTo(activity);
			//addRefreshButtonTo(activity);
			break;
		case HEADER_STYLE_BACK:
			addBackButtonTo(activity);
			addTitleButtonTo(activity);
			//addWriteButtonTo(activity);
			//addSearchButtonTo(activity);
			//addRefreshButtonTo(activity);
			break;
		case HEADER_STYLE_WRITE:
		    addTitleButtonTo(activity);
			addBackButtonTo(activity);
			addWriteButtonTo(activity);
			break;
		case HEADER_STYLE_TITLE:
		    addTitleButtonTo(activity);
			break;
		case HEADER_STYLE_SEARCH:
			addBackButtonTo(activity);
			addSearchBoxTo(activity);
			//addSearchButtonTo(activity);
			break;
		}
	}

	/**
	 * 搜索硬按键行为
	 * 
	 * @deprecated 这个不晓得还有没有用, 已经是已经被新的搜索替代的吧 ?
	 */
	public boolean onSearchRequested() {
		/*
		 * Intent intent = new Intent(); intent.setClass(this,
		 * SearchActivity.class); startActivity(intent);
		 */
		return true;
	}

	/**
	 * 添加[LOGO/标题]按钮
	 * 
	 * @param acticity
	 */
	private void addTitleButtonTo(final Activity acticity) {
	    mTitleText = (TextView) acticity.findViewById(R.id.title_bar_text);
	    
	    mTitleText.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			    if (mActivity.getLocalClassName().equals("NearbyActivity")) {
					Intent intent = new Intent((NearbyActivity)mActivity, InfoMapActivity.class);
					
					Bundle bundle=new Bundle();
					bundle.putDouble("latitude", ((NearbyActivity)mActivity).getNowLat());
					bundle.putDouble("longitude", ((NearbyActivity)mActivity).getNowLng());
					bundle.putString("name", ((NearbyActivity)mActivity).getNameTxt());
					intent.putExtras(bundle);
					((NearbyActivity)mActivity).startActivityForResult(intent, 400);
				}
			}
		});
	}

	/**
	 * 设置标题
	 * 
	 * @param title
	 */
	public void setHeaderTitle(String title) {
		if (null != mTitleText) {
			mTitleText.setText(title);
			//TextPaint tp = mTitleText.getPaint();
			//tp.setFakeBoldText(true); // 中文粗体
		}
	}

	/**
	 * 设置标题
	 * 
	 * @param resource
	 *            R.string.xxx
	 */
	
	/*
	public void setHeaderTitle(int resource) {
		if (null != mTitleButton) {
			mTitleButton.setBackgroundResource(resource);
		}
	}
	*/

	/**
	 * 添加[刷新]按钮
	 * 
	 * @param activity
	 */
	private void addRefreshButtonTo(final Activity activity) {
		//mRefreshButton = (ImageView) activity.findViewById(R.id.top_refresh);
		

		mProgressBar = (ProgressBar) activity.findViewById(R.id.progress_bar);
		mLoadingProgress = (ProgressBar) activity.findViewById(R.id.top_refresh_progressBar);

		/*
		mRefreshButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				if (activity instanceof Refreshable) {
					((Refreshable) activity).doRetrieve();
				} else {
					Log.e(TAG, "The current view " + activity.getClass().getName() + " cann't be retrieved");
				}
			}

		});
		*/
	}

	/**
	 * Start/Stop Top Refresh Button's Animation
	 * 
	 * @param animate
	 *            start or stop
	 * @deprecated use feedback
	 */
	public void setRefreshAnimation(boolean animate) {
		if (mRefreshAnimation != null) {
			if (animate) {
				mRefreshAnimation.start();
			} else {
				mRefreshAnimation.setVisible(true, true); // restart
				mRefreshAnimation.start(); // goTo frame 0
				mRefreshAnimation.stop();
			}
		} else {
			Log.w(TAG, "mRefreshAnimation is null");
		}
	}

	/**
	 * 添加[搜索]按钮
	 * 
	 * @param activity
	 */
	private void addSearchButtonTo(final Activity activity) {
		mSearchButton = (ImageButton) activity.findViewById(R.id.search);
		mSearchButton.setVisibility(View.VISIBLE);
		
		mSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startSearch(activity);
			}
		});
	}

	// 这个方法会在SearchActivity里重写
	protected boolean startSearch(final Activity activity) {
//		Intent intent = new Intent();
//		intent.setClass(activity, SearchActivity.class);
//		activity.startActivity(intent);
		return true;
	}

	/**
	 * 添加[搜索框]
	 * 
	 * @param activity
	 */
	private void addSearchBoxTo(final Activity activity) {
		mSearchEdit = (EditText) activity.findViewById(R.id.search_edit);
	}

	/**
	 * 添加[增加]按钮
	 * 
	 * @param activity
	 */
	private void addAddButtonTo(final Activity activity) {

		mAddButton = (ImageButton) activity.findViewById(R.id.title_add_btn);
		mAddButton.setVisibility(View.VISIBLE);
		
		mAddButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// forward to add info activity
				Intent intent = new Intent();
//				intent.setClass(v.getContext(), AddInfoActivity.class);
				intent.setClass(v.getContext(), WriteInfoActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("category", mCategory);
				intent.putExtras(bundle);
				v.getContext().startActivity(intent);
			}
		});
	}
	
	/**
	 * 添加overflow按钮
	 * 
	 * @param activity
	 */
	private void addOverflowButtonTo(final Activity activity) {

		mOverflowButton = (ImageButton) activity.findViewById(R.id.new_record_btn);
		mOverflowButton.setVisibility(View.VISIBLE);
		
//		mNewRecordBtn.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				// forward to write activity
//				Intent intent = new Intent();
//				intent.setClass(v.getContext(), SoundRecorderActivity.class);
//				Bundle bundle = new Bundle();
//				bundle.putInt("category", mCategory);
//				intent.putExtras(bundle);
//				v.getContext().startActivity(intent);
//			}
//		});
		
		mOverflowButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
			    if (mActivity.getLocalClassName().equals("NearbyActivity")) {
					Intent intent = new Intent((NearbyActivity)mActivity, InfoMapActivity.class);
					
					Bundle bundle=new Bundle();
					//intent.putExtra("parentid", 0);
					//intent.putExtra("floornum", 0);
					intent.putExtras(bundle);
					
					((NearbyActivity)mActivity).startActivityForResult(intent, 400);
				}
			}
		});
	}
	
	/**
	 * 添加[撰写]按钮
	 * 
	 * @param activity
	 */
	private void addWriteButtonTo(final Activity activity) {

	    mNewRecordBtn = (ImageButton) activity.findViewById(R.id.new_record_btn);
		mNewRecordBtn.setVisibility(View.VISIBLE);
		
//		mNewRecordBtn.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				// forward to write activity
//				Intent intent = new Intent();
//				intent.setClass(v.getContext(), SoundRecorderActivity.class);
//				Bundle bundle = new Bundle();
//				bundle.putInt("category", mCategory);
//				intent.putExtras(bundle);
//				v.getContext().startActivity(intent);
//			}
//		});
	}

	/**
	 * 添加[回首页]按钮
	 * 
	 * @param activity
	 */
	@SuppressWarnings("unused")
	private void addHomeButton(final Activity activity) {
		mHomeButton = (ImageButton) activity.findViewById(R.id.home);

//		mHomeButton.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				// 动画
//				Animation anim = AnimationUtils.loadAnimation(v.getContext(), R.anim.scale_lite);
//				v.startAnimation(anim);
//
//				// forward to TwitterActivity
//				Intent intent = new Intent();
//				intent.setClass(v.getContext(), TwitterActivity.class);
//				v.getContext().startActivity(intent);
//
//			}
//		});
	}

	/**
	 * 添加[返回]按钮
	 * 
	 * @param activity
	 */
	private void addBackButtonTo(final Activity activity) {
		mBackButton = (ImageButton) activity.findViewById(R.id.back_btn);
		mBackButton.setVisibility(View.VISIBLE);
		// 中文粗体
		// TextPaint tp = backButton.getPaint();
		// tp.setFakeBoldText(true);

		mBackButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Go back to previous activity
				if (mActivity.getLocalClassName().equals("FollowingActivity")) {
					//((FriendActivityGroup) activity.getParent()).back();
				} else if (mActivity.getLocalClassName().equals("FollowersActivity")) {
				    //((FriendActivityGroup) activity.getParent()).back();
				} else if (mActivity.getLocalClassName().equals("TwitterActivity")) {
				    //((ChannelActivityGroup) activity.getParent()).back();
				} else if (mActivity.getLocalClassName().equals("CategoryActivity")) {
				    //((ChannelActivityGroup) activity.getParent()).back();
				} else if (mActivity.getLocalClassName().equals("com.codeim.floorview.CommentPinnedSectionActivity")) {
//				} else if (mActivity.getLocalClassName().contains("CommentPinnedSectionActivity")) {
					Intent intent = new Intent();
					
					intent.putExtra("infoId", ((CommentPinnedSectionActivity)mActivity).getInfoId());
					mActivity.setResult(Activity.RESULT_OK, intent);
					mActivity.finish();
				} else {
					mActivity.finish();
				}
			}
		});
	}

	public void destroy() {
		// dismiss dialog before destroy
		// to avoid android.view.WindowLeaked Exception
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
		//mRefreshButton = null;
		mSearchButton = null;
		//mWriteButton = null;
		//mTitleButton = null;
		mBackButton = null;
		mHomeButton = null;
		mSearchButton = null;
		mSearchEdit = null;
		mProgressBar = null;
		mLoadingProgress = null;
	}

	/*
	public ImageView getRefreshButton() {
		return mRefreshButton;
	}
	*/

	public ImageButton getSearchButton() {
		return mSearchButton;
	}

	/*
	public ImageButton getWriteButton() {
		return mWriteButton;
	}

	public TextView getTitleButton() {
		return mTitleButton;
	}
	*/

//	public Button getBackButton() {
//		return mBackButton;
//	}
	public ImageButton getBackButton() {
	return mBackButton;
}

	public ImageButton getHomeButton() {
		return mHomeButton;
	}

	public MenuDialog getDialog() {
		return mDialog;
	}

	public EditText getSearchEdit() {
		return mSearchEdit;
	}

	/** @deprecated 已废弃 */
	public AnimationDrawable getRefreshAnimation() {
		return mRefreshAnimation;
	}

	public ProgressBar getProgressBar() {
		return mProgressBar;
	}

	public ProgressBar getLoadingProgress() {
		return mLoadingProgress;
	}
	
	public void setCategory(int category) {
	    mCategory = category;
	}

	@Override
	public Context getContext() {
		if (null != mDialog) {
			return mDialog.getContext();
		}
		/*
		if (null != mTitleButton) {
			return mTitleButton.getContext();
		}
		*/
		return null;
	}
}
