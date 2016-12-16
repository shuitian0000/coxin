package com.codeim.coxin.view;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.codeim.coxin.TwitterApplication;
import com.codeim.coxin.ui.module.TweetAdapter;
import com.codeim.floorview.adapter.ImageFolderAdapter;
import com.codeim.floorview.bean.ImageFolder;
import com.codeim.floorview.view.BasePopupWindowForListView;
import com.codeim.coxin.R;

public class MultiChoiceDialog extends BaseDialogForListView<String> {
	private ListView mListOption;
	private MultiChoiceAdapter mMenuOptionAdapter;
	
	private Button positiveButton;
	private Button negativeButton;

	public MultiChoiceDialog(int width, int height,
			ArrayList<String> datas, Context context, int layout)
	{
		super(context, layout, width, height, datas);
//		this.mContentView = mContentView;
//		this.mDataList = datas;
		
		initViews();
		initEvents();
		init();
	}
	
	public void setTitleText(String title) {
		((TextView) findViewById(R.id.title)).setText(title);
	}

	@Override
	public void initViews()
	{
//        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        mView = inflater.inflate(R.layout.image_folder_pop_view, null);
		((LinearLayout) findViewById(R.id.content)).setVisibility(View.VISIBLE);
		((TextView) findViewById(R.id.message)).setVisibility(View.GONE);
		mListOption = (ListView) findViewById(R.id.dialog_listview_m);
		mListOption.setVisibility(View.VISIBLE);
		mListOption.setItemsCanFocus(false);
		mListOption.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mMenuOptionAdapter = new MultiChoiceAdapter(context, mDatas);
        
        positiveButton = (Button) findViewById(R.id.positiveButton);
        negativeButton = (Button) findViewById(R.id.negativeButton);
		
        mListOption.setAdapter(mMenuOptionAdapter);
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
	


	public interface OnPositiveButton
	{
		public void positiveButtonselected();
	}
	private OnPositiveButton mOnPositiveButton;
	public void setOnPositiveButton(OnPositiveButton mOnPositiveButton)
	{
		this.mOnPositiveButton = mOnPositiveButton;
	}
	public interface OnNegativeButton
	{
		public void negativeButtonselected();
	}
	private OnNegativeButton mOnNegativeButton;
	public void setOnNegativeButton(OnNegativeButton mOnNegativeButton)
	{
		this.mOnNegativeButton = mOnNegativeButton;
	}

	@Override
	public void initEvents()
	{
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
		
		mListOption.setOnItemClickListener(new OnItemClickListener(){    
	        @Override
	        public void onItemClick(AdapterView<?> parent, View view,
	                int position, long id) {
	            // TODO Auto-generated method stub
	            MultiChoiceAdapter.ViewHolder vHollder = (MultiChoiceAdapter.ViewHolder) view.getTag();    
	            //在每次获取点击的item时将对于的checkbox状态改变，同时修改map的值。  
	            boolean check = vHollder.cb.isChecked();
	            vHollder.cb.setChecked(!check);
	            mMenuOptionAdapter.isSelected.put(position, !check);
	        }    
	    });
		
		positiveButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mOnPositiveButton.positiveButtonselected();
			}
		});
		negativeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mOnNegativeButton.negativeButtonselected();
			}
		});
	}

	@Override
	public void init()
	{
		// TODO Auto-generated method stub

	}

	@Override
	protected void beforeInitWeNeedSomeParams(Object... params)
	{
		// TODO Auto-generated method stub
	}
	
	public void refresh()
	{
		this.mMenuOptionAdapter.refresh();
	}
	
	public String getAllSelected() {
	    return mMenuOptionAdapter.getAllSelected();
	}
	
	
	public class MultiChoiceAdapter extends BaseAdapter implements TweetAdapter {

		private Context mContext;
		private ArrayList<String> dataList;
		private DisplayMetrics dm;
		protected LayoutInflater mInflater;
		private ViewHolder mImageFolderHolder;
		
		// 用来控制CheckBox的选中状况  
	    private HashMap<Integer,Boolean> isSelected;  
		
		private final Handler mHandler = new Handler();

		public MultiChoiceAdapter(Context c, ArrayList<String> dataList) {

			mContext = c;
			this.dataList = dataList;
			dm = new DisplayMetrics();
//			((Activity) mContext).getWindowManager().getDefaultDisplay()
//					.getMetrics(dm);
			isSelected = new HashMap<Integer, Boolean>();
			initDate();
		}
		// 初始化isSelected的数据  
	    private void initDate(){
	    	int len=dataList.size();
	        for(int i=0; i<len;i++) {
	        	isSelected.put(i,false);
	        }  
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

		public class ViewHolder {
			public TextView tv;
			public CheckBox cb;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.base_dialog_multi_choice_item, parent, false);
				viewHolder.tv = (TextView) convertView.findViewById(R.id.base_dialog_item_tv);  
				viewHolder.cb = (CheckBox) convertView.findViewById(R.id.base_dialog_item_cb); 
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			
			// 设置list中TextView的显示  
			viewHolder.tv.setText(dataList.get(position));
			// 根据isSelected来设置checkbox的选中状况
			viewHolder.cb.setChecked(isSelected.get(position));
			
			viewHolder.tv.setTag(position);
			viewHolder.cb.setTag(position);

			return convertView;
		}
		
		public HashMap<Integer,Boolean> getIsSelected() {  
	        return isSelected;  
	    }
		public void setIsSelected(HashMap<Integer,Boolean> isSelected) {
			this.isSelected = isSelected;
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
		
		public String getAllSelected() {
			String allSelected="";
			
			int len=dataList.size();
	        for(int i=0; i<len;i++) {
	        	if(isSelected.get(i)) {
	        		if(!allSelected.equalsIgnoreCase("")) allSelected+=";";
	        		allSelected = allSelected+dataList.get(i);
	        	}
	        }
			
	        return allSelected;
		}

	}

}

