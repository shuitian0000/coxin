package com.codeim.coxin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.codeim.coxin.app.Preferences;
import com.codeim.coxin.task.GenericTask;
import com.codeim.coxin.ui.base.BaseNoDoubleClickActivity;
import com.codeim.coxin.ui.module.Feedback;
import com.codeim.coxin.ui.module.FeedbackFactory;
import com.codeim.coxin.ui.module.NavBar;
import com.codeim.coxin.ui.module.FeedbackFactory.FeedbackType;
import com.codeim.coxin.ui.module.PoiArrayAdapter;
import com.codeim.coxin.ui.module.PoiArrayAdapter.OnSearchClick;
import com.codeim.floorview.utils.DateFormatUtils;
import com.codeim.floorview.view.PullRefreshAndLoadMoreListView.OnLoadMoreListener;
import com.codeim.floorview.view.PullToRefreshListView.OnRefreshListener;

public class PoiSearchActivity extends BaseNoDoubleClickActivity {
//public class PoiSearchActivity extends CommentActivity {
	private static final String TAG = "PoiSearchActivity";
	
	private NavBar mNavBar;
    protected Feedback mFeedback;
    private LayoutInflater inflater ;
	
	private com.codeim.floorview.view.PullRefreshAndLoadMoreListView container;
    private List < PoiInfo > datas ;
	protected PoiArrayAdapter mPoiListAdapter;
	
	private double latitude;
	private double longitude;
	private String loc_city;
	private List<PoiInfo> mPoiInfo_list = new ArrayList<PoiInfo> ();
	private PoiSearch mPoiSearch = null;
	private OnGetPoiSearchResultListener mPoiListener;
//	private SuggestionSearch mSuggestionSearch = null;
//	private OnGetSuggestionResultListener mGetSuggestionResultListener;
//	private ArrayAdapter<String> sugAdapter = null;
	
    protected int page_size;
    protected int page_index;
    protected int last_id;
    protected boolean no_data;
	
