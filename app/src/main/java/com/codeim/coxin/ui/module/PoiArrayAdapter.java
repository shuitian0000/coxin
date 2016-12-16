package com.codeim.coxin.ui.module;

//import android.app.AlertDialog;
//import android.app.AlertDialog.Builder;
//import android.app.Dialog;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
//import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
// import android.widget.ListView;
import android.widget.TextView;
//import android.widget.Toast;

import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.codeim.coxin.PoiSearchActivity;
import com.codeim.coxin.R;

/*
 * 用于用户的Adapter
 */
//public class MsgTypeArrayAdapter extends BaseAdapter implements TweetAdapter, OnCompletionListener, OnErrorListener {
public class PoiArrayAdapter extends BaseAdapter implements TweetAdapter, OnClickListener {
	private static final String TAG = "PoiArrayAdapter";
	
	public static final int  INFO_TYPE = 0;
	public static final int  SEARCH_TYPE = 1;

	protected List<PoiInfo> mPoiInfos;
	private Context mContext;
	protected LayoutInflater mInflater;
	
	private int curPosition;
	
	private double latitude;
	private double longitude;
	private String loc_city;
	public SuggestionSearch mSuggestionSearch = null;
	private OnGetSuggestionResultListener mGetSuggestionResultListener;
	private ArrayAdapter<String> sugAdapter = null;
	private String keyword;

	public PoiArrayAdapter(Context context, double latitude, double longitude, String city) {
		mPoiInfos = new ArrayList<PoiInfo>();
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		
		//special action: for suggestion search
		this.latitude = latitude;
		this.longitude = longitude;
		this.loc_city = city;
		this.keyword = "";
		sugAdapter = new ArrayAdapter<String>(((PoiSearchActivity)mContext),
				android.R.layout.simple_dropdown_item_1line);
		mSuggestionSearch = SuggestionSearch.newInstance();
		mGetSuggestionResultListener = new OnGetSuggestionResultListener() {
			@Override
			public void onGetSuggestionResult(SuggestionResult arg0) {
				// TODO Auto-generated method stub
		        if (arg0 == null || arg0.getAllSuggestions() == null) {  
		            return;
		            //未找到相关结果  
		        }  
		        //获取在线建议检索结果 
		        sugAdapter.clear();
				for (SuggestionResult.SuggestionInfo info : arg0.getAllSuggestions()) {
					if (info.key != null)
						sugAdapter.add(info.key);
				}
				sugAdapter.notifyDataSetChanged();
			}
		};
		mSuggestionSearch.setOnGetSuggestionResultListener(mGetSuggestionResultListener);
	}

	@Override
	public int getCount() {
		Log.v("Poi_count", String.valueOf(mPoiInfos.size()));
		return mPoiInfos.size();
	}

