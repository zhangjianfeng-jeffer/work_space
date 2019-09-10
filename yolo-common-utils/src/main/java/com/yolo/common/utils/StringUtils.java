package com.yolo.common.utils;

public class StringUtils {
	
	
	public static boolean isBlank(String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if ((Character.isWhitespace(str.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    
    public static boolean isNotBlank(String str) {
        return !StringUtils.isBlank(str);
    }

    
    
    public static String getFieldNameByParamName(String paramName){
		StringBuffer strB=new StringBuffer();
		if(paramName!=null&&paramName.length()>0){
			char[] values=paramName.toCharArray();
			for(int i=0;i<values.length;i++){
				char c=values[i];
				if(c>='A'&&c<='Z'){
					strB.append("_"+c);
				}else{
					strB.append((c+"").toUpperCase());
				}
			}
		}
		return strB.toString();
	}
	
	public static String getParamNameByFieldName(String fieldName){
		StringBuffer strB=new StringBuffer();
		if(fieldName!=null&&fieldName.length()>0){
			char[] charArr=fieldName.toCharArray();
			if(charArr!=null&&charArr.length>0){
				for(int i=0;i<charArr.length;i++){
					if(i==0){
						strB.append((charArr[i]+"").toLowerCase());
					}else{
						if('_'==charArr[i]){
							i++;
							if(i<charArr.length){
								strB.append((charArr[i]+"").toUpperCase());
							}
						}else{
							strB.append((charArr[i]+"").toLowerCase());
						}
					}
				}
			}
		}
		return strB.toString();
	}
	
	
	public static String toLowerCaseFirst(String value){
		if(value==null||value.length()<=0){
			return value;
		}
		char[] valueChar=value.toCharArray();
		StringBuffer strB=new StringBuffer();
		for(int i=0;i<valueChar.length;i++){
			String str=null;
			if(i==0){
				str=(valueChar[i]+"").toLowerCase();
			}else{
				str=valueChar[i]+"";
			}
			strB.append(str);
		}
		return strB.toString();
	}
	
	public static String toUpperCaseFirst(String value){
		if(value==null||value.length()<=0){
			return value;
		}
		char[] valueChar=value.toCharArray();
		StringBuffer strB=new StringBuffer();
		for(int i=0;i<valueChar.length;i++){
			String str=null;
			if(i==0){
				str=(valueChar[i]+"").toUpperCase();
			}else{
				str=valueChar[i]+"";
			}
			strB.append(str);
		}
		return strB.toString();
	}
    
}
