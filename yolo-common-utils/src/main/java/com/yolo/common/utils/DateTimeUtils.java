package com.yolo.common.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {
	public enum FormatType{
		FORMAT_Y_M_D_1("yyyy-MM-dd"),
		FORMAT_Y_M_D_2("yyyyMMdd"),
		FORMAT_Y_M_D_3("yyMMdd"),
		FORMAT_Y_M_D_H_M_S_1("yyyy-MM-dd HH:mm:ss"),
		FORMAT_Y_M_D_H_M_S_S_1("yyyy-MM-dd HH:mm:ss.S"),
		FORMAT_M_D_1("MM-dd"),
		FORMAT_M_D_2("MMdd");
		
		private String value;
		private FormatType(String value){
			this.value = value;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}
		
	/**
	 *	时间字符串转换为时间Date
	 * @param date
	 * @param format
	 * @return
	 * @throws Exception
	 */
	public static Date stringFormatDate(String date,FormatType format)throws Exception{
		SimpleDateFormat dateFormat = new SimpleDateFormat(format.getValue());
		Date result = dateFormat.parse(date);
		return result;
	}
	
	
	/**
	 * 时间Date转换为时间字符串
	 * @param date
	 * @param format
	 * @return
	 * @throws Exception
	 */
	public static String dateFormatString(Date date,FormatType format)throws Exception{
		SimpleDateFormat dateFormat = new SimpleDateFormat(format.getValue());
		String result = dateFormat.format(date);
		return result;
	}
}
