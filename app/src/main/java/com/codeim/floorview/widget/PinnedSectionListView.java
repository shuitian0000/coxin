package com.codeim.floorview.widget;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.MeasureSpec;
import android.widget.AbsListView;
import android.widget.HeaderViewListAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;

import com.codeim.floorview.adapter.CommentArrayAdapter;
import com.codeim.floorview.view.PullToRefreshListView;
import com.codeim.floorview.view.PullRefreshAndLoadMoreListView.OnLoadMoreListener;
import com.codeim.coxin.R;

public class PinnedSectionListView extends PullToRefreshListView{
    protected static final String TAG = "LoadMoreListView";
    protected View mFooterView;
    protected OnScrollListener mOnScrollListener;
    protected OnLoadMoreListener mOnLoadMoreListener;

    /**
     * If is loading now.
     */
    protected boolean mIsLoading;
    public boolean data_finish;

    protected int mCurrentScrollState;
	
	private View sectionItemView;
    /** Shadow for being recycled, can be null. */
    PinnedSection mRecycleSection;
	
    /** shadow instance with a pinned view, can be null. */
    PinnedSection mPinnedSection;
    
    /** Pinned view Y-translation. We use it to stick pinned view to the next section. */
    int mTranslateY;
    // fields used for drawing shadow under a pinned section
    private GradientDrawable mShadowDrawable;
    private int mSectionsDistanceY;
    private int mShadowHeight;
    private int mTouchSlop;
	
    //-- inner classes

	/** List adapter to be implemented for being used with PinnedSectionListView adapter. */
	public static interface PinnedSectionListAdapter extends ListAdapter {
		/** This method shall return 'true' if views of given type has to be pinned. */
		boolean isItemViewTypePinned(int viewType);
	}

	/** Wrapper class for pinned section view and its position in the list. */
	static class PinnedSection {
		public View view;
		public int position;
		public long id;
	}
	
