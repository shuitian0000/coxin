package com.codeim.coxin.widget;

import java.util.ArrayList;

import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.coxin.R;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BaseWheelView extends ListView {
	
	private LayoutInflater inflater;
	
	protected View mContentView;
	protected Context context;
	private ListView mListView;
	private int display_count=3;
	
	public BaseWheelView(Context context) {
		super(context);
		this.context = context;
		init(context);
	}

	public BaseWheelView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init(context);
	}
	
	private void init(Context context) {
		setCacheColorHint(context.getResources().getColor(R.color.color_transparent));
		inflater = LayoutInflater.from(context);
		
		mContentView = (LinearLayout) inflater.inflate(R.layout.base_wheel_view, null);
		mListView = (ListView) mContentView.findViewById(R.id.wheel_listview);
	}
	
	public class BaseWheelAdapter extends BaseAdapter implements TweetAdapter {

		private Context mContext;
		private ArrayList<String> dataList;
		private ArrayList<String> displayDataList;
		private DisplayMetrics dm;
		protected LayoutInflater mInflater;
		
		private static final int DEFAULT_DISPLAY_COUNT=3;
		private int display_count = DEFAULT_DISPLAY_COUNT;
		
		private final Handler mHandler = new Handler();

		public BaseWheelAdapter(Context c, ArrayList<String> dataList) {

			mContext = c;
			this.dataList = new ArrayList<String> ();
			
			if((display_count-1)/2>0)
			for(int i=0; i<(display_count-1)/2; i++)
			    this.dataList.add("");
			this.dataList.addAll(dataList);
			if(display_count/2>0)
			for(int i=0; i<display_count/2; i++)
			    this.dataList.add("");
			
			dm = new DisplayMetrics();
//			((Activity) mContext).getWindowManager().getDefaultDisplay()
//					.getMetrics(dm);

		}
		public BaseWheelAdapter(Context c, ArrayList<String> dataList, int display_cnt) {
			mContext = c;
			this.dataList = new ArrayList<String> ();
			
			this.display_count = display_cnt;
			
			if((display_count-1)/2>0)
			for(int i=0; i<(display_count-1)/2; i++)
			    this.dataList.add("");
			this.dataList.addAll(dataList);
			if(display_count/2>0)
			for(int i=0; i<display_count/2; i++)
			    this.dataList.add("");
			
			dm = new DisplayMetrics();
//			((Activity) mContext).getWindowManager().getDefaultDisplay()
//					.getMetrics(dm);
		}

		@Override
		public int getCount() {
//			return dataList.size();
			return display_count;
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
			public TextView optionName;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.base_wheel_view_item, parent, false);
				viewHolder.optionName = (TextView) convertView
						.findViewById(R.id.item_text);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			viewHolder.optionName.setText(dataList.get(position));

			viewHolder.optionName.setTag(position);

			return convertView;
		}

//		private OnItemClickListener mOnItemClickListener;
	//
//		public void setOnItemClickListener(OnItemClickListener l) {
//			mOnItemClickListener = l;
//		}
	//
//		public interface OnItemClickListener {
//			public void onItemClick(int position);
//		}
		
		public void refresh(ArrayList<String> options) {
			dataList = (ArrayList<String>) options.clone();
			
			notifyDataSetChanged();
		}
		
		@Override
		public void refresh() {
			notifyDataSetChanged();
		}

	}

}