	@Override
	public PoiInfo getItem(int position) {
		return mPoiInfos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
    @Override 
    public int getViewTypeCount() {  
        return 2;  
    }  
       
    @Override 
    public int getItemViewType(int position) {
        if (position==0) {
            return SEARCH_TYPE;
        } else {
            return INFO_TYPE;  
        }  
    } 

  //第一个Item的ViewHolder  
    private class SearchItemViewHolder{  
		public AutoCompleteTextView keyWorldsView;
		public ImageView search_iv_delete;
		public Button search_btn;
    } 
    
	private static class ViewHolder {
        public TextView poi_info_txt; 
	}
	
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view;
		ViewGroup floor;
		
		view = convertView;
		ViewHolder holder;
		SearchItemViewHolder searchItemViewHolder;
		
		int currentType= getItemViewType(position);
		View SearchItemView = null;
		View PoiInfoItemView = null;
		
		if(currentType== INFO_TYPE) {
			PoiInfoItemView = convertView;
			if(PoiInfoItemView == null) {
				PoiInfoItemView = mInflater.inflate(R.layout.poi_list_item, parent, false);
				holder = new ViewHolder();
				
				holder.poi_info_txt = (TextView) PoiInfoItemView.findViewById(R.id.poi_info_txt);
		        
				PoiInfoItemView.setTag(holder);
			}
			else {
				holder = (ViewHolder) PoiInfoItemView.getTag();
			}
			
			PoiInfo poiInfo = mPoiInfos.get(position);
			holder.poi_info_txt.setText(poiInfo.address);
			
			return PoiInfoItemView;
		}
		//else if(currentType== SEARCH_TYPE) {
		else {
			SearchItemView = convertView;
			if(SearchItemView == null) {
				SearchItemView = mInflater.inflate(R.layout.poi_sug_item, parent, false);
				searchItemViewHolder = new SearchItemViewHolder();
				
				searchItemViewHolder.keyWorldsView = (AutoCompleteTextView) SearchItemView.findViewById(R.id.searchkey);
				searchItemViewHolder.search_iv_delete = (ImageView) SearchItemView.findViewById(R.id.search_iv_delete);
				searchItemViewHolder.search_btn = (Button) SearchItemView.findViewById(R.id.search_btn);
				
				SearchItemView.setTag(searchItemViewHolder);
			}
			else {
				searchItemViewHolder = (SearchItemViewHolder) SearchItemView.getTag();
			}
			
			searchItemViewHolder.keyWorldsView.setTag(position);
			searchItemViewHolder.search_iv_delete.setTag(position);
			searchItemViewHolder.search_btn.setTag(position);
			final AutoCompleteTextView keyWorldsView =searchItemViewHolder.keyWorldsView;
			final ImageView search_iv_delete = searchItemViewHolder.search_iv_delete;
			
			if(sugAdapter==null) {
				sugAdapter = new ArrayAdapter<String>(((PoiSearchActivity)mContext),
						android.R.layout.simple_dropdown_item_1line);
			}
			sugAdapter.clear();
			keyWorldsView.setAdapter(sugAdapter);
			/**
			 * 当输入关键字变化时，动态更新建议列表
			 */
			keyWorldsView.addTextChangedListener(new TextWatcher() {
				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1,
						int arg2, int arg3) {

				}

				@Override
				public void onTextChanged(CharSequence cs, int arg1, int arg2,
						int arg3) {
					if (cs.length() <= 0) {
						search_iv_delete.setVisibility(View.GONE);
						return;
					}
					search_iv_delete.setVisibility(View.VISIBLE);
//					String city = ((EditText) findViewById(R.id.city)).getText()
//							.toString();
					/**
					 * 使用建议搜索服务获取建议列表，结果在onSuggestionResult()中更新
					 */
					keyword = cs.toString();
					mSuggestionSearch
							.requestSuggestion((new SuggestionSearchOption())
									.keyword(cs.toString())
									.city(loc_city)
									.location(new LatLng(latitude, longitude))
									);
				}

				@Override
				public void afterTextChanged(Editable s) {
					// TODO Auto-generated method stub
					
				}
			});
			
			searchItemViewHolder.search_iv_delete.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					keyWorldsView.setText("");
				}
			});
			
			searchItemViewHolder.search_btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					InputMethodManager imm =(InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE); 
					imm.hideSoftInputFromWindow(keyWorldsView.getWindowToken(), 0);
					String key_string = keyWorldsView.getText().toString();
					if(key_string!="") {
					    mOnSearchClick.SearchClick(key_string);
					} else {
						Toast.makeText(mContext, "Please input the search",
								Toast.LENGTH_SHORT).show();
					}
				}
			});
			
			return SearchItemView;
		}
	}

	@Override
	public void onClick(View v) {
		int position = (Integer) v.getTag();
//		switch (v.getId()) {	    
//		    case R.id.down_arrow:
//		    	Log.e(TAG, "R.id.down_arrow position: "+String.valueOf(position));
//			    mOnClickDownArrow.downArrowPop(position);
//			    break;
////		    case R.id.txt_expire_time:
//		    case R.id.info_item_footer_status:
//		    	Log.e(TAG, "R.id.info_item_footer_status: "+String.valueOf(position));
//		    	mOnTimeSet.clickTimeSet(position);
//		    	break;
//		}
	}
	private OnClickDownArrow mOnClickDownArrow;
	public void setOnClickDownArrow(OnClickDownArrow l) {
		mOnClickDownArrow = l;
	}
	public interface OnClickDownArrow {
		public void downArrowPop(int position);
	}
	
	private OnSearchClick mOnSearchClick;
	public void setSearchClick(OnSearchClick l) {
		mOnSearchClick = l;
	}
	public interface OnSearchClick {
		public void SearchClick(String key_str);
	}

	public void refresh(List<PoiInfo> poiInfos) {		
		Log.v("adapter", String.valueOf(poiInfos.size()));
		
		this.mPoiInfos = poiInfos;
		
		notifyDataSetChanged();
	}
	
	@Override
	public void refresh() {
		notifyDataSetChanged();
	}
	
	protected void launchActivity(Intent intent) {
		mContext.startActivity(intent);
	}
	
	public String getKeyword() {
		return keyword;
	}
//	private ArrayList<String> getIntentArrayList(ArrayList<String> dataList) {
//		ArrayList<String> tDataList = new ArrayList<String>();
//		for (String s : dataList) {
//			if (!s.contains("default")) {
//				tDataList.add(s);
//			}
//		}
//		return tDataList;
//	}
	
}

