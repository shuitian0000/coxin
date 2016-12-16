package com.codeim.coxin.ui.base;

import java.text.DateFormat.Field;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;



//import com.codeim.coxin.AboutActivity;



import com.codeim.coxin.LoginActivity;
import com.codeim.coxin.TwitterApplication;
//import com.codeim.coxin.PreferencesActivity;
//import com.codeim.coxin.TwitterActivity;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.db.TwitterDatabase;
import com.codeim.coxin.fanfou.Weibo;
import com.codeim.coxin.util.CommonUtils;
//import com.codeim.coxin.service.TwitterService;

import com.codeim.coxin.R;

import android.app.ActionBar;

/**
 * A BaseNoDoubleClickActivity has common routines and variables for an Activity that
 * contains a list of tweets and a text input field.
 * 
 * Not the cleanest design, but works okay for several Activities in this app.
 */

public class BaseNoDoubleClickActivity extends BaseActivity {
	protected static final String TAG = "BaseNoDoubleClickActivity";
	
	/**屏蔽连续多次**/
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (CommonUtils.isFastDoubleClick()) {
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}
