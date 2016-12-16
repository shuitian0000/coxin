package com.codeim.coxin;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.ui.module.NavBar;
//import com.codeim.coxin.R;

import com.codeim.coxin.R;

public class AboutActivity extends Activity {
	//反馈信息
	private String versionName = null;
	private String deviceModel = null;
	private String versionRelease = null;
	private String feedback = null;

	private NavBar mNavBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		mNavBar = new NavBar(NavBar.HEADER_STYLE_BACK, this);
		mNavBar.setHeaderTitle("关于友聊");

		deviceModel=android.os.Build.MODEL;
		versionRelease=android.os.Build.VERSION.RELEASE;

		if (TwitterApplication.mPref.getBoolean(Preferences.FORCE_SCREEN_ORIENTATION_PORTRAIT, false)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		// set real version
		ComponentName comp = new ComponentName(this, getClass());
		PackageInfo pinfo = null;
		try {
			pinfo = getPackageManager().getPackageInfo(comp.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TextView version = (TextView) findViewById(R.id.version);

		// String versionString;
		if (TwitterApplication.DEBUG){
			version.setText(String.format("v %d(nightly)", pinfo.versionCode));
			versionName = String.format("%d", pinfo.versionCode);
		}else{
			version.setText(String.format("v %s", pinfo.versionName));
			versionName = pinfo.versionName;
		}
	}
}
