package com.codeim.coxin.app;

import android.graphics.Bitmap;

import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;

public interface ImageCache {
	public static Bitmap mDefaultBitmap = ImageManager.drawableToBitmap(TwitterApplication.mContext.getResources()
			.getDrawable(R.drawable.user_default_photo));

	public Bitmap get(String url);

	public void put(String url, Bitmap bitmap);
}
