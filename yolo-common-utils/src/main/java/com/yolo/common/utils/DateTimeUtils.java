package com.yolo.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {

	public static final String FORMAT_Y_M_D_1 = "yyyy-MM-dd";
	public static final String FORMAT_Y_M_D_2 = "yyyyMMdd";
	public static final String FORMAT_Y_M_D_H_M_S_S_1 = "yyyy-MM-dd HH:mm:ss.S";
	
	public static Date stringFormatDate(String date,String format)throws Exception{
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		Date result = dateFormat.parse(date);
		return result;
	}
	
	
	public static String dateFormatString(Date date,String format)throws Exception{
		SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		String result = dateFormat.format(date);
		return result;
	}
}
