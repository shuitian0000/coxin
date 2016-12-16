package com.codeim.coxin.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

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

public class InfoWheelSetTimeDialog extends BaseDialogForListView<String> {
	private ListView mListOption;
	private InfoMenuAdapter mMenuOptionAdapter;
	
	private ArrayList<String> mDataList;
	private View mView;
	
	private MyScrollView wheel_scrollview;
	private MyHorizontalScrollView wheel_horizontalScrollview;
	LinearLayout dialog_head;
	LinearLayout dialog_foot;
//	private WheelView days;
//	private WheelView hours;
//	private WheelView mins;
	private TextView before_time;
	private TextView change_time;
	public ImageButton btn_toggle;
	
//	private NumericWheelAdapter dayAdapter;
//	private NumericWheelAdapter hourAdapter;
//	private NumericWheelAdapter minAdapter;
	
	private Date limit_date;
	private Date old_time;
	private Date new_time;
	
	private Calendar cal;
	private DatePicker mDate;
	private TimePicker mTime;
	
//	private int new_year;
//	private int new_month;
//	private int new_day;
//	private int new_hour;
//	private int new_min;

	private int day_limit=1;
	private int hour_limit=23;
	private int min_limit=59;
	
	protected String infoId;
	protected GenericTask mGetExpireTask;
	protected Date beforeTime;
	
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		
//	}
//	
//	@Override
//	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		
//	}
	public InfoWheelSetTimeDialog(int width, int height,
			Context context, int layout, String infoId, int dayLimit, int hourLimit, int minLimit, Date old_time)
	{
		super(context, layout, width, height);
//		this.mContentView = mContentView;
//		this.mDataList = datas;
		
		this.infoId = infoId;
		this.day_limit = dayLimit<0?0:dayLimit;
		this.hour_limit = hourLimit<0?0:(hourLimit>23?23:hourLimit);
		this.min_limit = minLimit<0?0:(minLimit>59?59:minLimit);
		this.limit_date = DateTimeHelper.timeAdd(old_time, day_limit, hour_limit, min_limit, "");
		this.old_time =  old_time;
		this.new_time = old_time;
		
		cal = Calendar.getInstance();
		cal.setTime(old_time);

		initViews();
		initEvents();
		init();
	}
	
	public void setTitleText(String title) {
		((TextView) findViewById(R.id.title)).setText(title);
	}
	public void setOldTime(Date time) {
		this.old_time = time;
		
		reSetDate(mDate.getYear(), mDate.getMonth(), mDate.getDayOfMonth());
		reSetTime(mTime.getCurrentHour(), mTime.getCurrentMinute());
	}
	public Date getNewTime() {
		return this.new_time;
	}
	
	public void reSetDate(int year, int monthOfYear, int dayOfMonth) {
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, monthOfYear);
		cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		
		new_time=cal.getTime();
		change_time.setText("有效期限: "+DateTimeHelper.dateToString(new_time, ""));
	}
	public void reSetTime(int hourOfDay, int minute) {
		
		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, minute);

	    new_time=cal.getTime();
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
		
		wheel_scrollview=(MyScrollView) findViewById(R.id.wheel_scrollview);
		wheel_horizontalScrollview=(MyHorizontalScrollView) findViewById(R.id.wheel_horizontalScrollview);
		dialog_head = (LinearLayout) findViewById(R.id.dialog_head);
		dialog_foot = (LinearLayout) findViewById(R.id.dialog_foot);
		
		before_time = (TextView) findViewById(R.id.now_time);
		before_time.setText("目前有效: ");
		change_time = (TextView) findViewById(R.id.change_time);
		change_time.setText("有效期限: ");
		btn_toggle = (ImageButton) findViewById(R.id.time_set_view_toggle);
		
