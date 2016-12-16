package com.codeim.coxin.view;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.task.CommonTask;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.task.TaskAdapter;
import com.codeim.coxin.task.TaskListener;
import com.codeim.coxin.task.TaskParams;
import com.codeim.coxin.task.TaskResult;
import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.coxin.util.DateTimeHelper;
import com.codeim.coxin.widget.wheelold.NumericWheelAdapter;
import com.codeim.coxin.widget.wheelold.OnWheelChangedListener;
import com.codeim.coxin.widget.wheelold.WheelView;
import com.codeim.floorview.adapter.ImageFolderAdapter;
import com.codeim.floorview.bean.ImageFolder;
import com.codeim.floorview.view.BasePopupWindowForListView;
import com.codeim.coxin.R;

public class InfoWheelAddTimeDialog extends BaseDialogForListView<String> {
	private ListView mListOption;
	private InfoMenuAdapter mMenuOptionAdapter;
	
	private ArrayList<String> mDataList;
	private View mView;
	
	private WheelView days;
	private WheelView hours;
	private WheelView mins;
	private TextView before_time;
	private TextView change_time;
	public ImageButton btn_toggle;
	
	private NumericWheelAdapter dayAdapter;
	private NumericWheelAdapter hourAdapter;
	private NumericWheelAdapter minAdapter;
	
	private Date old_time;
	private Date new_time;
	
	private int day;
	private int hour;
	private int min;
	
	private int day_limit=1;
	private int hour_limit=23;
	private int min_limit=59;
	
	protected String infoId;
	protected GenericTask mGetExpireTask;
	protected Date beforeTime;

	public InfoWheelAddTimeDialog(int width, int height,
			Context context, int layout, String infoId, int dayLimit, int hourLimit, int minLimit)
	{
		super(context, layout, width, height);
//		this.mContentView = mContentView;
//		this.mDataList = datas;
		this.infoId = infoId;
		this.day_limit = dayLimit<0?0:dayLimit;
		this.hour_limit = hourLimit<0?0:(hourLimit>23?23:hourLimit);
		this.min_limit = minLimit<0?0:(minLimit>59?59:minLimit);
		
		this.old_time =  new Date();

		initViews();
		initEvents();
		init();
	}
	
	public InfoWheelAddTimeDialog(int width, int height,
			Context context, int layout, String infoId, int dayLimit, int hourLimit, int minLimit, Date old_time)
	{
		super(context, layout, width, height);
//		this.mContentView = mContentView;
//		this.mDataList = datas;
		this.infoId = infoId;
		this.day_limit = dayLimit<0?0:dayLimit;
		this.hour_limit = hourLimit<0?0:(hourLimit>23?23:hourLimit);
		this.min_limit = minLimit<0?0:(minLimit>59?59:minLimit);
		
		this.old_time =  old_time;

		initViews();
		initEvents();
		init();
	}
	
	public void setTitleText(String title) {
		((TextView) findViewById(R.id.title)).setText(title);
	}
	public void setOldTime(Date time) {
		this.old_time = time;
		reCalTime();
	}
	public Date getNewTime() {
		return this.new_time;
	}
	public long getAddTime() {
		return day*24*60*60 + hour*60*60 + min*60;
	}
	
	public void reCalTime() {
		new_time = old_time;
		day = days.getCurrentItem();
		hour = hours.getCurrentItem();
		min = mins.getCurrentItem();
		new_time = DateTimeHelper.timeAdd(old_time, day, hour, min, "");
		change_time.setText("有效期限: "+DateTimeHelper.dateToString(new_time, ""));
	}

