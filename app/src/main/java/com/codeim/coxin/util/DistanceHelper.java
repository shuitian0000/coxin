package com.codeim.coxin.util;

import java.text.DecimalFormat;

public class DistanceHelper {
    private static final String TAG = "DistanceHelper";
	
	public static String distanceConvert(int distance) {
		double resultFloat;
		int resultInt;
		DecimalFormat format = new DecimalFormat("#.00");
		
		if (distance < 1000) {
		    resultFloat = distance/1000.0;
			String s = "0" + format.format(resultFloat) + "km";
			return s;
		}
		else if (distance >= 1000 && distance < 100000) {
		    resultFloat = distance/1000.0;
			String s = format.format(resultFloat) + "km";
			return s;
		} else {
		    resultInt = distance/1000;
			String s = String.valueOf(resultInt) + "km";
			return s;
		}
	}
}