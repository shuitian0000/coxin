package com.codeim.coxin;

import com.codeim.coxin.widget.wheelold.NumericWheelAdapter;
import com.codeim.coxin.widget.wheelold.WheelView;
import com.codeim.coxin.R;

import android.app.Activity;
import android.os.Bundle;

public class Time2Activity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time2_layout);
        
        final WheelView days = (WheelView) findViewById(R.id.day);
        days.setAdapter(new NumericWheelAdapter(0,7));
        days.setLabel("days");
        days.setVisibleItems(5);
        days.setCyclic(false); 
        
//        final WheelView days = (WheelView) findViewById(R.id.day);
//        dayAdapter.setItemResource(R.layout.time2_wheel_text_item);
//        dayAdapter.setItemTextResource(R.id.text);
//        days.setViewAdapter(dayAdapter);
    
//        final WheelView hours = (WheelView) findViewById(R.id.hour);
//        NumericWheelAdapter hourAdapter = new NumericWheelAdapter(this, 0, 24);
//        hourAdapter.setItemResource(R.layout.time2_wheel_text_item);
//        hourAdapter.setItemTextResource(R.id.text);
//        hours.setViewAdapter(hourAdapter);
    
//        final WheelView mins = (WheelView) findViewById(R.id.mins);
//        NumericWheelAdapter minAdapter = new NumericWheelAdapter(this, 0, 59, "%02d");
//        minAdapter.setItemResource(R.layout.time2_wheel_text_item);
//        minAdapter.setItemTextResource(R.id.text);
//        mins.setViewAdapter(minAdapter);
//        mins.setCyclic(true);
        
//        final WheelView ampm = (WheelView) findViewById(R.id.ampm);
//        ArrayWheelAdapter<String> ampmAdapter =
//            new ArrayWheelAdapter<String>(this, new String[] {"AM", "PM"});
//        ampmAdapter.setItemResource(R.layout.wheel_text_item);
//        ampmAdapter.setItemTextResource(R.id.text);
//        ampm.setViewAdapter(ampmAdapter);
    
        // set current time
//        Calendar calendar = Calendar.getInstance(Locale.US);
//        hours.setCurrentItem(calendar.get(Calendar.HOUR));
//        mins.setCurrentItem(calendar.get(Calendar.MINUTE));
//        ampm.setCurrentItem(calendar.get(Calendar.AM_PM));
        
        days.setCurrentItem(3);
//        hours.setCurrentItem(12);
//        mins.setCurrentItem(30);
        
//        final WheelView day = (WheelView) findViewById(R.id.day);
//        day.setViewAdapter(new DayArrayAdapter(this, calendar));        
    }
    
    /**
     * Day adapter
     *
     */
//    private class DayArrayAdapter extends AbstractWheelTextAdapter {
//        // Count of days to be shown
//        private final int daysCount = 20;
//        
//        // Calendar
//        Calendar calendar;
//        
//        /**
//         * Constructor
//         */
//        protected DayArrayAdapter(Context context, Calendar calendar) {
//            super(context, R.layout.time2_day, NO_RESOURCE);
//            this.calendar = calendar;
//            
//            setItemTextResource(R.id.time2_monthday);
//        }
//
//        @Override
//        public View getItem(int index, View cachedView, ViewGroup parent) {
//            int day = -daysCount/2 + index;
//            Calendar newCalendar = (Calendar) calendar.clone();
//            newCalendar.roll(Calendar.DAY_OF_YEAR, day);
//            
//            View view = super.getItem(index, cachedView, parent);
//            TextView weekday = (TextView) view.findViewById(R.id.time2_weekday);
//            if (day == 0) {
//                weekday.setText("");
//            } else {
//                DateFormat format = new SimpleDateFormat("EEE");
//                weekday.setText(format.format(newCalendar.getTime()));
//            }
//
//            TextView monthday = (TextView) view.findViewById(R.id.time2_monthday);
//            if (day == 0) {
//                monthday.setText("Today");
//                monthday.setTextColor(0xFF0000F0);
//            } else {
//                DateFormat format = new SimpleDateFormat("MMM d");
//                monthday.setText(format.format(newCalendar.getTime()));
//                monthday.setTextColor(0xFF111111);
//            }
//
//            return view;
//        }
//        
//        @Override
//        public int getItemsCount() {
//            return daysCount + 1;
//        }
//        
//        @Override
//        protected CharSequence getItemText(int index) {
//            return "";
//        }
//    }
}
