package com.codeim.byme.base;

import java.util.Observable;
import java.util.Observer;

import com.codeim.byme.base.BaseFragmentActivity;
import com.codeim.byme.base.OnFragmentActivityTouchListener;
import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.hardware.ShakeListener;
import com.codeim.coxin.ui.module.FlingGestureListener;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class BaseFragment extends Fragment{
	static final String TAG = "BaseFragment";
	private String TYPE;
	private static final String SIS_RUNNING_KEY = "running";
	
	private Context mContext;
	protected View contextView;
	protected BaseFragmentActivity contextActivity;
	protected SharedPreferences mPreferences;
	
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		
//		contextView = inflater.inflate(getLayoutId(), container, false);
//		
//		//获取Activity传递过来的参数
//		Bundle mBundle = getArguments();
//		TYPE = mBundle.getString("TYPE");
//		Log.v(TAG, TYPE);
//		
//		return contextView;
//	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(null != contextView) {
			ViewGroup parent = (ViewGroup) contextView.getParent();
			if(null != parent) {
				parent.removeView(contextView);
			}
		} else {
			contextView = inflater.inflate(getLayoutId(), container, false);

			//获取Activity传递过来的参数
			Bundle mBundle = getArguments();
			TYPE = mBundle.getString("TYPE");
			Log.v(TAG, TYPE);
		}
		
		return contextView;
	}
	
	protected int getLayoutId() {
		return R.layout.info_list_fragment;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		contextActivity = (BaseFragmentActivity) this.getActivity();
		mContext = this.getActivity();
		mPreferences = TwitterApplication.mPref; // PreferenceManager.getDefaultSharedPreferences(this);
		
		setupState(isTrue(savedInstanceState, SIS_RUNNING_KEY));
		
        registerGestureListener();  //// 手势识别
        registerShakeListener();  //// 晃动刷新
	}
	
	protected void setupState(boolean isrunning) {
		init(isrunning);
		initView();
		initListener();
	}
	protected void init(boolean isrunning) {}
	protected void initView() {}
	protected void initListener() {}

	public static boolean isTrue(Bundle bundle, String key) {
		return bundle != null && bundle.containsKey(key) && bundle.getBoolean(key);
	}
	
    //////////////////// Gesture test /////////////////////////////////////
    private static boolean useGestrue;
    {
        useGestrue = TwitterApplication.mPref.getBoolean(Preferences.USE_GESTRUE, false);
        if (useGestrue) {
            Log.v(TAG, "Using Gestrue!");
        } else {
            Log.v(TAG, "Not Using Gestrue!");
        }
    }
    private static boolean useShake;
    {
        useShake = TwitterApplication.mPref.getBoolean(Preferences.USE_SHAKE, false);
        if (useShake) {
            Log.v(TAG, "Using Shake to refresh!");
        } else {
            Log.v(TAG, "Not Using Shake!");
        }
    }
    
    protected FlingGestureListener myGestureListener = null;

//  @Override
//  public boolean onTouchEvent(MotionEvent event) {
//      if (useGestrue && myGestureListener != null) {
//          return myGestureListener.getDetector().onTouchEvent(event);
//      }
//      return super.onTouchEvent(event);
//  }
  protected void setMyTouchListener() {
		/* Fragment中，注册
	    * 接收MainActivity的Touch回调的对象
	    * 重写其中的onTouchEvent函数，并进行该Fragment的逻辑处理
	    */
	  OnFragmentActivityTouchListener myTouchListener = new OnFragmentActivityTouchListener() {
	        @Override  
	        public void onTouchEvent(MotionEvent event) {
				// 处理手势事件
	           if (useGestrue && myGestureListener != null) {
	              myGestureListener.getDetector().onTouchEvent(event);
	          }
	        }  
	    };

	  // 将myTouchListener注册到分发列表
	   ((BaseFragmentActivity)this.getActivity()).registerMyTouchListener(myTouchListener);   
  }

  // use it in _onCreate
  protected void registerGestureListener() {
      if (useGestrue) {
//          myGestureListener = new FlingGestureListener(this, MyActivityFlipper.create(this));
//          getTweetList().setOnTouchListener(myGestureListener);
      }
  }
  
  protected ShakeListener mShaker = null;
  // use it in _onCreate
  protected void registerShakeListener() {
  	if (useShake){
	    	mShaker = new ShakeListener(this.getActivity());
	    	mShaker.setOnShakeListener(new ShakeListener.OnShakeListener() {
				
				@Override
				public void onShake() {
					Log.v(TAG, "onShake");
				}
			});
  	}
	}
	
  private class SearchLocationObserver implements Observer {
      @Override
      public void update(Observable observable, Object data) {
      }
  }
  
//	public String getUserId() {
//		return TwitterApplication.getMyselfId(false);
//	}
}
