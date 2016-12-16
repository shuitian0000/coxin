package com.codeim.coxin.util;

public class CommonUtils {
	private static final String TAG = "CommonUtils";
	
	private static long lastClickTime=0;
	
	public static boolean isFastDoubleClick() {
            long time = System.currentTimeMillis();
            long timeD = time - lastClickTime;
            if (timeD >= 0 && timeD <= 1000) {
                return true;
            } else {
                lastClickTime = time;
                return false;
            }
        }


}
