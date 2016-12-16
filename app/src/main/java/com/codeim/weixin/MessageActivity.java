package com.codeim.weixin;

import java.util.ArrayList;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.codeim.byme.base.BaseFragmentActivity;
import com.codeim.byme.fragment.InfoListFragment;
import com.codeim.coxin.R;
import com.codeim.weixin.fragment.ChatListFragment;
import com.codeim.weixin.fragment.ContactListFragment;

public class MessageActivity extends BaseFragmentActivity implements OnTouchListener {
	private static final String TAG = "MessageActivity";
    private static final String[] TYPE = new String[] { "liaotian", "tongxunlu"};  
	
	private ArrayList<Fragment> list = null;
	private ViewPager mViewPager;
	private Resources resources;
	
	private TextView liaotian;
	private TextView tongxunlu;
	
	private Fragment liaotian_fragment;
	private Fragment tongxunlu_fragment;
	
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		if (super._onCreate(savedInstanceState)) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);			
//			getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);  
			setContentView(getLayoutId());
			
			setupState();
			return true;
		} else {
			return false;
		}
	}

	protected int getLayoutId() {
		return R.layout.message_main;
	}
	protected void setupState() {
		init();
		initView();
		initListener();
	}
	protected void init() {
		mViewPager = (ViewPager) findViewById(R.id.pager);
		liaotian = (TextView) findViewById(R.id.liaotian);
		tongxunlu = (TextView) findViewById(R.id.tongxunlu);
		resources = getResources();
	}
	protected void initView() {
		liaotian_fragment = new ChatListFragment();
		tongxunlu_fragment = new ContactListFragment();
			
		list = new ArrayList<Fragment>();
			
		list.add(liaotian_fragment);
		list.add(tongxunlu_fragment);
			
		mViewPager.setAdapter(new MyFragmentAdapter(getSupportFragmentManager(),list));
		mViewPager.setCurrentItem(0);
		mViewPager.setOnPageChangeListener(new MyViewPagerChangedListener());
			
		//mViewPager.setCurrentItem(0);
		mViewPager.setCurrentItem(0, true);
	}
	protected void initListener() {
		liaotian.setOnClickListener(new MyClickListener(0));
		tongxunlu.setOnClickListener(new MyClickListener(1));
	}
	
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
			TranslateAnimation ta = null;
			switch (arg0) {
			case 0:
	    		liaotian.setTextColor(resources.getColor(R.color.color_tab_green));
	    		tongxunlu.setTextColor(resources.getColor(R.color.color_black));
				break;
			case 1:
	    		liaotian.setTextColor(resources.getColor(R.color.color_black));
	    		tongxunlu.setTextColor(resources.getColor(R.color.color_tab_green));
				break;
		    }
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		 if ((keyCode == KeyEvent.KEYCODE_MENU)) {       
	            return true;
	        }
		return super.onKeyDown(keyCode, event);
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
	
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v(TAG, "onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==200&&resultCode==100){ //return from delete friend
			tongxunlu_fragment.onActivityResult(requestCode, resultCode, data);
			liaotian_fragment.onActivityResult(requestCode, resultCode, data);
//			((ChatListFragment)liaotian_fragment).doRetrieve();
//			((ContactListFragment)liaotian_fragment).doRetrieve();
		}
	}

}

