package com.codeim.coxin;


import android.app.TabActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TabHost;

import com.codeim.byme.ListByMyActivity;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.R;
import com.codeim.weixin.MessageActivity;


public class NewActivity extends TabActivity implements OnCheckedChangeListener {
    /** Called when the activity is first created. */
	
	private static final String TAG = "NewActivity";
	private static final int RESULT_LOGOUT = RESULT_FIRST_USER + 1;
	
	private TabHost mHost;
	private RadioGroup radioderGroup;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		Log.d("NewActivity", "onCreate NewActivity");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_coxin);
		
        //检验是否登录
		if(checkIsLogin()) {
			//实例化TabHost
            mHost = this.getTabHost();
        
          //添加选项卡
            mHost.addTab(mHost.newTabSpec("NEARBY").setIndicator("NEARBY").setContent(new Intent(this, NearbyActivity.class)));
//            //mHost.addTab(mHost.newTabSpec("CHANNEL").setIndicator("CHANNEL").setContent(new Intent(this, ChannelActivityGroup.class)));
            mHost.addTab(mHost.newTabSpec("MY").setIndicator("MY").setContent(new Intent(this, ListByMyActivity.class)));
            mHost.addTab(mHost.newTabSpec("DIALOGUE").setIndicator("DIALOGUE").setContent(new Intent(this, MessageActivity.class)));//DialogueActivity.class)));
            mHost.addTab(mHost.newTabSpec("SETTING").setIndicator("SETTING").setContent(new Intent(this, SettingsActivity.class)));
        
            radioderGroup = (RadioGroup) findViewById(R.id.main_radio);
		    radioderGroup.setOnCheckedChangeListener(this);
		}
    }
	
	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch(checkedId) {
		    case R.id.radio_btn_nearby:
			    mHost.setCurrentTabByTag("NEARBY");
			    break;
//		    case R.id.radio_btn_channel:
//			    mHost.setCurrentTabByTag("CHANNEL");
//			    break;
		    case R.id.radio_btn_my:
			    mHost.setCurrentTabByTag("MY");
			    break;
		    case R.id.radio_btn_dialogue:
			    mHost.setCurrentTabByTag("DIALOGUE");
			    break;
		    case R.id.radio_btn_setting:
			    mHost.setCurrentTabByTag("SETTING");
			    break;
		}		
	}
	
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if(newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
		    NewActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        	// Toast.makeText(getApplicationContext(), "瑜版挸澧犵仦蹇撶娑撶儤铆鐏烇拷, Toast.LENGTH_SHORT).show();
        }else{
        	// Toast.makeText(getApplicationContext(), "瑜版挸澧犵仦蹇撶娑撹櫣鐝仦锟? Toast.LENGTH_SHORT).show();
        }
    }
	
	private boolean checkIsLogin() {
//	    if (TwitterApplication.mPref.getBoolean(Preferences.FORCE_SCREEN_ORIENTATION_PORTRAIT, false)) {
//			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		}
		if (!checkIsLogedIn()) {
			return false;
		} else {
			PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
			return true;
		}
	}
	
	protected boolean checkIsLogedIn() {
//		if (!getApi().isLoggedIn()) {
		if (!TwitterApplication.mApi.isLoggedIn()) {
			Log.d(TAG, "Not logged in.");
			handleLoggedOut();
			return false;
		}
		return true;
	}
	
	protected void handleLoggedOut() {
	    Log.d(TAG, "before handleLoggedOut");
		if (isTaskRoot()) {
		    Log.d(TAG, "before showLogin");
			showLogin();
		} else {
			setResult(RESULT_LOGOUT);
		}

		finish();
	}
	
	public Weibo getApi() {
		return TwitterApplication.mApi;
	}
	
	protected void showLogin() {
		Intent intent = new Intent(this, LoginActivity.class);
		// TODO: might be a hack?
		intent.putExtra(Intent.EXTRA_INTENT, getIntent());

		startActivity(intent);
	}
}