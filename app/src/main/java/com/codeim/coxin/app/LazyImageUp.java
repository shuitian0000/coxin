package com.codeim.coxin.app;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;

import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.LazyImageLoader.ImageLoaderCallback;
import com.codeim.coxin.http.HttpException;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter;

public class LazyImageUp {
	private static final String TAG = "ProfileImageCacheManager";
	public static final int HANDLER_MESSAGE_ID = 1;
	public static final String EXTRA_BITMAP = "extra_bitmap";
	public static final String EXTRA_IMAGE_URL = "extra_image_url";
	public static final String EXTRA_IMAGE_TAG="extra_image_tag";

	private ImageManager mImageManager = new ImageManager(TwitterApplication.mContext);
	private BlockingQueue<String> mUrlList = new ArrayBlockingQueue<String>(50);
	private BlockingQueue<Integer> mInfoIdList = new ArrayBlockingQueue<Integer>(50);
	private CallbackManager mCallbackManager = new CallbackManager();

	private SendImageTask mTask = new SendImageTask();

	/**
	 * 取图片, 可能直接从cache中返回, 或从文件得到
	 * 
	 * @param url
	 * @param callback
	 * @return
	 */
	
	public void upMultiImage(List< String > images,  int info_id, ImageUpCallback callback) {
		for(String image_url: images) {
             mCallbackManager.put(image_url,info_id,callback);
             startUpThread(image_url, info_id);
		}
	}

	private void startUpThread(String URL, int info_id) {
		if (URL != null) {
			addUrlToUpQueue(URL, info_id);
		}

		// Start Thread
		State state = mTask.getState();
		if (Thread.State.NEW == state) {
			mTask.start(); // first start
		} else if (Thread.State.TERMINATED == state) {
			mTask = new SendImageTask(); // restart
			mTask.start();
		}
	}

	private void addUrlToUpQueue(String URL, int info_id) {
		if (!mUrlList.contains(URL)) {
			try {
				mUrlList.put(URL);
				mInfoIdList.put(info_id);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	// Low-level interface to get ImageManager
	public ImageManager getImageManager() {
		return mImageManager;
	}

	private class SendImageTask extends Thread {
		private volatile boolean mTaskTerminated = false;
		private static final int TIMEOUT = 3 * 60;
		private boolean isPermanent = true;

		@Override
		public void run() {
			try {
				while (!mTaskTerminated) {
					String url;
					int info_id=0;
					if (isPermanent) {
						url = mUrlList.take();
						info_id = mInfoIdList.take();
					} else {
						url = mUrlList.poll(TIMEOUT, TimeUnit.SECONDS); // waiting
						if (null == url) {
							break;
						} // no more, shutdown
					}

					final int image_id = mImageManager.safeSend(url, info_id);

					// use handler to process callback
					final Message m = handler.obtainMessage(HANDLER_MESSAGE_ID);
					Bundle bundle = m.getData();
					bundle.putString(EXTRA_IMAGE_URL, url);
					bundle.putInt(EXTRA_IMAGE_TAG, image_id);
					
					handler.sendMessage(m);
				}
			} catch (HttpException ioe) {
				Log.e(TAG, "Send Image failed, " + ioe.getMessage());
			} catch (InterruptedException e) {
				Log.w(TAG, e.getMessage());
			} finally {
				Log.v(TAG, "Send image task terminated.");
				mTaskTerminated = true;
			}
		}

		@SuppressWarnings("unused")
		public boolean isPermanent() {
			return isPermanent;
		}

		@SuppressWarnings("unused")
		public void setPermanent(boolean isPermanent) {
			this.isPermanent = isPermanent;
		}

		@SuppressWarnings("unused")
		public void shutDown() throws InterruptedException {
			mTaskTerminated = true;
		}
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLER_MESSAGE_ID:
				final Bundle bundle = msg.getData();
				String url = bundle.getString(EXTRA_IMAGE_URL);
//				Bitmap bitmap = (Bitmap) (bundle.get(EXTRA_BITMAP));
				int info_id = bundle.getInt(EXTRA_IMAGE_TAG);

				// callback
				mCallbackManager.call(url, info_id);
				break;
			default:
				// do nothing.
			}
		}
	};

	public interface ImageUpCallback {
		void refresh(String url, int info_id);
	}

	public static class CallbackManager {
		private static final String TAG = "CallbackManager";
		private ConcurrentHashMap<String, List<ImageUpCallback>> mCallbackMap;

		public CallbackManager() {
			mCallbackMap = new ConcurrentHashMap<String, List<ImageUpCallback>>();
		}

		public void put(String url, int info_id, ImageUpCallback callback) {
			Log.v(TAG, "url=" + url+"info_id="+String.valueOf(info_id));
			if (!mCallbackMap.containsKey(url+String.valueOf(info_id))) {
				Log.v(TAG, "url does not exist, add list to map");
				mCallbackMap.put(url+String.valueOf(info_id), new ArrayList<ImageUpCallback>());
				// mCallbackMap.put(url, Collections.synchronizedList(new
				// ArrayList<ImageLoaderCallback>()));
			}

			mCallbackMap.get(url+String.valueOf(info_id)).add(callback);
			Log.v(TAG, "Add callback to list, count(url)=" + mCallbackMap.get(url+String.valueOf(info_id)).size());
		}

//		public void call(String url, Bitmap bitmap) {
	    public void call(String url, int info_id) {
			Log.v(TAG, "call url=" + url +"info_id"+info_id);
			List<ImageUpCallback> callbackList = mCallbackMap.get(url+String.valueOf(info_id));
			if (callbackList == null) {
				// FIXME: 有时会到达这里，原因我还没想明白
				Log.e(TAG, "callbackList=null");
				return;
			}
			for (ImageUpCallback callback : callbackList) {
				if (callback != null) {
//					callback.refresh(url, bitmap);
					callback.refresh(url, info_id);
				}
			}

			callbackList.clear();
			mCallbackMap.remove(url);
		}
	}
	
}