	@Override
	public void initViews()
	{
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mView = inflater.inflate(R.layout.image_folder_pop_view, null);
        
//		mListOption = (ListView) findViewById(R.id.info_pop_menu);
//        mMenuOptionAdapter = new InfoMenuAdapter(context, mDatas);
//		
//        mListOption.setAdapter(mMenuOptionAdapter);
		
		before_time = (TextView) findViewById(R.id.now_time);
		before_time.setText("目前有效:                 ");
		change_time = (TextView) findViewById(R.id.change_time);
		change_time.setText("有效期限:                 ");
		btn_toggle = (ImageButton) findViewById(R.id.time_set_view_toggle);
		
        days = (WheelView) findViewById(R.id.dialog_day);
        dayAdapter = new NumericWheelAdapter(0,day_limit);
        days.setAdapter(dayAdapter);
        days.setLabel("天");
        days.setVisibleItems(5);
        days.setCyclic(true);
        
        hours = (WheelView) findViewById(R.id.dialog_hour);
        hourAdapter = new NumericWheelAdapter(0,hour_limit);
        hours.setAdapter(hourAdapter);
        hours.setLabel("小时");
        hours.setVisibleItems(5);
        hours.setCyclic(true);
        
        mins = (WheelView) findViewById(R.id.dialog_mins);
        minAdapter = new NumericWheelAdapter(0,min_limit);
        mins.setAdapter(minAdapter);
        mins.setLabel("分钟");
        mins.setVisibleItems(5);
        mins.setCyclic(true);
        mins.setCurrentItem(29);
        
        
//		mListOption.setAdapter(new CommonAdapter<ImageFolder>(context, mDatas,
//				R.layout.list_dir_item)
//		{
//			@Override
//			public void convert(ViewHolder helper, ImageFolder item)
//			{
//				helper.setText(R.id.id_dir_item_name, item.getName());
//				helper.setImageByUrl(R.id.id_dir_item_image,
//						item.getFirstImagePath());
//				helper.setText(R.id.id_dir_item_count, item.getCount() + "张");
//			}
//		});
	}

	public interface OnViewToggle
	{
		public void selected(int position, String option);
	}
	private OnViewToggle mOnViewToggle;
	public void setOnViewToggle(OnViewToggle mOnViewToggle)
	{
		this.mOnViewToggle = mOnViewToggle;
	}

	@Override
	public void initEvents()
	{
		
		days.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
            	//设置天数的 WheelView 的适配器
//            	day = newValue;
            	reCalTime();
            }
        });
		hours.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
            	//设置小时的 WheelView 的适配器
//            	hour = newValue;
            	reCalTime();
            }
        });
		mins.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
            	//设置小时的 WheelView 的适配器
//            	min = newValue;
            	reCalTime();
            }
        });
//		mListOption.setOnItemClickListener(new OnItemClickListener()
//		{
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view,
//					int position, long id)
//			{
//
//				if (mOnOptionSelected != null)
//				{
//					mOnOptionSelected.selected(position, mDatas.get(position));
//				}
//			}
//		});
	}

	@Override
	public void init()
	{
		// TODO Auto-generated method stub
		reCalTime();
		
		getNowExpireTime();
	}

	@Override
	protected void beforeInitWeNeedSomeParams(Object... params)
	{
		// TODO Auto-generated method stub
	}
	
	public void refresh()
	{
//		this.mMenuOptionAdapter.refresh();
	}
	
	public void getNowExpireTime() {
		
		if (mGetExpireTask != null && mGetExpireTask.getStatus() == GenericTask.Status.RUNNING) {
			return;
		} else {
			mGetExpireTask = new CommonTask.GetExpireTask();
			mGetExpireTask.setListener(mGetExpireTaskListener);

			TaskParams params = new TaskParams();
			params.put("infoId", infoId);
			mGetExpireTask.execute(params);
			
			//taskManager.addTask(mChangeTimeTask);
		}
	}
	
	private TaskListener mGetExpireTaskListener = new TaskAdapter() {

		@Override
		public String getName() {
			return "ChangeTimeTask";
		}

		@Override
		public void onPostExecute(GenericTask task, TaskResult result) {
			if (result == TaskResult.AUTH_ERROR) {
				//logout();
			} else if (result == TaskResult.OK) {
				//onChangeTimeSuccess(toChangeTimeId, afterChangeTime);
				if(task == mGetExpireTask) {
					beforeTime = ((CommonTask.GetExpireTask) mGetExpireTask).ExpireTime;
					onGetExpireSuccess();
				}
			} else if (result == TaskResult.IO_ERROR) {
				//onChangeTimeFailure();
			}
		}
	};
	
	public void onGetExpireSuccess() {
		before_time.setText("目前有效: "+DateTimeHelper.dateToString(beforeTime, ""));
		old_time = beforeTime;
		reCalTime();
	}
	
	public class InfoMenuAdapter extends BaseAdapter implements TweetAdapter {

		private Context mContext;
		private ArrayList<String> dataList;
		private DisplayMetrics dm;
		protected LayoutInflater mInflater;
		private ViewHolder mImageFolderHolder;
		
		private final Handler mHandler = new Handler();

		public InfoMenuAdapter(Context c, ArrayList<String> dataList) {

			mContext = c;
			this.dataList = dataList;
			dm = new DisplayMetrics();
//			((Activity) mContext).getWindowManager().getDefaultDisplay()
//					.getMetrics(dm);

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
			public TextView optionName;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.info_pop_menu_item, parent, false);
				viewHolder.optionName = (TextView) convertView
						.findViewById(R.id.option_name);
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