    public PinnedSectionListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
        initView();
    }

    public PinnedSectionListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        initView();
    }

    public PinnedSectionListView(Context context) {
        super(context);
        init(context);
        initView();
    }
    
    protected void init(Context context) {
        mFooterView = View.inflate(context, R.layout.comment_load_more_footer, null);
        addFooterView(mFooterView);
        hideFooterView();
        /*
         * Must use super.setOnScrollListener() here to avoid override when call this view's setOnScrollListener method
         */
        super.setOnScrollListener(superOnScrollListener);
    }
    
    /**
     * Hide the load more view(footer view)
     */
    protected void hideFooterView() {
        mFooterView.setVisibility(View.GONE);
    }
    
    /**
     * Show load more view
     */
    protected void showFooterView() {
        mFooterView.setVisibility(View.VISIBLE);
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mOnScrollListener = l;
    }
    
    /**
     * Set load more listener, usually you should get more data here.
     * 
     * @param listener OnLoadMoreListener
     * @see OnLoadMoreListener
     */
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
    }

    /**
     * When complete load more data, you must use this method to hide the footer view, if not the footer view will be
     * shown all the time.
     */
    public void onLoadMoreComplete() {
        mIsLoading = false;
        hideFooterView();
    }
    
    /**
     * Interface for load more
     */
    public interface OnLoadMoreListener {
        /**
         * Load more data.
         */
        void onLoadMore();
    }
    
    private void initView() {
        setOnScrollListener(mOnScrollListener);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        initShadow(true);
    }
    
    private OnScrollListener superOnScrollListener = new OnScrollListener() {
//    superOnScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            mCurrentScrollState = scrollState;
            // Avoid override when use setOnScrollListener
            if (mOnScrollListener != null) {
                mOnScrollListener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (mOnScrollListener != null) {
                mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
            // The count of footer view will be add to visibleItemCount also are
            // added to totalItemCount
            if (visibleItemCount == totalItemCount) {
                // If all the item can not fill screen, we should make the
                // footer view invisible.
//                hideFooterView();
                
                //add by ywwang for length screen, the item can not fill screen, but the data not all
                if(!data_finish&&!mIsLoading) {
                	showFooterView();
                    mIsLoading = true;
                	if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                    }
                } else {
                	hideFooterView();
                }
            } else if (!mIsLoading && (firstVisibleItem + visibleItemCount >= totalItemCount)
                    && mCurrentScrollState != SCROLL_STATE_IDLE) {
                showFooterView();
                mIsLoading = true;
                if (mOnLoadMoreListener != null) {
                    mOnLoadMoreListener.onLoadMore();
                }
            }
            
            // get expected adapter or fail fast
            ListAdapter adapter = getAdapter();
            if (adapter == null || visibleItemCount == 0) return; // nothing to do

            int firstVisibleItem_1=firstVisibleItem;
            if (adapter instanceof HeaderViewListAdapter) {
                adapter = ((HeaderViewListAdapter)adapter).getWrappedAdapter();
                firstVisibleItem_1=firstVisibleItem==0?0:firstVisibleItem-1;
            }
            final boolean isFirstVisibleItemSection =
                    isItemViewTypePinned(adapter, ((CommentArrayAdapter)adapter).getItemViewType(firstVisibleItem_1));

            if (isFirstVisibleItemSection) {
                View sectionView = getChildAt(0);
                if (sectionView.getTop() == getPaddingTop()) { // view sticks to the top, no need for pinned shadow
                    destroyPinnedShadow();
                } else { // section doesn't stick to the top, make sure we have a pinned shadow
                    ensureShadowForPosition(firstVisibleItem, firstVisibleItem, visibleItemCount);
                }

            } else { // section is not at the first visible position
                int sectionPosition = findCurrentSectionPosition(firstVisibleItem);
                if (sectionPosition > -1) { // we have section position
                    ensureShadowForPosition(sectionPosition, firstVisibleItem, visibleItemCount);
                } else { // there is no section for the first visible item, destroy shadow
                    destroyPinnedShadow();
                }
            }
            
//            CommentArrayAdapter mAdapter = (CommentArrayAdapter)getAdapter();
//            if(mAdapter!=null && firstVisibleItem>2) {
//				sectionItemView = mInflater.inflate(R.layout.comment_section_item, view, false);
//				sectionItemViewHolder = new SectionItemViewHolder();
//				sectionItemViewHolder.section_txt = (TextView) sectionItemView.findViewById(R.id.section_txt);
//				
//				sectionItemViewHolder.section_txt.setText("评论");
//            } else {
//            	
//            }
        }
    };
    
	/** Default change observer. */
    private final DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override public void onChanged() {
            recreatePinnedShadow();
        };
        @Override public void onInvalidated() {
            recreatePinnedShadow();
        }
    };
    
	@Override
	public void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);
		post(new Runnable() {
			@Override public void run() { // restore pinned view after configuration change
			    recreatePinnedShadow();
			}
		});
	}
	
	@Override
	public void setAdapter(ListAdapter adapter) {

	    // assert adapter in debug mode
//		if (BuildConfig.DEBUG && adapter != null) {
	    if (adapter != null) {
			if (!(adapter instanceof PinnedSectionListAdapter))
				throw new IllegalArgumentException("Does your adapter implement PinnedSectionListAdapter?");
			if (adapter.getViewTypeCount() < 2)
				throw new IllegalArgumentException("Does your adapter handle at least two types" +
						" of views in getViewTypeCount() method: items and sections?");
		}

		// unregister observer at old adapter and register on new one
		ListAdapter oldAdapter = getAdapter();
		if (oldAdapter != null) oldAdapter.unregisterDataSetObserver(mDataSetObserver);
		if (adapter != null) adapter.registerDataSetObserver(mDataSetObserver);

		// destroy pinned shadow, if new adapter is not same as old one
		if (oldAdapter != adapter) destroyPinnedShadow();

		super.setAdapter(adapter);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	    super.onLayout(changed, l, t, r, b);
        if (mPinnedSection != null) {
            int parentWidth = r - l - getPaddingLeft() - getPaddingRight();
            int shadowWidth = mPinnedSection.view.getWidth();
            if (parentWidth != shadowWidth) {
                recreatePinnedShadow();
            }
        }
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		if (mPinnedSection != null) {

			// prepare variables
			int pLeft = getListPaddingLeft();
			int pTop = getListPaddingTop();
			View view = mPinnedSection.view;

			// draw child
			canvas.save();

			int clipHeight = view.getHeight() +
			        (mShadowDrawable == null ? 0 : Math.min(mShadowHeight, mSectionsDistanceY));
			canvas.clipRect(pLeft, pTop, pLeft + view.getWidth(), pTop + clipHeight);

			canvas.translate(pLeft, pTop + mTranslateY);
			drawChild(canvas, mPinnedSection.view, getDrawingTime());

			if (mShadowDrawable != null && mSectionsDistanceY > 0) {
			    mShadowDrawable.setBounds(mPinnedSection.view.getLeft(),
			            mPinnedSection.view.getBottom(),
			            mPinnedSection.view.getRight(),
			            mPinnedSection.view.getBottom() + mShadowHeight);
			    mShadowDrawable.draw(canvas);
			}

			canvas.restore();
		}
	}
    
    public static boolean isItemViewTypePinned(ListAdapter adapter, int viewType) {
        if (adapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter)adapter).getWrappedAdapter();
        }
    	return ((PinnedSectionListAdapter) adapter).isItemViewTypePinned(viewType);
    	
