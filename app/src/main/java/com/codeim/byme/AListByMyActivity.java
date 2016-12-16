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

package com.codeim.byme;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.codeim.byme.fragment.InfoListFragment;
import com.codeim.coxin.R.id;
import com.codeim.coxin.R.layout;
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
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.NavBar;
import com.codeim.coxin.ui.module.SimpleFeedback;
import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
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
import com.codeim.floorview.adapter.CommentArrayAdapter;
import com.codeim.floorview.utils.DateFormatUtils;
import com.codeim.lib.viewpagerindicator.TabPageIndicator;
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

//public class AListByMyActivity extends BaseNoDoubleClickActivity implements OnTouchListener {
public class AListByMyActivity extends FragmentActivity implements OnTouchListener {
	private static final String TAG = "AListByMyActivity";
	
	/** 
     * Tab标题 
     */  
    private static final String[] TITLE = new String[] { "我发布的", "我回复的"};  
	private FragmentPagerAdapter adapter;
	private ViewPager pager;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "AListByMyActivity OnCreate start");
		setContentView(R.layout.info_list_about_my_a);
			
		//ViewPager的adapter  
	    adapter = new TabPageIndicatorAdapter(getSupportFragmentManager());
	    ViewPager pager = (ViewPager)findViewById(R.id.pager);  
	    pager.setAdapter(adapter);
	        
	    //实例化TabPageIndicator然后设置ViewPager与之关联  
	    TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);  
	    indicator.setViewPager(pager);
	        
	    //如果我们要对ViewPager设置监听，用indicator设置就行了  
	    indicator.setOnPageChangeListener(new OnPageChangeListener() {
	            @Override  
	            public void onPageSelected(int arg0) {  
	                Toast.makeText(getApplicationContext(), TITLE[arg0], Toast.LENGTH_SHORT).show();  
	            }  
	              
	            @Override  
	            public void onPageScrolled(int arg0, float arg1, int arg2) {  
	                  
	            }  
	              
	            @Override  
	            public void onPageScrollStateChanged(int arg0) {  
	                  
	            }  
	    });
	    //start_timer();
	}
	
	/** 
     * ViewPager适配器 
     * @author len 
     * 
     */  
    class TabPageIndicatorAdapter extends FragmentPagerAdapter {
        public TabPageIndicatorAdapter(FragmentManager fm) {  
            super(fm);  
        }  
  
        @Override  
        public Fragment getItem(int position) {  
            //新建一个Fragment来展示ViewPager item的内容，并传递参数  
            Fragment fragment = new InfoListFragment();
            Bundle args = new Bundle();    
            args.putString("arg", TITLE[position]);    
            fragment.setArguments(args);    
              
            return fragment;  
        }  
  
        @Override  
        public CharSequence getPageTitle(int position) {  
            return TITLE[position % TITLE.length];  
        }  
  
        @Override  
        public int getCount() {  
            return TITLE.length;  
        }  
    }

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}


//	@Override
//    public List<com.codeim.coxin.fanfou.Info> getNearbyInfo(int page_size, int page_index, int last_id, String infoType, 
//	        double lat, double lng) throws HttpException {
//	    return getApi().getFlyInfomsgRefreshLocation(page_size, page_index, last_id, infoType, lat, lng);
//	}
	
	//implement onTouchEvent for fragment
	public interface MyTouchListener {  
	    public void onTouchEvent(MotionEvent event);  
	}  
	      
	// 保存MyTouchListener接口的列表  
	private ArrayList<MyTouchListener> myTouchListeners = new ArrayList<MyTouchListener>();  
	      
	/** 
	* 提供给Fragment通过getActivity()方法来注册自己的触摸事件的方法 
	* @param listener 
	*/  
	public void registerMyTouchListener(MyTouchListener listener) {  
	     myTouchListeners.add(listener);  
	}  
	      
	/** 
	* 提供给Fragment通过getActivity()方法来取消注册自己的触摸事件的方法 
	* @param listener 
	*/  
	public void unRegisterMyTouchListener(MyTouchListener listener) {  
	    myTouchListeners.remove( listener );  
	}  
	      
	/** 
	* 分发触摸事件给所有注册了MyTouchListener的接口 
	*/  
	@Override  
	public boolean dispatchTouchEvent(MotionEvent ev) {   
	    for (MyTouchListener listener : myTouchListeners) {  
	    listener.onTouchEvent(ev);  
	    }  
	    return super.dispatchTouchEvent(ev);  
	}  
	
    
}