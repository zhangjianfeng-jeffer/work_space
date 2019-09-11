package com.test;

import com.yolo.common.utils.StringUtils;

public class StringUtilsTest {

	public static void main(String[] args) {
		System.out.println(StringUtils.toLowerCaseFirst("1Xjdhsjd"));
		System.out.println(StringUtils.getFieldNameByParamName("userId"));
		System.out.println(StringUtils.getParamNameByFieldName("USER_ID"));
		System.out.println(StringUtils.getParamNameByFieldName("user_id"));
		
	}
	
}
