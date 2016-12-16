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

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.codeim.byme.base.OnFragmentActivityTouchListener;
import com.codeim.byme.fragment.InfoListFragment;
import com.codeim.coxin.R;
import com.codeim.coxin.R.color;
import com.codeim.coxin.R.id;
import com.codeim.coxin.R.layout;

//public class ListByMyActivity extends BaseNoDoubleClickActivity implements OnTouchListener {
public class ListByMyActivity extends FragmentActivity implements OnTouchListener {
	private static final String TAG = "ListByMyActivity";
	
	private ArrayList<Fragment> list = null;
	
	private ViewPager mViewPager;
	
	private ImageView iv_bottom_line;
	
	private Resources resources;
	
	private TextView fabu;
	
	private TextView huifu;
	
	/** 
     * Tab标题 
     */  
    private static final String[] TITLE = new String[] { "我发布的", "我回复的"};  
    private static final String[] TYPE = new String[] { "my_send", "my_reply"};  
	private FragmentPagerAdapter adapter;
	private ViewPager pager;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_list_about_my_c);
		
		initView();
		
		initWidth();
		
		initViewPager();
	}
	
	private void initView(){
		iv_bottom_line = (ImageView) findViewById(R.id.iv_bottom_line);
		mViewPager = (ViewPager) findViewById(R.id.pager);

		fabu = (TextView) findViewById(R.id.tab_my_send);
		huifu = (TextView) findViewById(R.id.tab_my_reply);
		
		fabu.setOnClickListener(new MyClickListener(0));
		huifu.setOnClickListener(new MyClickListener(1));
	}
	
	
	private int first = 0;
	private int second = 0;
	private int third = 0;
	private int first_width=0;
	private int second_width=0;
	
	private void initWidth(){
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int width = dm.widthPixels;
		resources = getResources();
		first = 0;
		second = width/2;
		first_width = width/2;
		second_width = width/2;
		
//		int[] position = new int[2]; 
//		fabu.getLocationOnScreen(position); 
//		first = position[0];
//		huifu.getLocationOnScreen(position); 
//		second = position[0];
//		first_width = fabu.getWidth();
//		second_width = huifu.getWidth();
	}
	
	private void initViewPager() {
		Fragment fabu_fragment = new InfoListFragment();
		Fragment huifu_fragment = new InfoListFragment();
		
		list = new ArrayList<Fragment>();
		
		list.add(fabu_fragment);
		list.add(huifu_fragment);
		
		mViewPager.setAdapter(new MyFragmentAdapter(getSupportFragmentManager(),list));
		mViewPager.setCurrentItem(0);
		mViewPager.setOnPageChangeListener(new MyViewPagerChangedListener());
		
		//mViewPager.setCurrentItem(0);
		mViewPager.setCurrentItem(0, true);
		fabu.setTextColor(resources.getColor(R.color.color_red));
		huifu.setTextColor(resources.getColor(R.color.color_darkgray));
	}
	
	private void setBottomLineWidth(int curwidth) {
		LayoutParams para;
		para = iv_bottom_line.getLayoutParams();
		para.width = curwidth;
		iv_bottom_line.setLayoutParams(para);
	}
	
	/** 
     * ViewPager适配器 
     * @author len 
     * 
     */  
	public class MyFragmentAdapter extends FragmentPagerAdapter{

		private ArrayList<Fragment> list;
		
		public MyFragmentAdapter(FragmentManager fm) {
			super(fm);
		}
		public MyFragmentAdapter(FragmentManager fm,
				ArrayList<Fragment> list) {
			super(fm);
			this.list = list;
		}
		@Override
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub
            Bundle args = new Bundle();    
            args.putString("TYPE", TYPE[arg0]);    
            list.get(arg0).setArguments(args);
            
			return list.get(arg0);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}
	}

	private int currPosition = 0; 
	private int curwidth=0;
	
	class MyViewPagerChangedListener implements OnPageChangeListener{

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onPageSelected(int arg0) {
			Log.d("onchanged", "onchanged " + arg0);
			initWidth();
			TranslateAnimation ta = null;
			switch (arg0) {
			case 0:
				
				if (currPosition == 1) {
					fabu.setTextColor(resources.getColor(R.color.color_red));
					huifu.setTextColor(resources.getColor(R.color.color_darkgray));
					//ta = new TranslateAnimation(first, 0, 0, 0);
					ta = new TranslateAnimation(second, first, 0, 0);
					curwidth = first_width;
				}
				if (currPosition == 2) {
					ta = new TranslateAnimation(second, 0, 0, 0);
				}
				if (currPosition == 3) {
					ta = new TranslateAnimation(third, 0, 0, 0);
				}
				
				break;
				
			case 1:
				
				if (currPosition == 0) {
					fabu.setTextColor(resources.getColor(R.color.color_darkgray));
					huifu.setTextColor(resources.getColor(R.color.color_red));
					//ta = new TranslateAnimation(0, first, 0, 0);
					ta = new TranslateAnimation(first, second, 0, 0);
					curwidth = second_width;
				}
				if (currPosition == 2) {
					ta = new TranslateAnimation(second, first, 0, 0);
				}
				if (currPosition == 3) {
					ta = new TranslateAnimation(third, first, 0, 0);
				}
				
				break;
				
			case 2:
				if (currPosition == 0) {
					ta = new TranslateAnimation(0, second, 0, 0);
				}
				if (currPosition == 1) {
					ta = new TranslateAnimation(first, second, 0, 0);
				}
				if (currPosition == 3) {
					ta = new TranslateAnimation(third, second, 0, 0);
				}
				break;
				
			case 3:
				if (currPosition == 0) {
					ta = new TranslateAnimation(0, third, 0, 0);
				}
				if (currPosition == 1) {
					ta = new TranslateAnimation(first, third, 0, 0);
				}
				if (currPosition == 2) {
					ta = new TranslateAnimation(second, third, 0, 0);
				}
				break;

				
			}
			
			currPosition = arg0;
			
			ta.setDuration(300);
			ta.setFillAfter(true);
			iv_bottom_line.startAnimation(ta);
			
			LayoutParams para;
			para = iv_bottom_line.getLayoutParams();
			para.width = curwidth;
			iv_bottom_line.setLayoutParams(para);
		}
		
	}
	
	class MyClickListener implements OnClickListener{
		
		private int index =0;
		
		public MyClickListener (int i){
			index = i;
		}

		@Override
		public void onClick(View v) {
			mViewPager.setCurrentItem(index);
			
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
	      
	// 保存MyTouchListener接口的列表  
	private ArrayList<OnFragmentActivityTouchListener> myTouchListeners = new ArrayList<OnFragmentActivityTouchListener>();  
	      
	/** 
	* 提供给Fragment通过getActivity()方法来注册自己的触摸事件的方法 
	* @param listener 
	*/  
	public void registerMyTouchListener(OnFragmentActivityTouchListener listener) {  
	     myTouchListeners.add(listener);  
	}  
	      
	/** 
	* 提供给Fragment通过getActivity()方法来取消注册自己的触摸事件的方法 
	* @param listener 
	*/  
	public void unRegisterMyTouchListener(OnFragmentActivityTouchListener listener) {  
	    myTouchListeners.remove( listener );  
	}  
	      
	/** 
	* 分发触摸事件给所有注册了MyTouchListener的接口 
	*/  
	@Override  
	public boolean dispatchTouchEvent(MotionEvent ev) {   
	    for (OnFragmentActivityTouchListener listener : myTouchListeners) {  
	    listener.onTouchEvent(ev);  
	    }  
	    return super.dispatchTouchEvent(ev);  
	}  
	
    
}