	@Override
	protected boolean _onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "PoiSearch OnCreate start");

		if (super._onCreate(savedInstanceState)) {
			setContentView(R.layout.poi_search_main);
			
	        Bundle bundle = getIntent().getExtras();
	        latitude = bundle.getDouble("latitude");
	        longitude = bundle.getDouble("longitude");
	        loc_city = bundle.getString("city");

			mFeedback = FeedbackFactory.create(this, FeedbackType.PROGRESS);
			mNavBar = new NavBar(NavBar.HEADER_STYLE_BACK, this);
			mNavBar.setHeaderTitle("搜索位置");
			
			inflater = this.getLayoutInflater () ;
			container = ( com.codeim.floorview.view.PullRefreshAndLoadMoreListView ) findViewById ( R.id.container ) ;

			mPoiListAdapter = new PoiArrayAdapter(this, latitude, longitude, loc_city);
			container.setAdapter(mPoiListAdapter);
			
			setupListHeader(false);
			
			registerOnClickListener(container);
			
		    page_size=20;
		    page_index=0;
		    last_id=0;
		    container.data_finish = false; //data_finish need set in the real application
		    no_data=false;
			
	        Log.d ( "systemtime", DateFormatUtils.format ( new Date ( System.currentTimeMillis ()) ) ) ;
	        
			doDraw();

			return true;
		} else {
			return false;
		}
	}
	
    /**
     * 绑定listView底部 - 载入更多 NOTE: 必须在listView#setAdapter之前调用
     */
    protected void setupListHeader(boolean addFooter) {
		mPoiSearch = PoiSearch.newInstance();
		mPoiListener = new OnGetPoiSearchResultListener(){
			@Override
			public void onGetPoiDetailResult(PoiDetailResult arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

			}

			@Override
			public void onGetPoiResult(PoiResult arg0) {
				// TODO Auto-generated method stub
			    if (arg0 == null || arg0.error != SearchResult.ERRORNO.NO_ERROR) {  
			        return;  
			    }
			    
	        	container.onRefreshComplete();
	        	//if(task == mGetMoreTask) {
	        		container.onLoadMoreComplete();
	        	//}
			    if((arg0.getCurrentPageNum()+1)>=arg0.getTotalPageNum()) {
			    	container.data_finish = true;
			    }
			    
	            List<PoiInfo> poiInfo_list = arg0.getAllPoi();
	            if(poiInfo_list !=null && poiInfo_list.size()>0) {
		            mPoiInfo_list.addAll(arg0.getAllPoi());
	            }
	            mPoiListAdapter.refresh(mPoiInfo_list);
			}
		};
		mPoiSearch.setOnGetPoiSearchResultListener(mPoiListener);		
//		sugAdapter = new ArrayAdapter<String>(this,
//				android.R.layout.simple_dropdown_item_1line);
//		mSuggestionSearch = SuggestionSearch.newInstance();
//		mGetSuggestionResultListener = new OnGetSuggestionResultListener() {
//			@Override
//			public void onGetSuggestionResult(SuggestionResult arg0) {
//				// TODO Auto-generated method stub
//		        if (arg0 == null || arg0.getAllSuggestions() == null) {  
//		            return;  
//		            //未找到相关结果  
//		        }  
//		        //获取在线建议检索结果 
//		        
//			}
//		};
//		mSuggestionSearch.setOnGetSuggestionResultListener(mGetSuggestionResultListener);
		
        // Add Header to ListView
        // mListHeader = View.inflate(this, R.layout.listview_header, null);
        // mTweetList.addHeaderView(mListHeader, null, true);
    	container.setOnRefreshListener(new OnRefreshListener(){
    		@Override
    		public void onRefresh(){
//    			doRetrieve();
    			doDraw();
    		}
    	});
    	
		container.setOnLoadMoreListener(new OnLoadMoreListener() {

			@Override
			public void onLoadMore() {
				doGetMore();
			}
		});

//        // Add Footer to ListView
//        mListFooter = View.inflate(this, R.layout.listview_footer, null);
//        mTweetList.addFooterView(mListFooter, null, true);
//        
//        // Find View
//        loadMoreBtn = (TextView) findViewById(R.id.ask_for_more);
//        loadMoreGIF = (ProgressBar) findViewById(R.id.rectangleProgressBar);
//        loadMoreBtnTop = (TextView) findViewById(R.id.ask_for_more_header);
//        loadMoreGIFTop = (ProgressBar) findViewById(R.id.rectangleProgressBar_header);
    }
	
    protected void doDraw() {
        Log.d(TAG, "Attempting retrieve.");
        
        mPoiInfo_list.clear();
        mPoiInfo_list.add(new PoiInfo()); //for the first list item, not the real PoiInfo
        
        page_index = 0;
        container.data_finish = false;
        if(mPoiListAdapter!=null && mPoiListAdapter.getKeyword()!="") { //PoiSearch
            mPoiSearch.searchNearby((new PoiNearbySearchOption())
        		.location(new LatLng(latitude, longitude))
        		.keyword(mPoiListAdapter.getKeyword())
        		.pageCapacity(page_size)
        		.pageNum(0)
        		.radius(1000)
        		);   
        } else {
        	reverseGeoCode(new LatLng(latitude, longitude));
        }
        page_index++; 
    }
    protected void doGet(String arg0) {
        Log.d(TAG, "Attempting Get.");
        
        mPoiInfo_list.clear();
        mPoiInfo_list.add(new PoiInfo()); //for the first list item, not the real PoiInfo
        page_index=0;
        container.data_finish = false;
        
        mPoiSearch.searchNearby((new PoiNearbySearchOption())
        		.location(new LatLng(latitude, longitude))
        		.keyword(arg0)
        		.pageCapacity(page_size)
        		.pageNum(0)
        		.radius(1000)
        		);
        page_index++;
        
        //reverseGeoCode(new LatLng(latitude, longitude));
    }
    protected void doGetMore() {
        Log.d(TAG, "Attempting getMore.");
        
        if(mPoiListAdapter!=null && mPoiListAdapter.getKeyword()!="") { //PoiSearch
            mPoiSearch.searchNearby((new PoiNearbySearchOption())
        		.location(new LatLng(latitude, longitude))
        		.keyword(mPoiListAdapter.getKeyword())
        		.pageCapacity(page_size)
        		.pageNum(page_index)
        		.radius(1000)
        		);
            page_index++;
        } else {
            //all data in server have been got
            if(container.data_finish) {
            	container.onLoadMoreComplete();
            	return;
            }
        }

//        //all data in server have been got
//        if(container.data_finish) {
//        	container.onLoadMoreComplete();
//        	return;
//        }
        //the real action of getMore
    }
    
	protected void registerOnClickListener(ListView listView) {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(mPoiListAdapter.getItemViewType(position)!=0) {
					return;
				}
				
				//final Comment comment = getContextItemTweet(position);
				final PoiInfo mPoiInfo = getContextItem(position);

				if(position>1) { //should return Result, and then finish
//				    Toast.makeText(getApplicationContext(), mPoiInfo.address,
//						Toast.LENGTH_SHORT).show();
					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					// intent.putArrayListExtra("dataList", dataList);
					bundle.putString("place",mPoiInfo.address);
					intent.putExtras(bundle);
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		});
		
		mPoiListAdapter.setSearchClick(new OnSearchClick() {
			@Override
			public void SearchClick(String key_str) { //execute the PoiSearch function
				// TODO Auto-generated method stub
				doGet(key_str);
			}
			
		});
	}
	
	protected PoiInfo getContextItem(int position) {
        position = position - 1;
        // 因为List加了Header和footer，所以要跳过第一个以及忽略最后一个
        if (position >= 0 && position < mPoiListAdapter.getCount()) {
        	PoiInfo mPoiInfo = (PoiInfo) mPoiListAdapter.getItem(position);
            if (mPoiInfo == null) {
                return null;
            } else {
                return mPoiInfo;
            }
        } else {
            return null;
        }
	}
	
	/**
	 * 反地理编码得到地址信息
	 */
	private void reverseGeoCode(LatLng latLng) {
		// 创建地理编码检索实例
		GeoCoder geoCoder = GeoCoder.newInstance();
		//
		OnGetGeoCoderResultListener listener = new OnGetGeoCoderResultListener() {
	        // 反地理编码查询结果回调函数  
	        @Override  
	        public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {  
	            if (arg0 == null  
	                    || arg0.error != SearchResult.ERRORNO.NO_ERROR) {  
	                // 没有检测到结果  
//	                Toast.makeText(MainActivity.this, "抱歉，未能找到结果",  
//	                        Toast.LENGTH_LONG).show();  
	            }  
//	            Toast.makeText(MainActivity.this,  
//	                    "位置：" + arg0.getAddress(), Toast.LENGTH_LONG)  
//	                    .show();
//	            txt_location.setText(arg0.getAddress());
//	            sel_location = arg0.getAddress();
	            List<PoiInfo> poiInfo_list = arg0.getPoiList();
	            if(poiInfo_list !=null && poiInfo_list.size()>0) {
		            mPoiInfo_list.addAll(arg0.getPoiList());
	            }
	            mPoiListAdapter.refresh(mPoiInfo_list);
	        }  

	        // 地理编码查询结果回调函数  
	        @Override  
	        public void onGetGeoCodeResult(GeoCodeResult arg0) {  
	            if (arg0 == null  
	                    || arg0.error != SearchResult.ERRORNO.NO_ERROR) {  
	                // 没有检测到结果  
	            }  
	        }
		};
		// 设置地理编码检索监听者
		geoCoder.setOnGetGeoCodeResultListener(listener);
		//
		geoCoder.reverseGeoCode(new ReverseGeoCodeOption()
		    .location(latLng)
		    );
		// 释放地理编码检索实例
		// geoCoder.destroy();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mPoiSearch.destroy();
		//mSuggestionSearch.destroy();
		this.mPoiListAdapter.mSuggestionSearch.destroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}

}
