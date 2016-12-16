package com.codeim.floorview.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ToggleButton;

import com.codeim.coxin.R;
import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.app.LazyImageLoader;
import com.codeim.coxin.app.LazyImageLoader.ImageLoaderCallback;
import com.codeim.coxin.ui.module.NearbyInfoArrayAdapter;
import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.floorview.view.ListImageDirPopupWindow;
import com.codeim.floorview.view.SelectForFullImagePopupWindow;
import com.codeim.floorview.view.SelectForFullImagePopupWindow.OnCheckerClickListener;
//import com.web.utils.ImageManager2;

//public class CommentPicViewAdapter extends BaseAdapter implements
//		OnClickListener {
public class CommentPicViewAdapter extends BaseAdapter implements TweetAdapter{
	private static final String TAG = "CommentPicViewAdapter";

	private Context mContext;
	private ArrayList<String> dataList;
	private ArrayList<String> selectedDataList;
	private DisplayMetrics dm;
//	private SelectForFullImagePopupWindow mPopupWindow;

	
	public CommentPicViewAdapter(Context c) {
		mContext = c;
//		this.dataList = dataList;
		//this.selectedDataList = selectedDataList;
		dm = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);

	}
	
	public CommentPicViewAdapter(Context c, ArrayList<String> dataList) {

		mContext = c;
		this.dataList = dataList;
		//this.selectedDataList = selectedDataList;
		dm = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);

	}
	
	private ImageLoaderCallback callback = new ImageLoaderCallback() {
		@Override
		public void refresh(String url, Bitmap bitmap) {
			CommentPicViewAdapter.this.refresh();
//			notifyDataSetChanged();
		} 
	};
	
	public void setData(ArrayList<String> dataList) {
		this.dataList = dataList;
	}

	@Override
	public int getCount() {
		return dataList.size();
	}

	@Override
	public Object getItem(int position) {
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	private class ViewHolder {
		public ImageView imageView;
		public ToggleButton toggleButton;
//		public Button choosetoggle;
		public ToggleButton choosetoggle;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;

		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.comment_select_imageview, parent, false);
			viewHolder.imageView = (ImageView) convertView
					.findViewById(R.id.selected_image);
					
			viewHolder.toggleButton = (ToggleButton) convertView
					.findViewById(R.id.toggle_button);
			viewHolder.choosetoggle = (ToggleButton) convertView
					.findViewById(R.id.choosedbt);
		        viewHolder.toggleButton.setVisibility(View.GONE);
		        viewHolder.choosetoggle.setVisibility(View.GONE);
		        
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		String path;
		if (dataList != null && dataList.size() > position)
			path = dataList.get(position);
		else
			path = "camera_default";
			
		if (path.contains("default")) {
			viewHolder.imageView.setImageResource(R.drawable.bg_userheader_cover);
		} else {
			//TwitterApplication.mImageManager.displayImage(viewHolder.imageView,
			//		path, R.drawable.bg_userheader_cover, 100, 100);
			Bitmap bm = TwitterApplication.mImageLoader.get(path.toString(), callback);
			viewHolder.imageView.setImageBitmap(bm);
		}
		
		//viewHolder.toggleButton.setTag(position);
		//viewHolder.choosetoggle.setTag(position);
		//final ToggleButton mTbn = viewHolder.toggleButton;
		//final ToggleButton mToggleButton = viewHolder.choosetoggle;
		
		final int mPosition = position;
		final ImageView mImageView = viewHolder.imageView;
		
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View mView = inflater.inflate(R.layout.image_select_full_shower, null);
//		viewHolder.imageView.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				final SelectForFullImagePopupWindow mPopupWindow;
//				mPopupWindow = new SelectForFullImagePopupWindow(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
//						dataList.get(mPosition), mView, false, true);
//				mPopupWindow.setOutsideTouchable(true);
//				mPopupWindow.setFocusable(true);
//				mPopupWindow.findViewById(R.id.image_checker).setVisibility(View.GONE);
//				final View headView = ((Activity) mContext).findViewById(R.id.title);
//				mPopupWindow.showAsDropDown(headView);
//				
//				//mPopupWindow.setOnCheckerSelected(new OnCheckerClickListener() {
//				//	@Override
//				//	public void checkerSelected(boolean isChecked) {
//				//		mToggleButton.setChecked(isChecked);
//				//		mOnItemClickListener.onItemClick(mToggleButton, mPosition,
//				//				dataList.get(mPosition), mToggleButton.isChecked());
//				//	}
//				//});
//			}
//		});

		return convertView;
	}

	public int dipToPx(int dip) {
		return (int) (dip * dm.density + 0.5f);
	}

//	@Override
//	public void onClick(View view) {	
//		if (view.getId()==R.id.choosedbt) {
//			ToggleButton toggleButton = (ToggleButton) view;
//			int position = (Integer) view.getTag();
//			if (dataList != null && mOnItemClickListener != null
//					&& position < dataList.size()) {
//				mOnItemClickListener.onItemClick(toggleButton, position,
//						dataList.get(position), toggleButton.isChecked());
//			}
//		}
//	}

	//private OnItemClickListener mOnItemClickListener;
        //
	//public void setOnItemClickListener(OnItemClickListener l) {
	//	mOnItemClickListener = l;
	//}
        //
	//public interface OnItemClickListener {
	//	public void onItemClick(ToggleButton view, int position, String path,
	//			boolean isChecked);
	//}
	
	@Override
	public void refresh() {
		notifyDataSetChanged();
	}
	
	protected void launchActivity(Intent intent) {
		mContext.startActivity(intent);
	}

}