//    	if (adapter instanceof CommentArrayAdapter) {
//            return (((CommentArrayAdapter)adapter).isItemViewTypePinned(viewType));
//    	} else {
//    		return false;
//    	}
    }
    
    //-- public API methods

    public void setShadowVisible(boolean visible) {
        initShadow(visible);
        if (mPinnedSection != null) {
            View v = mPinnedSection.view;
            invalidate(v.getLeft(), v.getTop(), v.getRight(), v.getBottom() + mShadowHeight);
        }
    }
    
    //-- pinned section drawing methods

    public void initShadow(boolean visible) {
        if (visible) {
            if (mShadowDrawable == null) {
                mShadowDrawable = new GradientDrawable(Orientation.TOP_BOTTOM,
                        new int[] { Color.parseColor("#ffa0a0a0"), Color.parseColor("#50a0a0a0"), Color.parseColor("#00a0a0a0")});
                mShadowHeight = (int) (8 * getResources().getDisplayMetrics().density);
            }
        } else {
            if (mShadowDrawable != null) {
                mShadowDrawable = null;
                mShadowHeight = 0;
            }
        }
    }
    
	/** Create shadow wrapper with a pinned view for a view at given position */
	void createPinnedShadow(int position) {

		// try to recycle shadow
		PinnedSection pinnedShadow = mRecycleSection;
		mRecycleSection = null;

		// create new shadow, if needed
		if (pinnedShadow == null) pinnedShadow = new PinnedSection();
		// request new view using recycled view, if such
		View pinnedView = getAdapter().getView(position, pinnedShadow.view, PinnedSectionListView.this);

		// read layout parameters
		LayoutParams layoutParams = (LayoutParams) pinnedView.getLayoutParams();
		if (layoutParams == null) { // create default layout params
		    layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		}

		int heightMode = MeasureSpec.getMode(layoutParams.height);
		int heightSize = MeasureSpec.getSize(layoutParams.height);

		if (heightMode == MeasureSpec.UNSPECIFIED) heightMode = MeasureSpec.EXACTLY;

		int maxHeight = getHeight() - getListPaddingTop() - getListPaddingBottom();
		if (heightSize > maxHeight) heightSize = maxHeight;

		// measure & layout
		int ws = MeasureSpec.makeMeasureSpec(getWidth() - getListPaddingLeft() - getListPaddingRight(), MeasureSpec.EXACTLY);
		int hs = MeasureSpec.makeMeasureSpec(heightSize, heightMode);
		pinnedView.measure(ws, hs);
		pinnedView.layout(0, 0, pinnedView.getMeasuredWidth(), pinnedView.getMeasuredHeight());
		mTranslateY = 0;

		// initialize pinned shadow
		pinnedShadow.view = pinnedView;
		pinnedShadow.position = position;
		pinnedShadow.id = getAdapter().getItemId(position);

		// store pinned shadow
		mPinnedSection = pinnedShadow;
	}
    
	/** Destroy shadow wrapper for currently pinned view */
	void destroyPinnedShadow() {
	    if (mPinnedSection != null) {
	        // keep shadow for being recycled later
	        mRecycleSection = mPinnedSection;
	        mPinnedSection = null;
	    }
	}
	
	/** Makes sure we have an actual pinned shadow for given position. */
    void ensureShadowForPosition(int sectionPosition, int firstVisibleItem, int visibleItemCount) {
        if (visibleItemCount < 2) { // no need for creating shadow at all, we have a single visible item
            destroyPinnedShadow();
            return;
        }

        if (mPinnedSection != null
                && mPinnedSection.position != sectionPosition) { // invalidate shadow, if required
            destroyPinnedShadow();
        }

        if (mPinnedSection == null) { // create shadow, if empty
            createPinnedShadow(sectionPosition);
        }

        // align shadow according to next section position, if needed
        int nextPosition = sectionPosition + 1;
        if (nextPosition < getCount()) {
            int nextSectionPosition = findFirstVisibleSectionPosition(nextPosition,
                    visibleItemCount - (nextPosition - firstVisibleItem));
            if (nextSectionPosition > -1) {
                View nextSectionView = getChildAt(nextSectionPosition - firstVisibleItem);
                final int bottom = mPinnedSection.view.getBottom() + getPaddingTop();
                mSectionsDistanceY = nextSectionView.getTop() - bottom;
                if (mSectionsDistanceY < 0) {
                    // next section overlaps pinned shadow, move it up
                    mTranslateY = mSectionsDistanceY;
                } else {
                    // next section does not overlap with pinned, stick to top
                    mTranslateY = 0;
                }
            } else {
                // no other sections are visible, stick to top
                mTranslateY = 0;
                mSectionsDistanceY = Integer.MAX_VALUE;
            }
        }

    }
    
	int findFirstVisibleSectionPosition(int firstVisibleItem, int visibleItemCount) {
		ListAdapter adapter = getAdapter();
		for (int childIndex = 0; childIndex < visibleItemCount; childIndex++) {
			int position = firstVisibleItem + childIndex;
			int viewType = adapter.getItemViewType(position);
			if (isItemViewTypePinned(adapter, viewType)) return position;
		}
		return -1;
	}

	int findCurrentSectionPosition(int fromPosition) {
		ListAdapter adapter = getAdapter();

		if (adapter instanceof SectionIndexer) {
			// try fast way by asking section indexer
			SectionIndexer indexer = (SectionIndexer) adapter;
			int sectionPosition = indexer.getSectionForPosition(fromPosition);
			int itemPosition = indexer.getPositionForSection(sectionPosition);
			int typeView = adapter.getItemViewType(itemPosition);
			if (isItemViewTypePinned(adapter, typeView)) {
				return itemPosition;
			} // else, no luck
		}

		// try slow way by looking through to the next section item above
		for (int position=fromPosition; position>=0; position--) {
			int viewType = adapter.getItemViewType(position);
			if (isItemViewTypePinned(adapter, viewType)) return position;
		}
		return -1; // no candidate found
	}

	void recreatePinnedShadow() {
	    destroyPinnedShadow();
        ListAdapter adapter = getAdapter();
        if (adapter != null && adapter.getCount() > 0) {
            int firstVisiblePosition = getFirstVisiblePosition();
            int sectionPosition = findCurrentSectionPosition(firstVisiblePosition);
            if (sectionPosition == -1) return; // no views to pin, exit
            ensureShadowForPosition(sectionPosition,
                    firstVisiblePosition, getLastVisiblePosition() - firstVisiblePosition);
        }
	}
    
}