//        days = (WheelView) findViewById(R.id.dialog_day);
//        dayAdapter = new NumericWheelAdapter(0,day_limit);
//        days.setAdapter(dayAdapter);
//        days.setLabel("天");
//        days.setVisibleItems(5);
//        days.setCyclic(true);
		
		mDate = (DatePicker) findViewById(R.id.date_picker);
		mTime = (TimePicker) findViewById(R.id.time_picker);
        
		resizePikcer(mDate);//调整datepicker大小
		resizePikcer(mTime);//调整timepicker大小
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
		
//		days.addChangingListener(new OnWheelChangedListener() {
//            @Override
//            public void onChanged(WheelView wheel, int oldValue, int newValue) {
//            	//设置天数的 WheelView 的适配器
////            	day = newValue;
//            	reCalTime();
//            }
//        });

//		wheel_scrollview.setOnTouchListener(new View.OnTouchListener() {
//			
//			@Override
//			public boolean onTouch(View v, MotionEvent event) {
//				// TODO Auto-generated method stub
//				mDate.getParent().requestDisallowInterceptTouchEvent(false);
//				return false;
//			}
//		});
		
//		wheel_scrollview.setOnTouchListener(new View.OnTouchListener() {
//		
//		@Override
//		public boolean onTouch(View v, MotionEvent event) {
//			// TODO Auto-generated method stub
//			mDate.getParent().requestDisallowInterceptTouchEvent(false);
//			return false;
//		}
//	    });
		
		mDate.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
//                if(event.getAction() == MotionEvent.ACTION_DOWN){
//                	mDate.getParent().requestDisallowInterceptTouchEvent(true);
//                	v.getParent().requestDisallowInterceptTouchEvent(true);//屏蔽父控件拦截onTouch事件
//                    wheel_scrollview.requestDisallowInterceptTouchEvent(true);  
//                } else if(event.getAction() == MotionEvent.ACTION_UP) {
//                	wheel_scrollview.requestDisallowInterceptTouchEvent(false);
//                }
//                else{  
//                	wheel_scrollview.requestDisallowInterceptTouchEvent(true);  
//                }  
				
//                if (event.getAction() == MotionEvent.ACTION_UP)  
//                {  
//                	wheel_scrollview.requestDisallowInterceptTouchEvent(false);  
//                } else {
//                	wheel_scrollview.requestDisallowInterceptTouchEvent(true);
//                }
				
				 switch (event.getAction()) {
				     case MotionEvent.ACTION_DOWN:
			        	 wheel_scrollview.requestDisallowInterceptTouchEvent(true);
			        	 wheel_horizontalScrollview.requestDisallowInterceptTouchEvent(true);
			             return true;
			         case MotionEvent.ACTION_MOVE:   
			        	 wheel_scrollview.requestDisallowInterceptTouchEvent(true);
			        	 wheel_horizontalScrollview.requestDisallowInterceptTouchEvent(true);
			             break;
			         case MotionEvent.ACTION_UP:
			         case MotionEvent.ACTION_CANCEL:
			        	 wheel_scrollview.requestDisallowInterceptTouchEvent(false);
			        	 wheel_horizontalScrollview.requestDisallowInterceptTouchEvent(false);
			             break;
			     }  
				
//				v.getParent().requestDisallowInterceptTouchEvent(true);
				return false;
			}
        	
        });
//        mDate.init(cal.get(Calendar.YEAR) - 1900, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_WEEK), new OnDateChangedListener() {
        mDate.init(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), new OnDateChangedListener() {

			@Override
			public void onDateChanged(DatePicker view, int year,
					int monthOfYear, int dayOfMonth) {
				// TODO Auto-generated method stub

				reSetDate(year, monthOfYear, dayOfMonth);
			}
        	
        });
        
        mTime.setCurrentHour(cal.get(Calendar.HOUR_OF_DAY));
        mTime.setCurrentMinute(cal.get(Calendar.MINUTE));
        mTime.setIs24HourView(true);
        mTime.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
//                if(event.getAction() == MotionEvent.ACTION_DOWN){  
//                	mTime.getParent().requestDisallowInterceptTouchEvent(true);
//                	v.getParent().requestDisallowInterceptTouchEvent(true);//屏蔽父控件拦截onTouch事件
//                    wheel_scrollview.requestDisallowInterceptTouchEvent(true);  
//                } else if(event.getAction() == MotionEvent.ACTION_UP) {
//                	wheel_scrollview.requestDisallowInterceptTouchEvent(false);
//                }
//                else{  
//                	wheel_scrollview.requestDisallowInterceptTouchEvent(true);  
//                } 
				
//				if (event.getAction() == MotionEvent.ACTION_UP)  
//                {  
//                	wheel_scrollview.requestDisallowInterceptTouchEvent(false);  
//                } else {
//                	wheel_scrollview.requestDisallowInterceptTouchEvent(true);
//                }
				
				 switch (event.getAction()) {
			         case MotionEvent.ACTION_DOWN:
		        	     wheel_scrollview.requestDisallowInterceptTouchEvent(true);
		        	     wheel_horizontalScrollview.requestDisallowInterceptTouchEvent(true);
		                 return true;
		             case MotionEvent.ACTION_MOVE:   
		        	     wheel_scrollview.requestDisallowInterceptTouchEvent(true);
		        	     wheel_horizontalScrollview.requestDisallowInterceptTouchEvent(true);
		                 break;
		             case MotionEvent.ACTION_UP:
		             case MotionEvent.ACTION_CANCEL:
		        	     wheel_scrollview.requestDisallowInterceptTouchEvent(false);
		        	     wheel_horizontalScrollview.requestDisallowInterceptTouchEvent(false);
		                 break;
		         }  
				
//				v.getParent().requestDisallowInterceptTouchEvent(true);
				return false;
			}
        	
        });
        mTime.setOnTimeChangedListener(new OnTimeChangedListener() {

			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				// TODO Auto-generated method stub
				reSetTime(hourOfDay, minute);
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
		setOldTime(old_time);
		
		getNowExpireTime();
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
		setOldTime(old_time);
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
	
	public void reDraw(int totalHeight) {
		//set window params
//		Window window = getWindow();
//		WindowManager.LayoutParams lp = window.getAttributes();
		//set width,height by density and gravity
		float density = getDensity(context);
		int a = dialog_head.getHeight();
		int b = dialog_foot.getHeight();
		int c = wheel_scrollview.getHeight();
		int reHeight = totalHeight - a -b;
		reHeight = reHeight>0?reHeight:c;
		
		LayoutParams lp = wheel_scrollview.getLayoutParams();
		lp.height = reHeight;
		wheel_scrollview.setLayoutParams(lp);
		
//		ViewGroup.LayoutParams param = new ViewGroup.LayoutParams(  
//	              ViewGroup.LayoutParams.WRAP_CONTENT,  reHeight);  
//		wheel_scrollview.setLayoutParams(param);  
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
	
	
	
	/**
	 * 调整FrameLayout大小
	 * @param tp
	 */
	private void resizePikcer(FrameLayout tp){
		List<NumberPicker> npList = findNumberPicker(tp);
		for(NumberPicker np:npList){
			resizeNumberPicker(np);
		}
	}
	
	/**
	 * 得到viewGroup里面的numberpicker组件
	 * @param viewGroup
	 * @return
	 */
	private List<NumberPicker> findNumberPicker(ViewGroup viewGroup){
		List<NumberPicker> npList = new ArrayList<NumberPicker>();
		View child = null;
		if(null != viewGroup){
			for(int i = 0; i < viewGroup.getChildCount(); i++){
				child = viewGroup.getChildAt(i);
				if(child instanceof NumberPicker){
					npList.add((NumberPicker)child);
				}
				else if(child instanceof LinearLayout){
					List<NumberPicker> result = findNumberPicker((ViewGroup)child);
					if(result.size()>0){
						return result;
					}
				}
			}
		}
		return npList;
	}
	
	/*
	 * 调整numberpicker大小
	 */
	private void resizeNumberPicker(NumberPicker np){
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(80, LayoutParams.WRAP_CONTENT);
		params.setMargins(8, 0, 8, 0);
		np.setLayoutParams(params);                                                                            
	}

}
